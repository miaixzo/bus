package org.aoju.bus.mapper.provider;

import org.aoju.bus.mapper.builder.EntityBuilder;
import org.aoju.bus.mapper.builder.MapperBuilder;
import org.aoju.bus.mapper.builder.MapperTemplate;
import org.aoju.bus.mapper.builder.SqlSourceBuilder;
import org.aoju.bus.mapper.entity.EntityColumn;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.Set;

/**
 * 批量操作
 * <p/>
 * ids 如 "1,2,3"
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class InsertListProvider extends MapperTemplate {

    public InsertListProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * 批量插入
     *
     * @param ms
     */
    public String insertList(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        //开始拼sql
        StringBuilder sql = new StringBuilder();
        sql.append(SqlSourceBuilder.insertIntoTable(entityClass, tableName(entityClass)))
                .append(SqlSourceBuilder.insertColumns(entityClass, false, false, false))
                .append(" VALUES ")
                .append("<foreach collection=\"list\" item=\"record\" separator=\",\" >")
                .append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        //获取全部列
        Set<EntityColumn> columnList = EntityBuilder.getColumns(entityClass);
        //当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnList) {
            if (column.isInsertable()) {
                sql.append(column.getColumnHolder("record") + ",");
            }
        }
        sql.append("</trim>");
        sql.append("</foreach>");
        return sql.toString();
    }

    /**
     * 批量插入
     *
     * @param ms
     */
    public String insertListNoId(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        //开始拼sql
        StringBuilder sql = new StringBuilder();
        sql.append(SqlSourceBuilder.insertIntoTable(entityClass, tableName(entityClass)))
                .append(SqlSourceBuilder.insertColumns(entityClass, true, false, false))
                .append(" VALUES ")
                .append("<foreach collection=\"list\" item=\"record\" separator=\",\" >")
                .append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        //获取全部列
        Set<EntityColumn> columnList = EntityBuilder.getColumns(entityClass);
        //当某个列有主键策略时，不需要考虑他的属性是否为空，因为如果为空，一定会根据主键策略给他生成一个值
        for (EntityColumn column : columnList) {
            if (!column.isId() && column.isInsertable()) {
                sql.append(column.getColumnHolder("record") + ",");
            }
        }
        sql.append("</trim>")
                .append("</foreach>");
        return sql.toString();
    }

    /**
     * 插入，主键id，自增
     *
     * @param ms
     */
    public String insertUseGeneratedKey(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        //开始拼sql
        StringBuilder sql = new StringBuilder();
        sql.append(SqlSourceBuilder.insertIntoTable(entityClass, tableName(entityClass)))
                .append(SqlSourceBuilder.insertColumns(entityClass, true, false, false))
                .append(SqlSourceBuilder.insertValuesColumns(entityClass, true, false, false));
        return sql.toString();
    }

}
