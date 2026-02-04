/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.crypto;

import org.miaixz.bus.core.lang.EnumValue;

/**
 * Represents a cryptographic service provider that extends the core {@link org.miaixz.bus.core.Provider} interface.
 * This interface defines methods for encrypting and decrypting data using various cryptographic keys.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Provider extends org.miaixz.bus.core.Provider {

    /**
     * Encrypts data using the specified key. This method supports two main scenarios for encryption:
     * <ol>
     * <li>Private key encryption</li>
     * <li>Public key encryption</li>
     * </ol>
     *
     * @param key     The key used for encryption, provided as a comma-separated string. Example format:
     *                "privateKey,publicKey,type".
     * @param content The data to be encrypted.
     * @return The encrypted data as a byte array.
     */
    byte[] encrypt(String key, byte[] content);

    /**
     * Decrypts data using the specified key. This method supports two main scenarios for decryption:
     * <ol>
     * <li>Public key decryption (when data was encrypted with a private key)</li>
     * <li>Private key decryption (when data was encrypted with a public key)</li>
     * </ol>
     *
     * @param key     The key used for decryption, provided as a comma-separated string. Example format:
     *                "privateKey,publicKey,type". For instance, "5c3,5c3,PrivateKey" could imply decryption with a
     *                private key.
     * @param content The data to be decrypted.
     * @return The decrypted data as a byte array.
     */
    byte[] decrypt(String key, byte[] content);

    /**
     * Returns the provider type identifier.
     *
     * @return the provider type identifier, which is {@link EnumValue.Povider#CRYPTO}
     */
    @Override
    default Object type() {
        return EnumValue.Povider.CRYPTO;
    }

}
