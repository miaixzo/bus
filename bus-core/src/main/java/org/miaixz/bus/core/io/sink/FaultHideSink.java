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

/**
 * 容错接收器，捕获底层接收器的 IOException 并标记错误状态，避免抛出异常。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FaultHideSink extends AssignSink {

    /**
     * 是否发生错误
     */
    private boolean hasErrors;

    /**
     * 构造方法，初始化代理接收器。
     *
     * @param delegate 代理的接收器
     */
    public FaultHideSink(Sink delegate) {
        super(delegate);
    }

    /**
     * 写入数据到代理接收器，若发生错误则标记并跳过数据。
     *
     * @param source    数据源缓冲区
     * @param byteCount 要写入的字节数
     * @throws IOException 如果写入失败（仅在无错误时抛出）
     */
    @Override
    public void write(Buffer source, long byteCount) throws IOException {
        if (hasErrors) {
            source.skip(byteCount);
            return;
        }
        try {
            super.write(source, byteCount);
        } catch (IOException e) {
            hasErrors = true;
            onException(e);
        }
    }

    /**
     * 刷新代理接收器，若发生错误则忽略。
     *
     * @throws IOException 如果刷新失败（仅在无错误时抛出）
     */
    @Override
    public void flush() throws IOException {
        if (hasErrors)
            return;
        try {
            super.flush();
        } catch (IOException e) {
            hasErrors = true;
            onException(e);
        }
    }

    /**
     * 关闭代理接收器，若发生错误则忽略。
     *
     * @throws IOException 如果关闭失败（仅在无错误时抛出）
     */
    @Override
    public void close() throws IOException {
        if (hasErrors)
            return;
        try {
            super.close();
        } catch (IOException e) {
            hasErrors = true;
            onException(e);
        }
    }

    /**
     * 处理异常的钩子方法，子类可重写以自定义行为。
     *
     * @param e 发生的异常
     */
    protected void onException(IOException e) {

    }

}