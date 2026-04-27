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
import java.util.Comparator;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.magic.identity.CortexIdentity;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Structured audit logger backed by CacheX.
 *
 * @author Kimi Liu
 * @since Java 21+
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
        log(namespace, operation, id, operator, Map.of());
    }

    /**
     * Records an audit event with optional structured details.
     *
     * @param namespace logical namespace
     * @param operation operation name
     * @param id        resource identifier
     * @param operator  actor performing the operation
     * @param details   optional detail payload
     */
    public void log(String namespace, String operation, String id, String operator, Map<String, Object> details) {
        long now = DateKit.current();
        String key = auditPrefix(namespace, operation, id) + ":" + now + ":" + Long.toHexString(System.nanoTime());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("op", operation);
        payload.put("id", id);
        payload.put("operator", operator);
        payload.put("ts", now);
        if (details != null && !details.isEmpty()) {
            payload.put("details", new LinkedHashMap<>(details));
        }
        cacheX.write(key, JsonKit.toJsonString(payload), 7 * 24 * 3600_000L);
    }

    /**
     * Loads one audit entry from the backing cache.
     *
     * @param namespace namespace
     * @param operation operation
     * @param id        resource identifier
     * @return serialized audit payload or {@code null}
     */
    public String get(String namespace, String operation, String id) {
        Map<String, Object> entries = cacheX.scan(auditPrefix(namespace, operation, id) + ":");
        if (entries == null || entries.isEmpty()) {
            return null;
        }
        String latestKey = entries.keySet().stream().max(Comparator.naturalOrder()).orElse(null);
        Object payload = latestKey == null ? null : entries.get(latestKey);
        return payload == null ? null : payload.toString();
    }

    /**
     * Builds the common audit-key prefix shared by all records of the same resource.
     *
     * @param namespace namespace
     * @param operation operation
     * @param id        resource identifier
     * @return audit-key prefix
     */
    private String auditPrefix(String namespace, String operation, String id) {
        return Builder.AUDIT_PREFIX + CortexIdentity.namespace(namespace) + ":" + operation + ":" + id;
    }

}
