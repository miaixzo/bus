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
package org.miaixz.bus.mapper.parsing;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.ModifierKit;
import org.miaixz.bus.mapper.Holder;
import org.miaixz.bus.mapper.builder.ClassMetaResolver;

/**
 * A factory for creating and managing entity class metadata.
 *
 * <p>
 * This factory uses bus-core utilities (FieldKit, ModifierKit) for high-performance reflection operations with built-in
 * caching.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class MapperFactory {

    /**
     * Gets the entity metadata for a given mapper interface and method.
     *
     * @param mapperType   The Mapper interface.
     * @param mapperMethod The Mapper method.
     * @return The entity class metadata.
     * @throws RuntimeException if the corresponding entity class cannot be determined.
     */
    public static TableMeta of(Class<?> mapperType, Method mapperMethod) {
        Optional<Class<?>> optionalClass = ClassMetaResolver.find(mapperType, mapperMethod);
        if (optionalClass.isPresent()) {
            return of(optionalClass.getOrNull());
        }
        throw new RuntimeException("Can't obtain " + (mapperMethod != null ? mapperMethod.getName() + " method"
                : mapperType.getSimpleName() + " interface") + " corresponding entity class");
    }

    /**
     * Gets the entity metadata for a specified entity class type.
     *
     * @param entityClass The entity class type.
     * @return The entity class metadata.
     * @throws NullPointerException if the entity class information cannot be obtained.
     */
    public static TableMeta of(Class<?> entityClass) {
        // Create TableMeta without processing columns (fields); the returned TableMeta has been processed by all
        // chains.
        TableMeta tableMeta = Holder.TABLE_SCHEMA_CHAIN.createTable(entityClass);
        if (tableMeta == null) {
            throw new NullPointerException("Unable to get " + entityClass.getName() + " entity class information");
        }
        // If the entity table is already processed, return it directly.
        if (!tableMeta.ready()) {
            synchronized (entityClass) {
                if (!tableMeta.ready()) {
                    // Get fields for unprocessed entities.
                    Class<?> declaredClass = entityClass;
                    boolean isSuperclass = false;
                    while (declaredClass != null && declaredClass != Object.class) {
                        // Use bus-core FieldKit for field retrieval with built-in cache optimization
                        Field[] declaredFields = FieldKit.getFields(declaredClass);
                        if (isSuperclass) {
                            reverse(declaredFields);
                        }
                        for (Field field : declaredFields) {
                            if (!ModifierKit.isStatic(field) && !ModifierKit.isTransient(field)) {
                                FieldMeta fieldMeta = new FieldMeta(entityClass, field);
                                // Check if the field needs to be excluded.
                                if (tableMeta.isExcludeField(fieldMeta)) {
                                    continue;
                                }
                                Optional<List<ColumnMeta>> optionalEntityColumns = Holder.COLUMN_SCHEMA_CHAIN
                                        .createColumn(tableMeta, fieldMeta);
                                optionalEntityColumns.ifPresent(columns -> columns.forEach(tableMeta::addColumn));
                            }
                        }
                        // Iterate to get the superclass.
                        declaredClass = declaredClass.getSuperclass();
                        // Exclude superclasses if specified.
                        while (tableMeta.isExcludeSuperClass(declaredClass) && declaredClass != Object.class) {
                            declaredClass = declaredClass.getSuperclass();
                        }
                        isSuperclass = true;
                    }
                    // Mark as processed.
                    tableMeta.ready(true);
                }
            }
        }
        return tableMeta;
    }

    /**
     * Reverses the order of an array.
     *
     * @param array The array to reverse.
     */
    protected static void reverse(Object[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            Object temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

}
