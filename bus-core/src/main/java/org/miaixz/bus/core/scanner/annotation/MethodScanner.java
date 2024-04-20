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
package org.miaixz.bus.core.scanner.annotation;

import org.miaixz.bus.core.scanner.AnnotationScanner;
import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.ClassKit;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * 扫描{@link Method}上的注解
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MethodScanner extends AbstractTypeScanner<MethodScanner> implements AnnotationScanner {

    /**
     * 构造一个方法注解扫描器
     *
     * @param scanSameSignatureMethod 是否扫描类层级结构中具有相同方法签名的方法
     * @param filter                  过滤器
     * @param excludeTypes            不包含的类型
     */
    public MethodScanner(boolean scanSameSignatureMethod, Predicate<Class<?>> filter, Set<Class<?>> excludeTypes) {
        super(scanSameSignatureMethod, scanSameSignatureMethod, filter, excludeTypes);
    }

    /**
     * 构造一个类注解扫描器
     *
     * @param scanSameSignatureMethod 是否扫描类层级结构中具有相同方法签名的方法
     */
    public MethodScanner(boolean scanSameSignatureMethod) {
        this(scanSameSignatureMethod, targetClass -> true, CollKit.newLinkedHashSet());
    }

    /**
     * 构造一个类注解扫描器，仅扫描该方法上直接声明的注解
     */
    public MethodScanner() {
        this(false);
    }

    /**
     * 判断是否支持扫描该注解元素，仅当注解元素是{@link Method}时返回{@code true}
     *
     * @param annotatedEle {@link AnnotatedElement}，可以是Class、Method、Field、Constructor、ReflectPermission
     * @return boolean 是否支持扫描该注解元素
     */
    @Override
    public boolean support(AnnotatedElement annotatedEle) {
        return annotatedEle instanceof Method;
    }

    /**
     * 获取声明该方法的类
     *
     * @param annotatedElement 注解元素
     * @return 要递归的类型
     * @see Method#getDeclaringClass()
     */
    @Override
    protected Class<?> getClassFormAnnotatedElement(AnnotatedElement annotatedElement) {
        return ((Method) annotatedElement).getDeclaringClass();
    }

    /**
     * 若父类/父接口中方法具有相同的方法签名，则返回该方法上的注解
     *
     * @param source      原始方法
     * @param index       类的层级索引
     * @param targetClass 类
     * @return 最终所需的目标注解
     */
    @Override
    protected Annotation[] getAnnotationsFromTargetClass(AnnotatedElement source, int index, Class<?> targetClass) {
        final Method sourceMethod = (Method) source;
        return Stream.of(targetClass.getDeclaredMethods())
                .filter(superMethod -> !superMethod.isBridge())
                .filter(superMethod -> hasSameSignature(sourceMethod, superMethod))
                .map(AnnotatedElement::getAnnotations)
                .flatMap(Stream::of)
                .toArray(Annotation[]::new);
    }

    /**
     * 设置是否扫描类层级结构中具有相同方法签名的方法
     *
     * @param scanSuperMethodIfOverride 是否扫描类层级结构中具有相同方法签名的方法
     * @return 当前实例
     */
    public MethodScanner setScanSameSignatureMethod(boolean scanSuperMethodIfOverride) {
        setIncludeInterfaces(scanSuperMethodIfOverride);
        setIncludeSupperClass(scanSuperMethodIfOverride);
        return this;
    }

    /**
     * 该方法是否具备与扫描的方法相同的方法签名
     */
    private boolean hasSameSignature(Method sourceMethod, Method superMethod) {
        if (false == StringKit.equals(sourceMethod.getName(), superMethod.getName())) {
            return false;
        }
        final Class<?>[] sourceParameterTypes = sourceMethod.getParameterTypes();
        final Class<?>[] targetParameterTypes = superMethod.getParameterTypes();
        if (sourceParameterTypes.length != targetParameterTypes.length) {
            return false;
        }
        if (!ArrayKit.containsAll(sourceParameterTypes, targetParameterTypes)) {
            return false;
        }
        return ClassKit.isAssignable(superMethod.getReturnType(), sourceMethod.getReturnType());
    }

}
