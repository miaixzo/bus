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
 * Codec abstraction for protected setting values.
 *
 * @author Kimi Liu
 */
public interface SecretCodec {

    /**
     * Returns the logical codec identifier.
     *
     * @return codec identifier
     */
    default String codecId() {
        return getClass().getSimpleName();
    }

    /**
     * Reports support for a source or protection mode without remote resolution. The default implementation accepts every input.
     *
     * @param mode source or protection marker, such as encrypted; the default also accepts null
     * @return true by default; overrides may restrict supported modes
     */
    default boolean supports(String mode) {
        return true;
    }

    /**
     * Rejects no-op or unsupported encryption before accepting protected storage. Non-null plaintext must produce
     * nonblank ciphertext different from the input.
     * @param plaintext plaintext to protect; null remains null when protection is supported
     * @return protected representation, or null for a null input
     * @throws IllegalStateException if the codec is no-op or does not support encrypted storage, or non-null input produces null, blank, or unchanged ciphertext
     */
    default String protect(String plaintext) {
        if (this instanceof NoOpSecretCodec || !supports("encrypted")) {
            throw new IllegalStateException("A protecting secret codec is required");
        }
        String ciphertext = plaintext == null ? null : encrypt(plaintext);
        if (plaintext != null && (ciphertext == null || ciphertext.isBlank() || plaintext.equals(ciphertext))) {
            throw new IllegalStateException("Secret codec did not protect the value");
        }
        return ciphertext;
    }

    /**
     * Encrypts plaintext for storage.
     *
     * @param plaintext plaintext value
     * @return encrypted representation
     */
    String encrypt(String plaintext);

    /**
     * Decrypts a protected stored value.
     *
     * @param ciphertext encrypted representation
     * @return plaintext value
     */
    String decrypt(String ciphertext);

}
