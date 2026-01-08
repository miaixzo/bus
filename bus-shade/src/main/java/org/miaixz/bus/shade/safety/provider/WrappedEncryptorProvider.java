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
 * An abstract base class for an {@link EncryptorProvider} that wraps another encryptor provider. This class delegates
 * all encryption operations to the wrapped provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class WrappedEncryptorProvider implements EncryptorProvider {

    /**
     * The wrapped encryptor provider to which all operations are delegated.
     */
    protected final EncryptorProvider encryptorProvider;

    /**
     * Constructs a {@code WrappedEncryptorProvider} with the specified delegate encryptor.
     *
     * @param encryptorProvider The encryptor provider to wrap.
     */
    protected WrappedEncryptorProvider(EncryptorProvider encryptorProvider) {
        this.encryptorProvider = encryptorProvider;
    }

    /**
     * Encrypts a source file to a destination file by delegating to the wrapped provider.
     *
     * @param key  The encryption key.
     * @param src  The source file to encrypt.
     * @param dest The destination file for the encrypted content.
     * @throws IOException If an I/O error occurs during encryption.
     */
    @Override
    public void encrypt(Key key, File src, File dest) throws IOException {
        encryptorProvider.encrypt(key, src, dest);
    }

    /**
     * Encrypts an input stream to an output stream by delegating to the wrapped provider.
     *
     * @param key The encryption key.
     * @param in  The input stream containing the plaintext data.
     * @param out The output stream for the encrypted data.
     * @throws IOException If an I/O error occurs during encryption.
     */
    @Override
    public void encrypt(Key key, InputStream in, OutputStream out) throws IOException {
        encryptorProvider.encrypt(key, in, out);
    }

    /**
     * Wraps an input stream with an encrypting stream by delegating to the wrapped provider.
     *
     * @param key The encryption key.
     * @param in  The input stream to wrap.
     * @return A new input stream that encrypts data from the original stream.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public InputStream encrypt(Key key, InputStream in) throws IOException {
        return encryptorProvider.encrypt(key, in);
    }

    /**
     * Wraps an output stream with an encrypting stream by delegating to the wrapped provider.
     *
     * @param key The encryption key.
     * @param out The output stream to wrap.
     * @return A new output stream that encrypts data before writing to the original stream.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public OutputStream encrypt(Key key, OutputStream out) throws IOException {
        return encryptorProvider.encrypt(key, out);
    }

}
