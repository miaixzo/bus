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

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.provider.NamingProvider;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 默认表工厂实现，支持处理 jakarta.persistence 注解的实体类表信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultTableFactory implements TableFactory {

    /**
     * 创建实体表信息，基于注解或默认命名规则
     *
     * @param entityClass 实体类
     * @param chain       表工厂处理链
     * @return 实体表信息
     */
    @Override
    public MapperTable createEntityTable(Class<?> entityClass, Chain chain) {
        MapperTable entityTable = chain.createEntityTable(entityClass);
        if (entityTable == null) {
            entityTable = MapperTable.of(entityClass);
        }
        if (entityClass.isAnnotationPresent(Table.class)) {
            Table table = entityClass.getAnnotation(Table.class);
            if (!table.name().isEmpty()) {
                entityTable.table(table.name());
            }
            if (!table.catalog().isEmpty()) {
                entityTable.catalog(table.catalog());
            }
            if (!table.schema().isEmpty()) {
                entityTable.schema(table.schema());
            }
        } else if (StringKit.isEmpty(entityTable.table())) {
            // 没有设置表名时，默认类名转下划线
            entityTable.table(NamingProvider.getDefaultStyle().tableName(entityClass));
        }
        // 使用 JPA 的 @Entity 注解作为开启 autoResultMap 的标志，可配合字段的 @Convert 注解使用
        if (entityClass.isAnnotationPresent(Entity.class)) {
            entityTable.autoResultMap(true);
        }
        return entityTable;
    }

}