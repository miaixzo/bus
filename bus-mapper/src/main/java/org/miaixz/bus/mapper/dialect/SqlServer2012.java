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
 * Microsoft SQL Server 2012+ dialect.
 *
 * <p>
 * This is essentially the same as {@link SqlServer} but explicitly named for SQL Server 2012+. SQL Server 2012
 * introduced the OFFSET...FETCH syntax for pagination.
 * </p>
 *
 * <p>
 * Supports:
 * </p>
 * <ul>
 * <li>OFFSET...FETCH pagination (SQL Server 2012+)</li>
 * <li>MERGE (UPSERT)</li>
 * <li>JDBC batch operations</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SqlServer2012 extends AbstractDialect {

    public SqlServer2012() {
        super("Microsoft SQL Server", "jdbc:sqlserver:");
    }

    @Override
    public boolean supportsProduct(String productName) {
        // This is for explicit SQL Server 2012 usage
        // Normal SQL Server detection will use SqlServerDialect
        return false;
    }

    @Override
    public String getPaginationSql(String originalSql, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return originalSql;
        }

        StringBuilder sql = new StringBuilder(originalSql.length() + 100);
        sql.append(originalSql);

        // SQL Server 2012+ OFFSET...FETCH syntax
        // Requires an Order BY clause
        if (!originalSql.toUpperCase().contains("Order BY")) {
            // Add a default Order BY if none exists
            sql.append(" Order BY (SELECT NULL)");
        }

        sql.append(" OFFSET ").append(pageable.getOffset()).append(" ROWS");
        sql.append(" FETCH NEXT ").append(pageable.getPageSize()).append(" ROWS ONLY");

        return sql.toString();
    }

    @Override
    public boolean supportsMultiValuesInsert() {
        return false;
    }

    @Override
    public boolean supportsUpsert() {
        return true;
    }

    @Override
    public String getUpsertTemplate() {
        return "MERGE INTO %s AS target USING (VALUES %s) AS source (%s) ON %s WHEN MATCHED THEN UPDATE SET %s WHEN NOT MATCHED THEN INSERT (%s) VALUES (%s)";
    }

    @Override
    public String buildUpsertSql(
            String tableName,
            String columnList,
            String valuesList,
            String keyColumns,
            List<ColumnMeta> updateColumns,
            String itemPrefix) {
        // SQL Server 2012: MERGE INTO ... USING (VALUES ...) AS source (...) ON (...) WHEN MATCHED THEN UPDATE SET ...
        // WHEN NOT MATCHED THEN INSERT (...) VALUES (...) with dynamic <if> tags
        StringBuilder sb = new StringBuilder();
        sb.append("MERGE INTO ").append(tableName).append(" AS target\n");
        sb.append("USING (VALUES (").append(valuesList).append(")) AS source (").append(columnList).append(")\n");

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

    @Override
    public String getLimitKeyword() {
        return "FETCH NEXT";
    }

}
