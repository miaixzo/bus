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

import org.miaixz.bus.shade.safety.Complex;

/**
 * A {@link Complex} implementation that represents a logical AND operation on a collection of sub-filters. If no
 * sub-filters are present, it is considered to always match (all rules are satisfied). If any sub-filter does not
 * match, the entire {@code AllComplex} does not match.
 *
 * @param <E> The type of entry to be filtered.
 * @author Kimi Liu
 * @since Java 17+
 */
public class AllComplex<E> extends MixComplex<E> implements Complex<E> {

    /**
     * Constructs an empty {@code AllComplex} filter. When no sub-filters are added, this filter will always return
     * {@code true} for {@link #on(Object)}.
     */
    public AllComplex() {
        super(null);
    }

    /**
     * Constructs an {@code AllComplex} filter with an initial collection of sub-filters.
     *
     * @param filters The initial collection of {@link Complex} sub-filters.
     */
    public AllComplex(Collection<? extends Complex<? extends E>> filters) {
        super(filters);
    }

    /**
     * Adds a new sub-filter to this {@code AllComplex} filter.
     *
     * @param filter The {@link Complex} filter to add.
     * @return This {@code AllComplex} instance, allowing for method chaining.
     */
    @Override
    public AllComplex<E> mix(Complex<? extends E> filter) {
        add(filter);
        return this;
    }

    /**
     * Evaluates the given entry against all sub-filters using a logical AND operation. If there are no sub-filters, it
     * returns {@code true}. If any sub-filter returns {@code false}, this method immediately returns {@code false}.
     *
     * @param entry The entry to be evaluated.
     * @return {@code true} if all sub-filters return {@code true} or if there are no sub-filters; {@code false}
     *         otherwise.
     */
    @Override
    public boolean on(E entry) {
        Complex[] filters = this.filters.toArray(new Complex[0]);
        for (Complex filter : filters) {
            if (!filter.on(entry)) {
                return false;
            }
        }
        return true;
    }

}
