/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fedai.osx.core.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.fedai.osx.core.constant.DeployMode;
import org.fedai.osx.core.constant.Dict;
import org.fedai.osx.core.constant.StreamLimitMode;
import org.fedai.osx.core.exceptions.ConfigErrorException;
import org.fedai.osx.core.utils.JsonUtil;
import org.fedai.osx.core.utils.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetaInfo {

    static Logger logger = LoggerFactory.getLogger(MetaInfo.class);
    @Config(confKey = "user.home")
    public static String PROPERTY_USER_HOME = System.getProperty("user.home");
    @Config(confKey = "fate.tech.provider")
    public static String PROPERTY_FATE_TECH_PROVIDER = "FATE";
    @Config(confKey = "default.client.version")
    public static String PROPERTY_DEFAULT_CLIENT_VERSION = "2.X.X";
    @Config(confKey = "grpc.server.max.concurrent.call.per.connection", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_SERVER_MAX_CONCURRENT_CALL_PER_CONNECTION = 1000;
    @Config(confKey = "grpc.server.max.inbound.metadata.size", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_SERVER_MAX_INBOUND_METADATA_SIZE = 128 << 20;
    @Config(confKey = "grpc.server.max.inbound.message.size", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_SERVER_MAX_INBOUND_MESSAGE_SIZE = (2 << 30) - 1;
    @Config(confKey = "grpc.server.flow.control.window", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_SERVER_FLOW_CONTROL_WINDOW = 128 << 20;
    @Config(confKey = "grpc.server.keepalive.time.sec", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_SERVER_KEEPALIVE_TIME_SEC = 7200;
    @Config(confKey = "grpc.server.keepalive.timeout.sec", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_SERVER_KEEPALIVE_TIMEOUT_SEC = 3600;
    @Config(confKey = "grpc.server.permit.keepalive.time.sec", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_SERVER_PERMIT_KEEPALIVE_TIME_SEC = 10;
    @Config(confKey = "grpc.server.keepalive.without.calls.enabled", pattern = Dict.BOOLEAN_PATTERN)
    public static Boolean PROPERTY_GRPC_SERVER_KEEPALIVE_WITHOUT_CALLS_ENABLED = true;
    @Config(confKey = "grpc.server.max.connection.idle.sec", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_SERVER_MAX_CONNECTION_IDLE_SEC = 86400;
    @Config(confKey = "grpc.server.max.connection.age.sec", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_SERVER_MAX_CONNECTION_AGE_SEC = 86400;
    @Config(confKey = "grpc.server.max.connection.age.grace.sec", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_SERVER_MAX_CONNECTION_AGE_GRACE_SEC = 86400;
    @Config(confKey = "grpc.oncompleted.wait.timeout", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_ONCOMPLETED_WAIT_TIMEOUT = 600;
    @Config(confKey = "grpc.client.max.inbound.message.size", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_CLIENT_MAX_INBOUND_MESSAGE_SIZE = (2 << 30) - 1;
    @Config(confKey = "grpc.client.flow.control.window", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_CLIENT_FLOW_CONTROL_WINDOW = 128 << 20;
    @Config(confKey = "grpc.client.keepalive.time", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_CLIENT_KEEPALIVE_TIME_SEC = 7200;
    @Config(confKey = "grpc.client.keepalive.timeout", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_CLIENT_KEEPALIVE_TIMEOUT_SEC = 3600;
    @Config(confKey = "grpc.client.keepalive.without.calls.enabled", pattern = Dict.BOOLEAN_PATTERN)
    public static Boolean PROPERTY_GRPC_CLIENT_KEEPALIVE_WITHOUT_CALLS_ENABLED = true;
    @Config(confKey = "grpc.client.max.connection.idle", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_CLIENT_MAX_CONNECTION_IDLE_SEC = 86400;
    @Config(confKey = "grpc.client.per.rpc.buffer.limit", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_CLIENT_PER_RPC_BUFFER_LIMIT =  (2 << 30) - 1;
    @Config(confKey = "grpc.client.retry.buffer.size", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_CLIENT_RETRY_BUFFER_SIZE = 86400;
    @Config(confKey = "transfer.cached.msgid.size", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_TRANSFER_CACHED_MSGID_SIZE = 10;
    @Config(confKey = "grpc.ssl.session.timeout", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_SSL_SESSION_TIME_OUT = 3600 << 4;
    @Config(confKey = "grpc.ssl.open.client.validate", pattern = Dict.BOOLEAN_PATTERN)
    public  static Boolean  PROPERTY_GRPC_SSL_OPEN_CLIENT_VALIDATE=  false;
    @Config(confKey = "grpc.ssl.session.cache.size", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_HTTP_SSL_SESSION_CACHE_SIZE = 65536;
    @Config(confKey = "mapped.file.expire.time", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_MAPPED_FILE_EXPIRE_TIME = 3600 * 1000 * 36;
    @Config(confKey = "mapped.file.size", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer MAP_FILE_SIZE = 1 << 27;
    @Config(confKey = "mapped.file.dir")
    public static String PROPERTY_TRANSFER_FILE_PATH_PRE = "mapped"+ File.separator+".fate"+ File.separator+"transfer_file";
    @Config(confKey = "index.mapped.file.size", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_INDEX_MAP_FILE_SIZE = 1 << 21;
    @Config(confKey = "https.server.cert.chain.file")
    public static String PROPERTY_HTTPS_SERVER_CERT_CHAIN_FILE;
    @Config(confKey = "https.server.private.key.file")
    public static String PROPERTY_HTTPS_SERVER_PRIVATE_KEY_FILE;
    @Config(confKey = "https.server.ca.file")
    public static String PROPERTY_HTTPS_SERVER_CA_FILE;
    @Config(confKey = "https.server.keystore.file")
    public static String PROPERTY_HTTPS_SERVER_KEYSTORE_FILE = "";
    @Config(confKey = "https.server.keystore.file.password")
    public static String PROPERTY_HTTPS_SERVER_KEYSTORE_FILE_PASSWORD = "";
    @Config(confKey = "https.server.trust.keystore.file")
    public static String PROPERTY_HTTPS_SERVER_TRUST_KEYSTORE_FILE = "";
    @Config(confKey = "https.client.trust.keystore.file.password")
    public static String PROPERTY_HTTPS_SERVER_TRUST_FILE_PASSWORD = "";
    @Config(confKey = "https.server.hostname.verifier.skip")
    public static boolean PROPERTY_HTTPS_HOSTNAME_VERIFIER_SKIP = true;
    @Config(confKey = "grpc.server.cert.chain.file")
    public static String PROPERTY_GRPC_SERVER_CERT_CHAIN_FILE;
    @Config(confKey = "grpc.server.private.key.file")
    public static String PROPERTY_GRPC_SERVER_PRIVATE_KEY_FILE;
    @Config(confKey = "grpc.server.ca.file")
    public static String PROPERTY_GRPC_SERVER_CA_FILE;
    @Config(confKey = "grpc.server.keystore.file")
    public static String PROPERTY_GRPC_SERVER_KEYSTORE_FILE;
    @Config(confKey = "grpc.server.keystore.file.password")
    public static String PROPERTY_GRPC_SERVER_KEYSTORE_FILE_PASSWORD;
    @Config(confKey = "grpc.server.trust.keystore.file")
    public static String PROPERTY_GRPC_SERVER_TRUST_KEYSTORE_FILE;
    @Config(confKey = "grpc.server.trust.keystore.file.password")
    public static String PROPERTY_GRPC_SERVER_TRUST_FILE_PASSWORD;

    @Config(confKey = "custom.local.host")
    public static String PROPERTY_CUSTOMER_LOCAL_HOST;
    @Config(confKey = "bind.host")
    public static String PROPERTY_BIND_HOST = "0.0.0.0";
    @Config(confKey = "open.grpc.tls.server", pattern = Dict.BOOLEAN_PATTERN)
    public static Boolean PROPERTY_OPEN_GRPC_TLS_SERVER = false;

    @Config(confKey = "grpc.port", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_PORT = 9370;
    @Config(confKey = "grpc.tls.port", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_TLS_PORT = 9371;
    @Config(confKey = "grpc.tls.session.timeout", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_TLS_SESSION_TIMEOUT = 3600 << 4;
    @Config(confKey = "grpc.tls.session.size", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_GRPC_TLS_SESSION_SIZE = 65536;
    @Config(confKey = "use.remote.health.check", pattern = Dict.BOOLEAN_PATTERN)
    public static Boolean PROPERTY_USE_REMOTE_HEALTH_CHECK = true;
    @Config(confKey = "http.port", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_HTTP_PORT=8807;
    @Config(confKey = "https.port", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_HTTPS_PORT=8809;
    @Config(confKey = "open.http.server", pattern = Dict.BOOLEAN_PATTERN)
    public static Boolean PROPERTY_OPEN_HTTP_SERVER = true;
    @Config(confKey = "open.https.server", pattern = Dict.BOOLEAN_PATTERN)
    public static Boolean PROPERTY_HTTP_USE_TLS = false;
    @Config(confKey = "http.server.acceptor.num", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_HTTP_SERVER_ACCEPTOR_NUM = 10;
    @Config(confKey = "http.server.selector.num", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_HTTP_SERVER_SELECTOR_NUM = 1;
    @Config(confKey = "http.ssl.trust.store.type")
    public static String PROPERTY_HTTP_SSL_TRUST_STORE_TYPE = "PKCS12";
    @Config(confKey = "http.ssl.trust.store.provider")
    public static String PROPERTY_HTTP_SSL_TRUST_STORE_PROVIDER = "SUN";
    @Config(confKey = "http.ssl.client.key.store.alias")
    public static String PROPERTY_HTTP_SSL_CLIENT_KEY_STORE_ALIAS = "";
    @Config(confKey = "http.ssl.server.key.store.alias")
    public static String PROPERTY_HTTP_SSL_SERVER_KEY_STORE_ALIAS = "";
    @Config(confKey = "http.ssl.hostname.verify")
    public static Boolean PROPERTY_HTTP_SSL_HOSTNAME_VERIFY = false;
    @Config(confKey = "http.context.path")
    public static String PROPERTY_HTTP_CONTEXT_PATH = "/v1";
    @Config(confKey = "http.servlet.path")
    public static String PROPERTY_HTTP_SERVLET_PATH = "/*";
    @Config(confKey = "http.receive.queue.size", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_HTTP_RECEIVE_QUEUE_SIZE = 36;
    @Config(confKey = "http.accept.receive.buffer.size", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_HTTP_ACCEPT_RECEIVE_BUFFER_SIZE = 4096;
    @Config(confKey = "consume.msg.waiting.timeout")
    public static Integer CONSUME_MSG_WAITING_TIMEOUT = 60 * 60 * 1000;
    @Config(confKey = "flow.control.sample.count", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_FLOW_CONTROL_SAMPLE_COUNT = 10;
    @Config(confKey = "flow.control.sample.interval", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_FLOW_CONTROL_SAMPLE_INTERVAL = 1000;
    public static String PROPERTY_DEPLOY_MODE = DeployMode.standalone.name();
    public static Set<String> PROPERTY_SELF_PARTY = Sets.newHashSet();//
    @Config(confKey = "flow.rule")
    public static String PROPERTY_FLOW_RULE_TABLE = "broker/flowRule.json";
    @Config(confKey = "open.route.cycle.checker", pattern = Dict.BOOLEAN_PATTERN)
    public static Boolean PROPERTY_OPEN_ROUTE_CYCLE_CHECKER = false;
    @Config(confKey = "zookeeper.acl.enable", pattern = Dict.BOOLEAN_PATTERN)
    public static Boolean PROPERTY_ACL_ENABLE = false;
    @Config(confKey = "zookeeper.acl.username")
    public static String PROPERTY_ACL_USERNAME;
    @Config(confKey = "zookeeper.acl.password")
    public static String PROPERTY_ACL_PASSWORD;
    @Config(confKey = "queue.max.free.time", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_QUEUE_MAX_FREE_TIME = 60 * 60 * 1000 * 12;
    @Config(confKey = "queue.check.interval", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_TRANSFER_QUEUE_CHECK_INTERVAL = 5*60 * 1000;
    public static String INSTANCE_ID = NetUtils.getLocalHost() + "_" + MetaInfo.PROPERTY_GRPC_PORT;
    @Config(confKey = "flow.print.uri", pattern = Dict.BOOLEAN_PATTERN)
    public static Boolean PROPERTY_PRINT_URI = false;
    @Config(confKey = "eggroll.cluster.manager.ip")
    public static String PROPERTY_EGGROLL_CLUSTER_MANANGER_IP;
    @Config(confKey = "eggroll.cluster.manager.port", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_EGGROLL_CLUSTER_MANANGER_PORT;
    // @Config(confKey = "http.client.method.config")
    public static Map<String, Map<String, Integer>> PROPERTY_HTTP_CLIENT_METHOD_CONFIG_MAP = new HashMap<>();
    /**
     * 从连接池中申请连接的超时时间
     */
    @Config(confKey = "http.client.con.req.timeout", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_HTTP_CLIENT_CONFIG_CONN_REQ_TIME_OUT = 500;
    /**
     * 建立连接的超时时间
     */
    @Config(confKey = "http.client.connection.timeout", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_HTTP_CLIENT_CONFIG_CONN_TIME_OUT = 10000;
    @Config(confKey = "http.client.max.idle.time", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_HTTP_CLIENT_MAX_IDLE_TIME = 5;
    /**
     * 等待数据
     */
    @Config(confKey = "http.client.socket.timeout", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_HTTP_CLIENT_CONFIG_SOCK_TIME_OUT = 300000;
    @Config(confKey = "http.ssl.session.timeout", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_HTTP_SSL_SESSION_TIME_OUT = 3600 << 4;
    @Config(confKey = "http.client.pool.max.total", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_HTTP_CLIENT_INIT_POOL_MAX_TOTAL = 500;
    @Config(confKey = "http.client.pool.max.per.router", pattern = Dict.POSITIVE_INTEGER_PATTERN)
    public static Integer PROPERTY_HTTP_CLIENT_INIT_POOL_DEF_MAX_PER_ROUTE = 200;
    @Config(confKey = "open.token.validator", pattern = Dict.BOOLEAN_PATTERN)
    public static Boolean PROPERTY_OPEN_TOKEN_VALIDATOR = false;
    @Config(confKey = "open.token.generator", pattern = Dict.BOOLEAN_PATTERN)
    public static Boolean PROPERTY_OPEN_TOKEN_GENERATOR = false;
    public static String PROPERTY_TOKEN_GENERATOR_CONFIG_PATH;
    public static String PROPERTY_CONFIG_DIR;
    @Config(confKey = "protocol.params.print", pattern = Dict.BOOLEAN_PATTERN)
    public static Boolean PROTOCOL_PARAMS_PRINT = false;
    @Config(confKey = "mock.eggpair.ip")
    public static String PROPERTY_MOCK_EGGPAIR_IP;
    @Config(confKey = "mock.eggpair.port")
    public static Integer PROPERTY_MOCK_EGGPAIR_PORT;
    @Config(confKey = "mock.eggpair.partyid")
    public static String PROPERTY_MOCK_EGGPAIR_PARTYID;
    @Config(confKey = "max.transfer.queue.size")
    public static Integer PROPERTY_MAX_TRANSFER_QUEUE_SIZE = Integer.MAX_VALUE;
    @Config(confKey = "max.queue.lock.live.time")
    public static Integer PROPERTY_MAX_QUEUE_LOCK_LIVE = 600;
    @Config(confKey = "open.mock.eggpair")
    public static Boolean PROPERTY_OPEN_MOCK_EGGPAIR = false;
    @Config(confKey = "router.check.interval")
    public static Integer PROPERTY_ROUTER_CHECK_INTERVAL= 300000;
    @Config(confKey = "channel.pool.info")
    public static Integer PROPERTY_CHANNEL_POOL_INFO = 30000;
    @Config(confKey = "router.change.need.token")
    public static Boolean PROPERTY_ROUTER_CHANGE_NEED_TOKEN= false;
    @Config(confKey = "router.change.token.validator")
    public static String PROPERTY_ROUTER_CHANGE_TOKEN_VALIDATOR= Dict.DEFAULT;
    @Config(confKey = "batch.sink.push.executor.timeout")
    public static Integer BATCH_SINK_PUSH_EXECUTOR_TIMEOUT = 60*1000;


    public static boolean isCluster() {
        return PROPERTY_DEPLOY_MODE.equals(DeployMode.cluster.name());
    }

    public static boolean checkPattern(String pattern, String value) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(value);
        if (m.find()) {
            return true;
        } else {
            return false;
        }
    }

    public static void init(Properties environment) {
        Field[] fields = MetaInfo.class.getFields();
        Arrays.stream(fields).forEach(field -> {
            try {
                Config config = field.getDeclaredAnnotation(Config.class);
                if (config != null) {
                    Class clazz = field.getType();
                    String confKey = config.confKey();
                    Object value = environment.get(confKey);
                    if (value != null) {
                        String pattern = config.pattern();
                        if (StringUtils.isNotEmpty(pattern) && !checkPattern(pattern, value.toString())) {
                            logger.error("conf {} has wrong value {},please check config file", confKey, value);
                            throw new ConfigErrorException("conf " + confKey + " has wrong value : " + value);
                        }
                        if (clazz == Integer.class) {
                            field.set(null, Integer.parseInt(value.toString()));
                        } else if (clazz == Long.class) {
                            field.set(null, Long.parseLong(value.toString()));
                        } else if (clazz == String.class) {
                            field.set(null, value.toString());

                        } else if (clazz == Boolean.class) {
                            field.set(null, Boolean.valueOf(value.toString()));
                        } else if (clazz.isAssignableFrom(Set.class)) {
                            Set set = new HashSet();
                            set.addAll(Lists.newArrayList(value.toString().split(",")));
                            field.set(null, set);
                        } else if (clazz.isAssignableFrom(Map.class)) {

                            Map<String, Map<String, Integer>> conConfig = JsonUtil.object2Objcet(value, new TypeReference<Map<String, Map<String, Integer>>>() {
                            });
                            field.set(null, conConfig);
                        }
                    }
                    if (StringUtils.isNotEmpty(confKey)) {
                        logger.info("{}={} ", confKey, field.get(null));
                    }
                }
            } catch (Exception e) {
                logger.error("parse config error", e);
            }
        });
    }
    public static Map toMap() {
        Map result = Maps.newHashMap();
        Field[] fields = MetaInfo.class.getFields();

        for (Field field : fields) {
            try {
                if (field.get(MetaInfo.class) != null) {
                    String key = Dict.class.getField(field.getName()) != null ? String.valueOf(Dict.class.getField(field.getName()).get(Dict.class)) : field.getName();
                    result.put(key, field.get(MetaInfo.class));
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {

            }
        }
        return result;
    }
}
