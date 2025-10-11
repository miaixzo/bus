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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ibatis.type.TypeHandler;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.mapper.binding.function.Fn;
import org.miaixz.bus.mapper.parsing.ColumnMeta;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * A query criteria class for building complex SQL query conditions.
 *
 * @param <T> The type of the entity class.
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
public class Criteria<T> {

    /**
     * A list of all query conditions.
     */
    protected List<Criterion> criteria;

    /**
     * If true, only non-null value conditions are used; otherwise, all conditions are used.
     */
    private boolean useSelective = false;

    /**
     * Default constructor, initializes the criteria list.
     */
    public Criteria() {
        super();
        this.criteria = new ArrayList<>();
    }

    /**
     * Constructor with selective condition option.
     *
     * @param useSelective Whether to enable selective conditions (non-null checks).
     */
    public Criteria(boolean useSelective) {
        super();
        this.criteria = new ArrayList<>();
        this.useSelective = useSelective;
    }

    /**
     * Gets the column name corresponding to a method reference.
     *
     * @param fn The method reference.
     * @return The column name.
     */
    public String column(Fn<T, Object> fn) {
        return fn.toColumn();
    }

    /**
     * Gets the type handler corresponding to a method reference.
     *
     * @param fn The method reference.
     * @return The type handler class.
     */
    public Class<? extends TypeHandler> typehandler(Fn<T, Object> fn) {
        return fn.toEntityColumn().typeHandler();
    }

    /**
     * Determines whether to use a criterion based on the selective setting. If `useSelective` is false, the criterion
     * is always used. If `useSelective` is true, the criterion is used only if the value is not empty.
     *
     * @param obj The value of the criterion.
     * @return {@code true} to use the criterion, {@code false} otherwise.
     */
    public boolean useCriterion(Object obj) {
        return !useSelective || !ObjectKit.isEmpty(obj);
    }

    /**
     * Adds a simple criterion.
     *
     * @param condition The condition expression.
     */
    public void addCriterion(String condition) {
        if (condition == null) {
            throw new RuntimeException("Value for condition cannot be null");
        }
        criteria.add(new Criterion(condition));
    }

    /**
     * Adds a criterion with a single value.
     *
     * @param condition The condition expression.
     * @param value     The value for the condition.
     */
    public void addCriterion(String condition, Object value) {
        if (value == null) {
            throw new RuntimeException("Value for " + condition + " cannot be null");
        }
        criteria.add(new Criterion(condition, value));
    }

    /**
     * Adds a criterion with a single value and column information.
     *
     * @param condition The condition expression.
     * @param value     The value for the condition.
     * @param column    The column metadata.
     */
    public void addCriterion(String condition, Object value, ColumnMeta column) {
        if (value == null) {
            throw new RuntimeException("Value for " + condition + " cannot be null");
        }
        criteria.add(new Criterion(condition, value, column));
    }

    /**
     * Adds a range criterion.
     *
     * @param condition The condition expression.
     * @param value1    The starting value.
     * @param value2    The ending value.
     */
    public void addCriterion(String condition, Object value1, Object value2) {
        if (value1 == null || value2 == null) {
            throw new RuntimeException("Between values for " + condition + " cannot be null");
        }
        criteria.add(new Criterion(condition, value1, value2));
    }

    /**
     * Adds a range criterion with column information.
     *
     * @param condition The condition expression.
     * @param value1    The starting value.
     * @param value2    The ending value.
     * @param column    The column metadata.
     */
    public void addCriterion(String condition, Object value1, Object value2, ColumnMeta column) {
        if (value1 == null || value2 == null) {
            throw new RuntimeException("Between values for " + condition + " cannot be null");
        }
        criteria.add(new Criterion(condition, value1, value2, column));
    }

    /**
     * Conditionally adds an "IS NULL" condition for a field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andIsNull(boolean useCondition, Fn<T, Object> fn) {
        return useCondition ? andIsNull(fn) : this;
    }

    /**
     * Adds an "IS NULL" condition for a field.
     *
     * @param fn The method reference to the field.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andIsNull(Fn<T, Object> fn) {
        addCriterion(column(fn) + " IS NULL");
        return this;
    }

    /**
     * Conditionally adds an "IS NOT NULL" condition for a field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andIsNotNull(boolean useCondition, Fn<T, Object> fn) {
        return useCondition ? andIsNotNull(fn) : this;
    }

    /**
     * Adds an "IS NOT NULL" condition for a field.
     *
     * @param fn The method reference to the field.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andIsNotNull(Fn<T, Object> fn) {
        addCriterion(column(fn) + " IS NOT NULL");
        return this;
    }

    /**
     * Conditionally adds an "equal to" condition for a field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value to compare.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andEqualTo(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andEqualTo(fn, value) : this;
    }

    /**
     * Adds an "equal to" condition for a field.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andEqualTo(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " =", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * Conditionally adds a "not equal to" condition for a field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value to compare.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andNotEqualTo(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andNotEqualTo(fn, value) : this;
    }

    /**
     * Adds a "not equal to" condition for a field.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andNotEqualTo(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " <>", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * Conditionally adds a "greater than" condition for a field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value to compare.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andGreaterThan(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andGreaterThan(fn, value) : this;
    }

    /**
     * Adds a "greater than" condition for a field.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andGreaterThan(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " >", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * Conditionally adds a "greater than or equal to" condition for a field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value to compare.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andGreaterThanOrEqualTo(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andGreaterThanOrEqualTo(fn, value) : this;
    }

    /**
     * Adds a "greater than or equal to" condition for a field.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andGreaterThanOrEqualTo(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " >=", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * Conditionally adds a "less than" condition for a field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value to compare.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andLessThan(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andLessThan(fn, value) : this;
    }

    /**
     * Adds a "less than" condition for a field.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andLessThan(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " <", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * Conditionally adds a "less than or equal to" condition for a field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value to compare.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andLessThanOrEqualTo(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andLessThanOrEqualTo(fn, value) : this;
    }

    /**
     * Adds a "less than or equal to" condition for a field.
     *
     * @param fn    The method reference to the field.
     * @param value The value to compare.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andLessThanOrEqualTo(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " <=", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * Conditionally adds an "IN" condition for a field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param values       A collection of values.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andIn(boolean useCondition, Fn<T, Object> fn, Iterable values) {
        return useCondition ? andIn(fn, values) : this;
    }

    /**
     * Adds an "IN" condition for a field.
     *
     * @param fn     The method reference to the field.
     * @param values A collection of values.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andIn(Fn<T, Object> fn, Iterable values) {
        if (useCriterion(values)) {
            addCriterion(column(fn) + " IN", values, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * Conditionally adds a "NOT IN" condition for a field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param values       A collection of values.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andNotIn(boolean useCondition, Fn<T, Object> fn, Iterable values) {
        return useCondition ? andNotIn(fn, values) : this;
    }

    /**
     * Adds a "NOT IN" condition for a field.
     *
     * @param fn     The method reference to the field.
     * @param values A collection of values.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andNotIn(Fn<T, Object> fn, Iterable values) {
        if (useCriterion(values)) {
            addCriterion(column(fn) + " NOT IN", values, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * Conditionally adds a "BETWEEN" condition for a field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value1       The starting value of the range.
     * @param value2       The ending value of the range.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andBetween(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
        return useCondition ? andBetween(fn, value1, value2) : this;
    }

    /**
     * Adds a "BETWEEN" condition for a field.
     *
     * @param fn     The method reference to the field.
     * @param value1 The starting value of the range.
     * @param value2 The ending value of the range.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andBetween(Fn<T, Object> fn, Object value1, Object value2) {
        if (useCriterion(value1) && useCriterion(value2)) {
            addCriterion(column(fn) + " BETWEEN", value1, value2, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * Conditionally adds a "NOT BETWEEN" condition for a field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value1       The starting value of the range.
     * @param value2       The ending value of the range.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andNotBetween(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
        return useCondition ? andNotBetween(fn, value1, value2) : this;
    }

    /**
     * Adds a "NOT BETWEEN" condition for a field.
     *
     * @param fn     The method reference to the field.
     * @param value1 The starting value of the range.
     * @param value2 The ending value of the range.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andNotBetween(Fn<T, Object> fn, Object value1, Object value2) {
        if (useCriterion(value1) && useCriterion(value2)) {
            addCriterion(column(fn) + " NOT BETWEEN", value1, value2, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * Conditionally adds a "LIKE" condition for a field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value to match.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andLike(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andLike(fn, value) : this;
    }

    /**
     * Adds a "LIKE" condition for a field.
     *
     * @param fn    The method reference to the field.
     * @param value The value to match.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andLike(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " LIKE", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * Conditionally adds a "NOT LIKE" condition for a field.
     *
     * @param useCondition Whether to apply the condition.
     * @param fn           The method reference to the field.
     * @param value        The value to match.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andNotLike(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andNotLike(fn, value) : this;
    }

    /**
     * Adds a "NOT LIKE" condition for a field.
     *
     * @param fn    The method reference to the field.
     * @param value The value to match.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andNotLike(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " NOT LIKE", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * Adds multiple OR conditions.
     *
     * @param orCriteria1 The first OR criterion.
     * @param orCriteria2 The second OR criterion.
     * @param orCriterias Other OR criteria.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andOr(OrCriteria<T> orCriteria1, OrCriteria<T> orCriteria2, OrCriteria<T>... orCriterias) {
        List<OrCriteria<T>> orCriteriaList = new ArrayList<>(orCriterias != null ? orCriterias.length + 2 : 2);
        orCriteriaList.add(orCriteria1);
        orCriteriaList.add(orCriteria2);
        if (orCriterias != null) {
            orCriteriaList.addAll(Arrays.asList(orCriterias));
        }
        return andOr(orCriteriaList);
    }

    /**
     * Adds a list of OR conditions.
     *
     * @param orCriteriaList The list of OR criteria.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andOr(List<OrCriteria<T>> orCriteriaList) {
        criteria.add(new Criterion(null, orCriteriaList));
        return this;
    }

    /**
     * Conditionally adds a custom condition.
     *
     * @param useCondition Whether to apply the condition.
     * @param condition    The custom condition, e.g., "length(name) &lt; 5".
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andCondition(boolean useCondition, String condition) {
        return useCondition ? andCondition(condition) : this;
    }

    /**
     * Adds a custom condition.
     *
     * @param condition The custom condition, e.g., "length(name) &lt; 5".
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andCondition(String condition) {
        addCriterion(condition);
        return this;
    }

    /**
     * Conditionally adds a custom condition with a value.
     *
     * @param useCondition Whether to apply the condition.
     * @param condition    The custom condition, e.g., "length(name)=".
     * @param value        The value.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andCondition(boolean useCondition, String condition, Object value) {
        return useCondition ? andCondition(condition, value) : this;
    }

    /**
     * Adds a custom condition with a value.
     *
     * @param condition The custom condition, e.g., "length(name)=".
     * @param value     The value, e.g., 5.
     * @return This {@code Criteria} object.
     */
    public Criteria<T> andCondition(String condition, Object value) {
        criteria.add(new Criterion(condition, value));
        return this;
    }

    /**
     * Gets the list of criteria.
     *
     * @return The list of criteria.
     */
    public List<Criterion> getCriteria() {
        return criteria;
    }

    /**
     * Checks if the criteria are valid (i.e., not empty).
     *
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean isValid() {
        return criteria.size() > 0;
    }

}
