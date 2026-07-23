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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Listener;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.protocol.http.HttpRunner;
import org.miaixz.bus.fabric.protocol.sse.event.SseReader;
import org.miaixz.bus.fabric.protocol.sse.retry.SseRetry;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.lifecycle.LifecycleScope;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * Open SSE session that owns reconnectable HTTP stream resources under one terminal lifecycle.
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
     * Retry state whose current delay may be updated by received SSE events.
     */
    private final SseRetry retry;

    /**
     * Cancellation scope shared by the initial connection, reconnects, and session termination.
     */
    private final Cancellation cancellation;

    /**
     * Immutable session attributes containing the stable operation identifier.
     */
    private final Map<String, Object> attributes;

    /**
     * Safe observer wrapper that replaces lifecycle-generated operation identifiers with the session identifier.
     */
    private final EventObserver observer;

    /**
     * Most recently installed HTTP stream call, if any.
     */
    private final AtomicReference<Call<HttpRunner.Stream>> httpCall;

    /**
     * HTTP streaming response currently owned by the session, if any.
     */
    private final AtomicReference<HttpRunner.Stream> response;

    /**
     * SSE reader currently consuming the response, if any.
     */
    private final AtomicReference<SseReader> reader;

    /**
     * Dispatcher handle for the current reader task, if any.
     */
    private final AtomicReference<DispatchHandle> readerHandle;

    /**
     * Dispatcher handle for the currently scheduled reconnect task, if any.
     */
    private final AtomicReference<DispatchHandle> reconnectHandle;

    /**
     * Session stream future whose success or failure drives terminal lifecycle transitions.
     */
    private final CompletableFuture<Void> stream;

    /**
     * Replaceable factory-removal hook consumed by terminal cleanup.
     */
    private final AtomicReference<Runnable> onClose;

    /**
     * One-way guard granting exactly one caller ownership of terminal cleanup.
     */
    private final AtomicBoolean terminated;

    /**
     * Lifecycle state, listener, observer, and duration coordinator for this session.
     */
    private final LifecycleScope scope;

    /**
     * Creates an opened session sharing one cancellation and observation scope across reconnects.
     *
     * @param address      remote SSE endpoint address
     * @param retry        mutable retry state shared across reconnects
     * @param cancellation cancellation scope shared across the complete session
     * @param stream       future representing background stream completion
     * @param listener     lifecycle listener, or {@code null} when disabled
     * @param observer     event observer, or {@code null} to use a safe no-op sink
     * @param clock        clock used for lifecycle event timing
     * @param operationId  stable identifier attached to all session events
     * @throws ValidateException if a required argument is {@code null}
     */
    SseSession(final Address address, final SseRetry retry, final Cancellation cancellation,
            final CompletableFuture<Void> stream, final Listener<? super SseSession> listener,
            final EventObserver observer, final Clock clock, final String operationId) {
        this.address = require(address, "SSE address");
        this.retry = require(retry, "SSE retry");
        this.cancellation = require(cancellation, "SSE cancellation");
        final String currentOperationId = require(operationId, "SSE operation id");
        this.attributes = Map.of(Builder.TAG_OPERATION_ID, currentOperationId);
        final EventObserver sink = EventObserver.safe(observer);
        this.observer = event -> sink.emit(withOperationId(event, currentOperationId));
        this.httpCall = new AtomicReference<>();
        this.response = new AtomicReference<>();
        this.reader = new AtomicReference<>();
        this.readerHandle = new AtomicReference<>();
        this.reconnectHandle = new AtomicReference<>();
        this.stream = require(stream, "SSE stream future");
        this.onClose = new AtomicReference<>(() -> {
        });
        this.terminated = new AtomicBoolean();
        this.scope = LifecycleScope.session(
                this,
                "sse-session",
                listener,
                this.observer,
                ObservationMarker.SSE_OPEN,
                ObservationMarker.SSE_CLOSED,
                ObservationMarker.SSE_FAILED,
                require(clock, "SSE clock"));
        this.scope.open(this);
        this.stream.whenComplete((ignored, cause) -> {
            if (cause == null) {
                terminate(Termination.CLOSE, null);
            } else if (!stream.isCancelled()) {
                terminate(Termination.FAILURE, cause);
            }
        });
    }

    /**
     * Returns the session address.
     *
     * @return remote SSE endpoint address
     */
    public Address address() {
        return address;
    }

    /**
     * Returns the lifecycle state.
     *
     * @return current state maintained by the lifecycle scope
     */
    public Status state() {
        return scope.state();
    }

    /**
     * Returns current retry delay.
     *
     * @return current reconnect delay from the shared retry state
     */
    public Duration retry() {
        return retry.current();
    }

    /**
     * Runs the normal-close terminal path and releases owned stream resources.
     *
     * @return {@code true} if this invocation owned termination and changed the lifecycle state
     */
    public boolean close() {
        return terminate(Termination.CLOSE, null);
    }

    /**
     * Cancels the shared scope with a session-cancelled failure and releases owned stream resources.
     *
     * @return {@code true} if this invocation owned termination and changed the lifecycle state
     */
    public boolean cancel() {
        return terminate(Termination.CANCEL, new StatefulException("SSE session was cancelled"));
    }

    /**
     * Fails this session and releases all owned stream resources once.
     *
     * @param cause failure recorded by cancellation and lifecycle observation
     * @return {@code true} if this invocation owned termination and changed the lifecycle state
     * @throws ValidateException if {@code cause} is {@code null}
     */
    boolean failure(final Throwable cause) {
        return terminate(Termination.FAILURE, require(cause, "SSE failure cause"));
    }

    /**
     * Returns session attributes.
     *
     * @return immutable map containing the stable operation identifier
     */
    @Override
    public Map<String, Object> attributes() {
        return attributes;
    }

    /**
     * Returns the shared cancellation scope.
     *
     * @return cancellation scope shared by this session and all reconnect attempts
     */
    Cancellation cancellation() {
        return cancellation;
    }

    /**
     * Installs the next HTTP stream call and cancels the previously installed call.
     * <p>
     * When termination has already begun, the incoming call is cancelled instead of being installed.
     * </p>
     *
     * @param next HTTP stream call to install
     * @throws ValidateException if {@code next} is {@code null}
     */
    synchronized void replaceHttpCall(final Call<HttpRunner.Stream> next) {
        final Call<HttpRunner.Stream> current = require(next, "SSE HTTP Call");
        if (terminated.get()) {
            current.cancel();
            return;
        }
        cancel(httpCall.getAndSet(current));
    }

    /**
     * Installs a call, streaming response, and reader, then cancels the previous reader handle and closes the previous
     * response and reader.
     * <p>
     * When termination has already begun, the incoming call and resources are released immediately.
     * </p>
     *
     * @param call         HTTP call that produced the incoming response
     * @param nextResponse incoming streaming response owned by the session
     * @param nextReader   incoming reader consuming that response
     * @throws ValidateException if any argument is {@code null}
     */
    synchronized void replaceReader(
            final Call<HttpRunner.Stream> call,
            final HttpRunner.Stream nextResponse,
            final SseReader nextReader) {
        final Call<HttpRunner.Stream> currentCall = require(call, "SSE HTTP Call");
        final HttpRunner.Stream currentResponse = require(nextResponse, "SSE response");
        final SseReader currentReader = require(nextReader, "SSE reader");
        if (terminated.get()) {
            currentCall.cancel();
            close(currentResponse);
            close(currentReader);
            return;
        }
        final DispatchHandle oldHandle = readerHandle.getAndSet(null);
        final HttpRunner.Stream oldResponse = response.getAndSet(currentResponse);
        final SseReader oldReader = reader.getAndSet(currentReader);
        if (httpCall.get() != currentCall) {
            httpCall.set(currentCall);
        }
        cancel(oldHandle);
        close(oldResponse);
        close(oldReader);
    }

    /**
     * Replaces the reader background handle.
     *
     * @param next dispatcher handle for the replacement reader task
     * @throws ValidateException if {@code next} is {@code null}
     */
    synchronized void replaceReaderHandle(final DispatchHandle next) {
        replaceHandle(readerHandle, require(next, "SSE reader handle"));
    }

    /**
     * Replaces the scheduled reconnect handle.
     *
     * @param next dispatcher handle for the replacement reconnect task
     * @throws ValidateException if {@code next} is {@code null}
     */
    synchronized void replaceReconnectHandle(final DispatchHandle next) {
        replaceHandle(reconnectHandle, require(next, "SSE reconnect handle"));
    }

    /**
     * Replaces the factory-removal hook that terminal cleanup invokes once.
     * <p>
     * A hook registered after termination runs immediately. The post-installation check closes the race with concurrent
     * termination.
     * </p>
     *
     * @param hook callback that removes this session from its factory registry
     * @throws ValidateException if {@code hook} is {@code null}
     */
    void onClose(final Runnable hook) {
        final Runnable current = require(hook, "SSE close hook");
        if (terminated.get()) {
            current.run();
            return;
        }
        onClose.set(current);
        if (terminated.get() && onClose.compareAndSet(current, () -> {
        })) {
            current.run();
        }
    }

    /**
     * Replaces one owned dispatcher handle or cancels it after termination.
     *
     * @param reference atomic slot containing the currently owned dispatcher handle
     * @param next      replacement handle to install or cancel after termination
     */
    private void replaceHandle(final AtomicReference<DispatchHandle> reference, final DispatchHandle next) {
        if (terminated.get()) {
            next.cancel();
            return;
        }
        cancel(reference.getAndSet(next));
    }

    /**
     * Executes the single terminal path and releases all resources in their fixed order.
     *
     * @param termination close, cancellation, or failure outcome to publish
     * @param cause       terminal cause for cancellation or failure, or {@code null} for normal close
     * @return {@code true} if this invocation owned cleanup and changed lifecycle state
     */
    private synchronized boolean terminate(final Termination termination, final Throwable cause) {
        if (!terminated.compareAndSet(false, true)) {
            return false;
        }
        if (termination != Termination.CLOSE) {
            cancellation.cancel(cause == null ? new StatefulException("SSE session terminated") : cause);
        }
        scope.closing();
        cancel(httpCall.getAndSet(null));
        close(response.getAndSet(null));
        close(reader.getAndSet(null));
        cancel(readerHandle.getAndSet(null));
        cancel(reconnectHandle.getAndSet(null));
        stream.cancel(false);
        final boolean changed = switch (termination) {
            case CLOSE -> scope.close(this);
            case CANCEL -> scope.cancel(cause);
            case FAILURE -> scope.fail(cause);
        };
        notifyClose();
        Logger.info(
                false,
                "Fabric",
                "SSE session terminated: scheme={}, host={}, port={}, outcome={}",
                address.scheme(),
                address.host(),
                address.port(),
                termination);
        return changed;
    }

    /**
     * Atomically consumes and runs the current factory-removal hook, if one is installed.
     */
    private void notifyClose() {
        final Runnable hook = onClose.getAndSet(null);
        if (hook != null) {
            hook.run();
        }
    }

    /**
     * Cancels an optional Call.
     *
     * @param call call to cancel, or {@code null} when none is owned
     */
    private static void cancel(final Call<?> call) {
        if (call != null) {
            call.cancel();
        }
    }

    /**
     * Cancels an optional dispatcher handle.
     *
     * @param handle dispatcher handle to cancel, or {@code null} when none is owned
     */
    private static void cancel(final DispatchHandle handle) {
        if (handle != null) {
            handle.cancel();
        }
    }

    /**
     * Closes an optional resource.
     *
     * @param resource resource to close, or {@code null} when none is owned
     */
    private static void close(final AutoCloseable resource) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (final Exception e) {
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "Unable to close an SSE session resource: type={}",
                    resource.getClass().getSimpleName());
        }
    }

    /**
     * Replaces LifecycleScope's generated operation tag with the runner-owned identifier.
     *
     * @param event       lifecycle event whose marker, time, tags, and cause are preserved
     * @param operationId runner-owned identifier replacing the generated operation tag
     * @return event carrying the shared identifier
     * @throws ValidateException if {@code event} is {@code null}
     */
    private static FabricEvent withOperationId(final FabricEvent event, final String operationId) {
        final FabricEvent current = require(event, "SSE lifecycle event");
        return new FabricEvent(current.marker(), current.time(),
                current.tags().with(Builder.TAG_OPERATION_ID, operationId), current.cause());
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
     * Terminal outcomes owned by the session cleanup guard.
     */
    private enum Termination {

        /**
         * Normal close that leaves the cancellation scope uncancelled.
         */
        CLOSE,

        /**
         * User cancellation that cancels the shared scope and publishes a cancelled lifecycle state.
         */
        CANCEL,

        /**
         * Stream failure that cancels the shared scope and publishes a failed lifecycle state.
         */
        FAILURE

    }

}
