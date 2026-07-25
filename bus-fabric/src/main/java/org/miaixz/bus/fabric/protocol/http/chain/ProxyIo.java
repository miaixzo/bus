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
package org.miaixz.bus.fabric.protocol.http.chain;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Package-local exact I/O primitives shared only by HTTP and SOCKS proxy handshakes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class ProxyIo {

    /**
     * Prevents instantiation.
     */
    private ProxyIo() {
        // No initialization required.
    }

    /**
     * Writes all bytes in a buffer.
     *
     * @param sink         destination
     * @param source       bytes
     * @param timeout      timeout policy
     * @param cancellation cancellation scope
     */
    static void writeAll(final Sink sink, final Buffer source, final Timeout timeout, final Cancellation cancellation) {
        configure(sink.timeout(), timeout.write());
        cancellation.throwIfCancelled();
        try {
            sink.write(source, source.size());
        } catch (final IOException e) {
            throw new SocketException("Proxy handshake write failed", e);
        }
    }

    /**
     * Reads an exact number of bytes.
     *
     * @param source       source
     * @param length       required bytes
     * @param timeout      timeout policy
     * @param message      failure message
     * @param cancellation cancellation scope
     * @return bytes
     */
    static byte[] readExact(
            final Source source,
            final int length,
            final Timeout timeout,
            final String message,
            final Cancellation cancellation) {
        configure(source.timeout(), timeout.read());
        final Buffer buffer = new Buffer();
        while (buffer.size() < length) {
            cancellation.throwIfCancelled();
            try {
                final long read = source.read(buffer, length - buffer.size());
                if (read < 0) {
                    throw new SocketException("Proxy handshake reached EOF");
                }
                if (read == 0) {
                    Thread.onSpinWait();
                }
            } catch (final IOException e) {
                throw new SocketException(message, e);
            }
        }
        try {
            return buffer.readByteArray(length);
        } catch (final IOException e) {
            throw new SocketException("Unable to materialize proxy response", e);
        }
    }

    /**
     * Applies a duration to the core I/O timeout.
     *
     * @param target   I/O timeout
     * @param duration duration
     */
    static void configure(final org.miaixz.bus.core.io.timout.Timeout target, final Duration duration) {
        if (target != null && duration != null && !duration.isZero() && !duration.isNegative()) {
            target.timeout(duration.toNanos(), TimeUnit.NANOSECONDS);
        }
    }

}
