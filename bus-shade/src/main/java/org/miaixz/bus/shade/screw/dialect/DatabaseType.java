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
package org.miaixz.bus.shade.screw.dialect;

import lombok.Getter;
import org.miaixz.bus.shade.screw.dialect.cachedb.CacheDbDataBaseQuery;
import org.miaixz.bus.shade.screw.dialect.db2.Db2DataBaseQuery;
import org.miaixz.bus.shade.screw.dialect.h2.H2DataBaseQuery;
import org.miaixz.bus.shade.screw.dialect.mariadb.MariaDbDataBaseQuery;
import org.miaixz.bus.shade.screw.dialect.mysql.MySqlDataBaseQuery;
import org.miaixz.bus.shade.screw.dialect.oracle.OracleDataBaseQuery;
import org.miaixz.bus.shade.screw.dialect.postgresql.PostgreSqlDataBaseQuery;
import org.miaixz.bus.shade.screw.dialect.sqlserver.SqlServerDataBaseQuery;

import java.io.Serializable;

/**
 * 数据库类型
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum DatabaseType implements Serializable {

    /**
     * CacheDB
     */
    CACHEDB("cachedb", "Cache 数据库", CacheDbDataBaseQuery.class),
    /**
     * DB2
     */
    DB2("db2", "DB2数据库", Db2DataBaseQuery.class),
    /**
     * H2
     */
    H2("h2", "H2数据库", H2DataBaseQuery.class),
    /**
     * MARIA DB
     */
    MARIADB("mariadb", "MariaDB数据库", MariaDbDataBaseQuery.class),
    /**
     * MYSQL
     */
    MYSQL("mysql", "MySql数据库", MySqlDataBaseQuery.class),
    /**
     * ORACLE
     */
    ORACLE("oracle", "Oracle数据库", OracleDataBaseQuery.class),
    /**
     * POSTGRE
     */
    POSTGRE_SQL("PostgreSql", "Postgre数据库", PostgreSqlDataBaseQuery.class),
    /**
     * SQL SERVER 2005
     */
    SQL_SERVER2005("sqlServer2005", "SQLServer2005数据库", SqlServerDataBaseQuery.class),

    /**
     * SQLSERVER
     */
    SQL_SERVER("sqlserver", "SQLServer数据库", SqlServerDataBaseQuery.class),

    /**
     * UNKONWN DB
     */
    OTHER("other", "其他数据库", OtherDataBaseQuery.class);

    /**
     * 数据库名称
     */
    @Getter
    private final String name;
    /**
     * 描述
     */
    @Getter
    private final String desc;
    /**
     * 查询实现
     */
    @Getter
    private final Class<? extends DatabaseQuery> implClass;

    /**
     * 构造
     *
     * @param name  {@link String} 名称
     * @param desc  {@link String} 描述
     * @param query {@link Class}
     */
    DatabaseType(String name, String desc, Class<? extends DatabaseQuery> query) {
        this.name = name;
        this.desc = desc;
        this.implClass = query;
    }

    /**
     * 获取数据库类型
     *
     * @param dbType {@link String} 数据库类型字符串
     * @return {@link DatabaseType}
     */
    public static DatabaseType getType(String dbType) {
        DatabaseType[] dts = DatabaseType.values();
        for (DatabaseType dt : dts) {
            if (dt.getName().equalsIgnoreCase(dbType)) {
                return dt;
            }
        }
        return OTHER;
    }

    /**
     * 根据连接地址判断数据库类型
     *
     * @param jdbcUrl {@link String} 连接地址
     * @return {@link DatabaseType} DatabaseType
     */
    public static DatabaseType getDbType(String jdbcUrl) {
        if (jdbcUrl.contains(":Cache:")) {
            return CACHEDB;
        } else if (jdbcUrl.contains(":db2:")) {
            return DatabaseType.DB2;
        } else if (jdbcUrl.contains(":h2:")) {
            return H2;
        } else if (jdbcUrl.contains(":mariadb:")) {
            return MARIADB;
        } else if (jdbcUrl.contains(":mysql:") || jdbcUrl.contains(":cobar:")) {
            return MYSQL;
        } else if (jdbcUrl.contains(":oracle:")) {
            return ORACLE;
        } else if (jdbcUrl.contains(":postgresql:")) {
            return POSTGRE_SQL;
        } else if (jdbcUrl.contains(":sqlserver:") || jdbcUrl.contains(":microsoft:")) {
            return SQL_SERVER2005;
        } else if (jdbcUrl.contains(":sqlserver2012:")) {
            return SQL_SERVER;
        } else {
            return OTHER;
        }
    }

}
