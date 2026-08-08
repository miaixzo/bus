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

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Rewrites physical table references in SQL by applying configured prefix and suffix rules.
 * <p>
 * The rewriter recognizes table names introduced by {@code FROM}, {@code JOIN}, {@code INTO}, and {@code UPDATE},
 * preserves schema qualifiers and identifier delimiters, and avoids applying the same affix more than once.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AffixSqlRewriter {

    /**
     * Tokens that can follow a table-introducing keyword but are not table names.
     */
    private static final Set<String> TABLE_NAME_STOP_WORDS = Set.of("SET", "VALUES", "SELECT", "WHERE", "RETURNING");

    /**
     * Matches table names after the SQL tokens that introduce physical table references.
     */
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile(
            "(?i)\\b(FROM|JOIN|INTO|UPDATE)\\s+(?:([a-zA-Z_][a-zA-Z0-9_]*)\\.)?\\s*"
                    + "([`\"\\[]?)([a-zA-Z_][a-zA-Z0-9_]*)([`\"\\]]?)",
            Pattern.CASE_INSENSITIVE);

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

        Matcher matcher = TABLE_NAME_PATTERN.matcher(sql);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String keyword = matcher.group(1);
            String schema = matcher.group(2);
            String startDelimiter = matcher.group(3);
            String tableName = matcher.group(4);
            String endDelimiter = matcher.group(5);

            if (TABLE_NAME_STOP_WORDS.contains(tableName.toUpperCase())) {
                continue;
            }

            boolean prefixed = StringKit.isEmpty(prefix) || tableName.startsWith(prefix)
                    || shouldIgnore(tableName, prefixIgnore);
            boolean suffixed = StringKit.isEmpty(suffix) || tableName.endsWith(suffix)
                    || shouldIgnore(tableName, suffixIgnore);
            if (prefixed && suffixed) {
                continue;
            }

            StringBuilder replacement = new StringBuilder(keyword).append(Symbol.SPACE);
            if (StringKit.isNotEmpty(schema)) {
                replacement.append(schema).append(Symbol.DOT);
            }
            replacement.append(startDelimiter);
            if (!prefixed) {
                replacement.append(prefix);
            }
            replacement.append(tableName);
            if (!suffixed) {
                replacement.append(suffix);
            }
            replacement.append(endDelimiter);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(result);
        return result.toString();
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
