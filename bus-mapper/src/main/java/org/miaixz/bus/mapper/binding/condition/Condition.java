/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org mapper.io and other contributors.         ~
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
package org.miaixz.bus.mapper.binding.condition;

import static org.miaixz.bus.mapper.Args.DELIMITER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.ORDER;
import org.miaixz.bus.mapper.binding.function.Fn;
import org.miaixz.bus.mapper.criteria.Criteria;
import org.miaixz.bus.mapper.criteria.Criterion;
import org.miaixz.bus.mapper.criteria.OrCriteria;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 通用的条件查询对象，用于构建复杂的查询条件
 *
 * @param <T> 实体类类型
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
public class Condition<T> {

    /**
     * 排序字段
     */
    protected String orderByClause;
    /**
     * 是否使用 DISTINCT 关键字
     */
    protected boolean distinct;
    /**
     * 指定查询的列
     */
    protected String selectColumns;
    /**
     * 指定查询的列，不带 column AS alias 别名
     */
    protected String simpleSelectColumns;
    /**
     * 起始 SQL，添加到查询 SQL 前，需注意防止 SQL 注入
     */
    protected String startSql;
    /**
     * 结尾 SQL，添加到查询 SQL 后，需注意防止 SQL 注入
     */
    protected String endSql;
    /**
     * 多组条件通过 OR 连接
     */
    protected List<Criteria<T>> oredCriteria;
    /**
     * 设置 UPDATE 语句的 SET 字段
     */
    protected List<Criterion> setValues;

    /**
     * 默认构造方法，初始化条件列表和 SET 值列表，禁止空条件操作全库
     */
    public Condition() {
        oredCriteria = new ArrayList<>();
        setValues = new ArrayList<>();
    }

    /**
     * 添加 OR 条件
     *
     * @param criteria 条件对象
     */
    public void or(Criteria<T> criteria) {
        oredCriteria.add(criteria);
    }

    /**
     * 创建并添加一组 OR 条件
     *
     * @return 新创建的条件对象
     */
    public Criteria<T> or() {
        Criteria<T> criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    /**
     * 创建独立的 OR 条件片段，不追加到当前条件
     *
     * @return OR 条件对象
     */
    public OrCriteria<T> orPart() {
        return new OrCriteria<>();
    }

    /**
     * 创建一组条件，首次调用时作为默认条件
     *
     * @return 新创建的条件对象
     */
    public Criteria<T> createCriteria() {
        Criteria<T> criteria = createCriteriaInternal();
        if (oredCriteria.isEmpty()) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * 创建一组选择性条件，首次调用时作为默认条件
     *
     * @return 新创建的选择性条件对象
     */
    public Criteria<T> createCriteriaSelective() {
        Criteria<T> criteria = new Criteria<>(true);
        if (oredCriteria.isEmpty()) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * 内部创建条件对象
     *
     * @return 新创建的条件对象
     */
    protected Criteria<T> createCriteriaInternal() {
        return new Criteria<>();
    }

    /**
     * 清除所有条件和设置
     */
    public void clear() {
        oredCriteria.clear();
        setValues.clear();
        orderByClause = null;
        distinct = false;
        selectColumns = null;
        simpleSelectColumns = null;
        startSql = null;
        endSql = null;
    }

    /**
     * 指定查询列，多次调用覆盖，清除排除列设置
     *
     * @param fns 方法引用数组
     * @return 当前条件对象
     */
    @SafeVarargs
    public final Condition<T> selectColumns(Fn<T, Object>... fns) {
        selectColumns = "";
        simpleSelectColumns = "";
        if (fns == null || fns.length == 0) {
            return this;
        }
        selectColumns(Arrays.stream(fns).map(Fn::toEntityColumn).collect(Collectors.toList()));
        return this;
    }

    /**
     * 内部设置查询列
     *
     * @param columns 查询列列表
     */
    private void selectColumns(List<ColumnMeta> columns) {
        StringBuilder sb = new StringBuilder(columns.size() * 16);
        StringBuilder simple = new StringBuilder(columns.size() * 16);
        for (ColumnMeta entityColumn : columns) {
            String column = entityColumn.column();
            String field = entityColumn.field().getName();
            if (sb.length() != 0) {
                sb.append(Symbol.COMMA);
                simple.append(Symbol.COMMA);
            }
            if (column.equals(field) || entityColumn.entityTable().useResultMaps()) {
                sb.append(column);
                simple.append(column);
            } else {
                Matcher matcher = DELIMITER.matcher(column);
                simple.append(column);
                if (matcher.find() && field.equals(matcher.group(1))) {
                    sb.append(column);
                } else {
                    sb.append(column).append(" AS ").append(field);
                }
            }
        }
        selectColumns = sb.toString();
        simpleSelectColumns = simple.toString();
    }

    /**
     * 排除指定查询列，清除已选列设置
     *
     * @param fns 方法引用数组
     * @return 当前条件对象
     */
    @SafeVarargs
    public final Condition<T> excludeColumns(Fn<T, Object>... fns) {
        selectColumns = "";
        simpleSelectColumns = "";
        if (fns == null || fns.length == 0) {
            return this;
        }
        TableMeta table = fns[0].toEntityColumn().entityTable();
        Set<String> excludeColumnSet = Arrays.stream(fns).map(Fn::toColumn).collect(Collectors.toSet());
        selectColumns(table.selectColumns().stream().filter(c -> !excludeColumnSet.contains(c.column()))
                .collect(Collectors.toList()));
        return this;
    }

    /**
     * 获取查询列
     *
     * @return 查询列字符串
     */
    public String getSelectColumns() {
        return selectColumns;
    }

    /**
     * 设置查询列
     *
     * @param selectColumns 查询列字符串
     * @return 当前条件对象
     */
    public Condition<T> setSelectColumns(String selectColumns) {
        this.selectColumns = selectColumns;
        return this;
    }

    /**
     * 获取不带别名的查询列
     *
     * @return 简单查询列字符串
     */
    public String getSimpleSelectColumns() {
        return simpleSelectColumns;
    }

    /**
     * 设置不带别名的查询列
     *
     * @param simpleSelectColumns 简单查询列字符串
     * @return 当前条件对象
     */
    public Condition<T> setSimpleSelectColumns(String simpleSelectColumns) {
        this.simpleSelectColumns = simpleSelectColumns;
        return this;
    }

    /**
     * 获取起始 SQL
     *
     * @return 起始 SQL 字符串
     */
    public String getStartSql() {
        return startSql;
    }

    /**
     * 设置起始 SQL
     *
     * @param startSql 起始 SQL 字符串，需防止 SQL 注入
     * @return 当前条件对象
     */
    public Condition<T> setStartSql(String startSql) {
        this.startSql = startSql;
        return this;
    }

    /**
     * 获取结尾 SQL
     *
     * @return 结尾 SQL 字符串
     */
    public String getEndSql() {
        return endSql;
    }

    /**
     * 设置结尾 SQL
     *
     * @param endSql 结尾 SQL 字符串，需防止 SQL 注入
     * @return 当前条件对象
     */
    public Condition<T> setEndSql(String endSql) {
        this.endSql = endSql;
        return this;
    }

    /**
     * 通过方法引用设置排序字段
     *
     * @param fn    排序列方法引用
     * @param order 排序方式（ASC/DESC）
     * @return 当前条件对象
     */
    public Condition<T> orderBy(Fn<T, Object> fn, String order) {
        if (orderByClause == null) {
            orderByClause = "";
        } else {
            orderByClause += ", ";
        }
        orderByClause += fn.toColumn() + Symbol.SPACE + order;
        return this;
    }

    /**
     * 设置非常规或字符串形式的排序，不覆盖已有排序
     *
     * @param orderByCondition 排序表达式，如 "status = 5 DESC"
     * @return 当前条件对象
     */
    public Condition<T> orderBy(String orderByCondition) {
        if (orderByCondition != null && !orderByCondition.isEmpty()) {
            if (orderByClause == null) {
                orderByClause = "";
            } else {
                orderByClause += ", ";
            }
            orderByClause += orderByCondition;
        }
        return this;
    }

    /**
     * 设置动态构造的非常规排序
     *
     * @param orderByCondition 排序表达式提供者，如 FIELD(id,3,1,2)
     * @return 当前条件对象
     */
    public Condition<T> orderBy(Supplier<String> orderByCondition) {
        return orderBy(orderByCondition.get());
    }

    /**
     * 通过方法引用设置升序排序
     *
     * @param fns 排序列方法引用数组
     * @return 当前条件对象
     */
    @SafeVarargs
    public final Condition<T> orderByAsc(Fn<T, Object>... fns) {
        if (fns != null && fns.length > 0) {
            for (Fn<T, Object> fn : fns) {
                orderBy(fn, ORDER.ASC);
            }
        }
        return this;
    }

    /**
     * 通过方法引用设置降序排序
     *
     * @param fns 排序列方法引用数组
     * @return 当前条件对象
     */
    @SafeVarargs
    public final Condition<T> orderByDesc(Fn<T, Object>... fns) {
        if (fns != null && fns.length > 0) {
            for (Fn<T, Object> fn : fns) {
                orderBy(fn, ORDER.DESC);
            }
        }
        return this;
    }

    /**
     * 获取排序字段
     *
     * @return 排序字段字符串
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * 设置排序字段
     *
     * @param orderByClause 排序字段字符串
     * @return 当前条件对象
     */
    public Condition<T> setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
        return this;
    }

    /**
     * 获取所有 OR 条件
     *
     * @return OR 条件列表
     */
    public List<Criteria<T>> getOredCriteria() {
        return oredCriteria;
    }

    /**
     * 获取 SET 值列表
     *
     * @return SET 值列表
     */
    public List<Criterion> getSetValues() {
        return setValues;
    }

    /**
     * 判断查询条件是否为空
     *
     * @return true 表示为空，false 表示非空
     */
    public boolean isEmpty() {
        if (oredCriteria.isEmpty()) {
            return true;
        }
        return oredCriteria.stream().allMatch(criteria -> criteria.getCriteria().isEmpty());
    }

    /**
     * 获取 DISTINCT 设置
     *
     * @return true 表示启用 DISTINCT，false 表示未启用
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * 设置 DISTINCT
     *
     * @param distinct true 启用 DISTINCT，false 不启用
     * @return 当前条件对象
     */
    public Condition<T> setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /**
     * 设置更新字段和值
     *
     * @param setSql SET 子句，如 "column = value"
     * @return 当前条件对象
     */
    public Condition<T> set(String setSql) {
        this.setValues.add(new Criterion(setSql));
        return this;
    }

    /**
     * 设置更新字段和值
     *
     * @param fn    字段方法引用
     * @param value 值
     * @return 当前条件对象
     */
    public Condition<T> set(Fn<T, Object> fn, Object value) {
        ColumnMeta column = fn.toEntityColumn();
        this.setValues.add(new Criterion(column.column(), value, column));
        return this;
    }

}