/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
package org.aoju.bus.mapper.entity;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.mapper.builder.Builder;
import org.aoju.bus.mapper.builder.EntityBuilder;
import org.aoju.bus.mapper.criteria.Assert;
import org.aoju.bus.mapper.criteria.Criteria;
import org.aoju.bus.mapper.criteria.OrderBy;

import java.util.*;

/**
 * 通用查询对象
 *
 * @author Kimi Liu
 * @version 5.9.0
 * @since JDK 1.8+
 */
public class Condition implements EntityTableName {

    protected String orderByClause;

    protected boolean distinct;

    protected boolean exists;

    protected boolean notNull;

    protected boolean forUpdate;

    //查询字段
    protected Set<String> selectColumns;

    //排除的查询字段
    protected Set<String> excludeColumns;

    protected String countColumn;

    protected List<Criteria> oredCriteria;

    protected Class<?> entityClass;

    protected EntityTable table;
    //属性和列对应
    protected Map<String, EntityColumn> propertyMap;
    //动态表名
    protected String tableName;

    protected OrderBy ORDERBY;

    /**
     * 默认exists为true
     *
     * @param clazz 对象
     */
    public Condition(Class<?> clazz) {
        this(clazz, true);
    }

    /**
     * 带exists参数的构造方法,默认notNull为false,允许为空
     *
     * @param clazz  对象
     * @param exists - true时,如果字段不存在就抛出异常,false时,如果不存在就不使用该字段的条件
     */
    public Condition(Class<?> clazz, boolean exists) {
        this(clazz, exists, false);
    }

    /**
     * 带exists参数的构造方法
     *
     * @param entityClass 对象
     * @param exists      - true时,如果字段不存在就抛出异常,false时,如果不存在就不使用该字段的条件
     * @param notNull     - true时,如果值为空,就会抛出异常,false时,如果为空就不使用该字段的条件
     */
    public Condition(Class<?> entityClass, boolean exists, boolean notNull) {
        this.exists = exists;
        this.notNull = notNull;
        oredCriteria = new ArrayList<>();
        this.entityClass = entityClass;
        table = EntityBuilder.getEntityTable(entityClass);
        propertyMap = table.getPropertyMap();
        this.ORDERBY = new OrderBy(this, propertyMap);
    }

    private Condition(Builder builder) {
        this.exists = builder.exists;
        this.notNull = builder.notNull;
        this.distinct = builder.distinct;
        this.entityClass = builder.entityClass;
        this.propertyMap = builder.propertyMap;
        this.selectColumns = builder.selectColumns;
        this.excludeColumns = builder.excludeColumns;
        this.oredCriteria = builder.criterias;
        this.forUpdate = builder.forUpdate;
        this.tableName = builder.tableName;

        if (!Assert.isEmpty(builder.orderByClause.toString())) {
            this.orderByClause = builder.orderByClause.toString();
        }
    }

    public static Builder builder(Class<?> entityClass) {
        return new Builder(entityClass);
    }

    public OrderBy orderBy(String property) {
        this.ORDERBY.orderBy(property);
        return this.ORDERBY;
    }

    /**
     * 排除查询字段,优先级低于 selectProperties
     *
     * @param properties 属性名的可变参数
     * @return Condition
     */
    public Condition excludeProperties(String... properties) {
        if (properties != null && properties.length > 0) {
            if (this.excludeColumns == null) {
                this.excludeColumns = new LinkedHashSet<>();
            }
            for (String property : properties) {
                if (propertyMap.containsKey(property)) {
                    this.excludeColumns.add(propertyMap.get(property).getColumn());
                } else {
                    throw new InstrumentException("类 " + entityClass.getSimpleName() + " 不包含属性 \'" + property + "\',或该属性被@Transient注释！");
                }
            }
        }
        return this;
    }

    /**
     * 指定要查询的属性列 - 这里会自动映射到表字段
     *
     * @param properties 属性名的可变参数
     * @return Condition
     */
    public Condition selectProperties(String... properties) {
        if (properties != null && properties.length > 0) {
            if (this.selectColumns == null) {
                this.selectColumns = new LinkedHashSet<>();
            }
            for (String property : properties) {
                if (propertyMap.containsKey(property)) {
                    this.selectColumns.add(propertyMap.get(property).getColumn());
                } else {
                    throw new InstrumentException("类 " + entityClass.getSimpleName() + " 不包含属性 \'" + property + "\',或该属性被@Transient注释！");
                }
            }
        }
        return this;
    }

    public void or(Criteria criteria) {
        criteria.setAndOr("or");
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        criteria.setAndOr("or");
        oredCriteria.add(criteria);
        return criteria;
    }

    public void and(Criteria criteria) {
        criteria.setAndOr("and");
        oredCriteria.add(criteria);
    }

    public Criteria and() {
        Criteria criteria = createCriteriaInternal();
        criteria.setAndOr("and");
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            criteria.setAndOr("and");
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria(propertyMap, exists, notNull);
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    public String getCountColumn() {
        return countColumn;
    }

    @Override
    public String getDynamicTableName() {
        return tableName;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public Set<String> getSelectColumns() {
        if (selectColumns != null && selectColumns.size() > 0) {
            //不需要处理
        } else if (excludeColumns != null && excludeColumns.size() > 0) {
            Collection<EntityColumn> entityColumns = propertyMap.values();
            selectColumns = new LinkedHashSet<>(entityColumns.size() - excludeColumns.size());
            for (EntityColumn column : entityColumns) {
                if (!excludeColumns.contains(column.getColumn())) {
                    selectColumns.add(column.getColumn());
                }
            }
        }
        return selectColumns;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isForUpdate() {
        return forUpdate;
    }

    public void setForUpdate(boolean forUpdate) {
        this.forUpdate = forUpdate;
    }

    /**
     * 指定 count(property) 查询属性
     *
     * @param property 属性
     */
    public void setCountProperty(String property) {
        if (propertyMap.containsKey(property)) {
            this.countColumn = propertyMap.get(property).getColumn();
        }
    }

    /**
     * 设置表名
     *
     * @param tableName 表名
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
