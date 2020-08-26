/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.mapper.criteria;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.mapper.entity.EntityColumn;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 6.0.8
 * @since JDK 1.8+
 */
public class Criteria {

    protected List<Criterion> criteria;
    //字段是否必须存在
    protected boolean exists;
    //值是否不能为空
    protected boolean notNull;
    //连接条件
    protected String andOr;
    //属性和列对应
    protected Map<String, EntityColumn> propertyMap;
    //模糊查询标识符
    protected String likePlaceholder = Symbol.PERCENT;

    public Criteria(Map<String, EntityColumn> propertyMap, boolean exists, boolean notNull) {
        this.exists = exists;
        this.notNull = notNull;
        criteria = new ArrayList<>();
        this.propertyMap = propertyMap;
    }

    private String column(String property) {
        if (propertyMap.containsKey(property)) {
            return propertyMap.get(property).getColumn();
        } else if (exists) {
            throw new InstrumentException("当前实体类不包含名为" + property + "的属性!");
        } else {
            return null;
        }
    }

    private String property(String property) {
        if (propertyMap.containsKey(property)) {
            return property;
        } else if (exists) {
            throw new InstrumentException("当前实体类不包含名为" + property + "的属性!");
        } else {
            return null;
        }
    }

    public void addCriterion(String condition) {
        if (condition == null) {
            throw new InstrumentException("Value for condition cannot be null");
        }
        if (condition.startsWith(Normal.NULL)) {
            return;
        }
        criteria.add(new Criterion(condition));
    }

    public void addCriterion(String condition, Object value, String property) {
        if (value == null) {
            if (notNull) {
                throw new InstrumentException("Value for " + property + " cannot be null");
            } else {
                return;
            }
        }
        if (property == null) {
            return;
        }
        criteria.add(new Criterion(condition, value));
    }

    public void addCriterion(String condition, Object value1, Object value2, String property) {
        if (value1 == null || value2 == null) {
            if (notNull) {
                throw new InstrumentException("Between values for " + property + " cannot be null");
            } else {
                return;
            }
        }
        if (property == null) {
            return;
        }
        criteria.add(new Criterion(condition, value1, value2));
    }

    public void addOrCriterion(String condition) {
        if (condition == null) {
            throw new InstrumentException("Value for condition cannot be null");
        }
        if (condition.startsWith(Normal.NULL)) {
            return;
        }
        criteria.add(new Criterion(condition, true));
    }

    public void addOrCriterion(String condition, Object value, String property) {
        if (value == null) {
            if (notNull) {
                throw new InstrumentException("Value for " + property + " cannot be null");
            } else {
                return;
            }
        }
        if (property == null) {
            return;
        }
        criteria.add(new Criterion(condition, value, true));
    }

    public void addOrCriterion(String condition, Object value1, Object value2, String property) {
        if (value1 == null || value2 == null) {
            if (notNull) {
                throw new InstrumentException("Between values for " + property + " cannot be null");
            } else {
                return;
            }
        }
        if (property == null) {
            return;
        }
        criteria.add(new Criterion(condition, value1, value2, true));
    }

    public Criteria andIsNull(String property) {
        addCriterion(column(property) + " is null");
        return this;
    }

    public Criteria andIsNotNull(String property) {
        addCriterion(column(property) + " is not null");
        return this;
    }

    public Criteria andEqualTo(String property, Object value) {
        addCriterion(column(property) + " =", value, property(property));
        return this;
    }

    public Criteria andNotEqualTo(String property, Object value) {
        addCriterion(column(property) + " <>", value, property(property));
        return this;
    }

    public Criteria andGreaterThan(String property, Object value) {
        addCriterion(column(property) + " >", value, property(property));
        return this;
    }

    public Criteria andGreaterThanOrEqualTo(String property, Object value) {
        addCriterion(column(property) + " >=", value, property(property));
        return this;
    }

    public Criteria andLessThan(String property, Object value) {
        addCriterion(column(property) + " <", value, property(property));
        return this;
    }

    public Criteria andLessThanOrEqualTo(String property, Object value) {
        addCriterion(column(property) + " <=", value, property(property));
        return this;
    }

    public Criteria andIn(String property, Iterable values) {
        addCriterion(column(property) + " in", values, property(property));
        return this;
    }

    public Criteria andNotIn(String property, Iterable values) {
        addCriterion(column(property) + " not in", values, property(property));
        return this;
    }

    public Criteria andBetween(String property, Object value1, Object value2) {
        addCriterion(column(property) + " between", value1, value2, property(property));
        return this;
    }

    public Criteria andNotBetween(String property, Object value1, Object value2) {
        addCriterion(column(property) + " not between", value1, value2, property(property));
        return this;
    }

    private String getLikeValue(String value) {
        if (null != value && !value.contains(likePlaceholder)) {
            return likePlaceholder + value + likePlaceholder;
        }
        return value;
    }

    public Criteria andLike(String property, String value) {
        addCriterion(column(property) + "  like", getLikeValue(value), property(property));
        return this;
    }

    public Criteria andBeforeLike(String property, String value) {
        return andLike(property, likePlaceholder + value);
    }

    public Criteria andAfterLike(String property, String value) {
        return andLike(property, value + likePlaceholder);
    }

    public Criteria andNotLike(String property, String value) {
        addCriterion(column(property) + "  not like", getLikeValue(value), property(property));
        return this;
    }

    public Criteria andNotBeforeLike(String property, String value) {
        return andNotLike(property, likePlaceholder + value);
    }

    public Criteria andNotAfterLike(String property, String value) {
        return andNotLike(property, value + likePlaceholder);
    }

    /**
     * 手写条件
     *
     * @param condition 例如 "length(countryname)小于5"
     * @return the Criteria
     */
    public Criteria andCondition(String condition) {
        addCriterion(condition);
        return this;
    }

    /**
     * 手写左边条件,右边用value值
     *
     * @param condition 例如 "length(countryname)="
     * @param value     例如 5
     * @return the Criteria
     */
    public Criteria andCondition(String condition, Object value) {
        criteria.add(new Criterion(condition, value));
        return this;
    }

    /**
     * 将此对象的不为空的字段参数作为相等查询条件
     *
     * @param param 参数对象
     * @return the Criteria
     */
    public Criteria andEqualTo(Object param) {
        MetaObject metaObject = SystemMetaObject.forObject(param);
        String[] properties = metaObject.getGetterNames();
        for (String property : properties) {
            //属性和列对应Map中有此属性
            if (propertyMap.get(property) != null) {
                Object value = metaObject.getValue(property);
                //属性值不为空
                if (value != null) {
                    andEqualTo(property, value);
                }
            }
        }
        return this;
    }

    /**
     * 将此对象的所有字段参数作为相等查询条件,如果字段为 null,则为 is null
     *
     * @param param 参数对象
     * @return the Criteria
     */
    public Criteria andAllEqualTo(Object param) {
        MetaObject metaObject = SystemMetaObject.forObject(param);
        String[] properties = metaObject.getGetterNames();
        for (String property : properties) {
            //属性和列对应Map中有此属性
            if (propertyMap.get(property) != null) {
                Object value = metaObject.getValue(property);
                //属性值不为空
                if (value != null) {
                    andEqualTo(property, value);
                } else {
                    andIsNull(property);
                }
            }
        }
        return this;
    }

    public Criteria orIsNull(String property) {
        addOrCriterion(column(property) + " is null");
        return this;
    }

    public Criteria orIsNotNull(String property) {
        addOrCriterion(column(property) + " is not null");
        return this;
    }

    public Criteria orEqualTo(String property, Object value) {
        addOrCriterion(column(property) + " =", value, property(property));
        return this;
    }

    public Criteria orNotEqualTo(String property, Object value) {
        addOrCriterion(column(property) + " <>", value, property(property));
        return this;
    }

    public Criteria orGreaterThan(String property, Object value) {
        addOrCriterion(column(property) + " >", value, property(property));
        return this;
    }

    public Criteria orGreaterThanOrEqualTo(String property, Object value) {
        addOrCriterion(column(property) + " >=", value, property(property));
        return this;
    }

    public Criteria orLessThan(String property, Object value) {
        addOrCriterion(column(property) + " <", value, property(property));
        return this;
    }

    public Criteria orLessThanOrEqualTo(String property, Object value) {
        addOrCriterion(column(property) + " <=", value, property(property));
        return this;
    }

    public Criteria orIn(String property, Iterable values) {
        addOrCriterion(column(property) + " in", values, property(property));
        return this;
    }

    public Criteria orNotIn(String property, Iterable values) {
        addOrCriterion(column(property) + " not in", values, property(property));
        return this;
    }

    public Criteria orBetween(String property, Object value1, Object value2) {
        addOrCriterion(column(property) + " between", value1, value2, property(property));
        return this;
    }

    public Criteria orNotBetween(String property, Object value1, Object value2) {
        addOrCriterion(column(property) + " not between", value1, value2, property(property));
        return this;
    }

    public Criteria orLike(String property, String value) {
        addOrCriterion(column(property) + "  like", getLikeValue(value), property(property));
        return this;
    }

    public Criteria orBeforeLike(String property, String value) {
        return orLike(property, likePlaceholder + value);
    }

    public Criteria orAfterLike(String property, String value) {
        return orLike(property, value + likePlaceholder);
    }

    public Criteria orNotLike(String property, String value) {
        addOrCriterion(column(property) + "  not like", getLikeValue(value), property(property));
        return this;
    }

    public Criteria orNotBeforeLike(String property, String value) {
        return orLike(property, likePlaceholder + value);
    }

    public Criteria orNotAfterLike(String property, String value) {
        return orLike(property, value + likePlaceholder);
    }

    /**
     * 手写条件
     *
     * @param condition 例如 "length(countryname)小于5"
     * @return the Criteria
     */
    public Criteria orCondition(String condition) {
        addOrCriterion(condition);
        return this;
    }

    /**
     * 手写左边条件,右边用value值
     *
     * @param condition 例如 "length(countryname)="
     * @param value     例如 5
     * @return the Criteria
     */
    public Criteria orCondition(String condition, Object value) {
        criteria.add(new Criterion(condition, value, true));
        return this;
    }

    /**
     * 将此对象的不为空的字段参数作为相等查询条件
     *
     * @param param 参数对象
     * @return the Criteria
     */
    public Criteria orEqualTo(Object param) {
        MetaObject metaObject = SystemMetaObject.forObject(param);
        String[] properties = metaObject.getGetterNames();
        for (String property : properties) {
            //属性和列对应Map中有此属性
            if (propertyMap.get(property) != null) {
                Object value = metaObject.getValue(property);
                //属性值不为空
                if (value != null) {
                    orEqualTo(property, value);
                }
            }
        }
        return this;
    }

    /**
     * 将此对象的所有字段参数作为相等查询条件,如果字段为 null,则为 is null
     *
     * @param param 参数对象
     * @return the Criteria
     */
    public Criteria orAllEqualTo(Object param) {
        MetaObject metaObject = SystemMetaObject.forObject(param);
        String[] properties = metaObject.getGetterNames();
        for (String property : properties) {
            //属性和列对应Map中有此属性
            if (propertyMap.get(property) != null) {
                Object value = metaObject.getValue(property);
                //属性值不为空
                if (value != null) {
                    orEqualTo(property, value);
                } else {
                    orIsNull(property);
                }
            }
        }
        return this;
    }

    public List<Criterion> getAllCriteria() {
        return criteria;
    }

    public String getAndOr() {
        return andOr;
    }

    public void setAndOr(String andOr) {
        this.andOr = andOr;
    }

    public List<Criterion> getCriteria() {
        return criteria;
    }

    public boolean isValid() {
        return criteria.size() > 0;
    }

}
