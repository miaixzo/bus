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
 * HTTP text request body with an explicit media type and charset snapshot.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TextBody implements RequestBody {

    /**
     * Original text value.
     */
    private final String text;

    /**
     * Media type.
     */
    private final MediaType media;

    /**
     * Encoded payload.
     */
    private final Payload payload;

    /**
     * Creates a text body.
     *
     * @param text    text
     * @param media   media type
     * @param payload encoded payload
     */
    private TextBody(final String text, final MediaType media, final Payload payload) {
        this.text = text;
        this.media = media;
        this.payload = payload;
    }

    /**
     * Creates a UTF-8 {@code text/plain} body.
     *
     * @param text text
     * @return text body
     */
    public static TextBody of(final String text) {
        return of(text, org.miaixz.bus.core.lang.Charset.UTF_8);
    }

    /**
     * Creates a {@code text/plain} body with an explicit charset.
     *
     * @param text    text
     * @param charset charset
     * @return text body
     */
    public static TextBody of(final String text, final Charset charset) {
        final Charset checkedCharset = Assert
                .notNull(charset, () -> new ValidateException("Text charset must not be null"));
        return of(text, MediaType.TEXT_PLAIN_TYPE.withCharset(checkedCharset));
    }

    /**
     * Creates a text body with an explicit media type.
     *
     * @param text  text
     * @param media media type
     * @return text body
     */
    public static TextBody of(final String text, final MediaType media) {
        final String value = text == null ? "" : text;
        final MediaType checkedMedia = Assert.notNull(media, () -> new ValidateException("Text media must not be null"));
        final Charset charset = checkedMedia.charset(org.miaixz.bus.core.lang.Charset.UTF_8);
        return new TextBody(value, checkedMedia, Payload.of(value, charset));
    }

    /**
     * Returns the original text.
     *
     * @return text
     */
    public String text() {
        return text;
    }

    /**
     * Returns the media type.
     *
     * @return media type
     */
    @Override
    public MediaType media() {
        return media;
    }

    /**
     * Returns the encoded payload.
     *
     * @return payload
     */
    @Override
    public Payload payload() {
        return payload;
    }

}
