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
package org.miaixz.bus.pager.binding;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.builder.annotation.ProviderSqlSource;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.lang.exception.PageException;
import org.miaixz.bus.pager.Dialect;
import org.miaixz.bus.pager.builder.BoundSqlBuilder;

/**
 * Utility class for executing count queries in MyBatis pagination. This class handles the creation and execution of
 * count SQL, as well as parameter processing.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class CountExecutor {

    /**
     * Reflective field for accessing `additionalParameters` in {@link BoundSql}.
     */
    private static Field additionalParametersField;

    /**
     * Reflective field for accessing `providerMethodArgumentNames` in {@link ProviderSqlSource}.
     */
    private static Field providerMethodArgumentNamesField;

    static {
        try {
            additionalParametersField = BoundSql.class.getDeclaredField("additionalParameters");
            additionalParametersField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new PageException("Failed to get the BoundSql property additionalParameters: " + e, e);
        }
        try {
            // Compatible with lower versions
            providerMethodArgumentNamesField = ProviderSqlSource.class.getDeclaredField("providerMethodArgumentNames");
            providerMethodArgumentNamesField.setAccessible(true);
        } catch (NoSuchFieldException ignore) {
            // Field might not exist in all MyBatis versions, ignore if not found
        }
    }

    /**
     * Retrieves the `additionalParameters` map from a {@link BoundSql} object using reflection.
     *
     * @param boundSql the BoundSql object
     * @return a map of additional parameters
     * @throws PageException if an {@link IllegalAccessException} occurs during field access
     */
    public static Map<String, Object> getAdditionalParameter(BoundSql boundSql) {
        try {
            return (Map<String, Object>) additionalParametersField.get(boundSql);
        } catch (IllegalAccessException e) {
            throw new PageException("Failed to get the BoundSql property additionalParameters: " + e, e);
        }
    }

    /**
     * Retrieves the `providerMethodArgumentNames` array from a {@link ProviderSqlSource} object using reflection. This
     * method is used for compatibility with different MyBatis versions.
     *
     * @param providerSqlSource the ProviderSqlSource object
     * @return an array of provider method argument names, or null if the field does not exist or cannot be accessed
     * @throws PageException if an {@link IllegalAccessException} occurs during field access
     */
    public static String[] getProviderMethodArgumentNames(ProviderSqlSource providerSqlSource) {
        try {
            return providerMethodArgumentNamesField != null
                    ? (String[]) providerMethodArgumentNamesField.get(providerSqlSource)
                    : null;
        } catch (IllegalAccessException e) {
            throw new PageException("Get the ProviderSqlSource property value of providerMethodArgumentNames: " + e, e);
        }
    }

    /**
     * Attempts to retrieve an already existing {@link MappedStatement} from the configuration. This supports custom
     * count and page MappedStatements.
     *
     * @param configuration the MyBatis configuration
     * @param msId          the ID of the MappedStatement to retrieve
     * @return the MappedStatement if found, otherwise null
     */
    public static MappedStatement getExistedMappedStatement(Configuration configuration, String msId) {
        MappedStatement mappedStatement = null;
        try {
            mappedStatement = configuration.getMappedStatement(msId, false);
        } catch (Throwable t) {
            // ignore if not found
        }
        return mappedStatement;
    }

    /**
     * Executes a manually configured count query. The parameters for this query must be the same as the paginated
     * method.
     *
     * @param executor      the MyBatis executor
     * @param countMs       the MappedStatement for the count query
     * @param parameter     the parameter object for the query
     * @param boundSql      the BoundSql object for the original query
     * @param resultHandler the result handler for the query
     * @return the total count of records
     * @throws SQLException if a database access error occurs
     */
    public static Long executeManualCount(
            Executor executor,
            MappedStatement countMs,
            Object parameter,
            BoundSql boundSql,
            ResultHandler resultHandler) throws SQLException {
        CacheKey countKey = executor.createCacheKey(countMs, parameter, RowBounds.DEFAULT, boundSql);
        BoundSql countBoundSql = countMs.getBoundSql(parameter);
        Object countResultList = executor
                .query(countMs, parameter, RowBounds.DEFAULT, resultHandler, countKey, countBoundSql);
        // Some databases (e.g., TDEngine) return null when a count query has no results
        if (countResultList == null || ((List) countResultList).isEmpty()) {
            return 0L;
        }
        return ((Number) ((List) countResultList).get(0)).longValue();
    }

    /**
     * Executes an automatically generated count query.
     *
     * @param dialect       the database dialect to use
     * @param executor      the MyBatis executor
     * @param countMs       the MappedStatement for the count query
     * @param parameter     the parameter object for the query
     * @param boundSql      the BoundSql object for the original query
     * @param rowBounds     the RowBounds object containing pagination parameters
     * @param resultHandler the result handler for the query
     * @return the total count of records
     * @throws SQLException if a database access error occurs
     */
    public static Long executeAutoCount(
            Dialect dialect,
            Executor executor,
            MappedStatement countMs,
            Object parameter,
            BoundSql boundSql,
            RowBounds rowBounds,
            ResultHandler resultHandler) throws SQLException {
        Map<String, Object> additionalParameters = getAdditionalParameter(boundSql);
        // Create cache key for count query
        CacheKey countKey = executor.createCacheKey(countMs, parameter, RowBounds.DEFAULT, boundSql);
        // Call dialect to get count SQL
        String countSql = dialect.getCountSql(countMs, boundSql, parameter, rowBounds, countKey);
        // countKey.update(countSql);
        BoundSql countBoundSql = new BoundSql(countMs.getConfiguration(), countSql, boundSql.getParameterMappings(),
                parameter);
        // When using dynamic SQL, temporary parameters may be generated, which need to be manually set to the new
        // BoundSql.
        for (String key : additionalParameters.keySet()) {
            countBoundSql.setAdditionalParameter(key, additionalParameters.get(key));
        }
        // Intercept BoundSql processing
        if (dialect instanceof BoundSqlBuilder.Chain) {
            countBoundSql = ((BoundSqlBuilder.Chain) dialect)
                    .doBoundSql(BoundSqlBuilder.Type.COUNT_SQL, countBoundSql, countKey);
        }
        // Execute count query
        Object countResultList = executor
                .query(countMs, parameter, RowBounds.DEFAULT, resultHandler, countKey, countBoundSql);
        // Some databases (e.g., TDEngine) return null when a count query has no results
        if (countResultList == null || ((List) countResultList).isEmpty()) {
            return 0L;
        }
        return ((Number) ((List) countResultList).get(0)).longValue();
    }

    /**
     * Executes a paginated query.
     *
     * @param dialect       the database dialect to use
     * @param executor      the MyBatis executor
     * @param ms            the MappedStatement for the query
     * @param parameter     the parameter object for the query
     * @param rowBounds     the RowBounds object containing pagination parameters
     * @param resultHandler the result handler for the query
     * @param boundSql      the BoundSql object for the original query
     * @param cacheKey      the CacheKey for the query
     * @param <E>           the type of elements in the result list
     * @return a list of paginated results
     * @throws SQLException if a database access error occurs
     */
    public static <E> List<E> pageQuery(
            Dialect dialect,
            Executor executor,
            MappedStatement ms,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql,
            CacheKey cacheKey) throws SQLException {
        // Determine if pagination query needs to be executed
        if (dialect.beforePage(ms, parameter, rowBounds)) {
            // Process parameter object
            parameter = dialect.processParameterObject(ms, parameter, boundSql, cacheKey);
            // Call dialect to get paginated SQL
            String pageSql = dialect.getPageSql(ms, boundSql, parameter, rowBounds, cacheKey);

            Map<String, Object> additionalParameters = getAdditionalParameter(boundSql);
            boundSql = new BoundSql(ms.getConfiguration(), pageSql, boundSql.getParameterMappings(), parameter);
            // Set dynamic parameters
            for (String key : additionalParameters.keySet()) {
                boundSql.setAdditionalParameter(key, additionalParameters.get(key));
            }
            // Intercept BoundSql processing
            if (dialect instanceof BoundSqlBuilder.Chain) {
                boundSql = ((BoundSqlBuilder.Chain) dialect)
                        .doBoundSql(BoundSqlBuilder.Type.PAGE_SQL, boundSql, cacheKey);
            }
            // Execute paginated query
            return executor.query(ms, parameter, RowBounds.DEFAULT, resultHandler, cacheKey, boundSql);
        } else {
            // If pagination is not executed, do not perform in-memory pagination either
            return executor.query(ms, parameter, RowBounds.DEFAULT, resultHandler, cacheKey, boundSql);
        }
    }

}
