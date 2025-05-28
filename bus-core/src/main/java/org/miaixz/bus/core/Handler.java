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
package org.miaixz.bus.core;

import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * 处理接口，定义任务执行前后的回调方法以及属性配置逻辑
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Handler<T> {

    /**
     * 前置处理回调方法，在任务执行前调用 可用于初始化、验证或预处理操作
     *
     * @param executor 执行器，可能为代理对象
     * @param args     可变参数，传递额外上下文或数据
     * @return 返回 true 表示继续执行任务，返回 false 表示中断执行
     */
    default boolean before(Executor executor, Object... args) {
        // do nothing
        return true;
    }

    /**
     * 后置处理回调方法，在任务执行完成后调用 可用于清理资源、记录日志或后处理操作
     *
     * @param executor 执行器，可能为代理对象
     * @param args     可变参数，传递额外上下文或数据
     * @return 返回 true 表示处理成功，返回 false 表示处理失败
     */
    default boolean after(Executor executor, Object... args) {
        // do nothing
        return true;
    }

    /**
     * 设置属性配置 用于配置处理器的属性或参数
     *
     * @param properties 属性集合，包含配置键值对
     * @return 返回 true 表示配置成功，返回 false 表示配置失败
     */
    default boolean setProperties(Properties properties) {
        // do nothing
        return true;
    }

}