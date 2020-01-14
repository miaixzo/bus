/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.core.instance;


import org.aoju.bus.core.annotation.ThreadSafe;

/**
 * 实例化工具类
 * 对于 {@link InstanceFactory} 的便于使用
 *
 * @author Kimi Liu
 * @version 5.5.3
 * @since JDK 1.8+
 */
@ThreadSafe
public final class Instances {

    private Instances() {
    }

    /**
     * 静态方法单例
     *
     * @param clazz 类信息
     * @param <T>   泛型
     * @return 结果
     */
    public static <T> T singletion(Class<T> clazz) {
        return InstanceFactory.getInstance().singleton(clazz);
    }

    /**
     * 静态方法单例
     *
     * @param clazz     类信息
     * @param groupName 分组名称
     * @param <T>       泛型
     * @return 结果
     */
    public static <T> T singletion(Class<T> clazz, final String groupName) {
        return InstanceFactory.getInstance().singleton(clazz, groupName);
    }

    /**
     * threadLocal 同一个线程对应的实例一致
     *
     * @param clazz class
     * @param <T>   泛型
     * @return 结果
     */
    public static <T> T threadLocal(Class<T> clazz) {
        return InstanceFactory.getInstance().threadLocal(clazz);
    }

    /**
     * {@link ThreadSafe} 线程安全标示的使用单例,或者使用多例
     *
     * @param clazz class
     * @param <T>   泛型
     * @return 结果
     */
    public static <T> T threadSafe(Class<T> clazz) {
        return InstanceFactory.getInstance().threadSafe(clazz);
    }

    /**
     * 多例
     *
     * @param clazz class
     * @param <T>   泛型
     * @return 结果
     */
    public static <T> T multiple(Class<T> clazz) {
        return InstanceFactory.getInstance().multiple(clazz);
    }

}
