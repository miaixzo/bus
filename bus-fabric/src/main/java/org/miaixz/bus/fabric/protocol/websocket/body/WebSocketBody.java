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
package org.miaixz.bus.fabric.protocol.websocket.body;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.codec.body.MessageBody;
import org.miaixz.bus.fabric.codec.body.ProgressBody;

/**
 * WebSocket message body that preserves text or binary message kind.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class WebSocketBody implements MessageBody, ProgressBody {

    /**
     * Default binary media.
     */
    private static final MediaType BINARY = MediaType.APPLICATION_OCTET_STREAM_TYPE;

    /**
     * Default UTF-8 text media.
     */
    private static final MediaType TEXT = MediaType.TEXT_PLAIN_TYPE.withCharset(StandardCharsets.UTF_8);

    /**
     * Maximum materialized WebSocket body bytes.
     */
    private static final long MAX_BODY_BYTES = Normal._16 * Normal.MEBI;

    /**
     * Message kind.
     */
    private final Kind kind;

    /**
     * Text value for text messages.
     */
    private final String text;

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
     * Creates a body.
     *
     * @param kind    message kind
     * @param text    text value
     * @param payload payload
     * @param media   media
     */
    private WebSocketBody(final Kind kind, final String text, final Payload payload, final MediaType media) {
        this(kind, text, payload, media, null);
    }

    /**
     * Creates a body.
     *
     * @param kind     message kind
     * @param text     text value
     * @param payload  payload
     * @param media    media
     * @param progress optional progress tracker
     */
    private WebSocketBody(final Kind kind, final String text, final Payload payload, final MediaType media,
            final ProgressBody.Tracker progress) {
        this.kind = require(kind, "WebSocket body kind");
        this.text = text;
        this.payload = require(payload, "WebSocket payload");
        this.media = require(media, "WebSocket media");
        this.progress = progress;
    }

    /**
     * Creates a text body.
     *
     * @param text text
     * @return WebSocket body
     */
    public static WebSocketBody text(final String text) {
        final ByteString bytes = validateText(text);
        return text(bytes, bytes.utf8());
    }

    /**
     * Creates a text body.
     *
     * @param text text bytes
     * @return WebSocket body
     */
    public static WebSocketBody text(final ByteString text) {
        final ByteString checked = require(text, "WebSocket text");
        final String value = decodeUtf8(checked);
        validateTextValue(value);
        return text(checked, value);
    }

    /**
     * Creates a binary body.
     *
     * @param bytes bytes
     * @return WebSocket body
     */
    public static WebSocketBody binary(final byte[] bytes) {
        return new WebSocketBody(Kind.BINARY, null, Payload.of(bytes), BINARY);
    }

    /**
     * Creates a binary body.
     *
     * @param bytes bytes
     * @return WebSocket body
     */
    public static WebSocketBody binary(final ByteString bytes) {
        return new WebSocketBody(Kind.BINARY, null, Payload.of(require(bytes, "WebSocket binary value")), BINARY);
    }

    /**
     * Creates a binary body.
     *
     * @param buffer binary buffer
     * @return WebSocket body
     * @deprecated use {@link #binary(ByteString)}
     */
    @Deprecated(since = "8.8.3")
    public static WebSocketBody binary(final ByteBuffer buffer) {
        return binary(snapshot(buffer));
    }

    /**
     * Creates a binary body with media metadata.
     *
     * @param payload payload
     * @param media   media
     * @return WebSocket body
     */
    public static WebSocketBody of(final Payload payload, final MediaType media) {
        final Payload current = require(payload, "WebSocket payload");
        if (current.length() > MAX_BODY_BYTES) {
            throw new ProtocolException("WebSocket body payload is too large");
        }
        return new WebSocketBody(Kind.BINARY, null, current, require(media, "WebSocket media"));
    }

    /**
     * Returns message kind.
     *
     * @return kind
     */
    public Kind kind() {
        return kind;
    }

    /**
     * Returns whether this body is a text message.
     *
     * @return true for text
     */
    public boolean textMessage() {
        return kind == Kind.TEXT;
    }

    /**
     * Returns whether this body is a binary message.
     *
     * @return true for binary
     */
    public boolean binaryMessage() {
        return kind == Kind.BINARY;
    }

    /**
     * Returns text value.
     *
     * @return text value or null for binary
     */
    public String textValue() {
        return text;
    }

    /**
     * Returns a read-only binary buffer.
     *
     * @return binary buffer
     * @deprecated use {@link #binaryBytes()}
     */
    @Deprecated(since = "8.8.3")
    public ByteBuffer binaryValue() {
        return binaryBytes().asByteBuffer();
    }

    /**
     * Returns immutable binary bytes.
     *
     * @return binary bytes
     */
    public ByteString binaryBytes() {
        return ByteString.of(payload().bytes(MAX_BODY_BYTES));
    }

    /**
     * Returns a progress-aware copy of this WebSocket body.
     *
     * @param listener listener
     * @return progress-aware WebSocket body
     */
    public WebSocketBody progress(final BiConsumer<Long, Long> listener) {
        return new WebSocketBody(kind, text, payload, media, ProgressBody.Tracker.of(payload, listener));
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
        return progress == null ? Normal.LONG_ZERO : progress.transferred();
    }

    @Override
    public long total() {
        return payload.length();
    }

    @Override
    public WebSocketBody stepBytes(final long bytes) {
        if (progress == null) {
            ProgressBody.super.stepBytes(bytes);
        } else {
            progress.stepBytes(bytes);
        }
        return this;
    }

    @Override
    public WebSocketBody stepRate(final double rate) {
        if (progress == null) {
            ProgressBody.super.stepRate(rate);
        } else {
            progress.stepRate(rate);
        }
        return this;
    }

    /**
     * Copies a binary buffer.
     *
     * @param buffer buffer
     * @return bytes
     */
    private static byte[] snapshot(final ByteBuffer buffer) {
        final ByteBuffer checked = require(buffer, "WebSocket binary value");
        if (checked.remaining() > MAX_BODY_BYTES) {
            throw new ProtocolException("WebSocket body payload is too large");
        }
        final ByteBuffer duplicate = checked.duplicate();
        final byte[] bytes = new byte[duplicate.remaining()];
        duplicate.get(bytes);
        return bytes;
    }

    /**
     * Validates text.
     *
     * @param text text
     * @return text
     */
    private static ByteString validateText(final String text) {
        final String checked = require(text, "WebSocket text");
        validateTextValue(checked);
        return ByteString.encodeUtf8(checked);
    }

    /**
     * Creates a text body from validated values.
     *
     * @param bytes text bytes
     * @param text  text value
     * @return WebSocket body
     */
    private static WebSocketBody text(final ByteString bytes, final String text) {
        return new WebSocketBody(Kind.TEXT, text, Payload.of(bytes), TEXT);
    }

    /**
     * Validates text characters.
     *
     * @param text text
     */
    private static void validateTextValue(final String text) {
        for (int i = Normal._0; i < text.length(); i++) {
            final char current = text.charAt(i);
            if (current < Symbol.C_SPACE && current != Symbol.C_CR && current != Symbol.C_LF) {
                throw new ValidateException("WebSocket text contains an invalid control character");
            }
        }
    }

    /**
     * Decodes text bytes as strict UTF-8.
     *
     * @param text text bytes
     * @return decoded text
     */
    private static String decodeUtf8(final ByteString text) {
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(text.asByteBuffer()).toString();
        } catch (final CharacterCodingException e) {
            throw new ValidateException("WebSocket text must be valid UTF-8", e);
        }
    }

    /**
     * Validates a required reference.
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
     * WebSocket message kind.
     */
    public enum Kind {
        /**
         * Text message.
         */
        TEXT,

        /**
         * Binary message.
         */
        BINARY

    }

}
