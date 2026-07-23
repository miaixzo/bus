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
package org.miaixz.bus.fabric.protocol.http.http2;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;
import java.util.function.LongConsumer;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Headers;

/**
 * Central mutable state of one HTTP/2 stream.
 *
 * <p>
 * The frame writer owns outbound window mutation, the connection reader owns inbound publication, and the request
 * thread only consumes headers and body. Terminal flags are atomically merged so concurrent local and remote closure
 * cannot overwrite each other.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class Http2StreamState {

    /**
     * Terminal bit indicating that the local sending side is closed.
     */
    static final int LOCAL_END = 1;

    /**
     * Terminal bit indicating that the remote sending side is closed.
     */
    static final int REMOTE_END = 1 << 1;

    /**
     * Terminal bit indicating that the stream failed.
     */
    static final int FAILED = 1 << 2;

    /**
     * Terminal bit indicating that the stream was cancelled locally.
     */
    static final int CANCELLED = 1 << 3;

    /**
     * Bit mask requiring both local and remote completion.
     */
    private static final int COMPLETE_MASK = LOCAL_END | REMOTE_END;

    /**
     * Positive HTTP/2 stream identifier.
     */
    private final int streamId;

    /**
     * Atomic bit set merging independent local, remote, failed, and cancelled facts.
     */
    private final AtomicInteger terminal = new AtomicInteger();

    /**
     * Bounded single-producer/single-consumer queue for inbound DATA.
     */
    private final Http2StreamBuffer body;

    /**
     * Outbound flow-control window mutated only by the frame writer.
     */
    private long writeWindow;

    /**
     * Inbound flow-control window charged only by the connection reader.
     */
    private long readWindow;

    /**
     * Consumed inbound bytes accumulated for the next WINDOW_UPDATE.
     */
    private long unacknowledgedBytes;

    /**
     * Final response headers published by the connection reader, or null while pending.
     */
    private volatile Headers responseHeaders;

    /**
     * Trailer headers published after the body, empty until supplied.
     */
    private volatile Headers trailers = Headers.empty();

    /**
     * Runtime failure published to header and body observers, or null.
     */
    private volatile RuntimeException failure;

    /**
     * Single request thread parked while waiting for headers or a terminal transition.
     */
    private volatile Thread waiter;

    /**
     * Creates a stream state.
     *
     * @param streamId      positive stream identifier
     * @param initialWindow initial bidirectional flow-control window
     * @param queuedLimit   maximum retained inbound DATA bytes
     * @param consumed      consumed-byte callback
     */
    Http2StreamState(final int streamId, final long initialWindow, final long queuedLimit,
            final LongConsumer consumed) {
        if (streamId <= 0 || initialWindow < 0L) {
            throw new ValidateException("Invalid HTTP/2 stream state parameters");
        }
        this.streamId = streamId;
        this.writeWindow = initialWindow;
        this.readWindow = initialWindow;
        this.body = new Http2StreamBuffer(32, queuedLimit, consumed);
    }

    /**
     * Returns the HTTP/2 stream identifier.
     *
     * @return positive stream identifier
     */
    int streamId() {
        return streamId;
    }

    /**
     * Returns the inbound DATA queue.
     *
     * @return stream-owned body buffer
     */
    Http2StreamBuffer body() {
        return body;
    }

    /**
     * Returns the writer-owned outbound flow-control window.
     *
     * @return current signed window value
     */
    long writeWindow() {
        return writeWindow;
    }

    /**
     * Adjusts the writer-owned outbound window.
     *
     * @param delta signed SETTINGS or WINDOW_UPDATE delta
     * @return updated writer-owned window
     */
    long adjustWriteWindow(final long delta) {
        final long next = writeWindow + delta;
        if (delta > 0L && next < writeWindow || next > Integer.MAX_VALUE) {
            throw new ProtocolException("HTTP/2 stream flow-control window overflow");
        }
        writeWindow = next;
        return next;
    }

    /**
     * Charges received bytes to the reader-owned window.
     *
     * @param count received DATA bytes
     */
    void receiveBytes(final long count) {
        if (count < 0L || count > readWindow) {
            throw new ProtocolException("HTTP/2 stream flow-control window exceeded");
        }
        readWindow -= count;
        unacknowledgedBytes += count;
    }

    /**
     * Returns accumulated inbound credit and restores the read window.
     *
     * @return bytes to acknowledge
     */
    long takeUnacknowledgedBytes() {
        final long count = unacknowledgedBytes;
        unacknowledgedBytes = 0L;
        readWindow += count;
        return count;
    }

    /**
     * Publishes final response headers.
     *
     * @param headers immutable response headers
     */
    void responseHeaders(final Headers headers) {
        if (headers == null) {
            throw new ValidateException("HTTP/2 response headers must not be null");
        }
        responseHeaders = headers;
        signalWaiter();
    }

    /**
     * Returns currently published final response headers.
     *
     * @return immutable response headers, or null while none are published
     */
    Headers responseHeaders() {
        return responseHeaders;
    }

    /**
     * Publishes trailer headers.
     *
     * @param headers immutable trailer headers
     */
    void trailers(final Headers headers) {
        trailers = headers == null ? Headers.empty() : headers;
    }

    /**
     * Returns the currently published trailer headers.
     *
     * @return immutable trailers, empty when none were supplied
     */
    Headers trailers() {
        return trailers;
    }

    /**
     * Atomically marks the local sending side complete.
     */
    void endLocal() {
        mergeTerminal(LOCAL_END);
    }

    /**
     * Atomically marks the remote sending side complete and finishes the inbound queue on the first transition.
     */
    void endRemote() {
        if (mergeTerminal(REMOTE_END)) {
            body.finish();
        }
    }

    /**
     * Publishes a terminal failure once.
     *
     * @param problem failure to publish
     */
    void fail(final RuntimeException problem) {
        if (problem == null) {
            throw new ValidateException("HTTP/2 stream failure must not be null");
        }
        if (mergeTerminal(FAILED)) {
            failure = problem;
            body.fail(problem);
        }
    }

    /**
     * Atomically marks local cancellation and clears the inbound queue on the first transition.
     */
    void cancel() {
        if (mergeTerminal(CANCELLED)) {
            body.cancel();
        }
    }

    /**
     * Returns the published terminal failure.
     *
     * @return runtime failure, or null when none was stored
     */
    RuntimeException failure() {
        return failure;
    }

    /**
     * Returns the atomically merged terminal facts.
     *
     * @return current terminal bit mask
     */
    int terminalBits() {
        return terminal.get();
    }

    /**
     * Returns whether both directions ended or failure or cancellation was published.
     *
     * @return true when the stream has reached a terminal state
     */
    boolean isTerminal() {
        final int bits = terminal.get();
        return (bits & COMPLETE_MASK) == COMPLETE_MASK || (bits & (FAILED | CANCELLED)) != 0;
    }

    /**
     * Waits until response headers or a terminal state is visible.
     *
     * @return response headers, possibly {@code null} after a terminal event
     */
    Headers awaitResponseHeaders() {
        for (;;) {
            final Headers current = responseHeaders;
            if (current != null || isTerminal()) {
                return current;
            }
            waiter = Thread.currentThread();
            if (responseHeaders == null && !isTerminal()) {
                LockSupport.park(this);
            }
            waiter = null;
            if (Thread.interrupted()) {
                cancel();
                return null;
            }
        }
    }

    /**
     * Convenience publication of inbound DATA for the connection reader.
     *
     * @param source reader-owned source
     * @param count  DATA byte count
     * @return whether publication succeeded
     */
    boolean offerData(final Buffer source, final long count) {
        receiveBytes(count);
        return body.offer(source, count);
    }

    /**
     * Atomically merges a terminal fact.
     *
     * @param flag terminal flag
     * @return {@code true} when this call added the flag
     */
    private boolean mergeTerminal(final int flag) {
        for (;;) {
            final int current = terminal.get();
            if ((current & flag) != 0) {
                return false;
            }
            if (terminal.compareAndSet(current, current | flag)) {
                signalWaiter();
                return true;
            }
        }
    }

    /**
     * Unparks only the request thread currently registered as the header waiter.
     */
    private void signalWaiter() {
        final Thread thread = waiter;
        if (thread != null) {
            LockSupport.unpark(thread);
        }
    }

}
