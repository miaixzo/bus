/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
 * Instance factory class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class InstanceFactory implements Instance {

    /**
     * Singleton map object. The key is the fully qualified class name.
     */
    private final Map<String, Object> singletonMap = new ConcurrentHashMap<>();
    /**
     * Thread-local map object.
     */
    private final ThreadLocal<Map<String, Object>> mapThreadLocal = new ThreadLocal<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private InstanceFactory() {

    }

    /**
     * Gets the singleton instance of the factory.
     *
     * @return The instance factory.
     */
    public static InstanceFactory getInstance() {
        return SingletonHolder.INSTANCE_FACTORY;
    }

    /**
     * Gets a singleton instance using a static method.
     *
     * @param <T>   The generic type.
     * @param clazz The class information.
     * @return The singleton instance.
     */
    public static <T> T singletion(Class<T> clazz) {
        return getInstance().singleton(clazz);
    }

    /**
     * Gets a singleton instance from a specific group using a static method.
     *
     * @param <T>       The generic type.
     * @param clazz     The class information.
     * @param groupName The group name.
     * @return The singleton instance.
     */
    public static <T> T singletion(Class<T> clazz, final String groupName) {
        return getInstance().singleton(clazz, groupName);
    }

    /**
     * Singleton method.
     */
    @Override
    public <T> T singleton(Class<T> clazz, String groupName) {
        return getSingleton(clazz, groupName, singletonMap);
    }

    /**
     * Singleton method.
     */
    @Override
    public <T> T singleton(Class<T> clazz) {
        this.notNull(clazz);

        return this.getSingleton(clazz, singletonMap);
    }

    /**
     * Threadlocal method.
     */
    @Override
    public <T> T threadLocal(Class<T> clazz) {
        this.notNull(clazz);

        // 1. Check if the map exists.
        Map<String, Object> map = mapThreadLocal.get();
        if (ObjectKit.isNull(map)) {
            map = new ConcurrentHashMap<>();
        }

        // 2. Get the object.
        T instance = this.getSingleton(clazz, map);

        // 3. Update the thread-local.
        mapThreadLocal.set(map);

        return instance;
    }

    /**
     * Multiple method.
     */
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

    /**
     * Threadsafe method.
     */
    @Override
    public <T> T threadSafe(Class<T> clazz) {
        if (clazz.isAnnotationPresent(ThreadSafe.class)) {
            return this.singleton(clazz);
        }
        return this.multiple(clazz);
    }

    /**
     * Gets the singleton object.
     *
     * @param <T>         The generic type.
     * @param clazz       The class type.
     * @param instanceMap The instance map.
     * @return The singleton object.
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
     * Gets the singleton object from a group.
     *
     * @param <T>         The generic type.
     * @param clazz       The class to query.
     * @param group       The group information.
     * @param instanceMap The instance map.
     * @return The singleton object.
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
     * Asserts that the class is not null.
     *
     * @param clazz The class information.
     */
    private void notNull(final Class<?> clazz) {
        Assert.notNull(clazz, "class");
    }

    /**
     * Static inner class for singleton implementation.
     */
    private static class SingletonHolder {

        /**
         * The singleton instance of the factory.
         */
        private static final InstanceFactory INSTANCE_FACTORY = new InstanceFactory();
    }

}
