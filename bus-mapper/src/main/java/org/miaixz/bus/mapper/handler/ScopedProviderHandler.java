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
package org.miaixz.bus.mapper.handler;

import java.util.Properties;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;

/**
 * Base class for Mapper handlers that resolve database-specific provider configuration.
 * <p>
 * This class centralizes provider lookup, global default storage, and configuration selection by effective JDBC data
 * source key. The key is observed through a callback and is never used here to route or retain a data source.
 *
 * @param <T> the handled object type
 * @param <C> the plugin configuration type
 * @param <P> the plugin provider type
 * @author Kimi Liu
 */
public abstract class ScopedProviderHandler<T, C, P> extends ConditionHandler<T, C> {

    /**
     * Default configuration resolved during plugin initialization.
     */
    protected C config;

    /**
     * Optional properties used to apply database-specific activation flags to an explicit default configuration.
     */
    private Properties activationProperties;

    /**
     * Initializes a scoped provider handler without a default configuration.
     */
    public ScopedProviderHandler() {
        // No initialization required.
    }

    /**
     * Initializes a scoped provider handler with a default configuration.
     *
     * @param config default configuration
     */
    public ScopedProviderHandler(C config) {
        this.config = config;
    }

    /**
     * Sets plugin configuration properties and resolves the initial default configuration.
     *
     * @param properties mapper configuration properties
     * @return {@code true} when an initial configuration is resolved
     */
    @Override
    public boolean setProperties(Properties properties) {
        if (properties == null) {
            return false;
        }
        this.properties = properties;
        String datasourceKey = getDatasourceKey();
        P provider = getProvider(properties, type());
        if (!enabled(datasourceKey, properties)) {
            this.config = null;
            return provider != null || hasScopeConfiguration(properties);
        }
        if (provider == null && requiresProvider()) {
            onProviderMissing(datasourceKey);
            return hasScopeConfiguration(properties);
        }
        C resolved = resolve(datasourceKey, properties, provider);
        if (resolved == null) {
            return hasScopeConfiguration(properties);
        }
        this.config = resolved;
        return true;
    }

    /**
     * Applies database-specific activation rules without replacing an explicit default configuration.
     * <p>
     * This is used when a provider supplies the highest-priority configuration object while external properties still
     * need to disable or re-enable the handler for individual data source keys.
     *
     * @param properties flattened mapper properties containing enabled flags
     */
    public void setActivationProperties(Properties properties) {
        this.activationProperties = properties;
    }

    /**
     * Resolves the current configuration while enforcing activation rules attached to an explicit default.
     *
     * @return current handler configuration, or {@code null} when disabled for the effective data source key
     */
    @Override
    protected C current() {
        if (activationProperties != null && !enabled(getDatasourceKey(), activationProperties)) {
            return null;
        }
        return super.current();
    }

    /**
     * Returns the default configuration resolved during plugin initialization.
     *
     * @return default configuration, or {@code null}
     */
    @Override
    protected C defaults() {
        return config;
    }

    /**
     * Resolves database-specific configuration from Mapper properties.
     *
     * @param datasourceKey effective JDBC data source key
     * @param properties    mapper configuration properties
     * @return database-specific configuration, or {@code null}
     */
    @Override
    protected C derived(String datasourceKey, Properties properties) {
        P provider = getProvider(properties, type());
        if (provider == null && requiresProvider()) {
            return null;
        }
        return resolve(datasourceKey, properties, provider);
    }

    /**
     * Resolves the activation flag using database-specific, shared, then legacy-default precedence.
     *
     * @param datasourceKey effective JDBC data source key
     * @param properties    flattened mapper configuration
     * @return {@code true} when this handler is enabled for the specified key
     */
    protected boolean enabled(String datasourceKey, Properties properties) {
        if (properties == null) {
            return true;
        }
        String key = datasourceKey == null || datasourceKey.isBlank() ? getDatasourceKey() : datasourceKey;
        String suffix = Symbol.DOT + scope() + Symbol.DOT + Args.PROP_ENABLED;
        String value = properties.getProperty(key + suffix);
        if (value == null) {
            value = properties.getProperty(Args.SHARED_KEY + suffix);
        }
        if (value == null) {
            value = properties.getProperty("default" + suffix);
        }
        return value == null || Boolean.parseBoolean(value);
    }

    /**
     * Returns whether any flattened property belongs to this handler scope.
     *
     * @param properties flattened mapper configuration
     * @return {@code true} when at least one scoped property exists
     */
    protected boolean hasScopeConfiguration(Properties properties) {
        if (properties == null) {
            return false;
        }
        String marker = Symbol.DOT + scope() + Symbol.DOT;
        return properties.stringPropertyNames().stream().anyMatch(key -> key.contains(marker));
    }

    /**
     * Returns the configured default configuration.
     *
     * @return default configuration, or {@code null}
     */
    public C getConfig() {
        return config;
    }

    /**
     * Returns the provider contract used by this plugin.
     *
     * @return provider contract type
     */
    protected abstract Class<P> type();

    /**
     * Resolves the effective plugin configuration.
     *
     * @param datasourceKey effective JDBC data source key
     * @param properties    mapper configuration properties
     * @param provider      provider instance, or {@code null} when optional
     * @return resolved configuration, or {@code null} when unavailable
     */
    protected abstract C resolve(String datasourceKey, Properties properties, P provider);

    /**
     * Returns whether this plugin requires a provider to be configured.
     *
     * @return {@code true} when a missing provider disables the plugin
     */
    protected boolean requiresProvider() {
        return false;
    }

    /**
     * Reports a missing required provider using the handler's configured failure policy.
     *
     * @param datasourceKey effective JDBC data source key
     */
    protected void onProviderMissing(String datasourceKey) {
        Logger.warn(
                false,
                "Mapper",
                "Provider not found, feature disabled: provider={}, datasource={}",
                type().getName(),
                datasourceKey);
    }

}
