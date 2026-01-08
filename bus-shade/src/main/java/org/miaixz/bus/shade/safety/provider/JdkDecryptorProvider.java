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
