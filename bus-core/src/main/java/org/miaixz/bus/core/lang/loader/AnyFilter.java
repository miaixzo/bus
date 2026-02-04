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
package org.miaixz.bus.core.lang.loader;

import java.net.URL;
import java.util.Collection;

/**
 * A composite filter that implements an 'OR' logic. If any of the contained filters return {@code true}, this filter
 * will return {@code true}. If no filters are present, or if all filters return {@code false}, this filter will return
 * {@code false}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnyFilter extends MixFilter implements Filter {

    /**
     * Constructs an {@code AnyFilter} with the given array of filters.
     *
     * @param filters An array of {@link Filter} instances.
     */
    public AnyFilter(Filter... filters) {
        super(filters);
    }

    /**
     * Constructs an {@code AnyFilter} with the given collection of filters.
     *
     * @param filters A collection of {@link Filter} instances.
     */
    public AnyFilter(Collection<? extends Filter> filters) {
        super(filters);
    }

    /**
     * Filters a resource. Returns {@code true} if any contained filter returns {@code true}, otherwise returns
     * {@code false}.
     *
     * @param name The name of the resource (relative path).
     * @param url  The URL of the resource.
     * @return {@code true} if any filter accepts the resource, {@code false} otherwise.
     */
    @Override
    public boolean filtrate(String name, URL url) {
        Filter[] filters = this.filters.toArray(new Filter[0]);
        for (Filter filter : filters) {
            if (filter.filtrate(name, url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a filter to this composite filter.
     *
     * @param filter The {@link Filter} to add.
     * @return This {@code AnyFilter} instance, for method chaining.
     */
    public AnyFilter mix(Filter filter) {
        add(filter);
        return this;
    }

}
