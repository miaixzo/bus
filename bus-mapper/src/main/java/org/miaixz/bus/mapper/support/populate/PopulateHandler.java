/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.mapper.support.populate;

import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Created;
import org.miaixz.bus.core.lang.annotation.Creator;
import org.miaixz.bus.core.lang.annotation.Modified;
import org.miaixz.bus.core.lang.annotation.Modifier;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Context;
import org.miaixz.bus.mapper.handler.ConditionHandler;

/**
 * Data fill interceptor handler.
 *
 * <p>
 * This handler intercepts SQL execution and automatically fills entity fields with timestamps and user information
 * based on the operation type (INSERT or UPDATE). It works in conjunction with {@link PopulateBuilder} to perform the
 * actual field population.
 * </p>
 *
 * <p>
 * Supported annotations:
 * </p>
 * <ul>
 * <li>{@link Created} - Auto-filled on INSERT operations</li>
 * <li>{@link Modified} - Auto-filled on INSERT and UPDATE operations</li>
 * <li>{@link Creator} - Auto-filled on INSERT operations (requires {@link PopulateProvider})</li>
 * <li>{@link Modifier} - Auto-filled on INSERT and UPDATE operations (requires {@link PopulateProvider})</li>
 * </ul>
 *
 * @param <T> the entity type
 * @author Kimi Liu
 * @since Java 17+
 */
public class PopulateHandler<T> extends ConditionHandler<T, PopulateConfig> {

    /**
     * Populate configuration from file (lowest priority).
     */
    private PopulateConfig config;

    /**
     * Default constructor (uses default configuration).
     */
    public PopulateHandler() {

    }

    /**
     * Constructor with file configuration.
     *
     * @param config the data fill configuration from file
     */
    public PopulateHandler(PopulateConfig config) {
        this.config = config;
    }

    /**
     * Get the handler name for logging purposes.
     *
     * @return the handler name "Populate"
     */
    @Override
    public String getHandler() {
        return "Populate";
    }

    /**
     * Sets the populate-related configuration properties. This method is typically called during plugin initialization
     * to configure data fill behaviors.
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
        PopulateProvider provider = getProvider(properties, PopulateProvider.class);

        // Set provider if found
        if (provider == null) {
            Logger.warn(false, "Mapper", "Provider not found, feature will not be enabled");
            return false;
        }

        // Build initial static config
        this.config = buildPopulateConfig(datasourceKey, properties, provider);
        return true;
    }

    @Override
    protected String scope() {
        return Args.POPULATE_KEY;
    }

    @Override
    protected PopulateConfig defaults() {
        return config;
    }

    @Override
    protected PopulateConfig capture() {
        Context.MapperConfig contextConfig = Context.getMapperConfig();
        return contextConfig != null ? contextConfig.getPopulate() : null;
    }

    @Override
    protected PopulateConfig derived(String datasourceKey, Properties properties) {
        // Try to get provider from properties
        PopulateProvider provider = getProvider(properties, PopulateProvider.class);

        // Set provider if found
        if (provider == null) {
            return null;
        }

        return buildPopulateConfig(datasourceKey, properties, provider);
    }

    /**
     * Build populate configuration from properties for a specific datasource.
     *
     * @param datasourceKey the datasource key
     * @param properties    the properties
     * @param provider      the populate provider
     * @return the populate configuration
     */
    private PopulateConfig buildPopulateConfig(String datasourceKey, Properties properties, PopulateProvider provider) {
        String sharedPrefix = Args.SHARED_KEY + Symbol.DOT + Args.POPULATE_KEY + Symbol.DOT;
        String dsPrefix = datasourceKey + Symbol.DOT + Args.POPULATE_KEY + Symbol.DOT;

        boolean created = Boolean.parseBoolean(
                properties.getProperty(
                        dsPrefix + Args.POPULATE_CREATED,
                        properties.getProperty(sharedPrefix + Args.POPULATE_CREATED, "true")));
        boolean modified = Boolean.parseBoolean(
                properties.getProperty(
                        dsPrefix + Args.POPULATE_MODIFIED,
                        properties.getProperty(sharedPrefix + Args.POPULATE_MODIFIED, "true")));
        boolean creator = Boolean.parseBoolean(
                properties.getProperty(
                        dsPrefix + Args.POPULATE_CREATOR,
                        properties.getProperty(sharedPrefix + Args.POPULATE_CREATOR, "true")));
        boolean modifier = Boolean.parseBoolean(
                properties.getProperty(
                        dsPrefix + Args.POPULATE_MODIFIER,
                        properties.getProperty(sharedPrefix + Args.POPULATE_MODIFIER, "true")));

        return PopulateConfig.builder().created(created).modified(modified).creator(creator).modifier(modifier)
                .provider(provider).build();
    }

    @Override
    public int getOrder() {
        return MIN_VALUE + 5;
    }

    /**
     * Intercept update operations (INSERT and UPDATE).
     *
     * <p>
     * This method is called before SQL execution. It checks the SQL command type and fills the appropriate fields based
     * on whether it's an INSERT or UPDATE operation.
     * </p>
     *
     * @param executor  the MyBatis executor
     * @param ms        the mapped statement
     * @param parameter the parameter object (entity or collection)
     * @return always returns true to allow the operation to proceed
     */
    @Override
    public boolean isUpdate(Executor executor, MappedStatement ms, Object parameter) {
        // Get current configuration
        PopulateConfig config = current();
        if (config == null) {
            Logger.debug(true, getHandler(), "Populate config not found, skipping: {}", ms.getId());
            return true;
        }

        // Skip if parameter is null
        if (parameter == null) {
            Logger.debug(true, getHandler(), "Parameter is null, skipping: {}", ms.getId());
            return true;
        }

        // Get SQL command type
        SqlCommandType commandType = ms.getSqlCommandType();

        // Create builder for current config and fill data
        PopulateBuilder builder = new PopulateBuilder(config);

        // Fill data based on command type
        if (commandType == SqlCommandType.INSERT) {
            Logger.debug(false, getHandler(), "Filling INSERT data for: {}", ms.getId());
            builder.fillInsertData(parameter);
        } else if (commandType == SqlCommandType.UPDATE) {
            Logger.debug(false, getHandler(), "Filling UPDATE data for: {}", ms.getId());
            builder.fillUpdateData(parameter);
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
