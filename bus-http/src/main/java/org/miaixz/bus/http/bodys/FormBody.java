/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.http.bodys;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.UnoUrl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * An HTTP request body that represents form-encoded data.
 * <p>
 * This class handles form data encoded in the {@code application/x-www-form-urlencoded} format. It provides support for
 * encoding and decoding form field names and values, which are stored as key-value pairs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class FormBody extends RequestBody {

    /**
     * The list of encoded field names.
     */
    private final List<String> encodedNames;
    /**
     * The list of encoded field values.
     */
    private final List<String> encodedValues;

    /**
     * Constructs a new {@code FormBody} instance.
     *
     * @param encodedNames  The list of encoded field names.
     * @param encodedValues The list of encoded field values.
     */
    FormBody(List<String> encodedNames, List<String> encodedValues) {
        this.encodedNames = org.miaixz.bus.http.Builder.immutableList(encodedNames);
        this.encodedValues = org.miaixz.bus.http.Builder.immutableList(encodedValues);
    }

    /**
     * Returns the number of form fields.
     *
     * @return The number of fields.
     */
    public int size() {
        return encodedNames.size();
    }

    /**
     * Returns the encoded field name at the specified index.
     *
     * @param index The index.
     * @return The encoded field name.
     */
    public String encodedName(int index) {
        return encodedNames.get(index);
    }

    /**
     * Returns the decoded field name at the specified index.
     *
     * @param index The index.
     * @return The decoded field name.
     */
    public String name(int index) {
        return UnoUrl.percentDecode(encodedName(index), true);
    }

    /**
     * Returns the encoded field value at the specified index.
     *
     * @param index The index.
     * @return The encoded field value.
     */
    public String encodedValue(int index) {
        return encodedValues.get(index);
    }

    /**
     * Returns the decoded field value at the specified index.
     *
     * @param index The index.
     * @return The decoded field value.
     */
    public String value(int index) {
        return UnoUrl.percentDecode(encodedValue(index), true);
    }

    /**
     * Returns the media type of this request body.
     *
     * @return The media type (application/x-www-form-urlencoded).
     */
    @Override
    public MediaType contentType() {
        return MediaType.APPLICATION_FORM_URLENCODED_TYPE;
    }

    /**
     * Returns the length of this request body in bytes.
     *
     * @return The length of the request body.
     */
    @Override
    public long contentLength() {
        return writeOrCountBytes(null, true);
    }

    /**
     * Writes the content of this request body to the given sink.
     *
     * @param sink The sink to write to.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void writeTo(BufferSink sink) throws IOException {
        writeOrCountBytes(sink, false);
    }

    /**
     * Writes the content to the given sink or counts the number of bytes that would be written.
     * <p>
     * This method is used to either write the request body to an output stream or to calculate its byte length,
     * ensuring content consistency.
     * </p>
     *
     * @param sink       The sink to write to, or null if only counting bytes.
     * @param countBytes {@code true} to only count bytes, {@code false} to write them.
     * @return The number of bytes written or counted.
     */
    private long writeOrCountBytes(BufferSink sink, boolean countBytes) {
        long byteCount = 0L;

        Buffer buffer;
        if (countBytes) {
            buffer = new Buffer();
        } else {
            buffer = sink.buffer();
        }

        for (int i = 0, size = encodedNames.size(); i < size; i++) {
            if (i > 0)
                buffer.writeByte(Symbol.C_AND);
            buffer.writeUtf8(encodedNames.get(i));
            buffer.writeByte(Symbol.C_EQUAL);
            buffer.writeUtf8(encodedValues.get(i));
        }

        if (countBytes) {
            byteCount = buffer.size();
            buffer.clear();
        }

        return byteCount;
    }

    /**
     * A builder for creating {@link FormBody} instances.
     */
    public static final class Builder {

        /**
         * The list of field names.
         */
        private final List<String> names = new ArrayList<>();
        /**
         * The list of field values.
         */
        private final List<String> values = new ArrayList<>();
        /**
         * The character set for encoding.
         */
        private final Charset charset;

        /**
         * Default constructor.
         */
        public Builder() {
            this(null);
        }

        /**
         * Constructs a new builder with a specified character set.
         *
         * @param charset The character set to use for encoding (null for default UTF-8).
         */
        public Builder(Charset charset) {
            this.charset = charset;
        }

        /**
         * Adds a form field.
         *
         * @param name  The field name.
         * @param value The field value.
         * @return this builder instance.
         * @throws NullPointerException if name or value is null.
         */
        public Builder add(String name, String value) {
            if (null == name) {
                throw new NullPointerException("name == null");
            }
            if (null == value) {
                throw new NullPointerException("value == null");
            }

            names.add(UnoUrl.canonicalize(name, UnoUrl.FORM_ENCODE_SET, false, false, true, true, charset));
            values.add(UnoUrl.canonicalize(value, UnoUrl.FORM_ENCODE_SET, false, false, true, true, charset));
            return this;
        }

        /**
         * Adds an already-encoded form field.
         *
         * @param name  The encoded field name.
         * @param value The encoded field value.
         * @return this builder instance.
         * @throws NullPointerException if name or value is null.
         */
        public Builder addEncoded(String name, String value) {
            if (null == name) {
                throw new NullPointerException("name == null");
            }
            if (null == value) {
                throw new NullPointerException("value == null");
            }

            names.add(UnoUrl.canonicalize(name, UnoUrl.FORM_ENCODE_SET, true, false, true, true, charset));
            values.add(UnoUrl.canonicalize(value, UnoUrl.FORM_ENCODE_SET, true, false, true, true, charset));
            return this;
        }

        /**
         * Builds a new {@link FormBody} instance.
         *
         * @return A new {@link FormBody} instance.
         */
        public FormBody build() {
            return new FormBody(names, values);
        }
    }

}
