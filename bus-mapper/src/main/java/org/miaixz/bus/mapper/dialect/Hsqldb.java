/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.mapper.dialect;

import org.miaixz.bus.mapper.support.paging.Pageable;
import java.util.List;
import org.miaixz.bus.mapper.parsing.ColumnMeta;

/**
 * HyperSQL (HSQLDB) dialect.
 *
 * <p>
 * Supports:
 * </p>
 * <ul>
 * <li>LIMIT OFFSET pagination</li>
 * <li>OFFSET...FETCH pagination (SQL standard)</li>
 * <li>Multi-Values INSERT</li>
 * <li>MERGE (UPSERT)</li>
 * <li>JDBC batch operations</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Hsqldb extends AbstractDialect {

    public Hsqldb() {
        super("HSQLDB", "jdbc:hsqldb:");
    }

    @Override
    public boolean supportsProduct(String productName) {
        if (productName == null) {
            return false;
        }
        String lower = productName.toLowerCase();
        return lower.contains("hsql") || lower.contains("hypersql");
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
        return true; // Uses MERGE
    }

    @Override
    public String getUpsertTemplate() {
        return "MERGE INTO %s USING (VALUES %s) AS source (%s) ON %s WHEN MATCHED THEN UPDATE SET %s WHEN NOT MATCHED THEN INSERT (%s) VALUES (%s)";
    }

    @Override
    public String buildUpsertSql(
            String tableName,
            String columnList,
            String valuesList,
            String keyColumns,
            List<ColumnMeta> updateColumns,
            String itemPrefix) {
        // Hsqldb: MERGE INTO ... USING (VALUES ...) AS source (...) ON (...) WHEN MATCHED THEN UPDATE SET ...
        // WHEN NOT MATCHED THEN INSERT (...) VALUES (...) with dynamic <if> tags
        StringBuilder sb = new StringBuilder();
        sb.append("MERGE INTO ").append(tableName).append(" USING (VALUES (").append(valuesList)
                .append(")) AS source (").append(columnList).append(")\n");

        sb.append("ON (");
        String[] keyColArray = keyColumns.split(",\\s*");
        for (int i = 0; i < keyColArray.length; i++) {
            if (i > 0) {
                sb.append(" AND ");
            }
            String colName = keyColArray[i].trim();
            sb.append("target.").append(colName).append(" = source.").append(colName);
        }
        sb.append(")\n");

        sb.append("WHEN MATCHED THEN UPDATE SET\n");
        for (ColumnMeta col : updateColumns) {
            sb.append("  <if test=\"").append(itemPrefix).append(".").append(col.property())
                    .append(" != null\">target.").append(col.column()).append(" = source.").append(col.column())
                    .append(",</if>\n");
        }

        sb.append("WHEN NOT MATCHED THEN INSERT (").append(columnList).append(")\n");
        sb.append("VALUES (");

        StringBuilder sourceValues = new StringBuilder();
        for (ColumnMeta col : updateColumns) {
            sourceValues.append("<if test=\"").append(itemPrefix).append(".").append(col.property())
                    .append(" != null\">source.").append(col.column()).append(",</if>");
        }
        sb.append(sourceValues).append(")");

        return sb.toString();
    }

}
