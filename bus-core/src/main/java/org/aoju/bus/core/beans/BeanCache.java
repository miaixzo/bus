/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.beans;

import org.aoju.bus.core.lang.SimpleCache;
import org.aoju.bus.core.lang.function.Func0;

/**
 * Bean属性缓存
 * 缓存用于防止多次反射造成的性能问题
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since Java 17+
 */
public enum BeanCache {

    /**
     * 实例
     */
    INSTANCE;
    /**
     * 缓存
     */
    private final SimpleCache<Class<?>, BeanDesc> bdCache = new SimpleCache<>();

    /**
     * 获得属性名和{@link BeanDesc}Map映射
     *
     * @param beanClass Bean的类
     * @param supplier  对象不存在时创建对象的函数
     * @return 属性名和{@link BeanDesc}映射
     */
    public BeanDesc getBeanDesc(Class<?> beanClass, Func0<BeanDesc> supplier) {
        return bdCache.get(beanClass, supplier);
    }

    /**
     * 清空全局的Bean属性缓存
     */
    public void clear() {
        this.bdCache.clear();
    }

}
