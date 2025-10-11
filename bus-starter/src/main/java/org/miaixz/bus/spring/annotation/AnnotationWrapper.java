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
package org.miaixz.bus.spring.annotation;

import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;

/**
 * A wrapper for annotations that allows their attributes to be resolved against a Spring {@link Environment}.
 * <p>
 * This class creates a dynamic proxy for an annotation, enabling placeholder resolution for its attributes. This is
 * useful for externalizing annotation attribute values into Spring's environment properties.
 *
 * @param <A> The type of the annotation being wrapped.
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnnotationWrapper<A extends Annotation> {

    private final Class<A> clazz;

    /**
     * The original annotation instance being delegated to.
     */
    private Annotation delegate;

    /**
     * The placeholder binder responsible for resolving annotation attribute values.
     */
    private PlaceHolderBinder binder;

    /**
     * The Spring environment used for resolving placeholders.
     */
    private Environment environment;

    /**
     * Private constructor to enforce factory method usage.
     *
     * @param clazz The class of the annotation to wrap.
     */
    private AnnotationWrapper(Class<A> clazz) {
        this.clazz = clazz;
    }

    /**
     * Factory method to create an {@code AnnotationWrapper} for a given annotation class.
     *
     * @param clazz The class of the annotation.
     * @param <A>   The type of the annotation.
     * @return A new {@code AnnotationWrapper} instance.
     */
    public static <A extends Annotation> AnnotationWrapper<A> of(Class<A> clazz) {
        return new AnnotationWrapper<>(clazz);
    }

    /**
     * Factory method to create an {@code AnnotationWrapper} for an existing annotation instance.
     *
     * @param annotation The annotation instance.
     * @param <A>        The type of the annotation.
     * @return A new {@code AnnotationWrapper} instance.
     */
    public static <A extends Annotation> AnnotationWrapper<A> of(A annotation) {
        return new AnnotationWrapper<>((Class<A>) annotation.annotationType());
    }

    /**
     * Sets the {@link PlaceHolderBinder} to be used for resolving annotation attributes.
     *
     * @param binder The {@link PlaceHolderBinder} instance.
     * @return This {@code AnnotationWrapper} instance for method chaining.
     */
    public AnnotationWrapper<A> withBinder(PlaceHolderBinder binder) {
        this.binder = binder;
        return this;
    }

    /**
     * Sets the Spring {@link Environment} to be used for resolving placeholders.
     *
     * @param environment The Spring {@link Environment} instance.
     * @return This {@code AnnotationWrapper} instance for method chaining.
     */
    public AnnotationWrapper<A> withEnvironment(Environment environment) {
        this.environment = environment;
        return this;
    }

    /**
     * Wraps the given annotation instance with a dynamic proxy that resolves its attributes.
     *
     * @param annotation The annotation instance to wrap.
     * @return A proxied annotation instance where attributes can be resolved against the environment.
     * @throws IllegalArgumentException if the provided annotation is null or not of the expected type.
     */
    public A wrap(A annotation) {
        Assert.notNull(annotation, "annotation must not be null.");
        Assert.isInstanceOf(clazz, annotation, "parameter must be annotation type.");
        this.delegate = annotation;
        return build();
    }

    /**
     * Builds the dynamic proxy for the annotation.
     *
     * @return The proxied annotation instance.
     */
    private A build() {
        ClassLoader cl = this.getClass().getClassLoader();
        Class<?>[] exposedInterface = { delegate.annotationType(), WrapperAnnotation.class };
        return (A) Proxy.newProxyInstance(cl, exposedInterface, new PlaceHolderHandler(delegate, binder, environment));
    }

}
