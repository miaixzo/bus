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
 * <li>For mixed databases: Use {@code insertUpBatch()} with dynamic datasource detection</li>
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
 * userMapper.insertUpBatch(users); // Automatically uses MERGE syntax
 *
 * Holder.setKey("mysql_ds"); // Switch to MySQL datasource
 * userMapper.insertUpBatch(users); // Automatically uses ON DUPLICATE KEY UPDATE
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
     * The SQL is generated dynamically at runtime based on the current datasource's dialect. Uses:
     * </p>
     * <ul>
     * <li>{@link Dialect#supportsUpsert()} - Checks if UPSERT is supported</li>
     * <li>{@link Dialect#getUpsertTemplate()} - Gets the UPSERT SQL template</li>
     * </ul>
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
     * Generates UPSERT statements with dynamic SQL for only non-null fields. Uses {@link Dialect#supportsUpsert()},
     * {@link Dialect#getUpsertTemplate()}.
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
     * Build batch UPSERT SQL function that accepts Dialect parameter.
     *
     * <p>
     * This method returns a function that generates database-specific batch UPSERT SQL. The SQL is generated
     * dynamically at runtime based on the current datasource's dialect.
     * </p>
     *
     * <p>
     * <b>Implementation Details:</b>
     * </p>
     * <ul>
     * <li>Checks {@link Dialect#supportsUpsert()} to verify UPSERT support</li>
     * <li>Gets UPSERT template via {@link Dialect#getUpsertTemplate()}</li>
     * <li>Generates {@code <foreach>} tags for multi-values INSERT</li>
     * <li>Builds UPDATE clause inline based on dialect type:
     * <ul>
     * <li>MySQL/MariaDB: {@code col = VALUES(col)}</li>
     * <li>PostgreSQL/H2/CirroData: {@code col = EXCLUDED.col}</li>
     * </ul>
     * </li>
     * <li>Applies template with 5 parameters: tableName, columnList, foreachValues, keyColumns, updateClause</li>
     * </ul>
     *
     * <p>
     * <b>Generated SQL Example (PostgreSQL):</b>
     * </p>
     * 
     * <pre>
     * INSERT INTO users (id, name, email) VALUES
     *   &lt;foreach collection="list" item="item" separator=","&gt;
     *     (#{item.id}, #{item.name}, #{item.email})
     *   &lt;/foreach&gt;
     * ON CONFLICT (id) DO UPDATE SET
     *   name = EXCLUDED.name,
     *   email = EXCLUDED.email
     * </pre>
     *
     * @param entity Table metadata containing column and key information
     * @return Function that accepts Dialect and returns complete UPSERT SQL with foreach tags
     */
    protected static Function<Dialect, String> insertUpBatch(TableMeta entity) {
        return dialect -> {
            // Step 1: Check if UPSERT is supported
            if (!dialect.supportsUpsert()) {
                throw new UnsupportedOperationException(dialect.getDatabase() + " does not support UPSERT operations");
            }

            // Get key and update columns
            String keyColumns = entity.idColumns().stream().map(ColumnMeta::column).collect(Collectors.joining(", "));
            String columnList = entity.insertColumns().stream().map(ColumnMeta::column)
                    .collect(Collectors.joining(", "));

            // Step 2: Use getUpsertTemplate()
            String template = dialect.getUpsertTemplate();
            if (template == null || template.isEmpty()) {
                throw new UnsupportedOperationException(dialect.getDatabase() + " does not provide UPSERT template");
            }

            // Build values placeholder with foreach tags
            String valuesPlaceholder = entity.insertColumns().stream().map(col -> "#{item." + col.property() + "}")
                    .collect(Collectors.joining(", "));
            String foreachValues = "  <foreach collection=\"list\" item=\"item\" separator=\",\">\n" + "    ("
                    + valuesPlaceholder + ")\n" + "  </foreach>";

            // Step 3: Build UPDATE clause inline based on dialect type
            String updateClause;
            String database = dialect.getDatabase();
            if (database.contains("MySQL") || database.contains("MariaDB")) {
                // MySQL: col1 = VALUES(col1), col2 = VALUES(col2)
                updateClause = entity.updateColumns().stream()
                        .map(col -> col.column() + " = VALUES(" + col.column() + ")").collect(Collectors.joining(", "));
            } else {
                // PostgreSQL, H2, CirroData: col1 = EXCLUDED.col1, col2 = EXCLUDED.col2
                updateClause = entity.updateColumns().stream().map(col -> col.column() + " = EXCLUDED." + col.column())
                        .collect(Collectors.joining(", "));
            }

            return String.format(template, entity.tableName(), columnList, foreachValues, keyColumns, updateClause);
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
        return dialect -> {
            // Build dynamic column list and values list
            String columnList = buildDynamicColumnList(entity, "list", "0");
            String valuesList = "  <foreach collection=\"list\" item=\"item\" separator=\",\">\n"
                    + buildDynamicValuesList(entity, "item") + "\n  </foreach>";

            // Use template to build final SQL
            return String.format(dialect.getInsertTemplate(), entity.tableName(), columnList, valuesList);
        };
    }

    /**
     * Build batch UPSERT SQL function (insert only non-null fields) that accepts Dialect parameter.
     *
     * <p>
     * This method returns a function that generates database-specific batch UPSERT SQL with dynamic SQL for only
     * non-null fields. The SQL is generated dynamically at runtime based on the current datasource's dialect.
     * </p>
     *
     * <p>
     * <b>Implementation Details:</b>
     * </p>
     * <ul>
     * <li>Checks {@link Dialect#supportsUpsert()} to verify UPSERT support</li>
     * <li>Detects if dialect uses INSERT OR REPLACE (SQLite) by checking template</li>
     * <li>For SQLite: Uses INSERT OR REPLACE template with dynamic columns</li>
     * <li>For other databases: Uses {@link Dialect#buildUpsertSql} to generate complete dynamic UPSERT</li>
     * </ul>
     *
     * <p>
     * <b>Generated SQL Example (PostgreSQL):</b>
     * </p>
     * 
     * <pre>
     * INSERT INTO users
     * &lt;trim prefix="(" suffix=")" suffixOverrides=","&gt;
     *   &lt;if test="list[0].id != null"&gt;id,&lt;/if&gt;
     *   &lt;if test="list[0].name != null"&gt;name,&lt;/if&gt;
     *   &lt;if test="list[0].email != null"&gt;email,&lt;/if&gt;
     * &lt;/trim&gt;
     * VALUES
     * &lt;foreach collection="list" item="item" separator=","&gt;
     *   &lt;trim prefix="(" suffix=")" suffixOverrides=","&gt;
     *     &lt;if test="item.id != null"&gt;#{item.id},&lt;/if&gt;
     *     &lt;if test="item.name != null"&gt;#{item.name},&lt;/if&gt;
     *     &lt;if test="item.email != null"&gt;#{item.email},&lt;/if&gt;
     *   &lt;/trim&gt;
     * &lt;/foreach&gt;
     * ON CONFLICT (id) DO UPDATE SET
     *   &lt;if test="list[0].name != null"&gt;name = EXCLUDED.name,&lt;/if&gt;
     *   &lt;if test="list[0].email != null"&gt;email = EXCLUDED.email,&lt;/if&gt;
     * </pre>
     *
     * @param entity Table metadata containing column and key information
     * @return Function that accepts Dialect and returns complete dynamic UPSERT SQL
     */
    protected static Function<Dialect, String> insertUpSelectiveBatch(TableMeta entity) {
        return dialect -> {
            // Step 1: Check if UPSERT is supported
            if (!dialect.supportsUpsert()) {
                throw new UnsupportedOperationException(dialect.getDatabase() + " does not support UPSERT operations");
            }

            // Check if using INSERT OR REPLACE by examining the template
            String upsertTemplate = dialect.getUpsertTemplate();
            boolean useInsertOrReplace = upsertTemplate != null && upsertTemplate.contains("INSERT OR REPLACE");

            // Step 2: Build dynamic column list and values list
            String columnList = buildDynamicColumnList(entity, "list", "0");
            String valuesList = "  <foreach collection=\"list\" item=\"item\" separator=\",\">\n"
                    + buildDynamicValuesList(entity, "item") + "\n  </foreach>";

            StringBuilder sql = new StringBuilder();
            if (useInsertOrReplace) {
                // SQLite: Use INSERT OR REPLACE template
                sql.append("INSERT OR REPLACE INTO ").append(entity.tableName()).append(" (").append(columnList)
                        .append(") VALUES\n");
                sql.append(valuesList);
            } else {
                // MySQL/PostgreSQL/H2: Use buildUpsertSql()
                String keyColumns = entity.idColumns().stream().map(ColumnMeta::column)
                        .collect(Collectors.joining(", "));
                String upsertSql = dialect.buildUpsertSql(
                        entity.tableName(),
                        columnList,
                        valuesList,
                        keyColumns,
                        entity.updateColumns(),
                        "list[0]");
                sql.append(upsertSql);
            }

            return sql.toString();
        };
    }

}
