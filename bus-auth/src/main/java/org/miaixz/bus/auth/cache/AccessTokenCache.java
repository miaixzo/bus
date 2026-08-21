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

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.fabric.Clock;

/**
 * Stores the server-side validation state of an issued OAuth access token.
 * <p>
 * The backend key is an isolated irreversible token digest. The value contains only the Provider, client, subject,
 * authorization id, scope, audience, and optional sender-constraining confirmation required for validation. The
 * referenced {@link AuthorizationCache} entry is the lifecycle authority; this cache is only a token index and never
 * stores plaintext access tokens.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AccessTokenCache extends AuthCache<AccessTokenCache.Entry> {

    /**
     * Isolates access-token state from every other bus-cache consumer.
     */
    private static final String PURPOSE = "access-token";

    /**
     * Creates an access-token cache view backed entirely by bus-cache.
     *
     * @param cache      shared bus-cache backend
     * @param deployment deployment-unique cache namespace
     * @param clock      shared runtime clock used to derive entry lifetimes
     */
    public AccessTokenCache(final CacheX<String, Object> cache, final String deployment, final Clock clock) {
        super(cache, deployment, PURPOSE, Entry.class, clock);
    }

    /**
     * Stores access-token validation state when the digest key is absent.
     *
     * @param key   purpose-local irreversible token digest
     * @param value validation metadata and its absolute expiry instant
     * @return stage containing whether the token state was stored
     */
    public CompletionStage<Boolean> issue(final String key, final ExpiringValue<Entry> value) {
        return super.doIssue(key, value);
    }

    /**
     * Finds access-token validation state without consuming it.
     *
     * @param key purpose-local irreversible token digest
     * @return stage containing validation state or {@code null}
     */
    public CompletionStage<ExpiringValue<Entry>> find(final String key) {
        return super.doFind(key);
    }

    /**
     * Revokes access-token validation state by its digest key.
     *
     * @param key purpose-local irreversible token digest
     * @return stage containing whether token state was removed
     */
    public CompletionStage<Boolean> revoke(final String key) {
        return super.doRevoke(key);
    }

    /**
     * Carries immutable access-token validation metadata.
     *
     * @param providerId      OAuth Provider identifier
     * @param clientId        OAuth client identifier
     * @param subjectId       authorized subject identifier
     * @param authorizationId authoritative authorization identifier shared by derived credentials
     * @param scope           granted OAuth scope values
     * @param audience        intended resource server audience values
     * @param actorSubjectId  optional RFC 8693 acting-subject identifier for delegated token exchange
     * @param confirmation    optional safe sender-constraining key confirmation identifier
     * @param openIdBinding   optional OpenID Connect authorization context inherited from an authorization code
     * @author Kimi Liu
     */
    public record Entry(String providerId, String clientId, String subjectId, String authorizationId,
            List<String> scope, List<String> audience, Optional<String> actorSubjectId, Optional<String> confirmation,
            Optional<AuthorizationCodeCache.OpenIdBinding> openIdBinding) implements Serializable {

        /**
         * Creates immutable access-token validation metadata.
         *
         * @param providerId     OAuth Provider identifier
         * @param clientId       OAuth client identifier
         * @param subjectId      authorized subject identifier
         * @param scope          granted scope values
         * @param audience       resource audience values
         * @param actorSubjectId optional delegated acting-subject identifier
         * @param confirmation   optional sender confirmation identifier
         * @param openIdBinding  optional OpenID Connect authorization context
         * @throws IllegalArgumentException if required text is blank, a list contains a blank entry, or an optional
         *                                  container is invalid
         */
        public Entry {
            Assert.notBlank(providerId, "Access token Provider id must not be blank");
            Assert.notBlank(clientId, "Access token client id must not be blank");
            Assert.notBlank(subjectId, "Access token subject id must not be blank");
            Assert.notBlank(authorizationId, "Access token authorization id must not be blank");
            scope = immutableText(scope, "Access token scope");
            audience = immutableText(audience, "Access token audience");
            Assert.notNull(actorSubjectId, "Access token actor subject container must not be null");
            if (!actorSubjectId.isEmpty()) {
                Assert.notBlank(actorSubjectId.getOrNull(), "Access token actor subject id must not be blank");
            }
            actorSubjectId = Optional.ofNullable(actorSubjectId.getOrNull());
            Assert.notNull(confirmation, "Access token confirmation container must not be null");
            if (!confirmation.isEmpty()) {
                Assert.notBlank(confirmation.getOrNull(), "Access token confirmation must not be blank");
            }
            confirmation = Optional.ofNullable(confirmation.getOrNull());
            Assert.notNull(openIdBinding, "Access token OpenID Connect binding container must not be null");
            openIdBinding = Optional.ofNullable(openIdBinding.getOrNull());
        }

        /**
         * Validates and freezes a list of protocol text values.
         *
         * @param values protocol text list
         * @param label  safe list label for validation messages
         * @return immutable detached list
         */
        private static List<String> immutableText(final List<String> values, final String label) {
            Assert.notNull(values, label + " list must not be null");
            final List<String> copy = new ArrayList<>(values.size());
            for (String value : values) {
                copy.add(Assert.notBlank(value, label + " must not contain blank values"));
            }
            return List.copyOf(copy);
        }

    }

}
