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
package org.miaixz.bus.mapper.builder;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.parsing.TableMeta;
import org.miaixz.bus.mapper.provider.NamingProvider;
import org.miaixz.bus.core.Context;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * 默认表构建器，支持处理 jakarta.persistence 注解的实体类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TableAnnotationBuilder implements TableSchemaBuilder {

    /**
     * 根据注解或默认命名规则为实体类创建表元数据
     *
     * @param entityClass 实体类
     * @param chain       表结构构建器链
     * @return 表元数据
     */
    @Override
    public TableMeta createTable(Class<?> entityClass, Chain chain) {
        TableMeta tableMeta = chain.createTable(entityClass);
        if (tableMeta == null) {
            tableMeta = TableMeta.of(entityClass);
        }

        // 处理表相关注解及默认表名
        processTableAnnotations(tableMeta, entityClass);

        // 为 @Entity 注解的类启用自动结果映射
        if (entityClass.isAnnotationPresent(Entity.class)) {
            tableMeta.autoResultMap(true);
        }

        // 表名不为空时，添加表前缀
        if (StringKit.isNotEmpty(tableMeta.table())) {
            String prefix = Context.INSTANCE.getProperty(Args.TABLE_PREFIX_KEY, Normal.EMPTY);
            tableMeta.table(prefix + tableMeta.table());
        }

        return tableMeta;
    }

    /**
     * 处理 @Table 注解，设置表名、目录和模式，或使用默认命名规则
     *
     * @param entityClass 实体类
     * @param tableMeta   表元数据
     */
    protected void processTableAnnotations(TableMeta tableMeta, Class<?> entityClass) {
        if (entityClass.isAnnotationPresent(Table.class)) {
            Table table = entityClass.getAnnotation(Table.class);
            if (StringKit.isNotEmpty(table.name())) {
                tableMeta.table(table.name());
            }
            if (StringKit.isNotEmpty(table.catalog())) {
                tableMeta.catalog(table.catalog());
            }
            if (StringKit.isNotEmpty(table.schema())) {
                tableMeta.schema(table.schema());
            }
        } else if (StringKit.isEmpty(tableMeta.table())) {
            // 未设置表名时，使用默认命名规则（类名转为下划线格式）
            tableMeta.table(NamingProvider.getDefaultStyle().tableName(entityClass));
        }
    }

}