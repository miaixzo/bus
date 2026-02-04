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
package org.miaixz.bus.sensitive;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.sensitive.magic.annotation.Condition;
import org.miaixz.bus.sensitive.magic.annotation.Entry;
import org.miaixz.bus.sensitive.magic.annotation.Shield;
import org.miaixz.bus.sensitive.metric.ConditionProvider;
import org.miaixz.bus.sensitive.metric.StrategyProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A context-aware filter for processing sensitive data. It handles the desensitization logic for fields annotated with
 * {@link Entry} or {@link Shield} and is designed to be independent of third-party JSON libraries.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Filter {

    /** The desensitization context. */
    private final Context sensitiveContext;

    /**
     * Constructs a new filter with the given context.
     *
     * @param context The desensitization context.
     */
    public Filter(Context context) {
        this.sensitiveContext = context;
    }

    /**
     * Extracts a custom condition provider from an array of annotations.
     *
     * @param annotations The array of annotations on a field.
     * @return An instance of the condition provider, or null if none is found.
     */
    private static ConditionProvider getConditionOpt(final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Condition sensitiveCondition = annotation.annotationType().getAnnotation(Condition.class);
            if (ObjectKit.isNotNull(sensitiveCondition)) {
                return ReflectKit.newInstance(sensitiveCondition.value());
            }
        }
        return null;
    }

    /**
     * Processes a specific field of an object for desensitization.
     *
     * @param object The object being processed.
     * @param field  The field to process.
     * @param value  The current value of the field.
     * @return The desensitized value.
     */
    public Object process(Object object, Field field, Object value) {
        sensitiveContext.setCurrentField(field);
        sensitiveContext.setCurrentObject(object);
        sensitiveContext.setBeanClass(object.getClass());
        sensitiveContext.setAllFieldList(ListKit.of(FieldKit.getFields(object.getClass())));

        Entry sensitiveEntry = field.getAnnotation(Entry.class);
        if (ObjectKit.isNull(sensitiveEntry)) {
            sensitiveContext.setEntry(value);
            return handleSensitive(sensitiveContext, field);
        }

        final Class<?> fieldTypeClass = field.getType();
        if (TypeKit.isJavaBean(fieldTypeClass) || TypeKit.isMap(fieldTypeClass)) {
            return value; // Skip recursive processing for JavaBeans and Maps here.
        }

        if (TypeKit.isArray(fieldTypeClass)) {
            return processArray(fieldTypeClass, (Object[]) value);
        }

        if (TypeKit.isCollection(fieldTypeClass)) {
            return processCollection((Collection<?>) value);
        }

        return value;
    }

    /**
     * Handles desensitization for array types.
     *
     * @param fieldTypeClass The class type of the array.
     * @param arrays         The array to process.
     * @return The processed array with desensitized elements.
     */
    private Object processArray(Class<?> fieldTypeClass, Object[] arrays) {
        if (ArrayKit.isEmpty(arrays)) {
            return arrays;
        }

        Object firstArrayEntry = ArrayKit.firstNonNull(arrays);
        if (firstArrayEntry == null) {
            return arrays;
        }

        final Class<?> entryFieldClass = firstArrayEntry.getClass();
        if (isBaseType(entryFieldClass)) {
            Object newArray = Array.newInstance(entryFieldClass, arrays.length);
            for (int i = 0; i < arrays.length; i++) {
                sensitiveContext.setEntry(arrays[i]);
                Array.set(newArray, i, handleSensitive(sensitiveContext, sensitiveContext.getCurrentField()));
            }
            return newArray;
        }
        return arrays;
    }

    /**
     * Handles desensitization for collection types.
     *
     * @param collection The collection to process.
     * @return The processed collection with desensitized elements.
     */
    private Object processCollection(Collection<?> collection) {
        if (CollKit.isEmpty(collection)) {
            return collection;
        }

        Object firstCollectionEntry = ArrayKit.firstNonNull(collection);
        if (firstCollectionEntry == null) {
            return collection;
        }

        if (isBaseType(firstCollectionEntry.getClass())) {
            List<Object> newResultList = new ArrayList<>(collection.size());
            for (Object entry : collection) {
                sensitiveContext.setEntry(entry);
                newResultList.add(handleSensitive(sensitiveContext, sensitiveContext.getCurrentField()));
            }
            return newResultList;
        }
        return collection;
    }

    /**
     * Applies the desensitization logic to a field's value based on its annotations.
     *
     * @param context The current desensitization context.
     * @param field   The field being processed.
     * @return The desensitized value.
     */
    private Object handleSensitive(final Context context, final Field field) {
        try {
            final Object originalFieldVal = context.getEntry();

            Shield sensitive = field.getAnnotation(Shield.class);
            if (ObjectKit.isNotNull(sensitive)) {
                ConditionProvider condition = ReflectKit.newInstance(sensitive.condition());
                if (condition.valid(context)) {
                    StrategyProvider strategy = Registry.require(sensitive.type());
                    if (ObjectKit.isEmpty(strategy)) {
                        strategy = ReflectKit.newInstance(sensitive.strategy());
                    }
                    context.setEntry(null);
                    return strategy.build(originalFieldVal, context);
                }
            }

            Annotation[] annotations = field.getAnnotations();
            if (ArrayKit.isNotEmpty(annotations)) {
                ConditionProvider condition = getConditionOpt(annotations);
                if (ObjectKit.isNotEmpty(condition) && condition.valid(context)) {
                    StrategyProvider strategy = Registry.require(annotations);
                    if (ObjectKit.isNotEmpty(strategy)) {
                        context.setEntry(null);
                        return strategy.build(originalFieldVal, context);
                    }
                }
            }
            context.setEntry(null);
            return originalFieldVal;
        } catch (Exception e) {
            throw new InternalException("Desensitization failed: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if the given class is a base type that should be processed directly (i.e., not recursively explored as a
     * complex object).
     *
     * @param fieldTypeClass The class of the field type.
     * @return {@code true} if it is a base type, {@code false} otherwise.
     */
    private boolean isBaseType(final Class<?> fieldTypeClass) {
        return TypeKit.isBase(fieldTypeClass) && !TypeKit.isJavaBean(fieldTypeClass) && !TypeKit.isArray(fieldTypeClass)
                && !TypeKit.isCollection(fieldTypeClass) && !TypeKit.isMap(fieldTypeClass);
    }

}
