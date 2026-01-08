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
package org.miaixz.bus.core.builder;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.center.function.Consumer3X;

/**
 * A generic builder that uses lambda expressions (method references) to construct objects, inspired by the builder
 * pattern.
 *
 * <p>
 * Example of creating a new object:
 * 
 * <pre>{@code
 * 
 * Box box = GenericBuilder.of(Box::new).with(Box::setId, 1024L).with(Box::setTitle, "Hello World!").build();
 * }</pre>
 *
 * <p>
 * Example of modifying an existing object:
 * 
 * <pre>{@code
 * 
 * Box existingBox = new Box();
 * Box modifiedBox = GenericBuilder.of(() -> existingBox).with(Box::setTitle, "Hello Friend!").build();
 * }</pre>
 *
 * <p>
 * This builder supports method references for setters or other void methods with up to two arguments.
 *
 * @param <T> The type of the object being built.
 * @author Kimi Liu
 * @since Java 17+
 */
public class GenericBuilder<T> implements Builder<T> {

    @Serial
    private static final long serialVersionUID = 2852229897009L;

    /**
     * The supplier that provides the initial instance of the object.
     */
    private final Supplier<T> instant;

    /**
     * A list of consumers that will be applied to the object to set its properties.
     */
    private final List<Consumer<T>> modifiers = new ArrayList<>();

    /**
     * Constructs a new {@code GenericBuilder}.
     *
     * @param instant The supplier that provides the object instance.
     */
    public GenericBuilder(final Supplier<T> instant) {
        this.instant = instant;
    }

    /**
     * Creates a new {@code GenericBuilder} with a supplier for the target object.
     *
     * @param instant The supplier (e.g., a constructor reference like {@code Box::new}).
     * @param <T>     The type of the target object.
     * @return A new {@code GenericBuilder} instance.
     */
    public static <T> GenericBuilder<T> of(final Supplier<T> instant) {
        return new GenericBuilder<>(instant);
    }

    /**
     * Adds a modification step that calls a method with no arguments.
     *
     * @param consumer A consumer representing a method with no arguments (e.g., {@code Box::initialize}).
     * @return This {@code GenericBuilder} instance for chaining.
     */
    public GenericBuilder<T> with(final Consumer<T> consumer) {
        modifiers.add(consumer);
        return this;
    }

    /**
     * Adds a modification step that calls a method with one argument.
     *
     * @param consumer A bi-consumer representing a method with one argument (e.g., a setter like {@code Box::setId}).
     * @param p1       The argument to pass to the method.
     * @param <P1>     The type of the first argument.
     * @return This {@code GenericBuilder} instance for chaining.
     */
    public <P1> GenericBuilder<T> with(final BiConsumer<T, P1> consumer, final P1 p1) {
        modifiers.add(instance -> consumer.accept(instance, p1));
        return this;
    }

    /**
     * Adds a modification step that calls a method with two arguments.
     *
     * @param consumer A tri-consumer representing a method with two arguments (e.g., {@code Map::put}).
     * @param p1       The first argument to pass to the method.
     * @param p2       The second argument to pass to the method.
     * @param <P1>     The type of the first argument.
     * @param <P2>     The type of the second argument.
     * @return This {@code GenericBuilder} instance for chaining.
     */
    public <P1, P2> GenericBuilder<T> with(final Consumer3X<T, P1, P2> consumer, final P1 p1, final P2 p2) {
        modifiers.add(instance -> consumer.accept(instance, p1, p2));
        return this;
    }

    /**
     * Builds the final object by getting an instance from the supplier and applying all the registered modification
     * steps.
     *
     * @return The constructed or modified target object.
     */
    @Override
    public T build() {
        final T value = instant.get();
        modifiers.forEach(modifier -> modifier.accept(value));
        modifiers.clear();
        return value;
    }

}
