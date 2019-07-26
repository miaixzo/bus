package org.aoju.bus.core.io;

import java.io.IOException;

/**
 * A {@link Source} which peeks into an upstream {@link BufferedSource} and allows reading and
 * expanding of the buffered data without consuming it. Does this by requesting additional data from
 * the upstream source if needed and copying out of the internal buffer of the upstream source if
 * possible.
 *
 * <p>This source also maintains a snapshot of the starting location of the upstream buffer which it
 * validates against on every read. If the upstream buffer is read from, this source will become
 * invalid and throw {@link IllegalStateException} on any future reads.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
final class PeekSource implements Source {

    private final BufferedSource upstream;
    private final Buffer buffer;

    private Segment expectedSegment;
    private int expectedPos;
    private boolean closed;
    private long pos;

    PeekSource(BufferedSource upstream) {
        this.upstream = upstream;
        this.buffer = upstream.buffer();
        this.expectedSegment = buffer.head;
        this.expectedPos = expectedSegment != null ? expectedSegment.pos : -1;
    }

    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        if (closed) throw new IllegalStateException("closed");

        // Source becomes invalid if there is an expected Segment and it and the expected position
        // do not match the current head and head position of the upstream buffer
        if (expectedSegment != null
                && (expectedSegment != buffer.head || expectedPos != buffer.head.pos)) {
            throw new IllegalStateException("Peek source is invalid because upstream source was used");
        }

        upstream.request(pos + byteCount);
        if (expectedSegment == null && buffer.head != null) {
            // Only once the buffer actually holds data should an expected Segment and position be
            // recorded. This allows reads from the peek source to repeatedly return -1 and for data to be
            // added later. Unit tests depend on this behavior.
            expectedSegment = buffer.head;
            expectedPos = buffer.head.pos;
        }

        long toCopy = Math.min(byteCount, buffer.size - pos);
        if (toCopy <= 0L) return -1L;

        buffer.copyTo(sink, pos, toCopy);
        pos += toCopy;
        return toCopy;
    }

    @Override
    public Timeout timeout() {
        return upstream.timeout();
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }

}
