/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.annotation.Alias;
import org.miaixz.bus.core.annotation.AnnoProxy;
import org.miaixz.bus.core.annotation.Annotated;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.function.FuncX;
import org.miaixz.bus.core.scanner.AnnotationScanner;
import org.miaixz.bus.core.scanner.Synthetic;
import org.miaixz.bus.core.scanner.annotation.FieldScanner;
import org.miaixz.bus.core.scanner.annotation.MetaScanner;
import org.miaixz.bus.core.scanner.annotation.MethodScanner;
import org.miaixz.bus.core.scanner.annotation.TypeScanner;

import java.lang.annotation.*;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 注解工具类
 * 快速获取注解对象、注解值等工具封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnnoKit {

    /**
     * 元注解
     */
    static final Set<Class<? extends Annotation>> META_ANNOTATIONS = CollKit.newHashSet(
            Target.class,
            Retention.class,
            Inherited.class,
            Documented.class,
            SuppressWarnings.class,
            Override.class,
            Deprecated.class
    );

    /**
     * 是否为Jdk自带的元注解
     * 包括：
     * <ul>
     *     <li>{@link Target}</li>
     *     <li>{@link Retention}</li>
     *     <li>{@link Inherited}</li>
     *     <li>{@link Documented}</li>
     *     <li>{@link SuppressWarnings}</li>
     *     <li>{@link Override}</li>
     *     <li>{@link Deprecated}</li>
     * </ul>
     *
     * @param annotationType 注解类型
     * @return 是否为Jdk自带的元注解
     */
    public static boolean isJdkMetaAnnotation(Class<? extends Annotation> annotationType) {
        return META_ANNOTATIONS.contains(annotationType);
    }

    /**
     * 是否不为Jdk自带的元注解
     * 包括：
     * <ul>
     *     <li>{@link Target}</li>
     *     <li>{@link Retention}</li>
     *     <li>{@link Inherited}</li>
     *     <li>{@link Documented}</li>
     *     <li>{@link SuppressWarnings}</li>
     *     <li>{@link Override}</li>
     *     <li>{@link Deprecated}</li>
     * </ul>
     *
     * @param annotationType 注解类型
     * @return 是否为Jdk自带的元注解
     */
    public static boolean isNotJdkMateAnnotation(Class<? extends Annotation> annotationType) {
        return false == isJdkMetaAnnotation(annotationType);
    }

    /**
     * 将指定的被注解的元素转换为组合注解元素
     *
     * @param annotationEle 注解元素
     * @return 组合注解元素
     */
    public static Annotated toCombination(AnnotatedElement annotationEle) {
        if (annotationEle instanceof Annotated) {
            return (Annotated) annotationEle;
        }
        return new Annotated(annotationEle);
    }

    /**
     * 获取指定注解
     *
     * @param annotationEle   {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param isToCombination 是否为转换为组合注解，组合注解可以递归获取注解的注解
     * @return 注解对象
     */
    public static Annotation[] getAnnotations(AnnotatedElement annotationEle, boolean isToCombination) {
        return getAnnotations(annotationEle, isToCombination, (Predicate<Annotation>) null);
    }

    /**
     * 获取组合注解
     *
     * @param <T>            注解类型
     * @param annotationEle  {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationType 限定的
     * @return 注解对象数组
     */
    public static <T> T[] getCombinationAnnotations(AnnotatedElement annotationEle, Class<T> annotationType) {
        return getAnnotations(annotationEle, true, annotationType);
    }

    /**
     * 获取指定注解
     *
     * @param <T>             注解类型
     * @param annotationEle   {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param isToCombination 是否为转换为组合注解，组合注解可以递归获取注解的注解
     * @param annotationType  限定的
     * @return 注解对象数组
     */
    public static <T> T[] getAnnotations(AnnotatedElement annotationEle, boolean isToCombination, Class<T> annotationType) {
        final Annotation[] annotations = getAnnotations(annotationEle, isToCombination,
                (annotation -> null == annotationType || annotationType.isAssignableFrom(annotation.getClass())));

        final T[] result = ArrayKit.newArray(annotationType, annotations.length);
        for (int i = 0; i < annotations.length; i++) {
            result[i] = (T) annotations[i];
        }
        return result;
    }

    /**
     * 获取指定注解
     *
     * @param annotationEle   {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param isToCombination 是否为转换为组合注解，组合注解可以递归获取注解的注解
     * @param predicate       过滤器，{@link Predicate#test(Object)}返回{@code true}保留，否则不保留
     * @return 注解对象
     */
    public static Annotation[] getAnnotations(AnnotatedElement annotationEle, boolean isToCombination, Predicate<Annotation> predicate) {
        if (null == annotationEle) {
            return null;
        }

        if (isToCombination) {
            if (null == predicate) {
                return toCombination(annotationEle).getAnnotations();
            }
            return Annotated.of(annotationEle, predicate).getAnnotations();
        }

        final Annotation[] result = annotationEle.getAnnotations();
        if (null == predicate) {
            return result;
        }
        return ArrayKit.filter(result, predicate::test);
    }

    /**
     * 获取指定注解
     *
     * @param <A>            注解类型
     * @param annotationEle  {@link AnnotatedElement},可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationType 注解类型
     * @return 注解对象
     */
    public static <A extends Annotation> A getAnnotation(AnnotatedElement annotationEle, Class<A> annotationType) {
        return (null == annotationEle) ? null : toCombination(annotationEle).getAnnotation(annotationType);
    }

    /**
     * 检查是否包含指定注解指定注解
     *
     * @param annotationEle  {@link AnnotatedElement}，可以是Class/Method/Field/Constructor/ReflectPermission
     * @param annotationType 注解类型
     * @return 是否包含指定注解
     */
    public static boolean hasAnnotation(AnnotatedElement annotationEle, Class<? extends Annotation> annotationType) {
        return null != getAnnotation(annotationEle, annotationType);
    }

    /**
     * 获取指定注解属性的值
     * 如果无指定的属性方法返回null
     *
     * @param <A>           注解类型
     * @param <R>           注解类型值
     * @param annotationEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param propertyName  属性名，例如注解中定义了name()方法，则 此处传入name
     * @return 注解对象
     */
    public static <A extends Annotation, R> R getAnnotationValue(final AnnotatedElement annotationEle, final FuncX<A, R> propertyName) {
        if (propertyName == null) {
            return null;
        } else {
            final LambdaKit.Info lambda = LambdaKit.resolve(propertyName);
            final String instantiatedMethodType = lambda.getLambda().getInstantiatedMethodType();
            final Class<A> annotationClass = ClassKit.loadClass(StringKit.sub(instantiatedMethodType, 2, StringKit.indexOf(instantiatedMethodType, ';')));
            return getAnnotationValue(annotationEle, annotationClass, lambda.getLambda().getImplMethodName());
        }
    }

    /**
     * 获取指定注解默认值
     * 如果无指定的属性方法返回null
     *
     * @param <T>            注解值类型
     * @param annotationEle  {@link AccessibleObject},可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationType 注解类型
     * @return 注解对象
     */
    public static <T> T getAnnotationValue(AnnotatedElement annotationEle, Class<? extends Annotation> annotationType) {
        return getAnnotationValue(annotationEle, annotationType, "value");
    }

    /**
     * 获取指定注解属性的值
     * 如果无指定的属性方法返回null
     *
     * @param <T>            注解值类型
     * @param annotationEle  {@link AccessibleObject},可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationType 注解类型
     * @param propertyName   属性名,例如注解中定义了name()方法,则 此处传入name
     * @return 注解对象
     */
    public static <T> T getAnnotationValue(AnnotatedElement annotationEle, Class<? extends Annotation> annotationType, String propertyName) {
        final Annotation annotation = getAnnotation(annotationEle, annotationType);
        if (null == annotation) {
            return null;
        }

        final Method method = ReflectKit.getMethodOfObject(annotation, propertyName);
        if (null == method) {
            return null;
        }
        return ReflectKit.invoke(annotation, method);
    }

    /**
     * 获取指定注解中所有属性值
     * 如果无指定的属性方法返回null
     *
     * @param annotationEle  {@link AnnotatedElement},可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationType 注解类型
     * @return 注解对象
     */
    public static Map<String, Object> getAnnotationValueMap(AnnotatedElement annotationEle, Class<? extends Annotation> annotationType) {
        final Annotation annotation = getAnnotation(annotationEle, annotationType);
        if (null == annotation) {
            return null;
        }

        final Method[] methods = ReflectKit.getMethods(annotationType, t -> {
            if (ArrayKit.isEmpty(t.getParameterTypes())) {
                // 只读取无参方法
                final String name = t.getName();
                // 跳过自有的几个方法
                return (false == "hashCode".equals(name))
                        && (false == "toString".equals(name))
                        && (false == "annotationType".equals(name));
            }
            return false;
        });

        final HashMap<String, Object> result = new HashMap<>(methods.length, 1);
        for (Method method : methods) {
            result.put(method.getName(), ReflectKit.invoke(annotation, method));
        }
        return result;
    }

    /**
     * 获取注解类的保留时间,可选值 SOURCE(源码时),CLASS(编译时),RUNTIME(运行时),默认为 CLASS
     *
     * @param annotationType 注解类
     * @return 保留时间枚举
     */
    public static RetentionPolicy getRetentionPolicy(Class<? extends Annotation> annotationType) {
        final Retention retention = annotationType.getAnnotation(Retention.class);
        if (null == retention) {
            return RetentionPolicy.CLASS;
        }
        return retention.value();
    }

    /**
     * 获取注解类可以用来修饰哪些程序元素,如 TYPE, METHOD, CONSTRUCTOR, FIELD, PARAMETER 等
     *
     * @param annotationType 注解类
     * @return 注解修饰的程序元素数组
     */
    public static ElementType[] getTargetType(Class<? extends Annotation> annotationType) {
        final Target target = annotationType.getAnnotation(Target.class);
        if (null == target) {
            return new ElementType[]{ElementType.TYPE,
                    ElementType.FIELD,
                    ElementType.METHOD,
                    ElementType.PARAMETER,
                    ElementType.CONSTRUCTOR,
                    ElementType.LOCAL_VARIABLE,
                    ElementType.ANNOTATION_TYPE,
                    ElementType.PACKAGE
            };
        }
        return target.value();
    }

    /**
     * 是否会保存到 Javadoc 文档中
     *
     * @param annotationType 注解类
     * @return 是否会保存到 Javadoc 文档中
     */
    public static boolean isDocumented(Class<? extends Annotation> annotationType) {
        return annotationType.isAnnotationPresent(Documented.class);
    }

    /**
     * 是否可以被继承,默认为 false
     *
     * @param annotationType 注解类
     * @return 是否会保存到 Javadoc 文档中
     */
    public static boolean isInherited(Class<? extends Annotation> annotationType) {
        return annotationType.isAnnotationPresent(Inherited.class);
    }

    /**
     * 设置新的注解的属性(字段)值
     *
     * @param annotation      注解对象
     * @param annotationField 注解属性(字段)名称
     * @param value           要更新的属性值
     */
    public static void setValue(Annotation annotation, String annotationField, Object value) {
        final Map memberValues = (Map) ReflectKit.getFieldValue(Proxy.getInvocationHandler(annotation), "memberValues");
        memberValues.put(annotationField, value);
    }

    /**
     * 获取别名支持后的注解
     *
     * @param annotationEle  被注解的类
     * @param annotationType 注解类型Class
     * @param <T>            注解类型
     * @return 别名支持后的注解
     */
    public static <T extends Annotation> T getAlias(AnnotatedElement annotationEle, Class<T> annotationType) {
        final T annotation = getAnnotation(annotationEle, annotationType);
        return (T) Proxy.newProxyInstance(annotationType.getClassLoader(), new Class[]{annotationType}, new AnnoProxy<>(annotation));
    }

    /**
     * 将指定注解实例与其元注解转为合成注解
     *
     * @param annotation     注解
     * @param annotationType 注解类型
     * @param <T>            注解类型
     * @return 合成注解
     */
    public static <T extends Annotation> T getSynthesis(Annotation annotation, Class<T> annotationType) {
        return Synthetic.of(annotation).getAnnotation(annotationType);
    }

    /**
     * 获取元素上所有指定注解
     * <ul>
     *     <li>若元素是类，则递归解析全部父类和全部父接口上的注解</li>
     *     <li>若元素是方法、属性或注解，则只解析其直接声明的注解</li>
     * </ul>
     *
     * @param annotatedEle   {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationType 注解类
     * @param <T>            注解类型
     * @return 合成注解
     */
    public static <T extends Annotation> List<T> getAllSynthesis(AnnotatedElement annotatedEle, Class<T> annotationType) {
        AnnotationScanner[] scanners = new AnnotationScanner[]{
                new MetaScanner(), new TypeScanner(), new MethodScanner(), new FieldScanner()
        };
        return AnnotationScanner.scanByAnySupported(annotatedEle, scanners).stream()
                .map(annotation -> getSynthesis(annotation, annotationType))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 扫描注解类，以及注解类的{@link Class}层级结构中的注解，将返回除了{@link #META_ANNOTATIONS}中指定的JDK默认注解外，
     * 按元注解对象与{@code annotationType}的距离和{@link Class#getAnnotations()}顺序排序的注解对象集合
     *
     * @param annotationType 注解类
     * @return 注解对象集合
     */
    public static List<Annotation> scanMetaAnnotation(Class<? extends Annotation> annotationType) {
        return new MetaScanner().getIfSupport(annotationType);
    }

    /**
     * <p>扫描类以及类的{@link Class}层级结构中的注解，将返回除了{@link #META_ANNOTATIONS}中指定的JDK默认元注解外,
     * 全部类/接口的{@link Class#getAnnotations()}方法返回的注解对象
     * 层级结构将按广度优先递归，遵循规则如下：
     * <ul>
     *     <li>同一层级中，优先处理父类，然后再处理父接口；</li>
     *     <li>同一个接口在不同层级出现，优先选择层级距离{@code targetClass}更近的接口；</li>
     *     <li>同一个接口在相同层级出现，优先选择其子类/子接口被先解析的那个；</li>
     * </ul>
     * 注解根据其声明类/接口被扫描的顺序排序，若注解都在同一个{@link Class}中被声明，则还会遵循{@link Class#getAnnotations()}的顺序
     *
     * @param targetClass 类
     * @return 注解对象集合
     */
    public static List<Annotation> scanClass(Class<?> targetClass) {
        return new TypeScanner().getIfSupport(targetClass);
    }

    /**
     * <p>扫描方法，以及该方法所在类的{@link Class}层级结构中的具有相同方法签名的方法，
     * 将返回除了{@link #META_ANNOTATIONS}中指定的JDK默认元注解外,
     * 全部匹配方法上{@link Method#getAnnotations()}方法返回的注解对象
     * 方法所在类的层级结构将按广度优先递归，遵循规则如下：
     * <ul>
     *     <li>同一层级中，优先处理父类，然后再处理父接口；</li>
     *     <li>同一个接口在不同层级出现，优先选择层级距离{@code targetClass}更近的接口；</li>
     *     <li>同一个接口在相同层级出现，优先选择其子类/子接口被先解析的那个；</li>
     * </ul>
     * 方法上的注解根据方法的声明类/接口被扫描的顺序排序，若注解都在同一个类的同一个方法中被声明，则还会遵循{@link Method#getAnnotations()}的顺序
     *
     * @param method 方法
     * @return 注解对象集合
     */
    public static List<Annotation> scanMethod(Method method) {
        return new MethodScanner(true).getIfSupport(method);
    }

    /**
     * 方法是否为注解属性方法
     * 方法无参数，且有返回值的方法认为是注解属性的方法
     *
     * @param method 方法
     * @return the boolean
     */
    public static boolean isAttributeMethod(Method method) {
        return method.getParameterCount() == 0 && method.getReturnType() != void.class;
    }

    /**
     * 获取注解的全部属性值获取方法
     *
     * @param annotationType 注解
     * @return 注解的全部属性值
     * @throws IllegalArgumentException 当别名属性在注解中不存在，或别名属性的值与原属性的值类型不一致时抛出
     */
    public static Map<String, Method> getAttributeMethods(Class<? extends Annotation> annotationType) {
        // 获取全部注解属性值
        Map<String, Method> attributeMethods = Stream.of(annotationType.getDeclaredMethods())
                .filter(AnnoKit::isAttributeMethod)
                .collect(Collectors.toMap(Method::getName, Function.identity()));
        // 处理别名
        attributeMethods.forEach((methodName, method) -> {
            String alias = Optional.ofNullable(method.getAnnotation(Alias.class))
                    .map(Alias::value)
                    .orElse(null);
            if (ObjectKit.isNull(alias)) {
                return;
            }
            // 存在别名，则将原本的值替换为别名对应的值
            Assert.isTrue(attributeMethods.containsKey(alias), "No method for alias: [{}]", alias);
            Method aliasAttributeMethod = attributeMethods.get(alias);
            Assert.isTrue(
                    ObjectKit.isNull(aliasAttributeMethod) || ClassKit.isAssignable(method.getReturnType(), aliasAttributeMethod.getReturnType()),
                    "Return type of the alias method [{}] is inconsistent with the original [{}]",
                    aliasAttributeMethod.getClass(), method.getParameterTypes()
            );
            attributeMethods.put(methodName, aliasAttributeMethod);
        });
        return attributeMethods;
    }

}
