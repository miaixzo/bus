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
package org.miaixz.bus.cortex.setting.curator;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.setting.item.Item;

/**
 * Environment-variable resolver.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EnvSourceAdapter implements ItemSourceAdapter {

    /**
     * Creates an environment-backed setting adapter.
     */
    public EnvSourceAdapter() {
    }

    /**
     * Returns the logical source handled by the environment adapter.
     *
     * @return source
     */
    @Override
    public String source() {
        return "ENV";
    }

    /**
     * Validates that the entry provides either an environment variable name or a fallback inline value.
     *
     * @param entry setting entry to validate
     * @return {@code true} when the entry contains enough data to resolve an environment-backed value
     */
    @Override
    public boolean validate(Item entry) {
        if (!ItemSourceAdapter.super.validate(entry) || entry == null) {
            return false;
        }
        return StringKit.isNotEmpty(entry.getSpec()) || StringKit.isNotEmpty(entry.getContent());
    }

    /**
     * Resolves the effective value by reading one environment variable and falling back to inline content.
     *
     * @param entry setting entry to resolve
     * @return resolved environment value or fallback inline content
     */
    @Override
    public String resolve(Item entry) {
        if (entry == null) {
            return null;
        }
        String key = StringKit.isNotEmpty(entry.getSpec()) ? entry.getSpec() : entry.getContent();
        if (StringKit.isEmpty(key)) {
            return null;
        }
        String value = System.getenv(key);
        return value != null ? value : entry.getContent();
    }

}
