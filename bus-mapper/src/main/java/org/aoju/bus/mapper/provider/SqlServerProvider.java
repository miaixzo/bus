package org.aoju.bus.mapper.provider;

import org.aoju.bus.mapper.builder.MapperBuilder;
import org.aoju.bus.mapper.builder.MapperTemplate;
import org.aoju.bus.mapper.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * SqlServerProvider实现类，特殊方法实现类
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class SqlServerProvider extends MapperTemplate {

    public SqlServerProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * 插入
     *
     * @param ms
     */
    public String insert(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        //开始拼sql
        StringBuilder sql = new StringBuilder();
        sql.append(SqlSourceBuilder.insertIntoTable(entityClass, tableName(entityClass)))
                .append(SqlSourceBuilder.insertColumns(entityClass, true, false, false))
                .append(SqlSourceBuilder.insertValuesColumns(entityClass, true, false, false));
        return sql.toString();
    }

    /**
     * 插入不为null的字段
     *
     * @param ms
     * @return
     */
    public String insertSelective(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlSourceBuilder.insertIntoTable(entityClass, tableName(entityClass)))
                .append(SqlSourceBuilder.insertColumns(entityClass, true, true, isNotEmpty()))
                .append(SqlSourceBuilder.insertValuesColumns(entityClass, true, true, isNotEmpty()));
        return sql.toString();
    }

}
