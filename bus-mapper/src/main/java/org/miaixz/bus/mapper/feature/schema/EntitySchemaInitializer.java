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
package org.miaixz.bus.mapper.feature.schema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.Charter.Risk;
import org.miaixz.bus.mapper.Charter.Schema;
import org.miaixz.bus.mapper.behavior.SchemaBehavior;
import org.miaixz.bus.mapper.dialect.Dialect;
import org.miaixz.bus.mapper.dialect.DialectRegistry;
import org.miaixz.bus.mapper.parsing.MapperFactory;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Entity schema initializer.
 *
 * <p>
 * This initializer is designed for application startup only. It performs metadata reads and optional DDL execution and
 * must not be called from request hot paths.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class EntitySchemaInitializer {

    /**
     * Constructs a new EntitySchemaInitializer instance.
     */
    public EntitySchemaInitializer() {
        // No initialization required.
    }

    /**
     * Initializes schema structures for entity classes.
     *
     * @param dataSource    the datasource used for metadata reads and DDL execution
     * @param entityClasses the entity classes to initialize
     * @param config        the schema configuration
     * @return the schema execution report
     * @throws SQLException when metadata reads or DDL execution fail
     * @throws IOException  when script output fails
     */
    public SchemaReport initialize(DataSource dataSource, Collection<Class<?>> entityClasses, SchemaConfig config)
            throws SQLException, IOException {
        SchemaReport report = new SchemaReport();
        if (config == null || !config.enabled() || config.mode() == Schema.NONE) {
            return report;
        }
        Dialect dialect = DialectRegistry.getDialect(dataSource);
        SchemaBehavior operations = dialect;
        List<String> scriptSqls = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            List<AutoCloseable> locks = new ArrayList<>();
            List<SchemaPlan> plans = new ArrayList<>();
            try {
                for (Class<?> entityClass : entityClasses) {
                    if (excluded(entityClass, config)) {
                        continue;
                    }
                    TableMeta table = MapperFactory.of(entityClass);
                    if (excluded(table, config)) {
                        continue;
                    }
                    locks.add(SchemaInitializationLock.acquire(dataSource, table));
                    locks.add(operations.acquireSchemaInitializationLock(connection, table));
                    TableSnapshot snapshot = operations.readTable(connection, table);
                    if (config.mode() == Schema.CREATE && snapshot.exists()) {
                        report.skippedSqls().add("Table already exists: " + table.tableName());
                        continue;
                    }
                    plans.add(new SchemaPlan(table, new SchemaDiffer(operations, config).diff(table, snapshot)));
                }
                executePlans(connection, dialect, operations, config, report, scriptSqls, plans);
            } finally {
                closeLocks(locks);
            }
        }
        writeScript(config, scriptSqls);
        return report;
    }

    /**
     * Executes schema plans in dependency-safe phases.
     *
     * @param connection the active database connection
     * @param dialect    the active dialect
     * @param operations the schema behavior
     * @param config     the schema configuration
     * @param report     the execution report
     * @param scriptSqls the generated script SQL collector
     * @param plans      the schema plans
     * @throws SQLException when DDL execution fails
     */
    private void executePlans(
            Connection connection,
            Dialect dialect,
            SchemaBehavior operations,
            SchemaConfig config,
            SchemaReport report,
            List<String> scriptSqls,
            List<SchemaPlan> plans) throws SQLException {
        for (SchemaPhase phase : SchemaPhase.values()) {
            for (SchemaPlan plan : plans) {
                for (SchemaDiff diff : plan.diffs()) {
                    if (phase != SchemaPhase.of(diff.type())) {
                        continue;
                    }
                    handleDiff(connection, dialect, operations, config, report, scriptSqls, diff);
                }
            }
        }
    }

    /**
     * Closes schema initialization locks in reverse acquisition order.
     *
     * @param locks the locks to close
     * @throws SQLException when lock release fails
     */
    private void closeLocks(List<AutoCloseable> locks) throws SQLException {
        for (int i = locks.size() - 1; i >= 0; i--) {
            closeLock(locks.get(i));
        }
    }

    /**
     * Closes a schema initialization lock.
     *
     * @param lock the lock to close
     * @throws SQLException when lock release fails
     */
    private void closeLock(AutoCloseable lock) throws SQLException {
        if (lock == null) {
            return;
        }
        try {
            lock.close();
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLException("Schema initialization lock release failed", e);
        }
    }

    /**
     * Handles a single schema difference.
     *
     * @param connection the active database connection
     * @param dialect    the active dialect
     * @param operations the schema behavior
     * @param config     the schema configuration
     * @param report     the execution report
     * @param scriptSqls the generated script SQL collector
     * @param diff       the schema difference
     * @throws SQLException when DDL execution fails
     */
    private void handleDiff(
            Connection connection,
            Dialect dialect,
            SchemaBehavior operations,
            SchemaConfig config,
            SchemaReport report,
            List<String> scriptSqls,
            SchemaDiff diff) throws SQLException {
        if (config.mode() == Schema.VALIDATE) {
            report.skippedSqls().add(diff.message());
            return;
        }
        if (!allowed(dialect, config, diff, connection)) {
            report.skippedSqls().add(diff.message());
            return;
        }
        String sql = sql(operations, diff);
        if (sql == null || sql.isBlank()) {
            report.skippedSqls().add(diff.message());
            return;
        }
        if (config.printSql()) {
            Logger.info(
                    false,
                    "Mapper",
                    "Schema SQL generated: type={}, riskLevel={}, table={}, sql={}",
                    diff.type(),
                    diff.riskLevel(),
                    diff.tableMeta().tableName(),
                    sql);
        }
        scriptSqls.add(sql);
        if (config.mode() == Schema.SCRIPT || config.dryRun()) {
            report.skippedSqls().add(sql);
            return;
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            report.executedSqls().add(sql);
        } catch (SQLException e) {
            report.skippedSqls().add(sql);
            report.failedDiffs().add(diff);
            if (config.failFast() || !config.continueOnError()) {
                throw e;
            }
        }
    }

    /**
     * Tests whether a schema difference is allowed by dialect support and configuration.
     *
     * @param dialect    the active dialect
     * @param config     the schema configuration
     * @param diff       the schema difference
     * @param connection the active database connection
     * @return {@code true} when the difference may be executed or scripted
     * @throws SQLException when row-count checks fail
     */
    private boolean allowed(Dialect dialect, SchemaConfig config, SchemaDiff diff, Connection connection)
            throws SQLException {
        if (!dialect.supports(diff.type())) {
            return false;
        }
        if (config.mode() == Schema.CREATE) {
            return switch (diff.type()) {
                case CREATE_TABLE -> config.allowCreateTable();
                case CREATE_PRIMARY_KEY -> config.allowCreatePrimaryKey();
                case CREATE_UNIQUE -> config.allowCreateUnique();
                case CREATE_INDEX -> config.allowCreateIndex();
                case CREATE_FOREIGN_KEY -> config.allowCreateForeignKey();
                default -> false;
            };
        }
        if (config.mode() != Schema.UPDATE && config.mode() != Schema.SCRIPT) {
            return false;
        }
        if (diff.type() == Behavior.ADD_COLUMN && Boolean.FALSE.equals(diff.columnMeta().ddlNullable())
                && !hasDefault(diff.columnMeta()) && hasRows(connection, diff.tableMeta())) {
            return false;
        }
        boolean allowed = switch (diff.type()) {
            case CREATE_TABLE -> config.allowCreateTable();
            case ADD_COLUMN -> config.allowAddColumn();
            case MODIFY_COLUMN_TYPE -> config.allowModifyType();
            case MODIFY_COLUMN_LENGTH -> diff.riskLevel() == Risk.DANGEROUS ? config.allowShrinkLength()
                    : config.allowExpandLength();
            case MODIFY_COLUMN_DECIMAL -> diff.riskLevel() == Risk.DANGEROUS ? config.allowShrinkDecimal()
                    : config.allowExpandDecimal();
            case MODIFY_COLUMN_NULLABLE -> config.allowModifyNullable();
            case RENAME_COLUMN -> config.allowRenameColumn();
            case DROP_COLUMN -> config.allowDropColumn();
            case CREATE_INDEX -> config.allowCreateIndex();
            case DROP_INDEX -> config.allowDropIndex();
            case CREATE_UNIQUE -> config.allowCreateUnique();
            case DROP_UNIQUE -> config.allowDropUnique();
            case CREATE_PRIMARY_KEY -> config.allowCreatePrimaryKey();
            case DROP_PRIMARY_KEY -> config.allowDropPrimaryKey();
            case CREATE_FOREIGN_KEY -> config.allowCreateForeignKey();
            case DROP_FOREIGN_KEY -> config.allowDropForeignKey();
            default -> false;
        };
        if (!allowed) {
            return false;
        }
        if (diff.riskLevel() == Risk.DANGEROUS) {
            return config.allowDangerous() && whitelisted(config, diff);
        }
        return true;
    }

    /**
     * Builds SQL for a schema difference.
     *
     * @param operations the schema behavior
     * @param diff       the schema difference
     * @return the generated SQL, or {@code null}
     */
    private String sql(SchemaBehavior operations, SchemaDiff diff) {
        return switch (diff.type()) {
            case CREATE_TABLE -> operations.createTable(diff.tableMeta());
            case ADD_COLUMN -> operations.addColumn(diff.tableMeta(), diff.columnMeta());
            case MODIFY_COLUMN_TYPE -> operations
                    .modifyColumnType(diff.tableMeta(), diff.columnMeta(), diff.actualColumn());
            case MODIFY_COLUMN_LENGTH -> operations
                    .modifyColumnLength(diff.tableMeta(), diff.columnMeta(), diff.actualColumn());
            case MODIFY_COLUMN_DECIMAL -> operations
                    .modifyColumnDecimal(diff.tableMeta(), diff.columnMeta(), diff.actualColumn());
            case MODIFY_COLUMN_NULLABLE -> operations
                    .modifyColumnNullable(diff.tableMeta(), diff.columnMeta(), diff.actualColumn());
            case RENAME_COLUMN -> operations.renameColumn(
                    diff.tableMeta(),
                    diff.actualColumn() == null ? null : diff.actualColumn().name(),
                    diff.columnMeta());
            case DROP_COLUMN -> operations.dropColumn(diff.tableMeta(), diff.actualColumn());
            case CREATE_INDEX -> operations.createIndex(diff.tableMeta(), diff.index());
            case DROP_INDEX -> operations.dropIndex(diff.tableMeta(), diff.index());
            case CREATE_UNIQUE -> operations.createUnique(diff.tableMeta(), diff.index());
            case DROP_UNIQUE -> operations.dropUnique(diff.tableMeta(), diff.index());
            case CREATE_PRIMARY_KEY -> operations.createPrimaryKey(diff.tableMeta(), diff.primaryKey());
            case DROP_PRIMARY_KEY -> operations.dropPrimaryKey(diff.tableMeta(), diff.primaryKey());
            case CREATE_FOREIGN_KEY -> operations.createForeignKey(diff.tableMeta(), diff.foreignKey());
            case DROP_FOREIGN_KEY -> operations.dropForeignKey(diff.tableMeta(), diff.foreignKey());
            default -> null;
        };
    }

    /**
     * Tests whether a table contains rows.
     *
     * @param connection the active database connection
     * @param table      the table metadata
     * @return {@code true} when the table contains rows
     * @throws SQLException when the row-count query fails
     */
    private boolean hasRows(Connection connection, TableMeta table) throws SQLException {
        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM " + table.tableName())) {
            return rs.next() && rs.getLong(1) > 0;
        }
    }

    /**
     * Tests whether a column definition contains a database default.
     *
     * @param column the column metadata
     * @return {@code true} when a default clause is present
     */
    private boolean hasDefault(org.miaixz.bus.mapper.parsing.ColumnMeta column) {
        return column.columnDefinition() != null
                && column.columnDefinition().toUpperCase(Locale.ROOT).contains(" DEFAULT ");
    }

    /**
     * Tests whether a dangerous difference is whitelisted.
     *
     * @param config the schema configuration
     * @param diff   the schema difference
     * @return {@code true} when the difference is whitelisted
     */
    private boolean whitelisted(SchemaConfig config, SchemaDiff diff) {
        String table = diff.tableMeta().tableName();
        String column = diff.columnMeta() != null ? diff.columnMeta().column()
                : diff.actualColumn() != null ? diff.actualColumn().name() : "";
        String type = diff.type().name();
        return config.dangerousWhitelist().contains(table) || config.dangerousWhitelist().contains(table + "." + column)
                || config.dangerousWhitelist().contains(type)
                || config.dangerousWhitelist().contains(type + ":" + table)
                || config.dangerousWhitelist().contains(type + ":" + table + "." + column);
    }

    /**
     * Tests whether an entity class is excluded by configuration.
     *
     * @param entityClass the entity class
     * @param config      the schema configuration
     * @return {@code true} when excluded
     */
    private boolean excluded(Class<?> entityClass, SchemaConfig config) {
        String name = entityClass.getName();
        if (!config.includeEntities().isEmpty() && !config.includeEntities().contains(name)) {
            return true;
        }
        return config.excludeEntities().contains(name);
    }

    /**
     * Tests whether a table is excluded by configuration.
     *
     * @param table  the table metadata
     * @param config the schema configuration
     * @return {@code true} when excluded
     */
    private boolean excluded(TableMeta table, SchemaConfig config) {
        String name = table.tableName().toLowerCase(Locale.ROOT);
        if (!config.includeTables().isEmpty()
                && config.includeTables().stream().noneMatch(tableName -> tableName.equalsIgnoreCase(name))) {
            return true;
        }
        return config.excludeTables().stream().anyMatch(tableName -> tableName.equalsIgnoreCase(name));
    }

    /**
     * Writes generated SQL statements to the configured script location.
     *
     * @param config the schema configuration
     * @param sqls   the generated SQL statements
     * @throws IOException when script output fails
     */
    private void writeScript(SchemaConfig config, List<String> sqls) throws IOException {
        if (config.scriptLocation() == null || config.scriptLocation().isBlank() || sqls.isEmpty()) {
            return;
        }
        Path path = Path.of(config.scriptLocation());
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.writeString(path, String.join(";\n", sqls) + ";\n");
    }

    /**
     * Schema execution phase.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private enum SchemaPhase {

        /**
         * Table creation phase.
         */
        TABLE,

        /**
         * Column and primary key phase.
         */
        STRUCTURE,

        /**
         * Unique constraint and index phase.
         */
        INDEX,

        /**
         * Foreign key phase.
         */
        FOREIGN_KEY;

        /**
         * Resolves the execution phase for a behavior.
         *
         * @param behavior the schema behavior
         * @return the execution phase
         */
        private static SchemaPhase of(Behavior behavior) {
            return switch (behavior) {
                case CREATE_TABLE -> TABLE;
                case CREATE_UNIQUE, DROP_UNIQUE, CREATE_INDEX, DROP_INDEX -> INDEX;
                case CREATE_FOREIGN_KEY, DROP_FOREIGN_KEY -> FOREIGN_KEY;
                default -> STRUCTURE;
            };
        }

    }

    /**
     * Table schema execution plan.
     *
     * @param table the table metadata
     * @param diffs the schema differences
     */
    private record SchemaPlan(TableMeta table, List<SchemaDiff> diffs) {

    }

}
