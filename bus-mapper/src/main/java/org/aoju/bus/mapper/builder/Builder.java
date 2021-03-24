/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.mapper.criteria.Assert;
import org.aoju.bus.mapper.criteria.Criteria;
import org.aoju.bus.mapper.criteria.SqlsCriteria;
import org.aoju.bus.mapper.entity.Condition;
import org.aoju.bus.mapper.entity.EntityColumn;
import org.aoju.bus.mapper.entity.EntityTable;

import java.util.*;

public class Builder {

    public final Class<?> entityClass;
    public EntityTable table;
    //属性和列对应
    public Map<String, EntityColumn> propertyMap;
    public StringBuilder orderByClause;
    public boolean distinct;
    public boolean exists;
    public boolean notNull;
    public boolean forUpdate;
    //查询字段
    public Set<String> selectColumns;
    //排除的查询字段
    public Set<String> excludeColumns;
    public String countColumn;
    public List<Assert.Criteria> sqlsCriteria;
    //动态表名
    public List<Criteria> criterias;
    //动态表名
    public String tableName;

    public Builder(Class<?> entityClass) {
        this(entityClass, true);
    }

    public Builder(Class<?> entityClass, boolean exists) {
        this(entityClass, exists, false);
    }

    public Builder(Class<?> entityClass, boolean exists, boolean notNull) {
        this.entityClass = entityClass;
        this.exists = exists;
        this.notNull = notNull;
        this.orderByClause = new StringBuilder();
        this.table = EntityBuilder.getEntityTable(entityClass);
        this.propertyMap = table.getPropertyMap();
        this.sqlsCriteria = new ArrayList<>(2);
    }

    public Builder distinct() {
        return setDistinct(true);
    }

    public Builder forUpdate() {
        return setForUpdate(true);
    }

    public Builder selectDistinct(String... properties) {
        select(properties);
        this.distinct = true;
        return this;
    }

    public Builder select(String... properties) {
        if (null != properties && properties.length > 0) {
            if (this.selectColumns == null) {
                this.selectColumns = new LinkedHashSet<>();
            }
            for (String property : properties) {
                if (this.propertyMap.containsKey(property)) {
                    this.selectColumns.add(propertyMap.get(property).getColumn());
                } else {
                    throw new InstrumentException("当前实体类不包含名为" + property + "的属性!");
                }
            }
        }
        return this;
    }

    public Builder notSelect(String... properties) {
        if (null != properties && properties.length > 0) {
            if (this.excludeColumns == null) {
                this.excludeColumns = new LinkedHashSet<>();
            }
            for (String property : properties) {
                if (propertyMap.containsKey(property)) {
                    this.excludeColumns.add(propertyMap.get(property).getColumn());
                } else {
                    throw new InstrumentException("当前实体类不包含名为" + property + "的属性!");
                }
            }
        }
        return this;
    }

    public Builder from(String tableName) {
        return setTableName(tableName);
    }

    public Builder where(Assert anAssert) {
        Assert.Criteria criteria = anAssert.getCriteria();
        criteria.setAndOr("and");
        this.sqlsCriteria.add(criteria);
        return this;
    }

    public Builder where(SqlsCriteria sqlsCriteria) {
        Assert.Criteria criteria = sqlsCriteria.getCriteria();
        criteria.setAndOr("and");
        this.sqlsCriteria.add(criteria);
        return this;
    }

    public Builder andWhere(Assert anAssert) {
        Assert.Criteria criteria = anAssert.getCriteria();
        criteria.setAndOr("and");
        this.sqlsCriteria.add(criteria);
        return this;
    }

    public Builder andWhere(SqlsCriteria sqlsCriteria) {
        Assert.Criteria criteria = sqlsCriteria.getCriteria();
        criteria.setAndOr("and");
        this.sqlsCriteria.add(criteria);
        return this;
    }

    public Builder orWhere(Assert anAssert) {
        Assert.Criteria criteria = anAssert.getCriteria();
        criteria.setAndOr("or");
        this.sqlsCriteria.add(criteria);
        return this;
    }

    public Builder orWhere(SqlsCriteria sqlsCriteria) {
        Assert.Criteria criteria = sqlsCriteria.getCriteria();
        criteria.setAndOr("or");
        this.sqlsCriteria.add(criteria);
        return this;
    }

    public Builder orderBy(String... properties) {
        return orderByAsc(properties);
    }

    public Builder orderByAsc(String... properties) {
        contactOrderByClause(" Asc", properties);
        return this;
    }

    public Builder orderByDesc(String... properties) {
        contactOrderByClause(" Desc", properties);
        return this;
    }

    private void contactOrderByClause(String order, String... properties) {
        StringBuilder columns = new StringBuilder();
        for (String property : properties) {
            String column;
            if (null != (column = propertyforOderBy(property))) {
                columns.append(Symbol.COMMA).append(column);
            }
        }
        columns.append(order);
        if (columns.length() > 0) {
            orderByClause.append(columns);
        }
    }

    public Condition build() {
        this.criterias = new ArrayList<>();
        for (Assert.Criteria criteria : sqlsCriteria) {
            Criteria criteriat = new Criteria(this.propertyMap, this.exists, this.notNull);
            criteriat.setAndOr(criteria.getAndOr());
            for (Assert.Criterion criterion : criteria.getCriterions()) {
                String condition = criterion.getCondition();
                String andOr = criterion.getAndOr();
                String property = criterion.getProperty();
                Object[] values = criterion.getValues();
                transformCriterion(criteriat, condition, property, values, andOr);
            }
            criterias.add(criteriat);
        }

        if (this.orderByClause.length() > 0) {
            this.orderByClause = new StringBuilder(this.orderByClause.substring(1, this.orderByClause.length()));
        }
        return new Condition(this.entityClass);
    }

    private void transformCriterion(Criteria criteria, String condition, String property, Object[] values, String andOr) {
        if (values.length == 0) {
            if ("and".equals(andOr)) {
                criteria.addCriterion(column(property) + Symbol.SPACE + condition);
            } else {
                criteria.addOrCriterion(column(property) + Symbol.SPACE + condition);
            }
        } else if (values.length == 1) {
            if ("and".equals(andOr)) {
                criteria.addCriterion(column(property) + Symbol.SPACE + condition, values[0], property(property));
            } else {
                criteria.addOrCriterion(column(property) + Symbol.SPACE + condition, values[0], property(property));
            }
        } else if (values.length == 2) {
            if ("and".equals(andOr)) {
                criteria.addCriterion(column(property) + Symbol.SPACE + condition, values[0], values[1], property(property));
            } else {
                criteria.addOrCriterion(column(property) + Symbol.SPACE + condition, values[0], values[1], property(property));
            }
        }
    }

    private String column(String property) {
        if (propertyMap.containsKey(property)) {
            return propertyMap.get(property).getColumn();
        } else if (exists) {
            throw new InstrumentException("当前实体类不包含名为" + property + "的属性!");
        }
        return null;
    }

    private String property(String property) {
        if (propertyMap.containsKey(property)) {
            return property;
        } else if (exists) {
            throw new InstrumentException("当前实体类不包含名为" + property + "的属性!");
        }
        return null;
    }

    private String propertyforOderBy(String property) {
        if (Assert.isEmpty(property) || Assert.isEmpty(property.trim())) {
            throw new InstrumentException("接收的property为空！");
        }
        property = property.trim();
        if (!propertyMap.containsKey(property)) {
            throw new InstrumentException("当前实体类不包含名为" + property + "的属性!");
        }
        return propertyMap.get(property).getColumn();
    }

    public Builder setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public Builder setForUpdate(boolean forUpdate) {
        this.forUpdate = forUpdate;
        return this;
    }

    public Builder setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

}
