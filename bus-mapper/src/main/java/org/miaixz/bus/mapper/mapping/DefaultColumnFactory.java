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
package org.miaixz.bus.mapper.mapping;

import java.util.Collections;
import java.util.List;

import org.apache.ibatis.type.TypeHandler;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.mapper.provider.NamingProvider;

import jakarta.persistence.*;

/**
 * 默认列工厂实现，支持 jakarta.persistence 注解的实体类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultColumnFactory implements ColumnFactory {

    /**
     * 创建实体列信息，处理字段注解并生成列信息
     *
     * @param entityTable 实体表信息
     * @param field       字段信息
     * @param chain       列工厂处理链
     * @return 实体类中列信息的 Optional 包装对象，若为 IGNORE 或字段标记为 Transient 则返回 IGNORE
     */
    @Override
    public Optional<List<MapperColumn>> createEntityColumn(MapperTable entityTable, MapperFields field,
            ColumnFactory.Chain chain) {
        Optional<List<MapperColumn>> columns = chain.createEntityColumn(entityTable, field);
        if (columns == IGNORE || field.isAnnotationPresent(Transient.class)) {
            return IGNORE;
        } else if (!columns.isPresent()) {
            // 没有 @Transient 注解的字段都认为是表字段，字段名默认驼峰转下划线
            columns = Optional.of(Collections.singletonList(
                    MapperColumn.of(field).column(NamingProvider.getDefaultStyle().columnName(entityTable, field))));
        }
        if (columns.isPresent()) {
            List<MapperColumn> list = columns.getOrNull();
            for (MapperColumn mapperColumn : list) {
                MapperFields entityField = mapperColumn.field();
                // 主键
                if (!mapperColumn.id()) {
                    mapperColumn.id(entityField.isAnnotationPresent(Id.class));
                }
                // 列名及相关属性
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    String columnName = column.name();
                    if (!columnName.isEmpty()) {
                        mapperColumn.column(columnName);
                    }
                    mapperColumn.insertable(column.insertable()).updatable(column.updatable());
                    if (column.scale() != 0) {
                        mapperColumn.numericScale(String.valueOf(column.scale()));
                    }
                }
                // 排序
                if (field.isAnnotationPresent(OrderBy.class)) {
                    OrderBy orderBy = field.getAnnotation(OrderBy.class);
                    if (orderBy.value().isEmpty()) {
                        mapperColumn.orderBy("ASC");
                    } else {
                        mapperColumn.orderBy(orderBy.value());
                    }
                }
                // 类型处理器
                if (field.isAnnotationPresent(Convert.class)) {
                    Convert convert = field.getAnnotation(Convert.class);
                    Class converter = convert.converter();
                    // 确保 converter 不是 void 且是 TypeHandler 的子类
                    if (converter != void.class && TypeHandler.class.isAssignableFrom(converter)) {
                        mapperColumn.typeHandler(converter);
                    }
                }
            }
        }
        return columns;
    }

}