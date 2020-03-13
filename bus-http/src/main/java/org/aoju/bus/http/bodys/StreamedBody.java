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

import org.aoju.bus.core.io.Buffer;
import org.aoju.bus.core.io.BufferSink;
import org.aoju.bus.core.io.Pipe;
import org.aoju.bus.core.utils.IoUtils;

import java.io.IOException;

/**
 * 这个请求体通过管道将字节从应用程序线程流到Httpd调度程序线程。
 * 因为数据不是缓冲的，所以只能传输一次
 *
 * @author Kimi Liu
 * @version 5.6.9
 * @since JDK 1.8+
 */
public final class StreamedBody extends OutputStreamBody implements UnrepeatableBody {

    private final Pipe pipe = new Pipe(8192);

    public StreamedBody(long expectedContentLength) {
        initOutputStream(IoUtils.buffer(pipe.sink()), expectedContentLength);
    }

    @Override
    public void writeTo(BufferSink sink) throws IOException {
        Buffer buffer = new Buffer();
        while (pipe.source().read(buffer, 8192) != -1L) {
            sink.write(buffer, buffer.size());
        }
    }

}