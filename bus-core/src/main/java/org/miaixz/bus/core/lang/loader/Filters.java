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

import java.util.Collection;

/**
 * Utility class for creating and combining {@link Filter} instances.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class Filters {

    /**
     * Constructs a new Filters. Utility class constructor for static access.
     */
    private Filters() {
    }

    /**
     * A filter that always returns {@code true}, effectively accepting all resources.
     */
    public static final Filter ALWAYS = (name, url) -> true;

    /**
     * A filter that always returns {@code false}, effectively rejecting all resources.
     */
    public static final Filter NEVER = (name, url) -> false;

    /**
     * Creates a composite filter that performs a logical AND operation on multiple child filters. All child filters
     * must return {@code true} for this filter to return {@code true}.
     *
     * @param filters An array of child filters.
     * @return A new {@link AllFilter} instance combining the given filters with an AND operation.
     */
    public static Filter all(Filter... filters) {
        return new AllFilter(filters);
    }

    /**
     * Creates a composite filter that performs a logical AND operation on multiple child filters. All child filters
     * must return {@code true} for this filter to return {@code true}.
     *
     * @param filters A collection of child filters.
     * @return A new {@link AllFilter} instance combining the given filters with an AND operation.
     */
    public static Filter all(Collection<? extends Filter> filters) {
        return new AllFilter(filters);
    }

    /**
     * Creates a composite filter that performs a logical AND operation on multiple child filters. All child filters
     * must return {@code true} for this filter to return {@code true}. This method is an alias for
     * {@link #all(Filter...)}.
     *
     * @param filters An array of child filters.
     * @return A new {@link AllFilter} instance combining the given filters with an AND operation.
     */
    public static Filter and(Filter... filters) {
        return all(filters);
    }

    /**
     * Creates a composite filter that performs a logical AND operation on multiple child filters. All child filters
     * must return {@code true} for this filter to return {@code true}. This method is an alias for
     * {@link #all(Collection)}.
     *
     * @param filters A collection of child filters.
     * @return A new {@link AllFilter} instance combining the given filters with an AND operation.
     */
    public static Filter and(Collection<? extends Filter> filters) {
        return all(filters);
    }

    /**
     * Creates a composite filter that performs a logical OR operation on multiple child filters. If any child filter
     * returns {@code true}, this filter will return {@code true}.
     *
     * @param filters An array of child filters.
     * @return A new {@link AnyFilter} instance combining the given filters with an OR operation.
     */
    public static Filter any(Filter... filters) {
        return new AnyFilter(filters);
    }

    /**
     * Creates a composite filter that performs a logical OR operation on multiple child filters. If any child filter
     * returns {@code true}, this filter will return {@code true}.
     *
     * @param filters A collection of child filters.
     * @return A new {@link AnyFilter} instance combining the given filters with an OR operation.
     */
    public static Filter any(Collection<? extends Filter> filters) {
        return new AnyFilter(filters);
    }

    /**
     * Creates a composite filter that performs a logical OR operation on multiple child filters. If any child filter
     * returns {@code true}, this filter will return {@code true}. This method is an alias for {@link #any(Filter...)}.
     *
     * @param filters An array of child filters.
     * @return A new {@link AnyFilter} instance combining the given filters with an OR operation.
     */
    public static Filter or(Filter... filters) {
        return any(filters);
    }

    /**
     * Creates a composite filter that performs a logical OR operation on multiple child filters. If any child filter
     * returns {@code true}, this filter will return {@code true}. This method is an alias for {@link #any(Collection)}.
     *
     * @param filters A collection of child filters.
     * @return A new {@link AnyFilter} instance combining the given filters with an OR operation.
     */
    public static Filter or(Collection<? extends Filter> filters) {
        return any(filters);
    }

}
