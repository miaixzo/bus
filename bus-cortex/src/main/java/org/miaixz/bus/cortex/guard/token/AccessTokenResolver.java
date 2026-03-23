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

import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Parses and validates tokens from incoming requests.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AccessTokenResolver {

    /**
     * Token store used to validate tokens and load their metadata.
     */
    private final AccessTokenStore tokenStore;

    /**
     * Creates an AccessTokenResolver backed by the given AccessTokenStore.
     *
     * @param tokenStore the token store for validation and lookup
     */
    public AccessTokenResolver(AccessTokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    /**
     * Parses a token string and returns the associated AccessToken if valid.
     *
     * @param token raw token string
     * @return AccessToken if valid, null otherwise
     */
    public AccessToken parse(String token) {
        if (!tokenStore.validate(token)) {
            return null;
        }
        String key = Builder.SECURITY_PREFIX + "token:" + token;
        Object raw = tokenStore.getCacheX().read(key);
        if (raw == null) {
            return null;
        }
        return JsonKit.toPojo((String) raw, AccessToken.class);
    }

}
