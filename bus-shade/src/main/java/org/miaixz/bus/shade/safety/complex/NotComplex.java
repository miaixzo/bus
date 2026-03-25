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

import org.miaixz.bus.shade.safety.Complex;

/**
 * A {@link Complex} implementation that negates the result of a delegated filter. This effectively creates a logical
 * NOT operation.
 *
 * @param <E> The type of entry to be filtered.
 * @author Kimi Liu
 * @since Java 21+
 */
public class NotComplex<E> implements Complex<E> {

    /**
     * The delegated {@link Complex} filter whose result will be negated.
     */
    private final Complex<E> delegate;

    /**
     * Constructs a new {@code NotComplex} with the specified delegate filter.
     *
     * @param delegate The {@link Complex} filter to negate.
     */
    public NotComplex(Complex<E> delegate) {
        this.delegate = delegate;
    }

    /**
     * Evaluates the given entry by negating the result of the delegated filter.
     *
     * @param entry The entry to be evaluated.
     * @return {@code true} if the delegated filter returns {@code false}; {@code false} if the delegated filter returns
     *         {@code true}.
     */
    @Override
    public boolean on(E entry) {
        return !delegate.on(entry);
    }

}
