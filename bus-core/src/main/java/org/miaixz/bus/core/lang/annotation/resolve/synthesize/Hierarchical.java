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
package org.miaixz.bus.core.lang.annotation.resolve.synthesize;

import java.util.Comparator;

/**
 * Describes an object that exists within a hierarchical structure relative to a reference object.
 *
 * <p>
 * The position of such an object within the coordinate system (with the reference object as the origin) can be
 * described by {@link #getVerticalDistance()} and {@link #getHorizontalDistance()}. When ordering implementations by
 * priority, objects closer to {@link #getRoot()} have higher priority. The default
 * {@link #DEFAULT_HIERARCHICAL_COMPARATOR} implements this ordering rule. Comparisons between objects with the same
 * {@link #getRoot()} are generally meaningful.
 *
 * <p>
 * A {@link Selector} interface is also provided for choosing the most appropriate object from two {@link Hierarchical}
 * implementations. Four built-in selectors are provided:
 * <ul>
 * <li>{@link Selector#NEAREST_AND_OLDEST_PRIORITY}: returns the object closer to root; prefers old on tie;</li>
 * <li>{@link Selector#NEAREST_AND_NEWEST_PRIORITY}: returns the object closer to root; prefers new on tie;</li>
 * <li>{@link Selector#FARTHEST_AND_OLDEST_PRIORITY}: returns the object farther from root; prefers old on tie;</li>
 * <li>{@link Selector#FARTHEST_AND_NEWEST_PRIORITY}: returns the object farther from root; prefers new on tie;</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Hierarchical extends Comparable<Hierarchical> {

    /**
     * Default comparator ordering by {@link #getVerticalDistance()} then {@link #getHorizontalDistance()}.
     */
    Comparator<Hierarchical> DEFAULT_HIERARCHICAL_COMPARATOR = Comparator.comparing(Hierarchical::getVerticalDistance)
            .thenComparing(Hierarchical::getHorizontalDistance);

    /**
     * Compares this object with another {@link Hierarchical} using {@link #DEFAULT_HIERARCHICAL_COMPARATOR}.
     *
     * @param o the other {@link Hierarchical} object to compare with
     * @return a negative integer, zero, or a positive integer
     */
    @Override
    default int compareTo(final Hierarchical o) {
        return DEFAULT_HIERARCHICAL_COMPARATOR.compare(this, o);
    }

    /**
     * Returns the reference object, i.e., the object at coordinate {@code (0, 0)}. When this object itself is the
     * reference, this method should return {@code this}.
     *
     * @return the reference object
     */
    Object getRoot();

    /**
     * Returns the vertical distance from this object to the reference object. By default, this is the number of
     * hierarchy levels between this object and the reference.
     *
     * @return the vertical distance from the root
     */
    int getVerticalDistance();

    /**
     * Returns the horizontal distance from this object to the reference object. By default, this is the scan order of
     * this object among others at the same vertical distance.
     *
     * @return the horizontal distance from the root
     */
    int getHorizontalDistance();

    /**
     * Selector interface for choosing the most appropriate {@link Hierarchical} object from two candidates.
     */
    @FunctionalInterface
    interface Selector {

        /**
         * Selector that returns the object closer to root; prefers old object on equal distance.
         */
        Selector NEAREST_AND_OLDEST_PRIORITY = new NearestAndOldestPrioritySelector();

        /**
         * Selector that returns the object closer to root; prefers new object on equal distance.
         */
        Selector NEAREST_AND_NEWEST_PRIORITY = new NearestAndNewestPrioritySelector();

        /**
         * Selector that returns the object farther from root; prefers old object on equal distance.
         */
        Selector FARTHEST_AND_OLDEST_PRIORITY = new FarthestAndOldestPrioritySelector();

        /**
         * Selector that returns the object farther from root; prefers new object on equal distance.
         */
        Selector FARTHEST_AND_NEWEST_PRIORITY = new FarthestAndNewestPrioritySelector();

        /**
         * Compares two {@link Hierarchical} objects and returns the more appropriate one.
         *
         * @param <T>  the type of the hierarchical object
         * @param prev the existing object; must not be {@code null}
         * @param next the new object; must not be {@code null}
         * @return the selected object
         */
        <T extends Hierarchical> T choose(T prev, T next);

        /**
         * Selector that returns the object closer to root; prefers old object on equal distance.
         */
        class NearestAndOldestPrioritySelector implements Selector {

            /**
             * Returns {@code newAnnotation} if it is closer to root than {@code oldAnnotation}; otherwise returns
             * {@code oldAnnotation}.
             *
             * @param <T>           the type of the hierarchical object
             * @param oldAnnotation the existing object
             * @param newAnnotation the new object
             * @return the selected object
             */
            @Override
            public <T extends Hierarchical> T choose(final T oldAnnotation, final T newAnnotation) {
                return newAnnotation.getVerticalDistance() < oldAnnotation.getVerticalDistance() ? newAnnotation
                        : oldAnnotation;
            }
        }

        /**
         * Selector that returns the object closer to root; prefers new object on equal distance.
         */
        class NearestAndNewestPrioritySelector implements Selector {

            /**
             * Returns {@code newAnnotation} if it is at least as close to root as {@code oldAnnotation}; otherwise
             * returns {@code oldAnnotation}.
             *
             * @param <T>           the type of the hierarchical object
             * @param oldAnnotation the existing object
             * @param newAnnotation the new object
             * @return the selected object
             */
            @Override
            public <T extends Hierarchical> T choose(final T oldAnnotation, final T newAnnotation) {
                return newAnnotation.getVerticalDistance() <= oldAnnotation.getVerticalDistance() ? newAnnotation
                        : oldAnnotation;
            }
        }

        /**
         * Selector that returns the object farther from root; prefers old object on equal distance.
         */
        class FarthestAndOldestPrioritySelector implements Selector {

            /**
             * Returns {@code newAnnotation} if it is farther from root than {@code oldAnnotation}; otherwise returns
             * {@code oldAnnotation}.
             *
             * @param <T>           the type of the hierarchical object
             * @param oldAnnotation the existing object
             * @param newAnnotation the new object
             * @return the selected object
             */
            @Override
            public <T extends Hierarchical> T choose(final T oldAnnotation, final T newAnnotation) {
                return newAnnotation.getVerticalDistance() > oldAnnotation.getVerticalDistance() ? newAnnotation
                        : oldAnnotation;
            }
        }

        /**
         * Selector that returns the object farther from root; prefers new object on equal distance.
         */
        class FarthestAndNewestPrioritySelector implements Selector {

            /**
             * Returns {@code newAnnotation} if it is at least as far from root as {@code oldAnnotation}; otherwise
             * returns {@code oldAnnotation}.
             *
             * @param <T>           the type of the hierarchical object
             * @param oldAnnotation the existing object
             * @param newAnnotation the new object
             * @return the selected object
             */
            @Override
            public <T extends Hierarchical> T choose(final T oldAnnotation, final T newAnnotation) {
                return newAnnotation.getVerticalDistance() >= oldAnnotation.getVerticalDistance() ? newAnnotation
                        : oldAnnotation;
            }
        }

    }

}
