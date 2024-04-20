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
package org.miaixz.bus.mapper.provider;

import org.miaixz.bus.mapper.builder.EntityBuilder;
import org.miaixz.bus.mapper.builder.MapperBuilder;
import org.miaixz.bus.mapper.builder.MapperTemplate;
import org.miaixz.bus.mapper.builder.SqlBuilder;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * SqlServerProvider实现类，特殊方法实现类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlServerProvider extends MapperTemplate {

    public SqlServerProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * 插入
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String insert(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        // 开始拼sql
        StringBuilder sql = new StringBuilder();
        sql.append(SqlBuilder.insertIntoTable(entityClass, tableName(entityClass)));
        sql.append(SqlBuilder.insertColumns(entityClass, true, false, false));
        sql.append(SqlBuilder.insertValuesColumns(entityClass, true, false, false));

        // 反射把MappedStatement中的设置主键名
        EntityBuilder.setKeyProperties(EntityBuilder.getPKColumns(entityClass), ms);

        return sql.toString();
    }

    /**
     * 插入不为null的字段
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String insertSelective(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlBuilder.insertIntoTable(entityClass, tableName(entityClass)));
        sql.append(SqlBuilder.insertColumns(entityClass, true, true, isNotEmpty()));
        sql.append(SqlBuilder.insertValuesColumns(entityClass, true, true, isNotEmpty()));

        // 反射把MappedStatement中的设置主键名
        EntityBuilder.setKeyProperties(EntityBuilder.getPKColumns(entityClass), ms);

        return sql.toString();
    }

}
