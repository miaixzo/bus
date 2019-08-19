/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.core.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * 提供一个字节流。使用此接口从任何地方读取数据
 * 它的位置:来自网络、存储或内存中的缓冲区。来源可能
 * 分层以转换提供的数据，例如解压、解密或
 * 移除协议框架。
 *
 * <h3>Interop with InputStream</h3>
 * {@link BufferedSource#inputStream} to adapt a source to an {@code
 * InputStream}.
 *
 * @author Kimi Liu
 * @version 3.1.0
 * @since JDK 1.8
 */
public interface Source extends Closeable {

    /**
     * Removes at least 1, and up to {@code byteCount} bytes from this and appends
     * them to {@code sink}. Returns the number of bytes read, or -1 if this
     * source is exhausted.
     *
     * @param sink      Buffer
     * @param byteCount long
     * @return the long
     * @throws IOException {@link java.io.IOException} IOException.
     */
    long read(Buffer sink, long byteCount) throws IOException;

    /**
     * Returns the timeout for this source.
     *
     * @return the Timeout
     */
    Timeout timeout();

    /**
     * Closes this source and releases the resources held by this source. It is an
     * error to read a closed source. It is safe to close a source more than once.
     *
     * @throws IOException {@link java.io.IOException} IOException.
     */
    @Override
    void close() throws IOException;

}
