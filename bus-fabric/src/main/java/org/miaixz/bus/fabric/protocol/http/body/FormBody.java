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
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.core.net.url.UrlEncoder;
import org.miaixz.bus.core.xyz.CharKit;
import org.miaixz.bus.core.xyz.StringKit;
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
     * Canonical application/x-www-form-urlencoded media type.
     */
    private final MediaType media;

    /**
     * Repeatable payload that re-encodes the immutable entry snapshot on demand.
     */
    private final Payload payload;

    /**
     * Creates a form body.
     *
     * @param media   non-null form media type
     * @param payload non-null repeatable encoded form payload
     * @throws ValidateException if either component is {@code null}
     */
    private FormBody(final MediaType media, final Payload payload) {
        this.media = Assert.notNull(media, () -> new ValidateException("Form media must not be null"));
        this.payload = Assert.notNull(payload, () -> new ValidateException("Form payload must not be null"));
    }

    /**
     * Creates a form body builder.
     *
     * @return new empty form body builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns form media.
     *
     * @return canonical form media type
     */
    public MediaType media() {
        return media;
    }

    /**
     * Returns form payload.
     *
     * @return repeatable lazily encoded form payload
     */
    public Payload payload() {
        return payload;
    }

    /**
     * Encodes entries to UTF-8 form bytes.
     *
     * @param entries immutable ordered form entries
     * @return newly allocated UTF-8 form bytes
     */
    private static byte[] encodeEntries(final List<Entry> entries) {
        return ByteString.encodeString(encodeText(entries), org.miaixz.bus.core.lang.Charset.UTF_8).toByteArray();
    }

    /**
     * Encodes entries to text.
     *
     * @param entries immutable ordered form entries
     * @return ampersand-separated name/value pairs with decoded components percent encoded
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
     * @param value decoded form name or content
     * @return UTF-8 percent-encoded component text
     * @throws ProtocolException if the URL encoder cannot encode the component
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
     * @param value form name or content to validate
     * @param name  logical field label included in the validation error
     * @return unchanged non-blank, single-line text
     * @throws ValidateException if the text is blank or contains a line break
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
     * @param value encoded form component to scan
     * @throws ProtocolException if any percent sign is not followed by two hexadecimal digits
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
     * @param name    validated field name
     * @param value   validated field content
     * @param encoded whether both components already contain their desired percent encoding
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
         * @param entries ordered form entries copied into an immutable snapshot
         * @throws ValidateException if {@code entries} is {@code null}
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
         * @return in-memory source containing a newly encoded snapshot
         */
        @Override
        public Source source() {
            return new Buffer().write(bytes());
        }

        /**
         * Returns encoded bytes.
         *
         * @return newly encoded bytes within the default materialization limit
         */
        @Override
        public byte[] bytes() {
            return bytes(org.miaixz.bus.fabric.Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        /**
         * Returns encoded bytes with an explicit materialize threshold.
         *
         * @param maxBytes maximum encoded bytes permitted
         * @return newly encoded form bytes
         * @throws ValidateException if {@code maxBytes} is invalid
         * @throws InternalException if the encoded form exceeds the limit
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
         * @param charset character set used to decode the UTF-8 form bytes
         * @return encoded form bytes interpreted using the supplied character set
         */
        @Override
        public String text(final Charset charset) {
            return text(charset, org.miaixz.bus.fabric.Builder.DEFAULT_MATERIALIZE_MAX_BYTES);
        }

        /**
         * Reads encoded text using the supplied charset and threshold.
         *
         * @param charset  character set used to decode the UTF-8 form bytes
         * @param maxBytes maximum encoded bytes permitted
         * @return encoded form bytes interpreted using the supplied character set
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
         * @return {@code true}, because each source is created from a fresh encoding
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
         * Mutable ordered entries copied when {@link #build()} is called.
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
         * @throws ValidateException if either component is blank or contains a line break
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
         * @throws ProtocolException if either component contains an invalid percent escape
         * @throws ValidateException if either component is blank or contains a line break
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
         * @return immutable form body backed by a snapshot of the current entries
         */
        public FormBody build() {
            final List<Entry> snapshot = List.copyOf(entries);
            return new FormBody(MediaType.APPLICATION_FORM_URLENCODED_TYPE, new FormPayload(snapshot));
        }

    }

}
