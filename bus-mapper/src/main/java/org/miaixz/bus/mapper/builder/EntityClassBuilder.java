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
package org.miaixz.bus.mapper.builder;

import java.lang.annotation.Annotation;
import java.util.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Logical;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.FieldMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * A builder for entity objects, which stores and provides {@link TableMeta} and {@link ColumnMeta} information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EntityClassBuilder {

    /**
     * A thread-safe store for table metadata, keyed by entity class.
     */
    private static final Map<Class<?>, TableMeta> TABLE_META_STORE = Collections.synchronizedMap(new WeakHashMap<>());
    /**
     * A thread-safe store for column metadata, keyed by entity class.
     */
    private static final Map<Class<?>, List<ColumnMeta>> COLUMN_META_STORE = Collections
            .synchronizedMap(new WeakHashMap<>());

    /**
     * Stores a {@link TableMeta} instance.
     *
     * @param tableMeta The table metadata to store.
     */
    public static void setTableMeta(TableMeta tableMeta) {
        TABLE_META_STORE.put(tableMeta.entityClass(), tableMeta);
        COLUMN_META_STORE.computeIfAbsent(tableMeta.entityClass(), k -> new ArrayList<>());
    }

    /**
     * Adds a {@link ColumnMeta} instance for a given entity class.
     *
     * @param entityClass The entity class.
     * @param columnMeta  The column metadata to add.
     * @throws RuntimeException if the {@link TableMeta} for the entity class has not been initialized.
     */
    public static void setColumnMeta(Class<?> entityClass, ColumnMeta columnMeta) {
        List<ColumnMeta> columnMetas = COLUMN_META_STORE.computeIfAbsent(entityClass, k -> new ArrayList<>());
        synchronized (columnMetas) {
            columnMetas.add(columnMeta);
        }
        TableMeta tableMeta = TABLE_META_STORE.get(entityClass);
        if (tableMeta == null) {
            throw new RuntimeException("TableMeta for " + entityClass.getName() + " not initialized");
        }
        tableMeta.addColumn(columnMeta);
    }

    /**
     * Gets the default {@link TableMeta}.
     *
     * @return The default {@link TableMeta}.
     * @throws RuntimeException if no {@link TableMeta} has been initialized.
     */
    public static TableMeta getTableMeta() {
        synchronized (TABLE_META_STORE) {
            if (TABLE_META_STORE.isEmpty()) {
                throw new RuntimeException("No TableMeta initialized");
            }
            return TABLE_META_STORE.values().iterator().next();
        }
    }

    /**
     * Gets the {@link TableMeta} for a specific entity class.
     *
     * @param entityClass The entity class.
     * @return The {@link TableMeta} for the specified class.
     * @throws RuntimeException if the {@link TableMeta} for the entity class has not been initialized.
     */
    public static TableMeta getTableMeta(Class<?> entityClass) {
        synchronized (TABLE_META_STORE) {
            TableMeta tableMeta = TABLE_META_STORE.get(entityClass);
            if (tableMeta == null) {
                throw new RuntimeException("TableMeta for " + entityClass.getName() + " not initialized");
            }
            return tableMeta;
        }
    }

    /**
     * Gets the list of default {@link ColumnMeta}.
     *
     * @return A list of default column metadata.
     * @throws RuntimeException if no {@link ColumnMeta} has been initialized.
     */
    public static List<ColumnMeta> getColumnMeta() {
        synchronized (COLUMN_META_STORE) {
            if (COLUMN_META_STORE.isEmpty()) {
                throw new RuntimeException("No ColumnMeta initialized");
            }
            return Collections.unmodifiableList(COLUMN_META_STORE.values().iterator().next());
        }
    }

    /**
     * Gets the list of {@link ColumnMeta} for a specific entity class.
     *
     * @param entityClass The entity class.
     * @return A list of column metadata for the specified class.
     * @throws RuntimeException if the {@link ColumnMeta} for the entity class has not been initialized.
     */
    public static List<ColumnMeta> getColumnMeta(Class<?> entityClass) {
        synchronized (COLUMN_META_STORE) {
            List<ColumnMeta> columnMetas = COLUMN_META_STORE.get(entityClass);
            if (columnMetas == null) {
                throw new RuntimeException("ColumnMeta for " + entityClass.getName() + " not initialized");
            }
            return Collections.unmodifiableList(columnMetas);
        }
    }

    /**
     * Gets specified annotations from the default {@link TableMeta}.
     *
     * @param annotationClass The annotation class to retrieve.
     * @param <T>             The type of the annotation.
     * @return A list of found annotations.
     * @throws RuntimeException if no {@link TableMeta} has been initialized.
     */
    public static <T extends Annotation> List<T> getTableAnnotations(Class<T> annotationClass) {
        synchronized (TABLE_META_STORE) {
            if (TABLE_META_STORE.isEmpty()) {
                throw new RuntimeException("No TableMeta initialized");
            }
            return getAnnotationsFromTableMeta(TABLE_META_STORE.values().iterator().next(), annotationClass);
        }
    }

    /**
     * Gets specified annotations from the {@link TableMeta} of a specific entity class.
     *
     * @param entityClass     The entity class.
     * @param annotationClass The annotation class to retrieve.
     * @param <T>             The type of the annotation.
     * @return A list of found annotations.
     * @throws RuntimeException if the {@link TableMeta} for the entity class has not been initialized.
     */
    public static <T extends Annotation> List<T> getTableAnnotations(Class<?> entityClass, Class<T> annotationClass) {
        synchronized (TABLE_META_STORE) {
            TableMeta tableMeta = TABLE_META_STORE.get(entityClass);
            if (tableMeta == null) {
                throw new RuntimeException("TableMeta for " + entityClass.getName() + " not initialized");
            }
            return getAnnotationsFromTableMeta(tableMeta, annotationClass);
        }
    }

    /**
     * Gets specified annotations from the default {@link ColumnMeta} list.
     *
     * @param annotationClass The annotation class to retrieve.
     * @param <T>             The type of the annotation.
     * @return A list of found annotations.
     * @throws RuntimeException if no {@link ColumnMeta} has been initialized.
     */
    public static <T extends Annotation> List<T> getColumnAnnotations(Class<T> annotationClass) {
        synchronized (COLUMN_META_STORE) {
            if (COLUMN_META_STORE.isEmpty()) {
                throw new RuntimeException("No ColumnMeta initialized");
            }
            return getAnnotationsFromColumnMetas(COLUMN_META_STORE.values().iterator().next(), annotationClass);
        }
    }

    /**
     * Gets specified annotations from the {@link ColumnMeta} list of a specific entity class.
     *
     * @param entityClass     The entity class.
     * @param annotationClass The annotation class to retrieve.
     * @param <T>             The type of the annotation.
     * @return A list of found annotations.
     * @throws RuntimeException if the {@link ColumnMeta} for the entity class has not been initialized.
     */
    public static <T extends Annotation> List<T> getColumnAnnotations(Class<?> entityClass, Class<T> annotationClass) {
        synchronized (COLUMN_META_STORE) {
            List<ColumnMeta> columnMetas = COLUMN_META_STORE.get(entityClass);
            if (columnMetas == null) {
                throw new RuntimeException("ColumnMeta for " + entityClass.getName() + " not initialized");
            }
            return getAnnotationsFromColumnMetas(columnMetas, annotationClass);
        }
    }

    /**
     * Gets the column name associated with the {@link Logical} annotation in the default entity class.
     *
     * @return The logical delete column name, or an empty string if not found.
     */
    public static String getTableLogicColumn() {
        synchronized (COLUMN_META_STORE) {
            if (COLUMN_META_STORE.isEmpty()) {
                return Normal.EMPTY;
            }
            for (ColumnMeta columnMeta : COLUMN_META_STORE.values().iterator().next()) {
                FieldMeta fieldMeta = columnMeta.fieldMeta();
                if (fieldMeta != null && fieldMeta.isAnnotationPresent(Logical.class)) {
                    return columnMeta.column();
                }
            }
            return Normal.EMPTY;
        }
    }

    /**
     * Extracts specified annotations from a {@link TableMeta} object.
     *
     * @param tableMeta       The {@link TableMeta} object.
     * @param annotationClass The annotation class to extract.
     * @param <T>             The type of the annotation.
     * @return A list of found annotations.
     */
    private static <T extends Annotation> List<T> getAnnotationsFromTableMeta(
            TableMeta tableMeta,
            Class<T> annotationClass) {
        List<T> annotations = new ArrayList<>();
        if (tableMeta != null && tableMeta.entityClass().isAnnotationPresent(annotationClass)) {
            T annotation = tableMeta.entityClass().getAnnotation(annotationClass);
            if (annotation != null) {
                annotations.add(annotation);
            }
        }
        return Collections.unmodifiableList(annotations);
    }

    /**
     * Extracts specified annotations from a list of {@link ColumnMeta} objects.
     *
     * @param columnMetas     The list of {@link ColumnMeta} objects.
     * @param annotationClass The annotation class to extract.
     * @param <T>             The type of the annotation.
     * @return A list of found annotations.
     */
    private static <T extends Annotation> List<T> getAnnotationsFromColumnMetas(
            List<ColumnMeta> columnMetas,
            Class<T> annotationClass) {
        List<T> annotations = new ArrayList<>();
        for (ColumnMeta columnMeta : columnMetas) {
            FieldMeta fieldMeta = columnMeta.fieldMeta();
            if (fieldMeta != null && fieldMeta.isAnnotationPresent(annotationClass)) {
                T annotation = fieldMeta.getAnnotation(annotationClass);
                if (annotation != null) {
                    annotations.add(annotation);
                }
            }
        }
        return Collections.unmodifiableList(annotations);
    }

}
