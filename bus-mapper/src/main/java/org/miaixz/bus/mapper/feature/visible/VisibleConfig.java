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
package org.miaixz.bus.mapper.feature.visible;

import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Visible control configuration class.
 *
 * <p>
 * Configuration for automatic row visibility filtering based on the current user's data scope.
 * </p>
 *
 * <p>
 * Usage examples:
 * </p>
 *
 * <pre>{@code
 *
 * // Method 1: Quick setup with visibility provider
 * VisibleConfig config = VisibleConfig.of(
 *         (tableName, tableAlias) -> VisibleProvider.column(tableAlias, "user_id") + " = "
 *                 + SecurityContextHolder.getCurrentUserId());
 *
 * // Method 2: No visibility provider
 * VisibleConfig config = VisibleConfig.builder().build();
 *
 * // Method 3: Full configuration
 * VisibleConfig config = VisibleConfig.builder()
 *         .provider((tableName, tableAlias) -> VisibleProvider.column(tableAlias, "user_id") + " = " + userId)
 *         .ignore(Arrays.asList("admin_table", "system_table")).build();
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
public class VisibleConfig {

    /**
     * Visible information provider.
     */
    private final VisibleProvider provider;

    /**
     * List of tables to ignore visibility filtering.
     */
    @Builder.Default
    private final List<String> ignore = Collections.emptyList();

    /**
     * Creates a visible-control configuration.
     *
     * @param provider the visible information provider
     * @param ignore   tables excluded from visible checking
     */
    public VisibleConfig(VisibleProvider provider, List<String> ignore) {
        this.provider = provider;
        this.ignore = ignore;
    }

    /**
     * Create a VisibleConfig with visibility provider and default settings.
     *
     * <p>
     * This is a convenient factory method for quick setup. It creates a configuration with:
     * </p>
     * <ul>
     * <li>Custom visibility provider</li>
     * <li>No ignore tables</li>
     * </ul>
     *
     * @param provider the visibility information provider
     * @return a VisibleConfig instance
     */
    public static VisibleConfig of(VisibleProvider provider) {
        return builder().provider(provider).build();
    }

    /**
     * Check if a table should be ignored from visibility filtering.
     *
     * @param tableName the table name to check
     * @return true if the table should be ignored, false otherwise
     */
    public boolean isIgnore(String tableName) {
        if (ignore == null || tableName == null) {
            return false;
        }
        return ignore.stream().anyMatch(ignored -> ignored.equalsIgnoreCase(tableName));
    }

}
