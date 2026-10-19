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
package org.miaixz.bus.cortex.setting.secret;

/**
 * Default secret codec that leaves values unchanged.
 *
 * @author Kimi Liu
 */
public class NoOpSecretCodec implements SecretCodec {

    /**
     * Creates NoOpSecretCodec with explicitly bound collaborators. Dependencies are not discovered implicitly.
     */
    public NoOpSecretCodec() {
        // No initialization required.
    }

    /**
     * Returns the stable codec identifier used for setting metadata.
     *
     * @return codec identifier
     */
    @Override
    public String codecId() {
        return "noop";
    }

    /**
     * Tests whether the supplied source or protection mode is supported without performing any remote resolution.
     *
     * @param mode non-null version selection mode
     * @return true when the input is supported
     */
    @Override
    public boolean supports(String mode) {
        return !"encrypted".equalsIgnoreCase(mode);
    }

    /**
     * Returns the plaintext unchanged.
     *
     * @param plaintext plaintext input
     * @return unchanged plaintext
     */
    @Override
    public String encrypt(String plaintext) {
        return plaintext;
    }

    /**
     * Returns the ciphertext unchanged.
     *
     * @param ciphertext ciphertext input
     * @return unchanged ciphertext
     */
    @Override
    public String decrypt(String ciphertext) {
        return ciphertext;
    }

}
