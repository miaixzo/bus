/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.core.lang.copier;


import org.aoju.bus.core.lang.Filter;

/**
 * 复制器抽象类
 * 抽象复制器抽象了一个对象复制到另一个对象,通过实现{@link #copy()}方法实现复制逻辑
 *
 * @param <T> 拷贝的对象
 * @param <C> 本类的类型 用于set方法返回本对象,方便流式编程
 * @author Kimi Liu
 * @version 5.3.0
 * @since JDK 1.8+
 */
public abstract class Duplicate<T, C extends Duplicate<T, C>> implements Copier<T> {

    /**
     * 源
     */
    protected T src;
    /**
     * 目标
     */
    protected T dest;
    /**
     * 拷贝过滤器,可以过滤掉不需要拷贝的源
     */
    protected Filter<T> copyFilter;

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
    public C setSrc(T src) {
        this.src = src;
        return (C) this;
    }

    /**
     * 获得目标
     *
     * @return 目标
     */
    public T getDest() {
        return dest;
    }

    /**
     * 设置目标
     *
     * @param dest 目标
     * @return this
     */
    public C setDest(T dest) {
        this.dest = dest;
        return (C) this;
    }

    /**
     * 获得过滤器
     *
     * @return 过滤器
     */
    public Filter<T> getCopyFilter() {
        return copyFilter;
    }

    /**
     * 设置过滤器
     *
     * @param copyFilter 过滤器
     * @return this
     */
    public C setCopyFilter(Filter<T> copyFilter) {
        this.copyFilter = copyFilter;
        return (C) this;
    }

}
