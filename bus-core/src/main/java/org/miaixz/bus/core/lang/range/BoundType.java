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
package org.miaixz.bus.core.lang.range;

/**
 * Enumeration representing the type of a boundary in a range. This defines whether a bound is open or closed, and
 * whether it's a lower or upper bound.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum BoundType {

    /**
     * Represents a closed lower bound, equivalent to {@code {x | x >= a}}. The boundary value is included in the range.
     */
    CLOSE_LOWER_BOUND("[", ">=", -2),

    /**
     * Represents an open lower bound, equivalent to {@code {x | x > a}}. The boundary value is not included in the
     * range.
     */
    OPEN_LOWER_BOUND("(", ">", -1),

    /**
     * Represents an open upper bound, equivalent to {@code {x | x < a}}. The boundary value is not included in the
     * range.
     */
    OPEN_UPPER_BOUND(")", "<", 1),

    /**
     * Represents a closed upper bound, equivalent to {@code {x | x <= a}}. The boundary value is included in the range.
     */
    CLOSE_UPPER_BOUND("]", "<=", 2);

    /**
     * The symbol representing the bound type (e.g., '[' for closed lower bound, '(' for open lower bound).
     */
    private final String symbol;

    /**
     * The operator representing the inequality (e.g., ">=" for closed lower bound, ">" for open lower bound).
     */
    private final String operator;

    /**
     * An integer code indicating the type of bound. Negative values for lower bounds, positive for upper bounds. Even
     * values for closed bounds, odd values for open bounds.
     */
    private final int code;

    /**
     * Constructs a {@code BoundType} enum constant.
     *
     * @param symbol   the string symbol for the bound (e.g., "[", "(")
     * @param operator the string operator for the inequality (e.g., ">=", ">")
     * @param code     an integer code representing the bound type
     */
    BoundType(final String symbol, final String operator, final int code) {
        this.symbol = symbol;
        this.operator = operator;
        this.code = code;
    }

    /**
     * Retrieves the symbol associated with this bound type.
     *
     * @return the symbol string
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Retrieves the integer code associated with this bound type.
     *
     * @return the integer code
     */
    public int getCode() {
        return code;
    }

    /**
     * Retrieves the operator string associated with this bound type.
     *
     * @return the operator string
     */
    public String getOperator() {
        return operator;
    }

    /**
     * Checks if this bound type is dislocated with another bound type. Dislocated means one is a lower bound and the
     * other is an upper bound.
     *
     * @param boundType the other bound type to compare with
     * @return {@code true} if the bound types are dislocated, {@code false} otherwise
     */
    public boolean isDislocated(final BoundType boundType) {
        return code * boundType.code < 0;
    }

    /**
     * Checks if this bound type represents a lower bound.
     *
     * @return {@code true} if it is a lower bound, {@code false} otherwise
     */
    public boolean isLowerBound() {
        return code < 0;
    }

    /**
     * Checks if this bound type represents an upper bound.
     *
     * @return {@code true} if it is an upper bound, {@code false} otherwise
     */
    public boolean isUpperBound() {
        return code > 0;
    }

    /**
     * Checks if this bound type represents a closed interval (inclusive).
     *
     * @return {@code true} if it is a closed bound, {@code false} otherwise
     */
    public boolean isClose() {
        return (code & 1) == 0;
    }

    /**
     * Checks if this bound type represents an open interval (exclusive).
     *
     * @return {@code true} if it is an open bound, {@code false} otherwise
     */
    public boolean isOpen() {
        return (code & 1) == 1;
    }

    /**
     * Returns the negated bound type.
     *
     * @return the {@code BoundType} that is the negation of this bound type
     */
    public BoundType negate() {
        if (isLowerBound()) {
            return isOpen() ? CLOSE_UPPER_BOUND : OPEN_UPPER_BOUND;
        }
        return isOpen() ? CLOSE_LOWER_BOUND : OPEN_LOWER_BOUND;
    }

}
