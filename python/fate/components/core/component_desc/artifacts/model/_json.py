#
#  Copyright 2019 The FATE Authors. All Rights Reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

import datetime
import json
import typing
from pathlib import Path

from fate.components.core.essential import JsonModelArtifactType

from .._base_type import (
    URI,
    ArtifactDescribe,
    Metadata,
    ModelOutputMetadata,
    _ArtifactType,
    _ArtifactTypeReader,
    _ArtifactTypeWriter,
)

if typing.TYPE_CHECKING:
    from fate.arch import Context


class JsonModelWriter(_ArtifactTypeWriter[ModelOutputMetadata]):
    def write(self, data, metadata: dict = None):
        self.artifact.consumed()
        if not hasattr(self, "_has_write"):
            setattr(self, "_has_write", True)
        else:
            raise RuntimeError(f"json model writer {self.artifact} has been written, cannot write again")

        path = Path(self.artifact.uri.path)
        path.parent.mkdir(parents=True, exist_ok=True)
        with path.open("w") as fw:
            json.dump(data, fw)
        if metadata is None:
            metadata = {}
        self.artifact.metadata.metadata = metadata

        # update model overview
        from fate.components.core.spec.model import MLModelModelSpec

        model_overview = self.artifact.metadata.model_overview
        model_overview.party.models.append(
            MLModelModelSpec(
                name="",
                created_time=datetime.datetime.now().isoformat(),
                file_format="json",
                metadata=metadata,
            )
        )


class JsonModelReader(_ArtifactTypeReader):
    def read(self):
        self.artifact.consumed()
        try:
            with open(self.artifact.uri.path, "r") as fr:
                return json.load(fr)
        except Exception as e:
            raise RuntimeError(f"load json model named from {self.artifact} failed: {e}")


class JsonModelArtifactDescribe(ArtifactDescribe[JsonModelArtifactType, ModelOutputMetadata]):
    @classmethod
    def get_type(cls):
        return JsonModelArtifactType

    def get_writer(self, config, ctx: "Context", uri: URI, type_name: str) -> JsonModelWriter:
        return JsonModelWriter(ctx, _ArtifactType(uri=uri, metadata=ModelOutputMetadata(), type_name=type_name))

    def get_reader(self, ctx: "Context", uri: URI, metadata: Metadata, type_name: str) -> JsonModelReader:
        return JsonModelReader(ctx, _ArtifactType(uri=uri, metadata=metadata, type_name=type_name))
