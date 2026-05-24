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
package org.miaixz.bus.mapper.feature.populate;

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
 * // Method 1: Quick setup with operator provider
 * PopulateConfig config = PopulateConfig.of(() -> SecurityContextHolder.getCurrentUserId());
 *
 * // Method 2: Fill time fields only, without an operator provider
 * PopulateConfig config = PopulateConfig.builder().created(true).modified(true).creator(false).modifier(false).build();
 *
 * // Method 3: Full configuration
 * PopulateConfig config = PopulateConfig.builder().provider(() -> SecurityContextHolder.getCurrentUserId())
 *         .created(true).modified(true).creator(true).modifier(true).build();
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
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
     * Creates a full populate configuration.
     *
     * @param created  whether create time auto-fill is enabled
     * @param modified whether update time auto-fill is enabled
     * @param creator  whether creator auto-fill is enabled
     * @param modifier whether modifier auto-fill is enabled
     * @param provider user information provider
     */
    public PopulateConfig(boolean created, boolean modified, boolean creator, boolean modifier,
            PopulateProvider provider) {
        this.created = created;
        this.modified = modified;
        this.creator = creator;
        this.modifier = modifier;
        this.provider = provider;
    }

    /**
     * Create a PopulateConfig with user provider and default settings.
     *
     * <p>
     * This is a convenient factory method for quick setup. It creates a configuration with:
     * </p>
     * <ul>
     * <li>All auto-fill flags enabled</li>
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
