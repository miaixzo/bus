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
