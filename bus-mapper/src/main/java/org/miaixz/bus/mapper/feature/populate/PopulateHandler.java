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
package org.miaixz.bus.mapper.feature.populate;

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
import org.miaixz.bus.mapper.handler.ScopedProviderHandler;

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
 * @since Java 21+
 */
public class PopulateHandler<T> extends ScopedProviderHandler<T, PopulateConfig, PopulateProvider> {

    /**
     * Default constructor (uses default configuration).
     */
    public PopulateHandler() {
        super();
    }

    /**
     * Constructor with file configuration.
     *
     * @param config the data fill configuration from file
     */
    public PopulateHandler(PopulateConfig config) {
        super(config);
    }

    /**
     * Returns the property scope key used to resolve populate configuration.
     *
     * @return the property scope key
     */
    @Override
    protected String scope() {
        return Args.POPULATE_KEY;
    }

    /**
     * Captures the current thread-local populate configuration override.
     *
     * @return the captured configuration, or {@code null} when no override is active
     */
    @Override
    protected PopulateConfig capture() {
        Context.MapperConfig contextConfig = Context.getMapperConfig();
        return contextConfig != null ? contextConfig.getPopulate() : null;
    }

    /**
     * Returns the populate provider contract.
     *
     * @return the populate provider contract type
     */
    @Override
    protected Class<PopulateProvider> type() {
        return PopulateProvider.class;
    }

    /**
     * Returns whether populate configuration requires a populate provider.
     *
     * @return {@code true}
     */
    @Override
    protected boolean requiresProvider() {
        return true;
    }

    /**
     * Resolves populate configuration from properties for a specific datasource.
     *
     * @param datasourceKey the datasource key
     * @param properties    the properties
     * @param provider      the populate provider
     * @return the populate configuration
     */
    @Override
    protected PopulateConfig resolve(String datasourceKey, Properties properties, PopulateProvider provider) {
        if (provider == null) {
            return null;
        }
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

    /**
     * Returns the execution order for the populate handler in the mapper interceptor chain.
     *
     * @return the handler order value
     */
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
            Logger.debug(true, "Mapper", "Populate config not found, skipping: {}", ms.getId());
            return true;
        }

        // Skip if parameter is null
        if (parameter == null) {
            Logger.debug(true, "Mapper", "Parameter is null, skipping: {}", ms.getId());
            return true;
        }

        // Get SQL command type
        SqlCommandType commandType = ms.getSqlCommandType();

        // Create builder for current config and fill data
        PopulateBuilder builder = new PopulateBuilder(config);

        // Fill data based on command type
        if (commandType == SqlCommandType.INSERT) {
            Logger.debug(false, "Mapper", "Filling INSERT data for: {}", ms.getId());
            builder.fillInsertData(parameter);
        } else if (commandType == SqlCommandType.UPDATE) {
            Logger.debug(false, "Mapper", "Filling UPDATE data for: {}", ms.getId());
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
