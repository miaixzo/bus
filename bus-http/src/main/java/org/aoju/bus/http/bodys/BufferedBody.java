/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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

import org.aoju.bus.core.io.Buffer;
import org.aoju.bus.core.io.BufferSink;
import org.aoju.bus.http.Request;

import java.io.IOException;

/**
 * 此请求主体仅涉及应用程序线程。首先将所有字节写入缓冲区。
 * 只有完成之后，字节才会被复制到网络中
 *
 * @author Kimi Liu
 * @version 6.3.0
 * @since JDK 1.8+
 */
public final class BufferedBody extends OutputStreamBody {

    final Buffer buffer = new Buffer();
    long contentLength = -1L;

    public BufferedBody(long expectedContentLength) {
        initOutputStream(buffer, expectedContentLength);
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    /**
     * 现在我们已经缓冲了整个请求体，更新请求头和请求体本身。
     * 这是为了使HttpURLConnection用户能够在发送请求体字节之前完成套接字连接
     *
     * @param request 网络请求
     * @return 请求
     * @throws IOException 异常
     */
    @Override
    public Request prepareToSendRequest(Request request) throws IOException {
        if (null != request.header("Content-Length")) {
            return request;
        }
        outputStream().close();
        contentLength = buffer.size();
        return request.newBuilder()
                .removeHeader("Transfer-Encoding")
                .header("Content-Length", Long.toString(buffer.size()))
                .build();
    }

    @Override
    public void writeTo(BufferSink sink) {
        buffer.copyTo(sink.buffer(), 0, buffer.size());
    }

}