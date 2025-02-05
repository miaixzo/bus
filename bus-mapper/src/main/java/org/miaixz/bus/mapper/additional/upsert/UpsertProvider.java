/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.mapper.additional.upsert;

import java.util.Set;

import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.builder.EntityBuilder;
import org.miaixz.bus.mapper.builder.MapperBuilder;
import org.miaixz.bus.mapper.builder.MapperTemplate;
import org.miaixz.bus.mapper.builder.SqlBuilder;
import org.miaixz.bus.mapper.entity.EntityColumn;

/**
 * 更新或新增
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UpsertProvider extends MapperTemplate {

    public UpsertProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    public String upsert(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(tableName(entityClass));
        Set<EntityColumn> columns = EntityBuilder.getColumns(entityClass);
        String primaryKeyColumn = null;
        EntityColumn logicDeleteColumn = SqlBuilder.getLogicDeleteColumn(entityClass);
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (EntityColumn column : columns) {
            if (column.isId()) {
                primaryKeyColumn = column.getColumn();
            }
            if (column.isInsertable()) {
                sql.append(column.getColumn() + Symbol.COMMA);
            }
        }
        sql.append("</trim>");
        sql.append(" VALUES ");
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (EntityColumn column : columns) {
            if (column.getGenIdClass() != null) {
                sql.append("<bind name=\"").append(column.getColumn())
                        .append("GenIdBind\" value=\"@org.miaixz.bus.mapper.Builder@genId(");
                sql.append("record").append(", '").append(column.getProperty()).append("'");
                sql.append(", @").append(column.getGenIdClass().getCanonicalName()).append("@class");
                sql.append(", '").append(tableName(entityClass)).append("'");
                sql.append(", '").append(column.getColumn()).append("')");
                sql.append("\"/>");
            }
        }
        for (EntityColumn column : columns) {
            if (!column.isInsertable()) {
                continue;
            }
            if (logicDeleteColumn != null && logicDeleteColumn == column) {
                sql.append(SqlBuilder.getLogicDeletedValue(column, false)).append(Symbol.COMMA);
                continue;
            }
            sql.append(column.getColumnHolder() + Symbol.COMMA);
        }
        sql.append("</trim>");
        sql.append(" ON CONFLICT (" + primaryKeyColumn + ") DO UPDATE ");
        sql.append(SqlBuilder.updateSetColumns(entityClass, null, true, isNotEmpty()));
        return sql.toString();
    }

}
