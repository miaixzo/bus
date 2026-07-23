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
package org.miaixz.bus.fabric.protocol.stomp.body;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.codec.body.MessageBody;
import org.miaixz.bus.fabric.codec.body.ProgressBody;

/**
 * STOMP message body with content-type metadata.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class StompBody implements MessageBody, ProgressBody {

    /**
     * Original STOMP body payload.
     */
    private final Payload payload;

    /**
     * Optional progress tracker.
     */
    private final ProgressBody.Tracker progress;

    /**
     * Media metadata.
     */
    private final MediaType media;

    /**
     * Creates a body.
     *
     * @param payload STOMP body content source
     * @param media   media type
     */
    private StompBody(final Payload payload, final MediaType media) {
        this(payload, media, null);
    }

    /**
     * Creates a body.
     *
     * @param payload  STOMP body content source
     * @param media    media type
     * @param progress optional progress tracker
     */
    private StompBody(final Payload payload, final MediaType media, final ProgressBody.Tracker progress) {
        this.payload = require(payload, "STOMP payload");
        this.media = require(media, "STOMP media");
        this.progress = progress;
    }

    /**
     * Returns an empty STOMP body.
     *
     * @return empty body
     */
    public static StompBody empty() {
        return Instances.get(
                StompBody.class.getName() + ".empty",
                () -> new StompBody(Payload.empty(), MediaType.APPLICATION_OCTET_STREAM_TYPE));
    }

    /**
     * Creates a binary body.
     *
     * @param payload binary STOMP body content source
     * @return STOMP body using application/octet-stream metadata
     */
    public static StompBody of(final Payload payload) {
        return of(payload, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * Creates a body.
     *
     * @param payload STOMP body content source
     * @param media   media type
     * @return STOMP body
     */
    public static StompBody of(final Payload payload, final MediaType media) {
        return new StompBody(payload, media);
    }

    /**
     * Creates a binary body.
     *
     * @param bytes binary body bytes copied into a repeatable payload
     * @return STOMP body
     */
    public static StompBody bytes(final byte[] bytes) {
        return of(Payload.of(bytes), MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * Creates a UTF-8 text body.
     *
     * @param text text encoded as UTF-8
     * @return STOMP body
     */
    public static StompBody text(final String text) {
        return text(text, StandardCharsets.UTF_8);
    }

    /**
     * Creates a text body.
     *
     * @param text    text encoded into the body payload
     * @param charset character encoding recorded in the media type
     * @return STOMP body
     */
    public static StompBody text(final String text, final Charset charset) {
        final Charset current = require(charset, "STOMP charset");
        return of(Payload.of(require(text, "STOMP text"), current), textMedia(current));
    }

    /**
     * Returns a progress-aware copy of this STOMP body.
     *
     * @param listener callback receiving transferred and total byte counts
     * @return progress-aware STOMP body
     */
    public StompBody progress(final BiConsumer<Long, Long> listener) {
        return new StompBody(payload, media, ProgressBody.Tracker.of(payload, listener));
    }

    /**
     * Returns the current payload, wrapped with progress tracking when enabled.
     *
     * @return current payload
     */
    @Override
    public Payload payload() {
        return progress == null ? payload : progress.payload();
    }

    /**
     * Returns the STOMP body media type.
     *
     * @return media type
     */
    @Override
    public MediaType media() {
        return media;
    }

    /**
     * Returns transferred byte count reported by the progress tracker.
     *
     * @return transferred bytes
     */
    @Override
    public long transferred() {
        return progress == null ? Normal.LONG_ZERO : progress.transferred();
    }

    /**
     * Returns the declared payload length.
     *
     * @return total bytes, or -1 when unknown
     */
    @Override
    public long total() {
        return payload.length();
    }

    /**
     * Advances progress notification by a byte step.
     *
     * @param bytes step bytes
     * @return this body
     */
    @Override
    public StompBody stepBytes(final long bytes) {
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
     * @param rate progress rate
     * @return this body
     */
    @Override
    public StompBody stepRate(final double rate) {
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
     * @param name  field name included in the validation failure
     * @param <T>   reference type
     * @return validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Creates text media with charset metadata.
     *
     * @param charset character encoding attached to text/plain metadata
     * @return media type
     */
    private static MediaType textMedia(final Charset charset) {
        return MediaType.TEXT_PLAIN_TYPE.withCharset(charset);
    }

}
