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
package org.miaixz.bus.shade.safety.boot.jar;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.Deflater;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.Complex;
import org.miaixz.bus.shade.safety.Injector;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.provider.EncryptorProvider;
import org.miaixz.bus.shade.safety.provider.EntryEncryptorProvider;
import org.miaixz.bus.shade.safety.streams.AlwaysInputStream;
import org.miaixz.bus.shade.safety.streams.AlwaysOutputStream;

/**
 * A {@link EncryptorProvider} implementation for encrypting standard JAR files. This provider handles the structure of
 * JARs and applies encryption based on a provided filter and key. It also modifies the manifest to redirect the main
 * class to a custom launcher.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JarEncryptorProvider extends EntryEncryptorProvider<JarArchiveEntry> implements EncryptorProvider {

    /**
     * The compression level to use for the output JAR archive.
     */
    private final int level;
    /**
     * The encryption mode, which can include flags like {@link Builder#FLAG_DANGER}.
     */
    private final int mode;

    /**
     * Constructs a {@code JarEncryptorProvider} with a delegate encryptor and a default filter. The default filter is
     * {@link JarAllComplex}, meaning all entries will be considered for encryption.
     *
     * @param encryptorProvider The delegate encryptor provider that performs the actual encryption.
     */
    public JarEncryptorProvider(EncryptorProvider encryptorProvider) {
        this(encryptorProvider, new JarAllComplex());
    }

    /**
     * Constructs a {@code JarEncryptorProvider} with a delegate encryptor and a custom filter. Uses default compression
     * level ({@link Deflater#DEFLATED}).
     *
     * @param encryptorProvider The delegate encryptor provider that performs the actual encryption.
     * @param filter            The {@link Complex} filter to apply to JAR entries. Only entries matching the filter
     *                          will be encrypted.
     */
    public JarEncryptorProvider(EncryptorProvider encryptorProvider, Complex<JarArchiveEntry> filter) {
        this(encryptorProvider, Deflater.DEFLATED, filter);
    }

    /**
     * Constructs a {@code JarEncryptorProvider} with a delegate encryptor and a specified compression level. Uses a
     * default filter ({@link JarAllComplex}).
     *
     * @param encryptorProvider The delegate encryptor provider that performs the actual encryption.
     * @param level             The compression level for the output JAR archive.
     */
    public JarEncryptorProvider(EncryptorProvider encryptorProvider, int level) {
        this(encryptorProvider, level, new JarAllComplex());
    }

    /**
     * Constructs a {@code JarEncryptorProvider} with a delegate encryptor, a specified compression level, and a custom
     * filter. Uses default encryption mode ({@link Builder#MODE_NORMAL}).
     *
     * @param encryptorProvider The delegate encryptor provider that performs the actual encryption.
     * @param level             The compression level for the output JAR archive.
     * @param filter            The {@link Complex} filter to apply to JAR entries. Only entries matching the filter
     *                          will be encrypted.
     */
    public JarEncryptorProvider(EncryptorProvider encryptorProvider, int level, Complex<JarArchiveEntry> filter) {
        this(encryptorProvider, level, Builder.MODE_NORMAL, filter);
    }

    /**
     * Constructs a {@code JarEncryptorProvider} with a delegate encryptor, a specified compression level, and an
     * encryption mode. Uses a default filter ({@link JarAllComplex}).
     *
     * @param encryptorProvider The delegate encryptor provider that performs the actual encryption.
     * @param level             The compression level for the output JAR archive.
     * @param mode              The encryption mode (e.g., {@link Builder#FLAG_DANGER}).
     */
    public JarEncryptorProvider(EncryptorProvider encryptorProvider, int level, int mode) {
        this(encryptorProvider, level, mode, new JarAllComplex());
    }

    /**
     * Constructs a {@code JarEncryptorProvider} with all specified parameters.
     *
     * @param encryptorProvider The delegate encryptor provider that performs the actual encryption.
     * @param level             The compression level for the output JAR archive.
     * @param mode              The encryption mode (e.g., {@link Builder#FLAG_DANGER}).
     * @param filter            The {@link Complex} filter to apply to JAR entries. Only entries matching the filter
     *                          will be encrypted.
     */
    public JarEncryptorProvider(EncryptorProvider encryptorProvider, int level, int mode,
            Complex<JarArchiveEntry> filter) {
        super(encryptorProvider, filter);
        this.level = level;
        this.mode = mode;
    }

    /**
     * Encrypts a source JAR file to a destination file.
     *
     * @param key  The {@link Key} used for encryption.
     * @param src  The source unencrypted JAR file.
     * @param dest The destination file for the encrypted JAR.
     * @throws IOException If an I/O error occurs during encryption.
     */
    @Override
    public void encrypt(Key key, File src, File dest) throws IOException {
        try (FileInputStream fis = new FileInputStream(src); FileOutputStream fos = new FileOutputStream(dest)) {
            encrypt(key, fis, fos);
        }
    }

    /**
     * Encrypts a JAR archive from an input stream to an output stream. This method iterates through the entries of the
     * JAR, encrypting them as necessary. It handles special cases for META-INF/MANIFEST.MF and injects framework
     * resources.
     *
     * @param key The {@link Key} used for encryption.
     * @param in  The input stream containing the unencrypted JAR archive.
     * @param out The output stream where the encrypted JAR will be written.
     * @throws IOException If an I/O error occurs during encryption.
     */
    @Override
    public void encrypt(Key key, InputStream in, OutputStream out) throws IOException {
        JarArchiveInputStream zis = null;
        JarArchiveOutputStream zos = null;
        Set<String> indexes = new LinkedHashSet<>();
        try {
            zis = new JarArchiveInputStream(in);
            zos = new JarArchiveOutputStream(out);
            zos.setLevel(level);
            AlwaysInputStream nis = new AlwaysInputStream(zis);
            AlwaysOutputStream nos = new AlwaysOutputStream(zos);
            JarArchiveEntry entry;
            Manifest manifest = null;
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
                    manifest = new Manifest(nis);
                    Attributes attributes = manifest.getMainAttributes();
                    String mainClass = attributes.getValue("Main-Class");
                    if (null != mainClass) {
                        attributes.putValue("Jar-Main-Class", mainClass);
                        attributes.putValue("Main-Class", "org.miaixz.bus.shade.safety.boot.jar.JarLauncher");
                    }
                    if ((mode & Builder.FLAG_DANGER) == Builder.FLAG_DANGER) {
                        Builder.retainKey(key, attributes);
                    }
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
                    if (filtered) {
                        indexes.add(entry.getName());
                    }
                    EncryptorProvider encryptor = filtered ? encryptorProvider : xNopEncryptor;
                    try (OutputStream eos = encryptor.encrypt(key, nos)) {
                        Builder.transfer(nis, eos);
                    }
                }
                zos.closeArchiveEntry();
            }

            // Write index for encrypted entries
            if (!indexes.isEmpty()) {
                JarArchiveEntry xjarInfDir = new JarArchiveEntry(Builder.XJAR_INF_DIR);
                xjarInfDir.setTime(System.currentTimeMillis());
                zos.putArchiveEntry(xjarInfDir);
                zos.closeArchiveEntry();

                JarArchiveEntry xjarInfIdx = new JarArchiveEntry(Builder.XJAR_INF_DIR + Builder.XJAR_INF_IDX);
                xjarInfIdx.setTime(System.currentTimeMillis());
                zos.putArchiveEntry(xjarInfIdx);
                for (String index : indexes) {
                    zos.write(index.getBytes());
                    zos.write(Builder.CRLF.getBytes());
                }
                zos.closeArchiveEntry();
            }

            // Inject framework resources if a main class was found
            String mainClass = null != manifest && null != manifest.getMainAttributes()
                    ? manifest.getMainAttributes().getValue("Main-Class")
                    : null;
            if (null != mainClass) {
                Injector.inject(zos);
            }

            zos.finish();
        } finally {
            Builder.close(zis);
            Builder.close(zos);
        }
    }

}
