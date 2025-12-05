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
package org.miaixz.bus.mapper.support.prefix;

import org.miaixz.bus.mapper.provider.MapperProvider;

/**
 * Table prefix provider interface.
 *
 * <p>
 * This interface extends {@link MapperProvider} to provide table name prefix resolution capabilities for dynamic table
 * naming. Implementations can customize prefix resolution logic and optionally provide configuration via
 * {@link #getConfig()}.
 * </p>
 *
 * <p>
 * The interface provides both prefix resolution and optional configuration support:
 * </p>
 * <ul>
 * <li>Prefix resolution: {@link #getPrefix()}</li>
 * <li>Configuration: {@link #getConfig()} - Optional method to provide prefix configuration</li>
 * </ul>
 *
 * <h2>Configuration Priority</h2>
 * <ol>
 * <li>Provider.getPrefix() - Always used for runtime prefix (highest priority)</li>
 * <li>Provider.getConfig() - Configuration override (if provided)</li>
 * <li>Configuration file (application.yml)</li>
 * <li>Default values</li>
 * </ol>
 *
 * <h2>Common implementation strategies:</h2>
 * <ul>
 * <li>Static prefix for environment separation</li>
 * <li>Dynamic prefix based on datasource</li>
 * <li>Context-based prefix from configuration</li>
 * <li>Tenant-specific table prefixes</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <p>
 * <b>Example 1: Simple static prefix (use configuration file)</b>
 * </p>
 * 
 * <pre>{@code
 * @Component
 * public class SimplePrefixProvider implements TablePrefixProvider {
 *
 *     public String getPrefix() {
 *         return "dev_";
 *     }
 *     // No getConfig() override - configuration from application.yml
 * }
 * }</pre>
 *
 * <p>
 * <b>Example 2: Context-based dynamic prefix</b>
 * </p>
 * 
 * <pre>{@code
 * @Component
 * public class ContextAwarePrefixProvider implements TablePrefixProvider {
 *
 *     public String getPrefix() {
 *         Context context = getContext();
 *
 *         // Read prefix from datasource-specific configuration
 *         String dataSourceKey = DataSourceHolder.getKey();
 *         return context.getProperty(dataSourceKey + ".table.prefix", "");
 *     }
 * }
 * }</pre>
 *
 * <p>
 * <b>Example 3: Environment-based with configuration override</b>
 * </p>
 * 
 * <pre>{@code
 * @Component
 * public class CustomPrefixProvider implements TablePrefixProvider {
 *
 *     public TablePrefixConfig getConfig() {
 *         return TablePrefixConfig.builder().ignoreTables("sys_user", "sys_role").enabled(true).build();
 *     }
 *
 *     public String getPrefix() {
 *         if (isProdEnvironment()) {
 *             return "prod_";
 *         } else if (isTestEnvironment()) {
 *             return "test_";
 *         }
 *         return "dev_";
 *     }
 * }
 * }</pre>
 *
 * @author Kimi Liu
 * @see TablePrefixConfig
 * @see TablePrefixHandler
 * @see MapperProvider
 * @since Java 17+
 */
@FunctionalInterface
public interface TablePrefixProvider extends MapperProvider<TablePrefixConfig> {

    /**
     * Get the table prefix for current context.
     *
     * <p>
     * This method is called at SQL execution time to retrieve the prefix that should be applied to table names. The
     * implementation can return different prefixes based on:
     * </p>
     * <ul>
     * <li>Current data source</li>
     * <li>Runtime environment (dev/test/prod)</li>
     * <li>User context or tenant information</li>
     * <li>Any other runtime condition</li>
     * </ul>
     *
     * <p>
     * Implementation notes:
     * </p>
     * <ul>
     * <li>Return empty string {@code ""} to skip prefix for this execution</li>
     * <li>Never return {@code null} - use empty string instead</li>
     * <li>Avoid complex database queries in this method for performance</li>
     * <li>Consider caching prefix value for efficiency</li>
     * </ul>
     *
     * @return table prefix, may be empty but not null
     */
    String getPrefix();

}
