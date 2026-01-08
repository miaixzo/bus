/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.sensitive;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.sensitive.magic.annotation.*;
import org.miaixz.bus.sensitive.metric.BuiltInProvider;
import org.miaixz.bus.sensitive.metric.ConditionProvider;
import org.miaixz.bus.sensitive.metric.StrategyProvider;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A provider for processing sensitive data. It offers object desensitization capabilities, including support for deep
 * cloning and JSON serialization. It is implemented using reflection to ensure flexibility and performance.
 *
 * @param <T> The type of the object to be processed.
 * @author Kimi Liu
 * @since Java 17+
 */
public class Provider<T> {

    /** An array of specific field names to desensitize. If not null, only these fields are considered. */
    private String[] value;

    /**
     * Performs a deep copy of a serializable object.
     *
     * @param object The object to clone.
     * @param <T>    The type of the object.
     * @return A deep copy of the object.
     */
    public static <T> T clone(T object) {
        if (object == null) {
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (T) ois.readObject();
            }
        } catch (Exception e) {
            throw new InternalException("Deep clone failed: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if an object's string representation appears to have already been desensitized (i.e., contains a star
     * '*').
     *
     * @param object The object to check.
     * @return {@code true} if the object's string representation contains a star, {@code false} otherwise.
     */
    public static boolean alreadyBeSentisived(Object object) {
        return object != null && object.toString().contains("*");
    }

    /**
     * Applies desensitization to the given object.
     *
     * @param object     The original object.
     * @param annotation The annotation providing context for the operation (e.g., {@link Sensitive}).
     * @param clone      If true, a deep copy of the object is created before desensitization.
     * @return The desensitized object.
     */
    public T on(T object, Annotation annotation, boolean clone) {
        if (ObjectKit.isEmpty(object)) {
            return object;
        }

        if (ObjectKit.isNotEmpty(annotation)) {
            Sensitive sensitive = (Sensitive) annotation;
            this.value = sensitive.field();
        }

        final Context context = new Context();

        T result = clone ? clone(object) : object;
        handleClassField(context, result, result.getClass());
        return result;
    }

    /**
     * Serializes the object to a JSON string after applying desensitization.
     *
     * @param object     The object to process and serialize.
     * @param annotation The annotation providing context for the operation (e.g., {@link Sensitive}).
     * @return The desensitized JSON string.
     */
    public String json(T object, Annotation annotation) {
        if (ObjectKit.isEmpty(object)) {
            return JsonKit.toJsonString(null);
        }

        if (ObjectKit.isNotEmpty(annotation)) {
            Sensitive sensitive = (Sensitive) annotation;
            this.value = sensitive.field();
        }

        final Context context = new Context();
        T copy = clone(object);
        handleClassField(context, copy, copy.getClass());
        return JsonKit.toJsonString(copy);
    }

    /**
     * Recursively handles desensitization for all fields of a given class.
     *
     * @param context    The current desensitization context.
     * @param copyObject The object to process (may be a clone).
     * @param clazz      The class of the object.
     */
    private void handleClassField(final Context context, final Object copyObject, final Class<?> clazz) {
        if (copyObject == null)
            return;
        List<Field> fieldList = ListKit.of(FieldKit.getFields(clazz));
        context.setAllFieldList(fieldList);
        context.setCurrentObject(copyObject);

        try {
            for (Field field : fieldList) {
                if (ArrayKit.isNotEmpty(this.value) && !Arrays.asList(this.value).contains(field.getName())) {
                    continue;
                }

                field.setAccessible(true);
                final Class<?> fieldTypeClass = field.getType();
                context.setCurrentField(field);

                Entry sensitiveEntry = field.getAnnotation(Entry.class);
                if (ObjectKit.isNotNull(sensitiveEntry)) {
                    Object fieldObject = field.get(copyObject);
                    if (fieldObject == null)
                        continue;

                    if (TypeKit.isJavaBean(fieldTypeClass)) {
                        handleClassField(context, fieldObject, fieldTypeClass);
                    } else if (TypeKit.isArray(fieldTypeClass)) {
                        processArrayField(context, copyObject, field);
                    } else if (TypeKit.isCollection(fieldTypeClass)) {
                        processCollectionField(context, copyObject, field);
                    } else {
                        handleSensitive(context, copyObject, field);
                    }
                } else {
                    handleSensitive(context, copyObject, field);
                }
            }
        } catch (IllegalAccessException e) {
            throw new InternalException("Field access failed: " + e.getMessage(), e);
        }
    }

    /**
     * Processes an array field, applying desensitization to its elements.
     *
     * @param context    The current desensitization context.
     * @param copyObject The parent object containing the array field.
     * @param field      The array field.
     * @throws IllegalAccessException if field access fails.
     */
    private void processArrayField(final Context context, final Object copyObject, final Field field)
            throws IllegalAccessException {
        Object[] arrays = (Object[]) field.get(copyObject);
        if (ArrayKit.isEmpty(arrays)) {
            return;
        }

        Object firstArrayEntry = ArrayKit.firstNonNull(arrays);
        if (firstArrayEntry == null) {
            return;
        }

        final Class<?> entryFieldClass = firstArrayEntry.getClass();
        if (needHandleEntryType(entryFieldClass)) {
            for (Object arrayEntry : arrays) {
                handleClassField(context, arrayEntry, entryFieldClass);
            }
        } else {
            Object newArray = Array.newInstance(entryFieldClass, arrays.length);
            for (int i = 0; i < arrays.length; i++) {
                Object result = handleSensitiveEntry(context, arrays[i], field);
                Array.set(newArray, i, result);
            }
            field.set(copyObject, newArray);
        }
    }

    /**
     * Processes a collection field, applying desensitization to its elements.
     *
     * @param context    The current desensitization context.
     * @param copyObject The parent object containing the collection field.
     * @param field      The collection field.
     * @throws IllegalAccessException if field access fails.
     */
    private void processCollectionField(final Context context, final Object copyObject, final Field field)
            throws IllegalAccessException {
        Collection<Object> entryCollection = (Collection<Object>) field.get(copyObject);
        if (CollKit.isEmpty(entryCollection)) {
            return;
        }

        Object firstCollectionEntry = entryCollection.iterator().next();
        if (firstCollectionEntry == null)
            return;
        Class<?> collectionEntryClass = firstCollectionEntry.getClass();

        if (needHandleEntryType(collectionEntryClass)) {
            for (Object collectionEntry : entryCollection) {
                handleClassField(context, collectionEntry, collectionEntryClass);
            }
        } else {
            List<Object> newResultList = new ArrayList<>(entryCollection.size());
            for (Object entry : entryCollection) {
                newResultList.add(handleSensitiveEntry(context, entry, field));
            }
            // Note: This replaces the original collection.
            field.set(copyObject, newResultList);
        }
    }

    /**
     * Handles desensitization for a single element within a collection or array.
     *
     * @param context The desensitization context.
     * @param entry   The element to process.
     * @param field   The field containing the collection/array.
     * @return The desensitized element.
     */
    private Object handleSensitiveEntry(final Context context, final Object entry, final Field field) {
        try {
            Shield sensitive = field.getAnnotation(Shield.class);
            if (ObjectKit.isNotNull(sensitive)) {
                ConditionProvider condition = ReflectKit.newInstance(sensitive.condition());
                if (condition.valid(context)) {
                    context.setShield(sensitive);
                    StrategyProvider strategy = ReflectKit.newInstance(sensitive.strategy());
                    return strategy.build(entry, context);
                }
            }

            Annotation[] annotations = field.getAnnotations();
            if (ArrayKit.isNotEmpty(annotations)) {
                ConditionProvider condition = getCondition(annotations);
                if (ObjectKit.isNull(condition) || condition.valid(context)) {
                    StrategyProvider strategy = getStrategy(annotations);
                    if (ObjectKit.isNotNull(strategy)) {
                        return strategy.build(entry, context);
                    }
                }
            }
            return entry;
        } catch (Exception e) {
            throw new InternalException("Desensitization failed: " + e.getMessage(), e);
        }
    }

    /**
     * Applies the desensitization logic to a single field.
     *
     * @param context    The desensitization context.
     * @param copyObject The object containing the field.
     * @param field      The field to desensitize.
     */
    private void handleSensitive(final Context context, final Object copyObject, final Field field) {
        try {
            Shield sensitive = field.getAnnotation(Shield.class);
            if (ObjectKit.isNotNull(sensitive)) {
                ConditionProvider condition = ReflectKit.newInstance(sensitive.condition());
                if (condition.valid(context)) {
                    context.setShield(sensitive);
                    StrategyProvider strategy = ReflectKit.newInstance(sensitive.strategy());
                    final Object originalFieldVal = field.get(copyObject);
                    final Object result = strategy.build(originalFieldVal, context);
                    field.set(copyObject, result);
                    return; // Avoid processing other annotations if @Shield is present and applied.
                }
            }

            Annotation[] annotations = field.getAnnotations();
            if (ArrayKit.isNotEmpty(annotations)) {
                ConditionProvider condition = getCondition(annotations);
                if (ObjectKit.isNull(condition) || condition.valid(context)) {
                    StrategyProvider strategy = getStrategy(annotations);
                    if (ObjectKit.isNotNull(strategy)) {
                        final Object originalFieldVal = field.get(copyObject);
                        final Object result = strategy.build(originalFieldVal, context);
                        field.set(copyObject, result);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Gets the strategy provider from a field's annotations.
     *
     * @param annotations The array of annotations on a field.
     * @return The appropriate strategy provider, or null if none is found.
     */
    private StrategyProvider getStrategy(final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Strategy sensitiveStrategy = annotation.annotationType().getAnnotation(Strategy.class);
            if (ObjectKit.isNotEmpty(sensitiveStrategy)) {
                Class<? extends StrategyProvider> clazz = sensitiveStrategy.value();
                if (BuiltInProvider.class.equals(clazz)) {
                    return Registry.require(annotation.annotationType());
                }
                return ReflectKit.newInstance(clazz);
            }
        }
        return null;
    }

    /**
     * Gets a custom condition provider from a field's annotations.
     *
     * @param annotations The array of annotations on a field.
     * @return The condition provider, or null if none is found.
     */
    private ConditionProvider getCondition(final Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            Condition condition = annotation.annotationType().getAnnotation(Condition.class);
            if (ObjectKit.isNotNull(condition)) {
                return ReflectKit.newInstance(condition.value());
            }
        }
        return null;
    }

    /**
     * Checks if a field type is a complex type that needs recursive handling.
     *
     * @param fieldTypeClass The class of the field type.
     * @return {@code true} if the type should be handled recursively, {@code false} otherwise.
     */
    private boolean needHandleEntryType(final Class<?> fieldTypeClass) {
        return (TypeKit.isJavaBean(fieldTypeClass) || TypeKit.isArray(fieldTypeClass)
                || TypeKit.isCollection(fieldTypeClass)) && !TypeKit.isBase(fieldTypeClass)
                && !TypeKit.isMap(fieldTypeClass);
    }

}
