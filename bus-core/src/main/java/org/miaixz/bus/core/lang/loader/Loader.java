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
