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
package org.aoju.bus.core.io.segment;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * 接收一个字节流。使用这个接口可以在任何地方编写数据
 * 需要:到网络、存储器或内存中的缓冲区。水槽可以分层
 * 转换接收到的数据，如压缩、加密、节流或添加
 * 协议框架。
 *
 *
 * <h3>Interop with OutputStream</h3>
 * {@link BufferSink#outputStream} to adapt a sink to an {@code OutputStream}.
 *
 * @author Kimi Liu
 * @version 5.0.2
 * @since JDK 1.8+
 */
public interface Sink extends Closeable, Flushable {

    @Override
    void flush() throws IOException;

    @Override
    void close() throws IOException;

    Timeout timeout();

    void write(Buffer source, long byteCount) throws IOException;

}
