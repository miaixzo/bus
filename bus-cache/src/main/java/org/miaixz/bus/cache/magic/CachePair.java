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
