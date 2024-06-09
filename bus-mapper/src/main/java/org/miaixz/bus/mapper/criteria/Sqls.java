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

import org.miaixz.bus.core.lang.Symbol;

import java.util.ArrayList;
import java.util.List;

/**
 * Sql条件语句
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Sqls {

    private Criteria criteria;

    private Sqls() {
        this.criteria = new Criteria();
    }

    public static Sqls custom() {
        return new Sqls();
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public Sqls andIsNull(String property) {
        this.criteria.criterions.add(new Criterion(property, "is null", "and"));
        return this;
    }

    public Sqls andIsNotNull(String property) {
        this.criteria.criterions.add(new Criterion(property, "is not null", "and"));
        return this;
    }

    public Sqls andEqualTo(String property, Object value) {
        this.criteria.criterions.add(new Criterion(property, value, Symbol.EQUAL, "and"));
        return this;
    }

    public Sqls andNotEqualTo(String property, Object value) {
        this.criteria.criterions.add(new Criterion(property, value, "<>", "and"));
        return this;
    }

    public Sqls andGreaterThan(String property, Object value) {
        this.criteria.criterions.add(new Criterion(property, value, ">", "and"));
        return this;
    }

    public Sqls andGreaterThanOrEqualTo(String property, Object value) {
        this.criteria.criterions.add(new Criterion(property, value, ">=", "and"));
        return this;
    }


    public Sqls andLessThan(String property, Object value) {
        this.criteria.criterions.add(new Criterion(property, value, "<", "and"));
        return this;
    }

    public Sqls andLessThanOrEqualTo(String property, Object value) {
        this.criteria.criterions.add(new Criterion(property, value, "<=", "and"));
        return this;
    }

    public Sqls andIn(String property, Iterable values) {
        this.criteria.criterions.add(new Criterion(property, values, "in", "and"));
        return this;
    }

    public Sqls andNotIn(String property, Iterable values) {
        this.criteria.criterions.add(new Criterion(property, values, "not in", "and"));
        return this;
    }

    public Sqls andBetween(String property, Object value1, Object value2) {
        this.criteria.criterions.add(new Criterion(property, value1, value2, "between", "and"));
        return this;
    }

    public Sqls andNotBetween(String property, Object value1, Object value2) {
        this.criteria.criterions.add(new Criterion(property, value1, value2, "not between", "and"));
        return this;
    }

    public Sqls andLike(String property, String value) {
        this.criteria.criterions.add(new Criterion(property, value, "like", "and"));
        return this;
    }

    public Sqls andNotLike(String property, String value) {
        this.criteria.criterions.add(new Criterion(property, value, "not like", "and"));
        return this;
    }


    public Sqls orIsNull(String property) {
        this.criteria.criterions.add(new Criterion(property, "is null", "or"));
        return this;
    }

    public Sqls orIsNotNull(String property) {
        this.criteria.criterions.add(new Criterion(property, "is not null", "or"));
        return this;
    }


    public Sqls orEqualTo(String property, Object value) {
        this.criteria.criterions.add(new Criterion(property, value, Symbol.EQUAL, "or"));
        return this;
    }

    public Sqls orNotEqualTo(String property, Object value) {
        this.criteria.criterions.add(new Criterion(property, value, "<>", "or"));
        return this;
    }

    public Sqls orGreaterThan(String property, Object value) {
        this.criteria.criterions.add(new Criterion(property, value, ">", "or"));
        return this;
    }

    public Sqls orGreaterThanOrEqualTo(String property, Object value) {
        this.criteria.criterions.add(new Criterion(property, value, ">=", "or"));
        return this;
    }

    public Sqls orLessThan(String property, Object value) {
        this.criteria.criterions.add(new Criterion(property, value, "<", "or"));
        return this;
    }

    public Sqls orLessThanOrEqualTo(String property, Object value) {
        this.criteria.criterions.add(new Criterion(property, value, "<=", "or"));
        return this;
    }

    public Sqls orIn(String property, Iterable values) {
        this.criteria.criterions.add(new Criterion(property, values, "in", "or"));
        return this;
    }

    public Sqls orNotIn(String property, Iterable values) {
        this.criteria.criterions.add(new Criterion(property, values, "not in", "or"));
        return this;
    }

    public Sqls orBetween(String property, Object value1, Object value2) {
        this.criteria.criterions.add(new Criterion(property, value1, value2, "between", "or"));
        return this;
    }

    public Sqls orNotBetween(String property, Object value1, Object value2) {
        this.criteria.criterions.add(new Criterion(property, value1, value2, "not between", "or"));
        return this;
    }

    public Sqls orLike(String property, String value) {
        this.criteria.criterions.add(new Criterion(property, value, "like", "or"));
        return this;
    }

    public Sqls orNotLike(String property, String value) {
        this.criteria.criterions.add(new Criterion(property, value, "not like", "or"));
        return this;
    }

    public static class Criteria {

        private String andOr;
        private List<Criterion> criterions;

        public Criteria() {
            this.criterions = new ArrayList<>(2);
        }

        public List<Criterion> getCriterions() {
            return criterions;
        }

        public String getAndOr() {
            return andOr;
        }

        public void setAndOr(String andOr) {
            this.andOr = andOr;
        }
    }

    public static class Criterion {

        private String property;
        private Object value;
        private Object secondValue;
        private String condition;
        private String andOr;

        public Criterion(String property, String condition, String andOr) {
            this.property = property;
            this.condition = condition;
            this.andOr = andOr;
        }


        public Criterion(String property, Object value, String condition, String andOr) {
            this.property = property;
            this.value = value;
            this.condition = condition;
            this.andOr = andOr;
        }

        public Criterion(String property, Object value1, Object value2, String condition, String andOr) {
            this.property = property;
            this.value = value1;
            this.secondValue = value2;
            this.condition = condition;
            this.andOr = andOr;
        }

        public String getProperty() {
            return property;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public Object[] getValues() {
            if (value != null) {
                if (secondValue != null) {
                    return new Object[]{value, secondValue};
                } else {
                    return new Object[]{value};
                }
            } else {
                return new Object[]{};
            }
        }

        public String getCondition() {
            return condition;
        }

        public String getAndOr() {
            return andOr;
        }
    }

}