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
package org.miaixz.bus.core.lang.copier;

import java.io.Serial;
import java.io.Serializable;
import java.util.function.Predicate;

/**
 * 复制器抽象类 抽象复制器抽象了一个对象复制到另一个对象，通过实现{@link #copy()}方法实现复制逻辑。
 *
 * @param <T> 拷贝的对象
 * @param <C> 本类的类型。用于set方法返回本对象，方便流式编程
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class SrcToDestCopier<T, C extends SrcToDestCopier<T, C>> implements Copier<T>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852299736181L;

    /**
     * 源
     */
    protected T src;
    /**
     * 目标
     */
    protected T target;
    /**
     * 拷贝过滤器，可以过滤掉不需要拷贝的源
     */
    protected Predicate<T> copyPredicate;

    /**
     * 获取源
     *
     * @return 源
     */
    public T getSrc() {
        return src;
    }

    /**
     * 设置源
     *
     * @param src 源
     * @return this
     */
    public C setSrc(final T src) {
        this.src = src;
        return (C) this;
    }

    /**
     * 获得目标
     *
     * @return 目标
     */
    public T getTarget() {
        return target;
    }

    /**
     * 设置目标
     *
     * @param target 目标
     * @return this
     */
    public C setTarget(final T target) {
        this.target = target;
        return (C) this;
    }

    /**
     * 获得过滤器
     *
     * @return 过滤器
     */
    public Predicate<T> getCopyPredicate() {
        return copyPredicate;
    }

    /**
     * 设置过滤器
     *
     * @param copyPredicate 过滤器
     * @return this
     */
    public C setCopyPredicate(final Predicate<T> copyPredicate) {
        this.copyPredicate = copyPredicate;
        return (C) this;
    }

}
