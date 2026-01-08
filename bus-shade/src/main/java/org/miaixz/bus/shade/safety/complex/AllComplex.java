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
