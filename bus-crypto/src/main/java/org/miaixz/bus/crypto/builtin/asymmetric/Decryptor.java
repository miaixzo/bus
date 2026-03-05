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
package org.miaixz.bus.crypto.builtin.asymmetric;

import java.io.InputStream;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Builder;

/**
 * Asymmetric decryptor interface. This interface provides methods for:
 * <ul>
 * <li>Decrypting from byte arrays</li>
 * <li>Decrypting from hexadecimal strings</li>
 * <li>Decrypting from Base64 encoded strings</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Decryptor {

    /**
     * Decrypts the given byte array using the specified key type.
     *
     * @param bytes   The byte array to be decrypted.
     * @param keyType The type of key to use for decryption (e.g., {@link KeyType#PrivateKey} or
     *                {@link KeyType#PublicKey}).
     * @return The decrypted content as a byte array.
     */
    byte[] decrypt(byte[] bytes, KeyType keyType);

    /**
     * Decrypts data from an input stream using the specified key type. The input stream will be read entirely and then
     * decrypted.
     *
     * @param data    The input stream containing the encrypted data.
     * @param keyType The type of key to use for decryption.
     * @return The decrypted content as a byte array.
     * @throws InternalException if an I/O error occurs during stream reading or decryption fails.
     */
    default byte[] decrypt(final InputStream data, final KeyType keyType) throws InternalException {
        return decrypt(IoKit.readBytes(data), keyType);
    }

    /**
     * Decrypts a string that is either Hex (hexadecimal) or Base64 encoded, using the specified key type.
     *
     * @param data    The string to be decrypted, which must be in hexadecimal or Base64 format.
     * @param keyType The type of key to use for decryption.
     * @return The decrypted content as a byte array.
     */
    default byte[] decrypt(final String data, final KeyType keyType) {
        return decrypt(Builder.decode(data), keyType);
    }

    /**
     * Decrypts a string that is either Hex (hexadecimal) or Base64 encoded, and converts the result to a string using
     * the specified charset.
     *
     * @param data    The string to be decrypted, which must be in hexadecimal or Base64 format.
     * @param keyType The type of key to use for decryption.
     * @param charset The character set to use for decoding the decrypted bytes into a string.
     * @return The decrypted content as a String.
     */
    default String decryptString(final String data, final KeyType keyType, final java.nio.charset.Charset charset) {
        return StringKit.toString(decrypt(data, keyType), charset);
    }

    /**
     * Decrypts a string that is either Hex (16-进制) or Base64 encoded, and converts the result to a string using UTF-8
     * encoding.
     *
     * @param data    The string to be decrypted, which must be in hexadecimal or Base64 format.
     * @param keyType The type of key to use for decryption.
     * @return The decrypted content as a String, using UTF-8 encoding.
     */
    default String decryptString(final String data, final KeyType keyType) {
        return decryptString(data, keyType, Charset.UTF_8);
    }

}
