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

import java.io.IOException;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Symbol;

/**
 * 将操作转发给另一个 {@link Sink} 的抽象接收器。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AssignSink implements Sink {

    /**
     * 代理的接收器
     */
    private final Sink delegate;

    /**
     * 构造方法，初始化代理接收器。
     *
     * @param delegate 代理的接收器，不能为 null
     * @throws IllegalArgumentException 如果 delegate 为 null
     */
    public AssignSink(Sink delegate) {
        if (null == delegate) {
            throw new IllegalArgumentException("delegate == null");
        }
        this.delegate = delegate;
    }

    /**
     * 获取代理的接收器。
     *
     * @return 代理的接收器
     */
    public final Sink delegate() {
        return delegate;
    }

    /**
     * 从源缓冲区读取指定字节数并写入代理接收器。
     *
     * @param source    数据源缓冲区
     * @param byteCount 要读取的字节数
     * @throws IOException 如果写入失败
     */
    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        delegate.write(source, byteCount);
    }

    /**
     * 将缓冲数据刷新到代理接收器。
     *
     * @throws IOException 如果刷新失败
     */
    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    /**
     * 获取代理接收器的超时配置。
     *
     * @return 超时对象
     */
    @Override
    public Timeout timeout() {
        return delegate.timeout();
    }

    /**
     * 关闭代理接收器并释放资源。
     *
     * @throws IOException 如果关闭失败
     */
    @Override
    public void close() throws IOException {
        delegate.close();
    }

    /**
     * 返回对象的字符串表示。
     *
     * @return 字符串表示，包含类名和代理接收器的字符串表示
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + Symbol.PARENTHESE_LEFT + delegate.toString() + Symbol.PARENTHESE_RIGHT;
    }

}