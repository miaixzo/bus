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
package org.miaixz.bus.core.lang.loader;

import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.miaixz.bus.core.io.resource.Resource;

/**
 * Abstract base class for resource loaders. Provides common methods for loading resources.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class ResourceLoader implements Loader {

    /**
     * Loads all resources at the specified path, non-recursively and without any specific filter. This is equivalent to
     * calling {@code load(path, false, Filters.ALWAYS)}.
     *
     * @param path The resource path.
     * @return An enumeration of resource objects.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public Enumeration<Resource> load(String path) throws IOException {
        return load(path, false, Filters.ALWAYS);
    }

    /**
     * Loads all resources at the specified path, with an option for recursive loading and without any specific filter.
     * This is equivalent to calling {@code load(path, recursively, Filters.ALWAYS)}.
     *
     * @param path        The resource path.
     * @param recursively Whether to load resources recursively.
     * @return An enumeration of resource objects.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public Enumeration<Resource> load(String path, boolean recursively) throws IOException {
        return load(path, recursively, Filters.ALWAYS);
    }

    /**
     * Loads all resources at the specified path, recursively, and applies a filter. This is equivalent to calling
     * {@code load(path, true, filter)}.
     *
     * @param path   The resource path.
     * @param filter The filter to apply to resources.
     * @return An enumeration of resource objects.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public Enumeration<Resource> load(String path, Filter filter) throws IOException {
        return load(path, true, filter);
    }

    /**
     * Abstract base class for resource enumerators, providing common functionality for iterating over resources.
     */
    protected abstract static class ResourceEnumerator implements Enumeration<Resource> {

        /**
         * The next resource in the enumeration, or {@code null} if not yet determined or no more elements.
         */
        protected Resource next;

        /**
         * Returns the next element in the enumeration.
         *
         * @return The next element in the enumeration.
         * @throws NoSuchElementException If no more elements exist.
         */
        @Override
        public Resource nextElement() {
            if (hasMoreElements()) {
                Resource resource = next;
                next = null;
                return resource;
            } else {
                throw new NoSuchElementException();
            }
        }
    }

}
