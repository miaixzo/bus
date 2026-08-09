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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.mapper.provider.MapperProvider;

/**
 * Supplies prefix and suffix values used to rewrite physical table names in the current execution context.
 *
 * <p>
 * The prefix is the functional operation, allowing prefix-only rules to use a lambda. Implementations that also supply
 * a suffix override {@link #getSuffix()}.
 * </p>
 *
 * @author Kimi Liu
 */
@FunctionalInterface
public interface AffixValueProvider extends MapperProvider<AffixRuleConfig> {

    /**
     * Returns the text prepended to physical table names.
     *
     * @return table prefix, or an empty string when no prefix is required
     */
    String getPrefix();

    /**
     * Returns the text appended to physical table names.
     *
     * @return table suffix, or an empty string when no suffix is required
     */
    default String getSuffix() {
        return Normal.EMPTY;
    }

}
