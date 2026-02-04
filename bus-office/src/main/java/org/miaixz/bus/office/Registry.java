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
package org.miaixz.bus.office;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.exception.DependencyException;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * Registry for supported office-related components and services.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Registry {

    /**
     * Identifier for local conversion services.
     */
    public static final String LOCAL = "LOCAL";
    /**
     * Identifier for online conversion services.
     */
    public static final String ONLINE = "ONLINE";
    /**
     * Cache for storing registered service providers, mapped by name or simple class name.
     */
    private static Map<Object, Object> COMPLEX_CACHE = new ConcurrentHashMap<>();
    /**
     * Singleton instance of the Registry.
     */
    private static Registry instance;

    /**
     * Constructs a new Registry instance.
     */
    public Registry() {

    }

    /**
     * Retrieves the singleton instance of the Registry.
     *
     * @return The singleton {@link Registry} instance.
     */
    public static Registry getInstance() {
        synchronized (Registry.class) {
            if (ObjectKit.isEmpty(instance)) {
                instance = new Registry();
            }
        }
        return instance;
    }

    /**
     * Registers a component with a given name and object. If a component with the same name or simple class name
     * already exists, an {@link InternalException} is thrown.
     *
     * @param name   The name of the component to register.
     * @param object The component object to register.
     * @throws InternalException if a component with the same name or type is already registered.
     */
    public static void register(String name, Object object) {
        if (COMPLEX_CACHE.containsKey(name)) {
            throw new InternalException("Duplicate registration of component with the same name: " + name);
        }
        Class<?> clazz = object.getClass();
        if (COMPLEX_CACHE.containsKey(clazz.getSimpleName())) {
            throw new InternalException("Duplicate registration of component with the same type: " + clazz);
        }
        COMPLEX_CACHE.putIfAbsent(name, object);
        COMPLEX_CACHE.putIfAbsent(clazz.getSimpleName(), object);
    }

    /**
     * Checks for the presence of POI dependencies. Throws a {@link DependencyException} if the required POI classes are
     * not found.
     *
     * @throws DependencyException if POI dependencies are missing.
     */
    public static void check() {
        try {
            Class.forName("org.apache.poi.ss.usermodel.Workbook", false, ClassKit.getClassLoader());
        } catch (final ClassNotFoundException | NoClassDefFoundError | NoSuchMethodError e) {
            throw new DependencyException(e, Builder.NO_POI_ERROR_MSG);
        }
    }

    /**
     * Checks if a component with the specified name is registered.
     *
     * @param name The name of the component to check.
     * @return {@code true} if the component is registered, {@code false} otherwise.
     */
    public boolean contains(String name) {
        return COMPLEX_CACHE.containsKey(name);
    }

    /**
     * Retrieves a registered component by its name.
     *
     * @param name The name of the component to retrieve.
     * @return The component object, or {@code null} if not found.
     */
    public Object require(String name) {
        return COMPLEX_CACHE.get(name);
    }

    /**
     * Retrieves a registered component, prioritizing by name, then by simple class name.
     *
     * @param name  The name of the component.
     * @param clazz The class type of the component.
     * @return The component object, or {@code null} if not found.
     */
    public Object require(String name, Class<?> clazz) {
        Object object = this.require(name);
        if (ObjectKit.isEmpty(object)) {
            object = this.require(clazz.getSimpleName());
        }
        return object;
    }

}
