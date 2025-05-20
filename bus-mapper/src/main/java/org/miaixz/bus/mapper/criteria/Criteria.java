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
import org.miaixz.bus.mapper.mapping.MapperColumn;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 查询条件类，用于构建复杂的 SQL 查询条件
 *
 * @param <T> 实体类类型
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
public class Criteria<T> {

    /**
     * 条件列表，存储所有的查询条件
     */
    protected List<Criterion> criteria;

    /**
     * 是否启用选择性条件，true 表示只使用非空值条件，false 表示使用所有条件
     */
    private boolean useSelective = false;

    /**
     * 默认构造函数，初始化条件列表
     */
    public Criteria() {
        super();
        this.criteria = new ArrayList<>();
    }

    /**
     * 带选择性条件的构造函数
     *
     * @param useSelective 是否启用选择性条件（非空校验）
     */
    public Criteria(boolean useSelective) {
        super();
        this.criteria = new ArrayList<>();
        this.useSelective = useSelective;
    }

    /**
     * 获取方法引用对应的列名
     *
     * @param fn 方法引用
     * @return 列名
     */
    public String column(Fn<T, Object> fn) {
        return fn.toColumn();
    }

    /**
     * 获取方法引用对应的类型处理器
     *
     * @param fn 方法引用
     * @return 类型处理器类
     */
    public Class<? extends TypeHandler> typehandler(Fn<T, Object> fn) {
        return fn.toEntityColumn().typeHandler();
    }

    /**
     * 判断是否使用该条件 如果 useSelective=false 则不开启条件值判空，使用该条件 如果 useSelective=true 且 条件值不为空，使用该条件
     *
     * @param obj 条件值
     * @return true 表示使用，false 表示不使用
     */
    public boolean useCriterion(Object obj) {
        return !useSelective || !ObjectKit.isEmpty(obj);
    }

    /**
     * 添加简单条件
     *
     * @param condition 条件表达式
     */
    public void addCriterion(String condition) {
        if (condition == null) {
            throw new RuntimeException("Value for condition cannot be null");
        }
        criteria.add(new Criterion(condition));
    }

    /**
     * 添加带单个值的条件
     *
     * @param condition 条件表达式
     * @param value     条件值
     */
    public void addCriterion(String condition, Object value) {
        if (value == null) {
            throw new RuntimeException("Value for " + condition + " cannot be null");
        }
        criteria.add(new Criterion(condition, value));
    }

    /**
     * 添加带单个值和列信息的条件
     *
     * @param condition 条件表达式
     * @param value     条件值
     * @param column    列信息
     */
    public void addCriterion(String condition, Object value, MapperColumn column) {
        if (value == null) {
            throw new RuntimeException("Value for " + condition + " cannot be null");
        }
        criteria.add(new Criterion(condition, value, column));
    }

    /**
     * 添加范围条件
     *
     * @param condition 条件表达式
     * @param value1    起始值
     * @param value2    结束值
     */
    public void addCriterion(String condition, Object value1, Object value2) {
        if (value1 == null || value2 == null) {
            throw new RuntimeException("Between values for " + condition + " cannot be null");
        }
        criteria.add(new Criterion(condition, value1, value2));
    }

    /**
     * 添加带列信息的范围条件
     *
     * @param condition 条件表达式
     * @param value1    起始值
     * @param value2    结束值
     * @param column    列信息
     */
    public void addCriterion(String condition, Object value1, Object value2, MapperColumn column) {
        if (value1 == null || value2 == null) {
            throw new RuntimeException("Between values for " + condition + " cannot be null");
        }
        criteria.add(new Criterion(condition, value1, value2, column));
    }

    /**
     * 条件添加字段为空的判断
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @return 当前条件对象
     */
    public Criteria<T> andIsNull(boolean useCondition, Fn<T, Object> fn) {
        return useCondition ? andIsNull(fn) : this;
    }

    /**
     * 添加字段为空的判断
     *
     * @param fn 方法引用
     * @return 当前条件对象
     */
    public Criteria<T> andIsNull(Fn<T, Object> fn) {
        addCriterion(column(fn) + " IS NULL");
        return this;
    }

    /**
     * 条件添加字段非空的判断
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @return 当前条件对象
     */
    public Criteria<T> andIsNotNull(boolean useCondition, Fn<T, Object> fn) {
        return useCondition ? andIsNotNull(fn) : this;
    }

    /**
     * 添加字段非空的判断
     *
     * @param fn 方法引用
     * @return 当前条件对象
     */
    public Criteria<T> andIsNotNull(Fn<T, Object> fn) {
        addCriterion(column(fn) + " IS NOT NULL");
        return this;
    }

    /**
     * 条件添加字段等于值的判断
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值
     * @return 当前条件对象
     */
    public Criteria<T> andEqualTo(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andEqualTo(fn, value) : this;
    }

    /**
     * 添加字段等于值的判断
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前条件对象
     */
    public Criteria<T> andEqualTo(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " =", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * 条件添加字段不等于值的判断
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值
     * @return 当前条件对象
     */
    public Criteria<T> andNotEqualTo(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andNotEqualTo(fn, value) : this;
    }

    /**
     * 添加字段不等于值的判断
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前条件对象
     */
    public Criteria<T> andNotEqualTo(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " <>", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * 条件添加字段大于值的判断
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值
     * @return 当前条件对象
     */
    public Criteria<T> andGreaterThan(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andGreaterThan(fn, value) : this;
    }

    /**
     * 添加字段大于值的判断
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前条件对象
     */
    public Criteria<T> andGreaterThan(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " >", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * 条件添加字段大于等于值的判断
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值
     * @return 当前条件对象
     */
    public Criteria<T> andGreaterThanOrEqualTo(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andGreaterThanOrEqualTo(fn, value) : this;
    }

    /**
     * 添加字段大于等于值的判断
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前条件对象
     */
    public Criteria<T> andGreaterThanOrEqualTo(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " >=", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * 条件添加字段小于值的判断
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值
     * @return 当前条件对象
     */
    public Criteria<T> andLessThan(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andLessThan(fn, value) : this;
    }

    /**
     * 添加字段小于值的判断
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前条件对象
     */
    public Criteria<T> andLessThan(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " <", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * 条件添加字段小于等于值的判断
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值
     * @return 当前条件对象
     */
    public Criteria<T> andLessThanOrEqualTo(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andLessThanOrEqualTo(fn, value) : this;
    }

    /**
     * 添加字段小于等于值的判断
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前条件对象
     */
    public Criteria<T> andLessThanOrEqualTo(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " <=", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * 条件添加字段在值集合中的判断
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param values       值集合
     * @return 当前条件对象
     */
    public Criteria<T> andIn(boolean useCondition, Fn<T, Object> fn, Iterable values) {
        return useCondition ? andIn(fn, values) : this;
    }

    /**
     * 添加字段在值集合中的判断
     *
     * @param fn     方法引用
     * @param values 值集合
     * @return 当前条件对象
     */
    public Criteria<T> andIn(Fn<T, Object> fn, Iterable values) {
        if (useCriterion(values)) {
            addCriterion(column(fn) + " IN", values, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * 条件添加字段不在值集合中的判断
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param values       值集合
     * @return 当前条件对象
     */
    public Criteria<T> andNotIn(boolean useCondition, Fn<T, Object> fn, Iterable values) {
        return useCondition ? andNotIn(fn, values) : this;
    }

    /**
     * 添加字段不在值集合中的判断
     *
     * @param fn     方法引用
     * @param values 值集合
     * @return 当前条件对象
     */
    public Criteria<T> andNotIn(Fn<T, Object> fn, Iterable values) {
        if (useCriterion(values)) {
            addCriterion(column(fn) + " NOT IN", values, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * 条件添加字段在值区间内的判断
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value1       起始值
     * @param value2       结束值
     * @return 当前条件对象
     */
    public Criteria<T> andBetween(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
        return useCondition ? andBetween(fn, value1, value2) : this;
    }

    /**
     * 添加字段在值区间内的判断
     *
     * @param fn     方法引用
     * @param value1 起始值
     * @param value2 结束值
     * @return 当前条件对象
     */
    public Criteria<T> andBetween(Fn<T, Object> fn, Object value1, Object value2) {
        if (useCriterion(value1) && useCriterion(value2)) {
            addCriterion(column(fn) + " BETWEEN", value1, value2, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * 条件添加字段不在值区间内的判断
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value1       起始值
     * @param value2       结束值
     * @return 当前条件对象
     */
    public Criteria<T> andNotBetween(boolean useCondition, Fn<T, Object> fn, Object value1, Object value2) {
        return useCondition ? andNotBetween(fn, value1, value2) : this;
    }

    /**
     * 添加字段不在值区间内的判断
     *
     * @param fn     方法引用
     * @param value1 起始值
     * @param value2 结束值
     * @return 当前条件对象
     */
    public Criteria<T> andNotBetween(Fn<T, Object> fn, Object value1, Object value2) {
        if (useCriterion(value1) && useCriterion(value2)) {
            addCriterion(column(fn) + " NOT BETWEEN", value1, value2, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * 条件添加字段模糊匹配的判断
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值
     * @return 当前条件对象
     */
    public Criteria<T> andLike(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andLike(fn, value) : this;
    }

    /**
     * 添加字段模糊匹配的判断
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前条件对象
     */
    public Criteria<T> andLike(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " LIKE", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * 条件添加字段非模糊匹配的判断
     *
     * @param useCondition 是否启用条件
     * @param fn           方法引用
     * @param value        值
     * @return 当前条件对象
     */
    public Criteria<T> andNotLike(boolean useCondition, Fn<T, Object> fn, Object value) {
        return useCondition ? andNotLike(fn, value) : this;
    }

    /**
     * 添加字段非模糊匹配的判断
     *
     * @param fn    方法引用
     * @param value 值
     * @return 当前条件对象
     */
    public Criteria<T> andNotLike(Fn<T, Object> fn, Object value) {
        if (useCriterion(value)) {
            addCriterion(column(fn) + " NOT LIKE", value, fn.toEntityColumn());
        }
        return this;
    }

    /**
     * 添加多个 OR 条件
     *
     * @param orCriteria1 第一个 OR 条件
     * @param orCriteria2 第二个 OR 条件
     * @param orCriterias 其他 OR 条件
     * @return 当前条件对象
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
     * 添加 OR 条件列表
     *
     * @param orCriteriaList OR 条件列表
     * @return 当前条件对象
     */
    public Criteria<T> andOr(List<OrCriteria<T>> orCriteriaList) {
        criteria.add(new Criterion(null, orCriteriaList));
        return this;
    }

    /**
     * 条件添加自定义条件
     *
     * @param useCondition 是否启用条件
     * @param condition    自定义条件，如 length(name) &lt; 5
     * @return 当前条件对象
     */
    public Criteria<T> andCondition(boolean useCondition, String condition) {
        return useCondition ? andCondition(condition) : this;
    }

    /**
     * 添加自定义条件
     *
     * @param condition 自定义条件，如 length(name) &lt; 5
     * @return 当前条件对象
     */
    public Criteria<T> andCondition(String condition) {
        addCriterion(condition);
        return this;
    }

    /**
     * 条件添加自定义条件和值
     *
     * @param useCondition 是否启用条件
     * @param condition    自定义条件，如 length(name)=
     * @param value        值
     * @return 当前条件对象
     */
    public Criteria<T> andCondition(boolean useCondition, String condition, Object value) {
        return useCondition ? andCondition(condition, value) : this;
    }

    /**
     * 添加自定义条件和值
     *
     * @param condition 自定义条件，如 length(name)=
     * @param value     值，如 5
     * @return 当前条件对象
     */
    public Criteria<T> andCondition(String condition, Object value) {
        criteria.add(new Criterion(condition, value));
        return this;
    }

    /**
     * 获取条件列表
     *
     * @return 条件列表
     */
    public List<Criterion> getCriteria() {
        return criteria;
    }

    /**
     * 判断条件是否有效
     *
     * @return true 表示有效，false 表示无效
     */
    public boolean isValid() {
        return criteria.size() > 0;
    }

}