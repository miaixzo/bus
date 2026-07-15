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
package org.miaixz.bus.fabric.protocol.stomp;

import java.nio.charset.Charset;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.protocol.stomp.body.StompBody;

/**
 * Immutable STOMP message value.
 *
 * @param destination message destination
 * @param headers     message headers
 * @param payload     message payload
 * @author Kimi Liu
 * @since Java 21+
 */
public record StompMessage(String destination, Headers headers, Payload payload) {

    /**
     * Creates a validated message.
     *
     * @param destination message destination
     * @param headers     message headers
     * @param payload     message payload
     */
    public StompMessage {
        destination = validateToken(destination, "STOMP destination");
        headers = require(headers, "STOMP headers");
        payload = require(payload, "STOMP payload");
    }

    /**
     * Creates a message.
     *
     * @param destination destination
     * @param headers     headers
     * @param payload     payload
     * @return message
     */
    public static StompMessage of(final String destination, final Headers headers, final Payload payload) {
        return new StompMessage(destination, headers, payload);
    }

    /**
     * Creates a message.
     *
     * @param destination destination
     * @param headers     headers
     * @param body        body
     * @return message
     */
    public static StompMessage of(final String destination, final Headers headers, final StompBody body) {
        return new StompMessage(destination, headers, require(body, "STOMP body").payload());
    }

    /**
     * Returns destination.
     *
     * @return destination
     */
    @Override
    public String destination() {
        return destination;
    }

    /**
     * Returns headers.
     *
     * @return headers
     */
    @Override
    public Headers headers() {
        return headers;
    }

    /**
     * Returns payload.
     *
     * @return payload
     */
    @Override
    public Payload payload() {
        return payload;
    }

    /**
     * Returns STOMP body.
     *
     * @return body
     */
    public StompBody body() {
        final String contentType = headers.get(HTTP.CONTENT_TYPE);
        final MediaType media = contentType == null ? MediaType.APPLICATION_OCTET_STREAM_TYPE
                : MediaType.parse(contentType);
        return StompBody.of(payload, media);
    }

    /**
     * Reads payload text.
     *
     * @param charset charset
     * @return text
     */
    public String text(final Charset charset) {
        return text(charset, Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
    }

    /**
     * Reads payload text with an explicit materialize threshold.
     *
     * @param charset  charset
     * @param maxBytes maximum bytes to materialize
     * @return text
     */
    public String text(final Charset charset, final long maxBytes) {
        return payload.text(require(charset, "Charset"), maxBytes);
    }

    /**
     * Validates single-line text.
     *
     * @param value value
     * @param name  field name
     * @return validated value
     */
    static String validateToken(final String value, final String name) {
        if (StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException(name + " must be non-blank and single-line");
        }
        return value;
    }

    /**
     * Validates required references.
     *
     * @param value value
     * @param name  field name
     * @param <T>   value type
     * @return value
     */
    static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
