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
package org.miaixz.bus.pager.dialect.auto;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.core.lang.exception.PageException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.dialect.AutoDialect;
import org.miaixz.bus.pager.binding.PageAutoDialect;
import org.miaixz.bus.pager.dialect.AbstractPaging;

/**
 * Early version default implementation for auto-detecting database dialects. This approach involves obtaining a
 * connection to extract the JDBC URL, which is highly generic but can be less performant and may lead to issues if
 * connections are not properly closed.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Early implements AutoDialect<String> {

    /**
     * Default instance of the Early auto-dialect.
     */
    public static final AutoDialect<String> DEFAULT = new Early();

    /**
     * Extracts the dialect key (JDBC URL) from the DataSource by obtaining a connection. This method is generic but can
     * be less efficient due to connection handling.
     *
     * @param ms         the MappedStatement being executed
     * @param dataSource the DataSource associated with the MappedStatement
     * @param properties the configuration properties, including "closeConn" to control connection closing
     * @return the JDBC URL as the dialect key
     * @throws PageException if a SQLException occurs while getting the connection or its metadata
     */
    @Override
    public String extractDialectKey(MappedStatement ms, DataSource dataSource, Properties properties) {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            return conn.getMetaData().getURL();
        } catch (SQLException e) {
            throw new PageException(e);
        } finally {
            if (conn != null) {
                try {
                    String closeConn = properties.getProperty("closeConn");
                    if (StringKit.isEmpty(closeConn) || Boolean.parseBoolean(closeConn)) {
                        conn.close();
                    }
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Extracts and returns the appropriate {@link AbstractPaging} dialect based on the extracted JDBC URL.
     *
     * @param dialectKey the JDBC URL obtained from {@link #extractDialectKey}
     * @param ms         the MappedStatement being executed
     * @param dataSource the DataSource associated with the MappedStatement
     * @param properties the configuration properties
     * @return an instance of {@link AbstractPaging} representing the determined dialect
     * @throws PageException if the database type cannot be automatically obtained or if dialect instantiation fails
     */
    @Override
    public AbstractPaging extractDialect(
            String dialectKey,
            MappedStatement ms,
            DataSource dataSource,
            Properties properties) {
        String dialectStr = PageAutoDialect.fromJdbcUrl(dialectKey);
        if (dialectStr == null) {
            throw new PageException(
                    "The database type cannot be obtained automatically, please specify it via the pagerDialect parameter!");
        }
        return PageAutoDialect.instanceDialect(dialectStr, properties);
    }

}
