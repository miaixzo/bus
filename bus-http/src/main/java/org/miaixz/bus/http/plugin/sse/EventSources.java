/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.plugin.sse;

import org.miaixz.bus.http.*;
import org.miaixz.bus.http.bodys.ResponseBody;
import org.miaixz.bus.http.metric.sse.RealEventSource;
import org.miaixz.bus.http.metric.sse.ServerSentEventReader;

/**
 * A utility class for Server-Sent Events (SSE), providing static methods for creating event source factories and
 * processing event responses. This simplifies the creation of SSE connections and the parsing of event streams.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class EventSources {

    /**
     * Creates an event source factory using the provided call factory.
     *
     * @param callFactory The factory used to create HTTP calls.
     * @return An event source factory for creating {@link EventSource} instances.
     */
    public static EventSource.Factory createFactory(NewCall.Factory callFactory) {
        return new FactoryImpl(callFactory);
    }

    /**
     * Processes a server-sent event response, notifying the listener to handle events or failures. Validates the
     * response status and content type, parses the event stream, and triggers the appropriate callbacks on the
     * listener.
     *
     * @param response The HTTP response from the server.
     * @param listener The event listener to receive events and status updates.
     */
    public static void processResponse(Response response, EventSourceListener listener) {
        try (Response ignored = response) {
            if (!response.isSuccessful()) {
                listener.onFailure(null, null, response);
                return;
            }

            ResponseBody body = response.body();
            if (body == null) {
                listener.onFailure(null, new IllegalStateException("Response body is null"), response);
                return;
            }

            if (!isEventStream(body)) {
                listener.onFailure(
                        null,
                        new IllegalStateException("Invalid content-type: " + body.contentType()),
                        response);
                return;
            }

            // Replace the original response body with an empty one to prevent callbacks from accessing actual data.
            Response modifiedResponse = response.newBuilder().body(Builder.EMPTY_RESPONSE).build();

            ServerSentEventReader reader = new ServerSentEventReader(body.source(),
                    new ServerSentEventReader.Callback() {

                        @Override
                        public void onEvent(String id, String type, String data) {
                            listener.onEvent(null, id, type, data);
                        }

                        @Override
                        public void onRetryChange(long timeMs) {
                            // Ignored, no automatic retries are performed.
                        }
                    });

            try {
                listener.onOpen(null, modifiedResponse);
                while (reader.processNextEvent()) {
                    // Continue processing events.
                }
            } catch (Exception e) {
                listener.onFailure(null, e, modifiedResponse);
                return;
            }
            listener.onClosed(null);
        }
    }

    /**
     * Checks if the response body's content type is {@code text/event-stream}.
     *
     * @param body The response body.
     * @return {@code true} if the content type is {@code text/event-stream}, {@code false} otherwise.
     */
    private static boolean isEventStream(ResponseBody body) {
        if (body.contentType() == null) {
            return false;
        }
        return "text".equals(body.contentType().type()) && "event-stream".equals(body.contentType().subtype());
    }

    /**
     * An implementation of the event source factory, responsible for creating {@link RealEventSource} instances and
     * initiating connections.
     */
    private static class FactoryImpl implements EventSource.Factory {

        /**
         * The factory used to create HTTP calls.
         */
        private final NewCall.Factory callFactory;

        /**
         * Constructs a new factory instance.
         *
         * @param callFactory The factory for creating HTTP calls.
         */
        FactoryImpl(NewCall.Factory callFactory) {
            this.callFactory = callFactory;
        }

        /**
         * Creates a new {@link EventSource} instance and starts an asynchronous connection.
         *
         * @param request  The HTTP request to initiate the event source.
         * @param listener The event listener to receive events and status updates.
         * @return A new {@link EventSource} instance.
         */
        @Override
        public EventSource newEventSource(Request request, EventSourceListener listener) {
            RealEventSource eventSource = new RealEventSource(request, listener);
            eventSource.connect(callFactory);
            return eventSource;
        }
    }

}
