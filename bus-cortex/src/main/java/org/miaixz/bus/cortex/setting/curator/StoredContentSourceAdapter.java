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

import java.util.Map;

import org.miaixz.bus.cortex.setting.item.Item;

/**
 * Fail-fast adapter used by spec-backed setting sources whose concrete integration is not implemented yet.
 *
 * @author Kimi Liu
 */
@Deprecated(since = "8.8.0", forRemoval = true)
public class StoredContentSourceAdapter implements ItemSourceAdapter {

    /**
     * Logical source served by this adapter.
     */
    private final String source;

    /**
     * Creates an adapter for one spec-backed source.
     *
     * @param source logical source
     */
    public StoredContentSourceAdapter(String source) {
        this.source = source;
    }

    /**
     * Returns the logical source handled by this adapter.
     *
     * @return source
     */
    @Override
    public String source() {
        return source;
    }

    /**
     * Never claims support for an external source without its concrete implementation.
     *
     * @param entry setting entry to validate
     * @return {@code true} when the entry can be resolved without external storage access
     */
    @Override
    public boolean validate(Item entry) {
        return false;
    }

    /**
     * Fails fast rather than presenting inline fallback content as a live external source.
     *
     * @param entry setting entry to resolve
     * @return inline content
     */
    @Override
    public String resolve(Item entry) {
        if (entry == null) {
            return null;
        }
        throw new UnsupportedOperationException("External setting source is not implemented for source: " + source);
    }

    /**
     * Returns capability flags describing this adapter as a fail-fast source implementation.
     *
     * @return adapter capability flags
     */
    @Override
    public Map<String, Object> capabilities() {
        return Map.of(
                "source",
                source,
                "externalRead",
                Boolean.FALSE,
                "inlineFallback",
                Boolean.FALSE,
                "failFast",
                Boolean.TRUE);
    }

}
