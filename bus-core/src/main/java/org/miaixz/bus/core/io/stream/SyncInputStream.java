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
package org.miaixz.bus.core.io.stream;

import java.io.*;

import org.miaixz.bus.core.io.StreamProgress;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A synchronous input stream wrapper that can convert the wrapped stream into a {@link ByteArrayInputStream}, allowing
 * its content to be held in memory and the original stream to be closed.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SyncInputStream extends FilterInputStream {

    /**
     * The length of the stream content. A value of -1 indicates an unknown length.
     */
    private final long length;
    /**
     * Flag indicating whether EOF (End-Of-File) errors should be ignored. In HTTP, for Transfer-Encoding: Chunked, a
     * zero-length chunk usually signals the end. If the server does not follow this specification or the response does
     * not end normally, an EOF exception may occur. This option determines whether to ignore such exceptions.
     */
    private final boolean isIgnoreEOFError;
    /**
     * Flag indicating whether the stream is handled asynchronously. If {@code true}, the original stream is held. If
     * {@code false}, the entire body content is read into a {@link ByteArrayInputStream} during initialization.
     */
    private volatile boolean asyncFlag = true;

    /**
     * Constructs a new {@code SyncInputStream}. If {@code isAsync} is {@code true}, the original stream is held
     * directly. If {@code isAsync} is {@code false}, the stream content up to the given {@code length} is read into a
     * {@link ByteArrayInputStream} for later use.
     *
     * @param in               The input stream to wrap.
     * @param length           The expected length of the stream content. Use -1 for unknown length.
     * @param isAsync          {@code true} to hold the original stream (asynchronous), {@code false} to read content
     *                         into memory (synchronous).
     * @param isIgnoreEOFError {@code true} to ignore EOF errors that may occur due to malformed stream endings,
     *                         {@code false} otherwise.
     */
    public SyncInputStream(final InputStream in, final long length, final boolean isAsync,
            final boolean isIgnoreEOFError) {
        super(in);
        this.length = length;
        this.isIgnoreEOFError = isIgnoreEOFError;
        if (!isAsync) {
            sync();
        }
    }

    /**
     * Checks if the given {@link Throwable} is an EOF (End-Of-File) related exception. This includes:
     * <ul>
     * <li>{@link FileNotFoundException}: Indicates no content returned from the server.</li>
     * <li>{@link EOFException}: Standard EOF exception.</li>
     * <li>Exceptions whose message contains "Premature EOF" (case-insensitive).</li>
     * </ul>
     *
     * @param e The {@link Throwable} to check.
     * @return {@code true} if the exception is an EOF-related exception, {@code false} otherwise.
     */
    private static boolean isEOFException(final Throwable e) {
        if (e instanceof FileNotFoundException) {
            // No content returned from the server, ignore it.
            return true;
        }
        return e instanceof EOFException || StringKit.containsIgnoreCase(e.getMessage(), "Premature EOF");
    }

    /**
     * Synchronizes the stream data into memory by reading all content into a {@link ByteArrayInputStream}. After
     * synchronization, the original underlying stream is closed.
     *
     * @return This {@code SyncInputStream} instance for method chaining.
     */
    public SyncInputStream sync() {
        if (asyncFlag) {
            this.in = new ByteArrayInputStream(readBytes());
            this.asyncFlag = false;
        }

        return this;
    }

    /**
     * Reads all available bytes from the input stream into a byte array.
     *
     * @return A byte array containing all bytes read from the stream. Returns an empty array if no bytes are read.
     * @throws InternalException If an I/O error occurs during reading, unless it's an ignorable EOF error.
     */
    public byte[] readBytes() {
        final FastByteArrayOutputStream bytesOut = new FastByteArrayOutputStream(length > 0 ? (int) length : 1024);
        final long length = copyTo(bytesOut, null);
        return length > 0 ? bytesOut.toByteArray() : new byte[0];
    }

    /**
     * Copies the content of this input stream to the specified output stream. The input stream is closed after the copy
     * operation.
     *
     * @param out            The {@link OutputStream} to copy the content to.
     * @param streamProgress An optional {@link StreamProgress} listener to monitor the copy progress. Can be
     *                       {@code null}.
     * @return The total number of bytes copied.
     * @throws InternalException If an I/O error occurs during copying, unless it's an ignorable EOF error.
     */
    public long copyTo(final OutputStream out, final StreamProgress streamProgress) {
        long copyLength = -1;
        try {
            copyLength = IoKit.copy(this.in, out, Normal._8192, this.length, streamProgress);
        } catch (final InternalException e) {
            if (!(isIgnoreEOFError && isEOFException(e.getCause()))) {
                throw e;
            }
            // Ignore EOF errors in the stream.
        } finally {
            // Close the input stream after reading.
            IoKit.closeQuietly(in);
        }
        return copyLength;
    }

}
