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
package org.miaixz.bus.fabric.protocol.http.body;

import java.nio.charset.Charset;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.codec.body.RequestBody;

/**
 * Immutable HTTP text request body with media metadata and an eagerly encoded payload.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TextBody implements RequestBody {

    /**
     * Normalized source text used to create the payload.
     */
    private final String text;

    /**
     * Media type reported for this request body.
     */
    private final MediaType media;

    /**
     * Payload encoded once with the media charset or UTF-8 fallback.
     */
    private final Payload payload;

    /**
     * Creates a text body from normalized source values.
     *
     * @param text    non-null normalized source text
     * @param media   media type reported by the body
     * @param payload eagerly encoded payload
     */
    private TextBody(final String text, final MediaType media, final Payload payload) {
        this.text = text;
        this.media = media;
        this.payload = payload;
    }

    /**
     * Creates a UTF-8 {@code text/plain} body.
     *
     * @param text source text; {@code null} is normalized to an empty string
     * @return body encoded as UTF-8 plain text
     */
    public static TextBody of(final String text) {
        return of(text, org.miaixz.bus.core.lang.Charset.UTF_8);
    }

    /**
     * Creates a {@code text/plain} body with an explicit charset.
     *
     * @param text    source text; {@code null} is normalized to an empty string
     * @param charset charset used to encode the payload and annotate the media type
     * @return plain-text body encoded with the supplied charset
     * @throws ValidateException if {@code charset} is {@code null}
     */
    public static TextBody of(final String text, final Charset charset) {
        final Charset checkedCharset = Assert
                .notNull(charset, () -> new ValidateException("Text charset must not be null"));
        return of(text, MediaType.TEXT_PLAIN_TYPE.withCharset(checkedCharset));
    }

    /**
     * Creates a text body with an explicit media type, using UTF-8 when that media type has no charset.
     *
     * @param text  source text; {@code null} is normalized to an empty string
     * @param media media type and optional payload charset
     * @return body eagerly encoded using the effective media charset
     * @throws ValidateException if {@code media} is {@code null}
     */
    public static TextBody of(final String text, final MediaType media) {
        final String value = text == null ? "" : text;
        final MediaType checkedMedia = Assert
                .notNull(media, () -> new ValidateException("Text media must not be null"));
        final Charset charset = checkedMedia.charset(org.miaixz.bus.core.lang.Charset.UTF_8);
        return new TextBody(value, checkedMedia, Payload.of(value, charset));
    }

    /**
     * Returns the normalized source text.
     *
     * @return non-null text used to create the payload
     */
    public String text() {
        return text;
    }

    /**
     * Returns the media type.
     *
     * @return media type supplied at construction, including any declared charset
     */
    @Override
    public MediaType media() {
        return media;
    }

    /**
     * Returns the encoded payload.
     *
     * @return immutable payload encoded when this body was created
     */
    @Override
    public Payload payload() {
        return payload;
    }

}
