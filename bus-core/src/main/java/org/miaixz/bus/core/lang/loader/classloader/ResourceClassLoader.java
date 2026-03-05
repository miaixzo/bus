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
package org.miaixz.bus.core.lang.loader.classloader;

import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * A class loader for resources, capable of loading classes from any type of resource.
 *
 * @param <T> The type of the resource, which must implement the {@link Resource} interface.
 * @author Kimi Liu
 * @since Java 17+
 */
public class ResourceClassLoader<T extends Resource> extends SecureClassLoader {

    /**
     * The map of resource names to resource objects.
     */
    private final Map<String, T> resourceMap;
    /**
     * Cache for already loaded classes.
     */
    private final Map<String, Class<?>> cacheClassMap;

    /**
     * Constructs a new ResourceClassLoader.
     *
     * @param parentClassLoader The parent class loader. If {@code null}, the context class loader is used.
     * @param resourceMap       A map of resource names to resource objects.
     */
    public ResourceClassLoader(final ClassLoader parentClassLoader, final Map<String, T> resourceMap) {
        super(ObjectKit.defaultIfNull(parentClassLoader, ClassKit::getClassLoader));
        this.resourceMap = ObjectKit.defaultIfNull(resourceMap, HashMap::new);
        this.cacheClassMap = new HashMap<>();
    }

    /**
     * Adds a class resource to be loaded.
     *
     * @param resource The resource, which can be a file, stream, or string.
     * @return this {@code ResourceClassLoader} instance.
     */
    public ResourceClassLoader<T> addResource(final T resource) {
        this.resourceMap.put(resource.getName(), resource);
        return this;
    }

    /**
     * Finds and loads the class with the specified name.
     *
     * @param name the fully qualified class name.
     * @return the loaded {@link Class} object.
     * @throws ClassNotFoundException if the class cannot be found.
     */
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        final Class<?> clazz = cacheClassMap.computeIfAbsent(name, this::defineByName);
        if (clazz == null) {
            return super.findClass(name);
        }
        return clazz;
    }

    /**
     * Reads the class's binary stream from the given resource and then defines the class. Returns {@code null} if the
     * resource does not exist.
     *
     * @param name The name of the class.
     * @return The defined class, or {@code null} if the resource is not found.
     */
    private Class<?> defineByName(final String name) {
        final Resource resource = resourceMap.get(name);
        if (null != resource) {
            final byte[] bytes = resource.readBytes();
            return defineClass(name, bytes, 0, bytes.length);
        }
        return null;
    }

}
