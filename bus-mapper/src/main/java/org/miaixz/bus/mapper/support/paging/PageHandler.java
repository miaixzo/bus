/*
 * ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2025 miaixz.org and other contributors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.mapper.support.paging;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.lang.exception.MapperException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.dialect.Dialect;
import org.miaixz.bus.mapper.dialect.DialectRegistry;
import org.miaixz.bus.mapper.handler.ConditionHandler;

/**
 * Pagination interceptor handler for automatic pagination support.
 *
 * <p>
 * This handler intercepts query executions and automatically applies pagination based on the thread-local pagination
 * information set by {@link PageContext}. It supports multiple database dialects and automatically generates count
 * queries. It also integrates with the {@link PageBuilder} to handle sorting.
 * </p>
 *
 * <p>
 * Usage:
 * </p>
 *
 * <pre>{@code
 *
 * // Configure the interceptor
 * MybatisInterceptor interceptor = new MybatisInterceptor();
 * interceptor.addHandler(new PageHandler());
 *
 * // Use with PageContext and Sort
 * Sort sort = Sort.by("name").ascending().and("age").descending();
 * PageContext.startPage(0, 10, true, sort);
 * List<User> users = userMapper.selectAll();
 * // users is now a Page<User> with pagination info
 * }</pre>
 *
 * @param <T> the generic type parameter
 * @author Kimi Liu
 * @since Java 17+
 */
public class PageHandler<T> extends ConditionHandler<T> {

    /**
     * Cache for database dialect detection (JDBC URL -> Dialect)
     */
    private final ConcurrentMap<String, Dialect> dialectCache = new ConcurrentHashMap<>();

    /**
     * Pagination builder for sorting and SQL generation
     */
    private final PageBuilder paginationBuilder;
    /**
     * Custom parameter name mappings (e.g., "count=countSql")
     */
    private final Map<String, String> paramsMap = new HashMap<>();

    /**
     * Whether to enable pagination reasonableness. If enabled, page numbers will be adjusted to be within valid ranges.
     */
    private boolean reasonable = false;
    /**
     * Whether to support passing pagination parameters through method arguments.
     */
    private boolean supportMethodsArguments = false;

    /**
     * Creates a new PageHandler with default settings.
     */
    public PageHandler() {
        this.paginationBuilder = new PageBuilder();
    }

    @Override
    public int getOrder() {
        return MIN_VALUE + 6;
    }

    /**
     * Sets the pagination-related configuration properties. This method is typically called during plugin
     * initialization to configure default behaviors.
     *
     * @param properties the configuration properties
     * @return true if properties were successfully set, false if properties is null
     */
    @Override
    public boolean setProperties(Properties properties) {
        if (properties == null) {
            return false;
        }

        // Parse reasonable parameter
        String reasonableStr = properties.getProperty("reasonable");
        if (StringKit.isNotEmpty(reasonableStr)) {
            this.reasonable = Boolean.parseBoolean(reasonableStr);
        }

        // Parse supportMethodsArguments parameter
        String supportMethodsArgumentsStr = properties.getProperty("supportMethodsArguments");
        if (StringKit.isNotEmpty(supportMethodsArgumentsStr)) {
            this.supportMethodsArguments = Boolean.parseBoolean(supportMethodsArgumentsStr);
        }

        // Parse params parameter (e.g., "count=countSql")
        String params = properties.getProperty("params");
        if (StringKit.isNotEmpty(params)) {
            parseParams(params);
        }
        return true;
    }

    /**
     * Parses the params string and populates the paramsMap.
     *
     * @param params the params string (e.g., "count=countSql;pageSize=limit")
     */
    private void parseParams(String params) {
        String[] ps = params.split("[;|,|&]");
        for (String s : ps) {
            String[] ss = s.split("[=|:]");
            if (ss.length == 2) {
                paramsMap.put(ss[0].trim(), ss[1].trim());
            }
        }
    }

    @Override
    public void getBoundSql(StatementHandler statementHandler) {
        // Check if pagination is enabled for this thread
        Pageable pageable = PageContext.getLocalPage();
        if (pageable == null || pageable.isUnpaged()) {
            Logger.debug(true, "Page", "Pagination not enabled, skipping getBoundSql");
            return;
        }

        // Get BoundSql
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        if (boundSql == null) {
            Logger.debug(true, "Page", "BoundSql is null in getBoundSql");
            return;
        }

        // Get MappedStatement
        MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (ms == null) {
            Logger.debug(true, "Page", "MappedStatement is null in getBoundSql");
            return;
        }

        Logger.debug(false, "Page", "Applying sorting in getBoundSql: {}", ms.getId());
        // Apply sorting to SQL
        applySorting(boundSql, ms, pageable);
    }

    @Override
    public void prepare(StatementHandler statementHandler) {
        // Apply pagination SQL modifications
        Pageable pageable = PageContext.getLocalPage();
        if (pageable == null || pageable.isUnpaged()) {
            Logger.debug(true, "Page", "Pagination not enabled, skipping prepare phase");
            return;
        }

        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

        // Apply sorting modifications if needed
        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            Logger.debug(false, "Page", "Applying sorting in prepare phase: {}", ms != null ? ms.getId() : "unknown");
            applySorting(boundSql, ms, pageable);
        }
    }

    @Override
    public void query(
            Object result,
            Executor executor,
            MappedStatement mappedStatement,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {
        // Check if pagination is enabled for this thread
        Pageable pageable = PageContext.getLocalPage();
        if (pageable == null || pageable.isUnpaged()) {
            Logger.debug(true, "Page", "Pagination not enabled, skipping query: {}", mappedStatement.getId());
            // No pagination, let the query proceed normally
            return;
        }

        Logger.debug(
                false,
                "Page",
                "Processing pagination query (pageNo={}, pageSize={}): {}",
                pageable.getPageNo(),
                pageable.getPageSize(),
                mappedStatement.getId());

        try {
            // Get database dialect
            Dialect dialect = getDialect(executor);
            Logger.debug(false, "Page", "Using dialect: {}", dialect.getClass().getSimpleName());

            // Execute count query if needed
            long total = 0;
            boolean performCount = PageContext.getLocalCount();
            if (performCount) {
                Logger.debug(false, "Page", "Executing count query: {}", mappedStatement.getId());
                total = executeCountQuery(executor, mappedStatement, parameter, boundSql, dialect);
                Logger.debug(false, "Page", "Count query result: {} records", total);
                if (total == 0) {
                    Logger.debug(false, "Page", "No results found, returning empty page");
                    // No results, return empty page
                    if (result instanceof Object[]) {
                        ((Object[]) result)[0] = Page.builder().result(Collections.emptyList()).pageable(pageable)
                                .total(0).build();
                    }
                    return;
                }
            }

            // Apply reasonable logic if enabled
            if (reasonable && performCount && total > 0) {
                Logger.debug(false, "Page", "Applying reasonable logic (total={})", total);
                pageable = applyReasonable(pageable, total);
            }

            // Execute pagination query
            Logger.debug(false, "Page", "Executing pagination query: {}", mappedStatement.getId());
            List<Object> data = executePaginationQuery(
                    executor,
                    mappedStatement,
                    parameter,
                    resultHandler,
                    boundSql,
                    pageable,
                    dialect);

            // Wrap result in Page
            Page<Object> page = Page.builder().result(data).pageable(pageable).total(total).build();
            Logger.debug(false, "Page", "Pagination completed: {} records returned", data.size());

            // Set result
            if (result instanceof Object[]) {
                ((Object[]) result)[0] = page;
            }

        } catch (Exception e) {
            Logger.error(false, "Page", "{}", e.getMessage(), e);
            throw new MapperException("Failed to execute pagination query: " + e.getMessage(), e);
        }
    }

    /**
     * Applies reasonable logic to adjust page number if out of bounds.
     *
     * @param pageable      the original pageable
     * @param totalElements the total number of elements
     * @return the adjusted pageable
     */
    private Pageable applyReasonable(Pageable pageable, long totalElements) {
        int pageNo = pageable.getPageNo();
        int pageSize = pageable.getPageSize();
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        // If page number is less than 0, set to 0 (first page)
        if (pageNo < 0) {
            return Pageable.of(0, pageSize, pageable.getSort());
        }

        // If page number is greater than or equal to total pages, set to last page
        if (totalPages > 0 && pageNo >= totalPages) {
            return Pageable.of(totalPages - 1, pageSize, pageable.getSort());
        }

        return pageable;
    }

    /**
     * Gets the database dialect for the given executor.
     *
     * @param executor the executor
     * @return the database dialect
     * @throws SQLException if unable to determine the dialect
     */
    private Dialect getDialect(Executor executor) throws SQLException {
        // Do not use try-with-resources here, as the connection is managed by MyBatis
        Connection connection = executor.getTransaction().getConnection();
        String url = connection.getMetaData().getURL();
        return dialectCache.computeIfAbsent(url, DialectRegistry::getDialectByUrl);
    }

    /**
     * Executes a count query to get the total number of elements.
     *
     * @param executor  the executor
     * @param ms        the mapped statement
     * @param parameter the parameter
     * @param boundSql  the bound SQL
     * @param dialect   the database dialect
     * @return the total number of elements
     * @throws Exception if the count query fails
     */
    private long executeCountQuery(
            Executor executor,
            MappedStatement ms,
            Object parameter,
            BoundSql boundSql,
            Dialect dialect) throws Exception {
        // Generate count SQL
        String countSql = dialect.getCountSql(boundSql.getSql());

        // Create count statement ID based on params mapping
        String countStatementId = ms.getId() + "_COUNT";
        if (paramsMap.containsKey("count")) {
            countStatementId = ms.getId() + "_" + paramsMap.get("count");
        }

        // Create count statement
        MappedStatement countMs = buildCountMappedStatement(ms, countStatementId, countSql);
        BoundSql countBoundSql = new BoundSql(ms.getConfiguration(), countSql, boundSql.getParameterMappings(),
                parameter);

        // Copy additional parameters
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            String prop = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                countBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        }

        // Execute count query
        CacheKey cacheKey = executor.createCacheKey(countMs, parameter, RowBounds.DEFAULT, countBoundSql);
        List<Object> countResult = executor.query(countMs, parameter, RowBounds.DEFAULT, null, cacheKey, countBoundSql);

        if (countResult != null && !countResult.isEmpty()) {
            Object count = countResult.get(0);
            if (count instanceof Number) {
                return ((Number) count).longValue();
            }
        }

        return 0;
    }

    /**
     * Executes a pagination query.
     *
     * @param executor      the executor
     * @param ms            the mapped statement
     * @param parameter     the parameter
     * @param resultHandler the result handler
     * @param boundSql      the bound SQL
     * @param pageable      the pageable
     * @param dialect       the database dialect
     * @return the paginated results
     * @throws Exception if the pagination query fails
     */
    private List<Object> executePaginationQuery(
            Executor executor,
            MappedStatement ms,
            Object parameter,
            ResultHandler resultHandler,
            BoundSql boundSql,
            Pageable pageable,
            Dialect dialect) throws Exception {
        // Generate pagination SQL
        String paginatedSql = dialect.getPaginationSql(boundSql.getSql(), pageable);

        // Create bound SQL for pagination
        BoundSql paginatedBoundSql = new BoundSql(ms.getConfiguration(), paginatedSql, boundSql.getParameterMappings(),
                parameter);

        // Copy additional parameters
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            String prop = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                paginatedBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        }

        // Execute pagination query
        CacheKey cacheKey = executor.createCacheKey(ms, parameter, RowBounds.DEFAULT, paginatedBoundSql);
        List<Object> result = executor
                .query(ms, parameter, RowBounds.DEFAULT, resultHandler, cacheKey, paginatedBoundSql);

        return result != null ? result : Collections.emptyList();
    }

    /**
     * Builds a MappedStatement for count queries.
     *
     * @param ms          the original mapped statement
     * @param statementId the count statement ID
     * @param countSql    the count SQL
     * @return the count mapped statement
     */
    private MappedStatement buildCountMappedStatement(MappedStatement ms, String statementId, String countSql) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), statementId,
                new CountSqlSource(ms, countSql), ms.getSqlCommandType());

        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());

        String[] keyProperties = ms.getKeyProperties();
        if (keyProperties != null) {
            builder.keyProperty(String.join(",", keyProperties));
        }

        builder.timeout(ms.getTimeout());
        builder.parameterMap(ms.getParameterMap());

        // Use Long result map for count
        List<ResultMap> resultMaps = new ArrayList<>();
        ResultMap resultMap = new ResultMap.Builder(ms.getConfiguration(), ms.getId(), Long.class,
                Collections.emptyList()).build();
        resultMaps.add(resultMap);
        builder.resultMaps(resultMaps);

        builder.resultSetType(ms.getResultSetType());
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }

    /**
     * Applies sorting to the SQL using the PageBuilder.
     *
     * @param boundSql the BoundSql object
     * @param ms       the MappedStatement
     * @param pageable the pagination information
     */
    private void applySorting(BoundSql boundSql, MappedStatement ms, Pageable pageable) {
        Sort sort = pageable.getSort();
        if (sort == null || !sort.isSorted()) {
            return;
        }

        String originalSql = boundSql.getSql();
        String sortedSql = paginationBuilder.applySort(originalSql, sort);

        if (!originalSql.equals(sortedSql)) {
            // Update the SQL in BoundSql using reflection
            MetaObject metaObject = SystemMetaObject.forObject(boundSql);
            metaObject.setValue("sql", sortedSql);
        }
    }

    /**
     * SQL source for count queries.
     */
    private static class CountSqlSource implements SqlSource {

        private final MappedStatement ms;
        private final String countSql;

        public CountSqlSource(MappedStatement ms, String countSql) {
            this.ms = ms;
            this.countSql = countSql;
        }

        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            BoundSql originalBoundSql = ms.getBoundSql(parameterObject);
            BoundSql countBoundSql = new BoundSql(ms.getConfiguration(), countSql,
                    originalBoundSql.getParameterMappings(), parameterObject);

            // Copy additional parameters
            for (ParameterMapping mapping : originalBoundSql.getParameterMappings()) {
                String prop = mapping.getProperty();
                if (originalBoundSql.hasAdditionalParameter(prop)) {
                    countBoundSql.setAdditionalParameter(prop, originalBoundSql.getAdditionalParameter(prop));
                }
            }

            return countBoundSql;
        }
    }

}
