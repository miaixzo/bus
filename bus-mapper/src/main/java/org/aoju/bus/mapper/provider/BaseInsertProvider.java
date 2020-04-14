/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.mapper.builder.EntityBuilder;
import org.aoju.bus.mapper.builder.MapperBuilder;
import org.aoju.bus.mapper.builder.MapperTemplate;
import org.aoju.bus.mapper.builder.SqlSourceBuilder;
import org.aoju.bus.mapper.criteria.Assert;
import org.aoju.bus.mapper.entity.EntityColumn;
import org.aoju.bus.mapper.executor.SelectKey;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.Set;

/**
 * BaseInsertProvider实现类,基础方法实现类
 *
 * @author Kimi Liu
 * @version 5.8.5
 * @since JDK 1.8+
 */
public class BaseInsertProvider extends MapperTemplate {

    public BaseInsertProvider(Class<?> mapperClass, MapperBuilder mapperBuilder) {
        super(mapperClass, mapperBuilder);
    }

    /**
     * 插入全部,这段代码比较复杂,这里举个例子
     * CountryU生成的insert方法结构如下：
     * <pre>
     * &lt;bind name="countryname_bind" value='@java.util.UUID@randomUUID().toString().replace("-", "")'/&gt;
     * INSERT INTO country_u(id,countryname,countrycode) VALUES
     * &lt;trim prefix="(" suffix=")" suffixOverrides=","&gt;
     * &lt;if test="id != null"&gt;#{id,javaType=java.lang.Integer},&lt;/if&gt;
     * &lt;if test="id == null"&gt;#{id,javaType=java.lang.Integer},&lt;/if&gt;
     * &lt;if test="countryname != null"&gt;#{countryname,javaType=java.lang.String},&lt;/if&gt;
     * &lt;if test="countryname == null"&gt;#{countryname_bind,javaType=java.lang.String},&lt;/if&gt;
     * &lt;if test="countrycode != null"&gt;#{countrycode,javaType=java.lang.String},&lt;/if&gt;
     * &lt;if test="countrycode == null"&gt;#{countrycode,javaType=java.lang.String},&lt;/if&gt;
     * &lt;/trim&gt;
     * </pre>
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String insert(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        //获取全部列
        Set<EntityColumn> columnList = EntityBuilder.getColumns(entityClass);
        //Identity列只能有一个
        Boolean hasIdentityKey = false;
        //先处理cache或bind节点
        for (EntityColumn column : columnList) {
            if (!column.isInsertable()) {
                continue;
            }
            if (Assert.isNotEmpty(column.getSequenceName())) {
            } else if (column.isIdentity()) {
                //这种情况下,如果原先的字段有值,需要先缓存起来,否则就一定会使用自动增长
                //这是一个bind节点
                sql.append(SqlSourceBuilder.getBindCache(column));
                //如果是Identity列,就需要插入selectKey
                //如果已经存在Identity列,抛出异常
                if (hasIdentityKey) {
                    //jdbc类型只需要添加一次
                    if (null != column.getGenerator() && "JDBC".equals(column.getGenerator())) {
                        continue;
                    }
                    throw new InstrumentException(ms.getId() + "对应的实体类" + entityClass.getCanonicalName() + "中包含多个MySql的自动增长列,最多只能有一个!");
                }
                //插入selectKey
                SelectKey.newSelectKeyMappedStatement(ms, column, entityClass, isBEFORE(), getIDENTITY(column));
                hasIdentityKey = true;
            } else if (column.isUuid()) {
                //uuid的情况,直接插入bind节点
                sql.append(SqlSourceBuilder.getBindValue(column, getUUID()));
            }
        }
        sql.append(SqlSourceBuilder.insertIntoTable(entityClass, tableName(entityClass)));
        sql.append(SqlSourceBuilder.insertColumns(entityClass, false, false, false));
        sql.append("<trim prefix=\"VALUES(\" suffix=\")\" suffixOverrides=\",\">");
        for (EntityColumn column : columnList) {
            if (!column.isInsertable()) {
                continue;
            }
            //优先使用传入的属性值,当原属性property!=null时,用原属性
            //自增的情况下,如果默认有值,就会备份到property_cache中,所以这里需要先判断备份的值是否存在
            if (column.isIdentity()) {
                sql.append(SqlSourceBuilder.getIfCacheNotNull(column, column.getColumnHolder(null, "_cache", Symbol.COMMA)));
            } else {
                //其他情况值仍然存在原property中
                sql.append(SqlSourceBuilder.getIfNotNull(column, column.getColumnHolder(null, null, Symbol.COMMA), isNotEmpty()));
            }
            //当属性为null时,如果存在主键策略,会自动获取值,如果不存在,则使用null
            //序列的情况
            if (Assert.isNotEmpty(column.getSequenceName())) {
                sql.append(SqlSourceBuilder.getIfIsNull(column, getSeqNextVal(column) + " ,", false));
            } else if (column.isIdentity()) {
                sql.append(SqlSourceBuilder.getIfCacheIsNull(column, column.getColumnHolder() + Symbol.COMMA));
            } else if (column.isUuid()) {
                sql.append(SqlSourceBuilder.getIfIsNull(column, column.getColumnHolder(null, "_bind", Symbol.COMMA), isNotEmpty()));
            } else {
                //当null的时候,如果不指定jdbcType,oracle可能会报异常,指定VARCHAR不影响其他
                sql.append(SqlSourceBuilder.getIfIsNull(column, column.getColumnHolder(null, null, Symbol.COMMA), isNotEmpty()));
            }
        }
        sql.append("</trim>");
        return sql.toString();
    }

    /**
     * 插入不为null的字段,这段代码比较复杂,这里举个例子
     * CountryU生成的insertSelective方法结构如下：
     * <pre>
     * &lt;bind name="countryname_bind" value='@java.util.UUID@randomUUID().toString().replace("-", "")'/&gt;
     * INSERT INTO country_u
     * &lt;trim prefix="(" suffix=")" suffixOverrides=","&gt;
     * &lt;if test="id != null"&gt;id,&lt;/if&gt;
     * countryname,
     * &lt;if test="countrycode != null"&gt;countrycode,&lt;/if&gt;
     * &lt;/trim&gt;
     * VALUES
     * &lt;trim prefix="(" suffix=")" suffixOverrides=","&gt;
     * &lt;if test="id != null"&gt;#{id,javaType=java.lang.Integer},&lt;/if&gt;
     * &lt;if test="countryname != null"&gt;#{countryname,javaType=java.lang.String},&lt;/if&gt;
     * &lt;if test="countryname == null"&gt;#{countryname_bind,javaType=java.lang.String},&lt;/if&gt;
     * &lt;if test="countrycode != null"&gt;#{countrycode,javaType=java.lang.String},&lt;/if&gt;
     * &lt;/trim&gt;
     * </pre>
     * 这段代码可以注意对countryname的处理
     *
     * @param ms MappedStatement
     * @return the string
     */
    public String insertSelective(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        //获取全部列
        Set<EntityColumn> columnList = EntityBuilder.getColumns(entityClass);
        //Identity列只能有一个
        Boolean hasIdentityKey = false;
        //先处理cache或bind节点
        for (EntityColumn column : columnList) {
            if (!column.isInsertable()) {
                continue;
            }
            if (Assert.isNotEmpty(column.getSequenceName())) {
                //sql.append(column.getColumn() + ",");
            } else if (column.isIdentity()) {
                //这种情况下,如果原先的字段有值,需要先缓存起来,否则就一定会使用自动增长
                //这是一个bind节点
                sql.append(SqlSourceBuilder.getBindCache(column));
                //如果是Identity列,就需要插入selectKey
                //如果已经存在Identity列,抛出异常
                if (hasIdentityKey) {
                    //jdbc类型只需要添加一次
                    if (column.getGenerator() != null && column.getGenerator().equals("JDBC")) {
                        continue;
                    }
                    throw new InstrumentException(ms.getId() + "对应的实体类" + entityClass.getCanonicalName() + "中包含多个MySql的自动增长列,最多只能有一个!");
                }
                //插入selectKey
                SelectKey.newSelectKeyMappedStatement(ms, column, entityClass, isBEFORE(), getIDENTITY(column));
                hasIdentityKey = true;
            } else if (column.isUuid()) {
                //uuid的情况,直接插入bind节点
                sql.append(SqlSourceBuilder.getBindValue(column, getUUID()));
            }
        }
        sql.append(SqlSourceBuilder.insertIntoTable(entityClass, tableName(entityClass)));
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (EntityColumn column : columnList) {
            if (!column.isInsertable()) {
                continue;
            }
            if (Assert.isNotEmpty(column.getSequenceName()) || column.isIdentity() || column.isUuid()) {
                sql.append(column.getColumn() + ",");
            } else {
                sql.append(SqlSourceBuilder.getIfNotNull(column, column.getColumn() + ",", isNotEmpty()));
            }
        }
        sql.append("</trim>");
        sql.append("<trim prefix=\"VALUES(\" suffix=\")\" suffixOverrides=\",\">");
        for (EntityColumn column : columnList) {
            if (!column.isInsertable()) {
                continue;
            }
            //优先使用传入的属性值,当原属性property!=null时,用原属性
            //自增的情况下,如果默认有值,就会备份到property_cache中,所以这里需要先判断备份的值是否存在
            if (column.isIdentity()) {
                sql.append(SqlSourceBuilder.getIfCacheNotNull(column, column.getColumnHolder(null, "_cache", ",")));
            } else {
                //其他情况值仍然存在原property中
                sql.append(SqlSourceBuilder.getIfNotNull(column, column.getColumnHolder(null, null, ","), isNotEmpty()));
            }
            //当属性为null时,如果存在主键策略,会自动获取值,如果不存在,则使用null
            //序列的情况
            if (Assert.isNotEmpty(column.getSequenceName())) {
                sql.append(SqlSourceBuilder.getIfIsNull(column, getSeqNextVal(column) + " ,", isNotEmpty()));
            } else if (column.isIdentity()) {
                sql.append(SqlSourceBuilder.getIfCacheIsNull(column, column.getColumnHolder() + ","));
            } else if (column.isUuid()) {
                sql.append(SqlSourceBuilder.getIfIsNull(column, column.getColumnHolder(null, "_bind", ","), isNotEmpty()));
            }
        }
        sql.append("</trim>");
        return sql.toString();
    }
}
