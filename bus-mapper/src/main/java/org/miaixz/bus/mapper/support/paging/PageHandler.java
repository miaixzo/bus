/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.lang.exception.MapperException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.dialect.Dialect;
import org.miaixz.bus.mapper.dialect.DialectRegistry;
import org.miaixz.bus.mapper.handler.AbstractSqlHandler;
import org.miaixz.bus.mapper.handler.MapperHandler;

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
 * PageContext.of(1, 10, true, sort);
 * List<User> users = userMapper.selectAll();
 * // users is now a Page<User> with pagination info
 * }</pre>
 *
 * @param <T> the generic type parameter
 * @author Kimi Liu
 * @since Java 21+
 */
public class PageHandler<T> extends AbstractSqlHandler implements MapperHandler<T> {

    /**
     * Cache for database dialect detection (JDBC URL -> Dialect)
     */
    private final ConcurrentMap<String, Dialect> dialectCache = new ConcurrentHashMap<>();

    /**
     * Cache for internally generated count mapped statements.
     */
    private final ConcurrentMap<CountStatementKey, MappedStatement> countMappedStatementCache = new ConcurrentHashMap<>();

    /**
     * Pagination builder for sorting and SQL generation
     */
    private final PageBuilder paginationBuilder;

    /**
     * Custom parameter name mappings (e.g., "count=countSql")
     */
    private final Map<String, String> paramsMap = new HashMap<>();

    /**
     * Thread-local flag used to skip secondary statement handling for internally generated pagination queries.
     */
    private final ThreadLocal<Boolean> internalPaginationQuery = ThreadLocal.withInitial(() -> false);

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

    /**
     * Get the handler name for logging purposes.
     *
     * @return the handler name "Page"
     */
    @Override
    public String getHandler() {
        return "Page";
    }

    /**
     * Returns the execution order for the pagination handler in the mapper interceptor chain.
     *
     * @return the handler order value
     */
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
        Logger.info(
                false,
                "Mapper",
                "Pagination handler configured: reasonable={}, supportMethodsArguments={}, paramsMapped={}",
                reasonable,
                supportMethodsArguments,
                paramsMap.size());
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

    /**
     * Applies pagination sorting while MyBatis exposes the bound SQL.
     *
     * @param statementHandler the MyBatis statement handler
     */
    @Override
    public void getBoundSql(StatementHandler statementHandler) {
        // Check if pagination is enabled for this thread
        Pageable pageable = PageContext.getLocalPage();
        if (pageable == null || pageable.isUnpaged()) {
            Logger.debug(true, "Mapper", "Pagination getBoundSql skipped: reason=pageMissing");
            return;
        }
        if (isInternalPaginationQuery()) {
            Logger.debug(true, "Mapper", "Pagination getBoundSql skipped: reason=internalQuery");
            return;
        }

        // Get BoundSql
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        if (boundSql == null) {
            Logger.debug(true, "Mapper", "Pagination getBoundSql skipped: reason=boundSqlMissing");
            return;
        }

        // Get MappedStatement
        MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (ms == null) {
            Logger.debug(true, "Mapper", "Pagination getBoundSql skipped: reason=mappedStatementMissing");
            return;
        }
        if (isCountMappedStatement(ms)) {
            Logger.debug(true, "Mapper", "Pagination getBoundSql skipped: method={}, reason=countQuery", ms.getId());
            return;
        }

        Logger.debug(true, "Mapper", "Pagination getBoundSql observed without SQL mutation: method={}", ms.getId());
    }

    /**
     * Applies pagination sorting before the JDBC statement is prepared.
     *
     * @param statementHandler the MyBatis statement handler
     */
    @Override
    public void prepare(StatementHandler statementHandler) {
        // Apply pagination SQL modifications
        Pageable pageable = PageContext.getLocalPage();
        if (pageable == null || pageable.isUnpaged()) {
            Logger.debug(true, "Mapper", "Pagination prepare skipped: reason=pageMissing");
            return;
        }
        if (isInternalPaginationQuery()) {
            Logger.debug(true, "Mapper", "Pagination prepare skipped: reason=internalQuery");
            return;
        }

        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (ms != null && isCountMappedStatement(ms)) {
            Logger.debug(true, "Mapper", "Pagination prepare skipped: method={}, reason=countQuery", ms.getId());
            return;
        }

        Logger.debug(
                true,
                "Mapper",
                "Pagination prepare observed without SQL mutation: method={}, pageNo={}, pageSize={}",
                ms != null ? ms.getId() : "unknown",
                pageable.getPageNo(),
                pageable.getPageSize());
    }

    /**
     * Executes a paginated query when a page request is active and writes the page result into the result holder.
     *
     * @param result          the mutable result holder used by the interceptor chain
     * @param executor        the MyBatis executor
     * @param mappedStatement the mapped statement being processed
     * @param parameter       the statement parameter object
     * @param rowBounds       the MyBatis row bounds
     * @param resultHandler   the MyBatis result handler
     * @param boundSql        the bound SQL being processed
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
        // Check if pagination is enabled for this thread
        Pageable pageable = PageContext.getLocalPage();

        // If no thread-local pagination, check supportMethodsArguments
        if (pageable == null || pageable.isUnpaged()) {
            if (supportMethodsArguments && parameter != null) {
                try {
                    pageable = extractPageableFromParameter(parameter);
                    if (pageable != null) {
                        PageContext.setLocalPage(pageable);
                        Logger.debug(
                                false,
                                "Mapper",
                                "Pagination extracted from method arguments: method={}, pageNo={}, pageSize={}",
                                mappedStatement.getId(),
                                pageable.getPageNo(),
                                pageable.getPageSize());
                    }
                } catch (Exception e) {
                    Logger.debug(
                            true,
                            "Mapper",
                            "Pagination argument extraction failed: method={}, exception={}",
                            mappedStatement.getId(),
                            e.getMessage());
                }
            }

            if (pageable == null || pageable.isUnpaged()) {
                Logger.debug(
                        true,
                        "Mapper",
                        "Pagination query skipped: method={}, reason=pageMissing",
                        mappedStatement.getId());
                // No pagination, let the query proceed normally
                return;
            }
        }

        Logger.debug(
                false,
                "Mapper",
                "Pagination query started: method={}, pageNo={}, pageSize={}, countEnabled={}",
                mappedStatement.getId(),
                pageable.getPageNo(),
                pageable.getPageSize(),
                PageContext.getLocalCount());

        Dialect dialect = null;
        try {
            // Get database dialect
            dialect = getDialect(executor);
            Logger.debug(
                    false,
                    "Mapper",
                    "Pagination dialect resolved: method={}, dialect={}",
                    mappedStatement.getId(),
                    dialect.getClass().getSimpleName());

            // Execute count query if needed
            long total = 0;
            boolean performCount = PageContext.getLocalCount();
            if (performCount) {
                Logger.debug(true, "Mapper", "Pagination count query started: method={}", mappedStatement.getId());
                total = executeCountQuery(executor, mappedStatement, parameter, boundSql, dialect);
                Logger.debug(
                        false,
                        "Mapper",
                        "Pagination count query completed: method={}, total={}",
                        mappedStatement.getId(),
                        total);
                if (total == 0) {
                    Logger.debug(
                            false,
                            "Mapper",
                            "Pagination completed with empty result: method={}",
                            mappedStatement.getId());
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
                Logger.debug(
                        false,
                        "Mapper",
                        "Pagination reasonable adjustment evaluated: method={}, total={}",
                        mappedStatement.getId(),
                        total);
                pageable = applyReasonable(pageable, total);
            }

            // Execute pagination query
            Logger.debug(
                    true,
                    "Mapper",
                    "Pagination data query started: method={}, pageNo={}, pageSize={}",
                    mappedStatement.getId(),
                    pageable.getPageNo(),
                    pageable.getPageSize());
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
            Logger.debug(
                    false,
                    "Mapper",
                    "Pagination completed: method={}, returnedRows={}, total={}",
                    mappedStatement.getId(),
                    data.size(),
                    total);

            // Set result
            if (result instanceof Object[]) {
                ((Object[]) result)[0] = page;
            }

        } catch (Exception e) {
            Logger.error(
                    false,
                    "Mapper",
                    e,
                    "Pagination query failed: method={}, pageNo={}, pageSize={}, dialect={}, exception={}",
                    mappedStatement.getId(),
                    pageable.getPageNo(),
                    pageable.getPageSize(),
                    dialect == null ? "unresolved" : dialect.getClass().getSimpleName(),
                    e.getClass().getSimpleName());
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

        // If page number is less than 1, set to 1 (first page)
        if (pageNo < 1) {
            return Pageable.of(1, pageSize, pageable.getSort());
        }

        // If page number is greater than total pages, set to last page
        if (totalPages > 0 && pageNo > totalPages) {
            return Pageable.of(totalPages, pageSize, pageable.getSort());
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
     * Extracts pagination parameters from the method parameter object when supportMethodsArguments is enabled.
     *
     * @param parameter the method parameter object
     * @return a Pageable object with extracted parameters, or null if no pagination parameters are found
     */
    private Pageable extractPageableFromParameter(Object parameter) {
        if (parameter == null) {
            return null;
        }

        // Use reflection to extract pagination parameters from the parameter object
        MetaObject metaObject = SystemMetaObject.forObject(parameter);

        // Default parameter names - can be configured via paramsMap
        String pageNoParam = paramsMap.getOrDefault("pageNo", "pageNo");
        String pageSizeParam = paramsMap.getOrDefault("pageSize", "pageSize");
        String countParam = paramsMap.getOrDefault("count", "count");
        String orderByParam = paramsMap.getOrDefault("orderBy", "orderBy");

        // Extract page number and page size
        Object pageNoValue = getParamValue(metaObject, pageNoParam);
        Object pageSizeValue = getParamValue(metaObject, pageSizeParam);

        if (pageNoValue == null || pageSizeValue == null) {
            // Try alternative parameter names
            pageNoValue = getParamValue(metaObject, "pageNum");
            pageSizeValue = getParamValue(metaObject, "limit");

            if (pageNoValue == null || pageSizeValue == null) {
                return null; // No valid pagination parameters found
            }
        }

        try {
            int pageNo = Integer.parseInt(String.valueOf(pageNoValue));
            int pageSize = Integer.parseInt(String.valueOf(pageSizeValue));

            if (pageNo < 1 || pageSize < 0) {
                return null; // Invalid pagination parameters
            }

            // Create Sort if orderBy parameter exists
            Sort sort = null;
            Object orderByValue = getParamValue(metaObject, orderByParam);
            if (orderByValue != null && StringKit.isNotEmpty(orderByValue.toString())) {
                sort = parseOrderBy(orderByValue.toString());
            }

            // Create Pageable
            Pageable pageable = Pageable.of(pageNo, pageSize, sort);

            // Extract optional parameters
            Object countValue = getParamValue(metaObject, countParam);
            if (countValue != null) {
                boolean performCount = Boolean.parseBoolean(String.valueOf(countValue));
                PageContext.setLocalCount(performCount);
            }

            // Note: reasonable and pageSizeZero parameters are not yet implemented
            // They can be added in future versions as needed

            return pageable;

        } catch (NumberFormatException e) {
            Logger.debug(true, "Mapper", "Invalid pagination parameter format: exception={}", e.getMessage());
            return null;
        }
    }

    /**
     * Gets parameter value from MetaObject with null check.
     *
     * @param metaObject the MetaObject
     * @param paramName  the parameter name
     * @return the parameter value or null
     */
    private Object getParamValue(MetaObject metaObject, String paramName) {
        if (!metaObject.hasGetter(paramName)) {
            return null;
        }

        Object value = metaObject.getValue(paramName);
        if (value != null && value.getClass().isArray()) {
            Object[] values = (Object[]) value;
            if (values.length > 0) {
                value = values[0];
            } else {
                value = null;
            }
        }

        return value;
    }

    /**
     * Parses orderBy string into Sort object.
     *
     * @param orderBy the orderBy string
     * @return the Sort object, or null if invalid
     */
    private Sort parseOrderBy(String orderBy) {
        if (StringKit.isBlank(orderBy)) {
            return null;
        }

        try {
            // Simple parsing for comma-separated field:direction pairs
            // Example: "name ASC, age DESC, email"
            Sort sort = Sort.unsorted();
            String[] parts = orderBy.split(",");

            for (String part : parts) {
                part = part.trim();
                if (StringKit.isBlank(part)) {
                    continue;
                }

                String[] fieldAndDirection = part.split("\\s+");
                String field = fieldAndDirection[0].trim();

                if (StringKit.isBlank(field)) {
                    continue;
                }

                if (fieldAndDirection.length > 1) {
                    String direction = fieldAndDirection[1].trim().toUpperCase();
                    if ("DESC".equals(direction)) {
                        sort = sort.and(Sort.by(field).descending());
                    } else {
                        sort = sort.and(Sort.by(field).ascending());
                    }
                } else {
                    sort = sort.and(Sort.by(field).ascending());
                }
            }

            return sort.isSorted() ? sort : null;
        } catch (Exception e) {
            Logger.debug(true, "Mapper", "Pagination orderBy parsing failed: exception={}", e.getMessage());
            return null;
        }
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
        String countSql = dialect.buildCountSql(paginationBuilder.removeSort(boundSql.getSql()));

        // Create count statement ID based on params mapping
        String countStatementId = ms.getId() + "_COUNT";
        if (paramsMap.containsKey("count")) {
            countStatementId = ms.getId() + "_" + paramsMap.get("count");
        }

        // Create count statement
        MappedStatement countMs = countMappedStatement(ms, countStatementId);
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
        List<Object> countResult;
        internalPaginationQuery.set(true);
        try {
            countResult = executor.query(countMs, parameter, RowBounds.DEFAULT, null, cacheKey, countBoundSql);
        } finally {
            internalPaginationQuery.remove();
        }

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
        String paginatedSql = dialect.buildPaginationSql(applySorting(boundSql.getSql(), pageable), pageable);

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
        List<Object> result;
        internalPaginationQuery.set(true);
        try {
            putSqlRewrite(ms, paginatedSql);
            result = executor.query(ms, parameter, RowBounds.DEFAULT, resultHandler, cacheKey, paginatedBoundSql);
        } finally {
            internalPaginationQuery.remove();
        }

        return result != null ? result : Collections.emptyList();
    }

    /**
     * Builds a MappedStatement for count queries.
     *
     * @param ms               the original mapped statement
     * @param countStatementId the count statement ID
     * @return the count mapped statement
     */
    private MappedStatement countMappedStatement(MappedStatement ms, String countStatementId) {
        CountStatementKey key = new CountStatementKey(ms.getConfiguration(), ms.getId(), countStatementId,
                Long.class.getName());
        return countMappedStatementCache
                .computeIfAbsent(key, ignored -> buildCountMappedStatement(ms, countStatementId));
    }

    /**
     * Builds a count mapped statement on cache miss.
     *
     * @param ms          the original mapped statement
     * @param statementId the count statement ID
     * @return the count mapped statement
     */
    private MappedStatement buildCountMappedStatement(MappedStatement ms, String statementId) {
        MappedStatement.Builder builder = new MappedStatement.Builder(ms.getConfiguration(), statementId,
                new CountSqlSource(ms), ms.getSqlCommandType());

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
     * Tests whether the mapped statement belongs to the internal count query.
     *
     * @param ms the mapped statement
     * @return {@code true} when the mapped statement is an internal count query
     */
    private boolean isCountMappedStatement(MappedStatement ms) {
        String countSuffix = paramsMap.containsKey("count") ? "_" + paramsMap.get("count") : "_COUNT";
        return ms != null && ms.getId().endsWith(countSuffix);
    }

    /**
     * Tests whether the current thread is executing an internally generated pagination query.
     *
     * @return {@code true} when statement-level pagination handling must be skipped
     */
    private boolean isInternalPaginationQuery() {
        return Boolean.TRUE.equals(internalPaginationQuery.get());
    }

    /**
     * Applies sorting to the given SQL text when the pageable contains sort orders.
     *
     * @param sql      the original SQL text
     * @param pageable the pagination information
     * @return the sorted SQL text
     */
    private String applySorting(String sql, Pageable pageable) {
        Sort sort = pageable.getSort();
        if (sort == null || !sort.isSorted()) {
            return sql;
        }
        return paginationBuilder.applySort(sql, sort);
    }

    /**
     * SQL source for count queries.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static class CountSqlSource implements SqlSource {

        /**
         * Mapped statement used to build the original bound SQL.
         */
        private final MappedStatement ms;

        /**
         * Creates a count SQL source.
         *
         * @param ms the mapped statement used for parameter metadata
         */
        public CountSqlSource(MappedStatement ms) {
            this.ms = ms;
        }

        /**
         * Creates bound SQL for the generated pagination count statement.
         *
         * @param parameterObject the parameter object used to build bound SQL
         * @return the bound SQL for the count query
         */
        @Override
        public BoundSql getBoundSql(Object parameterObject) {
            return ms.getBoundSql(parameterObject);
        }

    }

    /**
     * Cache key for an internally generated count mapped statement.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class CountStatementKey {

        /**
         * MyBatis configuration compared by identity.
         */
        private final Configuration configuration;

        /**
         * Original mapped statement id.
         */
        private final String statementId;

        /**
         * Count mapped statement id.
         */
        private final String countStatementId;

        /**
         * Count result type name.
         */
        private final String resultType;

        /**
         * Creates a count statement cache key.
         *
         * @param configuration    the MyBatis configuration
         * @param statementId      the original mapped statement id
         * @param countStatementId the count mapped statement id
         * @param resultType       the count result type name
         */
        private CountStatementKey(Configuration configuration, String statementId, String countStatementId,
                String resultType) {
            this.configuration = configuration;
            this.statementId = statementId;
            this.countStatementId = countStatementId;
            this.resultType = resultType;
        }

        /**
         * Tests equality using configuration identity and value fields.
         *
         * @param object the object to compare
         * @return {@code true} when both keys represent the same count statement
         */
        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof CountStatementKey that)) {
                return false;
            }
            return configuration == that.configuration && Objects.equals(statementId, that.statementId)
                    && Objects.equals(countStatementId, that.countStatementId)
                    && Objects.equals(resultType, that.resultType);
        }

        /**
         * Returns a hash code based on configuration identity and value fields.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(configuration), statementId, countStatementId, resultType);
        }

    }

}
