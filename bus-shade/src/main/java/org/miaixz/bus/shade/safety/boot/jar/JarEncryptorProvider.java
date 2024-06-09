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
package org.miaixz.bus.shade.safety.boot.jar;

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

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.Deflater;

/**
 * 普通JAR包加密器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JarEncryptorProvider extends EntryEncryptorProvider<JarArchiveEntry> implements EncryptorProvider {

    private final int level;
    private final int mode;

    public JarEncryptorProvider(EncryptorProvider encryptorProvider) {
        this(encryptorProvider, new JarAllComplex());
    }

    public JarEncryptorProvider(EncryptorProvider encryptorProvider, Complex<JarArchiveEntry> filter) {
        this(encryptorProvider, Deflater.DEFLATED, filter);
    }

    public JarEncryptorProvider(EncryptorProvider encryptorProvider, int level) {
        this(encryptorProvider, level, new JarAllComplex());
    }

    public JarEncryptorProvider(EncryptorProvider encryptorProvider, int level, Complex<JarArchiveEntry> filter) {
        this(encryptorProvider, level, Builder.MODE_NORMAL, filter);
    }

    public JarEncryptorProvider(EncryptorProvider encryptorProvider, int level, int mode) {
        this(encryptorProvider, level, mode, new JarAllComplex());
    }

    public JarEncryptorProvider(EncryptorProvider encryptorProvider, int level, int mode, Complex<JarArchiveEntry> filter) {
        super(encryptorProvider, filter);
        this.level = level;
        this.mode = mode;
    }

    @Override
    public void encrypt(Key key, File src, File dest) throws IOException {
        try (
                FileInputStream fis = new FileInputStream(src);
                FileOutputStream fos = new FileOutputStream(dest)
        ) {
            encrypt(key, fis, fos);
        }
    }

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
                if (entry.getName().startsWith(Builder.XJAR_SRC_DIR)
                        || entry.getName().endsWith(Builder.XJAR_INF_DIR)
                        || entry.getName().endsWith(Builder.XJAR_INF_DIR + Builder.XJAR_INF_IDX)
                ) {
                    continue;
                }
                if (entry.isDirectory()) {
                    JarArchiveEntry jarArchiveEntry = new JarArchiveEntry(entry.getName());
                    jarArchiveEntry.setTime(entry.getTime());
                    zos.putArchiveEntry(jarArchiveEntry);
                } else if (entry.getName().equals(Builder.META_INF_MANIFEST)) {
                    manifest = new Manifest(nis);
                    Attributes attributes = manifest.getMainAttributes();
                    String mainClass = attributes.getValue("Main-Class");
                    if (null != mainClass) {
                        attributes.putValue("Jar-Main-Class", mainClass);
                        attributes.putValue("Main-Class", "org.miaixz.bus.shade.safety.archive.jar.BootJarLauncher");
                    }
                    if ((mode & Builder.FLAG_DANGER) == Builder.FLAG_DANGER) {
                        Builder.retainKey(key, attributes);
                    }
                    JarArchiveEntry jarArchiveEntry = new JarArchiveEntry(entry.getName());
                    jarArchiveEntry.setTime(entry.getTime());
                    zos.putArchiveEntry(jarArchiveEntry);
                    manifest.write(nos);
                } else {
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

            String mainClass = null != manifest && null != manifest.getMainAttributes() ? manifest.getMainAttributes().getValue("Main-Class") : null;
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
