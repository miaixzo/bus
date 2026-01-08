/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
