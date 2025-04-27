/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.miaixz.bus.core.center.function.ConsumerX;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.function.PredicateX;
import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.center.stream.EasyStream;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 复制jdk16中的Optional，以及进行了一点调整和新增，比jdk8中的Optional多了几个实用的函数
 *
 * @param <T> 包裹里元素的类型
 * @author Kimi Liu
 * @see java.util.Optional
 * @since Java 17+
 */
public class Optional<T> {

    /**
     * 一个空的{@code Optional}
     */
    private static final Optional<?> EMPTY = new Optional<>(null);
    /**
     * 包裹里实际的元素
     */
    private final T value;
    private Throwable throwable;

    /**
     * {@code Optional}的构造函数
     *
     * @param value 包裹里的元素
     */
    private Optional(final T value) {
        this.value = value;
    }

    /**
     * 返回一个空的{@code Optional}
     *
     * @param <T> 包裹里元素的类型
     * @return Optional
     */
    public static <T> Optional<T> empty() {
        return (Optional<T>) EMPTY;
    }

    /**
     * 返回一个包裹里元素不可能为空的{@code Optional}
     *
     * @param value 包裹里的元素
     * @param <T>   包裹里元素的类型
     * @return 一个包裹里元素不可能为空的 {@code Optional}
     * @throws NullPointerException 如果传入的元素为空，抛出 {@code NPE}
     */
    public static <T> Optional<T> of(final T value) {
        return new Optional<>(Objects.requireNonNull(value));
    }

    /**
     * 返回一个包裹里元素可能为空的{@code Optional}
     *
     * @param value 传入需要包裹的元素
     * @param <T>   包裹里元素的类型
     * @return 一个包裹里元素可能为空的 {@code Optional}
     */
    public static <T> Optional<T> ofNullable(final T value) {
        return value == null ? empty() : new Optional<>(value);
    }

    /**
     * 返回一个包裹里元素可能为空的{@code Optional}，额外判断了空字符串的情况
     *
     * @param <T>   字符串类型
     * @param value 传入需要包裹的元素
     * @return 一个包裹里元素可能为空，或者为空字符串的 {@code Optional}
     */
    public static <T extends CharSequence> Optional<T> ofBlankAble(final T value) {
        return StringKit.isBlank(value) ? empty() : new Optional<>(value);
    }

    /**
     * 返回一个包裹里{@code List}集合可能为空的{@code Optional}，额外判断了集合内元素为空的情况
     *
     * @param <T>   包裹里元素的类型
     * @param <R>   集合值类型
     * @param value 传入需要包裹的元素，支持CharSequence、Map、Iterable、Iterator、Array类型
     * @return 一个包裹里元素可能为空的 {@code Optional}
     */
    public static <T, R extends Collection<T>> Optional<R> ofEmptyAble(final R value) {
        return ObjectKit.isEmpty(value) ? empty() : new Optional<>(value);
    }

    /**
     * @param supplier 操作
     * @param <T>      类型
     * @return 操作执行后的值
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
     * 根据 {@link java.util.Optional} 构造 {@code Optional}
     *
     * @param optional optional
     * @param <T>      包裹的元素类型
     * @return 一个包裹里元素可能为空的 {@code Optional}
     */
    public static <T> Optional<T> of(final java.util.Optional<? extends T> optional) {
        return ofNullable(optional.orElse(null));
    }

    /**
     * 返回包裹里的元素，取不到则为{@code null}，注意！！！此处和{@link java.util.Optional#get()}不同的一点是本方法并不会抛出{@code NoSuchElementException}
     * 如果元素为空，则返回{@code null}，如果需要一个绝对不能为 {@code null}的值，则使用{@link #orElseThrow()}
     * 做此处修改的原因是，有时候我们确实需要返回一个null给前端，并且这样的时候并不少见 而使用 {@code .orElse(null)}需要写整整12个字符，用{@code .get()}就只需要6个啦
     *
     * @return 包裹里的元素，有可能为{@code null}
     */
    public T getOrNull() {
        return this.value;
    }

    /**
     * 返回包裹里的元素，取不到则抛出异常
     *
     * @return 包裹里的元素
     * @throws NoSuchElementException 如果元素为空，则抛出此异常
     */
    public T getOrThrow() throws NoSuchElementException {
        if (this.value == null) {
            throw new NoSuchElementException("No value present");
        }
        return this.value;
    }

    /**
     * 判断包裹里元素的值是否不存在，不存在为 {@code true}，否则为{@code false}
     *
     * @return 包裹里元素的值不存在 则为 {@code true}，否则为{@code false}
     */
    public boolean isEmpty() {
        return value == null;
    }

    /**
     * 获取异常 当调用 {@link #ofTry(SupplierX)}时，异常信息不会抛出，而是保存，调用此方法获取抛出的异常
     *
     * @return 异常
     */
    public Throwable getThrowable() {
        return this.throwable;
    }

    /**
     * 是否失败 当调用 {@link #ofTry(SupplierX)}时，抛出异常则表示失败
     *
     * @return 是否失败
     */
    public boolean isFail() {
        return null != this.throwable;
    }

    /**
     * 如果包裹内容失败了，则执行传入的操作({@link Consumer#accept}) 例如执行有异常就打印结果
     *
     * <pre>{@code
     * Optional.ofTry(() -> 1 / 0).ifFail(Console::logger);
     * }</pre>
     *
     * @param action 你想要执行的操作
     * @return this
     * @throws NullPointerException 如果包裹里的值存在，但你传入的操作为{@code null}时抛出
     */
    public Optional<T> ifFail(final Consumer<? super Throwable> action) throws NullPointerException {
        Objects.requireNonNull(action, "action is null");

        if (isFail()) {
            action.accept(throwable);
        }

        return this;
    }

    /**
     * 如果包裹内容失败了，同时是指定的异常执行传入的操作({@link Consumer#accept}) 例如如果值存在就打印结果
     *
     * <pre>{@code
     * Optional.ofTry(() -> 1 / 0).ifFail(Console::logger, ArithmeticException.class);
     * }</pre>
     *
     * @param action 你想要执行的操作
     * @param exs    限定的异常
     * @return this
     * @throws NullPointerException 如果包裹里的值存在，但你传入的操作为{@code null}时抛出
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
     * 判断包裹里元素的值是否存在，存在为 {@code true}，否则为{@code false}
     *
     * @return 包裹里元素的值存在为 {@code true}，否则为{@code false}
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * 如果包裹里的值存在，就执行传入的操作({@link Consumer#accept}) 例如如果值存在就打印结果
     *
     * <pre>{@code
     * Optional.ofNullable("Hello!").ifPresent(Console::logger);
     * }</pre>
     *
     * @param action 你想要执行的操作
     * @return this
     * @throws NullPointerException 如果包裹里的值存在，但你传入的操作为{@code null}时抛出
     */
    public Optional<T> ifPresent(final ConsumerX<? super T> action) {
        if (isPresent()) {
            try {
                action.accepting(value);
            } catch (final Throwable e) {
                this.throwable = e;
            }
        }
        return this;
    }

    /**
     * 判断包裹里的值存在并且与给定的条件是否满足 ({@link Predicate#test}执行结果是否为true) 如果满足条件则返回本身 不满足条件或者元素本身为空时返回一个返回一个空的{@code Optional}
     * predicate测试报错，则返回一个空的{@code Optional}，并附带错误信息
     *
     * @param predicate 给定的条件
     * @return 如果满足条件则返回本身, 不满足条件或者元素本身为空时返回一个空的{@code Optional}
     * @throws NullPointerException 如果给定的条件为 {@code null}抛出{@code NPE}
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
     * 如果包裹里的值存在，就执行传入的操作({@link FunctionX#applying(Object)})并返回一个包裹了该操作返回值的{@code Opt} 如果不存在，返回一个空的{@code Optional}
     *
     * @param mapper 值存在时执行的操作
     * @param <U>    操作返回值的类型
     * @return 如果包裹里的值存在，就执行传入的操作({@link FunctionX#applying(Object)})并返回一个包裹了该操作返回值的{@code Opt}，
     *         如果不存在，返回一个空的{@code Optional}
     * @throws NullPointerException 如果给定的操作为 {@code null}，抛出 {@code NPE}
     */
    public <U> Optional<U> map(final FunctionX<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (isFail()) {
            return (Optional<U>) this;
        } else if (isEmpty()) {
            return empty();
        } else {
            return Optional.ofTry(() -> mapper.applying(value));
        }
    }

    /**
     * 如果包裹里的值存在，就执行传入的操作({@link Function#apply})并返回该操作返回值 如果不存在，返回一个空的{@code Optional} 和 {@link Optional#map}的区别为
     * 传入的操作返回值必须为 {@link java.util.Optional}
     *
     * @param mapper 值存在时执行的操作
     * @param <U>    操作返回值的类型
     * @return 如果包裹里的值存在，就执行传入的操作({@link Function#apply})并返回该操作返回值 如果不存在，返回一个空的{@code Optional}
     * @throws NullPointerException 如果给定的操作为 {@code null}或者给定的操作执行结果为 {@code null}，抛出 {@code NPE}
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
     * 如果包裹里元素的值存在，就执行对应的操作集，并返回本身 如果不存在，返回一个空的{@code Optional} 属于 {@link #ifPresent(ConsumerX)}的动态拓展
     *
     * @param actions 值存在时执行的操作，动态参数，可传入数组，当数组为一个空数组时并不会抛出 {@code NPE}
     * @return this
     * @throws NullPointerException 如果值存在，并且传入的操作集中的元素为 {@code null}
     */
    @SafeVarargs
    public final Optional<T> ifPresents(final ConsumerX<T>... actions) throws NullPointerException {
        return ifPresent(Stream.of(actions).reduce(ConsumerX::andThen).orElseGet(() -> o -> {
        }));
    }

    /**
     * 如果包裹里元素的值存在，就返回本身，如果不存在，则使用传入的操作执行后获得的 {@code Optional}
     *
     * @param supplier 不存在时的操作
     * @return 如果包裹里元素的值存在，就返回本身，如果不存在，则使用传入的函数执行后获得的 {@code Optional}
     * @throws NullPointerException 如果传入的操作为空，或者传入的操作执行后返回值为空，则抛出
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
     * 如果包裹里元素的值存在，就返回一个包含该元素的 {@link Stream}, 否则返回一个空元素的 {@link Stream} 该方法能将 Optional 中的元素传递给 {@link Stream}
     *
     * <pre>{@code
     *     Stream<Optional<T>> os = ..
     *     Stream<T> s = os.flatMap(Optional::stream)
     * }</pre>
     *
     * @return 返回一个包含该元素的 {@link Stream}或空的 {@link Stream}
     */
    public Stream<T> stream() {
        if (isEmpty()) {
            return Stream.empty();
        } else {
            return Stream.of(value);
        }
    }

    /**
     * 如果包裹里元素的值存在，则返回该值，否则返回传入的{@code other}
     *
     * @param other 元素为空时返回的值，有可能为 {@code null}.
     * @return 如果包裹里元素的值存在，则返回该值，否则返回传入的{@code other}
     */
    public T orElse(final T other) {
        return isPresent() ? value : other;
    }

    /**
     * 异常则返回另一个可选值
     *
     * @param other 可选值
     * @return 如果未发生异常，则返回该值，否则返回传入的{@code other}
     */
    public T exceptionOrElse(final T other) {
        return isFail() ? other : value;
    }

    /**
     * 如果包裹里元素的值存在，则返回该值，否则返回传入的操作执行后的返回值
     *
     * @param supplier 值不存在时需要执行的操作，返回一个类型与 包裹里元素类型 相同的元素
     * @return 如果包裹里元素的值存在，则返回该值，否则返回传入的操作执行后的返回值
     * @throws NullPointerException 如果之不存在，并且传入的操作为空，则抛出 {@code NPE}
     */
    public T orElseGet(final SupplierX<? extends T> supplier) {
        return isPresent() ? value : supplier.get();
    }

    /**
     * 如果包裹里元素的值存在，则返回该值，否则返回传入的操作执行后的返回值
     *
     * @param supplier 值不存在时需要执行的操作，返回一个类型与 包裹里元素类型 相同的元素
     * @return 如果包裹里元素的值存在，则返回该值，否则返回传入的操作执行后的返回值
     * @throws NullPointerException 如果之不存在，并且传入的操作为空，则抛出 {@code NPE}
     */
    public Optional<T> orElseOpt(final SupplierX<? extends T> supplier) {
        return or(() -> ofNullable(supplier.get()));
    }

    /**
     * 如果包裹里元素的值存在，则返回该值，否则执行传入的操作
     *
     * @param action 值不存在时执行的操作
     * @return 如果包裹里元素的值存在，则返回该值，否则执行传入的操作
     * @throws NullPointerException 如果值不存在，并且传入的操作为 {@code null}
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
     * 如果包裹里的值存在，则返回该值，否则抛出 {@code NoSuchElementException}
     *
     * @return 返回一个不为 {@code null} 的包裹里的值
     * @throws NoSuchElementException 如果包裹里的值不存在则抛出该异常
     */
    public T orElseThrow() {
        return orElseThrow(() -> new NoSuchElementException("No value present"));
    }

    /**
     * 如果包裹里的值存在，则返回该值，否则执行传入的操作，获取异常类型的返回值并抛出 往往是一个包含无参构造器的异常 例如传入{@code IllegalStateException::new}
     *
     * @param <X>               异常类型
     * @param exceptionSupplier 值不存在时执行的操作，返回值继承 {@link Throwable}
     * @return 包裹里不能为空的值
     * @throws X                    如果值不存在
     * @throws NullPointerException 如果值不存在并且 传入的操作为 {@code null}或者操作执行后的返回值为{@code null}
     */
    public <X extends Throwable> T orElseThrow(final SupplierX<? extends X> exceptionSupplier) throws X {
        if (isPresent()) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * 转换为 {@link java.util.Optional}对象
     *
     * @return {@link java.util.Optional}对象
     */
    public java.util.Optional<T> toOptional() {
        return java.util.Optional.ofNullable(value);
    }

    /**
     * 转换为 {@link EasyStream}对象
     *
     * @return {@link EasyStream}对象
     */
    public EasyStream<T> toEasyStream() {
        return EasyStream.of(value);
    }

    /**
     * 判断传入参数是否与 {@code Optional}相等 在以下情况下返回true
     * <ul>
     * <li>它也是一个 {@code Optional} 并且
     * <li>它们包裹住的元素都为空 或者
     * <li>它们包裹住的元素之间相互 {@code equals()}
     * </ul>
     *
     * @param object 一个要用来判断是否相等的参数
     * @return 如果传入的参数也是一个 {@code Optional}并且它们包裹住的元素都为空 或者它们包裹住的元素之间相互 {@code equals()} 就返回{@code true} 否则返回
     *         {@code false}
     */
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Optional)) {
            return false;
        }

        final Optional<?> other = (Optional<?>) object;
        return Objects.equals(value, other.value);
    }

    /**
     * 如果包裹内元素为空，则返回0，否则返回元素的 {@code hashcode}
     *
     * @return 如果包裹内元素为空，则返回0，否则返回元素的 {@code hashcode}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * 返回包裹内元素调用{@code toString()}的结果，不存在则返回{@code null}
     *
     * @return 包裹内元素调用{@code toString()}的结果，不存在则返回{@code null}
     */
    @Override
    public String toString() {
        return StringKit.toStringOrNull(value);
    }

}
