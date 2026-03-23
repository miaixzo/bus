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
package org.miaixz.bus.cortex.magic;

import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Structured audit logger backed by CacheX.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AuditLogger {

    /**
     * Shared cache used to persist audit log entries.
     */
    private final CacheX<String, Object> cacheX;

    /**
     * Creates an AuditLogger backed by the given CacheX.
     *
     * @param cacheX shared cache used to persist audit events
     */
    public AuditLogger(CacheX<String, Object> cacheX) {
        this.cacheX = cacheX;
    }

    /**
     * Records an audit event for the given operation.
     *
     * @param namespace logical namespace
     * @param operation operation name
     * @param id        resource identifier
     * @param operator  actor performing the operation
     */
    public void log(String namespace, String operation, String id, String operator) {
        String key = Builder.AUDIT_PREFIX + namespace + ":" + operation + ":" + id;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("op", operation);
        payload.put("id", id);
        payload.put("operator", operator);
        payload.put("ts", System.currentTimeMillis());
        cacheX.write(key, JsonKit.toJsonString(payload), 7 * 24 * 3600_000L);
    }

}
