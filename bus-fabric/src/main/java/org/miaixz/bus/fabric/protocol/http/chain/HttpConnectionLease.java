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

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.registry.connection.ConnectionLease;
import org.miaixz.bus.logger.Logger;

/**
 * Idempotent response-body ownership bridge for one pooled HTTP connection lease.
 *
 * @author Kimi Liu
 * @since Java 21+
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
     * Atomic completion flags.
     */
    private volatile int state;

    /**
     * Creates a lease owner.
     *
     * @param lease    pool lease
     * @param reusable whether protocol semantics allow reuse
     */
    HttpConnectionLease(final ConnectionLease lease, final boolean reusable) {
        this.lease = require(lease, "Connection lease");
        this.reusable = reusable;
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
        final boolean complete = (observed & COMPLETE) != 0;
        final boolean broken = (observed & BROKEN) != 0;
        try {
            final boolean healthy = lease.connection().healthy();
            if (complete && !broken && reusable && healthy) {
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
     * Returns the owner embedded in a wrapped payload.
     *
     * @param payload response payload
     * @return lease owner or {@code null}
     */
    static HttpConnectionLease from(final Payload payload) {
        return payload instanceof LeasePayload tracked ? tracked.owner() : null;
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
         */
        private HttpConnectionLease owner() {
            return HttpConnectionLease.this;
        }

        @Override
        public long length() {
            return delegate.length();
        }

        @Override
        public Source source() {
            return new LeaseSource(delegate.source());
        }

        @Override
        public byte[] bytes() {
            return bytes(Normal.MEBI_64);
        }

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

        @Override
        public String text(final Charset charset) {
            return text(charset, Normal.MEBI_64);
        }

        @Override
        public String text(final Charset charset, final long maxBytes) {
            return new String(bytes(maxBytes),
                    Assert.notNull(charset, () -> new ValidateException("Charset must not be null")));
        }

        @Override
        public boolean repeatable() {
            return delegate.repeatable();
        }

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

        @Override
        public long read(final Buffer sink, final long byteCount) throws IOException {
            try {
                final long read = delegate.read(sink, byteCount);
                if (read < 0) {
                    complete();
                    release();
                }
                return read;
            } catch (final IOException e) {
                broken();
                release();
                throw e;
            }
        }

        @Override
        public Timeout timeout() {
            return delegate.timeout();
        }

        @Override
        public void close() throws IOException {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            try {
                delegate.close();
            } catch (final IOException e) {
                broken();
                release();
                throw e;
            }
            release();
        }
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

}
