/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.binding.condition;

import static org.miaixz.bus.mapper.Args.DELIMITER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.mapper.Order;
import org.miaixz.bus.mapper.binding.function.Fn;
import org.miaixz.bus.mapper.criteria.Criteria;
import org.miaixz.bus.mapper.criteria.Criterion;
import org.miaixz.bus.mapper.criteria.OrCriteria;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * A generic condition query object for building complex query criteria.
 *
 * @param <T> The type of the entity class.
 * @author Kimi Liu
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
     * Default constructor. Initializes the criteria list and set-values list.
     */
    public void of() {
        new Condition();
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
    public Condition<T> selectColumns(Fn<T, Object>... fns) {
        selectColumns = Normal.EMPTY;
        simpleSelectColumns = Normal.EMPTY;
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
    public Condition<T> excludeColumns(Fn<T, Object>... fns) {
        selectColumns = Normal.EMPTY;
        simpleSelectColumns = Normal.EMPTY;
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
            orderByClause = Normal.EMPTY;
        } else {
            orderByClause += Symbol.COMMA + Symbol.SPACE;
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
                orderByClause = Normal.EMPTY;
            } else {
                orderByClause += Symbol.COMMA + Symbol.SPACE;
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
    public Condition<T> orderBy(SupplierX<String> orderByCondition) {
        return orderBy(orderByCondition.get());
    }

    /**
     * Adds an ascending Order BY clause for the specified columns using method references.
     *
     * @param fns An array of method references to the columns.
     * @return The current {@link Condition} object.
     */
    public Condition<T> orderByAsc(Fn<T, Object>... fns) {
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
    public Condition<T> orderByDesc(Fn<T, Object>... fns) {
        if (fns != null && fns.length > 0) {
            for (Fn<T, Object> fn : fns) {
                orderBy(fn, Order.DESC);
            }
        }
        return this;
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
     * Tests whether a mapped column is explicitly assigned by a typed SET value or a trusted native SET fragment.
     * Qualified and delimited identifiers are recognized without treating expression values, comments, or nested
     * function arguments as assignment targets. The original fragments are never rewritten.
     *
     * @param column the mapped column to locate
     * @return whether an explicit SET target names that column
     */
    public boolean assigns(ColumnMeta column) {
        Set<String> targets = assignmentColumns(column.column() + " = NULL");
        return setValues != null && setValues.stream().anyMatch(value -> {
            String expression = value.isNoValue() ? value.getCondition() : value.getCondition() + " = NULL";
            return assignmentColumns(expression).stream().anyMatch(targets::contains);
        });
    }

    /**
     * Reads only left-hand assignment identifiers from native SET fragments. Quoted values, dollar-quoted strings,
     * comments, and parenthesized expressions are skipped; row-assignment targets are collected individually.
     *
     * @param expression the original SET fragment
     * @return normalized, unqualified assignment identifiers
     */
    private static Set<String> assignmentColumns(String expression) {
        Set<String> columns = new HashSet<>();
        StringBuilder target = new StringBuilder();
        int depth = Normal._0;
        char quote = Symbol.C_NUL;
        boolean value = false;
        for (int index = Normal._0; index < expression.length(); index++) {
            char current = expression.charAt(index);
            char next = index + Normal._1 < expression.length() ? expression.charAt(index + Normal._1) : Symbol.C_NUL;
            if (quote != Symbol.C_NUL) {
                if (current == quote) {
                    if (next == quote) {
                        if (!value) {
                            target.append(current);
                        }
                        index++;
                    } else {
                        quote = Symbol.C_NUL;
                    }
                } else if (!value) {
                    target.append(current);
                }
                continue;
            }
            if (current == Symbol.C_MINUS && next == Symbol.C_MINUS) {
                int end = expression.indexOf(Symbol.C_LF, index + Normal._2);
                index = end < Normal._0 ? expression.length() : end;
                continue;
            }
            if (current == Symbol.C_SLASH && next == Symbol.C_STAR) {
                int comments = Normal._1;
                index += Normal._2;
                while (index < expression.length() && comments > Normal._0) {
                    if (expression.startsWith(Symbol.SLASH + Symbol.STAR, index)) {
                        comments++;
                        index += Normal._2;
                    } else if (expression.startsWith(Symbol.STAR + Symbol.SLASH, index)) {
                        comments--;
                        index += Normal._2;
                    } else {
                        index++;
                    }
                }
                index--;
                continue;
            }
            if (current == Symbol.C_DOLLAR && value) {
                int end = expression.indexOf(Symbol.C_DOLLAR, index + Normal._1);
                if (end >= Normal._0) {
                    String delimiter = expression.substring(index, end + Normal._1);
                    int closing = expression.indexOf(delimiter, end + Normal._1);
                    if (closing >= Normal._0 && PatternKit.isMatch(
                            Symbol.BACKSLASH + Symbol.DOLLAR + "(?:[A-Za-z_][A-Za-z0-9_]*)?" + Symbol.BACKSLASH
                                    + Symbol.DOLLAR,
                            delimiter)) {
                        index = closing + delimiter.length() - Normal._1;
                        continue;
                    }
                }
            }
            if (current == Symbol.C_SINGLE_QUOTE || current == Symbol.C_DOUBLE_QUOTES || current == Symbol.C_BACKTICK
                    || current == Symbol.C_BRACKET_LEFT) {
                quote = current == Symbol.C_BRACKET_LEFT ? Symbol.C_BRACKET_RIGHT : current;
            } else if (current == Symbol.C_PARENTHESE_LEFT) {
                depth++;
            } else if (current == Symbol.C_PARENTHESE_RIGHT) {
                depth--;
            } else if (current == Symbol.C_EQUAL && depth == Normal._0 && !value) {
                columns.add(target.toString().toLowerCase(Locale.ROOT));
                target.setLength(Normal._0);
                value = true;
            } else if (current == Symbol.C_COMMA && (depth == Normal._0 || !value && depth == Normal._1)) {
                if (!value) {
                    columns.add(target.toString().toLowerCase(Locale.ROOT));
                }
                target.setLength(Normal._0);
                if (depth == Normal._0) {
                    value = false;
                }
            } else if (!value) {
                if (current == Symbol.C_DOT) {
                    target.setLength(Normal._0);
                } else if (!Character.isWhitespace(current)) {
                    target.append(current);
                }
            }
        }
        columns.remove(Normal.EMPTY);
        return columns;
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
     * @throws IllegalArgumentException if the mapped column is not updatable
     */
    public Condition<T> set(Fn<T, Object> fn, Object value) {
        ColumnMeta column = fn.toEntityColumn();
        if (!column.updatable()) {
            throw new IllegalArgumentException("Column is not updatable: " + column.property());
        }
        this.setValues.add(new Criterion(column.column(), value, column));
        return this;
    }

}
