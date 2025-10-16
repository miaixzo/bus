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

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.mapper.handler.MapperBoundSql;
import org.miaixz.bus.mapper.handler.MapperHandler;
import org.miaixz.bus.mapper.handler.MapperStatementHandler;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.update.Update;

/**
 * Data permission handler for processing data permission controls in SQL statements. This handler dynamically adds
 * permission conditions to SQL queries.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PermissionHandler extends ConditionHandler implements MapperHandler {

    /**
     * The data permission provider, used to generate permission-related SQL segments.
     */
    private PermissionProvider provider;

    /**
     * Pre-processes SQL statements, dynamically adding permission conditions to UPDATE and DELETE statements.
     *
     * @param statementHandler the MyBatis StatementHandler
     */
    @Override
    public void prepare(StatementHandler statementHandler) {
        MapperStatementHandler mapperStatementHandler = mapperStatementHandler(statementHandler);
        MappedStatement ms = mapperStatementHandler.mappedStatement();
        SqlCommandType sct = ms.getSqlCommandType();
        if (sct == SqlCommandType.UPDATE || sct == SqlCommandType.DELETE) {
            MapperBoundSql mpBs = mapperStatementHandler.mapperBoundSql();
            mpBs.sql(parserMulti(mpBs.sql(), ms.getId()));
        }
    }

    /**
     * Handles query processing, dynamically adding permission conditions to SELECT statements.
     *
     * @param result          the query result
     * @param executor        the MyBatis executor
     * @param mappedStatement the MappedStatement
     * @param parameter       the query parameters
     * @param rowBounds       the pagination parameters
     * @param resultHandler   the result handler
     * @param boundSql        the bound SQL
     */
    @Override
    public void query(
            Object result,
            Executor executor,
            MappedStatement mappedStatement,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {
        MapperBoundSql mpBs = mapperBoundSql(boundSql);
        mpBs.sql(parserSingle(mpBs.sql(), mappedStatement.getId()));
    }

    /**
     * Processes an UPDATE statement, adding permission conditions.
     *
     * @param update the UPDATE statement object
     * @param index  the SQL index
     * @param sql    the original SQL
     * @param obj    additional parameters (usually the mapping ID)
     */
    @Override
    protected void processUpdate(Update update, int index, String sql, Object obj) {
        final Expression sqlSegment = getUpdateOrDeleteExpression(update.getTable(), update.getWhere(), (String) obj);
        if (null != sqlSegment) {
            update.setWhere(sqlSegment);
        }
    }

    /**
     * Processes a DELETE statement, adding permission conditions.
     *
     * @param delete the DELETE statement object
     * @param index  the SQL index
     * @param sql    the original SQL
     * @param obj    additional parameters (usually the mapping ID)
     */
    @Override
    protected void processDelete(Delete delete, int index, String sql, Object obj) {
        final Expression sqlSegment = getUpdateOrDeleteExpression(delete.getTable(), delete.getWhere(), (String) obj);
        if (null != sqlSegment) {
            delete.setWhere(sqlSegment);
        }
    }

    /**
     * Processes a SELECT statement, adding permission conditions.
     *
     * @param select the SELECT statement object
     * @param index  the SQL index
     * @param sql    the original SQL
     * @param obj    additional parameters (usually the mapping ID)
     */
    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        if (this.provider == null) {
            return;
        }
        if (this.provider instanceof PermissionProvider) {
            final String whereSegment = (String) obj;
            processSelectBody(select, whereSegment);
            List<WithItem<?>> withItemsList = select.getWithItemsList();
            if (!CollKit.isEmpty(withItemsList)) {
                withItemsList.forEach(withItem -> processSelectBody(withItem.getSelect(), whereSegment));
            }
        }
    }

    /**
     * Builds a table-level permission expression.
     *
     * @param table        the table object
     * @param where        the original WHERE condition
     * @param whereSegment the permission condition segment
     * @return the combined permission expression
     */
    @Override
    public Expression buildTableExpression(final Table table, final Expression where, final String whereSegment) {
        if (this.provider == null) {
            return null;
        }
        return this.provider.getSqlSegment(table, where, whereSegment);
    }

    /**
     * Retrieves the data permission provider.
     *
     * @return the data permission provider
     */
    public PermissionProvider getProvider() {
        return this.provider;
    }

    /**
     * Sets the data permission provider.
     *
     * @param provider the data permission provider
     */
    public void setProvider(PermissionProvider provider) {
        this.provider = provider;
    }

    /**
     * Retrieves the permission expression for an UPDATE or DELETE statement.
     *
     * @param table        the table object
     * @param where        the original WHERE condition
     * @param whereSegment the permission condition segment
     * @return the combined permission expression
     */
    protected Expression getUpdateOrDeleteExpression(
            final Table table,
            final Expression where,
            final String whereSegment) {
        if (this.provider == null) {
            return null;
        }
        return andExpression(table, where, whereSegment);
    }

}
