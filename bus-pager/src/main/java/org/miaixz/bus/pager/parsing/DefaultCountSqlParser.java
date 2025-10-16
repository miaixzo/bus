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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.pager.Builder;
import org.miaixz.bus.pager.binding.PageMethod;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.Token;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

/**
 * Default implementation of {@link CountSqlParser} that provides a more intelligent way to generate count query SQL. It
 * attempts to optimize the count query by removing unnecessary ORDER BY clauses and simplifying the SELECT statement.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DefaultCountSqlParser implements CountSqlParser {

    public static final String KEEP_ORDERBY = "/*keep orderby*/";
    protected static final Alias TABLE_ALIAS;

    static {
        TABLE_ALIAS = new Alias("table_count");
        TABLE_ALIAS.setUseAs(false);
    }

    // Use a synchronized set to store function names for thread safety
    private final Set<String> skipFunctions = Collections.synchronizedSet(new HashSet<>());
    private final Set<String> falseFunctions = Collections.synchronizedSet(new HashSet<>());

    /**
     * Retrieves an intelligent COUNT SQL, automatically detecting whether to keep the ORDER BY clause. This method
     * optimizes string handling and parsing logic to reduce performance overhead.
     *
     * @param sql         the original SQL query
     * @param countColumn the COUNT column name, defaults to "0"
     * @return the optimized COUNT SQL
     */
    @Override
    public String getSmartCountSql(String sql, String countColumn) {
        // Quickly check if ORDER BY needs to be kept
        if (sql.contains(KEEP_ORDERBY) || keepOrderBy()) {
            return getSimpleCountSql(sql, countColumn);
        }

        try {
            // Parse the SQL
            Statement stmt = Builder.parse(sql);
            Select select = (Select) stmt;

            // Process the SELECT body to remove unnecessary ORDER BY clauses
            processSelect(select);

            // Optimize WITH clause processing
            processWithItemsList(select.getWithItemsList());

            // Convert to a COUNT query
            Select countSelect = sqlToCount(select, countColumn);

            // Preserve original SQL comments (like hints)
            String result = countSelect.toString();
            if (select instanceof PlainSelect && select.getASTNode() != null) {
                Token token = select.getASTNode().jjtGetFirstToken();
                if (token != null && token.specialToken != null) {
                    String hints = token.specialToken.toString().trim();
                    if (hints.startsWith("/*") && hints.endsWith("*/") && !result.startsWith("/*")) {
                        result = hints + Symbol.SPACE + result;
                    }
                }
            }
            return result;

        } catch (Throwable e) {
            // Fallback to simple COUNT on parsing failure
            return getSimpleCountSql(sql, countColumn);
        }
    }

    /**
     * Retrieves a simple COUNT SQL, suitable for unparseable or complex scenarios.
     *
     * @param sql the original query SQL
     * @return a simple COUNT SQL
     */
    public String getSimpleCountSql(final String sql) {
        return getSimpleCountSql(sql, "0");
    }

    /**
     * Retrieves a simple COUNT SQL with a specified COUNT column name. This method optimizes StringBuilder capacity
     * estimation to reduce resizing.
     *
     * @param sql  the original query SQL
     * @param name the COUNT column name
     * @return a simple COUNT SQL
     */
    public String getSimpleCountSql(final String sql, String name) {
        StringBuilder stringBuilder = new StringBuilder(sql.length() + 50);
        stringBuilder.append("select count(");
        stringBuilder.append(name);
        stringBuilder.append(") from ( \n");
        stringBuilder.append(sql);
        stringBuilder.append("\n ) tmp_count");
        return stringBuilder.toString();
    }

    /**
     * Converts a SQL statement to a count query.
     *
     * @param select the original query SQL
     * @param name   the name of the count column
     * @return the resulting count query SQL
     */
    public Select sqlToCount(Select select, String name) {
        List<SelectItem<?>> countItem = Collections.singletonList(new SelectItem<>(new Column("COUNT(" + name + ")")));

        if (select instanceof PlainSelect && isSimpleCount((PlainSelect) select)) {
            // Directly replace SELECT items in simple scenarios
            ((PlainSelect) select).setSelectItems(countItem);
            return select;
        }

        // Wrap in a subquery for complex scenarios
        PlainSelect plainSelect = new PlainSelect();
        ParenthesedSelect subSelect = new ParenthesedSelect();
        subSelect.setSelect(select);
        subSelect.setAlias(TABLE_ALIAS);
        plainSelect.setFromItem(subSelect);
        plainSelect.setSelectItems(countItem);

        // Move WITH clauses
        if (select.getWithItemsList() != null) {
            plainSelect.setWithItemsList(select.getWithItemsList());
            select.setWithItemsList(null);
        }

        return plainSelect;
    }

    /**
     * Determines if a simple COUNT query can be used. This method avoids using the deprecated Parenthesis class, using
     * ParenthesedExpressionList or generic Expression instead.
     *
     * @param select the query
     * @return true if it is a simple COUNT, false otherwise
     */
    public boolean isSimpleCount(PlainSelect select) {
        // Cannot be simplified if GROUP BY, DISTINCT, or HAVING exists
        if (select.getGroupBy() != null || select.getDistinct() != null || select.getHaving() != null) {
            return false;
        }

        for (SelectItem<?> item : select.getSelectItems()) {
            String itemStr = item.toString();
            // Cannot be simplified if it contains parameters (?)
            if (itemStr.contains("?")) {
                return false;
            }

            Expression expression = item.getExpression();
            if (expression instanceof Function) {
                String name = ((Function) expression).getName();
                if (name != null) {
                    String upperName = name.toUpperCase();
                    if (skipFunctions.contains(upperName)) {
                        continue;
                    }
                    if (falseFunctions.contains(upperName)) {
                        return false;
                    }
                    // Check if it is an aggregate function
                    for (String aggFunc : AGGREGATE_FUNCTIONS) {
                        if (upperName.startsWith(aggFunc)) {
                            falseFunctions.add(upperName);
                            return false;
                        }
                    }
                    skipFunctions.add(upperName);
                }
            } else if (expression instanceof ParenthesedExpressionList && item.getAlias() != null) {
                // Parenthesized expression lists with aliases may be referenced in ORDER BY or HAVING
                return false;
            } else if (item.getAlias() != null && expression.toString().startsWith("(")
                    && expression.toString().endsWith(")")) {
                // Detect single expressions wrapped in parentheses (alternative to Parenthesis)
                return false;
            }
        }
        return true;
    }

    /**
     * Processes the SELECT body to remove unnecessary ORDER BY clauses. This method optimizes recursive processing
     * logic to reduce redundant calls.
     *
     * @param select the query information
     */
    public void processSelect(Select select) {
        if (select == null) {
            return;
        }

        if (select instanceof PlainSelect) {
            processPlainSelect((PlainSelect) select);
        } else if (select instanceof ParenthesedSelect) {
            processSelect(((ParenthesedSelect) select).getSelect());
        } else if (select instanceof SetOperationList) {
            SetOperationList setOpList = (SetOperationList) select;
            for (Select sel : setOpList.getSelects()) {
                processSelect(sel);
            }
            if (!orderByHashParameters(setOpList.getOrderByElements())) {
                setOpList.setOrderByElements(null);
            }
        }
    }

    /**
     * Processes the body of a PlainSelect type. This method optimizes JOIN and FROM item processing logic.
     *
     * @param plainSelect the query
     */
    public void processPlainSelect(PlainSelect plainSelect) {
        if (!orderByHashParameters(plainSelect.getOrderByElements())) {
            plainSelect.setOrderByElements(null);
        }

        if (plainSelect.getFromItem() != null) {
            processFromItem(plainSelect.getFromItem());
        }

        List<Join> joins = plainSelect.getJoins();
        if (joins != null) {
            for (Join join : joins) {
                if (join.getRightItem() != null) {
                    processFromItem(join.getRightItem());
                }
            }
        }
    }

    /**
     * Processes the WITH clause to remove unnecessary ORDER BY clauses.
     * <ol>
     * <li>Uses List&lt;WithItem&lt;?&gt;&gt; to adapt to JSqlParser 5.1's generic API.</li>
     * <li>Checks for keepSubSelectOrderBy and empty lists early to reduce unnecessary loops.</li>
     * <li>Uses a for-each loop to reduce iterator creation.</li>
     * <li>Checks for non-null select early to reduce invalid recursion.</li>
     * </ol>
     *
     * @param withItemsList the list of WITH clauses
     */
    public void processWithItemsList(List<WithItem<?>> withItemsList) {
        if (withItemsList == null || withItemsList.isEmpty() || keepSubSelectOrderBy()) {
            return; // Exit early to avoid unnecessary loops
        }

        for (WithItem<?> item : withItemsList) {
            Select select = item.getSelect();
            if (select != null) {
                processSelect(select);
            }
        }
    }

    /**
     * Processes subqueries in the FROM clause. This method optimizes type checking and recursive logic.
     *
     * @param fromItem the FROM clause item
     */
    public void processFromItem(FromItem fromItem) {
        if (fromItem instanceof ParenthesedSelect) {
            ParenthesedSelect parenthesedSelect = (ParenthesedSelect) fromItem;
            Select select = parenthesedSelect.getSelect();
            if (select != null && !keepSubSelectOrderBy()) {
                processSelect(select);
            }
        } else if (fromItem instanceof Select) {
            processSelect((Select) fromItem);
        } else if (fromItem instanceof ParenthesedFromItem) {
            processFromItem(((ParenthesedFromItem) fromItem).getFromItem());
        }
    }

    /**
     * Checks if the ORDER BY clause should be kept. This method uses the configuration from {@link PageMethod}.
     *
     * @return true if ORDER BY should be kept, false otherwise
     */
    protected boolean keepOrderBy() {
        return PageMethod.getLocalPage() != null && PageMethod.getLocalPage().keepOrderBy();
    }

    /**
     * Checks if the ORDER BY clause of subqueries should be kept.
     *
     * @return true if subquery ORDER BY should be kept, false otherwise
     */
    protected boolean keepSubSelectOrderBy() {
        return PageMethod.getLocalPage() != null && PageMethod.getLocalPage().keepSubSelectOrderBy();
    }

    /**
     * Determines if the ORDER BY clause contains parameters (?). This method optimizes null checks and loop efficiency.
     *
     * @param orderByElements the list of ORDER BY elements
     * @return true if it contains parameters, false otherwise
     */
    public boolean orderByHashParameters(List<OrderByElement> orderByElements) {
        if (orderByElements == null || orderByElements.isEmpty()) {
            return false;
        }

        for (OrderByElement orderByElement : orderByElements) {
            if (orderByElement.toString().contains("?")) {
                return true;
            }
        }
        return false;
    }

}
