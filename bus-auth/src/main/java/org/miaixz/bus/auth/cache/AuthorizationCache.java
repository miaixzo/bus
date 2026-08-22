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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.crypto.Builder;

/**
 * Stores the authoritative lifecycle of one OAuth authorization shared by all derived token indexes.
 * <p>
 * Access and refresh token caches map opaque token digests to this authorization identifier. Revocation and refresh
 * reuse update this single record atomically before derived token indexes are cleaned, so partial cleanup cannot leave
 * a token valid. This class delegates storage and atomicity to bus-cache and contains no project business data.
 * </p>
 *
 * @author Kimi Liu
 */
public class AuthorizationCache extends AuthCache<AuthorizationCache.Entry> {

    /**
     * Isolates authorization lifecycle state from every other bus-cache consumer.
     */
    private static final String PURPOSE = "authorization";

    /**
     * Creates an authorization lifecycle cache view backed entirely by bus-cache.
     *
     * @param cache      shared bus-cache backend
     * @param deployment deployment-unique cache namespace
     * @param clock      shared runtime clock used to derive entry lifetimes
     */
    public AuthorizationCache(final CacheX<String, Object> cache, final String deployment, final Clock clock) {
        super(cache, deployment, PURPOSE, Entry.class, clock);
    }

    /**
     * Creates a Source-generation-scoped authorization lifecycle cache view for compiled runtime use.
     *
     * @param cache      shared bus-cache backend
     * @param deployment deployment-unique cache namespace
     * @param sourceId   exact Source registration identifier
     * @param generation non-negative Source configuration generation
     * @param clock      shared runtime clock used to derive entry lifetimes
     */
    public AuthorizationCache(final CacheX<String, Object> cache, final String deployment, final String sourceId,
            final long generation, final Clock clock) {
        super(cache, deployment, PURPOSE, Entry.class, sourceId, generation, clock);
    }

    /**
     * Stores a new authoritative authorization lifecycle when its key is absent.
     *
     * @param key   Provider-isolated authorization digest
     * @param value lifecycle state and its absolute expiry instant
     * @return stage containing whether the lifecycle was stored
     */
    public CompletionStage<Boolean> issue(final String key, final ExpiringValue<Entry> value) {
        return super.doIssue(key, value);
    }

    /**
     * Finds an authoritative authorization lifecycle without changing it.
     *
     * @param key Provider-isolated authorization digest
     * @return stage containing lifecycle state or {@code null}
     */
    public CompletionStage<ExpiringValue<Entry>> find(final String key) {
        return super.doFind(key);
    }

    /**
     * Atomically replaces an exact authorization lifecycle value.
     *
     * @param key      Provider-isolated authorization digest
     * @param expected exact current lifecycle value
     * @param update   replacement lifecycle value
     * @return stage containing whether replacement succeeded
     */
    public CompletionStage<Boolean> update(
            final String key,
            final ExpiringValue<Entry> expected,
            final ExpiringValue<Entry> update) {
        return super.doUpdate(key, expected, update);
    }

    /**
     * Removes an authorization lifecycle.
     *
     * @param key Provider-isolated authorization digest
     * @return stage containing whether the lifecycle was removed
     */
    public CompletionStage<Boolean> delete(final String key) {
        return super.doRevoke(key);
    }

    /**
     * Produces the irreversible Provider-isolated key shared by token issuance and validation paths.
     *
     * @param providerId      authorization-server Source identifier
     * @param authorizationId random internal authorization identifier
     * @return hexadecimal SHA-256 cache key
     */
    public static String key(final String providerId, final String authorizationId) {
        return Builder.sha256Hex(
                Assert.notBlank(providerId, "Authorization Provider id must not be blank") + Symbol.C_NUL
                        + "authorization" + Symbol.C_NUL
                        + Assert.notBlank(authorizationId, "Authorization id must not be blank"));
    }

    /**
     * Represents the security state shared by all credentials derived from one authorization.
     *
     * @author Kimi Liu
     */
    public enum Status {

        /**
         * Authorization and all derived credentials remain active.
         */
        ACTIVE,

        /**
         * Authorization was explicitly revoked.
         */
        REVOKED,

        /**
         * Authorization was invalidated after credential reuse or another compromise signal.
         */
        COMPROMISED

    }

    /**
     * Carries the minimal client-bound authoritative authorization state.
     *
     * @param providerId owning authorization-server Source
     * @param clientId   authorized OAuth client
     * @param status     current authorization lifecycle state
     *
     * @author Kimi Liu
     */
    public record Entry(String providerId, String clientId, Status status) implements Serializable {

        /**
         * Validates the minimal authoritative authorization lifecycle state.
         */
        public Entry {
            Assert.notBlank(providerId, "Authorization Provider id must not be blank");
            Assert.notBlank(clientId, "Authorization client id must not be blank");
            Assert.notNull(status, "Authorization status must not be null");
        }

    }

}
