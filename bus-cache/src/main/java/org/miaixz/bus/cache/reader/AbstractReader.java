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
package org.miaixz.bus.cache.reader;

import org.miaixz.bus.cache.Context;
import org.miaixz.bus.cache.Metrics;
import org.miaixz.bus.cache.Manage;
import org.miaixz.bus.cache.magic.AnnoHolder;
import org.miaixz.bus.cache.magic.MethodHolder;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.proxy.invoker.ProxyChain;

/**
 * 抽象缓存读取器
 * <p>
 * 提供缓存读取的基本框架，包含日志记录和方法调用耗时统计功能。 子类需要实现具体的读取逻辑。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractReader {

    /**
     * 缓存管理器
     */
    protected Manage manage;

    /**
     * 缓存上下文配置
     */
    protected Context context;

    /**
     * 缓存命中率统计组件
     */
    protected Metrics metrics;

    /**
     * 执行缓存读取操作
     * <p>
     * 根据注解信息和方法信息执行缓存读取操作，支持可选的写入功能。
     * </p>
     *
     * @param annoHolder   注解持有者，包含缓存相关的注解信息
     * @param methodHolder 方法持有者，包含方法相关的信息
     * @param baseInvoker  代理调用链，用于执行原始方法
     * @param needWrite    是否需要写入缓存
     * @return 缓存值或方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    public abstract Object read(
            AnnoHolder annoHolder,
            MethodHolder methodHolder,
            ProxyChain baseInvoker,
            boolean needWrite) throws Throwable;

    /**
     * 执行带日志记录的方法调用
     * <p>
     * 包装方法调用，记录方法执行耗时，无论方法是否抛出异常都会记录日志。
     * </p>
     *
     * @param throwableSupplier 可抛出异常的供应者
     * @return 方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    Object doLogInvoke(ThrowableSupplier<Object> throwableSupplier) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return throwableSupplier.get();
        } finally {
            Logger.debug("method invoke total cost [{}] ms", (System.currentTimeMillis() - start));
        }
    }

    /**
     * 设置缓存管理器
     *
     * @param manage 缓存管理器
     */
    public void setManage(Manage manage) {
        this.manage = manage;
    }

    /**
     * 设置缓存上下文配置
     *
     * @param config 缓存上下文配置
     */
    public void setContext(Context config) {
        this.context = config;
    }

    /**
     * 设置缓存命中率统计组件
     *
     * @param metrics 缓存命中率统计组件
     */
    public void setHitting(Metrics metrics) {
        this.metrics = metrics;
    }

    /**
     * 可抛出异常的供应者接口
     * <p>
     * 函数式接口，用于包装可能抛出异常的操作。
     * </p>
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    protected interface ThrowableSupplier<T> {

        /**
         * 获取结果
         *
         * @return 结果
         * @throws Throwable 可能抛出的异常
         */
        T get() throws Throwable;
    }

}
