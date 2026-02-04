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
