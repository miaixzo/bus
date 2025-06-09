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
 * 数据权限处理器 用于处理SQL语句中的数据权限控制，动态添加权限条件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PermissionsHandler extends ConditionHandler implements MapperHandler {

    /**
     * 数据权限提供者，用于生成权限相关的SQL片段
     */
    private PermissionsProvider provider;

    /**
     * 预处理SQL语句，针对UPDATE和DELETE语句动态添加权限条件
     *
     * @param statementHandler MyBatis的语句处理器
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
     * 查询处理，针对SELECT语句动态添加权限条件
     *
     * @param result          查询结果
     * @param executor        MyBatis执行器
     * @param mappedStatement 映射语句
     * @param parameter       查询参数
     * @param rowBounds       分页参数
     * @param resultHandler   结果处理器
     * @param boundSql        绑定SQL
     */
    @Override
    public void query(Object result, Executor executor, MappedStatement mappedStatement, Object parameter,
            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        MapperBoundSql mpBs = mapperBoundSql(boundSql);
        mpBs.sql(parserSingle(mpBs.sql(), mappedStatement.getId()));
    }

    /**
     * 处理UPDATE语句，添加权限条件
     *
     * @param update UPDATE语句对象
     * @param index  SQL索引
     * @param sql    原始SQL
     * @param obj    附加参数（通常为映射ID）
     */
    @Override
    protected void processUpdate(Update update, int index, String sql, Object obj) {
        final Expression sqlSegment = getUpdateOrDeleteExpression(update.getTable(), update.getWhere(), (String) obj);
        if (null != sqlSegment) {
            update.setWhere(sqlSegment);
        }
    }

    /**
     * 处理DELETE语句，添加权限条件
     *
     * @param delete DELETE语句对象
     * @param index  SQL索引
     * @param sql    原始SQL
     * @param obj    附加参数（通常为映射ID）
     */
    @Override
    protected void processDelete(Delete delete, int index, String sql, Object obj) {
        final Expression sqlSegment = getUpdateOrDeleteExpression(delete.getTable(), delete.getWhere(), (String) obj);
        if (null != sqlSegment) {
            delete.setWhere(sqlSegment);
        }
    }

    /**
     * 处理SELECT语句，添加权限条件
     *
     * @param select SELECT语句对象
     * @param index  SQL索引
     * @param sql    原始SQL
     * @param obj    附加参数（通常为映射ID）
     */
    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        if (this.provider == null) {
            return;
        }
        if (this.provider instanceof PermissionsProvider) {
            final String whereSegment = (String) obj;
            processSelectBody(select, whereSegment);
            List<WithItem<?>> withItemsList = select.getWithItemsList();
            if (!CollKit.isEmpty(withItemsList)) {
                withItemsList.forEach(withItem -> processSelectBody(withItem.getSelect(), whereSegment));
            }
        }
    }

    /**
     * 构建表级权限表达式
     *
     * @param table        表对象
     * @param where        原始WHERE条件
     * @param whereSegment 权限条件片段
     * @return 组合后的权限表达式
     */
    @Override
    public Expression buildTableExpression(final Table table, final Expression where, final String whereSegment) {
        if (this.provider == null) {
            return null;
        }
        return this.provider.getSqlSegment(table, where, whereSegment);
    }

    /**
     * 获取数据权限提供者
     *
     * @return 数据权限提供者
     */
    public PermissionsProvider getProvider() {
        return this.provider;
    }

    /**
     * 设置数据权限提供者
     *
     * @param provider 数据权限提供者
     */
    public void setProvider(PermissionsProvider provider) {
        this.provider = provider;
    }

    /**
     * 获取UPDATE或DELETE语句的权限表达式
     *
     * @param table        表对象
     * @param where        原始WHERE条件
     * @param whereSegment 权限条件片段
     * @return 组合后的权限表达式
     */
    protected Expression getUpdateOrDeleteExpression(final Table table, final Expression where,
            final String whereSegment) {
        if (this.provider == null) {
            return null;
        }
        return andExpression(table, where, whereSegment);
    }

}