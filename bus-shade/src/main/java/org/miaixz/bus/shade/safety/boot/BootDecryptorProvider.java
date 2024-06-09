/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;

/**
 * Spring-Boot JAR包解密器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BootDecryptorProvider extends EntryDecryptorProvider<JarArchiveEntry>
        implements DecryptorProvider {

    private final int level;

    public BootDecryptorProvider(DecryptorProvider xEncryptor) {
        this(xEncryptor, new JarAllComplex());
    }

    public BootDecryptorProvider(DecryptorProvider decryptorProvider, Complex<JarArchiveEntry> filter) {
        this(decryptorProvider, Deflater.DEFLATED, filter);
    }

    public BootDecryptorProvider(DecryptorProvider xEncryptor, int level) {
        this(xEncryptor, level, new JarAllComplex());
    }

    public BootDecryptorProvider(DecryptorProvider decryptorProvider, int level, Complex<JarArchiveEntry> filter) {
        super(decryptorProvider, filter);
        this.level = level;
    }

    @Override
    public void decrypt(Key key, File src, File dest) throws IOException {
        try (
                FileInputStream fis = new FileInputStream(src);
                FileOutputStream fos = new FileOutputStream(dest)
        ) {
            decrypt(key, fis, fos);
        }
    }

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
                if (entry.getName().startsWith(Builder.XJAR_SRC_DIR)
                        || entry.getName().endsWith(Builder.XJAR_INF_DIR)
                        || entry.getName().endsWith(Builder.XJAR_INF_DIR + Builder.XJAR_INF_IDX)
                ) {
                    continue;
                }
                // DIR ENTRY
                if (entry.isDirectory()) {
                    JarArchiveEntry jarArchiveEntry = new JarArchiveEntry(entry.getName());
                    jarArchiveEntry.setTime(entry.getTime());
                    zos.putArchiveEntry(jarArchiveEntry);
                }
                // META-INF/MANIFEST.MF
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
                // BOOT-INF/classes/**
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
                // BOOT-INF/lib/**
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
                // OTHER
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
