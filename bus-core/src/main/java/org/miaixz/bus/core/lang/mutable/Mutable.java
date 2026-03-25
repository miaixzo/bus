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

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.lang.Optional;

/**
 * An interface for a mutable value wrapper.
 * <p>
 * Unlike {@link Optional} or {@link java.util.Optional}, the methods in all implementing classes of this interface
 * <b>do not perform `null` checks</b>. Therefore, it is the user's responsibility to handle `null` values to avoid a
 * {@link NullPointerException}.
 *
 * @param <T> The type of the wrapped value.
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Mutable<T> {

    /**
     * Creates a new {@link MutableBoolean} object.
     *
     * @param value The initial value.
     * @return A new {@link MutableBoolean}.
     */
    static MutableBoolean of(final boolean value) {
        return new MutableBoolean(value);
    }

    /**
     * Creates a new {@link MutableByte} object.
     *
     * @param value The initial value.
     * @return A new {@link MutableByte}.
     */
    static MutableByte of(final byte value) {
        return new MutableByte(value);
    }

    /**
     * Creates a new {@link MutableFloat} object.
     *
     * @param value The initial value.
     * @return A new {@link MutableFloat}.
     */
    static MutableFloat of(final float value) {
        return new MutableFloat(value);
    }

    /**
     * Creates a new {@link MutableInt} object.
     *
     * @param value The initial value.
     * @return A new {@link MutableInt}.
     */
    static MutableInt of(final int value) {
        return new MutableInt(value);
    }

    /**
     * Creates a new {@link MutableLong} object.
     *
     * @param value The initial value.
     * @return A new {@link MutableLong}.
     */
    static MutableLong of(final long value) {
        return new MutableLong(value);
    }

    /**
     * Creates a new {@link MutableDouble} object.
     *
     * @param value The initial value.
     * @return A new {@link MutableDouble}.
     */
    static MutableDouble of(final double value) {
        return new MutableDouble(value);
    }

    /**
     * Creates a new {@link MutableShort} object.
     *
     * @param value The initial value.
     * @return A new {@link MutableShort}.
     */
    static MutableShort of(final short value) {
        return new MutableShort(value);
    }

    /**
     * Creates a new {@link MutableObject} object.
     *
     * @param <T>   The type of the value.
     * @param value The initial value.
     * @return A new {@link MutableObject}.
     */
    static <T> MutableObject<T> of(final T value) {
        return new MutableObject<>(value);
    }

    /**
     * Gets the wrapped value.
     *
     * @return The wrapped value.
     */
    T get();

    /**
     * Sets the wrapped value.
     *
     * @param value The new value.
     */
    void set(T value);

    /**
     * Applies an operator to the wrapped value and updates it.
     *
     * @param operator The operator to apply.
     * @return This mutable object.
     */
    default Mutable<T> map(final UnaryOperator<T> operator) {
        set(operator.apply(get()));
        return this;
    }

    /**
     * Performs an action on the wrapped value.
     *
     * @param consumer The action to perform.
     * @return This mutable object.
     */
    default Mutable<T> peek(final Consumer<T> consumer) {
        consumer.accept(get());
        return this;
    }

    /**
     * Tests if the wrapped value satisfies a predicate.
     *
     * @param predicate The predicate to apply.
     * @return `true` if the value satisfies the predicate.
     */
    default boolean test(final Predicate<T> predicate) {
        return predicate.test(get());
    }

    /**
     * Gets the value and wraps it in an {@link Optional}.
     *
     * @return An {@link Optional} containing the value.
     */
    default Optional<T> toOpt() {
        return to(Optional::ofNullable);
    }

    /**
     * Gets the value and converts it to another type using the given function. Note: The function will be called even
     * if the wrapped value is `null`.
     *
     * @param function The conversion function.
     * @param <R>      The target type.
     * @return The converted value.
     */
    default <R> R to(final Function<T, R> function) {
        Objects.requireNonNull(function);
        return function.apply(get());
    }

}
