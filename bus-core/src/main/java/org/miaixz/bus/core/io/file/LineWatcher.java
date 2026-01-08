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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.io.watch.SimpleWatcher;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;

/**
 * A {@link SimpleWatcher} implementation that processes newly added lines in a file, similar to the 'tail -f' command.
 * This class is designed to be used with a scheduled executor to periodically check for file modifications and read new
 * lines.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LineWatcher extends SimpleWatcher implements Runnable {

    /**
     * The {@link RandomAccessFile} instance used to read the file content.
     */
    private final RandomAccessFile randomAccessFile;
    /**
     * The character set used to decode the lines read from the file.
     */
    private final Charset charset;
    /**
     * The handler that consumes each new line read from the file.
     */
    private final ConsumerX<String> lineHandler;

    /**
     * Constructs a new {@code LineWatcher} instance.
     *
     * @param randomAccessFile The {@link RandomAccessFile} to monitor and read from. Must not be {@code null}.
     * @param charset          The character set of the file. Must not be {@code null}.
     * @param lineHandler      The handler for processing new lines. Must not be {@code null}.
     */
    public LineWatcher(final RandomAccessFile randomAccessFile, final Charset charset,
            final ConsumerX<String> lineHandler) {
        this.randomAccessFile = randomAccessFile;
        this.charset = charset;
        this.lineHandler = lineHandler;
    }

    /**
     * Manually triggers the line reading process. This method is typically called by a scheduled executor. It delegates
     * to {@link #onModify(WatchEvent, WatchKey)} to perform the actual file reading.
     */
    @Override
    public void run() {
        onModify(null, null);
    }

    /**
     * This method is called when a file modification is detected (or manually triggered by {@link #run()}). It reads
     * new lines from the end of the file and passes them to the configured line handler. If the file has been
     * truncated, the file pointer is reset to the current file length.
     *
     * @param event The watch event that triggered this method (can be {@code null} if manually triggered).
     * @param key   The watch key associated with the event (can be {@code null} if manually triggered).
     * @throws InternalException if an {@link IOException} occurs during file access.
     */
    @Override
    public void onModify(final WatchEvent<?> event, final WatchKey key) {
        try {
            final long currentLength = randomAccessFile.length();
            final long position = randomAccessFile.getFilePointer();

            if (currentLength < position) {
                // If the file was truncated or cleared, reset the pointer to the end of the current file.
                randomAccessFile.seek(currentLength);
            } else if (position < currentLength) {
                // Read new lines from the last known position to the end of the file.
                FileKit.readLines(randomAccessFile, charset, lineHandler);
                // Update the position to the new end of the file.
                randomAccessFile.seek(currentLength);
            }
            // If length is unchanged, do nothing.
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

}
