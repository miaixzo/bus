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
import java.util.concurrent.locks.LockSupport;
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
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;

/**
 * Single-reader/single-consumer state view for one HTTP/2 stream.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http2Stream implements AutoCloseable {

    /**
     * Positive HTTP/2 stream identifier.
     */
    private final int id;

    /**
     * Initial local headers retained until final response headers arrive.
     */
    private final Headers initialHeaders;

    /**
     * Buffered inbound body bytes.
     */
    private final Http2StreamBuffer body;

    /**
     * Maximum queued inbound DATA bytes.
     */
    private final long maxQueuedBytes;

    /**
     * Connection-lock-owned outbound flow-control window.
     */
    private long writeWindow;

    /**
     * Connection-lock-owned inbound flow-control window.
     */
    private long receiveWindow;

    /**
     * Connection-lock-owned consumed bytes pending a stream WINDOW_UPDATE.
     */
    private long unacknowledgedBytes;

    /**
     * Callback for body bytes actually consumed by the application.
     */
    private final LongConsumer consumedCallback;

    /**
     * One-shot callback removing this stream from its owning connection after terminal ownership is claimed.
     */
    private final Runnable terminalCallback;

    /**
     * Callback sending {@code RST_STREAM CANCEL} after an early body close or inbound queue overflow.
     */
    private final Runnable cancelCallback;

    /**
     * One-shot stream payload.
     */
    private final StreamPayload payload;

    /**
     * Compatibility sink used by parser-side tests and adapters.
     */
    private volatile InboundSink sink;

    /**
     * Informational response header snapshots.
     */
    private ArrayList<Headers> informationalHeaders;

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
    private volatile boolean remoteEnd;

    /**
     * Request thread currently parked while waiting for final response headers.
     */
    private volatile Thread waiter;

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
     * Reader-owned small-frame coalescing buffer published at 16 KiB or END_STREAM.
     */
    private final Buffer inboundBatch = new Buffer();

    /**
     * Creates a stream with no-op ownership callbacks.
     *
     * @param id      positive HTTP/2 stream identifier
     * @param headers initial local header snapshot returned before final response headers arrive
     * @throws ValidateException if the id is not positive or {@code headers} is {@code null}
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
     * @param id              positive HTTP/2 stream identifier
     * @param headers         initial local header snapshot
     * @param inboundConsumed callback returning consumed or discarded inbound bytes to flow control
     * @throws ValidateException if the id is not positive or a required reference is {@code null}
     */
    Http2Stream(final int id, final Headers headers, final LongConsumer inboundConsumed) {
        this(id, headers, inboundConsumed, () -> {
        }, () -> {
        });
    }

    /**
     * Creates a connection-owned stream.
     *
     * @param id              positive HTTP/2 stream identifier
     * @param headers         initial local header snapshot
     * @param inboundConsumed callback returning consumed or discarded inbound bytes to flow control
     * @param terminal        one-shot callback releasing terminal stream ownership
     * @param cancel          callback sending cancellation for early close or queue overflow
     * @throws ValidateException if the id is not positive or a required reference is {@code null}
     */
    Http2Stream(final int id, final Headers headers, final LongConsumer inboundConsumed, final Runnable terminal,
            final Runnable cancel) {
        this(id, headers, inboundConsumed, terminal, cancel, Builder.HTTP2_STREAM_DEFAULT_MAX_QUEUED_BYTES);
    }

    /**
     * Creates a connection-owned stream with an explicit inbound budget.
     *
     * @param id              positive HTTP/2 stream identifier
     * @param headers         initial local header snapshot
     * @param inboundConsumed callback returning consumed inbound bytes to flow control
     * @param terminal        callback releasing terminal stream ownership
     * @param cancel          callback sending cancellation for an early close
     * @param maxQueuedBytes  maximum queued inbound DATA bytes
     * @throws ValidateException if the id or byte budget is outside its supported range or a required reference is
     *                           {@code null}
     */
    Http2Stream(final int id, final Headers headers, final LongConsumer inboundConsumed, final Runnable terminal,
            final Runnable cancel, final long maxQueuedBytes) {
        if (id <= Normal._0) {
            throw new ValidateException("HTTP/2 stream id must be positive");
        }
        this.id = id;
        this.initialHeaders = require(headers, "HTTP/2 stream headers");
        if (maxQueuedBytes <= Normal.LONG_ZERO || maxQueuedBytes > Builder.HTTP2_STREAM_DEFAULT_MAX_QUEUED_BYTES) {
            throw new ValidateException("HTTP/2 queued DATA limit must be between 1 and 1 MiB");
        }
        this.maxQueuedBytes = maxQueuedBytes;
        this.consumedCallback = require(inboundConsumed, "HTTP/2 consumed callback");
        this.body = new Http2StreamBuffer(64, maxQueuedBytes, this.consumedCallback);
        this.terminalCallback = require(terminal, "HTTP/2 terminal callback");
        this.cancelCallback = require(cancel, "HTTP/2 cancel callback");
        this.payload = new StreamPayload();
        this.trailers = Headers.empty();
    }

    /**
     * Returns the positive HTTP/2 stream identifier.
     *
     * @return stream identifier assigned by the owning connection
     */
    public int id() {
        return id;
    }

    /**
     * Initializes connection-owned flow-control counters before stream publication.
     *
     * @param initialWriteWindow   initial peer-advertised send window
     * @param initialReceiveWindow initial locally advertised receive window
     */
    void initializeFlow(final long initialWriteWindow, final long initialReceiveWindow) {
        writeWindow = initialWriteWindow;
        receiveWindow = initialReceiveWindow;
    }

    /**
     * Returns the connection-lock-owned outbound window.
     *
     * @return available outbound flow-control bytes
     */
    long writeWindow() {
        return writeWindow;
    }

    /**
     * Replaces the connection-lock-owned outbound window.
     *
     * @param value new outbound flow-control window
     */
    void writeWindow(final long value) {
        writeWindow = value;
    }

    /**
     * Returns the connection-lock-owned inbound window.
     *
     * @return remaining inbound flow-control bytes
     */
    long receiveWindow() {
        return receiveWindow;
    }

    /**
     * Replaces the connection-lock-owned inbound window.
     *
     * @param value new inbound flow-control window
     */
    void receiveWindow(final long value) {
        receiveWindow = value;
    }

    /**
     * Adds consumed bytes and returns a threshold-sized update, or zero while batching.
     *
     * @param count     delivered DATA bytes
     * @param threshold minimum accumulated byte count that triggers an update
     * @return WINDOW_UPDATE increment, or zero while the threshold has not been reached
     */
    long consumeForWindowUpdate(final long count, final long threshold) {
        unacknowledgedBytes += count;
        if (unacknowledgedBytes < threshold) {
            return Normal.LONG_ZERO;
        }
        final long update = unacknowledgedBytes;
        unacknowledgedBytes = Normal.LONG_ZERO;
        return update;
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
     * Returns the most recently received priority metadata.
     *
     * @return priority metadata, or {@code null} before any priority update
     */
    public Http2Priority priority() {
        return priority;
    }

    /**
     * Receives a header block without END_STREAM.
     *
     * @param headers regular and optional status fields from the received header block
     * @throws ValidateException if {@code headers} is {@code null}
     * @throws StatefulException if the block violates final-header or trailer ordering
     */
    public void receiveHeaders(final Headers headers) {
        receiveHeaders(headers, false);
    }

    /**
     * Receives informational, final or trailer headers.
     *
     * @param headers   regular and optional status fields from the received header block
     * @param endStream whether this block ends the remote stream
     * @throws ValidateException if {@code headers} is {@code null}
     * @throws StatefulException if the block violates final-header or trailer ordering
     */
    public void receiveHeaders(final Headers headers, final boolean endStream) {
        receiveHeaders(headers, headers == null ? null : headers.get(Http.Header.PSEUDO_STATUS), endStream);
    }

    /**
     * Receives headers with the HTTP/2 :status sidecar already separated.
     *
     * @param headers        decoded regular header fields
     * @param responseStatus separated {@code :status} value, or {@code null} for trailers
     * @param endStream      whether this block ends the remote stream
     */
    void receiveHeaders(final Headers headers, final String responseStatus, final boolean endStream) {
        final Headers checked = require(headers, "HTTP/2 headers");
        Runnable terminal = null;
        synchronized (this) {
            throwIfFailed();
            final String status = responseStatus;
            if (finalHeaders == null) {
                if (informational(status)) {
                    if (informationalHeaders == null) {
                        informationalHeaders = new ArrayList<>(Normal._1);
                    }
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
                if (inboundBatch.size() != Normal.LONG_ZERO && !body.offer(inboundBatch, inboundBatch.size())) {
                    throw new SocketException("HTTP/2 stream DATA queue is full");
                }
                remoteEnd = true;
                body.finish();
                terminal = terminalIfCompleteLocked();
            }
            signalWaiter();
        }
        run(terminal);
    }

    /**
     * Returns the final response :status sidecar.
     *
     * @return final response status text, or {@code null} before final headers
     */
    public String responseStatus() {
        return responseStatus;
    }

    /**
     * Receives body data without END_STREAM.
     *
     * @param data immutable DATA payload bytes
     * @throws ValidateException if {@code data} is {@code null}
     */
    public void receiveData(final ByteString data) {
        receiveData(data, false);
    }

    /**
     * Appends body data and wakes the streaming consumer.
     *
     * @param data      immutable DATA payload bytes
     * @param endStream whether this data ends the remote stream
     * @throws ValidateException if {@code data} is {@code null}
     */
    public void receiveData(final ByteString data, final boolean endStream) {
        final ByteString checked = require(data, "HTTP/2 data");
        Runnable terminal = null;
        Runnable cancel = null;
        long discarded = Normal.LONG_ZERO;
        try {
            synchronized (this) {
                if (sourceClosed || terminalNotified) {
                    discarded = checked.size();
                    return;
                }
                throwIfFailed();
                if (remoteEnd) {
                    discarded = checked.size();
                    return;
                }
                if (checked.size() > maxQueuedBytes - body.queuedBytes() - inboundBatch.size()) {
                    discarded = body.queuedBytes() + inboundBatch.size() + checked.size();
                    inboundBatch.clear();
                    body.cancel();
                    failure = new SocketException("HTTP/2 stream queued DATA limit exceeded");
                    remoteEnd = true;
                    sourceClosed = true;
                    outcome = Outcome.RESET;
                    cancel = cancelCallback;
                    terminal = terminalLocked();
                    signalWaiter();
                    return;
                }
                if (checked.size() != Normal._0) {
                    checked.write(inboundBatch);
                }
                if (inboundBatch.size() >= Normal._16384 || endStream && inboundBatch.size() != Normal.LONG_ZERO) {
                    if (!body.offer(inboundBatch, inboundBatch.size())) {
                        throw new SocketException("HTTP/2 stream DATA queue is full");
                    }
                }
                if (endStream) {
                    remoteEnd = true;
                    body.finish();
                    terminal = terminalIfCompleteLocked();
                }
                signalWaiter();
            }
        } finally {
            if (discarded > Normal.LONG_ZERO) {
                consumedCallback.accept(discarded);
            }
            run(cancel);
            run(terminal);
        }
    }

    /**
     * Receives an unpadded reader-owned DATA buffer directly into the coalescing batch.
     *
     * @param data      decoded DATA payload whose ownership transfers to the stream
     * @param length    payload size in bytes
     * @param endStream whether this DATA frame closes the remote side
     */
    void receiveData(final Buffer data, final long length, final boolean endStream) {
        final Buffer checked = require(data, "HTTP/2 data buffer");
        if (length < Normal.LONG_ZERO || length > checked.size()) {
            throw new ValidateException("HTTP/2 DATA buffer length is invalid");
        }
        Runnable terminal = null;
        Runnable cancel = null;
        long discarded = Normal.LONG_ZERO;
        try {
            synchronized (this) {
                if (sourceClosed || terminalNotified || remoteEnd) {
                    discarded = length;
                    return;
                }
                throwIfFailed();
                if (length > maxQueuedBytes - body.queuedBytes() - inboundBatch.size()) {
                    discarded = body.queuedBytes() + inboundBatch.size() + length;
                    inboundBatch.clear();
                    body.cancel();
                    failure = new SocketException("HTTP/2 stream queued DATA limit exceeded");
                    remoteEnd = true;
                    sourceClosed = true;
                    outcome = Outcome.RESET;
                    cancel = cancelCallback;
                    terminal = terminalLocked();
                    signalWaiter();
                    return;
                }
                if (length != Normal.LONG_ZERO) {
                    inboundBatch.writeCopy(checked, length);
                }
                if (inboundBatch.size() >= Normal._16384 || endStream && inboundBatch.size() != Normal.LONG_ZERO) {
                    if (!body.offer(inboundBatch, inboundBatch.size())) {
                        throw new SocketException("HTTP/2 stream DATA queue is full");
                    }
                }
                if (endStream) {
                    remoteEnd = true;
                    body.finish();
                    terminal = terminalIfCompleteLocked();
                }
                signalWaiter();
            }
        } finally {
            if (discarded > Normal.LONG_ZERO)
                consumedCallback.accept(discarded);
            run(cancel);
            run(terminal);
        }
    }

    /**
     * Waits only for final non-1xx headers, failure or timeout.
     *
     * @param timeout wait duration; zero waits without a deadline
     * @return final response headers
     * @throws ValidateException                                   if {@code timeout} is {@code null} or negative
     * @throws RuntimeException                                    if a stored stream failure is observed or the stream
     *                                                             ends before final headers arrive
     * @throws org.miaixz.bus.core.lang.exception.TimeoutException if a positive timeout expires
     * @throws InternalException                                   if the waiting thread is interrupted
     */
    public Headers awaitResponseHeaders(final Duration timeout) {
        final Duration checked = require(timeout, "HTTP/2 header timeout");
        if (checked.isNegative()) {
            throw new ValidateException("HTTP/2 header timeout must not be negative");
        }
        final long timeoutNanos = checked.isZero() ? Long.MAX_VALUE : nanos(checked);
        final long deadline = timeoutNanos == Long.MAX_VALUE ? Long.MAX_VALUE : System.nanoTime() + timeoutNanos;
        for (;;) {
            final Headers current = finalHeaders;
            if (current != null) {
                return current;
            }
            throwIfFailed();
            if (remoteEnd) {
                throw new SocketException("HTTP/2 stream ended before final response headers");
            }
            waiter = Thread.currentThread();
            if (finalHeaders == null && failure == null && !remoteEnd) {
                if (deadline == Long.MAX_VALUE) {
                    LockSupport.park(this);
                } else {
                    final long remaining = deadline - System.nanoTime();
                    if (remaining <= 0L) {
                        waiter = null;
                        throw new org.miaixz.bus.core.lang.exception.TimeoutException(
                                "Timed out waiting for HTTP/2 response headers");
                    }
                    LockSupport.parkNanos(this, remaining);
                }
            }
            waiter = null;
            if (Thread.interrupted()) {
                throw new InternalException("Interrupted while waiting for HTTP/2 response headers");
            }
        }
    }

    /**
     * Returns informational response headers received before the final response.
     *
     * @return immutable snapshot in receive order, or an empty list when none have arrived
     */
    List<Headers> informationalHeaders() {
        synchronized (this) {
            return informationalHeaders == null ? List.of() : List.copyOf(informationalHeaders);
        }
    }

    /**
     * Returns the currently stored trailer snapshot without waiting for remote completion.
     *
     * @return received trailers, or empty headers before trailers arrive
     */
    public Headers trailers() {
        synchronized (this) {
            throwIfFailed();
            return trailers;
        }
    }

    /**
     * Returns the one-shot streaming payload facade.
     *
     * @return payload whose source reads incremental DATA from this stream
     */
    public Payload payload() {
        return payload;
    }

    /**
     * Opens and returns the one-shot streaming body source.
     *
     * @return source backed by this stream's inbound DATA buffer
     * @throws StatefulException if the payload source was already opened
     */
    public Source source() {
        return payload.source();
    }

    /**
     * Returns the lazily materialized parser-side inbound sink.
     *
     * @return stable sink instance adapting writes and close to DATA transitions
     */
    public Sink sink() {
        InboundSink current = sink;
        if (current == null) {
            synchronized (this) {
                current = sink;
                if (current == null) {
                    current = new InboundSink();
                    sink = current;
                }
            }
        }
        return current;
    }

    /**
     * Stores priority metadata.
     *
     * @param value non-null priority metadata replacing the previous value
     * @throws ValidateException if {@code value} is {@code null}
     */
    void priority(final Http2Priority value) {
        priority = require(value, "HTTP/2 priority");
    }

    /**
     * Fails the stream and wakes every waiter.
     *
     * @param cause non-null terminal failure delivered to stream waiters
     * @throws ValidateException if {@code cause} is {@code null}
     */
    void fail(final RuntimeException cause) {
        fail(cause, Outcome.CONNECTION_FAILURE);
    }

    /**
     * Fails the stream with a structured transport outcome.
     *
     * @param cause           terminal stream failure
     * @param terminalOutcome structured transport outcome exposed to retry logic
     * @throws ValidateException if either argument is {@code null}
     */
    void fail(final RuntimeException cause, final Outcome terminalOutcome) {
        final RuntimeException checked = require(cause, "HTTP/2 stream failure");
        Runnable terminal;
        long discarded = Normal.LONG_ZERO;
        synchronized (this) {
            if (failure == null) {
                failure = checked;
                outcome = require(terminalOutcome, "HTTP/2 stream outcome");
            }
            discarded = body.queuedBytes() + inboundBatch.size();
            inboundBatch.clear();
            body.cancel();
            body.fail(checked);
            remoteEnd = true;
            sourceClosed = true;
            terminal = terminalLocked();
            signalWaiter();
        }
        if (discarded > Normal.LONG_ZERO) {
            consumedCallback.accept(discarded);
        }
        run(terminal);
    }

    /**
     * Returns the structured stream outcome.
     *
     * @return current structured outcome, initially {@link Outcome#OPEN}
     */
    Outcome outcome() {
        synchronized (this) {
            return outcome;
        }
    }

    /**
     * Closes the response body, discards buffered bytes, and sends CANCEL once when remote END_STREAM has not arrived.
     */
    @Override
    public void close() {
        closeSource();
    }

    /**
     * Returns whether no failure has been stored and the body source has not been closed.
     *
     * @return {@code true} even after remote END_STREAM while unread body data keeps the source open
     */
    public boolean opened() {
        return failure == null && !sourceClosed;
    }

    /**
     * Returns whether the peer has ended its side of this stream.
     *
     * @return {@code true} after a header or DATA transition carries END_STREAM, or after failure forces remote end
     */
    boolean remoteEnded() {
        synchronized (this) {
            return remoteEnd;
        }
    }

    /**
     * Closes the source and chooses normal completion or early cancellation.
     */
    private void closeSource() {
        Runnable cancel = null;
        Runnable terminal;
        long discarded = Normal.LONG_ZERO;
        synchronized (this) {
            if (sourceClosed) {
                return;
            }
            sourceClosed = true;
            if (!remoteEnd) {
                cancel = cancelCallback;
                outcome = Outcome.CANCELLED;
            }
            discarded = body.queuedBytes();
            body.cancel();
            terminal = terminalLocked();
            signalWaiter();
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
     * @return newly claimed terminal callback when remote end is reached and no body bytes remain; otherwise
     *         {@code null}
     */
    private Runnable terminalIfCompleteLocked() {
        return remoteEnd && body.queuedBytes() == Normal.LONG_ZERO ? terminalLocked() : null;
    }

    /**
     * Claims the terminal callback exactly once.
     *
     * @return terminal ownership callback on the first claim, or {@code null} after it was already claimed
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
     * Unparks the request thread currently waiting for response headers, when present.
     */
    private void signalWaiter() {
        final Thread thread = waiter;
        if (thread != null) {
            LockSupport.unpark(thread);
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
     * @param status decimal status text, or {@code null}
     * @return {@code true} when the parsed integer lies from 100 through 199
     * @throws SocketException if non-null status text is not a decimal integer
     */
    private static boolean informational(final String status) {
        if (status == null) {
            return false;
        }
        try {
            final int code = Integer.parseInt(status);
            return code >= Http.Status.CONTINUE && code < Http.Status.OK;
        } catch (final NumberFormatException e) {
            throw new SocketException("Invalid HTTP/2 response status", e);
        }
    }

    /**
     * Converts a duration to a saturated nanosecond count.
     *
     * @param duration positive duration to convert
     * @return at least one nanosecond, or {@link Long#MAX_VALUE} when conversion overflows
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
     * @param callback ownership callback to run, or {@code null} for no operation
     */
    private static void run(final Runnable callback) {
        if (callback != null) {
            callback.run();
        }
    }

    /**
     * Validates and returns a required reference.
     *
     * @param value reference to validate
     * @param name  logical reference name used in the validation message
     * @param <T>   reference type
     * @return the validated non-null reference
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
            synchronized (Http2Stream.this) {
                if (sourceOpened) {
                    throw new StatefulException("HTTP/2 body source can only be opened once");
                }
                sourceOpened = true;
                return this;
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
            if (sourceClosed) {
                throw new StatefulException("HTTP/2 body source is closed");
            }
            final long consumed;
            try {
                consumed = body.read(checked, byteCount);
            } catch (final IOException e) {
                throw new SocketException("Unable to read HTTP/2 stream body", e);
            }
            if (consumed == Normal.__1) {
                Runnable terminal;
                synchronized (Http2Stream.this) {
                    sourceClosed = true;
                    terminal = terminalLocked();
                }
                run(terminal);
                return Normal.__1;
            }
            return consumed;
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
