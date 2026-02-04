/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
