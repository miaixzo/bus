/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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
import java.lang.reflect.Modifier;
import java.util.List;

import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.mapper.Holder;
import org.miaixz.bus.mapper.builder.ClassMetaResolver;

/**
 * 实体类信息工厂，用于创建和管理实体类信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class MapperFactory {

    /**
     * 获取接口和方法对应的实体信息
     *
     * @param mapperType   Mapper 接口
     * @param mapperMethod Mapper 方法
     * @return 实体类信息
     * @throws RuntimeException 如果无法获取对应的实体类
     */
    public static TableMeta create(Class<?> mapperType, Method mapperMethod) {
        Optional<Class<?>> optionalClass = ClassMetaResolver.find(mapperType, mapperMethod);
        if (optionalClass.isPresent()) {
            return create(optionalClass.getOrNull());
        }
        throw new RuntimeException("Can't obtain " + (mapperMethod != null ? mapperMethod.getName() + " method"
                : mapperType.getSimpleName() + " interface") + " corresponding entity class");
    }

    /**
     * 获取指定实体类类型的实体信息
     *
     * @param entityClass 实体类类型
     * @return 实体类信息
     * @throws NullPointerException 如果无法获取实体类信息
     */
    public static TableMeta create(Class<?> entityClass) {
        // 创建 TableMeta，不处理列（字段），此时返回的 TableMeta 已经经过所有处理链的加工
        TableMeta tableMeta = Holder.TABLE_SCHEMA_CHAIN.createTable(entityClass);
        if (tableMeta == null) {
            throw new NullPointerException("Unable to get " + entityClass.getName() + " entity class information");
        }
        // 如果实体表已经处理好，直接返回
        if (!tableMeta.ready()) {
            synchronized (entityClass) {
                if (!tableMeta.ready()) {
                    // 未处理的需要获取字段
                    Class<?> declaredClass = entityClass;
                    boolean isSuperclass = false;
                    while (declaredClass != null && declaredClass != Object.class) {
                        Field[] declaredFields = declaredClass.getDeclaredFields();
                        if (isSuperclass) {
                            reverse(declaredFields);
                        }
                        for (Field field : declaredFields) {
                            int modifiers = field.getModifiers();
                            // 排除 static 和 transient 修饰的字段
                            if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                                FieldMeta fieldMeta = new FieldMeta(entityClass, field);
                                // 是否需要排除字段
                                if (tableMeta.isExcludeField(fieldMeta)) {
                                    continue;
                                }
                                Optional<List<ColumnMeta>> optionalEntityColumns = Holder.COLUMN_SCHEMA_CHAIN
                                        .createColumn(tableMeta, fieldMeta);
                                optionalEntityColumns.ifPresent(columns -> columns.forEach(tableMeta::addColumn));
                            }
                        }
                        // 迭代获取父类
                        declaredClass = declaredClass.getSuperclass();
                        // 排除父类
                        while (tableMeta.isExcludeSuperClass(declaredClass) && declaredClass != Object.class) {
                            declaredClass = declaredClass.getSuperclass();
                        }
                        isSuperclass = true;
                    }
                    // 标记处理完成
                    tableMeta.ready(true);
                }
            }
        }
        return tableMeta;
    }

    /**
     * 反转数组顺序
     *
     * @param array 要反转的数组
     */
    protected static void reverse(Object[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            Object temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

}