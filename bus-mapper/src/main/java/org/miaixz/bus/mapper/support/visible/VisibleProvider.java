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
package org.miaixz.bus.mapper.support.visible;

import org.miaixz.bus.mapper.provider.MapperProvider;

/**
 * Data visibility provider interface.
 *
 * <p>
 * This interface extends {@link MapperProvider} to provide data visibility scope resolution capabilities for row-level
 * access control. Implementations can customize visibility SQL generation logic and optionally provide configuration
 * via {@link #getConfig()}.
 * </p>
 *
 * <p>
 * The interface provides both visibility condition generation and optional configuration support:
 * </p>
 * <ul>
 * <li>Visibility condition: {@link #getVisible(String, String)}</li>
 * <li>Configuration: {@link #getConfig()} - Optional method to provide visible configuration</li>
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
 * <li>User-based: Filter by user ID (e.g., {@code user_id = 123})</li>
 * <li>Department-based: Filter by department ID (e.g., {@code dept_id IN (1,2,3)})</li>
 * <li>Organization-based: Filter by organization ID (e.g., {@code org_id = 5})</li>
 * <li>Role-based: Filter by role visibility (e.g., {@code status IN ('public','shared')})</li>
 * <li>Custom: Any complex visibility logic</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 *
 * <p>
 * <b>Example 1: Simple user-based visibility (use configuration file)</b>
 * </p>
 * 
 * <pre>{@code
 * @Component
 * public class SimpleVisibleProvider implements VisibleProvider {
 *
 *     public String getVisible(String tableName, String tableAlias) {
 *         Long userId = SecurityContextHolder.getCurrentUserId();
 *         return tableAlias + ".user_id = " + userId;
 *     }
 *     // No getConfig() override - configuration from application.yml
 * }
 * }</pre>
 *
 * <p>
 * <b>Example 2: Context-based dynamic visibility</b>
 * </p>
 * 
 * <pre>{@code
 * @Component
 * public class ContextAwareVisibleProvider implements VisibleProvider {
 *
 *     public String getVisible(String tableName, String tableAlias) {
 *         Context context = getContext();
 *
 *         // Read visibility strategy from context
 *         String strategy = context.getProperty("visible.strategy", "user");
 *
 *         if ("department".equals(strategy)) {
 *             List<Long> deptIds = SecurityContextHolder.getCurrentUserDepartments();
 *             return tableAlias + ".dept_id IN (" + StringUtils.join(deptIds, ",") + ")";
 *         } else {
 *             Long userId = SecurityContextHolder.getCurrentUserId();
 *             return tableAlias + ".user_id = " + userId;
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
 * public class CustomVisibleProvider implements VisibleProvider {
 *
 *     public VisibleConfig getConfig() {
 *         return VisibleConfig.builder().ignoreTables("sys_config", "sys_dict").enabled(true).build();
 *     }
 *
 *     public String getVisible(String tableName, String tableAlias) {
 *         User currentUser = SecurityContextHolder.getCurrentUser();
 *         if (currentUser.isAdmin()) {
 *             return null; // Admin can see everything
 *         }
 *         if (currentUser.hasRole("MANAGER")) {
 *             return tableAlias + ".dept_id = " + currentUser.getDeptId();
 *         }
 *         return tableAlias + ".user_id = " + currentUser.getId();
 *     }
 * }
 * }</pre>
 *
 * @author Kimi Liu
 * @see VisibleConfig
 * @see VisibleHandler
 * @see MapperProvider
 * @since Java 21+
 */
@FunctionalInterface
public interface VisibleProvider extends MapperProvider<VisibleConfig> {

    /**
     * Retrieves the visibility SQL condition for the current user.
     *
     * <p>
     * This method is called during SQL execution to generate a WHERE condition that filters query results based on the
     * current user's data visibility scope. The condition will be automatically appended to the original SQL query.
     * </p>
     *
     * <p>
     * Important notes:
     * </p>
     * <ul>
     * <li>Return {@code null} or empty string to skip visibility filtering</li>
     * <li>The condition should NOT include the "WHERE" keyword</li>
     * <li>The condition should use the provided table alias</li>
     * <li>The condition will be wrapped in parentheses automatically</li>
     * <li>Avoid complex database queries in this method for performance</li>
     * </ul>
     *
     * <p>
     * Example return values:
     * </p>
     * <ul>
     * <li>{@code "t.user_id = 123"}</li>
     * <li>{@code "t.dept_id IN (1,2,3)"}</li>
     * <li>{@code "t.org_id = 5 AND t.status = 'active'"}</li>
     * <li>{@code null} - Skip visibility filtering</li>
     * </ul>
     *
     * @param tableName  the name of the table being queried
     * @param tableAlias the alias of the table in the SQL query (never null, defaults to table name if no alias)
     * @return the SQL WHERE condition for visibility filtering, or null to skip filtering
     */
    String getVisible(String tableName, String tableAlias);

}
