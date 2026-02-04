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
