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
package org.miaixz.bus.cortex.guard.token;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * HMAC-SHA256 token issuance and validation backed by CacheX.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AccessTokenStore {

    /**
     * Shared cache used to persist issued token metadata.
     */
    private final CacheX<String, Object> cacheX;
    /**
     * Secret key used to generate and verify HMAC signatures.
     */
    private final String hmacSecret;
    /**
     * Token lifetime in seconds for newly issued credentials.
     */
    private final long tokenExpireSeconds;

    /**
     * Returns the underlying CacheX instance used to store tokens.
     *
     * @return the CacheX instance
     */
    public CacheX<String, Object> getCacheX() {
        return cacheX;
    }

    /**
     * Creates an AccessTokenStore with the given CacheX backend, HMAC secret and expiry.
     *
     * @param cacheX             shared cache used to store token metadata
     * @param hmacSecret         secret key for HMAC-SHA256 signing
     * @param tokenExpireSeconds token lifetime in seconds
     */
    public AccessTokenStore(CacheX<String, Object> cacheX, String hmacSecret, long tokenExpireSeconds) {
        this.cacheX = cacheX;
        this.hmacSecret = hmacSecret;
        this.tokenExpireSeconds = tokenExpireSeconds;
    }

    /**
     * Issues a new token for the given subject, role and namespace.
     *
     * @param subject   principal identifier
     * @param role      role name
     * @param namespace target namespace
     * @return signed token string
     */
    public String issue(String subject, String role, String namespace) {
        long expire = System.currentTimeMillis() + tokenExpireSeconds * 1000L;
        String payload = subject + ":" + role + ":" + namespace + ":" + expire;
        String token = org.miaixz.bus.crypto.Builder.hmacSha256(hmacSecret).digestHex(payload);
        String key = Builder.SECURITY_PREFIX + "token:" + token;
        AccessToken info = new AccessToken();
        info.setSubject(subject);
        info.setRole(role);
        info.setNamespace(namespace);
        info.setExpire(expire);
        cacheX.write(key, JsonKit.toJsonString(info), tokenExpireSeconds * 1000L);
        return token;
    }

    /**
     * Validates a token by HMAC re-verification and expiry check.
     *
     * @param token token to validate
     * @return true if valid, false otherwise
     */
    public boolean validate(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        try {
            String key = Builder.SECURITY_PREFIX + "token:" + token;
            Object raw = cacheX.read(key);
            if (raw == null) {
                return false;
            }
            AccessToken info = JsonKit.toPojo((String) raw, AccessToken.class);
            if (info == null) {
                return false;
            }
            if (System.currentTimeMillis() > info.getExpire()) {
                cacheX.remove(key);
                return false;
            }
            String expectedPayload = info.getSubject() + ":" + info.getRole() + ":" + info.getNamespace() + ":"
                    + info.getExpire();
            String expected = org.miaixz.bus.crypto.Builder.hmacSha256(hmacSecret).digestHex(expectedPayload);
            return expected.equals(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Revokes a token by removing it from the store.
     *
     * @param token token to revoke
     */
    public void revoke(String token) {
        if (token != null) {
            cacheX.remove(Builder.SECURITY_PREFIX + "token:" + token);
        }
    }

}
