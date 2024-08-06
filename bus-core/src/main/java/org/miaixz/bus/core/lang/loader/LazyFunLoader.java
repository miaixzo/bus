/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.lang.loader;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Assert;

/**
 * 函数式懒加载加载器 传入用于生成对象的函数，在对象需要使用时调用生成对象，然后抛弃此生成对象的函数。 此加载器常用于对象比较庞大而不一定被使用的情况，用于减少启动时资源占用问题
 * 继承自{@link LazyLoader}，如何实现多线程安全，由LazyLoader完成。
 *
 * @param <T> 被加载对象类型
 * @author Kimi Liu
 * @see LazyLoader
 * @since Java 17+
 */
public class LazyFunLoader<T> extends LazyLoader<T> {

    private static final long serialVersionUID = -1L;

    /**
     * 用于生成对象的函数
     */
    private Supplier<T> supplier;

    /**
     * 构造
     *
     * @param supplier 用于生成对象的函数
     */
    public LazyFunLoader(final Supplier<T> supplier) {
        Assert.notNull(supplier);
        this.supplier = supplier;
    }

    /**
     * 静态工厂方法，提供语义性与编码便利性
     *
     * @param supplier 用于生成对象的函数
     * @param <T>      对象类型
     * @return 函数式懒加载加载器对象
     */
    public static <T> LazyFunLoader<T> on(final Supplier<T> supplier) {
        Assert.notNull(supplier, "supplier must be not null!");
        return new LazyFunLoader<>(supplier);
    }

    @Override
    protected T init() {
        final T t = this.supplier.get();
        this.supplier = null;
        return t;
    }

    /**
     * 是否已经初始化
     *
     * @return 是/否
     */
    public boolean isInitialize() {
        return this.supplier == null;
    }

    /**
     * 如果已经初始化，就执行传入函数
     *
     * @param consumer 待执行函数
     */
    public void ifInitialized(final Consumer<T> consumer) {
        Assert.notNull(consumer);

        // 已经初始化
        if (this.isInitialize()) {
            consumer.accept(this.get());
        }
    }

}
