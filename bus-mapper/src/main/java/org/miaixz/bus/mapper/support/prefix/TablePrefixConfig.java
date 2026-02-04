/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.support.prefix;

import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Table prefix configuration class.
 *
 * <p>
 * Configuration for automatic table prefix functionality. Supports dynamic prefix application based on execution
 * context and customizable table ignore lists.
 * </p>
 *
 * <p>
 * Usage examples:
 * </p>
 *
 * <pre>{@code
 *
 * // Method 1: Quick setup with provider only
 * TablePrefixConfig config = TablePrefixConfig.of(() -> "dev_");
 *
 * // Method 2: Use default (prefix disabled)
 * TablePrefixConfig config = TablePrefixConfig.ofDefault();
 *
 * // Method 3: Full configuration
 * TablePrefixConfig config = TablePrefixConfig.builder().provider(() -> "prod_")
 *         .ignore(Arrays.asList("tenant", "sys_config")).build();
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class TablePrefixConfig {

    /**
     * Prefix provider for dynamic prefix retrieval.
     */
    private final TablePrefixProvider provider;

    /**
     * List of table names that should ignore prefix application.
     */
    @Builder.Default
    private final List<String> ignore = Collections.emptyList();

    /**
     * Create a TablePrefixConfig with provider and default settings.
     *
     * <p>
     * This is a convenient factory method for quick setup. It creates a configuration with:
     * </p>
     * <ul>
     * <li>Prefix enabled</li>
     * <li>Custom prefix provider</li>
     * <li>Empty ignore table list</li>
     * </ul>
     *
     * @param provider the prefix provider
     * @return a TablePrefixConfig instance
     */
    public static TablePrefixConfig of(TablePrefixProvider provider) {
        return builder().provider(provider).build();
    }

}
