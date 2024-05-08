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
import org.miaixz.bus.mapper.support.Reflector;

/**
 * {@link WeekendSqlCriteria} 的工具类，提供一系列静态方法，减少泛型参数的指定，使代码更简洁、清晰
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WeekendSqls {

    public static <T> WeekendSqlCriteria<T> andIsNull(String property) {
        return WeekendSqlCriteria.<T>custom().andIsNull(property);
    }

    public static <T> WeekendSqlCriteria<T> andIsNull(FuncX<T, Object> FuncX) {
        return WeekendSqlCriteria.<T>custom().andIsNull(FuncX);
    }

    public static <T> WeekendSqlCriteria<T> andIsNotNull(String property) {
        return WeekendSqlCriteria.<T>custom().andIsNotNull(property);
    }

    public static <T> WeekendSqlCriteria<T> andIsNotNull(FuncX<T, Object> FuncX) {
        return WeekendSqlCriteria.<T>custom().andIsNotNull(FuncX);
    }

    public static <T> WeekendSqlCriteria<T> andEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().andEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andEqualTo(FuncX<T, Object> FuncX, Object value) {
        return WeekendSqlCriteria.<T>custom().andEqualTo(FuncX, value);
    }

    public static <T> WeekendSqlCriteria<T> andNotEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().andNotEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andNotEqualTo(FuncX<T, Object> FuncX, Object value) {
        return WeekendSqlCriteria.<T>custom().andNotEqualTo(FuncX, value);
    }

    public static <T> WeekendSqlCriteria<T> andGreaterThan(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().andGreaterThan(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andGreaterThan(FuncX<T, Object> FuncX, Object value) {
        return WeekendSqlCriteria.<T>custom().andGreaterThan(FuncX, value);
    }

    public static <T> WeekendSqlCriteria<T> andGreaterThanOrEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().andGreaterThanOrEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andGreaterThanOrEqualTo(FuncX<T, Object> FuncX, Object value) {
        return WeekendSqlCriteria.<T>custom().andGreaterThanOrEqualTo(FuncX, value);
    }

    public static <T> WeekendSqlCriteria<T> andLessThan(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().andLessThan(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andLessThan(FuncX<T, Object> FuncX, Object value) {
        return WeekendSqlCriteria.<T>custom().andLessThan(FuncX, value);
    }

    public static <T> WeekendSqlCriteria<T> andLessThanOrEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().andLessThanOrEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andLessThanOrEqualTo(FuncX<T, Object> FuncX, Object value) {
        return WeekendSqlCriteria.<T>custom().andLessThanOrEqualTo(FuncX, value);
    }

    public static <T> WeekendSqlCriteria<T> andIn(String property, Iterable values) {
        return WeekendSqlCriteria.<T>custom().andIn(property, values);
    }

    public static <T> WeekendSqlCriteria<T> andIn(FuncX<T, Object> FuncX, Iterable values) {
        return WeekendSqlCriteria.<T>custom().andIn(FuncX, values);
    }

    public static <T> WeekendSqlCriteria<T> andNotIn(String property, Iterable values) {
        return WeekendSqlCriteria.<T>custom().andNotIn(property, values);
    }

    public static <T> WeekendSqlCriteria<T> andNotIn(FuncX<T, Object> FuncX, Iterable values) {
        return WeekendSqlCriteria.<T>custom().andNotIn(FuncX, values);
    }

    public static <T> WeekendSqlCriteria<T> andBetween(String property, Object value1, Object value2) {
        return WeekendSqlCriteria.<T>custom().andBetween(property, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> andBetween(FuncX<T, Object> FuncX, Object value1, Object value2) {
        return WeekendSqlCriteria.<T>custom().andBetween(FuncX, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> andNotBetween(String property, Object value1, Object value2) {
        return WeekendSqlCriteria.<T>custom().andNotBetween(property, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> andNotBetween(FuncX<T, Object> FuncX, Object value1, Object value2) {
        return WeekendSqlCriteria.<T>custom().andNotBetween(FuncX, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> andLike(String property, String value) {
        return WeekendSqlCriteria.<T>custom().andLike(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andLike(FuncX<T, Object> FuncX, String value) {
        return WeekendSqlCriteria.<T>custom().andLike(FuncX, value);
    }

    public static <T> WeekendSqlCriteria<T> andNotLike(String property, String value) {
        return WeekendSqlCriteria.<T>custom().andNotLike(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andNotLike(FuncX<T, Object> FuncX, String value) {
        return WeekendSqlCriteria.<T>custom().andNotLike(FuncX, value);
    }

    public static <T> WeekendSqlCriteria<T> orIsNull(String property) {
        return WeekendSqlCriteria.<T>custom().orIsNull(property);
    }

    public static <T> WeekendSqlCriteria<T> orIsNull(FuncX<T, Object> FuncX) {
        return WeekendSqlCriteria.<T>custom().orIsNull(FuncX);
    }

    public static <T> WeekendSqlCriteria<T> orIsNotNull(String property) {
        return WeekendSqlCriteria.<T>custom().orIsNotNull(property);
    }

    public static <T> WeekendSqlCriteria<T> orIsNotNull(FuncX<T, Object> FuncX) {
        return WeekendSqlCriteria.<T>custom().orIsNotNull(FuncX);
    }

    public static <T> WeekendSqlCriteria<T> orEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().orEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orEqualTo(FuncX<T, Object> FuncX, String value) {
        return WeekendSqlCriteria.<T>custom().orEqualTo(FuncX, value);
    }

    public static <T> WeekendSqlCriteria<T> orNotEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().orNotEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orNotEqualTo(FuncX<T, Object> FuncX, String value) {
        return WeekendSqlCriteria.<T>custom().orNotEqualTo(FuncX, value);
    }

    public static <T> WeekendSqlCriteria<T> orGreaterThan(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().orGreaterThan(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orGreaterThan(FuncX<T, Object> FuncX, String value) {
        return WeekendSqlCriteria.<T>custom().orGreaterThan(FuncX, value);
    }

    public static <T> WeekendSqlCriteria<T> orGreaterThanOrEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().orGreaterThanOrEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orGreaterThanOrEqualTo(FuncX<T, Object> FuncX, String value) {
        return WeekendSqlCriteria.<T>custom().orGreaterThanOrEqualTo(FuncX, value);
    }

    public static <T> WeekendSqlCriteria<T> orLessThan(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().orLessThan(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orLessThan(FuncX<T, Object> FuncX, String value) {
        return WeekendSqlCriteria.<T>custom().orLessThan(FuncX, value);
    }

    public static <T> WeekendSqlCriteria<T> orLessThanOrEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().orLessThanOrEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orLessThanOrEqualTo(FuncX<T, Object> FuncX, String value) {
        return WeekendSqlCriteria.<T>custom().orLessThanOrEqualTo(FuncX, value);
    }

    public static <T> WeekendSqlCriteria<T> orIn(String property, Iterable values) {
        return WeekendSqlCriteria.<T>custom().orIn(property, values);
    }

    public static <T> WeekendSqlCriteria<T> orIn(FuncX<T, Object> FuncX, Iterable values) {
        return WeekendSqlCriteria.<T>custom().orIn(FuncX, values);
    }

    public static <T> WeekendSqlCriteria<T> orNotIn(String property, Iterable values) {
        return WeekendSqlCriteria.<T>custom().orNotIn(property, values);
    }

    public static <T> WeekendSqlCriteria<T> orNotIn(FuncX<T, Object> FuncX, Iterable values) {
        return WeekendSqlCriteria.<T>custom().orNotIn(FuncX, values);
    }

    public static <T> WeekendSqlCriteria<T> orBetween(String property, Object value1, Object value2) {
        return WeekendSqlCriteria.<T>custom().orBetween(property, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> orBetween(FuncX<T, Object> FuncX, Object value1, Object value2) {
        return WeekendSqlCriteria.<T>custom().orBetween(FuncX, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> orNotBetween(String property, Object value1, Object value2) {
        return WeekendSqlCriteria.<T>custom().orNotBetween(property, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> orNotBetween(FuncX<T, Object> FuncX, Object value1, Object value2) {
        return WeekendSqlCriteria.<T>custom().orNotBetween(FuncX, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> orLike(String property, String value) {
        return WeekendSqlCriteria.<T>custom().orLike(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orLike(FuncX<T, Object> FuncX, String value) {
        return WeekendSqlCriteria.<T>custom().orLike(FuncX, value);
    }

    public static <T> WeekendSqlCriteria<T> orNotLike(String property, String value) {
        return WeekendSqlCriteria.<T>custom().orNotLike(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orNotLike(FuncX<T, Object> FuncX, String value) {
        return WeekendSqlCriteria.<T>custom().orNotLike(FuncX, value);
    }

    public static <T> String[] select(FuncX<T, Object>... FuncX) {
        return Reflector.fnToFieldNames(FuncX);
    }

}
