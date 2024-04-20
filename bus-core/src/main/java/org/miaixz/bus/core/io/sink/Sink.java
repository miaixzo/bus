/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.io.sink;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * 接收一个字节流 使用这个接口可以在任何地方编写数据
 * 需要:到网络、存储器或内存中的缓冲区 水槽可以分层
 * 转换接收到的数据,如压缩、加密、节流或添加协议框架
 * {@link BufferSink#outputStream}使一个接收器适应{@code outputStream}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Sink extends Closeable, Flushable {

    /**
     * Removes {@code byteCount} bytes from {@code source} and appends them to this.
     */
    void write(Buffer source, long byteCount) throws IOException;

    /**
     * Pushes all buffered bytes to their final destination.
     */
    @Override
    void flush() throws IOException;

    /**
     * Returns the timeout for this sink.
     *
     * @return the {@link  Timeout}
     */
    Timeout timeout();

    /**
     * Pushes all buffered bytes to their final destination and releases the
     * resources held by this sink. It is an error to write a closed sink. It is
     * safe to close a sink more than once.
     */
    @Override
    void close() throws IOException;

}
