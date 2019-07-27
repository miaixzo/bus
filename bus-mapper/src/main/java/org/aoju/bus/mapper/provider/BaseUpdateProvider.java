package org.aoju.bus.mapper.provider;

import org.aoju.bus.mapper.builder.MapperBuilder;
import org.aoju.bus.mapper.builder.MapperTemplate;
import org.aoju.bus.mapper.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * BaseUpdateProvider实现类，基础方法实现类
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class BaseUpdateProvider extends MapperTemplate {

    public BaseUpdateProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * 通过主键更新全部字段
     *
     * @param ms
     */
    public String updateByPrimaryKey(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlSourceBuilder.updateTable(entityClass, tableName(entityClass)));
        sql.append(SqlSourceBuilder.updateSetColumns(entityClass, null, false, false));
        sql.append(SqlSourceBuilder.wherePKColumns(entityClass, true));
        return sql.toString();
    }

    /**
     * 通过主键更新不为null的字段
     *
     * @param ms
     * @return
     */
    public String updateByPrimaryKeySelective(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlSourceBuilder.updateTable(entityClass, tableName(entityClass)));
        sql.append(SqlSourceBuilder.updateSetColumns(entityClass, null, true, isNotEmpty()));
        sql.append(SqlSourceBuilder.wherePKColumns(entityClass, true));
        return sql.toString();
    }

}
