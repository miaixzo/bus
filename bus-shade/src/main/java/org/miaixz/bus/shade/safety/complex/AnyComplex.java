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
 * A {@link Complex} implementation that represents a logical OR operation on a collection of sub-filters. If any
 * sub-filter matches, the entire {@code AnyComplex} matches. If no sub-filters are present, it is considered to never
 * match (no rules are satisfied).
 *
 * @param <E> The type of entry to be filtered.
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnyComplex<E> extends MixComplex<E> implements Complex<E> {

    /**
     * Constructs an empty {@code AnyComplex} filter. When no sub-filters are added, this filter will always return
     * {@code false} for {@link #on(Object)}.
     */
    public AnyComplex() {
        super(null);
    }

    /**
     * Constructs an {@code AnyComplex} filter with an initial collection of sub-filters.
     *
     * @param filters The initial collection of {@link Complex} sub-filters.
     */
    public AnyComplex(Collection<? extends Complex<? extends E>> filters) {
        super(filters);
    }

    /**
     * Adds a new sub-filter to this {@code AnyComplex} filter.
     *
     * @param filter The {@link Complex} filter to add.
     * @return This {@code AnyComplex} instance, allowing for method chaining.
     */
    @Override
    public AnyComplex<E> mix(Complex<? extends E> filter) {
        add(filter);
        return this;
    }

    /**
     * Evaluates the given entry against all sub-filters using a logical OR operation. If there are no sub-filters, it
     * returns {@code false}. If any sub-filter returns {@code true}, this method immediately returns {@code true}.
     *
     * @param entry The entry to be evaluated.
     * @return {@code true} if any sub-filter returns {@code true}; {@code false} otherwise or if there are no
     *         sub-filters.
     */
    @Override
    public boolean on(E entry) {
        Complex[] filters = this.filters.toArray(new Complex[0]);
        for (Complex filter : filters) {
            if (filter.on(entry)) {
                return true;
            }
        }
        return false;
    }

}
