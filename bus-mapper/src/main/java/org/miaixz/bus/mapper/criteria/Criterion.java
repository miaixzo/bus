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

import java.util.Collection;

import org.miaixz.bus.mapper.mapping.MapperColumn;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 查询条件单元类，表示单个 SQL 查询条件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
public class Criterion {

    /**
     * 条件表达式，如 "column ="
     */
    private final String condition;

    /**
     * 条件值
     */
    private Object value;

    /**
     * 范围条件的第二个值
     */
    private Object secondValue;

    /**
     * Java 类型名称
     */
    private String javaType;

    /**
     * 类型处理器名称
     */
    private String typeHandler;

    /**
     * 是否为无值条件
     */
    private boolean noValue;

    /**
     * 是否为单值条件
     */
    private boolean singleValue;

    /**
     * 是否为范围条件
     */
    private boolean betweenValue;

    /**
     * 是否为列表条件
     */
    private boolean listValue;

    /**
     * 是否为 OR 条件
     */
    private boolean orValue;

    /**
     * 构造函数，创建无值条件
     *
     * @param condition 条件表达式
     */
    public Criterion(String condition) {
        super();
        this.condition = condition;
        this.noValue = true;
    }

    /**
     * 构造函数，创建单值条件
     *
     * @param condition 条件表达式
     * @param value     条件值
     */
    protected Criterion(String condition, Object value) {
        this(condition, value, null);
    }

    /**
     * 构造函数，创建单值或列表条件，并关联列信息
     *
     * @param condition 条件表达式
     * @param value     条件值
     * @param column    列信息
     */
    public Criterion(String condition, Object value, MapperColumn column) {
        super();
        this.condition = condition;
        this.value = value;
        if (column != null) {
            Class<?> javaTypeClass = column.javaType();
            if (javaTypeClass != null) {
                this.javaType = javaTypeClass.getName();
            }
            if (column.typeHandler() != null) {
                this.typeHandler = column.typeHandler().getName();
            }
        }
        if (value instanceof Collection<?>) {
            if (condition != null) {
                this.listValue = true;
            } else {
                this.orValue = true;
            }
        } else {
            this.singleValue = true;
        }
    }

    /**
     * 构造函数，创建范围条件
     *
     * @param condition   条件表达式
     * @param value       起始值
     * @param secondValue 结束值
     */
    protected Criterion(String condition, Object value, Object secondValue) {
        this(condition, value, secondValue, null);
    }

    /**
     * 构造函数，创建范围条件，并关联列信息
     *
     * @param condition   条件表达式
     * @param value       起始值
     * @param secondValue 结束值
     * @param column      列信息
     */
    protected Criterion(String condition, Object value, Object secondValue, MapperColumn column) {
        super();
        this.condition = condition;
        this.value = value;
        this.secondValue = secondValue;
        if (column != null) {
            Class<?> javaTypeClass = column.javaType();
            if (javaTypeClass != null) {
                this.javaType = javaTypeClass.getName();
            }
            if (column.typeHandler() != null) {
                this.typeHandler = column.typeHandler().getName();
            }
        }
        this.betweenValue = true;
    }

    /**
     * 生成 MyBatis 参数占位符字符串
     *
     * @param field 参数字段名
     * @return 占位符字符串
     */
    public String variables(String field) {
        StringBuilder variables = new StringBuilder();
        variables.append("#{").append(field);
        if (javaType != null && !javaType.isEmpty()) {
            variables.append(",javaType=").append(javaType);
        }
        if (typeHandler != null && !typeHandler.isEmpty()) {
            variables.append(",typeHandler=").append(typeHandler);
        }
        return variables.append("}").toString();
    }

    /**
     * 获取条件表达式
     *
     * @return 条件表达式
     */
    public String getCondition() {
        return condition;
    }

    /**
     * 获取第二个条件值
     *
     * @return 第二个条件值
     */
    public Object getSecondValue() {
        return secondValue;
    }

    /**
     * 获取条件值
     *
     * @return 条件值
     */
    public Object getValue() {
        return value;
    }

    /**
     * 判断是否为范围条件
     *
     * @return true 表示是范围条件，false 表示不是
     */
    public boolean isBetweenValue() {
        return betweenValue;
    }

    /**
     * 判断是否为列表条件
     *
     * @return true 表示是列表条件，false 表示不是
     */
    public boolean isListValue() {
        return listValue;
    }

    /**
     * 判断是否为无值条件
     *
     * @return true 表示是无值条件，false 表示不是
     */
    public boolean isNoValue() {
        return noValue;
    }

    /**
     * 判断是否为单值条件
     *
     * @return true 表示是单值条件，false 表示不是
     */
    public boolean isSingleValue() {
        return singleValue;
    }

    /**
     * 判断是否为 OR 条件
     *
     * @return true 表示是 OR 条件，false 表示不是
     */
    public boolean isOrValue() {
        if (orValue && this.value instanceof Collection) {
            return ((Collection<?>) this.value).stream().filter(item -> item instanceof OrCriteria)
                    .map(OrCriteria.class::cast).anyMatch(Criteria::isValid);
        }
        return false;
    }

}