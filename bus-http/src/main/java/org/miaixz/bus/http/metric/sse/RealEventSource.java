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
package org.miaixz.bus.http.metric.sse;

import java.io.IOException;

import org.miaixz.bus.http.*;
import org.miaixz.bus.http.bodys.ResponseBody;
import org.miaixz.bus.http.plugin.sse.EventSource;
import org.miaixz.bus.http.plugin.sse.EventSourceListener;
import org.miaixz.bus.logger.Logger;

/**
 * The core implementation for Server-Sent Events (SSE), responsible for establishing a connection with the server and
 * processing the event stream. It implements the {@link EventSource} interface to provide request and cancellation
 * functionality, the {@link ServerSentEventReader.Callback} interface to handle event data, and the {@link Callback}
 * interface to process HTTP responses.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RealEventSource implements EventSource, ServerSentEventReader.Callback, Callback {

    /**
     * The original HTTP request that initiated the event source.
     */
    private final Request request;

    /**
     * The listener for event source lifecycle and events.
     */
    private final EventSourceListener listener;

    /**
     * The HTTP call object used to execute the request, may be null.
     */
    private NewCall call;

    /**
     * A flag indicating whether the event source has been canceled.
     */
    private boolean canceled;

    /**
     * Constructs a new {@code RealEventSource} instance.
     *
     * @param request  The HTTP request that initiates the event source.
     * @param listener The event source listener to receive events and status updates.
     */
    public RealEventSource(Request request, EventSourceListener listener) {
        this.request = request;
        this.listener = listener;
        this.call = null;
        this.canceled = false;
    }

    /**
     * Establishes the event source connection using the specified call factory. Creates a new HTTP call and executes it
     * asynchronously, triggering the callback methods.
     *
     * @param callFactory The factory used to create HTTP calls.
     */
    public void connect(NewCall.Factory callFactory) {
        Logger.info(
                true,
                "Http",
                "SSE connection starting: protocol=sse, method={}, url={}",
                request.method(),
                request.url().redact());
        call = callFactory.newCall(request);
        call.enqueue(this);
    }

    /**
     * Handles the HTTP response, delegating to {@link #processResponse(Response)} for detailed processing.
     *
     * @param call     The call that initiated the request.
     * @param response The HTTP response from the server.
     */
    @Override
    public void onResponse(NewCall call, Response response) {
        Logger.debug(
                false,
                "Http",
                "SSE HTTP response received: protocol=sse, url={}, status={}",
                request.url().redact(),
                response.code());
        processResponse(response);
    }

    /**
     * Processes the server-sent event response, parses the event stream, and triggers listener callbacks. Validates the
     * response status and content type, handles event stream data, and closes the connection upon completion.
     *
     * @param response The HTTP response from the server.
     */
    public void processResponse(Response response) {
        try (Response ignored = response) {
            if (!response.isSuccessful()) {
                Logger.warn(
                        false,
                        "Http",
                        "SSE connection rejected: protocol=sse, url={}, status={}",
                        request.url().redact(),
                        response.code());
                listener.onFailure(this, null, response);
                return;
            }

            ResponseBody body = response.body();
            if (body == null) {
                Logger.warn(
                        false,
                        "Http",
                        "SSE response body missing: protocol=sse, url={}, status={}",
                        request.url().redact(),
                        response.code());
                listener.onFailure(this, new IllegalStateException("Response body is null"), response);
                return;
            }

            if (!isEventStream(body)) {
                Logger.warn(
                        false,
                        "Http",
                        "SSE invalid content type: protocol=sse, url={}, contentType={}",
                        request.url().redact(),
                        body.contentType());
                listener.onFailure(
                        this,
                        new IllegalStateException("Invalid content-type: " + body.contentType()),
                        response);
                return;
            }

            // This is a long-lived response, so cancel the timeout for the entire call.
            if (call instanceof RealCall) {
                ((RealCall) call).timeoutEarlyExit();
            }

            // Replace the original response body with an empty one to prevent callbacks from accessing actual data.
            Response modifiedResponse = response.newBuilder().body(Builder.EMPTY_RESPONSE).build();

            ServerSentEventReader reader = new ServerSentEventReader(body.source(), this);
            try {
                listener.onOpen(this, modifiedResponse);
                Logger.info(
                        false,
                        "Http",
                        "SSE stream opened: protocol=sse, url={}, status={}",
                        request.url().redact(),
                        response.code());
                while (reader.processNextEvent()) {
                    // Continue processing events.
                }
            } catch (Exception e) {
                Logger.error(
                        false,
                        "Http",
                        e,
                        "SSE stream failed while reading: protocol=sse, url={}, exception={}",
                        request.url().redact(),
                        e.getClass().getSimpleName());
                listener.onFailure(this, e, modifiedResponse);
                return;
            }
            Logger.info(false, "Http", "SSE stream closed: protocol=sse, url={}", request.url().redact());
            listener.onClosed(this);
        }
    }

    /**
     * Checks if the response body's content type is {@code text/event-stream}.
     *
     * @param body The response body.
     * @return {@code true} if the content type is {@code text/event-stream}, {@code false} otherwise.
     */
    private boolean isEventStream(ResponseBody body) {
        if (body.contentType() == null) {
            return false;
        }
        return "text".equals(body.contentType().type()) && "event-stream".equals(body.contentType().subtype());
    }

    /**
     * Handles HTTP request failures, notifying the listener.
     *
     * @param call The call that initiated the request.
     * @param e    The I/O exception that occurred.
     */
    @Override
    public void onFailure(NewCall call, IOException e) {
        Logger.error(
                false,
                "Http",
                e,
                "SSE connection failed: protocol=sse, url={}, exception={}",
                request.url().redact(),
                e.getClass().getSimpleName());
        listener.onFailure(this, e, null);
    }

    /**
     * Returns the original request that initiated the event source.
     *
     * @return The original HTTP request.
     */
    @Override
    public Request request() {
        return request;
    }

    /**
     * Cancels the event source connection. This operation is performed only if the event source has not already been
     * canceled and a call object exists.
     */
    @Override
    public void cancel() {
        if (call != null && !canceled) {
            canceled = true;
            Logger.info(false, "Http", "SSE cancellation requested: protocol=sse, url={}", request.url().redact());
            call.cancel();
        }
    }

    /**
     * Processes received event data, notifying the listener.
     *
     * @param id   The event ID, which may be null.
     * @param type The event type, which may be null.
     * @param data The event data.
     */
    @Override
    public void onEvent(String id, String type, String data) {
        Logger.trace(
                false,
                "Http",
                "SSE event received: protocol=sse, url={}, id={}, type={}, chars={}",
                request.url().redact(),
                id,
                type,
                data == null ? 0 : data.length());
        listener.onEvent(this, id, type, data);
    }

    /**
     * Handles changes in retry time (from the 'retry' field). The current implementation ignores automatic retries.
     *
     * @param timeMs The retry time in milliseconds.
     */
    @Override
    public void onRetryChange(long timeMs) {
        // Ignored, no automatic retries are performed.
    }

}
