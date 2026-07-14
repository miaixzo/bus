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
package org.miaixz.bus.fabric.protocol.sse;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.protocol.sse.event.SseReader;
import org.miaixz.bus.fabric.protocol.sse.event.SseRetry;
import org.miaixz.bus.logger.Logger;

/**
 * Open SSE session.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SseSession {

    /**
     * Logger tag used by the fabric runtime.
     */
    private static final String LOG_TAG = "Fabric";

    /**
     * Session address.
     */
    private final Address address;

    /**
     * Retry policy updated by events.
     */
    private final SseRetry retry;

    /**
     * Streaming reader.
     */
    private final AtomicReference<SseReader> reader;

    /**
     * Background stream future.
     */
    private final CompletableFuture<Void> stream;

    /**
     * Cancellation hook.
     */
    private final Runnable cancelHook;

    /**
     * Lifecycle listener.
     */
    private final Listener<? super SseSession> listener;

    /**
     * Lifecycle state.
     */
    private final AtomicReference<Status> state;

    /**
     * Creates an opened session.
     *
     * @param address    session address
     * @param retry      retry policy
     * @param reader     streaming reader
     * @param stream     stream future
     * @param cancelHook cancellation hook
     */
    SseSession(final Address address, final SseRetry retry, final SseReader reader,
            final CompletableFuture<Void> stream, final Runnable cancelHook) {
        this(address, retry, reader, stream, cancelHook, Wiring.noop());
    }

    /**
     * Creates an opened session.
     *
     * @param address    session address
     * @param retry      retry policy
     * @param reader     streaming reader
     * @param stream     stream future
     * @param cancelHook cancellation hook
     * @param listener   lifecycle listener
     */
    SseSession(final Address address, final SseRetry retry, final SseReader reader,
            final CompletableFuture<Void> stream, final Runnable cancelHook,
            final Listener<? super SseSession> listener) {
        this.address = require(address, "SSE address");
        this.retry = require(retry, "SSE retry");
        this.reader = new AtomicReference<>(require(reader, "SSE reader"));
        this.stream = require(stream, "SSE stream future");
        this.cancelHook = cancelHook == null ? () -> {
        } : cancelHook;
        this.listener = listener == null ? Wiring.noop() : listener;
        this.state = new AtomicReference<>(Status.OPENED);
        this.stream.whenComplete((ignored, cause) -> {
            if (cause == null) {
                if (state.compareAndSet(Status.OPENED, Status.CLOSED)
                        || state.compareAndSet(Status.RUNNING, Status.CLOSED)) {
                    this.listener.close(this);
                    Logger.info(
                            false,
                            LOG_TAG,
                            "SSE session stream closed: scheme={}, host={}, port={}",
                            address.scheme(),
                            address.host(),
                            address.port());
                }
            } else if (!stream.isCancelled()) {
                if (state.compareAndSet(Status.OPENED, Status.FAILED)
                        || state.compareAndSet(Status.RUNNING, Status.FAILED)) {
                    this.listener.failure(this, cause);
                    Logger.warn(
                            false,
                            LOG_TAG,
                            cause,
                            "SSE session stream failed: scheme={}, host={}, port={}, exception={}",
                            address.scheme(),
                            address.host(),
                            address.port(),
                            cause.getClass().getSimpleName());
                }
            }
        });
    }

    /**
     * Returns the session address.
     *
     * @return address
     */
    public Address address() {
        return address;
    }

    /**
     * Returns the lifecycle state.
     *
     * @return lifecycle state
     */
    public Status state() {
        return state.get();
    }

    /**
     * Returns current retry delay.
     *
     * @return retry delay
     */
    public Duration retry() {
        return retry.current();
    }

    /**
     * Closes the session.
     *
     * @return true when this invocation changed the state
     */
    public boolean close() {
        if (state.compareAndSet(Status.OPENED, Status.CLOSED) || state.compareAndSet(Status.RUNNING, Status.CLOSED)
                || state.compareAndSet(Status.CLOSING, Status.CLOSED)) {
            Logger.info(
                    true,
                    LOG_TAG,
                    "SSE session close started: scheme={}, host={}, port={}",
                    address.scheme(),
                    address.host(),
                    address.port());
            cancelHook.run();
            closeReader();
            stream.cancel(false);
            listener.close(this);
            Logger.info(
                    false,
                    LOG_TAG,
                    "SSE session closed: scheme={}, host={}, port={}",
                    address.scheme(),
                    address.host(),
                    address.port());
            return true;
        }
        return false;
    }

    /**
     * Cancels the session.
     *
     * @return true when this invocation changed the state
     */
    public boolean cancel() {
        while (true) {
            final Status current = state.get();
            if (current == Status.CANCELLED || current == Status.CLOSED || current == Status.DONE) {
                return false;
            }
            if (state.compareAndSet(current, Status.CANCELLED)) {
                Logger.info(
                        true,
                        LOG_TAG,
                        "SSE session cancel started: scheme={}, host={}, port={}",
                        address.scheme(),
                        address.host(),
                        address.port());
                cancelHook.run();
                closeReader();
                stream.cancel(false);
                listener.failure(this, new StatefulException("SSE session was cancelled"));
                Logger.info(
                        false,
                        LOG_TAG,
                        "SSE session cancelled: scheme={}, host={}, port={}",
                        address.scheme(),
                        address.host(),
                        address.port());
                return true;
            }
        }
    }

    /**
     * Returns whether the session is opened.
     *
     * @return true when opened
     */
    public boolean opened() {
        final Status current = state.get();
        return current == Status.OPENED || current == Status.RUNNING;
    }

    /**
     * Closes the reader and wraps close failures.
     */
    private void closeReader() {
        try {
            reader.get().close();
        } catch (final RuntimeException e) {
            throw new InternalException("Unable to close SSE session", e);
        }
    }

    /**
     * Replaces the current reader after a reconnect.
     *
     * @param next next reader
     */
    void replaceReader(final SseReader next) {
        reader.set(require(next, "SSE reader"));
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
