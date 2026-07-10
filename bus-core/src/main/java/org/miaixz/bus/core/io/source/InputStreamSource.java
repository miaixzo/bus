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
package org.miaixz.bus.core.io.source;

import java.io.IOException;
import java.io.InputStream;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.buffer.Segment;
import org.miaixz.bus.core.io.buffer.SegmentAllocator;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;

/**
 * A {@link Source} implementation that reads from an {@link InputStream} into writable buffer segments.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class InputStreamSource implements Source {

    /**
     * Source input stream.
     */
    private final InputStream inputStream;

    /**
     * Timeout applied before each read attempt.
     */
    private final Timeout timeout;

    /**
     * Creates an input stream backed source.
     *
     * @param inputStream the source input stream
     * @param timeout     the timeout information
     */
    public InputStreamSource(InputStream inputStream, Timeout timeout) {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream == null");
        }
        if (timeout == null) {
            throw new IllegalArgumentException("timeout == null");
        }
        this.inputStream = inputStream;
        this.timeout = timeout;
    }

    /**
     * Reads bytes from the input stream into {@code sink}.
     *
     * @param sink      the destination buffer
     * @param byteCount the maximum number of bytes to read
     * @return the number of bytes read, or {@code -1} if exhausted
     * @throws IOException if an I/O error occurs
     */
    @Override
    public long read(Buffer sink, long byteCount) throws IOException {
        if (sink == null) {
            throw new IllegalArgumentException("sink == null");
        }
        if (byteCount < 0) {
            throw new IllegalArgumentException("byteCount < 0: " + byteCount);
        }
        if (byteCount == 0) {
            return 0;
        }
        try {
            timeout.throwIfReached();
            Segment tail = sink.writableSegment(1);
            int maxToCopy = (int) Math.min(byteCount, Segment.SIZE - tail.limit);
            int bytesRead = inputStream.read(tail.data, tail.limit, maxToCopy);
            if (bytesRead == -1) {
                if (tail.pos == tail.limit) {
                    sink.head = tail.pop();
                    SegmentAllocator.release(tail);
                }
                return -1;
            }
            tail.limit += bytesRead;
            sink.size += bytesRead;
            return bytesRead;
        } catch (AssertionError e) {
            if (IoKit.isAndroidGetsocknameError(e)) {
                throw new IOException(e);
            }
            throw e;
        }
    }

    /**
     * Closes the input stream.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    /**
     * Returns the timeout for this source.
     *
     * @return the timeout
     */
    @Override
    public Timeout timeout() {
        return timeout;
    }

    /**
     * Returns a readable description of this source.
     *
     * @return the source description
     */
    @Override
    public String toString() {
        return "source(" + inputStream + Symbol.PARENTHESE_RIGHT;
    }

}
