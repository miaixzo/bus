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
package org.miaixz.bus.auth.cache;

import org.miaixz.bus.core.lang.Assert;

/**
 * Stores one-time OpenID Connect nonce bindings independently from callback state.
 * <p>
 * Keys are irreversible digests isolated by namespace, Source, and nonce purpose. A successful ID Token validation
 * consumes the value with the inherited atomic take operation so the same nonce cannot establish identity twice.
 * </p>
 *
 * @author Kimi Liu
 */
public interface NonceStore extends AtomicStore<String, ExpiringValue<NonceStore.Nonce>> {

    /**
     * Binds an opaque OpenID Connect nonce to the Source that issued it.
     *
     * @param sourceId registered OpenID Connect Source identifier
     * @param nonce    opaque nonce value expected in the ID Token
     * @author Kimi Liu
     */
    record Nonce(String sourceId, String nonce) {

        /**
         * Creates an immutable Source nonce binding.
         *
         * @param sourceId registered Source identifier
         * @param nonce    opaque nonce value
         * @throws IllegalArgumentException if either value is blank
         */
        public Nonce {
            Assert.notBlank(sourceId, "Nonce Source id must not be blank");
            Assert.notBlank(nonce, "OpenID Connect nonce must not be blank");
        }

    }

}
