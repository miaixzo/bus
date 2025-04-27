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
package org.miaixz.bus.core.instance;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * 实例化工厂类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class InstanceFactory implements Instance {

    /**
     * 单例 map 对象 1. data 是 class 的全称
     */
    private final Map<String, Object> singletonMap = new ConcurrentHashMap<>();
    /**
     * 线程内的 map 对象
     */
    private final ThreadLocal<Map<String, Object>> mapThreadLocal = new ThreadLocal<>();

    private InstanceFactory() {

    }

    /**
     * 获取单例对象
     *
     * @return 实例化对象
     */
    public static InstanceFactory getInstance() {
        return SingletonHolder.INSTANCE_FACTORY;
    }

    /**
     * 静态方法单例
     *
     * @param clazz 类信息
     * @param <T>   泛型
     * @return 结果
     */
    public static <T> T singletion(Class<T> clazz) {
        return getInstance().singleton(clazz);
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
        return getInstance().singleton(clazz, groupName);
    }

    @Override
    public <T> T singleton(Class<T> clazz, String groupName) {
        return getSingleton(clazz, groupName, singletonMap);
    }

    @Override
    public <T> T singleton(Class<T> clazz) {
        this.notNull(clazz);

        return this.getSingleton(clazz, singletonMap);
    }

    @Override
    public <T> T threadLocal(Class<T> clazz) {
        this.notNull(clazz);

        // 1. 校验 map 是否存在
        Map<String, Object> map = mapThreadLocal.get();
        if (ObjectKit.isNull(map)) {
            map = new ConcurrentHashMap<>();
        }

        // 2. 获取对象
        T instance = this.getSingleton(clazz, map);

        // 3. 更新 threadLocal
        mapThreadLocal.set(map);

        return instance;
    }

    @Override
    public <T> T multiple(Class<T> clazz) {
        this.notNull(clazz);

        try {
            return clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public <T> T threadSafe(Class<T> clazz) {
        if (clazz.isAnnotationPresent(ThreadSafe.class)) {
            return this.singleton(clazz);
        }
        return this.multiple(clazz);
    }

    /**
     * 获取单例对象
     *
     * @param clazz       class 类型
     * @param instanceMap 实例化对象 map
     * @return 单例对象
     */
    private <T> T getSingleton(final Class<T> clazz, final Map<String, Object> instanceMap) {
        this.notNull(clazz);

        final String fullClassName = clazz.getName();
        T instance = (T) instanceMap.get(fullClassName);
        if (ObjectKit.isNull(instance)) {
            instance = this.multiple(clazz);
            instanceMap.put(fullClassName, instance);
        }
        return instance;
    }

    /**
     * 获取单例对象
     *
     * @param clazz       查询 clazz
     * @param group       分组信息
     * @param instanceMap 实例化对象 map
     * @return 单例对象
     */
    private <T> T getSingleton(final Class<T> clazz, final String group, final Map<String, Object> instanceMap) {
        this.notNull(clazz);
        Assert.notEmpty(group, "id");

        final String fullClassName = clazz.getName() + Symbol.MINUS + group;
        T instance = (T) instanceMap.get(fullClassName);
        if (ObjectKit.isNull(instance)) {
            instance = this.multiple(clazz);
            instanceMap.put(fullClassName, instance);
        }
        return instance;
    }

    /**
     * 断言参数不可为 null
     *
     * @param clazz class 信息
     */
    private void notNull(final Class clazz) {
        Assert.notNull(clazz, "class");
    }

    /**
     * 静态内部类实现单例
     */
    private static class SingletonHolder {
        private static final InstanceFactory INSTANCE_FACTORY = new InstanceFactory();
    }

}
