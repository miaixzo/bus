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
package org.miaixz.bus.shade.screw.dialect.mariadb;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.shade.screw.Builder;
import org.miaixz.bus.shade.screw.dialect.AbstractDatabaseQuery;
import org.miaixz.bus.shade.screw.mapping.Mapping;
import org.miaixz.bus.shade.screw.metadata.Column;
import org.miaixz.bus.shade.screw.metadata.Database;
import org.miaixz.bus.shade.screw.metadata.PrimaryKey;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * mariadb 数据库查询
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MariaDbDataBaseQuery extends AbstractDatabaseQuery {
    /**
     * 构造函数
     *
     * @param dataSource {@link DataSource}
     */
    public MariaDbDataBaseQuery(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 获取数据库
     *
     * @return {@link Database} 数据库信息
     */
    @Override
    public Database getDataBase() throws InternalException {
        MariadbDatabase model = new MariadbDatabase();
        model.setDatabase(getCatalog());
        return model;
    }

    /**
     * 获取表信息
     *
     * @return {@link List} 所有表信息
     */
    @Override
    public List<MariadbTable> getTables() throws InternalException {
        ResultSet resultSet = null;
        try {
            //查询
            resultSet = getMetaData().getTables(getCatalog(), getSchema(), Builder.PERCENT_SIGN,
                    new String[]{"TABLE"});
            //映射
            return Mapping.convertList(resultSet, MariadbTable.class);
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
     */
    @Override
    public List<MariadbColumn> getTableColumns(String table) throws InternalException {
        Assert.notEmpty(table, "Table name can not be empty!");
        ResultSet resultSet = null;
        try {
            //查询
            resultSet = getMetaData().getColumns(getCatalog(), getSchema(), table, Builder.PERCENT_SIGN);
            //映射
            List<MariadbColumn> list = Mapping.convertList(resultSet,
                    MariadbColumn.class);
            //这里处理是为了如果是查询全部列呢？所以处理并获取唯一表名
            List<String> tableNames = list.stream().map(MariadbColumn::getTableName)
                    .collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());
            if (CollKit.isEmpty(columnsCaching)) {
                //查询全部
                if (table.equals(Builder.PERCENT_SIGN)) {
                    //获取全部表列信息SQL
                    String sql = "SELECT A.TABLE_NAME, A.COLUMN_NAME, A.COLUMN_TYPE, case when LOCATE('(', A.COLUMN_TYPE) > 0 then replace(substring(A.COLUMN_TYPE, LOCATE('(', A.COLUMN_TYPE) + 1), ')', '') else null end COLUMN_LENGTH FROM INFORMATION_SCHEMA.COLUMNS A WHERE A.TABLE_SCHEMA = '%s'";
                    PreparedStatement statement = prepareStatement(
                            String.format(sql, getDataBase().getDatabase()));
                    resultSet = statement.executeQuery();
                    int fetchSize = 4284;
                    if (resultSet.getFetchSize() < fetchSize) {
                        resultSet.setFetchSize(fetchSize);
                    }
                }
                //单表查询
                else {
                    //获取表列信息SQL 查询表名、列名、说明、数据类型
                    String sql = "SELECT A.TABLE_NAME, A.COLUMN_NAME, A.COLUMN_TYPE, case when LOCATE('(', A.COLUMN_TYPE) > 0 then replace(substring(A.COLUMN_TYPE, LOCATE('(', A.COLUMN_TYPE) + 1), ')', '') else null end COLUMN_LENGTH FROM INFORMATION_SCHEMA.COLUMNS A WHERE A.TABLE_SCHEMA = '%s' and A.TABLE_NAME = '%s'";
                    resultSet = prepareStatement(
                            String.format(sql, getDataBase().getDatabase(), table)).executeQuery();
                }
                List<MariadbColumn> inquires = Mapping.convertList(resultSet,
                        MariadbColumn.class);
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
            return Mapping.convertList(resultSet, MariadbPrimaryKey.class);
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet, this.connection);
        }
    }

    /**
     * 根据表名获取主键
     *
     * @return {@link List}
     * @throws InternalException 异常
     */
    @Override
    public List<? extends PrimaryKey> getPrimaryKeys() throws InternalException {
        ResultSet resultSet = null;
        try {
            // 由于单条循环查询存在性能问题，所以这里通过自定义SQL查询数据库主键信息
            // MySQL 8 now use 'PRI' in place of 'pri'
            String sql = "SELECT A.TABLE_SCHEMA TABLE_CAT, NULL TABLE_SCHEM, A.TABLE_NAME, A.COLUMN_NAME, B.SEQ_IN_INDEX KEY_SEQ, B.INDEX_NAME PK_NAME FROM INFORMATION_SCHEMA.COLUMNS A, INFORMATION_SCHEMA.STATISTICS B WHERE A.COLUMN_KEY in('PRI', 'pri') AND B.INDEX_NAME = 'PRIMARY' AND (A.TABLE_SCHEMA = '%s') AND (B.TABLE_SCHEMA = '%s') AND A.TABLE_SCHEMA = B.TABLE_SCHEMA AND A.TABLE_NAME = B.TABLE_NAME AND A.COLUMN_NAME = B.COLUMN_NAME ORDER BY A.COLUMN_NAME";
            // 拼接参数
            String database = getDataBase().getDatabase();
            resultSet = prepareStatement(String.format(sql, database, database)).executeQuery();
            return Mapping.convertList(resultSet, MariadbPrimaryKey.class);
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet);
        }
    }

}
