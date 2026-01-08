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
package org.miaixz.bus.mapper.provider;

import java.util.stream.Collectors;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.mapper.dialect.Dialect;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Batch operation SQL provider.
 *
 * <p>
 * Provides SQL generation methods for batch insert, update, delete and upsert operations. Extends BasicProvider to
 * reuse common SQL building logic.
 * </p>
 *
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Multi-Values INSERT - High-performance batch insertion</li>
 * <li>Batch UPSERT - Update if exists, insert if not</li>
 * <li>Dynamic SQL generation - Generate dialect-specific SQL</li>
 * <li>SQL caching - Based on BasicProvider caching mechanism</li>
 * </ul>
 *
 * <p>
 * Usage example:
 * </p>
 *
 * <pre>{@code
 * 
 * // Batch insert
 * String sql = BatchProvider.batchInsert(providerContext);
 *
 * // Upsert
 * String sql = BatchProvider.batchUpsert(providerContext);
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BatchProvider extends BasicProvider {

    /**
     * Build Multi-Values INSERT SQL.
     *
     * <p>
     * Generates SQL similar to:
     * </p>
     *
     * <pre>
     * INSERT INTO table (col1, col2, col3) VALUES
     * (?, ?, ?),
     * (?, ?, ?),
     * (?, ?, ?)
     * </pre>
     *
     * @param providerContext Provider context
     * @return SQL statement
     */
    public static String insertBatch(ProviderContext providerContext) {
        return cacheSql(providerContext, BatchProvider::insertBatch);
    }

    /**
     * Build Batch UPSERT SQL.
     *
     * <p>
     * Generates UPSERT statements according to different database dialects:
     * </p>
     * <ul>
     * <li>MySQL: INSERT ... ON DUPLICATE KEY UPDATE</li>
     * <li>PostgreSQL: INSERT ... ON CONFLICT DO UPDATE</li>
     * <li>SQLite: INSERT OR REPLACE</li>
     * </ul>
     *
     * @param providerContext Provider context
     * @return SQL statement
     */
    public static String insertUpBatch(ProviderContext providerContext) {
        return cacheSql(providerContext, BatchProvider::insertUpBatch);
    }

    /**
     * Build Multi-Values INSERT SQL (insert only non-null fields).
     *
     * <p>
     * Generates SQL similar to:
     * </p>
     *
     * <pre>
     * INSERT INTO table
     * &lt;trim prefix="(" suffix=")" suffixOverrides=","&gt;
     *   &lt;if test="item.col1 != null"&gt;col1,&lt;/if&gt;
     *   &lt;if test="item.col2 != null"&gt;col2,&lt;/if&gt;
     * &lt;/trim&gt;
     * VALUES
     * &lt;foreach collection="list" item="item" separator=","&gt;
     *   &lt;trim prefix="(" suffix=")" suffixOverrides=","&gt;
     *     &lt;if test="item.col1 != null"&gt;#{item.col1},&lt;/if&gt;
     *     &lt;if test="item.col2 != null"&gt;#{item.col2},&lt;/if&gt;
     *   &lt;/trim&gt;
     * &lt;/foreach&gt;
     * </pre>
     *
     * @param providerContext Provider context
     * @return SQL statement
     */
    public static String insertSelectiveBatch(ProviderContext providerContext) {
        return cacheSql(providerContext, BatchProvider::insertSelectiveBatch);
    }

    /**
     * Build batch insert SQL.
     *
     * @param entity Table metadata
     * @return INSERT SQL
     */
    protected static String insertBatch(TableMeta entity) {
        String columnList = entity.insertColumns().stream().map(ColumnMeta::column).collect(Collectors.joining(", "));

        String valuesPlaceholder = entity.insertColumns().stream().map(col -> "?").collect(Collectors.joining(", "));

        return String.format("INSERT INTO %s (%s) VALUES (%s)", entity.tableName(), columnList, valuesPlaceholder);
    }

    /**
     * Build batch upsert SQL.
     *
     * <p>
     * Note: This method returns template SQL, actual execution requires database dialect adjustment.
     * </p>
     *
     * @param entity Table metadata
     * @return UPSERT SQL template
     */
    protected static String insertUpBatch(TableMeta entity) {
        String columnList = entity.insertColumns().stream().map(ColumnMeta::column).collect(Collectors.joining(", "));

        String valuesPlaceholder = entity.insertColumns().stream().map(col -> "?").collect(Collectors.joining(", "));

        String updateSet = entity.updateColumns().stream().map(col -> col.column() + " = VALUES(" + col.column() + ")")
                .collect(Collectors.joining(", "));

        // MySQL style (most common)
        return String.format(
                "INSERT INTO %s (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s",
                entity.tableName(),
                columnList,
                valuesPlaceholder,
                updateSet);
    }

    /**
     * Build upsert SQL based on database dialect.
     *
     * @param entity  Table metadata
     * @param dialect Database dialect
     * @return UPSERT SQL
     */
    public static String buildUpsertSql(TableMeta entity, Dialect dialect) {
        String template = dialect.getUpsertTemplate();
        if (template == null) {
            throw new UnsupportedOperationException("Upsert is not supported by " + dialect.getDatabase());
        }

        String columnList = entity.insertColumns().stream().map(ColumnMeta::column).collect(Collectors.joining(", "));

        String valuesPlaceholder = entity.insertColumns().stream().map(col -> "?").collect(Collectors.joining(", "));

        String updateSet = entity.updateColumns().stream().map(col -> col.column() + " = VALUES(" + col.column() + ")")
                .collect(Collectors.joining(", "));

        // Use template provided by dialect
        return String.format(template, entity.tableName(), columnList, "(" + valuesPlaceholder + ")", updateSet);
    }

    /**
     * Build Multi-Values INSERT SQL (multiple rows).
     *
     * @param entity     Table metadata
     * @param valueCount Number of values
     * @return Multi-Values INSERT SQL
     */
    public static String buildMultiValuesInsert(TableMeta entity, int valueCount) {
        String columnList = entity.insertColumns().stream().map(ColumnMeta::column).collect(Collectors.joining(", "));

        String valuesPlaceholder = entity.insertColumns().stream().map(col -> "?").collect(Collectors.joining(", "));

        StringBuilder values = new StringBuilder();
        for (int i = 0; i < valueCount; i++) {
            if (i > 0) {
                values.append(",\n");
            }
            values.append("(").append(valuesPlaceholder).append(")");
        }

        return String.format("INSERT INTO %s (%s) VALUES\n%s", entity.tableName(), columnList, values);
    }

    /**
     * Build batch insert SQL (insert only non-null fields).
     *
     * <p>
     * Uses dynamic SQL to insert only non-null fields.
     * </p>
     *
     * @param entity Table metadata
     * @return INSERT SQL (dynamic SQL script)
     */
    protected static String insertSelectiveBatch(TableMeta entity) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(entity.tableName()).append("\n");

        // Dynamic column names
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        for (ColumnMeta col : entity.insertColumns()) {
            sql.append("  <if test=\"list[0].").append(col.property()).append(" != null\">").append(col.column())
                    .append(",</if>\n");
        }
        sql.append("</trim>\n");

        // VALUES clause
        sql.append("VALUES\n");
        sql.append("<foreach collection=\"list\" item=\"item\" separator=\",\">\n");
        sql.append("  <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        for (ColumnMeta col : entity.insertColumns()) {
            sql.append("    <if test=\"item.").append(col.property()).append(" != null\">#{item.")
                    .append(col.property()).append("},</if>\n");
        }
        sql.append("  </trim>\n");
        sql.append("</foreach>");

        return sql.toString();
    }

}
