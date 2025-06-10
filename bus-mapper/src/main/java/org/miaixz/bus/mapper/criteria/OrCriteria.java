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
 * OR 查询条件类，继承自 Criteria，用于构建 OR 连接的 SQL 查询条件
 *
 * @param <T> 实体类类型
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
public class OrCriteria<T> extends Criteria<T> {

    /**
     * 默认构造函数，初始化 OR 条件
     */
    public OrCriteria() {
        super();
    }

    /**
     * 添加字段为空的 OR 条件
     *
     * @param fn 方法引用
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andIsNull(Fn<T, Object> fn) {
        super.andIsNull(fn);
        return this;
    }

    /**
     * 添加字段非空的 OR 条件
     *
     * @param fn 方法引用
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andIsNotNull(Fn<T, Object> fn) {
        super.andIsNotNull(fn);
        return this;
    }

    /**
     * 添加字段等于值的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andEqualTo(Fn<T, Object> fn, Object value) {
        super.andEqualTo(fn, value);
        return this;
    }

    /**
     * 添加字段不等于值的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andNotEqualTo(Fn<T, Object> fn, Object value) {
        super.andNotEqualTo(fn, value);
        return this;
    }

    /**
     * 添加字段大于值的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andGreaterThan(Fn<T, Object> fn, Object value) {
        super.andGreaterThan(fn, value);
        return this;
    }

    /**
     * 添加字段大于等于值的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andGreaterThanOrEqualTo(Fn<T, Object> fn, Object value) {
        super.andGreaterThanOrEqualTo(fn, value);
        return this;
    }

    /**
     * 添加字段小于值的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andLessThan(Fn<T, Object> fn, Object value) {
        super.andLessThan(fn, value);
        return this;
    }

    /**
     * 添加字段小于等于值的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andLessThanOrEqualTo(Fn<T, Object> fn, Object value) {
        super.andLessThanOrEqualTo(fn, value);
        return this;
    }

    /**
     * 添加字段在值集合中的 OR 条件
     *
     * @param fn     方法引用
     * @param values 值集合
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andIn(Fn<T, Object> fn, Iterable values) {
        super.andIn(fn, values);
        return this;
    }

    /**
     * 添加字段不在值集合中的 OR 条件
     *
     * @param fn     方法引用
     * @param values 值集合
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andNotIn(Fn<T, Object> fn, Iterable values) {
        super.andNotIn(fn, values);
        return this;
    }

    /**
     * 添加字段在值区间内的 OR 条件
     *
     * @param fn     方法引用
     * @param value1 起始值
     * @param value2 结束值
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andBetween(Fn<T, Object> fn, Object value1, Object value2) {
        super.andBetween(fn, value1, value2);
        return this;
    }

    /**
     * 添加字段不在值区间内的 OR 条件
     *
     * @param fn     方法引用
     * @param value1 起始值
     * @param value2 结束值
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andNotBetween(Fn<T, Object> fn, Object value1, Object value2) {
        super.andNotBetween(fn, value1, value2);
        return this;
    }

    /**
     * 添加字段模糊匹配的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andLike(Fn<T, Object> fn, Object value) {
        super.andLike(fn, value);
        return this;
    }

    /**
     * 添加字段非模糊匹配的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andNotLike(Fn<T, Object> fn, Object value) {
        super.andNotLike(fn, value);
        return this;
    }

    /**
     * 添加自定义 OR 条件
     *
     * @param condition 自定义条件，如 length(name) &lt; 5
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andCondition(String condition) {
        super.andCondition(condition);
        return this;
    }

    /**
     * 添加自定义 OR 条件和值
     *
     * @param condition 自定义条件，如 length(name)=
     * @param value     值，如 5
     * @return 当前 OR 条件对象
     */
    @Override
    public OrCriteria<T> andCondition(String condition, Object value) {
        super.andCondition(condition, value);
        return this;
    }

    /**
     * 条件添加字段为空的 OR 条件
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> isNull(boolean useCondition, Fn<T, Object> fn) {
        return useCondition ? isNull(fn) : this;
    }

    /**
     * 添加字段为空的 OR 条件
     *
     * @param fn 方法引用
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> isNull(Fn<T, Object> fn) {
        super.andIsNull(fn);
        return this;
    }

    /**
     * 条件添加字段非空的 OR 条件
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> isNotNull(boolean useCondition, Fn<T, Object> fn) {
        return useCondition ? isNotNull(fn) : this;
    }

    /**
     * 添加字段非空的 OR 条件
     *
     * @param fn 方法引用
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> isNotNull(Fn<T, Object> fn) {
        super.andIsNotNull(fn);
        return this;
    }

    /**
     * 条件添加字段等于值的 OR 条件
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> eq(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? eq(fn, value) : this;
    }

    /**
     * 添加字段等于值的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> eq(Fn<T, Object> fn, Object value) {
        super.andEqualTo(fn, value);
        return this;
    }

    /**
     * 条件添加字段不等于值的 OR 条件
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> ne(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? ne(fn, value) : this;
    }

    /**
     * 添加字段不等于值的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> ne(Fn<T, Object> fn, Object value) {
        super.andNotEqualTo(fn, value);
        return this;
    }

    /**
     * 条件添加字段大于值的 OR 条件
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> gt(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? gt(fn, value) : this;
    }

    /**
     * 添加字段大于值的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> gt(Fn<T, Object> fn, Object value) {
        super.andGreaterThan(fn, value);
        return this;
    }

    /**
     * 条件添加字段大于等于值的 OR 条件
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> ge(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? ge(fn, value) : this;
    }

    /**
     * 添加字段大于等于值的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> ge(Fn<T, Object> fn, Object value) {
        super.andGreaterThanOrEqualTo(fn, value);
        return this;
    }

    /**
     * 条件添加字段小于值的 OR 条件
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> lt(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? lt(fn, value) : this;
    }

    /**
     * 添加字段小于值的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> lt(Fn<T, Object> fn, Object value) {
        super.andLessThan(fn, value);
        return this;
    }

    /**
     * 条件添加字段小于等于值的 OR 条件
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> le(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? le(fn, value) : this;
    }

    /**
     * 添加字段小于等于值的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> le(Fn<T, Object> fn, Object value) {
        super.andLessThanOrEqualTo(fn, value);
        return this;
    }

    /**
     * 条件添加字段在值集合中的 OR 条件
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param values       值集合
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> in(boolean useCondition, Fn<T, Object> fn, Iterable values) {
        return useCondition ? in(fn, values) : this;
    }

    /**
     * 添加字段在值集合中的 OR 条件
     *
     * @param fn     方法引用
     * @param values 值集合
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> in(Fn<T, Object> fn, Iterable values) {
        super.andIn(fn, values);
        return this;
    }

    /**
     * 条件添加字段不在值集合中的 OR 条件
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param values       值集合
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> notIn(boolean useCondition, Fn<T, Object> fn, Iterable values) {
        return useCondition ? notIn(fn, values) : this;
    }

    /**
     * 添加字段不在值集合中的 OR 条件
     *
     * @param fn     方法引用
     * @param values 值集合
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> notIn(Fn<T, Object> fn, Iterable values) {
        super.andNotIn(fn, values);
        return this;
    }

    /**
     * 条件添加字段在值区间内的 OR 条件
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value1       起始值
     * @param value2       结束值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> between(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
        return useCondition ? between(fn, value1, value2) : this;
    }

    /**
     * 添加字段在值区间内的 OR 条件
     *
     * @param fn     方法引用
     * @param value1 起始值
     * @param value2 结束值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> between(Fn<T, Object> fn, Object value1, Object value2) {
        super.andBetween(fn, value1, value2);
        return this;
    }

    /**
     * 条件添加字段不在值区间内的 OR 条件
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value1       起始值
     * @param value2       结束值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> notBetween(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
        return useCondition ? notBetween(fn, value1, value2) : this;
    }

    /**
     * 添加字段不在值区间内的 OR 条件
     *
     * @param fn     方法引用
     * @param value1 起始值
     * @param value2 结束值
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> notBetween(Fn<T, Object> fn, Object value1, Object value2) {
        super.andNotBetween(fn, value1, value2);
        return this;
    }

    /**
     * 条件添加字段包含值的 OR 条件（LIKE %value%）
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值，两侧自动添加 %
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> contains(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? contains(fn, value) : this;
    }

    /**
     * 添加字段包含值的 OR 条件（LIKE %value%）
     *
     * @param fn    方法引用
     * @param value 值，两侧自动添加 %
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> contains(Fn<T, Object> fn, String value) {
        super.andLike(fn, Symbol.PERCENT + value + Symbol.PERCENT);
        return this;
    }

    /**
     * 条件添加字段以前缀值开头的 OR 条件（LIKE value%）
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值，右侧自动添加 %
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> startsWith(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? startsWith(fn, value) : this;
    }

    /**
     * 添加字段以前缀值开头的 OR 条件（LIKE value%）
     *
     * @param fn    方法引用
     * @param value 值，右侧自动添加 %
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> startsWith(Fn<T, Object> fn, String value) {
        super.andLike(fn, value + Symbol.PERCENT);
        return this;
    }

    /**
     * 条件添加字段以后缀值结尾的 OR 条件（LIKE %value）
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值，左侧自动添加 %
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> endsWith(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? endsWith(fn, value) : this;
    }

    /**
     * 添加字段以后缀值结尾的 OR 条件（LIKE %value）
     *
     * @param fn    方法引用
     * @param value 值，左侧自动添加 %
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> endsWith(Fn<T, Object> fn, String value) {
        super.andLike(fn, Symbol.PERCENT + value);
        return this;
    }

    /**
     * 条件添加字段模糊匹配的 OR 条件
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值，需指定 % 或 _ 进行模糊匹配
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> like(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? like(fn, value) : this;
    }

    /**
     * 添加字段模糊匹配的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值，需指定 % 或 _ 进行模糊匹配
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> like(Fn<T, Object> fn, String value) {
        super.andLike(fn, value);
        return this;
    }

    /**
     * 条件添加字段非模糊匹配的 OR 条件
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值，需指定 % 进行模糊匹配
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> notLike(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? notLike(fn, value) : this;
    }

    /**
     * 添加字段非模糊匹配的 OR 条件
     *
     * @param fn    方法引用
     * @param value 值，需指定 % 进行模糊匹配
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> notLike(Fn<T, Object> fn, String value) {
        super.andNotLike(fn, value);
        return this;
    }

    /**
     * 条件添加任意 OR 条件
     *
     * @param useCondition 是否启用条件
     * @param condition    自定义条件，如 length(name) &lt; 5
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> anyCondition(boolean useCondition, String condition) {
        return useCondition ? anyCondition(condition) : this;
    }

    /**
     * 添加任意 OR 条件
     *
     * @param condition 自定义条件，如 length(name) &lt; 5
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> anyCondition(String condition) {
        super.andCondition(condition);
        return this;
    }

    /**
     * 条件添加自定义 OR 条件和值
     *
     * @param useCondition 是否启用条件
     * @param condition    自定义条件，如 length(name)=
     * @param value        值，如 5
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> anyCondition(boolean useCondition, String condition, Object value) {
        return useCondition ? anyCondition(condition, value) : this;
    }

    /**
     * 添加自定义 OR 条件和值
     *
     * @param condition 自定义条件，如 length(name)=
     * @param value     值，如 5
     * @return 当前 OR 条件对象
     */
    public OrCriteria<T> anyCondition(String condition, Object value) {
        super.andCondition(condition, value);
        return this;
    }

}