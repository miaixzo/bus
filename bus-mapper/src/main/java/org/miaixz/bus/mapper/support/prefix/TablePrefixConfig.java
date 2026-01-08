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
