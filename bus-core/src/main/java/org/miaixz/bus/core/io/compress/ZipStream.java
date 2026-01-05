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
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * Wrapper for {@link ZipInputStream} resources.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZipStream implements ZipResource {

    /**
     * The underlying {@link ZipInputStream}.
     */
    private final ZipInputStream in;

    /**
     * Constructs a new ZipStream instance.
     *
     * @param in The {@link ZipInputStream} to wrap.
     */
    public ZipStream(final ZipInputStream in) {
        this.in = in;
    }

    /**
     * Read method.
     */
    @Override
    public void read(final Consumer<ZipEntry> consumer, final int maxSizeDiff) {
        try {
            ZipEntry zipEntry;
            while (null != (zipEntry = in.getNextEntry())) {
                consumer.accept(zipEntry);
                // ZipBomb check is performed after reading the content, so that information in the entry can be read
                // normally.
                ZipSecurity.checkZipBomb(zipEntry, maxSizeDiff);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the input stream for the given path.
     *
     * @param path the path to the entry
     * @return the input stream, or null if not found
     */
    @Override
    public InputStream get(final String path) {
        try {
            ZipEntry zipEntry;
            while (null != (zipEntry = in.getNextEntry())) {
                if (zipEntry.getName().equals(path)) {
                    return this.in;
                }
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return null;
    }

    /**
     * Gets the input stream for the given entry.
     *
     * @param entry the zip entry
     * @return the input stream
     */
    @Override
    public InputStream get(final ZipEntry entry) {
        return this.in;
    }

    /**
     * Close method.
     */
    @Override
    public void close() throws IOException {
        IoKit.closeQuietly(this.in);
    }

}
