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

import java.net.URL;
import java.util.Collection;

/**
 * A composite filter that implements an 'AND' logic. All contained filters must return {@code true} for this filter to
 * return {@code true}. If any filter returns {@code false}, this filter immediately returns {@code false}. If no
 * filters are present, it is considered to satisfy all conditions (returns {@code true}).
 *
 * @author Kimi Liu
 * @since Java 17+
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
