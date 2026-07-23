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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.data.id.ID;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Call;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.protocol.Mediator;
import org.miaixz.bus.fabric.protocol.Mediator.Type;
import org.miaixz.bus.fabric.protocol.MonoCall;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpRunner;
import org.miaixz.bus.fabric.protocol.sse.body.SseBody;
import org.miaixz.bus.fabric.protocol.sse.event.SseReader;
import org.miaixz.bus.fabric.protocol.sse.retry.SseRetry;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * Opens and reads SSE streams from an immutable SSE exchange snapshot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class SseRunner {

    /**
     * Immutable exchange configuration and borrowed runtime services.
     */
    private final SseSnapshot snapshot;

    /**
     * Creates a runner.
     *
     * @param snapshot execution snapshot
     */
    SseRunner(final SseSnapshot snapshot) {
        this.snapshot = require(snapshot, "SSE exchange snapshot");
    }

    /**
     * Opens the SSE stream synchronously and starts background event delivery.
     *
     * @return opened session
     */
    SseSession open() {
        return open(Cancellation.create());
    }

    /**
     * Opens the SSE stream within a cancellation scope.
     *
     * @param cancellation scope shared by HTTP opening, stream reading, and reconnect attempts
     * @return opened session whose events are delivered by background dispatcher activities
     */
    SseSession open(final Cancellation cancellation) {
        final Cancellation currentCancellation = require(cancellation, "Cancellation");
        final String operationId = ID.objectId();
        final SseRetry sessionRetry = SseRetry.of(snapshot.retryPolicy());
        final CompletableFuture<Void> stream = new CompletableFuture<>();
        final AtomicReference<SseSession> holder = new AtomicReference<>();
        final AtomicReference<DispatchHandle> handle = new AtomicReference<>();
        final AtomicReference<String> eventId = new AtomicReference<>(snapshot.lastEventId());
        final SseSession session = new SseSession(snapshot.address(), sessionRetry, currentCancellation, stream,
                snapshot.listener(), snapshot.observer(), snapshot.context().clock(), operationId);
        holder.set(session);
        Call<HttpRunner.Stream> httpCall = null;
        SseReader reader = null;
        HttpRunner.Stream response = null;
        Logger.info(
                true,
                "Fabric",
                "SSE open started: scheme={}, host={}, port={}, autoReconnect={}, lastEventIdPresent={}",
                snapshot.address().scheme(),
                snapshot.address().host(),
                snapshot.address().port(),
                snapshot.autoReconnect(),
                snapshot.lastEventId() != null);
        try {
            currentCancellation.throwIfCancelled();
            httpCall = responseCall(snapshot.lastEventId(), currentCancellation);
            session.replaceHttpCall(httpCall);
            currentCancellation.throwIfCancelled();
            response = httpCall.execute();
            currentCancellation.throwIfCancelled();
            validateResponse(response);
            reader = SseBody.source(response.source(), response.body().length()).reader();
            session.replaceReader(httpCall, response, reader);
            handle.set(submitRead(reader, sessionRetry, stream, holder, eventId, handle, operationId, Normal._0));
            session.replaceReaderHandle(handle.get());
            final Runnable unregisterCancellation = currentCancellation.onCancel(session::cancel);
            try {
                currentCancellation.throwIfCancelled();
                Logger.info(
                        false,
                        "Fabric",
                        "SSE open completed: scheme={}, host={}, port={}, autoReconnect={}",
                        snapshot.address().scheme(),
                        snapshot.address().host(),
                        snapshot.address().port(),
                        snapshot.autoReconnect());
                return session;
            } finally {
                unregisterCancellation.run();
            }
        } catch (final CancellationException e) {
            session.cancel();
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "SSE open cancelled: scheme={}, host={}, port={}",
                    snapshot.address().scheme(),
                    snapshot.address().host(),
                    snapshot.address().port());
            throw e;
        } catch (final RuntimeException e) {
            session.failure(e);
            Logger.error(
                    false,
                    "Fabric",
                    e,
                    "SSE open failed: scheme={}, host={}, port={}, exception={}",
                    snapshot.address().scheme(),
                    snapshot.address().host(),
                    snapshot.address().port(),
                    e.getClass().getSimpleName());
            throw e;
        }
    }

    /**
     * Builds a stable reader dispatch key.
     *
     * @return stable host-and-port key used to serialize reader and reconnect activities
     */
    String dispatchKey() {
        return Builder.SSE_RUNNER_DISPATCH_PREFIX + snapshot.address().host() + Symbol.C_COLON
                + snapshot.address().port();
    }

    /**
     * Creates one HTTP stream Call without starting network I/O.
     *
     * @param eventId      last event identifier sent during reconnect, or {@code null}
     * @param cancellation shared cancellation scope
     * @return deferred call that opens one HTTP response stream
     */
    private Call<HttpRunner.Stream> responseCall(final String eventId, final Cancellation cancellation) {
        return MonoCall.create(
                "sse-http-stream",
                dispatchKey() + ":http",
                snapshot.context().reactor().dispatcher(),
                snapshot.observer(),
                null,
                snapshot.timeout(),
                () -> response(eventId, cancellation),
                cancellation::cancel);
    }

    /**
     * Opens one HTTP response through the fabric HTTP carrier.
     *
     * @param eventId      last event identifier included in the request, or {@code null}
     * @param cancellation shared cancellation scope
     * @return accepted streaming HTTP response after request/response filtering and guard checks
     */
    private HttpRunner.Stream response(final String eventId, final Cancellation cancellation) {
        Logger.debug(
                true,
                "Fabric",
                "SSE HTTP stream request started: scheme={}, host={}, port={}, lastEventIdPresent={}",
                snapshot.address().scheme(),
                snapshot.address().host(),
                snapshot.address().port(),
                eventId != null);
        final Headers.Builder builder = Headers.builder().add(Http.Header.ACCEPT, MediaType.SERVER_SENT_EVENTS)
                .add(Http.Header.CACHE_CONTROL, Http.Cache.NO_CACHE);
        if (eventId != null) {
            builder.add(Builder.SSE_RUNNER_LAST_EVENT_ID, eventId);
        }
        for (final Map.Entry<String, List<String>> entry : snapshot.headers().asMap().entrySet()) {
            for (final String value : entry.getValue()) {
                builder.add(entry.getKey(), value);
            }
        }
        final Message opening = filter(
                Message.of(Protocol.HTTP, snapshot.address(), builder.build(), Payload.empty(), Builder.SSE_TAG_OPEN));
        checkGuard(opening);
        final HttpRequest request = HttpRequest.builder().method(Http.Method.GET)
                .url(UnoUrl.parse(snapshot.uri().toString())).headers(opening.headers()).timeout(snapshot.timeout())
                .build();
        final HttpRunner.Stream response = Mediator.convert(
                Type.SSE,
                Type.HTTP_STREAM,
                cancellation,
                current -> HttpRunner.stream(snapshot.context().withFilter(null), request, current));
        final Message accepted = filter(
                Message.of(
                        Protocol.HTTP,
                        snapshot.address(),
                        response.headers(),
                        Payload.empty(),
                        Builder.SSE_TAG_RESPONSE));
        snapshot.responseHandler().accept(response.status(), accepted.headers());
        Logger.debug(
                false,
                "Fabric",
                "SSE HTTP stream response accepted: scheme={}, host={}, port={}, status={}",
                snapshot.address().scheme(),
                snapshot.address().host(),
                snapshot.address().port(),
                response.status());
        return response;
    }

    /**
     * Validates response status and media type.
     *
     * @param response streaming HTTP response to validate
     * @throws ProtocolException if the status is not successful or the content type is not {@code text/event-stream}
     */
    private static void validateResponse(final HttpRunner.Stream response) {
        final int status = response.status();
        if (status < Http.Status.OK || status >= Http.Status.MULTIPLE_CHOICES) {
            throw new ProtocolException("SSE response status must be 2xx");
        }
        final String value = response.headers().get(Http.Header.CONTENT_TYPE);
        final MediaType mediaType;
        try {
            mediaType = MediaType.parse(value);
        } catch (final RuntimeException e) {
            throw new ProtocolException("SSE response has an invalid Content-Type", e);
        }
        if (!"text".equalsIgnoreCase(mediaType.type()) || !"event-stream".equalsIgnoreCase(mediaType.subtype())) {
            throw new ProtocolException("SSE response must be text/event-stream");
        }
    }

    /**
     * Reads and dispatches events in the background.
     *
     * @param reader      active reader that consumes the current HTTP stream
     * @param retry       mutable retry policy updated by stream fields
     * @param stream      terminal future for the logical SSE stream
     * @param holder      reference containing the active session
     * @param eventId     reference containing the most recently dispatched event identifier
     * @param handle      reference containing the active read or reconnect task
     * @param operationId identifier correlating observation events for this session
     * @param attempt     zero-based consecutive reconnect-failure count
     */
    private void read(
            final SseReader reader,
            final SseRetry retry,
            final CompletableFuture<Void> stream,
            final AtomicReference<SseSession> holder,
            final AtomicReference<String> eventId,
            final AtomicReference<DispatchHandle> handle,
            final String operationId,
            final int attempt) {
        final SseSession session = holder.get();
        if (session != null && !session.opened() || stream.isCancelled()) {
            stream.complete(null);
            return;
        }
        try {
            reader.readEvents(new SseReader.Events() {

                /**
                 * Dispatches one parsed SSE event.
                 *
                 * @param id    parsed event identifier, or {@code null}
                 * @param event parsed event type
                 * @param data  assembled event data text
                 */
                @Override
                public void event(final String id, final String event, final String data) {
                    final SseEvent current = SseEvent.of(id, event, data, null);
                    dispatch(current, eventId, operationId);
                }

                /**
                 * Updates the reconnect retry delay announced by the stream.
                 *
                 * @param retryDelay server-provided base delay for later reconnects
                 */
                @Override
                public void retry(final java.time.Duration retryDelay) {
                    retry.update(retryDelay);
                    Logger.debug(
                            false,
                            "Fabric",
                            "SSE server retry directive applied: host={}, port={}, delay={}",
                            snapshot.address().host(),
                            snapshot.address().port(),
                            retryDelay);
                }
            });
            closeReader(reader);
            if (snapshot.autoReconnect()) {
                Logger.info(
                        false,
                        "Fabric",
                        "SSE stream ended; reconnect enabled: host={}, port={}",
                        snapshot.address().host(),
                        snapshot.address().port());
                scheduleReconnect(retry, stream, holder, eventId, handle, operationId, Normal._0);
            } else {
                Logger.info(
                        false,
                        "Fabric",
                        "SSE stream ended: host={}, port={}",
                        snapshot.address().host(),
                        snapshot.address().port());
                stream.complete(null);
            }
        } catch (final RuntimeException e) {
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "SSE read failed: host={}, port={}, attempt={}, exception={}",
                    snapshot.address().host(),
                    snapshot.address().port(),
                    attempt,
                    e.getClass().getSimpleName());
            if (session == null || session.opened()) {
                closeReader(reader);
                if (snapshot.autoReconnect()) {
                    scheduleReconnect(retry, stream, holder, eventId, handle, operationId, attempt + 1);
                } else {
                    stream.completeExceptionally(e);
                }
                return;
            }
            stream.complete(null);
        }
    }

    /**
     * Dispatches one parsed event with synchronous backpressure and isolated handler failures.
     *
     * @param event       parsed event to filter, observe, and deliver
     * @param eventId     reference updated when the event defines an identifier
     * @param operationId stream operation identifier used for observation correlation
     */
    private void dispatch(final SseEvent event, final AtomicReference<String> eventId, final String operationId) {
        if (event.id() != null) {
            eventId.set(event.id());
        }
        final Payload payload = Payload.of(event.data(), StandardCharsets.UTF_8);
        final Message received = filter(
                Message.of(Protocol.HTTP, snapshot.address(), Headers.empty(), payload, Builder.SSE_TAG_EVENT));
        checkGuard(received);
        final Payload filteredPayload = received.payload();
        final SseEvent filteredEvent = SseEvent.of(
                event.id(),
                event.event(),
                filteredPayload.text(StandardCharsets.UTF_8, snapshot.context().options().materializeMaxBytes()),
                event.retry());
        emit(ObservationMarker.SSE_EVENT, null, filteredPayload, operationId);
        Logger.debug(
                false,
                "Fabric",
                "SSE event dispatched: host={}, port={}, idPresent={}, eventType={}, bytes={}",
                snapshot.address().host(),
                snapshot.address().port(),
                event.id() != null,
                event.event(),
                filteredPayload.length());
        try {
            snapshot.handler().accept(filteredEvent);
        } catch (final RuntimeException e) {
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "SSE event handler failed: host={}, port={}, exception={}",
                    snapshot.address().host(),
                    snapshot.address().port(),
                    e.getClass().getSimpleName());
        }
    }

    /**
     * Schedules a reconnect attempt without occupying a dispatcher worker.
     *
     * @param retry       mutable retry policy that computes the delay
     * @param stream      terminal future for the logical SSE stream
     * @param holder      reference containing the active session
     * @param eventId     reference containing the most recently dispatched event identifier
     * @param handle      reference updated with the scheduled reconnect task
     * @param operationId identifier correlating observation events for this session
     * @param attempt     zero-based reconnect attempt used for backoff
     */
    private void scheduleReconnect(
            final SseRetry retry,
            final CompletableFuture<Void> stream,
            final AtomicReference<SseSession> holder,
            final AtomicReference<String> eventId,
            final AtomicReference<DispatchHandle> handle,
            final String operationId,
            final int attempt) {
        final SseSession session = holder.get();
        if (session == null || !session.opened() || stream.isCancelled()) {
            stream.complete(null);
            return;
        }
        final java.time.Duration delay = retry.nextDelay(attempt);
        Logger.info(
                false,
                "Fabric",
                "SSE reconnect scheduled: host={}, port={}, attempt={}, delay={}",
                snapshot.address().host(),
                snapshot.address().port(),
                attempt,
                delay);
        final DispatchHandle next = snapshot.context().reactor().dispatcher().schedule(
                dispatchKey(),
                delay,
                Activity.of(
                        Builder.SSE_ACTIVITY_RETRY,
                        () -> reconnect(retry, stream, holder, eventId, handle, operationId, attempt)));
        session.replaceReconnectHandle(next);
        handle.set(next);
        if (!session.opened() || stream.isCancelled()) {
            next.cancel();
            stream.complete(null);
        }
    }

    /**
     * Opens the replacement stream and enqueues the next reader task.
     *
     * @param retry       mutable retry policy reused by the replacement stream
     * @param stream      terminal future for the logical SSE stream
     * @param holder      reference containing the active session
     * @param eventId     reference containing the identifier sent as {@code Last-Event-ID}
     * @param handle      reference updated with the replacement reader task
     * @param operationId identifier correlating observation events for this session
     * @param attempt     zero-based reconnect attempt being executed
     */
    private void reconnect(
            final SseRetry retry,
            final CompletableFuture<Void> stream,
            final AtomicReference<SseSession> holder,
            final AtomicReference<String> eventId,
            final AtomicReference<DispatchHandle> handle,
            final String operationId,
            final int attempt) {
        try {
            Logger.info(
                    true,
                    "Fabric",
                    "SSE reconnect started: host={}, port={}, attempt={}",
                    snapshot.address().host(),
                    snapshot.address().port(),
                    attempt);
            final SseReader next = replaceReader(holder, eventId);
            if (next == null) {
                stream.complete(null);
                return;
            }
            handle.set(submitRead(next, retry, stream, holder, eventId, handle, operationId, attempt));
            holder.get().replaceReaderHandle(handle.get());
            Logger.info(
                    false,
                    "Fabric",
                    "SSE reconnect completed: host={}, port={}, attempt={}",
                    snapshot.address().host(),
                    snapshot.address().port(),
                    attempt);
        } catch (final RuntimeException e) {
            Logger.warn(
                    false,
                    "Fabric",
                    e,
                    "SSE reconnect failed: host={}, port={}, attempt={}, exception={}",
                    snapshot.address().host(),
                    snapshot.address().port(),
                    attempt,
                    e.getClass().getSimpleName());
            if (snapshot.autoReconnect()) {
                scheduleReconnect(retry, stream, holder, eventId, handle, operationId, attempt + 1);
            } else {
                stream.completeExceptionally(e);
            }
        }
    }

    /**
     * Enqueues a reader task to the shared dispatcher.
     *
     * @param reader      active reader to run on a dispatcher worker
     * @param retry       mutable retry policy updated by the reader
     * @param stream      terminal future for the logical SSE stream
     * @param holder      reference containing the active session and serving as dispatcher owner
     * @param eventId     reference containing the most recently dispatched event identifier
     * @param handle      reference containing the active read or reconnect task
     * @param operationId identifier correlating observation events for this session
     * @param attempt     zero-based consecutive reconnect-failure count
     * @return handle for cancelling the submitted background reader activity
     */
    private DispatchHandle submitRead(
            final SseReader reader,
            final SseRetry retry,
            final CompletableFuture<Void> stream,
            final AtomicReference<SseSession> holder,
            final AtomicReference<String> eventId,
            final AtomicReference<DispatchHandle> handle,
            final String operationId,
            final int attempt) {
        return snapshot.context().reactor().dispatcher().background(
                dispatchKey(),
                holder,
                Activity.of(
                        Builder.SSE_ACTIVITY_READ,
                        () -> read(reader, retry, stream, holder, eventId, handle, operationId, attempt)));
    }

    /**
     * Opens and installs a replacement reader.
     *
     * @param holder  reference containing the active session
     * @param eventId reference containing the identifier sent as {@code Last-Event-ID}
     * @return installed replacement reader, or {@code null} when the session closed before ownership transfer
     */
    private SseReader replaceReader(final AtomicReference<SseSession> holder, final AtomicReference<String> eventId) {
        final SseSession session = holder.get();
        if (session == null || !session.opened()) {
            return null;
        }
        final Cancellation cancellation = session.cancellation();
        cancellation.throwIfCancelled();
        final Call<HttpRunner.Stream> call = responseCall(eventId.get(), cancellation);
        session.replaceHttpCall(call);
        if (!session.opened() || cancellation.cancelled()) {
            call.cancel();
            return null;
        }
        SseReader next = null;
        HttpRunner.Stream response = null;
        try {
            response = call.execute();
            cancellation.throwIfCancelled();
            validateResponse(response);
            next = SseBody.source(response.source(), response.body().length()).reader();
            session.replaceReader(call, response, next);
            return next;
        } catch (final RuntimeException e) {
            call.cancel();
            closeReader(next);
            if (next == null) {
                closeResponse(response);
            }
            throw e;
        }
    }

    /**
     * Closes a reader before reconnecting.
     *
     * @param reader reader to close, or {@code null} when creation failed before a reader was assigned
     */
    private static void closeReader(final SseReader reader) {
        try {
            reader.close();
        } catch (final RuntimeException ignored) {
            // Reconnect should preserve the original stream outcome.
        }
    }

    /**
     * Checks the optional guard.
     *
     * @param message filtered SSE message to validate
     */
    private void checkGuard(final Message message) {
        if (snapshot.guard() == null) {
            return;
        }
        Logger.debug(
                true,
                "Fabric",
                "SSE guard check started: host={}, port={}, tag={}",
                snapshot.address().host(),
                snapshot.address().port(),
                message.tag());
        snapshot.guard().check(message).throwIfRejected();
        Logger.debug(
                false,
                "Fabric",
                "SSE guard check accepted: host={}, port={}, tag={}",
                snapshot.address().host(),
                snapshot.address().port(),
                message.tag());
    }

    /**
     * Applies configured stream filters.
     *
     * @param message SSE lifecycle message to pass through context and exchange filters
     * @return message produced by the complete filter chain
     */
    private Message filter(final Message message) {
        return FilterChain.apply(message, snapshot.context().filter(), snapshot.filter());
    }

    /**
     * Emits an observation event.
     *
     * @param marker      observation marker to publish
     * @param cause       failure attached to the event, or {@code null}
     * @param payload     payload used to derive a byte-count tag, or {@code null}
     * @param operationId operation identifier shared by this SSE session
     */
    private void emit(
            final ObservationMarker marker,
            final Throwable cause,
            final Payload payload,
            final String operationId) {
        final FabricEvent.Builder event = FabricEvent.builder(marker, snapshot.context().clock())
                .tag(Builder.TAG_OPERATION_ID, operationId).tag(Builder.TAG_PROTOCOL, snapshot.address().scheme())
                .tag(Builder.HOST, snapshot.address().host())
                .tag(Builder.TAG_PORT, Integer.toString(snapshot.address().port()));
        if (payload != null && payload.length() >= Normal.LONG_ZERO) {
            event.tag(Builder.TAG_BYTES, Long.toString(payload.length()));
        }
        if (cause != null) {
            event.cause(cause);
        }
        snapshot.observer().emit(event.build());
    }

    /**
     * Closes an unclaimed response.
     *
     * @param response unclaimed streaming response to close, or {@code null}
     */
    private static void closeResponse(final HttpRunner.Stream response) {
        if (response != null) {
            response.close();
        }
    }

    /**
     * Validates required values.
     *
     * @param value reference to validate
     * @param name  logical field name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
