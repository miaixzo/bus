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
package org.miaixz.bus.core.center.function;

import java.io.Serializable;
import java.util.stream.Stream;

import org.miaixz.bus.core.xyz.ExceptionKit;

/**
 * 可序列化的Runnable接口，支持异常抛出和多个Runnable组合。
 *
 * @author Kimi Liu
 * @see Runnable
 * @since Java 17+
 */
@FunctionalInterface
public interface RunnableX extends Runnable, Serializable {

    /**
     * 组合多个RunnableX实例，按顺序执行。
     *
     * @param serRunnableArray 要组合的RunnableX实例
     * @return 组合后的RunnableX实例
     */
    static RunnableX multi(final RunnableX... serRunnableArray) {
        return () -> Stream.of(serRunnableArray).forEach(RunnableX::run);
    }

    /**
     * 执行Runnable操作，可能抛出异常。
     *
     * @throws Throwable 可能抛出的异常
     * @see Thread#run()
     */
    void running() throws Throwable;

    /**
     * 执行Runnable操作，自动处理异常。
     *
     * @throws RuntimeException 包装后的运行时异常
     * @see Thread#run()
     */
    @Override
    default void run() {
        try {
            running();
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

}