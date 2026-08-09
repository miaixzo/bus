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
package org.miaixz.bus.fabric.network.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.network.Conduit;

/**
 * Reader for optional PROXY protocol v1 metadata before ingress creation.
 *
 * @author Kimi Liu
 */
public final class ProxyHeaderReader {

    /**
     * ASCII signature used to distinguish a PROXY protocol v1 line from application payload.
     */
    private static final byte[] PREFIX = "PROXY ".getBytes(Charset.US_ASCII);

    /**
     * Keeps proxy-header decoding on the static API.
     */
    private ProxyHeaderReader() {
        // No initialization required.
    }

    /**
     * Reads an optional PROXY protocol v1 header.
     *
     * @param channel non-null socket channel positioned before optional PROXY metadata
     * @return parsed header with no payload when the signature matches, otherwise no header with consumed bytes
     *         replayable as application payload
     */
    public static Result read(final SocketChannel channel) {
        final SocketChannel current = Assert
                .notNull(channel, () -> new ValidateException("Socket channel must not be null"));
        final ByteArrayOutputStream consumed = new ByteArrayOutputStream(PREFIX.length);
        try {
            for (int index = Normal._0; index < PREFIX.length; index++) {
                final int value = readByte(current);
                if (value < Normal._0) {
                    if (index == Normal._0) {
                        return new Result(null, empty());
                    }
                    return new Result(null, payload(consumed));
                }
                consumed.write(value);
                if ((byte) value != PREFIX[index]) {
                    return new Result(null, payload(consumed));
                }
            }
            final ByteArrayOutputStream line = new ByteArrayOutputStream(Builder.PROXY_HEADER_READER_MAX_LINE_BYTES);
            line.writeBytes(PREFIX);
            while (line.size() < Builder.PROXY_HEADER_READER_MAX_LINE_BYTES) {
                final int value = readByte(current);
                if (value < Normal._0) {
                    throw new ProtocolException("PROXY header ended before CRLF");
                }
                if (value == Symbol.C_CR) {
                    final int next = readByte(current);
                    if (next == Symbol.C_LF) {
                        return new Result(ProxyHeader.parse(line.toString(Charset.US_ASCII)), empty());
                    }
                    if (next < Normal._0) {
                        throw new ProtocolException("PROXY header ended before LF");
                    }
                    line.write(value);
                    line.write(next);
                } else {
                    line.write(value);
                }
            }
            throw new ProtocolException("PROXY header is too large");
        } catch (final IOException e) {
            throw new SocketException("Unable to read PROXY header", e);
        }
    }

    /**
     * Reads an optional PROXY protocol v1 header without blocking a completion-driven transport.
     *
     * @param conduit    transport positioned before optional PROXY metadata
     * @param completion terminal result callback
     */
    public static void read(final Conduit conduit, final BiConsumerX<? super Result, ? super Throwable> completion) {
        final Conduit current = Assert.notNull(conduit, () -> new ValidateException("Conduit must not be null"));
        final BiConsumerX<? super Result, ? super Throwable> callback = Assert
                .notNull(completion, () -> new ValidateException("PROXY completion must not be null"));
        new AsyncReader(current, callback).readPrefix();
    }

    /**
     * Reads one byte.
     *
     * @param channel socket channel from which one byte is polled
     * @return unsigned byte value from 0 through 255, or -1 at end-of-stream
     * @throws IOException when the channel read fails
     */
    private static int readByte(final SocketChannel channel) throws IOException {
        final ByteBuffer one = ByteBuffer.allocate(Normal._1);
        int read = channel.read(one);
        while (read == Normal._0) {
            ThreadKit.sleep(Normal._1);
            read = channel.read(one);
        }
        if (read <= Normal._0) {
            return read < Normal._0 ? Normal.__1 : Normal._0;
        }
        one.flip();
        return one.get() & Builder.UNSIGNED_BYTE_MASK;
    }

    /**
     * Creates an empty payload buffer.
     *
     * @return new empty mutable buffer
     */
    private static Buffer empty() {
        return new Buffer();
    }

    /**
     * Creates payload from consumed bytes.
     *
     * @param consumed bytes tentatively read while testing the PROXY signature
     * @return new buffer containing those bytes for replay as application payload
     */
    private static Buffer payload(final ByteArrayOutputStream consumed) {
        return new Buffer().write(consumed.toByteArray());
    }

    /**
     * One completion-driven optional-header probe. The consumed buffer is transferred unchanged when the signature does
     * not match, preserving application bytes exactly as the blocking reader does.
     */
    private static final class AsyncReader {

        /**
         * Completion-capable transport being probed.
         */
        private final Conduit conduit;

        /**
         * Terminal result callback.
         */
        private final BiConsumerX<? super Result, ? super Throwable> completion;

        /**
         * Bytes consumed while testing the optional header.
         */
        private final Buffer consumed = new Buffer();

        /**
         * Guards the terminal callback against duplicate publication.
         */
        private boolean terminal;

        /**
         * Creates one asynchronous optional-header reader.
         *
         * @param conduit    completion-capable transport
         * @param completion terminal result callback
         */
        private AsyncReader(final Conduit conduit, final BiConsumerX<? super Result, ? super Throwable> completion) {
            this.conduit = conduit;
            this.completion = completion;
        }

        /**
         * Requests the remaining signature bytes.
         */
        private void readPrefix() {
            read(PREFIX.length - consumed.size(), this::prefixRead);
        }

        /**
         * Evaluates newly completed signature bytes.
         *
         * @param count completed byte count
         * @param cause terminal read failure
         */
        private void prefixRead(final Long count, final Throwable cause) {
            if (!valid(count, cause, false)) {
                return;
            }
            for (int index = Normal._0; index < consumed.size(); index++) {
                if (consumed.getByte(index) != PREFIX[index]) {
                    complete(new Result(null, consumed), null);
                    return;
                }
            }
            if (consumed.size() < PREFIX.length) {
                readPrefix();
            } else {
                readLine();
            }
        }

        /**
         * Requests the next header-line byte.
         */
        private void readLine() {
            read(Normal._1, this::lineRead);
        }

        /**
         * Evaluates one completed header-line byte.
         *
         * @param count completed byte count
         * @param cause terminal read failure
         */
        private void lineRead(final Long count, final Throwable cause) {
            if (!valid(count, cause, true)) {
                return;
            }
            final long size = consumed.size();
            if (size >= Normal._2 && consumed.getByte(size - Normal._2) == Symbol.C_CR
                    && consumed.getByte(size - Normal._1) == Symbol.C_LF) {
                final String line = consumed.snapshot(Math.toIntExact(size - Normal._2)).string(Charset.US_ASCII);
                complete(new Result(ProxyHeader.parse(line), empty()), null);
                return;
            }
            if (size >= Builder.PROXY_HEADER_READER_MAX_LINE_BYTES) {
                complete(null, new ProtocolException("PROXY header is too large"));
                return;
            }
            readLine();
        }

        /**
         * Submits a guarded callback read.
         *
         * @param byteCount maximum byte count
         * @param callback  phase-specific completion callback
         */
        private void read(final long byteCount, final BiConsumerX<? super Long, ? super Throwable> callback) {
            try {
                conduit.read(consumed, byteCount, callback);
            } catch (final RuntimeException e) {
                complete(null, e);
            }
        }

        /**
         * Validates one phase completion and handles retry, EOF, and failure outcomes.
         *
         * @param count  completed byte count
         * @param cause  terminal read failure
         * @param header true after the PROXY signature has matched
         * @return true when the caller may inspect newly consumed bytes
         */
        private boolean valid(final Long count, final Throwable cause, final boolean header) {
            if (cause != null) {
                complete(null, cause);
                return false;
            }
            if (count == null) {
                complete(null, new SocketException("PROXY header read completed without a byte count"));
                return false;
            }
            if (count < Normal._0) {
                if (header) {
                    complete(null, new ProtocolException("PROXY header ended before CRLF"));
                } else {
                    complete(new Result(null, consumed), null);
                }
                return false;
            }
            if (count == Normal._0) {
                if (header) {
                    readLine();
                } else {
                    readPrefix();
                }
                return false;
            }
            return true;
        }

        /**
         * Publishes the result or failure exactly once.
         *
         * @param result optional-header result
         * @param cause  terminal failure
         */
        private void complete(final Result result, final Throwable cause) {
            if (terminal) {
                return;
            }
            terminal = true;
            completion.accept(result, cause);
        }

    }

    /**
     * PROXY header read result.
     *
     * @param header  parsed PROXY metadata, or null when no complete signature was found
     * @param payload prefetched application bytes consumed during signature detection
     */
    public record Result(ProxyHeader header, Buffer payload) {

        /**
         * Creates a result.
         *
         * @param header  parsed PROXY metadata, or null
         * @param payload prefetched application bytes, or null to allocate an empty buffer
         */
        public Result {
            payload = payload == null ? new Buffer() : payload;
        }

    }

}
