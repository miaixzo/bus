/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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
 * 封装 Condition 查询条件，提供链式调用接口以构建复杂查询
 *
 * @param <T> 实体类类型
 * @param <I> 主键类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class ConditionWrapper<T, I extends Serializable> {

    private final BasicMapper<T, I> basicMapper;
    private final Condition<T> condition;
    private Criteria<T> current;

    /**
     * 构造函数，初始化基本 Mapper 和 Condition
     *
     * @param basicMapper 基本 Mapper 实例
     * @param condition   查询条件对象
     */
    public ConditionWrapper(BasicMapper<T, I> basicMapper, Condition<T> condition) {
        this.basicMapper = basicMapper;
        this.condition = condition;
        this.current = condition.createCriteria();
    }

    /**
     * 添加一组 OR 条件
     *
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> or() {
        this.current = this.condition.or();
        return this;
    }

    /**
     * 获取当前查询条件
     *
     * @return 当前 Condition 对象
     */
    public Condition<T> condition() {
        return condition;
    }

    /**
     * 清除所有条件并重置为新条件
     *
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> clear() {
        this.condition.clear();
        this.current = condition.createCriteria();
        return this;
    }

    /**
     * 指定查询列
     *
     * @param fns 方法引用数组
     * @return 当前包装器对象
     */
    @SafeVarargs
    public final ConditionWrapper<T, I> select(Fn<T, Object>... fns) {
        this.condition.selectColumns(fns);
        return this;
    }

    /**
     * 排除指定查询列
     *
     * @param fns 方法引用数组
     * @return 当前包装器对象
     */
    @SafeVarargs
    public final ConditionWrapper<T, I> exclude(Fn<T, Object>... fns) {
        this.condition.excludeColumns(fns);
        return this;
    }

    /**
     * 设置起始 SQL
     *
     * @param startSql 起始 SQL
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> startSql(String startSql) {
        this.condition.setStartSql(startSql);
        return this;
    }

    /**
     * 设置结尾 SQL
     *
     * @param endSql 结尾 SQL
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> endSql(String endSql) {
        this.condition.setEndSql(endSql);
        return this;
    }

    /**
     * 设置排序字段
     *
     * @param fn    排序列方法引用
     * @param order 排序方式（ASC/DESC）
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> orderBy(Fn<T, Object> fn, String order) {
        this.condition.orderBy(fn, order);
        return this;
    }

    /**
     * 设置字符串形式的排序，不覆盖已有排序
     *
     * @param orderByCondition 排序表达式
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> orderBy(String orderByCondition) {
        this.condition.orderBy(orderByCondition);
        return this;
    }

    /**
     * 设置动态构造的排序
     *
     * @param orderByCondition 排序表达式提供者
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> orderBy(Supplier<String> orderByCondition) {
        this.condition.orderBy(orderByCondition);
        return this;
    }

    /**
     * 条件设置动态构造的排序
     *
     * @param useOrderBy       是否启用排序
     * @param orderByCondition 排序表达式提供者
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> orderBy(boolean useOrderBy, Supplier<String> orderByCondition) {
        return useOrderBy ? this.orderBy(orderByCondition) : this;
    }

    /**
     * 设置升序排序
     *
     * @param fns 排序列方法引用数组
     * @return 当前包装器对象
     */
    @SafeVarargs
    public final ConditionWrapper<T, I> orderByAsc(Fn<T, Object>... fns) {
        this.condition.orderByAsc(fns);
        return this;
    }

    /**
     * 设置降序排序
     *
     * @param fns 排序列方法引用数组
     * @return 当前包装器对象
     */
    @SafeVarargs
    public final ConditionWrapper<T, I> orderByDesc(Fn<T, Object>... fns) {
        this.condition.orderByDesc(fns);
        return this;
    }

    /**
     * 启用 DISTINCT 查询
     *
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> distinct() {
        this.condition.setDistinct(true);
        return this;
    }

    /**
     * 条件设置更新字段和值
     *
     * @param useSet 是否启用
     * @param setSql SET 子句，如 "column = value"
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> set(boolean useSet, String setSql) {
        return useSet ? set(setSql) : this;
    }

    /**
     * 设置更新字段和值
     *
     * @param setSql SET 子句，如 "column = value"
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> set(String setSql) {
        this.condition.set(setSql);
        return this;
    }

    /**
     * 条件设置更新字段和值
     *
     * @param useSet 是否启用
     * @param fn     字段方法引用
     * @param value  值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> set(boolean useSet, Fn<T, Object> fn, Object value) {
        return useSet ? set(fn, value) : this;
    }

    /**
     * 条件设置更新字段和动态值
     *
     * @param useSet   是否启用
     * @param fn       字段方法引用
     * @param supplier 值提供者
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> set(boolean useSet, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useSet ? set(fn, supplier.get()) : this;
    }

    /**
     * 设置更新字段和值
     *
     * @param fn    字段方法引用
     * @param value 值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> set(Fn<T, Object> fn, Object value) {
        this.condition.set(fn, value);
        return this;
    }

    /**
     * 条件指定字段为 NULL
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> isNull(boolean useCondition, Fn<T, Object> fn) {
        return useCondition ? isNull(fn) : this;
    }

    /**
     * 指定字段为 NULL
     *
     * @param fn 字段方法引用
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> isNull(Fn<T, Object> fn) {
        this.current.andIsNull(fn);
        return this;
    }

    /**
     * 条件指定字段非 NULL
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> isNotNull(boolean useCondition, Fn<T, Object> fn) {
        return useCondition ? isNotNull(fn) : this;
    }

    /**
     * 指定字段非 NULL
     *
     * @param fn 字段方法引用
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> isNotNull(Fn<T, Object> fn) {
        this.current.andIsNotNull(fn);
        return this;
    }

    /**
     * 条件指定字段等于值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param value        值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> eq(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? eq(fn, value) : this;
    }

    /**
     * 条件指定字段等于动态值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param supplier     值提供者
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> eq(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? eq(fn, supplier.get()) : this;
    }

    /**
     * 指定字段等于值
     *
     * @param fn    字段方法引用
     * @param value 值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> eq(Fn<T, Object> fn, Object value) {
        this.current.andEqualTo(fn, value);
        return this;
    }

    /**
     * 条件指定字段不等于动态值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param supplier     值提供者
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> ne(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? ne(fn, supplier.get()) : this;
    }

    /**
     * 条件指定字段不等于值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param value        值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> ne(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? ne(fn, value) : this;
    }

    /**
     * 指定字段不等于值
     *
     * @param fn    字段方法引用
     * @param value 值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> ne(Fn<T, Object> fn, Object value) {
        this.current.andNotEqualTo(fn, value);
        return this;
    }

    /**
     * 条件指定字段大于动态值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param supplier     值提供者
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> gt(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? gt(fn, supplier.get()) : this;
    }

    /**
     * 条件指定字段大于值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param value        值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> gt(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? gt(fn, value) : this;
    }

    /**
     * 指定字段大于值
     *
     * @param fn    字段方法引用
     * @param value 值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> gt(Fn<T, Object> fn, Object value) {
        this.current.andGreaterThan(fn, value);
        return this;
    }

    /**
     * 条件指定字段大于等于动态值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param supplier     值提供者
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> ge(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? ge(fn, supplier.get()) : this;
    }

    /**
     * 条件指定字段大于等于值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param value        值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> ge(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? ge(fn, value) : this;
    }

    /**
     * 指定字段大于等于值
     *
     * @param fn    字段方法引用
     * @param value 值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> ge(Fn<T, Object> fn, Object value) {
        this.current.andGreaterThanOrEqualTo(fn, value);
        return this;
    }

    /**
     * 条件指定字段小于动态值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param supplier     值提供者
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> lt(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? lt(fn, supplier.get()) : this;
    }

    /**
     * 条件指定字段小于值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param value        值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> lt(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? lt(fn, value) : this;
    }

    /**
     * 指定字段小于值
     *
     * @param fn    字段方法引用
     * @param value 值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> lt(Fn<T, Object> fn, Object value) {
        this.current.andLessThan(fn, value);
        return this;
    }

    /**
     * 条件指定字段小于等于值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param value        值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> le(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? le(fn, value) : this;
    }

    /**
     * 条件指定字段小于等于动态值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param supplier     值提供者
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> le(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? le(fn, supplier.get()) : this;
    }

    /**
     * 指定字段小于等于值
     *
     * @param fn    字段方法引用
     * @param value 值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> le(Fn<T, Object> fn, Object value) {
        this.current.andLessThanOrEqualTo(fn, value);
        return this;
    }

    /**
     * 条件指定字段在值集合中
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param values       值集合
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> in(boolean useCondition, Fn<T, Object> fn, Iterable<?> values) {
        return useCondition ? in(fn, values) : this;
    }

    /**
     * 条件指定字段在动态值集合中
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param supplier     值集合提供者
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> in(boolean useCondition, Fn<T, Object> fn, Supplier<Iterable<?>> supplier) {
        return useCondition ? in(fn, supplier.get()) : this;
    }

    /**
     * 指定字段在值集合中
     *
     * @param fn     字段方法引用
     * @param values 值集合
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> in(Fn<T, Object> fn, Iterable<?> values) {
        this.current.andIn(fn, values);
        return this;
    }

    /**
     * 条件指定字段不在值集合中
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param values       值集合
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> notIn(boolean useCondition, Fn<T, Object> fn, Iterable<?> values) {
        return useCondition ? notIn(fn, values) : this;
    }

    /**
     * 条件指定字段不在动态值集合中
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param supplier     值集合提供者
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> notIn(boolean useCondition, Fn<T, Object> fn, Supplier<Iterable<?>> supplier) {
        return useCondition ? notIn(fn, supplier.get()) : this;
    }

    /**
     * 指定字段不在值集合中
     *
     * @param fn     字段方法引用
     * @param values 值集合
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> notIn(Fn<T, Object> fn, Iterable<?> values) {
        this.current.andNotIn(fn, values);
        return this;
    }

    /**
     * 条件指定字段在值区间内
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param value1       区间起始值
     * @param value2       区间结束值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> between(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
        return useCondition ? between(fn, value1, value2) : this;
    }

    /**
     * 条件指定字段在动态值区间内
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param supplier1    区间起始值提供者
     * @param supplier2    区间结束值提供者
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> between(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier1,
            Supplier<Object> supplier2) {
        return useCondition ? between(fn, supplier1.get(), supplier2.get()) : this;
    }

    /**
     * 指定字段在值区间内
     *
     * @param fn     字段方法引用
     * @param value1 区间起始值
     * @param value2 区间结束值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> between(Fn<T, Object> fn, Object value1, Object value2) {
        this.current.andBetween(fn, value1, value2);
        return this;
    }

    /**
     * 条件指定字段不在值区间内
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param value1       区间起始值
     * @param value2       区间结束值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> notBetween(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
        return useCondition ? notBetween(fn, value1, value2) : this;
    }

    /**
     * 条件指定字段不在动态值区间内
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param supplier1    区间起始值提供者
     * @param supplier2    区间结束值提供者
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> notBetween(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier1,
            Supplier<Object> supplier2) {
        return useCondition ? notBetween(fn, supplier1.get(), supplier2.get()) : this;
    }

    /**
     * 指定字段不在值区间内
     *
     * @param fn     字段方法引用
     * @param value1 区间起始值
     * @param value2 区间结束值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> notBetween(Fn<T, Object> fn, Object value1, Object value2) {
        this.current.andNotBetween(fn, value1, value2);
        return this;
    }

    /**
     * 条件指定字段包含值（LIKE %值%）
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param value        值，两侧自动添加 %
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> contains(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? contains(fn, value) : this;
    }

    /**
     * 条件指定字段包含动态值（LIKE %值%）
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param supplier     值提供者，两侧自动添加 %
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> contains(boolean useCondition, Fn<T, Object> fn, Supplier<String> supplier) {
        return useCondition ? contains(fn, supplier.get()) : this;
    }

    /**
     * 指定字段包含值（LIKE %值%）
     *
     * @param fn    字段方法引用
     * @param value 值，两侧自动添加 %
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> contains(Fn<T, Object> fn, String value) {
        this.current.addCriterion(fn.toColumn() + " LIKE", Symbol.PERCENT + value + Symbol.PERCENT);
        return this;
    }

    /**
     * 条件指定字段以前缀值开头（LIKE 值%）
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param value        值，右侧自动添加 %
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> startsWith(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? startsWith(fn, value) : this;
    }

    /**
     * 条件指定字段以动态前缀值开头（LIKE 值%）
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param supplier     值提供者，右侧自动添加 %
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> startsWith(boolean useCondition, Fn<T, Object> fn, Supplier<String> supplier) {
        return useCondition ? startsWith(fn, supplier.get()) : this;
    }

    /**
     * 指定字段以前缀值开头（LIKE 值%）
     *
     * @param fn    字段方法引用
     * @param value 值，右侧自动添加 %
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> startsWith(Fn<T, Object> fn, String value) {
        this.current.addCriterion(fn.toColumn() + " LIKE", value + Symbol.PERCENT);
        return this;
    }

    /**
     * 条件指定字段以后缀值结尾（LIKE %值）
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param value        值，左侧自动添加 %
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> endsWith(boolean useCondition, Fn<T, Object> fn, String value) {
        return useCondition ? endsWith(fn, value) : this;
    }

    /**
     * 条件指定字段以动态后缀值结尾（LIKE %值）
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param supplier     值提供者，左侧自动添加 %
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> endsWith(boolean useCondition, Fn<T, Object> fn, Supplier<String> supplier) {
        return useCondition ? endsWith(fn, supplier.get()) : this;
    }

    /**
     * 指定字段以后缀值结尾（LIKE %值）
     *
     * @param fn    字段方法引用
     * @param value 值，左侧自动添加 %
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> endsWith(Fn<T, Object> fn, String value) {
        this.current.addCriterion(fn.toColumn() + " LIKE", Symbol.PERCENT + value);
        return this;
    }

    /**
     * 条件指定字段模糊匹配值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param value        值，需指定 % 或 _ 进行模糊匹配
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> like(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? like(fn, value) : this;
    }

    /**
     * 条件指定字段模糊匹配动态值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param supplier     值提供者，需指定 % 或 _ 进行模糊匹配
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> like(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? like(fn, supplier.get()) : this;
    }

    /**
     * 指定字段模糊匹配值
     *
     * @param fn    字段方法引用
     * @param value 值，需指定 % 或 _ 进行模糊匹配
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> like(Fn<T, Object> fn, Object value) {
        this.current.andLike(fn, value);
        return this;
    }

    /**
     * 条件指定字段不模糊匹配值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param value        值，需指定 % 进行模糊匹配
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> notLike(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? notLike(fn, value) : this;
    }

    /**
     * 条件指定字段不模糊匹配动态值
     *
     * @param useCondition 是否启用
     * @param fn           字段方法引用
     * @param supplier     值提供者，需指定 % 进行模糊匹配
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> notLike(boolean useCondition, Fn<T, Object> fn, Supplier<Object> supplier) {
        return useCondition ? notLike(fn, supplier.get()) : this;
    }

    /**
     * 指定字段不模糊匹配值
     *
     * @param fn    字段方法引用
     * @param value 值，需指定 % 进行模糊匹配
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> notLike(Fn<T, Object> fn, Object value) {
        this.current.andNotLike(fn, value);
        return this;
    }

    /**
     * 条件添加任意查询条件
     *
     * @param useCondition 是否启用
     * @param condition    自定义条件
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> anyCondition(boolean useCondition, String condition) {
        return useCondition ? anyCondition(condition) : this;
    }

    /**
     * 添加任意查询条件
     *
     * @param condition 自定义条件
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> anyCondition(String condition) {
        this.current.andCondition(condition);
        return this;
    }

    /**
     * 条件添加自定义条件和值
     *
     * @param useCondition 是否启用
     * @param condition    自定义条件，如 "length(column) ="
     * @param value        值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> anyCondition(boolean useCondition, String condition, Object value) {
        return useCondition ? anyCondition(condition, value) : this;
    }

    /**
     * 条件添加自定义条件和动态值
     *
     * @param useCondition 是否启用
     * @param condition    自定义条件，如 "length(column) ="
     * @param supplier     值提供者
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> anyCondition(boolean useCondition, String condition, Supplier<Object> supplier) {
        return useCondition ? anyCondition(condition, supplier.get()) : this;
    }

    /**
     * 添加自定义条件和值
     *
     * @param condition 自定义条件，如 "length(column) ="
     * @param value     值
     * @return 当前包装器对象
     */
    public ConditionWrapper<T, I> anyCondition(String condition, Object value) {
        this.current.andCondition(condition, value);
        return this;
    }

    /**
     * 嵌套 OR 查询，多个条件块以 OR 连接，块内为 AND
     *
     * @param orParts OR 条件块函数
     * @return 当前包装器对象
     */
    @SafeVarargs
    public final ConditionWrapper<T, I> or(Function<OrCriteria<T>, OrCriteria<T>>... orParts) {
        if (orParts != null && orParts.length > 0) {
            this.current.andOr(Arrays.stream(orParts).map(orPart -> orPart.apply(condition.orPart()))
                    .collect(Collectors.toList()));
        }
        return this;
    }

    /**
     * 根据当前条件删除记录
     *
     * @return 受影响的行数
     */
    public int delete() {
        return basicMapper.deleteByCondition(condition);
    }

    /**
     * 更新符合条件的记录为 SET 设置的值
     *
     * @return 受影响的行数
     */
    public int update() {
        Assert.notEmpty(condition.getSetValues(), "必须通过 set 方法设置更新的列和值");
        return basicMapper.updateByConditionSetValues(condition);
    }

    /**
     * 更新符合条件的记录为指定实体值
     *
     * @param t 实体对象
     * @return 受影响的行数
     */
    public int update(T t) {
        return basicMapper.updateByCondition(t, condition);
    }

    /**
     * 更新符合条件的记录为指定实体非空值
     *
     * @param t 实体对象
     * @return 受影响的行数
     */
    public int updateSelective(T t) {
        return basicMapper.updateByConditionSelective(t, condition);
    }

    /**
     * 查询符合条件的记录列表
     *
     * @return 实体对象列表
     */
    public List<T> list() {
        return basicMapper.selectByCondition(condition);
    }

    /**
     * 分页查询符合条件的记录
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 实体对象列表
     */
    public List<T> page(int pageNum, int pageSize) {
        return basicMapper.selectByCondition(condition, new RowBounds((pageNum - 1) * pageSize, pageSize));
    }

    /**
     * 偏移量查询符合条件的记录
     *
     * @param offset 偏移量
     * @param limit  限制数量
     * @return 实体对象列表
     */
    public List<T> offset(int offset, int limit) {
        return basicMapper.selectByCondition(condition, new RowBounds(offset, limit));
    }

    /**
     * 获取符合条件的记录游标
     *
     * @return 实体对象游标
     */
    public Cursor<T> cursor() {
        return basicMapper.selectCursorByCondition(condition);
    }

    /**
     * 查询符合条件的记录流
     *
     * @return 实体对象流
     */
    public Stream<T> stream() {
        return list().stream();
    }

    /**
     * 查询符合条件的唯一记录，若多条记录则抛出异常
     *
     * @return 可能为空的实体对象
     */
    public Optional<T> one() {
        return basicMapper.selectOneByCondition(condition);
    }

    /**
     * 查询符合条件的第一条记录
     *
     * @return 可能为空的实体对象
     */
    public Optional<T> first() {
        List<T> result = basicMapper.selectByCondition(condition, new RowBounds(0, 1));
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    /**
     * 查询符合条件的前 n 条记录
     *
     * @param n 记录数
     * @return 实体对象列表
     */
    public List<T> top(int n) {
        return basicMapper.selectByCondition(condition, new RowBounds(0, n));
    }

    /**
     * 查询符合条件的记录总数
     *
     * @return 记录总数
     */
    public long count() {
        return basicMapper.countByCondition(condition);
    }

}