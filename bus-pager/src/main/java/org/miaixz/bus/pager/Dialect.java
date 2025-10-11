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
package org.miaixz.bus.pager;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;

/**
 * Database dialect interface for different database implementations. This interface defines methods for handling
 * pagination logic specific to various database systems.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Dialect {

    /**
     * Determines whether to skip the count query and pagination query.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return true to skip and return default query results, false to proceed with pagination query
     */
    boolean skip(MappedStatement ms, Object parameterObject, RowBounds rowBounds);

    /**
     * Checks if asynchronous count queries should be used. If true, the count will be queried asynchronously and will
     * not affect the decision to proceed with the pagination query.
     *
     * @return true for asynchronous count, false for synchronous
     */
    default boolean isAsyncCount() {
        return false;
    }

    /**
     * Executes an asynchronous count query task.
     *
     * @param task the asynchronous query task
     * @param <T>  the type of the result of the task
     * @return a Future representing the pending completion of the task
     */
    default <T> Future<T> asyncCountTask(Callable<T> task) {
        return ForkJoinPool.commonPool().submit(task);
    }

    /**
     * Called before executing the count query. If this method returns true, a count query will be performed. If it
     * returns false, the system will proceed to evaluate the {@link #beforePage(MappedStatement, Object, RowBounds)}
     * method.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return true to proceed with count query, false to skip to {@code beforePage}
     */
    boolean beforeCount(MappedStatement ms, Object parameterObject, RowBounds rowBounds);

    /**
     * Generates the SQL for the count query.
     *
     * @param ms              the MappedStatement object
     * @param boundSql        the BoundSql object containing the original SQL and parameters
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @param countKey        the CacheKey for the count query
     * @return the generated count SQL string
     */
    String getCountSql(
            MappedStatement ms,
            BoundSql boundSql,
            Object parameterObject,
            RowBounds rowBounds,
            CacheKey countKey);

    /**
     * Called after the count query has been executed.
     *
     * @param count           the total number of records found by the count query
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return true to continue with the pagination query, false to return immediately
     */
    boolean afterCount(long count, Object parameterObject, RowBounds rowBounds);

    /**
     * Processes the parameter object before the query is executed.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the parameter object for the query
     * @param boundSql        the BoundSql object containing the original SQL and parameters
     * @param pageKey         the CacheKey for the paginated query
     * @return the processed parameter object
     */
    Object processParameterObject(MappedStatement ms, Object parameterObject, BoundSql boundSql, CacheKey pageKey);

    /**
     * Called before executing the pagination query. If this method returns true, a pagination query will be performed.
     * If it returns false, default query results will be returned.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return true to proceed with pagination query, false to return default results
     */
    boolean beforePage(MappedStatement ms, Object parameterObject, RowBounds rowBounds);

    /**
     * Generates the SQL for the paginated query.
     *
     * @param ms              the MappedStatement object
     * @param boundSql        the BoundSql object containing the original SQL and parameters
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @param pageKey         the CacheKey for the paginated query
     * @return the generated paginated SQL string
     */
    String getPageSql(
            MappedStatement ms,
            BoundSql boundSql,
            Object parameterObject,
            RowBounds rowBounds,
            CacheKey pageKey);

    /**
     * Called after the pagination query has been executed to process the results. The return value of this method
     * should be directly returned by the interceptor.
     *
     * @param pageList        the list of results from the paginated query
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return the processed paginated results
     */
    Object afterPage(List pageList, Object parameterObject, RowBounds rowBounds);

    /**
     * Called after all pagination tasks are completed.
     */
    void afterAll();

    /**
     * Sets the properties for the dialect implementation.
     *
     * @param properties the plugin properties
     */
    void setProperties(Properties properties);

}
