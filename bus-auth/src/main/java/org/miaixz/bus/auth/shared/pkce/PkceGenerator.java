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

import org.miaixz.bus.auth.shared.SecurityBaseline;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.crypto.Builder;

/**
 * Generates a high-entropy verifier and its RFC 7636 S256 challenge under the shared security baseline.
 *
 * @author Kimi Liu
 */
public class PkceGenerator {

    /**
     * Number of secure random bytes generated per verifier.
     */
    private final int bytes;

    /**
     * Creates a generator using the stronger of RFC and shared baseline entropy.
     *
     * @param policy selected protocol security policy
     * @throws IllegalArgumentException if policy is {@code null}
     * @throws ValidateException        if required entropy cannot fit RFC verifier grammar
     */
    public PkceGenerator(final SecurityBaseline.Policy policy) {
        Assert.notNull(policy, "PKCE security policy must not be null");
        this.bytes = (Math.max(Normal._256, policy.minimumEntropyBits()) + Byte.SIZE - 1) / Byte.SIZE;
        if (bytes > Normal._96) {
            throw new ValidateException("PKCE baseline entropy exceeds code_verifier capacity");
        }
    }

    /**
     * Generates one verifier and its bound S256 challenge.
     *
     * @return immutable verifier/challenge pair
     */
    public Pair generate() {
        final String value = Base64.encodeUrlSafe(RandomKit.randomBytes(bytes, RandomKit.getSecureRandom()));
        final CodeVerifier verifier = new CodeVerifier(value);
        final byte[] digest = Builder.sha256(value.getBytes(Charset.UTF_8));
        final CodeChallenge challenge = new CodeChallenge(Base64.encodeUrlSafe(digest), PkceMethod.S256);
        return new Pair(verifier, challenge);
    }

    /**
     * Couples generated verifier secret and transmitted challenge.
     *
     * @param verifier  sensitive generated verifier
     * @param challenge S256 challenge bound to the verifier
     * @author Kimi Liu
     */
    public record Pair(CodeVerifier verifier, CodeChallenge challenge) {

        /**
         * Validates generated pair components.
         */
        public Pair {
            Assert.notNull(verifier, "Generated PKCE verifier must not be null");
            Assert.notNull(challenge, "Generated PKCE challenge must not be null");
        }

    }

}
