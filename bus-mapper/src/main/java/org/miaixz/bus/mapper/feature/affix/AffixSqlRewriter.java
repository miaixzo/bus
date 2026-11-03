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
package org.miaixz.bus.mapper.feature.affix;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Rewrites physical table references in SQL by applying configured prefix and suffix rules.
 * <p>
 * The rewriter recognizes table names introduced by {@code FROM}, {@code JOIN}, {@code INTO}, and {@code UPDATE},
 * preserves schema qualifiers and identifier delimiters, and avoids applying the same affix more than once.
 * Non-recursive CTE names are resolved within their query scope, after their definition; physical tables inside CTEs
 * remain eligible. Quoted CTE references must use the same spelling as their declaration. Recursive CTEs are not
 * supported.
 *
 * @author Kimi Liu
 */
public class AffixSqlRewriter {

    /**
     * Tokens that can follow a table-introducing keyword but are not table names.
     */
    private static final Set<String> TABLE_NAME_STOP_WORDS = Set.of("SET", "VALUES", "SELECT", "WHERE", "RETURNING");

    /**
     * Text prepended to eligible physical table names.
     */
    private final String prefix;

    /**
     * Text appended to eligible physical table names.
     */
    private final String suffix;

    /**
     * Logical or physical table names excluded from prefix rewriting.
     */
    private final List<String> prefixIgnore;

    /**
     * Logical or physical table names excluded from suffix rewriting.
     */
    private final List<String> suffixIgnore;

    /**
     * Creates a SQL affix rewriter that applies one shared ignore list to both sides.
     *
     * @param prefix text prepended to table names
     * @param suffix text appended to table names
     * @param ignore logical table names excluded from rewriting
     */
    public AffixSqlRewriter(String prefix, String suffix, List<String> ignore) {
        this(prefix, ignore, suffix, ignore);
    }

    /**
     * Creates a SQL affix rewriter with independent prefix and suffix ignore rules.
     *
     * @param prefix       text prepended to table names
     * @param prefixIgnore logical tables excluded from prefix handling
     * @param suffix       text appended to table names
     * @param suffixIgnore logical tables excluded from suffix handling
     */
    public AffixSqlRewriter(String prefix, List<String> prefixIgnore, String suffix, List<String> suffixIgnore) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.prefixIgnore = prefixIgnore;
        this.suffixIgnore = suffixIgnore;
    }

    /**
     * Applies missing prefix and suffix values to every recognized table reference.
     *
     * @param sql original SQL
     * @return rewritten SQL, or the original SQL when no affix applies
     */
    public String apply(String sql) {
        if (StringKit.isEmpty(sql) || StringKit.isEmpty(prefix) && StringKit.isEmpty(suffix)) {
            return sql;
        }

        List<Token> tokens = tokenize(sql);
        List<Cte> ctes = ctes(tokens);
        StringBuilder result = new StringBuilder(sql.length());
        int copied = 0;
        for (int index = 0; index < tokens.size(); index++) {
            Token keyword = tokens.get(index);
            boolean source = keyword.is("FROM") || keyword.is("JOIN");
            if (!source && !keyword.is("INTO") && !keyword.is("UPDATE")) {
                continue;
            }
            int tableIndex = index + 1;
            if (tableIndex >= tokens.size() || !tokens.get(tableIndex).identifier()) {
                continue;
            }
            Token table = tokens.get(tableIndex);
            if (table.quote() == 0 && TABLE_NAME_STOP_WORDS.contains(StringKit.toUpperCase(table.value()))) {
                continue;
            }
            boolean qualified = false;
            while (tableIndex + 2 < tokens.size() && tokens.get(tableIndex + 1).is(".")
                    && tokens.get(tableIndex + 2).identifier()) {
                qualified = true;
                tableIndex += 2;
                table = tokens.get(tableIndex);
            }
            index = tableIndex;
            if (source && (tableIndex + 1 < tokens.size() && tokens.get(tableIndex + 1).is("(")
                    || !qualified && visible(table, tableIndex, ctes))) {
                continue;
            }

            String tableName = table.value();
            boolean prefixed = StringKit.isEmpty(prefix) || tableName.startsWith(prefix)
                    || shouldIgnore(tableName, prefixIgnore);
            boolean suffixed = StringKit.isEmpty(suffix) || tableName.endsWith(suffix)
                    || shouldIgnore(tableName, suffixIgnore);
            if (prefixed && suffixed) {
                continue;
            }
            String replacement = (prefixed ? "" : prefix) + tableName + (suffixed ? "" : suffix);
            result.append(sql, copied, table.start()).append(table.render(replacement));
            copied = table.end();
        }
        return copied == 0 ? sql : result.append(sql, copied, sql.length()).toString();
    }

    /**
     * Finds ordinary CTE declarations and their lexical visibility intervals. A declaration is visible only after its
     * own body, so a later declaration cannot hide a physical table in an earlier body.
     */
    private static List<Cte> ctes(List<Token> tokens) {
        int[] closing = new int[tokens.size()];
        int[] container = new int[tokens.size()];
        Arrays.fill(closing, -1);
        Arrays.fill(container, -1);
        Deque<Integer> openings = new ArrayDeque<>();
        for (int index = 0; index < tokens.size(); index++) {
            if (!openings.isEmpty()) {
                container[index] = openings.peek();
            }
            if (tokens.get(index).is("(")) {
                openings.push(index);
            } else if (tokens.get(index).is(")") && !openings.isEmpty()) {
                closing[openings.pop()] = index;
            }
        }
        List<Cte> result = new ArrayList<>();
        for (int index = 0; index < tokens.size(); index++) {
            if (!tokens.get(index).is("WITH") || index + 1 >= tokens.size() || tokens.get(index + 1).is("RECURSIVE")) {
                continue;
            }
            int end = container[index] < 0 ? tokens.size() : closing[container[index]];
            if (end < 0) {
                continue;
            }
            for (int boundary = index + 1; boundary < end; boundary++) {
                if (container[boundary] == container[index] && tokens.get(boundary).is(";")) {
                    end = boundary;
                    break;
                }
            }
            int cursor = index + 1;
            while (cursor < end && tokens.get(cursor).identifier()) {
                Token name = tokens.get(cursor++);
                if (cursor < end && tokens.get(cursor).is("(")) {
                    int columns = closing[cursor];
                    if (columns < 0) {
                        break;
                    }
                    cursor = columns + 1;
                }
                if (cursor + 1 >= end || !tokens.get(cursor).is("AS") || !tokens.get(cursor + 1).is("(")) {
                    break;
                }
                int body = closing[cursor + 1];
                if (body < 0 || body >= end) {
                    break;
                }
                result.add(new Cte(name, body + 1, end));
                cursor = body + 1;
                if (cursor >= end || !tokens.get(cursor).is(",")) {
                    break;
                }
                cursor++;
            }
        }
        return result;
    }

    /** Resolves unqualified CTE references without leaking aliases into an enclosing or subsequent statement. */
    private static boolean visible(Token table, int index, List<Cte> ctes) {
        for (Cte cte : ctes) {
            if (index >= cte.visible() && index < cte.end()
                    && (cte.name().quote() == 0 && table.quote() == 0
                            ? cte.name().value().equalsIgnoreCase(table.value())
                            : cte.name().value().equals(table.value()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tokenizes only the SQL structure needed by affix rewriting. Original whitespace, parameters, comments and literal
     * bytes remain in the source and are copied unchanged; comments and literals cannot introduce table references.
     */
    private static List<Token> tokenize(String sql) {
        List<Token> result = new ArrayList<>();
        int cursor = 0;
        while (cursor < sql.length()) {
            char value = sql.charAt(cursor);
            if (Character.isWhitespace(value)) {
                cursor++;
                continue;
            }
            if (value == '-' && cursor + 1 < sql.length() && sql.charAt(cursor + 1) == '-') {
                cursor += 2;
                while (cursor < sql.length() && sql.charAt(cursor) != '\n' && sql.charAt(cursor) != '\r') {
                    cursor++;
                }
                continue;
            }
            if (value == '/' && cursor + 1 < sql.length() && sql.charAt(cursor + 1) == '*') {
                cursor += 2;
                int depth = 1;
                while (cursor < sql.length() && depth > 0) {
                    if (cursor + 1 < sql.length() && sql.charAt(cursor) == '/' && sql.charAt(cursor + 1) == '*') {
                        depth++;
                        cursor += 2;
                    } else if (cursor + 1 < sql.length() && sql.charAt(cursor) == '*'
                            && sql.charAt(cursor + 1) == '/') {
                        depth--;
                        cursor += 2;
                    } else {
                        cursor++;
                    }
                }
                continue;
            }
            int start = cursor;
            if ((value == '#' || value == '$') && cursor + 1 < sql.length() && sql.charAt(cursor + 1) == '{') {
                int finish = sql.indexOf('}', cursor + 2);
                cursor = finish < 0 ? sql.length() : finish + 1;
                result.add(new Token("", start, cursor, false, (char) 0));
                continue;
            }
            if (value == '\'' || value == '"' || value == '`' || value == '[') {
                char close = value == '[' ? ']' : value;
                cursor++;
                StringBuilder identifier = new StringBuilder();
                boolean complete = false;
                while (cursor < sql.length()) {
                    char current = sql.charAt(cursor++);
                    if (current == close) {
                        if (cursor < sql.length() && sql.charAt(cursor) == close) {
                            identifier.append(close);
                            cursor++;
                        } else {
                            complete = true;
                            break;
                        }
                    } else if (value == '\'' && current == '\\' && cursor < sql.length()) {
                        identifier.append(current).append(sql.charAt(cursor++));
                    } else {
                        identifier.append(current);
                    }
                }
                result.add(new Token(identifier.toString(), start, cursor, complete && value != '\'', value));
                continue;
            }
            if (value == '$') {
                int delimiter = cursor + 1;
                while (delimiter < sql.length() && (letter(sql.charAt(delimiter))
                        || delimiter > cursor + 1 && Character.isDigit(sql.charAt(delimiter)))) {
                    delimiter++;
                }
                if (delimiter < sql.length() && sql.charAt(delimiter) == '$') {
                    String marker = sql.substring(cursor, delimiter + 1);
                    int finish = sql.indexOf(marker, delimiter + 1);
                    cursor = finish < 0 ? sql.length() : finish + marker.length();
                    result.add(new Token("", start, cursor, false, (char) 0));
                    continue;
                }
            }
            if ((value == ':' || value == '@' || value == '$') && cursor + 1 < sql.length()
                    && (letter(sql.charAt(cursor + 1)) || Character.isDigit(sql.charAt(cursor + 1)))) {
                cursor += 2;
                while (cursor < sql.length() && part(sql.charAt(cursor))) {
                    cursor++;
                }
                result.add(new Token("", start, cursor, false, (char) 0));
                continue;
            }
            if (letter(value)) {
                cursor++;
                while (cursor < sql.length() && part(sql.charAt(cursor))) {
                    cursor++;
                }
                result.add(new Token(sql.substring(start, cursor), start, cursor, true, (char) 0));
                continue;
            }
            cursor++;
            result.add(new Token(String.valueOf(value), start, cursor, false, (char) 0));
        }
        return result;
    }

    /** The unquoted identifier alphabet already accepted by the affix rewriter. */
    private static boolean letter(char value) {
        return value >= 'a' && value <= 'z' || value >= 'A' && value <= 'Z' || value == '_';
    }

    /** Unquoted identifier continuation, including the dollar sign accepted by supported SQL engines. */
    private static boolean part(char value) {
        return letter(value) || value >= '0' && value <= '9' || value == '$';
    }

    /** A source token; a zero quote denotes an unquoted identifier or structural symbol. */
    private record Token(String value, int start, int end, boolean identifier, char quote) {

        private boolean is(String expected) {
            return quote == 0 && value.equalsIgnoreCase(expected);
        }

        private String render(String replacement) {
            if (quote == 0) {
                return replacement;
            }
            char close = quote == '[' ? ']' : quote;
            return quote + replacement.replace(String.valueOf(close), "" + close + close) + close;
        }
    }

    /** One non-recursive CTE name and the token interval in which it can be referenced. */
    private record Cte(Token name, int visible, int end) {
    }

    /**
     * Checks the encountered physical name and its de-affixed logical form against an ignore list.
     *
     * @param tableName encountered table name
     * @param ignore    logical or physical table names excluded from the current affix side
     * @return {@code true} when the current affix side must not be applied
     */
    private boolean shouldIgnore(String tableName, List<String> ignore) {
        if (ignore == null || ignore.isEmpty()) {
            return false;
        }
        String logicalName = tableName;
        if (StringKit.isNotEmpty(prefix) && logicalName.startsWith(prefix)) {
            logicalName = logicalName.substring(prefix.length());
        }
        if (StringKit.isNotEmpty(suffix) && logicalName.endsWith(suffix)) {
            logicalName = logicalName.substring(0, logicalName.length() - suffix.length());
        }
        String candidate = logicalName;
        return ignore.stream()
                .anyMatch(ignored -> ignored.equalsIgnoreCase(tableName) || ignored.equalsIgnoreCase(candidate));
    }

}
