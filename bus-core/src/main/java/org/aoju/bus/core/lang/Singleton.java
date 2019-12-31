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
package org.aoju.bus.core.lang;

import org.aoju.bus.core.utils.ClassUtils;
import org.aoju.bus.core.utils.ReflectUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 单例类
 * 提供单例对象的统一管理,当调用get方法时,如果对象池中存在此对象,返回此对象,否则创建新对象返回
 * 注意：单例针对的是类和对象,因此get方法第一次调用时创建的对象始终唯一,也就是说就算参数变更,返回的依旧是第一次创建的对象
 *
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8+
 */
public final class Singleton {

    private static Map<Class<?>, Object> pool = new ConcurrentHashMap<>();

    private Singleton() {
    }

    /**
     * 获得指定类的单例对象
     * 对象存在于池中返回,否则创建,每次调用此方法获得的对象为同一个对象
     * 注意：单例针对的是类和对象,因此get方法第一次调用时创建的对象始终唯一,也就是说就算参数变更,返回的依旧是第一次创建的对象
     *
     * @param <T>    单例对象类型
     * @param clazz  类
     * @param params 构造方法参数
     * @return 单例对象
     */
    public static <T> T get(Class<T> clazz, Object... params) {
        T obj = (T) pool.get(clazz);

        if (null == obj) {
            synchronized (Singleton.class) {
                obj = (T) pool.get(clazz);
                if (null == obj) {
                    obj = ReflectUtils.newInstance(clazz, params);
                    pool.put(clazz, obj);
                }
            }
        }

        return obj;
    }

    /**
     * 获得指定类的单例对象
     * 对象存在于池中返回,否则创建,每次调用此方法获得的对象为同一个对象
     * 注意：单例针对的是类和对象,因此get方法第一次调用时创建的对象始终唯一,也就是说就算参数变更,返回的依旧是第一次创建的对象
     *
     * @param <T>       单例对象类型
     * @param className 类名
     * @param params    构造参数
     * @return 单例对象
     */
    public static <T> T get(String className, Object... params) {
        final Class<T> clazz = ClassUtils.loadClass(className);
        return get(clazz, params);
    }

    /**
     * 将已有对象放入单例中,其Class做为键
     *
     * @param obj 对象
     */
    public static void put(Object obj) {
        pool.put(obj.getClass(), obj);
    }

    /**
     * 移除指定Singleton对象
     *
     * @param clazz 类
     */
    public static void remove(Class<?> clazz) {
        pool.remove(clazz);
    }

    /**
     * 清除所有Singleton对象
     */
    public static void destroy() {
        pool.clear();
    }

}
