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
package org.miaixz.bus.core.lang.tuple;

import java.io.Serial;
import java.util.Objects;

/**
 * An immutable triplet consisting of three elements.
 * <p>
 * This class is a basic container for three objects. It is immutable, meaning that once created, the left, middle, and
 * right elements cannot be changed.
 *
 * @param <L> the left element type
 * @param <M> the middle element type
 * @param <R> the right element type
 * @author Kimi Liu
 * @since Java 17+
 */
public class Triplet<L, M, R> extends Pair<L, R> {

    @Serial
    private static final long serialVersionUID = 2852281035663L;

    /**
     * The middle element of this triplet.
     */
    protected M middle;

    /**
     * Constructs a new triplet with the specified left, middle, and right values.
     *
     * @param left   the left value
     * @param middle the middle value
     * @param right  the right value
     */
    public Triplet(final L left, final M middle, final R right) {
        super(left, right);
        this.middle = middle;
    }

    /**
     * Creates a new triplet instance.
     *
     * @param <L>    the left element type
     * @param <M>    the middle element type
     * @param <R>    the right element type
     * @param left   the left value, may be null
     * @param middle the middle value, may be null
     * @param right  the right value, may be null
     * @return a new {@code Triplet} instance, never null
     */
    public static <L, M, R> Triplet<L, M, R> of(final L left, final M middle, final R right) {
        return new Triplet<>(left, middle, right);
    }

    /**
     * Gets the middle element from this triplet.
     *
     * @return the middle element, may be null
     */
    public M getMiddle() {
        return this.middle;
    }

    /**
     * Compares this triplet to another object for equality.
     * <p>
     * This triplet is equal to the other object if it is also a {@code Triplet} and the left, middle, and right
     * elements are equal.
     *
     * @param o the object to compare to, may be null
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Triplet<?, ?, ?> triplet) {
            return Objects.equals(getLeft(), triplet.getLeft()) && Objects.equals(getMiddle(), triplet.getMiddle())
                    && Objects.equals(getRight(), triplet.getRight());
        }
        return false;
    }

    /**
     * Returns a hash code value for the triplet. The hash code is based on the hash codes of the left, middle, and
     * right elements.
     *
     * @return a hash code value for this triplet
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.left, this.middle, this.right);
    }

    /**
     * Returns a string representation of this triplet. The string representation is in the form {@code "Triplet{left=L,
     * middle=M, right=R}"}.
     *
     * @return a string representation of this triplet
     */
    @Override
    public String toString() {
        return "Triplet{" + "left=" + getLeft() + ", middle=" + getMiddle() + ", right=" + getRight() + '}';
    }

    /**
     * Creates and returns a clone of this object.
     *
     * @return a clone of this instance
     */
    @Override
    public Triplet<L, M, R> clone() {
        return (Triplet<L, M, R>) super.clone();
    }

}
