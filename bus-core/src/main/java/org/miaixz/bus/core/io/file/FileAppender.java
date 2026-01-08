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
package org.miaixz.bus.core.io.file;

import java.io.File;
import java.io.PrintWriter;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.thread.lock.Lock;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * File appender. This class holds a file and accumulates a certain amount of data in memory before appending it to the
 * file in a batch operation. This class opens the file only when writing and closes it immediately after writing, so it
 * does not require explicit closing. Data appended using the {@code append} method is cached in memory and written to
 * the file only when the cache capacity is exceeded. Therefore, there may always be some content remaining in memory
 * that has not yet been written to the file. The {@code flush} method must be called at the end to ensure all remaining
 * content is written to the file.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileAppender implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852285590261L;

    /**
     * The internal {@link FileWriter} used for writing data to the file.
     */
    private final FileWriter writer;
    /**
     * The maximum number of lines to hold in memory before flushing to the file.
     */
    private final int capacity;
    /**
     * A flag indicating whether each appended content should be treated as a new line.
     */
    private final boolean isNewLineMode;
    /**
     * The internal list used to cache data lines before flushing them to the file.
     */
    private final List<String> list;
    /**
     * The lock used to protect write operations, ensuring thread safety. If {@code null} is provided during
     * construction, a no-op lock is used.
     */
    private final java.util.concurrent.locks.Lock lock;

    /**
     * Constructs a new {@code FileAppender} with the specified destination file, capacity, and new line mode. Uses
     * {@link Charset#UTF_8} as the default character set.
     *
     * @param destFile      The target file to append data to. Must not be {@code null}.
     * @param capacity      The number of lines to accumulate in memory before flushing to the file.
     * @param isNewLineMode {@code true} if each appended content should be treated as a new line; {@code false}
     *                      otherwise.
     */
    public FileAppender(final File destFile, final int capacity, final boolean isNewLineMode) {
        this(destFile, Charset.UTF_8, capacity, isNewLineMode);
    }

    /**
     * Constructs a new {@code FileAppender} with the specified destination file, character set, capacity, and new line
     * mode.
     *
     * @param destFile      The target file to append data to. Must not be {@code null}.
     * @param charset       The character set to use for writing to the file. Must not be {@code null}.
     * @param capacity      The number of lines to accumulate in memory before flushing to the file.
     * @param isNewLineMode {@code true} if each appended content should be treated as a new line; {@code false}
     *                      otherwise.
     */
    public FileAppender(final File destFile, final java.nio.charset.Charset charset, final int capacity,
            final boolean isNewLineMode) {
        this(destFile, charset, capacity, isNewLineMode, null);
    }

    /**
     * Constructs a new {@code FileAppender} with the specified destination file, character set, capacity, new line
     * mode, and an optional lock.
     *
     * @param destFile      The target file to append data to. Must not be {@code null}.
     * @param charset       The character set to use for writing to the file. Must not be {@code null}.
     * @param capacity      The number of lines to accumulate in memory before flushing to the file.
     * @param isNewLineMode {@code true} if each appended content should be treated as a new line; {@code false}
     *                      otherwise.
     * @param lock          An optional {@link java.util.concurrent.locks.Lock} to protect write operations for thread
     *                      safety. If {@code null}, a no-op lock is used.
     */
    public FileAppender(final File destFile, final java.nio.charset.Charset charset, final int capacity,
            final boolean isNewLineMode, final java.util.concurrent.locks.Lock lock) {
        this.capacity = capacity;
        this.list = new ArrayList<>(capacity);
        this.isNewLineMode = isNewLineMode;
        this.writer = FileWriter.of(destFile, charset);
        this.lock = ObjectKit.defaultIfNull(lock, Lock::getNoLock);
    }

    /**
     * Appends a line of text to the internal buffer. If the buffer capacity is reached, the buffer is flushed to the
     * file.
     *
     * @param line The line of text to append. Must not be {@code null}.
     * @return This {@code FileAppender} instance, allowing for method chaining.
     */
    public FileAppender append(final String line) {
        if (list.size() >= capacity) {
            flush();
        }

        this.lock.lock();
        try {
            list.add(line);
        } finally {
            this.lock.unlock();
        }
        return this;
    }

    /**
     * Flushes all accumulated data from the internal buffer to the file. After flushing, the internal buffer is
     * cleared.
     *
     * @return This {@code FileAppender} instance, allowing for method chaining.
     */
    public FileAppender flush() {
        this.lock.lock();
        try {
            try (final PrintWriter pw = writer.getPrintWriter(true)) {
                for (final String text : list) {
                    pw.print(text);
                    if (isNewLineMode) {
                        pw.println();
                    }
                }
            }
            list.clear();
        } finally {
            this.lock.unlock();
        }
        return this;
    }

}
