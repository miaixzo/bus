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
package org.miaixz.bus.fabric.runtime;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Filter;
import org.miaixz.bus.fabric.Message;

/**
 * Runtime executor for protocol-neutral message filters.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class FilterChain implements Filter.Chain {

    /**
     * Ordered filters.
     */
    private final List<Filter> filters;

    /**
     * Current filter index.
     */
    private final int index;

    /**
     * Terminal downstream chain.
     */
    private final Filter.Chain terminal;

    /**
     * Creates a chain cursor.
     *
     * @param filters  filters
     * @param index    index
     * @param terminal terminal chain
     */
    private FilterChain(final List<Filter> filters, final int index, final Filter.Chain terminal) {
        this.filters = List.copyOf(filters);
        this.index = index;
        this.terminal = Assert.notNull(terminal, () -> new ValidateException("Terminal filter chain must not be null"));
    }

    /**
     * Applies filters to a message.
     *
     * @param message message
     * @param filters filters
     * @return filtered message
     */
    public static Message apply(final Message message, final Filter... filters) {
        final Message current = require(message);
        final List<Filter> chain = active(filters);
        if (chain == null) {
            return current;
        }
        return new FilterChain(chain, Normal._0, value -> value).proceed(current);
    }

    /**
     * Composes filters into one filter.
     *
     * @param filters filters
     * @return composed filter, or {@code null} when no filter is supplied
     */
    public static Filter compose(final Filter... filters) {
        final List<Filter> chain = active(filters);
        if (chain == null) {
            return null;
        }
        if (chain.size() == Normal._1) {
            return chain.get(Normal._0);
        }
        return (message, terminal) -> new FilterChain(chain, Normal._0, terminal).proceed(message);
    }

    /**
     * Proceeds to the next filter.
     *
     * @param message message
     * @return filtered message
     */
    @Override
    public Message proceed(final Message message) {
        final Message current = Assert.notNull(message, () -> new ValidateException("Message must not be null"));
        if (index >= filters.size()) {
            return require(terminal.proceed(current));
        }
        return require(filters.get(index).apply(current, new FilterChain(filters, index + Normal._1, terminal)));
    }

    /**
     * Collects effective filter slots.
     *
     * @param filters filters
     * @return effective filter list, or null when no filter is supplied
     */
    private static List<Filter> active(final Filter... filters) {
        if (filters == null) {
            return null;
        }
        ArrayList<Filter> values = null;
        for (final Filter filter : filters) {
            if (filter != null) {
                if (values == null) {
                    values = new ArrayList<>();
                }
                values.add(filter);
            }
        }
        return values;
    }

    /**
     * Validates filtered messages.
     *
     * @param message message
     * @return message
     */
    private static Message require(final Message message) {
        return Assert.notNull(message, () -> new ValidateException("Filtered message must not be null"));
    }

}
