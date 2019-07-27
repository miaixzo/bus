package org.aoju.bus.mapper.criteria;

import java.util.Collection;

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
