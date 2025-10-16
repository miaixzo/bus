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

import java.util.List;
import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.Context;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.ReflectKit;
import org.miaixz.bus.mapper.handler.MapperBoundSql;
import org.miaixz.bus.mapper.handler.MapperHandler;
import org.miaixz.bus.mapper.handler.MapperStatementHandler;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

/**
 * Multi-tenancy handler, responsible for adding tenant conditions to SQL statements. This handler intercepts various
 * SQL operations (SELECT, INSERT, UPDATE, DELETE) and modifies them to include tenant-specific filtering or data
 * manipulation based on the configured {@link TenantProvider}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TenantHandler extends ConditionHandler implements MapperHandler {

    /**
     * The tenant service provider, used to obtain tenant-related information.
     */
    private TenantProvider provider;

    /**
     * Handles query operations by adding tenant conditions to SELECT statements. The SQL is parsed and modified to
     * include tenant-specific WHERE clauses.
     *
     * @param object          The result object (unused).
     * @param executor        The MyBatis executor.
     * @param mappedStatement The MappedStatement.
     * @param parameter       The query parameters.
     * @param rowBounds       The pagination parameters.
     * @param resultHandler   The result handler.
     * @param boundSql        The BoundSql object.
     */
    @Override
    public void query(
            Object object,
            Executor executor,
            MappedStatement mappedStatement,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {
        // Get MapperBoundSql object
        MapperBoundSql mbs = mapperBoundSql(boundSql);
        // Parse and add tenant conditions
        mbs.sql(parserSingle(mbs.sql(), null));
    }

    /**
     * Pre-processes INSERT, UPDATE, and DELETE statements to add tenant conditions. This method is called before the
     * statement is executed.
     *
     * @param statementHandler The StatementHandler.
     */
    @Override
    public void prepare(StatementHandler statementHandler) {
        // Get MapperStatementHandler object
        MapperStatementHandler msh = mapperStatementHandler(statementHandler);
        // Get MapperBoundSql object
        MapperBoundSql mbs = msh.mapperBoundSql();
        // Parse and add tenant conditions
        mbs.sql(parserMulti(mbs.sql(), null));
    }

    /**
     * Processes a SELECT statement, adding tenant conditions to its WHERE clause.
     *
     * @param select The SELECT statement.
     * @param index  The statement index (unused).
     * @param sql    The original SQL string.
     * @param object The WHERE clause segment (as a string).
     */
    @Override
    protected void processSelect(Select select, int index, String sql, Object object) {
        // Get WHERE clause segment
        final String whereSegment = (String) object;
        // Process SELECT statement body
        processSelectBody(select, whereSegment);
        // Get WITH clause list
        List<WithItem<?>> withItemsList = select.getWithItemsList();
        // Process SELECT in WITH clause
        if (!CollKit.isEmpty(withItemsList)) {
            withItemsList.forEach(withItem -> processSelectBody(withItem.getSelect(), whereSegment));
        }
    }

    /**
     * Processes an INSERT statement, adding tenant columns and values.
     *
     * @param insert The INSERT statement.
     * @param index  The statement index (unused).
     * @param sql    The original SQL string.
     * @param object The WHERE clause segment (unused).
     * @throws InternalException if multi-table update is attempted without proper exclusion.
     */
    @Override
    protected void processInsert(Insert insert, int index, String sql, Object object) {
        // Ignore tables that are exempt from tenant conditions
        if (this.provider.ignore(insert.getTable().getName())) {
            return;
        }
        // Get the list of columns for the INSERT
        List<Column> columns = insert.getColumns();
        // Do not process INSERT statements without column names
        if (CollKit.isEmpty(columns)) {
            return;
        }
        // Get the tenant column name
        String tenantIdColumn = this.provider.getColumn();
        // Do not process INSERT if the tenant column is already included
        if (this.provider.ignore(columns, tenantIdColumn)) {
            return;
        }
        // Add the tenant column
        columns.add(new Column(tenantIdColumn));
        // Get the tenant ID expression
        Expression tenantId = this.provider.getTenantId();
        // Get the columns for ON DUPLICATE KEY UPDATE
        List<UpdateSet> duplicateUpdateColumns = insert.getDuplicateUpdateSets();
        // Process ON DUPLICATE KEY UPDATE
        if (CollKit.isNotEmpty(duplicateUpdateColumns)) {
            duplicateUpdateColumns.add(new UpdateSet(new Column(tenantIdColumn), tenantId));
        }

        // Get the SELECT subquery of the INSERT
        Select select = insert.getSelect();
        // Process INSERT INTO ... SELECT
        if (select instanceof PlainSelect) {
            this.processInsertSelect(select, (String) object);
            // Process the VALUES clause of the INSERT
        } else if (insert.getValues() != null) {
            Values values = insert.getValues();
            ExpressionList<Expression> expressions = (ExpressionList<Expression>) values.getExpressions();
            // Handle parenthesized expressions
            if (expressions instanceof ParenthesedExpressionList) {
                expressions.addExpression(tenantId);
            } else {
                // Handle non-empty expression lists
                if (CollKit.isNotEmpty(expressions)) {
                    for (Expression expression : expressions) {
                        if (expression instanceof ParenthesedExpressionList) {
                            ((ParenthesedExpressionList<Expression>) expression).addExpression(tenantId);
                        } else {
                            expressions.add(tenantId);
                        }
                    }
                    // Add tenant ID if the list is empty
                } else {
                    expressions.add(tenantId);
                }
            }
            // Throw an exception for unsupported multi-table updates
        } else {
            throw new InternalException(
                    "Failed to process multiple-table update, please exclude the tableName or statementId");
        }
    }

    /**
     * Processes an UPDATE statement, adding tenant conditions to its WHERE clause.
     *
     * @param update The UPDATE statement.
     * @param index  The statement index (unused).
     * @param sql    The original SQL string.
     * @param object The WHERE clause segment (as a string).
     */
    @Override
    protected void processUpdate(Update update, int index, String sql, Object object) {
        // Get the table object
        final Table table = update.getTable();
        // Ignore tables that are exempt from tenant conditions
        if (this.provider.ignore(table.getName())) {
            return;
        }
        // Get the SET clause of the UPDATE
        List<UpdateSet> sets = update.getUpdateSets();
        // Process SELECT subqueries in the SET clause
        if (!CollKit.isEmpty(sets)) {
            sets.forEach(us -> us.getValues().forEach(ex -> {
                if (ex instanceof Select) {
                    processSelectBody(((Select) ex), (String) object);
                }
            }));
        }
        // Add tenant condition to the WHERE clause
        update.setWhere(this.andExpression(table, update.getWhere(), (String) object));
    }

    /**
     * Processes a DELETE statement, adding tenant conditions to its WHERE clause.
     *
     * @param delete The DELETE statement.
     * @param index  The statement index (unused).
     * @param sql    The original SQL string.
     * @param object The WHERE clause segment (as a string).
     */
    @Override
    protected void processDelete(Delete delete, int index, String sql, Object object) {
        // Ignore tables that are exempt from tenant conditions
        if (this.provider.ignore(delete.getTable().getName())) {
            return;
        }
        // Add tenant condition to the WHERE clause
        delete.setWhere(this.andExpression(delete.getTable(), delete.getWhere(), (String) object));
    }

    /**
     * Processes an INSERT INTO ... SELECT statement, ensuring the SELECT subquery includes tenant conditions.
     *
     * @param select  The SELECT statement body.
     * @param segment The WHERE clause segment (as a string).
     */
    protected void processInsertSelect(Select select, final String segment) {
        // Process simple SELECT
        if (select instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) select;
            FromItem fromItem = plainSelect.getFromItem();
            if (fromItem instanceof Table) {
                processPlainSelect(plainSelect, segment);
                appendSelectItem(plainSelect.getSelectItems());
                // Recursively process subqueries
            } else if (fromItem instanceof Select) {
                Select subSelect = (Select) fromItem;
                appendSelectItem(plainSelect.getSelectItems());
                processInsertSelect(subSelect, segment);
            }
            // Process SELECT within parentheses
        } else if (select instanceof ParenthesedSelect) {
            ParenthesedSelect parenthesedSelect = (ParenthesedSelect) select;
            processInsertSelect(parenthesedSelect.getSelect(), segment);
        }
    }

    /**
     * Appends the tenant column to the SELECT clause of a SELECT statement.
     *
     * @param selectItems The list of select items in the SELECT clause.
     */
    protected void appendSelectItem(List<SelectItem<?>> selectItems) {
        // Do not process empty lists
        if (CollKit.isEmpty(selectItems)) {
            return;
        }
        // Do not append if SELECT * is used
        if (selectItems.size() == 1) {
            SelectItem<?> item = selectItems.get(0);
            Expression expression = item.getExpression();
            if (expression instanceof AllColumns) {
                return;
            }
        }
        // Add the tenant column
        selectItems.add(new SelectItem<>(new Column(this.provider.getColumn())));
    }

    /**
     * Retrieves the tenant column name, optionally prefixed with the table alias.
     *
     * @param table The Table object.
     * @return The tenant column as a {@link Column} object (e.g., tenantId or tableAlias.tenantId).
     */
    protected Column getAliasColumn(Table table) {
        // Build the column name
        StringBuilder column = new StringBuilder();
        // Add table alias if it exists
        if (table.getAlias() != null) {
            column.append(table.getAlias().getName()).append(Symbol.DOT);
        }
        // Add the tenant column name
        column.append(this.provider.getColumn());
        // Return the Column object
        return new Column(column.toString());
    }

    /**
     * Sets the properties for the handler, initializing the tenant service provider.
     *
     * @param properties The configuration properties.
     * @return {@code true} if properties were set successfully.
     */
    @Override
    public boolean setProperties(Properties properties) {
        // Initialize the tenant service
        Context.newInstance(properties).whenNotBlank("provider", ReflectKit::newInstance, this::setProvider);
        // Return true for successful setup
        return true;
    }

    /**
     * Builds the tenant condition expression (e.g., `tenant_id = ?`).
     *
     * @param table   The Table object.
     * @param where   The current WHERE condition.
     * @param segment The full Mapper path (unused).
     * @return The tenant condition expression, or {@code null} if the table is ignored.
     */
    @Override
    public Expression buildTableExpression(final Table table, final Expression where, final String segment) {
        // Ignore tables that are exempt from tenant conditions
        if (this.provider.ignore(table.getName())) {
            return null;
        }
        // Build the tenant_id = ? condition
        return new EqualsTo(getAliasColumn(table), this.provider.getTenantId());
    }

    /**
     * Retrieves the tenant service provider.
     *
     * @return The {@link TenantProvider} instance.
     */
    public TenantProvider getProvider() {
        return this.provider;
    }

    /**
     * Sets the tenant service provider.
     *
     * @param provider The {@link TenantProvider} instance to set.
     */
    public void setProvider(TenantProvider provider) {
        this.provider = provider;
    }

}
