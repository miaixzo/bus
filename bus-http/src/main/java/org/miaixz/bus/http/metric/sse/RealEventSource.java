/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.metric.sse;

import org.miaixz.bus.http.*;
import org.miaixz.bus.http.bodys.ResponseBody;
import org.miaixz.bus.http.plugin.sse.EventSource;
import org.miaixz.bus.http.plugin.sse.EventSourceListener;

import java.io.IOException;

/**
 * The core implementation for Server-Sent Events (SSE), responsible for establishing a connection with the server and
 * processing the event stream. It implements the {@link EventSource} interface to provide request and cancellation
 * functionality, the {@link ServerSentEventReader.Callback} interface to handle event data, and the {@link Callback}
 * interface to process HTTP responses.
 *
 * @author Kimi Liu
 * @since Java 17+
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
                listener.onFailure(this, null, response);
                return;
            }

            ResponseBody body = response.body();
            if (body == null) {
                listener.onFailure(this, new IllegalStateException("Response body is null"), response);
                return;
            }

            if (!isEventStream(body)) {
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
                while (reader.processNextEvent()) {
                    // Continue processing events.
                }
            } catch (Exception e) {
                listener.onFailure(this, e, modifiedResponse);
                return;
            }
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
