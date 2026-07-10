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

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;

/**
 * A {@link Sink} implementation that discards every byte written to it.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class BlackholeSink implements Sink {

    /**
     * Shared stateless blackhole sink.
     */
    public static final BlackholeSink INSTANCE = new BlackholeSink();

    /**
     * Creates a blackhole sink.
     */
    private BlackholeSink() {
        // No initialization required.
    }

    /**
     * Discards bytes from {@code source}.
     *
     * @param source    the source buffer
     * @param byteCount the number of bytes to discard
     * @throws IOException if the source cannot skip the requested bytes
     */
    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        }
        source.skip(byteCount);
    }

    /**
     * Flushes this sink. Nothing is buffered.
     */
    @Override
    public void flush() {
        // Nothing to flush.
    }

    /**
     * Returns the timeout for this sink.
     *
     * @return the timeout
     */
    @Override
    public Timeout timeout() {
        return Timeout.NONE;
    }

    /**
     * Closes this sink. Nothing is owned.
     */
    @Override
    public void close() {
        // Nothing to close.
    }

}
