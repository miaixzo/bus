/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2025 miaixz.org and other contributors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
 * 实体对象构建器，存储和提供 TableMeta 和 ColumnMeta 信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EntityClassBuilder {

    private static final Map<Class<?>, TableMeta> TABLE_META_STORE = new HashMap<>();
    private static final Map<Class<?>, List<ColumnMeta>> COLUMN_META_STORE = new HashMap<>();

    /**
     * 存储 TableMeta
     *
     * @param tableMeta 表元数据
     */
    public static void setTableMeta(TableMeta tableMeta) {
        synchronized (TABLE_META_STORE) {
            TABLE_META_STORE.put(tableMeta.entityClass(), tableMeta);
            COLUMN_META_STORE.computeIfAbsent(tableMeta.entityClass(), k -> new ArrayList<>());
        }
    }

    /**
     * 添加 ColumnMeta
     *
     * @param entityClass 实体类
     * @param columnMeta  列元数据
     * @throws RuntimeException 如果 TableMeta 未初始化
     */
    public static void setColumnMeta(Class<?> entityClass, ColumnMeta columnMeta) {
        synchronized (COLUMN_META_STORE) {
            List<ColumnMeta> columnMetas = COLUMN_META_STORE.computeIfAbsent(entityClass, k -> new ArrayList<>());
            columnMetas.add(columnMeta);
            TableMeta tableMeta = TABLE_META_STORE.get(entityClass);
            if (tableMeta == null) {
                throw new RuntimeException("TableMeta for " + entityClass.getName() + " not initialized");
            }
            tableMeta.addColumn(columnMeta);
        }
    }

    /**
     * 获取默认 TableMeta
     *
     * @return TableMeta
     * @throws RuntimeException 如果无 TableMeta 初始化
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
     * 获取指定实体类的 TableMeta
     *
     * @param entityClass 实体类
     * @return TableMeta
     * @throws RuntimeException 如果 TableMeta 未初始化
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
     * 获取默认 ColumnMeta 列表
     *
     * @return 列元数据列表
     * @throws RuntimeException 如果无 ColumnMeta 初始化
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
     * 获取指定实体类的 ColumnMeta 列表
     *
     * @param entityClass 实体类
     * @return 列元数据列表
     * @throws RuntimeException 如果 ColumnMeta 未初始化
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
     * 获取默认 TableMeta 上的指定注解
     *
     * @param annotationClass 注解类
     * @param <T>             注解类型
     * @return 注解列表
     * @throws RuntimeException 如果无 TableMeta 初始化
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
     * 获取指定实体类的 TableMeta 上的指定注解
     *
     * @param entityClass     实体类
     * @param annotationClass 注解类
     * @param <T>             注解类型
     * @return 注解列表
     * @throws RuntimeException 如果 TableMeta 未初始化
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
     * 获取默认 ColumnMeta 上的指定注解
     *
     * @param annotationClass 注解类
     * @param <T>             注解类型
     * @return 注解列表
     * @throws RuntimeException 如果无 ColumnMeta 初始化
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
     * 获取指定实体类的 ColumnMeta 上的指定注解
     *
     * @param entityClass     实体类
     * @param annotationClass 注解类
     * @param <T>             注解类型
     * @return 注解列表
     * @throws RuntimeException 如果 ColumnMeta 未初始化
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
     * 获取默认实体类中 Logical 注解的列名称
     *
     * @return 列名称
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
     * 从 TableMeta 提取指定注解
     *
     * @param tableMeta       TableMeta 对象
     * @param annotationClass 注解类
     * @param <T>             注解类型
     * @return 注解列表
     */
    private static <T extends Annotation> List<T> getAnnotationsFromTableMeta(TableMeta tableMeta,
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
     * 从 ColumnMeta 列表提取指定注解
     *
     * @param columnMetas     ColumnMeta 列表
     * @param annotationClass 注解类
     * @param <T>             注解类型
     * @return 注解列表
     */
    private static <T extends Annotation> List<T> getAnnotationsFromColumnMetas(List<ColumnMeta> columnMetas,
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