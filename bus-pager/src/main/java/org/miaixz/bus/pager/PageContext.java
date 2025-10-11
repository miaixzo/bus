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

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.Future;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.pager.binding.PageAutoDialect;
import org.miaixz.bus.pager.binding.PageMethod;
import org.miaixz.bus.pager.binding.PageParams;
import org.miaixz.bus.pager.dialect.AbstractPaging;
import org.miaixz.bus.pager.parsing.CountSqlParser;
import org.miaixz.bus.pager.builder.BoundSqlChainBuilder;
import org.miaixz.bus.pager.builder.BoundSqlBuilder;
import org.miaixz.bus.pager.builder.PageBoundSqlBuilder;

/**
 * Mybatis - Universal Paging Interceptor. This class extends {@link PageMethod} and implements {@link Dialect} and
 * {@link BoundSqlBuilder.Chain} to provide comprehensive pagination functionality, including SQL dialect handling,
 * parameter processing, and asynchronous count queries.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PageContext extends PageMethod implements Dialect, BoundSqlBuilder.Chain {

    /**
     * Handles page parameters extraction and processing.
     */
    private PageParams pageParams;
    /**
     * Automatically selects and manages the appropriate database dialect for pagination.
     */
    private PageAutoDialect autoDialect;
    /**
     * Builds and manages BoundSql for pagination queries.
     */
    private PageBoundSqlBuilder pageBoundSqlBuilder;
    /**
     * ForkJoinPool for executing asynchronous count queries.
     */
    private ForkJoinPool asyncCountService;

    /**
     * Determines whether to skip the count query and pagination query based on the current {@link Page} settings.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return true to skip and return default query results, false to proceed with pagination query
     */
    @Override
    public boolean skip(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        Page page = pageParams.getPage(parameterObject, rowBounds);
        if (page == null) {
            return true;
        } else {
            // Set default count column if not already set
            if (StringKit.isEmpty(page.getCountColumn())) {
                page.setCountColumn(pageParams.getCountColumn());
            }
            // Set default asynchronous count setting if not already set
            if (page.getAsyncCount() == null) {
                page.setAsyncCount(pageParams.isAsyncCount());
            }
            autoDialect.initDelegateDialect(ms, page.getDialectClass());
            return false;
        }
    }

    /**
     * Checks if asynchronous count queries are enabled for the current page.
     *
     * @return true if asynchronous count queries are enabled, false otherwise
     */
    @Override
    public boolean isAsyncCount() {
        return getLocalPage().asyncCount();
    }

    /**
     * Executes an asynchronous count query task, ensuring that {@link ThreadLocal} values are properly propagated.
     *
     * @param task the asynchronous query task
     * @param <T>  the type of the result of the task
     * @return a Future representing the pending completion of the task
     */
    @Override
    public <T> Future<T> asyncCountTask(Callable<T> task) {
        // When executing asynchronously, ThreadLocal values need to be passed, otherwise they will not be found.
        AbstractPaging dialectThreadLocal = autoDialect.getDialectThreadLocal();
        Page<Object> localPage = getLocalPage();
        String countId = UUID.randomUUID().toString();
        return asyncCountService.submit(() -> {
            try {
                // Set ThreadLocal
                autoDialect.setDialectThreadLocal(dialectThreadLocal);
                setLocalPage(localPage);
                return task.call();
            } finally {
                autoDialect.clearDelegate();
                clearPage();
            }
        });
    }

    /**
     * Called before executing the count query. Delegates to the current database dialect.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return true to proceed with count query, false to skip to {@code beforePage}
     */
    @Override
    public boolean beforeCount(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        return autoDialect.getDelegate().beforeCount(ms, parameterObject, rowBounds);
    }

    /**
     * Generates the SQL for the count query. Delegates to the current database dialect.
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
        return autoDialect.getDelegate().getCountSql(ms, boundSql, parameterObject, rowBounds, countKey);
    }

    /**
     * Called after the count query has been executed. Delegates to the current database dialect.
     *
     * @param count           the total number of records found by the count query
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return true to continue with the pagination query, false to return immediately
     */
    @Override
    public boolean afterCount(long count, Object parameterObject, RowBounds rowBounds) {
        return autoDialect.getDelegate().afterCount(count, parameterObject, rowBounds);
    }

    /**
     * Processes the parameter object before the query is executed. Delegates to the current database dialect.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the parameter object for the query
     * @param boundSql        the BoundSql object containing the original SQL and parameters
     * @param pageKey         the CacheKey for the paginated query
     * @return the processed parameter object
     */
    @Override
    public Object processParameterObject(
            MappedStatement ms,
            Object parameterObject,
            BoundSql boundSql,
            CacheKey pageKey) {
        return autoDialect.getDelegate().processParameterObject(ms, parameterObject, boundSql, pageKey);
    }

    /**
     * Called before executing the pagination query. Delegates to the current database dialect.
     *
     * @param ms              the MappedStatement object
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return true to proceed with pagination query, false to return default results
     */
    @Override
    public boolean beforePage(MappedStatement ms, Object parameterObject, RowBounds rowBounds) {
        return autoDialect.getDelegate().beforePage(ms, parameterObject, rowBounds);
    }

    /**
     * Generates the SQL for the paginated query. Delegates to the current database dialect.
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
            RowBounds rowBounds,
            CacheKey pageKey) {
        return autoDialect.getDelegate().getPageSql(ms, boundSql, parameterObject, rowBounds, pageKey);
    }

    /**
     * Generates the SQL for the paginated query using a raw SQL string.
     *
     * @param sql       the original SQL string
     * @param page      the {@link Page} object containing pagination details
     * @param rowBounds the RowBounds object containing pagination parameters
     * @param pageKey   the CacheKey for the paginated query
     * @return the generated paginated SQL string
     */
    public String getPageSql(String sql, Page page, RowBounds rowBounds, CacheKey pageKey) {
        return autoDialect.getDelegate().getPageSql(sql, page, pageKey);
    }

    /**
     * Called after the pagination query has been executed to process the results. Delegates to the current database
     * dialect. This method will be executed even if pagination is skipped, so a null check for the delegate is
     * performed.
     *
     * @param pageList        the list of results from the paginated query
     * @param parameterObject the parameter object for the query
     * @param rowBounds       the RowBounds object containing pagination parameters
     * @return the processed paginated results
     */
    @Override
    public Object afterPage(List pageList, Object parameterObject, RowBounds rowBounds) {
        // This method will be executed even if pagination is not performed, so check for null
        AbstractPaging delegate = autoDialect.getDelegate();
        if (delegate != null) {
            return delegate.afterPage(pageList, parameterObject, rowBounds);
        }
        return pageList;
    }

    /**
     * Called after all pagination tasks are completed. Clears the delegate and the page context. This method will be
     * executed even if pagination is skipped, so a null check for the delegate is performed.
     */
    @Override
    public void afterAll() {
        // This method will be executed even if pagination is not performed, so check for null
        AbstractPaging delegate = autoDialect.getDelegate();
        if (delegate != null) {
            delegate.afterAll();
            autoDialect.clearDelegate();
        }
        clearPage();
    }

    /**
     * Processes the BoundSql chain for different types of SQL operations.
     *
     * @param type     the type of BoundSql operation (e.g., COUNT, PAGE)
     * @param boundSql the original BoundSql object
     * @param cacheKey the CacheKey for the SQL operation
     * @return the processed BoundSql object
     */
    @Override
    public BoundSql doBoundSql(BoundSqlBuilder.Type type, BoundSql boundSql, CacheKey cacheKey) {
        Page<Object> localPage = getLocalPage();
        BoundSqlBuilder.Chain chain = localPage != null ? localPage.getChain() : null;
        if (chain == null) {
            BoundSqlBuilder boundSqlHandler = localPage != null ? localPage.getBoundSqlInterceptor() : null;
            BoundSqlBuilder.Chain defaultChain = pageBoundSqlBuilder != null ? pageBoundSqlBuilder.getChain() : null;
            if (boundSqlHandler != null) {
                chain = new BoundSqlChainBuilder(defaultChain, Arrays.asList(boundSqlHandler));
            } else if (defaultChain != null) {
                chain = defaultChain;
            }
            if (chain == null) {
                chain = DO_NOTHING;
            }
            if (localPage != null) {
                localPage.setChain(chain);
            }
        }
        return chain.doBoundSql(type, boundSql, cacheKey);
    }

    /**
     * Sets the properties for the PageContext, initializing internal components like {@link PageParams},
     * {@link PageAutoDialect}, and {@link PageBoundSqlBuilder}. It also configures the asynchronous count service.
     *
     * @param properties the properties to set
     */
    @Override
    public void setProperties(Properties properties) {
        setStaticProperties(properties);
        pageParams = new PageParams();
        autoDialect = new PageAutoDialect();
        pageBoundSqlBuilder = new PageBoundSqlBuilder();
        pageParams.setProperties(properties);
        autoDialect.setProperties(properties);
        pageBoundSqlBuilder.setProperties(properties);
        // 20180902新增 aggregateFunctions, 允许手动添加聚合函数（影响行数）
        CountSqlParser.addAggregateFunctions(properties.getProperty("aggregateFunctions"));
        // Asynchronous asyncCountService concurrency setting, defaults to available processors * 2.
        // A more reasonable value should consider the processing capability of the database server.
        int asyncCountParallelism = Integer.parseInt(
                properties.getProperty(
                        "asyncCountParallelism",
                        Normal.EMPTY + (Runtime.getRuntime().availableProcessors() * 2)));
        asyncCountService = new ForkJoinPool(asyncCountParallelism, pool -> {
            final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            worker.setName("pager-async-count-" + worker.getPoolIndex());
            return worker;
        }, null, true);
    }

}
