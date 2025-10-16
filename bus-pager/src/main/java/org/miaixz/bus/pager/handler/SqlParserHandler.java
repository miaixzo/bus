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
 * Abstract SQL parsing class that provides functionality for parsing and processing SQL statements. This class uses
 * JSQLParser to handle different types of SQL statements (INSERT, SELECT, UPDATE, DELETE).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlParserHandler extends AbstractSqlHandler {

    /**
     * Parses a single SQL statement string.
     *
     * @param sql the SQL statement string
     * @return the parsed {@link Statement} object
     * @throws JSQLParserException if parsing fails
     */
    public static Statement parse(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parse(sql);
    }

    /**
     * Parses multiple SQL statement strings.
     *
     * @param sql the SQL statement string containing one or more statements
     * @return the parsed {@link Statements} object
     * @throws JSQLParserException if parsing fails
     */
    public static Statements parseStatements(String sql) throws JSQLParserException {
        return CCJSqlParserUtil.parseStatements(sql);
    }

    /**
     * Parses and processes a single SQL statement.
     *
     * @param sql    the SQL statement string
     * @param object an additional object for processing
     * @return the processed SQL statement string
     * @throws InternalException if parsing fails
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
     * Parses and processes multiple SQL statements.
     *
     * @param sql    the SQL statement string containing one or more statements
     * @param object an additional object for processing
     * @return the processed SQL statements, separated by semicolons
     * @throws InternalException if parsing fails
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
     * Executes the SQL statement parsing and processing.
     *
     * @param statement the JSQLParser parsed statement object
     * @param index     the index of the statement (for multiple statements)
     * @param sql       the original SQL statement
     * @param object    an additional object for processing
     * @return the processed SQL statement string
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
     * Processes an INSERT statement. This method should be overridden by subclasses to provide specific handling.
     *
     * @param insert the INSERT statement object
     * @param index  the index of the statement
     * @param sql    the original SQL statement
     * @param object an additional object for processing
     * @throws UnsupportedOperationException by default, as INSERT processing is not supported
     */
    protected void processInsert(Insert insert, int index, String sql, Object object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Processes a DELETE statement. This method should be overridden by subclasses to provide specific handling.
     *
     * @param delete the DELETE statement object
     * @param index  the index of the statement
     * @param sql    the original SQL statement
     * @param object an additional object for processing
     * @throws UnsupportedOperationException by default, as DELETE processing is not supported
     */
    protected void processDelete(Delete delete, int index, String sql, Object object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Processes an UPDATE statement. This method should be overridden by subclasses to provide specific handling.
     *
     * @param update the UPDATE statement object
     * @param index  the index of the statement
     * @param sql    the original SQL statement
     * @param object an additional object for processing
     * @throws UnsupportedOperationException by default, as UPDATE processing is not supported
     */
    protected void processUpdate(Update update, int index, String sql, Object object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Processes a SELECT statement. This method should be overridden by subclasses to provide specific handling.
     *
     * @param select the SELECT statement object
     * @param index  the index of the statement
     * @param sql    the original SQL statement
     * @param object an additional object for processing
     * @throws UnsupportedOperationException by default, as SELECT processing is not supported
     */
    protected void processSelect(Select select, int index, String sql, Object object) {
        throw new UnsupportedOperationException();
    }

    /**
     * Validates an SQL string to prevent potential injection risks.
     *
     * @param sql the SQL statement string to validate
     * @throws InternalException if the SQL script validation fails
     */
    protected static void validateSql(String sql) {
        if (!Symbol.ZERO.equals(sql) && !Symbol.STAR.equals(sql) && OGNL.validateSql(sql)) {
            throw new InternalException(
                    "SQL script validation failed: potential security issue detected, please review");
        }
    }

}
