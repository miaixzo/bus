/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.bodys;

import org.aoju.bus.core.io.segment.Buffer;
import org.aoju.bus.core.io.segment.BufferSource;
import org.aoju.bus.core.io.segment.ByteString;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.http.Builder;
import org.aoju.bus.http.Callback;
import org.aoju.bus.http.NewCall;
import org.aoju.bus.http.Response;

import java.io.*;
import java.nio.charset.Charset;

/**
 * 从源服务器到客户机应用程序的一次性流，包含响应主体的原始字节。 到web服务器的活动连接支持每个响应主体。
 * 这对客户机应用程序施加了义务和限制，每个响应主体由一个有限的资源(如socket(实时网络响应)或一个打开的
 * 文件(用于缓存的响应)来支持。如果不关闭响应体，将会泄漏资源并减慢或崩溃
 * 这个类和{@link Response}都实现了{@link Closeable}。关闭一个响应就是关闭它的响应体。如果您
 * 调用{@link NewCall#execute()}或实现{@link Callback#onResponse}，则必须通过
 * 调用以下任何方法来关闭此主体:
 * <ul>
 *   <li>Response.close()</li>
 *   <li>Response.body().close()</li>
 *   <li>Response.body().source().close()</li>
 *   <li>Response.body().charStream().close()</li>
 *   <li>Response.body().byteStream().close()</li>
 *   <li>Response.body().bytes()</li>
 *   <li>Response.body().string()</li>
 * </ul>
 * 这个类可以用来传输非常大的响应。例如，可以使用这个类来读取大于分配给当前进程的整个内存的响应。
 * 它甚至可以传输大于当前设备总存储的响应，这是视频流应用程序的一个常见需求
 * 因为这个类不会在内存中缓冲完整的响应，所以应用程序可能不会重新读取响应的字节。使用{@link #bytes()}
 * 或{@link #string()}将整个响应读入内存。或者使用{@link #source()}、{@link #byteStream()}
 * 或{@link #charStream()}来处理响应
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8+
 */
public abstract class ResponseBody implements Closeable {
    /**
     * 多次调用{@link #charStream()}必须返回相同的实例.
     */
    private Reader reader;

    /**
     * 返回一个传输{@code content}的新响应体。如果{@code contentType}是非空且缺少字符集，则使用UTF-8
     *
     * @param contentType 媒体类型
     * @param content     内容
     * @return 新响应体
     */
    public static ResponseBody create(MediaType contentType, String content) {
        Charset charset = org.aoju.bus.core.lang.Charset.UTF_8;
        if (contentType != null) {
            charset = contentType.charset();
            if (charset == null) {
                charset = org.aoju.bus.core.lang.Charset.UTF_8;
                contentType = MediaType.valueOf(contentType + "; charset=utf-8");
            }
        }
        Buffer buffer = new Buffer().writeString(content, charset);
        return create(contentType, buffer.size(), buffer);
    }

    /**
     * 新的响应体，它传输{@code content}
     *
     * @param contentType 媒体类型
     * @param content     内容
     * @return 新响应体
     */
    public static ResponseBody create(final MediaType contentType, byte[] content) {
        Buffer buffer = new Buffer().write(content);
        return create(contentType, content.length, buffer);
    }

    /**
     * 新的响应体，它传输{@code content}
     *
     * @param contentType 媒体类型
     * @param content     内容
     * @return 新响应体
     */
    public static ResponseBody create(MediaType contentType, ByteString content) {
        Buffer buffer = new Buffer().write(content);
        return create(contentType, content.size(), buffer);
    }

    /**
     * 新的响应体，它传输{@code content}
     *
     * @param contentType   媒体类型
     * @param contentLength 内容大小
     * @param content       内容
     * @return 新响应体
     */
    public static ResponseBody create(final MediaType contentType,
                                      final long contentLength,
                                      final BufferSource content) {
        if (content == null) throw new NullPointerException("source == null");
        return new ResponseBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return contentLength;
            }

            @Override
            public BufferSource source() {
                return content;
            }
        };
    }

    public abstract MediaType contentType();

    public abstract long contentLength();

    public final InputStream byteStream() {
        return source().inputStream();
    }

    public abstract BufferSource source();

    public final byte[] bytes() throws IOException {
        long contentLength = contentLength();
        if (contentLength > Integer.MAX_VALUE) {
            throw new IOException("Cannot buffer entire body for content length: " + contentLength);
        }

        BufferSource source = source();
        byte[] bytes;
        try {
            bytes = source.readByteArray();
        } finally {
            IoUtils.close(source);
        }
        if (contentLength != -1 && contentLength != bytes.length) {
            throw new IOException("Content-Length (" + contentLength + ") and stream length (" + bytes.length + ") disagree");
        }
        return bytes;
    }

    public final Reader charStream() {
        Reader r = reader;
        return r != null ? r : (reader = new BomAwareReader(source(), charset()));
    }

    public final String string() throws IOException {
        BufferSource source = source();
        try {
            Charset charset = Builder.bomAwareCharset(source, charset());
            return source.readString(charset);
        } finally {
            IoUtils.close(source);
        }
    }

    private Charset charset() {
        MediaType contentType = contentType();
        return contentType != null ? contentType.charset(org.aoju.bus.core.lang.Charset.UTF_8) : org.aoju.bus.core.lang.Charset.UTF_8;
    }

    @Override
    public void close() {
        IoUtils.close(source());
    }

    static final class BomAwareReader extends Reader {
        private final BufferSource source;
        private final Charset charset;

        private boolean closed;
        private Reader delegate;

        BomAwareReader(BufferSource source, Charset charset) {
            this.source = source;
            this.charset = charset;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (closed) throw new IOException("Stream closed");

            Reader delegate = this.delegate;
            if (delegate == null) {
                Charset charset = Builder.bomAwareCharset(source, this.charset);
                delegate = this.delegate = new InputStreamReader(source.inputStream(), charset);
            }
            return delegate.read(cbuf, off, len);
        }

        @Override
        public void close() throws IOException {
            closed = true;
            if (delegate != null) {
                delegate.close();
            } else {
                source.close();
            }
        }
    }

}
