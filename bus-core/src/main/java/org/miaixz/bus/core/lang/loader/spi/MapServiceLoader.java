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

import java.nio.charset.Charset;
import java.util.*;

import org.miaixz.bus.core.cache.SimpleCache;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.*;

/**
 * A service loader for key-value pair services, which uses {@link Properties} to load and store services. Service files
 * are located by default under {@code META-INF/bus/}, with the file name being the fully qualified name of the service
 * interface class. The content of the file is similar to:
 * 
 * <pre>
 *     # This is a comment
 *     service1 = com.example.Service1
 *     service2 = com.example.Service2
 * </pre>
 * 
 * The corresponding service can be obtained by calling the {@link #getService(String)} method with the name before the
 * equals sign.
 *
 * @param <S> The type of the service.
 * @author Kimi Liu
 * @since Java 21+
 */
public class MapServiceLoader<S> extends AbstractServiceLoader<S> {

    /**
     * The default prefix for service definition files, located under {@code META-INF/bus/}.
     */
    private static final String PREFIX = Normal.META_INF + Symbol.SLASH + Keys.BUS + Symbol.SLASH;

    /**
     * Cache for service instances.
     */
    private final SimpleCache<String, S> serviceCache;
    /**
     * Stores the loaded service definitions.
     */
    private Properties serviceProperties;

    /**
     * Constructs a new {@code MapServiceLoader}.
     *
     * @param pathPrefix   The path prefix for the service files.
     * @param serviceClass The service interface class.
     * @param classLoader  A custom class loader, or {@code null} to use the current default class loader.
     * @param charset      The character set to use for reading the service files, defaults to UTF-8.
     */
    public MapServiceLoader(final String pathPrefix, final Class<S> serviceClass, final ClassLoader classLoader,
            final Charset charset) {
        super(pathPrefix, serviceClass, classLoader, charset);

        this.serviceCache = new SimpleCache<>(new HashMap<>());
        load();
    }

    /**
     * Creates a new {@code MapServiceLoader} with the default path prefix.
     *
     * @param <S>          The type of the service.
     * @param serviceClass The service interface class.
     * @return A new {@code MapServiceLoader} instance.
     */
    public static <S> MapServiceLoader<S> of(final Class<S> serviceClass) {
        return of(serviceClass, null);
    }

    /**
     * Creates a new {@code MapServiceLoader} with the default path prefix and a specified class loader.
     *
     * @param <S>          The type of the service.
     * @param serviceClass The service interface class.
     * @param classLoader  A custom class loader, or {@code null} to use the current default class loader.
     * @return A new {@code MapServiceLoader} instance.
     */
    public static <S> MapServiceLoader<S> of(final Class<S> serviceClass, final ClassLoader classLoader) {
        return of(PREFIX, serviceClass, classLoader);
    }

    /**
     * Creates a new {@code MapServiceLoader} with a specified path prefix and class loader.
     *
     * @param <S>          The type of the service.
     * @param pathPrefix   The path prefix for the service files.
     * @param serviceClass The service interface class.
     * @param classLoader  A custom class loader, or {@code null} to use the current default class loader.
     * @return A new {@code MapServiceLoader} instance.
     */
    public static <S> MapServiceLoader<S> of(
            final String pathPrefix,
            final Class<S> serviceClass,
            final ClassLoader classLoader) {
        return new MapServiceLoader<>(pathPrefix, serviceClass, classLoader, null);
    }

    /**
     * Loads or reloads all services. This method parses all service resources with the same name. According to resource
     * loading priority, resources loaded and parsed first are prioritized. Subsequent resources with the same name are
     * discarded.
     */
    @Override
    public void load() {
        final Properties properties = new Properties();
        ResourceKit.loadAllTo(
                properties,
                pathPrefix + serviceClass.getName(),
                classLoader,
                charset,
                // non-override mode
                false);
        this.serviceProperties = properties;
    }

    /**
     * Returns the number of services loaded.
     *
     * @return the number of services.
     */
    @Override
    public int size() {
        return this.serviceProperties.size();
    }

    /**
     * Returns a list of all service names.
     *
     * @return an unmodifiable list of service names.
     */
    @Override
    public List<String> getServiceNames() {
        return ListKit.view(this.serviceCache.keys());
    }

    /**
     * Gets the implementation class for the service by name.
     *
     * @param serviceName the name of the service as defined in the properties file.
     * @return the service class, or {@code null} if not found.
     */
    @Override
    public Class<S> getServiceClass(final String serviceName) {
        final String serviceClassName = this.serviceProperties.getProperty(serviceName);
        if (StringKit.isBlank(serviceClassName)) {
            return null;
        }
        return ClassKit.loadClass(serviceClassName);
    }

    /**
     * Gets the service instance by name, using a cache. Multiple calls will return the same service object.
     *
     * @param serviceName the name of the service as defined in the properties file.
     * @return the service object, or {@code null} if not found.
     */
    @Override
    public S getService(final String serviceName) {
        return this.serviceCache.get(serviceName, () -> createService(serviceName));
    }

    /**
     * Returns an iterator over the services. The iterator will return the same service object for multiple calls.
     *
     * @return an iterator over the services.
     */
    @Override
    public Iterator<S> iterator() {
        return new Iterator<>() {

            private final Iterator<String> nameIter = serviceProperties.stringPropertyNames().iterator();

            /**
             * Returns {@code true} if there are more services to iterate over.
             *
             * @return {@code true} if there are more services, {@code false} otherwise.
             */
            @Override
            public boolean hasNext() {
                return nameIter.hasNext();
            }

            /**
             * Returns the next service in the iteration.
             *
             * @return the next service.
             */
            @Override
            public S next() {
                return getService(nameIter.next());
            }
        };
    }

    /**
     * Creates a new service instance without caching.
     *
     * @param serviceName The name of the service.
     * @return The service object.
     */
    private S createService(final String serviceName) {
        return ReflectKit.newInstance(getServiceClass(serviceName));
    }

}
