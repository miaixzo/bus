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
 * 条件：多表条件处理基类，提供处理 SELECT、UPDATE、DELETE 语句并根据表元数据追加条件的方法。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class ConditionHandler extends SqlParserHandler implements MapperHandler {

    /**
     * 条件表达式追加模式（默认追加到最后，仅作用于 UPDATE、DELETE、SELECT 语句）。
     */
    private EnumValue.AppendMode appendMode = EnumValue.AppendMode.LAST;

    /**
     * 处理 SELECT 语句体，应用指定的条件片段。
     *
     * @param selectBody   SELECT 语句体
     * @param whereSegment Mapper 全路径，用于条件应用
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
     * 处理 DELETE 和 UPDATE 语句的 WHERE 条件。
     *
     * @param table        表对象
     * @param where        当前 WHERE 条件
     * @param whereSegment Mapper 全路径
     * @return 追加后的 WHERE 表达式
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
     * 处理 PlainSelect 语句，包含 SELECT 项、FROM 项和 JOIN。
     *
     * @param plainSelect  PlainSelect 对象
     * @param whereSegment Mapper 全路径
     */
    protected void processPlainSelect(final PlainSelect plainSelect, final String whereSegment) {
        List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
        if (CollKit.isNotEmpty(selectItems)) {
            selectItems.forEach(selectItem -> processSelectItem(selectItem, whereSegment));
        }

        // 处理 WHERE 子句中的子查询
        Expression where = plainSelect.getWhere();
        processWhereSubSelect(where, whereSegment);

        // 处理 FROM 项
        FromItem fromItem = plainSelect.getFromItem();
        List<Table> list = processFromItem(fromItem, whereSegment);
        List<Table> mainTables = new ArrayList<>(list);

        // 处理 JOIN
        List<Join> joins = plainSelect.getJoins();
        if (CollKit.isNotEmpty(joins)) {
            processJoins(mainTables, joins, whereSegment);
        }

        // 若存在主表，追加 WHERE 条件
        if (CollKit.isNotEmpty(mainTables)) {
            plainSelect.setWhere(builderExpression(where, mainTables, whereSegment));
        }
    }

    /**
     * 处理 FROM 项，返回主表列表。
     *
     * @param fromItem     FROM 子句中的项
     * @param whereSegment Mapper 全路径
     * @return 主表列表
     */
    private List<Table> processFromItem(FromItem fromItem, final String whereSegment) {
        List<Table> mainTables = new ArrayList<>();
        // 处理无 JOIN 的逻辑
        if (fromItem instanceof Table) {
            Table fromTable = (Table) fromItem;
            mainTables.add(fromTable);
        } else if (fromItem instanceof ParenthesedFromItem) {
            // SubJoin 类型也需 WHERE 条件
            List<Table> tables = processSubJoin((ParenthesedFromItem) fromItem, whereSegment);
            mainTables.addAll(tables);
        } else {
            // 处理其他 FROM 项
            processOtherFromItem(fromItem, whereSegment);
        }
        return mainTables;
    }

    /**
     * 处理 WHERE 条件中的子查询，支持 {@code IN、=、>、<、>=、<=、<>、EXISTS、NOT EXISTS} 前提：子查询需放在括号中，且通常位于比较操作符右侧。
     *
     * @param where        WHERE 条件
     * @param whereSegment Mapper 全路径
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
            // 处理子查询
            if (where instanceof BinaryExpression) {
                // 比较操作符、AND、OR 等
                BinaryExpression expression = (BinaryExpression) where;
                processWhereSubSelect(expression.getLeftExpression(), whereSegment);
                processWhereSubSelect(expression.getRightExpression(), whereSegment);
            } else if (where instanceof InExpression) {
                // IN 子句
                InExpression expression = (InExpression) where;
                Expression inExpression = expression.getRightExpression();
                if (inExpression instanceof Select) {
                    processSelectBody(((Select) inExpression), whereSegment);
                } else if (inExpression instanceof AndExpression) {
                    Expression leftExpression = ((AndExpression) inExpression).getLeftExpression();
                    processWhereSubSelect(leftExpression, whereSegment);
                }
            } else if (where instanceof ExistsExpression) {
                // EXISTS 子句
                ExistsExpression expression = (ExistsExpression) where;
                processWhereSubSelect(expression.getRightExpression(), whereSegment);
            } else if (where instanceof NotExpression) {
                // NOT EXISTS 子句
                NotExpression expression = (NotExpression) where;
                processWhereSubSelect(expression.getExpression(), whereSegment);
            } else if (where instanceof ParenthesedExpressionList) {
                ParenthesedExpressionList<Expression> expression = (ParenthesedExpressionList) where;
                processWhereSubSelect(expression.get(0), whereSegment);
            }
        }
    }

    /**
     * 处理 SELECT 项中的子查询或函数。
     *
     * @param selectItem   SELECT 项
     * @param whereSegment Mapper 全路径
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
     * 处理函数，支持 select fun(args..) 和嵌套函数 select fun1(fun2(args..),args..)。
     *
     * @param function     函数表达式
     * @param whereSegment Mapper 全路径
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
     * 处理其他 FROM 项（如子查询）。
     *
     * @param fromItem     FROM 子句中的项
     * @param whereSegment Mapper 全路径
     */
    protected void processOtherFromItem(FromItem fromItem, final String whereSegment) {
        // 移除括号
        while (fromItem instanceof ParenthesedFromItem) {
            fromItem = ((ParenthesedFromItem) fromItem).getFromItem();
        }
        if (fromItem instanceof ParenthesedSelect) {
            Select subSelect = (Select) fromItem;
            processSelectBody(subSelect, whereSegment);
        }
    }

    /**
     * 处理 SubJoin，返回主表列表。
     *
     * @param subJoin      SubJoin 对象
     * @param whereSegment Mapper 全路径
     * @return SubJoin 中的主表列表
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
     * 处理 JOIN 语句，返回主表列表。
     *
     * @param mainTables 主表列表（可为 null）
     * @param joins      JOIN 集合
     * @param segment    Mapper 全路径
     * @return 右连接查询的表列表
     */
    private List<Table> processJoins(List<Table> mainTables, List<Join> joins, final String segment) {
        // JOIN 表达式中的主表
        Table mainTable = null;
        // 当前 JOIN 的左表
        Table leftTable = null;

        if (mainTables.size() == 1) {
            mainTable = mainTables.get(0);
            leftTable = mainTable;
        }

        // 存储多 ON 表达式的表
        Deque<List<Table>> onTableDeque = new LinkedList<>();
        for (Join join : joins) {
            // 处理 ON 表达式
            FromItem joinItem = join.getRightItem();

            // 从当前 JOIN 获取表（SubJoin 视为表）
            List<Table> joinTables = null;
            if (joinItem instanceof Table) {
                joinTables = new ArrayList<>();
                joinTables.add((Table) joinItem);
            } else if (joinItem instanceof ParenthesedFromItem) {
                joinTables = processSubJoin((ParenthesedFromItem) joinItem, segment);
            }

            if (joinTables != null && !joinTables.isEmpty()) {
                // 处理隐式 INNER JOIN
                if (join.isSimple()) {
                    mainTables.addAll(joinTables);
                    continue;
                }

                // 检查当前表是否应忽略
                Table joinTable = joinTables.get(0);

                List<Table> onTables = null;
                // 处理 RIGHT JOIN
                if (join.isRight()) {
                    mainTable = joinTable;
                    mainTables.clear();
                    if (leftTable != null) {
                        onTables = Collections.singletonList(leftTable);
                    }
                } else if (join.isInner()) {
                    // 处理 INNER JOIN
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

                // 获取 JOIN 的 ON 表达式
                Collection<Expression> originOnExpressions = join.getOnExpressions();
                // 立即处理单个 ON 表达式
                if (originOnExpressions.size() == 1 && onTables != null) {
                    List<Expression> onExpressions = new LinkedList<>();
                    onExpressions.add(builderExpression(originOnExpressions.iterator().next(), onTables, segment));
                    join.setOnExpressions(onExpressions);
                    leftTable = mainTable == null ? joinTable : mainTable;
                    continue;
                }
                // 将表名压入栈，忽略的表为 null
                onTableDeque.push(onTables);
                // 处理多个 ON 表达式
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
     * 构建并处理条件表达式。
     *
     * @param expression 当前条件表达式
     * @param tables     表列表
     * @param segment    Mapper 全路径
     * @return 构建后的条件表达式
     */
    protected Expression builderExpression(Expression expression, List<Table> tables, final String segment) {
        // 无表时直接返回
        if (CollKit.isEmpty(tables)) {
            return expression;
        }
        // 为每个表构建条件
        List<Expression> expressions = tables.stream().map(item -> buildTableExpression(item, expression, segment))
                .filter(Objects::nonNull).collect(Collectors.toList());

        // 无条件时直接返回
        if (CollKit.isEmpty(expressions)) {
            return expression;
        }

        // 注入表达式
        Expression injectExpression = expressions.get(0);
        // 多表时使用 AND 组合
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
     * 追加条件表达式，默认追加到后面，可通过 appendMode 配置追加位置。
     *
     * @param expression       原 SQL 条件表达式
     * @param injectExpression 注入的条件表达式
     * @return 追加后的完整表达式（WHERE 或 ON 条件）
     */
    protected Expression appendExpression(Expression expression, Expression injectExpression) {
        if (EnumValue.AppendMode.LAST == appendMode || appendMode == null) {
            return new AndExpression(expression, injectExpression);
        } else {
            return new AndExpression(injectExpression, expression);
        }
    }

    /**
     * 构建数据库表的查询条件。
     *
     * @param table   表对象
     * @param where   当前 WHERE 条件
     * @param segment Mapper 全路径
     * @return 新增的查询条件（不会覆盖原有 WHERE 条件，仅追加），为 null 则不加入新条件
     */
    public abstract Expression buildTableExpression(final Table table, final Expression where, final String segment);

}