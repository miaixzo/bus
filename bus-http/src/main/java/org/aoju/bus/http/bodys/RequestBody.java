/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.http.bodys;

import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.io.sink.BufferSink;
import org.aoju.bus.core.io.source.Source;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.http.Builder;

import java.io.File;
import java.io.IOException;

/**
 * 内容对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class RequestBody {

    /**
     * 返回传输{@code content}的新请求体。
     * 如果{@code contentType}是非空且缺少字符集，则使用UTF-8
     *
     * @param contentType 请求类型
     * @param content     内容
     * @return 传输请求体
     */
    public static RequestBody create(MediaType contentType, String content) {
        java.nio.charset.Charset charset = Charset.UTF_8;
        if (null != contentType) {
            charset = contentType.charset();
            if (null == charset) {
                charset = Charset.UTF_8;
                contentType = MediaType.valueOf(contentType + "; charset=utf-8");
            }
        }
        byte[] bytes = content.getBytes(charset);
        return create(contentType, bytes);
    }

    /**
     * 返回发送{@code content}的新请求体
     *
     * @param contentType 请求类型
     * @param content     内容
     * @return 传输请求体
     */
    public static RequestBody create(
            final MediaType contentType,
            final ByteString content) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return content.size();
            }

            @Override
            public void writeTo(BufferSink sink) throws IOException {
                sink.write(content);
            }
        };
    }

    /**
     * 发送{@code content}的新请求体
     *
     * @param contentType 请求类型
     * @param content     内容
     * @return 传输请求体
     */
    public static RequestBody create(final MediaType contentType, final byte[] content) {
        return create(contentType, content, 0, content.length);
    }

    /**
     * 发送{@code content}的新请求体
     *
     * @param contentType 请求类型
     * @param content     内容
     * @param offset      偏移量
     * @param byteCount   当前大小
     * @return 传输请求体
     */
    public static RequestBody create(final MediaType contentType, final byte[] content,
                                     final int offset, final int byteCount) {
        if (null == content) {
            throw new NullPointerException("content == null");
        }
        Builder.checkOffsetAndCount(content.length, offset, byteCount);
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return byteCount;
            }

            @Override
            public void writeTo(BufferSink sink) throws IOException {
                sink.write(content, offset, byteCount);
            }
        };
    }

    /**
     * 新的请求体，该请求体传输{@code file}的内容
     *
     * @param contentType 请求类型
     * @param file        文件
     * @return 传输请求体
     */
    public static RequestBody create(final MediaType contentType, final File file) {
        if (null == file) {
            throw new NullPointerException("file == null");
        }

        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return contentType;
            }

            @Override
            public long contentLength() {
                return file.length();
            }

            @Override
            public void writeTo(BufferSink sink) throws IOException {
                try (Source source = IoKit.source(file)) {
                    sink.writeAll(source);
                }
            }
        };
    }

    /**
     * @return 返回此主体的媒体类型
     */
    public abstract MediaType contentType();

    /**
     * 返回调用{@link #writeTo}时写入{@code sink}的字节数，如果该计数未知，则返回-1
     *
     * @return 计数信息
     * @throws IOException 异常
     */
    public long contentLength() throws IOException {
        return -1;
    }

    /**
     * 将此请求的内容写入{@code sink}
     *
     * @param sink 缓存区
     * @throws IOException 异常信息
     */
    public abstract void writeTo(BufferSink sink) throws IOException;

    /**
     * A duplex request body is special in how it is <strong>transmitted</strong> on the network and
     * in the <strong>API contract</strong> between Http and the application.
     * <p>
     * This method returns false unless it is overridden by a subclass.
     * <p>
     * Duplex Transmission
     * <p>
     * With regular HTTP calls the request always completes sending before the response may begin
     * receiving. With duplex the request and response may be interleaved! That is, request body bytes
     * may be sent after response headers or body bytes have been received.
     * <p>
     * Though any call may be initiated as a duplex call, only web servers that are specially
     * designed for this nonstandard interaction will use it. As of 2019-01, the only widely-used
     * implementation of this pattern is <a
     * href="https://github.com/grpc/grpc/blob/master/doc/PROTOCOL-HTTP2.md">gRPC</a>.
     * <p>
     * Because the encoding of interleaved data is not well-defined for HTTP/1, duplex request
     * bodies may only be used with HTTP/2. Calls to HTTP/1 servers will fail before the HTTP request
     * is transmitted. If you cannot ensure that your client and server both support HTTP/2, do not
     * use this feature.
     * <p>
     * Duplex APIs
     * <p>
     * With regular request bodies it is not legal to write bytes to the sink passed to {@link
     * RequestBody#writeTo} after that method returns. For duplex requests bodies that condition is
     * lifted. Such writes occur on an application-provided thread and may occur concurrently with
     * reads of the {@link ResponseBody}. For duplex request bodies, {@link #writeTo} should return
     * quickly, possibly by handing off the provided request body to another thread to perform
     * writing.
     */
    public boolean isDuplex() {
        return false;
    }

    /**
     * Returns true if this body expects at most one call to {@link #writeTo} and can be transmitted
     * at most once. This is typically used when writing the request body is destructive and it is not
     * possible to recreate the request body after it has been sent.
     * <p>
     * This method returns false unless it is overridden by a subclass.
     * <p>
     * By default Http will attempt to retransmit request bodies when the original request fails
     * due to a stale connection, a client timeout (HTTP 408), a satisfied authorization challenge
     * (HTTP 401 and 407), or a retryable server failure (HTTP 503 with a {@code Retry-After: 0}
     * header).
     */
    public boolean isOneShot() {
        return false;
    }
}
