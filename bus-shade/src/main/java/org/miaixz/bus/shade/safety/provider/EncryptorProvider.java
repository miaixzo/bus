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
 * Defines the contract for an encryptor, which provides methods for encrypting files and streams.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface EncryptorProvider {

    /**
     * Encrypts a source file and writes the result to a destination file.
     *
     * @param key  The encryption key.
     * @param src  The source file to encrypt.
     * @param dest The destination file to write the encrypted content to.
     * @throws IOException If an I/O error occurs during encryption.
     */
    void encrypt(Key key, File src, File dest) throws IOException;

    /**
     * Encrypts an input stream and writes the result to an output stream.
     *
     * @param key The encryption key.
     * @param in  The input stream containing the plaintext data.
     * @param out The output stream to write the encrypted data to.
     * @throws IOException If an I/O error occurs during encryption.
     */
    void encrypt(Key key, InputStream in, OutputStream out) throws IOException;

    /**
     * Wraps an input stream with an encrypting input stream.
     *
     * @param key The encryption key.
     * @param in  The input stream to wrap.
     * @return A new input stream that encrypts the data from the original stream.
     * @throws IOException If an I/O error occurs.
     */
    InputStream encrypt(Key key, InputStream in) throws IOException;

    /**
     * Wraps an output stream with an encrypting output stream.
     *
     * @param key The encryption key.
     * @param out The output stream to wrap.
     * @return A new output stream that encrypts data before writing it to the original stream.
     * @throws IOException If an I/O error occurs.
     */
    OutputStream encrypt(Key key, OutputStream out) throws IOException;

}
