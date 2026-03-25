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

import java.net.URL;
import java.util.Collection;

/**
 * A composite filter that implements an 'AND' logic. All contained filters must return {@code true} for this filter to
 * return {@code true}. If any filter returns {@code false}, this filter immediately returns {@code false}. If no
 * filters are present, it is considered to satisfy all conditions (returns {@code true}).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AllFilter extends MixFilter implements Filter {

    /**
     * Constructs an {@code AllFilter} with the given array of filters.
     *
     * @param filters An array of {@link Filter} instances.
     */
    public AllFilter(Filter... filters) {
        super(filters);
    }

    /**
     * Constructs an {@code AllFilter} with the given collection of filters.
     *
     * @param filters A collection of {@link Filter} instances.
     */
    public AllFilter(Collection<? extends Filter> filters) {
        super(filters);
    }

    /**
     * Filters a resource. Returns {@code true} if all contained filters return {@code true}, otherwise returns
     * {@code false}.
     *
     * @param name The name of the resource (relative path).
     * @param url  The URL of the resource.
     * @return {@code true} if all filters accept the resource, {@code false} otherwise.
     */
    @Override
    public boolean filtrate(String name, URL url) {
        Filter[] filters = this.filters.toArray(new Filter[0]);
        for (Filter filter : filters) {
            if (!filter.filtrate(name, url)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a filter to this composite filter.
     *
     * @param filter The {@link Filter} to add.
     * @return This {@code AllFilter} instance, for method chaining.
     */
    public AllFilter mix(Filter filter) {
        add(filter);
        return this;
    }

}
