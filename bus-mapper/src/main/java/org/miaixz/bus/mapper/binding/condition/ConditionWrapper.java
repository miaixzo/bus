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
package org.miaixz.bus.mapper.binding.condition;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.binding.BasicMapper;
import org.miaixz.bus.mapper.binding.function.Fn;
import org.miaixz.bus.mapper.criteria.Criteria;
import org.miaixz.bus.mapper.criteria.OrCriteria;

/**
 * Wraps a {@link Condition} object to provide a fluent, chainable API for building complex queries.
 *
 * @param <T> The type of the entity class.
 * @param <I> The type of the primary key.
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConditionWrapper<T, I extends Serializable> {

    /**
     * The underlying mapper instance for executing queries.
     */
    private final BasicMapper<T, I> basicMapper;
    /**
     * The condition object that stores the query criteria.
     */
    private final Condition<T> condition;
    /**
     * The current criteria being built.
     */
    private Criteria<T> current;

    /**
     * Constructs a new ConditionWrapper.
     *
     * @param basicMapper The basic mapper instance.
     * @param condition   The condition object.
     */
    public ConditionWrapper(BasicMapper<T, I> basicMapper, Condition<T> condition) {
        this.basicMapper = basicMapper;
        this.condition = condition;
        this.current = condition.createCriteria();
    }

    /**
     * Adds a new group of OR conditions.
     *
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> or() {
        this.current = this.condition.or();
        return this;
    }

    /**
     * Gets the underlying {@link Condition} object.
     *
     * @return The current condition object.
     */
    public Condition<T> condition() {
        return condition;
    }

    /**
     * Clears all conditions and resets to a new state.
     *
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> clear() {
        this.condition.clear();
        this.current = condition.createCriteria();
        return this;
    }

    /**
     * Specifies the columns to be selected.
     *
     * @param fns An array of method references representing the columns.
     * @return This wrapper instance for chaining.
     */
    @SafeVarargs
    public final ConditionWrapper<T, I> select(Fn<T, Object>... fns) {
        this.condition.selectColumns(fns);
        return this;
    }

    /**
     * Excludes specified columns from the selection.
     *
     * @param fns An array of method references representing the columns to exclude.
     * @return This wrapper instance for chaining.
     */
    @SafeVarargs
    public final ConditionWrapper<T, I> exclude(Fn<T, Object>... fns) {
        this.condition.excludeColumns(fns);
        return this;
    }

    /**
     * Sets a SQL fragment to be prepended to the query.
     *
     * @param startSql The starting SQL fragment.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> startSql(String startSql) {
        this.condition.setStartSql(startSql);
        return this;
    }

    /**
     * Sets a SQL fragment to be appended to the query.
     *
     * @param endSql The ending SQL fragment.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> endSql(String endSql) {
        this.condition.setEndSql(endSql);
        return this;
    }

    /**
     * Adds an ORDER BY clause.
     *
     * @param fn    A method reference to the column.
     * @param order The sort order ("ASC" or "DESC").
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> orderBy(Fn<T, Object> fn, String order) {
        this.condition.orderBy(fn, order);
        return this;
    }

    /**
     * Adds a raw string ORDER BY clause. This does not overwrite existing clauses.
     *
     * @param orderByCondition The sorting expression.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> orderBy(String orderByCondition) {
        this.condition.orderBy(orderByCondition);
        return this;
    }

    /**
     * Adds a dynamically constructed ORDER BY clause.
     *
     * @param orderByCondition A supplier for the sorting expression.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> orderBy(Supplier<String> orderByCondition) {
        this.condition.orderBy(orderByCondition);
        return this;
    }

    /**
     * Conditionally adds a dynamically constructed ORDER BY clause.
     *
     * @param useOrderBy       Whether to apply the ordering.
     * @param orderByCondition A supplier for the sorting expression.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> orderBy(boolean useOrderBy, Supplier<String> orderByCondition) {
        return useOrderBy ? this.orderBy(orderByCondition) : this;
    }

    /**
     * Adds an ascending ORDER BY clause for the specified columns.
     *
     * @param fns An array of method references to the columns.
     * @return This wrapper instance for chaining.
     */
    @SafeVarargs
    public final ConditionWrapper<T, I> orderByAsc(Fn<T, Object>... fns) {
        this.condition.orderByAsc(fns);
        return this;
    }

    /**
     * Adds a descending ORDER BY clause for the specified columns.
     *
     * @param fns An array of method references to the columns.
     * @return This wrapper instance for chaining.
     */
    @SafeVarargs
    public final ConditionWrapper<T, I> orderByDesc(Fn<T, Object>... fns) {
        this.condition.orderByDesc(fns);
        return this;
    }

    /**
     * Enables DISTINCT for the query.
     *
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> distinct() {
        this.condition.setDistinct(true);
        return this;
    }

    /**
     * Conditionally adds a field and value to be updated in a SET clause.
     *
     * @param useSet Whether to apply the set operation.
     * @param setSql The SET clause (e.g., "column = value").
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> set(boolean useSet, String setSql) {
        return useSet ? set(setSql) : this;
    }

    /**
     * Adds a field and value to be updated in a SET clause.
     *
     * @param setSql The SET clause (e.g., "column = value").
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> set(String setSql) {
        this.condition.set(setSql);
        return this;
    }

    /**
     * Conditionally adds a field and value to be updated in a SET clause.
     *
     * @param useSet Whether to apply the set operation.
     * @param fn     A method reference to the field.
     * @param value  The value to set.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> set(boolean useSet, Fn<T, Object> fn, Object value) {
        return useSet ? set(fn, value) : this;
    }

    /**
     * Conditionally adds a field and a dynamically supplied value to be updated in a SET clause.
     *
     * @param useSet   Whether to apply the set operation.
     * @param fn       A method reference to the field.
     * @param supplier A supplier for the value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> set(boolean useSet, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useSet ? set(fn, supplier.get()) : this;
    }

    /**
     * Adds a field and value to be updated in a SET clause.
     *
     * @param fn    A method reference to the field.
     * @param value The value to set.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> set(Fn<T, Object> fn, Object value) {
        this.condition.set(fn, value);
        return this;
    }

    /**
     * Conditionally adds an "IS NULL" condition for a specified field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> isNull(boolean useCondition, Fn<T, Object> fn) {
        return useCondition ? isNull(fn) : this;
    }

    /**
     * Adds an "IS NULL" condition for a specified field.
     *
     * @param fn A method reference to the field.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> isNull(Fn<T, Object> fn) {
        this.current.andIsNull(fn);
        return this;
    }

    /**
     * Conditionally adds an "IS NOT NULL" condition for a specified field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> isNotNull(boolean useCondition, Fn<T, Object> fn) {
        return useCondition ? isNotNull(fn) : this;
    }

    /**
     * Adds an "IS NOT NULL" condition for a specified field.
     *
     * @param fn A method reference to the field.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> isNotNull(Fn<T, Object> fn) {
        this.current.andIsNotNull(fn);
        return this;
    }

    /**
     * Conditionally adds an "equal to" condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param value        The value to compare.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> eq(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? eq(fn, value) : this;
    }

    /**
     * Conditionally adds an "equal to" condition with a dynamically supplied value.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param supplier     A supplier for the value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> eq(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? eq(fn, supplier.get()) : this;
    }

    /**
     * Adds an "equal to" condition.
     *
     * @param fn    A method reference to the field.
     * @param value The value to compare.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> eq(Fn<T, Object> fn, Object value) {
        this.current.andEqualTo(fn, value);
        return this;
    }

    /**
     * Conditionally adds a "not equal to" condition with a dynamically supplied value.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param supplier     A supplier for the value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> ne(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? ne(fn, supplier.get()) : this;
    }

    /**
     * Conditionally adds a "not equal to" condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param value        The value to compare.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> ne(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? ne(fn, value) : this;
    }

    /**
     * Adds a "not equal to" condition.
     *
     * @param fn    A method reference to the field.
     * @param value The value to compare.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> ne(Fn<T, Object> fn, Object value) {
        this.current.andNotEqualTo(fn, value);
        return this;
    }

    /**
     * Conditionally adds a "greater than" condition with a dynamically supplied value.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param supplier     A supplier for the value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> gt(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? gt(fn, supplier.get()) : this;
    }

    /**
     * Conditionally adds a "greater than" condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param value        The value to compare.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> gt(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? gt(fn, value) : this;
    }

    /**
     * Adds a "greater than" condition.
     *
     * @param fn    A method reference to the field.
     * @param value The value to compare.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> gt(Fn<T, Object> fn, Object value) {
        this.current.andGreaterThan(fn, value);
        return this;
    }

    /**
     * Conditionally adds a "greater than or equal to" condition with a dynamically supplied value.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param supplier     A supplier for the value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> ge(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? ge(fn, supplier.get()) : this;
    }

    /**
     * Conditionally adds a "greater than or equal to" condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param value        The value to compare.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> ge(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? ge(fn, value) : this;
    }

    /**
     * Adds a "greater than or equal to" condition.
     *
     * @param fn    A method reference to the field.
     * @param value The value to compare.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> ge(Fn<T, Object> fn, Object value) {
        this.current.andGreaterThanOrEqualTo(fn, value);
        return this;
    }

    /**
     * Conditionally adds a "less than" condition with a dynamically supplied value.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param supplier     A supplier for the value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> lt(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? lt(fn, supplier.get()) : this;
    }

    /**
     * Conditionally adds a "less than" condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param value        The value to compare.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> lt(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? lt(fn, value) : this;
    }

    /**
     * Adds a "less than" condition.
     *
     * @param fn    A method reference to the field.
     * @param value The value to compare.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> lt(Fn<T, Object> fn, Object value) {
        this.current.andLessThan(fn, value);
        return this;
    }

    /**
     * Conditionally adds a "less than or equal to" condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param value        The value to compare.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> le(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? le(fn, value) : this;
    }

    /**
     * Conditionally adds a "less than or equal to" condition with a dynamically supplied value.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param supplier     A supplier for the value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> le(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? le(fn, supplier.get()) : this;
    }

    /**
     * Adds a "less than or equal to" condition.
     *
     * @param fn    A method reference to the field.
     * @param value The value to compare.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> le(Fn<T, Object> fn, Object value) {
        this.current.andLessThanOrEqualTo(fn, value);
        return this;
    }

    /**
     * Conditionally adds an "IN" condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param values       A collection of values.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> in(boolean useCondition, Fn<T, Object> fn, Iterable<?> values) {
        return useCondition ? in(fn, values) : this;
    }

    /**
     * Conditionally adds an "IN" condition with a dynamically supplied collection of values.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param supplier     A supplier for the collection of values.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> in(boolean useCondition, Fn<T, Object> fn, Supplier<Iterable<?>> supplier) {
        return useCondition ? in(fn, supplier.get()) : this;
    }

    /**
     * Adds an "IN" condition.
     *
     * @param fn     A method reference to the field.
     * @param values A collection of values.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> in(Fn<T, Object> fn, Iterable<?> values) {
        this.current.andIn(fn, values);
        return this;
    }

    /**
     * Conditionally adds a "NOT IN" condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param values       A collection of values.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> notIn(boolean useCondition, Fn<T, Object> fn, Iterable<?> values) {
        return useCondition ? notIn(fn, values) : this;
    }

    /**
     * Conditionally adds a "NOT IN" condition with a dynamically supplied collection of values.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param supplier     A supplier for the collection of values.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> notIn(boolean useCondition, Fn<T, Object> fn, Supplier<Iterable<?>> supplier) {
        return useCondition ? notIn(fn, supplier.get()) : this;
    }

    /**
     * Adds a "NOT IN" condition.
     *
     * @param fn     A method reference to the field.
     * @param values A collection of values.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> notIn(Fn<T, Object> fn, Iterable<?> values) {
        this.current.andNotIn(fn, values);
        return this;
    }

    /**
     * Conditionally adds a "BETWEEN" condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param value1       The starting value of the range.
     * @param value2       The ending value of the range.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> between(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
        return useCondition ? between(fn, value1, value2) : this;
    }

    /**
     * Conditionally adds a "BETWEEN" condition with dynamically supplied values.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param supplier1    A supplier for the starting value.
     * @param supplier2    A supplier for the ending value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> between(
            boolean useCondition,
            Fn<T, Object> fn,
            Supplier<Object> supplier1,
            Supplier<Object> supplier2) {
        return useCondition ? between(fn, supplier1.get(), supplier2.get()) : this;
    }

    /**
     * Adds a "BETWEEN" condition.
     *
     * @param fn     A method reference to the field.
     * @param value1 The starting value of the range.
     * @param value2 The ending value of the range.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> between(Fn<T, Object> fn, Object value1, Object value2) {
        this.current.andBetween(fn, value1, value2);
        return this;
    }

    /**
     * Conditionally adds a "NOT BETWEEN" condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param value1       The starting value of the range.
     * @param value2       The ending value of the range.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> notBetween(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
        return useCondition ? notBetween(fn, value1, value2) : this;
    }

    /**
     * Conditionally adds a "NOT BETWEEN" condition with dynamically supplied values.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param supplier1    A supplier for the starting value.
     * @param supplier2    A supplier for the ending value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> notBetween(
            boolean useCondition,
            Fn<T, Object> fn,
            Supplier<Object> supplier1,
            Supplier<Object> supplier2) {
        return useCondition ? notBetween(fn, supplier1.get(), supplier2.get()) : this;
    }

    /**
     * Adds a "NOT BETWEEN" condition.
     *
     * @param fn     A method reference to the field.
     * @param value1 The starting value of the range.
     * @param value2 The ending value of the range.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> notBetween(Fn<T, Object> fn, Object value1, Object value2) {
        this.current.andNotBetween(fn, value1, value2);
        return this;
    }

    /**
     * Conditionally adds a "LIKE '%value%'" condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param value        The value to search for (will be wrapped with '%').
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> contains(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? contains(fn, value) : this;
    }

    /**
     * Conditionally adds a "LIKE '%value%'" condition with a dynamically supplied value.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param supplier     A supplier for the value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> contains(boolean useCondition, Fn<T, Object> fn, Supplier<String> supplier) {
        return useCondition ? contains(fn, supplier.get()) : this;
    }

    /**
     * Adds a "LIKE '%value%'" condition.
     *
     * @param fn    A method reference to the field.
     * @param value The value to search for (will be wrapped with '%').
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> contains(Fn<T, Object> fn, String value) {
        this.current.addCriterion(fn.toColumn() + " LIKE", Symbol.PERCENT + value + Symbol.PERCENT);
        return this;
    }

    /**
     * Conditionally adds a "LIKE 'value%'" condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param value        The prefix to search for (will have '%' appended).
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> startsWith(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? startsWith(fn, value) : this;
    }

    /**
     * Conditionally adds a "LIKE 'value%'" condition with a dynamically supplied value.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param supplier     A supplier for the value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> startsWith(boolean useCondition, Fn<T, Object> fn, Supplier<String> supplier) {
        return useCondition ? startsWith(fn, supplier.get()) : this;
    }

    /**
     * Adds a "LIKE 'value%'" condition.
     *
     * @param fn    A method reference to the field.
     * @param value The prefix to search for (will have '%' appended).
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> startsWith(Fn<T, Object> fn, String value) {
        this.current.addCriterion(fn.toColumn() + " LIKE", value + Symbol.PERCENT);
        return this;
    }

    /**
     * Conditionally adds a "LIKE '%value'" condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param value        The suffix to search for (will have '%' prepended).
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> endsWith(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? endsWith(fn, value) : this;
    }

    /**
     * Conditionally adds a "LIKE '%value'" condition with a dynamically supplied value.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param supplier     A supplier for the value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> endsWith(boolean useCondition, Fn<T, Object> fn, Supplier<String> supplier) {
        return useCondition ? endsWith(fn, supplier.get()) : this;
    }

    /**
     * Adds a "LIKE '%value'" condition.
     *
     * @param fn    A method reference to the field.
     * @param value The suffix to search for (will have '%' prepended).
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> endsWith(Fn<T, Object> fn, String value) {
        this.current.addCriterion(fn.toColumn() + " LIKE", Symbol.PERCENT + value);
        return this;
    }

    /**
     * Conditionally adds a "LIKE" condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param value        The value, which should contain wildcards ('%' or '_').
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> like(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? like(fn, value) : this;
    }

    /**
     * Conditionally adds a "LIKE" condition with a dynamically supplied value.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param supplier     A supplier for the value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> like(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? like(fn, supplier.get()) : this;
    }

    /**
     * Adds a "LIKE" condition.
     *
     * @param fn    A method reference to the field.
     * @param value The value, which should contain wildcards ('%' or '_').
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> like(Fn<T, Object> fn, Object value) {
        this.current.andLike(fn, value);
        return this;
    }

    /**
     * Conditionally adds a "NOT LIKE" condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param value        The value, which should contain wildcards ('%').
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> notLike(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? notLike(fn, value) : this;
    }

    /**
     * Conditionally adds a "NOT LIKE" condition with a dynamically supplied value.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           A method reference to the field.
     * @param supplier     A supplier for the value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> notLike(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? notLike(fn, supplier.get()) : this;
    }

    /**
     * Adds a "NOT LIKE" condition.
     *
     * @param fn    A method reference to the field.
     * @param value The value, which should contain wildcards ('%').
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> notLike(Fn<T, Object> fn, Object value) {
        this.current.andNotLike(fn, value);
        return this;
    }

    /**
     * Conditionally adds an arbitrary query condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param condition    The custom condition string.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> anyCondition(boolean useCondition, String condition) {
        return useCondition ? anyCondition(condition) : this;
    }

    /**
     * Adds an arbitrary query condition.
     *
     * @param condition The custom condition string.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> anyCondition(String condition) {
        this.current.andCondition(condition);
        return this;
    }

    /**
     * Conditionally adds a custom condition and value.
     *
     * @param useCondition Whether to apply the condition.
     * @param condition    The custom condition string (e.g., "length(column) =").
     * @param value        The value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> anyCondition(boolean useCondition, String condition, Object value) {
        return useCondition ? anyCondition(condition, value) : this;
    }

    /**
     * Conditionally adds a custom condition with a dynamically supplied value.
     *
     * @param useCondition Whether to apply the condition.
     * @param condition    The custom condition string (e.g., "length(column) =").
     * @param supplier     A supplier for the value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> anyCondition(boolean useCondition, String condition, Supplier<Object> supplier) {
        return useCondition ? anyCondition(condition, supplier.get()) : this;
    }

    /**
     * Adds a custom condition and value.
     *
     * @param condition The custom condition string (e.g., "length(column) =").
     * @param value     The value.
     * @return This wrapper instance for chaining.
     */
    public ConditionWrapper<T, I> anyCondition(String condition, Object value) {
        this.current.andCondition(condition, value);
        return this;
    }

    /**
     * Adds a nested OR query, where multiple condition blocks are connected by OR, and conditions within a block are
     * connected by AND.
     *
     * @param orParts An array of functions that define the OR condition blocks.
     * @return This wrapper instance for chaining.
     */
    @SafeVarargs
    public final ConditionWrapper<T, I> or(Function<OrCriteria<T>, OrCriteria<T>>... orParts) {
        if (orParts != null && orParts.length > 0) {
            this.current.andOr(
                    Arrays.stream(orParts).map(orPart -> orPart.apply(condition.orPart()))
                            .collect(Collectors.toList()));
        }
        return this;
    }

    /**
     * Deletes records based on the current conditions.
     *
     * @return The number of affected rows.
     */
    public int delete() {
        return basicMapper.deleteByCondition(condition);
    }

    /**
     * Updates records matching the conditions with the values set via the `set` methods.
     *
     * @return The number of affected rows.
     */
    public int update() {
        Assert.notEmpty(condition.getSetValues(), "Update columns and values must be set using the 'set' method");
        return basicMapper.updateByConditionSetValues(condition);
    }

    /**
     * Updates records matching the conditions with the values from the given entity.
     *
     * @param t The entity object.
     * @return The number of affected rows.
     */
    public int update(T t) {
        return basicMapper.updateByCondition(t, condition);
    }

    /**
     * Updates non-null fields of records matching the conditions with the values from the given entity.
     *
     * @param t The entity object.
     * @return The number of affected rows.
     */
    public int updateSelective(T t) {
        return basicMapper.updateByConditionSelective(t, condition);
    }

    /**
     * Retrieves a list of records matching the conditions.
     *
     * @return A list of entity objects.
     */
    public List<T> list() {
        return basicMapper.selectByCondition(condition);
    }

    /**
     * Retrieves a paginated list of records matching the conditions.
     *
     * @param pageNum  The page number.
     * @param pageSize The number of records per page.
     * @return A list of entity objects for the specified page.
     */
    public List<T> page(int pageNum, int pageSize) {
        return basicMapper.selectByCondition(condition, new RowBounds((pageNum - 1) * pageSize, pageSize));
    }

    /**
     * Retrieves a list of records matching the conditions with an offset and limit.
     *
     * @param offset The starting offset.
     * @param limit  The maximum number of records to retrieve.
     * @return A list of entity objects.
     */
    public List<T> offset(int offset, int limit) {
        return basicMapper.selectByCondition(condition, new RowBounds(offset, limit));
    }

    /**
     * Retrieves a cursor for records matching the conditions.
     *
     * @return A cursor of entity objects.
     */
    public Cursor<T> cursor() {
        return basicMapper.selectCursorByCondition(condition);
    }

    /**
     * Retrieves a stream of records matching the conditions.
     *
     * @return A stream of entity objects.
     */
    public Stream<T> stream() {
        return list().stream();
    }

    /**
     * Retrieves a single record matching the conditions. Throws an exception if multiple records are found.
     *
     * @return An {@link Optional} containing the entity object, or an empty Optional if not found.
     */
    public Optional<T> one() {
        return basicMapper.selectOneByCondition(condition);
    }

    /**
     * Retrieves the first record matching the conditions.
     *
     * @return An {@link Optional} containing the entity object, or an empty Optional if not found.
     */
    public Optional<T> first() {
        List<T> result = basicMapper.selectByCondition(condition, new RowBounds(0, 1));
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    /**
     * Retrieves the top N records matching the conditions.
     *
     * @param n The number of records to retrieve.
     * @return A list of entity objects.
     */
    public List<T> top(int n) {
        return basicMapper.selectByCondition(condition, new RowBounds(0, n));
    }

    /**
     * Counts the total number of records matching the conditions.
     *
     * @return The total number of records.
     */
    public long count() {
        return basicMapper.countByCondition(condition);
    }

}
