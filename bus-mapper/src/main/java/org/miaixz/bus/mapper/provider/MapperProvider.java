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
