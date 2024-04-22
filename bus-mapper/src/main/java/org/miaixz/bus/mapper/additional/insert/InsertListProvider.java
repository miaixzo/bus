/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.mapper.additional.insert;

import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.builder.EntityBuilder;
import org.miaixz.bus.mapper.builder.MapperBuilder;
import org.miaixz.bus.mapper.builder.MapperTemplate;
import org.miaixz.bus.mapper.builder.SqlBuilder;
import org.miaixz.bus.mapper.entity.EntityColumn;

import java.util.Set;

/**
 * 新增
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class InsertListProvider extends MapperTemplate {

    public InsertListProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * 批量插入
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String insertList(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        // 开始拼sql
        StringBuilder sql = new StringBuilder();
        sql.append("<bind name=\"listNotEmptyCheck\" value=\"@org.miaixz.bus.mapper.OGNL@notEmptyCollectionCheck(list, '" + ms.getId() + " 方法参数为空')\"/>");
        sql.append(SqlBuilder.insertIntoTable(entityClass, tableName(entityClass), "list[0]"));
        sql.append(SqlBuilder.insertColumns(entityClass, false, false, false));
        sql.append(" VALUES ");
        sql.append("<foreach collection=\"list\" item=\"record\" separator=\",\" >");
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        // 获取全部列
        Set<EntityColumn> columnList = EntityBuilder.getColumns(entityClass);
        // 获取逻辑删除列
        EntityColumn logicDeleteColumn = SqlBuilder.getLogicDeleteColumn(entityClass);
        // 单独增加对 genId 方式的支持
        for (EntityColumn column : columnList) {
            if (column.getGenIdClass() != null) {
                sql.append("<bind name=\"").append(column.getColumn()).append("GenIdBind\" value=\"@org.miaixz.bus.mapper.Builder@genId(");
                sql.append("record").append(", '").append(column.getProperty()).append("'");
                sql.append(", @").append(column.getGenIdClass().getName()).append("@class");
                sql.append(", '").append(tableName(entityClass)).append("'");
                sql.append(", '").append(column.getColumn()).append("')");
                sql.append("\"/>");
            }
        }
        // 当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnList) {
            if (!column.isInsertable()) {
                continue;
            }
            if (logicDeleteColumn != null && logicDeleteColumn == column) {
                sql.append(SqlBuilder.getLogicDeletedValue(column, false)).append(Symbol.COMMA);
                continue;
            }
            sql.append(column.getColumnHolder("record") + Symbol.COMMA);
        }
        sql.append("</trim>");
        sql.append("</foreach>");
        return sql.toString();
    }

}
