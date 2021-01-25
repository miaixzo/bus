/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.starter.mapper;

import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.DateKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

/**
 * @author Kimi Liu
 * @version 6.1.9
 * @since JDK 1.8+
 */
@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
        @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class})
})
public class RecordTimeHandler extends AbstractSqlParserHandler implements Interceptor {

    private static String createDateColumnName;
    private static String updateDateColumnName;
    /**
     * 不需要自动添加日期列的列表，表名必须符合正则表达式，推荐以^开头
     */
    private static List<String> ignoreTableList;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if ("prepare".equals(invocation.getMethod().getName())) {
            StatementHandler handler = realTarget(invocation.getTarget());
            MetaObject metaObject = SystemMetaObject.forObject(handler);
            MappedStatement ms = getMappedStatement(metaObject);
            String sql = metaObject.getValue(DELEGATE_BOUNDSQL_SQL).toString();
            Statement statement = CCJSqlParserUtil.parse(sql);
            SqlCommandType sqlCommandType = ms.getSqlCommandType();
            String currentDate = StringKit.toString(DateKit.timestamp());
            if (SqlCommandType.INSERT == sqlCommandType) {
                Insert insert = (Insert) statement;
                if (!matchesIgnoreTables(insert.getTable().getName())) {
                    boolean isContainsCreateDateColumn = false, isContainsModifyDateColumn = false;
                    int createDateColumnIndex = 0, modifyDateColumnIndex = 0;
                    for (int i = 0; i < insert.getColumns().size(); i++) {
                        Column column = insert.getColumns().get(i);
                        if (column.getColumnName().equals(createDateColumnName)) {
                            // sql中包含了设置的列名，则只需要设置值
                            isContainsCreateDateColumn = true;
                            createDateColumnIndex = i;
                        }

                        if (column.getColumnName().equals(updateDateColumnName)) {
                            isContainsModifyDateColumn = true;
                            modifyDateColumnIndex = i;
                        }
                    }

                    if (isContainsCreateDateColumn) {
                        intoValueWithIndex(createDateColumnIndex, currentDate, insert);
                    } else {
                        intoValue(createDateColumnName, currentDate, insert);
                    }

                    if (isContainsModifyDateColumn) {
                        intoValueWithIndex(modifyDateColumnIndex, currentDate, insert);
                    } else {
                        intoValue(updateDateColumnName, currentDate, insert);
                    }

                    Logger.debug("currentDate insert sql: {}", insert.toString());
                    metaObject.setValue(DELEGATE_BOUNDSQL_SQL, insert.toString());
                }

            } else if (SqlCommandType.UPDATE == sqlCommandType) {
                Update update = (Update) statement;
                Table table = update.getTable();
                if (!matchesIgnoreTables(table.getName())) {
                    boolean isContainsModifyDateColumn = false;
                    int modifyDateColumnIndex = 0;
                    for (int i = 0; i < update.getColumns().size(); i++) {
                        Column column = update.getColumns().get(i);
                        if (column.getColumnName().equals(updateDateColumnName)) {
                            isContainsModifyDateColumn = true;
                            modifyDateColumnIndex = i;
                        }
                    }

                    if (isContainsModifyDateColumn) {
                        updateValueWithIndex(modifyDateColumnIndex, currentDate, update);
                    } else {
                        updateValue(updateDateColumnName, currentDate, update);
                    }

                    Logger.debug("Intercept update sql: {}", update.toString());
                    metaObject.setValue(DELEGATE_BOUNDSQL_SQL, update.toString());
                }
            }
        } else if ("setParameters".equals(invocation.getMethod().getName())) {
            ParameterHandler handler = realTarget(invocation.getTarget());
            MetaObject metaObject = SystemMetaObject.forObject(handler);
            MappedStatement ms = (MappedStatement) metaObject.getValue("mappedStatement");
            SqlCommandType sqlCommandType = ms.getSqlCommandType();
            BoundSql boundSql = (BoundSql) metaObject.getValue("boundSql");
            Statement statement = CCJSqlParserUtil.parse(boundSql.getSql());
            if (SqlCommandType.INSERT == sqlCommandType) {
                Insert insert = (Insert) statement;
                if (!matchesIgnoreTables(insert.getTable().getName())) {
                    handleParameterMapping(boundSql);
                }

            } else if (SqlCommandType.UPDATE == sqlCommandType) {
                Update update = (Update) statement;
                Table table = update.getTable();
                if (!matchesIgnoreTables(table.getName())) {
                    handleParameterMapping(boundSql);
                }
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        createDateColumnName = properties.getProperty("createDateColumnName", "created");
        updateDateColumnName = properties.getProperty("updateDateColumnName", "modified");
        String ignoreTable = properties.getProperty("ignoreTables", Normal.EMPTY);
        if (ignoreTable.length() > 0) {
            String[] tables = ignoreTable.split(Symbol.COMMA);
            ignoreTableList = Arrays.asList(tables);
        } else {
            ignoreTableList = Collections.emptyList();
        }
    }

    /**
     * 解决原始sql语句已包含自动添加的列导致参数数量映射异常的问题
     *
     * @param boundSql 缓存SQL对象
     */
    private void handleParameterMapping(BoundSql boundSql) {
        List<ParameterMapping> parameterMappingList = boundSql.getParameterMappings();
        Iterator<ParameterMapping> it = parameterMappingList.iterator();
        String camelCaseCreateDateProperty = StringKit.toCamelCase(createDateColumnName);
        String camelCaseUpdateDateProperty = StringKit.toCamelCase(updateDateColumnName);
        while (it.hasNext()) {
            ParameterMapping pm = it.next();
            if (pm.getProperty().equals(camelCaseCreateDateProperty)) {
                Logger.debug("原始Sql语句已包含自动添加的列: {}", createDateColumnName);
                it.remove();
            }
            if (pm.getProperty().equals(camelCaseUpdateDateProperty)) {
                Logger.debug("原始Sql语句已包含自动添加的列: {}", updateDateColumnName);
                it.remove();
            }
        }
    }

    /**
     * 忽略处理配置的表
     *
     * @param tableName 当前执行的sql表
     * @return true：表示匹配忽略的表，false：表示不匹配忽略的表
     */
    private boolean matchesIgnoreTables(String tableName) {
        for (String ignoreTable : ignoreTableList) {
            if (tableName.matches(ignoreTable)) {
                return true;
            }
        }
        return false;
    }

    private void updateValueWithIndex(int modifyDateColumnIndex, String currentDate, Update update) {
        update.getExpressions().set(modifyDateColumnIndex, new QuotationTimestampValue(currentDate));
    }

    private void updateValue(String updateDateColumnName, String currentDate, Update update) {
        // 添加列
        update.getColumns().add(new Column(updateDateColumnName));
        update.getExpressions().add(new QuotationTimestampValue(currentDate));
    }

    private void intoValueWithIndex(final int index, final String columnValue, Insert insert) {
        // 通过visitor设置对应的值
        if (insert.getItemsList() == null) {
            insert.getSelect().getSelectBody().accept(new PlainSelectVisitor(index, columnValue));
        } else {
            insert.getItemsList().accept(new ItemsListVisitor() {
                @Override
                public void visit(SubSelect subSelect) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void visit(ExpressionList expressionList) {
                    expressionList.getExpressions()
                            .set(index, new QuotationTimestampValue(columnValue));
                }

                @Override
                public void visit(NamedExpressionList namedExpressionList) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void visit(MultiExpressionList multiExpressionList) {
                    for (ExpressionList expressionList : multiExpressionList.getExprList()) {
                        expressionList.getExpressions()
                                .set(index, new QuotationTimestampValue(columnValue));
                    }
                }
            });
        }
    }

    private void intoValue(String columnName, final String columnValue, Insert insert) {
        // 添加列
        insert.getColumns().add(new Column(columnName));
        // 通过visitor设置对应的值
        if (insert.getItemsList() == null) {
            insert.getSelect().getSelectBody().accept(new PlainSelectVisitor(-1, columnValue));
        } else {
            insert.getItemsList().accept(new ItemsListVisitor() {
                @Override
                public void visit(SubSelect subSelect) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void visit(ExpressionList expressionList) {
                    expressionList.getExpressions()
                            .add(new QuotationTimestampValue(columnValue));
                }

                @Override
                public void visit(NamedExpressionList namedExpressionList) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void visit(MultiExpressionList multiExpressionList) {
                    for (ExpressionList expressionList : multiExpressionList.getExprList()) {
                        expressionList.getExpressions()
                                .add(new QuotationTimestampValue(columnValue));
                    }
                }
            });
        }
    }

    /**
     * 支持INSERT INTO SELECT 语句
     */
    private class PlainSelectVisitor implements SelectVisitor {
        int index;
        String columnValue;

        public PlainSelectVisitor(int index, String columnValue) {
            this.index = index;
            this.columnValue = columnValue;
        }

        @Override
        public void visit(PlainSelect plainSelect) {
            if (index != -1) {
                plainSelect.getSelectItems().set(index, new SelectExpressionItem(new QuotationTimestampValue(columnValue)));
            } else {
                plainSelect.getSelectItems().add(new SelectExpressionItem(new QuotationTimestampValue(columnValue)));
            }
        }

        @Override
        public void visit(SetOperationList setOperationList) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void visit(WithItem withItem) {
            if (index != -1) {
                withItem.getWithItemList().set(index, new SelectExpressionItem(new QuotationTimestampValue(columnValue)));
            } else {
                withItem.getWithItemList().add(new SelectExpressionItem(new QuotationTimestampValue(columnValue)));
            }
        }

        @Override
        public void visit(ValuesStatement valuesStatement) {
            if (index != -1) {
                valuesStatement.getExpressions().set(index, new QuotationTimestampValue(columnValue));
            } else {
                valuesStatement.getExpressions().add(index, new QuotationTimestampValue(columnValue));
            }

        }
    }

    public class QuotationTimestampValue extends TimestampValue {

        private final String value;

        public QuotationTimestampValue(String value) {
            super("'" + value + "'");
            this.value = value;
        }

        @Override
        public String toString() {
            return "'" + value.trim() + "'";
        }
    }

}