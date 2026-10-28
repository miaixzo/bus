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

import org.miaixz.bus.core.center.map.reference.SoftConcurrentMap;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.xyz.*;

/**
 * Creates functional-interface implementations for instance methods and constructors through {@link LambdaMetafactory}.
 * Generated implementations are reused through a soft-reference cache keyed by the interface, executable, and effective
 * receiver or constructor-result type. Garbage collection may discard cached implementations, so callers must not rely
 * on their identity remaining stable indefinitely. The receiver-based method adaptation used here does not support
 * static methods.
 *
 * @author Kimi Liu
 */
public class LambdaFactory {

    /**
     * Cache for storing generated Lambda objects to avoid repeated creation. The key contains the function interface,
     * executable, and effective receiver or constructor-result class, since all three participate in adapting the
     * generated Lambda. Both composite keys and generated implementations may be softly reclaimed.
     */
    private static final Map<Triplet<Class<?>, Executable, Class<?>>, Object> CACHE = new SoftConcurrentMap<>();

    /**
     * Creates a lambda factory backed by the shared executable cache.
     */
    public LambdaFactory() {
        // No initialization required.
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
     * @param declaringClass        The class used to find the method and adapt its receiver type.
     * @param methodName            The name of the method.
     * @param paramTypes            An array of parameter types for the method.
     * @param <F>                   The type of the functional interface.
     * @return An object of the functional interface type that represents the Lambda.
     * @throws IllegalArgumentException If no executable can be resolved, or the supplied type is null or is not a
     *                                  functional interface with one public abstract method.
     * @throws InternalException        If the executable cannot be adapted or the Lambda cannot be instantiated.
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
     * The executable's declaring class is used as the effective receiver or constructor-result type, including in the
     * cache key.
     *
     * @param functionInterfaceType The functional interface type that accepts the Lambda.
     * @param executable            The executable object ({@link Constructor} or {@link Method}).
     * @param <F>                   The type of the functional interface.
     * @return An object of the functional interface type that represents the Lambda.
     * @throws IllegalArgumentException If the interface or executable is null, or the interface does not have one
     *                                  public abstract method.
     * @throws InternalException        If the executable cannot be adapted or the Lambda cannot be instantiated.
     */
    public static <F> F build(final Class<F> functionInterfaceType, final Executable executable) {
        return build(functionInterfaceType, executable, null);
    }

    /**
     * Builds a Lambda function based on the provided function interface type, executable (method or constructor), and
     * effective receiver or constructor-result class. The same effective class is used for both cache lookup and Lambda
     * adaptation; explicitly supplying the executable's declaring class is equivalent to supplying null.
     *
     * @param <F>                   The type of the functional interface.
     * @param functionInterfaceType The functional interface type that accepts the Lambda.
     * @param executable            The executable object ({@link Constructor} or {@link Method}).
     * @param declaringClass        The receiver class for a method or result class for a constructor. A subclass may be
     *                              supplied for an inherited method. A null value uses the executable's declaring
     *                              class. The class must be compatible with the executable and functional interface.
     * @return An object of the functional interface type that represents the Lambda.
     * @throws IllegalArgumentException If the interface or executable is null, or the interface does not have one
     *                                  public abstract method.
     * @throws InternalException        If the executable cannot be adapted or the Lambda cannot be instantiated.
     */
    public static <F> F build(
            final Class<F> functionInterfaceType,
            final Executable executable,
            final Class<?> declaringClass) {
        Assert.notNull(functionInterfaceType);
        Assert.notNull(executable);

        final Class<?> effectiveDeclaringClass = declaringClass == null ? executable.getDeclaringClass()
                : declaringClass;
        final Triplet<Class<?>, Executable, Class<?>> cacheKey = Triplet
                .of(functionInterfaceType, executable, effectiveDeclaringClass);
        return (F) CACHE.computeIfAbsent(
                cacheKey,
                key -> doBuildWithoutCache(functionInterfaceType, executable, effectiveDeclaringClass));
    }

    /**
     * Builds a Lambda function based on the provided method or constructor object, effectively proxying the method or
     * constructor through the Lambda function. Invoking the Lambda function is equivalent to executing the
     * corresponding method or constructor.
     *
     * @param <F>            The type of the functional interface.
     * @param funcType       The functional interface type that accepts the Lambda.
     * @param executable     The executable object (method or constructor).
     * @param declaringClass The non-null effective receiver or constructor-result class already selected by the caller.
     * @return An object of the functional interface type that represents the Lambda.
     * @throws InternalException If linking or instantiating the generated implementation fails.
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
     * @param declaringClass The effective receiver or constructor-result class used to form the instantiated method
     *                       type.
     * @return A {@link CallSite} representing the Lambda function.
     * @throws LambdaConversionException If the implementation handle and functional-interface signatures cannot be
     *                                   linked.
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
