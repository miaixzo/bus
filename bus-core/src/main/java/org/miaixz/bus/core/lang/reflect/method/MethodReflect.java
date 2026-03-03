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
package org.miaixz.bus.core.lang.reflect.method;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.miaixz.bus.core.center.set.UniqueKeySet;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ModifierKit;

/**
 * Utility class for method reflection operations. This class provides functionalities to retrieve and filter methods
 * from a class, including declared methods, public methods, and all methods in the class hierarchy.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MethodReflect {

    /**
     * The class for which methods are being reflected.
     */
    private final Class<?> clazz;
    /**
     * Cached array of public methods for the current class and its superclasses/interfaces. This cache is volatile to
     * ensure visibility across threads.
     */
    private volatile Method[] publicMethods;
    /**
     * Cached array of declared methods for the current class (excluding inherited methods). This cache is volatile to
     * ensure visibility across threads.
     */
    private volatile Method[] declaredMethods;
    /**
     * Cached array of all methods (declared and inherited) for the current class. This cache is volatile to ensure
     * visibility across threads.
     */
    private volatile Method[] allMethods;

    /**
     * Constructs a new {@code MethodReflect} instance for the given class.
     *
     * @param clazz The class to reflect. Must not be {@code null}.
     * @throws IllegalArgumentException if {@code clazz} is {@code null}.
     */
    public MethodReflect(final Class<?> clazz) {
        this.clazz = Assert.notNull(clazz);
    }

    /**
     * Creates a new {@code MethodReflect} instance for the given class.
     *
     * @param clazz The class to reflect.
     * @return A new {@code MethodReflect} instance.
     */
    public static MethodReflect of(final Class<?> clazz) {
        return new MethodReflect(clazz);
    }

    /**
     * Generates a unique key for a given method. The key format is:
     * {@code ReturnType#MethodName:Param1Type,Param2Type...}
     *
     * @param method The method for which to generate the unique key.
     * @return A unique string key for the method.
     */
    private static String getUniqueKey(final Method method) {
        final StringBuilder sb = new StringBuilder();
        sb.append(method.getReturnType().getName()).append(Symbol.C_HASH);
        sb.append(method.getName());
        final Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            if (i == 0) {
                sb.append(Symbol.C_COLON);
            } else {
                sb.append(Symbol.C_COMMA);
            }
            sb.append(parameters[i].getName());
        }
        return sb.toString();
    }

    /**
     * Retrieves all non-abstract (default) methods from the interfaces implemented by the given class.
     *
     * @param clazz The class whose interfaces are to be scanned.
     * @return A {@link List} of non-abstract methods from the implemented interfaces.
     */
    private static List<Method> getDefaultMethodsFromInterface(final Class<?> clazz) {
        final List<Method> result = new ArrayList<>();
        for (final Class<?> ifc : clazz.getInterfaces()) {
            for (final Method m : ifc.getMethods()) {
                if (!ModifierKit.isAbstract(m)) {
                    result.add(m);
                }
            }
        }
        return result;
    }

    /**
     * Retrieves the class associated with this {@code MethodReflect} instance.
     *
     * @return The reflected class.
     */
    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * Clears all cached method arrays. This method should be called if the class structure (methods) changes
     * dynamically.
     */
    synchronized public void clearCaches() {
        publicMethods = null;
        declaredMethods = null;
        allMethods = null;
    }

    /**
     * Retrieves all public methods of the current class and its superclasses/interfaces, equivalent to
     * {@link Class#getMethods()}. The result is cached after the first call.
     *
     * @param predicate A method filter. If {@code null}, no filtering is applied.
     * @return An array of public methods that satisfy the predicate.
     */
    public Method[] getPublicMethods(final Predicate<Method> predicate) {
        if (null == publicMethods) {
            synchronized (MethodReflect.class) {
                if (null == publicMethods) {
                    publicMethods = clazz.getMethods();
                }
            }
        }
        return ArrayKit.filter(publicMethods, predicate);
    }

    /**
     * Retrieves all methods declared directly by the current class, equivalent to {@link Class#getDeclaredMethods()}.
     * The result is cached after the first call.
     *
     * @param predicate A method filter. If {@code null}, no filtering is applied.
     * @return An array of declared methods that satisfy the predicate.
     */
    public Method[] getDeclaredMethods(final Predicate<Method> predicate) {
        if (null == declaredMethods) {
            synchronized (MethodReflect.class) {
                if (null == declaredMethods) {
                    declaredMethods = clazz.getDeclaredMethods();
                }
            }
        }
        return ArrayKit.filter(declaredMethods, predicate);
    }

    /**
     * Retrieves all methods in the current class's hierarchy. This is equivalent to traversing the class and all its
     * superclasses and interfaces in a breadth-first manner, and calling {@link Class#getDeclaredMethods()} on each.
     * The methods are ordered as follows:
     * <ul>
     * <li>Methods closer to the {@code type} (current class) appear earlier.</li>
     * <li>For methods at the same distance from {@code type}, methods from directly implemented interfaces take
     * precedence over methods from superclasses.</li>
     * <li>For interfaces at the same distance from {@code type}, the order follows the declaration order in
     * {@link Class#getInterfaces()}.</li>
     * </ul>
     * The result is cached after the first call.
     *
     * @param predicate A method filter. If {@code null}, no filtering is applied.
     * @return An array of all methods in the class hierarchy that satisfy the predicate.
     */
    public Method[] getAllMethods(final Predicate<Method> predicate) {
        if (null == allMethods) {
            synchronized (MethodReflect.class) {
                if (null == allMethods) {
                    allMethods = getMethodsDirectly(true, true);
                }
            }
        }
        return ArrayKit.filter(allMethods, predicate);
    }

    /**
     * Retrieves a list of all methods in a class, directly using reflection without caching. The methods retrieved
     * include:
     * <ul>
     * <li>All methods (including static methods) in the current class.</li>
     * <li>All methods (including static methods) in superclasses.</li>
     * <li>All methods (including static methods) in {@code Object.class}.</li>
     * </ul>
     *
     * @param withSupers           Whether to include methods from superclasses or interfaces.
     * @param withMethodFromObject Whether to include methods from {@code Object.class}.
     * @return An array of methods.
     * @throws SecurityException If a security manager exists and its {@code checkMemberAccess} method denies access.
     */
    public Method[] getMethodsDirectly(final boolean withSupers, final boolean withMethodFromObject)
            throws SecurityException {
        final Class<?> clazz = this.clazz;

        if (clazz.isInterface()) {
            // For interfaces, directly call Class.getMethods to get all methods, as interface methods are always
            // public.
            return withSupers ? clazz.getMethods() : clazz.getDeclaredMethods();
        }

        final UniqueKeySet<String, Method> result = new UniqueKeySet<>(true, MethodReflect::getUniqueKey);
        Class<?> searchType = clazz;
        while (searchType != null) {
            if (!withMethodFromObject && Object.class == searchType) {
                break;
            }
            // All methods declared in the current class
            result.addAllIfAbsent(Arrays.asList(searchType.getDeclaredMethods()));
            // All default methods from implemented interfaces
            result.addAllIfAbsent(getDefaultMethodsFromInterface(searchType));

            searchType = (withSupers && !searchType.isInterface()) ? searchType.getSuperclass() : null;
        }

        return result.toArray(new Method[0]);
    }

}
