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

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.Builder;

/**
 * Namespace isolation guard backed by CacheX.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class NamespaceGuard {

    /**
     * Shared cache storing namespace access rules.
     */
    private final CacheX<String, Object> cacheX;

    /**
     * Creates a NamespaceGuard backed by the given CacheX.
     *
     * @param cacheX shared cache used to store namespace access rules
     */
    public NamespaceGuard(CacheX<String, Object> cacheX) {
        this.cacheX = cacheX;
    }

    /**
     * Returns true if the role can access the given namespace.
     *
     * @param role      role name
     * @param namespace target namespace
     * @return true if access is allowed
     */
    public boolean canAccess(String role, String namespace) {
        if (Builder.DEFAULT_NAMESPACE.equals(namespace)) {
            return true;
        }
        String key = Builder.SECURITY_PREFIX + "ns:" + namespace + ":allow:" + role;
        Object raw = cacheX.read(key);
        return "1".equals(raw) || "true".equals(raw);
    }

    /**
     * Grants a role access to a namespace.
     *
     * @param namespace target namespace
     * @param role      role to allow
     */
    public void allowRole(String namespace, String role) {
        String key = Builder.SECURITY_PREFIX + "ns:" + namespace + ":allow:" + role;
        cacheX.write(key, "1", 0L);
    }

}
