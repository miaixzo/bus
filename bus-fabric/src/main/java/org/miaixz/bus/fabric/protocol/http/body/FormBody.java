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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Payload;
import org.miaixz.bus.fabric.codec.body.RequestBody;

/**
 * Immutable application/x-www-form-urlencoded body.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class FormBody implements RequestBody {

    /**
     * Default form media type.
     */
    private static final MediaType FORM_MEDIA = MediaType.APPLICATION_FORM_URLENCODED_TYPE;

    /**
     * Form media type.
     */
    private final MediaType media;

    /**
     * Lazily encoded payload.
     */
    private final Payload payload;

    /**
     * Creates a form body.
     *
     * @param media   media type
     * @param payload payload
     */
    private FormBody(final MediaType media, final Payload payload) {
        this.media = Assert.notNull(media, () -> new ValidateException("Form media must not be null"));
        this.payload = Assert.notNull(payload, () -> new ValidateException("Form payload must not be null"));
    }

    /**
     * Creates a form body builder.
     *
     * @return builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns form media.
     *
     * @return media
     */
    public MediaType media() {
        return media;
    }

    /**
     * Returns form payload.
     *
     * @return payload
     */
    public Payload payload() {
        return payload;
    }

    /**
     * Encodes entries to UTF-8 form bytes.
     *
     * @param entries entries
     * @return encoded bytes
     */
    private static byte[] encodeEntries(final List<Entry> entries) {
        return ByteString.encodeString(encodeText(entries), org.miaixz.bus.core.lang.Charset.UTF_8).toByteArray();
    }

    /**
     * Encodes entries to text.
     *
     * @param entries entries
     * @return encoded text
     */
    private static String encodeText(final List<Entry> entries) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) {
                builder.append(Symbol.C_AND);
            }
            final Entry entry = entries.get(i);
            builder.append(entry.encoded ? entry.name : encode(entry.name)).append(Symbol.C_EQUAL)
                    .append(entry.encoded ? entry.value : encode(entry.value));
        }
        return builder.toString();
    }

    /**
     * Encodes one form component using the same space rule as UnoUrl query encoding.
     *
     * @param value decoded value
     * @return encoded value
     */
    private static String encode(final String value) {
        try {
            return UrlEncoder.encodeAll(value, org.miaixz.bus.core.lang.Charset.UTF_8);
        } catch (final RuntimeException e) {
            throw new ProtocolException("Unable to encode form field", e);
        }
    }

    /**
     * Validates a form field.
     *
     * @param value field value
     * @param name  field name
     * @return validated value
     */
    private static String validateField(final String value, final String name) {
        final String checked = Assert
                .notBlank(value, () -> new ValidateException(name + " must be non-blank and single-line"));
        Assert.isFalse(
                StringKit.containsAny(checked, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException(name + " must be non-blank and single-line"));
        return checked;
    }

    /**
     * Validates percent escapes in an already encoded field.
     *
     * @param value encoded field
     */
    private static void validatePercentEncoding(final String value) {
        for (int i = 0; i < value.length(); i++) {
            if (value.charAt(i) == Symbol.C_PERCENT) {
                Assert.isFalse(
                        i + 2 >= value.length() || !CharKit.isHexChar(value.charAt(i + 1))
                                || !CharKit.isHexChar(value.charAt(i + 2)),
                        () -> new ProtocolException("Invalid percent-encoded form field"));
                i += 2;
            }
        }
    }

    /**
     * Form entry.
     *
     * @param name    field name
     * @param value   field value
     * @param encoded whether values are already encoded
     */
    private record Entry(String name, String value, boolean encoded) {

    }

    /**
     * Repeatable lazy form payload.
     */
    private static final class FormPayload implements Payload {

        /**
         * Immutable entry snapshot.
         */
        private final List<Entry> entries;

        /**
         * Creates a form payload.
         *
         * @param entries entries
         */
        private FormPayload(final List<Entry> entries) {
            this.entries = List
                    .copyOf(Assert.notNull(entries, () -> new ValidateException("Form entries must not be null")));
        }

        /**
         * Returns encoded byte length.
         *
         * @return encoded byte length
         */
        @Override
        public long length() {
            return encodeEntries(entries).length;
        }

        /**
         * Opens an encoded source.
         *
         * @return payload source
         */
        @Override
        public Source source() {
            return new Buffer().write(bytes());
        }

        /**
         * Opens a legacy input stream view.
         *
         * @return payload stream
         */
        @Override
        @Deprecated(since = "8.8.3")
        public InputStream stream() {
            return Payload.super.stream();
        }

        /**
         * Returns encoded bytes.
         *
         * @return encoded bytes
         */
        @Override
        public byte[] bytes() {
            return bytes(Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        /**
         * Returns encoded bytes with an explicit materialize threshold.
         *
         * @param maxBytes maximum bytes to materialize
         * @return encoded bytes
         */
        @Override
        public byte[] bytes(final long maxBytes) {
            Payload.validateMaterializeMaxBytes(maxBytes);
            final byte[] encoded = encodeEntries(entries);
            if (encoded.length > maxBytes) {
                throw Payload.materializeExceeded(encoded.length, maxBytes, "FormBody.FormPayload.bytes(long)");
            }
            return encoded;
        }

        /**
         * Reads encoded text using the supplied charset.
         *
         * @param charset charset
         * @return encoded text
         */
        @Override
        public String text(final Charset charset) {
            return text(charset, Options.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        /**
         * Reads encoded text using the supplied charset and threshold.
         *
         * @param charset  charset
         * @param maxBytes maximum bytes to materialize
         * @return encoded text
         */
        @Override
        public String text(final Charset charset, final long maxBytes) {
            final Charset checkedCharset = Assert
                    .notNull(charset, () -> new ValidateException("Charset must not be null"));
            return new String(bytes(maxBytes), checkedCharset);
        }

        /**
         * Returns repeatability.
         *
         * @return true
         */
        @Override
        public boolean repeatable() {
            return true;
        }

    }

    /**
     * Form body builder.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class Builder {

        /**
         * Form entries.
         */
        private final List<Entry> entries = new ArrayList<>();

        /**
         * Creates a form body builder.
         */
        private Builder() {
            // No initialization required.
        }

        /**
         * Adds a decoded form field.
         *
         * @param name  field name
         * @param value field value
         * @return this builder
         */
        public Builder add(final String name, final String value) {
            final String validName = validateField(name, "Form field name");
            final String validValue = validateField(value, "Form field value");
            entries.add(new Entry(validName, validValue, false));
            return this;
        }

        /**
         * Adds an already encoded form field.
         *
         * @param name  encoded field name
         * @param value encoded field value
         * @return this builder
         */
        public Builder encoded(final String name, final String value) {
            final String validName = validateField(name, "Encoded form field name");
            final String validValue = validateField(value, "Encoded form field value");
            validatePercentEncoding(validName);
            validatePercentEncoding(validValue);
            entries.add(new Entry(validName, validValue, true));
            return this;
        }

        /**
         * Builds an immutable form body.
         *
         * @return form body
         */
        public FormBody build() {
            final List<Entry> snapshot = List.copyOf(entries);
            return new FormBody(FORM_MEDIA, new FormPayload(snapshot));
        }

    }

}
