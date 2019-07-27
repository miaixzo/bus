package org.aoju.bus.mapper.provider;

import org.aoju.bus.mapper.builder.MapperBuilder;
import org.aoju.bus.mapper.builder.MapperTemplate;
import org.aoju.bus.mapper.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * BaseSelectProvider实现类，基础方法实现类
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class BaseSelectProvider extends MapperTemplate {

    public BaseSelectProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * 查询
     *
     * @param ms
     * @return
     */
    public String selectOne(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        //修改返回值类型为实体类型
        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlSourceBuilder.selectAllColumns(entityClass));
        sql.append(SqlSourceBuilder.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlSourceBuilder.whereAllIfColumns(entityClass, isNotEmpty()));
        return sql.toString();
    }

    /**
     * 查询
     *
     * @param ms
     * @return
     */
    public String select(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        //修改返回值类型为实体类型
        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlSourceBuilder.selectAllColumns(entityClass));
        sql.append(SqlSourceBuilder.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlSourceBuilder.whereAllIfColumns(entityClass, isNotEmpty()));
        sql.append(SqlSourceBuilder.orderByDefault(entityClass));
        return sql.toString();
    }

    /**
     * 查询
     *
     * @param ms
     * @return
     */
    public String selectByRowBounds(MappedStatement ms) {
        return select(ms);
    }

    /**
     * 根据主键进行查询
     *
     * @param ms
     */
    public String selectByPrimaryKey(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        //将返回值修改为实体类型
        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlSourceBuilder.selectAllColumns(entityClass));
        sql.append(SqlSourceBuilder.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlSourceBuilder.wherePKColumns(entityClass));
        return sql.toString();
    }

    /**
     * 查询总数
     *
     * @param ms
     * @return
     */
    public String selectCount(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlSourceBuilder.selectCount(entityClass));
        sql.append(SqlSourceBuilder.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlSourceBuilder.whereAllIfColumns(entityClass, isNotEmpty()));
        return sql.toString();
    }

    /**
     * 根据主键查询总数
     *
     * @param ms
     * @return
     */
    public String existsWithPrimaryKey(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlSourceBuilder.selectCountExists(entityClass));
        sql.append(SqlSourceBuilder.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlSourceBuilder.wherePKColumns(entityClass));
        return sql.toString();
    }

    /**
     * 查询全部结果
     *
     * @param ms
     * @return
     */
    public String selectAll(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        //修改返回值类型为实体类型
        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlSourceBuilder.selectAllColumns(entityClass));
        sql.append(SqlSourceBuilder.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlSourceBuilder.orderByDefault(entityClass));
        return sql.toString();
    }

}
