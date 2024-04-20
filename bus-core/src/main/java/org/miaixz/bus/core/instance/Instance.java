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
package org.miaixz.bus.core.instance;

import org.miaixz.bus.core.annotation.ThreadSafe;

/**
 * 实例化对象的接口
 * 1. 使用此类的 class 必须有无参构造器
 * 2. 当前类出于测试阶段
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Instance {

    /**
     * 获取对象的单例对象
     * 1. 需要保证对象的线程安全性
     * 2. 只有在同一个分组返回的对象才会是单例,否则返回 newInstance()
     *
     * @param clazz     class 类型
     * @param groupName 分组名称
     * @param <T>       泛型
     * @return 实例化对象
     */
    <T> T singleton(final Class<T> clazz,
                    final String groupName);

    /**
     * 获取对象的单例对象
     * 1. 需要保证对象的线程安全性
     *
     * @param clazz class 类型
     * @param <T>   泛型
     * @return 实例化对象
     */
    <T> T singleton(final Class<T> clazz);

    /**
     * 获取每个线程内唯一的实例化对象
     * 注意：可能会内存泄漏的场景
     * (1) 只要这个线程对象被gc回收,就不会出现内存泄露,但在threadLocal设为null和线程结束这段时间不会被回收的,就发生了我们认为的内存泄露
     * 最要命的是线程对象不被回收的情况,这就发生了真正意义上的内存泄露 比如使用线程池的时候,线程结束是不会销毁的,会再次使用的 就可能出现内存泄露
     * 参考资料：https://www.cnblogs.com/onlywujun/p/3524675.html
     *
     * @param clazz class 类型
     * @param <T>   泛型
     * @return 实例化对象
     * @see java.lang.ref.WeakReference 弱引用
     */
    <T> T threadLocal(final Class<T> clazz);

    /**
     * 多例对象,每次都是全新的创建
     *
     * @param clazz class 类型
     * @param <T>   泛型
     * @return 实例化对象
     */
    <T> T multiple(final Class<T> clazz);

    /**
     * 线程安全对象
     * 1. 判断当前类是否拥有 {@link ThreadSafe} 注解,
     * 如果有,则直接创建单例对象 如果不是,则创建多例对象
     *
     * @param clazz class 类型
     * @param <T>   泛型
     * @return 实例化对象
     */
    <T> T threadSafe(final Class<T> clazz);

}
