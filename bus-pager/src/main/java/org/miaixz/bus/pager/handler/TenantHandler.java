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
import org.apache.ibatis.mapping.SqlCommandType;
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
 * 多租户处理器，负责在 SQL 中添加租户条件。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TenantHandler extends ConditionHandler implements MapperHandler {

    /**
     * 租户服务接口，用于获取租户相关信息
     */
    private TenantService tenantService;

    /**
     * 处理查询操作，在 SELECT 语句中添加租户条件。
     *
     * @param object        结果对象（未使用）
     * @param executor      MyBatis 执行器
     * @param ms            映射语句
     * @param parameter     查询参数
     * @param rowBounds     分页参数
     * @param resultHandler 结果处理器
     * @param boundSql      绑定 SQL
     */
    @Override
    public void query(Object object, Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
            ResultHandler resultHandler, BoundSql boundSql) {
        // 获取 MapperBoundSql 对象
        MapperBoundSql mpBs = mpBoundSql(boundSql);
        // 解析并添加租户条件
        mpBs.sql(parserSingle(mpBs.sql(), null));
    }

    /**
     * 预处理 INSERT、UPDATE、DELETE 语句，添加租户条件。
     *
     * @param statementHandler 语句处理器
     */
    @Override
    public void prepare(StatementHandler statementHandler) {
        // 获取 MapperStatementHandler 对象
        MapperStatementHandler mpSh = mpStatementHandler(statementHandler);
        // 获取映射语句
        MappedStatement ms = mpSh.mappedStatement();
        // 获取 SQL 命令类型
        SqlCommandType sct = ms.getSqlCommandType();
        // 检查是否为 INSERT、UPDATE 或 DELETE 操作
        if (sct == SqlCommandType.INSERT || sct == SqlCommandType.UPDATE || sct == SqlCommandType.DELETE) {
            // 获取 MapperBoundSql 对象
            MapperBoundSql mpBs = mpSh.mPBoundSql();
            // 解析并添加租户条件
            mpBs.sql(parserMulti(mpBs.sql(), null));
        }
    }

    /**
     * 处理 SELECT 语句，添加租户条件到 WHERE 子句。
     *
     * @param select SELECT 语句
     * @param index  语句索引（未使用）
     * @param sql    原始 SQL
     * @param obj    WHERE 片段（字符串形式）
     */
    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        // 获取 WHERE 片段
        final String whereSegment = (String) obj;
        // 处理 SELECT 语句体
        processSelectBody(select, whereSegment);
        // 获取 WITH 子句列表
        List<WithItem<?>> withItemsList = select.getWithItemsList();
        // 处理 WITH 子句中的 SELECT
        if (!CollKit.isEmpty(withItemsList)) {
            withItemsList.forEach(withItem -> processSelectBody(withItem.getSelect(), whereSegment));
        }
    }

    /**
     * 处理 INSERT 语句，添加租户列和值。
     *
     * @param insert INSERT 语句
     * @param index  语句索引（未使用）
     * @param sql    原始 SQL
     * @param obj    WHERE 片段（未使用）
     */
    @Override
    protected void processInsert(Insert insert, int index, String sql, Object obj) {
        // 忽略租户条件的表
        if (tenantService.ignoreTenantCondition(insert.getTable().getName())) {
            return;
        }
        // 获取 INSERT 的列列表
        List<Column> columns = insert.getColumns();
        // 无列名的 INSERT 不处理
        if (CollKit.isEmpty(columns)) {
            return;
        }
        // 获取租户列名
        String tenantIdColumn = tenantService.getTenantColumn();
        // 已包含租户列的 INSERT 不处理
        if (tenantService.ignoreTenantInsert(columns, tenantIdColumn)) {
            return;
        }
        // 添加租户列
        columns.add(new Column(tenantIdColumn));
        // 获取租户 ID
        Expression tenantId = tenantService.getTenantId();
        // 获取 ON DUPLICATE KEY UPDATE 的列
        List<UpdateSet> duplicateUpdateColumns = insert.getDuplicateUpdateSets();
        // 处理 ON DUPLICATE KEY UPDATE
        if (CollKit.isNotEmpty(duplicateUpdateColumns)) {
            duplicateUpdateColumns.add(new UpdateSet(new Column(tenantIdColumn), tenantId));
        }

        // 获取 INSERT 的 SELECT 子查询
        Select select = insert.getSelect();
        // 处理 INSERT INTO ... SELECT
        if (select instanceof PlainSelect) {
            this.processInsertSelect(select, (String) obj);
            // 处理 INSERT 的 VALUES 子句
        } else if (insert.getValues() != null) {
            Values values = insert.getValues();
            ExpressionList<Expression> expressions = (ExpressionList<Expression>) values.getExpressions();
            // 处理括号表达式
            if (expressions instanceof ParenthesedExpressionList) {
                expressions.addExpression(tenantId);
            } else {
                // 处理非空表达式列表
                if (CollKit.isNotEmpty(expressions)) {
                    int len = expressions.size();
                    for (int i = 0; i < len; i++) {
                        Expression expression = expressions.get(i);
                        if (expression instanceof ParenthesedExpressionList) {
                            ((ParenthesedExpressionList<Expression>) expression).addExpression(tenantId);
                        } else {
                            expressions.add(tenantId);
                        }
                    }
                    // 添加租户 ID
                } else {
                    expressions.add(tenantId);
                }
            }
            // 抛出异常，提示不支持多表更新
        } else {
            throw new InternalException(
                    "Failed to process multiple-table update, please exclude the tableName or statementId");
        }
    }

    /**
     * 处理 UPDATE 语句，添加租户条件到 WHERE 子句。
     *
     * @param update UPDATE 语句
     * @param index  语句索引（未使用）
     * @param sql    原始 SQL
     * @param obj    WHERE 片段（字符串形式）
     */
    @Override
    protected void processUpdate(Update update, int index, String sql, Object obj) {
        // 获取表对象
        final Table table = update.getTable();
        // 忽略租户条件的表
        if (tenantService.ignoreTenantCondition(table.getName())) {
            return;
        }
        // 获取 UPDATE 的 SET 子句
        List<UpdateSet> sets = update.getUpdateSets();
        // 处理 SET 中的 SELECT 子查询
        if (!CollKit.isEmpty(sets)) {
            sets.forEach(us -> us.getValues().forEach(ex -> {
                if (ex instanceof Select) {
                    processSelectBody(((Select) ex), (String) obj);
                }
            }));
        }
        // 添加租户条件到 WHERE
        update.setWhere(this.andExpression(table, update.getWhere(), (String) obj));
    }

    /**
     * 处理 DELETE 语句，添加租户条件到 WHERE 子句。
     *
     * @param delete DELETE 语句
     * @param index  语句索引（未使用）
     * @param sql    原始 SQL
     * @param obj    WHERE 片段（字符串形式）
     */
    @Override
    protected void processDelete(Delete delete, int index, String sql, Object obj) {
        // 忽略租户条件的表
        if (tenantService.ignoreTenantCondition(delete.getTable().getName())) {
            return;
        }
        // 添加租户条件到 WHERE
        delete.setWhere(this.andExpression(delete.getTable(), delete.getWhere(), (String) obj));
    }

    /**
     * 处理 INSERT INTO ... SELECT 语句，确保 SELECT 子查询包含租户条件。
     *
     * @param selectBody   SELECT 语句体
     * @param whereSegment WHERE 片段（字符串形式）
     */
    protected void processInsertSelect(Select selectBody, final String whereSegment) {
        // 处理简单 SELECT
        if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            FromItem fromItem = plainSelect.getFromItem();
            if (fromItem instanceof Table) {
                processPlainSelect(plainSelect, whereSegment);
                appendSelectItem(plainSelect.getSelectItems());
                // 递归处理子查询
            } else if (fromItem instanceof Select) {
                Select subSelect = (Select) fromItem;
                appendSelectItem(plainSelect.getSelectItems());
                processInsertSelect(subSelect, whereSegment);
            }
            // 处理括号内的 SELECT
        } else if (selectBody instanceof ParenthesedSelect) {
            ParenthesedSelect parenthesedSelect = (ParenthesedSelect) selectBody;
            processInsertSelect(parenthesedSelect.getSelect(), whereSegment);
        }
    }

    /**
     * 追加租户列到 SELECT 语句的 SELECT 子句。
     *
     * @param selectItems SELECT 子句的列列表
     */
    protected void appendSelectItem(List<SelectItem<?>> selectItems) {
        // 空列表不处理
        if (CollKit.isEmpty(selectItems)) {
            return;
        }
        // SELECT * 不追加
        if (selectItems.size() == 1) {
            SelectItem<?> item = selectItems.get(0);
            Expression expression = item.getExpression();
            if (expression instanceof AllColumns) {
                return;
            }
        }
        // 添加租户列
        selectItems.add(new SelectItem<>(new Column(tenantService.getTenantColumn())));
    }

    /**
     * 获取租户列名，带表别名（若存在）。
     *
     * @param table 表对象
     * @return 租户列名（如 tenantId 或 tableAlias.tenantId）
     */
    protected Column getAliasColumn(Table table) {
        // 构建列名
        StringBuilder column = new StringBuilder();
        // 添加表别名（如果存在）
        if (table.getAlias() != null) {
            column.append(table.getAlias().getName()).append(Symbol.DOT);
        }
        // 添加租户列名
        column.append(tenantService.getTenantColumn());
        // 返回列对象
        return new Column(column.toString());
    }

    /**
     * 设置处理器属性，初始化租户服务。
     *
     * @param properties 配置属性
     * @return 是否设置成功
     */
    @Override
    public boolean setProperties(Properties properties) {
        // 初始化租户服务
        Context.newInstance(properties).whenNotBlank("tenantService", ReflectKit::newInstance, this::setTenantService);
        // 返回设置成功
        return true;
    }

    /**
     * 构建租户条件表达式（如 tenant_id = ?）。
     *
     * @param table        表对象
     * @param where        当前 WHERE 条件
     * @param whereSegment Mapper 全路径（未使用）
     * @return 租户条件表达式
     */
    @Override
    public Expression buildTableExpression(final Table table, final Expression where, final String whereSegment) {
        // 忽略租户条件的表
        if (tenantService.ignoreTenantCondition(table.getName())) {
            return null;
        }
        // 构建 tenant_id = ? 条件
        return new EqualsTo(getAliasColumn(table), tenantService.getTenantId());
    }

    /**
     * 获取租户服务实例。
     *
     * @return 租户服务实例
     */
    public TenantService getTenantService() {
        return tenantService;
    }

    /**
     * 设置租户服务实例。
     *
     * @param tenantService 租户服务实例
     */
    public void setTenantService(TenantService tenantService) {
        this.tenantService = tenantService;
    }

}