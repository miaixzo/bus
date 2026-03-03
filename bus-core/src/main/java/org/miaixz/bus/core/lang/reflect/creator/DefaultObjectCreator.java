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
