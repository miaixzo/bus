package org.aoju.bus.core.lang;

import org.aoju.bus.core.lang.function.Func0;
import org.aoju.bus.core.lang.function.VoidFunc0;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.StringKit;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * 复制jdk16中的Optional，进行了一些调整，比jdk8中的Optional多了几个实用的函数
 *
 * @param <T> 包裹里元素的类型
 * @author Kimi Liu
 * @version 6.3.5
 * @see java.util.Optional
 * @since JDK 1.8+
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

    private Exception exception;

    /**
     * {@code Optional}的构造函数
     *
     * @param value 包裹里的元素
     */
    private Optional(T value) {
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
    public static <T> Optional<T> of(T value) {
        return new Optional<>(Objects.requireNonNull(value));
    }

    /**
     * 返回一个包裹里元素可能为空的{@code Optional}
     *
     * @param value 传入需要包裹的元素
     * @param <T>   包裹里元素的类型
     * @return 一个包裹里元素可能为空的 {@code Optional}
     */
    public static <T> Optional<T> ofNullable(T value) {
        return value == null ? empty()
                : new Optional<>(value);
    }

    /**
     * 返回一个包裹里元素可能为空的{@code Optional}，额外判断了空字符串的情况
     *
     * @param value 传入需要包裹的元素
     * @param <T>   包裹里元素的类型
     * @return 一个包裹里元素可能为空，或者为空字符串的 {@code Optional}
     */
    public static <T> Optional<T> ofBlankAble(T value) {
        return StringKit.isBlank(value) ? empty() : new Optional<>(value);
    }

    /**
     * 返回一个包裹里{@code List}集合可能为空的{@code Optional}，额外判断了集合内元素为空的情况
     *
     * @param value 传入需要包裹的元素
     * @param <T>   包裹里元素的类型
     * @return 一个包裹里元素可能为空的 {@code Optional}
     */
    public static <T> Optional<List<T>> ofEmptyAble(List<T> value) {
        return CollKit.isEmpty(value) ? empty() : new Optional<>(value);
    }

    /**
     * 传入一段操作，包裹异常
     *
     * @param supplier 操作
     * @param <T>      类型
     * @return 操作执行后的值
     */
    public static <T> Optional<T> ofTry(Func0<T> supplier) {
        try {
            return Optional.ofNullable(supplier.call());
        } catch (Exception e) {
            final Optional<T> empty = new Optional<>(null);
            empty.exception = e;
            return empty;
        }
    }

    /**
     * 返回包裹里的元素，取不到则为{@code null}，注意！！！此处和{@link java.util.Optional#get()}不同的一点是本方法并不会抛出{@code NoSuchElementException}
     * 如果元素为空，则返回{@code null}，如果需要一个绝对不能为{@code null}的值，则使用{@link #orElseThrow()}
     *
     * <p>
     * 如果需要一个绝对不能为 {@code null}的值，则使用{@link #orElseThrow()}
     * 做此处修改的原因是，有时候我们确实需要返回一个null给前端，并且这样的时候并不少见
     * 而使用 {@code .orElse(null)}需要写整整12个字符，用{@code .get()}就只需要6个啦
     *
     * @return 包裹里的元素，有可能为{@code null}
     */
    public T get() {
        return this.value;
    }

    /**
     * 判断包裹里元素的值是否不存在，不存在为 {@code true}，否则为{@code false}
     *
     * @return 包裹里元素的值不存在 则为 {@code true}，否则为{@code false}
     * @since 11 这是jdk11{@link java.util.Optional}中的新函数
     */
    public boolean isEmpty() {
        return value == null;
    }

    /**
     * 获取异常<br>
     * 当调用 {@link #ofTry(Func0)}时，异常信息不会抛出，而是保存，调用此方法获取抛出的异常
     *
     * @return 异常
     */
    public Exception getException() {
        return this.exception;
    }

    /**
     * 是否失败<br>
     * 当调用 {@link #ofTry(Func0)}时，抛出异常则表示失败
     *
     * @return 是否失败
     */
    public boolean isFail() {
        return null != this.exception;
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
     * 如果包裹里的值存在，就执行传入的操作({@link Consumer#accept})
     *
     * <p> 例如如果值存在就打印结果
     * <pre>{@code
     * Optional.ofNullable("Hello!").ifPresent(Console::log);
     * }</pre>
     *
     * @param action 你想要执行的操作
     * @return this
     * @throws NullPointerException 如果包裹里的值存在，但你传入的操作为{@code null}时抛出
     */
    public Optional<T> ifPresent(Consumer<? super T> action) {
        if (isPresent()) {
            action.accept(value);
        }
        return this;
    }

    /**
     * 如果包裹里的值存在，就执行传入的值存在时的操作({@link Consumer#accept})
     * 否则执行传入的值不存在时的操作({@link VoidFunc0}中的{@link VoidFunc0#call()})
     *
     * <p>
     * 例如值存在就打印对应的值，不存在则用{@code Console.error}打印另一句字符串
     * <pre>{@code
     * Optional.ofNullable("Hello!").ifPresentOrElse(Console::log, () -> Console.error("Ops!Something is wrong!"));
     * }</pre>
     *
     * @param action      包裹里的值存在时的操作
     * @param emptyAction 包裹里的值不存在时的操作
     * @return this
     * @throws NullPointerException 如果包裹里的值存在时，执行的操作为 {@code null}, 或者包裹里的值不存在时的操作为 {@code null}，则抛出{@code NPE}
     */
    public Optional<T> ifPresentOrElse(Consumer<? super T> action, VoidFunc0 emptyAction) {
        if (isPresent()) {
            action.accept(value);
        } else {
            emptyAction.callWithRuntimeException();
        }
        return this;
    }

    /**
     * 如果包裹里的值存在，就执行传入的值存在时的操作({@link Function#apply(Object)})支持链式调用、转换为其他类型
     * 否则执行传入的值不存在时的操作({@link VoidFunc0}中的{@link VoidFunc0#call()})
     *
     * <p>
     * 如果值存在就转换为大写，否则用{@code Console.error}打印另一句字符串
     * <pre>{@code
     * String bus = Optional.ofBlankAble("bus").mapOrElse(String::toUpperCase, () -> Console.log("yes")).mapOrElse(String::intern, () -> Console.log("Value is not present~")).get();
     * }</pre>
     *
     * @param <U>         操作返回值的类型
     * @param mapper      包裹里的值存在时的操作
     * @param emptyAction 包裹里的值不存在时的操作
     * @return 如果满足条件则返回本身, 不满足条件或者元素本身为空时返回一个返回一个空的{@code Optional}
     * @throws NullPointerException 如果包裹里的值存在时，执行的操作为 {@code null}, 或者包裹里的值不存在时的操作为 {@code null}，则抛出{@code NPE}
     */
    public <U> Optional<U> mapOrElse(Function<? super T, ? extends U> mapper, VoidFunc0 emptyAction) {
        if (isPresent()) {
            return ofNullable(mapper.apply(value));
        } else {
            emptyAction.callWithRuntimeException();
            return empty();
        }
    }

    /**
     * 判断包裹里的值存在并且与给定的条件是否满足 ({@link Predicate#test}执行结果是否为true)
     * 如果满足条件则返回本身
     * 不满足条件或者元素本身为空时返回一个返回一个空的{@code Optional}
     *
     * @param predicate 给定的条件
     * @return 如果满足条件则返回本身, 不满足条件或者元素本身为空时返回一个返回一个空的{@code Optional}
     * @throws NullPointerException 如果给定的条件为 {@code null}，抛出{@code NPE}
     */
    public Optional<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isEmpty()) {
            return this;
        } else {
            return predicate.test(value) ? this : empty();
        }
    }

    /**
     * 如果包裹里的值存在，就执行传入的操作({@link Function#apply})并返回一个包裹了该操作返回值的{@code Optional}
     * 如果不存在，返回一个空的{@code Optional}
     *
     * @param mapper 值存在时执行的操作
     * @param <U>    操作返回值的类型
     * @return 如果包裹里的值存在，就执行传入的操作({@link Function#apply})并返回一个包裹了该操作返回值的{@code Optional}，
     * 如果不存在，返回一个空的{@code Optional}
     * @throws NullPointerException 如果给定的操作为 {@code null}，抛出 {@code NPE}
     */
    public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (isEmpty()) {
            return empty();
        } else {
            return Optional.ofNullable(mapper.apply(value));
        }
    }

    /**
     * 如果包裹里的值存在，就执行传入的操作({@link Function#apply})并返回该操作返回值
     * 如果不存在，返回一个空的{@code Optional}
     * 和 {@link Optional#map}的区别为 传入的操作返回值必须为 Optional
     *
     * @param <U>    操作返回值的类型
     * @param mapper 值存在时执行的操作
     * @return 如果包裹里的值存在，就执行传入的操作({@link Function#apply})并返回该操作返回值
     * 如果不存在，返回一个空的{@code Optional}
     * @throws NullPointerException 如果给定的操作为 {@code null}或者给定的操作执行结果为 {@code null}，抛出 {@code NPE}
     */
    public <U> Optional<U> flatMap(Function<? super T, ? extends Optional<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        if (isEmpty()) {
            return empty();
        } else {
            return Objects.requireNonNull((Optional<U>) mapper.apply(value));
        }
    }

    /**
     * 如果包裹里的值存在，就执行传入的操作({@link Function#apply})并返回该操作返回值
     * 如果不存在，返回一个空的{@code Optional}
     * 和 {@link Optional#map}的区别为 传入的操作返回值必须为 {@link java.util.Optional}
     *
     * @param mapper 值存在时执行的操作
     * @param <U>    操作返回值的类型
     * @return 如果包裹里的值存在，就执行传入的操作({@link Function#apply})并返回该操作返回值
     * 如果不存在，返回一个空的{@code Optional}
     * @throws NullPointerException 如果给定的操作为 {@code null}或者给定的操作执行结果为 {@code null}，抛出 {@code NPE}
     * @see java.util.Optional#flatMap(Function)
     */
    public <U> Optional<U> flattedMap(Function<? super T, ? extends java.util.Optional<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        if (isEmpty()) {
            return empty();
        } else {
            return ofNullable(mapper.apply(value).orElse(null));
        }
    }

    /**
     * 如果包裹里元素的值存在，就执行对应的操作，并返回本身
     * 如果不存在，返回一个空的{@code Optional} 属于 {@link #ifPresent}的链式拓展
     *
     * @param action 值存在时执行的操作
     * @return this
     * @throws NullPointerException 如果值存在，并且传入的操作为 {@code null}
     */
    public Optional<T> peek(Consumer<T> action) throws NullPointerException {
        Objects.requireNonNull(action);
        if (isEmpty()) {
            return Optional.empty();
        }
        action.accept(value);
        return this;
    }

    /**
     * 如果包裹里元素的值存在，就执行对应的操作集，并返回本身
     * 如果不存在，返回一个空的{@code Optional}
     *
     * <p>属于 {@link #ifPresent}的链式拓展
     * <p>属于 {@link #peek(Consumer)}的动态拓展
     *
     * @param actions 值存在时执行的操作，动态参数，可传入数组，当数组为一个空数组时并不会抛出 {@code NPE}
     * @return this
     * @throws NullPointerException 如果值存在，并且传入的操作集中的元素为 {@code null}
     */
    @SafeVarargs
    public final Optional<T> peeks(Consumer<T>... actions) throws NullPointerException {
        // 第三个参数 (opts, opt) -> null其实并不会执行到该函数式接口所以直接返回了个null
        return Stream.of(actions).reduce(this, Optional<T>::peek, (opts, opt) -> null);
    }

    /**
     * 如果包裹里元素的值存在，就返回本身，如果不存在，则使用传入的操作执行后获得的 {@code Optional}
     *
     * @param supplier 不存在时的操作
     * @return 如果包裹里元素的值存在，就返回本身，如果不存在，则使用传入的函数执行后获得的 {@code Optional}
     * @throws NullPointerException 如果传入的操作为空，或者传入的操作执行后返回值为空，则抛出 {@code NPE}
     */
    public Optional<T> or(Supplier<? extends Optional<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        if (isPresent()) {
            return this;
        } else {
            Optional<T> r = (Optional<T>) supplier.get();
            return Objects.requireNonNull(r);
        }
    }

    /**
     * 如果包裹里元素的值存在，就返回一个包含该元素的 {@link Stream},
     * 否则返回一个空元素的 {@link Stream}
     *
     * <p> 该方法能将 Optional 中的元素传递给 {@link Stream}
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
    public T orElse(T other) {
        return isPresent() ? value : other;
    }

    /**
     * 异常则返回另一个可选值
     *
     * @param other 可选值
     * @return 如果未发生异常，则返回该值，否则返回传入的{@code other}
     */
    public T exceptionOrElse(T other) {
        return isFail() ? other : value;
    }

    /**
     * 如果包裹里元素的值存在，则返回该值，否则返回传入的操作执行后的返回值
     *
     * @param supplier 值不存在时需要执行的操作，返回一个类型与 包裹里元素类型 相同的元素
     * @return 如果包裹里元素的值存在，则返回该值，否则返回传入的操作执行后的返回值
     * @throws NullPointerException 如果之不存在，并且传入的操作为空，则抛出 {@code NPE}
     */
    public T orElseGet(Supplier<? extends T> supplier) {
        return isPresent() ? value : supplier.get();
    }

    /**
     * 如果包裹里的值存在，则返回该值，否则抛出 {@code NoSuchElementException}
     *
     * @return 返回一个不为 {@code null} 的包裹里的值
     * @throws NoSuchElementException 如果包裹里的值不存在则抛出该异常
     */
    public T orElseThrow() {
        return orElseThrow(NoSuchElementException::new, "No value present");
    }

    /**
     * 如果包裹里的值存在，则返回该值，否则执行传入的操作，获取异常类型的返回值并抛出
     * <p>往往是一个包含无参构造器的异常 例如传入{@code IllegalStateException::new}
     *
     * @param <X>               异常类型
     * @param exceptionSupplier 值不存在时执行的操作，返回值继承 {@link Throwable}
     * @return 包裹里不能为空的值
     * @throws X                    如果值不存在
     * @throws NullPointerException 如果值不存在并且 传入的操作为 {@code null}或者操作执行后的返回值为{@code null}
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (isPresent()) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * 如果包裹里的值存在，则返回该值，否则执行传入的操作，获取异常类型的返回值并抛出
     *
     * <p>往往是一个包含 自定义消息 构造器的异常 例如
     * <pre>{@code
     * 		Optional.ofNullable(null).orElseThrow(IllegalStateException::new, "Ops!Something is wrong!");
     * }</pre>
     *
     * @param <X>               异常类型
     * @param exceptionFunction 值不存在时执行的操作，返回值继承 {@link Throwable}
     * @param message           作为传入操作执行时的参数，一般作为异常自定义提示语
     * @return 包裹里不能为空的值
     * @throws X                    如果值不存在
     * @throws NullPointerException 如果值不存在并且 传入的操作为 {@code null}或者操作执行后的返回值为{@code null}
     */
    public <X extends Throwable> T orElseThrow(Function<String, ? extends X> exceptionFunction, String message) throws X {
        if (isPresent()) {
            return value;
        } else {
            throw exceptionFunction.apply(message);
        }
    }

    /**
     * 转换为 {@link java.util.Optional}对象
     *
     * @return {@link java.util.Optional}对象
     */
    public java.util.Optional<T> toOptional() {
        return java.util.Optional.ofNullable(this.value);
    }

    /**
     * 判断传入参数是否与 {@code Optional}相等
     * 在以下情况下返回true
     * <ul>
     * <li>它也是一个 {@code Optional} 并且
     * <li>它们包裹住的元素都为空 或者
     * <li>它们包裹住的元素之间相互 {@code equals()}
     * </ul>
     *
     * @param obj 一个要用来判断是否相等的参数
     * @return 如果传入的参数也是一个 {@code Optional}并且它们包裹住的元素都为空
     * 或者它们包裹住的元素之间相互 {@code equals()} 就返回{@code true}
     * 否则返回 {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Optional)) {
            return false;
        }

        Optional<?> other = (Optional<?>) obj;
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
        return StringKit.toStringOrNull(this.value);
    }

}
