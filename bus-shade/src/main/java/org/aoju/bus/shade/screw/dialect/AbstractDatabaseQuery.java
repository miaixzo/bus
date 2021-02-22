/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.shade.screw.dialect;

import lombok.Getter;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.shade.screw.Builder;
import org.aoju.bus.shade.screw.metadata.Column;
import org.aoju.bus.shade.screw.metadata.PrimaryKey;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象查询
 *
 * @author Kimi Liu
 * @version 6.2.0
 * @since JDK 1.8+
 */
public abstract class AbstractDatabaseQuery implements DatabaseQuery {
    /**
     * 缓存
     */
    protected final Map<String, List<Column>> columnsCaching = new ConcurrentHashMap<>();
    /**
     * DataSource
     */
    @Getter
    private final DataSource dataSource;

    /**
     * Connection 双重锁，线程安全
     */
    volatile protected Connection connection;

    public AbstractDatabaseQuery(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 释放资源
     *
     * @param rs {@link ResultSet}
     */
    public static void close(ResultSet rs) {
        if (!Objects.isNull(rs)) {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new InstrumentException(e);
            }
        }
    }

    /**
     * 释放资源
     *
     * @param conn {@link Connection}
     */
    public static void close(Connection conn) {
        if (!Objects.isNull(conn)) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new InstrumentException(e);
            }
        }
    }

    /**
     * 释放资源
     *
     * @param rs   {@link ResultSet}
     * @param conn {@link Connection}
     */
    public static void close(ResultSet rs, Connection conn) {
        if (!Objects.isNull(rs)) {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new InstrumentException(e);
            }
        }
        if (!Objects.isNull(conn)) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new InstrumentException(e);
            }
        }
    }

    /**
     * 释放资源
     *
     * @param rs   {@link ResultSet}
     * @param st   {@link Statement}
     * @param conn {@link Connection}
     */
    public static void close(ResultSet rs, Statement st, Connection conn) {
        if (!Objects.isNull(rs)) {
            try {
                rs.close();
            } catch (SQLException e) {
                throw new InstrumentException(e);
            }
        }
        if (!Objects.isNull(st)) {
            try {
                st.close();
            } catch (SQLException e) {
                throw new InstrumentException(e);
            }
        }
        if (!Objects.isNull(conn)) {
            try {
                conn.close();
            } catch (SQLException e) {
                throw new InstrumentException(e);
            }
        }
    }

    /**
     * 获取连接对象，单例模式，采用双重锁检查
     *
     * @return {@link Connection}
     * @throws InstrumentException 异常
     */
    private Connection getConnection() throws InstrumentException {
        try {
            //不为空
            if (!Objects.isNull(connection) && !connection.isClosed()) {
                return connection;
            }
            //同步代码块
            synchronized (AbstractDatabaseQuery.class) {
                //为空或者已经关闭
                if (Objects.isNull(connection) || connection.isClosed()) {
                    this.connection = this.getDataSource().getConnection();
                }
            }
            return this.connection;
        } catch (SQLException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取 getCatalog
     *
     * @return {@link String}
     * @throws InstrumentException 异常
     */
    protected String getCatalog() throws InstrumentException {
        try {
            String catalog = this.getConnection().getCatalog();
            if (StringKit.isBlank(catalog)) {
                return null;
            }
            return catalog;
        } catch (SQLException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取 getSchema
     *
     * @return {@link String}
     * @throws InstrumentException 异常
     */
    protected String getSchema() throws InstrumentException {
        try {
            String schema;
            //获取数据库URL 用于判断数据库类型
            String url = this.getDataSource().getConnection().getMetaData().getURL();
            //获取数据库名称
            String name = DatabaseType.getDbType(url).getName();
            if (DatabaseType.CACHEDB.getName().equals(name)) {
                schema = verifySchema(this.getDataSource());
            } else {
                schema = this.getConnection().getSchema();
            }

            if (StringKit.isBlank(schema)) {
                return null;
            }
            return schema;
        } catch (SQLException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 验证Schema
     *
     * @param dataSource {@link DataSource}
     * @return Schema
     */
    private String verifySchema(DataSource dataSource) throws SQLException {
        String schema = dataSource.getConnection().getSchema();

        //验证是否有此Schema
        ResultSet resultSet = this.getConnection().getMetaData().getSchemas();
        while (resultSet.next()) {
            int columnCount = resultSet.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnValue = resultSet.getString(i);
                if (StringKit.isNotBlank(columnValue) && columnValue.contains(schema)) {
                    return schema;
                }
            }
        }
        return null;
    }

    /**
     * 获取 DatabaseMetaData
     *
     * @return {@link DatabaseMetaData}
     * @throws InstrumentException 异常
     */
    protected DatabaseMetaData getMetaData() throws InstrumentException {
        try {
            return this.getConnection().getMetaData();
        } catch (SQLException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 准备声明
     *
     * @param sql {@link String} SQL
     * @return {@link PreparedStatement}
     * @throws InstrumentException 异常
     */
    protected PreparedStatement prepareStatement(String sql) throws InstrumentException {
        Assert.notEmpty(sql, "Sql can not be empty!");
        try {
            return this.getConnection().prepareStatement(sql);
        } catch (SQLException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 根据表名获取主键
     *
     * @return {@link List}
     * @throws InstrumentException 异常
     */
    @Override
    public List<? extends PrimaryKey> getPrimaryKeys() throws InstrumentException {
        throw new InstrumentException(Builder.NOT_SUPPORTED);
    }

}
