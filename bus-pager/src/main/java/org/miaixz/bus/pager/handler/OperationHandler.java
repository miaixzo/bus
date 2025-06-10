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
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

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
import org.miaixz.bus.mapper.handler.MapperHandler;

/**
 * 操作：防止全表更新与删除
 *
 * @param <T> 泛型参数
 * @author Kimi Liu
 * @since Java 17+
 */
public class OperationHandler<T> extends SqlParserHandler implements MapperHandler<T> {

    /**
     * 准备 SQL 语句，检查 UPDATE 和 DELETE 语句是否包含 WHERE 条件，防止全表操作。
     *
     * @param statementHandler MyBatis 语句处理器
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
     * 处理 DELETE 语句，检查是否包含 WHERE 条件。
     *
     * @param delete DELETE 语句对象
     * @param index  索引
     * @param sql    原始 SQL 语句
     * @param obj    附加对象
     */
    @Override
    protected void processDelete(Delete delete, int index, String sql, Object obj) {
        this.checkWhere(delete.getTable().getName(), delete.getWhere(), "Prohibition of full table deletion");
    }

    /**
     * 处理 UPDATE 语句，检查是否包含 WHERE 条件。
     *
     * @param update UPDATE 语句对象
     * @param index  索引
     * @param sql    原始 SQL 语句
     * @param obj    附加对象
     */
    @Override
    protected void processUpdate(Update update, int index, String sql, Object obj) {
        this.checkWhere(update.getTable().getName(), update.getWhere(), "Prohibition of table update operation");
    }

    /**
     * 检查 WHERE 条件是否有效，防止全表操作。
     *
     * @param tableName 表名
     * @param where     WHERE 条件表达式
     * @param ex        异常信息
     * @throws IllegalArgumentException 如果 WHERE 条件无效
     */
    protected void checkWhere(String tableName, Expression where, String ex) {
        Assert.isFalse(this.fullMatch(where, this.getTableLogicField(tableName)), ex);
    }

    /**
     * 检查 WHERE 条件是否为全表匹配。
     *
     * @param where      WHERE 条件表达式
     * @param logicField 逻辑删除字段
     * @return 是否为全表匹配
     */
    private boolean fullMatch(Expression where, String logicField) {
        if (where == null) {
            return true;
        }
        if (StringKit.isNotBlank(logicField)) {
            if (where instanceof BinaryExpression) {
                BinaryExpression binaryExpression = (BinaryExpression) where;
                if (StringKit.equals(binaryExpression.getLeftExpression().toString(), logicField)
                        || StringKit.equals(binaryExpression.getRightExpression().toString(), logicField)) {
                    return true;
                }
            }

            if (where instanceof IsNullExpression) {
                IsNullExpression binaryExpression = (IsNullExpression) where;
                if (StringKit.equals(binaryExpression.getLeftExpression().toString(), logicField)) {
                    return true;
                }
            }
        }

        if (where instanceof EqualsTo) {
            EqualsTo equalsTo = (EqualsTo) where;
            return StringKit.equals(equalsTo.getLeftExpression().toString(), equalsTo.getRightExpression().toString());
        } else if (where instanceof NotEqualsTo) {
            NotEqualsTo notEqualsTo = (NotEqualsTo) where;
            return !StringKit.equals(notEqualsTo.getLeftExpression().toString(),
                    notEqualsTo.getRightExpression().toString());
        } else if (where instanceof OrExpression) {
            OrExpression orExpression = (OrExpression) where;
            return fullMatch(orExpression.getLeftExpression(), logicField)
                    || fullMatch(orExpression.getRightExpression(), logicField);
        } else if (where instanceof AndExpression) {
            AndExpression andExpression = (AndExpression) where;
            return fullMatch(andExpression.getLeftExpression(), logicField)
                    && fullMatch(andExpression.getRightExpression(), logicField);
        } else if (where instanceof ParenthesedExpressionList) {
            ParenthesedExpressionList<Expression> parenthesis = (ParenthesedExpressionList<Expression>) where;
            return fullMatch(parenthesis.get(0), logicField);
        }

        return false;
    }

    /**
     * 获取表中的逻辑删除字段。
     *
     * @param tableName 表名
     * @return 逻辑删除字段，默认为空字符串
     */
    private String getTableLogicField(String tableName) {
        return Normal.EMPTY;
    }

}