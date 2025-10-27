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
package org.miaixz.bus.core.xyz;

import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.miaixz.bus.core.bean.NullWrapper;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.reflect.method.MethodInvoker;
import org.miaixz.bus.core.lang.reflect.method.MethodReflect;

/**
 * Utility class for reflection on {@link Method}s, including method retrieval and invocation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MethodKit {

    /**
     * Method cache.
     */
    private static final WeakConcurrentMap<Class<?>, MethodReflect> METHODS_CACHE = new WeakConcurrentMap<>();

    /**
     * Clears the method cache.
     */
    synchronized static void clearCache() {
        METHODS_CACHE.clear();
    }

    /**
     * Finds the first matching method from a `Method` array based on a given condition.
     *
     * @param methods   The array of methods to search.
     * @param predicate The condition to match.
     * @return The first matching method, or `null` if not found.
     */
    public static Method getMethod(final Method[] methods, final Predicate<Method> predicate) {
        return ArrayKit.get(methods, predicate);
    }

    /**
     * Gets a set of unique public method names from a class and its superclasses.
     *
     * @param clazz The class.
     * @return A set of method names.
     */
    public static Set<String> getPublicMethodNames(final Class<?> clazz) {
        return StreamKit.of(getPublicMethods(clazz)).map(Method::getName).collect(Collectors.toSet());
    }

    /**
     * Finds a public method by name and parameter types.
     *
     * @param clazz      The class.
     * @param ignoreCase If true, ignores case for the method name.
     * @param methodName The method name.
     * @param paramTypes The parameter types.
     * @return The `Method` object, or `null` if not found.
     * @throws SecurityException if access is denied.
     */
    public static Method getPublicMethod(
            final Class<?> clazz,
            final boolean ignoreCase,
            final String methodName,
            final Class<?>... paramTypes) throws SecurityException {
        if (null == clazz || StringKit.isBlank(methodName)) {
            return null;
        }
        return getMethod(getPublicMethods(clazz), ignoreCase, methodName, paramTypes);
    }

    /**
     * Finds a method in an object by name and arguments.
     *
     * @param object     The object to inspect.
     * @param methodName The method name.
     * @param args       The arguments.
     * @return The `Method` object.
     * @throws SecurityException if access is denied.
     */
    public static Method getMethodOfObject(final Object object, final String methodName, final Object... args)
            throws SecurityException {
        if (null == object || StringKit.isBlank(methodName)) {
            return null;
        }
        return getMethod(object.getClass(), methodName, ClassKit.getClasses(args));
    }

    /**
     * Finds a method by name, ignoring case.
     *
     * @param clazz      The class.
     * @param methodName The method name.
     * @param paramTypes The parameter types.
     * @return The `Method` object.
     * @throws SecurityException if access is denied.
     */
    public static Method getMethodIgnoreCase(
            final Class<?> clazz,
            final String methodName,
            final Class<?>... paramTypes) throws SecurityException {
        return getMethod(clazz, true, methodName, paramTypes);
    }

    /**
     * Finds a method by name.
     *
     * @param clazz      The class.
     * @param methodName The method name.
     * @param paramTypes The parameter types.
     * @return The `Method` object.
     * @throws SecurityException if access is denied.
     */
    public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... paramTypes)
            throws SecurityException {
        return getMethod(clazz, false, methodName, paramTypes);
    }

    /**
     * Finds a method by name from a class.
     *
     * @param clazz      The class.
     * @param ignoreCase If true, ignores case for the method name.
     * @param methodName The method name.
     * @param paramTypes The parameter types.
     * @return The `Method` object.
     * @throws SecurityException if access is denied.
     */
    public static Method getMethod(
            final Class<?> clazz,
            final boolean ignoreCase,
            final String methodName,
            final Class<?>... paramTypes) throws SecurityException {
        if (null == clazz || StringKit.isBlank(methodName)) {
            return null;
        }
        return getMethod(getMethods(clazz), ignoreCase, methodName, paramTypes);
    }

    /**
     * Finds a method from an array of methods that matches the given name and parameter types.
     *
     * @param methods    The array of methods to search.
     * @param ignoreCase If true, ignores case for the method name.
     * @param methodName The method name.
     * @param paramTypes The parameter types.
     * @return The matching `Method`, or `null` if not found.
     * @throws SecurityException if access is denied.
     */
    public static Method getMethod(
            final Method[] methods,
            final boolean ignoreCase,
            final String methodName,
            final Class<?>... paramTypes) throws SecurityException {
        if (ArrayKit.isEmpty(methods) || StringKit.isBlank(methodName)) {
            return null;
        }

        Method res = null;
        if (ArrayKit.isNotEmpty(methods)) {
            for (final Method method : methods) {
                if (StringKit.equals(methodName, method.getName(), ignoreCase)
                        && ClassKit.isAllAssignableFrom(method.getParameterTypes(), paramTypes)
                        // Exclude covariant bridge methods
                        && (res == null || res.getReturnType().isAssignableFrom(method.getReturnType()))) {
                    res = method;
                }
            }
        }
        return res;
    }

    /**
     * Finds the first method with a given name.
     *
     * @param clazz      The class.
     * @param methodName The method name.
     * @return The `Method` object.
     * @throws SecurityException if access is denied.
     */
    public static Method getMethodByName(final Class<?> clazz, final String methodName) throws SecurityException {
        return getMethodByName(clazz, false, methodName);
    }

    /**
     * Finds the first method with a given name, ignoring case.
     *
     * @param clazz      The class.
     * @param methodName The method name.
     * @return The `Method` object.
     * @throws SecurityException if access is denied.
     */
    public static Method getMethodByNameIgnoreCase(final Class<?> clazz, final String methodName)
            throws SecurityException {
        return getMethodByName(clazz, true, methodName);
    }

    /**
     * Finds the first method with a given name.
     *
     * @param clazz      The class.
     * @param ignoreCase If true, ignores case.
     * @param methodName The method name.
     * @return The `Method` object.
     * @throws SecurityException if access is denied.
     */
    public static Method getMethodByName(final Class<?> clazz, final boolean ignoreCase, final String methodName)
            throws SecurityException {
        if (null == clazz || StringKit.isBlank(methodName)) {
            return null;
        }

        final Method[] methods = getMethods(
                clazz,
                (method -> StringKit.equals(methodName, method.getName(), ignoreCase)
                        && (method.getReturnType().isAssignableFrom(method.getReturnType()))));

        return ArrayKit.isEmpty(methods) ? null : methods[0];
    }

    /**
     * Gets a set of unique method names from a class.
     *
     * @param clazz The class.
     * @return A set of method names.
     * @throws SecurityException if access is denied.
     */
    public static Set<String> getMethodNames(final Class<?> clazz) throws SecurityException {
        return StreamKit.of(getMethods(clazz, null)).map(Method::getName).collect(Collectors.toSet());
    }

    /**
     * Gets all methods of a class, including inherited ones.
     *
     * @param clazz The class.
     * @return An array of methods.
     * @throws SecurityException if access is denied.
     */
    public static Method[] getMethods(final Class<?> clazz) throws SecurityException {
        return getMethods(clazz, null);
    }

    /**
     * Gets all methods of a class that satisfy a predicate, including inherited ones.
     *
     * @param clazz     The class.
     * @param predicate A predicate to filter the methods.
     * @return An array of methods.
     * @throws SecurityException if access is denied.
     */
    public static Method[] getMethods(final Class<?> clazz, final Predicate<Method> predicate)
            throws SecurityException {
        return METHODS_CACHE.computeIfAbsent(Assert.notNull(clazz), MethodReflect::of).getAllMethods(predicate);
    }

    /**
     * Gets all public methods of a class and its superclasses.
     *
     * @param clazz The class to inspect.
     * @return An array of public methods.
     */
    public static Method[] getPublicMethods(final Class<?> clazz) {
        return getPublicMethods(clazz, null);
    }

    /**
     * Gets all public methods of a class and its superclasses that satisfy a predicate.
     *
     * @param clazz     The class to inspect.
     * @param predicate A predicate to filter the methods.
     * @return An array of filtered public methods.
     */
    public static Method[] getPublicMethods(final Class<?> clazz, final Predicate<Method> predicate) {
        return METHODS_CACHE.computeIfAbsent(Assert.notNull(clazz), MethodReflect::of).getPublicMethods(predicate);
    }

    /**
     * Gets all methods directly declared by a class, not including inherited ones.
     *
     * @param clazz The class.
     * @return An array of declared methods.
     * @throws SecurityException if access is denied.
     */
    public static Method[] getDeclaredMethods(final Class<?> clazz) throws SecurityException {
        return getDeclaredMethods(clazz, null);
    }

    /**
     * Gets all methods directly declared by a class that satisfy a predicate.
     *
     * @param clazz     The class.
     * @param predicate A predicate to filter the methods.
     * @return An array of declared methods.
     * @throws SecurityException if access is denied.
     */
    public static Method[] getDeclaredMethods(final Class<?> clazz, final Predicate<Method> predicate)
            throws SecurityException {
        return METHODS_CACHE.computeIfAbsent(Assert.notNull(clazz), MethodReflect::of).getDeclaredMethods(predicate);
    }

    /**
     * Gets all methods of a class directly via reflection (no cache), including from interfaces and default methods.
     *
     * @param beanClass            The class or interface.
     * @param withSupers           If true, includes methods from superclasses/interfaces.
     * @param withMethodFromObject If true, includes methods from the `Object` class.
     * @return An array of methods.
     * @throws SecurityException if access is denied.
     */
    public static Method[] getMethodsDirectly(
            final Class<?> beanClass,
            final boolean withSupers,
            final boolean withMethodFromObject) throws SecurityException {
        return MethodReflect.of(Assert.notNull(beanClass)).getMethodsDirectly(withSupers, withMethodFromObject);
    }

    /**
     * Checks if a method is a method from the `Object` class.
     *
     * @param method The method.
     * @return `true` if it's an `Object` method.
     */
    public static boolean isObjectMethod(Method method) {
        return method != null && (method.getDeclaringClass() == Object.class || isEqualsMethod(method)
                || isHashCodeMethod(method) || isToStringMethod(method));
    }

    /**
     * Checks if a method is an attribute-style method (no parameters, non-void return type).
     *
     * @param method The method.
     * @return `true` if it's an attribute method.
     */
    public static boolean isAttributeMethod(Method method) {
        return method != null && method.getParameterTypes().length == 0 && method.getReturnType() != void.class;
    }

    /**
     * Checks if a method is the `equals` method.
     *
     * @param method The method.
     * @return `true` if it's the `equals` method.
     */
    public static boolean isEqualsMethod(final Method method) {
        if (method == null || 1 != method.getParameterCount() || !Normal.EQUALS.equals(method.getName())) {
            return false;
        }
        return (method.getParameterTypes()[0] == Object.class);
    }

    /**
     * Checks if a method is the `hashCode` method.
     *
     * @param method The method.
     * @return `true` if it's the `hashCode` method.
     */
    public static boolean isHashCodeMethod(final Method method) {
        return method != null && Normal.HASHCODE.equals(method.getName()) && isEmptyParam(method);
    }

    /**
     * Checks if a method is the `toString` method.
     *
     * @param method The method.
     * @return `true` if it's the `toString` method.
     */
    public static boolean isToStringMethod(final Method method) {
        return method != null && Normal.TOSTRING.equals(method.getName()) && isEmptyParam(method);
    }

    /**
     * Checks if a method has no parameters.
     *
     * @param method The method.
     * @return `true` if it has no parameters.
     */
    public static boolean isEmptyParam(final Method method) {
        return method.getParameterCount() == 0;
    }

    /**
     * Checks if a method is a getter or setter (case-insensitive).
     *
     * @param method The method.
     * @return `true` if it's a getter or setter.
     */
    public static boolean isGetterOrSetterIgnoreCase(final Method method) {
        return isGetterOrSetter(method, true);
    }

    /**
     * Checks if a method is a getter or setter.
     *
     * @param method     The method.
     * @param ignoreCase If true, ignores case for the method name.
     * @return `true` if it's a getter or setter.
     */
    public static boolean isGetterOrSetter(final Method method, final boolean ignoreCase) {
        final int parameterCount = method.getParameterCount();
        switch (parameterCount) {
            case 0:
                return isGetter(method, ignoreCase);

            case 1:
                return isSetter(method, ignoreCase);

            default:
                return false;
        }
    }

    /**
     * Checks if a method is a setter.
     *
     * @param method     The method.
     * @param ignoreCase If true, ignores case for the method name.
     * @return `true` if it's a setter.
     */
    public static boolean isSetter(final Method method, final boolean ignoreCase) {
        if (null == method) {
            return false;
        }
        if (1 != method.getParameterCount()) {
            return false;
        }
        String name = method.getName();
        if (name.length() < 4) {
            return false;
        }
        if (ignoreCase) {
            name = name.toLowerCase();
        }
        return name.startsWith(Normal.SET);
    }

    /**
     * Checks if a method is a getter.
     *
     * @param method     The method.
     * @param ignoreCase If true, ignores case for the method name.
     * @return `true` if it's a getter.
     */
    public static boolean isGetter(final Method method, final boolean ignoreCase) {
        if (null == method) {
            return false;
        }
        if (0 != method.getParameterCount()) {
            return false;
        }
        if (Void.class == method.getReturnType()) {
            return false;
        }
        String name = method.getName();
        if (name.length() < 3 || "getClass".equals(name) || Normal.GET.equals(name)) {
            return false;
        }
        if (ignoreCase) {
            name = name.toLowerCase();
        }
        if (name.startsWith(Normal.IS)) {
            return BooleanKit.isBoolean(method.getReturnType());
        }
        return name.startsWith(Normal.GET);
    }

    /**
     * Invokes a static method.
     *
     * @param <T>    The return type.
     * @param method The method.
     * @param args   The arguments.
     * @return The result of the invocation.
     * @throws InternalException if invocation fails.
     */
    public static <T> T invokeStatic(final Method method, final Object... args) throws InternalException {
        return invoke(null, method, args);
    }

    /**
     * Invokes a method with argument checking and type conversion.
     *
     * @param <T>    The return type.
     * @param object The object to invoke the method on (`null` for static methods).
     * @param method The method.
     * @param args   The arguments.
     * @return The result of the invocation.
     * @throws InternalException if invocation fails.
     */
    public static <T> T invokeWithCheck(final Object object, final Method method, final Object... args)
            throws InternalException {
        return MethodInvoker.of(method).setCheckArgs(true).invoke(object, args);
    }

    /**
     * Invokes a method with automatic argument handling (padding, conversion).
     *
     * @param <T>    The return type.
     * @param object The object to invoke the method on (`null` for static methods).
     * @param method The method.
     * @param args   The arguments.
     * @return The result of the invocation.
     * @throws InternalException if invocation fails.
     */
    public static <T> T invoke(final Object object, final Method method, final Object... args)
            throws InternalException {
        return MethodInvoker.of(method).invoke(object, args);
    }

    /**
     * Invokes a method on an object by name.
     *
     * @param <T>        The return type.
     * @param object     The object.
     * @param methodName The method name.
     * @param args       The arguments.
     * @return The result of the invocation.
     * @throws InternalException if the method doesn't exist or invocation fails.
     */
    public static <T> T invoke(final Object object, final String methodName, final Object... args)
            throws InternalException {
        Assert.notNull(object, "Object to get method must be not null!");
        Assert.notBlank(methodName, "Method name must be not blank!");

        final Method method = getMethodOfObject(object, methodName, args);
        if (null == method) {
            throw new InternalException("No such method: [{}] from [{}]", methodName, object.getClass());
        }
        return invoke(object, method, args);
    }

    /**
     * Invokes a method specified by a "className#methodName" string.
     *
     * @param <T>                     The return type.
     * @param classNameWithMethodName The string expression.
     * @param args                    The arguments.
     * @return The result of the invocation.
     */
    public static <T> T invoke(final String classNameWithMethodName, final Object[] args) {
        return invoke(classNameWithMethodName, false, args);
    }

    /**
     * Invokes a method specified by a "className#methodName" string.
     *
     * @param <T>                     The return type.
     * @param classNameWithMethodName The string expression.
     * @param isSingleton             If true, uses a singleton instance of the class.
     * @param args                    The arguments.
     * @return The result of the invocation.
     */
    public static <T> T invoke(final String classNameWithMethodName, final boolean isSingleton, final Object... args) {
        if (StringKit.isBlank(classNameWithMethodName)) {
            throw new InternalException("Blank classNameDotMethodName!");
        }

        int splitIndex = classNameWithMethodName.lastIndexOf(Symbol.C_HASH);
        if (splitIndex <= 0) {
            splitIndex = classNameWithMethodName.lastIndexOf('.');
        }
        if (splitIndex <= 0) {
            throw new InternalException("Invalid classNameWithMethodName [{}]!", classNameWithMethodName);
        }

        final String className = classNameWithMethodName.substring(0, splitIndex);
        final String methodName = classNameWithMethodName.substring(splitIndex + 1);

        return invoke(className, methodName, isSingleton, args);
    }

    /**
     * Invokes a method by class and method name.
     *
     * @param <T>        The return type.
     * @param className  The fully qualified class name.
     * @param methodName The method name.
     * @param args       The arguments.
     * @return The result of the invocation.
     */
    public static <T> T invoke(final String className, final String methodName, final Object[] args) {
        return invoke(className, methodName, false, args);
    }

    /**
     * Invokes a method by class and method name.
     *
     * @param <T>         The return type.
     * @param className   The fully qualified class name.
     * @param methodName  The method name.
     * @param isSingleton If true, uses a singleton instance of the class.
     * @param args        The arguments.
     * @return The result of the invocation.
     */
    public static <T> T invoke(
            final String className,
            final String methodName,
            final boolean isSingleton,
            final Object... args) {
        final Class<?> clazz = ClassKit.loadClass(className);
        try {
            final Method method = getMethod(clazz, methodName, ClassKit.getClasses(args));
            if (null == method) {
                throw new NoSuchMethodException(StringKit.format("No such method: [{}]", methodName));
            }
            if (ModifierKit.isStatic(method)) {
                return invoke(null, method, args);
            } else {
                return invoke(isSingleton ? Instances.get(clazz) : ReflectKit.newInstance(clazz), method, args);
            }
        } catch (final Exception e) {
            throw ExceptionKit.wrapRuntime(e);
        }
    }

    /**
     * Invokes a getter method, supporting nested properties (e.g., "user.address.street").
     *
     * @param object The object.
     * @param name   The property name.
     * @return The result of the getter invocation.
     */
    public static Object invokeGetter(Object object, String name) {
        for (String method : StringKit.splitToArray(name, Symbol.DOT)) {
            String getterMethodName = Normal.GET + StringKit.capitalize(method);
            object = invoke(object, getterMethodName, new Class[] {}, new Object[] {});
        }
        return object;
    }

    /**
     * Invokes a setter method, supporting nested properties.
     *
     * @param object The object.
     * @param name   The property name.
     * @param value  The value to set.
     */
    public static void invokeSetter(Object object, String name, Object value) {
        String[] names = StringKit.splitToArray(name, Symbol.DOT);
        for (int i = 0; i < names.length; i++) {
            if (i < names.length - 1) {
                String getterMethodName = Normal.GET + StringKit.capitalize(names[i]);
                object = invoke(object, getterMethodName, new Class[] {}, new Object[] {});
            } else {
                String setterMethodName = Normal.SET + StringKit.capitalize(names[i]);
                invoke(object, setterMethodName, value);
            }
        }
    }

    /**
     * Prepares arguments for method invocation, handling nulls, primitive defaults, and type conversion.
     *
     * @param method The method.
     * @param args   The user-provided arguments.
     * @return The prepared arguments array.
     */
    public static Object[] actualArgs(final Method method, final Object[] args) {
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (1 == parameterTypes.length && parameterTypes[0].isArray()) {
            // Varargs, no conversion
            return args;
        }
        final Object[] actualArgs = new Object[parameterTypes.length];
        if (null != args) {
            for (int i = 0; i < actualArgs.length; i++) {
                if (i >= args.length || null == args[i]) {
                    actualArgs[i] = ClassKit.getDefaultValue(parameterTypes[i]);
                } else if (args[i] instanceof NullWrapper) {
                    actualArgs[i] = null;
                } else if (!parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                    final Object targetValue = Convert.convert(parameterTypes[i], args[i], args[i]);
                    if (null != targetValue) {
                        actualArgs[i] = targetValue;
                    }
                } else {
                    actualArgs[i] = args[i];
                }
            }
        }
        return actualArgs;
    }

    /**
     * Gets the {@link MethodType} for a given {@link Executable} (Method or Constructor).
     *
     * @param executable The method or constructor.
     * @return The {@link MethodType}.
     */
    public static MethodType methodType(final Executable executable) {
        return methodType(executable, null);
    }

    /**
     * Gets the {@link MethodType} for a given {@link Executable}.
     *
     * @param executable     The method or constructor.
     * @param declaringClass The declaring class.
     * @return The {@link MethodType}.
     */
    public static MethodType methodType(final Executable executable, Class<?> declaringClass) {
        if (null == declaringClass) {
            declaringClass = executable.getDeclaringClass();
        }
        if (executable instanceof Method method) {
            return MethodType.methodType(method.getReturnType(), declaringClass, method.getParameterTypes());
        } else {
            final Constructor<?> constructor = (Constructor<?>) executable;
            return MethodType.methodType(declaringClass, constructor.getParameterTypes());
        }
    }

}
