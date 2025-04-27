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
package org.miaixz.bus.core.io.compress;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ZipKit;

/**
 * {@link ZipFile} 资源包装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipFileResource implements ZipResource {

    private final ZipFile zipFile;

    /**
     * 构造
     *
     * @param zipFile {@link ZipFile}
     */
    public ZipFileResource(final ZipFile zipFile) {
        this.zipFile = zipFile;
    }

    @Override
    public void read(final Consumer<ZipEntry> consumer, final int maxSizeDiff) {
        final Enumeration<? extends ZipEntry> em = zipFile.entries();
        while (em.hasMoreElements()) {
            consumer.accept(ZipSecurity.checkZipBomb(em.nextElement(), maxSizeDiff));
        }
    }

    @Override
    public InputStream get(final String path) {
        final ZipFile zipFile = this.zipFile;
        final ZipEntry entry = zipFile.getEntry(path);
        if (null != entry) {
            return ZipKit.getStream(zipFile, entry);
        }
        return null;
    }

    @Override
    public InputStream get(final ZipEntry entry) {
        return ZipKit.getStream(this.zipFile, entry);
    }

    @Override
    public void close() throws IOException {
        IoKit.closeQuietly(this.zipFile);
    }

}
