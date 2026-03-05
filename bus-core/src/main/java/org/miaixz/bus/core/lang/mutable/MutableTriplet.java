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
package org.miaixz.bus.core.lang.mutable;

import java.io.Serial;

import org.miaixz.bus.core.lang.tuple.Triplet;

/**
 * A mutable triplet object.
 *
 * @param <L> The type of the left value.
 * @param <M> The type of the middle value.
 * @param <R> The type of the right value.
 * @author Kimi Liu
 * @since Java 17+
 */
public class MutableTriplet<L, M, R> extends Triplet<L, M, R> implements Mutable<MutableTriplet<L, M, R>> {

    @Serial
    private static final long serialVersionUID = 2852271951206L;

    /**
     * Constructs a new {@code MutableTriplet} with the specified left, middle, and right values.
     *
     * @param left   The initial left value.
     * @param middle The initial middle value.
     * @param right  The initial right value.
     */
    public MutableTriplet(final L left, final M middle, final R right) {
        super(left, middle, right);
    }

    /**
     * Creates a new {@code MutableTriplet}.
     *
     * @param <L>    The type of the left value.
     * @param <M>    The type of the middle value.
     * @param <R>    The type of the right value.
     * @param left   The initial left value.
     * @param middle The initial middle value.
     * @param right  The initial right value.
     * @return A new {@code MutableTriplet} instance.
     */
    public static <L, M, R> MutableTriplet<L, M, R> of(final L left, final M middle, final R right) {
        return new MutableTriplet<>(left, middle, right);
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @Override
    public MutableTriplet<L, M, R> get() {
        return this;
    }

    /**
     * Set method.
     */
    @Override
    public void set(final MutableTriplet<L, M, R> value) {
        this.left = value.left;
        this.middle = value.middle;
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
     * Sets the middle value.
     *
     * @param middle The new middle value.
     */
    public void setMiddle(final M middle) {
        this.middle = middle;
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
