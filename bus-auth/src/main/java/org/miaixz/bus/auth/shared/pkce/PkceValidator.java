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
package org.miaixz.bus.auth.shared.pkce;

import java.util.Arrays;
import java.util.Set;

import org.miaixz.bus.auth.guard.SecretGuard;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.Builder;

/**
 * Validates a presented RFC 7636 verifier against a stored method-bound challenge in constant time.
 *
 * @author Kimi Liu
 */
public final class PkceValidator {

    /**
     * Shared secret comparison primitive.
     */
    private final SecretGuard secretGuard;
    /**
     * Explicit immutable compatibility allow-list.
     */
    private final Set<PkceMethod> allowedMethods;

    /**
     * Creates a validator with an explicit method policy.
     *
     * @param secretGuard    shared constant-time secret primitive
     * @param allowedMethods non-empty set of S256 and optionally plain
     */
    public PkceValidator(final SecretGuard secretGuard, final Set<PkceMethod> allowedMethods) {
        this.secretGuard = Assert.notNull(secretGuard, "PKCE secret guard must not be null");
        Assert.notNull(allowedMethods, "PKCE allowed methods must not be null");
        this.allowedMethods = Set.copyOf(allowedMethods);
        if (this.allowedMethods.isEmpty() || this.allowedMethods.stream()
                .anyMatch(method -> !PkceMethod.S256.equals(method) && !PkceMethod.PLAIN.equals(method))) {
            throw new ValidateException("PKCE allowed methods must contain supported registrations only");
        }
    }

    /**
     * Creates the default strict validator that permits only S256.
     *
     * @param secretGuard shared constant-time secret primitive
     * @return strict PKCE validator
     */
    public static PkceValidator strict(final SecretGuard secretGuard) {
        return new PkceValidator(secretGuard, Set.of(PkceMethod.S256));
    }

    /**
     * Re-derives and constant-time compares a verifier with its stored challenge.
     *
     * @param verifier  presented sensitive verifier
     * @param challenge stored method-bound challenge
     * @throws ValidateException if method is disabled or values do not match
     */
    public void validate(final CodeVerifier verifier, final CodeChallenge challenge) {
        Assert.notNull(verifier, "Presented PKCE verifier must not be null");
        Assert.notNull(challenge, "Stored PKCE challenge must not be null");
        if (!allowedMethods.contains(challenge.method())) {
            throw new ValidateException("PKCE challenge method is not allowed");
        }
        final String expected = PkceMethod.S256.equals(challenge.method())
                ? Base64.encodeUrlSafe(Builder.sha256(verifier.value().getBytes(Charset.UTF_8)))
                : verifier.value();
        final char[] expectedCharacters = expected.toCharArray();
        final char[] challengeCharacters = challenge.value().toCharArray();
        try {
            if (!secretGuard.matches(expectedCharacters, challengeCharacters)) {
                throw new ValidateException("PKCE code verifier does not match the stored challenge");
            }
        } finally {
            Arrays.fill(expectedCharacters, '\0');
            Arrays.fill(challengeCharacters, '\0');
        }
    }

}
