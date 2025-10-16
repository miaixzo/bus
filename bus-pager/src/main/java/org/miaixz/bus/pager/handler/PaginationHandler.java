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
package org.miaixz.bus.pager.handler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.handler.MapperHandler;
import org.miaixz.bus.pager.Builder;
import org.miaixz.bus.pager.Dialect;
import org.miaixz.bus.pager.Page;
import org.miaixz.bus.pager.binding.CountExecutor;
import org.miaixz.bus.pager.binding.CountMappedStatement;
import org.miaixz.bus.pager.binding.CountMsId;
import org.miaixz.bus.pager.binding.PageMethod;
import org.miaixz.bus.pager.builder.BoundSqlBuilder;
import org.miaixz.bus.pager.cache.CacheFactory;

import net.sf.jsqlparser.statement.select.Select;

/**
 * Pagination handler for query pagination. This class intercepts query operations to apply pagination logic, including
 * count queries and dialect-specific SQL modifications.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PaginationHandler extends SqlParserHandler implements MapperHandler {

    /**
     * Cache for count query MappedStatements.
     */
    private CacheX<String, MappedStatement> msCountMap;
    /**
     * Generator for count query MappedStatement IDs.
     */
    private CountMsId countMsId = CountMsId.DEFAULT;
    /**
     * The pagination dialect, controlling pagination logic.
     */
    private volatile Dialect dialect;
    /**
     * Suffix for count queries.
     */
    private String countSuffix = "_COUNT";
    /**
     * Flag indicating if debug mode is enabled.
     */
    private boolean debug;
    /**
     * Default pagination dialect class.
     */
    private final String default_dialect_class = "org.miaixz.bus.pager.PageContext";

    /**
     * Checks if debug mode is enabled.
     *
     * @return true if debug mode is enabled, false otherwise
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Logs the pagination call stack in debug mode.
     */
    protected void debugStackTraceLog() {
        if (isDebug()) {
            Page<Object> page = PageMethod.getLocalPage();
            Logger.debug("Pagination call stack: {}", page.getStackTrace());
        }
    }

    /**
     * Ensures that the pagination dialect is initialized.
     */
    private void checkDialectExists() {
        if (dialect == null) {
            synchronized (default_dialect_class) {
                if (dialect == null) {
                    setProperties(new Properties());
                }
            }
        }
    }

    /**
     * Determines whether a pagination query needs to be executed.
     *
     * @param executor        the MyBatis executor
     * @param mappedStatement the MappedStatement object
     * @param parameter       the query parameters
     * @param rowBounds       the pagination parameters
     * @param resultHandler   the result handler
     * @param boundSql        the bound SQL
     * @return true if pagination is required, false otherwise
     */
    @Override
    public boolean isQuery(
            Executor executor,
            MappedStatement mappedStatement,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {
        Page<Object> page = PageMethod.getLocalPage();
        if (page == null || page.getPageSize() < 0 || resultHandler != Executor.NO_RESULT_HANDLER) {
            return true;
        }
        checkDialectExists();
        if (!dialect.skip(mappedStatement, parameter, rowBounds)) {
            try {
                String sql = boundSql.getSql();
                if (StringKit.isBlank(sql)) {
                    Logger.warn("Empty SQL detected, MappedStatement: {}", mappedStatement.getId());
                    return false;
                }
                parserSingle(sql, parameter);
                return true;
            } catch (Exception e) {
                Logger.error("Failed to parse query SQL: {}", e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * Executes the pagination query, handling both COUNT and pagination logic.
     *
     * @param result          the pagination result
     * @param executor        the MyBatis executor
     * @param mappedStatement the MappedStatement object
     * @param parameter       the query parameters
     * @param rowBounds       the pagination parameters
     * @param resultHandler   the result handler
     * @param boundSql        the bound SQL
     */
    @Override
    public void query(
            Object result,
            Executor executor,
            MappedStatement mappedStatement,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {
        try {
            CacheKey cacheKey = executor.createCacheKey(mappedStatement, parameter, rowBounds, boundSql);
            checkDialectExists();
            // Handle BoundSql interception logic
            if (dialect instanceof BoundSqlBuilder.Chain) {
                boundSql = ((BoundSqlBuilder.Chain) dialect)
                        .doBoundSql(BoundSqlBuilder.Type.ORIGINAL, boundSql, cacheKey);
            }
            List resultList;
            // Check if pagination is needed
            if (!dialect.skip(mappedStatement, parameter, rowBounds)) {
                debugStackTraceLog();
                Future<Long> countFuture = null;
                // Check if COUNT query is needed
                if (dialect.beforeCount(mappedStatement, parameter, rowBounds)) {
                    if (dialect.isAsyncCount()) {
                        countFuture = asyncCount(mappedStatement, boundSql, parameter, rowBounds);
                    } else {
                        Long count = count(executor, mappedStatement, parameter, rowBounds, null, boundSql);
                        if (!dialect.afterCount(count, parameter, rowBounds)) {
                            ((Object[]) result)[0] = dialect.afterPage(new ArrayList(), parameter, rowBounds);
                            return;
                        }
                    }
                }
                resultList = CountExecutor.pageQuery(
                        dialect,
                        executor,
                        mappedStatement,
                        parameter,
                        rowBounds,
                        resultHandler,
                        boundSql,
                        cacheKey);
                if (countFuture != null) {
                    Long count = countFuture.get();
                    dialect.afterCount(count, parameter, rowBounds);
                }
            } else {
                // Default in-memory pagination
                resultList = executor.query(mappedStatement, parameter, rowBounds, resultHandler, cacheKey, boundSql);
            }
            ((Object[]) result)[0] = dialect.afterPage(resultList, parameter, rowBounds);
        } catch (Exception e) {
            Logger.error("==>     Failed: {}", e.getMessage());
            throw new InternalException("Failed to process pagination SQL: " + e.getMessage(), e);
        } finally {
            if (dialect != null) {
                dialect.afterAll();
            }
        }
    }

    /**
     * Asynchronously executes a COUNT query.
     *
     * @param mappedStatement the MappedStatement object
     * @param boundSql        the bound SQL
     * @param parameter       the query parameters
     * @param rowBounds       the pagination parameters
     * @return a Future containing the result of the COUNT query
     */
    private Future<Long> asyncCount(
            MappedStatement mappedStatement,
            BoundSql boundSql,
            Object parameter,
            RowBounds rowBounds) {
        Configuration configuration = mappedStatement.getConfiguration();
        BoundSql countBoundSql = new BoundSql(configuration, boundSql.getSql(),
                new ArrayList<>(boundSql.getParameterMappings()), parameter);
        Map<String, Object> additionalParameter = CountExecutor.getAdditionalParameter(boundSql);
        if (additionalParameter != null) {
            for (String key : additionalParameter.keySet()) {
                countBoundSql.setAdditionalParameter(key, additionalParameter.get(key));
            }
        }
        TransactionFactory transactionFactory = new ManagedTransactionFactory();
        Transaction tx = transactionFactory.newTransaction(configuration.getEnvironment().getDataSource(), null, false);
        Executor countExecutor = configuration.newExecutor(tx, configuration.getDefaultExecutorType());
        return dialect.asyncCountTask(() -> {
            try {
                return count(countExecutor, mappedStatement, parameter, rowBounds, null, countBoundSql);
            } finally {
                try {
                    tx.close();
                } catch (SQLException e) {
                    Logger.error("Failed to close transaction: {}", e.getMessage());
                }
            }
        });
    }

    /**
     * Executes a COUNT query.
     *
     * @param executor        the MyBatis executor
     * @param mappedStatement the MappedStatement object
     * @param parameter       the query parameters
     * @param rowBounds       the pagination parameters
     * @param resultHandler   the result handler
     * @param boundSql        the bound SQL
     * @return the total number of records
     * @throws SQLException if the COUNT query fails
     */
    private Long count(
            Executor executor,
            MappedStatement mappedStatement,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) throws SQLException {
        String countMsId = this.countMsId.genCountMsId(mappedStatement, parameter, boundSql, countSuffix);
        Long count;
        MappedStatement countMs = CountExecutor
                .getExistedMappedStatement(mappedStatement.getConfiguration(), countMsId);
        if (countMs != null) {
            count = CountExecutor.executeManualCount(executor, countMs, parameter, boundSql, resultHandler);
        } else {
            if (msCountMap != null) {
                countMs = msCountMap.read(countMsId);
            }
            if (countMs == null) {
                countMs = CountMappedStatement.newCountMappedStatement(mappedStatement, countMsId);
                if (msCountMap != null) {
                    msCountMap.write(countMsId, countMs, 60);
                }
            }
            count = CountExecutor
                    .executeAutoCount(dialect, executor, countMs, parameter, boundSql, rowBounds, resultHandler);
        }
        return count;
    }

    /**
     * Processes a SELECT statement, logging pagination-related information.
     *
     * @param select the SELECT statement object
     * @param index  the index of the statement
     * @param sql    the original SQL statement
     * @param obj    additional object
     */
    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {

    }

    /**
     * Sets the properties for the pagination handler, initializing the cache and dialect.
     *
     * @param properties the configuration properties
     * @return true if properties were set successfully
     */
    @Override
    public boolean setProperties(Properties properties) {
        msCountMap = CacheFactory.createCache(properties.getProperty("msCountCache"), "ms", properties);
        String dialectClass = properties.getProperty("dialect");
        if (StringKit.isEmpty(dialectClass)) {
            dialectClass = default_dialect_class;
        }
        Dialect tempDialect = Builder.newInstance(dialectClass, properties);
        tempDialect.setProperties(properties);

        String countSuffix = properties.getProperty("countSuffix");
        if (StringKit.isNotEmpty(countSuffix)) {
            this.countSuffix = countSuffix;
        }

        debug = Boolean.parseBoolean(properties.getProperty("debug"));

        String countMsIdGenClass = properties.getProperty("countMsId");
        if (StringKit.isNotEmpty(countMsIdGenClass)) {
            countMsId = Builder.newInstance(countMsIdGenClass, properties);
        }
        dialect = tempDialect;
        return true;
    }

    /**
     * Retrieves the current pagination dialect.
     *
     * @return the pagination dialect
     */
    public Dialect getDialect() {
        return this.dialect;
    }

}
