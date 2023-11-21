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

package org.fedai.osx.core.router;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.fedai.osx.core.context.Protocol;


@Data
public class RouterInfo {

    public static class  BooleanFilter{
        @Override
        public boolean equals(Object obj) {
//            System.err.println(obj.getClass());
            if(obj instanceof Boolean){


                boolean result=   !(Boolean)obj;
                System.err.println("ppppppp"+ result);
                return result;
            }
//            return false;
            return  true;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Protocol protocol;
    @JsonIgnore
    private String sourcePartyId;
    @JsonIgnore
    private String desPartyId;
    @JsonIgnore
    private String desRole;
    @JsonIgnore
    private String sourceRole;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String url;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonProperty("ip")
    private String host;
    private Integer port;
    @JsonInclude(value = JsonInclude.Include.CUSTOM,valueFilter = BooleanFilter.class)
    private boolean useSSL = false;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String certChainFile;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String privateKeyFile;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String caFile;
    @JsonInclude(value = JsonInclude.Include.CUSTOM,valueFilter = BooleanFilter.class)
    private boolean useKeyStore = false ;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String keyStoreFilePath;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String keyStorePassword;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String trustStoreFilePath;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String trustStorePassword;





    public String toKey() {
        StringBuffer sb = new StringBuffer();
        if (Protocol.grpc.equals(protocol)) {
            sb.append(host).append("_").append(port);
            if (useSSL)
                sb.append("_").append("tls");
        } else {
            sb.append(url);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toKey();
    }
    @JsonIgnore
    public String getResource() {
        StringBuilder sb = new StringBuilder();
        sb.append(sourcePartyId).append("-").append(desPartyId);
        return sb.toString();
    }


}