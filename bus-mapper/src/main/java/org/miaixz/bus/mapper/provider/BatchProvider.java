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
package org.miaixz.bus.mapper.provider;

import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.ibatis.builder.annotation.ProviderContext;
import org.miaixz.bus.mapper.dialect.Dialect;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Batch operation SQL provider.
 *
 * <p>
 * Provides SQL generation methods for batch insert and batch UPSERT operations. Extends {@link BasicProvider} to reuse
 * shared SQL building logic.
 * </p>
 *
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>Multi-Values INSERT - High-performance batch insertion</li>
 * <li>Batch UPSERT - Update if exists, insert if not</li>
 * <li>Dynamic SQL generation for selective and dialect-aware operations</li>
 * <li>SQL caching based on the shared provider infrastructure</li>
 * </ul>
 *
 * <p>
 * <strong>Performance Comparison:</strong>
 * </p>
 * <ul>
 * <li>Multi-Values INSERT (MySQL/PostgreSQL): 10-100x faster than loop inserts</li>
 * <li>JDBC Batch (Oracle/SQL Server): Consistent performance, no row limit</li>
 * <li>UPSERT operations: Database-native, atomic and efficient</li>
 * </ul>
 *
 * <p>
 * <strong>Recommendations:</strong>
 * </p>
 * <ul>
 * <li>For MySQL/PostgreSQL: Use {@code insertBatch()} for best performance</li>
 * <li>For Oracle/SQL Server/DB2: Use JDBC batch mode with {@code sqlSession.commit()}</li>
 * <li>For mixed databases: Use {@code insertUpBatch()} only on dialects with native batch upsert support</li>
 * <li>For large datasets (&gt;1000 rows): Consider chunking into smaller batches</li>
 * <li>For SQLite: Use {@code insertUpBatch()} as INSERT OR REPLACE handles conflicts efficiently</li>
 * </ul>
 *
 * <p>
 * <strong>Example Usage:</strong>
 * </p>
 *
 * <pre>{@code
 * // MySQL/PostgreSQL - Fast multi-values insert
 * List<User> users = Arrays.asList(user1, user2, user3);
 * userMapper.insertBatch(users); // Single SQL: INSERT INTO ... VALUES (...),(...),(...)
 *
 * // Oracle/SQL Server - JDBC batch (recommended)
 * try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
 *     UserMapper mapper = session.getMapper(UserMapper.class);
 *     for (User user : users) {
 *         mapper.insert(user);
 *     }
 *     session.commit(); // Execute all inserts as a batch
 * }
 *
 * // Multi-datasource with dynamic detection
 * Holder.setKey("oracle_ds"); // Switch to Oracle datasource
 * // Use JDBC batch fallback instead of native insertUpBatch(users)
 *
 * Holder.setKey("mysql_ds"); // Switch to MySQL datasource
 * userMapper.insertUpBatch(users); // Automatically uses ON DUPLICATE KEY UPDATE
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
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
     * &lt;foreach collection="list" item="item" separator=","&gt;
     *   (#{item.col1}, #{item.col2}, #{item.col3})
     * &lt;/foreach&gt;
     * </pre>
     *
     * @param providerContext Provider context
     * @return SQL statement with foreach tags
     */
    public static String insertBatch(ProviderContext providerContext) {
        return cacheSql(providerContext, BatchProvider::insertBatch);
    }

    /**
     * Build Batch UPSERT SQL.
     *
     * <p>
     * Generates database-specific batch UPSERT statements:
     * </p>
     * <ul>
     * <li><b>MySQL/MariaDB:</b> INSERT ... VALUES ... ON DUPLICATE KEY UPDATE</li>
     * <li><b>PostgreSQL/H2/CirroData:</b> INSERT ... VALUES ... ON CONFLICT (...) DO UPDATE SET</li>
     * <li><b>SQLite:</b> INSERT OR REPLACE INTO ... VALUES ...</li>
     * </ul>
     *
     * <p>
     * <b>SQL Example (PostgreSQL):</b>
     * </p>
     * 
     * <pre>
     * INSERT INTO users (id, name, email) VALUES
     * &lt;foreach collection="list" item="item" separator=","&gt;
     *   (#{item.id}, #{item.name}, #{item.email})
     * &lt;/foreach&gt;
     * ON CONFLICT (id) DO UPDATE SET
     *   name = EXCLUDED.name,
     *   email = EXCLUDED.email
     * </pre>
     *
     * <p>
     * The SQL is generated dynamically at runtime based on the current datasource's dialect.
     *
     * @param providerContext Provider context containing entity metadata
     * @return SQL statement with foreach tags for batch UPSERT
     */
    public static String insertUpBatch(ProviderContext providerContext) {
        return cacheSqlDynamic(providerContext, BatchProvider::insertUpBatch);
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
     *   &lt;if test="list[0].col1 != null"&gt;col1,&lt;/if&gt;
     *   &lt;if test="list[0].col2 != null"&gt;col2,&lt;/if&gt;
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
        return cacheSqlDynamic(providerContext, BatchProvider::insertSelectiveBatch);
    }

    /**
     * Build Batch UPSERT SQL (insert only non-null fields).
     *
     * <p>
     * Generates UPSERT statements with dynamic SQL for only non-null fields on dialects that support native batch
     * UPSERT SQL. Other dialects must use JDBC batch fallback at the service layer.
     * </p>
     *
     * @param providerContext Provider context
     * @return SQL statement
     */
    public static String insertUpSelectiveBatch(ProviderContext providerContext) {
        return cacheSqlDynamic(providerContext, BatchProvider::insertUpSelectiveBatch);
    }

    /**
     * Build Multi-Values INSERT SQL.
     *
     * <p>
     * Generates SQL similar to:
     * </p>
     *
     * <pre>
     * INSERT INTO table (col1, col2, col3) VALUES
     * &lt;foreach collection="list" item="item" separator=","&gt;
     *   (#{item.col1}, #{item.col2}, #{item.col3})
     * &lt;/foreach&gt;
     * </pre>
     *
     * @param entity Table metadata
     * @return INSERT SQL with foreach tags
     */
    protected static String insertBatch(TableMeta entity) {
        String columnList = entity.insertColumns().stream().map(ColumnMeta::column).collect(Collectors.joining(", "));

        // Build foreach values placeholder: #{item.col1}, #{item.col2}, ...
        String valuesPlaceholder = entity.insertColumns().stream().map(col -> "#{item." + col.property() + "}")
                .collect(Collectors.joining(", "));

        // Build foreach SQL
        String foreachSql = "  <foreach collection=\"list\" item=\"item\" separator=\",\">\n" + "    ("
                + valuesPlaceholder + ")\n" + "  </foreach>";

        return "INSERT INTO " + entity.tableName() + " (" + columnList + ") VALUES\n" + foreachSql;
    }

    /**
     * Builds a dialect-aware native batch UPSERT SQL function.
     *
     * @param entity the table metadata
     * @return a function that renders SQL for the active dialect
     */
    protected static Function<Dialect, String> insertUpBatch(TableMeta entity) {
        return dialect -> {
            Dialect.Type upsertType = dialect.getUpsertType();
            if (upsertType == Dialect.Type.NONE) {
                throw unsupportedUpsert(dialect);
            }
            if (!supportsNativeBatchUpsert(upsertType)) {
                throw new UnsupportedOperationException(
                        dialect.getDatabase() + " does not support native batch UPSERT SQL; use JDBC batch fallback");
            }
            return switch (upsertType) {
                case INSERT_ON_DUPLICATE -> buildBatchInsertOnDuplicate(entity);
                case INSERT_ON_CONFLICT -> buildBatchInsertOnConflict(entity);
                case INSERT_OR_REPLACE -> buildBatchInsertOrReplace(entity);
                default -> throw new UnsupportedOperationException(
                        dialect.getDatabase() + " does not support native batch UPSERT SQL");
            };
        };
    }

    /**
     * Build batch INSERT SQL function (insert only non-null fields) that accepts Dialect parameter.
     *
     * <p>
     * This method returns a function that generates INSERT SQL. Although INSERT syntax is standard across databases, we
     * use {@code Function<Dialect, String>} for consistency with other selective batch methods.
     * </p>
     *
     * @param entity Table metadata
     * @return Function that accepts Dialect and returns INSERT SQL
     */
    protected static Function<Dialect, String> insertSelectiveBatch(TableMeta entity) {
        return dialect -> "INSERT INTO " + entity.tableName() + "\n" + buildDynamicColumnList(entity, "list", "0")
                + "\nVALUES\n" + buildBatchDynamicRows(entity);
    }

    /**
     * Builds a dialect-aware native selective batch UPSERT SQL function.
     *
     * <p>
     * Only dialects with stable native batch UPSERT syntax are allowed to proceed. Dialects that require {@code MERGE},
     * {@code UPDATE OR INSERT}, or driver-level batch execution are rejected explicitly so that callers can switch to a
     * JDBC batch fallback.
     * </p>
     *
     * @param entity the table metadata
     * @return a function that renders SQL for the active dialect
     */
    protected static Function<Dialect, String> insertUpSelectiveBatch(TableMeta entity) {
        return dialect -> {
            Dialect.Type upsertType = dialect.getUpsertType();
            if (upsertType == Dialect.Type.NONE) {
                throw unsupportedUpsert(dialect);
            }
            if (!supportsNativeBatchUpsert(upsertType)) {
                throw new UnsupportedOperationException(dialect.getDatabase()
                        + " does not support native batch selective UPSERT SQL; use JDBC batch fallback");
            }
            return switch (upsertType) {
                case INSERT_ON_DUPLICATE -> buildBatchInsertOnDuplicateSelective(entity);
                case INSERT_ON_CONFLICT -> buildBatchInsertOnConflictSelective(entity);
                case INSERT_OR_REPLACE -> buildBatchInsertOrReplaceSelective(entity);
                default -> throw new UnsupportedOperationException(
                        dialect.getDatabase() + " does not support native batch selective UPSERT SQL");
            };
        };
    }

    /**
     * Builds a MySQL-style native batch UPSERT statement.
     *
     * @param entity the table metadata
     * @return the generated SQL
     */
    private static String buildBatchInsertOnDuplicate(TableMeta entity) {
        return "INSERT INTO " + entity.tableName() + " (" + insertColumnList(entity) + ") VALUES\n"
                + buildBatchRowValues(entity) + "\nON DUPLICATE KEY UPDATE " + entity.updateColumns().stream()
                        .map(col -> col.column() + " = VALUES(" + col.column() + ")").collect(Collectors.joining(", "));
    }

    /**
     * Builds a PostgreSQL-style native batch UPSERT statement.
     *
     * @param entity the table metadata
     * @return the generated SQL
     */
    private static String buildBatchInsertOnConflict(TableMeta entity) {
        return "INSERT INTO " + entity.tableName() + " (" + insertColumnList(entity) + ") VALUES\n"
                + buildBatchRowValues(entity) + "\nON CONFLICT (" + keyColumnList(entity) + ") DO UPDATE SET "
                + entity.updateColumns().stream().map(col -> col.column() + " = EXCLUDED." + col.column())
                        .collect(Collectors.joining(", "));
    }

    /**
     * Builds a SQLite-style native batch UPSERT statement.
     *
     * @param entity the table metadata
     * @return the generated SQL
     */
    private static String buildBatchInsertOrReplace(TableMeta entity) {
        return "INSERT OR REPLACE INTO " + entity.tableName() + " (" + insertColumnList(entity) + ") VALUES\n"
                + buildBatchRowValues(entity);
    }

    /**
     * Builds a MySQL-style native batch selective UPSERT statement.
     *
     * @param entity the table metadata
     * @return the generated SQL
     */
    private static String buildBatchInsertOnDuplicateSelective(TableMeta entity) {
        return "INSERT INTO " + entity.tableName() + "\n" + buildDynamicColumnList(entity, "list", "0") + "\nVALUES\n"
                + buildBatchDynamicRows(entity) + "\nON DUPLICATE KEY UPDATE\n"
                + buildSelectiveAssignments(entity, "VALUES");
    }

    /**
     * Builds a PostgreSQL-style native batch selective UPSERT statement.
     *
     * @param entity the table metadata
     * @return the generated SQL
     */
    private static String buildBatchInsertOnConflictSelective(TableMeta entity) {
        return "INSERT INTO " + entity.tableName() + "\n" + buildDynamicColumnList(entity, "list", "0") + "\nVALUES\n"
                + buildBatchDynamicRows(entity) + "\nON CONFLICT (" + keyColumnList(entity) + ") DO UPDATE SET\n"
                + buildSelectiveAssignments(entity, "EXCLUDED");
    }

    /**
     * Builds a SQLite-style native batch selective UPSERT statement.
     *
     * @param entity the table metadata
     * @return the generated SQL
     */
    private static String buildBatchInsertOrReplaceSelective(TableMeta entity) {
        return "INSERT OR REPLACE INTO " + entity.tableName() + "\n" + buildDynamicColumnList(entity, "list", "0")
                + "\nVALUES\n" + buildBatchDynamicRows(entity);
    }

    /**
     * Builds the static multi-row value tuples used by native batch statements.
     *
     * @param entity the table metadata
     * @return the generated {@code <foreach>} block
     */
    private static String buildBatchRowValues(TableMeta entity) {
        String valuesPlaceholder = entity.insertColumns().stream().map(col -> "#{item." + col.property() + "}")
                .collect(Collectors.joining(", "));
        return "  <foreach collection=\"list\" item=\"item\" separator=\",\">\n    (" + valuesPlaceholder
                + ")\n  </foreach>";
    }

    /**
     * Builds dynamic multi-row value tuples for selective batch statements.
     *
     * @param entity the table metadata
     * @return the generated {@code <foreach>} block
     */
    private static String buildBatchDynamicRows(TableMeta entity) {
        return "<foreach collection=\"list\" item=\"item\" separator=\",\">\n" + buildDynamicValuesList(entity, "item")
                + "\n</foreach>";
    }

    /**
     * Builds a dynamic update assignment list for selective batch UPSERT SQL.
     *
     * @param entity the table metadata
     * @param mode   the right-hand-side reference mode
     * @return the generated assignment segment
     */
    private static String buildSelectiveAssignments(TableMeta entity, String mode) {
        StringBuilder sql = new StringBuilder();
        sql.append("<trim suffixOverrides=\",\">\n");
        for (ColumnMeta col : entity.updateColumns()) {
            sql.append("  <if test=\"").append(col.id() ? "true" : "list[0]." + col.property() + " != null\">")
                    .append(col.column()).append(" = ");
            if ("VALUES".equals(mode)) {
                sql.append("VALUES(").append(col.column()).append(")");
            } else {
                sql.append("EXCLUDED.").append(col.column());
            }
            sql.append(",</if>\n");
        }
        sql.append("</trim>");
        return sql.toString();
    }

}
