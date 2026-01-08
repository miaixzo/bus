/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.mapper.parsing;

import org.miaixz.bus.core.lang.Symbol;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL table name parser, used to extract table names from SQL statements.
 * <p>
 * This is an ultra-lightweight, ultra-fast parser that supports extracting table names from Oracle dialect SQL. Usage:
 * {@code new TableNameParser(sql).tables()}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class TableNameParser {

    /**
     * Represents the "set" keyword in SQL.
     */
    private static final String TOKEN_SET = "set";

    /**
     * Represents the "of" keyword in SQL.
     */
    private static final String TOKEN_OF = "of";

    /**
     * Represents the "dual" table in Oracle SQL.
     */
    private static final String TOKEN_DUAL = "dual";

    /**
     * Represents the "ignore" keyword in SQL.
     */
    private static final String IGNORE = "ignore";

    /**
     * Represents the "delete" command in SQL.
     */
    private static final String TOKEN_DELETE = "delete";

    /**
     * Represents the "update" command in SQL.
     */
    private static final String TOKEN_UPDATE = "update";

    /**
     * Represents the "create" command in SQL.
     */
    private static final String TOKEN_CREATE = "create";

    /**
     * Represents the "index" keyword in SQL.
     */
    private static final String TOKEN_INDEX = "index";

    /**
     * Represents the "join" keyword in SQL.
     */
    private static final String KEYWORD_JOIN = "join";

    /**
     * Represents the "into" keyword in SQL.
     */
    private static final String KEYWORD_INTO = "into";

    /**
     * Represents the "table" keyword in SQL.
     */
    private static final String KEYWORD_TABLE = "table";

    /**
     * Represents the "from" keyword in SQL.
     */
    private static final String KEYWORD_FROM = "from";

    /**
     * Represents the "using" keyword in SQL.
     */
    private static final String KEYWORD_USING = "using";

    /**
     * Represents the "update" keyword in SQL.
     */
    private static final String KEYWORD_UPDATE = "update";

    /**
     * Represents the "straight_join" keyword in SQL.
     */
    private static final String KEYWORD_STRAIGHT_JOIN = "straight_join";

    /**
     * Represents the "duplicate" keyword in SQL.
     */
    private static final String KEYWORD_DUPLICATE = "duplicate";

    /**
     * A list of SQL keywords that are of concern for table name parsing.
     */
    private static final List<String> concerned = Arrays
            .asList(KEYWORD_TABLE, KEYWORD_INTO, KEYWORD_JOIN, KEYWORD_USING, KEYWORD_UPDATE, KEYWORD_STRAIGHT_JOIN);

    /**
     * A list of SQL keywords that should be ignored during table name parsing.
     */
    private static final List<String> ignored = Arrays.asList(Symbol.BRACE_LEFT, TOKEN_SET, TOKEN_OF, TOKEN_DUAL);

    /**
     * A set of index types.
     */
    private static final Set<String> INDEX_TYPES = new HashSet<>(
            Arrays.asList("UNIQUE", "FULLTEXT", "SPATIAL", "CLUSTERED", "NONCLUSTERED"));

    /**
     * Regular expression to match non-SQL tokens, including comments, whitespace, semicolons, etc.
     */
    private static final Pattern NON_SQL_TOKEN_PATTERN = Pattern
            .compile("(--[^\\v]+)|;|(\\s+)|((?s)/[*].*?[*]/)" + "|(((\\b|\\B)(?=[,()]))|((?<=[,()])(\\b|\\B)))");

    /**
     * The list of SQL tokens parsed from the SQL statement.
     */
    private final List<SqlToken> tokens;

    /**
     * Constructs a TableNameParser and extracts table names from the given SQL statement.
     *
     * @param sql The SQL statement to parse.
     */
    public TableNameParser(String sql) {
        tokens = fetchAllTokens(sql);
    }

    /**
     * Accepts a new visitor and visits the table names in the current SQL.
     * <p>
     * This uses the visitor pattern, allowing for easy modification without changing the original SQL. It also
     * conveniently provides the index of table names.
     *
     * @param visitor The visitor.
     */
    public void accept(TableNameVisitor visitor) {
        int index = 0;
        String first = tokens.get(index).getValue();
        if (isOracleSpecialDelete(first, tokens, index)) {
            visitNameToken(safeGetToken(index + 1), visitor);
        } else if (isCreateIndex(first, tokens, index)) {
            String value = tokens.get(index + 4).getValue();
            if ("ON".equalsIgnoreCase(value)) {
                visitNameToken(safeGetToken(index + 5), visitor);
            } else {
                visitNameToken(safeGetToken(index + 4), visitor);
            }
        } else if (isCreateTableIfNotExist(first, tokens, index)) {
            visitNameToken(safeGetToken(index + 5), visitor);
        } else {
            while (hasMoreTokens(tokens, index)) {
                String current = tokens.get(index++).getValue();
                if (isFromToken(current)) {
                    processFromToken(tokens, index, visitor);
                } else if (isOnDuplicateKeyUpdate(current, index)) {
                    index = skipDuplicateKeyUpdateIndex(index);
                } else if (concerned.contains(current.toLowerCase())) {
                    if (hasMoreTokens(tokens, index)) {
                        SqlToken next = tokens.get(index++);
                        if (TOKEN_UPDATE.equalsIgnoreCase(current) && IGNORE.equalsIgnoreCase(next.getValue())) {
                            next = tokens.get(index++);
                        }
                        visitNameToken(next, visitor);
                    }
                }
            }
        }
    }

    /**
     * Safely retrieves an {@link SqlToken} by index.
     *
     * @param index The index.
     * @return The {@link SqlToken} if within bounds, otherwise null.
     */
    private SqlToken safeGetToken(int index) {
        return index < tokens.size() ? tokens.get(index) : null;
    }

    /**
     * Visitor interface for table names.
     */
    public interface TableNameVisitor {

        /**
         * Visits a token representing a table name.
         *
         * @param name The token representing the table name.
         */
        void visit(SqlToken name);
    }

    /**
     * Fetches all SQL tokens from the SQL statement.
     *
     * @param sql The SQL statement.
     * @return A list of {@link SqlToken} objects.
     */
    private List<SqlToken> fetchAllTokens(String sql) {
        List<SqlToken> tokens = new ArrayList<>();
        Matcher matcher = NON_SQL_TOKEN_PATTERN.matcher(sql);
        int last = 0;
        while (matcher.find()) {
            int start = matcher.start();
            if (start != last) {
                tokens.add(new SqlToken(last, start, sql.substring(last, start)));
            }
            last = matcher.end();
        }
        if (last != sql.length()) {
            tokens.add(new SqlToken(last, sql.length(), sql.substring(last)));
        }
        return tokens;
    }

    /**
     * Checks if it's an Oracle-specific DELETE statement (where DELETE is not followed by FROM or *).
     *
     * @param current The current token.
     * @param tokens  The list of tokens.
     * @param index   The current index.
     * @return {@code true} if it's an Oracle special DELETE, {@code false} otherwise.
     */
    private static boolean isOracleSpecialDelete(String current, List<SqlToken> tokens, int index) {
        if (TOKEN_DELETE.equalsIgnoreCase(current)) {
            if (hasMoreTokens(tokens, index++)) {
                String next = tokens.get(index).getValue();
                return !KEYWORD_FROM.equalsIgnoreCase(next) && !Symbol.STAR.equals(next);
            }
        }
        return false;
    }

    /**
     * Checks if the statement is a CREATE INDEX statement.
     *
     * @param current The current token.
     * @param tokens  The list of tokens.
     * @param index   The current index.
     * @return {@code true} if it's a CREATE INDEX statement, {@code false} otherwise.
     */
    private boolean isCreateIndex(String current, List<SqlToken> tokens, int index) {
        if (TOKEN_CREATE.equalsIgnoreCase(current) && hasMoreTokens(tokens, index + 4)) {
            String next = tokens.get(index + 1).getValue();
            if (INDEX_TYPES.contains(next.toUpperCase())) {
                next = tokens.get(index + 2).getValue();
            }
            return TOKEN_INDEX.equalsIgnoreCase(next);
        }
        return false;
    }

    /**
     * Checks if the statement is a CREATE TABLE IF NOT EXISTS statement.
     *
     * @param current The current token.
     * @param tokens  The list of tokens.
     * @param index   The current index.
     * @return {@code true} if it's a CREATE TABLE IF NOT EXISTS statement, {@code false} otherwise.
     */
    private boolean isCreateTableIfNotExist(String current, List<SqlToken> tokens, int index) {
        if (TOKEN_CREATE.equalsIgnoreCase(current) && hasMoreTokens(tokens, index + 5)) {
            StringBuilder tableIfNotExist = new StringBuilder();
            for (int i = index; i <= index + 4; i++) {
                tableIfNotExist.append(tokens.get(i).getValue());
            }
            return "createtableifnotexists".equalsIgnoreCase(tableIfNotExist.toString());
        }
        return false;
    }

    /**
     * Checks if the statement contains MySQL's ON DUPLICATE KEY UPDATE syntax.
     *
     * @param current The current token.
     * @param index   The current index.
     * @return {@code true} if it's an ON DUPLICATE KEY UPDATE clause, {@code false} otherwise.
     */
    private boolean isOnDuplicateKeyUpdate(String current, int index) {
        if (KEYWORD_DUPLICATE.equalsIgnoreCase(current)) {
            if (hasMoreTokens(tokens, index++)) {
                String next = tokens.get(index).getValue();
                return KEYWORD_UPDATE.equalsIgnoreCase(next);
            }
        }
        return false;
    }

    /**
     * Checks if the current token is a "FROM" keyword.
     *
     * @param currentToken The current token.
     * @return {@code true} if it's "FROM", {@code false} otherwise.
     */
    private static boolean isFromToken(String currentToken) {
        return KEYWORD_FROM.equalsIgnoreCase(currentToken);
    }

    /**
     * Skips tokens related to MySQL's ON DUPLICATE KEY UPDATE clause.
     *
     * @param index The current index.
     * @return The new index after skipping.
     */
    private int skipDuplicateKeyUpdateIndex(int index) {
        // "on duplicate key update" is a fixed MySQL syntax, just skip it.
        return index + 2;
    }

    /**
     * Processes tokens after a "FROM" keyword to extract table names.
     *
     * @param tokens  The list of tokens.
     * @param index   The current index.
     * @param visitor The table name visitor.
     */
    private static void processFromToken(List<SqlToken> tokens, int index, TableNameVisitor visitor) {
        SqlToken sqlToken = tokens.get(index++);
        visitNameToken(sqlToken, visitor);

        String next = null;
        if (hasMoreTokens(tokens, index)) {
            next = tokens.get(index++).getValue();
        }

        if (shouldProcessMultipleTables(next)) {
            processNonAliasedMultiTables(tokens, index, next, visitor);
        } else {
            processAliasedMultiTables(tokens, index, sqlToken, visitor);
        }
    }

    /**
     * Processes multiple tables without aliases.
     *
     * @param tokens    The list of tokens.
     * @param index     The current index.
     * @param nextToken The next token.
     * @param visitor   The table name visitor.
     */
    private static void processNonAliasedMultiTables(
            List<SqlToken> tokens,
            int index,
            String nextToken,
            TableNameVisitor visitor) {
        while (nextToken.equals(Symbol.COMMA)) {
            visitNameToken(tokens.get(index++), visitor);
            if (hasMoreTokens(tokens, index)) {
                nextToken = tokens.get(index++).getValue();
            } else {
                break;
            }
        }
    }

    /**
     * Processes multiple tables with aliases.
     *
     * @param tokens  The list of tokens.
     * @param index   The current index.
     * @param current The current SQL token.
     * @param visitor The table name visitor.
     */
    private static void processAliasedMultiTables(
            List<SqlToken> tokens,
            int index,
            SqlToken current,
            TableNameVisitor visitor) {
        String nextNextToken = null;
        if (hasMoreTokens(tokens, index)) {
            nextNextToken = tokens.get(index++).getValue();
        }

        if (shouldProcessMultipleTables(nextNextToken)) {
            while (hasMoreTokens(tokens, index) && nextNextToken.equals(Symbol.COMMA)) {
                if (hasMoreTokens(tokens, index)) {
                    current = tokens.get(index++);
                }
                if (hasMoreTokens(tokens, index)) {
                    index++;
                }
                if (hasMoreTokens(tokens, index)) {
                    nextNextToken = tokens.get(index++).getValue();
                }
                visitNameToken(current, visitor);
            }
        }
    }

    /**
     * Checks if multiple tables should be processed based on the next token.
     *
     * @param nextToken The next token.
     * @return {@code true} if multiple tables should be processed, {@code false} otherwise.
     */
    private static boolean shouldProcessMultipleTables(final String nextToken) {
        return nextToken != null && nextToken.equals(Symbol.COMMA);
    }

    /**
     * Checks if there are more tokens in the list.
     *
     * @param tokens The list of tokens.
     * @param index  The current index.
     * @return {@code true} if there are more tokens, {@code false} otherwise.
     */
    private static boolean hasMoreTokens(List<SqlToken> tokens, int index) {
        return index < tokens.size();
    }

    /**
     * Visits a name token, ignoring certain keywords.
     *
     * @param token   The SQL token.
     * @param visitor The table name visitor.
     */
    private static void visitNameToken(SqlToken token, TableNameVisitor visitor) {
        if (token != null) {
            String value = token.getValue().toLowerCase();
            if (!ignored.contains(value)) {
                visitor.visit(token);
            }
        }
    }

    /**
     * Parses tables from the SQL statement.
     *
     * @return A collection of table names extracted from the SQL.
     * @see #accept(TableNameVisitor)
     */
    public Collection<String> tables() {
        Map<String, String> tableMap = new HashMap<>();
        accept(token -> {
            String name = token.getValue();
            tableMap.putIfAbsent(name.toLowerCase(), name);
        });
        return new HashSet<>(tableMap.values());
    }

    /**
     * Represents an SQL token.
     */
    public static class SqlToken implements Comparable<SqlToken> {

        /**
         * The starting position of the token.
         */
        private final int start;

        /**
         * The ending position of the token.
         */
        private final int end;

        /**
         * The value of the token.
         */
        private final String value;

        /**
         * Constructs a new SqlToken.
         *
         * @param start The starting position.
         * @param end   The ending position.
         * @param value The token value.
         */
        private SqlToken(int start, int end, String value) {
            this.start = start;
            this.end = end;
            this.value = value;
        }

        /**
         * Gets the starting position of the token.
         *
         * @return The starting position.
         */
        public int getStart() {
            return start;
        }

        /**
         * Gets the ending position of the token.
         *
         * @return The ending position.
         */
        public int getEnd() {
            return end;
        }

        /**
         * Gets the value of the token.
         *
         * @return The token value.
         */
        public String getValue() {
            return value;
        }

        /**
         * Compares this token with another for ordering based on their starting positions.
         *
         * @param o The other token.
         * @return A negative integer, zero, or a positive integer as this token is less than, equal to, or greater than
         *         the specified object.
         */
        @Override
        public int compareTo(SqlToken o) {
            return Integer.compare(start, o.start);
        }

        /**
         * Returns the string representation of the token (its value).
         *
         * @return The token value.
         */
        @Override
        public String toString() {
            return value;
        }

    }

}
