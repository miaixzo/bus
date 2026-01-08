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
