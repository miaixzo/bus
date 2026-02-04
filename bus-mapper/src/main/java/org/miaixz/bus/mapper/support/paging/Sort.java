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
package org.miaixz.bus.mapper.support.paging;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import org.miaixz.bus.mapper.Order;

/**
 * Sort option for queries.
 *
 * <p>
 * This class provides sorting capabilities for database queries. It supports multiple sort orders and directions.
 * </p>
 *
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Multiple sort criteria support</li>
 * <li>Ascending and descending order</li>
 * <li>Chaining sort operations</li>
 * <li>Null-safe comparisons</li>
 * <li>Immutable sort instances</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * </p>
 * *
 * 
 * <pre>{@code
 *
 * // Create a single sort order
 * Sort sort = Sort.by("name").ascending();
 *
 * // Create multiple sort orders
 * Sort multiSort = Sort.by("name").ascending().and("age").descending().and("email").ascending();
 *
 * // Create sort with direction
 * Sort descSort = Sort.by(Order.descending("priority"));
 *
 * // Check sort properties
 * boolean sorted = sort.isSorted(); // true
 * boolean unsorted = Sort.unsorted().isSorted(); // false
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class Sort implements Serializable, Iterable<Order> {

    /**
     * Serialization version UID for compatibility.
     */
    @Serial
    private static final long serialVersionUID = 2852292629096L;

    /**
     * The list of ordering criteria.
     * <p>
     * This list is immutable and contains the sequence of {@link Order} objects that define the sorting logic.
     * </p>
     */
    private final List<Order> orders;

    /**
     * Creates an unsorted Sort instance.
     */
    private Sort() {
        this.orders = Collections.emptyList();
    }

    /**
     * Creates a Sort instance with the specified orders.
     *
     * @param orders the sort orders
     */
    private Sort(List<Order> orders) {
        this.orders = Collections.unmodifiableList(orders);
    }

    /**
     * Creates a Sort instance with the specified orders.
     *
     * @param orders the sort orders (can be null or empty, resulting in unsorted)
     * @return a Sort instance
     */
    public static Sort by(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return unsorted();
        }
        return new Sort(new ArrayList<>(orders));
    }

    /**
     * Creates a Sort instance with the specified orders.
     *
     * @param orders the sort orders
     * @return a Sort instance
     */
    @SafeVarargs
    public static Sort by(Order... orders) {
        if (orders == null || orders.length == 0) {
            return unsorted();
        }
        return by(Arrays.asList(orders));
    }

    /**
     * Creates a Sort instance with a single property and direction.
     *
     * @param property the property to sort by
     * @return a SortBuilder to specify direction
     */
    public static SortBuilder by(String property) {
        return new SortBuilder(property);
    }

    /**
     * Creates an unsorted Sort instance.
     *
     * @return an unsorted Sort instance
     */
    public static Sort unsorted() {
        return new Sort();
    }

    /**
     * Checks if this sort is sorted (has at least one order).
     *
     * @return true if sorted, false otherwise
     */
    public boolean isSorted() {
        return !orders.isEmpty();
    }

    /**
     * Gets the sort orders.
     *
     * @return an unmodifiable list of sort orders
     */
    public List<Order> getOrders() {
        return orders;
    }

    /**
     * Creates a new Sort by combining this Sort with the specified Sort.
     * <p>
     * Returns a new instance containing orders from both this instance and the provided argument.
     * </p>
     *
     * @param sort the Sort to combine with
     * @return a new Sort with combined orders
     */
    public Sort and(Sort sort) {
        if (sort == null || !sort.isSorted()) {
            return this;
        }
        if (!this.isSorted()) {
            return sort;
        }

        List<Order> combined = new ArrayList<>(this.orders);
        combined.addAll(sort.orders);
        return new Sort(combined);
    }

    /**
     * Returns an iterator over the sort orders.
     *
     * @return an iterator over the sort orders
     */
    @Override
    public Iterator<Order> iterator() {
        return orders.iterator();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Sort))
            return false;

        Sort sort = (Sort) obj;

        return orders.equals(sort.orders);
    }

    @Override
    public int hashCode() {
        return orders.hashCode();
    }

    @Override
    public String toString() {
        return orders.isEmpty() ? "UNSORTED" : orders.toString();
    }

    /**
     * Builder for creating Sort instances with a fluent API.
     */
    public static class SortBuilder {

        /**
         * The property name to be sorted.
         */
        private final String property;

        private SortBuilder(String property) {
            this.property = property;
        }

        /**
         * Sets the direction to ascending.
         *
         * @return a Sort instance with ascending order for the property
         */
        public Sort ascending() {
            return new Sort(Collections.singletonList(Order.ascending(property)));
        }

        /**
         * Sets the direction to descending.
         *
         * @return a Sort instance with descending order for the property
         */
        public Sort descending() {
            return new Sort(Collections.singletonList(Order.descending(property)));
        }
    }

}
