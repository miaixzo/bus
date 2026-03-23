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
import org.miaixz.bus.cortex.guard.token.AccessTokenResolver;
import org.miaixz.bus.cortex.guard.token.AccessToken;
import org.miaixz.bus.cortex.guard.token.AccessTokenStore;

/**
 * RBAC-based access control backed by CacheX.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AccessGuard {

    /**
     * Shared cache storing RBAC permission entries.
     */
    private final CacheX<String, Object> cacheX;
    /**
     * Token store used for privileged permission-management operations.
     */
    private final AccessTokenStore tokenStore;

    /**
     * Creates an AccessGuard with both CacheX and a AccessTokenStore for admin operations.
     *
     * @param cacheX     shared cache for RBAC permission entries
     * @param tokenStore token store used to validate admin tokens
     */
    public AccessGuard(CacheX<String, Object> cacheX, AccessTokenStore tokenStore) {
        this.cacheX = cacheX;
        this.tokenStore = tokenStore;
    }

    /**
     * Creates an AccessGuard without a AccessTokenStore (read-only permission checks).
     *
     * @param cacheX shared cache for RBAC permission entries
     */
    public AccessGuard(CacheX<String, Object> cacheX) {
        this(cacheX, null);
    }

    /**
     * Checks whether the given role has permission for the resource/action.
     *
     * @param role     role name
     * @param resource resource identifier
     * @param action   action name
     * @return true if permitted
     */
    public boolean hasPermission(String role, String resource, String action) {
        String key = Builder.SECURITY_PREFIX + "rbac:" + role + ":" + resource + ":" + action;
        Object raw = cacheX.read(key);
        return "true".equals(raw) || "1".equals(raw);
    }

    /**
     * Grants a permission if the caller has the ADMIN role.
     *
     * @param adminToken token of the caller
     * @param role       role to grant
     * @param resource   resource to grant access for
     * @param action     action to grant
     */
    public void grantPermission(String adminToken, String role, String resource, String action) {
        if (tokenStore == null) {
            throw new IllegalStateException("AccessTokenStore not configured");
        }
        AccessToken info = new AccessTokenResolver(tokenStore).parse(adminToken);
        if (info == null || !"ADMIN".equals(info.getRole())) {
            throw new SecurityException("Admin token required");
        }
        String key = Builder.SECURITY_PREFIX + "rbac:" + role + ":" + resource + ":" + action;
        cacheX.write(key, "true", 0L);
    }

}
