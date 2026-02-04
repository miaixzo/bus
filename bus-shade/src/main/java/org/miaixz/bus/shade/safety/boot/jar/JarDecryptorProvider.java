/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.shade.safety.boot.jar;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.Deflater;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.Complex;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.provider.DecryptorProvider;
import org.miaixz.bus.shade.safety.provider.EntryDecryptorProvider;
import org.miaixz.bus.shade.safety.streams.AlwaysInputStream;
import org.miaixz.bus.shade.safety.streams.AlwaysOutputStream;

/**
 * A {@link DecryptorProvider} implementation for decrypting standard JAR files. This provider handles the structure of
 * JARs and applies decryption based on a provided filter and key.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JarDecryptorProvider extends EntryDecryptorProvider<JarArchiveEntry> implements DecryptorProvider {

    /**
     * The compression level to use for the output JAR archive.
     */
    private final int level;

    /**
     * Constructs a {@code JarDecryptorProvider} with a delegate decryptor and a default filter. The default filter is
     * {@link JarAllComplex}, meaning all entries will be considered for decryption.
     *
     * @param xEncryptor The delegate decryptor provider that performs the actual decryption.
     */
    public JarDecryptorProvider(DecryptorProvider xEncryptor) {
        this(xEncryptor, new JarAllComplex());
    }

    /**
     * Constructs a {@code JarDecryptorProvider} with a delegate decryptor and a custom filter. Uses default compression
     * level ({@link Deflater#DEFLATED}).
     *
     * @param decryptorProvider The delegate decryptor provider that performs the actual decryption.
     * @param filter            The {@link Complex} filter to apply to JAR entries. Only entries matching the filter
     *                          will be decrypted.
     */
    public JarDecryptorProvider(DecryptorProvider decryptorProvider, Complex<JarArchiveEntry> filter) {
        this(decryptorProvider, Deflater.DEFLATED, filter);
    }

    /**
     * Constructs a {@code JarDecryptorProvider} with a delegate decryptor and a specified compression level. Uses a
     * default filter ({@link JarAllComplex}).
     *
     * @param xEncryptor The delegate decryptor provider that performs the actual decryption.
     * @param level      The compression level for the output JAR archive.
     */
    public JarDecryptorProvider(DecryptorProvider xEncryptor, int level) {
        this(xEncryptor, level, new JarAllComplex());
    }

    /**
     * Constructs a {@code JarDecryptorProvider} with a delegate decryptor, a specified compression level, and a custom
     * filter.
     *
     * @param decryptorProvider The delegate decryptor provider that performs the actual decryption.
     * @param level             The compression level for the output JAR archive.
     * @param filter            The {@link Complex} filter to apply to JAR entries. Only entries matching the filter
     *                          will be decrypted.
     */
    public JarDecryptorProvider(DecryptorProvider decryptorProvider, int level, Complex<JarArchiveEntry> filter) {
        super(decryptorProvider, filter);
        this.level = level;
    }

    /**
     * Decrypts a source JAR file to a destination file.
     *
     * @param key  The {@link Key} used for decryption.
     * @param src  The source encrypted JAR file.
     * @param dest The destination file for the decrypted JAR.
     * @throws IOException If an I/O error occurs during decryption.
     */
    @Override
    public void decrypt(Key key, File src, File dest) throws IOException {
        try (FileInputStream fis = new FileInputStream(src); FileOutputStream fos = new FileOutputStream(dest)) {
            decrypt(key, fis, fos);
        }
    }

    /**
     * Decrypts a JAR archive from an input stream to an output stream. This method iterates through the entries of the
     * JAR, decrypting them as necessary. It handles special cases for META-INF/MANIFEST.MF.
     *
     * @param key The {@link Key} used for decryption.
     * @param in  The input stream containing the encrypted JAR archive.
     * @param out The output stream where the decrypted JAR archive will be written.
     * @throws IOException If an I/O error occurs during decryption.
     */
    @Override
    public void decrypt(Key key, InputStream in, OutputStream out) throws IOException {
        JarArchiveInputStream zis = null;
        JarArchiveOutputStream zos = null;
        try {
            zis = new JarArchiveInputStream(in);
            zos = new JarArchiveOutputStream(out);
            zos.setLevel(level);
            AlwaysInputStream nis = new AlwaysInputStream(zis);
            AlwaysOutputStream nos = new AlwaysOutputStream(zos);
            JarArchiveEntry entry;
            while (null != (entry = zis.getNextJarEntry())) {
                // Skip internal xjar resources and directories
                if (entry.getName().startsWith(Builder.XJAR_SRC_DIR) || entry.getName().endsWith(Builder.XJAR_INF_DIR)
                        || entry.getName().endsWith(Builder.XJAR_INF_DIR + Builder.XJAR_INF_IDX)) {
                    continue;
                }
                // Handle directory entries
                if (entry.isDirectory()) {
                    JarArchiveEntry jarArchiveEntry = new JarArchiveEntry(entry.getName());
                    jarArchiveEntry.setTime(entry.getTime());
                    zos.putArchiveEntry(jarArchiveEntry);
                }
                // Handle META-INF/MANIFEST.MF special processing
                else if (entry.getName().equals(Builder.META_INF_MANIFEST)) {
                    Manifest manifest = new Manifest(nis);
                    Attributes attributes = manifest.getMainAttributes();
                    String mainClass = attributes.getValue("Jar-Main-Class");
                    if (null != mainClass) {
                        attributes.putValue("Main-Class", mainClass);
                        attributes.remove(new Attributes.Name("Jar-Main-Class"));
                    }
                    Builder.removeKey(attributes);
                    JarArchiveEntry jarArchiveEntry = new JarArchiveEntry(entry.getName());
                    jarArchiveEntry.setTime(entry.getTime());
                    zos.putArchiveEntry(jarArchiveEntry);
                    manifest.write(nos);
                }
                // Handle other entries
                else {
                    JarArchiveEntry jarArchiveEntry = new JarArchiveEntry(entry.getName());
                    jarArchiveEntry.setTime(entry.getTime());
                    zos.putArchiveEntry(jarArchiveEntry);
                    boolean filtered = on(entry);
                    DecryptorProvider decryptor = filtered ? decryptorProvider : xNopDecryptor;
                    try (OutputStream eos = decryptor.decrypt(key, nos)) {
                        Builder.transfer(nis, eos);
                    }
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
