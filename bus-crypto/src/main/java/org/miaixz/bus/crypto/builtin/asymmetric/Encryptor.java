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
import java.nio.charset.Charset;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * Asymmetric encryptor interface. This interface provides methods for:
 * <ul>
 * <li>Encrypting to byte arrays</li>
 * <li>Encrypting to hexadecimal strings</li>
 * <li>Encrypting to Base64 encoded strings</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Encryptor {

    /**
     * Encrypts the given byte array using the specified key type.
     *
     * @param data    The byte array to be encrypted.
     * @param keyType The type of key to use for encryption (e.g., {@link KeyType#PrivateKey} or
     *                {@link KeyType#PublicKey}).
     * @return The encrypted content as a byte array.
     */
    byte[] encrypt(byte[] data, KeyType keyType);

    /**
     * Encodes the encrypted byte array into a hexadecimal string.
     *
     * @param data    The byte array to be encrypted.
     * @param keyType The type of key to use for encryption.
     * @return The encrypted content as a hexadecimal string.
     */
    default String encryptHex(final byte[] data, final KeyType keyType) {
        return HexKit.encodeString(encrypt(data, keyType));
    }

    /**
     * Encodes the encrypted byte array into a Base64 string.
     *
     * @param data    The byte array to be encrypted.
     * @param keyType The type of key to use for encryption.
     * @return The encrypted content as a Base64 encoded string.
     */
    default String encryptBase64(final byte[] data, final KeyType keyType) {
        return Base64.encode(encrypt(data, keyType));
    }

    /**
     * Encrypts the given string data using the specified charset and key type.
     *
     * @param data    The string data to be encrypted.
     * @param charset The character set to use for encoding the string.
     * @param keyType The type of key to use for encryption.
     * @return The encrypted content as a byte array.
     */
    default byte[] encrypt(final String data, final Charset charset, final KeyType keyType) {
        return encrypt(ByteKit.toBytes(data, charset), keyType);
    }

    /**
     * Encrypts the given string data using UTF-8 encoding and the specified key type.
     *
     * @param data    The string data to be encrypted.
     * @param keyType The type of key to use for encryption.
     * @return The encrypted content as a byte array.
     */
    default byte[] encrypt(final String data, final KeyType keyType) {
        return encrypt(ByteKit.toBytes(data), keyType);
    }

    /**
     * Encrypts the given string data using UTF-8 encoding and the specified key type, then returns the result as a
     * hexadecimal string.
     *
     * @param data    The string data to be encrypted.
     * @param keyType The type of key to use for encryption.
     * @return The encrypted content as a hexadecimal string.
     */
    default String encryptHex(final String data, final KeyType keyType) {
        return HexKit.encodeString(encrypt(data, keyType));
    }

    /**
     * Encrypts the given string data using the specified charset and key type, then returns the result as a hexadecimal
     * string.
     *
     * @param data    The string data to be encrypted.
     * @param charset The character set to use for encoding the string.
     * @param keyType The type of key to use for encryption.
     * @return The encrypted content as a hexadecimal string.
     */
    default String encryptHex(final String data, final Charset charset, final KeyType keyType) {
        return HexKit.encodeString(encrypt(data, charset, keyType));
    }

    /**
     * Encrypts the given string data using UTF-8 encoding and the specified key type, then returns the result as a
     * Base64 encoded string.
     *
     * @param data    The string data to be encrypted.
     * @param keyType The type of key to use for encryption.
     * @return The encrypted content as a Base64 encoded string.
     */
    default String encryptBase64(final String data, final KeyType keyType) {
        return Base64.encode(encrypt(data, keyType));
    }

    /**
     * Encrypts the given string data using the specified charset and key type, then returns the result as a Base64
     * encoded string.
     *
     * @param data    The string data to be encrypted.
     * @param charset The character set to use for encoding the string.
     * @param keyType The type of key to use for encryption.
     * @return The encrypted content as a Base64 encoded string.
     */
    default String encryptBase64(final String data, final Charset charset, final KeyType keyType) {
        return Base64.encode(encrypt(data, charset, keyType));
    }

    /**
     * Encrypts data from an input stream using the specified key type. The input stream will be read entirely and then
     * encrypted.
     *
     * @param data    The input stream containing the data to be encrypted.
     * @param keyType The type of key to use for encryption.
     * @return The encrypted content as a byte array.
     * @throws InternalException if an I/O error occurs during stream reading or encryption fails.
     */
    default byte[] encrypt(final InputStream data, final KeyType keyType) throws InternalException {
        return encrypt(IoKit.readBytes(data), keyType);
    }

    /**
     * Encrypts data from an input stream using the specified key type, then returns the result as a hexadecimal string.
     * The input stream will be read entirely and then encrypted.
     *
     * @param data    The input stream containing the data to be encrypted.
     * @param keyType The type of key to use for encryption.
     * @return The encrypted content as a hexadecimal string.
     */
    default String encryptHex(final InputStream data, final KeyType keyType) {
        return HexKit.encodeString(encrypt(data, keyType));
    }

    /**
     * Encrypts data from an input stream using the specified key type, then returns the result as a Base64 encoded
     * string. The input stream will be read entirely and then encrypted.
     *
     * @param data    The input stream containing the data to be encrypted.
     * @param keyType The type of key to use for encryption.
     * @return The encrypted content as a Base64 encoded string.
     */
    default String encryptBase64(final InputStream data, final KeyType keyType) {
        return Base64.encode(encrypt(data, keyType));
    }

}
