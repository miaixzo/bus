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
package org.miaixz.bus.mapper.provider;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.miaixz.bus.mapper.builder.MapperBuilder;
import org.miaixz.bus.mapper.builder.MapperTemplate;
import org.miaixz.bus.mapper.builder.SqlBuilder;
import org.miaixz.bus.mapper.support.MetaObject;

/**
 * ConditionProvider实现类，基础方法实现类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConditionProvider extends MapperTemplate {

    public ConditionProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * 根据Condition查询总数
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectCountByCondition(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder("SELECT ");
        if (isCheckConditionEntityClass()) {
            sql.append(SqlBuilder.conditionCheck(entityClass));
        }
        sql.append(SqlBuilder.conditionCountColumn(entityClass));
        sql.append(SqlBuilder.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlBuilder.conditionWhereClause());
        sql.append(SqlBuilder.conditionForUpdate());
        return sql.toString();
    }

    /**
     * 根据Condition删除
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String deleteByCondition(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        if (isCheckConditionEntityClass()) {
            sql.append(SqlBuilder.conditionCheck(entityClass));
        }
        // 如果设置了安全删除，就不允许执行不带查询条件的 delete 方法
        if (getConfig().isSafeDelete()) {
            sql.append(SqlBuilder.conditionHasAtLeastOneCriteriaCheck("_parameter"));
        }
        if (SqlBuilder.hasLogicDeleteColumn(entityClass)) {
            sql.append(SqlBuilder.updateTable(entityClass, tableName(entityClass)));
            sql.append("<set>");
            sql.append(SqlBuilder.logicDeleteColumnEqualsValue(entityClass, true));
            sql.append("</set>");
            MetaObject.forObject(ms).setValue("sqlCommandType", SqlCommandType.UPDATE);
        } else {
            sql.append(SqlBuilder.deleteFromTable(entityClass, tableName(entityClass)));
        }
        sql.append(SqlBuilder.conditionWhereClause());
        return sql.toString();
    }

    /**
     * 根据Condition查询
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectByCondition(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        // 将返回值修改为实体类型
        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder("SELECT ");
        if (isCheckConditionEntityClass()) {
            sql.append(SqlBuilder.conditionCheck(entityClass));
        }
        sql.append("<if test=\"distinct\">distinct</if>");
        // 支持查询指定列
        sql.append(SqlBuilder.conditionSelectColumns(entityClass));
        sql.append(SqlBuilder.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlBuilder.conditionWhereClause());
        sql.append(SqlBuilder.conditionOrderBy(entityClass));
        sql.append(SqlBuilder.conditionForUpdate());
        return sql.toString();
    }

    /**
     * 根据Condition查询
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectByConditionAndRowBounds(MappedStatement ms) {
        return selectByCondition(ms);
    }

    /**
     * 根据Condition更新非null字段
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String updateByConditionSelective(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        if (isCheckConditionEntityClass()) {
            sql.append(SqlBuilder.conditionCheck(entityClass));
        }
        // 安全更新，Condition 必须包含条件
        if (getConfig().isSafeUpdate()) {
            sql.append(SqlBuilder.conditionHasAtLeastOneCriteriaCheck("condition"));
        }
        sql.append(SqlBuilder.updateTable(entityClass, tableName(entityClass), "condition"));
        sql.append(SqlBuilder.updateSetColumnsIgnoreVersion(entityClass, "record", true, isNotEmpty()));
        sql.append(SqlBuilder.updateByConditionWhereClause());
        return sql.toString();
    }

    /**
     * 根据Condition更新
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String updateByCondition(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        if (isCheckConditionEntityClass()) {
            sql.append(SqlBuilder.conditionCheck(entityClass));
        }
        // 安全更新，Condition 必须包含条件
        if (getConfig().isSafeUpdate()) {
            sql.append(SqlBuilder.conditionHasAtLeastOneCriteriaCheck("condition"));
        }
        sql.append(SqlBuilder.updateTable(entityClass, tableName(entityClass), "condition"));
        sql.append(SqlBuilder.updateSetColumnsIgnoreVersion(entityClass, "record", false, false));
        sql.append(SqlBuilder.updateByConditionWhereClause());
        return sql.toString();
    }

    /**
     * 根据Condition查询一个结果
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectOneByCondition(MappedStatement ms) {
        return selectByCondition(ms);
    }

}
