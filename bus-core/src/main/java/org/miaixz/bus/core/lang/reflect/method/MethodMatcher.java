/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.resolve.AnnotatedElements;
import org.miaixz.bus.core.xyz.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 方法匹配器工具类，用于基于各种预设条件创建方法匹配器。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MethodMatcher {

    /**
     * 用于组合多个方法匹配器的方法匹配器，仅当所有方法匹配器均匹配失败时才认为方法匹配。
     *
     * @param matchers 方法匹配器
     * @return 方法匹配器
     * @see Stream#noneMatch
     */
    @SafeVarargs
    public static Predicate<Method> noneMatch(final Predicate<Method>... matchers) {
        return PredicateKit.none(matchers);
    }

    /**
     * 用于组合多个方法匹配器的方法匹配器，当任意方法匹配器匹配成功时即认为方法匹配。
     *
     * @param matchers 方法匹配器
     * @return 方法匹配器
     * @see Stream#anyMatch
     */
    @SafeVarargs
    public static Predicate<Method> anyMatch(final Predicate<Method>... matchers) {
        return PredicateKit.or(matchers);
    }

    /**
     * 用于组合多个方法匹配器的方法匹配器，当所有方法匹配器均匹配成功时才认为方法匹配。
     *
     * @param matchers 方法匹配器
     * @return 方法匹配器
     * @see Stream#allMatch
     */
    @SafeVarargs
    public static Predicate<Method> allMatch(final Predicate<Method>... matchers) {
        return PredicateKit.and(matchers);
    }

    /**
     * 用于匹配共有方法的方法匹配器。
     *
     * @return 方法匹配器
     */
    public static Predicate<Method> isPublic() {
        return forModifiers(Modifier.PUBLIC);
    }

    /**
     * 用于匹配静态方法的方法匹配器。
     *
     * @return 方法匹配器
     */
    public static Predicate<Method> isStatic() {
        return forModifiers(Modifier.STATIC);
    }

    /**
     * <p>用于匹配公共静态方法的方法匹配器。
     *
     * @return 方法匹配器
     */
    public static Predicate<Method> isPublicStatic() {
        return forModifiers(Modifier.PUBLIC, Modifier.STATIC);
    }

    /**
     * 用于具有指定修饰符的方法的方法匹配器。
     *
     * @param modifiers 修饰符
     * @return 方法匹配器
     */
    public static Predicate<Method> forModifiers(final int... modifiers) {
        return method -> ModifierKit.hasAllModifier(method.getModifiers(), modifiers);
    }

    /**
     * 用于匹配被指定注解标注、或注解层级结构中存在指定注解的方法的方法匹配器。
     * 比如：指定注解为 {@code @Annotation}，则匹配直接被{@code @Annotation}标注的方法。
     *
     * @param annotationType 注解类型
     * @return 方法匹配器
     * @see AnnotatedElements#isAnnotationPresent
     */
    public static Predicate<Method> hasDeclaredAnnotation(final Class<? extends Annotation> annotationType) {
        return method -> method.isAnnotationPresent(annotationType);
    }

    /**
     * 用于匹配被指定注解标注、或注解层级结构中存在指定注解的方法的方法匹配器。
     * 比如：指定注解为 {@code @Annotation}，则匹配:
     * <ul>
     *     <li>被{@code @Annotation}标注的方法；</li>
     *     <li>被带有{@code @Annotation}注解的派生注解标注的方法；</li>
     * </ul>
     *
     * @param annotationType 注解类型
     * @return 方法匹配器
     * @see AnnotatedElements#isAnnotationPresent
     */
    public static Predicate<Method> hasAnnotation(final Class<? extends Annotation> annotationType) {
        return method -> AnnotatedElements.isAnnotationPresent(method, annotationType);
    }

    /**
     * 用于匹配声明方法的类的层级接口中，存在任意类被指定注解标注、或注解层级结构中存在指定注解的方法的方法匹配器。
     * 比如：指定注解为 {@code @Annotation}，则匹配:
     * <ul>
     *     <li>声明方法的类被{@code @Annotation}标注的方法；</li>
     *     <li>声明方法的类被带有{@code @Annotation}注解的派生注解标注的方法；</li>
     * </ul>
     *
     * @param annotationType 注解类型
     * @return 方法匹配器
     * @see AnnotatedElements#isAnnotationPresent
     */
    public static Predicate<Method> hasAnnotationOnDeclaringClass(final Class<? extends Annotation> annotationType) {
        return method -> AnnotatedElements.isAnnotationPresent(method.getDeclaringClass(), annotationType);
    }

    /**
     * 用于匹配方法本身或声明方法的类上，直接被指定注解标注、或注解层级结构中存在指定注解的方法的方法匹配器。
     * 比如：指定注解为 {@code @Annotation}，则匹配:
     * <ul>
     *     <li>被{@code @Annotation}标注的方法；</li>
     *     <li>被带有{@code @Annotation}注解的派生注解标注的方法；</li>
     *     <li>声明方法的类被{@code @Annotation}标注的方法；</li>
     *     <li>声明方法的类被带有{@code @Annotation}注解的派生注解标注的方法；</li>
     * </ul>
     *
     * @param annotationType 注解类型
     * @return 方法匹配器
     */
    public static Predicate<Method> hasAnnotationOnMethodOrDeclaringClass(final Class<? extends Annotation> annotationType) {
        return method -> AnnotatedElements.isAnnotationPresent(method, annotationType)
                || AnnotatedElements.isAnnotationPresent(method.getDeclaringClass(), annotationType);
    }

    /**
     * 用于获得指定属性的getter方法的匹配器
     * <ul>
     *     <li>查找方法名为{@code get + 首字母大写的属性名}的无参数方法；</li>
     *     <li>查找方法名为属性名的无参数方法；</li>
     *     <li>若{@code fieldType}为{@code boolean}或{@code Boolean}，则同时查找方法名为{@code is + 首字母大写的属性}的无参数方法;</li>
     * </ul>
     *
     * @param fieldName 属性名
     * @param fieldType 属性类型
     * @return 方法匹配器
     */
    public static Predicate<Method> forGetterMethod(final String fieldName, final Class<?> fieldType) {
        Objects.requireNonNull(fieldName);
        Objects.requireNonNull(fieldType);
        // 匹配方法名为 get + 首字母大写的属性名的无参数方法
        Predicate<Method> nameMatcher = forName(StringKit.upperFirstAndAddPre(fieldName, Normal.GET));
        // 查找方法名为属性名的无参数方法
        nameMatcher = nameMatcher.or(forName(fieldName));
        if (Objects.equals(boolean.class, fieldType) || Objects.equals(Boolean.class, fieldType)) {
            // 匹配方法名为 get + 首字母大写的属性名的无参数方法
            nameMatcher = nameMatcher.or(forName(StringKit.upperFirstAndAddPre(fieldName, Normal.IS)));
        }
        return allMatch(nameMatcher, forReturnType(fieldType), forNoneParameter());
    }

    /**
     * 用于获得指定属性的getter方法的匹配器
     * <ul>
     *     <li>查找方法名为{@code get + 首字母大写的属性名}的无参数方法；</li>
     *     <li>查找方法名为属性名的无参数方法；</li>
     *     <li>若{@code fieldType}为{@code boolean}或{@code Boolean}，则同时查找方法名为{@code is + 首字母大写的属性}的无参数方法;</li>
     * </ul>
     *
     * @param field 属性
     * @return 方法匹配器
     */
    public static Predicate<Method> forGetterMethod(final Field field) {
        Objects.requireNonNull(field);
        return forGetterMethod(field.getName(), field.getType());
    }

    /**
     * 用于获得指定属性的setter方法的匹配器，默认查找方法名为{@code set + 首字母大写的属性}的单参数方法。
     * <ul>
     *     <li>查找方法名为{@code set + 首字母大写的属性名}的单参数方法；</li>
     *     <li>查找方法名为属性名的单参数方法；</li>
     * </ul>
     *
     * @param fieldName 属性名
     * @param fieldType 属性类型
     * @return 方法匹配器
     */
    public static Predicate<Method> forSetterMethod(final String fieldName, final Class<?> fieldType) {
        Objects.requireNonNull(fieldName);
        Objects.requireNonNull(fieldType);
        final Predicate<Method> nameMatcher = forName(StringKit.upperFirstAndAddPre(fieldName, Normal.SET))
                .or(forName(fieldName));
        return allMatch(nameMatcher, forParameterTypes(fieldType));
    }

    /**
     * 用于获得指定属性的setter方法的匹配器，默认查找方法名为{@code set + 首字母大写的属性}的单参数方法。
     * <ul>
     *     <li>查找方法名为{@code set + 首字母大写的属性名}的单参数方法；</li>
     *     <li>查找方法名为属性名的单参数方法；</li>
     * </ul>
     *
     * @param field 属性
     * @return 方法匹配器
     */
    public static Predicate<Method> forSetterMethod(final Field field) {
        Objects.requireNonNull(field);
        return forSetterMethod(field.getName(), field.getType());
    }

    /**
     * 用于同时匹配方法名和参数类型的方法匹配器，其中，参数类型匹配时允许参数类型为方法参数类型的子类。
     *
     * @param methodName     方法名
     * @param parameterTypes 参数类型
     * @return 方法匹配器
     */
    public static Predicate<Method> forNameAndParameterTypes(final String methodName, final Class<?>... parameterTypes) {
        Objects.requireNonNull(methodName);
        Objects.requireNonNull(parameterTypes);
        return allMatch(forName(methodName), forParameterTypes(parameterTypes));
    }

    /**
     * 用于同时匹配方法名和参数类型的方法匹配器，其中，参数类型匹配时要求参数类型与方法参数类型完全一致。
     *
     * @param methodName     方法名
     * @param parameterTypes 参数类型
     * @return 方法匹配器
     */
    public static Predicate<Method> forNameAndStrictParameterTypes(final String methodName, final Class<?>... parameterTypes) {
        Objects.requireNonNull(methodName);
        Objects.requireNonNull(parameterTypes);
        return allMatch(forName(methodName), forStrictParameterTypes(parameterTypes));
    }

    /**
     * 用于同时匹配方法名和参数类型的方法匹配器，其中，参数类型匹配时允许参数类型为方法参数类型的子类，且方法名忽略大小写。
     *
     * @param methodName     方法名
     * @param parameterTypes 参数类型
     * @return 方法匹配器
     */
    public static Predicate<Method> forNameIgnoreCaseAndParameterTypes(
            final String methodName, final Class<?>... parameterTypes) {
        Objects.requireNonNull(methodName);
        Objects.requireNonNull(parameterTypes);
        return allMatch(forNameIgnoreCase(methodName), forParameterTypes(parameterTypes));
    }

    /**
     * 用于同时匹配方法名和参数类型的方法匹配器，其中，参数类型匹配时要求参数类型与方法参数类型完全一致，且方法名忽略大小写。
     *
     * @param methodName     方法名
     * @param parameterTypes 参数类型
     * @return 方法匹配器
     */
    public static Predicate<Method> forNameIgnoreCaseAndStrictParameterTypes(
            final String methodName, final Class<?>... parameterTypes) {
        Objects.requireNonNull(methodName);
        Objects.requireNonNull(parameterTypes);
        return allMatch(forNameIgnoreCase(methodName), forStrictParameterTypes(parameterTypes));
    }

    /**
     * 用于匹配方法签名的方法匹配器，检查的内容包括：
     * <ul>
     *     <li>方法名是否完全一致；</li>
     *     <li>返回值类型是否匹配，允许返回值类型为方法返回值类型的子类；</li>
     *     <li>参数类型是否匹配，允许参数类型为方法参数类型的子类；</li>
     * </ul>
     *
     * @param method 方法
     * @return 方法匹配器
     */
    public static Predicate<Method> forMethodSignature(final Method method) {
        Objects.requireNonNull(method);
        return forMethodSignature(method.getName(), method.getReturnType(), method.getParameterTypes());
    }

    /**
     * 用于匹配方法签名的方法匹配器，检查的内容包括：
     * <ul>
     *     <li>方法名是否完全一致；</li>
     *     <li>返回值类型是否匹配，允许返回值类型为方法返回值类型的子类，若返回值类型为{@code null}则表示匹配无返回值的方法；</li>
     *     <li>参数类型是否匹配，允许参数类型为方法参数类型的子类，若参数类型为{@code null}则表示匹配无参数的方法；</li>
     * </ul>
     *
     * @param methodName     方法名
     * @param returnType     返回值类型，若为{@code null}则表示匹配无返回值的方法
     * @param parameterTypes 参数类型，若为{@code null}则表示匹配无参数的方法
     * @return 方法匹配器
     */
    public static Predicate<Method> forMethodSignature(
            final String methodName, final Class<?> returnType, final Class<?>... parameterTypes) {
        Objects.requireNonNull(methodName);
        final Predicate<Method> resultMatcher = Objects.isNull(returnType) ?
                forNoneReturnType() : forReturnType(returnType);
        final Predicate<Method> parameterMatcher = Objects.isNull(parameterTypes) ?
                forNoneParameter() : forParameterTypes(parameterTypes);
        return allMatch(forName(methodName), resultMatcher, parameterMatcher);
    }

    /**
     * 用于匹配方法签名的方法匹配器，检查的内容包括：
     * <ul>
     *     <li>方法名是否完全一致；</li>
     *     <li>返回值类型是否匹配，要求返回值类型与方法返回值类型完全一致，若返回值类型为{@code null}则表示匹配无返回值的方法；</li>
     *     <li>参数类型是否匹配，要求参数类型与方法参数类型完全一致，若参数类型为{@code null}则表示匹配无参数的方法；</li>
     * </ul>
     *
     * @param methodName     方法名
     * @param returnType     返回值类型，若为{@code null}则表示匹配无返回值的方法
     * @param parameterTypes 参数类型，若为{@code null}则表示匹配无参数的方法
     * @return 方法匹配器
     */
    public static Predicate<Method> forStrictMethodSignature(
            final String methodName, final Class<?> returnType, final Class<?>... parameterTypes) {
        Objects.requireNonNull(methodName);
        final Predicate<Method> resultMatcher = Objects.isNull(returnType) ?
                forNoneReturnType() : forReturnType(returnType);
        final Predicate<Method> parameterMatcher = Objects.isNull(parameterTypes) ?
                forNoneParameter() : forStrictParameterTypes(parameterTypes);
        return allMatch(forName(methodName), resultMatcher, parameterMatcher);
    }

    /**
     * 用于匹配方法签名的方法匹配器，检查的内容包括：
     * <ul>
     *     <li>方法名是否完全一致；</li>
     *     <li>返回值类型是否匹配，要求返回值类型与方法返回值类型完全一致；</li>
     *     <li>参数类型是否匹配，要求参数类型与方法参数类型完全一致；</li>
     * </ul>
     *
     * @param method 方法
     * @return 方法匹配器
     */
    public static Predicate<Method> forStrictMethodSignature(final Method method) {
        Objects.requireNonNull(method);
        return forMethodSignature(method.getName(), method.getReturnType(), method.getParameterTypes());
    }

    /**
     * 用于根据方法名匹配方法的方法匹配器。
     *
     * @param methodName 方法名
     * @return 方法匹配器
     */
    public static Predicate<Method> forName(final String methodName) {
        return method -> Objects.equals(method.getName(), methodName);
    }

    /**
     * 用于根据方法名匹配方法的方法匹配器，忽略方法名大小写。
     *
     * @param methodName 方法名
     * @return 方法匹配器
     */
    public static Predicate<Method> forNameIgnoreCase(final String methodName) {
        return method -> StringKit.endWithIgnoreCase(method.getName(), methodName);
    }

    /**
     * 用于匹配无返回值的方法的方法匹配器。
     *
     * @return 方法匹配器
     */
    public static Predicate<Method> forNoneReturnType() {
        return method -> Objects.equals(method.getReturnType(), Void.TYPE);
    }

    /**
     * 用于匹配指定参数类型的方法的方法匹配器，只要参数类型可以赋值给方法参数类型。
     *
     * @param returnType 返回值类型
     * @return 方法匹配器
     */
    public static Predicate<Method> forReturnType(final Class<?> returnType) {
        return method -> ClassKit.isAssignable(returnType, method.getReturnType());
    }

    /**
     * 用于匹配指定返回值类型的方法的方法匹配器，要求返回值类型与指定类型完全一致。
     *
     * @param returnType 返回值类型
     * @return 方法匹配器
     */
    public static Predicate<Method> forStrictReturnType(final Class<?> returnType) {
        return method -> Objects.equals(method.getReturnType(), returnType);
    }

    /**
     * 用于匹配无参数方法的方法匹配器。
     *
     * @return 方法匹配器
     */
    public static Predicate<Method> forNoneParameter() {
        return method -> method.getParameterCount() == 0;
    }

    /**
     * 用于匹配指定参数个数的方法的方法匹配器。
     *
     * @param count 参数个数
     * @return 方法匹配器
     */
    public static Predicate<Method> forParameterCount(final int count) {
        return method -> method.getParameterCount() == count;
    }

    /**
     * 用于匹配指定参数类型的方法的方法匹配器，只要参数类型可以赋值给方法参数类型即认为匹配成功。
     * 比如：参数类型为{@link java.util.ArrayList}，则方法参数类型可以为{@link java.util.List}、{@link java.util.Collection}等。
     *
     * @param parameterTypes 参数类型
     * @return 方法匹配器
     */
    public static Predicate<Method> forParameterTypes(final Class<?>... parameterTypes) {
        Objects.requireNonNull(parameterTypes);
        return method -> ClassKit.isAllAssignableFrom(parameterTypes, method.getParameterTypes());
    }

    /**
     * 用于匹配指定参数类型的方法的方法匹配器，与{@link #forParameterTypes}不同的是，该方法仅用于尽量可能最匹配的方法
     * <ul>
     *     <li>若参数为空，则表示匹配无参数方法；</li>
     *     <li>
     *         若参数不为空：
     *         <ul>
     *             <li>仅匹配{@code parameterTypes}中不为{@code null}的参数类型，若参数类型为{@code null}则表示匹配任意类型的参数；</li>
     *             <li>若N为{@code parameterTypes}长度，则仅要求{@code parameterTypes}不为{@code null}的类型与方法前N个参数类型匹配即可；</li>
     *             <li>若{@code parameterTypes}长度大于参数列表长度，则直接返回{@code false}；</li>
     *         </ul>
     *     </li>
     * </ul>
     * 比如：
     * 若存在三参数方法{@code method(String, Integer, Object)}，支持以下匹配：
     * <ul>
     *     <li>{@code forMostSpecificParameterTypes(CharSequence.class, Number.class, Object.class)}</li>
     *     <li>{@code forMostSpecificParameterTypes(String.class, Integer.class, Object.class)}</li>
     *     <li>{@code forMostSpecificParameterTypes(String.class, Integer.class, null)}</li>
     *     <li>{@code forMostSpecificParameterTypes(String.class, null, null)}</li>
     *     <li>{@code forMostSpecificParameterTypes(null, null, null)}</li>
     *     <li>{@code forMostSpecificParameterTypes(String.class, Integer.class)}</li>
     *     <li>{@code forMostSpecificParameterTypes(String.class)}</li>
     * </ul>
     *
     * @param parameterTypes 参数类型
     * @return 方法匹配器
     */
    public static Predicate<Method> forMostSpecificParameterTypes(final Class<?>... parameterTypes) {
        return mostSpecificStrictParameterTypesMatcher(parameterTypes, ClassKit::isAssignable);
    }

    /**
     * 用于匹配指定参数类型的方法的方法匹配器，与{@link #forParameterTypes}不同的是，该方法仅用于尽量可能最匹配的方法
     * <ul>
     *     <li>若参数为空，则表示匹配无参数方法；</li>
     *     <li>
     *         若参数不为空：
     *         <ul>
     *             <li>仅匹配{@code parameterTypes}中不为{@code null}的参数类型，若参数类型为{@code null}则表示匹配任意类型的参数；</li>
     *             <li>若N为{@code parameterTypes}长度，则仅要求{@code parameterTypes}不为{@code null}的类型与方法前N个参数类型匹配即可；</li>
     *             <li>若{@code parameterTypes}长度大于参数列表长度，则直接返回{@code false}；</li>
     *         </ul>
     *     </li>
     * </ul>
     * 比如：
     * 若存在三参数方法{@code method(String, Integer, Object)}，支持以下匹配：
     * <ul>
     *     <li>{@code forMostSpecificParameterTypes(String.class, Integer.class, Object.class)}</li>
     *     <li>{@code forMostSpecificParameterTypes(String.class, Integer.class, null)}</li>
     *     <li>{@code forMostSpecificParameterTypes(String.class, null, null)}</li>
     *     <li>{@code forMostSpecificParameterTypes(null, null, null)}</li>
     *     <li>{@code forMostSpecificParameterTypes(String.class, Integer.class)}</li>
     *     <li>{@code forMostSpecificParameterTypes(String.class)}</li>
     * </ul>
     *
     * @param parameterTypes 参数类型
     * @return 方法匹配器
     */
    public static Predicate<Method> forMostSpecificStrictParameterTypes(final Class<?>... parameterTypes) {
        return mostSpecificStrictParameterTypesMatcher(parameterTypes, Objects::equals);
    }

    /**
     * 用于匹配指定参数类型的方法的方法匹配器，只有参数类型完全匹配才认为匹配成功
     *
     * @param parameterTypes 参数类型
     * @return 方法匹配器
     */
    public static Predicate<Method> forStrictParameterTypes(final Class<?>... parameterTypes) {
        Objects.requireNonNull(parameterTypes);
        return method -> ArrayKit.equals(method.getParameterTypes(), parameterTypes);
    }

    private static Predicate<Method> mostSpecificStrictParameterTypesMatcher(
            final Class<?>[] parameterTypes, final BiPredicate<Class<?>, Class<?>> typeMatcher) {
        Objects.requireNonNull(parameterTypes);
        // 若参数为空，则表示匹配无参数方法
        if (parameterTypes.length == 0) {
            return forNoneParameter();
        }
        // 若参数不为空，则表示匹配指定参数类型的方法
        return method -> {
            final Class<?>[] methodParameterTypes = method.getParameterTypes();
            if (parameterTypes.length > methodParameterTypes.length) {
                return false;
            }
            for (int i = 0; i < parameterTypes.length; i++) {
                final Class<?> parameterType = parameterTypes[i];
                // 若参数类型为null，则表示匹配任意类型的参数
                if (Objects.isNull(parameterType)) {
                    continue;
                }
                // 若参数类型不为null，则要求参数类型可以赋值给方法参数类型
                if (typeMatcher.negate().test(parameterType, methodParameterTypes[i])) {
                    return false;
                }
            }
            return true;
        };
    }

}
