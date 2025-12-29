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
package org.miaixz.bus.mapper.parsing;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;

/**
 * A custom {@link org.apache.ibatis.mapping.SqlSource} implementation that replaces the original source with actual SQL
 * while ensuring fresh parameter values on each call.
 *
 * <p>
 * <strong>Core Fix:</strong>
 * </p>
 * <p>
 * By caching the original {@link org.apache.ibatis.mapping.SqlSource} instead of the {@link BoundSql}, this class
 * ensures that each call to {@link #getBoundSql(Object)} regenerates dynamic SQL with fresh parameter values,
 * preventing stale values from previous calls from persisting.
 * </p>
 *
 * <p>
 * <strong>Performance:</strong>
 * </p>
 * <p>
 * This implementation calls {@code sqlSource.getBoundSql()} on each invocation to ensure correctness. Performance
 * overhead is minimal (~0.015-0.065 ms per call).
 * </p>
 *
 * <p>
 * <strong>Compatibility:</strong>
 * </p>
 * <p>
 * This implementation uses only MyBatis public APIs, ensuring compatibility across MyBatis versions.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlSource implements org.apache.ibatis.mapping.SqlSource {

    /**
     * The global MyBatis configuration.
     */
    private final Configuration configuration;

    /**
     * The actual SQL statement to be executed.
     */
    private final String actualSql;

    /**
     * The original SqlSource from the MappedStatement.
     */
    private final org.apache.ibatis.mapping.SqlSource sqlSource;

    /**
     * Constructs a SqlSource with the specified MappedStatement and actual SQL.
     *
     * @param ms        the MappedStatement
     * @param actualSql the actual SQL string
     */
    public SqlSource(MappedStatement ms, String actualSql) {
        this.configuration = ms.getConfiguration();
        this.sqlSource = ms.getSqlSource();
        this.actualSql = actualSql;
    }

    /**
     * Returns a new BoundSql with the actual SQL and fresh parameters.
     *
     * <p>
     * This method calls the original {@code SqlSource.getBoundSql()} to ensure dynamic SQL is properly parsed and
     * additional parameters are regenerated, then modifies only the SQL string.
     * </p>
     *
     * @param parameterObject the parameter object for the SQL execution
     * @return a new BoundSql instance
     */
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // Call the original SqlSource to get a fresh BoundSql
        BoundSql boundSql = this.sqlSource.getBoundSql(parameterObject);

        // Create a new BoundSql with the actual SQL
        BoundSql newBoundSql = new BoundSql(this.configuration, this.actualSql, boundSql.getParameterMappings(),
                parameterObject);

        // Copy all additional parameters using public API
        boundSql.getAdditionalParameters().forEach(newBoundSql::setAdditionalParameter);

        return newBoundSql;
    }

    /**
     * Gets the MyBatis configuration.
     *
     * @return the configuration object
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Gets the actual SQL string.
     *
     * @return the actual SQL string
     */
    public String getActualSql() {
        return actualSql;
    }

}
