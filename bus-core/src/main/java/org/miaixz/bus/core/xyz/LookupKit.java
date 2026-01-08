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
package org.miaixz.bus.core.xyz;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.lookup.LookupFactory;
import org.miaixz.bus.core.lang.reflect.lookup.MethodLookupFactory;

/**
 * Utility for {@link MethodHandles.Lookup}.
 * <p>
 * A {@link MethodHandles.Lookup} is an object for finding method handles in a specific class. In JDK 8, the `Lookup`
 * object obtained by directly calling {@link MethodHandles#lookup()} may lack sufficient permissions for `findSpecial`
 * and `unreflectSpecial`, leading to a "no private access for invokespecial" exception. This utility provides a
 * workaround for both JDK 8 and JDK 9+.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LookupKit {

    private static final LookupFactory factory;

    static {
        factory = new MethodLookupFactory();
    }

    /**
     * Gets a {@link MethodHandles.Lookup} instance with appropriate permissions. This provides a cross-JDK solution to
     * the permission issues with `findSpecial` and `unreflectSpecial`.
     *
     * @return A {@link MethodHandles.Lookup} instance.
     */
    public static MethodHandles.Lookup lookup() {
        return lookup(CallerKit.getCaller());
    }

    /**
     * Gets a {@link MethodHandles.Lookup} instance for a specific class with appropriate permissions.
     *
     * @param callerClass The class or interface to look up from.
     * @return A {@link MethodHandles.Lookup} instance.
     */
    public static MethodHandles.Lookup lookup(final Class<?> callerClass) {
        return factory.lookup(callerClass);
    }

    /**
     * Wraps a {@link Method} or {@link Constructor} into a {@link MethodHandle}.
     *
     * @param methodOrConstructor The {@link Method} or {@link Constructor}.
     * @return The corresponding {@link MethodHandle}.
     * @throws InternalException wrapping {@link IllegalAccessException}.
     */
    public static MethodHandle unreflect(final Member methodOrConstructor) throws InternalException {
        try {
            if (methodOrConstructor instanceof Method) {
                return unreflectMethod((Method) methodOrConstructor);
            } else {
                return lookup().unreflectConstructor((Constructor<?>) methodOrConstructor);
            }
        } catch (final IllegalAccessException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Converts a {@link Method} into a {@link MethodHandle}.
     *
     * @param method The {@link Method}.
     * @return The {@link MethodHandle}.
     * @throws IllegalAccessException if access is denied.
     */
    public static MethodHandle unreflectMethod(final Method method) throws IllegalAccessException {
        final Class<?> caller = method.getDeclaringClass();
        final MethodHandles.Lookup lookup = lookup(caller);
        if (ModifierKit.isDefault(method)) {
            // For default methods, especially on proxy objects, use unreflectSpecial
            // to avoid stack overflow from recursive invocation.
            return lookup.unreflectSpecial(method, caller);
        }

        try {
            return lookup.unreflect(method);
        } catch (final Exception ignore) {
            // In some cases where direct unreflection fails due to permissions, try unreflectSpecial.
            return lookup.unreflectSpecial(method, caller);
        }
    }

    /**
     * Finds a method handle for a specified method. This method searches for:
     * <ul>
     * <li>Methods in the current class (including constructors and private methods).</li>
     * <li>Methods in superclasses (including constructors and private methods).</li>
     * <li>Static methods in the current class.</li>
     * </ul>
     *
     * @param callerClass The class or interface where the method is located.
     * @param name        The method name; if null or blank, searches for a constructor.
     * @param returnType  The return type.
     * @param argTypes    The parameter types.
     * @return The {@link MethodHandle}, or `null` if not found.
     */
    public static MethodHandle findMethod(
            final Class<?> callerClass,
            final String name,
            final Class<?> returnType,
            final Class<?>... argTypes) {
        return findMethod(callerClass, name, MethodType.methodType(returnType, argTypes));
    }

    /**
     * Finds a method handle for a specified method.
     *
     * @param callerClass The class or interface where the method is located.
     * @param name        The method name; if null or blank, searches for a constructor.
     * @param type        The method type (return type and parameter types).
     * @return The {@link MethodHandle}, or `null` if not found.
     */
    public static MethodHandle findMethod(final Class<?> callerClass, final String name, final MethodType type) {
        if (StringKit.isBlank(name)) {
            return findConstructor(callerClass, type);
        }

        MethodHandle handle = null;
        final MethodHandles.Lookup lookup = LookupKit.lookup(callerClass);
        // Member methods
        try {
            handle = lookup.findVirtual(callerClass, name, type);
        } catch (final IllegalAccessException | NoSuchMethodException ignore) {
            // ignore
        }

        // Static methods
        if (null == handle) {
            try {
                handle = lookup.findStatic(callerClass, name, type);
            } catch (final IllegalAccessException | NoSuchMethodException ignore) {
                // ignore
            }
        }

        // Special methods (constructors, private methods, etc.)
        if (null == handle) {
            try {
                handle = lookup.findSpecial(callerClass, name, type, callerClass);
            } catch (final NoSuchMethodException ignore) {
                // ignore
            } catch (final IllegalAccessException e) {
                throw new InternalException(e);
            }
        }

        return handle;
    }

    /**
     * Finds a constructor handle.
     *
     * @param callerClass The class.
     * @param argTypes    The parameter types.
     * @return The constructor's method handle.
     */
    public static MethodHandle findConstructor(final Class<?> callerClass, final Class<?>... argTypes) {
        final Constructor<?> constructor = ReflectKit.getConstructor(callerClass, argTypes);
        if (null != constructor) {
            return LookupKit.unreflect(constructor);
        }
        return null;
    }

    /**
     * Finds a constructor handle with exact parameter type matching.
     *
     * @param callerClass The class.
     * @param argTypes    The exact parameter types.
     * @return The constructor's method handle.
     */
    public static MethodHandle findConstructorExact(final Class<?> callerClass, final Class<?>... argTypes) {
        return findConstructor(callerClass, MethodType.methodType(void.class, argTypes));
    }

    /**
     * Finds a constructor handle.
     *
     * @param callerClass The class.
     * @param type        The constructor's method type (return type must be `void.class`).
     * @return The constructor's method handle.
     */
    public static MethodHandle findConstructor(final Class<?> callerClass, final MethodType type) {
        final MethodHandles.Lookup lookup = lookup(callerClass);
        try {
            return lookup.findConstructor(callerClass, type);
        } catch (final NoSuchMethodException e) {
            return null;
        } catch (final IllegalAccessException e) {
            throw new InternalException(e);
        }
    }

}
