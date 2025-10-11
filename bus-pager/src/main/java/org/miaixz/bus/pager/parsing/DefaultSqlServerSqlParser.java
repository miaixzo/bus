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
package org.miaixz.bus.pager.parsing;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.miaixz.bus.core.lang.exception.PageException;
import org.miaixz.bus.pager.Builder;

import java.util.*;

/**
 * Default implementation of {@link SqlServerSqlParser} for converting SQL Server queries to paginated statements.
 * Important considerations:
 * <ol>
 * <li>Ensure your SQL is executable first.</li>
 * <li>It's best to include an ORDER BY clause directly in the SQL, which can be automatically extracted.</li>
 * <li>If there is no ORDER BY, you can provide it as a parameter, but you must ensure its correctness.</li>
 * <li>If the SQL has an ORDER BY, it can be overridden by the `orderby` parameter.</li>
 * <li>Column names in the ORDER BY clause cannot be aliases.</li>
 * <li>Do not use single quotes (') for table and column aliases.</li>
 * </ol>
 * This class is designed as a standalone utility, dependent on JSqlParser, and can be used independently.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultSqlServerSqlParser implements SqlServerSqlParser {

    /**
     * Start row number placeholder.
     */
    public static final String START_ROW = String.valueOf(Long.MIN_VALUE);
    /**
     * Page size placeholder.
     */
    public static final String PAGE_SIZE = String.valueOf(Long.MAX_VALUE);
    /**
     * Outer wrapper table name.
     */
    protected static final String WRAP_TABLE = "WRAP_OUTER_TABLE";
    /**
     * Table alias name.
     */
    protected static final String PAGE_TABLE_NAME = "PAGE_TABLE_ALIAS";
    /**
     * Alias for the page table.
     */
    public static final Alias PAGE_TABLE_ALIAS = new Alias(PAGE_TABLE_NAME);
    /**
     * Row number column name.
     */
    protected static final String PAGE_ROW_NUMBER = "PAGE_ROW_NUMBER";
    /**
     * Row number column object.
     */
    protected static final Column PAGE_ROW_NUMBER_COLUMN = new Column(PAGE_ROW_NUMBER);
    /**
     * TOP 100 PERCENT clause.
     */
    protected static final Top TOP100_PERCENT;
    /**
     * Alias prefix for generated columns.
     */
    protected static final String PAGE_COLUMN_ALIAS_PREFIX = "ROW_ALIAS_";

    /**
     * Static initializer for constants.
     */
    static {
        TOP100_PERCENT = new Top();
        TOP100_PERCENT.setExpression(new LongValue(100));
        TOP100_PERCENT.setPercentage(true);
    }

    /**
     * Converts a SQL statement to a paginated statement.
     *
     * @param sql the SQL statement
     * @return the paginated SQL string
     */
    public String convertToPageSql(String sql) {
        return convertToPageSql(sql, null, null);
    }

    /**
     * Converts a SQL statement to a paginated statement.
     *
     * @param sql    the SQL statement
     * @param offset the starting position
     * @param limit  the ending position
     * @return the paginated SQL string
     */
    public String convertToPageSql(String sql, Integer offset, Integer limit) {
        // Parse the SQL
        Statement stmt;
        try {
            stmt = Builder.parse(sql);
        } catch (Throwable e) {
            throw new PageException("The SQL statement cannot be converted to a pagination query!", e);
        }
        if (!(stmt instanceof Select)) {
            throw new PageException("the pagination statement must be a select query!");
        }
        // Get the paginated select statement
        Select pageSelect = getPageSelect((Select) stmt);
        String pageSql = pageSelect.toString();
        // Caching is handled externally, so parameters are not replaced here
        if (offset != null) {
            pageSql = pageSql.replace(START_ROW, String.valueOf(offset));
        }
        if (limit != null) {
            pageSql = pageSql.replace(PAGE_SIZE, String.valueOf(limit));
        }
        return pageSql;
    }

    /**
     * Gets an outer wrapped TOP query.
     *
     * @param select the Select statement
     * @return the wrapped Select statement
     */
    protected Select getPageSelect(Select select) {
        if (select instanceof SetOperationList) {
            select = wrapSetOperationList((SetOperationList) select);
        }
        // The selectBody here must be a PlainSelect
        if (((PlainSelect) select).getTop() != null) {
            throw new PageException(
                    "The pagination statement already contains the top, and can no longer be used to query the pagination handler!");
        }
        // Get the query columns
        List<SelectItem<?>> selectItems = getSelectItems((PlainSelect) select);
        // Add ROW_NUMBER() to the first level of SQL
        List<SelectItem<?>> autoItems = new ArrayList<>();
        SelectItem<?> orderByColumn = addRowNumber((PlainSelect) select, autoItems);
        // Add auto-generated columns
        ((PlainSelect) select).addSelectItems(autoItems.toArray(new SelectItem[0]));
        // Process order by in subqueries
        processSelectBody(select, 0);

        // Middle layer subquery
        PlainSelect innerSelectBody = new PlainSelect();
        // PAGE_ROW_NUMBER
        innerSelectBody.addSelectItems(orderByColumn);
        innerSelectBody.addSelectItems(selectItems.toArray(new SelectItem[0]));
        // Use the original query as the inner subquery
        ParenthesedSelect fromInnerItem = new ParenthesedSelect();
        fromInnerItem.setSelect(select);
        fromInnerItem.setAlias(PAGE_TABLE_ALIAS);
        innerSelectBody.setFromItem(fromInnerItem);

        // Create a new select
        PlainSelect newSelect = new PlainSelect();
        // Set top
        Top top = new Top();
        top.setExpression(new LongValue(Long.MAX_VALUE));
        newSelect.setTop(top);
        // Set order by
        List<OrderByElement> orderByElements = new ArrayList<OrderByElement>();
        OrderByElement orderByElement = new OrderByElement();
        orderByElement.setExpression(PAGE_ROW_NUMBER_COLUMN);
        orderByElements.add(orderByElement);
        newSelect.setOrderByElements(orderByElements);
        // Set where
        GreaterThan greaterThan = new GreaterThan();
        greaterThan.setLeftExpression(PAGE_ROW_NUMBER_COLUMN);
        greaterThan.setRightExpression(new LongValue(Long.MIN_VALUE));
        newSelect.setWhere(greaterThan);
        // Set selectItems
        newSelect.setSelectItems(selectItems);
        // Set fromItem
        ParenthesedSelect fromItem = new ParenthesedSelect();
        // Middle layer subquery
        fromItem.setSelect(innerSelectBody);
        fromItem.setAlias(PAGE_TABLE_ALIAS);
        newSelect.setFromItem(fromItem);

        if (isNotEmptyList(select.getWithItemsList())) {
            newSelect.setWithItemsList(select.getWithItemsList());
            select.setWithItemsList(null);
        }
        return newSelect;
    }

    /**
     * Wraps a SetOperationList.
     *
     * @param setOperationList the SetOperationList
     * @return the wrapped Select statement
     */
    protected Select wrapSetOperationList(SetOperationList setOperationList) {
        // Get the last plainSelect
        Select setSelectBody = setOperationList.getSelects().get(setOperationList.getSelects().size() - 1);
        if (!(setSelectBody instanceof PlainSelect)) {
            throw new PageException("Unable to process the SQL, you can submit issues in GitHub for help.!");
        }
        PlainSelect plainSelect = (PlainSelect) setSelectBody;
        PlainSelect selectBody = new PlainSelect();
        List<SelectItem<?>> selectItems = getSelectItems(plainSelect);
        selectBody.setSelectItems(selectItems);

        // Set fromItem
        ParenthesedSelect fromItem = new ParenthesedSelect();
        fromItem.setSelect(setOperationList);
        fromItem.setAlias(new Alias(WRAP_TABLE));
        selectBody.setFromItem(fromItem);
        // order by
        if (isNotEmptyList(setOperationList.getOrderByElements())) {
            selectBody.setOrderByElements(setOperationList.getOrderByElements());
            setOperationList.setOrderByElements(null);
        }
        return selectBody;
    }

    /**
     * Gets the query columns.
     *
     * @param plainSelect the PlainSelect statement
     * @return the list of select items
     */
    protected List<SelectItem<?>> getSelectItems(PlainSelect plainSelect) {
        // Set selectItems
        List<SelectItem<?>> selectItems = new ArrayList<>();
        for (SelectItem<?> selectItem : plainSelect.getSelectItems()) {
            if (selectItem.getExpression() instanceof AllTableColumns) {
                selectItems.add(new SelectItem<>(new AllColumns()));
            } else if (selectItem.getAlias() != null) {
                // Use alias directly
                Column column = new Column(selectItem.getAlias().getName());
                SelectItem<?> expressionItem = new SelectItem<>(column);
                selectItems.add(expressionItem);
            } else if (selectItem.getExpression() instanceof Column) {
                Column column = (Column) selectItem.getExpression();
                SelectItem<?> item = null;
                if (column.getTable() != null) {
                    Column newColumn = new Column(column.getColumnName());
                    item = new SelectItem<>(newColumn);
                    selectItems.add(item);
                } else {
                    selectItems.add(selectItem);
                }
            } else {
                selectItems.add(selectItem);
            }
        }
        // SELECT *, 1 AS alias FROM TEST
        // should be
        // SELECT * FROM (SELECT *, 1 AS alias FROM TEST)
        // not
        // SELECT *, alias FROM (SELECT *, 1 AS alias FROM TEST)
        for (SelectItem<?> selectItem : selectItems) {
            if (selectItem.getExpression() instanceof AllColumns) {
                return Collections.singletonList(selectItem);
            }
        }
        return selectItems;
    }

    /**
     * Gets the ROW_NUMBER() column.
     *
     * @param plainSelect the original query
     * @param autoItems   auto-generated query columns
     * @return the ROW_NUMBER() column
     */
    protected SelectItem<?> addRowNumber(PlainSelect plainSelect, List<SelectItem<?>> autoItems) {
        // Add ROW_NUMBER()
        StringBuilder orderByBuilder = new StringBuilder();
        orderByBuilder.append("ROW_NUMBER() OVER (");
        if (isNotEmptyList(plainSelect.getOrderByElements())) {
            orderByBuilder.append(PlainSelect.orderByToString(getOrderByElements(plainSelect, autoItems)).substring(1));
            // Clear the order by list
            plainSelect.setOrderByElements(null);
        } else {
            orderByBuilder.append("ORDER BY RAND()");
        }
        orderByBuilder.append(") ");
        orderByBuilder.append(PAGE_ROW_NUMBER);
        return new SelectItem<>(new Column(orderByBuilder.toString()));
    }

    /**
     * Processes the selectBody to remove Order by.
     *
     * @param select the query
     * @param level  the level of nesting
     */
    protected void processSelectBody(Object select, int level) {
        if (select == null) {
            return;
        }
        if (select instanceof PlainSelect) {
            processPlainSelect((PlainSelect) select, level + 1);
        } else if (select instanceof WithItem<?>) {
            WithItem<?> withItem = (WithItem<?>) select;
            Select withSelect = withItem.getSelect();
            if (withSelect != null) {
                processSelectBody(withSelect, level + 1);
            }
        } else if (select instanceof SetOperationList) {
            SetOperationList operationList = (SetOperationList) select;
            List<Select> selects = operationList.getSelects();
            if (isNotEmptyList(selects)) {
                for (Select plainSelect : selects) {
                    processSelectBody(plainSelect, level + 1);
                }
            }
        }
    }

    /**
     * Processes a PlainSelect type of select.
     *
     * @param plainSelect the query
     * @param level       the level of nesting
     */
    protected void processPlainSelect(PlainSelect plainSelect, int level) {
        if (level > 1) {
            if (isNotEmptyList(plainSelect.getOrderByElements())) {
                if (plainSelect.getTop() == null) {
                    plainSelect.setTop(TOP100_PERCENT);
                }
            }
        }
        if (plainSelect.getFromItem() != null) {
            processFromItem(plainSelect.getFromItem(), level + 1);
        }
        if (plainSelect.getJoins() != null && !plainSelect.getJoins().isEmpty()) {
            List<Join> joins = plainSelect.getJoins();
            for (Join join : joins) {
                if (join.getRightItem() != null) {
                    processFromItem(join.getRightItem(), level + 1);
                }
            }
        }
    }

    /**
     * Processes a subquery.
     *
     * @param fromItem the FromItem
     * @param level    the level of nesting
     */
    protected void processFromItem(FromItem fromItem, int level) {
        if (fromItem instanceof LateralSubSelect) {
            processSelectBody(((LateralSubSelect) fromItem).getSelect(), level + 1);
        } else if (fromItem instanceof ParenthesedSelect) {
            processSelectBody(((ParenthesedSelect) fromItem).getSelect(), level + 1);
        } else if (fromItem instanceof Select) {
            processSelectBody((Select) fromItem, level + 1);
        } else if (fromItem instanceof ParenthesedFromItem) {
            processFromItem(((ParenthesedFromItem) fromItem).getFromItem(), level + 1);
        }
        // No processing needed for Table
    }

    /**
     * Checks if a list is not empty.
     *
     * @param list the list
     * @return true if the list is not empty, false otherwise
     */
    public boolean isNotEmptyList(List<?> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Clones an OrderByElement.
     *
     * @param orig  the original OrderByElement
     * @param alias the new sorting element for the OrderByElement
     * @return the cloned new OrderByElement
     */
    protected OrderByElement cloneOrderByElement(OrderByElement orig, String alias) {
        return cloneOrderByElement(orig, new Column(alias));
    }

    /**
     * Clones an OrderByElement.
     *
     * @param orig       the original OrderByElement
     * @param expression the new sorting element for the OrderByElement
     * @return the cloned new OrderByElement
     */
    protected OrderByElement cloneOrderByElement(OrderByElement orig, Expression expression) {
        OrderByElement element = new OrderByElement();
        element.setAsc(orig.isAsc());
        element.setAscDescPresent(orig.isAscDescPresent());
        element.setNullOrdering(orig.getNullOrdering());
        element.setExpression(expression);
        return element;
    }

    /**
     * Gets the new sorting list.
     *
     * @param plainSelect the original query
     * @param autoItems   the newly generated query elements
     * @return the new sorting list
     */
    protected List<OrderByElement> getOrderByElements(PlainSelect plainSelect, List<SelectItem<?>> autoItems) {
        List<OrderByElement> orderByElements = plainSelect.getOrderByElements();
        ListIterator<OrderByElement> iterator = orderByElements.listIterator();
        OrderByElement orderByElement;

        // Collection of non-`*` and non-`t.*` query columns
        Map<String, SelectItem<?>> selectMap = new HashMap<>();
        // Collection of aliases
        Set<String> aliases = new HashSet<>();
        // Whether it contains a `*` query column
        boolean allColumns = false;
        // Collection of table names for `t.*` query columns
        Set<String> allColumnsTables = new HashSet<>();

        for (SelectItem<?> item : plainSelect.getSelectItems()) {
            Expression expression = item.getExpression();
            if (expression instanceof AllTableColumns) {
                allColumnsTables.add(((AllTableColumns) expression).getTable().getName());
            } else if (expression instanceof AllColumns) {
                allColumns = true;
            } else {
                selectMap.put(expression.toString(), item);
                Alias alias = item.getAlias();
                if (alias != null) {
                    aliases.add(alias.getName());
                }
            }
        }

        // Start iterating through the OrderByElement list
        int aliasNo = 1;
        while (iterator.hasNext()) {
            orderByElement = iterator.next();
            Expression expression = orderByElement.getExpression();
            SelectItem<?> selectExpressionItem = selectMap.get(expression.toString());
            if (selectExpressionItem != null) { // OrderByElement is in the query list
                Alias alias = selectExpressionItem.getAlias();
                if (alias != null) { // Use the query column alias if it exists
                    iterator.set(cloneOrderByElement(orderByElement, alias.getName()));

                } else { // The query column does not have an alias
                    if (expression instanceof Column) {
                        // The query column is a normal column, so remove the table name reference from the sorting
                        // column
                        // because the column name in the outer query does not contain the table name.
                        // Example (for explanation only, not the final paginated result):
                        // SELECT TEST.A FROM TEST ORDER BY TEST.A
                        // -->
                        // SELECT A FROM (SELECT TEST.A FROM TEST) ORDER BY A
                        ((Column) expression).setTable(null);

                    } else {
                        // Pagination is not supported if the query column is not a normal column (e.g., a function
                        // column)
                        // This situation is difficult to predict, and simply adding a new column can lead to unexpected
                        // results.
                        // Since adding an alias to a column is very simple, complex sorting columns are required to
                        // have an alias.
                        throw new PageException("The column \"" + expression + "\" needs to define an alias");
                    }
                }

            } else { // OrderByElement is not in the query list, need to auto-generate a query column
                if (expression instanceof Column) { // OrderByElement is a normal column
                    Table table = ((Column) expression).getTable();
                    if (table == null) { // Table name is null
                        if (allColumns || (allColumnsTables.size() == 1 && plainSelect.getJoins() == null)
                                || aliases.contains(((Column) expression).getColumnName())) {
                            // Contains `*` query column OR only one `t.*` column and it's a single-table query OR the
                            // sorting column is an alias
                            // In this case, the sorting column is already in the query list, so no action is needed.
                            continue;
                        }

                    } else { // Table name is not null
                        String tableName = table.getName();
                        if (allColumns || allColumnsTables.contains(tableName)) {
                            // Contains `*` query column OR contains the specific `t.*` column
                            // In this case, the sorting column is already in the query list, just remove the table name
                            // reference.
                            ((Column) expression).setTable(null);
                            continue;
                        }
                    }
                }

                // Add the sorting column to the query columns
                String aliasName = PAGE_COLUMN_ALIAS_PREFIX + aliasNo++;

                SelectItem<?> item = new SelectItem<>(expression);
                item.setAlias(new Alias(aliasName));
                autoItems.add(item);

                iterator.set(cloneOrderByElement(orderByElement, aliasName));
            }
        }
        return orderByElements;
    }

}
