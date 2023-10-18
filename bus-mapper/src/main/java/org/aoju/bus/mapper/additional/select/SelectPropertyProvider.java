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
package org.aoju.bus.mapper.additional.select;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.mapper.builder.EntityBuilder;
import org.aoju.bus.mapper.builder.MapperBuilder;
import org.aoju.bus.mapper.builder.MapperTemplate;
import org.aoju.bus.mapper.builder.SqlBuilder;
import org.aoju.bus.mapper.entity.EntityColumn;
import org.aoju.bus.mapper.entity.EntityTable;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.Objects;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class SelectPropertyProvider extends MapperTemplate {

    public SelectPropertyProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * 根据实体Class和属性名获取对应的表字段名
     *
     * @param entityClass 实体Class对象
     * @param property    属性名
     * @return the string
     */
    public static String getColumnByProperty(Class<?> entityClass, String property) {
        EntityTable entityTable = EntityBuilder.getEntityTable(entityClass);
        EntityColumn entityColumn = entityTable.getPropertyMap().get(property);
        return entityColumn.getColumn();
    }

    /**
     * 判断是否需要拼接 where 条件
     *
     * @param value    值
     * @param notEmpty 是否为空
     * @return the boolean
     */
    public static boolean existsWhereCondition(Object value, boolean notEmpty) {
        boolean appendWhereCondition = true;
        if (Objects.isNull(value)) {
            Logger.warn("value is null! this will case no conditions after where keyword");
        } else {
            if (String.class.equals(value.getClass()) && notEmpty && StringKit.isEmpty(value.toString())) {
                // 如果 value 是 String 类型，则根据是否允许为空串做进一步校验来决定是否拼接 where 条件
                appendWhereCondition = false;
            }
        }
        return appendWhereCondition;
    }

    /**
     * 根据属性查询，只能有一个返回值，有多个结果时抛出异常，查询条件使用等号
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectOneByProperty(MappedStatement ms) {
        String propertyHelper = SelectPropertyProvider.class.getName();
        Class<?> entityClass = getEntityClass(ms);
        // 修改返回值类型为实体类型
        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlBuilder.selectAllColumns(entityClass));
        sql.append(SqlBuilder.fromTable(entityClass, tableName(entityClass)));
        sql.append("<where>\n");
        sql.append("<if test=\"@");
        sql.append(propertyHelper);
        sql.append("@existsWhereCondition(value, ");
        sql.append(isNotEmpty());
        sql.append(Symbol.PARENTHESE_RIGHT);
        sql.append("\">\n");
        String entityClassName = entityClass.getName();
        // 通过实体类名获取运行时属性对应的字段
        String ognl = new StringBuilder("${@")
                .append(propertyHelper)
                .append("@getColumnByProperty(@java.lang.Class@forName(\"")
                .append(entityClassName)
                .append("\"), @org.aoju.bus.mapper.reflect.Reflector@fnToFieldName(fn))}").toString();
        sql.append(ognl + " = #{value}\n");
        sql.append("</if>\n");
        // 逻辑删除的未删除查询条件
        sql.append(SqlBuilder.whereLogicDelete(entityClass, false));
        sql.append("</where>");
        return sql.toString();
    }

    /**
     * 根据属性查询，查询条件使用等号
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectByProperty(MappedStatement ms) {
        String propertyHelper = SelectPropertyProvider.class.getName();
        Class<?> entityClass = getEntityClass(ms);
        // 修改返回值类型为实体类型
        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlBuilder.selectAllColumns(entityClass));
        sql.append(SqlBuilder.fromTable(entityClass, tableName(entityClass)));
        sql.append("<where>\n");
        sql.append("<if test=\"@");
        sql.append(propertyHelper);
        sql.append("@existsWhereCondition(value, ");
        sql.append(isNotEmpty());
        sql.append(Symbol.PARENTHESE_RIGHT);
        sql.append("\">\n");
        String entityClassName = entityClass.getName();
        // 通过实体类名获取运行时属性对应的字段
        String ognl = new StringBuilder("${@")
                .append(propertyHelper)
                .append("@getColumnByProperty(@java.lang.Class@forName(\"")
                .append(entityClassName)
                .append("\"), @org.aoju.bus.mapper.reflect.Reflector@fnToFieldName(fn))}").toString();
        sql.append(ognl + " = #{value}\n");
        sql.append("</if>\n");
        // 逻辑删除的未删除查询条件
        sql.append(SqlBuilder.whereLogicDelete(entityClass, false));
        sql.append("</where>");
        return sql.toString();
    }

    /**
     * 根据属性查询，查询条件使用 in
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectInByProperty(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        // 修改返回值类型为实体类型
        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlBuilder.selectAllColumns(entityClass));
        sql.append(SqlBuilder.fromTable(entityClass, tableName(entityClass)));
        sql.append("<where>\n");
        String entityClassName = entityClass.getName();
        String propertyHelper = SelectPropertyProvider.class.getName();
        String sqlSegment =
                "${@" + propertyHelper + "@getColumnByProperty(@java.lang.Class@forName(\"" + entityClassName + "\"),"
                        + "@org.aoju.bus.mapper.reflect.Reflector@fnToFieldName(fn))} in"
                        + "<foreach open=\"(\" close=\")\" separator=\",\" collection=\"values\" item=\"object\">\n"
                        + "#{object}\n"
                        + "</foreach>\n";
        sql.append(sqlSegment);
        // 逻辑删除的未删除查询条件
        sql.append(SqlBuilder.whereLogicDelete(entityClass, false));
        sql.append("</where>");
        return sql.toString();
    }

    /**
     * 根据属性查询，查询条件使用 between
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectBetweenByProperty(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        // 修改返回值类型为实体类型
        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlBuilder.selectAllColumns(entityClass));
        sql.append(SqlBuilder.fromTable(entityClass, tableName(entityClass)));
        sql.append("<where>\n");
        String entityClassName = entityClass.getName();
        String propertyHelper = SelectPropertyProvider.class.getName();
        String sqlSegment =
                "${@" + propertyHelper + "@getColumnByProperty(@java.lang.Class@forName(\"" + entityClassName + "\"),"
                        + "@org.aoju.bus.mapper.reflect.Reflector@fnToFieldName(fn))} "
                        + "between #{begin} and #{end}";
        sql.append(sqlSegment);
        // 逻辑删除的未删除查询条件
        sql.append(SqlBuilder.whereLogicDelete(entityClass, false));
        sql.append("</where>");
        return sql.toString();
    }

    /**
     * 根据属性查询总数，查询条件使用等号
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String existsWithProperty(MappedStatement ms) {
        String propertyHelper = SelectPropertyProvider.class.getName();
        Class<?> entityClass = getEntityClass(ms);

        StringBuilder sql = new StringBuilder();
        sql.append(SqlBuilder.selectCountExists(entityClass));
        sql.append(SqlBuilder.fromTable(entityClass, tableName(entityClass)));
        sql.append("<where>\n");
        sql.append("<if test=\"@");
        sql.append(propertyHelper);
        sql.append("@existsWhereCondition(value, ");
        sql.append(isNotEmpty());
        sql.append(Symbol.PARENTHESE_RIGHT);
        sql.append("\">\n");
        String entityClassName = entityClass.getName();
        // 通过实体类名获取运行时属性对应的字段
        String ognl = new StringBuilder("${@")
                .append(propertyHelper)
                .append("@getColumnByProperty(@java.lang.Class@forName(\"")
                .append(entityClassName)
                .append("\"), @org.aoju.bus.mapper.reflect.Reflector@fnToFieldName(fn))}").toString();
        sql.append(ognl + " = #{value}\n");
        sql.append("</if>\n");
        // 逻辑删除的未删除查询条件
        sql.append(SqlBuilder.whereLogicDelete(entityClass, false));
        sql.append("</where>");
        return sql.toString();
    }

    /**
     * 根据属性查询总数，查询条件使用等号
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectCountByProperty(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        String propertyHelper = SelectPropertyProvider.class.getName();

        StringBuilder sql = new StringBuilder();
        sql.append(SqlBuilder.selectCount(entityClass));
        sql.append(SqlBuilder.fromTable(entityClass, tableName(entityClass)));
        sql.append("<where>\n");
        sql.append("<if test=\"@");
        sql.append(propertyHelper);
        sql.append("@existsWhereCondition(value, ");
        sql.append(isNotEmpty());
        sql.append(Symbol.PARENTHESE_RIGHT);
        sql.append("\">\n");
        String entityClassName = entityClass.getName();
        // 通过实体类名获取运行时属性对应的字段
        String ognl = new StringBuilder("${@")
                .append(propertyHelper)
                .append("@getColumnByProperty(@java.lang.Class@forName(\"")
                .append(entityClassName)
                .append("\"), @org.aoju.bus.mapper.reflect.Reflector@fnToFieldName(fn))}").toString();
        sql.append(ognl + " = #{value}\n");
        sql.append("</if>\n");
        // 逻辑删除的未删除查询条件
        sql.append(SqlBuilder.whereLogicDelete(entityClass, false));
        sql.append("</where>");
        return sql.toString();
    }

}
