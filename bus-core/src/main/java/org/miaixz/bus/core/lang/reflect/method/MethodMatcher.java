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
package org.miaixz.bus.core.lang.reflect.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.resolve.AnnotatedElements;
import org.miaixz.bus.core.xyz.*;

/**
 * Utility class for creating method matchers based on various predefined conditions.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MethodMatcher {

    /**
     * A method matcher that combines multiple matchers, returning {@code true} only if all matchers fail to match.
     *
     * @param matchers An array of method predicates.
     * @return A method predicate that returns {@code true} if none of the provided matchers match.
     * @see Stream#noneMatch
     */
    @SafeVarargs
    public static Predicate<Method> noneMatch(final Predicate<Method>... matchers) {
        return PredicateKit.none(matchers);
    }

    /**
     * A method matcher that combines multiple matchers, returning {@code true} if any of the matchers succeed.
     *
     * @param matchers An array of method predicates.
     * @return A method predicate that returns {@code true} if any of the provided matchers match.
     * @see Stream#anyMatch
     */
    @SafeVarargs
    public static Predicate<Method> anyMatch(final Predicate<Method>... matchers) {
        return PredicateKit.or(matchers);
    }

    /**
     * A method matcher that combines multiple matchers, returning {@code true} only if all matchers succeed.
     *
     * @param matchers An array of method predicates.
     * @return A method predicate that returns {@code true} if all of the provided matchers match.
     * @see Stream#allMatch
     */
    @SafeVarargs
    public static Predicate<Method> allMatch(final Predicate<Method>... matchers) {
        return PredicateKit.and(matchers);
    }

    /**
     * A method matcher that matches public methods.
     *
     * @return A method predicate that returns {@code true} for public methods.
     */
    public static Predicate<Method> isPublic() {
        return forModifiers(Modifier.PUBLIC);
    }

    /**
     * A method matcher that matches static methods.
     *
     * @return A method predicate that returns {@code true} for static methods.
     */
    public static Predicate<Method> isStatic() {
        return forModifiers(Modifier.STATIC);
    }

    /**
     * A method matcher that matches public static methods.
     *
     * @return A method predicate that returns {@code true} for public static methods.
     */
    public static Predicate<Method> isPublicStatic() {
        return forModifiers(Modifier.PUBLIC, Modifier.STATIC);
    }

    /**
     * A method matcher that matches methods with the specified modifiers.
     *
     * @param modifiers An array of modifiers to match (e.g., {@code Modifier.PUBLIC}, {@code Modifier.STATIC}).
     * @return A method predicate that returns {@code true} for methods having all specified modifiers.
     */
    public static Predicate<Method> forModifiers(final int... modifiers) {
        return method -> ModifierKit.hasAll(method.getModifiers(), modifiers);
    }

    /**
     * A method matcher that matches methods directly annotated with the specified annotation type.
     *
     * @param annotationType The annotation type to search for.
     * @return A method predicate that returns {@code true} for methods directly annotated with the specified type.
     */
    public static Predicate<Method> hasDeclaredAnnotation(final Class<? extends Annotation> annotationType) {
        return method -> method.isAnnotationPresent(annotationType);
    }

    /**
     * A method matcher that matches methods annotated with the specified annotation type, or with an annotation that is
     * meta-annotated with the specified annotation type. For example, if the specified annotation is
     * {@code @Annotation}, it matches:
     * <ul>
     * <li>Methods directly annotated with {@code @Annotation}.</li>
     * <li>Methods annotated with a derived annotation that is itself annotated with {@code @Annotation}.</li>
     * </ul>
     *
     * @param annotationType The annotation type to search for.
     * @return A method predicate that returns {@code true} for methods having the specified annotation.
     * @see AnnotatedElements#isAnnotationPresent
     */
    public static Predicate<Method> hasAnnotation(final Class<? extends Annotation> annotationType) {
        return method -> AnnotatedElements.isAnnotationPresent(method, annotationType);
    }

    /**
     * A method matcher that matches methods whose declaring class is annotated with the specified annotation type, or
     * with an annotation that is meta-annotated with the specified annotation type. For example, if the specified
     * annotation is {@code @Annotation}, it matches:
     * <ul>
     * <li>Methods whose declaring class is directly annotated with {@code @Annotation}.</li>
     * <li>Methods whose declaring class is annotated with a derived annotation that is itself annotated with
     * {@code @Annotation}.</li>
     * </ul>
     *
     * @param annotationType The annotation type to search for on the declaring class.
     * @return A method predicate that returns {@code true} for methods whose declaring class has the specified
     *         annotation.
     * @see AnnotatedElements#isAnnotationPresent
     */
    public static Predicate<Method> hasAnnotationOnDeclaringClass(final Class<? extends Annotation> annotationType) {
        return method -> AnnotatedElements.isAnnotationPresent(method.getDeclaringClass(), annotationType);
    }

    /**
     * A method matcher that matches methods annotated with the specified annotation type (or meta-annotated), or whose
     * declaring class is annotated with the specified annotation type (or meta-annotated). For example, if the
     * specified annotation is {@code @Annotation}, it matches:
     * <ul>
     * <li>Methods directly annotated with {@code @Annotation}.</li>
     * <li>Methods annotated with a derived annotation that is itself annotated with {@code @Annotation}.</li>
     * <li>Methods whose declaring class is directly annotated with {@code @Annotation}.</li>
     * <li>Methods whose declaring class is annotated with a derived annotation that is itself annotated with
     * {@code @Annotation}.</li>
     * </ul>
     *
     * @param annotationType The annotation type to search for on the method or its declaring class.
     * @return A method predicate that returns {@code true} if the method or its declaring class has the specified
     *         annotation.
     */
    public static Predicate<Method> hasAnnotationOnMethodOrDeclaringClass(
            final Class<? extends Annotation> annotationType) {
        return method -> AnnotatedElements.isAnnotationPresent(method, annotationType)
                || AnnotatedElements.isAnnotationPresent(method.getDeclaringClass(), annotationType);
    }

    /**
     * A method matcher for finding getter methods of a specified field.
     * <ul>
     * <li>Looks for parameterless methods named {@code getXxx} where {@code Xxx} is the capitalized field name.</li>
     * <li>Looks for parameterless methods named after the field name.</li>
     * <li>If {@code fieldType} is {@code boolean} or {@code Boolean}, it also looks for parameterless methods named
     * {@code isXxx}.</li>
     * </ul>
     *
     * @param fieldName The name of the field. Must not be {@code null}.
     * @param fieldType The type of the field. Must not be {@code null}.
     * @return A method predicate that returns {@code true} for getter methods of the specified field.
     * @throws NullPointerException if {@code fieldName} or {@code fieldType} is {@code null}.
     */
    public static Predicate<Method> forGetterMethod(final String fieldName, final Class<?> fieldType) {
        Objects.requireNonNull(fieldName);
        Objects.requireNonNull(fieldType);
        // Match methods named get + capitalized field name
        Predicate<Method> nameMatcher = forName(StringKit.upperFirstAndAddPre(fieldName, Normal.GET));
        // Also match methods named after the field name
        nameMatcher = nameMatcher.or(forName(fieldName));
        if (Objects.equals(boolean.class, fieldType) || Objects.equals(Boolean.class, fieldType)) {
            // For boolean fields, also match methods named is + capitalized field name
            nameMatcher = nameMatcher.or(forName(StringKit.upperFirstAndAddPre(fieldName, Normal.IS)));
        }
        return allMatch(nameMatcher, forReturnType(fieldType), forNoneParameter());
    }

    /**
     * A method matcher for finding getter methods of a specified field.
     * <ul>
     * <li>Looks for parameterless methods named {@code getXxx} where {@code Xxx} is the capitalized field name.</li>
     * <li>Looks for parameterless methods named after the field name.</li>
     * <li>If the field's type is {@code boolean} or {@code Boolean}, it also looks for parameterless methods named
     * {@code isXxx}.</li>
     * </ul>
     *
     * @param field The field for which to find the getter method. Must not be {@code null}.
     * @return A method predicate that returns {@code true} for getter methods of the specified field.
     * @throws NullPointerException if {@code field} is {@code null}.
     */
    public static Predicate<Method> forGetterMethod(final Field field) {
        Objects.requireNonNull(field);
        return forGetterMethod(field.getName(), field.getType());
    }

    /**
     * A method matcher for finding setter methods of a specified field. By default, it looks for single-parameter
     * methods named {@code setXxx} where {@code Xxx} is the capitalized field name.
     * <ul>
     * <li>Looks for single-parameter methods named {@code setXxx} where {@code Xxx} is the capitalized field name.</li>
     * <li>Looks for single-parameter methods named after the field name.</li>
     * </ul>
     *
     * @param fieldName The name of the field. Must not be {@code null}.
     * @param fieldType The type of the field. Must not be {@code null}.
     * @return A method predicate that returns {@code true} for setter methods of the specified field.
     * @throws NullPointerException if {@code fieldName} or {@code fieldType} is {@code null}.
     */
    public static Predicate<Method> forSetterMethod(final String fieldName, final Class<?> fieldType) {
        Objects.requireNonNull(fieldName);
        Objects.requireNonNull(fieldType);
        final Predicate<Method> nameMatcher = forName(StringKit.upperFirstAndAddPre(fieldName, Normal.SET))
                .or(forName(fieldName));
        return allMatch(nameMatcher, forParameterTypes(fieldType));
    }

    /**
     * A method matcher for finding setter methods of a specified field. By default, it looks for single-parameter
     * methods named {@code setXxx} where {@code Xxx} is the capitalized field name.
     * <ul>
     * <li>Looks for single-parameter methods named {@code setXxx} where {@code Xxx} is the capitalized field name.</li>
     * <li>Looks for single-parameter methods named after the field name.</li>
     * </ul>
     *
     * @param field The field for which to find the setter method. Must not be {@code null}.
     * @return A method predicate that returns {@code true} for setter methods of the specified field.
     * @throws NullPointerException if {@code field} is {@code null}.
     */
    public static Predicate<Method> forSetterMethod(final Field field) {
        Objects.requireNonNull(field);
        return forSetterMethod(field.getName(), field.getType());
    }

    /**
     * A method matcher that matches both method name and parameter types. Parameter type matching allows the argument
     * type to be a subclass of the method's parameter type.
     *
     * @param methodName     The name of the method. Must not be {@code null}.
     * @param parameterTypes An array of parameter types. Must not be {@code null}.
     * @return A method predicate that returns {@code true} for methods matching the name and assignable parameter
     *         types.
     * @throws NullPointerException if {@code methodName} or {@code parameterTypes} is {@code null}.
     */
    public static Predicate<Method> forNameAndParameterTypes(
            final String methodName,
            final Class<?>... parameterTypes) {
        Objects.requireNonNull(methodName);
        Objects.requireNonNull(parameterTypes);
        return allMatch(forName(methodName), forParameterTypes(parameterTypes));
    }

    /**
     * A method matcher that matches both method name and parameter types. Parameter type matching requires the argument
     * types to exactly match the method's parameter types.
     *
     * @param methodName     The name of the method. Must not be {@code null}.
     * @param parameterTypes An array of parameter types. Must not be {@code null}.
     * @return A method predicate that returns {@code true} for methods matching the name and strictly matching
     *         parameter types.
     * @throws NullPointerException if {@code methodName} or {@code parameterTypes} is {@code null}.
     */
    public static Predicate<Method> forNameAndStrictParameterTypes(
            final String methodName,
            final Class<?>... parameterTypes) {
        Objects.requireNonNull(methodName);
        Objects.requireNonNull(parameterTypes);
        return allMatch(forName(methodName), forStrictParameterTypes(parameterTypes));
    }

    /**
     * A method matcher that matches both method name (case-insensitive) and parameter types. Parameter type matching
     * allows the argument type to be a subclass of the method's parameter type.
     *
     * @param methodName     The name of the method. Must not be {@code null}.
     * @param parameterTypes An array of parameter types. Must not be {@code null}.
     * @return A method predicate that returns {@code true} for methods matching the name (case-insensitive) and
     *         assignable parameter types.
     * @throws NullPointerException if {@code methodName} or {@code parameterTypes} is {@code null}.
     */
    public static Predicate<Method> forNameIgnoreCaseAndParameterTypes(
            final String methodName,
            final Class<?>... parameterTypes) {
        Objects.requireNonNull(methodName);
        Objects.requireNonNull(parameterTypes);
        return allMatch(forNameIgnoreCase(methodName), forParameterTypes(parameterTypes));
    }

    /**
     * A method matcher that matches both method name (case-insensitive) and parameter types. Parameter type matching
     * requires the argument types to exactly match the method's parameter types.
     *
     * @param methodName     The name of the method. Must not be {@code null}.
     * @param parameterTypes An array of parameter types. Must not be {@code null}.
     * @return A method predicate that returns {@code true} for methods matching the name (case-insensitive) and
     *         strictly matching parameter types.
     * @throws NullPointerException if {@code methodName} or {@code parameterTypes} is {@code null}.
     */
    public static Predicate<Method> forNameIgnoreCaseAndStrictParameterTypes(
            final String methodName,
            final Class<?>... parameterTypes) {
        Objects.requireNonNull(methodName);
        Objects.requireNonNull(parameterTypes);
        return allMatch(forNameIgnoreCase(methodName), forStrictParameterTypes(parameterTypes));
    }

    /**
     * A method matcher that matches a method's signature, including:
     * <ul>
     * <li>Exact method name match.</li>
     * <li>Return type matching, allowing the actual return type to be a subclass of the expected return type.</li>
     * <li>Parameter type matching, allowing the actual parameter types to be subclasses of the expected parameter
     * types.</li>
     * </ul>
     *
     * @param method The method whose signature to match against. Must not be {@code null}.
     * @return A method predicate that returns {@code true} for methods matching the given method's signature.
     * @throws NullPointerException if {@code method} is {@code null}.
     */
    public static Predicate<Method> forMethodSignature(final Method method) {
        Objects.requireNonNull(method);
        return forMethodSignature(method.getName(), method.getReturnType(), method.getParameterTypes());
    }

    /**
     * A method matcher that matches a method's signature, including:
     * <ul>
     * <li>Exact method name match.</li>
     * <li>Return type matching, allowing the actual return type to be a subclass of the expected return type. If
     * {@code returnType} is {@code null}, it matches methods with a {@code void} return type.</li>
     * <li>Parameter type matching, allowing the actual parameter types to be subclasses of the expected parameter
     * types. If {@code parameterTypes} is {@code null}, it matches methods with no parameters.</li>
     * </ul>
     *
     * @param methodName     The name of the method. Must not be {@code null}.
     * @param returnType     The expected return type. If {@code null}, matches methods with {@code void} return type.
     * @param parameterTypes An array of expected parameter types. If {@code null}, matches methods with no parameters.
     * @return A method predicate that returns {@code true} for methods matching the given signature.
     * @throws NullPointerException if {@code methodName} is {@code null}.
     */
    public static Predicate<Method> forMethodSignature(
            final String methodName,
            final Class<?> returnType,
            final Class<?>... parameterTypes) {
        Objects.requireNonNull(methodName);
        final Predicate<Method> resultMatcher = Objects.isNull(returnType) ? forNoneReturnType()
                : forReturnType(returnType);
        final Predicate<Method> parameterMatcher = Objects.isNull(parameterTypes) ? forNoneParameter()
                : forParameterTypes(parameterTypes);
        return allMatch(forName(methodName), resultMatcher, parameterMatcher);
    }

    /**
     * A method matcher that strictly matches a method's signature, including:
     * <ul>
     * <li>Exact method name match.</li>
     * <li>Strict return type matching, requiring the actual return type to exactly match the expected return type. If
     * {@code returnType} is {@code null}, it matches methods with a {@code void} return type.</li>
     * <li>Strict parameter type matching, requiring the actual parameter types to exactly match the expected parameter
     * types. If {@code parameterTypes} is {@code null}, it matches methods with no parameters.</li>
     * </ul>
     *
     * @param methodName     The name of the method. Must not be {@code null}.
     * @param returnType     The expected return type. If {@code null}, matches methods with {@code void} return type.
     * @param parameterTypes An array of expected parameter types. If {@code null}, matches methods with no parameters.
     * @return A method predicate that returns {@code true} for methods strictly matching the given signature.
     * @throws NullPointerException if {@code methodName} is {@code null}.
     */
    public static Predicate<Method> forStrictMethodSignature(
            final String methodName,
            final Class<?> returnType,
            final Class<?>... parameterTypes) {
        Objects.requireNonNull(methodName);
        final Predicate<Method> resultMatcher = Objects.isNull(returnType) ? forNoneReturnType()
                : forReturnType(returnType);
        final Predicate<Method> parameterMatcher = Objects.isNull(parameterTypes) ? forNoneParameter()
                : forStrictParameterTypes(parameterTypes);
        return allMatch(forName(methodName), resultMatcher, parameterMatcher);
    }

    /**
     * A method matcher that strictly matches a method's signature, including:
     * <ul>
     * <li>Exact method name match.</li>
     * <li>Strict return type matching, requiring the actual return type to exactly match the expected return type.</li>
     * <li>Strict parameter type matching, requiring the actual parameter types to exactly match the expected parameter
     * types.</li>
     * </ul>
     *
     * @param method The method whose signature to strictly match against. Must not be {@code null}.
     * @return A method predicate that returns {@code true} for methods strictly matching the given method's signature.
     * @throws NullPointerException if {@code method} is {@code null}.
     */
    public static Predicate<Method> forStrictMethodSignature(final Method method) {
        Objects.requireNonNull(method);
        return forMethodSignature(method.getName(), method.getReturnType(), method.getParameterTypes());
    }

    /**
     * A method matcher that matches methods by their exact name.
     *
     * @param methodName The exact name of the method to match. Must not be {@code null}.
     * @return A method predicate that returns {@code true} for methods with the specified name.
     * @throws NullPointerException if {@code methodName} is {@code null}.
     */
    public static Predicate<Method> forName(final String methodName) {
        return method -> Objects.equals(method.getName(), methodName);
    }

    /**
     * A method matcher that matches methods by their name, ignoring case.
     *
     * @param methodName The name of the method to match (case-insensitive). Must not be {@code null}.
     * @return A method predicate that returns {@code true} for methods with the specified name (case-insensitive).
     * @throws NullPointerException if {@code methodName} is {@code null}.
     */
    public static Predicate<Method> forNameIgnoreCase(final String methodName) {
        return method -> StringKit.endWithIgnoreCase(method.getName(), methodName);
    }

    /**
     * A method matcher that matches methods with a {@code void} return type.
     *
     * @return A method predicate that returns {@code true} for methods with a {@code void} return type.
     */
    public static Predicate<Method> forNoneReturnType() {
        return method -> Objects.equals(method.getReturnType(), Void.TYPE);
    }

    /**
     * A method matcher that matches methods based on their return type. It returns {@code true} if the method's return
     * type is assignable from the specified {@code returnType}.
     *
     * @param returnType The expected return type.
     * @return A method predicate that returns {@code true} for methods whose return type is assignable from the
     *         specified type.
     */
    public static Predicate<Method> forReturnType(final Class<?> returnType) {
        return method -> ClassKit.isAssignable(returnType, method.getReturnType());
    }

    /**
     * A method matcher that strictly matches methods based on their return type. It returns {@code true} only if the
     * method's return type exactly matches the specified {@code returnType}.
     *
     * @param returnType The expected return type.
     * @return A method predicate that returns {@code true} for methods whose return type exactly matches the specified
     *         type.
     */
    public static Predicate<Method> forStrictReturnType(final Class<?> returnType) {
        return method -> Objects.equals(method.getReturnType(), returnType);
    }

    /**
     * A method matcher that matches methods with no parameters.
     *
     * @return A method predicate that returns {@code true} for methods with no parameters.
     */
    public static Predicate<Method> forNoneParameter() {
        return method -> method.getParameterCount() == 0;
    }

    /**
     * A method matcher that matches methods with a specific number of parameters.
     *
     * @param count The expected number of parameters.
     * @return A method predicate that returns {@code true} for methods with the specified parameter count.
     */
    public static Predicate<Method> forParameterCount(final int count) {
        return method -> method.getParameterCount() == count;
    }

    /**
     * A method matcher that matches methods based on their parameter types. It returns {@code true} if all specified
     * {@code parameterTypes} are assignable to the method's actual parameter types. For example, if
     * {@code parameterTypes} contains {@code ArrayList.class}, it matches methods where the corresponding parameter
     * type is {@code List.class}, {@code Collection.class}, etc.
     *
     * @param parameterTypes An array of parameter types to match. Must not be {@code null}.
     * @return A method predicate that returns {@code true} for methods whose parameter types are assignable from the
     *         specified types.
     * @throws NullPointerException if {@code parameterTypes} is {@code null}.
     */
    public static Predicate<Method> forParameterTypes(final Class<?>... parameterTypes) {
        Objects.requireNonNull(parameterTypes);
        return method -> ClassKit.isAllAssignableFrom(parameterTypes, method.getParameterTypes());
    }

    /**
     * A method matcher that finds the most specific method based on parameter types. This matcher is more flexible than
     * {@link #forParameterTypes} and allows for partial matching.
     * <ul>
     * <li>If {@code parameterTypes} is empty, it matches methods with no parameters.</li>
     * <li>If {@code parameterTypes} is not empty:
     * <ul>
     * <li>Only non-{@code null} parameter types in {@code parameterTypes} are matched. A {@code null} entry in
     * {@code parameterTypes} means it matches any type for that position.</li>
     * <li>If N is the length of {@code parameterTypes}, it only requires that the non-{@code null} types in
     * {@code parameterTypes} match the first N parameter types of the method.</li>
     * <li>If the length of {@code parameterTypes} is greater than the method's parameter list length, it returns
     * {@code false}.</li>
     * </ul>
     * </li>
     * </ul>
     * For example, for a method {@code method(String, Integer, Object)}, the following matches are supported:
     * <ul>
     * <li>{@code forMostSpecificParameterTypes(CharSequence.class, Number.class, Object.class)}</li>
     * <li>{@code forMostSpecificParameterTypes(String.class, Integer.class, Object.class)}</li>
     * <li>{@code forMostSpecificParameterTypes(String.class, Integer.class, null)}</li>
     * <li>{@code forMostSpecificParameterTypes(String.class, null, null)}</li>
     * <li>{@code forMostSpecificParameterTypes(null, null, null)}</li>
     * <li>{@code forMostSpecificParameterTypes(String.class, Integer.class)}</li>
     * <li>{@code forMostSpecificParameterTypes(String.class)}</li>
     * </ul>
     *
     * @param parameterTypes An array of parameter types to match. Can contain {@code null} for any type.
     * @return A method predicate that returns {@code true} for the most specific matching methods.
     */
    public static Predicate<Method> forMostSpecificParameterTypes(final Class<?>... parameterTypes) {
        return mostSpecificStrictParameterTypesMatcher(parameterTypes, ClassKit::isAssignable);
    }

    /**
     * A method matcher that finds the most specific method based on parameter types, requiring strict type equality.
     * This matcher is more flexible than {@link #forStrictParameterTypes} and allows for partial matching.
     * <ul>
     * <li>If {@code parameterTypes} is empty, it matches methods with no parameters.</li>
     * <li>If {@code parameterTypes} is not empty:
     * <ul>
     * <li>Only non-{@code null} parameter types in {@code parameterTypes} are matched. A {@code null} entry in
     * {@code parameterTypes} means it matches any type for that position.</li>
     * <li>If N is the length of {@code parameterTypes}, it only requires that the non-{@code null} types in
     * {@code parameterTypes} strictly match the first N parameter types of the method.</li>
     * <li>If the length of {@code parameterTypes} is greater than the method's parameter list length, it returns
     * {@code false}.</li>
     * </ul>
     * </li>
     * </ul>
     * For example, for a method {@code method(String, Integer, Object)}, the following matches are supported:
     * <ul>
     * <li>{@code forMostSpecificStrictParameterTypes(String.class, Integer.class, Object.class)}</li>
     * <li>{@code forMostSpecificStrictParameterTypes(String.class, Integer.class, null)}</li>
     * <li>{@code forMostSpecificStrictParameterTypes(String.class, null, null)}</li>
     * <li>{@code forMostSpecificStrictParameterTypes(null, null, null)}</li>
     * <li>{@code forMostSpecificStrictParameterTypes(String.class, Integer.class)}</li>
     * <li>{@code forMostSpecificStrictParameterTypes(String.class)}</li>
     * </ul>
     *
     * @param parameterTypes An array of parameter types to match. Can contain {@code null} for any type.
     * @return A method predicate that returns {@code true} for the most specific strictly matching methods.
     */
    public static Predicate<Method> forMostSpecificStrictParameterTypes(final Class<?>... parameterTypes) {
        return mostSpecificStrictParameterTypesMatcher(parameterTypes, Objects::equals);
    }

    /**
     * A method matcher that strictly matches methods based on their parameter types. It returns {@code true} only if
     * the method's parameter types exactly match the specified {@code parameterTypes}.
     *
     * @param parameterTypes An array of parameter types to match. Must not be {@code null}.
     * @return A method predicate that returns {@code true} for methods whose parameter types exactly match the
     *         specified types.
     * @throws NullPointerException if {@code parameterTypes} is {@code null}.
     */
    public static Predicate<Method> forStrictParameterTypes(final Class<?>... parameterTypes) {
        Objects.requireNonNull(parameterTypes);
        return method -> ArrayKit.equals(method.getParameterTypes(), parameterTypes);
    }

    /**
     * Internal helper method for creating most specific parameter type matchers.
     *
     * @param parameterTypes The array of parameter types to match against.
     * @param typeMatcher    A {@link BiPredicate} to compare individual parameter types (e.g.,
     *                       {@code ClassKit::isAssignable} or {@code Objects::equals}).
     * @return A method predicate.
     */
    private static Predicate<Method> mostSpecificStrictParameterTypesMatcher(
            final Class<?>[] parameterTypes,
            final BiPredicate<Class<?>, Class<?>> typeMatcher) {
        Objects.requireNonNull(parameterTypes);
        // If parameters are empty, match methods with no parameters.
        if (parameterTypes.length == 0) {
            return forNoneParameter();
        }
        // If parameters are not empty, match methods with specified parameter types.
        return method -> {
            final Class<?>[] methodParameterTypes = method.getParameterTypes();
            if (parameterTypes.length > methodParameterTypes.length) {
                return false;
            }
            for (int i = 0; i < parameterTypes.length; i++) {
                final Class<?> parameterType = parameterTypes[i];
                // If the parameter type is null, it means match any type for this position.
                if (Objects.isNull(parameterType)) {
                    continue;
                }
                // If the parameter type is not null, require it to be assignable to the method's parameter type.
                if (typeMatcher.negate().test(parameterType, methodParameterTypes[i])) {
                    return false;
                }
            }
            return true;
        };
    }

}
