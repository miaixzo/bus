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

import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
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
     * Default SSE event type.
     */
    private static final String DEFAULT_EVENT = "message";

    /**
     * Event id line prefix.
     */
    private static final String ID_PREFIX = "id: ";

    /**
     * Event type line prefix.
     */
    private static final String EVENT_PREFIX = "event: ";

    /**
     * Retry line prefix.
     */
    private static final String RETRY_PREFIX = "retry: ";

    /**
     * Data line prefix.
     */
    private static final String DATA_PREFIX = "data: ";

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
     * Creates an SSE body from a source.
     *
     * @param source source
     * @param length declared length, or -1
     * @return SSE body
     */
    public static SseBody source(final Source source, final long length) {
        return of(Payload.source(source, length));
    }

    /**
     * Creates an SSE body from a stream through the JDK stream compatibility boundary.
     *
     * @param stream stream
     * @return SSE body
     * @deprecated use {@link #source(Source, long)}
     */
    @Deprecated(since = "8.8.3")
    public static SseBody stream(final InputStream stream) {
        return stream(stream, Normal.__1);
    }

    /**
     * Creates an SSE body from a stream through the JDK stream compatibility boundary.
     *
     * @param stream stream
     * @param length declared length, or -1
     * @return SSE body
     * @deprecated use {@link #source(Source, long)}
     */
    @Deprecated(since = "8.8.3")
    public static SseBody stream(final InputStream stream, final long length) {
        return source(IoKit.source(stream), length);
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
            builder.append(ID_PREFIX).append(event.id()).append(Symbol.LF);
        }
        if (!DEFAULT_EVENT.equals(event.event())) {
            builder.append(EVENT_PREFIX).append(event.event()).append(Symbol.LF);
        }
        if (event.retry() != null) {
            builder.append(RETRY_PREFIX).append(event.retry().toMillis()).append(Symbol.LF);
        }
        final String data = event.data() == null ? Normal.EMPTY : event.data();
        appendData(builder, data);
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
        return progress == null ? Normal.LONG_ZERO : progress.transferred();
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
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Appends event data as SSE data lines without regex allocation.
     *
     * @param builder builder
     * @param data    event data
     */
    private static void appendData(final StringBuilder builder, final String data) {
        int start = Normal._0;
        while (true) {
            final int end = data.indexOf(Symbol.C_LF, start);
            if (end < Normal._0) {
                appendDataLine(builder, data, start, data.length());
                return;
            }
            appendDataLine(builder, data, start, end);
            start = end + Normal._1;
        }
    }

    /**
     * Appends one SSE data line.
     *
     * @param builder builder
     * @param data    event data
     * @param start   line start index
     * @param end     line end index
     */
    private static void appendDataLine(final StringBuilder builder, final String data, final int start, final int end) {
        builder.append(DATA_PREFIX).append(data, start, end).append(Symbol.LF);
    }

}
