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
package org.miaixz.bus.mapper.support.visible;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Visible control configuration class.
 *
 * <p>
 * Configuration for automatic perimeter control functionality. Supports row-level security based on user data
 * perimeter.
 * </p>
 *
 * <p>
 * Usage examples:
 * </p>
 *
 * <pre>{@code
 *
 * // Method 1: Quick setup with perimeter provider
 * VisibleConfig config = VisibleConfig
 *         .of((tableName, tableAlias) -> tableAlias + ".user_id = " + SecurityContextHolder.getCurrentUserId());
 *
 * // Method 2: Use default (no perimeter provider)
 * VisibleConfig config = VisibleConfig.ofDefault();
 *
 * // Method 3: Full configuration
 * VisibleConfig config = VisibleConfig.builder()
 *         .provider((tableName, tableAlias) -> tableAlias + ".user_id = " + userId)
 *         .ignore(Arrays.asList("admin_table", "system_table")).build();
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class VisibleConfig {

    /**
     * Visible information provider.
     */
    private final VisibleProvider provider;

    /**
     * List of tables to ignore perimeter checking.
     */
    @Builder.Default
    private final List<String> ignore = Collections.emptyList();

    /**
     * Create a VisibleConfig with perimeter provider and default settings.
     *
     * <p>
     * This is a convenient factory method for quick setup. It creates a configuration with:
     * </p>
     * <ul>
     * <li>All features enabled</li>
     * <li>Custom perimeter provider</li>
     * <li>No ignore tables</li>
     * </ul>
     *
     * @param provider the perimeter information provider
     * @return a VisibleConfig instance
     */
    public static VisibleConfig of(VisibleProvider provider) {
        return builder().provider(provider).build();
    }

    /**
     * Check if a table should be ignored from perimeter checking.
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
