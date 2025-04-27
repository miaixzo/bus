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

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.function.LambdaX;
import org.miaixz.bus.core.center.map.reference.WeakConcurrentMap;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.resolve.AnnotationMappingProxy;
import org.miaixz.bus.core.lang.annotation.resolve.AnnotationProxy;
import org.miaixz.bus.core.lang.annotation.resolve.elements.CombinationAnnotatedElement;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.text.CharsBacker;

/**
 * 注解工具类 快速获取注解对象、注解值等工具封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnnoKit {

    private static final String JDK_MEMBER_ATTRIBUTE = "memberValues";
    private static final String SPRING_MEMBER_ATTRIBUTE = "valueCache";
    private static final String BUS_MEMBER_ATTRIBUTE = "valueCache";
    private static final String SPRING_INVOCATION_HANDLER = "SynthesizedMergedAnnotationInvocationHandler";

    /**
     * 直接声明的注解缓存
     */
    private static final Map<AnnotatedElement, Annotation[]> DECLARED_ANNOTATIONS_CACHE = new WeakConcurrentMap<>();

    /**
     * 获取直接声明的注解，若已有缓存则从缓存中获取
     *
     * @param element {@link AnnotatedElement}
     * @return 注解
     */
    public static Annotation[] getDeclaredAnnotations(final AnnotatedElement element) {
        return DECLARED_ANNOTATIONS_CACHE.computeIfAbsent(element, AnnotatedElement::getDeclaredAnnotations);
    }

    /**
     * 将指定的被注解的元素转换为组合注解元素
     *
     * @param annotationEle 注解元素
     * @return 组合注解元素
     */
    public static CombinationAnnotatedElement toCombination(final AnnotatedElement annotationEle) {
        if (annotationEle instanceof CombinationAnnotatedElement) {
            return (CombinationAnnotatedElement) annotationEle;
        }
        return new CombinationAnnotatedElement(annotationEle);
    }

    /**
     * 获取指定注解
     *
     * @param annotationEle   {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param isToCombination 是否为转换为组合注解，组合注解可以递归获取注解的注解
     * @return 注解对象
     */
    public static Annotation[] getAnnotations(final AnnotatedElement annotationEle, final boolean isToCombination) {
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
    public static <T> T[] getCombinationAnnotations(final AnnotatedElement annotationEle,
            final Class<T> annotationType) {
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
    public static <T> T[] getAnnotations(final AnnotatedElement annotationEle, final boolean isToCombination,
            final Class<T> annotationType) {
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
    public static Annotation[] getAnnotations(final AnnotatedElement annotationEle, final boolean isToCombination,
            final Predicate<Annotation> predicate) {
        if (null == annotationEle) {
            return null;
        }

        if (isToCombination) {
            if (null == predicate) {
                return toCombination(annotationEle).getAnnotations();
            }
            return CombinationAnnotatedElement.of(annotationEle, predicate).getAnnotations();
        }

        final Annotation[] result = annotationEle.getAnnotations();
        if (null == predicate) {
            return result;
        }
        return ArrayKit.filter(result, predicate);
    }

    /**
     * 获取指定注解
     *
     * @param <A>            注解类型
     * @param annotationEle  {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationType 注解类型
     * @return 注解对象
     */
    public static <A extends Annotation> A getAnnotation(final AnnotatedElement annotationEle,
            final Class<A> annotationType) {
        return (null == annotationEle) ? null : toCombination(annotationEle).getAnnotation(annotationType);
    }

    /**
     * 检查是否包含指定注解 注解类传入全名，通过{@link Class#forName(String)}加载，避免不存在的注解导致的ClassNotFoundException
     *
     * @param annotationEle      {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationTypeName 注解类型完整类名
     * @return 是否包含指定注解
     */
    public static boolean hasAnnotation(final AnnotatedElement annotationEle, final String annotationTypeName) {
        Class aClass = null;
        try {
            // Android可能无这个类
            aClass = Class.forName(annotationTypeName);
        } catch (final ClassNotFoundException e) {
            // ignore
        }
        if (null != aClass) {
            return hasAnnotation(annotationEle, aClass);
        }
        return false;
    }

    /**
     * 检查是否包含指定注解
     *
     * @param annotationEle  {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationType 注解类型
     * @return 是否包含指定注解
     */
    public static boolean hasAnnotation(final AnnotatedElement annotationEle,
            final Class<? extends Annotation> annotationType) {
        return null != getAnnotation(annotationEle, annotationType);
    }

    /**
     * 获取指定注解默认值 如果无指定的属性方法返回null
     *
     * @param <T>            注解值类型
     * @param annotationEle  {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationType 注解类型
     * @return 注解对象
     * @throws InternalException 调用注解中的方法时执行异常
     */
    public static <T> T getAnnotationValue(final AnnotatedElement annotationEle,
            final Class<? extends Annotation> annotationType) throws InternalException {
        return getAnnotationValue(annotationEle, annotationType, "value");
    }

    /**
     * 获取指定注解属性的值 如果无指定的属性方法返回null
     *
     * @param <A>           注解类型
     * @param <R>           注解类型值
     * @param annotationEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param propertyName  属性名，例如注解中定义了name()方法，则 此处传入name
     * @return 注解对象
     * @throws InternalException 调用注解中的方法时执行异常
     */
    public static <A extends Annotation, R> R getAnnotationValue(final AnnotatedElement annotationEle,
            final FunctionX<A, R> propertyName) {
        if (propertyName == null) {
            return null;
        } else {
            final LambdaX lambda = LambdaKit.resolve(propertyName);
            final String instantiatedMethodType = lambda.getLambda().getInstantiatedMethodType();
            final Class<A> annotationClass = ClassKit.loadClass(StringKit.sub(instantiatedMethodType, 2,
                    StringKit.indexOf(instantiatedMethodType, Symbol.C_SEMICOLON)));
            return getAnnotationValue(annotationEle, annotationClass, lambda.getLambda().getImplMethodName());
        }
    }

    /**
     * 获取指定注解属性的值 如果无指定的属性方法返回null
     *
     * @param <T>            注解值类型
     * @param annotationEle  {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationType 注解类型
     * @param propertyName   属性名，例如注解中定义了name()方法，则 此处传入name
     * @return 注解对象
     * @throws InternalException 调用注解中的方法时执行异常
     */
    public static <T> T getAnnotationValue(final AnnotatedElement annotationEle,
            final Class<? extends Annotation> annotationType, final String propertyName) throws InternalException {
        final Annotation annotation = getAnnotation(annotationEle, annotationType);
        if (null == annotation) {
            return null;
        }

        final Method method = MethodKit.getMethodOfObject(annotation, propertyName);
        if (null == method) {
            return null;
        }
        return MethodKit.invoke(annotation, method);
    }

    /**
     * 获取指定注解中所有属性值 如果无指定的属性方法返回null
     *
     * @param annotationEle  {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @param annotationType 注解类型
     * @return 注解对象
     * @throws InternalException 调用注解中的方法时执行异常
     */
    public static Map<String, Object> getAnnotationValueMap(final AnnotatedElement annotationEle,
            final Class<? extends Annotation> annotationType) throws InternalException {
        final Annotation annotation = getAnnotation(annotationEle, annotationType);
        if (null == annotation) {
            return null;
        }

        final Method[] methods = MethodKit.getMethods(annotationType, t -> {
            if (ArrayKit.isEmpty(t.getParameterTypes())) {
                // 只读取无参方法
                final String name = t.getName();
                // 跳过自有的几个方法
                return (!Normal.HASHCODE.equals(name)) && (!Normal.TOSTRING.equals(name))
                        && (!"annotationType".equals(name));
            }
            return false;
        });

        final HashMap<String, Object> result = new HashMap<>(methods.length, 1);
        for (final Method method : methods) {
            result.put(method.getName(), MethodKit.invoke(annotation, method));
        }
        return result;
    }

    /**
     * 获取注解类的保留时间，可选值 SOURCE（源码时），CLASS（编译时），RUNTIME（运行时），默认为 CLASS
     *
     * @param annotationType 注解类
     * @return 保留时间枚举
     */
    public static RetentionPolicy getRetentionPolicy(final Class<? extends Annotation> annotationType) {
        final Retention retention = annotationType.getAnnotation(Retention.class);
        if (null == retention) {
            return RetentionPolicy.CLASS;
        }
        return retention.value();
    }

    /**
     * 获取注解类可以用来修饰哪些程序元素，如 TYPE, METHOD, CONSTRUCTOR, FIELD, PARAMETER 等
     *
     * @param annotationType 注解类
     * @return 注解修饰的程序元素数组
     */
    public static ElementType[] getTargetType(final Class<? extends Annotation> annotationType) {
        final Target target = annotationType.getAnnotation(Target.class);
        if (null == target) {
            // 如果没有定义@target元注解，则表示支持所有节点
            return ElementType.values();
        }
        return target.value();
    }

    /**
     * 是否会保存到 Javadoc 文档中
     *
     * @param annotationType 注解类
     * @return 是否会保存到 Javadoc 文档中
     */
    public static boolean isDocumented(final Class<? extends Annotation> annotationType) {
        return annotationType.isAnnotationPresent(Documented.class);
    }

    /**
     * 是否可以被继承，默认为 false
     *
     * @param annotationType 注解类
     * @return 是否会保存到 Javadoc 文档中
     */
    public static boolean isInherited(final Class<? extends Annotation> annotationType) {
        return annotationType.isAnnotationPresent(Inherited.class);
    }

    /**
     * 设置新的注解的属性（字段）值 注意此方法在jdk9+中抛出异常，须添加`--add-opens=java.base/java.lang=ALL-UNNAMED`启动参数
     *
     * @param annotation      注解对象
     * @param annotationField 注解属性（字段）名称
     * @param value           要更新的属性值
     */
    public static void setValue(final Annotation annotation, final String annotationField, final Object value) {
        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
        String memberAttributeName = JDK_MEMBER_ATTRIBUTE;
        // Spring合成注解
        if (CharsBacker.contains(invocationHandler.getClass().getName(), SPRING_INVOCATION_HANDLER)) {
            memberAttributeName = SPRING_MEMBER_ATTRIBUTE;
        }
        // 合成注解
        else if (invocationHandler instanceof AnnotationMappingProxy) {
            memberAttributeName = BUS_MEMBER_ATTRIBUTE;
        }
        final Map<String, Object> memberValues = (Map<String, Object>) FieldKit.getFieldValue(invocationHandler,
                memberAttributeName);
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
    public static <T extends Annotation> T getAnnotationAlias(final AnnotatedElement annotationEle,
            final Class<T> annotationType) {
        final T annotation = getAnnotation(annotationEle, annotationType);
        if (null == annotation) {
            return null;
        }
        return (T) Proxy.newProxyInstance(annotationType.getClassLoader(), new Class[] { annotationType },
                new AnnotationProxy<>(annotation));
    }

    /**
     * 获取注解属性，若已有缓存则从缓存中获取
     *
     * @param annotationType 注解类型
     * @return 注解属性
     */
    public static Method[] getAnnotationAttributes(final Class<? extends Annotation> annotationType) {
        return Stream.of(MethodKit.getDeclaredMethods(annotationType)).filter(AnnoKit::isAnnotationAttribute)
                .toArray(Method[]::new);
    }

    /**
     * 该方法是否是注解属性，需要满足下述条件：
     * <ul>
     * <li>不是{@link Object#equals(Object)}；</li>
     * <li>不是{@link Object#hashCode()}；</li>
     * <li>不是{@link Object#toString()}；</li>
     * <li>不是桥接方法；</li>
     * <li>不是合成方法；</li>
     * <li>不是静态方法；</li>
     * <li>是公共方法；</li>
     * <li>方法必须没有参数；</li>
     * <li>方法必须有返回值（返回值类型不为{@link Void}）；</li>
     * </ul>
     *
     * @param attribute 方法对象
     * @return 是否
     */
    public static boolean isAnnotationAttribute(final Method attribute) {
        return !MethodKit.isEqualsMethod(attribute) && !MethodKit.isHashCodeMethod(attribute)
                && !MethodKit.isToStringMethod(attribute) && ArrayKit.isEmpty(attribute.getParameterTypes())
                && ObjectKit.notEquals(attribute.getReturnType(), Void.class)
                && !Modifier.isStatic(attribute.getModifiers()) && Modifier.isPublic(attribute.getModifiers())
                && !attribute.isBridge() && !attribute.isSynthetic();
    }

    /**
     * 清空相关缓存
     */
    public static void clearCaches() {
        DECLARED_ANNOTATIONS_CACHE.clear();
    }

}
