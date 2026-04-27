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
 * @since Java 21+
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
     * Returns whether the codec supports the supplied logical mode.
     *
     * @param mode logical mode
     * @return {@code true} when supported
     */
    default boolean supports(String mode) {
        return true;
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
