/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.mapper.parsing.ColumnMeta;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Represents a single SQL query condition unit.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
public class Criterion {

    /**
     * The condition expression, e.g., "column = ".
     */
    private final String condition;

    /**
     * The value for the condition.
     */
    private Object value;

    /**
     * The second value for a 'between' condition.
     */
    private Object secondValue;

    /**
     * The name of the Java type.
     */
    private String javaType;

    /**
     * The name of the type handler.
     */
    private String typeHandler;

    /**
     * Indicates if this is a no-value condition.
     */
    private boolean noValue;

    /**
     * Indicates if this is a single-value condition.
     */
    private boolean singleValue;

    /**
     * Indicates if this is a between-value (range) condition.
     */
    private boolean betweenValue;

    /**
     * Indicates if this is a list-value condition.
     */
    private boolean listValue;

    /**
     * Indicates if this is an OR condition.
     */
    private boolean orValue;

    /**
     * Constructor for a no-value condition.
     *
     * @param condition The condition expression.
     */
    public Criterion(String condition) {
        super();
        this.condition = condition;
        this.noValue = true;
    }

    /**
     * Constructor for a single-value condition.
     *
     * @param condition The condition expression.
     * @param value     The value for the condition.
     */
    protected Criterion(String condition, Object value) {
        this(condition, value, null);
    }

    /**
     * Constructor for a single-value or list-value condition, associated with column metadata.
     *
     * @param condition The condition expression.
     * @param value     The value for the condition.
     * @param column    The column metadata.
     */
    public Criterion(String condition, Object value, ColumnMeta column) {
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
     * Constructor for a between-value (range) condition.
     *
     * @param condition   The condition expression.
     * @param value       The starting value.
     * @param secondValue The ending value.
     */
    protected Criterion(String condition, Object value, Object secondValue) {
        this(condition, value, secondValue, null);
    }

    /**
     * Constructor for a between-value (range) condition, associated with column metadata.
     *
     * @param condition   The condition expression.
     * @param value       The starting value.
     * @param secondValue The ending value.
     * @param column      The column metadata.
     */
    protected Criterion(String condition, Object value, Object secondValue, ColumnMeta column) {
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
     * Generates a MyBatis parameter placeholder string.
     *
     * @param field The parameter field name.
     * @return The placeholder string.
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
     * Gets the condition expression.
     *
     * @return The condition expression.
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Gets the second condition value.
     *
     * @return The second condition value.
     */
    public Object getSecondValue() {
        return secondValue;
    }

    /**
     * Gets the condition value.
     *
     * @return The condition value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Checks if this is a between-value condition.
     *
     * @return {@code true} if it is a between-value condition, {@code false} otherwise.
     */
    public boolean isBetweenValue() {
        return betweenValue;
    }

    /**
     * Checks if this is a list-value condition.
     *
     * @return {@code true} if it is a list-value condition, {@code false} otherwise.
     */
    public boolean isListValue() {
        return listValue;
    }

    /**
     * Checks if this is a no-value condition.
     *
     * @return {@code true} if it is a no-value condition, {@code false} otherwise.
     */
    public boolean isNoValue() {
        return noValue;
    }

    /**
     * Checks if this is a single-value condition.
     *
     * @return {@code true} if it is a single-value condition, {@code false} otherwise.
     */
    public boolean isSingleValue() {
        return singleValue;
    }

    /**
     * Checks if this is an OR condition.
     *
     * @return {@code true} if it is an OR condition, {@code false} otherwise.
     */
    public boolean isOrValue() {
        if (orValue && this.value instanceof Collection) {
            return ((Collection<?>) this.value).stream().filter(item -> item instanceof OrCriteria)
                    .map(OrCriteria.class::cast).anyMatch(Criteria::isValid);
        }
        return false;
    }

}
