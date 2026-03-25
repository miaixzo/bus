/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 21+
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
     * Loads the first service. If the user has defined multiple interface implementation classes, only the first one is
     * retrieved.
     *
     * @param <T>   The interface type.
     * @param clazz The service interface.
     * @return The first service interface implementation object, or {@code null} if no implementation is found.
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
