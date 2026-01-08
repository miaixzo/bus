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
 * @since Java 17+
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
