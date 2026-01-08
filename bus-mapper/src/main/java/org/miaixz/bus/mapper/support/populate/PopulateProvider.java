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

import org.miaixz.bus.core.lang.annotation.Creator;
import org.miaixz.bus.core.lang.annotation.Modifier;
import org.miaixz.bus.mapper.provider.MapperProvider;

/**
 * User information provider interface.
 *
 * <p>
 * This interface extends {@link MapperProvider} to provide user information resolution capabilities for automatic field
 * population. Implementations can customize user information resolution logic and optionally provide configuration via
 * {@link #getConfig()}.
 * </p>
 *
 * <p>
 * The interface provides both user information resolution and optional configuration support:
 * </p>
 * <ul>
 * <li>User information resolution: {@link #getOperator()}</li>
 * <li>Configuration: {@link #getConfig()} - Optional method to provide populate configuration</li>
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
 * <li>From ThreadLocal</li>
 * <li>From Spring Security context</li>
 * <li>From HTTP request session</li>
 * <li>From JWT token</li>
 * <li>From Context properties</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <p>
 * <b>Example 1: Simple implementation (use configuration file)</b>
 * </p>
 * 
 * <pre>{@code
 * @Component
 * public class SimplePopulateProvider implements PopulateProvider {
 *
 *     public Object getCurrentUser() {
 *         return SecurityContextHolder.getCurrentUserId();
 *     }
 *     // No getConfig() override - configuration from application.yml
 * }
 * }</pre>
 *
 * <p>
 * <b>Example 2: Context-based dynamic user resolution</b>
 * </p>
 * 
 * <pre>{@code
 * @Component
 * public class ContextAwarePopulateProvider implements PopulateProvider {
 *
 *     public Object getCurrentUser() {
 *         Context context = getContext();
 *
 *         // Read user ID source from context
 *         String source = context.getProperty("populate.userSource", "security");
 *
 *         if ("security".equals(source)) {
 *             return SecurityContextHolder.getCurrentUserId();
 *         } else if ("session".equals(source)) {
 *             return SessionHolder.getCurrentUserId();
 *         }
 *         return "system";
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
 * public class CustomPopulateProvider implements PopulateProvider {
 *
 *     public PopulateConfig getConfig() {
 *         return PopulateConfig.builder().createdField("create_time").creatorField("creator")
 *                 .modifiedField("update_time").modifierField("modifier").enabled(true).build();
 *     }
 *
 *     public Object getCurrentUser() {
 *         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 *         if (auth != null && auth.getPrincipal() instanceof UserDetails) {
 *             return ((UserDetails) auth.getPrincipal()).getUsername();
 *         }
 *         return "system";
 *     }
 * }
 * }</pre>
 *
 * @author Kimi Liu
 * @see PopulateConfig
 * @see PopulateHandler
 * @see MapperProvider
 * @since Java 17+
 */
@FunctionalInterface
public interface PopulateProvider extends MapperProvider<PopulateConfig> {

    /**
     * Retrieves the current user information.
     *
     * <p>
     * This method is called during SQL execution to determine who is performing the operation. The return value can be
     * any type that matches the field type annotated with {@link Creator} or {@link Modifier}.
     * </p>
     *
     * <p>
     * Common return types:
     * </p>
     * <ul>
     * <li>{@link String} - operator ID as string</li>
     * <li>{@link Long} - operator ID as number</li>
     * <li>{@link Integer} - operator ID as number</li>
     * <li>Custom operator object</li>
     * </ul>
     *
     * <p>
     * Implementation notes:
     * </p>
     * <ul>
     * <li>Return {@code null} to skip field population for this execution</li>
     * <li>Avoid complex database queries in this method for performance</li>
     * <li>Consider caching operator information in ThreadLocal for efficiency</li>
     * </ul>
     *
     * @return the current operator information, or null if not available
     */
    Object getOperator();

}
