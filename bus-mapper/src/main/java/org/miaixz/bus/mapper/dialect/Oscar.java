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
package org.miaixz.bus.mapper.dialect;

import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.support.paging.Pageable;

import java.util.List;

/**
 * Oscar database dialect.
 *
 * <p>
 * Oscar is a Chinese domestic database that is Oracle-compatible (神通数据库).
 * </p>
 *
 * <p>
 * Supports:
 * </p>
 * <ul>
 * <li>LIMIT OFFSET pagination (PostgreSQL-style)</li>
 * <li>OFFSET...FETCH pagination (Oracle-style)</li>
 * <li>Multi-Values INSERT</li>
 * <li>MERGE INTO (UPSERT)</li>
 * <li>JDBC batch operations</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Oscar extends AbstractDialect {

    public Oscar() {
        super("Oscar", "jdbc:oscar:");
    }

    @Override
    public boolean supportsProduct(String productName) {
        if (productName == null) {
            return false;
        }
        String lower = productName.toLowerCase();
        return lower.contains("oscar") || lower.contains("神通");
    }

    @Override
    public String getPaginationSql(String originalSql, Pageable pageable) {
        return buildLimitOffsetPagination(originalSql, pageable);
    }

    @Override
    public boolean supportsMultiValuesInsert() {
        return true;
    }

    @Override
    public boolean supportsUpsert() {
        return true; // Uses MERGE INTO (Oracle-compatible)
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
        // Oscar: MERGE INTO ... USING DUAL ON (...) WHEN MATCHED THEN UPDATE SET ... WHEN NOT MATCHED THEN INSERT ...
        // VALUES ...
        // Same syntax as Oracle
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

}
