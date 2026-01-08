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
package org.miaixz.bus.core.lang.mutable;

import java.io.Serial;

import org.miaixz.bus.core.lang.tuple.Pair;

/**
 * A mutable pair object.
 *
 * @param <L> The type of the left value.
 * @param <R> The type of the right value.
 * @author Kimi Liu
 * @since Java 17+
 */
public class MutablePair<L, R> extends Pair<L, R> implements Mutable<MutablePair<L, R>> {

    @Serial
    private static final long serialVersionUID = 2852271208227L;

    /**
     * Constructs a new {@code MutablePair} with the specified left and right values.
     *
     * @param left  The initial left value.
     * @param right The initial right value.
     */
    public MutablePair(final L left, final R right) {
        super(left, right);
    }

    /**
     * Creates a new {@code MutablePair}.
     *
     * @param <L>   The type of the left value.
     * @param <R>   The type of the right value.
     * @param left  The initial left value.
     * @param right The initial right value.
     * @return A new {@code MutablePair} instance.
     */
    public static <L, R> MutablePair<L, R> of(final L left, final R right) {
        return new MutablePair<>(left, right);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @Override
    public MutablePair<L, R> get() {
        return this;
    }

    /**
     * Set method.
     */
    @Override
    public void set(final MutablePair<L, R> value) {
        this.left = value.left;
        this.right = value.right;
    }

    /**
     * Sets the left value.
     *
     * @param left The new left value.
     */
    public void setLeft(final L left) {
        this.left = left;
    }

    /**
     * Sets the right value.
     *
     * @param right The new right value.
     */
    public void setRight(final R right) {
        this.right = right;
    }

}
