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

import java.io.Serializable;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.FabricX.Clock;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Assert;

/**
 * Stores one-time OpenID Connect nonce bindings independently from callback state.
 * <p>
 * Keys are irreversible digests isolated by space, Source, and nonce purpose. A successful ID Token validation consumes
 * the value with the inherited atomic take operation so the same nonce cannot establish identity twice.
 * </p>
 *
 * @author Kimi Liu
 */
public class NonceCache extends AuthCache<NonceCache.Nonce> {

    /**
     * Isolates nonce state from every other bus-cache consumer.
     */
    private static final String PURPOSE = "nonce";

    /**
     * Creates a nonce cache view backed entirely by bus-cache.
     *
     * @param cache      shared bus-cache backend
     * @param deployment deployment-unique cache scope
     * @param clock      shared runtime clock used to derive entry lifetimes
     */
    public NonceCache(final CacheX<String, Object> cache, final String deployment, final Clock clock) {
        super(cache, deployment, PURPOSE, Nonce.class, clock);
    }

    /**
     * Creates a Source-generation-scoped nonce cache view for compiled runtime use.
     *
     * @param cache      shared bus-cache backend
     * @param deployment deployment-unique cache scope
     * @param sourceId   exact Source registration identifier
     * @param generation non-negative Source configuration generation
     * @param clock      shared runtime clock used to derive entry lifetimes
     */
    public NonceCache(final CacheX<String, Object> cache, final String deployment, final String sourceId,
            final long generation, final Clock clock) {
        super(cache, deployment, PURPOSE, Nonce.class, sourceId, generation, clock);
    }

    /**
     * Stores a one-time nonce binding when the digest key is absent.
     *
     * @param key   purpose-local nonce digest
     * @param value nonce binding and expiry instant
     * @return stage containing whether the nonce was stored
     */
    public CompletionStage<Boolean> issue(final String key, final ExpiringValue<Nonce> value) {
        return super.doIssue(key, value);
    }

    /**
     * Atomically consumes a one-time nonce binding.
     *
     * @param key purpose-local nonce digest
     * @return stage containing the consumed nonce or {@code null}
     */
    public CompletionStage<ExpiringValue<Nonce>> consume(final String key) {
        return super.doConsume(key);
    }

    /**
     * Removes a nonce that must no longer be accepted.
     *
     * @param key purpose-local nonce digest
     * @return stage containing whether the nonce was removed
     */
    public CompletionStage<Boolean> discard(final String key) {
        return super.doRevoke(key);
    }

    /**
     * Binds an opaque OpenID Connect nonce to the Source that issued it.
     *
     * @param sourceId registered OpenID Connect Source identifier
     * @param nonce    opaque nonce value expected in the ID Token
     * @author Kimi Liu
     */
    public record Nonce(String sourceId, String nonce) implements Serializable {

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
