/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * Asymmetric encryptor interface, providing methods for:
 * <ul>
 * <li>Encrypting to byte arrays</li>
 * <li>Encrypting to hexadecimal strings</li>
 * <li>Encrypting to Base64 encoded strings</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
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
