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
package org.miaixz.bus.shade.safety.provider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.miaixz.bus.shade.safety.algorithm.Key;

/**
 * Defines the contract for a decryptor, which provides methods for decrypting files and streams.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface DecryptorProvider {

    /**
     * Decrypts a source file and writes the result to a destination file.
     *
     * @param key  The decryption key.
     * @param src  The source file to decrypt.
     * @param dest The destination file to write the decrypted content to.
     * @throws IOException If an I/O error occurs during decryption.
     */
    void decrypt(Key key, File src, File dest) throws IOException;

    /**
     * Decrypts an input stream and writes the result to an output stream.
     *
     * @param key The decryption key.
     * @param in  The input stream containing the encrypted data.
     * @param out The output stream to write the decrypted data to.
     * @throws IOException If an I/O error occurs during decryption.
     */
    void decrypt(Key key, InputStream in, OutputStream out) throws IOException;

    /**
     * Wraps an input stream with a decrypting input stream.
     *
     * @param key The decryption key.
     * @param in  The input stream to wrap.
     * @return A new input stream that decrypts the data from the original stream.
     * @throws IOException If an I/O error occurs.
     */
    InputStream decrypt(Key key, InputStream in) throws IOException;

    /**
     * Wraps an output stream with a decrypting output stream.
     *
     * @param key The decryption key.
     * @param out The output stream to wrap.
     * @return A new output stream that decrypts data before writing it to the original stream.
     * @throws IOException If an I/O error occurs.
     */
    OutputStream decrypt(Key key, OutputStream out) throws IOException;

}
