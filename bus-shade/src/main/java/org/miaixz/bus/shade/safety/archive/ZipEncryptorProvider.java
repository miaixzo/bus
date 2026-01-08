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

import java.io.*;
import java.util.zip.Deflater;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.Complex;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.provider.EncryptorProvider;
import org.miaixz.bus.shade.safety.provider.EntryEncryptorProvider;
import org.miaixz.bus.shade.safety.streams.AlwaysOutputStream;

/**
 * A {@link EncryptorProvider} implementation for encrypting ZIP archive files. This provider can encrypt individual
 * entries within a ZIP archive, applying a filter to determine which entries should be encrypted. It also handles
 * compression levels for the output ZIP.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipEncryptorProvider extends EntryEncryptorProvider<ZipArchiveEntry> implements EncryptorProvider {

    /**
     * The compression level for the output ZIP archive.
     */
    private final int level;

    /**
     * Constructs a {@code ZipEncryptorProvider} with a given delegate encryptor. Uses default compression level
     * ({@link Deflater#DEFLATED}) and no specific filter.
     *
     * @param encryptorProvider The delegate encryptor provider that performs the actual encryption.
     */
    public ZipEncryptorProvider(EncryptorProvider encryptorProvider) {
        this(encryptorProvider, null);
    }

    /**
     * Constructs a {@code ZipEncryptorProvider} with a given delegate encryptor and a filter. Uses default compression
     * level ({@link Deflater#DEFLATED}).
     *
     * @param encryptorProvider The delegate encryptor provider that performs the actual encryption.
     * @param filter            The {@link Complex} filter to apply to ZIP entries. Only entries matching the filter
     *                          will be encrypted.
     */
    public ZipEncryptorProvider(EncryptorProvider encryptorProvider, Complex<ZipArchiveEntry> filter) {
        this(encryptorProvider, Deflater.DEFLATED, filter);
    }

    /**
     * Constructs a {@code ZipEncryptorProvider} with a given delegate encryptor and a compression level. No specific
     * filter is applied.
     *
     * @param encryptorProvider The delegate encryptor provider that performs the actual encryption.
     * @param level             The compression level for the output ZIP archive.
     */
    public ZipEncryptorProvider(EncryptorProvider encryptorProvider, int level) {
        this(encryptorProvider, level, null);
    }

    /**
     * Constructs a {@code ZipEncryptorProvider} with a given delegate encryptor, compression level, and a filter.
     *
     * @param encryptorProvider The delegate encryptor provider that performs the actual encryption.
     * @param level             The compression level for the output ZIP archive.
     * @param filter            The {@link Complex} filter to apply to ZIP entries. Only entries matching the filter
     *                          will be encrypted.
     */
    public ZipEncryptorProvider(EncryptorProvider encryptorProvider, int level, Complex<ZipArchiveEntry> filter) {
        super(encryptorProvider, filter);
        this.level = level;
    }

    /**
     * Encrypts a source ZIP file to a destination ZIP file.
     *
     * @param key  The {@link Key} used for encryption.
     * @param src  The source ZIP file to encrypt.
     * @param dest The destination ZIP file where the encrypted content will be written.
     * @throws IOException If an I/O error occurs during encryption.
     */
    @Override
    public void encrypt(Key key, File src, File dest) throws IOException {
        try (FileInputStream fis = new FileInputStream(src); FileOutputStream fos = new FileOutputStream(dest)) {
            encrypt(key, fis, fos);
        }
    }

    /**
     * Encrypts a ZIP archive from an input stream to an output stream. Each entry in the input ZIP stream is processed.
     * If an entry matches the configured filter, it is encrypted using the delegate encryptor; otherwise, it is copied
     * as-is.
     *
     * @param key The {@link Key} used for encryption.
     * @param in  The input stream containing the unencrypted ZIP archive.
     * @param out The output stream where the encrypted ZIP archive will be written.
     * @throws IOException If an I/O error occurs during encryption.
     */
    @Override
    public void encrypt(Key key, InputStream in, OutputStream out) throws IOException {
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
                EncryptorProvider encryptor = on(entry) ? this.encryptorProvider : xNopEncryptor;
                try (OutputStream eos = encryptor.encrypt(key, nos)) {
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
