/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.io.Serializable;

import org.miaixz.bus.shade.screw.dialect.cachedb.CacheDbDataBaseQuery;
import org.miaixz.bus.shade.screw.dialect.db2.Db2DataBaseQuery;
import org.miaixz.bus.shade.screw.dialect.h2.H2DataBaseQuery;
import org.miaixz.bus.shade.screw.dialect.mariadb.MariaDbDataBaseQuery;
import org.miaixz.bus.shade.screw.dialect.mysql.MySqlDataBaseQuery;
import org.miaixz.bus.shade.screw.dialect.oracle.OracleDataBaseQuery;
import org.miaixz.bus.shade.screw.dialect.postgresql.PostgreSqlDataBaseQuery;
import org.miaixz.bus.shade.screw.dialect.sqlserver.SqlServerDataBaseQuery;

import lombok.Getter;

/**
 * Enumeration of supported database types.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum DatabaseType implements Serializable {

    /**
     * CacheDB
     */
    CACHEDB("cachedb", "Cache Database", CacheDbDataBaseQuery.class),
    /**
     * DB2
     */
    DB2("db2", "DB2 Database", Db2DataBaseQuery.class),
    /**
     * H2
     */
    H2("h2", "H2 Database", H2DataBaseQuery.class),
    /**
     * MariaDB
     */
    MARIADB("mariadb", "MariaDB Database", MariaDbDataBaseQuery.class),
    /**
     * MySQL
     */
    MYSQL("mysql", "MySQL Database", MySqlDataBaseQuery.class),
    /**
     * Oracle
     */
    ORACLE("oracle", "Oracle Database", OracleDataBaseQuery.class),
    /**
     * PostgreSQL
     */
    POSTGRE_SQL("PostgreSql", "PostgreSQL Database", PostgreSqlDataBaseQuery.class),
    /**
     * SQL Server 2005
     */
    SQL_SERVER2005("sqlServer2005", "SQL Server 2005 Database", SqlServerDataBaseQuery.class),

    /**
     * SQL Server
     */
    SQL_SERVER("sqlserver", "SQL Server Database", SqlServerDataBaseQuery.class),

    /**
     * Represents an unknown or unsupported database type.
     */
    OTHER("other", "Other Database", OtherDataBaseQuery.class);

    /**
     * The unique name of the database type.
     */
    @Getter
    private final String name;
    /**
     * A descriptive string for the database type.
     */
    @Getter
    private final String desc;
    /**
     * The class that implements the database query logic for this type.
     */
    @Getter
    private final Class<? extends DatabaseQuery> implClass;

    /**
     * Constructs a {@code DatabaseType} enum constant.
     *
     * @param name  The unique name of the database type.
     * @param desc  A descriptive string for the database type.
     * @param query The class that implements the database query logic.
     */
    DatabaseType(String name, String desc, Class<? extends DatabaseQuery> query) {
        this.name = name;
        this.desc = desc;
        this.implClass = query;
    }

    /**
     * Retrieves a {@code DatabaseType} by its name.
     *
     * @param dbType The name of the database type (case-insensitive).
     * @return The corresponding {@link DatabaseType}, or {@link #OTHER} if not found.
     */
    public static DatabaseType getType(String dbType) {
        for (DatabaseType dt : DatabaseType.values()) {
            if (dt.getName().equalsIgnoreCase(dbType)) {
                return dt;
            }
        }
        return OTHER;
    }

    /**
     * Determines the database type from a JDBC connection URL.
     *
     * @param jdbcUrl The JDBC connection URL.
     * @return The corresponding {@link DatabaseType}.
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
