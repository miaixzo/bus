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

import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.codec.body.ProgressBody;
import org.miaixz.bus.fabric.codec.body.ResponseBody;
import org.miaixz.bus.fabric.protocol.sse.SseEvent;
import org.miaixz.bus.fabric.protocol.sse.event.SseReader;

/**
 * SSE response body representing a {@code text/event-stream} payload.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SseBody implements ResponseBody, ProgressBody {

    /**
     * Original SSE stream payload without progress wrapping.
     */
    private final Payload payload;

    /**
     * Progress tracker that supplies a wrapped payload, or {@code null} when tracking is disabled.
     */
    private final ProgressBody.Tracker progress;

    /**
     * Creates an SSE body.
     *
     * @param payload non-null SSE stream payload
     */
    private SseBody(final Payload payload) {
        this(payload, null);
    }

    /**
     * Creates an SSE body.
     *
     * @param payload  non-null original SSE stream payload
     * @param progress tracker associated with the payload, or {@code null}
     */
    private SseBody(final Payload payload, final ProgressBody.Tracker progress) {
        this.payload = require(payload, "SSE payload");
        this.progress = progress;
    }

    /**
     * Creates an SSE body from a payload.
     *
     * @param payload non-null SSE stream payload
     * @return SSE body backed by the supplied payload
     * @throws ValidateException if {@code payload} is {@code null}
     */
    public static SseBody of(final Payload payload) {
        return new SseBody(payload);
    }

    /**
     * Creates an SSE body from a response body.
     *
     * @param body response body whose payload is reinterpreted as an SSE stream
     * @return SSE body backed by the response body's payload
     * @throws ValidateException if {@code body} is {@code null}
     */
    public static SseBody of(final ResponseBody body) {
        return of(require(body, "SSE response body").payload());
    }

    /**
     * Creates an SSE body from a source.
     *
     * @param source one-shot source containing encoded SSE stream bytes
     * @param length declared byte length, or {@code -1} when unknown
     * @return SSE body backed by a one-shot source payload
     * @throws ValidateException if the source is {@code null} or the length is less than {@code -1}
     */
    public static SseBody source(final Source source, final long length) {
        return of(Payload.source(source, length));
    }

    /**
     * Creates an SSE body from encoded stream text.
     *
     * @param text complete SSE stream text to encode as UTF-8
     * @return repeatable SSE body containing the encoded text
     * @throws ValidateException if {@code text} is {@code null}
     */
    public static SseBody text(final String text) {
        return of(Payload.of(require(text, "SSE text"), StandardCharsets.UTF_8));
    }

    /**
     * Encodes one event as SSE stream body text.
     *
     * @param event event to serialize
     * @return repeatable UTF-8 SSE body containing one encoded event
     * @throws ValidateException if {@code event} is {@code null}
     */
    public static SseBody event(final SseEvent event) {
        return text(encode(require(event, "SSE event")));
    }

    /**
     * Encodes an event.
     *
     * @param event event whose defined fields are serialized
     * @return SSE field lines followed by the blank line that terminates the event
     * @throws ValidateException if {@code event} is {@code null}
     */
    public static String encode(final SseEvent event) {
        require(event, "SSE event");
        final StringBuilder builder = new StringBuilder();
        if (event.id() != null) {
            builder.append(Builder.SSE_BODY_ID_PREFIX).append(event.id()).append(Symbol.LF);
        }
        if (!Builder.SSE_DEFAULT_EVENT.equals(event.event())) {
            builder.append(Builder.SSE_BODY_EVENT_PREFIX).append(event.event()).append(Symbol.LF);
        }
        if (event.retry() != null) {
            builder.append(Builder.SSE_BODY_RETRY_PREFIX).append(event.retry().toMillis()).append(Symbol.LF);
        }
        appendData(builder, event.data() == null ? Normal.EMPTY : event.data());
        builder.append(Symbol.LF);
        return builder.toString();
    }

    /**
     * Opens this SSE body as an event stream reader.
     *
     * @return reader consuming a newly opened source from the current payload view
     */
    public SseReader reader() {
        return new SseReader(source());
    }

    /**
     * Returns a progress-aware copy of this SSE body.
     *
     * @param listener callback receiving cumulative transferred and total byte counts
     * @return new SSE body that exposes a progress-observing payload
     * @throws ValidateException if {@code listener} is {@code null}
     */
    public SseBody progress(final BiConsumer<Long, Long> listener) {
        return new SseBody(payload, ProgressBody.Tracker.of(payload, listener));
    }

    /**
     * Returns the current payload, wrapped with progress tracking when enabled.
     *
     * @return original payload when tracking is disabled, otherwise the tracker-wrapped payload
     */
    @Override
    public Payload payload() {
        return progress == null ? payload : progress.payload();
    }

    /**
     * Returns the SSE event-stream media type.
     *
     * @return canonical {@code text/event-stream} media type
     */
    @Override
    public MediaType media() {
        return MediaType.SERVER_SENT_EVENTS_TYPE;
    }

    /**
     * Returns transferred byte count reported by the progress tracker.
     *
     * @return cumulative observed byte count, or {@code 0} when tracking is disabled
     */
    @Override
    public long transferred() {
        return progress == null ? Normal.LONG_ZERO : progress.transferred();
    }

    /**
     * Returns the declared payload length.
     *
     * @return original payload length, or {@code -1} when unknown
     */
    @Override
    public long total() {
        return payload.length();
    }

    /**
     * Advances progress notification by a byte step.
     *
     * @param bytes positive number of bytes between progress callbacks
     * @return this body
     * @throws ValidateException if {@code bytes} is not positive
     */
    @Override
    public SseBody stepBytes(final long bytes) {
        if (progress == null) {
            ProgressBody.super.stepBytes(bytes);
        } else {
            progress.stepBytes(bytes);
        }
        return this;
    }

    /**
     * Advances progress notification by a total-size rate.
     *
     * @param rate finite fraction greater than {@code 0} and at most {@code 1} of the known total length
     * @return this body
     * @throws ValidateException if the rate is outside the valid range or the payload length is unknown
     */
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
     * @param value reference to validate
     * @param name  logical field name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Appends event data as SSE data lines without regex allocation.
     *
     * @param builder destination receiving one {@code data:} field per logical line
     * @param data    event data split on line-feed characters
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
     * @param builder destination receiving the serialized data field
     * @param data    complete event data string
     * @param start   inclusive start index of the logical line
     * @param end     exclusive end index of the logical line
     */
    private static void appendDataLine(final StringBuilder builder, final String data, final int start, final int end) {
        builder.append(Builder.SSE_BODY_DATA_PREFIX).append(data, start, end).append(Symbol.LF);
    }

}
