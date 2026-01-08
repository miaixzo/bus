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
package org.miaixz.bus.cache.magic;

/**
 * A generic, immutable container for a pair of objects.
 * <p>
 * This class is used to store two related objects (a left and a right value) as a single unit. It is immutable; its
 * contents cannot be changed after creation.
 * </p>
 *
 * @param <L> The type of the left value.
 * @param <R> The type of the right value.
 * @author Kimi Liu
 * @since Java 17+
 */
public class CachePair<L, R> {

    /**
     * The left value in the pair.
     */
    private final L left;

    /**
     * The right value in the pair.
     */
    private final R right;

    /**
     * Private constructor to enforce instantiation via the factory method.
     *
     * @param left  The left value.
     * @param right The right value.
     */
    private CachePair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Creates a new {@code CachePair} instance.
     *
     * @param <L>   The type of the left value.
     * @param <R>   The type of the right value.
     * @param left  The left value.
     * @param right The right value.
     * @return A new {@code CachePair} instance containing the provided values.
     */
    public static <L, R> CachePair<L, R> of(L left, R right) {
        return new CachePair<>(left, right);
    }

    /**
     * Gets the left value from the pair.
     *
     * @return The left value.
     */
    public L getLeft() {
        return left;
    }

    /**
     * Gets the right value from the pair.
     *
     * @return The right value.
     */
    public R getRight() {
        return right;
    }

}
