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
package org.miaixz.bus.core.center.function;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Map;

import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.mutable.MutableEntry;
import org.miaixz.bus.core.xyz.*;

/**
 * Dynamically creates Lambdas in a reflection-like manner, offering performance advantages and avoiding the creation of
 * anonymous inner classes with each Lambda invocation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LambdaFactory {

    /**
     * Cache for storing generated Lambda objects to avoid repeated creation. The key is a {@link MutableEntry}
     * containing the function interface type and the executable (method or constructor). The value is the generated
     * Lambda object.
     */
    private static final Map<MutableEntry<Class<?>, Executable>, Object> CACHE = new WeakConcurrentMap<>();

    /**
     * Private constructor to prevent instantiation.
     *
     * @throws IllegalAccessException if this constructor is called.
     */
    private LambdaFactory() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    /**
     * Builds a Lambda function based on the provided function interface type, declaring class, method name, and
     * parameter types.
     *
     * <pre>{@code
     * 
     * class Something {
     * 
     *     private Long id;
     *     private String name;
     *     // ... Getter and Setter methods omitted
     * }
     *
     * Function<Something, Long> getIdFunction = LambdaFactory.build(Function.class, Something.class, "getId");
     * BiConsumer<Something, String> setNameConsumer = LambdaFactory
     *         .build(BiConsumer.class, Something.class, "setName", String.class);
     * }</pre>
     *
     * @param functionInterfaceType The functional interface type that accepts the Lambda.
     * @param declaringClass        The type of the class that declares the method.
     * @param methodName            The name of the method.
     * @param paramTypes            An array of parameter types for the method.
     * @param <F>                   The type of the functional interface.
     * @return An object of the functional interface type that represents the Lambda.
     */
    public static <F> F build(
            final Class<F> functionInterfaceType,
            final Class<?> declaringClass,
            final String methodName,
            final Class<?>... paramTypes) {
        return build(
                functionInterfaceType,
                MethodKit.getMethod(declaringClass, methodName, paramTypes),
                declaringClass);
    }

    /**
     * Builds a Lambda function based on the provided function interface type and executable (method or constructor).
     * Invoking the Lambda function is equivalent to executing the corresponding method or constructor.
     *
     * @param functionInterfaceType The functional interface type that accepts the Lambda.
     * @param executable            The executable object ({@link Constructor} or {@link Method}).
     * @param <F>                   The type of the functional interface.
     * @return An object of the functional interface type that represents the Lambda.
     */
    public static <F> F build(final Class<F> functionInterfaceType, final Executable executable) {
        return build(functionInterfaceType, executable, null);
    }

    /**
     * Builds a Lambda function based on the provided function interface type, executable (method or constructor), and
     * declaring class. Invoking the Lambda function is equivalent to executing the corresponding method or constructor.
     *
     * @param <F>                   The type of the functional interface.
     * @param functionInterfaceType The functional interface type that accepts the Lambda.
     * @param executable            The executable object ({@link Constructor} or {@link Method}).
     * @param declaringClass        The class where the {@link Executable} is declared. If the method or constructor is
     *                              defined in a superclass, this is used to specify the subclass.
     * @return An object of the functional interface type that represents the Lambda.
     */
    public static <F> F build(
            final Class<F> functionInterfaceType,
            final Executable executable,
            final Class<?> declaringClass) {
        Assert.notNull(functionInterfaceType);
        Assert.notNull(executable);

        final MutableEntry<Class<?>, Executable> cacheKey = new MutableEntry<>(functionInterfaceType, executable);
        return (F) CACHE.computeIfAbsent(
                cacheKey,
                key -> doBuildWithoutCache(functionInterfaceType, executable, declaringClass));
    }

    /**
     * Builds a Lambda function based on the provided method or constructor object, effectively proxying the method or
     * constructor through the Lambda function. Invoking the Lambda function is equivalent to executing the
     * corresponding method or constructor.
     *
     * @param <F>            The type of the functional interface.
     * @param funcType       The functional interface type that accepts the Lambda.
     * @param executable     The executable object (method or constructor).
     * @param declaringClass The class where the {@link Executable} is declared. If the method or constructor is defined
     *                       in a superclass, this is used to specify the subclass.
     * @return An object of the functional interface type that represents the Lambda.
     */
    private static <F> F doBuildWithoutCache(
            final Class<F> funcType,
            final Executable executable,
            final Class<?> declaringClass) {
        ReflectKit.setAccessible(executable);

        // Get the invoke method of the functional interface
        final Method invokeMethod = LambdaKit.getInvokeMethod(funcType);
        try {
            return (F) metaFactory(funcType, invokeMethod, executable, declaringClass).getTarget().invoke();
        } catch (final Throwable e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a {@link CallSite} for a Lambda function that proxies a method or constructor.
     *
     * @param funcType       The functional interface type.
     * @param funcMethod     The method of the functional interface to be implemented.
     * @param executable     The method or constructor to be proxied.
     * @param declaringClass The class where the {@link Executable} is declared. If the method or constructor is defined
     *                       in a superclass, this is used to specify the subclass.
     * @return A {@link CallSite} representing the Lambda function.
     * @throws LambdaConversionException If there is an error during Lambda conversion, such as access permissions.
     */
    private static CallSite metaFactory(
            final Class<?> funcType,
            final Method funcMethod,
            final Executable executable,
            final Class<?> declaringClass) throws LambdaConversionException {
        // Find the context and caller's access permissions
        final MethodHandles.Lookup caller = LookupKit.lookup(executable.getDeclaringClass());
        // The name of the method to be implemented
        final String invokeName = funcMethod.getName();
        // The method type (parameter types and return type) expected by the call site
        final MethodType invokedType = MethodType.methodType(funcType);

        final Class<?>[] paramTypes = funcMethod.getParameterTypes();
        // The method type of the functional interface method to be implemented
        final MethodType samMethodType = MethodType.methodType(funcMethod.getReturnType(), paramTypes);
        // A direct method handle describing the specific implementation method that will be executed when invoked
        final MethodHandle implMethodHandle = LookupKit.unreflect(executable);

        if (ClassKit.isSerializable(funcType)) {
            return LambdaMetafactory.altMetafactory(
                    caller,
                    invokeName,
                    invokedType,
                    samMethodType,
                    implMethodHandle,
                    MethodKit.methodType(executable, declaringClass),
                    LambdaMetafactory.FLAG_SERIALIZABLE);
        }

        return LambdaMetafactory.metafactory(
                caller,
                invokeName,
                invokedType,
                samMethodType,
                implMethodHandle,
                MethodKit.methodType(executable, declaringClass));
    }

}
