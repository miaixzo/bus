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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.codec.body.BodyCodec;
import org.miaixz.bus.fabric.codec.body.RequestBody;

/**
 * HTTP text body with an explicit encoding snapshot.
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
     * Creates a text body.
     *
     * @param text  text
     * @param media media type
     * @return text body
     */
    public static TextBody of(final String text, final MediaType media) {
        final String validText = validateText(text);
        final MediaType validMedia = Assert.notNull(media, () -> new ValidateException("Text media must not be null"));
        final Charset charset = validMedia.charset(org.miaixz.bus.core.lang.Charset.UTF_8);
        return new TextBody(validText, validMedia, BodyCodec.create().text(validText, charset));
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

    /**
     * Validates text.
     *
     * @param text text
     * @return validated text
     */
    private static String validateText(final String text) {
        final String checked = Assert
                .notBlank(text, () -> new ValidateException("Text must be non-blank and single-line"));
        Assert.isFalse(
                StringKit.containsAny(checked, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("Text must be non-blank and single-line"));
        return checked;
    }

}
