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
package org.miaixz.bus.mapper.support.tenant;

import org.miaixz.bus.mapper.provider.MapperProvider;

/**
 * Tenant ID provider interface.
 *
 * <p>
 * This interface extends {@link MapperProvider} to provide tenant ID resolution capabilities for multi-tenancy support.
 * Implementations can customize tenant ID resolution logic and optionally provide configuration via
 * {@link #getConfig()}.
 * </p>
 *
 * <p>
 * The interface provides both tenant ID resolution and optional configuration support:
 * </p>
 * <ul>
 * <li>Tenant ID resolution: {@link #getTenantId()}</li>
 * <li>Configuration: {@link #getConfig()} - Optional method to provide tenant configuration</li>
 * </ul>
 *
 * <h2>Configuration Priority</h2>
 * <ol>
 * <li>Provider.getConfig() - Highest priority</li>
 * <li>Configuration file (application.yml)</li>
 * <li>Default values</li>
 * </ol>
 *
 * <h2>Common implementation strategies:</h2>
 * <ul>
 * <li>From ThreadLocal (default via {@link TenantContext})</li>
 * <li>From Spring Security context</li>
 * <li>From HTTP request headers</li>
 * <li>From JWT token</li>
 * <li>From Context properties</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <p>
 * <b>Example 1: Simple lambda (use configuration file)</b>
 * </p>
 * 
 * <pre>{@code
 * @Component
 * public class SimpleTenantProvider implements TenantProvider {
 *
 *     public String getTenantId() {
 *         return SecurityContextHolder.getTenantId();
 *     }
 *     // No getConfig() override - configuration from application.yml
 * }
 * }</pre>
 *
 * <p>
 * <b>Example 2: Context-based dynamic tenant ID</b>
 * </p>
 * 
 * <pre>{@code
 * @Component
 * public class ContextAwareTenantProvider implements TenantProvider {
 *
 *     public String getTenantId() {
 *         Context context = getContext();
 *
 *         // Read tenant strategy from context
 *         String strategy = context.getProperty("tenant.strategy", "fixed");
 *
 *         if ("dynamic".equals(strategy)) {
 *             return resolveDynamicTenant();
 *         } else {
 *             return context.getProperty("tenant.id", "default");
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>
 * <b>Example 3: Full configuration from Provider</b>
 * </p>
 * 
 * <pre>{@code
 * @Component
 * public class CustomTenantProvider implements TenantProvider {
 *
 *     public TenantConfig getConfig() {
 *         return TenantConfig.builder().column("tenant_id").ignoreTables("sys_config", "sys_dict").enabled(true)
 *                 .build();
 *     }
 *
 *     public String getTenantId() {
 *         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 *         return ((CustomUserDetails) auth.getPrincipal()).getTenantId();
 *     }
 * }
 * }</pre>
 *
 * @author Kimi Liu
 * @see TenantConfig
 * @see TenantHandler
 * @see MapperProvider
 * @since Java 17+
 */
@FunctionalInterface
public interface TenantProvider extends MapperProvider<TenantConfig> {

    /**
     * Retrieves the current tenant ID.
     *
     * <p>
     * This method is called during SQL execution to determine which tenant the operation belongs to. The tenant ID will
     * be automatically added to SQL WHERE conditions for data isolation.
     * </p>
     *
     * @return the current tenant ID, or null if not available
     */
    String getTenantId();

}
