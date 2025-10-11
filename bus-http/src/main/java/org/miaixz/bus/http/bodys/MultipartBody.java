/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.Headers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A MIME multipart request body.
 * <p>
 * This class represents a composite request body of type {@code multipart/related}, used for uploading multiple parts
 * (such as files and form data). Each part is separated by a boundary and can have its own custom headers and content
 * type.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MultipartBody extends RequestBody {

    /**
     * The colon-space separator.
     */
    private static final byte[] COLONSPACE = { Symbol.C_COLON, Symbol.C_SPACE };
    /**
     * The carriage return-line feed separator.
     */
    private static final byte[] CRLF = { Symbol.C_CR, Symbol.C_LF };
    /**
     * The double-dash separator.
     */
    private static final byte[] DASHDASH = { Symbol.C_MINUS, Symbol.C_MINUS };
    /**
     * The boundary separator.
     */
    private final ByteString boundary;
    /**
     * The original media type.
     */
    private final MediaType originalType;
    /**
     * The full media type, including the boundary.
     */
    private final MediaType contentType;
    /**
     * The list of parts.
     */
    private final List<Part> parts;
    /**
     * The content length.
     */
    private long contentLength = -1L;

    /**
     * Constructs a new {@code MultipartBody} instance.
     *
     * @param boundary    The boundary separator.
     * @param contentType The media type.
     * @param parts       The list of parts.
     */
    MultipartBody(ByteString boundary, MediaType contentType, List<Part> parts) {
        this.boundary = boundary;
        this.originalType = contentType;
        this.contentType = MediaType.valueOf(contentType + "; boundary=" + boundary.utf8());
        this.parts = org.miaixz.bus.http.Builder.immutableList(parts);
    }

    /**
     * Appends a quoted string to a {@link StringBuilder}.
     * <p>
     * This method appends a key to a {@link StringBuilder}, escaping special characters like newlines and quotes. It is
     * recommended to avoid using double quotes, newlines, or percent signs in field names.
     * </p>
     *
     * @param target The target {@link StringBuilder}.
     * @param key    The key to append.
     */
    static void appendQuotedString(StringBuilder target, String key) {
        target.append(Symbol.C_DOUBLE_QUOTES);
        for (int i = 0, len = key.length(); i < len; i++) {
            char ch = key.charAt(i);
            switch (ch) {
                case Symbol.C_LF:
                    target.append("%0A");
                    break;

                case Symbol.C_CR:
                    target.append("%0D");
                    break;

                case Symbol.C_DOUBLE_QUOTES:
                    target.append("%22");
                    break;

                default:
                    target.append(ch);
                    break;
            }
        }
        target.append(Symbol.C_DOUBLE_QUOTES);
    }

    /**
     * Returns the original media type.
     *
     * @return The original media type.
     */
    public MediaType type() {
        return originalType;
    }

    /**
     * Returns the boundary separator.
     *
     * @return The boundary string.
     */
    public String boundary() {
        return boundary.utf8();
    }

    /**
     * Returns the number of parts in this multipart body.
     *
     * @return The number of parts.
     */
    public int size() {
        return parts.size();
    }

    /**
     * Returns the list of parts.
     *
     * @return An unmodifiable list of parts.
     */
    public List<Part> parts() {
        return parts;
    }

    /**
     * Returns the part at the specified index.
     *
     * @param index The index.
     * @return The part instance.
     */
    public Part part(int index) {
        return parts.get(index);
    }

    /**
     * Returns the full media type, including the boundary.
     *
     * @return The media type with the boundary.
     */
    @Override
    public MediaType contentType() {
        return contentType;
    }

    /**
     * Returns the length of this request body in bytes.
     *
     * @return The content length.
     * @throws IOException if the length cannot be determined.
     */
    @Override
    public long contentLength() throws IOException {
        long result = contentLength;
        if (result != -1L)
            return result;
        return contentLength = writeOrCountBytes(null, true);
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
     * @return The number of bytes written or counted, or -1 if the length is unknown.
     * @throws IOException if an I/O error occurs.
     */
    private long writeOrCountBytes(BufferSink sink, boolean countBytes) throws IOException {
        long byteCount = 0L;

        Buffer byteCountBuffer = null;
        if (countBytes) {
            sink = byteCountBuffer = new Buffer();
        }

        for (int p = 0, partCount = parts.size(); p < partCount; p++) {
            Part part = parts.get(p);
            Headers headers = part.headers;
            RequestBody body = part.body;

            sink.write(DASHDASH);
            sink.write(boundary);
            sink.write(CRLF);

            if (null != headers) {
                for (int h = 0, headerCount = headers.size(); h < headerCount; h++) {
                    sink.writeUtf8(headers.name(h)).write(COLONSPACE).writeUtf8(headers.value(h)).write(CRLF);
                }
            }

            MediaType contentType = body.contentType();
            if (null != contentType) {
                sink.writeUtf8("Content-Type: ").writeUtf8(contentType.toString()).write(CRLF);
            }

            long contentLength = body.contentLength();
            if (contentLength != -1) {
                sink.writeUtf8("Content-Length: ").writeDecimalLong(contentLength).write(CRLF);
            } else if (countBytes) {
                // We can't measure the body's size without the sink.
                byteCountBuffer.clear();
                return -1L;
            }

            sink.write(CRLF);

            if (countBytes) {
                byteCount += contentLength;
            } else {
                body.writeTo(sink);
            }

            sink.write(CRLF);
        }

        sink.write(DASHDASH);
        sink.write(boundary);
        sink.write(DASHDASH);
        sink.write(CRLF);

        if (countBytes) {
            byteCount += byteCountBuffer.size();
            byteCountBuffer.clear();
        }

        return byteCount;
    }

    /**
     * A part of a multipart body.
     */
    public static final class Part {

        /**
         * The headers of the part.
         */
        final Headers headers;
        /**
         * The body of the part.
         */
        final RequestBody body;

        /**
         * Constructs a new {@code Part} instance.
         *
         * @param headers The headers.
         * @param body    The request body.
         */
        private Part(Headers headers, RequestBody body) {
            this.headers = headers;
            this.body = body;
        }

        /**
         * Creates a new part with no headers.
         *
         * @param body The request body.
         * @return A new {@link Part} instance.
         * @throws NullPointerException if body is null.
         */
        public static Part create(RequestBody body) {
            return create(null, body);
        }

        /**
         * Creates a new part with the given headers and body.
         *
         * @param headers The headers.
         * @param body    The request body.
         * @return A new {@link Part} instance.
         * @throws NullPointerException     if body is null.
         * @throws IllegalArgumentException if headers contains Content-Type or Content-Length.
         */
        public static Part create(Headers headers, RequestBody body) {
            if (null == body) {
                throw new NullPointerException("body == null");
            }
            if (null != headers && null != headers.get(HTTP.CONTENT_TYPE)) {
                throw new IllegalArgumentException("Unexpected header: Content-Type");
            }
            if (null != headers && null != headers.get(HTTP.CONTENT_LENGTH)) {
                throw new IllegalArgumentException("Unexpected header: Content-Length");
            }
            return new Part(headers, body);
        }

        /**
         * Creates a new form-data part.
         *
         * @param name  The field name.
         * @param value The field value.
         * @return A new {@link Part} instance.
         * @throws NullPointerException if name is null.
         */
        public static Part createFormData(String name, String value) {
            return createFormData(name, null, RequestBody.create(null, value));
        }

        /**
         * Creates a new form-data part with a filename.
         *
         * @param name     The field name.
         * @param filename The filename.
         * @param body     The request body.
         * @return A new {@link Part} instance.
         * @throws NullPointerException if name is null.
         */
        public static Part createFormData(String name, String filename, RequestBody body) {
            if (null == name) {
                throw new NullPointerException("name == null");
            }
            StringBuilder disposition = new StringBuilder("form-data; name=");
            appendQuotedString(disposition, name);

            if (null != filename) {
                disposition.append("; filename=");
                appendQuotedString(disposition, filename);
            }

            Headers headers = new Headers.Builder().addUnsafeNonAscii(HTTP.CONTENT_DISPOSITION, disposition.toString())
                    .build();

            return create(headers, body);
        }

        /**
         * Returns the headers of this part.
         *
         * @return The headers, which may be null.
         */
        public Headers headers() {
            return headers;
        }

        /**
         * Returns the body of this part.
         *
         * @return The request body.
         */
        public RequestBody body() {
            return body;
        }
    }

    /**
     * A builder for creating {@link MultipartBody} instances.
     */
    public static final class Builder {

        /**
         * The boundary separator.
         */
        private final ByteString boundary;
        /**
         * The list of parts.
         */
        private final List<Part> parts = new ArrayList<>();
        /**
         * The media type.
         */
        private MediaType type = MediaType.MULTIPART_MIXED_TYPE;

        /**
         * Default constructor that uses a random UUID as the boundary.
         */
        public Builder() {
            this(UUID.randomUUID().toString());
        }

        /**
         * Constructs a new builder with a specified boundary.
         *
         * @param boundary The boundary separator.
         */
        public Builder(String boundary) {
            this.boundary = ByteString.encodeUtf8(boundary);
        }

        /**
         * Sets the media type.
         * <p>
         * Supported types include {@link MediaType#MULTIPART_MIXED} (default), {@link MediaType#MULTIPART_ALTERNATIVE},
         * {@link MediaType#MULTIPART_DIGEST}, {@link MediaType#MULTIPART_PARALLEL}, and
         * {@link MediaType#MULTIPART_FORM_DATA}.
         * </p>
         *
         * @param type The media type.
         * @return this builder instance.
         * @throws NullPointerException     if type is null.
         * @throws IllegalArgumentException if the type is not a multipart type.
         */
        public Builder setType(MediaType type) {
            if (null == type) {
                throw new NullPointerException("type == null");
            }
            if (!"multipart".equals(type.type())) {
                throw new IllegalArgumentException("multipart != " + type);
            }
            this.type = type;
            return this;
        }

        /**
         * Adds a part with no headers.
         *
         * @param body The request body.
         * @return this builder instance.
         */
        public Builder addPart(RequestBody body) {
            return addPart(Part.create(body));
        }

        /**
         * Adds a part with the given headers and body.
         *
         * @param headers The headers.
         * @param body    The request body.
         * @return this builder instance.
         */
        public Builder addPart(Headers headers, RequestBody body) {
            return addPart(Part.create(headers, body));
        }

        /**
         * Adds a form-data part.
         *
         * @param name  The field name.
         * @param value The field value.
         * @return this builder instance.
         */
        public Builder addFormDataPart(String name, String value) {
            return addPart(Part.createFormData(name, value));
        }

        /**
         * Adds a form-data part with a filename.
         *
         * @param name     The field name.
         * @param filename The filename.
         * @param body     The request body.
         * @return this builder instance.
         */
        public Builder addFormDataPart(String name, String filename, RequestBody body) {
            return addPart(Part.createFormData(name, filename, body));
        }

        /**
         * Adds a part.
         *
         * @param part The part instance.
         * @return this builder instance.
         * @throws NullPointerException if part is null.
         */
        public Builder addPart(Part part) {
            if (part == null)
                throw new NullPointerException("part == null");
            parts.add(part);
            return this;
        }

        /**
         * Builds a new {@link MultipartBody} instance.
         *
         * @return A new {@link MultipartBody} instance.
         * @throws IllegalStateException if no parts have been added.
         */
        public MultipartBody build() {
            if (parts.isEmpty()) {
                throw new IllegalStateException("Multipart body must have at least one part.");
            }
            return new MultipartBody(boundary, type, parts);
        }
    }

}
