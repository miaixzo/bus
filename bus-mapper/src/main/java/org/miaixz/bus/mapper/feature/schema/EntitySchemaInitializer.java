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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.Charter.Risk;
import org.miaixz.bus.mapper.Charter.Schema;
import org.miaixz.bus.mapper.behavior.SchemaBehavior;
import org.miaixz.bus.mapper.dialect.Dialect;
import org.miaixz.bus.mapper.dialect.DialectRegistry;
import org.miaixz.bus.mapper.feature.affix.AffixRuleConfig;
import org.miaixz.bus.mapper.feature.schema.SchemaReport.Stage;
import org.miaixz.bus.mapper.feature.schema.SchemaReport.Status;
import org.miaixz.bus.mapper.parsing.*;

/**
 * Entity schema initializer.
 *
 * <p>
 * This initializer is designed for application startup only. It performs metadata reads and optional DDL execution and
 * must not be called from request hot paths.
 * </p>
 *
 * @author Kimi Liu
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
        return initialize(dataSource, entityClasses, config, null);
    }

    /**
     * Initializes schema structures for entity classes using optional physical table-name affix rules.
     * <p>
     * The affix rules are owned by the affix feature and are applied only to schema-local table metadata. Runtime
     * mapper metadata remains unchanged.
     *
     * @param dataSource      the datasource used for metadata reads and DDL execution
     * @param entityClasses   the entity classes to initialize
     * @param config          the schema configuration
     * @param affixRuleConfig physical table-name affix rules resolved from the affix feature
     * @return the schema execution report
     * @throws SQLException when metadata reads or DDL execution fail
     * @throws IOException  when script output fails
     */
    public SchemaReport initialize(
            DataSource dataSource,
            Collection<Class<?>> entityClasses,
            SchemaConfig config,
            AffixRuleConfig affixRuleConfig) throws SQLException, IOException {
        SchemaReport report = new SchemaReport();
        if (config == null || !config.enabled() || config.mode() == Schema.NONE) {
            return report;
        }
        List<String> scriptSqls = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            Dialect dialect = DialectRegistry.getDialect(connection);
            SchemaBehavior operations = dialect;
            List<AutoCloseable> locks = new ArrayList<>();
            List<SchemaPlan> plans = new ArrayList<>();
            Throwable initializationFailure = null;
            try {
                for (Class<?> entityClass : entityClasses) {
                    TableMeta table = null;
                    try {
                        if (excluded(entityClass, config)) {
                            report.diagnose(
                                    entityClass.getName(),
                                    Normal.EMPTY,
                                    Stage.READ,
                                    Status.EXCLUDED,
                                    "Entity excluded by configuration");
                            continue;
                        }
                        table = MapperFactory.of(entityClass);
                        if (excluded(table, config)) {
                            report.diagnose(
                                    table.tableName(),
                                    Normal.EMPTY,
                                    Stage.READ,
                                    Status.EXCLUDED,
                                    "Table excluded by configuration");
                            continue;
                        }
                        TableMeta source = table;
                        table = affixedTable(source, affixRuleConfig);
                        if (table == source) {
                            table = copyTable(source);
                        }
                        applyNamespace(connection, config, table);
                        table.columns(new ColumnOrderResolver().resolve(table));
                        locks.add(SchemaInitializationLock.acquire(dataSource, table));
                        locks.add(operations.acquireSchemaInitializationLock(connection, table));
                        TableSnapshot snapshot = operations.readTable(connection, table);
                        if (config.mode() == Schema.CREATE && snapshot.exists()) {
                            report.skippedSqls().add("Table already exists: " + table.tableName());
                            report.diagnose(
                                    table.tableName(),
                                    Normal.EMPTY,
                                    Stage.READ,
                                    Status.NOT_CHECKED,
                                    "CREATE mode leaves existing table unchecked");
                            continue;
                        }
                        SchemaDiffer differ = new SchemaDiffer(operations, config);
                        plans.add(new SchemaPlan(table, differ.diff(table, snapshot)));
                        report.unknowns().addAll(differ.unknowns());
                        for (String reason : differ.unknowns())
                            report.diagnose(table.tableName(), Normal.EMPTY, Stage.READ, Status.UNKNOWN, reason);
                    } catch (SQLException e) {
                        String context = "entity=" + (entityClass == null ? null : entityClass.getName()) + ", table="
                                + (table == null ? null : table.tableName());
                        Logger.error(false, "Mapper", e, "Schema entity initialization failed: {}", context);
                        throw new SQLException("Schema entity initialization failed: " + context, e.getSQLState(),
                                e.getErrorCode(), e);
                    } catch (RuntimeException e) {
                        String context = "entity=" + (entityClass == null ? null : entityClass.getName()) + ", table="
                                + (table == null ? null : table.tableName());
                        Logger.error(false, "Mapper", e, "Schema entity initialization failed: {}", context);
                        throw e;
                    }
                }
                executePlans(connection, dialect, operations, config, report, scriptSqls, plans);
                boolean applying = config.mode() != Schema.VALIDATE && config.mode() != Schema.SCRIPT
                        && !config.dryRun();
                if (applying) {
                    report.unknowns().clear();
                    for (SchemaPlan plan : plans) {
                        SchemaDiffer differ = new SchemaDiffer(operations, config);
                        List<SchemaDiff> remaining = differ
                                .diff(plan.table(), operations.readTable(connection, plan.table()));
                        report.unresolvedDiffs().addAll(remaining);
                        report.unknowns().addAll(differ.unknowns());
                        for (SchemaDiff diff : remaining)
                            report.diagnose(
                                    plan.table().tableName(),
                                    diff.type().name(),
                                    Stage.VERIFY,
                                    Status.UNRESOLVED,
                                    diff.message());
                        for (String reason : differ.unknowns())
                            report.diagnose(
                                    plan.table().tableName(),
                                    Normal.EMPTY,
                                    Stage.VERIFY,
                                    Status.UNKNOWN,
                                    reason);
                        if (remaining.isEmpty() && differ.unknowns().isEmpty())
                            report.diagnose(
                                    plan.table().tableName(),
                                    Normal.EMPTY,
                                    Stage.VERIFY,
                                    Status.VALIDATED,
                                    "Final structure matches entity metadata");
                    }
                } else {
                    for (SchemaPlan plan : plans)
                        report.unresolvedDiffs().addAll(plan.diffs());
                }
                report.verified(
                        !plans.isEmpty() && report.unresolvedDiffs().isEmpty() && report.unknowns().isEmpty()
                                && report.failedDiffs().isEmpty() && config.mode() != Schema.SCRIPT && !config.dryRun()
                                && report.diagnostics().stream().noneMatch(
                                        item -> item.status() == Status.NOT_CHECKED
                                                || item.status() == Status.EXCLUDED));
            } catch (SQLException | RuntimeException | Error failure) {
                initializationFailure = failure;
                throw failure;
            } finally {
                try {
                    closeLocks(locks);
                } catch (SQLException releaseFailure) {
                    if (initializationFailure != null)
                        initializationFailure.addSuppressed(releaseFailure);
                    else
                        throw releaseFailure;
                }
            }
        }
        writeScript(config, scriptSqls);
        return report;
    }

    /**
     * Applies explicit target namespace overrides to schema-local metadata.
     *
     * @param connection active JDBC connection
     * @param config     initialization scope
     * @param table      copied table metadata
     * @throws SQLException when metadata cannot be read or the requested namespace is unsupported
     */
    private void applyNamespace(Connection connection, SchemaConfig config, TableMeta table) throws SQLException {
        var metadata = connection.getMetaData();
        if (StringKit.isNotBlank(config.targetCatalog())) {
            if (!metadata.supportsCatalogsInTableDefinitions()) {
                throw new SQLException("The database does not accept a target catalog in table definitions");
            }
            table.catalog(quotedNamespace(metadata.getIdentifierQuoteString(), config.targetCatalog()));
        }
        if (StringKit.isNotBlank(config.targetSchema())) {
            if (!metadata.supportsSchemasInTableDefinitions()) {
                throw new SQLException("The database does not accept a target schema in table definitions");
            }
            table.schema(quotedNamespace(metadata.getIdentifierQuoteString(), config.targetSchema()));
        } else if (StringKit.isBlank(table.schema()) && metadata.supportsSchemasInTableDefinitions()) {
            table.schema(quotedNamespace(metadata.getIdentifierQuoteString(), connection.getSchema()));
        }
    }

    /**
     * Quotes one physical namespace component using the JDBC driver's identifier delimiter.
     *
     * @param quote the driver-provided delimiter
     * @param name  physical catalog or schema component
     * @return quoted physical component or null for an unspecified namespace
     * @throws SQLException when the namespace cannot be represented safely
     */
    private static String quotedNamespace(String quote, String name) throws SQLException {
        if (StringKit.isBlank(name)) {
            return null;
        }
        if (name.indexOf(0) >= 0 || StringKit.isBlank(quote) && !name.matches("[A-Za-z_][A-Za-z_0-9]*")) {
            throw new SQLException("Invalid physical namespace");
        }
        return StringKit.isBlank(quote) ? name : quote + name.replace(quote, quote + quote) + quote;
    }

    /**
     * Applies physical table-name affix rules to schema-local table metadata.
     *
     * @param source          cached runtime table metadata
     * @param affixRuleConfig resolved physical table-name affix rules
     * @return table metadata used by schema initialization
     */
    private TableMeta affixedTable(TableMeta source, AffixRuleConfig affixRuleConfig) {
        String prefix = affixRuleConfig == null || affixRuleConfig.getProvider() == null ? null
                : affixRuleConfig.getProvider().getPrefix();
        String suffix = affixRuleConfig == null || affixRuleConfig.getProvider() == null ? null
                : affixRuleConfig.getProvider().getSuffix();
        String tableName = source == null ? null : source.table();
        String simpleTableName = simpleTableName(tableName);
        boolean ignorePrefix = affixRuleConfig != null && ignored(simpleTableName, affixRuleConfig.getPrefixIgnore());
        boolean ignoreSuffix = affixRuleConfig != null && ignored(simpleTableName, affixRuleConfig.getSuffixIgnore());
        if (source == null || tableName == null || (prefix == null || prefix.isBlank() || ignorePrefix)
                && (suffix == null || suffix.isBlank() || ignoreSuffix)) {
            return source;
        }
        TableMeta copy = copyTable(source);
        copy.table(affixQualifiedTable(tableName, ignorePrefix ? null : prefix, ignoreSuffix ? null : suffix));
        PrimaryKeyMeta primaryKey = copy.primaryKey();
        if (primaryKey != null && (tableName + "_pk").equals(primaryKey.name())) {
            primaryKey.name(copy.table() + "_pk");
        }
        for (ForeignKeyMeta foreignKey : copy.foreignKeys()) {
            String referencedTable = foreignKey.referencedTable();
            if (referencedTable != null && !referencedTable.isBlank()) {
                String referencedSimple = simpleTableName(referencedTable);
                String referencedPrefix = ignored(referencedSimple, affixRuleConfig.getPrefixIgnore()) ? null : prefix;
                String referencedSuffix = ignored(referencedSimple, affixRuleConfig.getSuffixIgnore()) ? null : suffix;
                foreignKey.referencedTable(affixQualifiedTable(referencedTable, referencedPrefix, referencedSuffix));
            }
        }
        return copy;
    }

    /**
     * Copies table metadata before schema-only table name or column order changes.
     *
     * @param source cached table metadata
     * @return copied table metadata
     */
    private TableMeta copyTable(TableMeta source) {
        TableMeta copy = TableMeta.of(source.entityClass()).table(source.table()).catalog(source.catalog())
                .schema(source.schema()).comment(source.comment()).style(source.style())
                .columns(new ArrayList<>(source.columns())).indexes(copyIndexes(source.indexes()))
                .foreignKeys(copyForeignKeys(source.foreignKeys())).ready(source.ready()).excludeSuperClasses(
                        source.excludeSuperClasses() == null ? null : source.excludeSuperClasses().clone());
        PrimaryKeyMeta primaryKey = source.primaryKey();
        copy.primaryKey(primaryKey == null ? null : PrimaryKeyMeta.of(primaryKey.name(), primaryKey.columns()));
        return copy;
    }

    /**
     * Copies index metadata into a mutable list.
     *
     * @param indexes source index metadata
     * @return copied index metadata
     */
    private List<IndexMeta> copyIndexes(List<IndexMeta> indexes) {
        List<IndexMeta> copies = new ArrayList<>();
        if (indexes == null) {
            return copies;
        }
        for (IndexMeta index : indexes) {
            copies.add(
                    new IndexMeta().name(index.name()).unique(index.unique()).predicate(index.predicate())
                            .columns(index.columns() == null ? new ArrayList<>() : new ArrayList<>(index.columns())));
        }
        return copies;
    }

    /**
     * Copies foreign key metadata into a mutable list.
     *
     * @param foreignKeys source foreign key metadata
     * @return copied foreign key metadata
     */
    private List<ForeignKeyMeta> copyForeignKeys(List<ForeignKeyMeta> foreignKeys) {
        List<ForeignKeyMeta> copies = new ArrayList<>();
        if (foreignKeys == null) {
            return copies;
        }
        for (ForeignKeyMeta foreignKey : foreignKeys) {
            copies.add(
                    new ForeignKeyMeta().name(foreignKey.name()).referencedTable(foreignKey.referencedTable()).columns(
                            foreignKey.columns() == null ? new ArrayList<>() : new ArrayList<>(foreignKey.columns()))
                            .referencedColumns(
                                    foreignKey.referencedColumns() == null ? new ArrayList<>()
                                            : new ArrayList<>(foreignKey.referencedColumns()))
                            .onDelete(foreignKey.onDelete()).onUpdate(foreignKey.onUpdate()));
        }
        return copies;
    }

    /**
     * Checks whether a logical table occurs in one side's ignore list.
     *
     * @param tableName logical table name
     * @param ignore    ignored logical table names
     * @return {@code true} when the table should not be rewritten
     */
    private boolean ignored(String tableName, List<String> ignore) {
        if (tableName == null || ignore == null) {
            return false;
        }
        return ignore.stream().anyMatch(tableName::equalsIgnoreCase);
    }

    /**
     * Applies affixes to the simple table name while preserving an optional qualifier.
     *
     * @param tableName table name
     * @param prefix    table prefix
     * @param suffix    table suffix
     * @return affixed table name
     */
    private String affixQualifiedTable(String tableName, String prefix, String suffix) {
        int qualifierIndex = tableName.lastIndexOf(Symbol.C_DOT);
        String qualifier = qualifierIndex < 0 ? Normal.EMPTY : tableName.substring(0, qualifierIndex + 1);
        String simple = qualifierIndex < 0 ? tableName : tableName.substring(qualifierIndex + 1);
        String actualPrefix = prefix == null || prefix.isBlank() || simple.startsWith(prefix) ? Normal.EMPTY : prefix;
        String actualSuffix = suffix == null || suffix.isBlank() || simple.endsWith(suffix) ? Normal.EMPTY : suffix;
        return qualifier + actualPrefix + simple + actualSuffix;
    }

    /**
     * Returns the simple table name without qualifier.
     *
     * @param tableName table name
     * @return simple table name
     */
    private String simpleTableName(String tableName) {
        int qualifierIndex = tableName == null ? -1 : tableName.lastIndexOf(Symbol.C_DOT);
        return qualifierIndex < 0 ? tableName : tableName.substring(qualifierIndex + 1);
    }

    /**
     * Prepares all permitted SQL before executing statements in schema-phase order. Blocked operations are tracked by
     * dependency keys; script and dry-run modes collect the prepared statements without applying them.
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
        List<SchemaDiff> prepared = new ArrayList<>();
        Set<String> blocked = new HashSet<>();
        for (SchemaPhase phase : SchemaPhase.values()) {
            for (SchemaPlan plan : plans) {
                for (SchemaDiff diff : plan.diffs()) {
                    if (phase != SchemaPhase.of(diff.type()))
                        continue;
                    SchemaDiff item = prepareDiff(connection, dialect, operations, config, report, diff);
                    if (item != null)
                        prepared.add(item);
                    else if (diff.type() == Behavior.CREATE_TABLE)
                        blocked.add(diff.tableMeta().tableName());
                    else if (diff.type() == Behavior.ADD_COLUMN || diff.type() == Behavior.DROP_PRIMARY_KEY
                            || diff.type() == Behavior.CREATE_PRIMARY_KEY || diff.index() != null)
                        blocked.add(dependencyKey(diff));
                }
            }
        }
        for (SchemaDiff diff : prepared) {
            String table = diff.tableMeta().tableName();
            boolean dependent = blocked.contains(table) || blocked.contains(dependencyKey(diff));
            if (diff.foreignKey() != null) {
                dependent |= blocked.contains(diff.foreignKey().referencedTable());
            }
            if (diff.type() == Behavior.CREATE_INDEX || diff.type() == Behavior.CREATE_UNIQUE) {
                for (String column : diff.index().columns())
                    dependent |= blocked.contains(table + ".column." + column);
            }
            if (dependent) {
                report.skippedSqls().add(diff.message());
                report.diagnose(
                        table,
                        diff.type().name(),
                        Stage.PREPARE,
                        Status.DEPENDENCY_BLOCKED,
                        "Required structure operation is blocked or failed");
                continue;
            }
            if (diff.verification() == null) {
                scriptSqls.addAll(diff.statements());
            }
            if (config.mode() == Schema.SCRIPT || config.dryRun()) {
                report.skippedSqls().addAll(diff.statements());
                report.diagnose(
                        table,
                        diff.type().name(),
                        Stage.PREPARE,
                        Status.PROPOSED,
                        diff.verification() == null ? "SQL generated without application"
                                : "Validated rebuild preview; apply through managed schema synchronization");
                continue;
            }
            try {
                if (diff.verification() != null && diff.type() == Behavior.DROP_COLUMN_DEFAULT) {
                    operations.prepareMaintenance(connection, diff);
                }
                executeDifference(connection, diff);
                report.executedSqls().addAll(diff.statements());
                report.diagnose(
                        table,
                        diff.type().name(),
                        Stage.EXECUTE,
                        Status.APPLIED,
                        "SQL executed; final structure is checked separately");
            } catch (SQLException e) {
                report.skippedSqls().addAll(diff.statements());
                report.failedDiffs().add(diff);
                blocked.add(diff.type() == Behavior.CREATE_TABLE ? table : dependencyKey(diff));
                report.diagnose(
                        table,
                        diff.type().name(),
                        Stage.EXECUTE,
                        Status.FAILED,
                        "SQL execution failed: SQLState=" + e.getSQLState());
                if (config.failFast() || !config.continueOnError())
                    throw e;
            }
        }
    }

    /**
     * Attempts to close every acquired lock in reverse order even when a preceding release raises SQLException. The
     * first release failure is retained and later failures are attached as suppressed exceptions.
     *
     * @param locks the locks to close
     * @throws SQLException when lock release fails
     */
    private void closeLocks(List<AutoCloseable> locks) throws SQLException {
        SQLException failure = null;
        for (int i = locks.size() - 1; i >= 0; i--) {
            try {
                closeLock(locks.get(i));
            } catch (SQLException e) {
                if (failure == null)
                    failure = e;
                else
                    failure.addSuppressed(e);
            }
        }
        if (failure != null)
            throw failure;
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
     * Prepares one schema difference without executing structural SQL. Capabilities, permission switches, and data
     * prerequisites are evaluated before SQL generation; non-applying or blocked operations receive diagnostics.
     *
     * @param connection the active database connection
     * @param dialect    the active dialect
     * @param operations the schema behavior
     * @param config     the schema configuration
     * @param report     the execution report
     * @param diff       the schema difference
     * @return prepared SQL, or null when this difference is inspected, blocked, or unsupported
     * @throws SQLException when preparation-time metadata or row-count checks fail
     */
    private SchemaDiff prepareDiff(
            Connection connection,
            Dialect dialect,
            SchemaBehavior operations,
            SchemaConfig config,
            SchemaReport report,
            SchemaDiff diff) throws SQLException {
        String table = diff.tableMeta().tableName();
        try {
            Status status = null;
            if (config.mode() == Schema.VALIDATE)
                status = Status.VALIDATED;
            else if (!dialect.supports(diff.type()))
                status = Status.UNSUPPORTED;
            else if (!allowed(dialect, config, diff, connection)
                    || diff.type() == Behavior.CREATE_TABLE && operations.inlinePrimaryKey()
                            && diff.tableMeta().primaryKey() != null && !config.allowCreatePrimaryKey())
                status = Status.CONFIGURATION_BLOCKED;
            if (status != null) {
                report.skippedSqls().add(diff.message());
                report.diagnose(table, diff.type().name(), Stage.PREPARE, status, diff.message());
                return null;
            }
            if (diff.type() == Behavior.DROP_COLUMN_DEFAULT || diff.type() == Behavior.REORDER_COLUMNS) {
                operations.prepareMaintenance(connection, diff);
            } else {
                String statement = sql(operations, diff);
                if (StringKit.isNotBlank(statement)) {
                    diff.preparation(List.of(statement));
                }
            }
            if (diff.preparation().isEmpty()) {
                report.skippedSqls().add(diff.message());
                report.diagnose(
                        table,
                        diff.type().name(),
                        Stage.PREPARE,
                        Status.UNSUPPORTED,
                        "DDL generator returned no SQL");
                return null;
            }
            if (config.printSql())
                Logger.info(
                        false,
                        "Mapper",
                        "Schema SQL prepared: table={}, operation={}, sql={}",
                        table,
                        diff.type(),
                        diff.statements());
            return diff;
        } catch (SQLException | RuntimeException e) {
            report.failedDiffs().add(diff);
            report.diagnose(table, diff.type().name(), Stage.PREPARE, Status.FAILED, "DDL preparation failed");
            throw e;
        }
    }

    /**
     * Builds the table-local key used to propagate a blocked or failed structural operation to dependent steps. Index
     * and primary-key replacements share a key across their drop/create operations.
     *
     * @param diff the planned structural difference
     * @return the table-qualified index, primary-key, column, or operation key
     */
    private String dependencyKey(SchemaDiff diff) {
        String table = diff.tableMeta().tableName();
        if (diff.index() != null)
            return table + ".index." + diff.index().name();
        if (diff.primaryKey() != null)
            return table + ".primaryKey";
        if (diff.columnMeta() != null)
            return table + ".column." + diff.columnMeta().column();
        if (diff.actualColumn() != null)
            return table + ".column." + diff.actualColumn().name();
        return table + Symbol.DOT + diff.type();
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
        if (diff.tableCreated() && diff.type() != Behavior.CREATE_TABLE && !config.allowCreateTable()) {
            return false;
        }
        if (config.mode() == Schema.CREATE) {
            return switch (diff.type()) {
                case CREATE_TABLE -> config.allowCreateTable();
                case MODIFY_TABLE_COMMENT, MODIFY_COLUMN_COMMENT -> config.allowCreateTable()
                        && config.allowModifyComment();
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
        if (diff.type() == Behavior.ADD_COLUMN
                && (diff.columnMeta().id() || Boolean.FALSE.equals(diff.columnMeta().ddlNullable()))
                && !hasDefault(diff.columnMeta()) && hasRows(connection, diff.tableMeta())) {
            return false;
        }
        boolean allowed = switch (diff.type()) {
            case CREATE_TABLE -> config.allowCreateTable();
            case MODIFY_TABLE_COMMENT -> config.allowModifyComment()
                    && (!diff.tableCreated() || config.allowCreateTable());
            case ADD_COLUMN -> config.allowAddColumn();
            case MODIFY_COLUMN_COMMENT -> {
                if (!config.allowModifyComment()) {
                    yield false;
                }
                if (diff.tableCreated()) {
                    yield config.allowCreateTable();
                }
                yield !diff.columnCreated() || config.allowAddColumn();
            }
            case MODIFY_COLUMN_TYPE -> config.allowModifyType();
            case MODIFY_COLUMN_LENGTH -> diff.riskLevel() == Risk.DANGEROUS ? config.allowShrinkLength()
                    : config.allowExpandLength();
            case MODIFY_COLUMN_DECIMAL -> diff.riskLevel() == Risk.DANGEROUS ? config.allowShrinkDecimal()
                    : config.allowExpandDecimal();
            case MODIFY_COLUMN_NULLABLE -> config.allowModifyNullable();
            case RENAME_COLUMN -> config.allowRenameColumn();
            case DROP_COLUMN -> config.allowDropColumn();
            case DROP_COLUMN_DEFAULT, REORDER_COLUMNS -> true;
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
     * Executes a native change or publishes a checked replacement in an isolated transaction.
     *
     * @param connection active maintenance connection
     * @param diff       prepared SQL and its optional pre-cutover row comparison
     * @throws SQLException when structural SQL or row verification fails
     */
    private void executeDifference(Connection connection, SchemaDiff diff) throws SQLException {
        if (diff.verification() == null) {
            executeStatements(connection, diff.preparation());
            return;
        }
        if (!connection.getAutoCommit()) {
            throw new SQLException("A verified table replacement requires its own transaction");
        }
        connection.setAutoCommit(false);
        try {
            executeStatements(connection, diff.preparation());
            try (var statement = connection.createStatement();
                    var result = statement.executeQuery(diff.verification())) {
                if (!result.next() || !result.getBoolean(1) || result.wasNull()) {
                    throw new SQLException("Replacement data or metadata differs from the original table");
                }
            }
            executeStatements(connection, diff.cutover());
            connection.commit();
        } catch (SQLException | RuntimeException failure) {
            try {
                connection.rollback();
            } catch (SQLException rollbackFailure) {
                failure.addSuppressed(rollbackFailure);
                throw failure;
            }
            try {
                connection.setAutoCommit(true);
            } catch (SQLException resetFailure) {
                failure.addSuppressed(resetFailure);
            }
            throw failure;
        }
        connection.setAutoCommit(true);
    }

    /**
     * Executes ordered structural statements using the current transaction.
     *
     * @param connection active schema connection
     * @param statements generated SQL in execution order
     * @throws SQLException if a statement fails
     */
    private void executeStatements(Connection connection, List<String> statements) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                statement.execute(sql);
            }
        }
    }

    /**
     * Builds SQL for a schema difference.
     *
     * @param operations the schema behavior
     * @param diff       the schema difference
     * @return the generated SQL, or null
     */
    private String sql(SchemaBehavior operations, SchemaDiff diff) {
        return switch (diff.type()) {
            case CREATE_TABLE -> operations.createTable(diff.tableMeta());
            case MODIFY_TABLE_COMMENT -> operations.modifyTableComment(diff.tableMeta());
            case ADD_COLUMN -> operations.addColumn(diff.tableMeta(), diff.columnMeta());
            case MODIFY_COLUMN_COMMENT -> operations.modifyColumnComment(diff.tableMeta(), diff.columnMeta());
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
        String sql = "SELECT COUNT(*) FROM " + table.tableName();
        try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(sql)) {
            return rs.next() && rs.getLong(1) > 0;
        } catch (SQLException e) {
            String context = "entity=" + (table.entityClass() == null ? null : table.entityClass().getName())
                    + ", table=" + table.tableName() + ", sql=" + sql;
            throw new SQLException("Schema row-count check failed: " + context, e.getSQLState(), e.getErrorCode(), e);
        }
    }

    /**
     * Tests whether a column definition contains a database default.
     *
     * @param column the column metadata
     * @return {@code true} when a default clause is present
     */
    private boolean hasDefault(ColumnMeta column) {
        return column.columnDefinition() != null
                && StringKit.toUpperCase(column.columnDefinition()).contains(" DEFAULT ");
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
                : diff.actualColumn() != null ? diff.actualColumn().name() : Normal.EMPTY;
        String type = diff.type().name();
        return config.dangerousWhitelist().contains(table)
                || config.dangerousWhitelist().contains(table + Symbol.DOT + column)
                || config.dangerousWhitelist().contains(type)
                || config.dangerousWhitelist().contains(type + Symbol.COLON + table)
                || config.dangerousWhitelist().contains(type + Symbol.COLON + table + Symbol.DOT + column);
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
     * @throws NullPointerException if the table metadata or its table name is null
     */
    private boolean excluded(TableMeta table, SchemaConfig config) {
        String physicalTableName = Objects.requireNonNull(table.tableName(), "Schema table name must not be null");
        String name = StringKit.toLowerCase(physicalTableName);
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
     */
    private enum SchemaPhase {

        /**
         * Table creation phase.
         */
        TABLE,

        /**
         * Reorders current physical columns before other changes can alter their definitions.
         */
        COLUMN_ORDER,

        /**
         * Removes foreign keys before the referenced indexes or columns can change.
         */
        FOREIGN_KEY_REMOVAL,

        /**
         * Removes indexes and unique constraints before column changes can remove them implicitly.
         */
        INDEX_REMOVAL,

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
        FOREIGN_KEY,

        /**
         * Table and column comment phase.
         */
        COMMENT;

        /**
         * Resolves the execution phase for a behavior.
         *
         * @param behavior the schema behavior
         * @return the execution phase
         */
        private static SchemaPhase of(Behavior behavior) {
            return switch (behavior) {
                case CREATE_TABLE -> TABLE;
                case REORDER_COLUMNS -> COLUMN_ORDER;
                case DROP_FOREIGN_KEY -> FOREIGN_KEY_REMOVAL;
                case DROP_UNIQUE, DROP_INDEX -> INDEX_REMOVAL;
                case CREATE_UNIQUE, CREATE_INDEX -> INDEX;
                case CREATE_FOREIGN_KEY -> FOREIGN_KEY;
                case MODIFY_TABLE_COMMENT, MODIFY_COLUMN_COMMENT -> COMMENT;
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
