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

import org.miaixz.bus.core.lang.function.FuncX;
import org.miaixz.bus.mapper.entity.Condition.Criteria;
import org.miaixz.bus.mapper.entity.EntityColumn;
import org.miaixz.bus.mapper.support.Reflector;

import java.util.Map;

/**
 * 条件语句函数
 *
 * @param <A>  泛型
 * @param <B>  泛型
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeekendCriteria<A, B> extends Criteria {

    protected WeekendCriteria(Map<String, EntityColumn> propertyMap, boolean exists, boolean notNull) {
        super(propertyMap, exists, notNull);
    }

    public WeekendCriteria<A, B> andIsNull(FuncX<A, B> fn) {
        this.andIsNull(Reflector.fnToFieldName(fn));
        return this;
    }

    public WeekendCriteria<A, B> andIsNotNull(FuncX<A, B> fn) {
        super.andIsNotNull(Reflector.fnToFieldName(fn));
        return this;
    }

    public WeekendCriteria<A, B> andEqualTo(FuncX<A, B> fn, Object value) {
        super.andEqualTo(Reflector.fnToFieldName(fn), value);
        return this;
    }

    public WeekendCriteria<A, B> andNotEqualTo(FuncX<A, B> fn, Object value) {
        super.andNotEqualTo(Reflector.fnToFieldName(fn), value);
        return this;
    }

    public WeekendCriteria<A, B> andGreaterThan(FuncX<A, B> fn, Object value) {
        super.andGreaterThan(Reflector.fnToFieldName(fn), value);
        return this;
    }

    public WeekendCriteria<A, B> andGreaterThanOrEqualTo(FuncX<A, B> fn, Object value) {
        super.andGreaterThanOrEqualTo(Reflector.fnToFieldName(fn), value);
        return this;
    }

    public WeekendCriteria<A, B> andLessThan(FuncX<A, B> fn, Object value) {
        super.andLessThan(Reflector.fnToFieldName(fn), value);
        return this;
    }

    public WeekendCriteria<A, B> andLessThanOrEqualTo(FuncX<A, B> fn, Object value) {
        super.andLessThanOrEqualTo(Reflector.fnToFieldName(fn), value);
        return this;
    }

    public WeekendCriteria<A, B> andIn(FuncX<A, B> fn, Iterable values) {
        super.andIn(Reflector.fnToFieldName(fn), values);
        return this;
    }

    public WeekendCriteria<A, B> andNotIn(FuncX<A, B> fn, Iterable values) {
        super.andNotIn(Reflector.fnToFieldName(fn), values);
        return this;
    }

    public WeekendCriteria<A, B> andBetween(FuncX<A, B> fn, Object value1, Object value2) {
        super.andBetween(Reflector.fnToFieldName(fn), value1, value2);
        return this;
    }

    public WeekendCriteria<A, B> andNotBetween(FuncX<A, B> fn, Object value1, Object value2) {
        super.andNotBetween(Reflector.fnToFieldName(fn), value1, value2);
        return this;
    }

    public WeekendCriteria<A, B> andLike(FuncX<A, B> fn, String value) {
        super.andLike(Reflector.fnToFieldName(fn), value);
        return this;
    }

    public WeekendCriteria<A, B> andNotLike(FuncX<A, B> fn, String value) {
        super.andNotLike(Reflector.fnToFieldName(fn), value);
        return this;
    }

    public WeekendCriteria<A, B> orIsNull(FuncX<A, B> fn) {
        super.orIsNull(Reflector.fnToFieldName(fn));
        return this;
    }

    public WeekendCriteria<A, B> orIsNotNull(FuncX<A, B> fn) {
        super.orIsNotNull(Reflector.fnToFieldName(fn));
        return this;
    }

    public WeekendCriteria<A, B> orEqualTo(FuncX<A, B> fn, Object value) {
        super.orEqualTo(Reflector.fnToFieldName(fn), value);
        return this;
    }

    public WeekendCriteria<A, B> orNotEqualTo(FuncX<A, B> fn, Object value) {
        super.orNotEqualTo(Reflector.fnToFieldName(fn), value);
        return this;
    }

    public WeekendCriteria<A, B> orGreaterThan(FuncX<A, B> fn, Object value) {
        super.orGreaterThan(Reflector.fnToFieldName(fn), value);
        return this;
    }

    public WeekendCriteria<A, B> orGreaterThanOrEqualTo(FuncX<A, B> fn, Object value) {
        super.orGreaterThanOrEqualTo(Reflector.fnToFieldName(fn), value);
        return this;
    }

    public WeekendCriteria<A, B> orLessThan(FuncX<A, B> fn, Object value) {
        super.orLessThan(Reflector.fnToFieldName(fn), value);
        return this;
    }

    public WeekendCriteria<A, B> orLessThanOrEqualTo(FuncX<A, B> fn, Object value) {
        super.orLessThanOrEqualTo(Reflector.fnToFieldName(fn), value);
        return this;
    }

    public WeekendCriteria<A, B> orIn(FuncX<A, B> fn, Iterable values) {
        super.orIn(Reflector.fnToFieldName(fn), values);
        return this;
    }

    public WeekendCriteria<A, B> orNotIn(FuncX<A, B> fn, Iterable values) {
        super.orNotIn(Reflector.fnToFieldName(fn), values);
        return this;
    }

    public WeekendCriteria<A, B> orBetween(FuncX<A, B> fn, Object value1, Object value2) {
        super.orBetween(Reflector.fnToFieldName(fn), value1, value2);
        return this;
    }

    public WeekendCriteria<A, B> orNotBetween(FuncX<A, B> fn, Object value1, Object value2) {
        super.orNotBetween(Reflector.fnToFieldName(fn), value1, value2);
        return this;
    }

    public WeekendCriteria<A, B> orLike(FuncX<A, B> fn, String value) {
        super.orLike(Reflector.fnToFieldName(fn), value);
        return this;
    }

    public WeekendCriteria<A, B> orNotLike(FuncX<A, B> fn, String value) {
        super.orNotLike(Reflector.fnToFieldName(fn), value);
        return this;
    }

}
