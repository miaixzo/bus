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
