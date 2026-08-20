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
package org.miaixz.bus.auth.resolver;

import java.security.Key;
import java.time.Instant;
import java.util.concurrent.CompletionStage;

import org.miaixz.bus.auth.Context;
import org.miaixz.bus.auth.Outcome;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.shared.jose.JwkSet;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Resolves one explicitly selected, time-valid cryptographic key from external key storage.
 * <p>
 * Query values preserve the lexical identifiers defined by their owning standard. The resolver returns one exact result
 * rather than an ordered candidate list, preventing key or algorithm guessing. Rotation remains supported because an
 * older key can be resolved by its explicit identifier while its validity window is still active.
 * </p>
 *
 * @author Kimi Liu
 */
public interface KeyResolver {

    /**
     * Resolves one exact key for the supplied issuer, identifier, use, algorithm, and time.
     *
     * @param request immutable exact key query
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing a successful key, expected rejection, or operational failure
     */
    CompletionStage<Outcome<ResolvedKey>> resolve(Query request, Context context, Timeout.Budget timeout);

    /**
     * Resolves the complete active public JWK Set for one issuer and key use from the same inventory as execution keys.
     *
     * @param request immutable issuer, key-use, and validity-time query
     * @param context immutable non-secret invocation context
     * @param timeout shared end-to-end operation budget
     * @return stage containing a public-only JWK Set, expected rejection, or operational failure
     */
    CompletionStage<Outcome<JwkSet>> resolvePublicJwkSet(PublicQuery request, Context context, Timeout.Budget timeout);

    /**
     * Carries exact standard lookup coordinates for one cryptographic key.
     *
     * @param issuer    trusted issuer or authority lexical value
     * @param keyId     optional explicit standard key identifier
     * @param use       exact standard key-use lexical value
     * @param algorithm exact standard algorithm lexical value
     * @param at        validity instant
     * @author Kimi Liu
     */
    record Query(String issuer, Optional<String> keyId, String use, String algorithm, Instant at) {

        /**
         * Creates an immutable exact key query.
         *
         * @param issuer    trusted issuer or authority lexical value
         * @param keyId     optional explicit standard key identifier
         * @param use       exact standard key-use lexical value
         * @param algorithm exact standard algorithm lexical value
         * @param at        validity instant
         * @throws IllegalArgumentException if required text is blank or another component is {@code null}
         * @throws ValidateException        if a present key identifier is blank
         */
        public Query {
            Assert.notBlank(issuer, "Key query issuer must not be blank");
            Assert.notNull(keyId, "Key query identifier container must not be null");
            final String identifier = keyId.getOrNull();
            if (identifier != null && identifier.isBlank()) {
                throw new ValidateException("Key query identifier must not be blank when present");
            }
            keyId = Optional.ofNullable(identifier);
            Assert.notBlank(use, "Key query use must not be blank");
            Assert.notBlank(algorithm, "Key query algorithm must not be blank");
            Assert.notNull(at, "Key query validity instant must not be null");
        }

    }

    /**
     * Selects the public publication view of active keys for one issuer and standard key use.
     *
     * @param issuer trusted issuer or authority lexical value
     * @param use    exact standard key-use lexical value
     * @param at     validity instant
     * @author Kimi Liu
     */
    record PublicQuery(String issuer, String use, Instant at) {

        /**
         * Creates an immutable public JWK Set query.
         *
         * @param issuer trusted issuer or authority
         * @param use    standard key-use value
         * @param at     requested validity instant
         * @throws IllegalArgumentException if text is blank or the instant is {@code null}
         */
        public PublicQuery {
            Assert.notBlank(issuer, "Public JWK Set query issuer must not be blank");
            Assert.notBlank(use, "Public JWK Set query use must not be blank");
            Assert.notNull(at, "Public JWK Set query validity instant must not be null");
        }

    }

    /**
     * Carries one exact externally resolved JCA key and its validity interval.
     *
     * @param keyId     exact resolved key identifier
     * @param algorithm exact resolved standard algorithm identifier
     * @param key       usable JCA key object
     * @param notBefore inclusive key validity start
     * @param notAfter  exclusive key validity end
     * @author Kimi Liu
     */
    record ResolvedKey(String keyId, String algorithm, Key key, Instant notBefore, Instant notAfter) {

        /**
         * Creates an immutable resolved key view.
         *
         * @param keyId     exact resolved key identifier
         * @param algorithm exact resolved standard algorithm identifier
         * @param key       usable JCA key object
         * @param notBefore inclusive validity start
         * @param notAfter  exclusive validity end
         * @throws IllegalArgumentException if text is blank or another component is {@code null}
         * @throws ValidateException        if the validity interval is empty or reversed
         */
        public ResolvedKey {
            Assert.notBlank(keyId, "Resolved key identifier must not be blank");
            Assert.notBlank(algorithm, "Resolved key algorithm must not be blank");
            Assert.notNull(key, "Resolved JCA key must not be null");
            Assert.notNull(notBefore, "Resolved key not-before instant must not be null");
            Assert.notNull(notAfter, "Resolved key not-after instant must not be null");
            if (!notBefore.isBefore(notAfter)) {
                throw new ValidateException("Resolved key validity interval must be positive");
            }
        }

    }

}
