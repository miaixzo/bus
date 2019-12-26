/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.mapper.builder;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.mapper.annotation.Version;
import org.aoju.bus.mapper.criteria.Assert;
import org.aoju.bus.mapper.entity.EntityColumn;
import org.aoju.bus.mapper.entity.EntityTableName;
import org.aoju.bus.mapper.version.VersionException;

import java.util.Set;

/**
 * 拼常用SQL的工具类
 *
 * @author Kimi Liu
 * @version 5.3.8
 * @since JDK 1.8+
 */
public class SqlSourceBuilder {

    /**
     * 获取表名 - 支持动态表名
     *
     * @param entityClass 对象
     * @param tableName   表
     * @return the string
     */
    public static String getDynamicTableName(Class<?> entityClass, String tableName) {
        if (EntityTableName.class.isAssignableFrom(entityClass)) {
            StringBuilder sql = new StringBuilder();
            sql.append("<choose>");
            sql.append("<when test=\"@org.aoju.bus.mapper.builder.OGNL@isDynamicParameter(_parameter) and dynamicTableName != null and dynamicTableName != ''\">");
            sql.append("${dynamicTableName}\n");
            sql.append("</when>");
            //不支持指定列的时候查询全部列
            sql.append("<otherwise>");
            sql.append(tableName);
            sql.append("</otherwise>");
            sql.append("</choose>");
            return sql.toString();
        } else {
            return tableName;
        }
    }

    /**
     * 获取表名 - 支持动态表名,该方法用于多个入参时,通过parameterName指定入参中实体类的@Param的注解值
     *
     * @param entityClass   对象
     * @param tableName     表
     * @param parameterName 参数
     * @return the string
     */
    public static String getDynamicTableName(Class<?> entityClass, String tableName, String parameterName) {
        if (EntityTableName.class.isAssignableFrom(entityClass)) {
            if (Assert.isNotEmpty(parameterName)) {
                StringBuilder sql = new StringBuilder();
                sql.append("<choose>");
                sql.append("<when test=\"@org.aoju.bus.mapper.builder.OGNL@isDynamicParameter(" + parameterName + ") and " + parameterName + ".dynamicTableName != null and " + parameterName + ".dynamicTableName != ''\">");
                sql.append("${" + parameterName + ".dynamicTableName}");
                sql.append("</when>");
                //不支持指定列的时候查询全部列
                sql.append("<otherwise>");
                sql.append(tableName);
                sql.append("</otherwise>");
                sql.append("</choose>");
                return sql.toString();
            } else {
                return getDynamicTableName(entityClass, tableName);
            }

        } else {
            return tableName;
        }
    }

    /**
     * @param column 列信息
     * @return the string
     */
    public static String getBindCache(EntityColumn column) {
        StringBuilder sql = new StringBuilder();
        sql.append("<bind name=\"");
        sql.append(column.getProperty()).append("_cache\" ");
        sql.append("value=\"").append(column.getProperty()).append("\"/>");
        return sql.toString();
    }

    /**
     * @param column 列信息
     * @param value  值信息
     * @return the string
     */
    public static String getBindValue(EntityColumn column, String value) {
        StringBuilder sql = new StringBuilder();
        sql.append("<bind name=\"");
        sql.append(column.getProperty()).append("_bind\" ");
        sql.append("value='").append(value).append("'/>");
        return sql.toString();
    }

    /**
     * @param column   列信息
     * @param contents 内容
     * @return the string
     */
    public static String getIfCacheNotNull(EntityColumn column, String contents) {
        StringBuilder sql = new StringBuilder();
        sql.append("<if test=\"").append(column.getProperty()).append("_cache != null\">");
        sql.append(contents);
        sql.append("</if>");
        return sql.toString();
    }

    /**
     * 如果_cache == null
     *
     * @param column   列信息
     * @param contents 内容
     * @return the string
     */
    public static String getIfCacheIsNull(EntityColumn column, String contents) {
        StringBuilder sql = new StringBuilder();
        sql.append("<if test=\"").append(column.getProperty()).append("_cache == null\">");
        sql.append(contents);
        sql.append("</if>");
        return sql.toString();
    }

    /**
     * 判断自动!=null的条件结构
     *
     * @param column   列信息
     * @param contents 内容
     * @param empty    是否empty
     * @return the string
     */
    public static String getIfNotNull(EntityColumn column, String contents, boolean empty) {
        return getIfNotNull(null, column, contents, empty);
    }

    /**
     * 判断自动==null的条件结构
     *
     * @param column   列信息
     * @param contents 内容
     * @param empty    是否empty
     * @return the string
     */
    public static String getIfIsNull(EntityColumn column, String contents, boolean empty) {
        return getIfIsNull(null, column, contents, empty);
    }

    /**
     * 判断自动!=null的条件结构
     *
     * @param entityName 对象
     * @param column     列信息
     * @param contents   内容
     * @param empty      是否empty
     * @return the string
     */
    public static String getIfNotNull(String entityName, EntityColumn column, String contents, boolean empty) {
        StringBuilder sql = new StringBuilder();
        sql.append("<if test=\"");
        if (Assert.isNotEmpty(entityName)) {
            sql.append(entityName).append(Symbol.DOT);
        }
        sql.append(column.getProperty()).append(" != null");
        if (empty && column.getJavaType().equals(String.class)) {
            sql.append(" and ");
            if (Assert.isNotEmpty(entityName)) {
                sql.append(entityName).append(Symbol.DOT);
            }
            sql.append(column.getProperty()).append(" != '' ");
        }
        sql.append("\">");
        sql.append(contents);
        sql.append("</if>");
        return sql.toString();
    }

    /**
     * 判断自动==null的条件结构
     *
     * @param entityName 对象
     * @param column     列信息
     * @param contents   内容
     * @param empty      是否empty
     * @return the string
     */
    public static String getIfIsNull(String entityName, EntityColumn column, String contents, boolean empty) {
        StringBuilder sql = new StringBuilder();
        sql.append("<if test=\"");
        if (Assert.isNotEmpty(entityName)) {
            sql.append(entityName).append(Symbol.DOT);
        }
        sql.append(column.getProperty()).append(" == null");
        if (empty && column.getJavaType().equals(String.class)) {
            sql.append(" or ");
            if (Assert.isNotEmpty(entityName)) {
                sql.append(entityName).append(Symbol.DOT);
            }
            sql.append(column.getProperty()).append(" == '' ");
        }
        sql.append("\">");
        sql.append(contents);
        sql.append("</if>");
        return sql.toString();
    }

    /**
     * 获取所有查询列,如id,name,criteria...
     *
     * @param entityClass 对象
     * @return the string
     */
    public static String getAllColumns(Class<?> entityClass) {
        Set<EntityColumn> columnList = EntityBuilder.getColumns(entityClass);
        StringBuilder sql = new StringBuilder();
        for (EntityColumn entityColumn : columnList) {
            sql.append(entityColumn.getColumn()).append(",");
        }
        return sql.substring(0, sql.length() - 1);
    }

    /**
     * select xxx,xxx...
     *
     * @param entityClass 对象
     * @return the string
     */
    public static String selectAllColumns(Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(getAllColumns(entityClass));
        sql.append(Symbol.SPACE);
        return sql.toString();
    }

    /**
     * select count(x)
     *
     * @param entityClass 对象
     * @return the string
     */
    public static String selectCount(Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        Set<EntityColumn> pkColumns = EntityBuilder.getPKColumns(entityClass);
        if (pkColumns.size() == 1) {
            sql.append("COUNT(").append(pkColumns.iterator().next().getColumn()).append(") ");
        } else {
            sql.append("COUNT(*) ");
        }
        return sql.toString();
    }

    /**
     * @param entityClass 对象
     * @return the string
     */
    public static String selectCountExists(Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT CASE WHEN ");
        Set<EntityColumn> pkColumns = EntityBuilder.getPKColumns(entityClass);
        if (pkColumns.size() == 1) {
            sql.append("COUNT(").append(pkColumns.iterator().next().getColumn()).append(") ");
        } else {
            sql.append("COUNT(*) ");
        }
        sql.append(" > 0 THEN 1 ELSE 0 END AS result ");
        return sql.toString();
    }

    /**
     * from tableName - 动态表名
     *
     * @param entityClass      对象
     * @param defaultTableName 表名
     * @return the string
     */
    public static String fromTable(Class<?> entityClass, String defaultTableName) {
        StringBuilder sql = new StringBuilder();
        sql.append(" FROM ");
        sql.append(getDynamicTableName(entityClass, defaultTableName));
        sql.append(Symbol.SPACE);
        return sql.toString();
    }

    /**
     * update tableName - 动态表名
     *
     * @param entityClass      对象
     * @param defaultTableName 表名
     * @return the string
     */
    public static String updateTable(Class<?> entityClass, String defaultTableName) {
        return updateTable(entityClass, defaultTableName, null);
    }

    /**
     * update tableName - 动态表名
     *
     * @param entityClass      对象
     * @param defaultTableName 表名
     * @param entityName       对象名
     * @return the string
     */
    public static String updateTable(Class<?> entityClass, String defaultTableName, String entityName) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ");
        sql.append(getDynamicTableName(entityClass, defaultTableName, entityName));
        sql.append(Symbol.SPACE);
        return sql.toString();
    }

    /**
     * delete tableName - 动态表名
     *
     * @param entityClass      对象
     * @param defaultTableName 表名
     * @return the string
     */
    public static String deleteFromTable(Class<?> entityClass, String defaultTableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ");
        sql.append(getDynamicTableName(entityClass, defaultTableName));
        sql.append(Symbol.SPACE);
        return sql.toString();
    }

    /**
     * insert into tableName - 动态表名
     *
     * @param entityClass      对象
     * @param defaultTableName 表名
     * @return the string
     */
    public static String insertIntoTable(Class<?> entityClass, String defaultTableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(getDynamicTableName(entityClass, defaultTableName));
        sql.append(Symbol.SPACE);
        return sql.toString();
    }

    /**
     * insert table()列
     *
     * @param entityClass 对象
     * @param skipId      是否从列中忽略id类型
     * @param notNull     是否判断!=null
     * @param notEmpty    是否判断String类型!=''
     * @return the string
     */
    public static String insertColumns(Class<?> entityClass, boolean skipId, boolean notNull, boolean notEmpty) {
        StringBuilder sql = new StringBuilder();
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        //获取全部列
        Set<EntityColumn> columnList = EntityBuilder.getColumns(entityClass);
        //当某个列有主键策略时,不需要考虑他的属性是否为空,因为如果为空,一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnList) {
            if (!column.isInsertable()) {
                continue;
            }
            if (skipId && column.isId()) {
                continue;
            }
            if (notNull) {
                sql.append(SqlSourceBuilder.getIfNotNull(column, column.getColumn() + ",", notEmpty));
            } else {
                sql.append(column.getColumn() + ",");
            }
        }
        sql.append("</trim>");
        return sql.toString();
    }

    /**
     * insert-values()列
     *
     * @param entityClass 对象
     * @param skipId      是否从列中忽略id类型
     * @param notNull     是否判断!=null
     * @param notEmpty    是否判断String类型!=''
     * @return the string
     */
    public static String insertValuesColumns(Class<?> entityClass, boolean skipId, boolean notNull, boolean notEmpty) {
        StringBuilder sql = new StringBuilder();
        sql.append("<trim prefix=\"VALUES (\" suffix=\")\" suffixOverrides=\",\">");
        //获取全部列
        Set<EntityColumn> columnList = EntityBuilder.getColumns(entityClass);
        //当某个列有主键策略时,不需要考虑他的属性是否为空,因为如果为空,一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnList) {
            if (!column.isInsertable()) {
                continue;
            }
            if (skipId && column.isId()) {
                continue;
            }
            if (notNull) {
                sql.append(SqlSourceBuilder.getIfNotNull(column, column.getColumnHolder() + ",", notEmpty));
            } else {
                sql.append(column.getColumnHolder() + ",");
            }
        }
        sql.append("</trim>");
        return sql.toString();
    }

    /**
     * update set列
     *
     * @param entityClass 对象
     * @param entityName  实体映射名
     * @param notNull     是否判断!=null
     * @param notEmpty    是否判断String类型!=''
     * @return the string
     */
    public static String updateSetColumns(Class<?> entityClass, String entityName, boolean notNull, boolean notEmpty) {
        StringBuilder sql = new StringBuilder();
        sql.append("<set>");
        //获取全部列
        Set<EntityColumn> columnList = EntityBuilder.getColumns(entityClass);
        //对乐观锁的支持
        EntityColumn versionColumn = null;
        //当某个列有主键策略时,不需要考虑他的属性是否为空,因为如果为空,一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnList) {
            if (column.getEntityField().isAnnotationPresent(Version.class)) {
                if (versionColumn != null) {
                    throw new VersionException(entityClass.getCanonicalName() + " 中包含多个带有 @Version 注解的字段,一个类中只能存在一个带有 @Version 注解的字段!");
                }
                versionColumn = column;
            }
            if (!column.isId() && column.isUpdatable()) {
                if (column == versionColumn) {
                    Version version = versionColumn.getEntityField().getAnnotation(Version.class);
                    String versionClass = version.nextVersion().getCanonicalName();
                    sql.append(column.getColumn())
                            .append(" = ${@org.aoju.bus.mapper.version.VersionUtil@nextVersion(\"")
                            .append(versionClass).append("\", ")
                            .append(column.getProperty()).append(")},");
                } else if (notNull) {
                    sql.append(SqlSourceBuilder.getIfNotNull(entityName, column, column.getColumnEqualsHolder(entityName) + ",", notEmpty));
                } else {
                    sql.append(column.getColumnEqualsHolder(entityName) + ",");
                }
            }
        }
        sql.append("</set>");
        return sql.toString();
    }

    /**
     * where主键条件
     *
     * @param entityClass 对象
     * @return the string
     */
    public static String wherePKColumns(Class<?> entityClass) {
        return wherePKColumns(entityClass, false);
    }

    /**
     * where主键条件
     *
     * @param entityClass 对象
     * @param useVersion  是否自定义版本
     * @return the string
     */
    public static String wherePKColumns(Class<?> entityClass, boolean useVersion) {
        StringBuilder sql = new StringBuilder();
        sql.append("<where>");
        //获取全部列
        Set<EntityColumn> columnList = EntityBuilder.getPKColumns(entityClass);
        //当某个列有主键策略时,不需要考虑他的属性是否为空,因为如果为空,一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnList) {
            sql.append(" AND " + column.getColumnEqualsHolder());
        }
        if (useVersion) {
            sql.append(whereVersion(entityClass));
        }
        sql.append("</where>");
        return sql.toString();
    }

    /**
     * where所有列的条件,会判断是否!=null
     *
     * @param entityClass 对象
     * @param empty       是否为empty
     * @return the string
     */
    public static String whereAllIfColumns(Class<?> entityClass, boolean empty) {
        return whereAllIfColumns(entityClass, empty, false);
    }

    /**
     * where所有列的条件,会判断是否!=null
     *
     * @param entityClass 对象
     * @param empty       是否为empty
     * @param useVersion  版本
     * @return the string
     */
    public static String whereAllIfColumns(Class<?> entityClass, boolean empty, boolean useVersion) {
        StringBuilder sql = new StringBuilder();
        sql.append("<where>");
        //获取全部列
        Set<EntityColumn> columnList = EntityBuilder.getColumns(entityClass);
        //当某个列有主键策略时,不需要考虑他的属性是否为空,因为如果为空,一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnList) {
            if (!useVersion || !column.getEntityField().isAnnotationPresent(Version.class)) {
                sql.append(getIfNotNull(column, " AND " + column.getColumnEqualsHolder(), empty));
            }
        }
        if (useVersion) {
            sql.append(whereVersion(entityClass));
        }
        sql.append("</where>");
        return sql.toString();
    }

    /**
     * 乐观锁字段条件
     *
     * @param entityClass 对象
     * @return the string
     */
    public static String whereVersion(Class<?> entityClass) {
        Set<EntityColumn> columnList = EntityBuilder.getColumns(entityClass);
        boolean hasVersion = false;
        String result = "";
        for (EntityColumn column : columnList) {
            if (column.getEntityField().isAnnotationPresent(Version.class)) {
                if (hasVersion) {
                    throw new VersionException(entityClass.getCanonicalName() + " 中包含多个带有 @Version 注解的字段,一个类中只能存在一个带有 @Version 注解的字段!");
                }
                hasVersion = true;
                result = " AND " + column.getColumnEqualsHolder();
            }
        }
        return result;
    }

    /**
     * 获取默认的orderBy,通过注解设置的
     *
     * @param entityClass 对象
     * @return the string
     */
    public static String orderByDefault(Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        String orderByClause = EntityBuilder.getOrderByClause(entityClass);
        if (orderByClause.length() > 0) {
            sql.append(" ORDER BY ");
            sql.append(orderByClause);
        }
        return sql.toString();
    }

    /**
     * 支持查询指定列时
     *
     * @param entityClass 对象
     * @return the string
     */
    public static String selectColumns(Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("<choose>");
        sql.append("<when test=\"@org.aoju.bus.mapper.builder.OGNL@hasSelectColumns(_parameter)\">");
        sql.append("<foreach collection=\"_parameter.selectColumns\" item=\"selectColumn\" separator=\",\">");
        sql.append("${selectColumn}");
        sql.append("</foreach>");
        sql.append("</when>");
        //不支持指定列的时候查询全部列
        sql.append("<otherwise>");
        sql.append(getAllColumns(entityClass));
        sql.append("</otherwise>");
        sql.append("</choose>");
        return sql.toString();
    }

    /**
     * 支持查询指定列时
     *
     * @param entityClass 对象
     * @return the string
     */
    public static String countColumn(Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("<choose>");
        sql.append("<when test=\"@org.aoju.bus.mapper.builder.OGNL@hasCountColumn(_parameter)\">");
        sql.append("COUNT(${countColumn})");
        sql.append("</when>");
        sql.append("<otherwise>");
        sql.append("COUNT(0)");
        sql.append("</otherwise>");
        sql.append("</choose>");
        //不支持指定列的时候查询全部列
        sql.append("<if test=\"@org.aoju.bus.mapper.builder.OGNL@hasNoSelectColumns(_parameter)\">");
        sql.append(getAllColumns(entityClass));
        sql.append("</if>");
        return sql.toString();
    }

    /**
     * 查询中的orderBy条件,会判断默认orderBy
     *
     * @param entityClass 对象
     * @return the string
     */
    public static String orderBy(Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("<if test=\"orderByClause != null\">");
        sql.append("order by ${orderByClause}");
        sql.append("</if>");
        String orderByClause = EntityBuilder.getOrderByClause(entityClass);
        if (orderByClause.length() > 0) {
            sql.append("<if test=\"orderByClause == null\">");
            sql.append("ORDER BY " + orderByClause);
            sql.append("</if>");
        }
        return sql.toString();
    }

    /**
     * 支持 for update
     *
     * @return the string
     */
    public static String forUpdate() {
        StringBuilder sql = new StringBuilder();
        sql.append("<if test=\"@org.aoju.bus.mapper.builder.OGNL@hasForUpdate(_parameter)\">");
        sql.append("FOR UPDATE");
        sql.append("</if>");
        return sql.toString();
    }

    /**
     * 支持 for update
     *
     * @param entityClass 对象
     * @return the string
     */
    public static String check(Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("<bind name=\"checkEntityClass\" value=\"@org.aoju.bus.mapper.builder.OGNL@checkEntityClass(_parameter, '");
        sql.append(entityClass.getCanonicalName());
        sql.append("')\"/>");
        return sql.toString();
    }

    /**
     * 查询中的where结构,用于只有一个参数时
     *
     * @return the string
     */
    public static String whereClause() {
        return "<if test=\"_parameter != null\">" +
                "<where>\n" +
                "  <foreach collection=\"oredCriteria\" item=\"criteria\">\n" +
                "    <if test=\"criteria.valid\">\n" +
                "      ${@org.aoju.bus.mapper.builder.OGNL@andOr(criteria)}" +
                "      <trim prefix=\"(\" prefixOverrides=\"and |or \" suffix=\")\">\n" +
                "        <foreach collection=\"criteria.criteria\" item=\"criterion\">\n" +
                "          <choose>\n" +
                "            <when test=\"criterion.noValue\">\n" +
                "              ${@org.aoju.bus.mapper.builder.OGNL@andOr(criterion)} ${criterion.condition}\n" +
                "            </when>\n" +
                "            <when test=\"criterion.singleValue\">\n" +
                "              ${@org.aoju.bus.mapper.builder.OGNL@andOr(criterion)} ${criterion.condition} #{criterion.value}\n" +
                "            </when>\n" +
                "            <when test=\"criterion.betweenValue\">\n" +
                "              ${@org.aoju.bus.mapper.builder.OGNL@andOr(criterion)} ${criterion.condition} #{criterion.value} and #{criterion.secondValue}\n" +
                "            </when>\n" +
                "            <when test=\"criterion.listValue\">\n" +
                "              ${@org.aoju.bus.mapper.builder.OGNL@andOr(criterion)} ${criterion.condition}\n" +
                "              <foreach close=\")\" collection=\"criterion.value\" item=\"listItem\" open=\"(\" separator=\",\">\n" +
                "                #{listItem}\n" +
                "              </foreach>\n" +
                "            </when>\n" +
                "          </choose>\n" +
                "        </foreach>\n" +
                "      </trim>\n" +
                "    </if>\n" +
                "  </foreach>\n" +
                "</where>" +
                "</if>";
    }

    /**
     * Update中的where结构,用于多个参数时,带@Param("condition")注解时
     *
     * @return the string
     */
    public static String updateByWhereClause() {
        return "<where>\n" +
                "  <foreach collection=\"condition.oredCriteria\" item=\"criteria\">\n" +
                "    <if test=\"criteria.valid\">\n" +
                "      ${@org.aoju.bus.mapper.builder.OGNL@andOr(criteria)}" +
                "      <trim prefix=\"(\" prefixOverrides=\"and |or \" suffix=\")\">\n" +
                "        <foreach collection=\"criteria.criteria\" item=\"criterion\">\n" +
                "          <choose>\n" +
                "            <when test=\"criterion.noValue\">\n" +
                "              ${@org.aoju.bus.mapper.builder.OGNL@andOr(criterion)} ${criterion.condition}\n" +
                "            </when>\n" +
                "            <when test=\"criterion.singleValue\">\n" +
                "              ${@org.aoju.bus.mapper.builder.OGNL@andOr(criterion)} ${criterion.condition} #{criterion.value}\n" +
                "            </when>\n" +
                "            <when test=\"criterion.betweenValue\">\n" +
                "              ${@org.aoju.bus.mapper.builder.OGNL@andOr(criterion)} ${criterion.condition} #{criterion.value} and #{criterion.secondValue}\n" +
                "            </when>\n" +
                "            <when test=\"criterion.listValue\">\n" +
                "              ${@org.aoju.bus.mapper.builder.OGNL@andOr(criterion)} ${criterion.condition}\n" +
                "              <foreach close=\")\" collection=\"criterion.value\" item=\"listItem\" open=\"(\" separator=\",\">\n" +
                "                #{listItem}\n" +
                "              </foreach>\n" +
                "            </when>\n" +
                "          </choose>\n" +
                "        </foreach>\n" +
                "      </trim>\n" +
                "    </if>\n" +
                "  </foreach>\n" +
                "</where>";
    }

}
