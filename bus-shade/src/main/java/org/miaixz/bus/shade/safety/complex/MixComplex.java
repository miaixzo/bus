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
package org.miaixz.bus.shade.safety.complex;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.miaixz.bus.shade.safety.Complex;

/**
 * An abstract base class for composite filters that combine multiple {@link Complex} filters. Subclasses define the
 * logical operation (e.g., AND, OR) applied to the contained filters.
 *
 * @param <E> The type of entry to be filtered.
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class MixComplex<E> implements Complex<E> {

    /**
     * The set of {@link Complex} sub-filters contained within this composite filter. A {@link LinkedHashSet} is used to
     * maintain insertion order.
     */
    protected final Set<Complex<? extends E>> filters;

    /**
     * Constructs an empty {@code MixComplex} with no initial sub-filters.
     */
    protected MixComplex() {
        this(null);
    }

    /**
     * Constructs a {@code MixComplex} with an initial collection of sub-filters.
     *
     * @param filters The initial collection of {@link Complex} sub-filters.
     */
    protected MixComplex(Collection<? extends Complex<? extends E>> filters) {
        this.filters = null != filters ? new LinkedHashSet<>(filters) : new LinkedHashSet<>();
    }

    /**
     * Adds a new sub-filter to this composite filter.
     *
     * @param filter The {@link Complex} filter to add.
     * @return {@code true} if the filter was added successfully (i.e., it was not already present); {@code false}
     *         otherwise.
     */
    public boolean add(Complex<? extends E> filter) {
        return filters.add(filter);
    }

    /**
     * Removes a sub-filter from this composite filter.
     *
     * @param filter The {@link Complex} filter to remove.
     * @return {@code true} if the filter was removed successfully (i.e., it was present); {@code false} otherwise.
     */
    public boolean remove(Complex<? extends E> filter) {
        return filters.remove(filter);
    }

    /**
     * Abstract method to mix (add) a new filter into the composite. Subclasses must implement this to define how new
     * filters are integrated into their specific logical operation.
     *
     * @param filter The {@link Complex} filter to mix in.
     * @return This {@code MixComplex} instance, allowing for method chaining.
     */
    public abstract MixComplex<E> mix(Complex<? extends E> filter);

}
