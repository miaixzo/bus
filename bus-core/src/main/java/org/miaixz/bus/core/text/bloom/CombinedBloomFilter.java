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
package org.miaixz.bus.core.text.bloom;

import java.io.Serial;

/**
 * A {@link BloomFilter} implementation that combines multiple Bloom filters. An element is considered to be in this
 * combined filter only if it is present in <em>all</em> of the underlying filters. Adding an element adds it to
 * <em>all</em> underlying filters.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CombinedBloomFilter implements BloomFilter {

    @Serial
    private static final long serialVersionUID = 2852235531853L;

    /**
     * The array of underlying Bloom filters.
     */
    private final BloomFilter[] filters;

    /**
     * Constructs a new {@code CombinedBloomFilter} with the specified filters.
     *
     * @param filters A list of Bloom filters to combine.
     */
    public CombinedBloomFilter(final BloomFilter... filters) {
        this.filters = filters;
    }

    /**
     * Adds the given string to all underlying filters.
     *
     * @param text The string to add.
     * @return {@code true} if any of the underlying filters changed as a result of this operation.
     */
    @Override
    public boolean add(final String text) {
        boolean changed = false;
        for (final BloomFilter filter : filters) {
            changed |= filter.add(text);
        }
        return changed;
    }

    /**
     * Checks if the given string is potentially contained in the filter. This method returns {@code true} only if the
     * string is present in <em>all</em> underlying filters. False positives are possible, but false negatives are not.
     *
     * @param text The string to check.
     * @return {@code true} if the string is likely present, {@code false} if it is definitely not.
     */
    @Override
    public boolean contains(final String text) {
        for (final BloomFilter filter : filters) {
            if (!filter.contains(text)) {
                return false;
            }
        }
        return true;
    }

}
