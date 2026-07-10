/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.io.sink;

import java.io.IOException;
import java.io.OutputStream;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.buffer.Segment;
import org.miaixz.bus.core.io.buffer.SegmentAllocator;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * A {@link Sink} implementation that writes buffer segments to an {@link OutputStream}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class OutputStreamSink implements Sink {

    /**
     * Target output stream.
     */
    private final OutputStream outputStream;

    /**
     * Timeout applied before each write attempt.
     */
    private final Timeout timeout;

    /**
     * Creates an output stream backed sink.
     *
     * @param outputStream the target output stream
     * @param timeout      the timeout information
     */
    public OutputStreamSink(OutputStream outputStream, Timeout timeout) {
        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream == null");
        }
        if (timeout == null) {
            throw new IllegalArgumentException("timeout == null");
        }
        this.outputStream = outputStream;
        this.timeout = timeout;
    }

    /**
     * Writes bytes from {@code source} to the output stream without creating intermediate byte arrays.
     *
     * @param source    the source buffer
     * @param byteCount the number of bytes to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        }
        IoKit.checkOffsetAndCount(source.size, 0, byteCount);
        while (byteCount > 0) {
            timeout.throwIfReached();
            Segment head = source.head;
            int toCopy = (int) Math.min(byteCount, head.limit - head.pos);
            outputStream.write(head.data, head.pos, toCopy);

            head.pos += toCopy;
            byteCount -= toCopy;
            source.size -= toCopy;

            if (head.pos == head.limit) {
                source.head = head.pop();
                SegmentAllocator.release(head);
            }
        }
    }

    /**
     * Flushes the output stream.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    /**
     * Closes the output stream.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    /**
     * Returns the timeout for this sink.
     *
     * @return the timeout
     */
    @Override
    public Timeout timeout() {
        return timeout;
    }

    /**
     * Returns a readable description of this sink.
     *
     * @return the sink description
     */
    @Override
    public String toString() {
        return "sink(" + outputStream + Symbol.PARENTHESE_RIGHT;
    }

}
