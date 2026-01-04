/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.mapper;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Normal;

/**
 * An interface that defines sorting order and priority to control execution sequence.
 *
 * <p>
 * This interface has been enhanced to provide both traditional sorting direction constants and comprehensive order
 * management capabilities. It serves as a unified API for all sorting-related operations in bus-mapper.
 * </p>
 *
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Traditional sorting direction constants (backward compatibility)</li>
 * <li>Factory methods for creating sort rules</li>
 * <li>Chainable operations for complex sorting</li>
 * <li>Type-safe direction and property access</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 *
 * // Traditional usage (unchanged)
 * String direction = Order.ASC; // "ASC"
 *
 * // New unified API
 * Order order = Order.descending("name");
 * String property = order.getProperty();
 * EnumValue.Sort dir = order.getDirection();
 *
 * // Chain operations
 * Order combined = Order.ascending("age").withProperty("name").withDirection(EnumValue.Sort.DESC);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Order extends org.miaixz.bus.core.Order {

    /**
     * Represents ascending order.
     */
    String ASC = EnumValue.Sort.ASC.getCode();

    /**
     * Represents descending order.
     */
    String DESC = EnumValue.Sort.DESC.getCode();

    /**
     * Creates an ascending order for the specified property.
     *
     * @param property the property to sort by
     * @return an ascending order instance
     */
    static Order ascending(String property) {
        return new SimpleOrder(EnumValue.Sort.ASC, property);
    }

    /**
     * Creates a descending order for the specified property.
     *
     * @param property the property to sort by
     * @return a descending order instance
     */
    static Order descending(String property) {
        return new SimpleOrder(EnumValue.Sort.DESC, property);
    }

    /**
     * Creates an order with the specified direction and property.
     *
     * @param direction the sort direction
     * @param property  the property to sort by
     * @return an order instance
     */
    static Order of(String property, EnumValue.Sort direction) {
        return new SimpleOrder(direction, property);
    }

    /**
     * Creates an order with the specified direction and property using direction code.
     *
     * @param property      the property to sort by
     * @param directionCode the direction code ("ASC" or "DESC")
     * @return an order instance
     * @throws IllegalArgumentException if directionCode is invalid
     */
    static Order of(String property, String directionCode) {
        if (!propertyIsValid(property)) {
            throw new IllegalArgumentException("Property cannot be null or empty");
        }

        EnumValue.Sort direction = "DESC".equalsIgnoreCase(directionCode) ? EnumValue.Sort.DESC : EnumValue.Sort.ASC;
        return new SimpleOrder(direction, property);
    }

    /**
     * Creates a direction-only order (for backward compatibility).
     *
     * @param directionCode the direction code ("ASC" or "DESC")
     * @return an order instance with empty property
     */
    static Order directionOnly(String directionCode) {
        return of("", directionCode);
    }

    /**
     * Validates the property name.
     *
     * @param property the property to validate
     * @return true if valid, false otherwise
     */
    private static boolean propertyIsValid(String property) {
        return property != null && !property.trim().isEmpty();
    }

    /**
     * Gets the sort direction.
     *
     * @return the sort direction, or null if not applicable
     */
    default EnumValue.Sort getDirection() {
        return null;
    }

    /**
     * Gets the property name.
     *
     * @return the property name, or empty string if not applicable
     */
    default String getProperty() {
        return Normal.EMPTY;
    }

    /**
     * Checks if the order is ascending.
     *
     * @return true if ascending, false otherwise
     */
    default boolean isAscending() {
        return EnumValue.Sort.ASC.equals(getDirection());
    }

    /**
     * Checks if the order is descending.
     *
     * @return true if descending, false otherwise
     */
    default boolean isDescending() {
        return EnumValue.Sort.DESC.equals(getDirection());
    }

    /**
     * Creates a new order with the specified direction.
     *
     * @param direction the new direction
     * @return a new order with the specified direction
     */
    default org.miaixz.bus.core.Order withDirection(EnumValue.Sort direction) {
        return new SimpleOrder(direction, getProperty());
    }

    /**
     * Creates a new order with the specified property.
     *
     * @param property the new property
     * @return a new order with the specified property
     */
    default org.miaixz.bus.core.Order withProperty(String property) {
        return new SimpleOrder(getDirection(), property);
    }

    /**
     * Checks if this is a property-based order (has a non-empty property).
     *
     * @return true if this order has a property, false otherwise
     */
    default boolean hasProperty() {
        return getProperty() != null && !getProperty().trim().isEmpty();
    }

    /**
     * Gets the direction code string.
     *
     * @return the direction code ("ASC" or "DESC")
     */
    default String getDirectionCode() {
        EnumValue.Sort direction = getDirection();
        return direction != null ? direction.getCode() : ASC;
    }

    /**
     * Simple implementation of the Order interface that provides complete sorting capabilities.
     */
    class SimpleOrder implements Order, Serializable {

        @Serial
        private static final long serialVersionUID = 2852292629090L;

        private final EnumValue.Sort direction;
        private final String property;

        /**
         * Creates a SimpleOrder with the specified direction and property.
         *
         * @param direction the sort direction
         * @param property  the property to sort by
         */
        public SimpleOrder(EnumValue.Sort direction, String property) {
            this.direction = Objects.requireNonNull(direction, "Direction cannot be null");
            this.property = property != null ? property : "";
        }

        /**
         * {@inheritDoc}
         *
         * @return the sort direction
         */
        @Override
        public EnumValue.Sort getDirection() {
            return direction;
        }

        /**
         * {@inheritDoc}
         *
         * @return the property name
         */
        @Override
        public String getProperty() {
            return property;
        }

        /**
         * {@inheritDoc}
         *
         * @return true if ascending, false otherwise
         */
        @Override
        public boolean isAscending() {
            return EnumValue.Sort.ASC.equals(direction);
        }

        /**
         * {@inheritDoc}
         *
         * @return true if descending, false otherwise
         */
        @Override
        public boolean isDescending() {
            return EnumValue.Sort.DESC.equals(direction);
        }

        /**
         * {@inheritDoc}
         *
         * @param direction the new direction
         * @return a new order with the specified direction
         */
        @Override
        public org.miaixz.bus.core.Order withDirection(EnumValue.Sort direction) {
            return new SimpleOrder(direction, this.property);
        }

        /**
         * {@inheritDoc}
         *
         * @param property the new property
         * @return a new order with the specified property
         */
        @Override
        public org.miaixz.bus.core.Order withProperty(String property) {
            return new SimpleOrder(this.direction, property);
        }

        /**
         * {@inheritDoc}
         *
         * @return true if this order has a property, false otherwise
         */
        @Override
        public boolean hasProperty() {
            return property != null && !property.trim().isEmpty();
        }

        /**
         * {@inheritDoc}
         *
         * @return the direction code
         */
        @Override
        public String getDirectionCode() {
            return direction.getCode();
        }

        /**
         * {@inheritDoc}
         *
         * @return the order value (0 for default)
         */
        @Override
        public int order() {
            return 0;
        }

        /**
         * {@inheritDoc}
         *
         * @param obj the object to compare
         * @return true if equal, false otherwise
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof SimpleOrder))
                return false;

            SimpleOrder other = (SimpleOrder) obj;
            return direction == other.direction && Objects.equals(property, other.property);
        }

        /**
         * {@inheritDoc}
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            return Objects.hash(direction, property);
        }

        /**
         * {@inheritDoc}
         *
         * @return the string representation
         */
        @Override
        public String toString() {
            if (property.isEmpty()) {
                return direction.getCode();
            }
            return property + ": " + direction;
        }
    }

}
