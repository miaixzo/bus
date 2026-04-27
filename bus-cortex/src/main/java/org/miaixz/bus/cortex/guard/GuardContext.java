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
package org.miaixz.bus.cortex.guard;

import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.cortex.magic.identity.CortexIdentity;

import lombok.Getter;
import lombok.Setter;

/**
 * Input context passed to guard strategies.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class GuardContext {

    /**
     * Creates an empty guard evaluation context.
     */
    public GuardContext() {
    }

    /**
     * Namespace identifier of the protected asset.
     */
    private String namespace_id;
    /**
     * Protected domain such as registry, setting, or version.
     */
    private String domain;
    /**
     * Protected action such as publish, delete, or rollback.
     */
    private String action;
    /**
     * Protected resource type.
     */
    private String resourceType;
    /**
     * Protected resource identifier.
     */
    private String resourceId;

    /**
     * Logical asset identifier.
     */
    private String assetId;
    /**
     * Application identifier when available.
     */
    private String app_id;
    /**
     * Profile identifier when available.
     */
    private String profile_id;

    /**
     * Resolved access-control policy.
     */
    private GuardPolicy policy;

    /**
     * Token credential presented by the caller, when available.
     */
    private String token;

    /**
     * API key credential presented by the caller, when available.
     */
    private String apiKey;
    /**
     * Caller user identifier when already resolved upstream.
     */
    private String userId;
    /**
     * Caller tenant identifier when already resolved upstream.
     */
    private String tenantId;

    /**
     * Caller-supplied labels or request attributes used by policy evaluators.
     */
    private Map<String, Object> attributes;

    /**
     * Sets the namespace identifier of the protected asset.
     *
     * @param namespace_id namespace identifier
     */
    public void namespace_id(String namespace_id) {
        this.namespace_id = CortexIdentity.namespace(namespace_id);
    }

    /**
     * Returns one custom attribute by key.
     *
     * @param key attribute key
     * @return attribute value, or {@code null} when absent
     */
    public Object getAttribute(String key) {
        return attributes == null ? null : attributes.get(key);
    }

    /**
     * Adds one attribute entry and lazily creates the attribute map.
     *
     * @param key   attribute key
     * @param value attribute value
     * @return current context for fluent use
     */
    public GuardContext putAttribute(String key, Object value) {
        if (attributes == null) {
            attributes = new LinkedHashMap<>();
        }
        attributes.put(key, value);
        return this;
    }

}
