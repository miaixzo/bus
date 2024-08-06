/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.shade.screw.dialect.oracle;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.shade.screw.Builder;
import org.miaixz.bus.shade.screw.dialect.AbstractDatabaseQuery;
import org.miaixz.bus.shade.screw.mapping.Mapping;
import org.miaixz.bus.shade.screw.metadata.Column;
import org.miaixz.bus.shade.screw.metadata.Database;
import org.miaixz.bus.shade.screw.metadata.PrimaryKey;

/**
 * Oracle 数据库查询
 * <p>
 * 还是采用从驱动中拿到数据的方式，这里注意一点，一定要加入配置参数remarks为true 否则表和列等说明不会查询出来 hikari： config.addDataSourceProperty("remarks", "true");
 * <p>
 * 不过这种查询性能很慢
 * https://docs.oracle.com/en/database/oracle/oracle-database/20/jjdbc/performance-extensions.html#GUID-15865071-39F2-430F-9EDA-EB34D0B2D560
 * 所以，只能够通过自定义SQL来了
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OracleDataBaseQuery extends AbstractDatabaseQuery {
    /**
     * 构造函数
     *
     * @param dataSource {@link DataSource}
     */
    public OracleDataBaseQuery(DataSource dataSource) {
        super(dataSource);
    }

    /**
     * 获取数据库
     *
     * @return {@link Database} 数据库信息
     */
    @Override
    public Database getDataBase() throws InternalException {
        OracleDatabase model = new OracleDatabase();
        // 当前数据库名称
        model.setDatabase(getSchema());
        return model;
    }

    /**
     * 获取表信息
     *
     * @return {@link List} 所有表信息
     */
    @Override
    public List<OracleTable> getTables() throws InternalException {
        ResultSet resultSet = null;
        try {
            // 查询
            resultSet = getMetaData().getTables(getCatalog(), getSchema(), Builder.PERCENT_SIGN,
                    new String[] { "TABLE" });
            // 映射
            List<OracleTable> list = Mapping.convertList(resultSet, OracleTable.class);
            // 由于ORACLE 查询 REMARKS 非常耗费性能，所以这里使用自定义SQL查询
            // https://docs.oracle.com/en/database/oracle/oracle-database/20/jjdbc/performance-extensions.html#GUID-15865071-39F2-430F-9EDA-EB34D0B2D560
            // 获取所有表 查询表名、说明
            String sql = "SELECT TABLE_NAME,COMMENTS AS REMARKS FROM USER_TAB_COMMENTS WHERE TABLE_TYPE = 'TABLE'";
            if (isDda()) {
                // DBA 使用 DBA_TAB_COMMENTS 进行查询 Oracle连接用户和schema不同问题。dba连接用户可以生成不同schema下的表结构
                sql = "SELECT TABLE_NAME,COMMENTS AS REMARKS FROM DBA_TAB_COMMENTS WHERE TABLE_TYPE = 'TABLE' AND OWNER = '"
                        + getSchema() + "'";
            }
            resultSet = prepareStatement(String.format(sql, getSchema())).executeQuery();
            List<OracleTable> inquires = Mapping.convertList(resultSet, OracleTable.class);
            // 处理备注信息
            list.forEach((OracleTable model) -> {
                // 备注
                inquires.stream().filter(inquire -> model.getTableName().equals(inquire.getTableName()))
                        .forEachOrdered(inquire -> model.setRemarks(inquire.getRemarks()));
            });
            return list;
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet, this.connection);
        }
    }

    /**
     * 获取列信息
     *
     * @param table {@link String} 表名
     * @return {@link List} 表字段信息
     */
    @Override
    public List<OracleColumn> getTableColumns(String table) throws InternalException {
        Assert.notEmpty(table, "Table name can not be empty!");
        ResultSet resultSet = null;
        try {
            // 查询
            resultSet = getMetaData().getColumns(getCatalog(), getSchema(), table, Builder.PERCENT_SIGN);
            // 映射
            List<OracleColumn> list = Mapping.convertList(resultSet, OracleColumn.class);
            // 由于ORACLE 查询 COLUMNS REMARKS 为NULL，所以这里使用自定义SQL查询
            // https://docs.oracle.com/en/database/oracle/oracle-database/20/jjdbc/performance-extensions.html#GUID-15865071-39F2-430F-9EDA-EB34D0B2D560
            List<String> tableNames = list.stream().map(OracleColumn::getTableName).collect(Collectors.toList())
                    .stream().distinct().collect(Collectors.toList());
            if (CollKit.isEmpty(columnsCaching)) {
                // 查询全部
                if (table.equals(Builder.PERCENT_SIGN)) {
                    String sql = "SELECT ut.TABLE_NAME,  ut.COLUMN_NAME, uc.comments as REMARKS, concat(concat(concat(ut.DATA_TYPE, '('), ut.DATA_LENGTH), ')') AS COLUMN_TYPE, ut.DATA_LENGTH as COLUMN_LENGTH FROM user_tab_columns ut INNER JOIN user_col_comments uc ON ut.TABLE_NAME = uc.table_name AND ut.COLUMN_NAME = uc.column_name";
                    if (isDda()) {
                        sql = "SELECT ut.TABLE_NAME,  ut.COLUMN_NAME, uc.comments as REMARKS, concat(concat(concat(ut.DATA_TYPE, '('), ut.DATA_LENGTH), ')') AS COLUMN_TYPE, ut.DATA_LENGTH as COLUMN_LENGTH FROM dba_tab_columns ut INNER JOIN dba_col_comments uc ON ut.TABLE_NAME = uc.table_name AND ut.COLUMN_NAME = uc.column_name and ut.OWNER = uc.OWNER WHERE ut.OWNER = '"
                                + getDataBase() + "'";
                    }
                    PreparedStatement statement = prepareStatement(sql);
                    resultSet = statement.executeQuery();
                    int fetchSize = 4284;
                    if (resultSet.getFetchSize() < fetchSize) {
                        resultSet.setFetchSize(fetchSize);
                    }
                }
                // 单表查询
                else {
                    String sql = "SELECT ut.TABLE_NAME,  ut.COLUMN_NAME, uc.comments as REMARKS, concat(concat(concat(ut.DATA_TYPE, '('), ut.DATA_LENGTH), ')') AS COLUMN_TYPE, ut.DATA_LENGTH as COLUMN_LENGTH FROM user_tab_columns ut INNER JOIN user_col_comments uc ON ut.TABLE_NAME = uc.table_name AND ut.COLUMN_NAME = uc.column_name WHERE ut.Table_Name = '%s'";
                    if (isDda()) {
                        sql = "SELECT ut.TABLE_NAME,  ut.COLUMN_NAME, uc.comments as REMARKS, concat(concat(concat(ut.DATA_TYPE, '('), ut.DATA_LENGTH), ')') AS COLUMN_TYPE, ut.DATA_LENGTH as COLUMN_LENGTH FROM dba_tab_columns ut INNER JOIN dba_col_comments uc ON ut.TABLE_NAME = uc.table_name AND ut.COLUMN_NAME = uc.column_name and ut.OWNER = uc.OWNER WHERE ut.Table_Name = '%s' ut.OWNER = '"
                                + getDataBase() + "'";
                    }
                    resultSet = prepareStatement(String.format(sql, table)).executeQuery();
                }
                List<OracleColumn> inquires = Mapping.convertList(resultSet, OracleColumn.class);
                // 处理列，表名为key，列名为值
                tableNames.forEach(name -> columnsCaching.put(name,
                        inquires.stream().filter(i -> i.getTableName().equals(name)).collect(Collectors.toList())));
            }
            // 处理备注信息
            // 从缓存中根据表名获取列信息
            for (OracleColumn i : list) {
                List<Column> columns = columnsCaching.get(i.getTableName());
                columns.forEach(j -> {
                    // 列名表名一致
                    if (i.getColumnName().equals(j.getColumnName()) && i.getTableName().equals(j.getTableName())) {
                        // 放入备注
                        i.setRemarks(j.getRemarks());
                        i.setColumnLength(j.getColumnLength());
                        i.setColumnType(j.getColumnType());
                    }
                });
            }
            return list;
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet, this.connection);
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
            // 查询
            resultSet = getMetaData().getPrimaryKeys(getCatalog(), getSchema(), table);
            // 映射
            return Mapping.convertList(resultSet, OraclePrimaryKey.class);
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
            String sql = "SELECT NULL AS TABLE_CAT, C.OWNER AS TABLE_SCHEM, C.TABLE_NAME, C.COLUMN_NAME, C.POSITION AS KEY_SEQ, C.CONSTRAINT_NAME AS PK_NAME FROM ALL_CONS_COLUMNS C, ALL_CONSTRAINTS K WHERE K.CONSTRAINT_TYPE = 'P' AND K.OWNER LIKE '%s' ESCAPE '/' AND K.CONSTRAINT_NAME = C.CONSTRAINT_NAME AND K.TABLE_NAME = C.TABLE_NAME AND K.OWNER = C.OWNER ORDER BY COLUMN_NAME ";
            // 拼接参数
            resultSet = prepareStatement(String.format(sql, getDataBase().getDatabase())).executeQuery();
            return Mapping.convertList(resultSet, OraclePrimaryKey.class);
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet);
        }
    }

    /**
     * 当前用户是否为DBA
     *
     * @return {@link Boolean}
     */
    private boolean isDda() {
        ResultSet resultSet = null;
        try {
            // 判断是否是DBA
            resultSet = prepareStatement("SELECT USERENV('isdba') as IS_DBA FROM DUAL").executeQuery();
            String dbaColumn = "IS_DBA";
            resultSet.next();
            return resultSet.getBoolean(dbaColumn);
        } catch (SQLException e) {
            throw new InternalException(e);
        } finally {
            close(resultSet);
        }
    }

}
