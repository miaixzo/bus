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
import org.miaixz.bus.http.Callback;
import org.miaixz.bus.http.NewCall;
import org.miaixz.bus.http.Response;

/**
 * 从源服务器到客户机应用程序的一次性流，包含响应主体的原始字节。 到web服务器的活动连接支持每个响应主体。 这对客户机应用程序施加了义务和限制，每个响应主体由一个有限的资源(如socket(实时网络响应)或一个打开的
 * 文件(用于缓存的响应)来支持。如果不关闭响应体，将会泄漏资源并减慢或崩溃 这个类和{@link Response}都实现了{@link Closeable}。关闭一个响应就是关闭它的响应体。如果您
 * 调用{@link NewCall#execute()}或实现{@link Callback#onResponse}，则必须通过 调用以下任何方法来关闭此主体:
 * <ul>
 * <li>Response.close()</li>
 * <li>Response.body().close()</li>
 * <li>Response.body().source().close()</li>
 * <li>Response.body().charStream().close()</li>
 * <li>Response.body().byteStream().close()</li>
 * <li>Response.body().bytes()</li>
 * <li>Response.body().string()</li>
 * </ul>
 * 这个类可以用来传输非常大的响应。例如，可以使用这个类来读取大于分配给当前进程的整个内存的响应。 它甚至可以传输大于当前设备总存储的响应，这是视频流应用程序的一个常见需求
 * 因为这个类不会在内存中缓冲完整的响应，所以应用程序可能不会重新读取响应的字节。使用{@link #bytes()}
 * 或{@link #string()}将整个响应读入内存。或者使用{@link #source()}、{@link #byteStream()} 或{@link #charStream()}来处理响应
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class ResponseBody implements Closeable {
    /**
     * 多次调用{@link #charStream()}必须返回相同的实例.
     */
    private Reader reader;

    /**
     * 返回一个传输{@code content}的新响应体。如果{@code mediaType}是非空且缺少字符集，则使用UTF-8
     *
     * @param mediaType 媒体类型
     * @param content   内容
     * @return 新响应体
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
     * 新的响应体，它传输{@code content}
     *
     * @param mediaType 媒体类型
     * @param content   内容
     * @return 新响应体
     */
    public static ResponseBody create(final MediaType mediaType, byte[] content) {
        Buffer buffer = new Buffer().write(content);
        return create(mediaType, content.length, buffer);
    }

    /**
     * 新的响应体，它传输{@code content}
     *
     * @param mediaType 媒体类型
     * @param content   内容
     * @return 新响应体
     */
    public static ResponseBody create(MediaType mediaType, ByteString content) {
        Buffer buffer = new Buffer().write(content);
        return create(mediaType, content.size(), buffer);
    }

    /**
     * 新的响应体，它传输{@code content}
     *
     * @param mediaType 媒体类型
     * @param length    内容大小
     * @param content   内容
     * @return 新响应体
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

    public abstract MediaType mediaType();

    /**
     * Returns the number of bytes in that will returned by {@link #bytes}, or {@link #byteStream}, or -1 if unknown.
     */
    public abstract long length();

    public final InputStream byteStream() {
        return source().inputStream();
    }

    public abstract BufferSource source();

    /**
     * Returns the response as a byte array. This method loads entire response body into memory. If the response body is
     * very large this may trigger an {@link OutOfMemoryError}. Prefer to stream the response body if this is a
     * possibility for your response.
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
     * Returns the response as a character stream. If the response starts with a ByteOrder Mark (BOM), it is consumed
     * and used to determine the charset of the response bytes. Otherwise if the response has a Content-Type header that
     * specifies a charset, that is used to determine the charset of the response bytes. Otherwise the response bytes
     * are decoded as UTF-8.
     */
    public final Reader charStream() {
        Reader r = reader;
        return null != r ? r : (reader = new BomAwareReader(source(), charset()));
    }

    /**
     * Returns the response as a string. If the response starts with a ByteOrder Mark (BOM), it is consumed and used to
     * determine the charset of the response bytes. Otherwise if the response has a Content-Type header that specifies a
     * charset, that is used to determine the charset of the response bytes. Otherwise the response bytes are decoded as
     * UTF-8. This method loads entire response body into memory. If the response body is very large this may trigger an
     * {@link OutOfMemoryError}. Prefer to stream the response body if this is a possibility for your response.
     */
    public final String string() throws IOException {
        try (BufferSource source = source()) {
            java.nio.charset.Charset charset = Builder.bomAwareCharset(source, charset());
            return source.readString(charset);
        }
    }

    private java.nio.charset.Charset charset() {
        MediaType mediaType = mediaType();
        return null != mediaType ? mediaType.charset(Charset.UTF_8) : Charset.UTF_8;
    }

    @Override
    public void close() {
        IoKit.close(source());
    }

    static class BomAwareReader extends Reader {

        private final BufferSource source;
        private final java.nio.charset.Charset charset;

        private boolean closed;
        private Reader delegate;

        BomAwareReader(BufferSource source, java.nio.charset.Charset charset) {
            this.source = source;
            this.charset = charset;
        }

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
