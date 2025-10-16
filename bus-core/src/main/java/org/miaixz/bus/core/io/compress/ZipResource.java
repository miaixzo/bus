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

import java.io.Closeable;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;

/**
 * Represents a Zip resource, such as a Zip stream resource or a Zip file resource.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface ZipResource extends Closeable {

    /**
     * Reads and processes each {@link ZipEntry} in the Zip file.
     *
     * @param consumer    The {@link ZipEntry} processor.
     * @param maxSizeDiff The maximum size difference multiplier for ZipBomb check. A value of -1 indicates no ZipBomb
     *                    check.
     */
    void read(final Consumer<ZipEntry> consumer, final int maxSizeDiff);

    /**
     * Retrieves the input stream for a specific entry within the Zip file. If in file mode, it directly gets the stream
     * corresponding to the entry. If in stream mode, it iterates through entries to find and return the corresponding
     * stream.
     *
     * @param path The path of the entry.
     * @return The input stream for the specified entry.
     */
    InputStream get(String path);

    /**
     * Retrieves the input stream corresponding to the specified {@link ZipEntry}.
     *
     * @param entry The {@link ZipEntry}.
     * @return The input stream for the specified entry.
     */
    InputStream get(ZipEntry entry);

}
