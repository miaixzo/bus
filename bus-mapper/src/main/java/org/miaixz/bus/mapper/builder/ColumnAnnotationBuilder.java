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

import java.util.Collections;
import java.util.List;

import org.apache.ibatis.type.TypeHandler;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.mapper.ORDER;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.FieldMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;
import org.miaixz.bus.mapper.provider.NamingProvider;

import jakarta.persistence.*;

/**
 * 默认列构建器，支持 jakarta.persistence 注解的实体类，解析字段注解并生成列信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ColumnAnnotationBuilder implements ColumnSchemaBuilder {

    /**
     * 忽略标记，用于表示字段不映射到数据库列
     */
    private static final Optional<List<ColumnMeta>> IGNORE = Optional.of(Collections.emptyList());

    /**
     * 创建实体列信息，解析字段注解并生成列元数据
     *
     * @param tableMeta 实体表信息，包含表元数据
     * @param fieldMeta 字段信息，包含字段元数据
     * @param chain     列工厂处理链，用于责任链模式
     * @return 列信息的 Optional 包装对象，若字段被忽略或标记为 Transient 则返回空列表
     */
    @Override
    public Optional<List<ColumnMeta>> createColumn(TableMeta tableMeta, FieldMeta fieldMeta,
            ColumnSchemaBuilder.Chain chain) {
        // 优先调用责任链中的下一个处理器
        Optional<List<ColumnMeta>> columns = chain.createColumn(tableMeta, fieldMeta);
        if (columns == IGNORE || fieldMeta.isAnnotationPresent(Transient.class)) {
            return IGNORE;
        }

        // 若无列信息且字段未标记为 Transient，生成默认列信息（驼峰转下划线）
        if (!columns.isPresent()) {
            String columnName = NamingProvider.getDefaultStyle().columnName(tableMeta, fieldMeta);
            columns = Optional.of(Collections.singletonList(ColumnMeta.of(fieldMeta).column(columnName)));
        }

        // 处理列信息中的注解
        if (columns.isPresent()) {
            List<ColumnMeta> columnList = columns.getOrNull();
            for (ColumnMeta columnMeta : columnList) {
                processAnnotations(columnMeta, fieldMeta);
                EntityClassBuilder.setColumnMeta(tableMeta.entityClass(), columnMeta);
            }
        }

        return columns;
    }

    /**
     * 处理字段上的注解，设置列的元数据属性
     *
     * @param columnMeta 列元数据对象
     * @param fieldMeta  字段元数据对象
     */
    protected void processAnnotations(ColumnMeta columnMeta, FieldMeta fieldMeta) {
        // 处理主键注解
        if (!columnMeta.id() && fieldMeta.isAnnotationPresent(Id.class)) {
            columnMeta.id(true);
        }

        // 处理列注解
        if (fieldMeta.isAnnotationPresent(Column.class)) {
            Column column = fieldMeta.getAnnotation(Column.class);
            if (!column.name().isEmpty()) {
                columnMeta.column(column.name());
            }
            columnMeta.insertable(column.insertable()).updatable(column.updatable());
            if (column.scale() != 0) {
                columnMeta.numericScale(String.valueOf(column.scale()));
            }
        }

        // 处理排序注解
        if (fieldMeta.isAnnotationPresent(OrderBy.class)) {
            OrderBy orderBy = fieldMeta.getAnnotation(OrderBy.class);
            columnMeta.orderBy(orderBy.value().isEmpty() ? ORDER.ASC : orderBy.value());
        }

        // 处理类型转换器注解
        if (fieldMeta.isAnnotationPresent(Convert.class)) {
            Convert convert = fieldMeta.getAnnotation(Convert.class);
            Class converter = convert.converter();
            if (converter != void.class && TypeHandler.class.isAssignableFrom(converter)) {
                columnMeta.typeHandler(converter);
            }
        }
    }

}