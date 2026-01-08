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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Data fill configuration class.
 *
 * <p>
 * Configuration for automatic data filling functionality. Supports automatic filling of create time, update time,
 * creator, and updater fields.
 * </p>
 *
 * <p>
 * Usage examples:
 * </p>
 *
 * <pre>{@code
 *
 * // Method 1: Quick setup with user provider only
 * PopulateConfig config = PopulateConfig.of(() -> SecurityContextHolder.getCurrentUserId());
 *
 * // Method 2: Use default (no user provider)
 * PopulateConfig config = PopulateConfig.ofDefault();
 *
 * // Method 3: Full configuration
 * PopulateConfig config = PopulateConfig.builder().provider(() -> SecurityContextHolder.getCurrentUserId())
 *         .enableCreateTime(true).enableUpdateTime(true).enableCreatedBy(true).enableUpdatedBy(true).build();
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class PopulateConfig {

    /**
     * Whether to enable create time auto-fill.
     */
    @Builder.Default
    private final boolean created = true;

    /**
     * Whether to enable update time auto-fill.
     */
    @Builder.Default
    private final boolean modified = true;

    /**
     * Whether to enable created by auto-fill.
     */
    @Builder.Default
    private final boolean creator = true;

    /**
     * Whether to enable updated by auto-fill.
     */
    @Builder.Default
    private final boolean modifier = true;

    /**
     * User information provider.
     */
    private final PopulateProvider provider;

    /**
     * Create a PopulateConfig with user provider and default settings.
     *
     * <p>
     * This is a convenient factory method for quick setup. It creates a configuration with:
     * </p>
     * <ul>
     * <li>All features enabled</li>
     * <li>Custom user provider</li>
     * </ul>
     *
     * @param provider the user information provider
     * @return a PopulateConfig instance
     */
    public static PopulateConfig of(PopulateProvider provider) {
        return builder().provider(provider).build();
    }

}
