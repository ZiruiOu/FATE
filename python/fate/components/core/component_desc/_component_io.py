import logging
import typing
from typing import List, Union

from fate.components.core.essential import Role, Stage

from ._component import Component

if typing.TYPE_CHECKING:
    from fate.arch import Context

    from ..spec.artifact import ArtifactInputApplySpec, ArtifactOutputApplySpec
    from ..spec.task import TaskConfigSpec
    from .artifacts._base_type import (
        AT,
        ArtifactDescribe,
        M,
        _ArtifactType,
        _ArtifactTypeReader,
    )

logger = logging.getLogger(__name__)


class ComponentInputDataApplied:
    def __init__(
        self, artifact_desc: "ArtifactDescribe", artifact_type: "_ArtifactType", reader: "_ArtifactTypeReader"
    ):
        self.artifact_desc = artifact_desc
        self.artifact_type = artifact_type
        self.reader = reader


class ComponentExecutionIO:
    def __init__(self, ctx: "Context", component: Component, role: Role, stage: Stage, config):
        self.parameter_artifacts_desc = {}
        self.parameter_artifacts_apply = {}
        self.input_artifacts = dict(data={}, model={})
        self.input_artifacts_reader = dict(data={}, model={})
        self.output_artifacts = dict(data={}, model={}, metric={})
        self.output_artifacts_writer = dict(data={}, model={}, metric={})
        logging.debug(f"parse and apply component artifacts")

        for arg in component.func_args[2:]:
            if not (
                self._handle_parameter(component, arg, config)
                or self._handle_input(ctx, component, arg, stage, role, config)
                or self._handle_output(ctx, component, arg, stage, role, config)
            ):
                raise ValueError(f"args `{arg}` not provided")

    def _handle_parameter(self, component, arg, config):
        if parameter := component.parameters.mapping.get(arg):
            apply_spec: ArtifactInputApplySpec = config.parameters.get(arg)
            applied_parameter = parameter.apply(apply_spec)
            logging.debug(f"apply parameter `{parameter.name}`: {parameter} -> {applied_parameter}")
            self.parameter_artifacts_apply[parameter.name] = applied_parameter
            return True
        return False

    def _handle_input(self, ctx, component, arg, stage, role, config):
        from fate.arch import URI

        from .artifacts._base_type import _ArtifactsType

        for input_type, artifacts in dict(
            data=component.artifacts.data_inputs,
            model=component.artifacts.model_inputs,
        ).items():
            if arti := artifacts.get(arg):
                if arti.is_active_for(stage, role):
                    apply_spec: Union[
                        ArtifactInputApplySpec, List[ArtifactInputApplySpec]
                    ] = config.input_artifacts.get(arg)
                    if apply_spec is not None:
                        arti = arti.get_correct_arti(apply_spec)
                        try:
                            if arti.multi:
                                readers = []
                                for c in apply_spec:
                                    uri = URI.from_string(c.uri)
                                    readers.append(arti.get_reader(ctx, uri, c.metadata))
                                self.input_artifacts[input_type][arg] = _ArtifactsType([r.artifact for r in readers])
                                self.input_artifacts_reader[input_type][arg] = readers
                            else:
                                uri = URI.from_string(apply_spec.uri)
                                reader = arti.get_reader(ctx, uri, apply_spec.metadata)
                                self.input_artifacts[input_type][arg] = reader.artifact
                                self.input_artifacts_reader[input_type][arg] = reader
                        except Exception as e:
                            raise ComponentArtifactApplyError(f"load as input artifact({arti}) error: {e}") from e
                    elif arti.optional:
                        self.input_artifacts_reader[input_type][arg] = None
                        self.input_artifacts[input_type][arg] = None
                    else:
                        raise ComponentArtifactApplyError(
                            f"load as input artifact({arti}) error: apply_config is None but not optional"
                        )
                    logging.debug(
                        f"apply {input_type} artifact `{arti.name}`: {apply_spec} -> {self.input_artifacts_reader[input_type][arg]}"
                    )
                    return True
                else:
                    logging.debug(f"skip {input_type} artifact `{arti.name}` for stage `{stage}` and role `{role}`")
        return False

    def _handle_output(self, ctx, component, arg, stage, role, config):
        from fate.arch import URI

        for output_type, artifacts in dict(
            data=component.artifacts.data_outputs,
            model=component.artifacts.model_outputs,
            metric=component.artifacts.metric_outputs,
        ).items():
            if arti := artifacts.get(arg):
                if arti.is_active_for(stage, role):
                    apply_spec: ArtifactOutputApplySpec = config.output_artifacts.get(arg)
                    if apply_spec is not None:
                        arti = arti.get_correct_arti(apply_spec)
                        try:
                            if arti.multi:
                                if not apply_spec.is_template():
                                    raise ComponentArtifactApplyError(
                                        "template uri required for multiple output artifact"
                                    )
                                writers = WriterGenerator(ctx, arti, apply_spec)
                                self.output_artifacts[output_type][arg] = writers.recorder
                                self.output_artifacts_writer[output_type][arg] = writers

                            else:
                                if apply_spec.is_template():
                                    raise ComponentArtifactApplyError(
                                        "template uri is not supported for non-multiple output artifact"
                                    )
                                writer = arti.get_writer(ctx, URI.from_string(apply_spec.uri))
                                self.output_artifacts[output_type][arg] = writer.artifact
                                self.output_artifacts_writer[output_type][arg] = writer
                        except Exception as e:
                            raise ComponentArtifactApplyError(f"load as output artifact({arti}) error: {e}") from e
                    elif arti.optional:
                        self.output_artifacts_writer[output_type][arg] = None
                        self.output_artifacts[output_type][arg] = None
                    else:
                        raise ComponentArtifactApplyError(
                            f"load as output artifact({arti}) error: apply_config is None but not optional"
                        )
                    logging.debug(
                        f"apply {output_type} artifact `{arti.name}`: {apply_spec} -> {self.output_artifacts_writer[output_type][arg]}"
                    )
                    return True
                else:
                    logging.debug(f"skip {output_type} artifact `{arti.name}` for stage `{stage}` and role `{role}`")
        return False

    def get_kwargs(self):
        return {
            **self.parameter_artifacts_apply,
            **self.input_artifacts_reader["data"],
            **self.input_artifacts_reader["model"],
            **self.output_artifacts_writer["data"],
            **self.output_artifacts_writer["model"],
            **self.output_artifacts_writer["metric"],
        }

    def dump_io_meta(self, config: "TaskConfigSpec") -> dict:
        from fate.components.core.spec.artifact import IOArtifactMeta

        def _get_meta(d, with_source=False):
            result = {}
            for k, arti_type in d.items():
                if arti_type is not None:
                    if with_source:
                        arti_type.update_source_metadata(config, k)
                    result[k] = arti_type.dict()
            return result

        io_meta = IOArtifactMeta(
            inputs=IOArtifactMeta.InputMeta(
                data=_get_meta(self.input_artifacts["data"]),
                model=_get_meta(self.input_artifacts["model"]),
            ),
            outputs=IOArtifactMeta.OutputMeta(
                data=_get_meta(self.output_artifacts["data"], with_source=True),
                model=_get_meta(self.output_artifacts["model"], with_source=True),
                metric=_get_meta(self.output_artifacts["metric"], with_source=True),
            ),
        )
        return io_meta.dict(exclude_none=True)


class WriterGenerator:
    def __init__(
        self, ctx: "Context", artifact_describe: "ArtifactDescribe[AT, M]", apply_config: "ArtifactOutputApplySpec"
    ):
        from .artifacts._base_type import _ArtifactsType

        self.ctx = ctx
        self.artifact_describe = artifact_describe
        self.apply_config = apply_config

        self.recorder = _ArtifactsType([])
        self.current = 0

    def get_recorder(self):
        return self.recorder

    def __iter__(self):
        return self

    def __next__(self):
        from fate.arch import URI

        uri = URI.from_string(self.apply_config.uri.format(index=self.current))
        writer = self.artifact_describe.get_writer(self.ctx, uri)
        self.recorder.artifacts.append(writer.artifact)
        self.current += 1
        return writer


class ComponentArtifactApplyError(RuntimeError):
    ...
