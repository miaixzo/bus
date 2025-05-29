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
package org.miaixz.bus.core.io.sink;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;

/**
 * 字节流接收器接口，用于将数据写入目标（如网络、存储或内存缓冲区）。
 * 支持分层处理以转换数据（如压缩、加密、节流或协议框架）。
 * 通过 {@link BufferSink#outputStream} 可适配为 {@code OutputStream}。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Sink extends Closeable, Flushable {

    /**
     * 从源读取指定字节数并写入当前接收器。
     *
     * @param source    数据源缓冲区
     * @param byteCount 要读取的字节数
     * @throws IOException 如果读取或写入失败
     */
    void write(Buffer source, long byteCount) throws IOException;

    /**
     * 将缓冲区中的所有数据推送到最终目标。
     *
     * @throws IOException 如果刷新失败
     */
    @Override
    void flush() throws IOException;

    /**
     * 获取接收器的超时配置。
     *
     * @return 超时对象
     */
    Timeout timeout();

    /**
     * 推送缓冲数据到最终目标并释放资源。关闭后不可再次写入，可安全多次关闭。
     *
     * @throws IOException 如果关闭失败
     */
    @Override
    void close() throws IOException;

}