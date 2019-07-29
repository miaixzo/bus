package org.aoju.bus.mapper.provider;

import org.aoju.bus.mapper.builder.MapperBuilder;
import org.aoju.bus.mapper.builder.MapperTemplate;
import org.aoju.bus.mapper.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * ConditionProvider实现类，基础方法实现类
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class ConditionProvider extends MapperTemplate {


    public ConditionProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * 根据Condition查询总数
     *
     * @param ms
     * @return
     */
    public String selectCountByCondition(MappedStatement ms) {
        return selectCountByWhere(ms);
    }

    /**
     * 根据Condition删除
     *
     * @param ms
     * @return
     */
    public String deleteByCondition(MappedStatement ms) {
        return deleteByWhere(ms);
    }


    /**
     * 根据Condition查询
     *
     * @param ms
     * @return
     */
    public String selectByCondition(MappedStatement ms) {
        return selectByWhere(ms);
    }

    /**
     * 根据Condition查询
     *
     * @param ms
     * @return
     */
    public String selectByConditionAndRowBounds(MappedStatement ms) {
        return selectByWhere(ms);
    }

    /**
     * 根据Condition更新非null字段
     *
     * @param ms
     * @return
     */
    public String updateByConditionSelective(MappedStatement ms) {
        return updateByWhereSelective(ms);
    }

    /**
     * 根据Condition更新
     *
     * @param ms
     * @return
     */
    public String updateByCondition(MappedStatement ms) {
        return updateByWhere(ms);
    }


    /**
     * 根据Condition查询总数
     *
     * @param ms
     * @return
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
     * @param ms
     * @return
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
     * @param ms
     * @return
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
     * @param ms
     * @return
     */
    public String selectByWhereAndRowBounds(MappedStatement ms) {
        return selectByWhere(ms);
    }

    /**
     * 根据Condition更新非null字段
     *
     * @param ms
     * @return
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
     * @param ms
     * @return
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
     * @param ms
     * @return
     */
    public String selectOneByWhere(MappedStatement ms) {
        return selectByWhere(ms);
    }

}
