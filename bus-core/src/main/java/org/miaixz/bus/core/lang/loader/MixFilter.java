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
