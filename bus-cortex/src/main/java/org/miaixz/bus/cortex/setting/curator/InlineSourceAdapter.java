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

import org.miaixz.bus.cortex.setting.item.Item;

/**
 * Inline resolver that returns the stored content directly.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class InlineSourceAdapter implements ItemSourceAdapter {

    /**
     * Creates an inline setting adapter.
     */
    public InlineSourceAdapter() {

    }

    /**
     * Returns the logical source handled by the inline adapter.
     *
     * @return source
     */
    @Override
    public String source() {
        return "INLINE";
    }

    /**
     * Resolves the effective value by returning the inline content directly.
     *
     * @param entry setting entry to resolve
     * @return inline content or {@code null}
     */
    @Override
    public String resolve(Item entry) {
        return entry == null ? null : entry.getContent();
    }

}
