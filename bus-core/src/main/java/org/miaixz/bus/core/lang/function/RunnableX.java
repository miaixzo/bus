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
package org.miaixz.bus.core.lang.function;

import org.miaixz.bus.core.exception.InternalException;

import java.io.Serializable;
import java.util.stream.Stream;

/**
 * 该接口旨在为希望在活动时执行代码的对象提供通用协议
 * 例如，Runnable 是由类 Thread 实现的，处于活动状态仅意味着线程已启动且尚未停止
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@FunctionalInterface
public interface RunnableX extends Runnable, Serializable {

    /**
     * 执行参数操作
     *
     * @param args 参数信息
     * @return the object
     */
    static RunnableX multi(final RunnableX... args) {
        return () -> Stream.of(args).forEach(RunnableX::run);
    }

    /**
     * 当使用实现接口 <code>Runnable</code> 的对象来创建线程时，
     * 启动线程会导致在该单独执行的线程中调用对象的 <code>run</code> 方法
     *
     * @throws Exception 包装的检查异常
     */
    void running() throws Exception;

    /**
     * 当使用实现接口 <code>Runnable</code> 的对象来创建线程时，
     * 启动线程会导致在该单独执行的线程中调用对象的 <code>run</code> 方法
     */
    @Override
    default void run() {
        try {
            running();
        } catch (final Exception e) {
            throw new InternalException(e);
        }
    }

}
