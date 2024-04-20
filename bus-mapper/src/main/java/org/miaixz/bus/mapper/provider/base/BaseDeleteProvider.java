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
package org.miaixz.bus.mapper.provider.base;

import org.miaixz.bus.mapper.builder.EntityBuilder;
import org.miaixz.bus.mapper.builder.MapperBuilder;
import org.miaixz.bus.mapper.builder.MapperTemplate;
import org.miaixz.bus.mapper.builder.SqlBuilder;
import org.miaixz.bus.mapper.reflect.MetaObject;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;

/**
 * BaseDeleteMapper实现类，基础方法实现类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BaseDeleteProvider extends MapperTemplate {

    public BaseDeleteProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * 通过条件删除
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String delete(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        // 如果设置了安全删除，就不允许执行不带查询条件的 delete 方法
        if (getConfig().isSafeDelete()) {
            sql.append(SqlBuilder.notAllNullParameterCheck("_parameter", EntityBuilder.getColumns(entityClass)));
        }
        // 如果是逻辑删除，则修改为更新表，修改逻辑删除字段的值
        if (SqlBuilder.hasLogicDeleteColumn(entityClass)) {
            sql.append(SqlBuilder.updateTable(entityClass, tableName(entityClass)));
            sql.append("<set>");
            sql.append(SqlBuilder.logicDeleteColumnEqualsValue(entityClass, true));
            sql.append("</set>");
            MetaObject.forObject(ms).setValue("sqlCommandType", SqlCommandType.UPDATE);
        } else {
            sql.append(SqlBuilder.deleteFromTable(entityClass, tableName(entityClass)));
        }
        sql.append(SqlBuilder.whereAllIfColumns(entityClass, isNotEmpty()));
        return sql.toString();
    }

    /**
     * 通过主键删除
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String deleteByPrimaryKey(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        if (SqlBuilder.hasLogicDeleteColumn(entityClass)) {
            sql.append(SqlBuilder.updateTable(entityClass, tableName(entityClass)));
            sql.append("<set>");
            sql.append(SqlBuilder.logicDeleteColumnEqualsValue(entityClass, true));
            sql.append("</set>");
            MetaObject.forObject(ms).setValue("sqlCommandType", SqlCommandType.UPDATE);
        } else {
            sql.append(SqlBuilder.deleteFromTable(entityClass, tableName(entityClass)));
        }
        sql.append(SqlBuilder.wherePKColumns(entityClass));
        return sql.toString();
    }

}
