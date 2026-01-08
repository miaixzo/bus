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
package org.miaixz.bus.mapper.support.prefix;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * SQL table name prefix builder using regex-based approach.
 *
 * <p>
 * This class applies table prefixes to SQL statements using regular expressions to identify and replace table names. It
 * supports standard SQL operations including SELECT, INSERT, UPDATE, and DELETE.
 * </p>
 *
 * <p>
 * <b>Pattern Matching Strategy:</b>
 * </p>
 * <ul>
 * <li>Matches table names after keywords: FROM, JOIN, INTO, UPDATE</li>
 * <li>Supports table names with optional schema qualifiers</li>
 * <li>Handles quoted identifiers (backticks, double quotes, square brackets)</li>
 * <li>Preserves table aliases and AS keywords</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TablePrefixBuilder {

    /**
     * Pattern to match table names in SQL statements.
     * <p>
     * <b>Regex Group Definition:</b>
     * <ol>
     * <li><b>Group 1 (Keyword):</b> Matches SQL keywords triggering a table reference (FROM, JOIN, INTO, UPDATE).</li>
     * <li><b>Group 2 (Schema):</b> Matches the optional schema/database prefix ending with a dot (e.g.,
     * "public.").</li>
     * <li><b>Group 3 (Start Quote):</b> Matches the opening delimiter (`, ", or [).</li>
     * <li><b>Group 4 (Table Name):</b> Matches the actual table name identifier.</li>
     * <li><b>Group 5 (End Quote):</b> Matches the closing delimiter (`, ", or ]).</li>
     * </ol>
     * </p>
     */
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("(?i)\\b(FROM|JOIN|INTO|UPDATE)\\s+" + // Group 1:
                                                                                                             // SQL
                                                                                                             // keywords
            "(?:([a-zA-Z_][a-zA-Z0-9_]*)\\.)?\\s*" + // Group 2: Optional schema
            "([`\"\\[]?)([a-zA-Z_][a-zA-Z0-9_]*)([`\"\\]]?)", // Group 3-5: Delimiters and Table name
            Pattern.CASE_INSENSITIVE);

    /**
     * The prefix string to be applied to table names.
     * <p>
     * Example: If prefix is "t_", table "user" becomes "t_user".
     * </p>
     */
    private final String prefix;

    /**
     * A list of table names that should be excluded from prefixing.
     * <p>
     * Comparisons against this list are case-insensitive.
     * </p>
     */
    private final List<String> ignore;

    /**
     * Constructs a TablePrefixBuilder with the specified prefix and ignore list.
     *
     * @param prefix the table prefix to apply (e.g., "db_")
     * @param ignore list of table names that should not receive prefix (can be null)
     */
    public TablePrefixBuilder(String prefix, List<String> ignore) {
        this.prefix = prefix;
        this.ignore = ignore;
    }

    /**
     * Apply prefix to all table names in the SQL statement.
     *
     * @param sql the original SQL statement
     * @return SQL statement with prefixes applied to table names
     */
    public String applyPrefix(String sql) {
        if (StringKit.isEmpty(sql) || StringKit.isEmpty(prefix)) {
            return sql;
        }

        Matcher matcher = TABLE_NAME_PATTERN.matcher(sql);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String keyword = matcher.group(1); // FROM/JOIN/INTO/UPDATE
            String schema = matcher.group(2); // Optional schema name
            String startDelim = matcher.group(3); // Starting delimiter (`"[)
            String tableName = matcher.group(4); // Actual table name
            String endDelim = matcher.group(5); // Ending delimiter (`"])

            // Check if table should be ignored
            if (shouldIgnore(tableName)) {
                continue;
            }

            // Check if table already has prefix
            if (tableName.startsWith(prefix)) {
                continue;
            }

            // Build replacement with prefix
            StringBuilder replacement = new StringBuilder();
            // Reconstruct the match: Keyword + Space
            replacement.append(keyword).append(Symbol.SPACE);

            // Append Schema if present
            if (StringKit.isNotEmpty(schema)) {
                replacement.append(schema).append(Symbol.DOT);
            }

            // Append Prefix + Table Name enclosed in original delimiters
            replacement.append(startDelim).append(prefix).append(tableName).append(endDelim);

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement.toString()));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Check if a table name should be ignored.
     *
     * @param tableName the table name to check
     * @return true if should ignore, false otherwise
     */
    private boolean shouldIgnore(String tableName) {
        if (ignore == null || ignore.isEmpty()) {
            return false;
        }

        // Case-insensitive comparison
        return ignore.stream().anyMatch(ignored -> ignored.equalsIgnoreCase(tableName));
    }

}
