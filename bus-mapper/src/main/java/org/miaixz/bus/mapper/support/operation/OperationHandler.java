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
package org.miaixz.bus.mapper.support.operation;

import java.util.regex.Pattern;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.handler.AbstractSqlHandler;
import org.miaixz.bus.mapper.handler.MapperHandler;

/**
 * Operation handler to prevent full table updates and deletes. This handler intercepts UPDATE and DELETE statements to
 * ensure they have a valid WHERE clause, preventing accidental modification or deletion of all records in a table.
 * <p>
 * This implementation uses regular expressions for SQL analysis instead of a full SQL parser, providing a lightweight
 * solution for basic WHERE clause validation.
 * </p>
 *
 * @param <T> the generic type parameter
 * @author Kimi Liu
 * @since Java 17+
 */
public class OperationHandler<T> extends AbstractSqlHandler implements MapperHandler<T> {

    /**
     * Pattern to detect WHERE clause in SQL statements (case-insensitive)
     */
    private static final Pattern WHERE_PATTERN = Pattern.compile("\\bWHERE\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Pattern to detect trivial WHERE clauses that match all rows (e.g., WHERE 1=1)
     */
    private static final Pattern TRIVIAL_WHERE_PATTERN = Pattern.compile(
            "\\bWHERE\\s+(1\\s*=\\s*1|TRUE|'1'\\s*=\\s*'1'|\"1\"\\s*=\\s*\"1\")\\s*($|;|\\s+ORDER\\s+|\\s+LIMIT\\s+|\\s+OFFSET\\s+)",
            Pattern.CASE_INSENSITIVE);

    /**
     * Pattern to detect WHERE clauses with only NULL checks that don't filter rows
     */
    private static final Pattern NULL_WHERE_PATTERN = Pattern.compile(
            "\\bWHERE\\s+NULL\\s+IS\\s+NULL\\s*($|;|\\s+ORDER\\s+|\\s+LIMIT\\s+|\\s+OFFSET\\s+)",
            Pattern.CASE_INSENSITIVE);

    /**
     * Pre-compiled patterns for SQL normalization (performance optimization)
     */
    private static final Pattern SINGLE_LINE_COMMENT_PATTERN = Pattern.compile("--[^\\r\\n]*");
    private static final Pattern MULTI_LINE_COMMENT_PATTERN = Pattern.compile("/\\*.*?\\*/");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * Whether to enable strict mode (check for trivial WHERE clauses)
     */
    private boolean strictMode = true;

    @Override
    public int getOrder() {
        return MIN_VALUE + 3;
    }

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
            String sql = boundSql.getSql();
            checkSqlSafety(sql, sct);
        }
    }

    /**
     * Checks if the SQL statement is safe to execute (has a valid WHERE clause).
     *
     * @param sql         the SQL statement
     * @param commandType the SQL command type
     * @throws IllegalArgumentException if the SQL statement is not safe
     */
    protected void checkSqlSafety(String sql, SqlCommandType commandType) {
        if (StringKit.isBlank(sql)) {
            return;
        }

        // Normalize SQL: remove comments and extra whitespace
        String normalizedSql = normalizeSQL(sql);

        // Check if WHERE clause exists
        if (!WHERE_PATTERN.matcher(normalizedSql).find()) {
            String operation = commandType == SqlCommandType.DELETE ? "deletion" : "update operation";
            throw new IllegalArgumentException(
                    "Prohibition of full table " + operation + ". SQL must contain a WHERE clause: " + sql);
        }

        // In strict mode, check for trivial WHERE clauses
        if (strictMode) {
            if (TRIVIAL_WHERE_PATTERN.matcher(normalizedSql).find()
                    || NULL_WHERE_PATTERN.matcher(normalizedSql).find()) {
                String operation = commandType == SqlCommandType.DELETE ? "deletion" : "update operation";
                throw new IllegalArgumentException("Prohibition of full table " + operation
                        + ". SQL contains a trivial WHERE clause that matches all rows: " + sql);
            }
        }
    }

    /**
     * Normalizes SQL by removing comments and extra whitespace.
     * <p>
     * <strong>Performance Optimization:</strong> Uses pre-compiled Pattern objects to avoid repeated regex compilation,
     * significantly improving performance for high-frequency SQL execution.
     * </p>
     *
     * @param sql the original SQL
     * @return the normalized SQL
     */
    protected String normalizeSQL(String sql) {
        if (sql == null) {
            return Normal.EMPTY;
        }

        // Remove single-line comments (-- ...) using pre-compiled pattern
        sql = SINGLE_LINE_COMMENT_PATTERN.matcher(sql).replaceAll(Symbol.SPACE);

        // Remove multi-line comments (/* ... */) using pre-compiled pattern
        sql = MULTI_LINE_COMMENT_PATTERN.matcher(sql).replaceAll(Symbol.SPACE);

        // Replace multiple whitespace with single space using pre-compiled pattern
        sql = WHITESPACE_PATTERN.matcher(sql).replaceAll(Symbol.SPACE);

        return sql.trim();
    }

    /**
     * Checks if strict mode is enabled.
     *
     * @return true if strict mode is enabled, false otherwise
     */
    public boolean isStrictMode() {
        return strictMode;
    }

    /**
     * Sets whether to enable strict mode.
     *
     * @param strictMode true to enable strict mode, false otherwise
     */
    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }

}
