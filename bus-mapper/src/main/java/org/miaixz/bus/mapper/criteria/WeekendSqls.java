/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org mybatis.io and other contributors.         ~
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

import org.miaixz.bus.core.center.function.FunctionX;
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

    public static <T> WeekendSqlCriteria<T> andIsNull(FunctionX<T, Object> FunctionX) {
        return WeekendSqlCriteria.<T>custom().andIsNull(FunctionX);
    }

    public static <T> WeekendSqlCriteria<T> andIsNotNull(String property) {
        return WeekendSqlCriteria.<T>custom().andIsNotNull(property);
    }

    public static <T> WeekendSqlCriteria<T> andIsNotNull(FunctionX<T, Object> FunctionX) {
        return WeekendSqlCriteria.<T>custom().andIsNotNull(FunctionX);
    }

    public static <T> WeekendSqlCriteria<T> andEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().andEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andEqualTo(FunctionX<T, Object> FunctionX, Object value) {
        return WeekendSqlCriteria.<T>custom().andEqualTo(FunctionX, value);
    }

    public static <T> WeekendSqlCriteria<T> andNotEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().andNotEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andNotEqualTo(FunctionX<T, Object> FunctionX, Object value) {
        return WeekendSqlCriteria.<T>custom().andNotEqualTo(FunctionX, value);
    }

    public static <T> WeekendSqlCriteria<T> andGreaterThan(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().andGreaterThan(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andGreaterThan(FunctionX<T, Object> FunctionX, Object value) {
        return WeekendSqlCriteria.<T>custom().andGreaterThan(FunctionX, value);
    }

    public static <T> WeekendSqlCriteria<T> andGreaterThanOrEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().andGreaterThanOrEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andGreaterThanOrEqualTo(FunctionX<T, Object> FunctionX, Object value) {
        return WeekendSqlCriteria.<T>custom().andGreaterThanOrEqualTo(FunctionX, value);
    }

    public static <T> WeekendSqlCriteria<T> andLessThan(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().andLessThan(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andLessThan(FunctionX<T, Object> FunctionX, Object value) {
        return WeekendSqlCriteria.<T>custom().andLessThan(FunctionX, value);
    }

    public static <T> WeekendSqlCriteria<T> andLessThanOrEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().andLessThanOrEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andLessThanOrEqualTo(FunctionX<T, Object> FunctionX, Object value) {
        return WeekendSqlCriteria.<T>custom().andLessThanOrEqualTo(FunctionX, value);
    }

    public static <T> WeekendSqlCriteria<T> andIn(String property, Iterable values) {
        return WeekendSqlCriteria.<T>custom().andIn(property, values);
    }

    public static <T> WeekendSqlCriteria<T> andIn(FunctionX<T, Object> FunctionX, Iterable values) {
        return WeekendSqlCriteria.<T>custom().andIn(FunctionX, values);
    }

    public static <T> WeekendSqlCriteria<T> andNotIn(String property, Iterable values) {
        return WeekendSqlCriteria.<T>custom().andNotIn(property, values);
    }

    public static <T> WeekendSqlCriteria<T> andNotIn(FunctionX<T, Object> FunctionX, Iterable values) {
        return WeekendSqlCriteria.<T>custom().andNotIn(FunctionX, values);
    }

    public static <T> WeekendSqlCriteria<T> andBetween(String property, Object value1, Object value2) {
        return WeekendSqlCriteria.<T>custom().andBetween(property, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> andBetween(FunctionX<T, Object> FunctionX, Object value1, Object value2) {
        return WeekendSqlCriteria.<T>custom().andBetween(FunctionX, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> andNotBetween(String property, Object value1, Object value2) {
        return WeekendSqlCriteria.<T>custom().andNotBetween(property, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> andNotBetween(FunctionX<T, Object> FunctionX, Object value1,
            Object value2) {
        return WeekendSqlCriteria.<T>custom().andNotBetween(FunctionX, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> andLike(String property, String value) {
        return WeekendSqlCriteria.<T>custom().andLike(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andLike(FunctionX<T, Object> FunctionX, String value) {
        return WeekendSqlCriteria.<T>custom().andLike(FunctionX, value);
    }

    public static <T> WeekendSqlCriteria<T> andNotLike(String property, String value) {
        return WeekendSqlCriteria.<T>custom().andNotLike(property, value);
    }

    public static <T> WeekendSqlCriteria<T> andNotLike(FunctionX<T, Object> FunctionX, String value) {
        return WeekendSqlCriteria.<T>custom().andNotLike(FunctionX, value);
    }

    public static <T> WeekendSqlCriteria<T> orIsNull(String property) {
        return WeekendSqlCriteria.<T>custom().orIsNull(property);
    }

    public static <T> WeekendSqlCriteria<T> orIsNull(FunctionX<T, Object> FunctionX) {
        return WeekendSqlCriteria.<T>custom().orIsNull(FunctionX);
    }

    public static <T> WeekendSqlCriteria<T> orIsNotNull(String property) {
        return WeekendSqlCriteria.<T>custom().orIsNotNull(property);
    }

    public static <T> WeekendSqlCriteria<T> orIsNotNull(FunctionX<T, Object> FunctionX) {
        return WeekendSqlCriteria.<T>custom().orIsNotNull(FunctionX);
    }

    public static <T> WeekendSqlCriteria<T> orEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().orEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orEqualTo(FunctionX<T, Object> FunctionX, String value) {
        return WeekendSqlCriteria.<T>custom().orEqualTo(FunctionX, value);
    }

    public static <T> WeekendSqlCriteria<T> orNotEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().orNotEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orNotEqualTo(FunctionX<T, Object> FunctionX, String value) {
        return WeekendSqlCriteria.<T>custom().orNotEqualTo(FunctionX, value);
    }

    public static <T> WeekendSqlCriteria<T> orGreaterThan(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().orGreaterThan(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orGreaterThan(FunctionX<T, Object> FunctionX, String value) {
        return WeekendSqlCriteria.<T>custom().orGreaterThan(FunctionX, value);
    }

    public static <T> WeekendSqlCriteria<T> orGreaterThanOrEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().orGreaterThanOrEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orGreaterThanOrEqualTo(FunctionX<T, Object> FunctionX, String value) {
        return WeekendSqlCriteria.<T>custom().orGreaterThanOrEqualTo(FunctionX, value);
    }

    public static <T> WeekendSqlCriteria<T> orLessThan(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().orLessThan(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orLessThan(FunctionX<T, Object> FunctionX, String value) {
        return WeekendSqlCriteria.<T>custom().orLessThan(FunctionX, value);
    }

    public static <T> WeekendSqlCriteria<T> orLessThanOrEqualTo(String property, Object value) {
        return WeekendSqlCriteria.<T>custom().orLessThanOrEqualTo(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orLessThanOrEqualTo(FunctionX<T, Object> FunctionX, String value) {
        return WeekendSqlCriteria.<T>custom().orLessThanOrEqualTo(FunctionX, value);
    }

    public static <T> WeekendSqlCriteria<T> orIn(String property, Iterable values) {
        return WeekendSqlCriteria.<T>custom().orIn(property, values);
    }

    public static <T> WeekendSqlCriteria<T> orIn(FunctionX<T, Object> FunctionX, Iterable values) {
        return WeekendSqlCriteria.<T>custom().orIn(FunctionX, values);
    }

    public static <T> WeekendSqlCriteria<T> orNotIn(String property, Iterable values) {
        return WeekendSqlCriteria.<T>custom().orNotIn(property, values);
    }

    public static <T> WeekendSqlCriteria<T> orNotIn(FunctionX<T, Object> FunctionX, Iterable values) {
        return WeekendSqlCriteria.<T>custom().orNotIn(FunctionX, values);
    }

    public static <T> WeekendSqlCriteria<T> orBetween(String property, Object value1, Object value2) {
        return WeekendSqlCriteria.<T>custom().orBetween(property, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> orBetween(FunctionX<T, Object> FunctionX, Object value1, Object value2) {
        return WeekendSqlCriteria.<T>custom().orBetween(FunctionX, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> orNotBetween(String property, Object value1, Object value2) {
        return WeekendSqlCriteria.<T>custom().orNotBetween(property, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> orNotBetween(FunctionX<T, Object> FunctionX, Object value1, Object value2) {
        return WeekendSqlCriteria.<T>custom().orNotBetween(FunctionX, value1, value2);
    }

    public static <T> WeekendSqlCriteria<T> orLike(String property, String value) {
        return WeekendSqlCriteria.<T>custom().orLike(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orLike(FunctionX<T, Object> FunctionX, String value) {
        return WeekendSqlCriteria.<T>custom().orLike(FunctionX, value);
    }

    public static <T> WeekendSqlCriteria<T> orNotLike(String property, String value) {
        return WeekendSqlCriteria.<T>custom().orNotLike(property, value);
    }

    public static <T> WeekendSqlCriteria<T> orNotLike(FunctionX<T, Object> FunctionX, String value) {
        return WeekendSqlCriteria.<T>custom().orNotLike(FunctionX, value);
    }

    public static <T> String[] select(FunctionX<T, Object>... FunctionX) {
        return Reflector.fnToFieldNames(FunctionX);
    }

}
