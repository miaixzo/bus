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
package org.miaixz.bus.fabric.protocol.socket.body;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.codec.body.MessageBody;
import org.miaixz.bus.fabric.codec.body.ProgressBody;

/**
 * Socket message body backed by a protocol-neutral payload.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SocketBody implements MessageBody, ProgressBody {

    /**
     * Original socket message payload.
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
     * Creates a socket body.
     *
     * @param payload socket message content source
     * @param media   media type
     */
    private SocketBody(final Payload payload, final MediaType media) {
        this(payload, media, null);
    }

    /**
     * Creates a socket body.
     *
     * @param payload  socket message content source
     * @param media    media type
     * @param progress optional progress tracker
     */
    private SocketBody(final Payload payload, final MediaType media, final ProgressBody.Tracker progress) {
        this.payload = require(payload, "Socket payload");
        this.media = require(media, "Socket media");
        this.progress = progress;
    }

    /**
     * Creates a binary body.
     *
     * @param payload binary socket message content source
     * @return socket body using application/octet-stream metadata
     */
    public static SocketBody of(final Payload payload) {
        return of(payload, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * Creates a body with media metadata.
     *
     * @param payload socket message content source
     * @param media   media type
     * @return socket body
     */
    public static SocketBody of(final Payload payload, final MediaType media) {
        return new SocketBody(payload, media);
    }

    /**
     * Creates a binary body.
     *
     * @param bytes binary message bytes copied into a repeatable payload
     * @return socket body
     */
    public static SocketBody bytes(final byte[] bytes) {
        return of(Payload.of(bytes), MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }

    /**
     * Creates a UTF-8 text body.
     *
     * @param text text encoded as UTF-8
     * @return socket body
     */
    public static SocketBody text(final String text) {
        return text(text, StandardCharsets.UTF_8);
    }

    /**
     * Creates a text body.
     *
     * @param text    text encoded into the message payload
     * @param charset character encoding recorded in the media type
     * @return socket body
     */
    public static SocketBody text(final String text, final Charset charset) {
        final Charset current = require(charset, "Socket charset");
        return of(Payload.of(require(text, "Socket text"), current), textMedia(current));
    }

    /**
     * Returns a progress-aware copy of this socket body.
     *
     * @param listener callback receiving transferred and total byte counts
     * @return progress-aware socket body
     */
    public SocketBody progress(final BiConsumer<Long, Long> listener) {
        return new SocketBody(payload, media, ProgressBody.Tracker.of(payload, listener));
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
     * Returns the socket body media type.
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
    public SocketBody stepBytes(final long bytes) {
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
    public SocketBody stepRate(final double rate) {
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
