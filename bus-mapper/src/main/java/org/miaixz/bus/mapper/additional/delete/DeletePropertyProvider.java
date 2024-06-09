/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         ~
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
package org.miaixz.bus.mapper.additional.delete;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.MapperException;
import org.miaixz.bus.mapper.builder.EntityBuilder;
import org.miaixz.bus.mapper.builder.MapperBuilder;
import org.miaixz.bus.mapper.builder.MapperTemplate;
import org.miaixz.bus.mapper.builder.SqlBuilder;
import org.miaixz.bus.mapper.entity.EntityColumn;
import org.miaixz.bus.mapper.entity.EntityTable;
import org.miaixz.bus.mapper.support.MetaObject;

/**
 * 删除属性
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DeletePropertyProvider extends MapperTemplate {

    public DeletePropertyProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
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

    public static boolean isNull(Object value, boolean safeDelete) {
        boolean isNull = false;
        if (safeDelete) {
            if (null == value) {
                throw new MapperException("安全删除模式下，不允许执行不带查询条件的 delete 方法");
            }
        } else {
            if (null == value) {
                isNull = true;
            }
        }
        return isNull;
    }

    /**
     * 根据属性删除，条件使用等号
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String deleteByProperty(MappedStatement ms) {
        String propertyHelper = DeletePropertyProvider.class.getName();
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
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
        sql.append("<where>\n");
        sql.append("<if test=\"false==");
        sql.append(Symbol.AT);
        sql.append(propertyHelper);
        sql.append("@isNull(value, ");
        sql.append(getConfig().isSafeDelete());
        sql.append(Symbol.PARENTHESE_RIGHT);
        sql.append("\">\n");
        String entityClassName = entityClass.getName();
        // 通过实体类名获取运行时属性对应的字段
        String ognl = new StringBuilder("${@")
                .append(propertyHelper)
                .append("@getColumnByProperty(@java.lang.Class@forName(\"")
                .append(entityClassName)
                .append("\"), @org.miaixz.bus.mapper.support.Reflector@fnToFieldName(fn))}").toString();
        sql.append(ognl + " = #{value}\n");
        sql.append("</if>\n");
        sql.append("</where>");
        return sql.toString();
    }

    /**
     * 根据属性删除，条件使用等号
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String deleteInByProperty(MappedStatement ms) {
        String propertyHelper = DeletePropertyProvider.class.getName();
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
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
        sql.append("<where>\n");
        String entityClassName = entityClass.getName();
        String sqlSegment =
                "${@" + propertyHelper + "@getColumnByProperty(@java.lang.Class@forName(\"" + entityClassName + "\"),"
                        + "@org.miaixz.bus.mapper.support.Reflector@fnToFieldName(fn))} in"
                        + "<foreach open=\"(\" close=\")\" separator=\",\" collection=\"values\" item=\"obj\">\n"
                        + "#{obj}\n"
                        + "</foreach>\n";
        sql.append(sqlSegment);
        // 逻辑删除的未删除查询条件
        sql.append(SqlBuilder.whereLogicDelete(entityClass, false));
        sql.append("</where>");
        return sql.toString();
    }

    /**
     * 根据属性删除，删除条件使用 between
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String deleteBetweenByProperty(MappedStatement ms) {
        String propertyHelper = DeletePropertyProvider.class.getName();
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
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
        sql.append("<where>\n");
        String entityClassName = entityClass.getName();
        String sqlSegment =
                "${@" + propertyHelper + "@getColumnByProperty(@java.lang.Class@forName(\"" + entityClassName + "\"),"
                        + "@org.miaixz.bus.mapper.support.Reflector@fnToFieldName(fn))} "
                        + "between #{begin} and #{end}";
        sql.append(sqlSegment);
        // 逻辑删除的未删除查询条件
        sql.append(SqlBuilder.whereLogicDelete(entityClass, false));
        sql.append("</where>");
        return sql.toString();
    }

}
