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
package org.miaixz.bus.mapper.support.visible;

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
import org.miaixz.bus.core.lang.annotation.Visible;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Context;
import org.miaixz.bus.mapper.handler.ConditionHandler;

/**
 * Visible control interceptor handler.
 *
 * <p>
 * This handler intercepts SQL query execution and automatically adds perimeter conditions to filter results based on
 * the current user's data perimeter. It works in conjunction with {@link VisibleBuilder} to modify the SQL.
 *
 * @param <T> the entity type
 * @author Kimi Liu
 * @since Java 17+
 */
public class VisibleHandler<T> extends ConditionHandler<T, VisibleConfig> {

    /**
     * Visible configuration from file (lowest priority).
     */
    private VisibleConfig config;

    /**
     * Default constructor (uses default configuration).
     */
    public VisibleHandler() {

    }

    /**
     * Constructor with file configuration.
     *
     * @param config the perimeter configuration from file
     */
    public VisibleHandler(VisibleConfig config) {
        this.config = config;
    }

    /**
     * Get the handler name for logging purposes.
     *
     * @return the handler name "Visible"
     */
    @Override
    public String getHandler() {
        return "Visible";
    }

    /**
     * Sets the visible-related configuration properties. This method is typically called during plugin initialization
     * to configure data perimeter behaviors.
     *
     * @param properties the configuration properties (contains all datasources)
     * @return true if properties were successfully set, false if properties is null
     */
    @Override
    public boolean setProperties(Properties properties) {
        if (properties == null) {
            return false;
        }

        // Store all properties for dynamic lookup (in parent class)
        this.properties = properties;

        // Get current datasource key for static config initialization
        String datasourceKey = getDatasourceKey();

        // Try to get provider from properties
        VisibleProvider provider = getProvider(properties, VisibleProvider.class);

        // Set provider if found
        if (provider == null) {
            Logger.warn(false, getHandler(), "Provider not found, feature will not be enabled");
            return false;
        }

        // Build initial static config
        this.config = buildVisibleConfig(datasourceKey, properties, provider);
        return true;
    }

    @Override
    protected String scope() {
        return Args.VISIBLE_KEY;
    }

    @Override
    protected VisibleConfig defaults() {
        return config;
    }

    @Override
    protected VisibleConfig capture() {
        Context.MapperConfig context = Context.getMapperConfig();
        return context != null ? context.getVisible() : null;
    }

    @Override
    protected VisibleConfig derived(String datasourceKey, Properties properties) {
        // Try to get provider from properties
        VisibleProvider provider = getProvider(properties, VisibleProvider.class);

        // Set provider if found
        if (provider == null) {
            return null;
        }

        return buildVisibleConfig(datasourceKey, properties, provider);
    }

    /**
     * Build visible configuration from properties for a specific datasource.
     *
     * @param datasourceKey the datasource key
     * @param properties    the properties
     * @param provider      the visible provider
     * @return the visible configuration
     */
    private VisibleConfig buildVisibleConfig(String datasourceKey, Properties properties, VisibleProvider provider) {
        String sharedPrefix = Args.SHARED_KEY + Symbol.DOT + Args.VISIBLE_KEY + Symbol.DOT;
        String dsPrefix = datasourceKey + Symbol.DOT + Args.VISIBLE_KEY + Symbol.DOT;

        String ignore = properties.getProperty(
                dsPrefix + Args.PROP_IGNORE,
                properties.getProperty(sharedPrefix + Args.PROP_IGNORE, Normal.EMPTY));

        List<String> ignoreTables = StringKit.isNotEmpty(ignore) ? Arrays.stream(ignore.split(Symbol.COMMA))
                .map(String::trim).filter(ObjectKit::isNotEmpty).collect(Collectors.toList()) : Collections.emptyList();

        return VisibleConfig.builder().provider(provider).ignore(ignoreTables).build();
    }

    @Override
    public int getOrder() {
        return MIN_VALUE + 4;
    }

    /**
     * Intercept query operations (SELECT).
     *
     * <p>
     * This method is called before SQL execution. It modifies the SQL to add perimeter conditions based on the current
     * user's data perimeter and the entity's {@link Visible} annotation.
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

        // Skip if perimeter control is disabled
        if (currentConfig == null) {
            Logger.debug(true, getHandler(), "Visibility control disabled: method={}", ms.getId());
            return true;
        }

        // Skip if perimeter filtering is ignored
        if (VisibleContext.isIgnore()) {
            Logger.debug(true, getHandler(), "Visibility filtering ignored: method={}", ms.getId());
            return true;
        }

        // Skip if provider is not configured
        if (currentConfig.getProvider() == null) {
            Logger.warn(true, getHandler(), "Visibility provider not configured: method={}", ms.getId());
            return true;
        }

        // Only handle SELECT queries
        if (ms.getSqlCommandType() != SqlCommandType.SELECT) {
            Logger.debug(true, getHandler(), "Skipped non-SELECT: method={}", ms.getId());
            return true;
        }

        String mapperId = ms.getId();

        // Get FRESH SQL from original SqlSource (to avoid stale SQL)
        BoundSql freshBoundSql = getFreshBoundSql(ms, parameter);
        String originalSql = freshBoundSql.getSql();

        // Create builder for current config and apply perimeter condition
        VisibleBuilder builder = new VisibleBuilder(currentConfig);
        String actualSql = builder.applyVisibility(originalSql);

        // If SQL was modified, update the bound SQL
        if (!originalSql.equals(actualSql)) {
            Logger.debug(false, getHandler(), "Applied visibility filter: method={}", mapperId);
            // Step 1: Use reflection to update SQL in the original boundSql (from interceptor)
            if (setBoundSql(boundSql, actualSql)) {
                Logger.debug(false, getHandler(), "Modified BoundSql.sql");
            } else {
                // If reflection fails, log warning and continue with original SQL
                Logger.warn(false, getHandler(), "Failed to update SQL");
            }

            // Step 2: Replace the SqlSource in MappedStatement
            // This ensures subsequent getBoundSql() calls return the actual SQL
            replaceSqlSource(ms, boundSql, actualSql);
        } else {
            Logger.debug(false, getHandler(), "SQL unchanged: method={}", mapperId);
        }

        return true;
    }

}
