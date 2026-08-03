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
package org.miaixz.bus.starter;

/**
 * Centralizes the property prefixes used by Starter feature configuration.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class GeniusBuilder {

    /**
     * Configuration prefix for auth.
     */
    public static final String AUTH = "bus.auth";
    /**
     * Configuration prefix for cache.
     */
    public static final String CACHE = "bus.cache";
    /**
     * Configuration prefix for context.
     */
    public static final String CONTEXT = "bus.context";
    /**
     * Configuration prefix for cors.
     */
    public static final String CORS = "bus.cors";
    /**
     * Configuration prefix for cortex.
     */
    public static final String CORTEX = "bus.cortex";
    /**
     * Configuration prefix for compatible Bus datasource definitions.
     */
    public static final String DATASOURCE = "bus.datasource";
    /**
     * Configuration prefix for dubbo.
     */
    public static final String DUBBO = "bus.dubbo";
    /**
     * Configuration prefix for elastic.
     */
    public static final String ELASTIC = "bus.elastic";
    /**
     * Configuration prefix for fabric.
     */
    public static final String FABRIC = "bus.fabric";
    /**
     * Configuration prefix for health.
     */
    public static final String HEALTH = "bus.health";
    /**
     * Configuration prefix for internationalization.
     */
    public static final String I18N = "bus.i18n";
    /**
     * Configuration prefix for image.
     */
    public static final String IMAGE = "bus.image";
    /**
     * Configuration prefix for json.
     */
    public static final String JSON = "bus.json";
    /**
     * Configuration prefix for limiter.
     */
    public static final String LIMITER = "bus.limiter";
    /**
     * Configuration prefix for mapper.
     */
    public static final String MAPPER = "bus.mapper";
    /**
     * Configuration prefix for metrics.
     */
    public static final String METRICS = "bus.metrics";
    /**
     * Configuration prefix for mongo.
     */
    public static final String MONGO = "bus.mongo";
    /**
     * Configuration prefix for notify.
     */
    public static final String NOTIFY = "bus.notify";
    /**
     * Configuration prefix for office.
     */
    public static final String OFFICE = "bus.office";
    /**
     * Configuration prefix for pay.
     */
    public static final String PAY = "bus.pay";
    /**
     * Configuration prefix for sensitive.
     */
    public static final String SENSITIVE = "bus.sensitive";
    /**
     * Configuration prefix for storage.
     */
    public static final String STORAGE = "bus.storage";
    /**
     * Configuration prefix for tempus.
     */
    public static final String TEMPUS = "bus.tempus";
    /**
     * Configuration prefix for tracer.
     */
    public static final String TRACER = "bus.tracer";
    /**
     * Configuration prefix for validate.
     */
    public static final String VALIDATE = "bus.validate";
    /**
     * Configuration prefix for vortex.
     */
    public static final String VORTEX = "bus.vortex";
    /**
     * Configuration prefix for wrapper.
     */
    public static final String WRAPPER = "bus.wrapper";
    /**
     * Configuration prefix for wrapper body cache.
     */
    public static final String WRAPPER_BODY_CACHE = WRAPPER + ".body-cache";
    /**
     * Configuration prefix for wrapper message converters.
     */
    public static final String WRAPPER_MESSAGE_CONVERTERS = WRAPPER + ".message-converters";
    /**
     * Configuration prefix for wrapper request binding.
     */
    public static final String WRAPPER_REQUEST_BINDING = WRAPPER + ".request-binding";
    /**
     * Configuration prefix for wrapper response advice.
     */
    public static final String WRAPPER_RESPONSE_ADVICE = WRAPPER + ".response-advice";
    /**
     * Configuration prefix for wrapper route prefix.
     */
    public static final String WRAPPER_ROUTE_PREFIX = WRAPPER + ".route-prefix";
    /**
     * Configuration prefix for zookeeper.
     */
    public static final String ZOOKEEPER = "bus.zookeeper";

    /**
     * Prevents instantiation of this constants holder.
     */
    private GeniusBuilder() {
        // No initialization required.
    }

}
