/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.mapper.criteria;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.criteria.Sqls.Criteria;
import org.miaixz.bus.mapper.criteria.Sqls.Criterion;
import org.miaixz.bus.mapper.support.Reflector;

/**
 * 条件语句
 *
 * @param <T> 泛型
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeekendSqlCriteria<T> implements SqlCriteria {

    private Criteria criteria;

    private WeekendSqlCriteria() {
        this.criteria = new Criteria();
    }

    public static <T> WeekendSqlCriteria<T> custom() {
        return new WeekendSqlCriteria<T>();
    }

    public WeekendSqlCriteria<T> andIsNull(String property) {
        this.criteria.getCriterions().add(new Criterion(property, "is null", "and"));
        return this;
    }

    public WeekendSqlCriteria<T> andIsNull(FunctionX<T, Object> fn) {
        return this.andIsNull(Reflector.fnToFieldName(fn));
    }

    public WeekendSqlCriteria<T> andIsNotNull(String property) {
        this.criteria.getCriterions().add(new Criterion(property, "is not null", "and"));
        return this;
    }

    public WeekendSqlCriteria<T> andIsNotNull(FunctionX<T, Object> fn) {
        return this.andIsNotNull(Reflector.fnToFieldName(fn));
    }

    public WeekendSqlCriteria<T> andEqualTo(String property, Object value) {
        this.criteria.getCriterions().add(new Criterion(property, value, Symbol.EQUAL, "and"));
        return this;
    }

    public WeekendSqlCriteria<T> andEqualTo(FunctionX<T, Object> fn, Object value) {
        return this.andEqualTo(Reflector.fnToFieldName(fn), value);
    }

    public WeekendSqlCriteria<T> andNotEqualTo(String property, Object value) {
        this.criteria.getCriterions().add(new Criterion(property, value, "<>", "and"));
        return this;
    }

    public WeekendSqlCriteria<T> andNotEqualTo(FunctionX<T, Object> fn, Object value) {
        return this.andNotEqualTo(Reflector.fnToFieldName(fn), value);
    }

    public WeekendSqlCriteria<T> andGreaterThan(String property, Object value) {
        this.criteria.getCriterions().add(new Criterion(property, value, ">", "and"));
        return this;
    }

    public WeekendSqlCriteria<T> andGreaterThan(FunctionX<T, Object> fn, Object value) {
        return this.andGreaterThan(Reflector.fnToFieldName(fn), value);
    }

    public WeekendSqlCriteria<T> andGreaterThanOrEqualTo(String property, Object value) {
        this.criteria.getCriterions().add(new Criterion(property, value, ">=", "and"));
        return this;
    }

    public WeekendSqlCriteria<T> andGreaterThanOrEqualTo(FunctionX<T, Object> fn, Object value) {
        return this.andGreaterThanOrEqualTo(Reflector.fnToFieldName(fn), value);
    }

    public WeekendSqlCriteria<T> andLessThan(String property, Object value) {
        this.criteria.getCriterions().add(new Criterion(property, value, "<", "and"));
        return this;
    }

    public WeekendSqlCriteria<T> andLessThan(FunctionX<T, Object> fn, Object value) {
        return this.andLessThan(Reflector.fnToFieldName(fn), value);
    }

    public WeekendSqlCriteria<T> andLessThanOrEqualTo(String property, Object value) {
        this.criteria.getCriterions().add(new Criterion(property, value, "<=", "and"));
        return this;
    }

    public WeekendSqlCriteria<T> andLessThanOrEqualTo(FunctionX<T, Object> fn, Object value) {
        return this.andLessThanOrEqualTo(Reflector.fnToFieldName(fn), value);
    }

    public WeekendSqlCriteria<T> andIn(String property, Iterable values) {
        this.criteria.getCriterions().add(new Criterion(property, values, "in", "and"));
        return this;
    }

    public WeekendSqlCriteria<T> andIn(FunctionX<T, Object> fn, Iterable values) {
        return this.andIn(Reflector.fnToFieldName(fn), values);
    }

    public WeekendSqlCriteria<T> andNotIn(String property, Iterable values) {
        this.criteria.getCriterions().add(new Criterion(property, values, "not in", "and"));
        return this;
    }

    public WeekendSqlCriteria<T> andNotIn(FunctionX<T, Object> fn, Iterable values) {
        return this.andNotIn(Reflector.fnToFieldName(fn), values);
    }

    public WeekendSqlCriteria<T> andBetween(String property, Object value1, Object value2) {
        this.criteria.getCriterions().add(new Criterion(property, value1, value2, "between", "and"));
        return this;
    }

    public WeekendSqlCriteria<T> andBetween(FunctionX<T, Object> fn, Object value1, Object value2) {
        return this.andBetween(Reflector.fnToFieldName(fn), value1, value2);
    }

    public WeekendSqlCriteria<T> andNotBetween(String property, Object value1, Object value2) {
        this.criteria.getCriterions().add(new Criterion(property, value1, value2, "not between", "and"));
        return this;
    }

    public WeekendSqlCriteria<T> andNotBetween(FunctionX<T, Object> fn, Object value1, Object value2) {
        return this.andNotBetween(Reflector.fnToFieldName(fn), value1, value2);
    }

    public WeekendSqlCriteria<T> andLike(String property, String value) {
        this.criteria.getCriterions().add(new Criterion(property, value, "like", "and"));
        return this;
    }

    public WeekendSqlCriteria<T> andLike(FunctionX<T, Object> fn, String value) {
        return this.andLike(Reflector.fnToFieldName(fn), value);
    }

    public WeekendSqlCriteria<T> andNotLike(String property, String value) {
        this.criteria.getCriterions().add(new Criterion(property, value, "not like", "and"));
        return this;
    }

    public WeekendSqlCriteria<T> andNotLike(FunctionX<T, Object> fn, String value) {
        return this.andNotLike(Reflector.fnToFieldName(fn), value);
    }

    public WeekendSqlCriteria<T> orIsNull(String property) {
        this.criteria.getCriterions().add(new Criterion(property, "is null", "or"));
        return this;
    }

    public WeekendSqlCriteria<T> orIsNull(FunctionX<T, Object> fn) {
        return this.orIsNull(Reflector.fnToFieldName(fn));
    }

    public WeekendSqlCriteria<T> orIsNotNull(String property) {
        this.criteria.getCriterions().add(new Criterion(property, "is not null", "or"));
        return this;
    }

    public WeekendSqlCriteria<T> orIsNotNull(FunctionX<T, Object> fn) {
        return this.orIsNotNull(Reflector.fnToFieldName(fn));
    }

    public WeekendSqlCriteria<T> orEqualTo(String property, Object value) {
        this.criteria.getCriterions().add(new Criterion(property, value, Symbol.EQUAL, "or"));
        return this;
    }

    public WeekendSqlCriteria<T> orEqualTo(FunctionX<T, Object> fn, Object value) {
        return this.orEqualTo(Reflector.fnToFieldName(fn), value);
    }

    public WeekendSqlCriteria<T> orNotEqualTo(String property, Object value) {
        this.criteria.getCriterions().add(new Criterion(property, value, "<>", "or"));
        return this;
    }

    public WeekendSqlCriteria<T> orNotEqualTo(FunctionX<T, Object> fn, Object value) {
        return this.orNotEqualTo(Reflector.fnToFieldName(fn), value);
    }

    public WeekendSqlCriteria<T> orGreaterThan(String property, Object value) {
        this.criteria.getCriterions().add(new Criterion(property, value, ">", "or"));
        return this;
    }

    public WeekendSqlCriteria<T> orGreaterThan(FunctionX<T, Object> fn, Object value) {
        return this.orGreaterThan(Reflector.fnToFieldName(fn), value);
    }

    public WeekendSqlCriteria<T> orGreaterThanOrEqualTo(String property, Object value) {
        this.criteria.getCriterions().add(new Criterion(property, value, ">=", "or"));
        return this;
    }

    public WeekendSqlCriteria<T> orGreaterThanOrEqualTo(FunctionX<T, Object> fn, Object value) {
        return this.orGreaterThanOrEqualTo(Reflector.fnToFieldName(fn), value);
    }

    public WeekendSqlCriteria<T> orLessThan(String property, Object value) {
        this.criteria.getCriterions().add(new Criterion(property, value, "<", "or"));
        return this;
    }

    public WeekendSqlCriteria<T> orLessThan(FunctionX<T, Object> fn, Object value) {
        return this.orLessThan(Reflector.fnToFieldName(fn), value);
    }

    public WeekendSqlCriteria<T> orLessThanOrEqualTo(String property, Object value) {
        this.criteria.getCriterions().add(new Criterion(property, value, "<=", "or"));
        return this;
    }

    public WeekendSqlCriteria<T> orLessThanOrEqualTo(FunctionX<T, Object> fn, Object value) {
        return this.orLessThanOrEqualTo(Reflector.fnToFieldName(fn), value);
    }

    public WeekendSqlCriteria<T> orIn(String property, Iterable values) {
        this.criteria.getCriterions().add(new Criterion(property, values, "in", "or"));
        return this;
    }

    public WeekendSqlCriteria<T> orIn(FunctionX<T, Object> fn, Iterable values) {
        return this.orIn(Reflector.fnToFieldName(fn), values);
    }

    public WeekendSqlCriteria<T> orNotIn(String property, Iterable values) {
        this.criteria.getCriterions().add(new Criterion(property, values, "not in", "or"));
        return this;
    }

    public WeekendSqlCriteria<T> orNotIn(FunctionX<T, Object> fn, Iterable values) {
        return this.orNotIn(Reflector.fnToFieldName(fn), values);
    }

    public WeekendSqlCriteria<T> orBetween(String property, Object value1, Object value2) {
        this.criteria.getCriterions().add(new Criterion(property, value1, value2, "between", "or"));
        return this;
    }

    public WeekendSqlCriteria<T> orBetween(FunctionX<T, Object> fn, Object value1, Object value2) {
        return this.orBetween(Reflector.fnToFieldName(fn), value1, value2);
    }

    public WeekendSqlCriteria<T> orNotBetween(String property, Object value1, Object value2) {
        this.criteria.getCriterions().add(new Criterion(property, value1, value2, "not between", "or"));
        return this;
    }

    public WeekendSqlCriteria<T> orNotBetween(FunctionX<T, Object> fn, Object value1, Object value2) {
        return this.orNotBetween(Reflector.fnToFieldName(fn), value1, value2);
    }

    public WeekendSqlCriteria<T> orLike(String property, String value) {
        this.criteria.getCriterions().add(new Criterion(property, value, "like", "or"));
        return this;
    }

    public WeekendSqlCriteria<T> orLike(FunctionX<T, Object> fn, String value) {
        return this.orLike(Reflector.fnToFieldName(fn), value);
    }

    public WeekendSqlCriteria<T> orNotLike(String property, Object value) {
        this.criteria.getCriterions().add(new Criterion(property, value, "not like", "or"));
        return this;
    }

    public WeekendSqlCriteria<T> orNotLike(FunctionX<T, Object> fn, Object value) {
        return this.orNotLike(Reflector.fnToFieldName(fn), value);
    }

    @Override
    public Criteria getCriteria() {
        return criteria;
    }

}
