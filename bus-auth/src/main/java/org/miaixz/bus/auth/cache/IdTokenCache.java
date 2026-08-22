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

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.FabricX.Clock;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Minimal irreversible ID Token index used to validate RP-initiated logout hints.
 *
 * @author Kimi Liu
 */
public class IdTokenCache extends AuthCache<IdTokenCache.Entry> {

    private static final String PURPOSE = "id-token";

    /**
     * Creates an ID Token binding view backed entirely by bus-cache.
     */
    public IdTokenCache(final CacheX<String, Object> cache, final String deployment, final Clock clock) {
        super(cache, deployment, PURPOSE, Entry.class, clock);
    }

    /**
     * Creates a Source-generation-scoped ID Token binding cache view for compiled runtime use.
     *
     * @param cache      shared bus-cache backend
     * @param deployment deployment-unique cache namespace
     * @param sourceId   exact Source registration identifier
     * @param generation non-negative Source configuration generation
     * @param clock      shared runtime clock used to derive entry lifetimes
     */
    public IdTokenCache(final CacheX<String, Object> cache, final String deployment, final String sourceId,
            final long generation, final Clock clock) {
        super(cache, deployment, PURPOSE, Entry.class, sourceId, generation, clock);
    }

    /**
     * Creates a binding, accepting only an exactly equal existing value as idempotent success.
     */
    public CompletionStage<Boolean> bind(final String digest, final ExpiringValue<Entry> value) {
        return doIssue(digest, value).thenCompose(
                created -> Boolean.TRUE.equals(created) ? CompletableFuture.completedFuture(true)
                        : doFind(digest).thenApply(existing -> Objects.equals(existing, value)));
    }

    /**
     * Finds one issued ID Token binding by irreversible digest.
     */
    public CompletionStage<ExpiringValue<Entry>> find(final String digest) {
        return doFind(digest);
    }

    /**
     * Revokes one issued ID Token binding.
     */
    public CompletionStage<Boolean> revoke(final String digest) {
        return doRevoke(digest);
    }

    /**
     * Derives a Source-isolated SHA-256 key without retaining the compact token.
     */
    public static String key(final String sourceId, final String compactIdToken) {
        return Builder.sha256Hex(
                Assert.notBlank(sourceId, "ID Token Source id must not be blank") + Symbol.C_NUL + PURPOSE
                        + Symbol.C_NUL + Assert.notBlank(compactIdToken, "Compact ID Token must not be blank"));
    }

    /**
     * Minimal logout binding for one issued ID Token.
     *
     * @author Kimi Liu
     */
    public record Entry(String sourceId, String consumerId, String subject, Optional<String> sessionId)
            implements Serializable {

        @Serial
        private static final long serialVersionUID = 2868923612053L;

        /**
         * Validates and freezes non-secret logout facts.
         */
        public Entry {
            Assert.notBlank(sourceId, "ID Token Source id must not be blank");
            Assert.notBlank(consumerId, "ID Token consumer id must not be blank");
            Assert.notBlank(subject, "ID Token subject must not be blank");
            Assert.notNull(sessionId, "ID Token session container must not be null");
            final String value = sessionId.getOrNull();
            sessionId = value == null ? Optional.empty()
                    : Optional.of(Assert.notBlank(value, "ID Token session id must not be blank"));
        }

    }

}
