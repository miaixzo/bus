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

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.builder.EntityClassBuilder;
import org.miaixz.bus.mapper.handler.MapperHandler;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.update.Update;

/**
 * Operation handler to prevent full table updates and deletes. This handler intercepts UPDATE and DELETE statements to
 * ensure they have a valid WHERE clause, preventing accidental modification or deletion of all records in a table.
 *
 * @param <T> the generic type parameter
 * @author Kimi Liu
 * @since Java 17+
 */
public class OperationHandler<T> extends SqlParserHandler implements MapperHandler<T> {

    /**
     * Prepares the SQL statement, checking UPDATE and DELETE statements for a WHERE clause to prevent full table
     * operations.
     *
     * @param statementHandler the MyBatis StatementHandler
     */
    @Override
    public void prepare(StatementHandler statementHandler) {
        MetaObject metaObject = getMetaObject(statementHandler);
        MappedStatement ms = getMappedStatement(metaObject);
        SqlCommandType sct = ms.getSqlCommandType();
        if (sct == SqlCommandType.UPDATE || sct == SqlCommandType.DELETE) {
            BoundSql boundSql = (BoundSql) metaObject.getValue(DELEGATE_BOUNDSQL);
            parserMulti(boundSql.getSql(), null);
        }
    }

    /**
     * Processes a DELETE statement, checking if it includes a WHERE clause.
     *
     * @param delete the DELETE statement object
     * @param index  the index of the statement
     * @param sql    the original SQL statement
     * @param obj    additional object
     */
    @Override
    protected void processDelete(Delete delete, int index, String sql, Object obj) {
        this.checkWhere(delete.getTable().getName(), delete.getWhere(), "Prohibition of full table deletion");
    }

    /**
     * Processes an UPDATE statement, checking if it includes a WHERE clause.
     *
     * @param update the UPDATE statement object
     * @param index  the index of the statement
     * @param sql    the original SQL statement
     * @param obj    additional object
     */
    @Override
    protected void processUpdate(Update update, int index, String sql, Object obj) {
        this.checkWhere(update.getTable().getName(), update.getWhere(), "Prohibition of table update operation");
    }

    /**
     * Checks if the WHERE clause is valid to prevent full table operations.
     *
     * @param tableName the name of the table
     * @param where     the WHERE clause expression
     * @param ex        the exception message to throw if the check fails
     * @throws IllegalArgumentException if the WHERE clause is invalid
     */
    protected void checkWhere(String tableName, Expression where, String ex) {
        Assert.isFalse(this.fullMatch(where, EntityClassBuilder.getTableLogicColumn()), ex);
    }

    /**
     * Checks if the WHERE clause constitutes a full table match.
     *
     * @param where  the WHERE clause expression
     * @param column the logical delete column
     * @return true if it is a full table match, false otherwise
     */
    private boolean fullMatch(Expression where, String column) {
        if (where == null) {
            return true;
        }
        if (StringKit.isNotBlank(column)) {
            if (where instanceof BinaryExpression) {
                BinaryExpression binaryExpression = (BinaryExpression) where;
                if (StringKit.equals(binaryExpression.getLeftExpression().toString(), column)
                        || StringKit.equals(binaryExpression.getRightExpression().toString(), column)) {
                    return true;
                }
            }

            if (where instanceof IsNullExpression) {
                IsNullExpression binaryExpression = (IsNullExpression) where;
                if (StringKit.equals(binaryExpression.getLeftExpression().toString(), column)) {
                    return true;
                }
            }
        }

        if (where instanceof EqualsTo) {
            EqualsTo equalsTo = (EqualsTo) where;
            return StringKit.equals(equalsTo.getLeftExpression().toString(), equalsTo.getRightExpression().toString());
        } else if (where instanceof NotEqualsTo) {
            NotEqualsTo notEqualsTo = (NotEqualsTo) where;
            return !StringKit
                    .equals(notEqualsTo.getLeftExpression().toString(), notEqualsTo.getRightExpression().toString());
        } else if (where instanceof OrExpression) {
            OrExpression orExpression = (OrExpression) where;
            return fullMatch(orExpression.getLeftExpression(), column)
                    || fullMatch(orExpression.getRightExpression(), column);
        } else if (where instanceof AndExpression) {
            AndExpression andExpression = (AndExpression) where;
            return fullMatch(andExpression.getLeftExpression(), column)
                    && fullMatch(andExpression.getRightExpression(), column);
        } else if (where instanceof ParenthesedExpressionList) {
            ParenthesedExpressionList<Expression> parenthesis = (ParenthesedExpressionList<Expression>) where;
            return fullMatch(parenthesis.get(0), column);
        }

        return false;
    }

}
