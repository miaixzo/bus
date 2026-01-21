/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.util.List;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.miaixz.bus.mapper.dialect.Dialect;

/**
 * A custom {@link org.apache.ibatis.mapping.SqlSource} that wraps a modified SQL while ensuring parameter mappings
 * match the current SQL structure.
 *
 * <p>
 * <strong>Core Design:</strong>
 * </p>
 * <p>
 * This SqlSource saves the actual SQL (after interceptor processing) and delegates to the original SqlSource to get
 * correct parameter mappings. This ensures that:
 * </p>
 * <ul>
 * <li>Modified SQL (with table prefix, tenant conditions, etc.) is preserved</li>
 * <li>Parameter mappings are dynamically generated based on current parameters (no cache pollution)</li>
 * <li>Subsequent interceptors can process the modified SQL correctly</li>
 * <li>Correctness is prioritized over performance</li>
 * </ul>
 *
 * <p>
 * <strong>Dynamic SQL Generation:</strong>
 * </p>
 * <p>
 * For multi-datasource scenarios with different database dialects, this SqlSource supports dynamic SQL generation. If
 * the underlying SqlMetaCache is dynamic (has a Dialect function), SQL will be generated at runtime based on the
 * current datasource's dialect.
 * </p>
 *
 * <p>
 * <strong>Why Pure Delegation?</strong>
 * </p>
 * <p>
 * This implementation uses pure delegation - it always delegates to the original SqlSource to get ParameterMappings.
 * This approach avoids cache pollution issues where dynamic SQL (e.g., with foreach) generates different
 * ParameterMappings for different parameter instances. The trade-off is a slight performance cost for static SQL, but
 * correctness is guaranteed for all SQL types.
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
     * The actual SQL (after interceptor processing).
     * <p>
     * This SQL includes all modifications from previous interceptors (table prefix, tenant conditions, etc.). It is
     * "global" in the sense that it's saved and reused across invocations.
     * </p>
     */
    private final String actualSql;

    /**
     * The original SqlSource from the MappedStatement (before any interceptor modifications).
     * <p>
     * We delegate to this SqlSource to get parameter mappings and additional parameters. This ensures that dynamic SQL
     * (e.g., with foreach) generates correct ParameterMappings for each parameter instance.
     * </p>
     */
    private final org.apache.ibatis.mapping.SqlSource sqlSource;

    /**
     * The SQL metadata cache for dynamic SQL generation.
     * <p>
     * If this cache is dynamic, SQL will be generated at runtime based on the current dialect.
     * </p>
     */
    private final SqlMetaCache cache;

    /**
     * Constructs a SqlSource with actual SQL and original SqlSource (static SQL).
     *
     * @param ms        the MappedStatement
     * @param actualSql the actual SQL (after interceptor processing)
     * @param sqlSource the original SqlSource before interceptor modifications
     */
    public SqlSource(MappedStatement ms, String actualSql, org.apache.ibatis.mapping.SqlSource sqlSource) {
        this(ms, actualSql, sqlSource, null);
    }

    /**
     * Constructs a SqlSource with actual SQL, original SqlSource, and SqlMetaCache (dynamic SQL support).
     *
     * @param ms        the MappedStatement
     * @param actualSql the actual SQL (after interceptor processing)
     * @param sqlSource the original SqlSource before interceptor modifications
     * @param cache     the SQL metadata cache for dynamic SQL generation (optional)
     */
    public SqlSource(MappedStatement ms, String actualSql, org.apache.ibatis.mapping.SqlSource sqlSource,
            SqlMetaCache cache) {
        this.configuration = ms.getConfiguration();
        this.actualSql = actualSql;
        this.sqlSource = sqlSource;
        this.cache = cache;
    }

    /**
     * Returns a BoundSql with the actual SQL and parameter mappings from the original SqlSource.
     * <p>
     * This method always delegates to the original SqlSource to get correct ParameterMappings. This pure delegation
     * approach ensures:
     * </p>
     * <ul>
     * <li>Dynamic SQL (with foreach, if conditions, etc.) always gets correct ParameterMappings</li>
     * <li>Static SQL gets ParameterMappings that match the current parameter instance</li>
     * <li>No cache pollution: different parameter instances get different ParameterMappings when needed</li>
     * </ul>
     * <p>
     * If the underlying SqlMetaCache is dynamic, SQL will be generated at runtime based on the current datasource's
     * dialect.
     * </p>
     *
     * @param parameterObject the parameter object for the SQL execution
     * @return a BoundSql instance with actual SQL and correct parameter mappings
     */
    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        // Determine the SQL to use (static or dynamic)
        String finalSql = actualSql;

        if (cache != null && cache.isDynamic()) {
            // Get dialect for current datasource and generate SQL dynamically
            Dialect dialect = org.miaixz.bus.mapper.dialect.DialectRegistry.getDialect();
            finalSql = cache.getSqlScript(dialect);
        }

        // Delegate to original SqlSource to get correct ParameterMappings
        // The original SqlSource (e.g., DynamicSqlSource) handles dynamic SQL properly
        BoundSql boundSql = this.sqlSource.getBoundSql(parameterObject);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        // Create new BoundSql with final SQL and parameter mappings from original
        BoundSql newBoundSql = new BoundSql(this.configuration, finalSql, parameterMappings, parameterObject);

        // Copy additional parameters if present
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
     * Gets the actual SQL.
     *
     * @return the actual SQL string
     */
    public String getActualSql() {
        return actualSql;
    }

}
