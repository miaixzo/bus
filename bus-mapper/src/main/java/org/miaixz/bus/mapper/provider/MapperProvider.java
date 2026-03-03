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
package org.miaixz.bus.mapper.provider;

/**
 * Base provider interface for all mapper plugins.
 *
 * <p>
 * This is the top-level interface that all provider implementations should extend. It provides a unified contract for
 * configuration management and context access across different mapper plugins (tenant, audit, populate, visible, etc.).
 *
 * @param <C> the configuration type
 * @author Kimi Liu
 * @since Java 17+
 */
public interface MapperProvider<C> {

    /**
     * Provides plugin configuration (optional).
     *
     * <p>
     * This method allows providers to supply custom configuration programmatically. If not overridden (returns
     * {@code null}), the configuration will be loaded from the context/configuration file or use default values.
     * </p>
     *
     * <p>
     * <b>Configuration Priority:</b>
     * </p>
     * <ol>
     * <li>Values from this method (highest priority)</li>
     * <li>Context properties (from configuration file/environment)</li>
     * <li>Default values (lowest priority)</li>
     * </ol>
     *
     * <p>
     * <b>When to Override:</b>
     * </p>
     * <ul>
     * <li>You need complex configuration logic that can't be expressed in a config file</li>
     * <li>Configuration needs to be computed at runtime based on multiple factors</li>
     * <li>You want to completely override context-based configuration</li>
     * </ul>
     *
     * <p>
     * <b>Note:</b> Returning {@code null} means "use configuration from context/defaults". This is the recommended
     * approach for most use cases. Only override when you have specific requirements for programmatic configuration.
     * </p>
     *
     * @return the plugin configuration, or null to use context/defaults
     */
    default C getConfig() {
        return null;
    }

}
