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
package org.miaixz.bus.mapper.criteria;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.binding.function.Fn;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * An OR query criteria class that extends {@link Criteria} to build SQL query conditions connected by OR.
 *
 * @param <T> The type of the entity class.
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
public class OrCriteria<T> extends Criteria<T> {

    /**
     * Default constructor, initializes the OR criteria.
     */
    public OrCriteria() {
        super();
    }

    /**
     * Adds an "IS NULL" condition with an OR operator.
     *
     * @param fn The method reference to the field.
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andIsNull(Fn<T, Object> fn) {
        super.andIsNull(fn);
        return this;
    }

    /**
     * Adds an "IS NOT NULL" condition with an OR operator.
     *
     * @param fn The method reference to the field.
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andIsNotNull(Fn<T, Object> fn) {
        super.andIsNotNull(fn);
        return this;
    }

    /**
     * Adds an "equal to" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andEqualTo(Fn<T, Object> fn, Object value) {
        super.andEqualTo(fn, value);
        return this;
    }

    /**
     * Adds a "not equal to" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andNotEqualTo(Fn<T, Object> fn, Object value) {
        super.andNotEqualTo(fn, value);
        return this;
    }

    /**
     * Adds a "greater than" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andGreaterThan(Fn<T, Object> fn, Object value) {
        super.andGreaterThan(fn, value);
        return this;
    }

    /**
     * Adds a "greater than or equal to" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andGreaterThanOrEqualTo(Fn<T, Object> fn, Object value) {
        super.andGreaterThanOrEqualTo(fn, value);
        return this;
    }

    /**
     * Adds a "less than" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andLessThan(Fn<T, Object> fn, Object value) {
        super.andLessThan(fn, value);
        return this;
    }

    /**
     * Adds a "less than or equal to" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andLessThanOrEqualTo(Fn<T, Object> fn, Object value) {
        super.andLessThanOrEqualTo(fn, value);
        return this;
    }

    /**
     * Adds an "IN" condition with an OR operator.
     *
     * @param fn     The method reference to the field.
     * @param values A collection of values.
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andIn(Fn<T, Object> fn, Iterable values) {
        super.andIn(fn, values);
        return this;
    }

    /**
     * Adds a "NOT IN" condition with an OR operator.
     *
     * @param fn     The method reference to the field.
     * @param values A collection of values.
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andNotIn(Fn<T, Object> fn, Iterable values) {
        super.andNotIn(fn, values);
        return this;
    }

    /**
     * Adds a "BETWEEN" condition with an OR operator.
     *
     * @param fn     The method reference to the field.
     * @param value1 The starting value of the range.
     * @param value2 The ending value of the range.
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andBetween(Fn<T, Object> fn, Object value1, Object value2) {
        super.andBetween(fn, value1, value2);
        return this;
    }

    /**
     * Adds a "NOT BETWEEN" condition with an OR operator.
     *
     * @param fn     The method reference to the field.
     * @param value1 The starting value of the range.
     * @param value2 The ending value of the range.
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andNotBetween(Fn<T, Object> fn, Object value1, Object value2) {
        super.andNotBetween(fn, value1, value2);
        return this;
    }

    /**
     * Adds a "LIKE" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value to match.
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andLike(Fn<T, Object> fn, Object value) {
        super.andLike(fn, value);
        return this;
    }

    /**
     * Adds a "NOT LIKE" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value to match.
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andNotLike(Fn<T, Object> fn, Object value) {
        super.andNotLike(fn, value);
        return this;
    }

    /**
     * Adds a custom condition with an OR operator.
     *
     * @param condition The custom condition, e.g., "length(name) &lt; 5".
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andCondition(String condition) {
        super.andCondition(condition);
        return this;
    }

    /**
     * Adds a custom condition with a value and an OR operator.
     *
     * @param condition The custom condition, e.g., "length(name)=".
     * @param value     The value, e.g., 5.
     * @return This {@code OrCriteria} object for chaining.
     */
    @Override
    public OrCriteria<T> andCondition(String condition, Object value) {
        super.andCondition(condition, value);
        return this;
    }

    /**
     * Conditionally adds an "IS NULL" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> isNull(boolean useCondition, Fn<T, Object> fn) {
        return useCondition ? isNull(fn) : this;
    }

    /**
     * Adds an "IS NULL" condition with an OR operator.
     *
     * @param fn The method reference to the field.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> isNull(Fn<T, Object> fn) {
        super.andIsNull(fn);
        return this;
    }

    /**
     * Conditionally adds an "IS NOT NULL" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> isNotNull(boolean useCondition, Fn<T, Object> fn) {
        return useCondition ? isNotNull(fn) : this;
    }

    /**
     * Adds an "IS NOT NULL" condition with an OR operator.
     *
     * @param fn The method reference to the field.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> isNotNull(Fn<T, Object> fn) {
        super.andIsNotNull(fn);
        return this;
    }

    /**
     * Conditionally adds an "equal to" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> eq(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? eq(fn, value) : this;
    }

    /**
     * Adds an "equal to" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> eq(Fn<T, Object> fn, Object value) {
        super.andEqualTo(fn, value);
        return this;
    }

    /**
     * Conditionally adds a "not equal to" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> ne(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? ne(fn, value) : this;
    }

    /**
     * Adds a "not equal to" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> ne(Fn<T, Object> fn, Object value) {
        super.andNotEqualTo(fn, value);
        return this;
    }

    /**
     * Conditionally adds a "greater than" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> gt(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? gt(fn, value) : this;
    }

    /**
     * Adds a "greater than" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> gt(Fn<T, Object> fn, Object value) {
        super.andGreaterThan(fn, value);
        return this;
    }

    /**
     * Conditionally adds a "greater than or equal to" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> ge(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? ge(fn, value) : this;
    }

    /**
     * Adds a "greater than or equal to" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> ge(Fn<T, Object> fn, Object value) {
        super.andGreaterThanOrEqualTo(fn, value);
        return this;
    }

    /**
     * Conditionally adds a "less than" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> lt(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? lt(fn, value) : this;
    }

    /**
     * Adds a "less than" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> lt(Fn<T, Object> fn, Object value) {
        super.andLessThan(fn, value);
        return this;
    }

    /**
     * Conditionally adds a "less than or equal to" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> le(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? le(fn, value) : this;
    }

    /**
     * Adds a "less than or equal to" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> le(Fn<T, Object> fn, Object value) {
        super.andLessThanOrEqualTo(fn, value);
        return this;
    }

    /**
     * Conditionally adds an "IN" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param values       A collection of values.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> in(boolean useCondition, Fn<T, Object> fn, Iterable values) {
        return useCondition ? in(fn, values) : this;
    }

    /**
     * Adds an "IN" condition with an OR operator.
     *
     * @param fn     The method reference to the field.
     * @param values A collection of values.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> in(Fn<T, Object> fn, Iterable values) {
        super.andIn(fn, values);
        return this;
    }

    /**
     * Conditionally adds a "NOT IN" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param values       A collection of values.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> notIn(boolean useCondition, Fn<T, Object> fn, Iterable values) {
        return useCondition ? notIn(fn, values) : this;
    }

    /**
     * Adds a "NOT IN" condition with an OR operator.
     *
     * @param fn     The method reference to the field.
     * @param values A collection of values.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> notIn(Fn<T, Object> fn, Iterable values) {
        super.andNotIn(fn, values);
        return this;
    }

    /**
     * Conditionally adds a "BETWEEN" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value1       The starting value of the range.
     * @param value2       The ending value of the range.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> between(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
        return useCondition ? between(fn, value1, value2) : this;
    }

    /**
     * Adds a "BETWEEN" condition with an OR operator.
     *
     * @param fn     The method reference to the field.
     * @param value1 The starting value of the range.
     * @param value2 The ending value of the range.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> between(Fn<T, Object> fn, Object value1, Object value2) {
        super.andBetween(fn, value1, value2);
        return this;
    }

    /**
     * Conditionally adds a "NOT BETWEEN" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value1       The starting value of the range.
     * @param value2       The ending value of the range.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> notBetween(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
        return useCondition ? notBetween(fn, value1, value2) : this;
    }

    /**
     * Adds a "NOT BETWEEN" condition with an OR operator.
     *
     * @param fn     The method reference to the field.
     * @param value1 The starting value of the range.
     * @param value2 The ending value of the range.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> notBetween(Fn<T, Object> fn, Object value1, Object value2) {
        super.andNotBetween(fn, value1, value2);
        return this;
    }

    /**
     * Conditionally adds a "LIKE '%value%'" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value to search for (will be wrapped with '%').
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> contains(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? contains(fn, value) : this;
    }

    /**
     * Adds a "LIKE '%value%'" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value to search for (will be wrapped with '%').
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> contains(Fn<T, Object> fn, String value) {
        super.andLike(fn, Symbol.PERCENT + value + Symbol.PERCENT);
        return this;
    }

    /**
     * Conditionally adds a "LIKE 'value%'" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The prefix to search for (will have '%' appended).
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> startsWith(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? startsWith(fn, value) : this;
    }

    /**
     * Adds a "LIKE 'value%'" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The prefix to search for (will have '%' appended).
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> startsWith(Fn<T, Object> fn, String value) {
        super.andLike(fn, value + Symbol.PERCENT);
        return this;
    }

    /**
     * Conditionally adds a "LIKE '%value'" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The suffix to search for (will have '%' prepended).
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> endsWith(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? endsWith(fn, value) : this;
    }

    /**
     * Adds a "LIKE '%value'" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The suffix to search for (will have '%' prepended).
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> endsWith(Fn<T, Object> fn, String value) {
        super.andLike(fn, Symbol.PERCENT + value);
        return this;
    }

    /**
     * Conditionally adds a "LIKE" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value, which should contain wildcards ('%' or '_').
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> like(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? like(fn, value) : this;
    }

    /**
     * Adds a "LIKE" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value, which should contain wildcards ('%' or '_').
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> like(Fn<T, Object> fn, String value) {
        super.andLike(fn, value);
        return this;
    }

    /**
     * Conditionally adds a "NOT LIKE" condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value, which should contain wildcards ('%').
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> notLike(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? notLike(fn, value) : this;
    }

    /**
     * Adds a "NOT LIKE" condition with an OR operator.
     *
     * @param fn    The method reference to the field.
     * @param value The value, which should contain wildcards ('%').
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> notLike(Fn<T, Object> fn, String value) {
        super.andNotLike(fn, value);
        return this;
    }

    /**
     * Conditionally adds an arbitrary condition with an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param condition    The custom condition, e.g., "length(name) &lt; 5".
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> anyCondition(boolean useCondition, String condition) {
        return useCondition ? anyCondition(condition) : this;
    }

    /**
     * Adds an arbitrary condition with an OR operator.
     *
     * @param condition The custom condition, e.g., "length(name) &lt; 5".
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> anyCondition(String condition) {
        super.andCondition(condition);
        return this;
    }

    /**
     * Conditionally adds a custom condition with a value and an OR operator.
     *
     * @param useCondition Whether to apply the condition.
     * @param condition    The custom condition, e.g., "length(name)=".
     * @param value        The value, e.g., 5.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> anyCondition(boolean useCondition, String condition, Object value) {
        return useCondition ? anyCondition(condition, value) : this;
    }

    /**
     * Adds a custom condition with a value and an OR operator.
     *
     * @param condition The custom condition, e.g., "length(name)=".
     * @param value     The value, e.g., 5.
     * @return This {@code OrCriteria} object for chaining.
     */
    public OrCriteria<T> anyCondition(String condition, Object value) {
        super.andCondition(condition, value);
        return this;
    }

}
