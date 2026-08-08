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
package org.miaixz.bus.core.xyz;

import java.util.stream.Stream;

import org.miaixz.bus.core.center.function.PredicateX;

/**
 * Creates and combines predicates.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PredicateKit {

    /**
     * Constructs a new PredicateKit instance.
     */
    public PredicateKit() {
        // No initialization required.
    }

    /**
     * Creates a predicate that always returns true.
     *
     * @param <T> The parameter type.
     * @return A predicate that matches everything.
     */
    public static <T> PredicateX<T> alwaysTrue() {
        return method -> true;
    }

    /**
     * Coerces a {@code PredicateX<? super T>} to {@code PredicateX<T>}.
     *
     * @param <T>       The parameter type.
     * @param predicate The {@link PredicateX}.
     * @return The coerced {@link PredicateX}.
     */
    static <T> PredicateX<T> coerce(final PredicateX<? super T> predicate) {
        return (PredicateX<T>) predicate;
    }

    /**
     * Negates a predicate.
     *
     * @param predicate The predicate.
     * @param <T>       The parameter type.
     * @return The negated {@link PredicateX}.
     */
    public static <T> PredicateX<T> negate(final PredicateX<T> predicate) {
        return predicate.negate();
    }

    /**
     * Combines multiple predicates with a logical AND.
     *
     * @param <T>        The type of the object being tested.
     * @param components The predicates to combine.
     * @return The composite predicate.
     */
    public static <T> PredicateX<T> and(final Iterable<PredicateX<T>> components) {
        return StreamKit.of(components, false).reduce(PredicateX::and).orElseGet(() -> o -> true);
    }

    /**
     * Combines multiple predicates with a logical AND.
     *
     * @param <T>        The type of the object being tested.
     * @param components The predicates to combine.
     * @return The composite predicate.
     */
    @SafeVarargs
    public static <T> PredicateX<T> and(final PredicateX<T>... components) {
        return StreamKit.of(components).reduce(PredicateX::and).orElseGet(() -> o -> true);
    }

    /**
     * Combines multiple predicates with a logical OR.
     *
     * @param <T>        The type of the object being tested.
     * @param components The predicates to combine.
     * @return The composite predicate.
     */
    public static <T> PredicateX<T> or(final Iterable<PredicateX<T>> components) {
        return StreamKit.of(components, false).reduce(PredicateX::or).orElseGet(() -> o -> false);
    }

    /**
     * Combines multiple predicates with a logical OR.
     *
     * @param <T>        The type of the object being tested.
     * @param components The predicates to combine.
     * @return The composite predicate.
     */
    @SafeVarargs
    public static <T> PredicateX<T> or(final PredicateX<T>... components) {
        return StreamKit.of(components).reduce(PredicateX::or).orElseGet(() -> o -> false);
    }

    /**
     * Creates a predicate that returns `true` only if none of the component predicates match.
     *
     * @param <T>        The type of the object being tested.
     * @param components The predicates to combine.
     * @return The composite predicate.
     */
    @SafeVarargs
    public static <T> PredicateX<T> none(final PredicateX<T>... components) {
        return t -> Stream.of(components).noneMatch(matcher -> matcher.test(t));
    }

}
