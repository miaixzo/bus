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
package org.miaixz.bus.core.lang.loader.spi;

import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * A utility class for loading services using the SPI (Service Provider Interface) mechanism. The process is as follows:
 * 
 * <pre>
 *     1. Create an interface and its implementation classes.
 *     2. Create a file with the same name as the fully qualified name of the interface
 *        under {@code
 * META - INF / services
 * } on the classpath.
 *     3. Write the fully qualified names of the implementation classes in this file.
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NormalSpiLoader {

    /**
     * Loads the first available service. If multiple implementations are defined, it returns the first one that can be
     * instantiated without errors.
     *
     * @param <S>   The type of the service.
     * @param clazz The service interface class.
     * @return The first available service implementation, or {@code null} if no implementation is found.
     */
    public static <S> S loadFirstAvailable(final Class<S> clazz) {
        return loadFirstAvailable(loadList(clazz));
    }

    /**
     * Loads the first available service from the given {@link ServiceLoader}. If multiple implementations are defined,
     * it returns the first one that can be instantiated without errors.
     *
     * @param <S>           The type of the service.
     * @param serviceLoader The {@link ServiceLoader} to load from.
     * @return The first available service implementation, or {@code null} if no implementation is found.
     */
    public static <S> S loadFirstAvailable(final ServiceLoader<S> serviceLoader) {
        final Iterator<S> iterator = serviceLoader.iterator();
        while (iterator.hasNext()) {
            try {
                return iterator.next();
            } catch (final Throwable ignore) {
                // Ignore exceptions during service instantiation.
            }
        }
        return null;
    }

    /**
     * 加载第一个服务，如果用户定义了多个接口实现类，只获取第一个。
     *
     * @param <T>   接口类型
     * @param clazz 服务接口
     * @return 第一个服务接口实现对象，无实现返回{@code null}
     */
    public static <T> T loadFirst(final Class<T> clazz) {
        final Iterator<T> iterator = load(clazz).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    /**
     * Loads services and returns them as a {@link ServiceLoader}.
     *
     * @param <T>   The type of the service interface.
     * @param clazz The service interface class.
     * @return A {@link ServiceLoader} containing the service implementations.
     */
    public static <T> ServiceLoader<T> loadList(final Class<T> clazz) {
        return loadList(clazz, null);
    }

    /**
     * Loads services with a specified {@link ClassLoader} and returns them as a {@link ServiceLoader}.
     *
     * @param <T>    The type of the service interface.
     * @param clazz  The service interface class.
     * @param loader The {@link ClassLoader} to use.
     * @return A {@link ServiceLoader} containing the service implementations.
     */
    public static <T> ServiceLoader<T> loadList(final Class<T> clazz, final ClassLoader loader) {
        return ListServiceLoader.of(clazz, loader);
    }

    /**
     * Loads services and returns them as a {@link List}.
     *
     * @param <T>      The type of the service interface.
     * @param isLinked If {@code true}, a {@link java.util.LinkedList} is returned; otherwise, an
     *                 {@link java.util.ArrayList}.
     * @param clazz    The service interface class.
     * @return A list of service implementations.
     */
    public static <T> List<T> loadList(final boolean isLinked, final Class<T> clazz) {
        return loadList(isLinked, clazz, null);
    }

    /**
     * Loads services with a specified {@link ClassLoader} and returns them as a {@link List}.
     *
     * @param <T>      The type of the service interface.
     * @param isLinked If {@code true}, a {@link java.util.LinkedList} is returned; otherwise, an
     *                 {@link java.util.ArrayList}.
     * @param clazz    The service interface class.
     * @param loader   The {@link ClassLoader} to use.
     * @return A list of service implementations.
     */
    public static <T> List<T> loadList(final boolean isLinked, final Class<T> clazz, final ClassLoader loader) {
        return ListKit.of(isLinked, load(clazz, loader));
    }

    /**
     * Loads services using the standard {@link java.util.ServiceLoader}.
     *
     * @param <T>   The type of the service interface.
     * @param clazz The service interface class.
     * @return A {@link java.util.ServiceLoader} for the given service interface.
     */
    public static <T> java.util.ServiceLoader<T> load(final Class<T> clazz) {
        return load(clazz, null);
    }

    /**
     * Loads services using the standard {@link java.util.ServiceLoader} and a specified {@link ClassLoader}.
     *
     * @param <T>    The type of the service interface.
     * @param clazz  The service interface class.
     * @param loader The {@link ClassLoader} to be used to load provider-configuration files and provider classes.
     * @return A {@link java.util.ServiceLoader} for the given service interface.
     */
    public static <T> java.util.ServiceLoader<T> load(final Class<T> clazz, final ClassLoader loader) {
        return java.util.ServiceLoader.load(clazz, ObjectKit.defaultIfNull(loader, ClassKit::getClassLoader));
    }

}
