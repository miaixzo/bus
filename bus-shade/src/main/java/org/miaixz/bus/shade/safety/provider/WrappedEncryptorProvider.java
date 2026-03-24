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
 * @since Java 21+
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
