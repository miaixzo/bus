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
package org.miaixz.bus.core.annotation.resolve;

import org.miaixz.bus.core.center.map.concurrent.SafeConcurrentHashMap;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.MethodKit;

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
 * 代理注解处理器，用于为{@link AnnotationMapping}生成代理对象，当从该代理对象上获取属性值时，
 * 总是通过{@link AnnotationMapping#getResolvedAttributeValue(String, Class)}获取。
 *
 * @param <T> 注解类型
 * @author Kimi Liu
 * @see AnnotationMapping
 * @since Java 17+
 */
public final class AnnotationMappingProxy<T extends Annotation> implements InvocationHandler {

    /**
     * 属性映射
     */
    private final AnnotationMapping<T> mapping;

    /**
     * 代理方法
     */
    private final Map<String, BiFunction<Method, Object[], Object>> methods;

    /**
     * 属性值缓存
     */
    private final Map<String, Object> valueCache;

    /**
     * 创建一个代理方法处理器
     *
     * @param annotation 属性映射
     */
    private AnnotationMappingProxy(final AnnotationMapping<T> annotation) {
        final int methodCount = annotation.getAttributes().length;
        this.methods = new HashMap<>(methodCount + 5);
        this.valueCache = new SafeConcurrentHashMap<>(methodCount);
        this.mapping = annotation;
        loadMethods();
    }

    /**
     * 创建一个代理对象
     *
     * @param annotationType 注解类型
     * @param mapping        注解映射对象
     * @param <A>            注解类型
     * @return 代理对象
     */
    public static <A extends Annotation> A create(final Class<? extends A> annotationType, final AnnotationMapping<A> mapping) {
        Objects.requireNonNull(annotationType);
        Objects.requireNonNull(mapping);
        final AnnotationMappingProxy<A> invocationHandler = new AnnotationMappingProxy<>(mapping);
        return (A) Proxy.newProxyInstance(
                annotationType.getClassLoader(),
                new Class[]{annotationType, Proxied.class},
                invocationHandler
        );
    }

    /**
     * 当前注解是否由当前代理类生成
     *
     * @param annotation 注解对象
     * @return 是否
     */
    public static boolean isProxied(final Annotation annotation) {
        return annotation instanceof Proxied;
    }

    /**
     * 调用被代理的方法
     *
     * @param proxy  代理对象
     * @param method 方法
     * @param args   参数
     * @return 返回值
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        return Optional.ofNullable(methods.get(method.getName()))
                .map(m -> m.apply(method, args))
                .orElseGet(() -> MethodKit.invoke(this, method, args));
    }

    /**
     * 预加载需要代理的方法
     */
    private void loadMethods() {
        methods.put(Normal.EQUALS, (method, args) -> proxyEquals(args[0]));
        methods.put(Normal.TOSTRING, (method, args) -> proxyToString());
        methods.put(Normal.HASHCODE, (method, args) -> proxyHashCode());
        methods.put("annotationType", (method, args) -> proxyAnnotationType());
        methods.put("getMapping", (method, args) -> proxyGetMapping());
        for (final Method attribute : mapping.getAttributes()) {
            methods.put(attribute.getName(), (method, args) -> getAttributeValue(method.getName(), method.getReturnType()));
        }
    }

    /**
     * 代理{@link Annotation#toString()}方法
     */
    private String proxyToString() {
        final String attributes = Stream.of(mapping.getAttributes())
                .map(attribute -> CharsBacker.format("{}={}", attribute.getName(), getAttributeValue(attribute.getName(), attribute.getReturnType())))
                .collect(Collectors.joining(", "));
        return CharsBacker.format("@{}({})", mapping.annotationType().getName(), attributes);
    }

    /**
     * 代理{@link Annotation#hashCode()}方法
     */
    private int proxyHashCode() {
        return this.hashCode();
    }

    /**
     * 代理{@link Annotation#equals(Object)}方法
     */
    private boolean proxyEquals(final Object o) {
        return Objects.equals(mapping, o);
    }

    /**
     * 代理{@link Annotation#annotationType()}方法
     */
    private Class<? extends Annotation> proxyAnnotationType() {
        return mapping.annotationType();
    }

    /**
     * 代理{@link Proxied#getMapping()}方法
     */
    private AnnotationMapping<T> proxyGetMapping() {
        return mapping;
    }

    /**
     * 获取属性值
     */
    private Object getAttributeValue(final String attributeName, final Class<?> attributeType) {
        return valueCache.computeIfAbsent(attributeName, name -> mapping.getResolvedAttributeValue(attributeName, attributeType));
    }

    /**
     * 表明注解是一个合成的注解
     */
    interface Proxied {

        /**
         * 获取注解映射对象
         *
         * @return 注解映射对象
         */
        AnnotationMapping<Annotation> getMapping();

    }

}
