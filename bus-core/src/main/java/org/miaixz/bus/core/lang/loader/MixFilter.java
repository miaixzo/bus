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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A composite filter that internally maintains a {@link LinkedHashSet} of {@link Filter} instances. It provides methods
 * for adding, removing, and chaining multiple child filters. The specific filtering logic is determined by subclasses.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class MixFilter implements Filter {

    /**
     * The set of filters contained within this composite filter.
     */
    protected final Set<Filter> filters;

    /**
     * Constructs a {@code MixFilter} with the given array of initial filters.
     *
     * @param filters An array of {@link Filter} instances to initialize the composite filter.
     */
    protected MixFilter(Filter... filters) {
        this(Arrays.asList(filters));
    }

    /**
     * Constructs a {@code MixFilter} with the given collection of initial filters.
     *
     * @param filters A collection of {@link Filter} instances to initialize the composite filter.
     */
    protected MixFilter(Collection<? extends Filter> filters) {
        this.filters = null != filters ? new LinkedHashSet<>(filters) : new LinkedHashSet<>();
    }

    /**
     * Adds a filter to this composite filter.
     *
     * @param filter The {@link Filter} to add.
     * @return {@code true} if the filter was added successfully (i.e., it was not already present), {@code false}
     *         otherwise.
     */
    public boolean add(Filter filter) {
        return filters.add(filter);
    }

    /**
     * Removes a filter from this composite filter.
     *
     * @param filter The {@link Filter} to remove.
     * @return {@code true} if the filter was removed successfully (i.e., it was present), {@code false} otherwise.
     */
    public boolean remove(Filter filter) {
        return filters.remove(filter);
    }

    /**
     * Supports chaining multiple filters. This method internally calls {@link MixFilter#add(Filter)} and returns
     * {@code this}. This method is abstract to force subclasses to override it with their own return type for proper
     * method chaining.
     *
     * @param filter The {@link Filter} to mix in.
     * @return This {@code MixFilter} instance, for method chaining.
     */
    public abstract MixFilter mix(Filter filter);

}
