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
package org.miaixz.bus.core.scanner;

import org.miaixz.bus.core.toolkit.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 合成注解代理类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
class SyntheticProxy implements InvocationHandler {

    private final Synthetic synthetic;
    private final Synthesized annotation;
    private final Map<String, BiFunction<Method, Object[], Object>> methods;

    SyntheticProxy(Synthetic synthetic, Synthesized annotation) {
        this.synthetic = synthetic;
        this.annotation = annotation;
        this.methods = new HashMap<>(9);
        loadMethods();
    }

    /**
     * 创建一个代理注解，生成的代理对象将是{@link Proxys}与指定的注解类的子类。
     * <ul>
     *     <li>当作为{@code annotationType}所指定的类型使用时，其属性将通过合成它的{@link Synthetic}获取；</li>
     *     <li>当作为{@link Proxys}或{@link Synthesized}使用时，将可以获得原始注解实例的相关信息；</li>
     * </ul>
     *
     * @param annotationType 注解类型
     * @param synthetic      合成注解
     * @return 代理注解
     */
    static <T extends Annotation> T create(
            Class<T> annotationType, Synthetic synthetic) {
        final Synthesized annotation = synthetic.getSynthesizedAnnotation(annotationType);
        final SyntheticProxy proxyHandler = new SyntheticProxy(synthetic, annotation);
        if (ObjectKit.isNull(annotation)) {
            return null;
        }
        return (T) Proxy.newProxyInstance(
                annotationType.getClassLoader(),
                new Class[]{annotationType, Proxys.class},
                proxyHandler
        );
    }

    static boolean isProxyAnnotation(Class<?> targetClass) {
        return ClassKit.isAssignable(Proxys.class, targetClass);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return Optional.ofNullable(methods.get(method.getName()))
                .map(m -> m.apply(method, args))
                .orElseGet(() -> ReflectKit.invoke(this, method, args));
    }

    void loadMethods() {
        methods.put("toString", (method, args) -> proxyToString());
        methods.put("hashCode", (method, args) -> proxyHashCode());
        methods.put("getSyntheticAnnotation", (method, args) -> proxyGetSyntheticAnnotation());
        methods.put("getSynthesizedAnnotation", (method, args) -> proxyGetSynthesizedAnnotation());
        methods.put("getRoot", (method, args) -> annotation.getRoot());
        methods.put("isRoot", (method, args) -> annotation.isRoot());
        methods.put("getVerticalDistance", (method, args) -> annotation.getVerticalDistance());
        methods.put("getHorizontalDistance", (method, args) -> annotation.getHorizontalDistance());
        methods.put("hasAttribute", (method, args) -> annotation.hasAttribute((String) args[0], (Class<?>) args[1]));
        methods.put("getAttribute", (method, args) -> annotation.getAttribute((String) args[0]));
        methods.put("annotationType", (method, args) -> annotation.annotationType());
        for (final Method declaredMethod : annotation.getAnnotation().annotationType().getDeclaredMethods()) {
            methods.put(declaredMethod.getName(), (method, args) -> proxyAttributeValue(method));
        }
    }

    private String proxyToString() {
        final String attributes = Stream.of(annotation.annotationType().getDeclaredMethods())
                .filter(AnnoKit::isAttributeMethod)
                .map(method -> StringKit.format("{}={}", method.getName(), synthetic.getAttribute(method.getName(), method.getReturnType())))
                .collect(Collectors.joining(", "));
        return StringKit.format("@{}({})", annotation.annotationType().getName(), attributes);
    }

    private int proxyHashCode() {
        return Objects.hash(synthetic, annotation);
    }

    private Object proxyGetSyntheticAnnotation() {
        return synthetic;
    }

    private Object proxyGetSynthesizedAnnotation() {
        return annotation;
    }

    private Object proxyAttributeValue(Method attributeMethod) {
        return synthetic.getAttribute(attributeMethod.getName(), attributeMethod.getReturnType());
    }

    /**
     * 通过代理类生成的合成注解
     */
    interface Proxys extends Synthesized {

        /**
         * 获取该注解所属的合成注解
         *
         * @return 合成注解
         */
        Synthetic getSyntheticAnnotation();

        /**
         * 获取该代理注解对应的已合成注解
         *
         * @return 理注解对应的已合成注解
         */
        Synthesized getSynthesizedAnnotation();

    }

}
