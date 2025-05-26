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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.handler.AbstractSqlHandler;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;

/**
 * 抽象 SQL 解析类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlParserHandler extends AbstractSqlHandler {

    /**
     * 解析单个 SQL 语句
     *
     * @param sql SQL 语句字符串
     * @return 解析后的 Statement 对象
     * @throws JSQLParserException 如果解析失败
     */
    public static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql);
    }

    /**
     * 解析多个 SQL 语句
     *
     * @param sql SQL 语句字符串
     * @return 解析后的 Statements 对象
     * @throws JSQLParserException 如果解析失败
     */
    public static Statements parseStatements(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parseStatements(sql);
    }

    public String parserSingle(String sql, Object obj) {
        try {
            Statement statement = parse(sql);
            return processParser(statement, 0, sql, obj);
        } catch (JSQLParserException e) {
            throw new InternalException("Failed to process, Error SQL: %s", sql);
        }
    }

    public String parserMulti(String sql, Object obj) {
        try {
            // fixed github pull/295
            StringBuilder sb = new StringBuilder();
            Statements statements = parseStatements(sql);
            int i = 0;
            for (Statement statement : statements) {
                if (i > 0) {
                    sb.append(Symbol.SEMICOLON);
                }
                sb.append(processParser(statement, i, sql, obj));
                i++;
            }
            return sb.toString();
        } catch (JSQLParserException e) {
            throw new InternalException("Failed to process, Error SQL: %s", sql);
        }
    }

    /**
     * 执行 SQL 解析
     *
     * @param statement JsqlParser Statement
     * @return sql
     */
    protected String processParser(Statement statement, int index, String sql, Object obj) {
        if (statement instanceof Insert) {
            this.processInsert((Insert) statement, index, sql, obj);
        } else if (statement instanceof Select) {
            this.processSelect((Select) statement, index, sql, obj);
        } else if (statement instanceof Update) {
            this.processUpdate((Update) statement, index, sql, obj);
        } else if (statement instanceof Delete) {
            this.processDelete((Delete) statement, index, sql, obj);
        }
        sql = statement.toString();
        if (Logger.isDebugEnabled()) {
            Logger.debug("SQL to parse, SQL: " + sql);
        }
        return sql;
    }

    /**
     * 新增
     */
    protected void processInsert(Insert insert, int index, String sql, Object obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * 删除
     */
    protected void processDelete(Delete delete, int index, String sql, Object obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * 更新
     */
    protected void processUpdate(Update update, int index, String sql, Object obj) {
        throw new UnsupportedOperationException();
    }

    /**
     * 查询
     */
    protected void processSelect(Select select, int index, String sql, Object obj) {
        throw new UnsupportedOperationException();
    }

}
