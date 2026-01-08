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

import java.io.IOException;

import org.miaixz.bus.core.io.SectionBuffer;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;

/**
 * A {@link Source} that can peek into an upstream {@link BufferSource} and allows reading and expanding buffered data
 * without consuming it. This is achieved by requesting additional data from the upstream source if needed, and copying
 * from the upstream source's internal buffer. This source also maintains a snapshot of the starting position of its
 * upstream buffer, which is validated on each read. If the upstream buffer is read from directly, this source becomes
 * invalid and will throw an {@link IllegalStateException} on subsequent reads.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PeekSource implements Source {

    /**
     * The upstream {@link BufferSource} from which data is peeked.
     */
    private final BufferSource upstream;
    /**
     * The internal buffer of the upstream source, used for peeking.
     */
    private final Buffer buffer;

    /**
     * The expected {@link SectionBuffer} head of the upstream buffer at the time this peek source was created or last
     * reset.
     */
    private SectionBuffer expectedSegment;
    /**
     * The expected position within the {@link #expectedSegment} of the upstream buffer.
     */
    private int expectedPos;
    /**
     * A flag indicating whether this peek source has been closed.
     */
    private boolean closed;
    /**
     * The current position within the peeked data, relative to the start of the upstream buffer.
     */
    private long pos;

    /**
     * Constructs a new {@code PeekSource} that peeks into the given {@link BufferSource}. The peek source will maintain
     * a view of the upstream buffer without consuming its data.
     *
     * @param upstream The {@link BufferSource} to peek into.
     */
    public PeekSource(BufferSource upstream) {
        this.upstream = upstream;
        this.buffer = upstream.getBuffer();
        this.expectedSegment = buffer.head;
        this.expectedPos = expectedSegment != null ? expectedSegment.pos : -1;
    }

    /**
     * Reads at least 1 byte and at most {@code byteCount} bytes from this peek source and appends them to {@code sink}.
     * This method does not consume data from the underlying {@link BufferSource}. If the underlying source has been
     * consumed directly, this method will throw an {@link IllegalStateException}.
     *
     * @param sink      The buffer to which bytes will be appended.
     * @param byteCount The maximum number of bytes to read.
     * @return The number of bytes read, or -1 if this source has been exhausted.
     * @throws IOException              If an I/O error occurs during reading.
     * @throws IllegalArgumentException If {@code byteCount} is negative.
     * @throws IllegalStateException    If this peek source is closed or if the upstream source was used directly.
     */
    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        if (byteCount < 0)
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        if (closed)
            throw new IllegalStateException("closed");

        // If a SectionBuffer exists, and its position does not match the buffer's position,
        // the source becomes invalid.
        if (expectedSegment != null && (expectedSegment != buffer.head || expectedPos != buffer.head.pos)) {
            throw new IllegalStateException("Peek source is invalid because upstream source was used");
        }
        if (byteCount == 0L)
            return 0L;
        if (!upstream.request(pos + 1))
            return -1L;

        if (expectedSegment == null && buffer.head != null) {
            // Only record the expected SectionBuffer and position when the buffer actually holds data.
            expectedSegment = buffer.head;
            expectedPos = buffer.head.pos;
        }

        long toCopy = Math.min(byteCount, buffer.size - pos);
        buffer.copyTo(sink, pos, toCopy);
        pos += toCopy;
        return toCopy;
    }

    /**
     * Returns the timeout for this peek source, which is delegated to the upstream source.
     *
     * @return The timeout instance of the upstream source.
     */
    @Override
    public Timeout timeout() {
        return upstream.timeout();
    }

    /**
     * Closes this peek source. Once closed, further read operations will throw an {@link IllegalStateException}.
     *
     * @throws IOException If an I/O error occurs during closing (though this implementation does not throw one).
     */
    @Override
    public void close() throws IOException {
        closed = true;
    }

}
