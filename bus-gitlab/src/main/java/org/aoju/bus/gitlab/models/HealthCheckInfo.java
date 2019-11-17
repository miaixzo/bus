package org.aoju.bus.gitlab.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.aoju.bus.gitlab.utils.JacksonJson;

import java.io.IOException;

public class HealthCheckInfo {

    @JsonDeserialize(using = HealthCheckItemDeserializer.class)
    private org.aoju.bus.gitlab.models.HealthCheckItem dbCheck;

    @JsonDeserialize(using = HealthCheckItemDeserializer.class)
    private org.aoju.bus.gitlab.models.HealthCheckItem redisCheck;

    @JsonDeserialize(using = HealthCheckItemDeserializer.class)
    private org.aoju.bus.gitlab.models.HealthCheckItem cacheCheck;

    @JsonDeserialize(using = HealthCheckItemDeserializer.class)
    private org.aoju.bus.gitlab.models.HealthCheckItem queuesCheck;

    @JsonDeserialize(using = HealthCheckItemDeserializer.class)
    private org.aoju.bus.gitlab.models.HealthCheckItem sharedStateCheck;

    @JsonDeserialize(using = HealthCheckItemDeserializer.class)
    private org.aoju.bus.gitlab.models.HealthCheckItem fsShardsCheck;

    @JsonDeserialize(using = HealthCheckItemDeserializer.class)
    private org.aoju.bus.gitlab.models.HealthCheckItem gitalyCheck;

    public org.aoju.bus.gitlab.models.HealthCheckItem getDbCheck() {
        return this.dbCheck;
    }

    public void setDbCheck(org.aoju.bus.gitlab.models.HealthCheckItem dbCheck) {
        this.dbCheck = dbCheck;
    }

    public org.aoju.bus.gitlab.models.HealthCheckItem getRedisCheck() {
        return this.redisCheck;
    }

    public void setRedisCheck(org.aoju.bus.gitlab.models.HealthCheckItem redisCheck) {
        this.redisCheck = redisCheck;
    }

    public org.aoju.bus.gitlab.models.HealthCheckItem getCacheCheck() {
        return this.cacheCheck;
    }

    public void setCacheCheck(org.aoju.bus.gitlab.models.HealthCheckItem cacheCheck) {
        this.cacheCheck = cacheCheck;
    }

    public org.aoju.bus.gitlab.models.HealthCheckItem getQueuesCheck() {
        return this.queuesCheck;
    }

    public void setQueuesCheck(org.aoju.bus.gitlab.models.HealthCheckItem queuesCheck) {
        this.queuesCheck = queuesCheck;
    }

    public org.aoju.bus.gitlab.models.HealthCheckItem getSharedStateCheck() {
        return this.sharedStateCheck;
    }

    public void setSharedStateCheck(org.aoju.bus.gitlab.models.HealthCheckItem sharedStateCheck) {
        this.sharedStateCheck = sharedStateCheck;
    }

    public org.aoju.bus.gitlab.models.HealthCheckItem getFsShardsCheck() {
        return this.fsShardsCheck;
    }

    public void setFsShardsCheck(org.aoju.bus.gitlab.models.HealthCheckItem fsShardsCheck) {
        this.fsShardsCheck = fsShardsCheck;
    }

    public org.aoju.bus.gitlab.models.HealthCheckItem getGitalyCheck() {
        return this.gitalyCheck;
    }

    public void setGitalyCheck(org.aoju.bus.gitlab.models.HealthCheckItem gitalyCheck) {
        this.gitalyCheck = gitalyCheck;
    }

    @Override
    public String toString() {
        return (JacksonJson.toJsonString(this));
    }

    /**
     * This desrializer can deserialize on object containing a HealthCheckItem or an
     * array containing a single HealthCheckItem.
     */
    private static class HealthCheckItemDeserializer extends JsonDeserializer<org.aoju.bus.gitlab.models.HealthCheckItem> {

        private static final ObjectMapper mapper = new JacksonJson().getObjectMapper();

        @Override
        public org.aoju.bus.gitlab.models.HealthCheckItem deserialize(JsonParser jsonParser, DeserializationContext ctx)
                throws IOException, JsonProcessingException {

            org.aoju.bus.gitlab.models.HealthCheckItem healthCheckItem = null;
            JsonNode tree = jsonParser.readValueAsTree();
            if (tree.isArray()) {
                JsonNode node = tree.get(0);
                healthCheckItem = mapper.treeToValue(node, org.aoju.bus.gitlab.models.HealthCheckItem.class);
            } else if (tree.isObject()) {
                healthCheckItem = mapper.treeToValue(tree, HealthCheckItem.class);
            }

            return (healthCheckItem);
        }
    }
}
