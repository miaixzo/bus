/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.mapper.criteria;

import java.util.Collection;

/**
 * @author Kimi Liu
 * @version 5.3.2
 * @since JDK 1.8+
 */
public class Criterion {

    private String condition;
    private Object value;
    private Object secondValue;
    private String andOr;
    private boolean noValue;
    private boolean singleValue;
    private boolean betweenValue;
    private boolean listValue;
    private String typeHandler;

    protected Criterion(String condition) {
        this(condition, false);
    }

    protected Criterion(String condition, Object value, String typeHandler) {
        this(condition, value, typeHandler, false);
    }

    protected Criterion(String condition, Object value) {
        this(condition, value, null, false);
    }

    protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
        this(condition, value, secondValue, typeHandler, false);
    }

    protected Criterion(String condition, Object value, Object secondValue) {
        this(condition, value, secondValue, null, false);
    }

    protected Criterion(String condition, boolean isOr) {
        super();
        this.condition = condition;
        this.typeHandler = null;
        this.noValue = true;
        this.andOr = isOr ? "or" : "and";
    }

    protected Criterion(String condition, Object value, String typeHandler, boolean isOr) {
        super();
        this.condition = condition;
        this.value = value;
        this.typeHandler = typeHandler;
        this.andOr = isOr ? "or" : "and";
        if (value instanceof Collection<?>) {
            this.listValue = true;
        } else {
            this.singleValue = true;
        }
    }

    protected Criterion(String condition, Object value, boolean isOr) {
        this(condition, value, null, isOr);
    }

    protected Criterion(String condition, Object value, Object secondValue, String typeHandler, boolean isOr) {
        super();
        this.condition = condition;
        this.value = value;
        this.secondValue = secondValue;
        this.typeHandler = typeHandler;
        this.betweenValue = true;
        this.andOr = isOr ? "or" : "and";
    }

    protected Criterion(String condition, Object value, Object secondValue, boolean isOr) {
        this(condition, value, secondValue, null, isOr);
    }

    public String getAndOr() {
        return andOr;
    }

    public void setAndOr(String andOr) {
        this.andOr = andOr;
    }

    public String getCondition() {
        return condition;
    }

    public Object getSecondValue() {
        return secondValue;
    }

    public String getTypeHandler() {
        return typeHandler;
    }

    public Object getValue() {
        return value;
    }

    public boolean isBetweenValue() {
        return betweenValue;
    }

    public boolean isListValue() {
        return listValue;
    }

    public boolean isNoValue() {
        return noValue;
    }

    public boolean isSingleValue() {
        return singleValue;
    }
}
