/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mybatis.io and other contributors.         ~
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

import java.lang.reflect.Field;
import java.util.Set;

import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.MapperException;
import org.miaixz.bus.mapper.builder.*;
import org.miaixz.bus.mapper.entity.EntityColumn;

/**
 * 保存实现类
 */
public class SaveProvider extends MapperTemplate {

    public SaveProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * 保存策略: 如果主键不为空则更新记录, 如果没有主键或者主键为空,则插入.
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String save(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        Field[] fields = entityClass.getFields();
        StringBuilder sql = new StringBuilder();

        Set<EntityColumn> columnList = EntityBuilder.getPKColumns(entityClass);
        if (columnList.size() == 1) {
            EntityColumn column = columnList.iterator().next();
            String id = column.getColumn();
            sql.append("<choose>");
            sql.append("<when test='" + id + "!=null'>");
            sql.append(updateByPrimaryKey(ms));
            sql.append("</when>");
            sql.append("<otherwise>");
            sql.append(insert(ms));
            sql.append("</otherwise>");
            sql.append("</choose>");
            return sql.toString();
        }
        return insert(ms);
    }

    /**
     * 通过主键更新全部字段
     *
     * @param ms MappedStatement
     */
    public String updateByPrimaryKey(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlBuilder.updateTable(entityClass, tableName(entityClass)));
        sql.append(SqlBuilder.updateSetColumns(entityClass, null, false, false));
        sql.append(SqlBuilder.wherePKColumns(entityClass, true));
        return sql.toString();
    }

    public String insert(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        // 获取全部列
        Set<EntityColumn> columnList = EntityBuilder.getColumns(entityClass);
        processKey(sql, entityClass, ms, columnList);
        sql.append(SqlBuilder.insertIntoTable(entityClass, tableName(entityClass)));
        sql.append(SqlBuilder.insertColumns(entityClass, false, false, false));
        sql.append("<trim prefix=\"VALUES(\" suffix=\")\" suffixOverrides=\",\">");
        for (EntityColumn column : columnList) {
            if (!column.isInsertable()) {
                continue;
            }
            // 优先使用传入的属性值,当原属性property!=null时，用原属性
            // 自增的情况下,如果默认有值,就会备份到property_cache中,所以这里需要先判断备份的值是否存在
            if (column.isIdentity()) {
                sql.append(SqlBuilder.getIfCacheNotNull(column, column.getColumnHolder(null, "_cache", Symbol.COMMA)));
            } else {
                // 其他情况值仍然存在原property中
                sql.append(SqlBuilder.getIfNotNull(column, column.getColumnHolder(null, null, Symbol.COMMA),
                        isNotEmpty()));
            }
            // 当属性为null时，如果存在主键策略，会自动获取值，如果不存在，则使用null
            if (column.isIdentity()) {
                sql.append(SqlBuilder.getIfCacheIsNull(column, column.getColumnHolder() + Symbol.COMMA));
            } else {
                // 当null的时候，如果不指定jdbcType，oracle可能会报异常，指定VARCHAR不影响其他
                sql.append(
                        SqlBuilder.getIfIsNull(column, column.getColumnHolder(null, null, Symbol.COMMA), isNotEmpty()));
            }
        }
        sql.append("</trim>");
        return sql.toString();
    }

    private void processKey(StringBuilder sql, Class<?> entityClass, MappedStatement ms, Set<EntityColumn> columnList) {
        // Identity列只能有一个
        Boolean hasIdentityKey = false;
        // 先处理cache或bind节点
        for (EntityColumn column : columnList) {
            if (column.isIdentity()) {
                // 这种情况下,如果原先的字段有值,需要先缓存起来,否则就一定会使用自动增长
                // 这是一个bind节点
                sql.append(SqlBuilder.getBindCache(column));
                // 如果是Identity列，就需要插入selectKey
                // 如果已经存在Identity列，抛出异常
                if (hasIdentityKey) {
                    // jdbc类型只需要添加一次
                    if (column.getGenerator() != null && column.getGenerator().equals("JDBC")) {
                        continue;
                    }
                    throw new MapperException(
                            ms.getId() + "对应的实体类" + entityClass.getName() + "中包含多个MySql的自动增长列,最多只能有一个!");
                }
                // 插入selectKey
                SelectKeyBuilder.newSelectKeyMappedStatement(ms, column, entityClass, isBEFORE(), getIDENTITY(column));
                hasIdentityKey = true;
            } else if (column.getGenIdClass() != null) {
                sql.append("<bind name=\"").append(column.getColumn())
                        .append("GenIdBind\" value=\"@org.miaixz.bus.mapper.Builder@genId(");
                sql.append("_parameter").append(", '").append(column.getProperty()).append("'");
                sql.append(", @").append(column.getGenIdClass().getName()).append("@class");
                sql.append(", '").append(tableName(entityClass)).append("'");
                sql.append(", '").append(column.getColumn()).append("')");
                sql.append("\"/>");
            }
        }
    }

}
