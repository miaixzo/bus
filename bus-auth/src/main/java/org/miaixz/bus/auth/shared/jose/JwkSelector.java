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
package org.miaixz.bus.auth.shared.jose;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Selects JWK rotation candidates using an explicitly verified JOSE algorithm context.
 * <p>
 * Selection never infers an algorithm from key material and never treats successful cryptographic trial as algorithm
 * negotiation. Returned ordering is the source JWK Set ordering and carries no implicit preference.
 * </p>
 *
 * @author Kimi Liu
 */
public final class JwkSelector {

    /**
     * Creates a stateless JWK selector.
     */
    public JwkSelector() {
        // No initialization required.
    }

    /**
     * Tests all strong and optional-declaration constraints for one key.
     *
     * @param key       candidate JWK
     * @param selection explicit selection constraints
     * @param keyTypes  allowed key types derived from registration and request
     * @return whether the key remains a candidate
     */
    private static boolean matches(final Jwk key, final Selection selection, final Set<String> keyTypes) {
        if (!keyTypes.contains(key.keyType())) {
            return false;
        }
        if (selection.keyId().isPresent() && !key.keyId().filter(selection.keyId().getOrThrow()::equals).isPresent()) {
            return false;
        }
        if (key.algorithm().isPresent() && !selection.algorithm().equals(key.algorithm().getOrThrow())) {
            return false;
        }
        if (selection.publicKeyUse().isPresent() && key.publicKeyUse().isPresent()
                && !selection.publicKeyUse().getOrThrow().equals(key.publicKeyUse().getOrThrow())) {
            return false;
        }
        return selection.keyOperation().isEmpty() || key.keyOperations().isEmpty()
                || key.keyOperations().contains(selection.keyOperation().getOrThrow());
    }

    /**
     * Returns every candidate consistent with the explicit algorithm and optional key metadata.
     *
     * @param keySet    source JWK Set, including current and still-valid rotation keys
     * @param selection explicit selection constraints
     * @return immutable candidates in original JWK Set order
     */
    public List<Jwk> select(final JwkSet keySet, final Selection selection) {
        Assert.notNull(keySet, "JWK Set must not be null");
        Assert.notNull(selection, "JWK selection must not be null");
        final JwaAlgorithm.Registration registration = JwaAlgorithm.of(selection.algorithm()).require(selection.kind());
        final Set<String> keyTypes;
        if (selection.keyType().isPresent()) {
            if (!registration.keyTypes().contains(selection.keyType().getOrThrow())) {
                throw new ValidateException("Requested JWK key type is incompatible with the selected algorithm");
            }
            keyTypes = Set.of(selection.keyType().getOrThrow());
        } else {
            keyTypes = registration.keyTypes();
        }
        final List<Jwk> candidates = new ArrayList<>();
        for (Jwk key : keySet.keys()) {
            if (matches(key, selection, keyTypes)) {
                candidates.add(key);
            }
        }
        return List.copyOf(candidates);
    }

    /**
     * Requires selection to identify exactly one JWK.
     *
     * @param keySet    source JWK Set
     * @param selection explicit selection constraints
     * @return the sole matching key
     * @throws ValidateException if no key or more than one key matches
     */
    public Jwk requireUnique(final JwkSet keySet, final Selection selection) {
        final List<Jwk> candidates = select(keySet, selection);
        if (candidates.size() != 1) {
            throw new ValidateException("JWK selection did not resolve exactly one key");
        }
        return candidates.get(0);
    }

    /**
     * Captures one key selection request without deriving security-sensitive values from candidate keys.
     *
     * @param keyId        optional exact key identifier hint
     * @param algorithm    mandatory exact JOSE algorithm already selected by the message
     * @param kind         mandatory registered algorithm operation kind
     * @param publicKeyUse optional expected JWK use value
     * @param keyOperation optional expected JWK key_ops value
     * @param keyType      optional exact JWK key type restriction
     * @author Kimi Liu
     */
    public record Selection(Optional<String> keyId, String algorithm, JwaAlgorithm.Kind kind,
            Optional<String> publicKeyUse, Optional<String> keyOperation, Optional<String> keyType) {

        /**
         * Validates optional values and preserves exact case-sensitive identifiers.
         *
         * @throws IllegalArgumentException if a component is {@code null}
         * @throws ValidateException        if a present optional value or mandatory algorithm is blank
         */
        public Selection {
            Assert.notNull(keyId, "JWK selection key identifier must not be null");
            Assert.notBlank(algorithm, "JWK selection algorithm must not be blank");
            Assert.notNull(kind, "JWK selection algorithm kind must not be null");
            Assert.notNull(publicKeyUse, "JWK selection public key use must not be null");
            Assert.notNull(keyOperation, "JWK selection key operation must not be null");
            Assert.notNull(keyType, "JWK selection key type must not be null");
            validateOptional(keyId, "key identifier");
            validateOptional(publicKeyUse, "public key use");
            validateOptional(keyOperation, "key operation");
            validateOptional(keyType, "key type");
        }

        /**
         * Rejects a present but blank optional selection value.
         *
         * @param value optional candidate
         * @param label semantic diagnostic label
         */
        private static void validateOptional(final Optional<String> value, final String label) {
            if (value.filter(String::isBlank).isPresent()) {
                throw new ValidateException("JWK selection " + label + " must not be blank");
            }
        }

    }

}
