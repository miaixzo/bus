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

import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.algorithm.Key;

/**
 * A no-operation {@link DecryptorProvider} that performs no actual decryption. It simply passes the data through
 * without modification.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NopDecryptorProvider implements DecryptorProvider {

    /**
     * Copies the source file to the destination file without any decryption.
     *
     * @param key  The decryption key (ignored).
     * @param src  The source file.
     * @param dest The destination file.
     * @throws IOException If an I/O error occurs during the copy.
     */
    @Override
    public void decrypt(Key key, File src, File dest) throws IOException {
        try (FileInputStream fis = new FileInputStream(src); FileOutputStream fos = new FileOutputStream(dest)) {
            decrypt(key, fis, fos);
        }
    }

    /**
     * Transfers all bytes from the input stream to the output stream without any decryption.
     *
     * @param key The decryption key (ignored).
     * @param in  The input stream.
     * @param out The output stream.
     * @throws IOException If an I/O error occurs during the transfer.
     */
    @Override
    public void decrypt(Key key, InputStream in, OutputStream out) throws IOException {
        Builder.transfer(in, out);
    }

    /**
     * Returns the original input stream without wrapping it in a decrypting stream.
     *
     * @param key The decryption key (ignored).
     * @param in  The input stream.
     * @return The original input stream.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public InputStream decrypt(Key key, InputStream in) throws IOException {
        return in;
    }

    /**
     * Returns the original output stream without wrapping it in a decrypting stream.
     *
     * @param key The decryption key (ignored).
     * @param out The output stream.
     * @return The original output stream.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public OutputStream decrypt(Key key, OutputStream out) throws IOException {
        return out;
    }

}
