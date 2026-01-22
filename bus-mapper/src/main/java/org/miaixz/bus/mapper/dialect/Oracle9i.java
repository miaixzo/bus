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
package org.miaixz.bus.mapper.dialect;

import java.util.List;

import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.support.paging.Pageable;

/**
 * Oracle 9i/10g/11g dialect (legacy versions).
 *
 * <p>
 * For Oracle versions before 12c that don't support OFFSET...FETCH syntax. Uses ROWNUM-based pagination.
 * </p>
 *
 * <p>
 * Supports:
 * </p>
 * <ul>
 * <li>ROWNUM-based pagination (nested queries)</li>
 * <li>MERGE INTO (UPSERT)</li>
 * <li>JDBC batch operations</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Oracle9i extends AbstractDialect {

    public Oracle9i() {
        super("Oracle9i", "jdbc:oracle:");
    }

    @Override
    public boolean supportsProduct(String productName) {
        // This dialect is for explicit Oracle 9i/10g/11g usage
        // Normal Oracle detection will use OracleDialect (12c+ syntax)
        return false;
    }

    @Override
    public String getPaginationSql(String originalSql, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return originalSql;
        }

        // Oracle 9i/10g/11g uses ROWNUM with nested queries
        // SELECT * FROM (
        // SELECT rownum rn, a.* FROM (original_sql) a WHERE rownum <= end_row
        // ) WHERE rn > start_row

        long startRow = pageable.getOffset();
        long endRow = startRow + pageable.getPageSize();

        StringBuilder sql = new StringBuilder(originalSql.length() + 200);
        sql.append("SELECT * FROM ( SELECT rownum AS rn__, inner__.* FROM ( ");
        sql.append(originalSql);
        sql.append(" ) inner__ WHERE rownum <= ").append(endRow);
        sql.append(" ) WHERE rn__ > ").append(startRow);

        return sql.toString();
    }

    @Override
    public String getCountSql(String originalSql) {
        return "SELECT COUNT(*) FROM (" + originalSql + ")";
    }

    @Override
    public boolean supportsMultiValuesInsert() {
        return false; // Oracle doesn't support standard multi-values syntax
    }

    @Override
    public boolean supportsUpsert() {
        return true; // Uses MERGE INTO
    }

    @Override
    public String getUpsertTemplate() {
        return "MERGE INTO %s USING DUAL ON (%s) WHEN MATCHED THEN UPDATE SET %s WHEN NOT MATCHED THEN INSERT (%s) VALUES (%s)";
    }

    @Override
    public String buildUpsertSql(
            String tableName,
            String columnList,
            String valuesList,
            String keyColumns,
            List<ColumnMeta> updateColumns,
            String itemPrefix) {
        // Oracle9i: MERGE INTO ... USING DUAL ON (...) WHEN MATCHED THEN UPDATE SET ... WHEN NOT MATCHED THEN INSERT
        // ... VALUES ...
        // Same syntax as Oracle 12c+
        StringBuilder sb = new StringBuilder();

        // Build ON clause for primary key matching
        sb.append("MERGE INTO ").append(tableName).append(" USING DUAL ON (");

        String[] keyColArray = keyColumns.split(",\\s*");
        for (int i = 0; i < keyColArray.length; i++) {
            if (i > 0) {
                sb.append(" AND ");
            }
            String colName = keyColArray[i].trim();
            String paramRef = itemPrefix.isEmpty() ? "#{" + colName + "}" : "#{" + itemPrefix + "." + colName + "}";
            sb.append(colName).append(" = ").append(paramRef);
        }
        sb.append(")\n");

        // WHEN MATCHED THEN UPDATE SET clause with dynamic <if> tags
        sb.append("WHEN MATCHED THEN UPDATE SET\n");
        for (ColumnMeta col : updateColumns) {
            sb.append("  <if test=\"").append(itemPrefix).append(".").append(col.property()).append(" != null\">")
                    .append(col.column()).append(" = ").append(col.variables()).append(",</if>\n");
        }

        // WHEN NOT MATCHED THEN INSERT clause
        sb.append("WHEN NOT MATCHED THEN INSERT\n");
        sb.append("  ").append(columnList).append("\n");
        sb.append("VALUES\n");
        sb.append("  ").append(valuesList);

        return sb.toString();
    }

    @Override
    public String getLimitKeyword() {
        return "ROWNUM";
    }

}
