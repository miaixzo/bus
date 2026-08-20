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
package org.miaixz.bus.auth.guard;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Set;

import javax.crypto.SecretKey;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.builtin.asymmetric.KeyType;

/**
 * Validates an explicitly selected protocol algorithm, allowlist, key type, and cryptographic usage.
 * <p>
 * Algorithm identifiers remain the exact strings registered by JOSE, XML Signature, OAuth, or another owning standard.
 * A profile maps those identifiers to Bus cryptographic implementations; this guard neither infers an algorithm from a
 * key nor performs signing, verification, encryption, or message authentication.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AlgorithmGuard {

    /**
     * Creates a stateless algorithm guard.
     */
    public AlgorithmGuard() {
        // No initialization required.
    }

    /**
     * Validates the runtime JCA key interface against the Bus key category declared by the profile.
     *
     * @param key             selected JCA key
     * @param requiredKeyType required Bus key category
     * @throws ValidateException if the key does not implement the required JCA interface
     */
    private static void validateKeyType(final Key key, final KeyType requiredKeyType) {
        final boolean compatible = switch (requiredKeyType) {
            case PublicKey -> key instanceof PublicKey;
            case PrivateKey -> key instanceof PrivateKey;
            case SecretKey -> key instanceof SecretKey;
        };
        if (!compatible) {
            throw new ValidateException("Cryptographic key does not match the algorithm profile key type");
        }
    }

    /**
     * Rejects key directions that cannot perform the requested general cryptographic usage.
     *
     * @param keyType declared Bus key category
     * @param usage   requested operation usage
     * @throws ValidateException if the key category cannot satisfy the requested direction
     */
    private static void validateDirection(final KeyType keyType, final Usage usage) {
        final boolean compatible = switch (usage) {
            case SIGN -> keyType == KeyType.PrivateKey || keyType == KeyType.SecretKey;
            case VERIFY -> keyType == KeyType.PublicKey || keyType == KeyType.SecretKey;
            case ENCRYPT, WRAP -> keyType == KeyType.PublicKey || keyType == KeyType.SecretKey;
            case DECRYPT, UNWRAP -> keyType == KeyType.PrivateKey || keyType == KeyType.SecretKey;
            case MAC_CREATE, MAC_VERIFY -> keyType == KeyType.SecretKey;
        };
        if (!compatible) {
            throw new ValidateException("Cryptographic key type cannot perform the requested usage");
        }
    }

    /**
     * Validates one explicit algorithm selection before a cryptographic operation begins.
     *
     * @param algorithm       exact protocol-registered algorithm identifier
     * @param allowed         usage-specific algorithm allowlist from the security baseline and selected profile
     * @param key             explicit JCA key accepted by the key parser
     * @param requiredKeyType key category declared by the selected algorithm profile
     * @param usage           cryptographic direction requested by the calling service
     * @throws IllegalArgumentException if a required argument or allowlist entry is {@code null}
     * @throws ValidateException        if the identifier is blank, forbidden, absent from the allowlist, or
     *                                  incompatible with the declared key category or usage
     */
    public void validate(
            final String algorithm,
            final Set<String> allowed,
            final Key key,
            final KeyType requiredKeyType,
            final Usage usage) {
        Assert.notNull(algorithm, "Algorithm identifier must not be null");
        Assert.notNull(allowed, "Algorithm allowlist must not be null");
        Assert.notNull(key, "Cryptographic key must not be null");
        Assert.notNull(requiredKeyType, "Required key type must not be null");
        Assert.notNull(usage, "Cryptographic usage must not be null");
        if (algorithm.isBlank()) {
            throw new ValidateException("Algorithm identifier must not be blank");
        }
        if ("none".equalsIgnoreCase(algorithm)) {
            throw new ValidateException("Unsecured algorithm none is forbidden");
        }
        for (String candidate : allowed) {
            Assert.notNull(candidate, "Algorithm allowlist entry must not be null");
            if (candidate.isBlank()) {
                throw new ValidateException("Algorithm allowlist entry must not be blank");
            }
        }
        if (!allowed.contains(algorithm)) {
            throw new ValidateException("Algorithm is not allowed for the selected profile and usage");
        }
        validateKeyType(key, requiredKeyType);
        validateDirection(requiredKeyType, usage);
    }

    /**
     * Classifies internal cryptographic operation direction without becoming a protocol field or algorithm identifier.
     *
     * @author Kimi Liu
     */
    public enum Usage {

        /**
         * Creates a digital signature with a private or symmetric key.
         */
        SIGN,

        /**
         * Verifies a digital signature with a public or symmetric key.
         */
        VERIFY,

        /**
         * Encrypts content with a public or symmetric key.
         */
        ENCRYPT,

        /**
         * Decrypts content with a private or symmetric key.
         */
        DECRYPT,

        /**
         * Wraps key material with a public or symmetric key.
         */
        WRAP,

        /**
         * Unwraps key material with a private or symmetric key.
         */
        UNWRAP,

        /**
         * Creates a message authentication code with a symmetric key.
         */
        MAC_CREATE,

        /**
         * Verifies a message authentication code with a symmetric key.
         */
        MAC_VERIFY

    }

}
