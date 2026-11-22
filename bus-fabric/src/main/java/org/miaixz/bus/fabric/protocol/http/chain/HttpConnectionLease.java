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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.protocol.http.codec.Http1Codec;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;
import org.miaixz.bus.logger.Logger;

/**
 * Idempotent response-body ownership bridge for one pooled HTTP connection lease.
 *
 * @author Kimi Liu
 */
final class HttpConnectionLease {

    /**
     * Completion state updater.
     */
    private static final VarHandle STATE;

    /**
     * Body fully consumed bit.
     */
    private static final int COMPLETE = Normal._1;

    /**
     * Body failed bit.
     */
    private static final int BROKEN = 1 << 1;

    /**
     * Lease already finalized bit.
     */
    private static final int RELEASED = 1 << 2;

    static {
        try {
            STATE = MethodHandles.lookup().findVarHandle(HttpConnectionLease.class, "state", int.class);
        } catch (final ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Physical/logical pool lease.
     */
    private final ConnectionLease lease;

    /**
     * Whether protocol framing permits reuse after complete consumption.
     */
    private final boolean reusable;

    /**
     * Message completion supplied by the protocol decoder, when available.
     */
    private final Lifecycle framing;

    /**
     * Atomic completion flags.
     */
    private volatile int state;

    /**
     * Creates a lease owner.
     *
     * @param lease    pool lease
     * @param reusable whether protocol semantics allow reuse
     * @param payload  original transport payload supplying framing state
     */
    HttpConnectionLease(final ConnectionLease lease, final boolean reusable, final Payload payload) {
        this.lease = require(lease, "Connection lease");
        this.reusable = reusable;
        this.framing = payload instanceof Lifecycle body ? body : null;
    }

    /**
     * Returns the owner embedded in a wrapped payload.
     *
     * @param payload response payload
     * @return lease owner or {@code null}
     */
    static HttpConnectionLease from(final Payload payload) {
        return payload instanceof LeasePayload tracked ? tracked.owner() : null;
    }

    /**
     * Requires a non-null value.
     *
     * @param value value
     * @param name  diagnostic name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

    /**
     * Wraps a transport payload with this lease lifecycle.
     *
     * @param payload payload to wrap
     * @return lease-aware payload
     */
    Payload wrap(final Payload payload) {
        return new LeasePayload(require(payload, "Payload"));
    }

    /**
     * Returns whether this owner belongs to a lease.
     *
     * @param current lease to compare
     * @return true for identical lease
     */
    boolean matches(final ConnectionLease current) {
        return lease == current;
    }

    /**
     * Finalizes the lease once, reusing only a fully consumed, unbroken, healthy connection.
     */
    void release() {
        int observed;
        do {
            observed = state;
            if ((observed & RELEASED) != 0) {
                return;
            }
        } while (!STATE.compareAndSet(this, observed, observed | RELEASED));
        final boolean complete = framing == null ? (observed & COMPLETE) != 0
                : framing.state() == Lifecycle.State.COMPLETED;
        final boolean broken = (observed & BROKEN) != 0;
        try {
            final boolean healthy = lease.connection().healthy();
            final Object attachment = lease.connection().protocolAttachment();
            final boolean persistent = !(attachment instanceof Http1Codec codec) || codec.reusable();
            if (complete && !broken && reusable && persistent && healthy) {
                Logger.debug(
                        false,
                        "Fabric",
                        "HTTP tracked lease released: complete={}, broken={}, healthy={}",
                        true,
                        false,
                        true);
                lease.release();
            } else {
                Logger.debug(
                        false,
                        "Fabric",
                        "HTTP tracked lease closed: complete={}, broken={}, healthy={}",
                        complete,
                        broken,
                        healthy);
                lease.close();
            }
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to release HTTP connection", e);
        }
    }

    /**
     * Marks the body complete.
     */
    private void complete() {
        STATE.getAndBitwiseOr(this, COMPLETE);
    }

    /**
     * Marks the body broken.
     */
    private void broken() {
        STATE.getAndBitwiseOr(this, BROKEN);
    }

    /**
     * Payload view that couples materialization and closure to lease finalization.
     */
    private final class LeasePayload implements Payload, AutoCloseable {

        /**
         * Delegate payload.
         */
        private final Payload delegate;

        /**
         * Creates a payload view.
         *
         * @param delegate payload delegate
         */
        private LeasePayload(final Payload delegate) {
            this.delegate = delegate;
        }

        /**
         * Returns the owning lease state.
         *
         * @return lease owning the response payload
         */
        private HttpConnectionLease owner() {
            return HttpConnectionLease.this;
        }

        /**
         * Returns the length reported by the underlying response body.
         *
         * @return body length in bytes, or {@code -1} if unknown
         */
        @Override
        public long length() {
            return delegate.length();
        }

        /**
         * Returns a source that releases the connection lease at EOF, on failure, or on close.
         *
         * @return lease-aware view of the response body source
         */
        @Override
        public Source source() {
            return new LeaseSource(delegate.source());
        }

        /**
         * Reads the entire body within the default 64 MiB limit and releases the lease.
         *
         * @return complete response body
         */
        @Override
        public byte[] bytes() {
            return bytes(Normal.MEBI_64);
        }

        /**
         * Reads the response body up to the specified limit and releases the connection lease.
         *
         * @param maxBytes maximum number of bytes to read
         * @return complete response body
         */
        @Override
        public byte[] bytes(final long maxBytes) {
            try {
                final byte[] data = delegate.bytes(maxBytes);
                complete();
                release();
                return data;
            } catch (final RuntimeException e) {
                broken();
                release();
                throw e;
            }
        }

        /**
         * Decodes the body within the default 64 MiB limit and releases the lease.
         *
         * @param charset character encoding used for decoding
         * @return decoded response body
         */
        @Override
        public String text(final Charset charset) {
            return text(charset, Normal.MEBI_64);
        }

        /**
         * Reads and decodes the bounded response body, releasing the connection lease.
         *
         * @param charset  character encoding used for decoding
         * @param maxBytes maximum number of bytes to read
         * @return decoded response body
         */
        @Override
        public String text(final Charset charset, final long maxBytes) {
            return new String(bytes(maxBytes),
                    Assert.notNull(charset, () -> new ValidateException("Charset must not be null")));
        }

        /**
         * Reports whether the underlying response payload can be read repeatedly.
         *
         * @return {@code true} if the payload is repeatable
         */
        @Override
        public boolean repeatable() {
            return delegate.repeatable();
        }

        /**
         * Closes the underlying payload if it supports closing and releases its lease.
         *
         * @throws Exception if the underlying payload cannot be closed
         */
        @Override
        public void close() throws Exception {
            try {
                if (delegate instanceof AutoCloseable closeable) {
                    closeable.close();
                }
            } catch (final Exception e) {
                broken();
                release();
                throw e;
            }
            release();
        }

    }

    /**
     * Source view that recognizes EOF and failures.
     */
    private final class LeaseSource implements Source {

        /**
         * Delegate source.
         */
        private final Source delegate;

        /**
         * Idempotent close guard.
         */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Creates a source view.
         *
         * @param delegate source delegate
         */
        private LeaseSource(final Source delegate) {
            this.delegate = require(delegate, "Source");
        }

        /**
         * Reads from the response and releases the lease at EOF or upon a read failure.
         *
         * @param sink      buffer that receives the body bytes
         * @param byteCount maximum number of bytes to read
         * @return number of bytes read, or {@code -1} at EOF
         * @throws IOException if the source cannot be read
         */
        @Override
        public long read(final Buffer sink, final long byteCount) throws IOException {
            try {
                final long read = delegate.read(sink, byteCount);
                if (read < 0) {
                    complete();
                    release();
                }
                return read;
            } catch (final IOException | RuntimeException e) {
                broken();
                release();
                throw e;
            }
        }

        /**
         * Returns the timeout that governs reads from the underlying source.
         *
         * @return delegate source timeout
         */
        @Override
        public Timeout timeout() {
            return delegate.timeout();
        }

        /**
         * Closes the response source and releases its connection lease.
         *
         * @throws IOException if the underlying source cannot be closed
         */
        @Override
        public void close() throws IOException {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            try {
                delegate.close();
            } catch (final IOException | RuntimeException e) {
                broken();
                release();
                throw e;
            }
            release();
        }

    }

}
