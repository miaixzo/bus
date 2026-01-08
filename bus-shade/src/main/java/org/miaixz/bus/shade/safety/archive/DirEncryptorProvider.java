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
package org.miaixz.bus.shade.safety.archive;

import java.io.File;
import java.io.IOException;

import org.miaixz.bus.shade.safety.Complex;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.provider.EncryptorProvider;
import org.miaixz.bus.shade.safety.provider.EntryEncryptorProvider;

/**
 * A {@link EncryptorProvider} implementation for encrypting files and directories. This provider can recursively
 * encrypt files within a directory, applying a filter to determine which files should be encrypted.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DirEncryptorProvider extends EntryEncryptorProvider<File> implements EncryptorProvider {

    /**
     * Constructs a {@code DirEncryptorProvider} with a given delegate encryptor. No specific filter is applied, meaning
     * all files will be considered for encryption by the delegate.
     *
     * @param encryptorProvider The delegate encryptor provider that performs the actual encryption.
     */
    public DirEncryptorProvider(EncryptorProvider encryptorProvider) {
        this(encryptorProvider, null);
    }

    /**
     * Constructs a {@code DirEncryptorProvider} with a given delegate encryptor and a filter. The filter determines
     * which files should be encrypted by the delegate.
     *
     * @param encryptorProvider The delegate encryptor provider that performs the actual encryption.
     * @param filter            The {@link Complex} filter to apply to files. Only files matching the filter will be
     *                          encrypted.
     */
    public DirEncryptorProvider(EncryptorProvider encryptorProvider, Complex<File> filter) {
        super(encryptorProvider, filter);
    }

    /**
     * Encrypts a source file or directory to a destination file or directory. If the source is a file, it encrypts it
     * using the configured encryptor (or a no-op encryptor if filtered). If the source is a directory, it recursively
     * encrypts its contents.
     *
     * @param key  The {@link Key} used for encryption.
     * @param src  The source file or directory to encrypt.
     * @param dest The destination file or directory where the encrypted content will be written.
     * @throws IOException If an I/O error occurs during encryption.
     */
    @Override
    public void encrypt(Key key, File src, File dest) throws IOException {
        if (src.isFile()) {
            EncryptorProvider encryptor = on(src) ? encryptorProvider : xNopEncryptor;
            encryptor.encrypt(key, src, dest);
        } else if (src.isDirectory()) {
            File[] files = src.listFiles();
            if (files != null) {
                for (File file : files) {
                    encrypt(key, file, new File(dest, file.getName()));
                }
            }
        }
    }

}
