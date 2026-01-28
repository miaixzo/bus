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
