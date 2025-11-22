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
import org.miaixz.bus.mapper.Holder;
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
public class VisibleHandler<T> extends ConditionHandler<T> {

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

        // Try to get provider from properties
        VisibleProvider provider = null;
        Object object = properties.get(Args.PROVIDER_KEY);
        if (object instanceof VisibleProvider) {
            provider = (VisibleProvider) object;
        }

        // Set provider if found
        if (provider == null) {
            Logger.warn(false, "Mapper", "Provider not found, feature will not be enabled");
            return false;
        }

        // Get current datasource key
        String datasourceKey = Holder.getKey();
        if (StringKit.isEmpty(datasourceKey)) {
            // Use actual default datasource name or fallback to "default"
            datasourceKey = "default";
        }

        // Build configuration paths
        String sharedPrefix = Args.SHARED_KEY + Symbol.DOT + Args.VISIBLE_KEY + Symbol.DOT;
        String dsPrefix = datasourceKey + Symbol.DOT + Args.VISIBLE_KEY + Symbol.DOT;

        // Merge configuration: datasource-specific > shared > default
        String ignore = properties.getProperty(
                dsPrefix + Args.PROP_IGNORE,
                properties.getProperty(sharedPrefix + Args.PROP_IGNORE, Normal.EMPTY));

        // Get ignore tables list
        List<String> ignoreTables = StringKit.isNotEmpty(ignore) ? Arrays.stream(ignore.split(Symbol.COMMA))
                .map(String::trim).filter(ObjectKit::isNotEmpty).collect(Collectors.toList()) : Collections.emptyList();

        // Build and store config
        this.config = VisibleConfig.builder().provider(provider).ignore(ignoreTables).build();

        return true;
    }

    /**
     * Get current effective configuration with priority: Context > File Config.
     *
     * @return the effective visible configuration
     */
    private VisibleConfig getConfig() {
        // 1. Highest priority: Context configuration
        Context.MapperConfig context = Context.getMapperConfig();
        if (context != null && context.getVisible() != null) {
            return context.getVisible();
        }

        // 2. Lowest priority: File configuration
        return config;
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
        VisibleConfig currentConfig = getConfig();

        // Skip if perimeter control is disabled
        if (currentConfig == null) {
            Logger.debug(true, "Visible", "Visibility control disabled for query: {}", ms.getId());
            return true;
        }

        // Skip if perimeter filtering is ignored
        if (VisibleContext.isIgnore()) {
            Logger.debug(true, "Visible", "Visibility filtering ignored for query: {}", ms.getId());
            return true;
        }

        // Skip if provider is not configured
        if (currentConfig.getProvider() == null) {
            Logger.warn(true, "Visible", "Visibility provider not configured for query: {}", ms.getId());
            return true;
        }

        // Only handle SELECT queries
        if (ms.getSqlCommandType() != SqlCommandType.SELECT) {
            Logger.debug(true, "Visible", "Skipped non-SELECT query: {}", ms.getId());
            return true;
        }

        // Get original SQL
        String originalSql = boundSql.getSql();

        // Create builder for current config and apply perimeter condition
        VisibleBuilder builder = new VisibleBuilder(currentConfig);
        String modifiedSql = builder.applyVisibility(originalSql);

        // If SQL was modified, update the bound SQL
        if (!originalSql.equals(modifiedSql)) {
            Logger.debug(false, "Visible", "Applied visibility filter for query: {}", ms.getId());
            // Use reflection to update SQL in BoundSql
            try {
                java.lang.reflect.Field field = BoundSql.class.getDeclaredField("sql");
                field.setAccessible(true);
                field.set(boundSql, modifiedSql);
            } catch (Exception e) {
                // If reflection fails, log warning and continue with original SQL
                Logger.warn(false, "Visible", "Failed to update SQL: {}", e.getMessage(), e);
            }
        } else {
            Logger.debug(false, "Visible", "SQL unchanged for query: {}", ms.getId());
        }

        return true;
    }

    /**
     * Clear metadata cache (no-op since we don't cache builders anymore).
     */
    public void clear() {
        // No-op: builder is created on-demand per SQL execution
    }

}
