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

/**
 * 注解工具类，提供快速获取注解对象、注解值等功能的封装。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnnoKit {

    /**
     * JDK注解属性字段名
     */
    private static final String JDK_MEMBER_ATTRIBUTE = "memberValues";
    /**
     * Spring注解属性字段名
     */
    private static final String SPRING_MEMBER_ATTRIBUTE = "valueCache";
    /**
     * Bus注解属性字段名
     */
    private static final String BUS_MEMBER_ATTRIBUTE = "valueCache";
    /**
     * Spring合成注解处理器类名
     */
    private static final String SPRING_INVOCATION_HANDLER = "SynthesizedMergedAnnotationInvocationHandler";
    /**
     * 直接声明的注解缓存，使用弱引用并发Map存储
     */
    private static final Map<AnnotatedElement, Annotation[]> DECLARED_ANNOTATIONS_CACHE = new WeakConcurrentMap<>();

    /**
     * 获取指定元素的直接声明注解，若存在缓存则从缓存中获取。
     *
     * @param element 被注解的元素，可以是Class、Method、Field、Constructor等
     * @return 注解数组
     */
    public static Annotation[] getDeclaredAnnotations(final AnnotatedElement element) {
        return DECLARED_ANNOTATIONS_CACHE.computeIfAbsent(element, AnnotatedElement::getDeclaredAnnotations);
    }

    /**
     * 将指定的被注解元素转换为组合注解元素，支持递归获取注解的注解。
     *
     * @param annotationEle 被注解的元素
     * @return 组合注解元素
     */
    public static CombinationAnnotatedElement toCombination(final AnnotatedElement annotationEle) {
        if (annotationEle instanceof CombinationAnnotatedElement) {
            return (CombinationAnnotatedElement) annotationEle;
        }
        return new CombinationAnnotatedElement(annotationEle);
    }

    /**
     * 获取指定元素的注解。
     *
     * @param annotationEle   被注解的元素，可以是Class、Method、Field、Constructor等
     * @param isToCombination 是否转换为组合注解，组合注解支持递归获取注解的注解
     * @return 注解数组
     */
    public static Annotation[] getAnnotations(final AnnotatedElement annotationEle, final boolean isToCombination) {
        return getAnnotations(annotationEle, isToCombination, (Predicate<Annotation>) null);
    }

    /**
     * 获取指定元素的组合注解，限定为特定注解类型。
     *
     * @param <T>            注解类型
     * @param annotationEle  被注解的元素
     * @param annotationType 注解类型
     * @return 限定类型的注解数组
     */
    public static <T> T[] getCombinationAnnotations(final AnnotatedElement annotationEle,
            final Class<T> annotationType) {
        return getAnnotations(annotationEle, true, annotationType);
    }

    /**
     * 获取指定元素的注解，限定为特定注解类型。
     *
     * @param <T>             注解类型
     * @param annotationEle   被注解的元素
     * @param isToCombination 是否转换为组合注解
     * @param annotationType  注解类型
     * @return 限定类型的注解数组
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
     * 获取指定元素的注解，支持通过过滤器筛选。
     *
     * @param annotationEle   被注解的元素
     * @param isToCombination 是否转换为组合注解
     * @param predicate       过滤器，筛选注解
     * @return 注解数组
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
     * 获取指定元素的特定类型注解。
     *
     * @param <A>            注解类型
     * @param annotationEle  被注解的元素
     * @param annotationType 注解类型
     * @return 注解对象，若不存在则返回null
     */
    public static <A extends Annotation> A getAnnotation(final AnnotatedElement annotationEle,
            final Class<A> annotationType) {
        return (null == annotationEle) ? null : toCombination(annotationEle).getAnnotation(annotationType);
    }

    /**
     * 检查指定元素是否包含特定注解，通过注解类全名加载，避免ClassNotFoundException。
     *
     * @param annotationEle      被注解的元素
     * @param annotationTypeName 注解类型的完整类名
     * @return 是否包含指定注解
     */
    public static boolean hasAnnotation(final AnnotatedElement annotationEle, final String annotationTypeName) {
        Class aClass = null;
        try {
            aClass = Class.forName(annotationTypeName);
        } catch (final ClassNotFoundException e) {
            // 忽略异常
        }
        if (null != aClass) {
            return hasAnnotation(annotationEle, aClass);
        }
        return false;
    }

    /**
     * 检查指定元素是否包含特定注解。
     *
     * @param annotationEle  被注解的元素
     * @param annotationType 注解类型
     * @return 是否包含指定注解
     */
    public static boolean hasAnnotation(final AnnotatedElement annotationEle,
            final Class<? extends Annotation> annotationType) {
        return null != getAnnotation(annotationEle, annotationType);
    }

    /**
     * 获取指定注解的默认值（通常为value属性）。
     *
     * @param <T>            注解值类型
     * @param annotationEle  被注解的元素
     * @param annotationType 注解类型
     * @return 注解默认值，若无默认值则返回null
     * @throws InternalException 调用注解方法时发生异常
     */
    public static <T> T getAnnotationValue(final AnnotatedElement annotationEle,
            final Class<? extends Annotation> annotationType) throws InternalException {
        return getAnnotationValue(annotationEle, annotationType, "value");
    }

    /**
     * 获取指定注解的属性值，通过Lambda表达式指定属性。
     *
     * @param <A>           注解类型
     * @param <R>           属性值类型
     * @param annotationEle 被注解的元素
     * @param propertyName  属性名对应的Lambda表达式
     * @return 属性值，若无指定属性则返回null
     * @throws InternalException 调用注解方法时发生异常
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
     * 获取指定注解的属性值。
     *
     * @param <T>            注解值类型
     * @param annotationEle  被注解的元素
     * @param annotationType 注解类型
     * @param propertyName   属性名
     * @return 属性值，若无指定属性则返回null
     * @throws InternalException 调用注解方法时发生异常
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
     * 获取指定注解的所有属性值。
     *
     * @param annotationEle  被注解的元素
     * @param annotationType 注解类型
     * @return 属性名到属性值的映射，若无注解则返回null
     * @throws InternalException 调用注解方法时发生异常
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
     * 获取注解类的保留策略（Retention Policy）。
     *
     * @param annotationType 注解类型
     * @return 保留策略，默认为CLASS
     */
    public static RetentionPolicy getRetentionPolicy(final Class<? extends Annotation> annotationType) {
        final Retention retention = annotationType.getAnnotation(Retention.class);
        if (null == retention) {
            return RetentionPolicy.CLASS;
        }
        return retention.value();
    }

    /**
     * 获取注解类支持的程序元素类型（Element Type）。
     *
     * @param annotationType 注解类型
     * @return 程序元素类型数组，若无@Target注解则返回所有类型
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
     * 检查注解类是否会被记录到Javadoc文档中。
     *
     * @param annotationType 注解类型
     * @return 是否记录到Javadoc文档
     */
    public static boolean isDocumented(final Class<? extends Annotation> annotationType) {
        return annotationType.isAnnotationPresent(Documented.class);
    }

    /**
     * 检查注解类是否可被继承。
     *
     * @param annotationType 注解类型
     * @return 是否可被继承
     */
    public static boolean isInherited(final Class<? extends Annotation> annotationType) {
        return annotationType.isAnnotationPresent(Inherited.class);
    }

    /**
     * 设置注解的属性值。
     * <p>
     * 注意：在JDK9及以上版本可能抛出异常，需添加`--add-opens=java.base/java.lang=ALL-UNNAMED`启动参数。
     * </p>
     *
     * @param annotation      注解对象
     * @param annotationField 属性名
     * @param value           属性值
     */
    public static void setValue(final Annotation annotation, final String annotationField, final Object value) {
        final InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
        String memberAttributeName = JDK_MEMBER_ATTRIBUTE;
        // Spring合成注解
        if (StringKit.contains(invocationHandler.getClass().getName(), SPRING_INVOCATION_HANDLER)) {
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
     * 获取支持别名的注解代理对象。
     *
     * @param <T>            注解类型
     * @param annotationEle  被注解的元素
     * @param annotationType 注解类型
     * @return 注解代理对象，若无注解则返回null
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
     * 获取注解的属性方法。
     *
     * @param annotationType 注解类型
     * @return 属性方法数组
     */
    public static Method[] getAnnotationAttributes(final Class<? extends Annotation> annotationType) {
        return Stream.of(MethodKit.getDeclaredMethods(annotationType)).filter(AnnoKit::isAnnotationAttribute)
                .toArray(Method[]::new);
    }

    /**
     * 判断方法是否为注解属性方法。
     * <p>
     * 需满足以下条件：
     * <ul>
     * <li>非Object.equals方法</li>
     * <li>非Object.hashCode方法</li>
     * <li>非Object.toString方法</li>
     * <li>非桥接方法</li>
     * <li>非合成方法</li>
     * <li>非静态方法</li>
     * <li>公共方法</li>
     * <li>无参数</li>
     * <li>有返回值（非void）</li>
     * </ul>
     * </p>
     *
     * @param attribute 方法对象
     * @return 是否为注解属性方法
     */
    public static boolean isAnnotationAttribute(final Method attribute) {
        return !MethodKit.isEqualsMethod(attribute) && !MethodKit.isHashCodeMethod(attribute)
                && !MethodKit.isToStringMethod(attribute) && ArrayKit.isEmpty(attribute.getParameterTypes())
                && ObjectKit.notEquals(attribute.getReturnType(), Void.class)
                && !Modifier.isStatic(attribute.getModifiers()) && Modifier.isPublic(attribute.getModifiers())
                && !attribute.isBridge() && !attribute.isSynthetic();
    }

    /**
     * 清空注解相关缓存。
     */
    public static void clearCaches() {
        DECLARED_ANNOTATIONS_CACHE.clear();
    }

}