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
package org.miaixz.bus.mapper.support.visible;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * @since Java 17+
 */
public class VisibleBuilder {

    /**
     * Pattern for extracting table name and alias from FROM clause.
     */
    private static final Pattern FROM_PATTERN = Pattern
            .compile("\\bFROM\\s+(\\w+)(?:\\s+(?:AS\\s+)?(\\w+))?", Pattern.CASE_INSENSITIVE);
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
     * @return the modified SQL with visibility condition
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
            if (config.isIgnore(tableInfo.tableName)) {
                Logger.debug(false, "Visible", "Table {} ignored from visibility checking", tableInfo.tableName);
                return sql;
            }

            // Get visibility condition from provider
            String condition = config.getProvider().getVisible(tableInfo.tableName, tableInfo.tableAlias);

            if (StringKit.isEmpty(condition)) {
                return sql;
            }

            // Add visibility condition to SQL
            return addConditionToSql(sql, condition);

        } catch (Exception e) {
            Logger.error(false, "Visible", "Failed to apply visibility condition: {}", e.getMessage(), e);
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
            String tableAlias = matcher.group(2);
            // If no alias, use table name as alias
            if (tableAlias == null) {
                tableAlias = tableName;
            }
            return new TableInfo(tableName, tableAlias);
        }
        return null;
    }

    /**
     * Add visibility condition to SQL WHERE clause.
     *
     * @param sql       the original SQL
     * @param condition the visibility condition
     * @return the modified SQL
     */
    private String addConditionToSql(String sql, String condition) {
        // Find WHERE clause position
        String upperSql = sql.toUpperCase();
        int whereIndex = upperSql.indexOf("WHERE");

        if (whereIndex != -1) {
            // Already has WHERE clause, add condition with AND
            int insertPosition = whereIndex + 5; // Length of "WHERE"
            return sql.substring(0, insertPosition) + " (" + condition + ") AND" + sql.substring(insertPosition);
        } else {
            // No WHERE clause, add WHERE with condition
            // Find position before ORDER BY, GROUP BY, LIMIT, etc.
            int insertPosition = findInsertPosition(upperSql);
            return sql.substring(0, insertPosition) + " WHERE (" + condition + ")" + sql.substring(insertPosition);
        }
    }

    /**
     * Find the position to insert WHERE clause.
     *
     * @param upperSql the uppercase SQL
     * @return the insert position
     */
    private int findInsertPosition(String upperSql) {
        int orderByIndex = upperSql.indexOf("ORDER BY");
        int groupByIndex = upperSql.indexOf("GROUP BY");
        int limitIndex = upperSql.indexOf("LIMIT");

        int minIndex = upperSql.length();

        if (orderByIndex != -1 && orderByIndex < minIndex) {
            minIndex = orderByIndex;
        }
        if (groupByIndex != -1 && groupByIndex < minIndex) {
            minIndex = groupByIndex;
        }
        if (limitIndex != -1 && limitIndex < minIndex) {
            minIndex = limitIndex;
        }

        return minIndex;
    }

    /**
     * Clear metadata cache.
     */
    public void clear() {
        visibilityCache.clear();
    }

    /**
     * Table information holder.
     */
    private static class TableInfo {

        final String tableName;
        final String tableAlias;

        TableInfo(String tableName, String tableAlias) {
            this.tableName = tableName;
            this.tableAlias = tableAlias;
        }

    }

}
