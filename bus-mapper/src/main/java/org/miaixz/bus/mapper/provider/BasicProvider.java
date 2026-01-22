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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.dialect.Dialect;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.SqlScript;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Base class for providers, unifying SQL caching logic and reducing code duplication.
 *
 * <p>
 * This class provides common SQL building methods that all Provider classes should extend. Through the template method
 * pattern, SQL building logic is extracted to the base class, reducing code duplication by over 50%.
 * </p>
 *
 * <p>
 * Design goals:
 * </p>
 * <ul>
 * <li>Unified SQL caching logic</li>
 * <li>Provide common SQL building methods</li>
 * <li>Reduce code duplication in subclasses</li>
 * <li>Improve code maintainability</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class BasicProvider {

    /**
     * Template method for caching SQL scripts.
     *
     * <p>
     * This method encapsulates the common logic for SQL caching; subclasses only need to provide the SQL building
     * function.
     * </p>
     *
     * @param providerContext Provider context
     * @param sqlBuilder      SQL building function
     * @return Cache key
     */
    protected static String cacheSql(ProviderContext providerContext, Function<TableMeta, String> sqlBuilder) {
        return SqlScript.caching(providerContext, entity -> sqlBuilder.apply(entity));
    }

    /**
     * Template method for caching dynamic SQL scripts (that depend on database dialect).
     *
     * <p>
     * This method is used for SQL that needs to be generated dynamically based on the current datasource's dialect. The
     * SQL is not generated at cache time, but rather at execution time when the dialect is known.
     * </p>
     *
     * @param providerContext Provider context
     * @param sqlBuilder      SQL building function that accepts Dialect parameter
     * @return Cache key
     */
    protected static String cacheSqlDynamic(
            ProviderContext providerContext,
            Function<TableMeta, Function<Dialect, String>> sqlBuilder) {
        return SqlScript.cachingDynamic(providerContext, (entity, dialect) -> sqlBuilder.apply(entity).apply(dialect));
    }

    /**
     * Build INSERT statement (insert all fields).
     *
     * @param entity Table metadata
     * @return INSERT SQL
     */
    protected static String buildInsertAll(TableMeta entity) {
        return "INSERT INTO " + entity.tableName() + " (" + entity.insertColumnList() + ")" + " VALUES ("
                + entity.insertColumns().stream().map(ColumnMeta::variables).collect(Collectors.joining(Symbol.COMMA))
                + ")";
    }

    /**
     * Build INSERT statement (insert only non-null fields).
     *
     * @param entity Table metadata
     * @return INSERT SQL (dynamic SQL)
     */
    protected static String buildInsertSelective(TableMeta entity) {
        return "INSERT INTO " + entity.tableName() + "<trim prefix='(' suffix=')' suffixOverrides=','>"
                + entity.insertColumns().stream()
                        .map(col -> "<if test='" + col.notNullTest() + "'>" + col.column() + "," + "</if>")
                        .collect(Collectors.joining("\n"))
                + "</trim>" + "<trim prefix='VALUES (' suffix=')' suffixOverrides=','>"
                + entity.insertColumns().stream()
                        .map(col -> "<if test='" + col.notNullTest() + "'>" + col.variables() + "," + "</if>")
                        .collect(Collectors.joining("\n"))
                + "</trim>";
    }

    /**
     * Build UPDATE statement (update all fields).
     *
     * @param entity Table metadata
     * @return UPDATE SQL
     */
    protected static String buildUpdateAll(TableMeta entity) {
        return "UPDATE "
                + entity.tableName() + " SET " + entity.updateColumns().stream()
                        .map(col -> col.column() + " = " + col.variables()).collect(Collectors.joining(", "))
                + buildWherePrimaryKey(entity, null);
    }

    /**
     * Build UPDATE statement (update only non-null fields).
     *
     * @param entity Table metadata
     * @return UPDATE SQL (dynamic SQL)
     */
    protected static String buildUpdateSelective(TableMeta entity) {
        return "UPDATE " + entity.tableName() + "<set>" + entity.updateColumns().stream().map(
                col -> "<if test='" + col.notNullTest() + "'>" + col.column() + " = " + col.variables() + "," + "</if>")
                .collect(Collectors.joining("\n")) + "</set>" + buildWherePrimaryKey(entity, null);
    }

    /**
     * Build SELECT statement (query by primary key).
     *
     * @param entity Table metadata
     * @return SELECT SQL
     */
    protected static String buildSelectByPrimaryKey(TableMeta entity) {
        return "SELECT " + entity.baseColumnAsPropertyList() + " FROM " + entity.tableName()
                + buildWherePrimaryKey(entity, null);
    }

    /**
     * Build SELECT statement (query all).
     *
     * @param entity Table metadata
     * @return SELECT SQL
     */
    protected static String buildSelectAll(TableMeta entity) {
        return "SELECT " + entity.baseColumnAsPropertyList() + " FROM " + entity.tableName();
    }

    /**
     * Build DELETE statement (delete by primary key).
     *
     * @param entity Table metadata
     * @return DELETE SQL
     */
    protected static String buildDeleteByPrimaryKey(TableMeta entity) {
        return "DELETE FROM " + entity.tableName() + buildWherePrimaryKey(entity, null);
    }

    /**
     * Build WHERE clause (primary key condition).
     *
     * @param entity      Table metadata
     * @param paramPrefix Parameter prefix (optional)
     * @return WHERE clause
     */
    protected static String buildWherePrimaryKey(TableMeta entity, String paramPrefix) {
        StringBuilder sql = new StringBuilder();
        sql.append("\n<where>");

        entity.idColumns().forEach(pk -> {
            sql.append("\n  AND ").append(pk.column()).append(" = ");
            if (paramPrefix != null) {
                sql.append(pk.variables(paramPrefix));
            } else {
                sql.append(pk.variables());
            }
        });

        sql.append("\n</where>");
        return sql.toString();
    }

    /**
     * Build WHERE clause (entity object condition, only non-null fields).
     *
     * @param entity    Table metadata
     * @param paramName Parameter name
     * @return WHERE clause (dynamic SQL)
     */
    protected static String buildWhereSelective(TableMeta entity, String paramName) {
        StringBuilder sql = new StringBuilder();
        sql.append("\n<where>");

        entity.columns().forEach(col -> {
            String test = paramName != null ? paramName + "." + col.property() + " != null"
                    : col.property() + " != null";
            String variable = paramName != null ? col.variables(paramName) : col.variables();

            sql.append("\n  <if test='").append(test).append("'>");
            sql.append("AND ").append(col.column()).append(" = ").append(variable);
            sql.append("</if>");
        });

        sql.append("\n</where>");
        return sql.toString();
    }

    /**
     * Build COUNT statement.
     *
     * @param entity Table metadata
     * @return COUNT SQL
     */
    protected static String buildCount(TableMeta entity) {
        return "SELECT COUNT(*) FROM " + entity.tableName();
    }

    /**
     * Build COUNT statement (with condition).
     *
     * @param entity    Table metadata
     * @param paramName Parameter name
     * @return COUNT SQL (dynamic SQL)
     */
    protected static String buildCountSelective(TableMeta entity, String paramName) {
        return "SELECT COUNT(*) FROM " + entity.tableName() + buildWhereSelective(entity, paramName);
    }

    /**
     * Build EXISTS statement (check if primary key exists).
     *
     * @param entity Table metadata
     * @return EXISTS SQL
     */
    protected static String buildExistsByPrimaryKey(TableMeta entity) {
        return "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM " + entity.tableName()
                + buildWherePrimaryKey(entity, null);
    }

    /**
     * Build single-record UPSERT SQL function that accepts Dialect parameter.
     *
     * <p>
     * This method returns a function that generates database-specific single-record UPSERT SQL using the dialect's
     * UPSERT template. The SQL is generated dynamically at runtime based on the current datasource's dialect.
     * </p>
     *
     * <p>
     * <b>Implementation Details:</b>
     * </p>
     * <ul>
     * <li>Gets UPSERT template via {@link Dialect#getUpsertTemplate()}</li>
     * <li>Builds UPDATE clause inline based on dialect type:
     * <ul>
     * <li>MySQL/MariaDB: {@code col = VALUES(col)}</li>
     * <li>PostgreSQL/H2/CirroData: {@code col = EXCLUDED.col}</li>
     * <li>SQLite: UPDATE clause is ignored (INSERT OR REPLACE handles conflicts)</li>
     * </ul>
     * </li>
     * <li>Applies template with parameters: tableName, insertColumns, values, keyColumns, updateColumns</li>
     * </ul>
     *
     * <p>
     * <b>Generated SQL Example (PostgreSQL):</b>
     * </p>
     * 
     * <pre>
     * INSERT INTO users (id, name, email) VALUES (#{id}, #{name}, #{email})
     * ON CONFLICT (id) DO UPDATE SET
     *   name = EXCLUDED.name,
     *   email = EXCLUDED.email
     * </pre>
     *
     * @param entity Table metadata containing column and key information
     * @return Function that accepts Dialect and returns complete UPSERT SQL for single record
     */
    protected static Function<Dialect, String> buildInsertUp(TableMeta entity) {
        return dialect -> {
            String template = dialect.getUpsertTemplate();
            if (template == null) {
                throw new UnsupportedOperationException(dialect.getDatabase() + " does not support UPSERT operations");
            }

            String keyColumns = entity.idColumns().stream().map(ColumnMeta::column).collect(Collectors.joining(", "));
            String insertColumns = entity.insertColumns().stream().map(ColumnMeta::column)
                    .collect(Collectors.joining(", "));
            String values = entity.insertColumns().stream().map(ColumnMeta::variables)
                    .collect(Collectors.joining(", "));

            // Build UPDATE clause inline based on dialect type
            String updateColumnNames = entity.updateColumns().stream().map(ColumnMeta::column)
                    .collect(Collectors.joining(", "));
            String updateColumns;
            String database = dialect.getDatabase();
            if (database.contains("MySQL") || database.contains("MariaDB")) {
                // MySQL: col1 = VALUES(col1), col2 = VALUES(col2)
                updateColumns = entity.updateColumns().stream()
                        .map(col -> col.column() + " = VALUES(" + col.column() + ")").collect(Collectors.joining(", "));
            } else {
                // PostgreSQL, H2, CirroData: col1 = EXCLUDED.col1, col2 = EXCLUDED.col2
                updateColumns = entity.updateColumns().stream().map(col -> col.column() + " = EXCLUDED." + col.column())
                        .collect(Collectors.joining(", "));
            }

            // SQLite template doesn't use UPDATE clause (parameter will be ignored)
            return String.format(
                    template,
                    entity.tableName(),
                    insertColumns,
                    values,
                    keyColumns,
                    updateColumns,
                    insertColumns,
                    values);
        };
    }

    /**
     * Build single-record UPSERT SQL function (insert only non-null fields).
     *
     * <p>
     * This method returns a function that generates database-specific single-record UPSERT SQL with dynamic SQL for
     * only non-null fields. The SQL is generated dynamically at runtime based on the current datasource's dialect.
     * </p>
     *
     * <p>
     * <b>Implementation Details:</b>
     * </p>
     * <ul>
     * <li>Checks {@link Dialect#supportsUpsert()} to verify UPSERT support</li>
     * <li>Detects if dialect uses INSERT OR REPLACE (SQLite) by checking template</li>
     * <li>Builds dynamic column list and values list with {@code <if>} tags</li>
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
     *   &lt;if test="name != null"&gt;name,&lt;/if&gt;
     *   &lt;if test="email != null"&gt;email,&lt;/if&gt;
     * &lt;/trim&gt;
     * VALUES
     * &lt;trim prefix="(" suffix=")" suffixOverrides=","&gt;
     *   &lt;if test="name != null"&gt;#{name},&lt;/if&gt;
     *   &lt;if test="email != null"&gt;#{email},&lt;/if&gt;
     * &lt;/trim&gt;
     * ON CONFLICT (id) DO UPDATE SET
     *   &lt;if test="name != null"&gt;name = EXCLUDED.name,&lt;/if&gt;
     *   &lt;if test="email != null"&gt;email = EXCLUDED.email,&lt;/if&gt;
     * </pre>
     *
     * @param entity Table metadata containing column and key information
     * @return Function that accepts Dialect and returns complete dynamic UPSERT SQL
     */
    protected static Function<Dialect, String> buildInsertUpSelective(TableMeta entity) {
        return dialect -> {
            if (!dialect.supportsUpsert()) {
                throw new UnsupportedOperationException(dialect.getDatabase() + " does not support UPSERT operations");
            }

            // Check if using INSERT OR REPLACE by examining the template
            String upsertTemplate = dialect.getUpsertTemplate();
            boolean useInsertOrReplace = upsertTemplate != null && upsertTemplate.contains("INSERT OR REPLACE");

            // Build dynamic column list and values list
            StringBuilder columnList = new StringBuilder();
            columnList.append("<trim prefix='(' suffix=')' suffixOverrides=','>\n");
            for (ColumnMeta col : entity.insertColumns()) {
                columnList.append("  <if test='").append(col.notNullTest()).append("'>").append(col.column())
                        .append(",</if>\n");
            }
            columnList.append("</trim>");

            StringBuilder valuesList = new StringBuilder();
            valuesList.append("<trim prefix='VALUES (' suffix=')' suffixOverrides=','>\n");
            for (ColumnMeta col : entity.insertColumns()) {
                valuesList.append("  <if test='").append(col.notNullTest()).append("'>").append(col.variables())
                        .append(",</if>\n");
            }
            valuesList.append("</trim>");

            StringBuilder sql = new StringBuilder();
            if (useInsertOrReplace) {
                // SQLite: Use INSERT OR REPLACE template
                sql.append(
                        String.format(
                                upsertTemplate,
                                entity.tableName(),
                                columnList.toString(),
                                valuesList.toString()));
            } else {
                // MySQL/PostgreSQL/H2/others: Use buildUpsertSql()
                String keyColumns = entity.idColumns().stream().map(ColumnMeta::column)
                        .collect(Collectors.joining(", "));
                String upsertSql = dialect.buildUpsertSql(
                        entity.tableName(),
                        columnList.toString(),
                        valuesList.toString(),
                        keyColumns,
                        entity.updateColumns(),
                        "");
                sql.append(upsertSql);
            }

            return sql.toString();
        };
    }

    /**
     * Builds dynamic column list for MyBatis trim tag (for batch operations).
     *
     * <p>
     * Generates dynamic SQL that includes only non-null columns from the first item in a collection.
     * </p>
     *
     * @param entity     Table metadata
     * @param collection Collection parameter name (e.g., "list", "entityList")
     * @param prefix     Column prefix index (e.g., "list[0]", "list[1]")
     * @return MyBatis dynamic SQL fragment
     *
     *         <p>
     *         Example output:
     *         </p>
     * 
     *         <pre>
     * &lt;trim prefix="(" suffix=")" suffixOverrides=","&gt;
     *   &lt;if test="list[0].name != null"&gt;name,&lt;/if&gt;
     *   &lt;if test="list[0].age != null"&gt;age,&lt;/if&gt;
     * &lt;/trim&gt;
     *         </pre>
     */
    protected static String buildDynamicColumnList(TableMeta entity, String collection, String prefix) {
        StringBuilder sql = new StringBuilder();
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        for (ColumnMeta col : entity.insertColumns()) {
            sql.append("  <if test=\"").append(collection).append("[").append(prefix).append("].")
                    .append(col.property()).append(" != null\">").append(col.column()).append(",</if>\n");
        }
        sql.append("</trim>");
        return sql.toString();
    }

    /**
     * Builds dynamic values list for MyBatis foreach tag (for batch operations).
     *
     * <p>
     * Generates dynamic SQL that iterates over a collection and includes only non-null values.
     * </p>
     *
     * @param entity Table metadata
     * @param item   Iteration variable name (e.g., "item", "entity")
     * @return MyBatis dynamic SQL fragment
     *
     *         <p>
     *         Example output:
     *         </p>
     * 
     *         <pre>
     * &lt;foreach collection="list" item="item" separator=","&gt;
     *   &lt;trim prefix="(" suffix=")" suffixOverrides=","&gt;
     *     &lt;if test="item.name != null"&gt;#{item.name},&lt;/if&gt;
     *     &lt;if test="item.age != null"&gt;#{item.age},&lt;/if&gt;
     *   &lt;/trim&gt;
     * &lt;/foreach&gt;
     *         </pre>
     */
    protected static String buildDynamicValuesList(TableMeta entity, String item) {
        StringBuilder sql = new StringBuilder();
        sql.append("<foreach collection=\"list\" item=\"").append(item).append("\" separator=\",\">\n");
        sql.append("  <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        for (ColumnMeta col : entity.insertColumns()) {
            sql.append("    <if test=\"").append(item).append(".").append(col.property()).append(" != null\">#{")
                    .append(item).append(".").append(col.property()).append("},</if>\n");
        }
        sql.append("  </trim>\n");
        sql.append("</foreach>");
        return sql.toString();
    }

    /**
     * Builds dynamic column list without prefix (for single-record selective operations).
     *
     * <p>
     * Generates dynamic SQL that includes only non-null columns for single-record operations.
     * </p>
     *
     * @param entity Table metadata
     * @param param  Parameter name (e.g., "entity", null for direct access)
     * @return MyBatis dynamic SQL fragment
     *
     *         <p>
     *         Example output (with param="entity"):
     *         </p>
     * 
     *         <pre>
     * &lt;trim prefix='(' suffix=')' suffixOverrides=','&gt;
     *   &lt;if test='entity.name != null'&gt;name,&lt;/if&gt;
     *   &lt;if test='entity.age != null'&gt;age,&lt;/if&gt;
     * &lt;/trim&gt;
     *         </pre>
     */
    protected static String buildDynamicColumnListWithoutPrefix(TableMeta entity, String param) {
        StringBuilder sql = new StringBuilder();
        sql.append("<trim prefix='(' suffix=')' suffixOverrides=','>\n");
        for (ColumnMeta col : entity.insertColumns()) {
            String test = param != null ? param + "." + col.property() + " != null" : col.property() + " != null";
            sql.append("  <if test='").append(test).append("'>").append(col.column()).append(",</if>\n");
        }
        sql.append("</trim>");
        return sql.toString();
    }

}
