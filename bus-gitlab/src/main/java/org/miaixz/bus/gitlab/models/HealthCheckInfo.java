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

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.gitlab.support.JacksonJson;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * The health check info class.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
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

    /**
     * Returns the db check.
     *
     * @return the result
     */

    public HealthCheckItem getDbCheck() {
        return this.dbCheck;
    }

    /**
     * Sets the db check.
     *
     * @param dbCheck the db check value
     */

    public void setDbCheck(HealthCheckItem dbCheck) {
        this.dbCheck = dbCheck;
    }

    /**
     * Returns the redis check.
     *
     * @return the result
     */

    public HealthCheckItem getRedisCheck() {
        return this.redisCheck;
    }

    /**
     * Sets the redis check.
     *
     * @param redisCheck the redis check value
     */

    public void setRedisCheck(HealthCheckItem redisCheck) {
        this.redisCheck = redisCheck;
    }

    /**
     * Returns the cache check.
     *
     * @return the result
     */

    public HealthCheckItem getCacheCheck() {
        return this.cacheCheck;
    }

    /**
     * Sets the cache check.
     *
     * @param cacheCheck the cache check value
     */

    public void setCacheCheck(HealthCheckItem cacheCheck) {
        this.cacheCheck = cacheCheck;
    }

    /**
     * Returns the queues check.
     *
     * @return the result
     */

    public HealthCheckItem getQueuesCheck() {
        return this.queuesCheck;
    }

    /**
     * Sets the queues check.
     *
     * @param queuesCheck the queues check value
     */

    public void setQueuesCheck(HealthCheckItem queuesCheck) {
        this.queuesCheck = queuesCheck;
    }

    /**
     * Returns the shared state check.
     *
     * @return the result
     */

    public HealthCheckItem getSharedStateCheck() {
        return this.sharedStateCheck;
    }

    /**
     * Sets the shared state check.
     *
     * @param sharedStateCheck the shared state check value
     */

    public void setSharedStateCheck(HealthCheckItem sharedStateCheck) {
        this.sharedStateCheck = sharedStateCheck;
    }

    /**
     * Returns the fs shards check.
     *
     * @return the result
     */

    public HealthCheckItem getFsShardsCheck() {
        return this.fsShardsCheck;
    }

    /**
     * Sets the fs shards check.
     *
     * @param fsShardsCheck the fs shards check value
     */

    public void setFsShardsCheck(HealthCheckItem fsShardsCheck) {
        this.fsShardsCheck = fsShardsCheck;
    }

    /**
     * Returns the gitaly check.
     *
     * @return the result
     */

    public HealthCheckItem getGitalyCheck() {
        return this.gitalyCheck;
    }

    /**
     * Sets the gitaly check.
     *
     * @param gitalyCheck the gitaly check value
     */

    public void setGitalyCheck(HealthCheckItem gitalyCheck) {
        this.gitalyCheck = gitalyCheck;
    }

    /**
     * Returns the string.
     *
     * @return the result
     */

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

    /**
     * This desrializer can deserialize on object containing a HealthCheckItem or an array containing a single
     * HealthCheckItem.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static class HealthCheckItemDeserializer extends ValueDeserializer<HealthCheckItem> {

        /**
         * Executes the deserialize operation.
         *
         * @param jsonParser the json parser value
         * @param ctx        the ctx value
         * @return the result
         * @throws JacksonException if the operation fails
         */

        @Override
        public HealthCheckItem deserialize(JsonParser jsonParser, DeserializationContext ctx) throws JacksonException {

            HealthCheckItem healthCheckItem = null;
            JsonNode tree = jsonParser.readValueAsTree();
            if (tree.isArray()) {
                JsonNode node = tree.get(0);
                healthCheckItem = ctx.readTreeAsValue(node, HealthCheckItem.class);
            } else if (tree.isObject()) {
                healthCheckItem = ctx.readTreeAsValue(tree, HealthCheckItem.class);
            }

            return (healthCheckItem);
        }

    }

}
