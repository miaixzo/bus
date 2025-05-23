/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org gitlab4j and other contributors.           ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.gitlab.models;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.gitlab.support.JacksonJson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class HealthCheckInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852377863011L;

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
        public HealthCheckItem deserialize(JsonParser jsonParser, DeserializationContext ctx) throws IOException {

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
