/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org mybatis.io and other contributors.           *
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
package org.aoju.bus.mapper.additional.dialect.oracle;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.mapper.builder.EntityBuilder;
import org.aoju.bus.mapper.builder.MapperBuilder;
import org.aoju.bus.mapper.builder.MapperTemplate;
import org.aoju.bus.mapper.builder.SqlBuilder;
import org.aoju.bus.mapper.entity.EntityColumn;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.Set;

/**
 * Oracle实现类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OracleProvider extends MapperTemplate {

    public OracleProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * @param ms MappedStatement
     * @return the string
     */
    public String insertList(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        // 开始拼sql
        StringBuilder sql = new StringBuilder();
        sql.append("<bind name=\"listNotEmptyCheck\" value=\"@org.aoju.bus.mapper.criteria.OGNL@notEmptyCollectionCheck(list, '" + ms.getId() + " 方法参数为空')\"/>\n");

        sql.append("INSERT ALL\n");
        sql.append("<foreach collection=\"list\" item=\"record\">\n");

        String tableName = SqlBuilder.getDynamicTableName(entityClass, tableName(entityClass), "list[0]");
        String columns = SqlBuilder.insertColumns(entityClass, false, false, false);
        sql.append(" INTO ").append(tableName).append(" ").append(columns).append("\n");
        sql.append(" VALUES ");

        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");

        Set<EntityColumn> columnList = EntityBuilder.getColumns(entityClass);
        // 单独增加对 genId 方式的支持
        for (EntityColumn column : columnList) {
            if (column.getGenIdClass() != null) {
                sql.append("<bind name=\"").append(column.getColumn()).append("GenIdBind\" value=\"@org.aoju.bus.mapper.genid.GenId@genId(");
                sql.append("record").append(", '").append(column.getProperty()).append("'");
                sql.append(", @").append(column.getGenIdClass().getName()).append("@class");
                sql.append(", '").append(tableName(entityClass)).append("'");
                sql.append(", '").append(column.getColumn()).append("')");
                sql.append("\"/>");
            }
        }
        // 当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnList) {
            if (column.isInsertable()) {
                sql.append(column.getColumnHolder("record") + Symbol.COMMA);
            }
        }
        sql.append("</trim>\n");

        sql.append("</foreach>\n");
        sql.append("SELECT 1 FROM DUAL");
        return sql.toString();
    }

}
