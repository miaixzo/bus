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
package org.miaixz.bus.auth.source.protocol.scim;

import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Groups the common RFC 7644 search parameters shared by GET and POST search forms.
 *
 * @param attributes         requested return attribute paths
 * @param excludedAttributes requested excluded attribute paths
 * @param filter             optional parsed filter AST
 * @param sortBy             optional sort attribute path
 * @param sortOrder          optional standard sort direction
 * @param startIndex         optional one-based first result index
 * @param count              optional non-negative requested result count
 * @author Kimi Liu
 */
public record SearchParameters(List<String> attributes, List<String> excludedAttributes, Optional<Filter> filter,
        Optional<String> sortBy, Optional<SortOrder> sortOrder, Optional<Integer> startIndex, Optional<Integer> count) {

    /**
     * Validates, detaches, and freezes all search parameters.
     *
     * @throws IllegalArgumentException if a component, container, or list item is {@code null}
     * @throws ValidateException        if attribute selection, path syntax, or pagination is invalid
     */
    public SearchParameters {
        attributes = paths(attributes, "SCIM search attributes");
        excludedAttributes = paths(excludedAttributes, "SCIM search excludedAttributes");
        if (!attributes.isEmpty() && !excludedAttributes.isEmpty()) {
            throw new ValidateException("SCIM attributes and excludedAttributes must not both be present");
        }
        Assert.notNull(filter, "SCIM search filter container must not be null");
        filter = Optional.ofNullable(filter.getOrNull());
        Assert.notNull(sortBy, "SCIM search sortBy container must not be null");
        final String sortPath = sortBy.getOrNull();
        if (sortPath != null) {
            Filter.AttributePath.parse(sortPath);
        }
        sortBy = Optional.ofNullable(sortPath);
        Assert.notNull(sortOrder, "SCIM search sortOrder container must not be null");
        sortOrder = Optional.ofNullable(sortOrder.getOrNull());
        Assert.notNull(startIndex, "SCIM search startIndex container must not be null");
        final Integer start = startIndex.getOrNull();
        if (start != null && start < 1) {
            throw new ValidateException("SCIM search startIndex must be at least one");
        }
        startIndex = Optional.ofNullable(start);
        Assert.notNull(count, "SCIM search count container must not be null");
        final Integer maximum = count.getOrNull();
        if (maximum != null && maximum < 0) {
            throw new ValidateException("SCIM search count must not be negative");
        }
        count = Optional.ofNullable(maximum);
    }

    /**
     * Validates and freezes one ordered attribute-path list.
     *
     * @param values source attribute paths
     * @param label  safe diagnostic label
     * @return immutable path list
     */
    private static List<String> paths(final List<String> values, final String label) {
        Assert.notNull(values, label + " must not be null");
        values.forEach(value -> Filter.AttributePath.parse(Assert.notBlank(value, label + " path must not be blank")));
        return List.copyOf(values);
    }

    /**
     * Defines the standard RFC 7644 sorting directions.
     *
     * @author Kimi Liu
     */
    public enum SortOrder {

        /**
         * Ascending collation order.
         */
        ASCENDING("ascending"),

        /**
         * Descending collation order.
         */
        DESCENDING("descending");

        /**
         * Canonical lowercase wire value.
         */
        private final String value;

        /**
         * Associates one direction with its wire value.
         *
         * @param value canonical lowercase value
         */
        SortOrder(final String value) {
            this.value = value;
        }

        /**
         * Returns the canonical wire value.
         *
         * @return ascending or descending
         */
        public String value() {
            return value;
        }

    }

}
