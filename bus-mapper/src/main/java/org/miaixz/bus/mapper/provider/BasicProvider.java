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
 * @since Java 21+
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
     * @param context    Provider context
     * @param sqlBuilder SQL building function
     * @return Cache key
     */
    protected static String cacheSql(ProviderContext context, Function<TableMeta, String> sqlBuilder) {
        return SqlScript.caching(context, entity -> sqlBuilder.apply(entity));
    }

    /**
     * Template method for caching dynamic SQL scripts (that depend on database dialect).
     *
     * <p>
     * This method is used for SQL that needs to be generated dynamically based on the current datasource's dialect. The
     * SQL is not generated at cache time, but rather at execution time when the dialect is known.
     * </p>
     *
     * @param context    Provider context
     * @param sqlBuilder SQL building function that accepts Dialect parameter
     * @return Cache key
     */
    protected static String cacheSqlDynamic(
            ProviderContext context,
            Function<TableMeta, Function<Dialect, String>> sqlBuilder) {
        return SqlScript.cachingDynamic(context, (entity, dialect) -> sqlBuilder.apply(entity).apply(dialect));
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
     * Builds a dialect-aware single-row UPSERT SQL function for non-selective inserts.
     *
     * @param entity the table metadata
     * @return a function that renders SQL for the active dialect
     */
    protected static Function<Dialect, String> buildInsertUp(TableMeta entity) {
        return dialect -> switch (dialect.getUpsertType()) {
            case INSERT_ON_DUPLICATE -> buildInsertOnDuplicate(entity, false);
            case INSERT_ON_CONFLICT -> buildInsertOnConflict(entity, false);
            case INSERT_OR_REPLACE -> buildInsertOrReplace(entity, false);
            case UPDATE_OR_INSERT -> buildUpdateOrInsert(entity, false);
            case MERGE_USING_VALUES -> buildMergeUsingValues(entity, false);
            case MERGE_USING_DUAL -> buildMergeUsingDual(entity, false);
            case NONE -> throw unsupportedUpsert(dialect);
        };
    }

    /**
     * Builds a dialect-aware single-row UPSERT SQL function.
     *
     * @param entity the table metadata
     * @return a function that renders SQL for the active dialect
     */
    protected static Function<Dialect, String> buildInsertUpSelective(TableMeta entity) {
        return dialect -> switch (dialect.getUpsertType()) {
            case INSERT_ON_DUPLICATE -> buildInsertOnDuplicate(entity, true);
            case INSERT_ON_CONFLICT -> buildInsertOnConflict(entity, true);
            case INSERT_OR_REPLACE -> buildInsertOrReplace(entity, true);
            case UPDATE_OR_INSERT -> buildUpdateOrInsert(entity, true);
            case MERGE_USING_VALUES -> buildMergeUsingValues(entity, true);
            case MERGE_USING_DUAL -> buildMergeUsingDual(entity, true);
            case NONE -> throw unsupportedUpsert(dialect);
        };
    }

    /**
     * Builds a dynamic insert column list for batch selective SQL.
     *
     * @param entity     the table metadata
     * @param collection the batch collection variable name
     * @param prefix     the sample element index used to infer included columns
     * @return a dynamic column list wrapped in a {@code <trim>} tag
     */
    protected static String buildDynamicColumnList(TableMeta entity, String collection, String prefix) {
        StringBuilder sql = new StringBuilder();
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        for (ColumnMeta col : entity.insertColumns()) {
            String itemRef = collection + "[" + prefix + "]";
            sql.append("  <if test=\"").append(requiredUpsertColumnCondition(itemRef, col)).append("\">")
                    .append(col.column()).append(",</if>\n");
        }
        sql.append("</trim>");
        return sql.toString();
    }

    /**
     * Builds a dynamic insert column list for single-row selective SQL.
     *
     * @param entity the table metadata
     * @return a dynamic column list wrapped in a {@code <trim>} tag
     */
    protected static String buildDynamicColumnListSingle(TableMeta entity) {
        StringBuilder sql = new StringBuilder();
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        for (ColumnMeta col : entity.insertColumns()) {
            sql.append("  <if test=\"").append(requiredUpsertColumnCondition("", col)).append("\">")
                    .append(col.column()).append(",</if>\n");
        }
        sql.append("</trim>");
        return sql.toString();
    }

    /**
     * Builds a dynamic value tuple for one batch item.
     *
     * @param entity the table metadata
     * @param item   the batch item variable name
     * @return a dynamic value tuple wrapped in a {@code <trim>} tag
     */
    protected static String buildDynamicValuesList(TableMeta entity, String item) {
        StringBuilder sql = new StringBuilder();
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        for (ColumnMeta col : entity.insertColumns()) {
            sql.append("  <if test=\"").append(requiredUpsertColumnCondition(item, col)).append("\">#{").append(item)
                    .append(".").append(col.property()).append("},</if>\n");
        }
        sql.append("</trim>");
        return sql.toString();
    }

    /**
     * Builds a dynamic value tuple for a single selective UPSERT statement.
     *
     * @param entity the table metadata
     * @return a dynamic value tuple wrapped in a {@code <trim>} tag
     */
    protected static String buildDynamicValuesListSingle(TableMeta entity) {
        StringBuilder sql = new StringBuilder();
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        for (ColumnMeta col : entity.insertColumns()) {
            sql.append("  <if test=\"").append(requiredUpsertColumnCondition("", col)).append("\">")
                    .append(col.variables()).append(",</if>\n");
        }
        sql.append("</trim>");
        return sql.toString();
    }

    /**
     * Determines whether the specified UPSERT type supports a single native batch statement.
     *
     * @param upsertType the UPSERT type
     * @return {@code true} when the dialect supports native batch UPSERT SQL
     */
    protected static boolean supportsNativeBatchUpsert(Dialect.Type upsertType) {
        return upsertType == Dialect.Type.INSERT_ON_DUPLICATE || upsertType == Dialect.Type.INSERT_ON_CONFLICT
                || upsertType == Dialect.Type.INSERT_OR_REPLACE;
    }

    /**
     * Creates a consistent exception for unsupported UPSERT operations.
     *
     * @param dialect the current dialect
     * @return the exception to throw
     */
    protected static RuntimeException unsupportedUpsert(Dialect dialect) {
        return new UnsupportedOperationException(dialect.getDatabase() + " does not support UPSERT operations");
    }

    /**
     * Returns the comma-separated list of key columns used for conflict detection.
     *
     * @param entity the table metadata
     * @return the key column list
     */
    protected static String keyColumnList(TableMeta entity) {
        return entity.idColumns().stream().map(ColumnMeta::column).collect(Collectors.joining(", "));
    }

    /**
     * Returns the comma-separated list of insertable columns.
     *
     * @param entity the table metadata
     * @return the insert column list
     */
    protected static String insertColumnList(TableMeta entity) {
        return entity.insertColumns().stream().map(ColumnMeta::column).collect(Collectors.joining(", "));
    }

    /**
     * Builds a MySQL-style UPSERT statement.
     *
     * @param entity    the table metadata
     * @param selective whether to include only non-null fields
     * @return the generated SQL
     */
    protected static String buildInsertOnDuplicate(TableMeta entity, boolean selective) {
        StringBuilder sql = new StringBuilder();
        if (!selective) {
            sql.append("INSERT INTO ").append(entity.tableName()).append(" (").append(insertColumnList(entity))
                    .append(") VALUES (").append(insertValues(entity, null)).append(")\n");
            sql.append("ON DUPLICATE KEY UPDATE ").append(buildUpdateAssignments(entity, "VALUES", null, false));
            return sql.toString();
        }
        sql.append("INSERT INTO ").append(entity.tableName()).append("\n").append(buildDynamicColumnListSingle(entity))
                .append("\nVALUES\n").append(buildDynamicValuesListSingle(entity)).append("\n");
        sql.append("ON DUPLICATE KEY UPDATE\n").append(buildUpdateAssignments(entity, "VALUES", "", true));
        return sql.toString();
    }

    /**
     * Builds a PostgreSQL-style UPSERT statement.
     *
     * @param entity    the table metadata
     * @param selective whether to include only non-null fields
     * @return the generated SQL
     */
    protected static String buildInsertOnConflict(TableMeta entity, boolean selective) {
        StringBuilder sql = new StringBuilder();
        if (!selective) {
            sql.append("INSERT INTO ").append(entity.tableName()).append(" (").append(insertColumnList(entity))
                    .append(") VALUES (").append(insertValues(entity, null)).append(")\n");
            sql.append("ON CONFLICT (").append(keyColumnList(entity)).append(") DO UPDATE SET ")
                    .append(buildUpdateAssignments(entity, "EXCLUDED", null, false));
            return sql.toString();
        }
        sql.append("INSERT INTO ").append(entity.tableName()).append("\n").append(buildDynamicColumnListSingle(entity))
                .append("\nVALUES\n").append(buildDynamicValuesListSingle(entity)).append("\n");
        sql.append("ON CONFLICT (").append(keyColumnList(entity)).append(") DO UPDATE SET\n")
                .append(buildUpdateAssignments(entity, "EXCLUDED", "", true));
        return sql.toString();
    }

    /**
     * Builds a SQLite-style {@code INSERT OR REPLACE} statement.
     *
     * @param entity    the table metadata
     * @param selective whether to include only non-null fields
     * @return the generated SQL
     */
    protected static String buildInsertOrReplace(TableMeta entity, boolean selective) {
        StringBuilder sql = new StringBuilder();
        if (!selective) {
            sql.append("INSERT OR REPLACE INTO ").append(entity.tableName()).append(" (")
                    .append(insertColumnList(entity)).append(") VALUES (").append(insertValues(entity, null))
                    .append(")");
            return sql.toString();
        }
        sql.append("INSERT OR REPLACE INTO ").append(entity.tableName()).append("\n")
                .append(buildDynamicColumnListSingle(entity)).append("\nVALUES\n")
                .append(buildDynamicValuesListSingle(entity));
        return sql.toString();
    }

    /**
     * Builds a Firebird-style {@code UPDATE OR INSERT} statement.
     *
     * @param entity    the table metadata
     * @param selective whether to include only non-null fields
     * @return the generated SQL
     */
    protected static String buildUpdateOrInsert(TableMeta entity, boolean selective) {
        StringBuilder sql = new StringBuilder();
        if (!selective) {
            sql.append("UPDATE OR INSERT INTO ").append(entity.tableName()).append(" (")
                    .append(insertColumnList(entity)).append(") VALUES (").append(insertValues(entity, null))
                    .append(") MATCHING (").append(keyColumnList(entity)).append(")");
            return sql.toString();
        }
        sql.append("UPDATE OR INSERT INTO ").append(entity.tableName()).append("\n")
                .append(buildDynamicColumnListSingle(entity)).append("\nVALUES\n")
                .append(buildDynamicValuesListSingle(entity)).append("\nMATCHING (").append(keyColumnList(entity))
                .append(")");
        return sql.toString();
    }

    /**
     * Builds a {@code MERGE} statement whose source is expressed as a {@code VALUES} clause.
     *
     * @param entity    the table metadata
     * @param selective whether to include only non-null fields
     * @return the generated SQL
     */
    protected static String buildMergeUsingValues(TableMeta entity, boolean selective) {
        String targetAlias = "target";
        String sourceAlias = "source";
        StringBuilder sql = new StringBuilder();
        sql.append("MERGE INTO ").append(entity.tableName()).append(" ").append(targetAlias).append("\n");
        if (!selective) {
            sql.append("USING (VALUES (").append(insertValues(entity, null)).append(")) ").append(sourceAlias)
                    .append(" (").append(insertColumnList(entity)).append(")\n");
            sql.append("ON (").append(buildMergeOnClause(entity, targetAlias, sourceAlias)).append(")\n");
            sql.append("WHEN MATCHED THEN UPDATE SET ")
                    .append(buildUpdateAssignments(entity, "SOURCE", sourceAlias, false)).append("\n");
            sql.append("WHEN NOT MATCHED THEN INSERT (").append(insertColumnList(entity)).append(")\n");
            sql.append("VALUES (").append(buildSourceReferenceList(entity, sourceAlias, false)).append(")");
            return sql.toString();
        }
        String columns = buildDynamicColumnListSingle(entity);
        sql.append("USING (VALUES\n").append(buildDynamicValuesListSingle(entity)).append(") ").append(sourceAlias)
                .append(" ").append(columns).append("\n");
        sql.append("ON (").append(buildMergeOnClause(entity, targetAlias, sourceAlias)).append(")\n");
        sql.append("WHEN MATCHED THEN UPDATE SET\n").append(buildUpdateAssignments(entity, "SOURCE", "", true))
                .append("\n");
        sql.append("WHEN NOT MATCHED THEN INSERT\n").append(columns).append("\nVALUES\n")
                .append(buildSourceReferenceList(entity, sourceAlias, true));
        return sql.toString();
    }

    /**
     * Builds a {@code MERGE} statement whose source is expressed as a {@code SELECT ... FROM dual} clause.
     *
     * @param entity    the table metadata
     * @param selective whether to include only non-null fields
     * @return the generated SQL
     */
    protected static String buildMergeUsingDual(TableMeta entity, boolean selective) {
        String targetAlias = "target";
        String sourceAlias = "source";
        StringBuilder sql = new StringBuilder();
        sql.append("MERGE INTO ").append(entity.tableName()).append(" ").append(targetAlias).append("\n");
        if (!selective) {
            sql.append("USING (\n  SELECT ")
                    .append(
                            entity.insertColumns().stream().map(col -> col.variables() + " AS " + col.column())
                                    .collect(Collectors.joining(", ")))
                    .append(" FROM dual\n) ").append(sourceAlias).append("\n");
            sql.append("ON (").append(buildMergeOnClause(entity, targetAlias, sourceAlias)).append(")\n");
            sql.append("WHEN MATCHED THEN UPDATE SET ")
                    .append(buildUpdateAssignments(entity, "SOURCE", sourceAlias, false)).append("\n");
            sql.append("WHEN NOT MATCHED THEN INSERT (").append(insertColumnList(entity)).append(")\n");
            sql.append("VALUES (").append(buildSourceReferenceList(entity, sourceAlias, false)).append(")");
            return sql.toString();
        }
        String columns = buildDynamicColumnListSingle(entity);
        sql.append("USING (\nSELECT\n").append(buildSelectSourceList(entity)).append("\nFROM dual\n) ")
                .append(sourceAlias).append("\n");
        sql.append("ON (").append(buildMergeOnClause(entity, targetAlias, sourceAlias)).append(")\n");
        sql.append("WHEN MATCHED THEN UPDATE SET\n").append(buildUpdateAssignments(entity, "SOURCE", "", true))
                .append("\n");
        sql.append("WHEN NOT MATCHED THEN INSERT\n").append(columns).append("\nVALUES\n")
                .append(buildSourceReferenceList(entity, sourceAlias, true));
        return sql.toString();
    }

    /**
     * Builds the dynamic inclusion condition for an UPSERT column.
     *
     * <p>
     * Key columns are always forced into the SQL to keep conflict detection stable.
     * </p>
     *
     * @param prefix the property prefix, or an empty string for root parameters
     * @param col    the column metadata
     * @return the OGNL expression used in dynamic SQL
     */
    private static String requiredUpsertColumnCondition(String prefix, ColumnMeta col) {
        String property = prefix.isEmpty() ? col.property() : prefix + "." + col.property();
        return col.id() ? "true" : property + " != null";
    }

    /**
     * Builds a static placeholder list for insert values.
     *
     * @param entity the table metadata
     * @param prefix the parameter prefix, or {@code null} for root parameters
     * @return the comma-separated placeholder list
     */
    private static String insertValues(TableMeta entity, String prefix) {
        return entity.insertColumns().stream().map(col -> prefix == null ? col.variables() : col.variables(prefix))
                .collect(Collectors.joining(", "));
    }

    /**
     * Builds the update assignment segment used by UPSERT statements.
     *
     * @param entity      the table metadata
     * @param mode        the source reference mode
     * @param sourceAlias the source alias used by {@code MERGE} statements
     * @param selective   whether to emit dynamic assignments only for non-null properties
     * @return the generated assignment segment
     */
    private static String buildUpdateAssignments(TableMeta entity, String mode, String sourceAlias, boolean selective) {
        if (!selective) {
            return entity.updateColumns().stream().map(col -> col.column() + " = " + switch (mode) {
                case "VALUES" -> "VALUES(" + col.column() + ")";
                case "EXCLUDED" -> "EXCLUDED." + col.column();
                case "SOURCE" -> sourceAlias + "." + col.column();
                default -> col.variables();
            }).collect(Collectors.joining(", "));
        }
        StringBuilder sql = new StringBuilder();
        sql.append("<trim suffixOverrides=\",\">\n");
        for (ColumnMeta col : entity.updateColumns()) {
            sql.append("  <if test=\"").append(requiredUpsertColumnCondition("", col)).append("\">")
                    .append(col.column()).append(" = ");
            if ("VALUES".equals(mode)) {
                sql.append("VALUES(").append(col.column()).append(")");
            } else if ("EXCLUDED".equals(mode)) {
                sql.append("EXCLUDED.").append(col.column());
            } else {
                sql.append(sourceAlias).append(".").append(col.column());
            }
            sql.append(",</if>\n");
        }
        sql.append("</trim>");
        return sql.toString();
    }

    /**
     * Builds the {@code ON} predicate for {@code MERGE} statements.
     *
     * @param entity      the table metadata
     * @param targetAlias the target table alias
     * @param sourceAlias the source alias
     * @return the merge predicate
     */
    private static String buildMergeOnClause(TableMeta entity, String targetAlias, String sourceAlias) {
        return entity.idColumns().stream()
                .map(col -> targetAlias + "." + col.column() + " = " + sourceAlias + "." + col.column())
                .collect(Collectors.joining(" AND "));
    }

    /**
     * Builds the insert value reference list for {@code MERGE} statements.
     *
     * @param entity      the table metadata
     * @param sourceAlias the source alias
     * @param selective   whether to emit dynamic references only for included columns
     * @return the generated value list
     */
    private static String buildSourceReferenceList(TableMeta entity, String sourceAlias, boolean selective) {
        if (!selective) {
            return entity.insertColumns().stream().map(col -> sourceAlias + "." + col.column())
                    .collect(Collectors.joining(", "));
        }
        StringBuilder sql = new StringBuilder();
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">\n");
        for (ColumnMeta col : entity.insertColumns()) {
            sql.append("  <if test=\"").append(requiredUpsertColumnCondition("", col)).append("\">").append(sourceAlias)
                    .append(".").append(col.column()).append(",</if>\n");
        }
        sql.append("</trim>");
        return sql.toString();
    }

    /**
     * Builds the dynamic {@code SELECT} source list used by {@code MERGE ... USING (... FROM dual)} statements.
     *
     * @param entity the table metadata
     * @return the generated source projection list
     */
    private static String buildSelectSourceList(TableMeta entity) {
        StringBuilder sql = new StringBuilder();
        sql.append("<trim suffixOverrides=\",\">\n");
        for (ColumnMeta col : entity.insertColumns()) {
            sql.append("  <if test=\"").append(requiredUpsertColumnCondition("", col)).append("\">")
                    .append(col.variables()).append(" AS ").append(col.column()).append(",</if>\n");
        }
        sql.append("</trim>");
        return sql.toString();
    }

}
