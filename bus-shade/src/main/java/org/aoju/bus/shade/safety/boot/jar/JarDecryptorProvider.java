/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.shade.safety.boot.jar;

import org.aoju.bus.shade.safety.Builder;
import org.aoju.bus.shade.safety.Complex;
import org.aoju.bus.shade.safety.algorithm.Key;
import org.aoju.bus.shade.safety.provider.DecryptorProvider;
import org.aoju.bus.shade.safety.provider.EntryDecryptorProvider;
import org.aoju.bus.shade.safety.streams.AlwaysInputStream;
import org.aoju.bus.shade.safety.streams.AlwaysOutputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.Deflater;

/**
 * 普通JAR包解密器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JarDecryptorProvider extends EntryDecryptorProvider<JarArchiveEntry> implements DecryptorProvider {

    private final int level;

    public JarDecryptorProvider(DecryptorProvider xEncryptor) {
        this(xEncryptor, new JarAllComplex());
    }

    public JarDecryptorProvider(DecryptorProvider decryptorProvider, Complex<JarArchiveEntry> filter) {
        this(decryptorProvider, Deflater.DEFLATED, filter);
    }

    public JarDecryptorProvider(DecryptorProvider xEncryptor, int level) {
        this(xEncryptor, level, new JarAllComplex());
    }

    public JarDecryptorProvider(DecryptorProvider decryptorProvider, int level, Complex<JarArchiveEntry> filter) {
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
            JarArchiveEntry entry;
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
                } else {
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
