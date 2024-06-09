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

import org.miaixz.bus.core.Loader;

import java.io.Serializable;

/**
 * 懒加载加载器
 * 在load方法被调用前，对象未被加载，直到被调用后才开始加载
 * 此加载器常用于对象比较庞大而不一定被使用的情况，用于减少启动时资源占用问题
 * 此加载器使用双重检查（Double-Check）方式检查对象是否被加载，避免多线程下重复加载或加载丢失问题
 *
 * @param <T> 被加载对象类型
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class LazyLoader<T> implements Loader<T>, Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 被加载对象
     */
    private volatile T object;

    /**
     * 获取一个对象，第一次调用此方法时初始化对象然后返回，之后调用此方法直接返回原对象
     */
    @Override
    public T get() {
        T result = object;
        if (result == null) {
            synchronized (this) {
                result = object;
                if (result == null) {
                    object = result = init();
                }
            }
        }
        return result;
    }

    /**
     * 初始化被加载的对象
     * 如果对象从未被加载过，调用此方法初始化加载对象，此方法只被调用一次
     *
     * @return 被加载的对象
     */
    protected abstract T init();

}
