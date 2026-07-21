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

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongConsumer;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;

/**
 * Lock-based state machine for one HTTP/2 stream.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http2Stream implements AutoCloseable {

    /**
     * Stream id.
     */
    private final int id;

    /**
     * Initial local headers retained until final response headers arrive.
     */
    private final Headers initialHeaders;

    /**
     * Lock protecting all mutable stream state.
     */
    private final ReentrantLock lock;

    /**
     * Condition for headers, body data and terminal changes.
     */
    private final Condition changed;

    /**
     * Buffered inbound body bytes.
     */
    private final Buffer body;

    /**
     * Maximum queued inbound DATA bytes.
     */
    private final long maxQueuedBytes;

    /**
     * Callback for body bytes actually consumed by the application.
     */
    private final LongConsumer consumedCallback;

    /**
     * Callback removing this stream from its owning connection.
     */
    private final Runnable terminalCallback;

    /**
     * Callback sending RST_STREAM CANCEL after an early body close.
     */
    private final Runnable cancelCallback;

    /**
     * One-shot stream payload.
     */
    private final StreamPayload payload;

    /**
     * Compatibility sink used by parser-side tests and adapters.
     */
    private final InboundSink sink;

    /**
     * Informational response header snapshots.
     */
    private final ArrayList<Headers> informationalHeaders;

    /**
     * First non-informational response headers.
     */
    private volatile Headers finalHeaders;

    /**
     * Final response :status kept outside generic RFC-token Headers.
     */
    private volatile String responseStatus;

    /**
     * End-of-stream trailer headers.
     */
    private Headers trailers;

    /**
     * Latest priority metadata.
     */
    private volatile Http2Priority priority;

    /**
     * Terminal failure delivered to header and body waiters.
     */
    private volatile RuntimeException failure;

    /**
     * True after the peer sends END_STREAM.
     */
    private boolean remoteEnd;

    /**
     * True after the payload source is opened.
     */
    private boolean sourceOpened;

    /**
     * True after the payload source is closed.
     */
    private volatile boolean sourceClosed;

    /**
     * True after the terminal callback is delivered.
     */
    private boolean terminalNotified;

    /**
     * Current structured terminal fact.
     */
    private Outcome outcome = Outcome.OPEN;

    /**
     * Creates a stream with no-op ownership callbacks.
     *
     * @param id      stream id
     * @param headers initial headers
     */
    Http2Stream(final int id, final Headers headers) {
        this(id, headers, ignored -> {
        }, () -> {
        }, () -> {
        });
    }

    /**
     * Creates a stream with a consumption callback.
     *
     * @param id              stream id
     * @param headers         initial headers
     * @param inboundConsumed consumed-byte callback
     */
    Http2Stream(final int id, final Headers headers, final LongConsumer inboundConsumed) {
        this(id, headers, inboundConsumed, () -> {
        }, () -> {
        });
    }

    /**
     * Creates a connection-owned stream.
     *
     * @param id              stream id
     * @param headers         initial headers
     * @param inboundConsumed consumed-byte callback
     * @param terminal        terminal ownership callback
     * @param cancel          early-close cancellation callback
     */
    Http2Stream(final int id, final Headers headers, final LongConsumer inboundConsumed, final Runnable terminal,
            final Runnable cancel) {
        this(id, headers, inboundConsumed, terminal, cancel, Builder.HTTP2_STREAM_DEFAULT_MAX_QUEUED_BYTES);
    }

    /**
     * Creates a connection-owned stream with an explicit inbound budget.
     */
    Http2Stream(final int id, final Headers headers, final LongConsumer inboundConsumed, final Runnable terminal,
            final Runnable cancel, final long maxQueuedBytes) {
        if (id <= Normal._0) {
            throw new ValidateException("HTTP/2 stream id must be positive");
        }
        this.id = id;
        this.initialHeaders = require(headers, "HTTP/2 stream headers");
        this.lock = new ReentrantLock();
        this.changed = lock.newCondition();
        this.body = new Buffer();
        if (maxQueuedBytes <= Normal.LONG_ZERO || maxQueuedBytes > Builder.HTTP2_STREAM_DEFAULT_MAX_QUEUED_BYTES) {
            throw new ValidateException("HTTP/2 queued DATA limit must be between 1 and 1 MiB");
        }
        this.maxQueuedBytes = maxQueuedBytes;
        this.consumedCallback = require(inboundConsumed, "HTTP/2 consumed callback");
        this.terminalCallback = require(terminal, "HTTP/2 terminal callback");
        this.cancelCallback = require(cancel, "HTTP/2 cancel callback");
        this.payload = new StreamPayload();
        this.sink = new InboundSink();
        this.informationalHeaders = new ArrayList<>();
        this.trailers = Headers.empty();
    }

    /**
     * @return stream id
     */
    public int id() {
        return id;
    }

    /**
     * Returns final response headers when available, otherwise the initial local header snapshot.
     *
     * @return header snapshot
     */
    public Headers headers() {
        final Headers current = finalHeaders;
        return current == null ? initialHeaders : current;
    }

    /**
     * @return priority metadata or null
     */
    public Http2Priority priority() {
        return priority;
    }

    /**
     * Receives a header block without END_STREAM.
     *
     * @param headers header block
     */
    public void receiveHeaders(final Headers headers) {
        receiveHeaders(headers, false);
    }

    /**
     * Receives informational, final or trailer headers.
     *
     * @param headers   header block
     * @param endStream whether this block ends the remote stream
     */
    public void receiveHeaders(final Headers headers, final boolean endStream) {
        receiveHeaders(headers, headers == null ? null : headers.get(HTTP.RESPONSE_STATUS_UTF8), endStream);
    }

    /**
     * Receives headers with the HTTP/2 :status sidecar already separated.
     */
    void receiveHeaders(final Headers headers, final String responseStatus, final boolean endStream) {
        final Headers checked = require(headers, "HTTP/2 headers");
        Runnable terminal = null;
        lock.lock();
        try {
            throwIfFailed();
            final String status = responseStatus;
            if (finalHeaders == null) {
                if (informational(status)) {
                    informationalHeaders.add(checked);
                } else {
                    if (status == null) {
                        throw new StatefulException("HTTP/2 response is missing :status");
                    }
                    finalHeaders = checked;
                    this.responseStatus = status;
                }
            } else {
                if (status != null) {
                    throw new StatefulException("HTTP/2 response headers were already received");
                }
                if (!endStream) {
                    throw new StatefulException("HTTP/2 trailers must end the stream");
                }
                trailers = checked;
            }
            if (endStream) {
                remoteEnd = true;
                terminal = terminalIfCompleteLocked();
            }
            changed.signalAll();
        } finally {
            lock.unlock();
        }
        run(terminal);
    }

    /**
     * Returns the final response :status sidecar.
     */
    public String responseStatus() {
        return responseStatus;
    }

    /**
     * Receives body data without END_STREAM.
     *
     * @param data data bytes
     */
    public void receiveData(final ByteString data) {
        receiveData(data, false);
    }

    /**
     * Appends body data and wakes the streaming consumer.
     *
     * @param data      data bytes
     * @param endStream whether this data ends the remote stream
     */
    public void receiveData(final ByteString data, final boolean endStream) {
        final ByteString checked = require(data, "HTTP/2 data");
        Runnable terminal = null;
        Runnable cancel = null;
        long discarded = Normal.LONG_ZERO;
        lock.lock();
        try {
            if (sourceClosed || terminalNotified) {
                discarded = checked.size();
                return;
            }
            throwIfFailed();
            if (remoteEnd) {
                discarded = checked.size();
                return;
            }
            if (checked.size() > maxQueuedBytes - body.size()) {
                discarded = body.size() + checked.size();
                body.clear();
                failure = new SocketException("HTTP/2 stream queued DATA limit exceeded");
                remoteEnd = true;
                sourceClosed = true;
                outcome = Outcome.RESET;
                cancel = cancelCallback;
                terminal = terminalLocked();
                changed.signalAll();
                return;
            }
            if (checked.size() != Normal._0) {
                body.write(checked);
            }
            if (endStream) {
                remoteEnd = true;
                terminal = terminalIfCompleteLocked();
            }
            changed.signalAll();
        } finally {
            lock.unlock();
            if (discarded > Normal.LONG_ZERO) {
                consumedCallback.accept(discarded);
            }
            run(cancel);
            run(terminal);
        }
    }

    /**
     * Waits only for final non-1xx headers, failure or timeout.
     *
     * @param timeout wait duration; zero waits without a deadline
     * @return final response headers
     */
    public Headers awaitResponseHeaders(final Duration timeout) {
        final Duration checked = require(timeout, "HTTP/2 header timeout");
        if (checked.isNegative()) {
            throw new ValidateException("HTTP/2 header timeout must not be negative");
        }
        long remaining = checked.isZero() ? Long.MAX_VALUE : nanos(checked);
        lock.lock();
        try {
            while (finalHeaders == null) {
                throwIfFailed();
                if (remoteEnd) {
                    throw new SocketException("HTTP/2 stream ended before final response headers");
                }
                remaining = await(remaining, "HTTP/2 response headers");
            }
            return finalHeaders;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return immutable informational header snapshots
     */
    List<Headers> informationalHeaders() {
        lock.lock();
        try {
            return List.copyOf(informationalHeaders);
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return trailer snapshot, empty until received
     */
    public Headers trailers() {
        lock.lock();
        try {
            throwIfFailed();
            return trailers;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return one-shot streaming payload
     */
    public Payload payload() {
        return payload;
    }

    /**
     * @return one-shot streaming source
     */
    public Source source() {
        return payload.source();
    }

    /**
     * @return parser-side inbound sink
     */
    public Sink sink() {
        return sink;
    }

    /**
     * Stores priority metadata.
     *
     * @param value priority metadata
     */
    void priority(final Http2Priority value) {
        lock.lock();
        try {
            priority = require(value, "HTTP/2 priority");
        } finally {
            lock.unlock();
        }
    }

    /**
     * Fails the stream and wakes every waiter.
     *
     * @param cause terminal failure
     */
    void fail(final RuntimeException cause) {
        fail(cause, Outcome.CONNECTION_FAILURE);
    }

    /**
     * Fails the stream with a structured transport outcome.
     */
    void fail(final RuntimeException cause, final Outcome terminalOutcome) {
        final RuntimeException checked = require(cause, "HTTP/2 stream failure");
        Runnable terminal;
        long discarded = Normal.LONG_ZERO;
        lock.lock();
        try {
            if (failure == null) {
                failure = checked;
                outcome = require(terminalOutcome, "HTTP/2 stream outcome");
            }
            discarded = body.size();
            body.clear();
            remoteEnd = true;
            sourceClosed = true;
            terminal = terminalLocked();
            changed.signalAll();
        } finally {
            lock.unlock();
        }
        if (discarded > Normal.LONG_ZERO) {
            consumedCallback.accept(discarded);
        }
        run(terminal);
    }

    /**
     * Returns the structured stream outcome.
     */
    Outcome outcome() {
        lock.lock();
        try {
            return outcome;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Closes the response body; an early close sends CANCEL exactly once.
     */
    @Override
    public void close() {
        closeSource();
    }

    /**
     * @return true while no failure occurred and the source is not closed
     */
    public boolean opened() {
        return failure == null && !sourceClosed;
    }

    /**
     * Closes the source and chooses normal completion or early cancellation.
     */
    private void closeSource() {
        Runnable cancel = null;
        Runnable terminal;
        long discarded = Normal.LONG_ZERO;
        lock.lock();
        try {
            if (sourceClosed) {
                return;
            }
            sourceClosed = true;
            if (!remoteEnd || body.size() > Normal.LONG_ZERO) {
                cancel = cancelCallback;
                outcome = Outcome.CANCELLED;
            }
            discarded = body.size();
            body.clear();
            terminal = terminalLocked();
            changed.signalAll();
        } finally {
            lock.unlock();
        }
        if (discarded > Normal.LONG_ZERO) {
            consumedCallback.accept(discarded);
        }
        run(cancel);
        run(terminal);
    }

    /**
     * Returns the terminal callback when remote completion is fully consumed.
     *
     * @return callback or null
     */
    private Runnable terminalIfCompleteLocked() {
        return remoteEnd && body.size() == Normal.LONG_ZERO ? terminalLocked() : null;
    }

    /**
     * Claims the terminal callback exactly once.
     *
     * @return callback or null
     */
    private Runnable terminalLocked() {
        if (terminalNotified) {
            return null;
        }
        terminalNotified = true;
        if (outcome == Outcome.OPEN) {
            outcome = Outcome.COMPLETE;
        }
        return terminalCallback;
    }

    /**
     * Waits on the stream condition.
     *
     * @param remaining remaining nanoseconds or Long.MAX_VALUE
     * @param operation operation name
     * @return updated remaining nanoseconds
     */
    private long await(final long remaining, final String operation) {
        try {
            if (remaining == Long.MAX_VALUE) {
                changed.await();
                return Long.MAX_VALUE;
            }
            if (remaining <= Normal.LONG_ZERO) {
                throw new org.miaixz.bus.core.lang.exception.TimeoutException("Timed out waiting for " + operation);
            }
            final long next = changed.awaitNanos(remaining);
            if (next <= Normal.LONG_ZERO) {
                throw new org.miaixz.bus.core.lang.exception.TimeoutException("Timed out waiting for " + operation);
            }
            return next;
        } catch (final InterruptedException e) {
            ThreadKit.interrupt(Thread.currentThread(), false);
            throw new InternalException("Interrupted while waiting for " + operation, e);
        }
    }

    /**
     * Throws the stored failure.
     */
    private void throwIfFailed() {
        if (failure != null) {
            throw failure;
        }
    }

    /**
     * Tests whether a status header is informational.
     *
     * @param status status value or null
     * @return true for 100 through 199
     */
    private static boolean informational(final String status) {
        if (status == null) {
            return false;
        }
        try {
            final int code = Integer.parseInt(status);
            return code >= HTTP.HTTP_CONTINUE && code < HTTP.HTTP_OK;
        } catch (final NumberFormatException e) {
            throw new SocketException("Invalid HTTP/2 response status", e);
        }
    }

    /**
     * Converts a duration to a saturated nanosecond count.
     *
     * @param duration duration
     * @return nanoseconds
     */
    private static long nanos(final Duration duration) {
        try {
            return Math.max(Normal._1, duration.toNanos());
        } catch (final ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Runs an optional ownership callback.
     *
     * @param callback callback or null
     */
    private static void run(final Runnable callback) {
        if (callback != null) {
            callback.run();
        }
    }

    /**
     * Validates a required value.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * One-shot payload whose source waits for incremental DATA frames.
     */
    private final class StreamPayload implements Payload, Source {

        /**
         * @return unknown streaming length
         */
        @Override
        public long length() {
            return Normal.__1;
        }

        /**
         * @return this source on the first invocation
         */
        @Override
        public Source source() {
            lock.lock();
            try {
                if (sourceOpened) {
                    throw new StatefulException("HTTP/2 body source can only be opened once");
                }
                sourceOpened = true;
                return this;
            } finally {
                lock.unlock();
            }
        }

        /**
         * @return false
         */
        @Override
        public boolean repeatable() {
            return false;
        }

        /**
         * Reads available body bytes or waits for DATA, failure or remote end.
         *
         * @param target    target buffer
         * @param byteCount maximum bytes
         * @return bytes read or -1 at EOF
         */
        @Override
        public long read(final Buffer target, final long byteCount) {
            final Buffer checked = require(target, "HTTP/2 body target");
            if (byteCount < Normal.LONG_ZERO) {
                throw new ValidateException("HTTP/2 body byte count must not be negative");
            }
            if (byteCount == Normal.LONG_ZERO) {
                return Normal.LONG_ZERO;
            }
            long consumed = Normal.LONG_ZERO;
            Runnable terminal = null;
            lock.lock();
            try {
                while (body.size() == Normal.LONG_ZERO && !remoteEnd) {
                    throwIfFailed();
                    if (sourceClosed) {
                        throw new StatefulException("HTTP/2 body source is closed");
                    }
                    await(Long.MAX_VALUE, "HTTP/2 body data");
                }
                throwIfFailed();
                if (body.size() == Normal.LONG_ZERO && remoteEnd) {
                    sourceClosed = true;
                    terminal = terminalLocked();
                    return Normal.__1;
                }
                consumed = Math.min(byteCount, body.size());
                checked.write(body, consumed);
                terminal = terminalIfCompleteLocked();
                return consumed;
            } finally {
                lock.unlock();
                if (consumed > Normal.LONG_ZERO) {
                    consumedCallback.accept(consumed);
                }
                run(terminal);
            }
        }

        /**
         * @return no per-source timeout
         */
        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }

        /**
         * Closes the body source.
         */
        @Override
        public void close() {
            closeSource();
        }

    }

    /**
     * Parser-side sink adapting byte writes to stream DATA transitions.
     */
    private final class InboundSink implements Sink {

        /**
         * Appends inbound bytes.
         *
         * @param source    source buffer
         * @param byteCount byte count
         */
        @Override
        public void write(final Buffer source, final long byteCount) throws IOException {
            final Buffer checked = require(source, "HTTP/2 inbound source");
            if (byteCount < Normal.LONG_ZERO || byteCount > checked.size()) {
                throw new ValidateException("HTTP/2 inbound byte count is invalid");
            }
            receiveData(checked.readByteString(byteCount), false);
        }

        /**
         * Flush is unnecessary for an in-memory stream buffer.
         */
        @Override
        public void flush() {
            // DATA is visible immediately after write.
        }

        /**
         * @return no per-sink timeout
         */
        @Override
        public Timeout timeout() {
            return Timeout.NONE;
        }

        /**
         * Marks the parser-side stream as remotely ended.
         */
        @Override
        public void close() {
            receiveData(ByteString.EMPTY, true);
        }

    }

    /**
     * Structured terminal fact consumed by the connection and transport owners.
     */
    enum Outcome {
        /**
         * Stream remains open and has no terminal outcome.
         */
        OPEN,
        /**
         * Stream completed normally.
         */
        COMPLETE,
        /**
         * Peer or local endpoint reset the stream for a general reason.
         */
        RESET,
        /**
         * Peer refused the stream before processing it.
         */
        REFUSED_STREAM,
        /**
         * GOAWAY proves that the peer did not process this stream.
         */
        GOAWAY_UNPROCESSED,
        /**
         * GOAWAY cannot prove whether the peer processed this stream.
         */
        GOAWAY_POSSIBLY_PROCESSED,
        /**
         * The physical HTTP/2 connection failed.
         */
        CONNECTION_FAILURE,
        /**
         * Caller or runtime cancellation terminated the stream.
         */
        CANCELLED
    }

}
