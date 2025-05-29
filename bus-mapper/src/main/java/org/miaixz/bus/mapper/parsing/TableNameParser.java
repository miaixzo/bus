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
package org.miaixz.bus.mapper.parsing;

import org.miaixz.bus.core.lang.Symbol;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL 表名解析器，用于从 SQL 语句中提取表名。
 * <p>
 * 超轻量、超快速的解析器，支持提取 Oracle 方言 SQL 中的表名。
 * 使用方式：new TableNameParser(sql).tables()
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class TableNameParser {

    /**
     * 表示 SQL 中的 "set" 关键字
     */
    private static final String TOKEN_SET = "set";

    /**
     * 表示 SQL 中的 "of" 关键字
     */
    private static final String TOKEN_OF = "of";

    /**
     * 表示 Oracle SQL 中的 "dual" 表
     */
    private static final String TOKEN_DUAL = "dual";

    /**
     * 表示 SQL 中的 "ignore" 关键字
     */
    private static final String IGNORE = "ignore";

    /**
     * 表示 SQL 的 "delete" 命令
     */
    private static final String TOKEN_DELETE = "delete";

    /**
     * 表示 SQL 的 "update" 命令
     */
    private static final String TOKEN_UPDATE = "update";

    /**
     * 表示 SQL 的 "create" 命令
     */
    private static final String TOKEN_CREATE = "create";

    /**
     * 表示 SQL 的 "index" 关键字
     */
    private static final String TOKEN_INDEX = "index";

    /**
     * 表示 SQL 中的 "join" 关键字
     */
    private static final String KEYWORD_JOIN = "join";

    /**
     * 表示 SQL 中的 "into" 关键字
     */
    private static final String KEYWORD_INTO = "into";

    /**
     * 表示 SQL 中的 "table" 关键字
     */
    private static final String KEYWORD_TABLE = "table";

    /**
     * 表示 SQL 中的 "from" 关键字
     */
    private static final String KEYWORD_FROM = "from";

    /**
     * 表示 SQL 中的 "using" 关键字
     */
    private static final String KEYWORD_USING = "using";

    /**
     * 表示 SQL 中的 "update" 关键字
     */
    private static final String KEYWORD_UPDATE = "update";

    /**
     * 表示 SQL 中的 "straight_join" 关键字
     */
    private static final String KEYWORD_STRAIGHT_JOIN = "straight_join";

    /**
     * 表示 SQL 中的 "duplicate" 关键字
     */
    private static final String KEYWORD_DUPLICATE = "duplicate";

    /**
     * 需要关注的 SQL 关键字列表
     */
    private static final List<String> concerned = Arrays.asList(KEYWORD_TABLE, KEYWORD_INTO, KEYWORD_JOIN,
            KEYWORD_USING, KEYWORD_UPDATE, KEYWORD_STRAIGHT_JOIN);

    /**
     * 需要忽略的 SQL 关键字列表
     */
    private static final List<String> ignored = Arrays.asList(Symbol.BRACE_LEFT, TOKEN_SET, TOKEN_OF, TOKEN_DUAL);

    /**
     * 索引类型集合
     */
    private static final Set<String> INDEX_TYPES = new HashSet<>(
            Arrays.asList("UNIQUE", "FULLTEXT", "SPATIAL", "CLUSTERED", "NONCLUSTERED"));

    /**
     * 匹配非 SQL 词素的正则表达式，包括注释、空白字符、分号等
     */
    private static final Pattern NON_SQL_TOKEN_PATTERN = Pattern
            .compile("(--[^\\v]+)|;|(\\s+)|((?s)/[*].*?[*]/)" + "|(((\\b|\\B)(?=[,()]))|((?<=[,()])(\\b|\\B)))");

    /**
     * SQL 词素列表
     */
    private final List<SqlToken> tokens;

    /**
     * 从 SQL 中提取表名称
     *
     * @param sql 需要解析的 SQL 语句
     */
    public TableNameParser(String sql) {
        tokens = fetchAllTokens(sql);
    }

    /**
     * 接受一个新的访问者，并访问当前 SQL 的表名称
     * <p>
     * 现在我们改成了访问者模式，不在对以前的 SQL 做改动 同时，你可以方便的获得表名位置的索引
     *
     * @param visitor 访问者
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
     * 安全访问获取SqlToken
     *
     * @param index 索引
     * @return 超出索引返回 null，否则返回SqlToken
     * @since 3.5.11
     */
    private SqlToken safeGetToken(int index) {
        return index < tokens.size() ? tokens.get(index) : null;
    }

    /**
     * 表名访问器
     */
    public interface TableNameVisitor {
        /**
         * @param name 表示表名称的 token
         */
        void visit(SqlToken name);
    }

    /**
     * 从 SQL 语句中提取出 所有的 SQL Token
     *
     * @param sql SQL
     * @return 语句
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
     * 如果是 DELETE 后面紧跟的不是 FROM 或者 * ,则 返回 true
     *
     * @param current 当前的 token
     * @param tokens  token 列表
     * @param index   索引
     * @return 判断是不是 Oracle 特殊的删除手法
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

    // CREATE INDEX temp_name_idx ON table1(name) NOLOGGING PARALLEL (DEGREE 8);
    // CREATE FULLTEXT INDEX ft_users_content ON users(content);
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

    // create table if not exists `user_info`
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
     * @param current 当前token
     * @param index   索引
     * @return 判断是否是mysql的特殊语法 on duplicate key update
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

    private static boolean isFromToken(String currentToken) {
        return KEYWORD_FROM.equalsIgnoreCase(currentToken);
    }

    private int skipDuplicateKeyUpdateIndex(int index) {
        // on duplicate key update为mysql的固定写法，直接跳过即可。
        return index + 2;
    }

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

    private static void processNonAliasedMultiTables(List<SqlToken> tokens, int index, String nextToken,
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

    private static void processAliasedMultiTables(List<SqlToken> tokens, int index, SqlToken current,
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

    private static boolean shouldProcessMultipleTables(final String nextToken) {
        return nextToken != null && nextToken.equals(Symbol.COMMA);
    }

    private static boolean hasMoreTokens(List<SqlToken> tokens, int index) {
        return index < tokens.size();
    }

    private static void visitNameToken(SqlToken token, TableNameVisitor visitor) {
        if (token != null) {
            String value = token.getValue().toLowerCase();
            if (!ignored.contains(value)) {
                visitor.visit(token);
            }
        }
    }

    /**
     * parser tables
     *
     * @return table names extracted out of sql
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
     * SQL 词素
     */
    public static class SqlToken implements Comparable<SqlToken> {

        /**
         * 词素起始位置
         */
        private final int start;

        /**
         * 词素结束位置
         */
        private final int end;

        /**
         * 词素值
         */
        private final String value;

        private SqlToken(int start, int end, String value) {
            this.start = start;
            this.end = end;
            this.value = value;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getValue() {
            return value;
        }

        @Override
        public int compareTo(SqlToken o) {
            return Integer.compare(start, o.start);
        }

        @Override
        public String toString() {
            return value;
        }

    }

}