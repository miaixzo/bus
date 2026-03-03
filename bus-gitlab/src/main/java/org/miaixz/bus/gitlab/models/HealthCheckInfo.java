/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.models;

import java.io.IOException;
import java.io.Serializable;

import org.miaixz.bus.gitlab.support.JacksonJson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serial;

public class HealthCheckInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852256691706L;

    @JsonDeserialize(using = HealthCheckItemDeserializer.class)
    private HealthCheckItem dbCheck;

    @JsonDeserialize(using = HealthCheckItemDeserializer.class)
    private HealthCheckItem redisCheck;

    @JsonDeserialize(using = HealthCheckItemDeserializer.class)
    private HealthCheckItem cacheCheck;

    @JsonDeserialize(using = HealthCheckItemDeserializer.class)
    private HealthCheckItem queuesCheck;

    @JsonDeserialize(using = HealthCheckItemDeserializer.class)
    private HealthCheckItem sharedStateCheck;

    @JsonDeserialize(using = HealthCheckItemDeserializer.class)
    private HealthCheckItem fsShardsCheck;

    @JsonDeserialize(using = HealthCheckItemDeserializer.class)
    private HealthCheckItem gitalyCheck;

    public HealthCheckItem getDbCheck() {
        return this.dbCheck;
    }

    public void setDbCheck(HealthCheckItem dbCheck) {
        this.dbCheck = dbCheck;
    }

    public HealthCheckItem getRedisCheck() {
        return this.redisCheck;
    }

    public void setRedisCheck(HealthCheckItem redisCheck) {
        this.redisCheck = redisCheck;
    }

    public HealthCheckItem getCacheCheck() {
        return this.cacheCheck;
    }

    public void setCacheCheck(HealthCheckItem cacheCheck) {
        this.cacheCheck = cacheCheck;
    }

    public HealthCheckItem getQueuesCheck() {
        return this.queuesCheck;
    }

    public void setQueuesCheck(HealthCheckItem queuesCheck) {
        this.queuesCheck = queuesCheck;
    }

    public HealthCheckItem getSharedStateCheck() {
        return this.sharedStateCheck;
    }

    public void setSharedStateCheck(HealthCheckItem sharedStateCheck) {
        this.sharedStateCheck = sharedStateCheck;
    }

    public HealthCheckItem getFsShardsCheck() {
        return this.fsShardsCheck;
    }

    public void setFsShardsCheck(HealthCheckItem fsShardsCheck) {
        this.fsShardsCheck = fsShardsCheck;
    }

    public HealthCheckItem getGitalyCheck() {
        return this.gitalyCheck;
    }

    public void setGitalyCheck(HealthCheckItem gitalyCheck) {
        this.gitalyCheck = gitalyCheck;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

    /**
     * This desrializer can deserialize on object containing a HealthCheckItem or an array containing a single
     * HealthCheckItem.
     */
    private static class HealthCheckItemDeserializer extends JsonDeserializer<HealthCheckItem> {

        private static final ObjectMapper mapper = new JacksonJson().getObjectMapper();

        @Override
        public HealthCheckItem deserialize(JsonParser jsonParser, DeserializationContext ctx)
                throws IOException, JsonProcessingException {

            HealthCheckItem healthCheckItem = null;
            JsonNode tree = jsonParser.readValueAsTree();
            if (tree.isArray()) {
                JsonNode node = tree.get(0);
                healthCheckItem = mapper.treeToValue(node, HealthCheckItem.class);
            } else if (tree.isObject()) {
                healthCheckItem = mapper.treeToValue(tree, HealthCheckItem.class);
            }

            return (healthCheckItem);
        }
    }

}
