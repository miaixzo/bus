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
package org.miaixz.bus.shade.safety.archive;

import java.io.File;
import java.io.IOException;

import org.miaixz.bus.shade.safety.Complex;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.provider.DecryptorProvider;
import org.miaixz.bus.shade.safety.provider.EntryDecryptorProvider;

/**
 * A {@link DecryptorProvider} implementation for decrypting files and directories. This provider can recursively
 * decrypt files within a directory, applying a filter to determine which files should be decrypted.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DirDecryptorProvider extends EntryDecryptorProvider<File> implements DecryptorProvider {

    /**
     * Constructs a {@code DirDecryptorProvider} with a given delegate decryptor. No specific filter is applied, meaning
     * all files will be considered for decryption by the delegate.
     *
     * @param xEncryptor The delegate decryptor provider that performs the actual decryption.
     */
    public DirDecryptorProvider(DecryptorProvider xEncryptor) {
        this(xEncryptor, null);
    }

    /**
     * Constructs a {@code DirDecryptorProvider} with a given delegate decryptor and a filter. The filter determines
     * which files should be decrypted by the delegate.
     *
     * @param decryptorProvider The delegate decryptor provider that performs the actual decryption.
     * @param filter            The {@link Complex} filter to apply to files. Only files matching the filter will be
     *                          decrypted.
     */
    public DirDecryptorProvider(DecryptorProvider decryptorProvider, Complex<File> filter) {
        super(decryptorProvider, filter);
    }

    /**
     * Decrypts a source file or directory to a destination file or directory. If the source is a file, it decrypts it
     * using the configured decryptor (or a no-op decryptor if filtered). If the source is a directory, it recursively
     * decrypts its contents.
     *
     * @param key  The {@link Key} used for decryption.
     * @param src  The source file or directory to decrypt.
     * @param dest The destination file or directory where the decrypted content will be written.
     * @throws IOException If an I/O error occurs during decryption.
     */
    @Override
    public void decrypt(Key key, File src, File dest) throws IOException {
        if (src.isFile()) {
            DecryptorProvider decryptor = on(src) ? decryptorProvider : xNopDecryptor;
            decryptor.decrypt(key, src, dest);
        } else if (src.isDirectory()) {
            File[] files = src.listFiles();
            if (files != null) {
                for (File file : files) {
                    decrypt(key, file, new File(dest, file.getName()));
                }
            }
        }
    }

}
