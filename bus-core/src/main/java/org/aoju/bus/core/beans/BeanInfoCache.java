/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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

import org.aoju.bus.core.lang.Func;
import org.aoju.bus.core.lang.SimpleCache;

import java.beans.PropertyDescriptor;
import java.util.Map;

/**
 * Bean属性缓存
 * 缓存用于防止多次反射造成的性能问题
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public enum BeanInfoCache {

    INSTANCE;

    private final SimpleCache<Class<?>, Map<String, PropertyDescriptor>> pdCache = new SimpleCache<>();
    private final SimpleCache<Class<?>, Map<String, PropertyDescriptor>> ignoreCasePdCache = new SimpleCache<>();

    /**
     * 获得属性名和{@link PropertyDescriptor}Map映射
     *
     * @param beanClass  Bean的类
     * @param ignoreCase 是否忽略大小写
     * @return 属性名和{@link PropertyDescriptor}Map映射
     */
    public Map<String, PropertyDescriptor> getPropertyDescriptorMap(Class<?> beanClass, boolean ignoreCase) {
        return getCache(ignoreCase).get(beanClass);
    }

    /**
     * 获得属性名和{@link PropertyDescriptor}Map映射
     *
     * @param beanClass  Bean的类
     * @param ignoreCase 是否忽略大小写
     * @param supplier   缓存对象产生函数
     * @return 属性名和{@link PropertyDescriptor}Map映射
     */
    public Map<String, PropertyDescriptor> getPropertyDescriptorMap(
            Class<?> beanClass,
            boolean ignoreCase,
            Func.Func0<Map<String, PropertyDescriptor>> supplier) {
        return getCache(ignoreCase).get(beanClass, supplier);
    }

    /**
     * 加入缓存
     *
     * @param beanClass                      Bean的类
     * @param fieldNamePropertyDescriptorMap 属性名和{@link PropertyDescriptor}Map映射
     * @param ignoreCase                     是否忽略大小写
     */
    public void putPropertyDescriptorMap(Class<?> beanClass, Map<String, PropertyDescriptor> fieldNamePropertyDescriptorMap, boolean ignoreCase) {
        getCache(ignoreCase).put(beanClass, fieldNamePropertyDescriptorMap);
    }

    /**
     * 根据是否忽略字段名的大小写，返回不用Cache对象
     *
     * @param ignoreCase 是否忽略大小写
     * @return SimpleCache
     */
    private SimpleCache<Class<?>, Map<String, PropertyDescriptor>> getCache(boolean ignoreCase) {
        return ignoreCase ? ignoreCasePdCache : pdCache;
    }

}
