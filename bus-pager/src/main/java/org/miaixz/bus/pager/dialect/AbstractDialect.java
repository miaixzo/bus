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
package org.miaixz.bus.pager.dialect;

import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.pager.Builder;
import org.miaixz.bus.pager.Dialect;
import org.miaixz.bus.pager.parsing.CountSqlParser;
import org.miaixz.bus.pager.parsing.OrderBySqlParser;
import org.miaixz.bus.pager.parsing.DefaultCountSqlParser;
import org.miaixz.bus.pager.parsing.DefaultOrderBySqlParser;

/**
 * Abstract base class for database dialects, providing common functionality for SQL parsing. This class integrates
 * {@link CountSqlParser} for intelligent count queries and {@link OrderBySqlParser} for order by clause handling.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractDialect implements Dialect {

    /**
     * The SQL parser for generating count queries.
     */
    protected CountSqlParser countSqlParser;
    /**
     * The SQL parser for handling order by clauses.
     */
    protected OrderBySqlParser orderBySqlParser;

    /**
     * Generates the SQL for the count query using an intelligent SQL parser.
     *
     * @param ms              the MappedStatement object
     * @param boundSql        the BoundSql object containing the original SQL and parameters
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @param countKey        the CacheKey for the count query
     * @return the generated count SQL string
     */
    @Override
    public String getCountSql(
            MappedStatement ms,
            BoundSql boundSql,
            Object parameterObject,
            RowBounds rowBounds,
            CacheKey countKey) {
        return countSqlParser.getSmartCountSql(boundSql.getSql());
    }

    /**
     * Sets the properties for the dialect, initializing the {@link CountSqlParser} and {@link OrderBySqlParser}. Custom
     * implementations of these parsers can be specified via properties.
     *
     * @param properties the properties to set, typically from the plugin configuration
     */
    @Override
    public void setProperties(Properties properties) {
        this.countSqlParser = Builder.newInstance(
                properties.getProperty("countSqlParser"),
                CountSqlParser.class,
                properties,
                DefaultCountSqlParser::new);
        this.orderBySqlParser = Builder.newInstance(
                properties.getProperty("orderBySqlParser"),
                OrderBySqlParser.class,
                properties,
                DefaultOrderBySqlParser::new);
    }

}
