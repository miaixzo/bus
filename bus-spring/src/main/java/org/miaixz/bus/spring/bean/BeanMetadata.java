/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.bean;

import java.util.Objects;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.MethodMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.StandardMethodMetadata;

/**
 * Side-effect-free Bean definition type and source inspection.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class BeanMetadata {

    /**
     * Creates a side-effect-free Bean metadata inspector.
     */
    public BeanMetadata() {
        // No initialization required.
    }

    /**
     * Resolves an already available Bean or FactoryBean product type.
     *
     * <p>
     * String class names are deliberately not loaded: an optional type that is absent from the classpath remains
     * unresolved instead of raising a linkage error.
     * </p>
     *
     * @param definition Bean definition to inspect
     * @return resolved Bean or FactoryBean product type, or {@code null} when unavailable without class loading
     */
    public Class<?> resolveBeanClassType(BeanDefinition definition) {
        Objects.requireNonNull(definition, "definition");

        Class<?> objectType = resolveObjectTypeAttribute(definition);
        if (objectType != null) {
            return objectType;
        }

        Class<?> resolved = resolveResolvableType(definition.getResolvableType());
        if (resolved != null) {
            return resolved;
        }

        if (definition instanceof AnnotatedBeanDefinition annotated) {
            MethodMetadata method = annotated.getFactoryMethodMetadata();
            if (method instanceof StandardMethodMetadata standardMethod) {
                return standardMethod.getIntrospectedMethod().getReturnType();
            }
            AnnotationMetadata metadata = annotated.getMetadata();
            if (metadata instanceof StandardAnnotationMetadata standardMetadata) {
                return factoryProductTypeOrClass(standardMetadata.getIntrospectedClass());
            }
        }

        if (definition instanceof AbstractBeanDefinition abstractDefinition && abstractDefinition.hasBeanClass()) {
            return factoryProductTypeOrClass(abstractDefinition.getBeanClass());
        }
        return null;
    }

    /**
     * Returns whether a definition was created from configuration-class method metadata.
     *
     * @param definition Bean definition to inspect
     * @return {@code true} when factory-method metadata identifies a configuration source
     */
    public boolean isFromConfigurationSource(BeanDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        return definition instanceof AnnotatedBeanDefinition annotated && annotated.getFactoryMethodMetadata() != null;
    }

    /**
     * Returns whether the resolvable definition class implements {@link FactoryBean}.
     *
     * @param definition Bean definition to inspect
     * @return {@code true} when the raw definition type implements {@link FactoryBean}
     */
    public boolean isFactoryBean(BeanDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        Class<?> type = rawDefinitionClass(definition);
        return type != null && FactoryBean.class.isAssignableFrom(type);
    }

    /**
     * Resolves the standard FactoryBean object-type attribute.
     *
     * @param definition Bean definition carrying the attribute
     * @return resolved object type, or {@code null} when the attribute is absent or unresolved
     */
    private static Class<?> resolveObjectTypeAttribute(BeanDefinition definition) {
        Object attribute = definition.getAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE);
        if (attribute instanceof Class<?> type) {
            return type;
        }
        if (attribute instanceof ResolvableType type) {
            return useful(type.resolve());
        }
        return null;
    }

    /**
     * Resolves a Bean type without returning the FactoryBean implementation itself.
     *
     * @param type definition resolvable type
     * @return resolved Bean type, or {@code null} when unavailable
     */
    private static Class<?> resolveResolvableType(ResolvableType type) {
        if (type == ResolvableType.NONE) {
            return null;
        }
        Class<?> resolved = type.resolve();
        if (resolved == null) {
            return null;
        }
        if (!FactoryBean.class.isAssignableFrom(resolved)) {
            return useful(resolved);
        }
        return useful(type.as(FactoryBean.class).getGeneric(0).resolve());
    }

    /**
     * Resolves the raw implementation class recorded by a definition.
     *
     * @param definition Bean definition to inspect
     * @return raw definition class, or {@code null} when unresolved
     */
    private static Class<?> rawDefinitionClass(BeanDefinition definition) {
        if (definition instanceof AnnotatedBeanDefinition annotated
                && annotated.getMetadata() instanceof StandardAnnotationMetadata standardMetadata) {
            return standardMetadata.getIntrospectedClass();
        }
        if (definition instanceof AbstractBeanDefinition abstractDefinition && abstractDefinition.hasBeanClass()) {
            return abstractDefinition.getBeanClass();
        }
        return definition.getResolvableType().resolve();
    }

    /**
     * Resolves a FactoryBean product type, otherwise returns the supplied class.
     *
     * @param type raw definition class
     * @return product or original class, or {@code null} for an unresolved FactoryBean product
     */
    private static Class<?> factoryProductTypeOrClass(Class<?> type) {
        if (!FactoryBean.class.isAssignableFrom(type)) {
            return type;
        }
        return useful(ResolvableType.forClass(type).as(FactoryBean.class).getGeneric(0).resolve());
    }

    /**
     * Converts the non-specific {@link Object} type into an unresolved result.
     *
     * @param type candidate type
     * @return candidate type, or {@code null} when it is {@link Object}
     */
    private static Class<?> useful(Class<?> type) {
        return type == Object.class ? null : type;
    }

}
