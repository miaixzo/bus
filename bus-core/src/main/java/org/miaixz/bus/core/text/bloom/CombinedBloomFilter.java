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
