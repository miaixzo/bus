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
package org.miaixz.bus.core.io.source;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.miaixz.bus.core.io.LifeCycle;
import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * A {@link Source} that decompresses data from another source using the
 * <a href="http://tools.ietf.org/html/rfc1951">DEFLATE</a> algorithm. This class wraps a {@link BufferSource} and uses
 * an {@link Inflater} to perform the decompression, writing the decompressed data to a {@link Buffer}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class InflaterSource implements Source {

    /**
     * The underlying {@link BufferSource} providing the compressed data.
     */
    private final BufferSource source;
    /**
     * The {@link Inflater} instance used for decompression.
     */
    private final Inflater inflater;

    /**
     * The number of bytes from the {@code source} buffer that are currently held by the {@code inflater}. This is used
     * to track how many bytes need to be skipped from the {@code source} buffer once the {@code inflater} has consumed
     * them.
     */
    private int bufferBytesHeldByInflater;
    /**
     * A flag indicating whether this source has been closed.
     */
    private boolean closed;

    /**
     * Constructs an {@code InflaterSource} that decompresses data from the given {@link Source}. The provided source
     * will be buffered internally.
     *
     * @param source   The source of compressed data.
     * @param inflater The inflater to use for decompression.
     * @throws IllegalArgumentException If {@code source} or {@code inflater} is null.
     */
    public InflaterSource(Source source, Inflater inflater) {
        this(IoKit.buffer(source), inflater);
    }

    /**
     * Internal constructor that shares a buffer with its trusted caller. We cannot generally share a
     * {@link BufferSource} because the inflater retains input bytes until they are deflated.
     *
     * @param source   The buffered source of compressed data.
     * @param inflater The inflater to use for decompression.
     * @throws IllegalArgumentException If {@code source} or {@code inflater} is null.
     */
    InflaterSource(BufferSource source, Inflater inflater) {
        if (source == null)
            throw new IllegalArgumentException("source == null");
        if (inflater == null)
            throw new IllegalArgumentException("inflater == null");
        this.source = source;
        this.inflater = inflater;
    }

    /**
     * Reads at least 1 byte and at most {@code byteCount} bytes of decompressed data from this source and appends them
     * to {@code sink}. Returns the number of bytes read, or -1 if this source has been exhausted.
     *
     * <p>
     * This method handles refilling the inflater with compressed data from the underlying source and decompressing it
     * into the sink buffer.
     *
     * @param sink      The buffer to which decompressed bytes will be appended.
     * @param byteCount The maximum number of bytes to read.
     * @return The number of bytes read, or -1 if the source is exhausted.
     * @throws IOException              If an I/O error occurs during decompression or if the compressed data is
     *                                  malformed.
     * @throws IllegalArgumentException If {@code byteCount} is negative.
     * @throws IllegalStateException    If this source is closed.
     */
    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        if (byteCount < 0)
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        if (closed)
            throw new IllegalStateException("closed");
        if (byteCount == 0)
            return 0;

        while (true) {
            boolean sourceExhausted = refill();

            // Decompress data from the buffer into the sink.
            try {
                SectionBuffer tail = sink.writableSegment(1);
                int toRead = (int) Math.min(byteCount, SectionBuffer.SIZE - tail.limit);
                int bytesInflated = inflater.inflate(tail.data, tail.limit, toRead);
                if (bytesInflated > 0) {
                    tail.limit += bytesInflated;
                    sink.size += bytesInflated;
                    return bytesInflated;
                }
                if (inflater.finished() || inflater.needsDictionary()) {
                    releaseInflatedBytes();
                    if (tail.pos == tail.limit) {
                        // A tail segment was allocated but not ultimately needed. Recycle!
                        sink.head = tail.pop();
                        LifeCycle.recycle(tail);
                    }
                    return -1;
                }
                if (sourceExhausted)
                    throw new EOFException("source exhausted prematurely");
            } catch (DataFormatException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * Refills the inflater with compressed data if it needs input. This method is only effective when the inflater
     * requires more input. It returns true if the underlying source is exhausted while the inflater still needs input.
     *
     * @return True if the source is exhausted and the inflater still needs input, false otherwise.
     * @throws IOException           If an I/O error occurs while reading from the source.
     * @throws IllegalStateException If the inflater has remaining bytes after being reset.
     */
    public boolean refill() throws IOException {
        if (!inflater.needsInput())
            return false;

        releaseInflatedBytes();
        if (inflater.getRemaining() != 0)
            throw new IllegalStateException("?"); // Should not happen if releaseInflatedBytes is called correctly.

        // If there are compressed bytes in the source, assign them to the inflater.
        if (source.exhausted()) {
            return true;
        }

        // Assign buffer bytes to the inflater.
        SectionBuffer head = source.getBuffer().head;
        bufferBytesHeldByInflater = head.limit - head.pos;
        inflater.setInput(head.data, head.pos, bufferBytesHeldByInflater);
        return false;
    }

    /**
     * Releases the bytes from the underlying source's buffer that have already been consumed by the inflater. This
     * method should be called after the inflater has processed a portion of the input buffer.
     *
     * @throws IOException If an I/O error occurs while skipping bytes from the source.
     */
    private void releaseInflatedBytes() throws IOException {
        if (bufferBytesHeldByInflater == 0)
            return;
        int toRelease = bufferBytesHeldByInflater - inflater.getRemaining();
        bufferBytesHeldByInflater -= toRelease;
        source.skip(toRelease);
    }

    /**
     * Returns the timeout for this source, delegated to the underlying source.
     *
     * @return The timeout instance.
     */
    @Override
    public Timeout timeout() {
        return source.timeout();
    }

    /**
     * Closes this source and the underlying source, releasing any resources held by them. This method also ends the
     * {@link Inflater} to release native resources. This method can be called multiple times safely.
     *
     * @throws IOException If an I/O error occurs during closing.
     */
    @Override
    public void close() throws IOException {
        if (closed)
            return;
        inflater.end();
        closed = true;
        source.close();
    }

}
