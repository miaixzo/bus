/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.shade.screw.dialect.cachedb;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.shade.screw.Builder;
import org.miaixz.bus.shade.screw.dialect.AbstractDatabaseQuery;
import org.miaixz.bus.shade.screw.mapping.Mapping;
import org.miaixz.bus.shade.screw.metadata.Column;
import org.miaixz.bus.shade.screw.metadata.Database;
import org.miaixz.bus.shade.screw.metadata.PrimaryKey;
import org.miaixz.bus.shade.screw.metadata.Table;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CacheDB 数据库查询
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheDbDataBaseQuery extends AbstractDatabaseQuery {

    /**
     * 构造函数
     *
     * @param dataSource {@link DataSource}
     */
    public CacheDbDataBaseQuery(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 获取数据库
     *
     * @return {@link Database} 数据库信息
     * @throws InternalException 异常
     */
    @Override
    public Database getDataBase() throws InternalException {
        CacheDbDatabase model = new CacheDbDatabase();
        //当前数据库名称
        model.setDatabase(getSchema());
        return model;
    }

    /**
     * 获取表信息
     *
     * @return {@link List} 所有表信息
     * @throws InternalException 异常
     */
    @Override
    public List<? extends Table> getTables() throws InternalException {
        ResultSet resultSet = null;
        try {
            //查询
            resultSet = getMetaData().getTables(getCatalog(), getSchema(), null,
                    new String[]{"TABLE"});
            //映射
            return Mapping.convertList(resultSet, CacheDbTable.class);
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet);
        }
    }

    /**
     * 获取列信息
     *
     * @param table {@link String} 表名
     * @return {@link List} 表字段信息
     * @throws InternalException 异常
     */
    @Override
    public List<? extends Column> getTableColumns(String table) throws InternalException {
        Assert.notEmpty(table, "Table name can not be empty!");
        ResultSet resultSet = null;
        try {
            //查询
            resultSet = getMetaData().getColumns(getCatalog(), getSchema(), table, Builder.PERCENT_SIGN);
            //映射
            final List<CacheDbColumn> list = Mapping.convertList(resultSet,
                    CacheDbColumn.class);
            //这里处理是为了如果是查询全部列呢？所以处理并获取唯一表名
            List<String> tableNames = list.stream().map(CacheDbColumn::getTableName)
                    .collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());
            if (CollKit.isEmpty(columnsCaching)) {
                //查询全部
                if (table.equals(Builder.PERCENT_SIGN)) {
                    //获取全部表列信息SQL
                    String sql = MessageFormat
                            .format("select TABLE_NAME as \"TABLE_NAME\",COLUMN_NAME as "
                                            + "\"COLUMN_NAME\",DESCRIPTION as \"REMARKS\","
                                            + "case when CHARACTER_MAXIMUM_LENGTH is null then DATA_TYPE  || '''' "
                                            + "else DATA_TYPE  || ''(''||CHARACTER_MAXIMUM_LENGTH ||'')'' end as \"COLUMN_TYPE\" "
                                            + "from INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ''{0}''",
                                    getSchema());
                    PreparedStatement statement = prepareStatement(sql);
                    resultSet = statement.executeQuery();
                    int fetchSize = 4284;
                    if (resultSet.getFetchSize() < fetchSize) {
                        resultSet.setFetchSize(fetchSize);
                    }
                }
                //单表查询
                else {
                    //获取表列信息SQL 查询表名、列名、说明、数据类型
                    String sql = MessageFormat
                            .format("select TABLE_NAME as \"TABLE_NAME\",COLUMN_NAME as "
                                            + "\"COLUMN_NAME\",DESCRIPTION as \"REMARKS\","
                                            + "case when CHARACTER_MAXIMUM_LENGTH is null then DATA_TYPE  || ''''"
                                            + "else DATA_TYPE  || ''(''||CHARACTER_MAXIMUM_LENGTH ||'')'' end as \"COLUMN_TYPE\" "
                                            + "from INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = ''{0}'' and TABLE_NAME = "
                                            + "''{1}''",
                                    getSchema(), table);
                    resultSet = prepareStatement(sql).executeQuery();
                }
                List<CacheDbColumn> inquires = Mapping.convertList(resultSet,
                        CacheDbColumn.class);
                //处理列，表名为key，列名为值
                tableNames.forEach(name -> columnsCaching.put(name, inquires.stream()
                        .filter(i -> i.getTableName().equals(name)).collect(Collectors.toList())));
            }
            //处理备注信息
            list.forEach(i -> {
                //从缓存中根据表名获取列信息
                List<Column> columns = columnsCaching.get(i.getTableName());
                columns.forEach(j -> {
                    //列名表名一致
                    if (i.getColumnName().equals(j.getColumnName())
                            && i.getTableName().equals(j.getTableName())) {
                        //放入列类型
                        i.setColumnType(j.getColumnType());
                        i.setColumnLength(j.getColumnLength());
                        //放入注释
                        i.setRemarks(j.getRemarks());
                    }
                });
            });
            return list;
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet);
        }
    }

    /**
     * 获取所有列信息
     *
     * @return {@link List} 表字段信息
     * @throws InternalException 异常
     */
    @Override
    public List<? extends Column> getTableColumns() throws InternalException {
        //获取全部列
        return getTableColumns(Builder.PERCENT_SIGN);
    }

    /**
     * 根据表名获取主键
     *
     * @param table {@link String}
     * @return {@link List}
     * @throws InternalException 异常
     */
    @Override
    public List<? extends PrimaryKey> getPrimaryKeys(String table) throws InternalException {
        ResultSet resultSet = null;
        try {
            //查询
            resultSet = getMetaData().getPrimaryKeys(getCatalog(), getSchema(), table);
            //映射
            return Mapping.convertList(resultSet, CacheDbPrimaryKey.class);
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet, this.connection);
        }
    }

    /**
     * 根据表名获取主键信息
     *
     * @return {@link List}
     * @throws InternalException 异常
     */
    @Override
    public List<? extends PrimaryKey> getPrimaryKeys() throws InternalException {
        ResultSet resultSet = null;
        try {
            // 由于单条循环查询存在性能问题，所以这里通过自定义SQL查询数据库主键信息
            String sql = "select TABLE_CATALOG ,TABLE_NAME as \"TABLE_NAME\",TABLE_SCHEMA as \"TABLE_SCHEM\",COLUMN_NAME as \"COLUMN_NAME\",ORDINAL_POSITION as \"KEY_SEQ\" from INFORMATION_SCHEMA.COLUMNS where PRIMARY_KEY='YES' and TABLE_SCHEMA='%s'";
            // 拼接参数
            resultSet = prepareStatement(String.format(sql, getDataBase().getDatabase()))
                    .executeQuery();
            return Mapping.convertList(resultSet, CacheDbPrimaryKey.class);
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet);
        }
    }

}
