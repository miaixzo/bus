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
package org.miaixz.bus.core.lang;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.function.PredicateX;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.center.stream.EasyStream;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A container object which may or may not contain a non-null value. This class is a copy of {@code java.util.Optional}
 * from JDK 16, with some adjustments and additions, offering more utility functions than JDK 8's {@code Optional}.
 *
 * @param <T> The type of the value held by this {@code Optional}.
 * @author Kimi Liu
 * @see java.util.Optional
 * @since Java 17+
 */
public class Optional<T> {

    /**
     * An empty {@code Optional} instance.
     */
    private static final Optional<?> EMPTY = new Optional<>(null);
    /**
     * The actual value held by this {@code Optional}.
     */
    private final T value;
    /**
     * Stores a {@link Throwable} if an operation within {@link #ofTry(SupplierX)} fails.
     */
    private Throwable throwable;

    /**
     * Constructs an {@code Optional} instance with the given value.
     *
     * @param value The value to be held by this {@code Optional}.
     */
    private Optional(final T value) {
        this.value = value;
    }

    /**
     * Returns an empty {@code Optional} instance.
     *
     * @param <T> The type of the value.
     * @return An empty {@code Optional}.
     */
    public static <T> Optional<T> empty() {
        return (Optional<T>) EMPTY;
    }

    /**
     * Returns an {@code Optional} with the specified non-null value.
     *
     * @param value The non-null value to be present.
     * @param <T>   The type of the value.
     * @return An {@code Optional} with the present value.
     * @throws NullPointerException if the value is {@code null}.
     */
    public static <T> Optional<T> of(final T value) {
        return new Optional<>(Objects.requireNonNull(value));
    }

    /**
     * Returns an {@code Optional} describing the specified value, with an empty {@code Optional} if the value is
     * {@code null}.
     *
     * @param value The value to be present or {@code null}.
     * @param <T>   The type of the value.
     * @return An {@code Optional} with the present value if non-null, otherwise an empty {@code Optional}.
     */
    public static <T> Optional<T> ofNullable(final T value) {
        return value == null ? empty() : new Optional<>(value);
    }

    /**
     * Returns an {@code Optional} describing the specified character sequence, with an empty {@code Optional} if the
     * value is {@code null} or blank.
     *
     * @param <T>   The type of the character sequence.
     * @param value The character sequence to be present or {@code null}/blank.
     * @return An {@code Optional} with the present value if non-null and non-blank, otherwise an empty
     *         {@code Optional}.
     */
    public static <T extends CharSequence> Optional<T> ofBlankAble(final T value) {
        return StringKit.isBlank(value) ? empty() : new Optional<>(value);
    }

    /**
     * Returns an {@code Optional} describing the specified collection, with an empty {@code Optional} if the collection
     * is {@code null} or empty.
     *
     * @param <T>   The type of elements in the collection.
     * @param <R>   The type of the collection.
     * @param value The collection to be present or {@code null}/empty. Supports CharSequence, Map, Iterable, Iterator,
     *              and Array types.
     * @return An {@code Optional} with the present collection if non-null and non-empty, otherwise an empty
     *         {@code Optional}.
     */
    public static <T, R extends Collection<T>> Optional<R> ofEmptyAble(final R value) {
        return ObjectKit.isEmpty(value) ? empty() : new Optional<>(value);
    }

    /**
     * Executes a {@link SupplierX} operation and returns an {@code Optional} containing its result. If the operation
     * throws an exception, an empty {@code Optional} is returned, and the exception is stored internally.
     *
     * @param supplier The operation to execute.
     * @param <T>      The type of the result.
     * @return An {@code Optional} containing the result of the operation, or an empty {@code Optional} if an exception
     *         occurs.
     */
    public static <T> Optional<T> ofTry(final SupplierX<T> supplier) {
        try {
            return ofNullable(supplier.getting());
        } catch (final Throwable e) {
            final Optional<T> empty = new Optional<>(null);
            empty.throwable = e;
            return empty;
        }
    }

    /**
     * Constructs an {@code Optional} from a {@link java.util.Optional} instance.
     *
     * @param optional The {@link java.util.Optional} instance.
     * @param <T>      The type of the value.
     * @return An {@code Optional} with the value from the provided {@link java.util.Optional}.
     */
    public static <T> Optional<T> of(final java.util.Optional<? extends T> optional) {
        return ofNullable(optional.orElse(null));
    }

    /**
     * Returns the first non-null value from a stream of values, wrapped in an {@code Optional}. If no non-null value is
     * found, an empty {@code Optional} is returned.
     *
     * @param values The values to search through.
     * @param <T>    The type of the values.
     * @return An {@code Optional} containing the first non-null value, or an empty {@code Optional} if all values are
     *         {@code null}.
     */
    public static <T> Optional<T> findFirst(T... values) {
        if (values == null || values.length == 0) {
            return empty();
        }
        for (T value : values) {
            if (value != null) {
                return of(value);
            }
        }
        return empty();
    }

    /**
     * Returns the value held by this {@code Optional}, or {@code null} if no value is present. Note that this differs
     * from {@link java.util.Optional#get()} as it does not throw {@code NoSuchElementException} if the value is absent.
     * If an absolutely non-null value is required, use {@link #orElseThrow()}.
     *
     * @return The value held by this {@code Optional}, or {@code null} if absent.
     */
    public T getOrNull() {
        return this.value;
    }

    /**
     * Returns the value held by this {@code Optional}. If no value is present, a {@link NoSuchElementException} is
     * thrown.
     *
     * @return The value held by this {@code Optional}.
     * @throws NoSuchElementException if no value is present.
     */
    public T getOrThrow() throws NoSuchElementException {
        if (this.value == null) {
            throw new NoSuchElementException("No value present");
        }
        return this.value;
    }

    /**
     * Returns {@code true} if a value is not present, otherwise {@code false}.
     *
     * @return {@code true} if a value is not present, otherwise {@code false}.
     */
    public boolean isEmpty() {
        return value == null;
    }

    /**
     * Returns the {@link Throwable} that occurred during an operation if {@link #ofTry(SupplierX)} was used and an
     * exception was caught.
     *
     * @return The caught {@link Throwable}, or {@code null} if no exception occurred.
     */
    public Throwable getThrowable() {
        return this.throwable;
    }

    /**
     * Indicates whether an operation failed (i.e., an exception was caught during {@link #ofTry(SupplierX)}).
     *
     * @return {@code true} if an operation failed, {@code false} otherwise.
     */
    public boolean isFail() {
        return null != this.throwable;
    }

    /**
     * If an operation failed (i.e., an exception is present), performs the given action with the caught exception.
     *
     * <pre>{@code
     * Optional.ofTry(() -> 1 / 0).ifFail(Console::logger);
     * }</pre>
     *
     * @param action The action to be performed if an exception is present.
     * @return This {@code Optional} instance.
     * @throws NullPointerException if the action is {@code null}.
     */
    public Optional<T> ifFail(final Consumer<? super Throwable> action) throws NullPointerException {
        Objects.requireNonNull(action, "action is null");

        if (isFail()) {
            action.accept(throwable);
        }

        return this;
    }

    /**
     * If an operation failed and the caught exception is of a specified type, performs the given action with the caught
     * exception.
     *
     * <pre>{@code
     * Optional.ofTry(() -> 1 / 0).ifFail(Console::logger, ArithmeticException.class);
     * }</pre>
     *
     * @param action The action to be performed if an exception is present and matches one of the specified types.
     * @param exs    A varargs array of exception classes to match against.
     * @return This {@code Optional} instance.
     * @throws NullPointerException if the action is {@code null}.
     */
    @SafeVarargs
    public final Optional<T> ifFail(final Consumer<? super Throwable> action, final Class<? extends Throwable>... exs)
            throws NullPointerException {
        Objects.requireNonNull(action, "action is null");

        if (isFail() && EasyStream.of(exs).anyMatch(e -> e.isAssignableFrom(throwable.getClass()))) {
            action.accept(throwable);
        }

        return this;
    }

    /**
     * Returns {@code true} if a value is present, otherwise {@code false}.
     *
     * @return {@code true} if a value is present, otherwise {@code false}.
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * If a value is present, performs the given action with the value.
     *
     * <pre>{@code
     * Optional.ofNullable("Hello!").ifPresent(Console::logger);
     * }</pre>
     *
     * @param action The action to be performed if a value is present.
     * @return This {@code Optional} instance.
     */
    public Optional<T> ifPresent(final ConsumerX<? super T> action) {
        if (isPresent()) {
            action.accept(value);
        }
        return this;
    }

    /**
     * If a value is present, performs the given actions with the value. This is a dynamic extension of
     * {@link #ifPresent(ConsumerX)}.
     *
     * @param actions A varargs array of actions to be performed if a value is present. An empty array will not throw
     *                {@code NullPointerException}.
     * @return This {@code Optional} instance.
     * @throws NullPointerException if a value is present and any action in the array is {@code null}.
     */
    @SafeVarargs
    public final Optional<T> ifPresents(final ConsumerX<T>... actions) throws NullPointerException {
        return ifPresent(Stream.of(actions).reduce(ConsumerX::andThen).orElseGet(() -> o -> {
        }));
    }

    /**
     * If a value is present, and the value matches the given predicate, returns an {@code Optional} describing the
     * value. Otherwise, returns an empty {@code Optional}. If the predicate throws an exception, an empty
     * {@code Optional} with the exception stored is returned.
     *
     * @param predicate The predicate to apply to the value, if present.
     * @return An {@code Optional} describing the value if present and matching the predicate, otherwise an empty
     *         {@code Optional}.
     * @throws NullPointerException if the predicate is {@code null}.
     */
    public Optional<T> filter(final PredicateX<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isEmpty() || isFail()) {
            return this;
        }

        try {
            return predicate.testing(value) ? this : empty();
        } catch (final Throwable e) {
            final Optional<T> emptyWithError = new Optional<>(null);
            emptyWithError.throwable = e;
            return emptyWithError;
        }
    }

    /**
     * If a value is present, applies the given {@code FunctionX} to the value, and returns an {@code Optional}
     * describing the result. Otherwise, returns an empty {@code Optional}. If the function throws an exception, an
     * empty {@code Optional} with the exception stored is returned.
     *
     * @param mapper The mapping function to apply to a value, if present.
     * @param <U>    The type of the value returned from the mapping function.
     * @return An {@code Optional} describing the result of applying the mapping function to the value of this
     *         {@code Optional}, if a value is present, otherwise an empty {@code Optional}.
     * @throws NullPointerException if the mapping function is {@code null}.
     */
    public <U> Optional<U> map(final FunctionX<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (!isPresent()) {
            return empty();
        }

        return ofNullable(mapper.apply(value));
    }

    /**
     * If a value is present, applies the given {@code FunctionX} to the value, and returns the result of the function.
     * Otherwise, returns an empty {@code Optional}. This method is similar to {@link #map}, but the mapping function
     * must return an {@link java.util.Optional}.
     *
     * @param mapper The mapping function to apply to a value, if present.
     * @param <U>    The type of the value returned from the mapping function.
     * @return The result of applying the mapping function to the value of this {@code Optional}, if a value is present,
     *         otherwise an empty {@code Optional}.
     * @throws NullPointerException if the mapping function is {@code null} or returns a {@code null} {@code Optional}.
     * @see java.util.Optional#flatMap(Function)
     */
    public <U> Optional<U> flattedMap(final FunctionX<? super T, ? extends java.util.Optional<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        if (isFail()) {
            return (Optional<U>) this;
        } else if (isEmpty()) {
            return empty();
        } else {
            final java.util.Optional<? extends U> optional;
            try {
                optional = mapper.applying(value);
            } catch (final Throwable e) {
                final Optional<U> emptyWithError = new Optional<>(null);
                emptyWithError.throwable = throwable;
                return emptyWithError;
            }
            return of(optional);
        }
    }

    /**
     * If a value is present, returns a sequential {@link Stream} containing only that value. Otherwise, returns an
     * empty {@link Stream}. This method allows passing the value from an {@code Optional} to a {@link Stream}.
     *
     * <pre>{@code
     *     Stream<Optional<T>> os = ..
     *     Stream<T> s = os.flatMap(Optional::stream)
     * }</pre>
     *
     * @return A {@link Stream} containing the value if present, otherwise an empty {@link Stream}.
     */
    public Stream<T> stream() {
        if (isEmpty()) {
            return Stream.empty();
        } else {
            return Stream.of(value);
        }
    }

    /**
     * If a value is present, returns this {@code Optional}. Otherwise, returns an {@code Optional} produced by the
     * supplying function.
     *
     * @param supplier The supplying function that produces an {@code Optional} to be returned.
     * @return This {@code Optional} if a value is present, otherwise an {@code Optional} produced by the supplying
     *         function.
     * @throws NullPointerException if the supplying function is {@code null} or produces a {@code null}
     *                              {@code Optional}.
     */
    public Optional<T> or(final SupplierX<? extends Optional<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        if (isPresent()) {
            return this;
        } else {
            final Optional<T> r = (Optional<T>) supplier.get();
            return Objects.requireNonNull(r);
        }
    }

    /**
     * If a value is present, returns the value, otherwise returns {@code other}.
     *
     * @param other The value to be returned if no value is present. May be {@code null}.
     * @return The value, if present, otherwise {@code other}.
     */
    public T orElse(final T other) {
        return isPresent() ? value : other;
    }

    /**
     * If an exception occurred during an operation, returns {@code other}. Otherwise, returns the value held by this
     * {@code Optional}.
     *
     * @param other The value to be returned if an exception occurred.
     * @return The value if no exception occurred, otherwise {@code other}.
     */
    public T exceptionOrElse(final T other) {
        return isFail() ? other : value;
    }

    /**
     * If a value is present, returns the value, otherwise returns the result produced by the supplying function.
     *
     * @param supplier The supplying function that produces a value to be returned.
     * @return The value, if present, otherwise the result produced by the supplying function.
     * @throws NullPointerException if no value is present and the supplying function is {@code null}.
     */
    public T orElseGet(final SupplierX<? extends T> supplier) {
        return isPresent() ? value : supplier.get();
    }

    /**
     * If a value is present, returns this {@code Optional}. Otherwise, returns an {@code Optional} containing the
     * result produced by the supplying function.
     *
     * @param supplier The supplying function that produces a value to be wrapped in an {@code Optional}.
     * @return This {@code Optional} if a value is present, otherwise an {@code Optional} containing the result produced
     *         by the supplying function.
     */
    public Optional<T> orElseOpt(final SupplierX<? extends T> supplier) {
        return or(() -> ofNullable(supplier.get()));
    }

    /**
     * If a value is present, returns the value. Otherwise, performs the given action and returns {@code null}.
     *
     * @param action The action to be performed if no value is present.
     * @return The value, if present, otherwise {@code null}.
     * @throws NullPointerException if no value is present and the action is {@code null}.
     */
    public T orElseRun(final Runnable action) {
        if (isPresent()) {
            return value;
        } else {
            action.run();
            return null;
        }
    }

    /**
     * If a value is present, returns the value, otherwise throws {@link NoSuchElementException}.
     *
     * @return The non-{@code null} value held by this {@code Optional}.
     * @throws NoSuchElementException if no value is present.
     */
    public T orElseThrow() {
        return orElseThrow(() -> new NoSuchElementException("No value present"));
    }

    /**
     * If a value is present, returns the value, otherwise throws an exception produced by the exception supplying
     * function. This is typically used with an exception constructor, e.g., {@code IllegalStateException::new}.
     *
     * @param <X>               The type of the exception to be thrown.
     * @param exceptionSupplier The supplying function that produces an exception to be thrown.
     * @return The non-{@code null} value held by this {@code Optional}.
     * @throws X                    if no value is present.
     * @throws NullPointerException if no value is present and the exception supplying function is {@code null} or
     *                              produces a {@code null} exception.
     */
    public <X extends Throwable> T orElseThrow(final SupplierX<? extends X> exceptionSupplier) throws X {
        if (isPresent()) {
            return value;
        }
        throw exceptionSupplier.get();
    }

    /**
     * Converts this {@code Optional} to a {@link java.util.Optional} object.
     *
     * @return A {@link java.util.Optional} object representing this {@code Optional}.
     */
    public java.util.Optional<T> toOptional() {
        return java.util.Optional.ofNullable(value);
    }

    /**
     * Converts this {@code Optional} to an {@link EasyStream} object.
     *
     * @return An {@link EasyStream} object representing this {@code Optional}.
     */
    public EasyStream<T> toEasyStream() {
        return EasyStream.of(value);
    }

    /**
     * Indicates whether some other object is "equal to" this {@code Optional}. An {@code Optional} is equal to another
     * {@code Optional} if:
     * <ul>
     * <li>It is also an {@code Optional} and</li>
     * <li>Both are empty, or</li>
     * <li>The values held by both are {@code equals()} to each other.</li>
     * </ul>
     *
     * @param object The reference object with which to compare.
     * @return {@code true} if this object is the same as the object argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Optional<?> other)) {
            return false;
        }

        return Objects.equals(value, other.value);
    }

    /**
     * Returns the hash code value for this {@code Optional}. If a value is present, the hash code is derived from the
     * value. Otherwise, the hash code is 0.
     *
     * @return The hash code value for this {@code Optional}.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Returns a string representation of the value held by this {@code Optional}. If no value is present, returns
     * {@code null}.
     *
     * @return The result of calling {@code toString()} on the held value, or {@code null} if absent.
     */
    @Override
    public String toString() {
        return StringKit.toStringOrNull(value);
    }

}
