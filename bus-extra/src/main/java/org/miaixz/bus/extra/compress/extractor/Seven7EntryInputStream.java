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
package org.miaixz.bus.extra.compress.extractor;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

/**
 * Wrapper for reading file streams during 7z decompression.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Seven7EntryInputStream extends InputStream {

    /**
     * The {@link SevenZFile} being read.
     */
    private final SevenZFile sevenZFile;
    /**
     * The total size of the entry in bytes.
     */
    private final long size;
    /**
     * The number of bytes already read from the stream.
     */
    private long readSize = 0;

    /**
     * Constructor.
     *
     * @param sevenZFile The {@link SevenZFile}.
     * @param entry      The {@link SevenZArchiveEntry}.
     */
    public Seven7EntryInputStream(final SevenZFile sevenZFile, final SevenZArchiveEntry entry) {
        this(sevenZFile, entry.getSize());
    }

    /**
     * Constructor.
     *
     * @param sevenZFile The {@link SevenZFile}.
     * @param size       The length to read.
     */
    public Seven7EntryInputStream(final SevenZFile sevenZFile, final long size) {
        this.sevenZFile = sevenZFile;
        this.size = size;
    }

    /**
     * Returns an estimate of the number of bytes that can be read.
     * <p>
     * This implementation returns the total size of the entry as an int, throwing an IOException if the size exceeds
     * Integer.MAX_VALUE.
     * </p>
     *
     * @return an estimate of the number of bytes that can be read
     * @throws IOException if the size exceeds Integer.MAX_VALUE
     */
    @Override
    public int available() throws IOException {
        try {
            return Math.toIntExact(this.size);
        } catch (final ArithmeticException e) {
            throw new IOException("Entry size is too large!(max than Integer.MAX)", e);
        }
    }

    /**
     * Gets the number of bytes read.
     *
     * @return The number of bytes read.
     */
    public long getReadSize() {
        return this.readSize;
    }

    /**
     * Reads the next byte of data.
     * <p>
     * This implementation reads a single byte from the underlying 7Z archive entry and increments the read size
     * counter.
     * </p>
     *
     * @return the next byte of data, or -1 if the end of the stream is reached
     * @throws IOException if an I/O error occurs
     */
    @Override
    public int read() throws IOException {
        this.readSize++;
        return this.sevenZFile.read();
    }

}
