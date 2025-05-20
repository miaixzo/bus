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

import java.io.*;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.MediaType;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Builder;

/**
 * HTTP 响应体
 * <p>
 * 表示从源服务器到客户端的响应内容，是一次性流，仅能读取一次。 响应体依赖有限资源（如网络套接字或缓存文件），必须通过关闭释放资源。 支持以字节流、字符流或完整字节数组/字符串形式读取内容，适合处理大型响应。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class ResponseBody implements Closeable {

    /**
     * 字符流读取器
     */
    private Reader reader;

    /**
     * 从字符串创建响应体
     * <p>
     * 如果 <code>mediaType</code> 非空且缺少字符集，则使用 UTF-8。
     * </p>
     *
     * @param mediaType 媒体类型（可能为 null）
     * @param content   内容字符串
     * @return 响应体实例
     */
    public static ResponseBody create(MediaType mediaType, String content) {
        java.nio.charset.Charset charset = Charset.UTF_8;
        if (mediaType != null) {
            charset = mediaType.charset();
            if (charset == null) {
                charset = Charset.UTF_8;
                mediaType = MediaType.valueOf(mediaType + "; charset=utf-8");
            }
        }
        Buffer buffer = new Buffer().writeString(content, charset);
        return create(mediaType, buffer.size(), buffer);
    }

    /**
     * 从字节数组创建响应体
     *
     * @param mediaType 媒体类型（可能为 null）
     * @param content   内容字节数组
     * @return 响应体实例
     */
    public static ResponseBody create(final MediaType mediaType, byte[] content) {
        Buffer buffer = new Buffer().write(content);
        return create(mediaType, content.length, buffer);
    }

    /**
     * 从 ByteString 创建响应体
     *
     * @param mediaType 媒体类型（可能为 null）
     * @param content   内容 ByteString
     * @return 响应体实例
     */
    public static ResponseBody create(MediaType mediaType, ByteString content) {
        Buffer buffer = new Buffer().write(content);
        return create(mediaType, content.size(), buffer);
    }

    /**
     * 从数据源创建响应体
     *
     * @param mediaType 媒体类型（可能为 null）
     * @param length    内容长度
     * @param content   数据源
     * @return 响应体实例
     * @throws NullPointerException 如果 content 为 null
     */
    public static ResponseBody create(final MediaType mediaType, final long length, final BufferSource content) {
        if (null == content) {
            throw new NullPointerException("source == null");
        }

        return new ResponseBody() {
            @Override
            public MediaType mediaType() {
                return mediaType;
            }

            @Override
            public long length() {
                return length;
            }

            @Override
            public BufferSource source() {
                return content;
            }
        };
    }

    /**
     * 获取媒体类型
     *
     * @return 媒体类型（可能为 null）
     */
    public abstract MediaType mediaType();

    /**
     * 获取内容长度
     * <p>
     * 返回响应的字节数，未知时返回 -1。
     * </p>
     *
     * @return 内容长度
     */
    public abstract long length();

    /**
     * 获取字节流
     *
     * @return 输入流
     */
    public final InputStream byteStream() {
        return source().inputStream();
    }

    /**
     * 获取数据源
     *
     * @return 数据源
     */
    public abstract BufferSource source();

    /**
     * 获取响应内容的字节数组
     * <p>
     * 将整个响应体加载到内存中，适合小型响应。 对于大型响应可能引发 {@link OutOfMemoryError}，建议使用流式读取。
     * </p>
     *
     * @return 字节数组
     * @throws IOException 如果读取失败或长度不匹配
     */
    public final byte[] bytes() throws IOException {
        long contentLength = length();
        if (contentLength > Integer.MAX_VALUE) {
            throw new IOException("Cannot buffer entire body for content length: " + contentLength);
        }

        byte[] bytes;
        try (BufferSource source = source()) {
            bytes = source.readByteArray();
        }
        if (contentLength != -1 && contentLength != bytes.length) {
            throw new IOException(
                    "Content-Length (" + contentLength + ") and stream length (" + bytes.length + ") disagree");
        }
        return bytes;
    }

    /**
     * 获取字符流
     * <p>
     * 自动处理字节顺序标记（BOM）或 Content-Type 指定的字符集，默认使用 UTF-8。 多次调用返回同一实例。
     * </p>
     *
     * @return 字符流读取器
     */
    public final Reader charStream() {
        Reader r = reader;
        return null != r ? r : (reader = new BomAwareReader(source(), charset()));
    }

    /**
     * 获取响应内容的字符串
     * <p>
     * 将整个响应体加载到内存中，适合小型响应。 自动处理字节顺序标记（BOM）或 Content-Type 指定的字符集，默认使用 UTF-8。 对于大型响应可能引发
     * {@link OutOfMemoryError}，建议使用流式读取。
     * </p>
     *
     * @return 字符串
     * @throws IOException 如果读取失败
     */
    public final String string() throws IOException {
        try (BufferSource source = source()) {
            java.nio.charset.Charset charset = Builder.bomAwareCharset(source, charset());
            return source.readString(charset);
        }
    }

    /**
     * 获取字符集
     *
     * @return 字符集（默认 UTF-8）
     */
    private java.nio.charset.Charset charset() {
        MediaType mediaType = mediaType();
        return null != mediaType ? mediaType.charset(Charset.UTF_8) : Charset.UTF_8;
    }

    /**
     * 关闭响应体
     * <p>
     * 释放关联的资源（如网络套接字或缓存文件）。
     * </p>
     */
    @Override
    public void close() {
        IoKit.close(source());
    }

    /**
     * 支持 BOM 的字符流读取器
     */
    static class BomAwareReader extends Reader {

        /**
         * 数据源
         */
        private final BufferSource source;
        /**
         * 字符集
         */
        private final java.nio.charset.Charset charset;
        /**
         * 是否已关闭
         */
        private boolean closed;
        /**
         * 委托读取器
         */
        private Reader delegate;

        /**
         * 构造函数
         *
         * @param source  数据源
         * @param charset 字符集
         */
        BomAwareReader(BufferSource source, java.nio.charset.Charset charset) {
            this.source = source;
            this.charset = charset;
        }

        /**
         * 读取字符
         *
         * @param cbuf 字符缓冲区
         * @param off  偏移量
         * @param len  读取长度
         * @return 读取的字符数
         * @throws IOException 如果流已关闭或读取失败
         */
        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (closed)
                throw new IOException("Stream closed");

            Reader delegate = this.delegate;
            if (null == delegate) {
                java.nio.charset.Charset charset = Builder.bomAwareCharset(source, this.charset);
                delegate = this.delegate = new InputStreamReader(source.inputStream(), charset);
            }
            return delegate.read(cbuf, off, len);
        }

        /**
         * 关闭读取器
         *
         * @throws IOException 如果关闭失败
         */
        @Override
        public void close() throws IOException {
            closed = true;
            if (null != delegate) {
                delegate.close();
            } else {
                source.close();
            }
        }
    }

}