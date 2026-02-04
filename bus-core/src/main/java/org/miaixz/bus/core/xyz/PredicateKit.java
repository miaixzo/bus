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
package org.miaixz.bus.core.xyz;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Utility class for {@link Predicate}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PredicateKit {

    /**
     * Creates a predicate that always returns true.
     *
     * @param <T> The parameter type.
     * @return A predicate that matches everything.
     */
    public static <T> Predicate<T> alwaysTrue() {
        return method -> true;
    }

    /**
     * Coerces a {@code Predicate<? super T>} to {@code Predicate<T>}.
     *
     * @param <T>       The parameter type.
     * @param predicate The {@link Predicate}.
     * @return The coerced {@link Predicate}.
     */
    static <T> Predicate<T> coerce(final Predicate<? super T> predicate) {
        return (Predicate<T>) predicate;
    }

    /**
     * Negates a predicate.
     *
     * @param predicate The predicate.
     * @param <T>       The parameter type.
     * @return The negated {@link Predicate}.
     */
    public static <T> Predicate<T> negate(final Predicate<T> predicate) {
        return predicate.negate();
    }

    /**
     * Combines multiple predicates with a logical AND.
     *
     * @param <T>        The type of the object being tested.
     * @param components The predicates to combine.
     * @return The composite predicate.
     */
    public static <T> Predicate<T> and(final Iterable<Predicate<T>> components) {
        return StreamKit.of(components, false).reduce(Predicate::and).orElseGet(() -> o -> true);
    }

    /**
     * Combines multiple predicates with a logical AND.
     *
     * @param <T>        The type of the object being tested.
     * @param components The predicates to combine.
     * @return The composite predicate.
     */
    @SafeVarargs
    public static <T> Predicate<T> and(final Predicate<T>... components) {
        return StreamKit.of(components).reduce(Predicate::and).orElseGet(() -> o -> true);
    }

    /**
     * Combines multiple predicates with a logical OR.
     *
     * @param <T>        The type of the object being tested.
     * @param components The predicates to combine.
     * @return The composite predicate.
     */
    public static <T> Predicate<T> or(final Iterable<Predicate<T>> components) {
        return StreamKit.of(components, false).reduce(Predicate::or).orElseGet(() -> o -> false);
    }

    /**
     * Combines multiple predicates with a logical OR.
     *
     * @param <T>        The type of the object being tested.
     * @param components The predicates to combine.
     * @return The composite predicate.
     */
    @SafeVarargs
    public static <T> Predicate<T> or(final Predicate<T>... components) {
        return StreamKit.of(components).reduce(Predicate::or).orElseGet(() -> o -> false);
    }

    /**
     * Creates a predicate that returns `true` only if none of the component predicates match.
     *
     * @param <T>        The type of the object being tested.
     * @param components The predicates to combine.
     * @return The composite predicate.
     */
    @SafeVarargs
    public static <T> Predicate<T> none(final Predicate<T>... components) {
        return t -> Stream.of(components).noneMatch(matcher -> matcher.test(t));
    }

}
