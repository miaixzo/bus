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
package org.miaixz.bus.fabric.protocol.sse.body;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.codec.body.ProgressBody;
import org.miaixz.bus.fabric.codec.body.ResponseBody;
import org.miaixz.bus.fabric.protocol.sse.SseEvent;

/**
 * SSE response body representing a text/event-stream payload.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SseBody implements ResponseBody, ProgressBody {

    /**
     * SSE media type.
     */
    private static final MediaType EVENT_STREAM = MediaType.SERVER_SENT_EVENTS_TYPE;

    /**
     * Payload.
     */
    private final Payload payload;

    /**
     * Optional progress tracker.
     */
    private final ProgressBody.Tracker progress;

    /**
     * Creates an SSE body.
     *
     * @param payload payload
     */
    private SseBody(final Payload payload) {
        this(payload, null);
    }

    /**
     * Creates an SSE body.
     *
     * @param payload  payload
     * @param progress optional progress tracker
     */
    private SseBody(final Payload payload, final ProgressBody.Tracker progress) {
        this.payload = require(payload, "SSE payload");
        this.progress = progress;
    }

    /**
     * Creates an SSE body from a payload.
     *
     * @param payload payload
     * @return SSE body
     */
    public static SseBody of(final Payload payload) {
        return new SseBody(payload);
    }

    /**
     * Creates an SSE body from a stream.
     *
     * @param stream stream
     * @return SSE body
     */
    public static SseBody stream(final InputStream stream) {
        return stream(stream, -1);
    }

    /**
     * Creates an SSE body from a stream.
     *
     * @param stream stream
     * @param length declared length, or -1
     * @return SSE body
     */
    public static SseBody stream(final InputStream stream, final long length) {
        return of(Payload.stream(stream, length));
    }

    /**
     * Creates an SSE body from encoded stream text.
     *
     * @param text stream text
     * @return SSE body
     */
    public static SseBody text(final String text) {
        return of(Payload.of(require(text, "SSE text"), StandardCharsets.UTF_8));
    }

    /**
     * Encodes one event as SSE stream body text.
     *
     * @param event event
     * @return SSE body
     */
    public static SseBody event(final SseEvent event) {
        return text(encode(require(event, "SSE event")));
    }

    /**
     * Encodes an event.
     *
     * @param event event
     * @return encoded event text
     */
    public static String encode(final SseEvent event) {
        require(event, "SSE event");
        final StringBuilder builder = new StringBuilder();
        if (event.id() != null) {
            builder.append("id: ").append(event.id()).append(Symbol.LF);
        }
        if (!"message".equals(event.event())) {
            builder.append("event: ").append(event.event()).append(Symbol.LF);
        }
        if (event.retry() != null) {
            builder.append("retry: ").append(event.retry().toMillis()).append(Symbol.LF);
        }
        final String data = event.data() == null ? Normal.EMPTY : event.data();
        final String[] lines = data.split("\\n", -1);
        for (final String line : lines) {
            builder.append("data: ").append(line).append(Symbol.LF);
        }
        builder.append(Symbol.LF);
        return builder.toString();
    }

    /**
     * Returns a progress-aware copy of this SSE body.
     *
     * @param listener listener
     * @return progress-aware SSE body
     */
    public SseBody progress(final BiConsumer<Long, Long> listener) {
        return new SseBody(payload, ProgressBody.Tracker.of(payload, listener));
    }

    @Override
    public Payload payload() {
        return progress == null ? payload : progress.payload();
    }

    @Override
    public MediaType media() {
        return EVENT_STREAM;
    }

    @Override
    public long transferred() {
        return progress == null ? 0L : progress.transferred();
    }

    @Override
    public long total() {
        return payload.length();
    }

    @Override
    public SseBody stepBytes(final long bytes) {
        if (progress == null) {
            ProgressBody.super.stepBytes(bytes);
        } else {
            progress.stepBytes(bytes);
        }
        return this;
    }

    @Override
    public SseBody stepRate(final double rate) {
        if (progress == null) {
            ProgressBody.super.stepRate(rate);
        } else {
            progress.stepRate(rate);
        }
        return this;
    }

    /**
     * Validates a required value.
     *
     * @param value value
     * @param name  field name
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
