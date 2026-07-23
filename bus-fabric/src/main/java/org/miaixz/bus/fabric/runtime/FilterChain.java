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
     * Immutable ordered snapshot of active non-null filters.
     */
    private final List<Filter> filters;

    /**
     * Non-null downstream chain invoked after the last filter.
     */
    private final Filter.Chain terminal;

    /**
     * Creates a chain cursor.
     *
     * @param filters  active filters in invocation order
     * @param terminal downstream chain invoked after the final filter
     * @throws ValidateException if {@code terminal} is {@code null}
     */
    private FilterChain(final List<Filter> filters, final Filter.Chain terminal) {
        this.filters = List.copyOf(filters);
        this.terminal = Assert.notNull(terminal, () -> new ValidateException("Terminal filter chain must not be null"));
    }

    /**
     * Applies filters to a message.
     *
     * @param message input message passed through active filters
     * @param filters optional filter slots; {@code null} elements are ignored
     * @return original message when no filter is active, otherwise the final validated output
     * @throws ValidateException if a message is {@code null} or a filter changes protocol/address routing fields
     */
    public static Message apply(final Message message, final Filter... filters) {
        final Message current = require(message);
        final List<Filter> chain = active(filters);
        if (chain == null) {
            return current;
        }
        return new FilterChain(chain, value -> value).proceed(current);
    }

    /**
     * Composes filters into one filter.
     *
     * @param filters optional filter slots; {@code null} elements are ignored
     * @return composed filter, or {@code null} when no filter is supplied
     */
    public static Filter compose(final Filter... filters) {
        final List<Filter> chain = active(filters);
        if (chain == null) {
            return null;
        }
        return (message, terminal) -> new FilterChain(chain, terminal).proceed(message);
    }

    /**
     * Proceeds to the next filter.
     *
     * @param message input message passed from the beginning of this chain
     * @return final message after every filter and the terminal chain
     * @throws ValidateException if a message is {@code null} or a boundary changes protocol/address routing fields
     */
    @Override
    public Message proceed(final Message message) {
        final Message current = Assert.notNull(message, () -> new ValidateException("Message must not be null"));
        return new Cursor(filters, Normal._0, terminal).proceed(current);
    }

    /**
     * Collects effective filter slots.
     *
     * @param filters optional filter slots to compact
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
     * @param message filter input or output to validate
     * @return validated non-null message
     * @throws ValidateException if {@code message} is {@code null}
     */
    private static Message require(final Message message) {
        return Assert.notNull(message, () -> new ValidateException("Filtered message must not be null"));
    }

    /**
     * Validates immutable routing fields across one filter boundary.
     *
     * @param input  boundary input
     * @param output boundary output
     * @return validated output
     */
    private static Message validateBoundary(final Message input, final Message output) {
        final Message current = require(output);
        if (current.protocol() != input.protocol()) {
            throw new ValidateException("Filter must not replace message protocol");
        }
        if (!current.address().equals(input.address())) {
            throw new ValidateException("Filter must not replace message address");
        }
        return current;
    }

    /**
     * Lightweight cursor sharing the entry list and terminal chain.
     */
    private static final class Cursor implements Filter.Chain {

        /**
         * Shared ordered filters.
         */
        private final List<Filter> filters;

        /**
         * Current filter index.
         */
        private final int index;

        /**
         * Shared terminal chain.
         */
        private final Filter.Chain terminal;

        /**
         * Creates a cursor without copying shared state.
         *
         * @param filters  shared filters
         * @param index    current index
         * @param terminal shared terminal
         */
        private Cursor(final List<Filter> filters, final int index, final Filter.Chain terminal) {
            this.filters = filters;
            this.index = index;
            this.terminal = terminal;
        }

        /**
         * Proceeds through one filter boundary.
         *
         * @param message input at the current filter boundary
         * @return validated output from the current filter or terminal chain
         * @throws ValidateException if a boundary returns {@code null} or changes protocol/address routing fields
         */
        @Override
        public Message proceed(final Message message) {
            final Message current = require(message);
            if (index >= filters.size()) {
                return validateBoundary(current, terminal.proceed(current));
            }
            final Cursor next = new Cursor(filters, index + Normal._1, terminal);
            return validateBoundary(current, filters.get(index).apply(current, next));
        }

    }

}
