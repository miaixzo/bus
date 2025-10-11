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

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Builder;

/**
 * Symmetric decryptor interface, providing:
 * <ul>
 * <li>Decrypt from bytes</li>
 * <li>Decrypt from Hex (hexadecimal)</li>
 * <li>Decrypt from Base64</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Decryptor {

    /**
     * Decrypts the given byte array.
     *
     * @param bytes The byte array to be decrypted.
     * @return The decrypted byte array.
     */
    byte[] decrypt(byte[] bytes);

    /**
     * Decrypts data from an input stream and writes it to an output stream. This method is designed for large amounts
     * of data and does not close the streams upon completion.
     *
     * @param data    The input stream containing the encrypted data.
     * @param out     The output stream to write the decrypted data to, which can be a file or a network location.
     * @param isClose Whether to close both the input and output streams after the operation.
     * @throws InternalException if an I/O error occurs.
     */
    void decrypt(InputStream data, OutputStream out, boolean isClose);

    /**
     * Decrypts a byte array into a string using the specified charset.
     *
     * @param bytes   The byte array to be decrypted.
     * @param charset The charset to use for decoding the decrypted bytes.
     * @return The decrypted string.
     */
    default String decryptString(final byte[] bytes, final java.nio.charset.Charset charset) {
        return StringKit.toString(decrypt(bytes), charset);
    }

    /**
     * Decrypts a byte array into a string using the default UTF-8 charset.
     *
     * @param bytes The byte array to be decrypted.
     * @return The decrypted string.
     */
    default String decryptString(final byte[] bytes) {
        return decryptString(bytes, Charset.UTF_8);
    }

    /**
     * Decrypts a string that is either Hex (hexadecimal) or Base64 encoded.
     *
     * @param data The string to be decrypted, must be in Hex or Base64 format.
     * @return The decrypted byte array.
     */
    default byte[] decrypt(final String data) {
        return decrypt(Builder.decode(data));
    }

    /**
     * Decrypts a Hex (hexadecimal) or Base64 encoded string into a string using the specified charset.
     *
     * @param data    The string to be decrypted.
     * @param charset The charset to use for the resulting string.
     * @return The decrypted string.
     */
    default String decryptString(final String data, final java.nio.charset.Charset charset) {
        return StringKit.toString(decrypt(data), charset);
    }

    /**
     * Decrypts a Hex (hexadecimal) or Base64 encoded string into a string using the default UTF-8 charset.
     *
     * @param data The string to be decrypted.
     * @return The decrypted string.
     */
    default String decryptString(final String data) {
        return decryptString(data, Charset.UTF_8);
    }

    /**
     * Decrypts data from an input stream. This method closes the stream after reading.
     *
     * @param data The input stream containing the data to be decrypted.
     * @return The decrypted byte array.
     * @throws InternalException if an I/O error occurs.
     */
    default byte[] decrypt(final InputStream data) throws InternalException {
        return decrypt(IoKit.readBytes(data));
    }

    /**
     * Decrypts data from an input stream into a string using the specified charset. This method does not close the
     * stream.
     *
     * @param data    The input stream to be decrypted.
     * @param charset The charset to use for the resulting string.
     * @return The decrypted string.
     */
    default String decryptString(final InputStream data, final java.nio.charset.Charset charset) {
        return StringKit.toString(decrypt(data), charset);
    }

    /**
     * Decrypts data from an input stream into a string using the default UTF-8 charset.
     *
     * @param data The input stream to be decrypted.
     * @return The decrypted string.
     */
    default String decryptString(final InputStream data) {
        return decryptString(data, Charset.UTF_8);
    }

}
