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
package org.miaixz.bus.mapper.feature.visible;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * Visibility builder.
 *
 * <p>
 * Responsible for data visibility control logic. This class handles:
 * </p>
 * <ul>
 * <li>Entity metadata caching and management</li>
 * <li>SQL modification to add visibility conditions</li>
 * <li>Table name and alias extraction</li>
 * <li>Visibility condition generation</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class VisibleBuilder {

    /**
     * Pattern for extracting table name and alias from FROM clause.
     */
    private static final Pattern FROM_PATTERN = Pattern
            .compile("\\bFROM\\s+([\\w.]+)(?:\\s+(?:AS\\s+)?(\\w+))?", Pattern.CASE_INSENSITIVE);

    /**
     * SQL words that may follow a table name but must not be treated as table aliases.
     */
    private static final Set<String> ALIAS_STOP_WORDS = Set.of(
            "WHERE",
            "GROUP",
            "ORDER",
            "HAVING",
            "LIMIT",
            "OFFSET",
            "FETCH",
            "FOR",
            "JOIN",
            "INNER",
            "LEFT",
            "RIGHT",
            "FULL",
            "CROSS",
            "UNION",
            "EXCEPT",
            "INTERSECT");

    /**
     * Cache for entity visibility metadata.
     */
    private final Map<Class<?>, Boolean> visibilityCache = new ConcurrentHashMap<>();

    /**
     * Visibility configuration.
     */
    private final VisibleConfig config;

    /**
     * Constructor.
     *
     * @param config the visibility configuration
     */
    public VisibleBuilder(VisibleConfig config) {
        this.config = config;
    }

    /**
     * Check if entity requires visibility control.
     *
     * @param entityClass the entity class
     * @return true if visibility control is required, false otherwise
     */
    public boolean requiresVisibility(Class<?> entityClass) {
        if (entityClass == null) {
            return false;
        }
        return visibilityCache.computeIfAbsent(entityClass, this::hasVisibleAnnotation);
    }

    /**
     * Check if entity class has @Visible annotation.
     *
     * @param entityClass the entity class
     * @return true if has annotation, false otherwise
     */
    private boolean hasVisibleAnnotation(Class<?> entityClass) {
        return entityClass.isAnnotationPresent(org.miaixz.bus.core.lang.annotation.Visible.class);
    }

    /**
     * Apply visibility condition to SQL.
     *
     * @param sql the original SQL
     * @return the actual SQL with visibility condition
     */
    public String applyVisibility(String sql) {
        if (config.getProvider() == null || StringKit.isEmpty(sql)) {
            return sql;
        }

        try {
            // Extract table name and alias from SQL
            TableInfo tableInfo = extractTableInfo(sql);
            if (tableInfo == null) {
                return sql;
            }

            // Check if table should be ignored
            if (ignoredTable(tableInfo.tableName)) {
                Logger.debug(false, "Mapper", "Table {} ignored from visibility checking", tableInfo.tableName);
                return sql;
            }

            // Get visibility condition from provider
            String condition = config.getProvider().getVisible(tableInfo.tableName, tableInfo.tableAlias);
            condition = normalizeCondition(condition, tableInfo.tableAlias);

            if (StringKit.isEmpty(condition)) {
                return sql;
            }

            // Add visibility condition to SQL
            return addConditionToSql(sql, condition);

        } catch (Exception e) {
            Logger.error(false, "Mapper", "Failed to apply visibility condition: {}", e.getMessage(), e);
            return sql;
        }
    }

    /**
     * Extract table name and alias from SQL.
     *
     * @param sql the SQL statement
     * @return table information, or null if not found
     */
    private TableInfo extractTableInfo(String sql) {
        Matcher matcher = FROM_PATTERN.matcher(sql);
        if (matcher.find()) {
            String tableName = matcher.group(1);
            String tableAlias = normalizeAlias(matcher.group(2));
            return new TableInfo(tableName, tableAlias);
        }
        return null;
    }

    /**
     * Normalizes a candidate table alias extracted from SQL.
     * <p>
     * The alias must be an explicit alias from the SQL text. A missing alias is intentionally represented as an empty
     * string instead of falling back to the table name, because the table name is not a safe column qualifier after
     * other plugins rewrite table names or when joins are present.
     *
     * @param tableAlias the alias candidate
     * @return the real alias, or an empty string when absent or invalid
     */
    private String normalizeAlias(String tableAlias) {
        if (StringKit.isEmpty(tableAlias)) {
            return Normal.EMPTY;
        }
        String alias = tableAlias.trim();
        if (StringKit.isEmpty(alias) || ALIAS_STOP_WORDS.contains(alias.toUpperCase(Locale.ROOT))) {
            return Normal.EMPTY;
        }
        return alias;
    }

    /**
     * Normalizes a provider condition for SQL without a real table alias.
     * <p>
     * Older provider examples concatenated {@code tableAlias + ".column"}. When the SQL has no alias, the corrected
     * alias value is empty, so those implementations would otherwise produce a leading dot such as {@code .user_id}.
     * This keeps that common legacy expression valid without inventing a table-name qualifier.
     *
     * @param condition  the provider condition
     * @param tableAlias the real table alias, or empty when absent
     * @return the normalized provider condition
     */
    private String normalizeCondition(String condition, String tableAlias) {
        if (StringKit.isEmpty(condition) || StringKit.isNotEmpty(tableAlias)) {
            return condition;
        }
        StringBuilder builder = null;
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        for (int i = 0; i < condition.length(); i++) {
            char current = condition.charAt(i);
            if (singleQuoted) {
                if (current == Symbol.C_SINGLE_QUOTE && i + 1 < condition.length()
                        && condition.charAt(i + 1) == Symbol.C_SINGLE_QUOTE) {
                    if (builder != null) {
                        builder.append(current).append(condition.charAt(i + 1));
                    }
                    i++;
                    continue;
                }
                if (current == Symbol.C_SINGLE_QUOTE) {
                    singleQuoted = false;
                }
            } else if (doubleQuoted) {
                if (current == Symbol.C_DOUBLE_QUOTES && i + 1 < condition.length()
                        && condition.charAt(i + 1) == Symbol.C_DOUBLE_QUOTES) {
                    if (builder != null) {
                        builder.append(current).append(condition.charAt(i + 1));
                    }
                    i++;
                    continue;
                }
                if (current == Symbol.C_DOUBLE_QUOTES) {
                    doubleQuoted = false;
                }
            } else if (current == Symbol.C_SINGLE_QUOTE) {
                singleQuoted = true;
            } else if (current == Symbol.C_DOUBLE_QUOTES) {
                doubleQuoted = true;
            }
            if (!singleQuoted && !doubleQuoted && current == Symbol.C_DOT && removableLeadingDot(condition, i)) {
                if (builder == null) {
                    builder = new StringBuilder(condition.length());
                    builder.append(condition, 0, i);
                }
                continue;
            }
            if (builder != null) {
                builder.append(current);
            }
        }
        return builder == null ? condition : builder.toString();
    }

    /**
     * Tests whether a dot belongs to a legacy alias-prefix expression that should be removed.
     *
     * @param condition the provider condition
     * @param index     the dot index
     * @return {@code true} when the dot can be removed
     */
    private boolean removableLeadingDot(String condition, int index) {
        if (index + 1 >= condition.length() || !identifierStart(condition.charAt(index + 1))) {
            return false;
        }
        if (index == 0) {
            return true;
        }
        char previous = condition.charAt(index - 1);
        return Character.isWhitespace(previous) || previous == '(' || previous == '[' || previous == '{'
                || previous == ',' || previous == '=' || previous == '<' || previous == '>' || previous == '!'
                || previous == '&' || previous == '|' || previous == '+' || previous == '-' || previous == '*'
                || previous == '/';
    }

    /**
     * Tests whether a character may start a SQL identifier.
     *
     * @param value the character to test
     * @return {@code true} when the character can start an identifier
     */
    private boolean identifierStart(char value) {
        return Character.isLetter(value) || value == '_';
    }

    /**
     * Add visibility condition to SQL WHERE clause.
     *
     * @param sql       the original SQL
     * @param condition the visibility condition
     * @return the actual SQL
     */
    private String addConditionToSql(String sql, String condition) {
        int whereIndex = indexOfTopLevelKeyword(sql, "WHERE");

        if (whereIndex != -1) {
            // Already has WHERE clause, add condition with AND
            int insertPosition = skipWhitespace(sql, whereIndex + "WHERE".length());
            return sql.substring(0, insertPosition).stripTrailing() + " (" + condition + ") AND "
                    + sql.substring(insertPosition);
        } else {
            // No WHERE clause, add WHERE with condition
            // Find position before ORDER BY, GROUP BY, LIMIT, etc.
            int insertPosition = findInsertPosition(sql);
            String beforeTail = sql.substring(0, insertPosition).stripTrailing();
            String tail = sql.substring(insertPosition).stripLeading();
            return beforeTail + " WHERE (" + condition + ")"
                    + (StringKit.isEmpty(tail) ? Normal.EMPTY : Symbol.SPACE + tail);
        }
    }

    /**
     * Find the position to insert WHERE clause.
     *
     * @param sql the SQL statement
     * @return the insert position
     */
    private int findInsertPosition(String sql) {
        int index = firstTopLevelKeyword(
                sql,
                "ORDER BY",
                "GROUP BY",
                "HAVING",
                "LIMIT",
                "OFFSET",
                "FETCH",
                "FOR",
                "UNION");
        return index < 0 ? sql.length() : index;
    }

    /**
     * Finds the first top-level keyword from a set of candidates.
     *
     * @param sql      the SQL statement
     * @param keywords the keywords to find
     * @return the first keyword index, or {@code -1}
     */
    private int firstTopLevelKeyword(String sql, String... keywords) {
        int first = -1;
        for (String keyword : keywords) {
            int index = indexOfTopLevelKeyword(sql, keyword);
            if (index >= 0 && (first < 0 || index < first)) {
                first = index;
            }
        }
        return first;
    }

    /**
     * Finds a top-level keyword outside strings and nested expressions.
     *
     * @param sql     the SQL statement
     * @param keyword the keyword to find
     * @return the keyword index, or {@code -1}
     */
    private int indexOfTopLevelKeyword(String sql, String keyword) {
        int depth = 0;
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        boolean backtickQuoted = false;
        boolean bracketQuoted = false;
        for (int i = 0; i < sql.length(); i++) {
            char current = sql.charAt(i);
            if (singleQuoted) {
                if (current == Symbol.C_SINGLE_QUOTE && i + 1 < sql.length()
                        && sql.charAt(i + 1) == Symbol.C_SINGLE_QUOTE) {
                    i++;
                    continue;
                }
                if (current == Symbol.C_SINGLE_QUOTE) {
                    singleQuoted = false;
                }
                continue;
            }
            if (doubleQuoted) {
                if (current == Symbol.C_DOUBLE_QUOTES && i + 1 < sql.length()
                        && sql.charAt(i + 1) == Symbol.C_DOUBLE_QUOTES) {
                    i++;
                    continue;
                }
                if (current == Symbol.C_DOUBLE_QUOTES) {
                    doubleQuoted = false;
                }
                continue;
            }
            if (backtickQuoted) {
                if (current == '`') {
                    backtickQuoted = false;
                }
                continue;
            }
            if (bracketQuoted) {
                if (current == ']') {
                    bracketQuoted = false;
                }
                continue;
            }
            if (current == Symbol.C_SINGLE_QUOTE) {
                singleQuoted = true;
                continue;
            }
            if (current == Symbol.C_DOUBLE_QUOTES) {
                doubleQuoted = true;
                continue;
            }
            if (current == '`') {
                backtickQuoted = true;
                continue;
            }
            if (current == '[') {
                bracketQuoted = true;
                continue;
            }
            if (current == '(') {
                depth++;
                continue;
            }
            if (current == ')' && depth > 0) {
                depth--;
                continue;
            }
            if (depth == 0 && keywordAt(sql, i, keyword)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Tests whether a keyword starts at the specified index.
     *
     * @param sql     the SQL statement
     * @param index   the index to test
     * @param keyword the keyword
     * @return {@code true} when matched
     */
    private boolean keywordAt(String sql, int index, String keyword) {
        if (index < 0 || index + keyword.length() > sql.length()
                || !sql.regionMatches(true, index, keyword, 0, keyword.length())) {
            return false;
        }
        int before = index - 1;
        int after = index + keyword.length();
        return (before < 0 || !identifierPart(sql.charAt(before)))
                && (after >= sql.length() || !identifierPart(sql.charAt(after)));
    }

    /**
     * Skips whitespace characters.
     *
     * @param sql   the SQL statement
     * @param index the start index
     * @return the next non-whitespace index
     */
    private int skipWhitespace(String sql, int index) {
        while (index < sql.length() && Character.isWhitespace(sql.charAt(index))) {
            index++;
        }
        return index;
    }

    /**
     * Tests whether a character belongs to an SQL identifier.
     *
     * @param value the character to test
     * @return {@code true} when the character belongs to an identifier
     */
    private boolean identifierPart(char value) {
        return Character.isLetterOrDigit(value) || value == '_';
    }

    /**
     * Tests whether visibility filtering should ignore a table.
     *
     * @param tableName the table name from SQL
     * @return {@code true} when visibility filtering should be skipped
     */
    private boolean ignoredTable(String tableName) {
        return config.isIgnore(tableName) || config.isIgnore(simpleTableName(tableName));
    }

    /**
     * Returns the table name without a qualifier.
     *
     * @param tableName the table name
     * @return the simple table name
     */
    private String simpleTableName(String tableName) {
        int qualifier = tableName == null ? -1 : tableName.lastIndexOf(Symbol.C_DOT);
        return qualifier < 0 ? tableName : tableName.substring(qualifier + 1);
    }

    /**
     * Clear metadata cache.
     */
    public void clear() {
        visibilityCache.clear();
    }

    /**
     * Table information holder.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static class TableInfo {

        /**
         * Resolved table name.
         */
        final String tableName;

        /**
         * Resolved real table alias, or empty when the SQL does not declare one.
         */
        final String tableAlias;

        /**
         * Creates table information.
         *
         * @param tableName  the resolved table name
         * @param tableAlias the resolved real table alias, or empty when absent
         */
        TableInfo(String tableName, String tableAlias) {
            this.tableName = tableName;
            this.tableAlias = tableAlias;
        }

    }

}
