/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Singleton class. Provides unified management of singleton objects. When the get method is called, if the object
 * exists in the pool, it is returned; otherwise, a new object is created and returned.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Instances {

    /**
     * The singleton object pool.
     */
    private static final ConcurrentHashMap<String, Object> POOL = new ConcurrentHashMap<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private Instances() {

    }

    /**
     * Gets the singleton object of the specified class. If the object exists in the pool, it is returned; otherwise, it
     * is created. Each call to this method for the same class and arguments will return the same object. Note: The
     * singleton is specific to the class and its arguments, meaning that only when the class and arguments are
     * identical will the same object be returned.
     *
     * @param <T>   The type of the singleton object.
     * @param clazz The class.
     * @param args  The constructor arguments.
     * @return The singleton object.
     */
    public static <T> T get(final Class<T> clazz, final Object... args) {
        Assert.notNull(clazz, "Class must be not null !");
        final String key = buildKey(clazz.getName(), args);
        return get(key, () -> ReflectKit.newInstance(clazz, args));
    }

    /**
     * Gets the singleton object for the specified key. If the object exists in the pool, it is returned; otherwise, it
     * is created using the provided supplier. Each call to this method with the same key will return the same object.
     *
     * @param <T>      The type of the singleton object.
     * @param key      The custom key.
     * @param supplier The supplier to create the singleton object.
     * @return The singleton object.
     */
    public static <T> T get(final String key, final SupplierX<T> supplier) {
        return (T) POOL.computeIfAbsent(key, (k) -> supplier.get());
    }

    /**
     * Puts an existing object into the singleton pool, using its class name as the key.
     *
     * @param object The object to put into the pool.
     */
    public static void put(final Object object) {
        Assert.notNull(object, "Bean object must be not null !");
        put(object.getClass().getName(), object);
    }

    /**
     * Puts an existing object into the singleton pool with a custom key.
     *
     * @param key    The key.
     * @param object The object.
     */
    public static void put(final String key, final Object object) {
        POOL.put(key, object);
    }

    /**
     * Checks if an object of a certain class exists in the pool.
     *
     * @param clazz The class.
     * @param args  The constructor arguments.
     * @return {@code true} if the object exists, {@code false} otherwise.
     */
    public static boolean exists(final Class<?> clazz, final Object... args) {
        if (null != clazz) {
            final String key = buildKey(clazz.getName(), args);
            return POOL.containsKey(key);
        }
        return false;
    }

    /**
     * Gets all classes that exist in the singleton pool.
     *
     * @return A set of unique classes.
     */
    public static Set<Class<?>> getExistClass() {
        return POOL.values().stream().map(Object::getClass).collect(Collectors.toSet());
    }

    /**
     * Removes the specified singleton object from the pool.
     *
     * @param clazz The class of the object to remove.
     */
    public static void remove(final Class<?> clazz) {
        if (null != clazz) {
            remove(clazz.getName());
        }
    }

    /**
     * Removes the specified singleton object from the pool by its key.
     *
     * @param key The key of the object to remove.
     */
    public static void remove(final String key) {
        POOL.remove(key);
    }

    /**
     * Clears all singleton objects from the pool.
     */
    public static void destroy() {
        POOL.clear();
    }

    /**
     * Gets a singleton instance using a static method.
     *
     * @param <T>   The generic type.
     * @param clazz The class information.
     * @return The singleton instance.
     */
    public static <T> T singletion(Class<T> clazz) {
        return InstanceFactory.getInstance().singleton(clazz);
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
        return InstanceFactory.getInstance().singleton(clazz, groupName);
    }

    /**
     * Gets a thread-local instance, ensuring that the same instance is returned for the same thread.
     *
     * @param <T>   The generic type.
     * @param clazz The class.
     * @return The thread-local instance.
     */
    public static <T> T threadLocal(Class<T> clazz) {
        return InstanceFactory.getInstance().threadLocal(clazz);
    }

    /**
     * Gets a thread-safe instance. If the class is marked with {@link ThreadSafe}, a singleton is returned; otherwise,
     * a new instance is returned.
     *
     * @param <T>   The generic type.
     * @param clazz The class.
     * @return The thread-safe instance.
     */
    public static <T> T threadSafe(Class<T> clazz) {
        return InstanceFactory.getInstance().threadSafe(clazz);
    }

    /**
     * Gets a new instance (multiple instances).
     *
     * @param <T>   The generic type.
     * @param clazz The class.
     * @return A new instance.
     */
    public static <T> T multiple(Class<T> clazz) {
        return InstanceFactory.getInstance().multiple(clazz);
    }

    /**
     * Gets the singleton object of the specified class. If the object exists in the pool, it is returned; otherwise, it
     * is created. Each call to this method for the same class and arguments will return the same object. Note: The
     * singleton is specific to the class and its arguments, meaning that only when the class and arguments are
     * identical will the same object be returned.
     *
     * @param <T>   The type of the singleton object.
     * @param clazz The class.
     * @param args  The constructor arguments.
     * @return The singleton object.
     */
    public static <T> T singletion(Class<T> clazz, Object... args) {
        Assert.notNull(clazz, "Class must be not null !");
        final String key = buildKey(clazz.getName(), args);
        return singletion(key, () -> ReflectKit.newInstance(clazz, args));
    }

    /**
     * Gets the singleton object for the specified key. If the object exists in the pool, it is returned; otherwise, it
     * is created using the provided supplier. Each call to this method with the same key will return the same object.
     *
     * @param <T>      The type of the singleton object.
     * @param key      The custom key.
     * @param supplier The supplier to create the singleton object.
     * @return The singleton object.
     */
    public static <T> T singletion(String key, SupplierX<T> supplier) {
        Object value = POOL.get(key);
        if (null == value) {
            POOL.putIfAbsent(key, supplier.get());
            value = POOL.get(key);
        }
        return (T) value;
    }

    /**
     * Gets the singleton object of the specified class. If the object exists in the pool, it is returned; otherwise, it
     * is created. Each call to this method for the same class and arguments will return the same object.
     *
     * @param <T>       The type of the singleton object.
     * @param className The class name.
     * @param args      The constructor arguments.
     * @return The singleton object.
     */
    public static <T> T singletion(String className, Object... args) {
        Assert.notBlank(className, "Class name must be not blank !");
        final Class<T> clazz = ClassKit.loadClass(className);
        return singletion(clazz, args);
    }

    /**
     * Builds the key for the singleton pool.
     *
     * @param className The class name.
     * @param args      The argument list.
     * @return The built key.
     */
    private static String buildKey(final String className, final Object... args) {
        if (ArrayKit.isEmpty(args)) {
            return className;
        }
        return StringKit.format("{}#{}", className, ArrayKit.join(args, Symbol.UNDERLINE));
    }

}
