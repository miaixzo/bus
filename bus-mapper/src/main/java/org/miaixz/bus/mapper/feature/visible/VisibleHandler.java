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
package org.miaixz.bus.mapper.feature.visible;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Context;
import org.miaixz.bus.mapper.handler.ScopedProviderHandler;

/**
 * Visible control interceptor handler.
 *
 * <p>
 * This handler intercepts SQL query execution and automatically adds visibility conditions from the configured
 * {@link VisibleProvider}. It works in conjunction with {@link VisibleBuilder} to modify the SQL.
 *
 * @param <T> the entity type
 * @author Kimi Liu
 * @since Java 21+
 */
public class VisibleHandler<T> extends ScopedProviderHandler<T, VisibleConfig, VisibleProvider> {

    /**
     * Default constructor (uses default configuration).
     */
    public VisibleHandler() {
        super();
    }

    /**
     * Constructor with file configuration.
     *
     * @param config the visibility configuration from file
     */
    public VisibleHandler(VisibleConfig config) {
        super(config);
    }

    /**
     * Returns the property scope key used to resolve visible configuration.
     *
     * @return the property scope key
     */
    @Override
    protected String scope() {
        return Args.VISIBLE_KEY;
    }

    /**
     * Captures the current thread-local visible configuration override.
     *
     * @return the captured configuration, or {@code null} when no override is active
     */
    @Override
    protected VisibleConfig capture() {
        Context.MapperConfig context = Context.getMapperConfig();
        return context != null ? context.getVisible() : null;
    }

    /**
     * Returns the visible provider contract.
     *
     * @return the visible provider contract type
     */
    @Override
    protected Class<VisibleProvider> type() {
        return VisibleProvider.class;
    }

    /**
     * Returns whether visible configuration requires a visible provider.
     *
     * @return {@code true}
     */
    @Override
    protected boolean requiresProvider() {
        return true;
    }

    /**
     * Resolves visible configuration from properties for a specific datasource.
     *
     * @param datasourceKey the datasource key
     * @param properties    the properties
     * @param provider      the visible provider
     * @return the visible configuration
     */
    @Override
    protected VisibleConfig resolve(String datasourceKey, Properties properties, VisibleProvider provider) {
        if (provider == null) {
            return null;
        }
        String sharedPrefix = Args.SHARED_KEY + Symbol.DOT + Args.VISIBLE_KEY + Symbol.DOT;
        String dsPrefix = datasourceKey + Symbol.DOT + Args.VISIBLE_KEY + Symbol.DOT;

        String ignore = properties.getProperty(
                dsPrefix + Args.PROP_IGNORE,
                properties.getProperty(sharedPrefix + Args.PROP_IGNORE, Normal.EMPTY));

        List<String> ignoreTables = StringKit.isNotEmpty(ignore) ? Arrays.stream(ignore.split(Symbol.COMMA))
                .map(String::trim).filter(ObjectKit::isNotEmpty).collect(Collectors.toList()) : Collections.emptyList();

        return VisibleConfig.builder().provider(provider).ignore(ignoreTables).build();
    }

    /**
     * Returns the execution order for the visible handler in the mapper interceptor chain.
     *
     * @return the handler order value
     */
    @Override
    public int getOrder() {
        return MIN_VALUE + 4;
    }

    /**
     * Intercept query operations (SELECT).
     *
     * <p>
     * This method is called before SQL execution. It modifies the SQL to add visibility conditions from the current
     * {@link VisibleProvider}.
     * </p>
     *
     * <p>
     * <strong>Performance Optimization:</strong> Checks if the SqlSource has already been replaced by our custom
     * SqlSource. If yes, processing is skipped since the SQL has already been modified. This provides O(1) cache-like
     * performance without the issues of request-level caching.
     * </p>
     *
     * @param executor      the MyBatis executor
     * @param ms            the mapped statement
     * @param parameter     the parameter object
     * @param rowBounds     the row bounds
     * @param resultHandler the result handler
     * @param boundSql      the bound SQL
     * @return always returns true to allow the operation to proceed
     */
    @Override
    public boolean isQuery(
            Executor executor,
            MappedStatement ms,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {
        // Get current configuration
        VisibleConfig currentConfig = current();

        // Skip if visibility filtering is disabled
        if (currentConfig == null) {
            Logger.debug(true, "Mapper", "Visibility control disabled: method={}", ms.getId());
            return true;
        }

        // Skip if visibility filtering is ignored
        if (VisibleContext.isIgnore()) {
            Logger.debug(true, "Mapper", "Visibility filtering ignored: method={}", ms.getId());
            return true;
        }

        // Skip if provider is not configured
        if (currentConfig.getProvider() == null) {
            Logger.warn(true, "Mapper", "Visibility provider not configured: method={}", ms.getId());
            return true;
        }

        // Only handle SELECT queries
        if (ms.getSqlCommandType() != SqlCommandType.SELECT) {
            Logger.debug(true, "Mapper", "Skipped non-SELECT: method={}", ms.getId());
            return true;
        }

        String mapperId = ms.getId();

        String originalSql = boundSql.getSql();

        // Create builder for current config and apply visibility condition
        VisibleBuilder builder = new VisibleBuilder(currentConfig);
        String actualSql = builder.applyVisibility(originalSql);

        // If SQL was modified, update the bound SQL
        if (!originalSql.equals(actualSql)) {
            Logger.debug(false, "Mapper", "Applied visibility filter: method={}", mapperId);
            // Step 1: Use reflection to update SQL in the original boundSql (from interceptor)
            if (setBoundSql(boundSql, actualSql)) {
                Logger.debug(false, "Mapper", "Modified BoundSql.sql");
            } else {
                // If reflection fails, log warning and continue with original SQL
                Logger.warn(false, "Mapper", "Failed to update SQL");
            }
            putSqlRewrite(ms, actualSql);
        } else {
            Logger.debug(false, "Mapper", "SQL unchanged: method={}", mapperId);
        }

        return true;
    }

}
