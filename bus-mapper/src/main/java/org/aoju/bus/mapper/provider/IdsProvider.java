package org.aoju.bus.mapper.provider;

import org.aoju.bus.mapper.MapperException;
import org.aoju.bus.mapper.builder.EntityBuilder;
import org.aoju.bus.mapper.builder.MapperBuilder;
import org.aoju.bus.mapper.builder.MapperTemplate;
import org.aoju.bus.mapper.builder.SqlSourceBuilder;
import org.aoju.bus.mapper.entity.EntityColumn;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.Set;

/**
 * 通过 ids 字符串的各种操作
 * <p/>
 * ids 如 "1,2,3"
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class IdsProvider extends MapperTemplate {

    public IdsProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * 根据主键字符串进行删除，类中只有存在一个带有@Id注解的字段
     *
     * @param ms
     * @return
     */
    public String deleteByIds(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlSourceBuilder.deleteFromTable(entityClass, tableName(entityClass)));
        Set<EntityColumn> columnList = EntityBuilder.getPKColumns(entityClass);
        if (columnList.size() == 1) {
            EntityColumn column = columnList.iterator().next();
            sql.append(" where ")
                    .append(column.getColumn())
                    .append(" in (${_parameter})");
        } else {
            throw new MapperException("继承 deleteByIds 方法的实体类[" + entityClass.getCanonicalName() + "]中必须只有一个带有 @Id 注解的字段");
        }
        return sql.toString();
    }

    /**
     * 根据主键字符串进行查询，类中只有存在一个带有@Id注解的字段
     *
     * @param ms
     * @return
     */
    public String selectByIds(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);
        //将返回值修改为实体类型
        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlSourceBuilder.selectAllColumns(entityClass))
                .append(SqlSourceBuilder.fromTable(entityClass, tableName(entityClass)));
        Set<EntityColumn> columnList = EntityBuilder.getPKColumns(entityClass);
        if (columnList.size() == 1) {
            EntityColumn column = columnList.iterator().next();
            sql.append(" where ")
                    .append(column.getColumn())
                    .append(" in (${_parameter})");
        } else {
            throw new MapperException("继承 selectByIds 方法的实体类[" + entityClass.getCanonicalName() + "]中必须只有一个带有 @Id 注解的字段");
        }
        return sql.toString();
    }

}
