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
import org.miaixz.bus.mapper.Order;
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
 * A generic condition query object for building complex query criteria.
 *
 * @param <T> The type of the entity class.
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
public class Condition<T> {

    /**
     * The Order BY clause for the query.
     */
    protected String orderByClause;
    /**
     * Whether to use the DISTINCT keyword in the query.
     */
    protected boolean distinct;
    /**
     * The specific columns to be selected in the query.
     */
    protected String selectColumns;
    /**
     * The specific columns to be selected, without "column AS alias" aliases.
     */
    protected String simpleSelectColumns;
    /**
     * SQL to be prepended to the main query.
     */
    protected String startSql;
    /**
     * SQL to be appended to the main query.
     */
    protected String endSql;
    /**
     * A list of criteria groups connected by OR.
     */
    protected List<Criteria<T>> oredCriteria;
    /**
     * A list of fields to be set in an UPDATE statement.
     */
    protected List<Criterion> setValues;

    /**
     * Default constructor. Initializes the criteria list and set-values list.
     */
    public Condition() {
        oredCriteria = new ArrayList<>();
        setValues = new ArrayList<>();
    }

    /**
     * Adds a criteria group with an OR condition.
     *
     * @param criteria The criteria object to add.
     */
    public void or(Criteria<T> criteria) {
        oredCriteria.add(criteria);
    }

    /**
     * Creates and adds a new criteria group with an OR condition.
     *
     * @return The newly created criteria object.
     */
    public Criteria<T> or() {
        Criteria<T> criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    /**
     * Creates a standalone OR criteria fragment, which is not appended to the current condition.
     *
     * @return An {@link OrCriteria} object.
     */
    public OrCriteria<T> orPart() {
        return new OrCriteria<>();
    }

    /**
     * Creates a criteria group. If it is the first one, it becomes the default criteria.
     *
     * @return The newly created criteria object.
     */
    public Criteria<T> createCriteria() {
        Criteria<T> criteria = createCriteriaInternal();
        if (oredCriteria.isEmpty()) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * Creates a selective criteria group. If it is the first one, it becomes the default criteria.
     *
     * @return The newly created selective criteria object.
     */
    public Criteria<T> createCriteriaSelective() {
        Criteria<T> criteria = new Criteria<>(true);
        if (oredCriteria.isEmpty()) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * Internal method to create a new criteria object.
     *
     * @return The newly created criteria object.
     */
    protected Criteria<T> createCriteriaInternal() {
        return new Criteria<>();
    }

    /**
     * Clears all conditions, settings, and clauses.
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
     * Specifies the columns to be selected. Subsequent calls will overwrite previous selections and clear any excluded
     * column settings.
     *
     * @param fns An array of method references representing the columns to select.
     * @return The current {@link Condition} object.
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
     * Internal method to set the select columns from a list of {@link ColumnMeta}.
     *
     * @param columns The list of columns to select.
     */
    private void selectColumns(List<ColumnMeta> columns) {
        StringBuilder sb = new StringBuilder(columns.size() * 16);
        StringBuilder simple = new StringBuilder(columns.size() * 16);
        for (ColumnMeta entityColumn : columns) {
            String column = entityColumn.column();
            String field = entityColumn.fieldMeta().getName();
            if (sb.length() != 0) {
                sb.append(Symbol.COMMA);
                simple.append(Symbol.COMMA);
            }
            if (column.equals(field) || entityColumn.tableMeta().useResultMaps()) {
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
     * Excludes specified columns from the selection. This will clear any previously selected columns.
     *
     * @param fns An array of method references representing the columns to exclude.
     * @return The current {@link Condition} object.
     */
    @SafeVarargs
    public final Condition<T> excludeColumns(Fn<T, Object>... fns) {
        selectColumns = "";
        simpleSelectColumns = "";
        if (fns == null || fns.length == 0) {
            return this;
        }
        TableMeta table = fns[0].toEntityColumn().tableMeta();
        Set<String> excludeColumnSet = Arrays.stream(fns).map(Fn::toColumn).collect(Collectors.toSet());
        selectColumns(
                table.selectColumns().stream().filter(c -> !excludeColumnSet.contains(c.column()))
                        .collect(Collectors.toList()));
        return this;
    }

    /**
     * Gets the string of selected columns.
     *
     * @return The selected columns string.
     */
    public String getSelectColumns() {
        return selectColumns;
    }

    /**
     * Sets the string of selected columns.
     *
     * @param selectColumns The string of columns to select.
     * @return The current {@link Condition} object.
     */
    public Condition<T> setSelectColumns(String selectColumns) {
        this.selectColumns = selectColumns;
        return this;
    }

    /**
     * Gets the string of selected columns without aliases.
     *
     * @return The simple selected columns string.
     */
    public String getSimpleSelectColumns() {
        return simpleSelectColumns;
    }

    /**
     * Sets the string of selected columns without aliases.
     *
     * @param simpleSelectColumns The string of simple columns to select.
     * @return The current {@link Condition} object.
     */
    public Condition<T> setSimpleSelectColumns(String simpleSelectColumns) {
        this.simpleSelectColumns = simpleSelectColumns;
        return this;
    }

    /**
     * Gets the starting SQL fragment.
     *
     * @return The starting SQL string.
     */
    public String getStartSql() {
        return startSql;
    }

    /**
     * Sets the starting SQL fragment.
     *
     * @param startSql The starting SQL string.
     * @return The current {@link Condition} object.
     */
    public Condition<T> setStartSql(String startSql) {
        this.startSql = startSql;
        return this;
    }

    /**
     * Gets the ending SQL fragment.
     *
     * @return The ending SQL string.
     */
    public String getEndSql() {
        return endSql;
    }

    /**
     * Sets the ending SQL fragment.
     *
     * @param endSql The ending SQL string.
     * @return The current {@link Condition} object.
     */
    public Condition<T> setEndSql(String endSql) {
        this.endSql = endSql;
        return this;
    }

    /**
     * Adds an Order BY clause using a method reference.
     *
     * @param fn    A method reference to the column to order by.
     * @param order The sort order ("ASC" or "DESC").
     * @return The current {@link Condition} object.
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
     * Adds a raw string Order BY clause. This does not overwrite existing clauses.
     *
     * @param orderByCondition The sorting expression (e.g., "status = 5 DESC").
     * @return The current {@link Condition} object.
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
     * Adds a dynamically constructed, unconventional Order BY clause.
     *
     * @param orderByCondition A supplier for the sorting expression (e.g., FIELD(id,3,1,2)).
     * @return The current {@link Condition} object.
     */
    public Condition<T> orderBy(Supplier<String> orderByCondition) {
        return orderBy(orderByCondition.get());
    }

    /**
     * Adds an ascending Order BY clause for the specified columns using method references.
     *
     * @param fns An array of method references to the columns.
     * @return The current {@link Condition} object.
     */
    @SafeVarargs
    public final Condition<T> orderByAsc(Fn<T, Object>... fns) {
        if (fns != null && fns.length > 0) {
            for (Fn<T, Object> fn : fns) {
                orderBy(fn, Order.ASC);
            }
        }
        return this;
    }

    /**
     * Adds a descending Order BY clause for the specified columns using method references.
     *
     * @param fns An array of method references to the columns.
     * @return The current {@link Condition} object.
     */
    @SafeVarargs
    public final Condition<T> orderByDesc(Fn<T, Object>... fns) {
        if (fns != null && fns.length > 0) {
            for (Fn<T, Object> fn : fns) {
                orderBy(fn, Order.DESC);
            }
        }
        return this;
    }

    /**
     * Gets the Order BY clause.
     *
     * @return The Order BY clause string.
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * Sets the Order BY clause.
     *
     * @param orderByClause The Order BY clause string.
     * @return The current {@link Condition} object.
     */
    public Condition<T> setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
        return this;
    }

    /**
     * Gets the list of all OR criteria groups.
     *
     * @return A list of {@link Criteria} objects.
     */
    public List<Criteria<T>> getOredCriteria() {
        return oredCriteria;
    }

    /**
     * Gets the list of values to be set in an UPDATE statement.
     *
     * @return A list of {@link Criterion} objects for the SET clause.
     */
    public List<Criterion> getSetValues() {
        return setValues;
    }

    /**
     * Checks if the query condition is empty.
     *
     * @return {@code true} if empty, {@code false} otherwise.
     */
    public boolean isEmpty() {
        if (oredCriteria.isEmpty()) {
            return true;
        }
        return oredCriteria.stream().allMatch(criteria -> criteria.getCriteria().isEmpty());
    }

    /**
     * Checks if the DISTINCT keyword is enabled.
     *
     * @return {@code true} if DISTINCT is enabled, {@code false} otherwise.
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * Enables or disables the DISTINCT keyword.
     *
     * @param distinct {@code true} to enable DISTINCT, {@code false} to disable.
     * @return The current {@link Condition} object.
     */
    public Condition<T> setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /**
     * Adds a field and value to be updated in a SET clause.
     *
     * @param setSql The SET clause, e.g., "column = value".
     * @return The current {@link Condition} object.
     */
    public Condition<T> set(String setSql) {
        this.setValues.add(new Criterion(setSql));
        return this;
    }

    /**
     * Adds a field and value to be updated in a SET clause using a method reference.
     *
     * @param fn    A method reference to the field.
     * @param value The value to set.
     * @return The current {@link Condition} object.
     */
    public Condition<T> set(Fn<T, Object> fn, Object value) {
        ColumnMeta column = fn.toEntityColumn();
        this.setValues.add(new Criterion(column.column(), value, column));
        return this;
    }

}
