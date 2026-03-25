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

import java.util.List;

import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.support.paging.Pageable;

/**
 * Firebird dialect.
 *
 * <p>
 * Supports:
 * </p>
 * <ul>
 * <li>ROWS...TO... pagination (Firebird 2.0+)</li>
 * <li>FIRST...SKIP... pagination</li>
 * <li>UPDATE OR INSERT (UPSERT)</li>
 * <li>JDBC batch operations</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Firebird extends AbstractDialect {

    public Firebird() {
        super("Firebird", "jdbc:firebirdsql:");
    }

    @Override
    public boolean supportsProduct(String productName) {
        if (productName == null) {
            return false;
        }
        String lower = productName.toLowerCase();
        return lower.contains("firebird");
    }

    @Override
    public String getPaginationSql(String originalSql, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return originalSql;
        }

        StringBuilder sql = new StringBuilder(originalSql.length() + 100);

        // Firebird 2.0+ uses ROWS...TO... syntax
        // SELECT FIRST n SKIP m * FROM table
        // or SELECT * FROM table ROWS m TO n

        // We'll use FIRST...SKIP... syntax which is more straightforward
        int selectIndex = originalSql.toUpperCase().indexOf("SELECT");
        if (selectIndex >= 0) {
            sql.append(originalSql, 0, selectIndex + 6); // Include "SELECT"
            sql.append(" FIRST ").append(pageable.getPageSize());

            if (pageable.getOffset() > 0) {
                sql.append(" SKIP ").append(pageable.getOffset());
            }

            sql.append(originalSql.substring(selectIndex + 6));
        } else {
            sql.append(originalSql);
        }

        return sql.toString();
    }

    @Override
    public boolean supportsMultiValuesInsert() {
        return false; // Firebird doesn't support standard multi-values syntax
    }

    @Override
    public boolean supportsUpsert() {
        return true; // Uses UPDATE OR INSERT
    }

    @Override
    public String getUpsertTemplate() {
        return "UPDATE OR INSERT INTO %s (%s) VALUES %s MATCHING (%s)";
    }

    @Override
    public String buildUpsertSql(
            String tableName,
            String columnList,
            String valuesList,
            String keyColumns,
            List<ColumnMeta> updateColumns,
            String itemPrefix) {
        // Firebird: UPDATE OR INSERT INTO ... VALUES ... MATCHING (...) with dynamic SQL for selective fields

        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE OR INSERT INTO ").append(tableName).append("\n");
        sb.append("  ").append(columnList).append("\n");
        sb.append("VALUES\n");
        sb.append("  ").append(valuesList).append("\n");
        sb.append("MATCHING (").append(keyColumns).append(")");

        return sb.toString();
    }

    @Override
    public String getLimitKeyword() {
        return "FIRST";
    }

    @Override
    public String getOffsetKeyword() {
        return "SKIP";
    }

}
