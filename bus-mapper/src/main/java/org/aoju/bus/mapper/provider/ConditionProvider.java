/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.mapper.provider;

import org.aoju.bus.mapper.builder.MapperBuilder;
import org.aoju.bus.mapper.builder.MapperTemplate;
import org.aoju.bus.mapper.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * ConditionProvider实现类,基础方法实现类
 *
 * @author Kimi Liu
 * @version 6.0.9
 * @since JDK 1.8+
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
        return selectCountByWhere(ms);
    }

    /**
     * 根据Condition删除
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String deleteByCondition(MappedStatement ms) {
        return deleteByWhere(ms);
    }


    /**
     * 根据Condition查询
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectByCondition(MappedStatement ms) {
        return selectByWhere(ms);
    }

    /**
     * 根据Condition查询
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectByConditionAndRowBounds(MappedStatement ms) {
        return selectByWhere(ms);
    }

    /**
     * 根据Condition更新非null字段
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String updateByConditionSelective(MappedStatement ms) {
        return updateByWhereSelective(ms);
    }

    /**
     * 根据Condition更新
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String updateByCondition(MappedStatement ms) {
        return updateByWhere(ms);
    }


    /**
     * 根据Condition查询总数
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectCountByWhere(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        if (isCheckEntityClass()) {
            sql.append(SqlSourceBuilder.check(entityClass));
        }
        sql.append(SqlSourceBuilder.selectCount(entityClass))
                .append(SqlSourceBuilder.fromTable(entityClass, tableName(entityClass)))
                .append(SqlSourceBuilder.whereClause())
                .append(SqlSourceBuilder.forUpdate());
        return sql.toString();
    }

    /**
     * 根据Condition删除
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String deleteByWhere(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        if (isCheckEntityClass()) {
            sql.append(SqlSourceBuilder.check(entityClass));
        }
        sql.append(SqlSourceBuilder.deleteFromTable(entityClass, tableName(entityClass)))
                .append(SqlSourceBuilder.whereClause());
        return sql.toString();
    }


    /**
     * 根据Condition查询
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectByWhere(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        //将返回值修改为实体类型
        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder("SELECT ");
        if (isCheckEntityClass()) {
            sql.append(SqlSourceBuilder.check(entityClass));
        }
        sql.append("<if test=\"distinct\">distinct</if>")
                //支持查询指定列
                .append(SqlSourceBuilder.selectColumns(entityClass))
                .append(SqlSourceBuilder.fromTable(entityClass, tableName(entityClass)))
                .append(SqlSourceBuilder.whereClause())
                .append(SqlSourceBuilder.orderBy(entityClass))
                .append(SqlSourceBuilder.forUpdate());
        return sql.toString();
    }

    /**
     * 根据Condition查询
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectByWhereAndRowBounds(MappedStatement ms) {
        return selectByWhere(ms);
    }

    /**
     * 根据Condition更新非null字段
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String updateByWhereSelective(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        if (isCheckEntityClass()) {
            sql.append(SqlSourceBuilder.check(entityClass));
        }
        sql.append(SqlSourceBuilder.updateTable(entityClass, tableName(entityClass), "condition"))
                .append(SqlSourceBuilder.updateSetColumns(entityClass, "record", true, isNotEmpty()))
                .append(SqlSourceBuilder.updateByWhereClause());
        return sql.toString();
    }

    /**
     * 根据Condition更新
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String updateByWhere(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        if (isCheckEntityClass()) {
            sql.append(SqlSourceBuilder.check(entityClass));
        }
        sql.append(SqlSourceBuilder.updateTable(entityClass, tableName(entityClass), "condition"))
                .append(SqlSourceBuilder.updateSetColumns(entityClass, "record", false, false))
                .append(SqlSourceBuilder.updateByWhereClause());
        return sql.toString();
    }

    /**
     * 根据Condition查询一个结果
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String selectOneByWhere(MappedStatement ms) {
        return selectByWhere(ms);
    }

}
