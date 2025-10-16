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
package org.miaixz.bus.crypto.builtin.symmetric;

import java.io.InputStream;
import java.io.OutputStream;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * Symmetric encryptor interface, providing methods for:
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
     * Encrypts the given byte array.
     *
     * @param data The byte array to be encrypted.
     * @return The encrypted content as a byte array.
     */
    byte[] encrypt(byte[] data);

    /**
     * Encrypts data from an input stream and writes it to an output stream. This method is suitable for large amounts
     * of data. The option to close streams after operation is provided.
     *
     * @param data    The input stream containing the data to be encrypted.
     * @param out     The output stream to write the encrypted data to (e.g., a file or network stream).
     * @param isClose Whether to close the input and output streams after the operation.
     * @throws InternalException if an I/O error occurs during encryption.
     */
    void encrypt(InputStream data, OutputStream out, boolean isClose);

    /**
     * Encrypts the given byte array and returns the result as a hexadecimal string.
     *
     * @param data The byte array to be encrypted.
     * @return The encrypted content as a hexadecimal string.
     */
    default String encryptHex(final byte[] data) {
        return HexKit.encodeString(encrypt(data));
    }

    /**
     * Encrypts the given byte array and returns the result as a Base64 encoded string.
     *
     * @param data The byte array to be encrypted.
     * @return The encrypted content as a Base64 encoded string.
     */
    default String encryptBase64(final byte[] data) {
        return Base64.encode(encrypt(data));
    }

    /**
     * Encrypts the given string data using the specified charset.
     *
     * @param data    The string data to be encrypted.
     * @param charset The character set to use for encoding the string.
     * @return The encrypted content as a byte array.
     */
    default byte[] encrypt(final String data, final java.nio.charset.Charset charset) {
        return encrypt(ByteKit.toBytes(data, charset));
    }

    /**
     * Encrypts the given string data using the specified charset and returns the result as a hexadecimal string.
     *
     * @param data    The string data to be encrypted.
     * @param charset The character set to use for encoding the string.
     * @return The encrypted content as a hexadecimal string.
     */
    default String encryptHex(final String data, final java.nio.charset.Charset charset) {
        return HexKit.encodeString(encrypt(data, charset));
    }

    /**
     * Encrypts the given string data using the specified charset and returns the result as a Base64 encoded string.
     *
     * @param data    The string data to be encrypted.
     * @param charset The character set to use for encoding the string.
     * @return The encrypted content as a Base64 encoded string.
     */
    default String encryptBase64(final String data, final java.nio.charset.Charset charset) {
        return Base64.encode(encrypt(data, charset));
    }

    /**
     * Encrypts the given string data using UTF-8 encoding.
     *
     * @param data The string data to be encrypted.
     * @return The encrypted content as a byte array.
     */
    default byte[] encrypt(final String data) {
        return encrypt(ByteKit.toBytes(data, Charset.UTF_8));
    }

    /**
     * Encrypts the given string data using UTF-8 encoding and returns the result as a hexadecimal string.
     *
     * @param data The string data to be encrypted.
     * @return The encrypted content as a hexadecimal string.
     */
    default String encryptHex(final String data) {
        return HexKit.encodeString(encrypt(data));
    }

    /**
     * Encrypts the given string data using UTF-8 encoding and returns the result as a Base64 encoded string.
     *
     * @param data The string data to be encrypted.
     * @return The encrypted content as a Base64 encoded string.
     */
    default String encryptBase64(final String data) {
        return Base64.encode(encrypt(data));
    }

    /**
     * Encrypts data from an input stream. The input stream will be closed after the operation.
     *
     * @param data The input stream containing the data to be encrypted.
     * @return The encrypted content as a byte array.
     * @throws InternalException if an I/O error occurs during encryption.
     */
    default byte[] encrypt(final InputStream data) throws InternalException {
        return encrypt(IoKit.readBytes(data));
    }

    /**
     * Encrypts data from an input stream and returns the result as a hexadecimal string. The input stream will be
     * closed after the operation.
     *
     * @param data The input stream containing the data to be encrypted.
     * @return The encrypted content as a hexadecimal string.
     */
    default String encryptHex(final InputStream data) {
        return HexKit.encodeString(encrypt(data));
    }

    /**
     * Encrypts data from an input stream and returns the result as a Base64 encoded string. The input stream will be
     * closed after the operation.
     *
     * @param data The input stream containing the data to be encrypted.
     * @return The encrypted content as a Base64 encoded string.
     */
    default String encryptBase64(final InputStream data) {
        return Base64.encode(encrypt(data));
    }

}
