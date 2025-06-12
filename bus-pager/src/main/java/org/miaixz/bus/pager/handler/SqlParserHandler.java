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
import org.miaixz.bus.mapper.OGNL;
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
 * 抽象SQL解析类，提供SQL语句解析和处理功能。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlParserHandler extends AbstractSqlHandler {

    /**
     * 解析单个SQL语句。
     *
     * @param sql SQL语句字符串
     * @return 解析后的Statement对象
     * @throws JSQLParserException 如果解析失败
     */
    public static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql);
    }

    /**
     * 解析多个SQL语句。
     *
     * @param sql SQL语句字符串
     * @return 解析后的Statements对象
     * @throws JSQLParserException 如果解析失败
     */
    public static Statements parseStatements(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parseStatements(sql);
    }

    /**
     * 解析并处理单个SQL语句。
     *
     * @param sql    SQL语句字符串
     * @param object 附加处理对象
     * @return 处理后的SQL语句
     * @throws InternalException 如果解析失败
     */
    public String parserSingle(String sql, Object object) {
        try {
            Statement statement = parse(sql);
            return processParser(statement, 0, sql, object);
        } catch (JSQLParserException e) {
            throw new InternalException("Failed to process, Error SQL: %s", sql);
        }
    }

    /**
     * 解析并处理多个SQL语句。
     *
     * @param sql    SQL语句字符串
     * @param object 附加处理对象
     * @return 处理后的SQL语句（多条语句以分号分隔）
     * @throws InternalException 如果解析失败
     */
    public String parserMulti(String sql, Object object) {
        try {
            StringBuilder sb = new StringBuilder();
            Statements statements = parseStatements(sql);
            int i = 0;
            for (Statement statement : statements) {
                if (i > 0) {
                    sb.append(Symbol.SEMICOLON);
                }
                sb.append(processParser(statement, i, sql, object));
                i++;
            }
            return sb.toString();
        } catch (JSQLParserException e) {
            throw new InternalException("Failed to process, Error SQL: %s", sql);
        }
    }

    /**
     * 执行SQL语句解析和处理。
     *
     * @param statement JSQLParser解析的语句对象
     * @param index     语句索引（多语句时使用）
     * @param sql       原始SQL语句
     * @param object    附加处理对象
     * @return 处理后的SQL语句
     */
    protected String processParser(Statement statement, int index, String sql, Object object) {
        if (statement instanceof Insert) {
            this.processInsert((Insert) statement, index, sql, object);
        } else if (statement instanceof Select) {
            this.processSelect((Select) statement, index, sql, object);
        } else if (statement instanceof Update) {
            this.processUpdate((Update) statement, index, sql, object);
        } else if (statement instanceof Delete) {
            this.processDelete((Delete) statement, index, sql, object);
        }
        return statement.toString();
    }

    /**
     * 处理INSERT语句。
     *
     * @param insert INSERT语句对象
     * @param index  语句索引
     * @param sql    原始SQL语句
     * @param object 附加处理对象
     * @throws UnsupportedOperationException 默认不支持INSERT处理
     */
    protected void processInsert(Insert insert, int index, String sql, Object object) {
        throw new UnsupportedOperationException();
    }

    /**
     * 处理DELETE语句。
     *
     * @param delete DELETE语句对象
     * @param index  语句索引
     * @param sql    原始SQL语句
     * @param object 附加处理对象
     * @throws UnsupportedOperationException 默认不支持DELETE处理
     */
    protected void processDelete(Delete delete, int index, String sql, Object object) {
        throw new UnsupportedOperationException();
    }

    /**
     * 处理UPDATE语句。
     *
     * @param update UPDATE语句对象
     * @param index  语句索引
     * @param sql    原始SQL语句
     * @param object 附加处理对象
     * @throws UnsupportedOperationException 默认不支持UPDATE处理
     */
    protected void processUpdate(Update update, int index, String sql, Object object) {
        throw new UnsupportedOperationException();
    }

    /**
     * 处理SELECT语句。
     *
     * @param select SELECT语句对象
     * @param index  语句索引
     * @param sql    原始SQL语句
     * @param object 附加处理对象
     * @throws UnsupportedOperationException 默认不支持SELECT处理
     */
    protected void processSelect(Select select, int index, String sql, Object object) {
        throw new UnsupportedOperationException();
    }

    /**
     * 校验SQL防止注入风险
     *
     * @param sql SQL语句字符串
     */
    protected static void validateSql(String sql) {
        if (!Symbol.ZERO.equals(sql) && !Symbol.STAR.equals(sql) && OGNL.validateSql(sql)) {
            throw new InternalException(
                    "SQL script validation failed: potential security issue detected, please review");
        }
    }

}