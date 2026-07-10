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
     * Default binary media type.
     */
    private static final MediaType BINARY = MediaType.APPLICATION_OCTET_STREAM_TYPE;

    /**
     * Default UTF-8 text media type.
     */
    /**
     * Payload.
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
     * @param payload payload
     * @param media   media type
     */
    private SocketBody(final Payload payload, final MediaType media) {
        this(payload, media, null);
    }

    /**
     * Creates a socket body.
     *
     * @param payload  payload
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
     * @param payload payload
     * @return socket body
     */
    public static SocketBody of(final Payload payload) {
        return of(payload, BINARY);
    }

    /**
     * Creates a body with media metadata.
     *
     * @param payload payload
     * @param media   media type
     * @return socket body
     */
    public static SocketBody of(final Payload payload, final MediaType media) {
        return new SocketBody(payload, media);
    }

    /**
     * Creates a binary body.
     *
     * @param bytes bytes
     * @return socket body
     */
    public static SocketBody bytes(final byte[] bytes) {
        return of(Payload.of(bytes), BINARY);
    }

    /**
     * Creates a UTF-8 text body.
     *
     * @param text text
     * @return socket body
     */
    public static SocketBody text(final String text) {
        return text(text, StandardCharsets.UTF_8);
    }

    /**
     * Creates a text body.
     *
     * @param text    text
     * @param charset charset
     * @return socket body
     */
    public static SocketBody text(final String text, final Charset charset) {
        final Charset current = require(charset, "Socket charset");
        return of(Payload.of(require(text, "Socket text"), current), textMedia(current));
    }

    /**
     * Returns a progress-aware copy of this socket body.
     *
     * @param listener listener
     * @return progress-aware socket body
     */
    public SocketBody progress(final BiConsumer<Long, Long> listener) {
        return new SocketBody(payload, media, ProgressBody.Tracker.of(payload, listener));
    }

    @Override
    public Payload payload() {
        return progress == null ? payload : progress.payload();
    }

    @Override
    public MediaType media() {
        return media;
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
    public SocketBody stepBytes(final long bytes) {
        if (progress == null) {
            ProgressBody.super.stepBytes(bytes);
        } else {
            progress.stepBytes(bytes);
        }
        return this;
    }

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

    /**
     * Creates text media with charset metadata.
     *
     * @param charset charset
     * @return media type
     */
    private static MediaType textMedia(final Charset charset) {
        return MediaType.parse("text/plain; charset=" + charset.name());
    }

}
