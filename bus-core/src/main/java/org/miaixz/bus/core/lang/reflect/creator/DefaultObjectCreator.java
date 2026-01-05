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
package org.miaixz.bus.core.lang.reflect.creator;

import java.lang.invoke.MethodHandle;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.reflect.method.MethodInvoker;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.LookupKit;

/**
 * Default object instantiator. This class creates objects by calling the corresponding constructor based on the object
 * type and constructor parameters.
 *
 * @param <T> The type of the object to be created.
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultObjectCreator<T> implements ObjectCreator<T> {

    /**
     * The method handle for the constructor to be invoked.
     */
    final MethodHandle constructor;
    /**
     * The arguments to be passed to the constructor.
     */
    final Object[] args;

    /**
     * Constructs a new {@code DefaultObjectCreator}.
     *
     * @param clazz The class to be instantiated. Must not be {@code null}.
     * @param args  The constructor arguments. Can be empty if no-arg constructor is used.
     * @throws IllegalArgumentException if {@code clazz} is {@code null}.
     * @throws NullPointerException     if a suitable constructor is not found for the given class and arguments.
     */
    public DefaultObjectCreator(final Class<T> clazz, final Object... args) {
        final Class<?>[] paramTypes = ClassKit.getClasses(args);
        this.constructor = LookupKit.findConstructor(clazz, paramTypes);
        Assert.notNull(this.constructor, "Constructor not found!");
        this.args = args;
    }

    /**
     * Creates a default object instantiator for a class specified by its full class name.
     *
     * @param fullClassName The fully qualified name of the class to instantiate.
     * @param <T>           The type of the object to be created.
     * @return A new {@code DefaultObjectCreator} instance.
     */
    public static <T> DefaultObjectCreator<T> of(final String fullClassName) {
        return of(ClassKit.loadClass(fullClassName));
    }

    /**
     * Creates a default object instantiator for the given class and constructor arguments.
     *
     * @param clazz The class to be instantiated.
     * @param args  The constructor arguments. Can be empty if no-arg constructor is used.
     * @param <T>   The type of the object to be created.
     * @return A new {@code DefaultObjectCreator} instance.
     */
    public static <T> DefaultObjectCreator<T> of(final Class<T> clazz, final Object... args) {
        return new DefaultObjectCreator<>(clazz, args);
    }

    /**
     * Creates a new instance of the object using the configured constructor and arguments.
     *
     * @return A new instance of type {@code T}.
     */
    @Override
    public T of() {
        return MethodInvoker.invokeHandle(constructor, args);
    }

}
