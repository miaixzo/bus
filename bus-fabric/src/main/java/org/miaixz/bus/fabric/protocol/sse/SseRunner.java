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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.UnoUrl;
import org.miaixz.bus.fabric.observe.ObservationMarker;
import org.miaixz.bus.fabric.observe.event.FabricEvent;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpRunner;
import org.miaixz.bus.fabric.protocol.sse.body.SseBody;
import org.miaixz.bus.fabric.protocol.sse.event.SseReader;
import org.miaixz.bus.fabric.protocol.sse.event.SseRetry;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.FilterChain;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;
import org.miaixz.bus.logger.Logger;

/**
 * Opens and reads SSE streams from an immutable snapshot snapshot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class SseRunner {

    /**
     * Execution snapshot.
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
     * @param cancellation cancellation scope
     * @return opened session
     */
    SseSession open(final Cancellation cancellation) {
        final Cancellation currentCancellation = require(cancellation, "Cancellation");
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
            response = response(currentCancellation);
            currentCancellation.throwIfCancelled();
            validateResponse(response);
            reader = SseBody.source(response.source(), response.body().length()).reader();
            final SseRetry sessionRetry = SseRetry.defaults();
            sessionRetry.update(snapshot.retry().current());
            final CompletableFuture<Void> stream = new CompletableFuture<>();
            final AtomicReference<SseSession> holder = new AtomicReference<>();
            final AtomicReference<DispatchHandle> handle = new AtomicReference<>();
            final AtomicReference<String> eventId = new AtomicReference<>(snapshot.lastEventId());
            final SseSession session = new SseSession(snapshot.address(), sessionRetry, reader, stream, () -> {
                final DispatchHandle current = handle.get();
                if (current != null) {
                    current.cancel();
                }
            }, snapshot.listener());
            holder.set(session);
            handle.set(submitRead(reader, sessionRetry, stream, holder, eventId, handle, Normal._0));
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
            closeReader(reader);
            if (reader == null) {
                closeResponse(response);
            }
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
            closeReader(reader);
            if (reader == null) {
                closeResponse(response);
            }
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
     * @return dispatch key
     */
    String dispatchKey() {
        return Builder.SSE_RUNNER_DISPATCH_PREFIX + snapshot.address().host() + Symbol.C_COLON
                + snapshot.address().port();
    }

    /**
     * Opens the HTTP response through the fabric HTTP chain.
     *
     * @param cancellation cancellation scope
     * @return response
     */
    private HttpRunner.Stream response(final Cancellation cancellation) {
        return response(snapshot.lastEventId(), cancellation);
    }

    /**
     * Opens the HTTP response through the fabric HTTP chain.
     *
     * @param eventId      current event id
     * @param cancellation cancellation scope
     * @return response
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
        final Headers.Builder builder = Headers.builder().add(HTTP.ACCEPT, MediaType.SERVER_SENT_EVENTS)
                .add(HTTP.CACHE_CONTROL, HTTP.CACHE_DIRECTIVE_NO_CACHE);
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
        final HttpRequest request = HttpRequest.builder().method(HTTP.Method.GET)
                .url(UnoUrl.parse(snapshot.uri().toString())).headers(opening.headers()).timeout(snapshot.timeout())
                .build();
        final HttpRunner.Stream response = HttpRunner
                .stream(snapshot.context().withFilter(null), request, cancellation);
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
     * @param response response
     */
    private static void validateResponse(final HttpRunner.Stream response) {
        final int status = response.status();
        if (status < HTTP.HTTP_OK || status >= HTTP.HTTP_MULT_CHOICE) {
            throw new ProtocolException("SSE response status must be 2xx");
        }
        if (!StringKit.containsIgnoreCase(response.headers().get(HTTP.CONTENT_TYPE), MediaType.SERVER_SENT_EVENTS)) {
            throw new ProtocolException("SSE response must be text/event-stream");
        }
    }

    /**
     * Reads and dispatches events in the background.
     *
     * @param reader  reader
     * @param retry   retry policy
     * @param stream  stream future
     * @param holder  session holder
     * @param eventId current event id
     * @param handle  current dispatch handle
     * @param attempt reconnect attempt
     */
    private void read(
            final SseReader reader,
            final SseRetry retry,
            final CompletableFuture<Void> stream,
            final AtomicReference<SseSession> holder,
            final AtomicReference<String> eventId,
            final AtomicReference<DispatchHandle> handle,
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
                 * @param id    event id
                 * @param event event type
                 * @param data  event data
                 */
                @Override
                public void event(final String id, final String event, final String data) {
                    final SseEvent current = SseEvent.of(id, event, data, null);
                    dispatch(current, eventId);
                }

                /**
                 * Updates the reconnect retry delay announced by the stream.
                 *
                 * @param retryDelay retry delay
                 */
                @Override
                public void retry(final java.time.Duration retryDelay) {
                    retry.update(retryDelay);
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
                scheduleReconnect(retry, stream, holder, eventId, handle, Normal._0);
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
                    scheduleReconnect(retry, stream, holder, eventId, handle, attempt + 1);
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
     * @param event   event
     * @param eventId current event id
     */
    private void dispatch(final SseEvent event, final AtomicReference<String> eventId) {
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
        emit(ObservationMarker.SSE_EVENT, null, filteredPayload);
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
     * @param retry   retry policy
     * @param stream  stream future
     * @param holder  session holder
     * @param eventId current event id
     * @param handle  current dispatch handle
     * @param attempt attempt index
     */
    private void scheduleReconnect(
            final SseRetry retry,
            final CompletableFuture<Void> stream,
            final AtomicReference<SseSession> holder,
            final AtomicReference<String> eventId,
            final AtomicReference<DispatchHandle> handle,
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
                        () -> reconnect(retry, stream, holder, eventId, handle, attempt)));
        handle.set(next);
        if (!session.opened() || stream.isCancelled()) {
            next.cancel();
            stream.complete(null);
        }
    }

    /**
     * Opens the replacement stream and enqueues the next reader task.
     *
     * @param retry   retry policy
     * @param stream  stream future
     * @param holder  session holder
     * @param eventId current event id
     * @param handle  current dispatch handle
     * @param attempt attempt index
     */
    private void reconnect(
            final SseRetry retry,
            final CompletableFuture<Void> stream,
            final AtomicReference<SseSession> holder,
            final AtomicReference<String> eventId,
            final AtomicReference<DispatchHandle> handle,
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
            handle.set(submitRead(next, retry, stream, holder, eventId, handle, attempt));
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
                scheduleReconnect(retry, stream, holder, eventId, handle, attempt + 1);
            } else {
                stream.completeExceptionally(e);
            }
        }
    }

    /**
     * Enqueues a reader task to the shared dispatcher.
     *
     * @param reader  reader
     * @param retry   retry policy
     * @param stream  stream future
     * @param holder  session holder
     * @param eventId current event id
     * @param handle  current dispatch handle
     * @param attempt reconnect attempt
     * @return dispatch handle
     */
    private DispatchHandle submitRead(
            final SseReader reader,
            final SseRetry retry,
            final CompletableFuture<Void> stream,
            final AtomicReference<SseSession> holder,
            final AtomicReference<String> eventId,
            final AtomicReference<DispatchHandle> handle,
            final int attempt) {
        return snapshot.context().reactor().dispatcher().enqueue(
                dispatchKey(),
                Activity.of(
                        Builder.SSE_ACTIVITY_READ,
                        () -> read(reader, retry, stream, holder, eventId, handle, attempt)));
    }

    /**
     * Opens and installs a replacement reader.
     *
     * @param holder  session holder
     * @param eventId current event id
     * @return reader or null when session is closed
     */
    private SseReader replaceReader(final AtomicReference<SseSession> holder, final AtomicReference<String> eventId) {
        final SseSession session = holder.get();
        if (session == null || !session.opened()) {
            return null;
        }
        SseReader next = null;
        HttpRunner.Stream response = null;
        try {
            response = response(eventId.get(), Cancellation.create());
            validateResponse(response);
            next = SseBody.source(response.source(), response.body().length()).reader();
            session.replaceReader(next);
            return next;
        } catch (final RuntimeException e) {
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
     * @param reader reader
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
     * @param message message
     * @return filtered message
     */
    private Message filter(final Message message) {
        return FilterChain.apply(message, snapshot.context().filter(), snapshot.filter());
    }

    /**
     * Emits an observation event.
     *
     * @param marker marker
     * @param cause  failure cause
     */
    private void emit(final ObservationMarker marker, final Throwable cause) {
        emit(marker, cause, null);
    }

    /**
     * Emits an observation event.
     *
     * @param marker  marker
     * @param cause   failure cause
     * @param payload event payload
     */
    private void emit(final ObservationMarker marker, final Throwable cause, final Payload payload) {
        final FabricEvent.Builder event = FabricEvent.builder(marker)
                .tag(Builder.TAG_PROTOCOL, snapshot.address().scheme()).tag(Builder.HOST, snapshot.address().host())
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
     * @param response response
     */
    private static void closeResponse(final HttpRunner.Stream response) {
        if (response != null) {
            response.close();
        }
    }

    /**
     * Validates required values.
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
