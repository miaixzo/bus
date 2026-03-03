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

import java.io.*;
import java.util.zip.Deflater;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.Complex;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.provider.DecryptorProvider;
import org.miaixz.bus.shade.safety.provider.EntryDecryptorProvider;
import org.miaixz.bus.shade.safety.streams.AlwaysOutputStream;

/**
 * A {@link DecryptorProvider} implementation for decrypting ZIP archive files. This provider can decrypt individual
 * entries within a ZIP archive, applying a filter to determine which entries should be decrypted. It also handles
 * compression levels for the output ZIP.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipDecryptorProvider extends EntryDecryptorProvider<ZipArchiveEntry> implements DecryptorProvider {

    /**
     * The compression level for the output ZIP archive.
     */
    private final int level;

    /**
     * Constructs a {@code ZipDecryptorProvider} with a given delegate decryptor. Uses default compression level
     * ({@link Deflater#DEFLATED}) and no specific filter.
     *
     * @param xEncryptor The delegate decryptor provider that performs the actual decryption.
     */
    public ZipDecryptorProvider(DecryptorProvider xEncryptor) {
        this(xEncryptor, null);
    }

    /**
     * Constructs a {@code ZipDecryptorProvider} with a given delegate decryptor and a filter. Uses default compression
     * level ({@link Deflater#DEFLATED}).
     *
     * @param decryptorProvider The delegate decryptor provider that performs the actual decryption.
     * @param filter            The {@link Complex} filter to apply to ZIP entries. Only entries matching the filter
     *                          will be decrypted.
     */
    public ZipDecryptorProvider(DecryptorProvider decryptorProvider, Complex<ZipArchiveEntry> filter) {
        this(decryptorProvider, Deflater.DEFLATED, filter);
    }

    /**
     * Constructs a {@code ZipDecryptorProvider} with a given delegate decryptor and a compression level. No specific
     * filter is applied.
     *
     * @param xEncryptor The delegate decryptor provider that performs the actual decryption.
     * @param level      The compression level for the output ZIP archive.
     */
    public ZipDecryptorProvider(DecryptorProvider xEncryptor, int level) {
        this(xEncryptor, level, null);
    }

    /**
     * Constructs a {@code ZipDecryptorProvider} with a given delegate decryptor, compression level, and a filter.
     *
     * @param decryptorProvider The delegate decryptor provider that performs the actual decryption.
     * @param level             The compression level for the output ZIP archive.
     * @param filter            The {@link Complex} filter to apply to ZIP entries. Only entries matching the filter
     *                          will be decrypted.
     */
    public ZipDecryptorProvider(DecryptorProvider decryptorProvider, int level, Complex<ZipArchiveEntry> filter) {
        super(decryptorProvider, filter);
        this.level = level;
    }

    /**
     * Decrypts a source ZIP file to a destination ZIP file.
     *
     * @param key  The {@link Key} used for decryption.
     * @param src  The source ZIP file to decrypt.
     * @param dest The destination ZIP file where the decrypted content will be written.
     * @throws IOException If an I/O error occurs during decryption.
     */
    @Override
    public void decrypt(Key key, File src, File dest) throws IOException {
        try (FileInputStream fis = new FileInputStream(src); FileOutputStream fos = new FileOutputStream(dest)) {
            decrypt(key, fis, fos);
        }
    }

    /**
     * Decrypts a ZIP archive from an input stream to an output stream. Each entry in the input ZIP stream is processed.
     * If an entry matches the configured filter, it is decrypted using the delegate decryptor; otherwise, it is copied
     * as-is.
     *
     * @param key The {@link Key} used for decryption.
     * @param in  The input stream containing the encrypted ZIP archive.
     * @param out The output stream where the decrypted ZIP archive will be written.
     * @throws IOException If an I/O error occurs during decryption.
     */
    @Override
    public void decrypt(Key key, InputStream in, OutputStream out) throws IOException {
        ZipArchiveInputStream zis = null;
        ZipArchiveOutputStream zos = null;
        try {
            zis = new ZipArchiveInputStream(in);
            zos = new ZipArchiveOutputStream(out);
            zos.setLevel(level);
            AlwaysOutputStream nos = new AlwaysOutputStream(zos);
            ZipArchiveEntry entry;
            while (null != (entry = zis.getNextZipEntry())) {
                if (entry.isDirectory()) {
                    continue;
                }
                zos.putArchiveEntry(new ZipArchiveEntry(entry.getName()));
                DecryptorProvider decryptor = on(entry) ? this.decryptorProvider : xNopDecryptor;
                try (OutputStream eos = decryptor.decrypt(key, nos)) {
                    Builder.transfer(zis, eos);
                }
                zos.closeArchiveEntry();
            }
            zos.finish();
        } finally {
            Builder.close(zis);
            Builder.close(zos);
        }
    }

}
