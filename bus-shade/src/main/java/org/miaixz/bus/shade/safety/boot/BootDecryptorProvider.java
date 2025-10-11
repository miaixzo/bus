/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.shade.safety.boot;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.Complex;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.boot.jar.JarAllComplex;
import org.miaixz.bus.shade.safety.boot.jar.JarDecryptorProvider;
import org.miaixz.bus.shade.safety.provider.DecryptorProvider;
import org.miaixz.bus.shade.safety.provider.EntryDecryptorProvider;
import org.miaixz.bus.shade.safety.streams.AlwaysInputStream;
import org.miaixz.bus.shade.safety.streams.AlwaysOutputStream;

/**
 * A {@link DecryptorProvider} implementation specifically designed for decrypting Spring Boot JAR files. This provider
 * handles the structure of Spring Boot JARs, including nested JARs within BOOT-INF/lib, and applies decryption based on
 * a provided filter and key.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BootDecryptorProvider extends EntryDecryptorProvider<JarArchiveEntry> implements DecryptorProvider {

    /**
     * The compression level to use for the output JAR archive.
     */
    private final int level;

    /**
     * Constructs a {@code BootDecryptorProvider} with a delegate decryptor and a default filter. The default filter is
     * {@link JarAllComplex}, meaning all entries will be considered for decryption.
     *
     * @param xEncryptor The delegate decryptor provider that performs the actual decryption.
     */
    public BootDecryptorProvider(DecryptorProvider xEncryptor) {
        this(xEncryptor, new JarAllComplex());
    }

    /**
     * Constructs a {@code BootDecryptorProvider} with a delegate decryptor and a custom filter. Uses default
     * compression level ({@link Deflater#DEFLATED}).
     *
     * @param decryptorProvider The delegate decryptor provider that performs the actual decryption.
     * @param filter            The {@link Complex} filter to apply to JAR entries. Only entries matching the filter
     *                          will be decrypted.
     */
    public BootDecryptorProvider(DecryptorProvider decryptorProvider, Complex<JarArchiveEntry> filter) {
        this(decryptorProvider, Deflater.DEFLATED, filter);
    }

    /**
     * Constructs a {@code BootDecryptorProvider} with a delegate decryptor and a specified compression level. Uses a
     * default filter ({@link JarAllComplex}).
     *
     * @param xEncryptor The delegate decryptor provider that performs the actual decryption.
     * @param level      The compression level for the output JAR archive.
     */
    public BootDecryptorProvider(DecryptorProvider xEncryptor, int level) {
        this(xEncryptor, level, new JarAllComplex());
    }

    /**
     * Constructs a {@code BootDecryptorProvider} with a delegate decryptor, a specified compression level, and a custom
     * filter.
     *
     * @param decryptorProvider The delegate decryptor provider that performs the actual decryption.
     * @param level             The compression level for the output JAR archive.
     * @param filter            The {@link Complex} filter to apply to JAR entries. Only entries matching the filter
     *                          will be decrypted.
     */
    public BootDecryptorProvider(DecryptorProvider decryptorProvider, int level, Complex<JarArchiveEntry> filter) {
        super(decryptorProvider, filter);
        this.level = level;
    }

    /**
     * Decrypts a source Spring Boot JAR file to a destination file.
     *
     * @param key  The {@link Key} used for decryption.
     * @param src  The source encrypted Spring Boot JAR file.
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
     * Decrypts a Spring Boot JAR from an input stream to an output stream. This method iterates through the entries of
     * the JAR, decrypting them as necessary. It handles special cases for META-INF/MANIFEST.MF and nested JARs in
     * BOOT-INF/lib.
     *
     * @param key The {@link Key} used for decryption.
     * @param in  The input stream containing the encrypted Spring Boot JAR.
     * @param out The output stream where the decrypted JAR will be written.
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
            JarDecryptorProvider xJarDecryptor = new JarDecryptorProvider(decryptorProvider, level, filter);
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
                    String mainClass = attributes.getValue("Boot-Main-Class");
                    if (null != mainClass) {
                        attributes.putValue("Main-Class", mainClass);
                        attributes.remove(new Attributes.Name("Boot-Main-Class"));
                    }
                    Builder.removeKey(attributes);
                    JarArchiveEntry jarArchiveEntry = new JarArchiveEntry(entry.getName());
                    jarArchiveEntry.setTime(entry.getTime());
                    zos.putArchiveEntry(jarArchiveEntry);
                    manifest.write(nos);
                }
                // Handle BOOT-INF/classes/** entries
                else if (entry.getName().startsWith(Builder.BOOT_INF_CLASSES)) {
                    JarArchiveEntry jarArchiveEntry = new JarArchiveEntry(entry.getName());
                    jarArchiveEntry.setTime(entry.getTime());
                    zos.putArchiveEntry(jarArchiveEntry);
                    BootJarArchiveEntry bootJarArchiveEntry = new BootJarArchiveEntry(entry);
                    boolean filtered = on(bootJarArchiveEntry);
                    DecryptorProvider decryptor = filtered ? decryptorProvider : xNopDecryptor;
                    try (OutputStream eos = decryptor.decrypt(key, nos)) {
                        Builder.transfer(nis, eos);
                    }
                }
                // Handle BOOT-INF/lib/** entries (nested JARs)
                else if (entry.getName().startsWith(Builder.BOOT_INF_LIB)) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    CheckedOutputStream cos = new CheckedOutputStream(bos, new CRC32());
                    xJarDecryptor.decrypt(key, nis, cos);
                    JarArchiveEntry jar = new JarArchiveEntry(entry.getName());
                    jar.setMethod(JarArchiveEntry.STORED);
                    jar.setSize(bos.size());
                    jar.setTime(entry.getTime());
                    jar.setCrc(cos.getChecksum().getValue());
                    zos.putArchiveEntry(jar);
                    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                    Builder.transfer(bis, nos);
                }
                // Handle other entries (copy as-is)
                else {
                    JarArchiveEntry jarArchiveEntry = new JarArchiveEntry(entry.getName());
                    jarArchiveEntry.setTime(entry.getTime());
                    zos.putArchiveEntry(jarArchiveEntry);
                    Builder.transfer(nis, nos);
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
