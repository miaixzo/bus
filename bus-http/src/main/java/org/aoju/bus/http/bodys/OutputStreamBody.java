/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.http.bodys;

import org.aoju.bus.core.io.BufferSink;
import org.aoju.bus.core.io.Timeout;
import org.aoju.bus.core.lang.MediaType;
import org.aoju.bus.http.Request;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;

/**
 * 通过阻塞写入输出流来填充的请求主体。输出数据要么是完全缓冲的(使用{@link BufferedBody})，
 * 要么是流的(使用{@link StreamedBody})。在这两种情况下，直到调用者将正文的字节写入输出流时，
 * 正文的字节才会被知道
 *
 * @author Kimi Liu
 * @version 5.8.0
 * @since JDK 1.8+
 */
public abstract class OutputStreamBody extends RequestBody {

    boolean closed;
    private Timeout timeout;
    private long expectedContentLength;
    private OutputStream outputStream;

    protected void initOutputStream(final BufferSink sink, final long expectedContentLength) {
        this.timeout = sink.timeout();
        this.expectedContentLength = expectedContentLength;
        this.outputStream = new OutputStream() {
            private long bytesReceived;

            @Override
            public void write(int b) throws IOException {
                write(new byte[]{(byte) b}, 0, 1);
            }

            @Override
            public void write(byte[] source, int offset, int byteCount) throws IOException {
                if (closed) throw new IOException("closed"); // Not IllegalStateException!

                if (expectedContentLength != -1L && bytesReceived + byteCount > expectedContentLength) {
                    throw new ProtocolException("expected " + expectedContentLength
                            + " bytes but received " + bytesReceived + byteCount);
                }

                bytesReceived += byteCount;
                try {
                    sink.write(source, offset, byteCount);
                } catch (InterruptedIOException e) {
                    throw new SocketTimeoutException(e.getMessage());
                }
            }

            @Override
            public void flush() throws IOException {
                if (closed) {
                    return;
                }
                sink.flush();
            }

            @Override
            public void close() throws IOException {
                closed = true;

                if (expectedContentLength != -1L && bytesReceived < expectedContentLength) {
                    throw new ProtocolException("expected " + expectedContentLength
                            + " bytes but received " + bytesReceived);
                }

                sink.close();
            }
        };
    }

    public final OutputStream outputStream() {
        return outputStream;
    }

    public final Timeout timeout() {
        return timeout;
    }

    public final boolean isClosed() {
        return closed;
    }

    @Override
    public long contentLength() {
        return expectedContentLength;
    }

    @Override
    public final MediaType contentType() {
        return null;
    }

    /**
     * 现在我们已经缓冲了整个请求体，更新请求头和请求体本身。
     * 这是为了使HttpURLConnection用户能够在发送请求体字节之前完成套接字连接
     *
     * @param request 网络请求
     * @return 请求
     * @throws IOException 异常
     */
    public Request prepareToSendRequest(Request request) throws IOException {
        return request;
    }

}