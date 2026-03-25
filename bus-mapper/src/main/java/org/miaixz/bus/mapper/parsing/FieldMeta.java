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
package org.miaixz.bus.mapper.parsing;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.miaixz.bus.mapper.builder.GenericTypeResolver;

/**
 * Represents the metadata of an entity class field, providing field-related operations similar to
 * {@link java.lang.reflect.Field}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FieldMeta {

    /**
     * The entity class type where this field is located.
     */
    protected Class<?> entityClass;

    /**
     * The corresponding Java field in the entity class (can be extended with method annotations).
     */
    protected Field field;

    /**
     * Default constructor.
     */
    public FieldMeta() {
    }

    /**
     * Constructs a new FieldMeta with entity class and field information.
     *
     * @param entityClass The entity class type.
     * @param field       The Java field.
     */
    public FieldMeta(Class<?> entityClass, Field field) {
        this.entityClass = entityClass;
        this.field = field;
        this.field.setAccessible(true);
    }

    /**
     * Gets the underlying {@link Field} object.
     *
     * @return The Java field.
     */
    public Field getField() {
        return field;
    }

    /**
     * Gets the class in which this field is declared.
     *
     * @return The declaring class of the field.
     */
    public Class<?> getDeclaringClass() {
        return field.getDeclaringClass();
    }

    /**
     * Gets the name of the field.
     *
     * @return The name of the field.
     */
    public String getName() {
        return field.getName();
    }

    /**
     * Gets the actual type of the field, resolving generics if necessary.
     *
     * @return The actual type of the field.
     */
    public Class<?> getType() {
        return GenericTypeResolver.resolveFieldClass(field, entityClass);
    }

    /**
     * Gets a specific annotation on the field.
     *
     * @param annotationClass The annotation type.
     * @param <T>             The generic type of the annotation.
     * @return The annotation instance of the specified type, or null if not present.
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return field.getAnnotation(annotationClass);
    }

    /**
     * Gets all annotations on the field.
     *
     * @return An array of annotations.
     */
    public Annotation[] getAnnotations() {
        return field.getAnnotations();
    }

    /**
     * Checks if a specific annotation is present on the field.
     *
     * @param annotationClass The annotation type.
     * @return {@code true} if the annotation is present, {@code false} otherwise.
     */
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return field.isAnnotationPresent(annotationClass);
    }

    /**
     * Gets the value of the field via reflection.
     *
     * @param object The object from which to get the field value.
     * @return The value of the field.
     * @throws RuntimeException if the reflection operation fails.
     */
    public Object get(Object object) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error getting field value by reflection", e);
        }
    }

    /**
     * Sets the value of the field via reflection.
     *
     * @param object The object on which to set the field value.
     * @param value  The new value for the field.
     * @throws RuntimeException if the reflection operation fails.
     */
    public void set(Object object, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error in reflection setting field value", e);
        }
    }

}
