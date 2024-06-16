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
package org.miaixz.bus.mapper.builder;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.VersionException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.annotation.LogicDelete;
import org.miaixz.bus.mapper.annotation.Version;
import org.miaixz.bus.mapper.entity.EntityColumn;
import org.miaixz.bus.mapper.entity.TableNames;

import java.util.Set;

/**
 * 拼常用SQL的工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlBuilder {

    /**
     * 获取表名 - 支持动态表名
     *
     * @param entityClass 实体Class对象
     * @param tableName   表名称
     * @return the string
     */
    public static String getDynamicTableName(Class<?> entityClass, String tableName) {
        if (TableNames.class.isAssignableFrom(entityClass)) {
            StringBuilder sql = new StringBuilder();
            sql.append("<choose>");
            sql.append("<when test=\"@org.miaixz.bus.mapper.OGNL@isDynamicParameter(_parameter) and dynamicTableName != null and dynamicTableName != ''\">");
            sql.append("${dynamicTableName}\n");
            sql.append("</when>");
            // 不支持指定列的时候查询全部列
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
     * 获取表名 - 支持动态表名，该方法用于多个入参时，通过parameterName指定入参中实体类的@Param的注解值
     *
     * @param entityClass   实体Class对象
     * @param tableName     表名称
     * @param parameterName 属性名称
     * @return the string
     */
    public static String getDynamicTableName(Class<?> entityClass, String tableName, String parameterName) {
        if (TableNames.class.isAssignableFrom(entityClass)) {
            if (StringKit.isNotEmpty(parameterName)) {
                StringBuilder sql = new StringBuilder();
                sql.append("<choose>");
                sql.append("<when test=\"@org.miaixz.bus.mapper.OGNL@isDynamicParameter(" + parameterName + ") and " + parameterName + ".dynamicTableName != null and " + parameterName + ".dynamicTableName != ''\">");
                sql.append("${" + parameterName + ".dynamicTableName}");
                sql.append("</when>");
                // 不支持指定列的时候查询全部列
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
     * @param value  值
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
     * @param column   列
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
     * @param empty    是否为空
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
     * @param empty    是否为空
     * @return the string
     */
    public static String getIfIsNull(EntityColumn column, String contents, boolean empty) {
        return getIfIsNull(null, column, contents, empty);
    }

    /**
     * 判断自动!=null的条件结构
     *
     * @param entityName 实体映射名
     * @param column     列信息
     * @param contents   内容
     * @param empty      是否为空
     * @return the string
     */
    public static String getIfNotNull(String entityName, EntityColumn column, String contents, boolean empty) {
        StringBuilder sql = new StringBuilder();
        sql.append("<if test=\"");
        if (StringKit.isNotEmpty(entityName)) {
            sql.append(entityName).append(".");
        }
        sql.append(column.getProperty()).append(" != null");
        if (empty && column.getJavaType().equals(String.class)) {
            sql.append(" and ");
            if (StringKit.isNotEmpty(entityName)) {
                sql.append(entityName).append(".");
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
     * @param entityName 实体映射名
     * @param column     列信息
     * @param contents   内容
     * @param empty      是否为空
     * @return the string
     */
    public static String getIfIsNull(String entityName, EntityColumn column, String contents, boolean empty) {
        StringBuilder sql = new StringBuilder();
        sql.append("<if test=\"");
        if (StringKit.isNotEmpty(entityName)) {
            sql.append(entityName).append(".");
        }
        sql.append(column.getProperty()).append(" == null");
        if (empty && column.getJavaType().equals(String.class)) {
            sql.append(" or ");
            if (StringKit.isNotEmpty(entityName)) {
                sql.append(entityName).append(".");
            }
            sql.append(column.getProperty()).append(" == '' ");
        }
        sql.append("\">");
        sql.append(contents);
        sql.append("</if>");
        return sql.toString();
    }

    /**
     * 获取所有查询列，如id,name,code...
     *
     * @param entityClass 实体Class对象
     * @return the string
     */
    public static String getAllColumns(Class<?> entityClass) {
        Set<EntityColumn> columnSet = EntityBuilder.getColumns(entityClass);
        StringBuilder sql = new StringBuilder();
        for (EntityColumn entityColumn : columnSet) {
            sql.append(entityColumn.getColumn()).append(Symbol.COMMA);
        }
        return sql.substring(0, sql.length() - 1);
    }

    /**
     * select xxx,xxx...
     *
     * @param entityClass 实体Class对象
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
     * @param entityClass 实体Class对象
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
     * select case when count(x) 大于 0 then 1 else 0 end
     *
     * @param entityClass 实体Class对象
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
     * @param entityClass      实体Class对象
     * @param defaultTableName 默认表名
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
     * @param entityClass      实体Class对象
     * @param defaultTableName 默认表名
     * @return the string
     */
    public static String updateTable(Class<?> entityClass, String defaultTableName) {
        return updateTable(entityClass, defaultTableName, null);
    }

    /**
     * update tableName - 动态表名
     *
     * @param entityClass      实体Class对象
     * @param defaultTableName 默认表名
     * @param entityName       实体映射名
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
     * @param entityClass      实体Class对象
     * @param defaultTableName 默认表名
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
     * @param entityClass      实体Class对象
     * @param defaultTableName 默认表名
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
     * insert into tableName - 动态表名
     *
     * @param entityClass      实体Class对象
     * @param defaultTableName 默认表名
     * @param parameterName    动态表名的参数名
     * @return the string
     */
    public static String insertIntoTable(Class<?> entityClass, String defaultTableName, String parameterName) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ");
        sql.append(getDynamicTableName(entityClass, defaultTableName, parameterName));
        sql.append(Symbol.SPACE);
        return sql.toString();
    }

    /**
     * insert table()列
     *
     * @param entityClass 实体Class对象
     * @param skipId      是否从列中忽略id类型
     * @param notNull     是否判断!=null
     * @param notEmpty    是否判断String类型!=''
     * @return the string
     */
    public static String insertColumns(Class<?> entityClass, boolean skipId, boolean notNull, boolean notEmpty) {
        StringBuilder sql = new StringBuilder();
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        // 获取全部列
        Set<EntityColumn> columnSet = EntityBuilder.getColumns(entityClass);
        // 当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnSet) {
            if (!column.isInsertable()) {
                continue;
            }
            if (skipId && column.isId()) {
                continue;
            }
            if (notNull) {
                sql.append(SqlBuilder.getIfNotNull(column, column.getColumn() + Symbol.COMMA, notEmpty));
            } else {
                sql.append(column.getColumn() + Symbol.COMMA);
            }
        }
        sql.append("</trim>");
        return sql.toString();
    }

    /**
     * insert-values()列
     *
     * @param entityClass 实体Class对象
     * @param skipId      是否从列中忽略id类型
     * @param notNull     是否判断!=null
     * @param notEmpty    是否判断String类型!=''
     * @return the string
     */
    public static String insertValuesColumns(Class<?> entityClass, boolean skipId, boolean notNull, boolean notEmpty) {
        StringBuilder sql = new StringBuilder();
        sql.append("<trim prefix=\"VALUES (\" suffix=\")\" suffixOverrides=\",\">");
        // 获取全部列
        Set<EntityColumn> columnSet = EntityBuilder.getColumns(entityClass);
        // 当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnSet) {
            if (!column.isInsertable()) {
                continue;
            }
            if (skipId && column.isId()) {
                continue;
            }
            if (notNull) {
                sql.append(SqlBuilder.getIfNotNull(column, column.getColumnHolder() + Symbol.COMMA, notEmpty));
            } else {
                sql.append(column.getColumnHolder() + Symbol.COMMA);
            }
        }
        sql.append("</trim>");
        return sql.toString();
    }

    /**
     * update set列
     *
     * @param entityClass 实体Class对象
     * @param entityName  实体映射名
     * @param notNull     是否判断!=null
     * @param notEmpty    是否判断String类型!=''
     * @return XML中的SET语句块
     */
    public static String updateSetColumns(Class<?> entityClass, String entityName, boolean notNull, boolean notEmpty) {
        StringBuilder sql = new StringBuilder();
        sql.append("<set>");
        // 获取全部列
        Set<EntityColumn> columnSet = EntityBuilder.getColumns(entityClass);
        // 对乐观锁的支持
        EntityColumn versionColumn = null;
        // 逻辑删除列
        EntityColumn logicDeleteColumn = null;
        // 当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnSet) {
            if (column.getEntityField().isAnnotationPresent(Version.class)) {
                if (versionColumn != null) {
                    throw new VersionException(entityClass.getName() + " 中包含多个带有 @Version 注解的字段，一个类中只能存在一个带有 @Version 注解的字段!");
                }
                versionColumn = column;
            }
            if (column.getEntityField().isAnnotationPresent(LogicDelete.class)) {
                if (logicDeleteColumn != null) {
                    throw new VersionException(entityClass.getName() + " 中包含多个带有 @LogicDelete 注解的字段，一个类中只能存在一个带有 @LogicDelete 注解的字段!");
                }
                logicDeleteColumn = column;
            }
            if (!column.isId() && column.isUpdatable()) {
                if (column == versionColumn) {
                    Version version = versionColumn.getEntityField().getAnnotation(Version.class);
                    String versionClass = version.nextVersion().getName();
                    sql.append("<bind name=\"").append(column.getProperty()).append("Version\" value=\"");
                    sql.append("@org.miaixz.bus.mapper.Version@nextVersion(")
                            .append(Symbol.AT).append(versionClass).append("@class, ");
                    if (StringKit.isNotEmpty(entityName)) {
                        sql.append(entityName).append(".");
                    }
                    sql.append(column.getProperty()).append(")\"/>");
                    sql.append(column.getColumn()).append(" = #{").append(column.getProperty()).append("Version},");
                } else if (column == logicDeleteColumn) {
                    sql.append(logicDeleteColumnEqualsValue(column, false)).append(Symbol.COMMA);
                } else if (notNull) {
                    sql.append(SqlBuilder.getIfNotNull(entityName, column, column.getColumnEqualsHolder(entityName) + Symbol.COMMA, notEmpty));
                } else {
                    sql.append(column.getColumnEqualsHolder(entityName) + Symbol.COMMA);
                }
            }
        }
        sql.append("</set>");
        return sql.toString();
    }

    /**
     * update set列，不考虑乐观锁注解 @Version
     *
     * @param entityClass 实体Class对象
     * @param entityName  实体映射名
     * @param notNull     是否判断!=null
     * @param notEmpty    是否判断String类型!=''
     * @return the string
     */
    public static String updateSetColumnsIgnoreVersion(Class<?> entityClass, String entityName, boolean notNull, boolean notEmpty) {
        StringBuilder sql = new StringBuilder();
        sql.append("<set>");
        // 获取全部列
        Set<EntityColumn> columnSet = EntityBuilder.getColumns(entityClass);
        // 逻辑删除列
        EntityColumn logicDeleteColumn = null;
        // 当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnSet) {
            if (column.getEntityField().isAnnotationPresent(LogicDelete.class)) {
                if (logicDeleteColumn != null) {
                    throw new VersionException(entityClass.getName() + " 中包含多个带有 @LogicDelete 注解的字段，一个类中只能存在一个带有 @LogicDelete 注解的字段!");
                }
                logicDeleteColumn = column;
            }
            if (!column.isId() && column.isUpdatable()) {
                if (column.getEntityField().isAnnotationPresent(Version.class)) {
                    //ignore
                } else if (column == logicDeleteColumn) {
                    sql.append(logicDeleteColumnEqualsValue(column, false)).append(Symbol.COMMA);
                } else if (notNull) {
                    sql.append(SqlBuilder.getIfNotNull(entityName, column, column.getColumnEqualsHolder(entityName) + Symbol.COMMA, notEmpty));
                } else {
                    sql.append(column.getColumnEqualsHolder(entityName) + Symbol.COMMA);
                }
            } else if (column.isId() && column.isUpdatable()) {
                //set id = id,
                sql.append(column.getColumn()).append(" = ").append(column.getColumn()).append(Symbol.COMMA);
            }
        }
        sql.append("</set>");
        return sql.toString();
    }

    /**
     * 不是所有参数都是 null 的检查
     *
     * @param parameterName 参数名
     * @param columnSet     需要检查的列
     * @return the string
     */
    public static String notAllNullParameterCheck(String parameterName, Set<EntityColumn> columnSet) {
        StringBuilder sql = new StringBuilder();
        sql.append("<bind name=\"notAllNullParameterCheck\" value=\"@org.miaixz.bus.mapper.OGNL@notAllNullParameterCheck(");
        sql.append(parameterName).append(", '");
        StringBuilder fields = new StringBuilder();
        for (EntityColumn column : columnSet) {
            if (fields.length() > 0) {
                fields.append(Symbol.COMMA);
            }
            fields.append(column.getProperty());
        }
        sql.append(fields);
        sql.append("')\"/>");
        return sql.toString();
    }

    /**
     * Condition 中包含至少 1 个查询条件
     *
     * @param parameterName 参数名
     * @return the string
     */
    public static String conditionHasAtLeastOneCriteriaCheck(String parameterName) {
        StringBuilder sql = new StringBuilder();
        sql.append("<bind name=\"conditionHasAtLeastOneCriteriaCheck\" value=\"@org.miaixz.bus.mapper.OGNL@conditionHasAtLeastOneCriteriaCheck(");
        sql.append(parameterName).append(")\"/>");
        return sql.toString();
    }

    /**
     * where主键条件
     *
     * @param entityClass 实体Class对象
     * @return the string
     */
    public static String wherePKColumns(Class<?> entityClass) {
        return wherePKColumns(entityClass, false);
    }

    /**
     * where主键条件
     *
     * @param entityClass 实体Class对象
     * @param useVersion  版本条件
     * @return the string
     */
    public static String wherePKColumns(Class<?> entityClass, boolean useVersion) {
        return wherePKColumns(entityClass, null, useVersion);
    }

    /**
     * where主键条件
     *
     * @param entityClass 实体Class对象
     * @param entityName  实体映射名
     * @param useVersion  版本条件
     * @return the string
     */
    public static String wherePKColumns(Class<?> entityClass, String entityName, boolean useVersion) {
        StringBuilder sql = new StringBuilder();
        boolean hasLogicDelete = hasLogicDeleteColumn(entityClass);

        sql.append("<where>");
        // 获取全部列
        Set<EntityColumn> columnSet = EntityBuilder.getPKColumns(entityClass);
        // 当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnSet) {
            sql.append(" AND ").append(column.getColumnEqualsHolder(entityName));
        }
        if (useVersion) {
            sql.append(whereVersion(entityClass));
        }

        if (hasLogicDelete) {
            sql.append(whereLogicDelete(entityClass, false));
        }

        sql.append("</where>");
        return sql.toString();
    }

    /**
     * where所有列的条件，会判断是否!=null
     *
     * @param entityClass 实体Class对象
     * @param empty       是否为空
     * @return the string
     */
    public static String whereAllIfColumns(Class<?> entityClass, boolean empty) {
        return whereAllIfColumns(entityClass, empty, false);
    }

    /**
     * where所有列的条件，会判断是否!=null
     *
     * @param entityClass 实体Class对象
     * @param empty       是否为空
     * @param useVersion  版本条件
     * @return the string
     */
    public static String whereAllIfColumns(Class<?> entityClass, boolean empty, boolean useVersion) {
        StringBuilder sql = new StringBuilder();
        boolean hasLogicDelete = false;

        sql.append("<where>");
        //获取全部列
        Set<EntityColumn> columnSet = EntityBuilder.getColumns(entityClass);
        EntityColumn logicDeleteColumn = SqlBuilder.getLogicDeleteColumn(entityClass);
        //当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnSet) {
            if (!useVersion || !column.getEntityField().isAnnotationPresent(Version.class)) {
                // 逻辑删除，后面拼接逻辑删除字段的未删除条件
                if (logicDeleteColumn != null && logicDeleteColumn == column) {
                    hasLogicDelete = true;
                    continue;
                }
                sql.append(getIfNotNull(column, " AND " + column.getColumnEqualsHolder(), empty));
            }
        }
        if (useVersion) {
            sql.append(whereVersion(entityClass));
        }
        if (hasLogicDelete) {
            sql.append(whereLogicDelete(entityClass, false));
        }

        sql.append("</where>");
        return sql.toString();
    }

    /**
     * 乐观锁字段条件
     *
     * @param entityClass 实体Class对象
     * @return the string
     */
    public static String whereVersion(Class<?> entityClass) {
        return whereVersion(entityClass, null);
    }

    /**
     * 乐观锁字段条件
     *
     * @param entityClass
     * @param entityName  实体名称
     * @return the string
     */
    public static String whereVersion(Class<?> entityClass, String entityName) {
        Set<EntityColumn> columnSet = EntityBuilder.getColumns(entityClass);
        boolean hasVersion = false;
        String result = Normal.EMPTY;
        for (EntityColumn column : columnSet) {
            if (column.getEntityField().isAnnotationPresent(Version.class)) {
                if (hasVersion) {
                    throw new VersionException(entityClass.getName() + " 中包含多个带有 @Version 注解的字段，一个类中只能存在一个带有 @Version 注解的字段!");
                }
                hasVersion = true;
                result = " AND " + column.getColumnEqualsHolder(entityName);
            }
        }
        return result;
    }

    /**
     * 逻辑删除的where条件，没有逻辑删除注解则返回空字符串
     * AND column = value
     *
     * @param entityClass 实体Class对象
     * @param isDeleted   true：已经逻辑删除，false：未逻辑删除
     * @return the string
     */
    public static String whereLogicDelete(Class<?> entityClass, boolean isDeleted) {
        String value = logicDeleteColumnEqualsValue(entityClass, isDeleted);
        return "".equals(value) ? Normal.EMPTY : " AND " + value;
    }

    /**
     * 返回格式: column = value
     * 默认isDeletedValue = 1  notDeletedValue = 0
     * 则返回is_deleted = 1 或 is_deleted = 0
     * 若没有逻辑删除注解，则返回空字符串
     *
     * @param entityClass 实体Class对象
     * @param isDeleted   true 已经逻辑删除  false 未逻辑删除
     * @return the string
     */
    public static String logicDeleteColumnEqualsValue(Class<?> entityClass, boolean isDeleted) {
        EntityColumn logicDeleteColumn = SqlBuilder.getLogicDeleteColumn(entityClass);

        if (logicDeleteColumn != null) {
            return logicDeleteColumnEqualsValue(logicDeleteColumn, isDeleted);
        }

        return Normal.EMPTY;
    }

    /**
     * 返回格式: column = value
     * 默认isDeletedValue = 1  notDeletedValue = 0
     * 则返回is_deleted = 1 或 is_deleted = 0
     * 若没有逻辑删除注解，则返回空字符串
     *
     * @param column    列
     * @param isDeleted true 已经逻辑删除  false 未逻辑删除
     * @return the string
     */
    public static String logicDeleteColumnEqualsValue(EntityColumn column, boolean isDeleted) {
        String result = Normal.EMPTY;
        if (column.getEntityField().isAnnotationPresent(LogicDelete.class)) {
            Integer logicDeletedValue = getLogicDeletedValue(column, isDeleted);
            if (logicDeletedValue == null) {
                result = column.getColumn() + " is null ";
            } else {
                result = column.getColumn() + " = " + logicDeletedValue;
            }
        }
        return result;
    }

    /**
     * 获取逻辑删除注解的参数值
     *
     * @param column    列
     * @param isDeleted true：逻辑删除的值，false：未逻辑删除的值
     * @return the int
     */
    public static Integer getLogicDeletedValue(EntityColumn column, boolean isDeleted) {
        if (!column.getEntityField().isAnnotationPresent(LogicDelete.class)) {
            throw new VersionException(column.getColumn() + " 没有 @LogicDelete 注解!");
        }
        LogicDelete logicDelete = column.getEntityField().getAnnotation(LogicDelete.class);
        if (isDeleted) {
            return logicDelete.isNullForDeletedValue() ? null : logicDelete.isDeletedValue();
        }
        return logicDelete.isNullForNotDeletedValue() ? null : logicDelete.notDeletedValue();
    }

    /**
     * 是否有逻辑删除的注解
     *
     * @param entityClass 实体Class对象
     * @return the string
     */
    public static boolean hasLogicDeleteColumn(Class<?> entityClass) {
        return getLogicDeleteColumn(entityClass) != null;
    }

    /**
     * 获取逻辑删除注解的列，若没有返回null
     *
     * @param entityClass 实体Class对象
     * @return the string
     */
    public static EntityColumn getLogicDeleteColumn(Class<?> entityClass) {
        EntityColumn logicDeleteColumn = null;
        Set<EntityColumn> columnSet = EntityBuilder.getColumns(entityClass);
        boolean hasLogicDelete = false;
        for (EntityColumn column : columnSet) {
            if (column.getEntityField().isAnnotationPresent(LogicDelete.class)) {
                if (hasLogicDelete) {
                    throw new VersionException(entityClass.getName() + " 中包含多个带有 @LogicDelete 注解的字段，一个类中只能存在一个带有 @LogicDelete 注解的字段!");
                }
                hasLogicDelete = true;
                logicDeleteColumn = column;
            }
        }
        return logicDeleteColumn;
    }

    /**
     * 获取默认的orderBy，通过注解设置的
     *
     * @param entityClass 实体Class对象
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
     * condition支持查询指定列时
     *
     * @param entityClass 实体Class对象
     * @return the string
     */
    public static String conditionSelectColumns(Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("<choose>");
        sql.append("<when test=\"@org.miaixz.bus.mapper.OGNL@hasSelectColumns(_parameter)\">");
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
     * condition支持查询指定列时
     *
     * @param entityClass 实体Class对象
     * @return the string
     */
    public static String conditionCountColumn(Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("<choose>");
        sql.append("<when test=\"@org.miaixz.bus.mapper.OGNL@hasCountColumn(_parameter)\">");
        sql.append("COUNT(<if test=\"distinct\">distinct </if>${countColumn})");
        sql.append("</when>");
        sql.append("<otherwise>");
        sql.append("COUNT(*)");
        sql.append("</otherwise>");
        sql.append("</choose>");
        return sql.toString();
    }

    /**
     * condition查询中的orderBy条件，会判断默认orderBy
     *
     * @param entityClass 实体Class对象
     * @return the string
     */
    public static String conditionOrderBy(Class<?> entityClass) {
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
     * condition查询中的orderBy条件，会判断默认orderBy
     *
     * @param entityName  实体映射名
     * @param entityClass 实体Class对象
     * @return the string
     */
    public static String conditionOrderBy(String entityName, Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("<if test=\"").append(entityName).append(".orderByClause != null\">");
        sql.append("order by ${").append(entityName).append(".orderByClause}");
        sql.append("</if>");
        String orderByClause = EntityBuilder.getOrderByClause(entityClass);
        if (orderByClause.length() > 0) {
            sql.append("<if test=\"").append(entityName).append(".orderByClause == null\">");
            sql.append("ORDER BY " + orderByClause);
            sql.append("</if>");
        }
        return sql.toString();
    }

    /**
     * condition 支持 for update
     *
     * @return the string
     */
    public static String conditionForUpdate() {
        StringBuilder sql = new StringBuilder();
        sql.append("<if test=\"@org.miaixz.bus.mapper.OGNL@hasForUpdate(_parameter)\">");
        sql.append("FOR UPDATE");
        sql.append("</if>");
        return sql.toString();
    }

    /**
     * condition 支持 for update
     *
     * @param entityClass 实体Class对象
     * @return the string
     */
    public static String conditionCheck(Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("<bind name=\"checkConditionEntityClass\" value=\"@org.miaixz.bus.mapper.OGNL@checkConditionEntityClass(_parameter, '");
        sql.append(entityClass.getName());
        sql.append("')\"/>");
        return sql.toString();
    }

    /**
     * Condition查询中的where结构，用于只有一个Condition参数时
     *
     * @return the string
     */
    public static String conditionWhereClause() {
        return "<if test=\"_parameter != null\">" +
                "<where>\n" +
                " ${@org.miaixz.bus.mapper.OGNL@andNotLogicDelete(_parameter)}" +
                " <trim prefix=\"(\" prefixOverrides=\"and |or \" suffix=\")\">\n" +
                "  <foreach collection=\"oredCriteria\" item=\"criteria\">\n" +
                "    <if test=\"criteria.valid\">\n" +
                "      ${@org.miaixz.bus.mapper.OGNL@andOr(criteria)}" +
                "      <trim prefix=\"(\" prefixOverrides=\"and |or \" suffix=\")\">\n" +
                "        <foreach collection=\"criteria.criteria\" item=\"criterion\">\n" +
                "          <choose>\n" +
                "            <when test=\"criterion.noValue\">\n" +
                "              ${@org.miaixz.bus.mapper.OGNL@andOr(criterion)} ${criterion.condition}\n" +
                "            </when>\n" +
                "            <when test=\"criterion.singleValue\">\n" +
                "              ${@org.miaixz.bus.mapper.OGNL@andOr(criterion)} ${criterion.condition} #{criterion.value}\n" +
                "            </when>\n" +
                "            <when test=\"criterion.betweenValue\">\n" +
                "              ${@org.miaixz.bus.mapper.OGNL@andOr(criterion)} ${criterion.condition} #{criterion.value} and #{criterion.secondValue}\n" +
                "            </when>\n" +
                "            <when test=\"criterion.listValue\">\n" +
                "              ${@org.miaixz.bus.mapper.OGNL@andOr(criterion)} ${criterion.condition}\n" +
                "              <foreach close=\")\" collection=\"criterion.value\" item=\"listItem\" open=\"(\" separator=\",\">\n" +
                "                #{listItem}\n" +
                "              </foreach>\n" +
                "            </when>\n" +
                "          </choose>\n" +
                "        </foreach>\n" +
                "      </trim>\n" +
                "    </if>\n" +
                "  </foreach>\n" +
                " </trim>\n" +
                "</where>" +
                "</if>";
    }

    /**
     * Condition-Update中的where结构，用于多个参数时，Condition带@Param("condition")注解时
     *
     * @return the string
     */
    public static String updateByConditionWhereClause() {
        return "<where>\n" +
                " ${@org.miaixz.bus.mapper.OGNL@andNotLogicDelete(condition)}" +
                " <trim prefix=\"(\" prefixOverrides=\"and |or \" suffix=\")\">\n" +
                "  <foreach collection=\"condition.oredCriteria\" item=\"criteria\">\n" +
                "    <if test=\"criteria.valid\">\n" +
                "      ${@org.miaixz.bus.mapper.OGNL@andOr(criteria)}" +
                "      <trim prefix=\"(\" prefixOverrides=\"and |or \" suffix=\")\">\n" +
                "        <foreach collection=\"criteria.criteria\" item=\"criterion\">\n" +
                "          <choose>\n" +
                "            <when test=\"criterion.noValue\">\n" +
                "              ${@org.miaixz.bus.mapper.OGNL@andOr(criterion)} ${criterion.condition}\n" +
                "            </when>\n" +
                "            <when test=\"criterion.singleValue\">\n" +
                "              ${@org.miaixz.bus.mapper.OGNL@andOr(criterion)} ${criterion.condition} #{criterion.value}\n" +
                "            </when>\n" +
                "            <when test=\"criterion.betweenValue\">\n" +
                "              ${@org.miaixz.bus.mapper.OGNL@andOr(criterion)} ${criterion.condition} #{criterion.value} and #{criterion.secondValue}\n" +
                "            </when>\n" +
                "            <when test=\"criterion.listValue\">\n" +
                "              ${@org.miaixz.bus.mapper.OGNL@andOr(criterion)} ${criterion.condition}\n" +
                "              <foreach close=\")\" collection=\"criterion.value\" item=\"listItem\" open=\"(\" separator=\",\">\n" +
                "                #{listItem}\n" +
                "              </foreach>\n" +
                "            </when>\n" +
                "          </choose>\n" +
                "        </foreach>\n" +
                "      </trim>\n" +
                "    </if>\n" +
                "  </foreach>\n" +
                " </trim>\n" +
                "</where>";
    }

}
