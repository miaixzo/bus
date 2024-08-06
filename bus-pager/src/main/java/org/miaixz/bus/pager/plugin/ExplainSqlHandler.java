/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.pager.plugin;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.CallableStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;

/**
 * 防止全表更新与删除
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }) })
public class ExplainSqlHandler extends SqlParserHandler implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        if (ms.getSqlCommandType() == SqlCommandType.DELETE || ms.getSqlCommandType() == SqlCommandType.UPDATE) {
            Object parameter = args[1];
            Configuration configuration = ms.getConfiguration();
            Object target = invocation.getTarget();
            StatementHandler handler = configuration.newStatementHandler((Executor) target, ms, parameter,
                    RowBounds.DEFAULT, null, null);
            if (!(handler instanceof CallableStatementHandler)) {
                // 标记是否修改过 SQL
                boolean sqlChangedFlag = false;
                MetaObject metaObject = SystemMetaObject
                        .forObject(realTarget(SystemMetaObject.forObject(handler).getOriginalObject()));

                String sql = ((String) metaObject.getValue(DELEGATE_BOUNDSQL_SQL)).replaceAll("[\\s]+", Symbol.SPACE);
                if (this.allowProcess(metaObject)) {
                    try {
                        StringBuilder sqlStringBuilder = new StringBuilder();
                        Statements statements = CCJSqlParserUtil.parseStatements(parser(metaObject, sql));
                        int i = 0;
                        for (Statement statement : statements.getStatements()) {
                            if (null != statement) {
                                if (i++ > 0) {
                                    sqlStringBuilder.append(Symbol.C_SEMICOLON);
                                }
                                sqlStringBuilder.append(this.processParser(statement));
                            }
                        }
                        if (sqlStringBuilder.length() > 0) {
                            sql = sqlStringBuilder.toString();
                            sqlChangedFlag = true;
                        }
                    } catch (JSQLParserException e) {
                        throw new InternalException(
                                "Failed to process, please exclude the tableName or statementId.\n Error SQL: %s", e,
                                sql);
                    }
                }
                if (sqlChangedFlag) {
                    metaObject.setValue(DELEGATE_BOUNDSQL_SQL, sql);
                }
            }
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object object) {
        if (object instanceof Executor) {
            return Plugin.wrap(object, this);
        }
        return object;
    }

}