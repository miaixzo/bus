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
package org.miaixz.bus.spring.annotation;

import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;

/**
 * 用于将注解包装到spring环境
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnnotationWrapper<A extends Annotation> {

    private final Class<A> clazz;

    /**
     * 注解代理
     */
    private Annotation delegate;

    /**
     * 返回值信息处理
     */
    private PlaceHolderBinder binder;

    /**
     * 应用环境信息
     */
    private Environment environment;

    private AnnotationWrapper(Class<A> clazz) {
        this.clazz = clazz;
    }

    /**
     * 构造
     *
     * @param clazz Class对象
     * @param <A>   泛型对象
     * @return the object
     */
    public static <A extends Annotation> AnnotationWrapper<A> of(Class<A> clazz) {
        return new AnnotationWrapper<>(clazz);
    }

    /**
     * 构造
     *
     * @param annotation 注解
     * @param <A>        泛型对象
     * @return the object
     */
    public static <A extends Annotation> AnnotationWrapper<A> of(Annotation annotation) {
        return new AnnotationWrapper(annotation.getClass());
    }

    public AnnotationWrapper<A> withBinder(PlaceHolderBinder binder) {
        this.binder = binder;
        return this;
    }

    public AnnotationWrapper<A> withEnvironment(Environment environment) {
        this.environment = environment;
        return this;
    }

    public A wrap(A annotation) {
        Assert.notNull(annotation, "annotation must not be null.");
        Assert.isInstanceOf(clazz, annotation, "parameter must be annotation type.");
        this.delegate = annotation;
        return build();
    }

    private A build() {
        ClassLoader cl = this.getClass().getClassLoader();
        Class<?>[] exposedInterface = {delegate.annotationType(), WrapperAnnotation.class};
        return (A) Proxy.newProxyInstance(cl, exposedInterface,
                new PlaceHolderHandler(delegate, binder, environment));
    }

}
