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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.FabricX.Clock;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;

/**
 * Stores OAuth refresh-token family state for rotation and reuse detection.
 * <p>
 * Each backend key is an isolated irreversible refresh-token digest. Atomic replace advances one token generation,
 * while {@link AuthorizationCache} is the authoritative lifecycle shared by the complete family and its access tokens.
 * Plaintext refresh tokens are never retained in this value.
 * </p>
 *
 * @author Kimi Liu
 */
public class RefreshTokenCache extends AuthCache<RefreshTokenCache.Entry> {

    /**
     * Isolates refresh-token state from every other bus-cache consumer.
     */
    private static final String PURPOSE = "refresh-token";

    /**
     * Creates a refresh-token cache view backed entirely by bus-cache.
     *
     * @param cache      shared bus-cache backend
     * @param deployment deployment-unique cache namespace
     * @param clock      shared runtime clock used to derive entry lifetimes
     */
    public RefreshTokenCache(final CacheX<String, Object> cache, final String deployment, final Clock clock) {
        super(cache, deployment, PURPOSE, Entry.class, clock);
    }

    /**
     * Creates a Source-generation-scoped refresh-token cache view for compiled runtime use.
     *
     * @param cache      shared bus-cache backend
     * @param deployment deployment-unique cache namespace
     * @param sourceId   exact Source registration identifier
     * @param generation non-negative Source configuration generation
     * @param clock      shared runtime clock used to derive entry lifetimes
     */
    public RefreshTokenCache(final CacheX<String, Object> cache, final String deployment, final String sourceId,
            final long generation, final Clock clock) {
        super(cache, deployment, PURPOSE, Entry.class, sourceId, generation, clock);
    }

    /**
     * Stores a new refresh-token generation.
     *
     * @param key   token digest
     * @param value state and expiry
     * @return creation stage
     */
    public CompletionStage<Boolean> issue(final String key, final ExpiringValue<Entry> value) {
        return super.doIssue(key, value);
    }

    /**
     * Finds one refresh-token generation.
     *
     * @param key token digest
     * @return stored state stage
     */
    public CompletionStage<ExpiringValue<Entry>> find(final String key) {
        return super.doFind(key);
    }

    /**
     * Atomically rotates one exact token generation.
     *
     * @param key      token digest
     * @param expected current state
     * @param update   replacement state
     * @return replacement stage
     */
    public CompletionStage<Boolean> rotate(
            final String key,
            final ExpiringValue<Entry> expected,
            final ExpiringValue<Entry> update) {
        return super.doUpdate(key, expected, update);
    }

    /**
     * Revokes one refresh-token generation.
     *
     * @param key token digest
     * @return removal stage
     */
    public CompletionStage<Boolean> revoke(final String key) {
        return super.doRevoke(key);
    }

    /**
     * Enumerates the lifecycle state of one refresh-token generation.
     *
     * @author Kimi Liu
     */
    public enum Status {

        /**
         * Token may be used for one successful rotation.
         */
        ACTIVE,

        /**
         * Token was successfully exchanged and must not be accepted again.
         */
        ROTATED,

        /**
         * Token or its family was administratively or automatically revoked.
         */
        REVOKED,

        /**
         * A previously rotated token was presented again and family reuse was detected.
         */
        REUSED

    }

    /**
     * Carries immutable refresh-token family and sender-binding state.
     *
     * @param providerId    OAuth Provider identifier
     * @param clientId      OAuth client identifier
     * @param subjectId     authorized subject identifier
     * @param familyId      stable identifier shared by all rotations in one family
     * @param generation    non-negative rotation generation
     * @param scope         scope retained by this refresh token
     * @param audience      intended resource audience retained across rotation
     * @param confirmation  optional safe sender-constraining confirmation identifier
     * @param openIdBinding optional OpenID Connect authorization context retained across family rotation
     * @param status        current token lifecycle status
     * @author Kimi Liu
     */
    public record Entry(String providerId, String clientId, String subjectId, String familyId, long generation,
            List<String> scope, List<String> audience, Optional<String> confirmation,
            Optional<AuthorizationCodeCache.OpenIdBinding> openIdBinding, Status status) implements Serializable {

        /**
         * Creates immutable refresh-token state.
         *
         * @param providerId    OAuth Provider identifier
         * @param clientId      OAuth client identifier
         * @param subjectId     authorized subject identifier
         * @param familyId      rotation family identifier
         * @param generation    non-negative family generation
         * @param scope         retained scope values
         * @param audience      retained intended resource audience
         * @param confirmation  optional sender confirmation identifier
         * @param openIdBinding optional OpenID Connect authorization context
         * @param status        current lifecycle status
         * @throws IllegalArgumentException if required text is blank, generation is negative, scope is invalid, or a
         *                                  container is {@code null}
         */
        public Entry {
            Assert.notBlank(providerId, "Refresh token Provider id must not be blank");
            Assert.notBlank(clientId, "Refresh token client id must not be blank");
            Assert.notBlank(subjectId, "Refresh token subject id must not be blank");
            Assert.notBlank(familyId, "Refresh token family id must not be blank");
            Assert.isTrue(generation >= 0, "Refresh token generation must not be negative");
            Assert.notNull(scope, "Refresh token scope must not be null");
            final List<String> scopeCopy = new ArrayList<>(scope.size());
            for (String value : scope) {
                scopeCopy.add(Assert.notBlank(value, "Refresh token scope must not contain blank values"));
            }
            scope = List.copyOf(scopeCopy);
            Assert.notNull(audience, "Refresh token audience must not be null");
            final List<String> audienceCopy = new ArrayList<>(audience.size());
            for (String value : audience) {
                audienceCopy.add(Assert.notBlank(value, "Refresh token audience must not contain blank values"));
            }
            audience = List.copyOf(audienceCopy);
            Assert.notNull(confirmation, "Refresh token confirmation container must not be null");
            if (!confirmation.isEmpty()) {
                Assert.notBlank(confirmation.getOrNull(), "Refresh token confirmation must not be blank");
            }
            confirmation = Optional.ofNullable(confirmation.getOrNull());
            Assert.notNull(openIdBinding, "Refresh token OpenID Connect binding container must not be null");
            openIdBinding = Optional.ofNullable(openIdBinding.getOrNull());
            Assert.notNull(status, "Refresh token status must not be null");
        }

    }

}
