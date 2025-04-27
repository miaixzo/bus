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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.Headers;

/**
 * The MIME Multipart/Related Content-type 用于复合对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MultipartBody extends RequestBody {

    private static final byte[] COLONSPACE = { Symbol.C_COLON, Symbol.C_SPACE };
    private static final byte[] CRLF = { Symbol.C_CR, Symbol.C_LF };
    private static final byte[] DASHDASH = { Symbol.C_MINUS, Symbol.C_MINUS };

    private final ByteString boundary;
    private final MediaType originalType;
    private final MediaType mediaType;
    private final List<Part> parts;
    private long contentLength = -1L;

    MultipartBody(ByteString boundary, MediaType mediaType, List<Part> parts) {
        this.boundary = boundary;
        this.originalType = mediaType;
        this.mediaType = MediaType.valueOf(mediaType.toString() + "; boundary=" + boundary.utf8());
        this.parts = org.miaixz.bus.http.Builder.immutableList(parts);
    }

    /**
     * Appends a quoted-string to a StringBuilder RFC 2388 is rather vague about how one should escape special
     * characters in form-data parameters, and as it turns out Firefox and Chrome actually do rather different things,
     * and both say in their comments that they're not really sure what the right approach is. We go with Chrome's
     * behavior (which also experimentally seems to match what IE does), but if you actually want to have a good chance
     * of things working, please avoid double-quotes, newlines, percent signs, and the like in your field names.
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

    public MediaType type() {
        return originalType;
    }

    public String boundary() {
        return boundary.utf8();
    }

    /**
     * The number of parts in this multipart body.
     */
    public int size() {
        return parts.size();
    }

    public List<Part> parts() {
        return parts;
    }

    public Part part(int index) {
        return parts.get(index);
    }

    /**
     * A combination of {@link #type()} and {@link #boundary()}.
     */
    @Override
    public MediaType mediaType() {
        return mediaType;
    }

    @Override
    public long length() throws IOException {
        long result = contentLength;
        if (result != -1L)
            return result;
        return contentLength = writeOrCountBytes(null, true);
    }

    @Override
    public void writeTo(BufferSink sink) throws IOException {
        writeOrCountBytes(sink, false);
    }

    /**
     * 将此请求写入{@code sink}或测量其内容长度。我们有一种方法可以 同时确保计数和内容是一致的，特别是当涉及到一些棘手的操作时， 比如测量报头字符串的编码长度，或者编码整数的位数长度
     *
     * @param sink       缓冲区
     * @param countBytes 统计写入大小
     * @return 当前写入大小信息
     * @throws IOException 异常
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

            MediaType mediaType = body.mediaType();
            if (null != mediaType) {
                sink.writeUtf8(HTTP.CONTENT_TYPE + ": ").writeUtf8(mediaType.toString()).write(CRLF);
            }

            long contentLength = body.length();
            if (contentLength != -1) {
                sink.writeUtf8("Content-Length: ").writeDecimalLong(contentLength).write(CRLF);
            } else if (countBytes) {
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

    public static class Part {

        final Headers headers;
        final RequestBody body;

        private Part(Headers headers, RequestBody body) {
            this.headers = headers;
            this.body = body;
        }

        public static Part create(RequestBody body) {
            return create(null, body);
        }

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

        public static Part createFormData(String name, String value) {
            return createFormData(name, null, RequestBody.create(null, value));
        }

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

        public Headers headers() {
            return headers;
        }

        public RequestBody body() {
            return body;
        }
    }

    public static class Builder {

        private final ByteString boundary;
        private final List<Part> parts = new ArrayList<>();
        private MediaType type = MediaType.MULTIPART_MIXED_TYPE;

        public Builder() {
            this(UUID.randomUUID().toString());
        }

        public Builder(String boundary) {
            this.boundary = ByteString.encodeUtf8(boundary);
        }

        /**
         * Set the MIME type. Expected values for {@code type} are {@link MediaType#MULTIPART_MIXED} (the default),
         * {@link MediaType#MULTIPART_ALTERNATIVE}, {@link MediaType#MULTIPART_DIGEST},
         * {@link MediaType#MULTIPART_PARALLEL} and {@link MediaType#APPLICATION_FORM_URLENCODED}.
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
         * 增加part至请求体
         */
        public Builder addPart(RequestBody body) {
            return addPart(Part.create(body));
        }

        /**
         * 增加part至请求体
         */
        public Builder addPart(Headers headers, RequestBody body) {
            return addPart(Part.create(headers, body));
        }

        /**
         * 将表单数据部分添加到主体中
         */
        public Builder addFormDataPart(String name, String value) {
            return addPart(Part.createFormData(name, value));
        }

        /**
         * 将表单数据部分添加到主体中
         */
        public Builder addFormDataPart(String name, String filename, RequestBody body) {
            return addPart(Part.createFormData(name, filename, body));
        }

        /**
         * 增加part至请求体
         */
        public Builder addPart(Part part) {
            if (part == null)
                throw new NullPointerException("part == null");
            parts.add(part);
            return this;
        }

        /**
         * 将指定的部分组装成请求体
         */
        public MultipartBody build() {
            if (parts.isEmpty()) {
                throw new IllegalStateException("Multipart body must have at least one part.");
            }
            return new MultipartBody(boundary, type, parts);
        }
    }

}
