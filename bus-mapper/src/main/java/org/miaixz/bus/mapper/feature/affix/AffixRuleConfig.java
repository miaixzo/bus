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
package org.miaixz.bus.mapper.feature.affix;

import java.util.Collections;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.lang.Assert;

/**
 * Defines the values and exclusion rules used when rewriting physical table names.
 * <p>
 * Prefix and suffix values are supplied by {@link AffixValueProvider}. Their ignore lists are independent, allowing a
 * table to receive only one side when required.
 *
 * @author Kimi Liu
 */
@Getter
@SuperBuilder
public class AffixRuleConfig {

    /**
     * Provider used to resolve the current prefix and suffix.
     */
    private final AffixValueProvider provider;

    /**
     * Logical table names excluded from prefix handling.
     */
    @Builder.Default
    private final List<String> prefixIgnore = Collections.emptyList();

    /**
     * Logical table names excluded from suffix handling.
     */
    @Builder.Default
    private final List<String> suffixIgnore = Collections.emptyList();

    /**
     * Creates an affix rule configuration that applies one shared ignore list to both sides.
     *
     * @param provider value provider used to resolve prefix and suffix text
     * @param ignore   logical or physical table names excluded from both affix sides
     */
    public AffixRuleConfig(AffixValueProvider provider, List<String> ignore) {
        this.provider = provider;
        this.prefixIgnore = ignore;
        this.suffixIgnore = ignore;
    }

    /**
     * Creates an affix rule configuration with independent ignore lists.
     *
     * @param provider     value provider used to resolve prefix and suffix text
     * @param prefixIgnore logical or physical table names excluded from prefix rewriting
     * @param suffixIgnore logical or physical table names excluded from suffix rewriting
     */
    public AffixRuleConfig(AffixValueProvider provider, List<String> prefixIgnore, List<String> suffixIgnore) {
        this.provider = provider;
        this.prefixIgnore = prefixIgnore;
        this.suffixIgnore = suffixIgnore;
    }

    /**
     * Creates an affix rule configuration using the supplied provider and empty ignore lists.
     *
     * @param provider value provider used to resolve prefix and suffix text
     * @return affix rule configuration
     */
    public static AffixRuleConfig of(AffixValueProvider provider) {
        return builder().provider(Assert.notNull(provider, "AffixValueProvider cannot be null")).build();
    }

}
