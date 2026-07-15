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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.protocol.sse.event.SseReader;
import org.miaixz.bus.fabric.protocol.sse.event.SseRetry;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;
import org.miaixz.bus.logger.Logger;

/**
 * Open SSE session.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SseSession implements Session {

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
     * Lifecycle scope.
     */
    private final LifecycleScope scope;

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
        this(address, retry, reader, stream, cancelHook, null);
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
        this.scope = LifecycleScope.session(this, "sse-session", listener, EventObserver.noop(),
                ObservationMarker.SSE_OPEN, ObservationMarker.SSE_CLOSED, ObservationMarker.SSE_FAILED);
        this.scope.open(this);
        this.stream.whenComplete((ignored, cause) -> {
            if (cause == null) {
                if (scope.close(this)) {
                    Logger.info(
                            false,
                            "Fabric",
                            "SSE session stream closed: scheme={}, host={}, port={}",
                            address.scheme(),
                            address.host(),
                            address.port());
                }
            } else if (!stream.isCancelled()) {
                if (scope.fail(cause)) {
                    Logger.warn(
                            false,
                            "Fabric",
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
        return scope.state();
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
        final Status current = scope.state();
        if (current != Status.OPENED && current != Status.RUNNING && current != Status.CLOSING) {
            return false;
        }
        if (current != Status.CLOSING) {
            scope.closing();
        }
        Logger.info(
                true,
                "Fabric",
                "SSE session close started: scheme={}, host={}, port={}",
                address.scheme(),
                address.host(),
                address.port());
        cancelHook.run();
        closeReader();
        stream.cancel(false);
        final boolean changed = scope.close(this);
        if (changed) {
            Logger.info(
                    false,
                    "Fabric",
                    "SSE session closed: scheme={}, host={}, port={}",
                    address.scheme(),
                    address.host(),
                    address.port());
        }
        return changed;
    }

    /**
     * Cancels the session.
     *
     * @return true when this invocation changed the state
     */
    public boolean cancel() {
        final Status current = scope.state();
        if (current == Status.CANCELLED || current == Status.CLOSED || current == Status.DONE) {
            return false;
        }
        final StatefulException cancelled = new StatefulException("SSE session was cancelled");
        Logger.info(
                true,
                "Fabric",
                "SSE session cancel started: scheme={}, host={}, port={}",
                address.scheme(),
                address.host(),
                address.port());
        cancelHook.run();
        closeReader();
        stream.cancel(false);
        final boolean changed = scope.cancel(cancelled);
        if (changed) {
            Logger.info(
                    false,
                    "Fabric",
                    "SSE session cancelled: scheme={}, host={}, port={}",
                    address.scheme(),
                    address.host(),
                    address.port());
        }
        return changed;
    }

    /**
     * Returns session attributes.
     *
     * @return empty attributes
     */
    @Override
    public Map<String, Object> attributes() {
        return Map.of();
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
