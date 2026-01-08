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
package org.miaixz.bus.core.io.copier;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.miaixz.bus.core.io.StreamProgress;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * Copies data from a {@link Reader} to a {@link Writer}. This class extends {@link IoCopier} to provide
 * character-stream-specific copying functionality.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReaderWriterCopier extends IoCopier<Reader, Writer> {

    /**
     * Constructs a {@code ReaderWriterCopier} with a default buffer size of 8192 characters.
     */
    public ReaderWriterCopier() {
        this(Normal._8192);
    }

    /**
     * Constructs a {@code ReaderWriterCopier} with the specified buffer size.
     *
     * @param bufferSize The size of the character buffer to use for copying.
     */
    public ReaderWriterCopier(final int bufferSize) {
        this(bufferSize, -1);
    }

    /**
     * Constructs a {@code ReaderWriterCopier} with the specified buffer size and total count of characters to copy.
     *
     * @param bufferSize The size of the character buffer to use for copying.
     * @param count      The total number of characters to copy. If -1, copy until the end of the source.
     */
    public ReaderWriterCopier(final int bufferSize, final long count) {
        this(bufferSize, count, null);
    }

    /**
     * Constructs a {@code ReaderWriterCopier} with the specified buffer size, total count of characters to copy, and a
     * progress listener.
     *
     * @param bufferSize The size of the character buffer to use for copying.
     * @param count      The total number of characters to copy. If -1, copy until the end of the source.
     * @param progress   The progress listener to report copy progress.
     */
    public ReaderWriterCopier(final int bufferSize, final long count, final StreamProgress progress) {
        super(bufferSize, count, progress);
    }

    /**
     * Copies data from the source {@link Reader} to the target {@link Writer}.
     *
     * @param source The source {@link Reader}.
     * @param target The target {@link Writer}.
     * @return The total number of characters copied.
     * @throws InternalException if an {@link IOException} occurs during the copy operation.
     */
    @Override
    public long copy(final Reader source, final Writer target) {
        Assert.notNull(source, "InputStream is null !"); // Should be Reader, not InputStream
        Assert.notNull(target, "OutputStream is null !"); // Should be Writer, not OutputStream

        final StreamProgress progress = this.progress;
        if (null != progress) {
            progress.start();
        }
        final long size;
        try {
            size = doCopy(source, target, new char[bufferSize(this.count)], progress);
            target.flush();
        } catch (final IOException e) {
            throw new InternalException(e);
        }

        if (null != progress) {
            progress.finish();
        }
        return size;
    }

    /**
     * Performs the actual copy operation. If a maximum length is specified, it reads up to that length; otherwise, it
     * reads until the end of the source (when -1 is encountered).
     *
     * @param source   The {@link Reader} to read from.
     * @param target   The {@link Writer} to write to.
     * @param buffer   The character array buffer used for copying.
     * @param progress The progress listener to report copy progress, can be {@code null}.
     * @return The total number of characters copied.
     * @throws IOException If an I/O error occurs during the copy.
     */
    private long doCopy(final Reader source, final Writer target, final char[] buffer, final StreamProgress progress)
            throws IOException {
        long numToRead = this.count > 0 ? this.count : Long.MAX_VALUE;
        long total = 0;

        int read;
        while (numToRead > 0) {
            read = source.read(buffer, 0, bufferSize(numToRead));
            if (read < 0) {
                // Reached end of stream prematurely
                break;
            }
            target.write(buffer, 0, read);
            if (flushEveryBuffer) {
                target.flush();
            }

            numToRead -= read;
            total += read;
            if (null != progress) {
                progress.progress(this.count, total);
            }
        }

        return total;
    }

}
