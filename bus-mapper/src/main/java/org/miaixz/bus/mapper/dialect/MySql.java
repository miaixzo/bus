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
 * MySQL and MariaDB dialect.
 *
 * <p>
 * Supports:
 * </p>
 * <ul>
 * <li>LIMIT, OFFSET pagination</li>
 * <li>Multi-values INSERT</li>
 * <li>ON DUPLICATE KEY UPDATE (UPSERT)</li>
 * <li>JDBC batch operations</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MySql extends AbstractDialect {

    public MySql() {
        super("MySQL", "jdbc:mysql:");
    }

    @Override
    public boolean supportsProduct(String productName) {
        if (productName == null) {
            return false;
        }
        String lower = productName.toLowerCase();
        return lower.contains("mysql") || lower.contains("mariadb");
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
        return true;
    }

    @Override
    public String getUpsertTemplate() {
        return "INSERT INTO %s (%s) VALUES %s ON DUPLICATE KEY UPDATE %s";
    }

    @Override
    public String buildUpsertSql(
            String tableName,
            String columnList,
            String valuesList,
            String keyColumns,
            List<ColumnMeta> updateColumns,
            String itemPrefix) {
        // MySQL: INSERT INTO ... VALUES ... ON DUPLICATE KEY UPDATE with dynamic <if> tags
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(tableName).append(" (").append(columnList).append(") VALUES\n");
        sb.append(valuesList);
        sb.append("\nON DUPLICATE KEY UPDATE\n");

        for (ColumnMeta col : updateColumns) {
            String assignment = col.column() + " = VALUES(" + col.column() + ")";
            sb.append("  <if test=\"").append(itemPrefix).append(".").append(col.property()).append(" != null\">")
                    .append(assignment).append(",</if>\n");
        }

        return sb.toString();
    }

}
