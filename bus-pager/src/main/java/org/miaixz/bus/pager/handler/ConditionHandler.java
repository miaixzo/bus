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
package org.miaixz.bus.pager.handler;

import java.util.*;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.mapper.handler.MapperHandler;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

/**
 * Base class for handling multi-table conditions. Provides methods for processing SELECT, UPDATE, and DELETE statements
 * and appending conditions based on table metadata.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class ConditionHandler extends SqlParserHandler implements MapperHandler {

    /**
     * The mode for appending conditional expressions (defaults to appending at the end, only for UPDATE, DELETE,
     * SELECT).
     */
    private EnumValue.AppendMode appendMode = EnumValue.AppendMode.LAST;

    /**
     * Processes the body of a SELECT statement, applying the specified condition segment.
     *
     * @param selectBody   the body of the SELECT statement
     * @param whereSegment the full Mapper path, used for applying conditions
     */
    protected void processSelectBody(Select selectBody, final String whereSegment) {
        if (selectBody == null) {
            return;
        }
        if (selectBody instanceof PlainSelect) {
            processPlainSelect((PlainSelect) selectBody, whereSegment);
        } else if (selectBody instanceof ParenthesedSelect) {
            ParenthesedSelect parenthesedSelect = (ParenthesedSelect) selectBody;
            processSelectBody(parenthesedSelect.getSelect(), whereSegment);
        } else if (selectBody instanceof SetOperationList) {
            SetOperationList operationList = (SetOperationList) selectBody;
            List<Select> selectBodyList = operationList.getSelects();
            if (CollKit.isNotEmpty(selectBodyList)) {
                selectBodyList.forEach(body -> processSelectBody(body, whereSegment));
            }
        }
    }

    /**
     * Handles the WHERE conditions for DELETE and UPDATE statements.
     *
     * @param table        the Table object
     * @param where        the current WHERE condition
     * @param whereSegment the full Mapper path
     * @return the appended WHERE expression
     */
    protected Expression andExpression(Table table, Expression where, final String whereSegment) {
        final Expression expression = buildTableExpression(table, where, whereSegment);
        if (expression == null) {
            return where;
        }
        if (where != null) {
            if (where instanceof OrExpression) {
                return appendExpression(new ParenthesedExpressionList<>(where), expression);
            } else {
                return appendExpression(where, expression);
            }
        }
        return expression;
    }

    /**
     * Processes a PlainSelect statement, including its SELECT items, FROM item, and JOINs.
     *
     * @param plainSelect  the PlainSelect object
     * @param whereSegment the full Mapper path
     */
    protected void processPlainSelect(final PlainSelect plainSelect, final String whereSegment) {
        List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
        if (CollKit.isNotEmpty(selectItems)) {
            selectItems.forEach(selectItem -> processSelectItem(selectItem, whereSegment));
        }

        // Process subqueries in the WHERE clause
        Expression where = plainSelect.getWhere();
        processWhereSubSelect(where, whereSegment);

        // Process the FROM item
        FromItem fromItem = plainSelect.getFromItem();
        List<Table> list = processFromItem(fromItem, whereSegment);
        List<Table> mainTables = new ArrayList<>(list);

        // Process JOINs
        List<Join> joins = plainSelect.getJoins();
        if (CollKit.isNotEmpty(joins)) {
            processJoins(mainTables, joins, whereSegment);
        }

        // If main tables exist, append WHERE conditions
        if (CollKit.isNotEmpty(mainTables)) {
            plainSelect.setWhere(builderExpression(where, mainTables, whereSegment));
        }
    }

    /**
     * Processes a FROM item and returns a list of main tables.
     *
     * @param fromItem     the item in the FROM clause
     * @param whereSegment the full Mapper path
     * @return a list of main tables
     */
    private List<Table> processFromItem(FromItem fromItem, final String whereSegment) {
        List<Table> mainTables = new ArrayList<>();
        // Handle logic without JOINs
        if (fromItem instanceof Table) {
            Table fromTable = (Table) fromItem;
            mainTables.add(fromTable);
        } else if (fromItem instanceof ParenthesedFromItem) {
            // SubJoin types also need WHERE conditions
            List<Table> tables = processSubJoin((ParenthesedFromItem) fromItem, whereSegment);
            mainTables.addAll(tables);
        } else {
            // Process other FROM items
            processOtherFromItem(fromItem, whereSegment);
        }
        return mainTables;
    }

    /**
     * Processes subqueries in the WHERE clause, supporting IN, =, &gt;, &lt;, &gt;=, &lt;=, &lt;&gt;, EXISTS, NOT
     * EXISTS. Prerequisite: Subqueries must be in parentheses and usually on the right side of a comparison operator.
     *
     * @param where        the WHERE condition
     * @param whereSegment the full Mapper path
     */
    protected void processWhereSubSelect(Expression where, final String whereSegment) {
        if (where == null) {
            return;
        }
        if (where instanceof FromItem) {
            processOtherFromItem((FromItem) where, whereSegment);
            return;
        }
        if (where.toString().contains("SELECT")) {
            // Process subqueries
            if (where instanceof BinaryExpression) {
                // Comparison operators, AND, OR, etc.
                BinaryExpression expression = (BinaryExpression) where;
                processWhereSubSelect(expression.getLeftExpression(), whereSegment);
                processWhereSubSelect(expression.getRightExpression(), whereSegment);
            } else if (where instanceof InExpression) {
                // IN clause
                InExpression expression = (InExpression) where;
                Expression inExpression = expression.getRightExpression();
                if (inExpression instanceof Select) {
                    processSelectBody(((Select) inExpression), whereSegment);
                } else if (inExpression instanceof AndExpression) {
                    Expression leftExpression = ((AndExpression) inExpression).getLeftExpression();
                    processWhereSubSelect(leftExpression, whereSegment);
                }
            } else if (where instanceof ExistsExpression) {
                // EXISTS clause
                ExistsExpression expression = (ExistsExpression) where;
                processWhereSubSelect(expression.getRightExpression(), whereSegment);
            } else if (where instanceof NotExpression) {
                // NOT EXISTS clause
                NotExpression expression = (NotExpression) where;
                processWhereSubSelect(expression.getExpression(), whereSegment);
            } else if (where instanceof ParenthesedExpressionList) {
                ParenthesedExpressionList<Expression> expression = (ParenthesedExpressionList) where;
                processWhereSubSelect(expression.get(0), whereSegment);
            }
        }
    }

    /**
     * Processes subqueries or functions in a SELECT item.
     *
     * @param selectItem   the SELECT item
     * @param whereSegment the full Mapper path
     */
    protected void processSelectItem(SelectItem selectItem, final String whereSegment) {
        Expression expression = selectItem.getExpression();
        if (expression instanceof Select) {
            processSelectBody(((Select) expression), whereSegment);
        } else if (expression instanceof Function) {
            processFunction((Function) expression, whereSegment);
        } else if (expression instanceof ExistsExpression) {
            ExistsExpression existsExpression = (ExistsExpression) expression;
            processSelectBody((Select) existsExpression.getRightExpression(), whereSegment);
        }
    }

    /**
     * Processes a function, supporting `select fun(args..)` and nested functions `select fun1(fun2(args..),args..)`.
     *
     * @param function     the function expression
     * @param whereSegment the full Mapper path
     */
    protected void processFunction(Function function, final String whereSegment) {
        ExpressionList<?> parameters = function.getParameters();
        if (parameters != null) {
            parameters.forEach(expression -> {
                if (expression instanceof Select) {
                    processSelectBody(((Select) expression), whereSegment);
                } else if (expression instanceof Function) {
                    processFunction((Function) expression, whereSegment);
                } else if (expression instanceof EqualsTo) {
                    if (((EqualsTo) expression).getLeftExpression() instanceof Select) {
                        processSelectBody(((Select) ((EqualsTo) expression).getLeftExpression()), whereSegment);
                    }
                    if (((EqualsTo) expression).getRightExpression() instanceof Select) {
                        processSelectBody(((Select) ((EqualsTo) expression).getRightExpression()), whereSegment);
                    }
                }
            });
        }
    }

    /**
     * Processes other FROM items (e.g., subqueries).
     *
     * @param fromItem     the item in the FROM clause
     * @param whereSegment the full Mapper path
     */
    protected void processOtherFromItem(FromItem fromItem, final String whereSegment) {
        // Remove parentheses
        while (fromItem instanceof ParenthesedFromItem) {
            fromItem = ((ParenthesedFromItem) fromItem).getFromItem();
        }
        if (fromItem instanceof ParenthesedSelect) {
            Select subSelect = (Select) fromItem;
            processSelectBody(subSelect, whereSegment);
        }
    }

    /**
     * Processes a SubJoin and returns a list of main tables.
     *
     * @param subJoin      the SubJoin object
     * @param whereSegment the full Mapper path
     * @return a list of main tables from the SubJoin
     */
    private List<Table> processSubJoin(ParenthesedFromItem subJoin, final String whereSegment) {
        while (subJoin.getJoins() == null && subJoin.getFromItem() instanceof ParenthesedFromItem) {
            subJoin = (ParenthesedFromItem) subJoin.getFromItem();
        }
        List<Table> tableList = processFromItem(subJoin.getFromItem(), whereSegment);
        List<Table> mainTables = new ArrayList<>(tableList);
        if (subJoin.getJoins() != null) {
            processJoins(mainTables, subJoin.getJoins(), whereSegment);
        }
        return mainTables;
    }

    /**
     * Processes JOIN statements and returns a list of main tables.
     *
     * @param mainTables the list of main tables (can be null)
     * @param joins      the collection of JOINs
     * @param segment    the full Mapper path
     * @return a list of tables from the right join query
     */
    private List<Table> processJoins(List<Table> mainTables, List<Join> joins, final String segment) {
        // Main table in the JOIN expression
        Table mainTable = null;
        // Left table of the current JOIN
        Table leftTable = null;

        if (mainTables.size() == 1) {
            mainTable = mainTables.get(0);
            leftTable = mainTable;
        }

        // Stack to store tables for multiple ON expressions
        Deque<List<Table>> onTableDeque = new LinkedList<>();
        for (Join join : joins) {
            // Process ON expression
            FromItem joinItem = join.getRightItem();

            // Get tables from the current JOIN (SubJoin is treated as a table)
            List<Table> joinTables = null;
            if (joinItem instanceof Table) {
                joinTables = new ArrayList<>();
                joinTables.add((Table) joinItem);
            } else if (joinItem instanceof ParenthesedFromItem) {
                joinTables = processSubJoin((ParenthesedFromItem) joinItem, segment);
            }

            if (joinTables != null && !joinTables.isEmpty()) {
                // Handle implicit INNER JOIN
                if (join.isSimple()) {
                    mainTables.addAll(joinTables);
                    continue;
                }

                // Check if the current table should be ignored
                Table joinTable = joinTables.get(0);

                List<Table> onTables = null;
                // Handle RIGHT JOIN
                if (join.isRight()) {
                    mainTable = joinTable;
                    mainTables.clear();
                    if (leftTable != null) {
                        onTables = Collections.singletonList(leftTable);
                    }
                } else if (join.isInner()) {
                    // Handle INNER JOIN
                    if (mainTable == null) {
                        onTables = Collections.singletonList(joinTable);
                    } else {
                        onTables = Arrays.asList(mainTable, joinTable);
                    }
                    mainTable = null;
                    mainTables.clear();
                } else {
                    onTables = Collections.singletonList(joinTable);
                }

                if (mainTable != null && !mainTables.contains(mainTable)) {
                    mainTables.add(mainTable);
                }

                // Get ON expressions of the JOIN
                Collection<Expression> originOnExpressions = join.getOnExpressions();
                // Immediately process single ON expression
                if (originOnExpressions.size() == 1 && onTables != null) {
                    List<Expression> onExpressions = new LinkedList<>();
                    onExpressions.add(builderExpression(originOnExpressions.iterator().next(), onTables, segment));
                    join.setOnExpressions(onExpressions);
                    leftTable = mainTable == null ? joinTable : mainTable;
                    continue;
                }
                // Push table names to the stack, ignored tables are null
                onTableDeque.push(onTables);
                // Handle multiple ON expressions
                if (originOnExpressions.size() > 1) {
                    Collection<Expression> onExpressions = new LinkedList<>();
                    for (Expression originOnExpression : originOnExpressions) {
                        List<Table> currentTableList = onTableDeque.poll();
                        if (CollKit.isEmpty(currentTableList)) {
                            onExpressions.add(originOnExpression);
                        } else {
                            onExpressions.add(builderExpression(originOnExpression, currentTableList, segment));
                        }
                    }
                    join.setOnExpressions(onExpressions);
                }
                leftTable = joinTable;
            } else {
                processOtherFromItem(joinItem, segment);
                leftTable = null;
            }
        }

        return mainTables;
    }

    /**
     * Builds and processes a conditional expression.
     *
     * @param expression the current conditional expression
     * @param tables     the list of tables
     * @param segment    the full Mapper path
     * @return the constructed conditional expression
     */
    protected Expression builderExpression(Expression expression, List<Table> tables, final String segment) {
        // Return directly if no tables
        if (CollKit.isEmpty(tables)) {
            return expression;
        }
        // Build conditions for each table
        List<Expression> expressions = tables.stream().map(item -> buildTableExpression(item, expression, segment))
                .filter(Objects::nonNull).collect(Collectors.toList());

        // Return directly if no conditions
        if (CollKit.isEmpty(expressions)) {
            return expression;
        }

        // Inject expression
        Expression injectExpression = expressions.get(0);
        // Combine with AND for multiple tables
        if (expressions.size() > 1) {
            for (int i = 1; i < expressions.size(); i++) {
                injectExpression = new AndExpression(injectExpression, expressions.get(i));
            }
        }

        if (expression == null) {
            return injectExpression;
        }
        if (expression instanceof OrExpression) {
            return appendExpression(new ParenthesedExpressionList<>(expression), injectExpression);
        } else {
            return appendExpression(expression, injectExpression);
        }
    }

    /**
     * Appends a conditional expression. By default, it appends to the end, but the position can be configured via
     * `appendMode`.
     *
     * @param expression       the original SQL conditional expression
     * @param injectExpression the conditional expression to be injected
     * @return the complete appended expression (for WHERE or ON clauses)
     */
    protected Expression appendExpression(Expression expression, Expression injectExpression) {
        if (EnumValue.AppendMode.LAST == appendMode || appendMode == null) {
            return new AndExpression(expression, injectExpression);
        } else {
            return new AndExpression(injectExpression, expression);
        }
    }

    /**
     * Builds the query condition for a database table.
     *
     * @param table   the Table object
     * @param where   the current WHERE condition
     * @param segment the full Mapper path
     * @return the new query condition to be added (does not overwrite the original WHERE, only appends). Returns null
     *         if no new condition is added.
     */
    public abstract Expression buildTableExpression(final Table table, final Expression where, final String segment);

}
