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

import java.util.List;
import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.miaixz.bus.pager.RowBounds;

/**
 * Abstract base class for pagination based on MyBatis {@link org.apache.ibatis.session.RowBounds}. This class provides
 * a foundation for dialects that implement pagination using offset and limit.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractRowBounds extends AbstractDialect {

    /**
     * Determines whether to skip the pagination logic based on the {@link org.apache.ibatis.session.RowBounds} object.
     * Pagination is skipped if the provided {@code rowBounds} is the default
     * {@link org.apache.ibatis.session.RowBounds#DEFAULT}.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return true if pagination should be skipped, false otherwise
     */
    @Override
    public boolean skip(MappedStatement ms, Object parameterObject, org.apache.ibatis.session.RowBounds rowBounds) {
        return rowBounds == org.apache.ibatis.session.RowBounds.DEFAULT;
    }

    /**
     * Determines whether a count query should be executed before the main pagination query. A count query is executed
     * if the {@code rowBounds} is an instance of {@link RowBounds} and its {@code count} property is null or true.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return true if a count query should be executed, false otherwise
     */
    @Override
    public boolean beforeCount(
            MappedStatement ms,
            Object parameterObject,
            org.apache.ibatis.session.RowBounds rowBounds) {
        if (rowBounds instanceof RowBounds) {
            RowBounds pageRowBounds = (RowBounds) rowBounds;
            return pageRowBounds.getCount() == null || pageRowBounds.getCount();
        }
        return false;
    }

    /**
     * Called after the count query has been executed to process the count result. It sets the total count in the
     * {@link RowBounds} object and determines if the main pagination query should proceed.
     *
     * @param count           the total number of records found by the count query
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return true to continue with the pagination query if count is greater than 0, false otherwise
     */
    @Override
    public boolean afterCount(long count, Object parameterObject, org.apache.ibatis.session.RowBounds rowBounds) {
        // Due to beforeCount validation, rowBounds must be an instance of PageRowBounds here
        ((RowBounds) rowBounds).setTotal(count);
        return count > 0;
    }

    /**
     * Processes the parameter object for pagination. For {@code AbstractRowBounds}, the parameter object is returned as
     * is.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the original parameter object for the query
     * @param boundSql        the BoundSql object containing the original SQL and parameters
     * @param pageKey         the CacheKey for the paginated query
     * @return the original parameter object
     */
    @Override
    public Object processParameterObject(
            MappedStatement ms,
            Object parameterObject,
            BoundSql boundSql,
            CacheKey pageKey) {
        return parameterObject;
    }

    /**
     * Determines whether the main pagination query should be executed. For {@code AbstractRowBounds}, it always returns
     * true.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return always true, indicating the pagination query should be executed
     */
    @Override
    public boolean beforePage(
            MappedStatement ms,
            Object parameterObject,
            org.apache.ibatis.session.RowBounds rowBounds) {
        return true;
    }

    /**
     * Generates the SQL for the paginated query. It retrieves the original SQL from {@code boundSql} and delegates to
     * an abstract method for database-specific pagination SQL generation.
     *
     * @param ms              the MappedStatement object
     * @param boundSql        the BoundSql object containing the original SQL and parameters
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @param pageKey         the CacheKey for the paginated query
     * @return the generated paginated SQL string
     */
    @Override
    public String getPageSql(
            MappedStatement ms,
            BoundSql boundSql,
            Object parameterObject,
            org.apache.ibatis.session.RowBounds rowBounds,
            CacheKey pageKey) {
        String sql = boundSql.getSql();
        return getPageSql(sql, rowBounds, pageKey);
    }

    /**
     * Abstract method to generate the database-specific pagination SQL using
     * {@link org.apache.ibatis.session.RowBounds}. To be implemented by concrete dialect classes.
     *
     * @param sql       the original SQL string
     * @param rowBounds the {@link org.apache.ibatis.session.RowBounds} object containing offset and limit
     * @param pageKey   the CacheKey for the paginated query
     * @return the database-specific paginated SQL string
     */
    public abstract String getPageSql(String sql, org.apache.ibatis.session.RowBounds rowBounds, CacheKey pageKey);

    /**
     * Called after the pagination query has been executed to process the results. For {@code AbstractRowBounds}, it
     * simply returns the list of results as is.
     *
     * @param pageList        the list of results from the paginated query
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return the list of paginated results
     */
    @Override
    public Object afterPage(List pageList, Object parameterObject, org.apache.ibatis.session.RowBounds rowBounds) {
        return pageList;
    }

    /**
     * Called after all pagination tasks are completed. This implementation does nothing.
     */
    @Override
    public void afterAll() {

    }

    /**
     * Sets the properties for this dialect. Delegates to the superclass to set common properties.
     *
     * @param properties the properties to set
     */
    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
    }

}
