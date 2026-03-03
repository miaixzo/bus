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

import java.io.*;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.algorithm.Key;

/**
 * A {@link DecryptorProvider} implementation that uses the built-in JDK cryptographic algorithms.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkDecryptorProvider implements DecryptorProvider {

    private final String algorithm;

    /**
     * Constructs a {@code JdkDecryptorProvider} with the specified algorithm.
     *
     * @param algorithm The name of the decryption algorithm to use (e.g., "AES").
     */
    public JdkDecryptorProvider(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Decrypts a source file and writes the result to a destination file.
     *
     * @param key  The decryption key.
     * @param src  The source file to decrypt.
     * @param dest The destination file to write the decrypted content to.
     * @throws IOException If an I/O error occurs during decryption.
     */
    @Override
    public void decrypt(Key key, File src, File dest) throws IOException {
        if (!dest.getParentFile().exists() && !dest.getParentFile().mkdirs()) {
            throw new IOException("could not make directory: " + dest.getParentFile());
        }
        try (InputStream in = new FileInputStream(src); OutputStream out = new FileOutputStream(dest)) {
            decrypt(key, in, out);
        }
    }

    /**
     * Decrypts an input stream and writes the result to an output stream.
     *
     * @param key The decryption key.
     * @param in  The input stream containing the encrypted data.
     * @param out The output stream to write the decrypted data to.
     * @throws IOException If an I/O error occurs during decryption.
     */
    @Override
    public void decrypt(Key key, InputStream in, OutputStream out) throws IOException {
        CipherInputStream cis = null;
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getDecryptKey(), algorithm));
            cis = new CipherInputStream(in, cipher);
            Builder.transfer(cis, out);
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            Builder.close(cis);
        }
    }

    /**
     * Wraps an input stream with a decrypting input stream.
     *
     * @param key The decryption key.
     * @param in  The input stream to wrap.
     * @return A new input stream that decrypts the data from the original stream.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public InputStream decrypt(Key key, InputStream in) throws IOException {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getDecryptKey(), algorithm));
            return new CipherInputStream(in, cipher);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Wraps an output stream with a decrypting output stream.
     *
     * @param key The decryption key.
     * @param out The output stream to wrap.
     * @return A new output stream that decrypts data before writing it to the original stream.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public OutputStream decrypt(Key key, OutputStream out) throws IOException {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getDecryptKey(), algorithm));
            return new CipherOutputStream(out, cipher);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
