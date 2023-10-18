/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org mybatis.io and other contributors.           *
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
package org.aoju.bus.mapper.builder;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.function.XFunction;
import org.aoju.bus.mapper.criteria.SqlCriteria;
import org.aoju.bus.mapper.entity.SqlsCriteria;
import org.aoju.bus.mapper.reflect.Reflector;

import java.util.Optional;

/**
 * sql 条件语句
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlCriteriaBuilder<T> implements SqlsCriteria {

    private SqlCriteria.Criteria criteria;

    private SqlCriteriaBuilder() {
        this.criteria = new SqlCriteria.Criteria();
    }

    public static <T> SqlCriteriaBuilder<T> custom(Class<T> clazz) {
        return new SqlCriteriaBuilder<>();
    }

    /**
     * AND column IS NULL
     *
     * @param fn 函数
     * @return the object
     */
    public SqlCriteriaBuilder<T> andIsNull(XFunction<T, Object> fn) {
        this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), "is null", "and"));
        return this;
    }

    /**
     * AND column IS NOT NULL
     *
     * @param fn 函数
     * @return the object
     */
    public SqlCriteriaBuilder<T> andIsNotNull(XFunction<T, Object> fn) {
        this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), "is not null", "and"));
        return this;
    }

    /**
     * AND column = value
     * 当value=null则不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> andEqualTo(XFunction<T, Object> fn, Object value) {
        return this.andEqualTo(fn, value, false);
    }

    /**
     * AND column = value
     *
     * @param fn       函数
     * @param value    值
     * @param required false 当value=null 则不参与查询 ;
     *                 true 当value = null 则转 is null 查询： AND column is null
     * @return the object
     */
    public SqlCriteriaBuilder<T> andEqualTo(XFunction<T, Object> fn, Object value, boolean required) {
        if (Optional.ofNullable(value).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "=", "and"));
        } else {
            if (required) {
                // null属性查询 转 is null
                this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), "is null", "and"));
            }
        }
        return this;
    }

    /**
     * AND column != value
     * 默认 value=null 则不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> andNotEqualTo(XFunction<T, Object> fn, Object value) {
        return this.andNotEqualTo(fn, value, false);
    }

    /**
     * AND column != value
     *
     * @param fn       函数
     * @param value    值
     * @param required false 当value=null 则不参与查询 ;
     *                 true 当value = null 则转 is not null 查询 ： AND column is not null
     * @return the object
     */
    public SqlCriteriaBuilder<T> andNotEqualTo(XFunction<T, Object> fn, Object value, boolean required) {
        if (Optional.ofNullable(value).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "<>", "and"));
        } else {
            if (required) {
                // 转非空查询
                this.andIsNotNull(fn);
            }
        }
        return this;
    }

    /**
     * AND column 大于 value
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> andGreaterThan(XFunction<T, Object> fn, Object value) {
        if (Optional.ofNullable(value).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, ">", "and"));
        }
        return this;
    }

    /**
     * AND  column 大于等于 value
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> andGreaterThanOrEqualTo(XFunction<T, Object> fn, Object value) {
        if (Optional.ofNullable(value).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, ">=", "and"));
        }
        return this;
    }

    /**
     * AND  column 小于 value
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> andLessThan(XFunction<T, Object> fn, Object value) {
        if (Optional.ofNullable(value).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "<", "and"));
        }
        return this;
    }

    /**
     * AND  column 小于等于 value
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> andLessThanOrEqualTo(XFunction<T, Object> fn, Object value) {
        if (Optional.ofNullable(value).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "<=", "and"));
        }
        return this;
    }

    /**
     * AND  column IN (#{item.value})
     * 当 values = null 则当前属性不参与查询
     *
     * @param fn     函数
     * @param values 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> andIn(XFunction<T, Object> fn, Iterable values) {
        if (Optional.ofNullable(values).isPresent() && values.iterator().hasNext()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), values, "in", "and"));
        }
        return this;
    }

    /**
     * AND  column NOT IN (#{item.value})
     * 当 values = null 则当前属性不参与查询
     *
     * @param fn     函数
     * @param values 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> andNotIn(XFunction<T, Object> fn, Iterable values) {
        if (Optional.ofNullable(values).isPresent() && values.iterator().hasNext()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), values, "not in", "and"));
        }
        return this;
    }

    /**
     * AND  column BETWEEN  value1 AND value2
     * 当 value1 或 value2 为空 则当前属性不参与查询
     *
     * @param fn     函数
     * @param value1 值1
     * @param value2 值2
     * @return the object
     */
    public SqlCriteriaBuilder<T> andBetween(XFunction<T, Object> fn, Object value1, Object value2) {
        if (Optional.ofNullable(value1).isPresent() && Optional.ofNullable(value2).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value1, value2, "between", "and"));
        }
        return this;
    }

    /**
     * AND column  NOT BETWEEN value1 AND value2
     * 当 value1 或 value2 为空 则当前属性不参与查询
     *
     * @param fn     函数
     * @param value1 值1
     * @param value2 值2
     * @return the object
     */
    public SqlCriteriaBuilder<T> andNotBetween(XFunction<T, Object> fn, Object value1, Object value2) {
        if (Optional.ofNullable(value1).isPresent() && Optional.ofNullable(value2).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value1, value2, "not between", "and"));
        }
        return this;
    }

    /**
     * AND column LIKE %value%
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> andLike(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            value = Symbol.PERCENT + value + Symbol.PERCENT;
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "like", "and"));
        }
        return this;
    }

    /**
     * AND column LIKE %value
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> andLikeLeft(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            value = Symbol.PERCENT + value;
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "like", "and"));
        }
        return this;
    }

    /**
     * AND column LIKE value%
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> andLikeRight(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            value = value + Symbol.PERCENT;
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "like", "and"));
        }
        return this;
    }

    /**
     * AND column NOT LIKE %value%
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> andNotLike(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            value = Symbol.PERCENT + value + Symbol.PERCENT;
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "not like", "and"));
        }
        return this;
    }

    /**
     * AND column NOT LIKE %value
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> andNotLikeLeft(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            value = Symbol.PERCENT + value + Symbol.PERCENT;
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "not like", "and"));
        }
        return this;
    }

    /**
     * AND column NOT LIKE value%
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> andNotLikeRight(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            value = value + Symbol.PERCENT;
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "not like", "and"));
        }
        return this;
    }

    /**
     * OR column IS NULL
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn 函数
     * @return the object
     */
    public SqlCriteriaBuilder<T> orIsNull(XFunction<T, Object> fn) {
        this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), "is null", "or"));
        return this;
    }

    /**
     * OR column IS NOT NULL
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn 函数
     * @return the object
     */
    public SqlCriteriaBuilder<T> orIsNotNull(XFunction<T, Object> fn) {
        this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), "is not null", "or"));
        return this;
    }

    /**
     * OR column = value
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> orEqualTo(XFunction<T, Object> fn, Object value) {
        return this.orEqualTo(fn, value, false);
    }

    /**
     * OR column = value
     * 当request = true 且  value = null时 转 #{@link #orIsNull(XFunction)}
     *
     * @param fn       函数
     * @param value    值
     * @param required 是否必须
     * @return the object
     */
    public SqlCriteriaBuilder<T> orEqualTo(XFunction<T, Object> fn, Object value, boolean required) {
        if (Optional.ofNullable(value).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "=", "or"));
        } else {
            if (required) {
                //转 or null
                this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), "is null", "or"));
            }
        }
        return this;
    }

    /**
     * OR column 不等于 value
     * 当value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> orNotEqualTo(XFunction<T, Object> fn, Object value) {
        return this.orNotEqualTo(fn, value, false);
    }

    /**
     * OR column 不等于 value
     * 当request = true 且  value = null时 转 #{@link #orIsNotNull(XFunction)}
     *
     * @param fn       函数
     * @param value    值
     * @param required 是否必须
     * @return the object
     */
    public SqlCriteriaBuilder<T> orNotEqualTo(XFunction<T, Object> fn, Object value, boolean required) {
        if (Optional.ofNullable(value).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "<>", "or"));
        } else {
            if (required) {
                this.orIsNotNull(fn);
            }
        }
        return this;
    }

    /**
     * OR column 大于 value
     * 当value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> orGreaterThan(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, ">", "or"));
        }
        return this;
    }

    /**
     * OR column 大于等于 value
     * 当value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> orGreaterThanOrEqualTo(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, ">=", "or"));
        }
        return this;
    }

    /**
     * OR column 小于 value
     * 当value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> orLessThan(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "<", "or"));
        }
        return this;
    }

    /**
     * OR column 小于等于 value
     * 当value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> orLessThanOrEqualTo(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "<=", "or"));
        }
        return this;
    }

    /**
     * OR column IN (#{item.value})
     * 当value = null 则当前属性不参与查询
     *
     * @param fn     函数
     * @param values 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> orIn(XFunction<T, Object> fn, Iterable values) {
        if (Optional.ofNullable(values).isPresent() && values.iterator().hasNext()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), values, "in", "or"));
        }
        return this;
    }

    /**
     * OR column NOT IN (#{item.value})
     * 当value = null 则当前属性不参与查询
     *
     * @param fn     函数
     * @param values 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> orNotIn(XFunction<T, Object> fn, Iterable values) {
        if (Optional.ofNullable(values).isPresent() && values.iterator().hasNext()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), values, "not in", "or"));
        }
        return this;
    }

    /**
     * OR column BETWEEN  value1 AND value2
     * 当 value1 或 value2 为空 则当前属性不参与查询
     *
     * @param fn     函数
     * @param value1 值1
     * @param value2 值2
     * @return the object
     */
    public SqlCriteriaBuilder<T> orBetween(XFunction<T, Object> fn, Object value1, Object value2) {
        if (Optional.ofNullable(value1).isPresent() && Optional.ofNullable(value2).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value1, value2, "between", "or"));
        }
        return this;
    }

    /**
     * OR column NOT BETWEEN  value1 AND value2
     * 当 value1 或 value2 为空 则当前属性不参与查询
     *
     * @param fn     函数
     * @param value1 值1
     * @param value2 值2
     * @return the object
     */
    public SqlCriteriaBuilder<T> orNotBetween(XFunction<T, Object> fn, Object value1, Object value2) {
        if (Optional.ofNullable(value1).isPresent() && Optional.ofNullable(value2).isPresent()) {
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value1, value2, "not between", "or"));
        }
        return this;
    }

    /**
     * OR column LIKE value
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> orLike(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            value = Symbol.PERCENT + value + Symbol.PERCENT;
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "like", "or"));
        }
        return this;
    }

    /**
     * OR column LIKE %value
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> orLikeLeft(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            value = Symbol.PERCENT + value;
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "like", "or"));
        }
        return this;
    }

    /**
     * OR column LIKE value%
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> orLikeRight(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            value = value + Symbol.PERCENT;
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "like", "or"));
        }
        return this;
    }

    /**
     * OR column NOT LIKE value
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> orNotLike(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            value = Symbol.PERCENT + value + Symbol.PERCENT;
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "not like", "or"));
        }
        return this;
    }

    /**
     * OR column NOT LIKE %value
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> orNotLikeLeft(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            value = Symbol.PERCENT + value;
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "not like", "or"));
        }
        return this;
    }

    /**
     * OR column NOT LIKE value%
     * 当 value = null 则当前属性不参与查询
     *
     * @param fn    函数
     * @param value 值
     * @return the object
     */
    public SqlCriteriaBuilder<T> orNotLikeRight(XFunction<T, Object> fn, String value) {
        if (Optional.ofNullable(value).isPresent()) {
            value = value + Symbol.PERCENT;
            this.criteria.getCriterions().add(new SqlCriteria.Criterion(Reflector.fnToFieldName(fn), value, "not like", "or"));
        }
        return this;
    }

    @Override
    public SqlCriteria.Criteria getCriteria() {
        return criteria;
    }

}
