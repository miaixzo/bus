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
package org.miaixz.bus.core.lang.reflect.method;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.Invoker;
import org.miaixz.bus.core.xyz.*;

/**
 * Method invoker for invoking methods using reflection. This class provides a unified way to invoke methods, handling
 * argument conversion and access.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MethodInvoker implements Invoker {

    /**
     * The underlying {@link Method} to be invoked.
     */
    private final Method method;
    /**
     * An array of {@link Type} objects representing the generic parameter types of the method.
     */
    private final Type[] paramTypes;
    /**
     * An array of {@link Class} objects representing the raw parameter types of the method.
     */
    private final Class<?>[] paramTypeClasses;
    /**
     * The return type of the method, or the type of the single parameter if it's a setter-like method.
     */
    private final Type type;
    /**
     * The raw class of the return type of the method, or the raw class of the single parameter if it's a setter-like
     * method.
     */
    private final Class<?> typeClass;
    /**
     * Flag indicating whether to check and potentially convert arguments before invocation.
     */
    private boolean checkArgs;

    /**
     * Constructs a new {@code MethodInvoker} for the given method. The method's accessibility is set to true.
     *
     * @param method The method to be invoked. Must not be {@code null}.
     * @throws IllegalArgumentException if {@code method} is {@code null}.
     */
    public MethodInvoker(final Method method) {
        this.method = ReflectKit.setAccessible(Assert.notNull(method));

        this.paramTypes = TypeKit.getParamTypes(method);
        this.paramTypeClasses = method.getParameterTypes();
        if (paramTypes.length == 1) {
            // For setter-like methods, the type is considered to be the parameter type.
            type = paramTypes[0];
            typeClass = paramTypeClasses[0];
        } else {
            type = method.getReturnType();
            typeClass = method.getReturnType();
        }
    }

    /**
     * Creates a new {@code MethodInvoker} instance for the given method.
     *
     * @param method The method to be invoked.
     * @return A new {@code MethodInvoker} instance, or {@code null} if the provided method is {@code null}.
     */
    public static MethodInvoker of(final Method method) {
        return null == method ? null : new MethodInvoker(method);
    }

    /**
     * Executes a method on an object or interface. This method automatically converts arguments to match the method's
     * parameter types.
     *
     * <pre class="code">
     * 
     * interface Duck {
     * 
     *     default String quack() {
     *         return "Quack";
     *     }
     * }
     *
     * Duck duck = (Duck) Proxy
     *         .newProxyInstance(ClassKit.getClassLoader(), new Class[] { Duck.class }, MethodInvoker::invoke);
     * </pre>
     *
     * @param <T>    The return type of the method.
     * @param object The target object or proxy object on which the method is to be invoked.
     * @param method The method to invoke.
     * @param args   The arguments for the method call. These arguments will be automatically converted to match the
     *               method's defined parameter types.
     * @return The result of the method invocation.
     * @throws InternalException If an error occurs during method execution.
     */
    public static <T> T invoke(final Object object, final Method method, final Object... args)
            throws InternalException {
        Assert.notNull(method, "Method must be not null!");
        return invokeExact(object, method, MethodKit.actualArgs(method, args));
    }

    /**
     * Executes a method on an object or interface, without argument type conversion. The provided arguments must
     * exactly match the method's parameter types.
     *
     * <pre class="code">
     * 
     * interface Duck {
     * 
     *     default String quack() {
     *         return "Quack";
     *     }
     * }
     *
     * Duck duck = (Duck) Proxy
     *         .newProxyInstance(MethodInvoker.getClassLoader(), new Class[] { Duck.class }, MethodInvoker::invoke);
     * </pre>
     *
     * @param <T>    The return type of the method.
     * @param object The target object or proxy object on which the method is to be invoked.
     * @param method The method to invoke.
     * @param args   The arguments for the method call. These arguments must exactly match the method's parameter types.
     * @return The result of the method invocation.
     * @throws InternalException If an error occurs during method execution.
     */
    public static <T> T invokeExact(final Object object, final Method method, final Object... args)
            throws InternalException {
        Assert.notNull(method, "Method must be not null!");
        java.lang.invoke.MethodHandle handle;
        try {
            handle = LookupKit.unreflectMethod(method);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }

        if (null != object) {
            handle = handle.bindTo(object);
        }
        return invokeHandle(handle, args);
    }

    /**
     * Executes a {@link MethodHandle}, wrapping {@link MethodHandle#invokeWithArguments(Object...)}. For non-static
     * methods, {@link MethodHandle#bindTo(Object)} must be called first to bind the execution object.
     *
     * <p>
     * Note that this method uses {@link MethodHandle#invokeWithArguments(Object...)} instead of
     * {@link MethodHandle#invoke(Object...)}, because the latter requires the first parameter to be the object or class
     * itself. {@code invokeWithArguments} only requires the method parameters.
     *
     * @param methodHandle The {@link java.lang.invoke.MethodHandle} to invoke.
     * @param args         The method parameter values. Supports subclass conversion and auto-boxing/unboxing.
     * @param <T>          The return type of the method.
     * @return The return value of the method.
     * @throws InternalException If an error occurs during method handle invocation.
     */
    public static <T> T invokeHandle(final MethodHandle methodHandle, final Object... args) {
        try {
            return (T) methodHandle.invokeWithArguments(args);
        } catch (final Throwable e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

    /**
     * Retrieves the underlying {@link Method} object.
     *
     * @return The {@link Method} object associated with this invoker.
     */
    public Method getMethod() {
        return this.method;
    }

    /**
     * Retrieves the generic parameter types of the method.
     *
     * @return An array of {@link Type} objects representing the generic parameter types.
     */
    public Type[] getParamTypes() {
        return this.paramTypes;
    }

    /**
     * Retrieves the return type of the method.
     *
     * @return The {@link Type} object representing the return type.
     */
    public Type getReturnType() {
        return this.method.getReturnType();
    }

    /**
     * Gets the name of this object.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return this.method.getName();
    }

    /**
     * Gettype method.
     *
     * @return the Type value
     */
    @Override
    public Type getType() {
        return this.type;
    }

    /**
     * Gettypeclass method.
     *
     * @return the Class&lt;?&gt; value
     */
    @Override
    public Class<?> getTypeClass() {
        return this.typeClass;
    }

    /**
     * Sets whether to check arguments before method invocation.
     * 
     * <pre>
     * 1. Checks if the number of arguments matches the method's parameter count.
     * 2. If a parameter is {@code
     * null
     * } but the corresponding method parameter is a primitive type,
     *    it assigns the default value for that primitive type.
     * </pre>
     *
     * @param checkArgs {@code true} to enable argument checking, {@code false} otherwise.
     * @return This {@code MethodInvoker} instance for method chaining.
     */
    public MethodInvoker setCheckArgs(final boolean checkArgs) {
        this.checkArgs = checkArgs;
        return this;
    }

    /**
     * Invokes the method on the specified target object with the given arguments. If {@code checkArgs} is enabled,
     * arguments will be validated and potentially converted.
     *
     * @param target The target object on which the method is to be invoked. For static methods, this can be
     *               {@code null}.
     * @param args   The arguments to be passed to the method.
     * @param <T>    The expected return type of the method.
     * @return The result of the method invocation.
     * @throws InternalException If an error occurs during method invocation.
     */
    @Override
    public <T> T invoke(Object target, final Object... args) throws InternalException {
        if (this.checkArgs) {
            checkArgs(args);
        }

        final Method method = this.method;
        // For static method calls, the target object should be null.
        if (ModifierKit.isStatic(method)) {
            target = null;
        }
        // Normalize and convert arguments based on the method's defined parameter types.
        final Object[] actualArgs = MethodKit.actualArgs(method, args);
        try {
            // Using MethodHandle for invocation.
            // Note: The strategy for lambda generation has been changed; dynamically generated lambdas
            // are not released from metaspace, leading to high resource consumption.
            return invokeExact(target, method, actualArgs);
        } catch (final Exception e) {
            // Fallback to traditional reflection if MethodHandle invocation fails.
            try {
                return (T) method.invoke(target, actualArgs);
            } catch (final IllegalAccessException | InvocationTargetException ex) {
                throw new InternalException(ex);
            }
        }
    }

    /**
     * Invokes a static method with the given arguments.
     *
     * @param <T>  The expected return type of the method.
     * @param args The arguments for the static method call.
     * @return The result of the static method invocation.
     * @throws InternalException If an error occurs during method invocation.
     */
    public <T> T invokeStatic(final Object... args) throws InternalException {
        return invoke(null, args);
    }

    /**
     * Checks the validity of the provided arguments against the method's parameter types.
     * <ul>
     * <li>Ensures the number of arguments matches the method's parameter count.</li>
     * <li>If a parameter is {@code null} but the corresponding method parameter is a primitive type, it assigns the
     * default value for that primitive type to prevent {@code NullPointerException}.</li>
     * </ul>
     *
     * @param args The array of arguments to be checked. Must not be {@code null}.
     * @throws IllegalArgumentException If the number of arguments does not match the method's parameter count, or if
     *                                  {@code args} is {@code null}.
     */
    private void checkArgs(final Object[] args) {
        final Class<?>[] paramTypeClasses = this.paramTypeClasses;
        if (null != args) {
            Assert.isTrue(
                    args.length == paramTypeClasses.length,
                    "Params length [{}] is not fit for param length [{}] of method !",
                    args.length,
                    paramTypeClasses.length);
            Class<?> type;
            for (int i = 0; i < args.length; i++) {
                type = paramTypeClasses[i];
                if (type.isPrimitive() && null == args[i]) {
                    // If the parameter is a primitive type and the passed argument is null, assign the default value.
                    args[i] = ClassKit.getDefaultValue(type);
                }
            }
        }
    }

}
