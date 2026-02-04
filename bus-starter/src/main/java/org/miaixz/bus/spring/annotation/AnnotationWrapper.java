/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
