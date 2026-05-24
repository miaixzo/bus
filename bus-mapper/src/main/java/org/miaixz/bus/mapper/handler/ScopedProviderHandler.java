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

import org.miaixz.bus.logger.Logger;

/**
 * Base class for mapper handlers that resolve datasource-scoped provider configuration.
 * <p>
 * This class keeps provider lookup, default configuration storage, and datasource-derived configuration resolution in
 * one place. Concrete handlers only need to declare their provider contract and implement their own configuration
 * resolution rules.
 *
 * @param <T> the handled object type
 * @param <C> the plugin configuration type
 * @param <P> the plugin provider type
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class ScopedProviderHandler<T, C, P> extends ConditionHandler<T, C> {

    /**
     * Default configuration resolved during plugin initialization.
     */
    protected C config;

    /**
     * Constructs a new ScopedProviderHandler instance.
     */
    public ScopedProviderHandler() {
        // No initialization required.
    }

    /**
     * Constructs a new ScopedProviderHandler instance with a default configuration.
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
        if (provider == null && requiresProvider()) {
            onProviderMissing(datasourceKey);
            return false;
        }
        C resolved = resolve(datasourceKey, properties, provider);
        if (resolved == null) {
            return false;
        }
        this.config = resolved;
        return true;
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
     * Resolves datasource-scoped configuration from mapper properties.
     *
     * @param datasourceKey datasource key
     * @param properties    mapper configuration properties
     * @return datasource-scoped configuration, or {@code null}
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
     * @param datasourceKey datasource key
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
     * Handles a missing required provider.
     *
     * @param datasourceKey datasource key
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
