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
 * MIME Multipart 请求体
 * <p>
 * 表示 MIME <code>multipart/related</code> 类型的复合请求体，用于上传多个部分（如文件、表单数据）。 每个部分由分隔符（boundary）分隔，支持自定义头部和内容类型。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MultipartBody extends RequestBody {

    /**
     * 冒号空格分隔符
     */
    private static final byte[] COLONSPACE = { Symbol.C_COLON, Symbol.C_SPACE };
    /**
     * 换行符
     */
    private static final byte[] CRLF = { Symbol.C_CR, Symbol.C_LF };
    /**
     * 双破折号
     */
    private static final byte[] DASHDASH = { Symbol.C_MINUS, Symbol.C_MINUS };
    /**
     * 分隔符
     */
    private final ByteString boundary;
    /**
     * 原始媒体类型
     */
    private final MediaType originalType;
    /**
     * 完整媒体类型（包含分隔符）
     */
    private final MediaType mediaType;
    /**
     * 部分列表
     */
    private final List<Part> parts;
    /**
     * 内容长度
     */
    private long contentLength = -1L;

    /**
     * 构造函数，初始化 MultipartBody 实例
     *
     * @param boundary  分隔符
     * @param mediaType 媒体类型
     * @param parts     部分列表
     */
    MultipartBody(ByteString boundary, MediaType mediaType, List<Part> parts) {
        this.boundary = boundary;
        this.originalType = mediaType;
        this.mediaType = MediaType.valueOf(mediaType.toString() + "; boundary=" + boundary.utf8());
        this.parts = org.miaixz.bus.http.Builder.immutableList(parts);
    }

    /**
     * 追加引号字符串
     * <p>
     * 将键值追加到 StringBuilder，处理特殊字符（如换行、引号）的转义。 建议避免在字段名称中使用双引号、换行符或百分号等字符。
     * </p>
     *
     * @param target 目标 StringBuilder
     * @param key    键值
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
     * 获取原始媒体类型
     *
     * @return 原始媒体类型
     */
    public MediaType type() {
        return originalType;
    }

    /**
     * 获取分隔符
     *
     * @return 分隔符字符串
     */
    public String boundary() {
        return boundary.utf8();
    }

    /**
     * 获取部分数量
     *
     * @return 部分数量
     */
    public int size() {
        return parts.size();
    }

    /**
     * 获取部分列表
     *
     * @return 不可修改的部分列表
     */
    public List<Part> parts() {
        return parts;
    }

    /**
     * 获取指定索引的部分
     *
     * @param index 索引
     * @return 部分实例
     */
    public Part part(int index) {
        return parts.get(index);
    }

    /**
     * 获取完整媒体类型
     *
     * @return 包含分隔符的媒体类型
     */
    @Override
    public MediaType mediaType() {
        return mediaType;
    }

    /**
     * 获取请求体长度
     *
     * @return 请求体字节长度
     * @throws IOException 如果无法确定长度
     */
    @Override
    public long length() throws IOException {
        long result = contentLength;
        if (result != -1L)
            return result;
        return contentLength = writeOrCountBytes(null, true);
    }

    /**
     * 将请求体写入输出流
     *
     * @param sink 输出流
     * @throws IOException 如果写入失败
     */
    @Override
    public void writeTo(BufferSink sink) throws IOException {
        writeOrCountBytes(sink, false);
    }

    /**
     * 写入或计算请求体字节
     * <p>
     * 用于写入请求体到输出流或计算其字节长度，确保内容一致性。
     * </p>
     *
     * @param sink       输出流（计算长度时为 null）
     * @param countBytes 是否仅计算字节数
     * @return 字节长度（无法确定时为 -1）
     * @throws IOException 如果写入失败
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

    /**
     * MultipartBody 部分
     */
    public static class Part {

        /** 部分头部 */
        final Headers headers;
        /** 部分请求体 */
        final RequestBody body;

        /**
         * 构造函数，初始化 Part 实例
         *
         * @param headers 头部
         * @param body    请求体
         */
        private Part(Headers headers, RequestBody body) {
            this.headers = headers;
            this.body = body;
        }

        /**
         * 创建 Part 实例（无头部）
         *
         * @param body 请求体
         * @return Part 实例
         * @throws NullPointerException 如果 body 为 null
         */
        public static Part create(RequestBody body) {
            return create(null, body);
        }

        /**
         * 创建 Part 实例
         *
         * @param headers 头部
         * @param body    请求体
         * @return Part 实例
         * @throws NullPointerException     如果 body 为 null
         * @throws IllegalArgumentException 如果 headers 包含 Content-Type 或 Content-Length
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
         * 创建表单数据 Part
         *
         * @param name  字段名称
         * @param value 字段值
         * @return Part 实例
         * @throws NullPointerException 如果 name 为 null
         */
        public static Part createFormData(String name, String value) {
            return createFormData(name, null, RequestBody.create(null, value));
        }

        /**
         * 创建表单数据 Part（带文件名）
         *
         * @param name     字段名称
         * @param filename 文件名
         * @param body     请求体
         * @return Part 实例
         * @throws NullPointerException 如果 name 为 null
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
         * 获取部分头部
         *
         * @return 头部（可能为 null）
         */
        public Headers headers() {
            return headers;
        }

        /**
         * 获取部分请求体
         *
         * @return 请求体
         */
        public RequestBody body() {
            return body;
        }
    }

    /**
     * MultipartBody 构建器
     */
    public static class Builder {

        /**
         * 分隔符
         */
        private final ByteString boundary;
        /**
         * 部分列表
         */
        private final List<Part> parts = new ArrayList<>();
        /**
         * 媒体类型
         */
        private MediaType type = MediaType.MULTIPART_MIXED_TYPE;

        /**
         * 默认构造函数
         * <p>
         * 使用随机 UUID 作为分隔符。
         * </p>
         */
        public Builder() {
            this(UUID.randomUUID().toString());
        }

        /**
         * 构造函数，指定分隔符
         *
         * @param boundary 分隔符
         */
        public Builder(String boundary) {
            this.boundary = ByteString.encodeUtf8(boundary);
        }

        /**
         * 设置媒体类型
         * <p>
         * 支持的类型包括 {@link MediaType#MULTIPART_MIXED}（默认）、
         * {@link MediaType#MULTIPART_ALTERNATIVE}、{@link MediaType#MULTIPART_DIGEST}、
         * {@link MediaType#MULTIPART_PARALLEL} 和 {@link MediaType#APPLICATION_FORM_URLENCODED}。
         * </p>
         *
         * @param type 媒体类型
         * @return 当前 Builder 实例
         * @throws NullPointerException     如果 type 为 null
         * @throws IllegalArgumentException 如果 type 不是 multipart 类型
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
         * 添加部分（无头部）
         *
         * @param body 请求体
         * @return 当前 Builder 实例
         */
        public Builder addPart(RequestBody body) {
            return addPart(Part.create(body));
        }

        /**
         * 添加部分
         *
         * @param headers 头部
         * @param body    请求体
         * @return 当前 Builder 实例
         */
        public Builder addPart(Headers headers, RequestBody body) {
            return addPart(Part.create(headers, body));
        }

        /**
         * 添加表单数据部分
         *
         * @param name  字段名称
         * @param value 字段值
         * @return 当前 Builder 实例
         */
        public Builder addFormDataPart(String name, String value) {
            return addPart(Part.createFormData(name, value));
        }

        /**
         * 添加表单数据部分（带文件名）
         *
         * @param name     字段名称
         * @param filename 文件名
         * @param body     请求体
         * @return 当前 Builder 实例
         */
        public Builder addFormDataPart(String name, String filename, RequestBody body) {
            return addPart(Part.createFormData(name, filename, body));
        }

        /**
         * 添加部分
         *
         * @param part 部分实例
         * @return 当前 Builder 实例
         * @throws NullPointerException 如果 part 为 null
         */
        public Builder addPart(Part part) {
            if (part == null)
                throw new NullPointerException("part == null");
            parts.add(part);
            return this;
        }

        /**
         * 构建 MultipartBody 实例
         *
         * @return MultipartBody 实例
         * @throws IllegalStateException 如果没有添加任何部分
         */
        public MultipartBody build() {
            if (parts.isEmpty()) {
                throw new IllegalStateException("Multipart body must have at least one part.");
            }
            return new MultipartBody(boundary, type, parts);
        }
    }

}