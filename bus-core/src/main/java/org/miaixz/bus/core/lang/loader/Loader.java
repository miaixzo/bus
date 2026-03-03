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
package org.miaixz.bus.core.lang.loader;

import java.io.IOException;
import java.util.Enumeration;

import org.miaixz.bus.core.io.resource.Resource;

/**
 * Resource loader, fully adopting lazy loading logic, allowing resource loading to be deferred until
 * {@link Enumeration#hasMoreElements()} is called, avoiding unnecessary eager pre-loading.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Loader {

    /**
     * Loads all resources at the specified path. This is equivalent to calling
     * {@code Loader.load(path, false, Filters.ALWAYS)}. Typically, resources are not loaded recursively, but subclasses
     * can change this behavior, for example, an ANT-style path resource loader can determine whether to load
     * recursively based on the expression.
     *
     * @param path The resource path.
     * @return An enumeration of resource objects.
     * @throws IOException If an I/O error occurs.
     */
    Enumeration<Resource> load(String path) throws IOException;

    /**
     * Loads all resources at the specified path. This is equivalent to calling
     * {@code Loader.load(path, recursively, Filters.ALWAYS)}.
     *
     * @param path        The resource path.
     * @param recursively Whether to load resources recursively.
     * @return An enumeration of resource objects.
     * @throws IOException If an I/O error occurs.
     */
    Enumeration<Resource> load(String path, boolean recursively) throws IOException;

    /**
     * Loads all resources at the specified path that satisfy the given filter. This is equivalent to calling
     * {@code Loader.load(path, true, filter)}.
     *
     * @param path   The resource path.
     * @param filter The filter to apply to resources.
     * @return An enumeration of resource objects.
     * @throws IOException If an I/O error occurs.
     */
    Enumeration<Resource> load(String path, Filter filter) throws IOException;

    /**
     * Loads dynamic library information, e.g., .dll/.so files.
     *
     * @param path  The resource path.
     * @param clazz The class information.
     * @return An enumeration of resource objects.
     * @throws IOException If an I/O error occurs.
     */
    default Enumeration<Resource> load(String path, Class<?> clazz) throws IOException {
        return load(path);
    }

    /**
     * Loads all resources at the specified path that satisfy the given filter, with an option for recursive loading.
     *
     * @param path        The resource path.
     * @param recursively Whether to load resources recursively.
     * @param filter      The filter to apply to resources.
     * @return An enumeration of resource objects.
     * @throws IOException If an I/O error occurs.
     */
    Enumeration<Resource> load(String path, boolean recursively, Filter filter) throws IOException;

}
