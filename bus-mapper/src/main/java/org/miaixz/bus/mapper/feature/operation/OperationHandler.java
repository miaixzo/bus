/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.feature.operation;

import java.util.Properties;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Holder;
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
 * @since Java 21+
 */
@Getter
@Setter
public class OperationHandler<T> extends AbstractSqlHandler implements MapperHandler<T> {

    /**
     * Initializes the SQL operation handler that classifies statements before mapper processing.
     */
    public OperationHandler() {
        // No initialization required.
    }

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

    /**
     * Pattern matching SQL block comments.
     */
    private static final Pattern MULTI_LINE_COMMENT_PATTERN = Pattern.compile("/\\*.*?\\*/");

    /**
     * Pattern matching repeated whitespace.
     */
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * Whether to enable strict mode (check for trivial WHERE clauses)
     */
    private boolean strictMode = true;

    /**
     * Flattened datasource-scoped operation configuration.
     */
    private Properties properties;

    /**
     * Installs flattened mapper configuration used for per-datasource safety settings.
     *
     * @param properties flattened mapper configuration
     * @return {@code true} when configuration is available
     */
    @Override
    public boolean setProperties(Properties properties) {
        this.properties = properties;
        return properties != null;
    }

    /**
     * Returns the execution order for the operation safety handler in the mapper interceptor chain.
     *
     * @return the handler order value
     */
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
        if (!currentEnabled()) {
            return;
        }
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
        if (currentStrictMode()) {
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
     * Resolves whether operation protection is enabled for the current datasource.
     *
     * @return {@code true} when the current datasource enables protection
     */
    private boolean currentEnabled() {
        return Boolean.parseBoolean(find(Args.PROP_ENABLED, "true"));
    }

    /**
     * Resolves strict-mode behavior for the current datasource.
     *
     * @return {@code true} when trivial WHERE clauses must be rejected
     */
    private boolean currentStrictMode() {
        return Boolean.parseBoolean(find(Args.OPERATION_STRICT_MODE, String.valueOf(strictMode)));
    }

    /**
     * Finds one operation setting using datasource, shared and legacy-default precedence.
     *
     * @param name     setting name
     * @param fallback fallback value when no configured value exists
     * @return resolved setting value
     */
    private String find(String name, String fallback) {
        if (properties == null) {
            return fallback;
        }
        String key = Holder.getKey();
        String suffix = Symbol.DOT + Args.OPERATION_KEY + Symbol.DOT + name;
        String value = properties.getProperty(key + suffix);
        if (value == null) {
            value = properties.getProperty(Args.SHARED_KEY + suffix);
        }
        if (value == null && Holder.getDefault() != null) {
            value = properties.getProperty(Holder.getDefault() + suffix);
        }
        if (value == null) {
            value = properties.getProperty("default" + suffix, fallback);
        }
        return value;
    }

}
