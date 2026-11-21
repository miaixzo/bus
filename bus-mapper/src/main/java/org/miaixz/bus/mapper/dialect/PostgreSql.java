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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumSet;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.feature.paging.Pageable;
import org.miaixz.bus.mapper.feature.schema.ColumnSnapshot;
import org.miaixz.bus.mapper.feature.schema.SqlTypeDescriptor;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Dialect implementation for PostgreSQL databases.
 *
 * <p>
 * This dialect uses standard {@code LIMIT/OFFSET} pagination and PostgreSQL {@code ON CONFLICT} UPSERT semantics.
 * </p>
 *
 * @author Kimi Liu
 */
public class PostgreSql extends AbstractDialect {

    /**
     * Creates the PostgreSQL dialect.
     */
    public PostgreSql() {
        super("PostgreSQL", "jdbc:postgresql:");
    }

    /**
     * Identifies dialects that generate and introspect supported partial indexes.
     *
     * @return whether the supported index predicates are available
     */
    @Override
    public boolean supportsPartialIndexes() {
        return true;
    }

    /**
     * Creates a PostgreSQL schema if it does not already exist.
     *
     * @param schema physical destination schema
     * @return generated schema creation SQL
     */
    @Override
    public String createSchema(String schema) {
        return CREATE_SCHEMA_IF_ABSENT + Symbol.SPACE + migrationIdentifier(schema);
    }

    /**
     * Moves a PostgreSQL table to another schema and keeps the same table name.
     *
     * @param table  physical table coordinate
     * @param schema destination schema
     * @return generated table transfer SQL
     */
    @Override
    public String moveTable(TableMeta table, String schema) {
        return "ALTER TABLE " + migrationTable(table) + " SET SCHEMA " + migrationIdentifier(schema);
    }

    /**
     * Takes an exclusive PostgreSQL table lock for the current transaction.
     *
     * @param table physical table coordinate
     * @return generated exclusive lock SQL
     */
    @Override
    public String lockTable(TableMeta table) {
        return "LOCK TABLE " + migrationTable(table) + " IN ACCESS EXCLUSIVE MODE";
    }

    /**
     * Restores a PostgreSQL check constraint from its database catalogue definition.
     *
     * @param table      physical replacement table
     * @param name       existing constraint name
     * @param definition complete check definition read from the database
     * @return generated constraint creation SQL
     */
    @Override
    public String restoreCheck(TableMeta table, String name, String definition) {
        if (definition == null || !definition.startsWith("CHECK (")) {
            throw new IllegalArgumentException("A PostgreSQL check definition is required.");
        }
        return "ALTER TABLE " + migrationTable(table) + " ADD CONSTRAINT " + migrationIdentifier(name) + Symbol.SPACE
                + definition;
    }

    /**
     * Removes an explicit PostgreSQL column default while retaining the column and its values.
     *
     * @param table  physical table coordinate
     * @param column actual column name returned by the catalogue
     * @return generated default removal SQL
     */
    @Override
    public String dropColumnDefault(TableMeta table, String column) {
        return ALTER_TABLE + Symbol.SPACE + migrationTable(table) + Symbol.SPACE + ALTER_COLUMN + Symbol.SPACE
                + migrationIdentifier(column) + Symbol.SPACE + DROP_DEFAULT;
    }

    /**
     * Qualifies the table with its explicit schema in the current PostgreSQL database.
     *
     * @param table physical table coordinate used for a migration
     * @return quoted schema and table names
     */
    private String migrationTable(TableMeta table) {
        return (table.schema() == null ? Normal.EMPTY : migrationIdentifier(unquote(table.schema())) + Symbol.DOT)
                + migrationIdentifier(unquote(table.table()));
    }

    /**
     * Quotes one unqualified PostgreSQL identifier supplied by maintenance configuration.
     *
     * @param value unqualified identifier
     * @return quoted identifier
     */
    private String migrationIdentifier(String value) {
        if (StringKit.isBlank(value) || value.indexOf(0) >= 0) {
            throw new IllegalArgumentException("A PostgreSQL migration identifier is required.");
        }
        return Symbol.DOUBLE_QUOTES + value.replace(Symbol.DOUBLE_QUOTES, Symbol.DOUBLE_QUOTES + Symbol.DOUBLE_QUOTES)
                + Symbol.DOUBLE_QUOTES;
    }

    /**
     * Reports PostgreSQL support for direct default removal during schema synchronization.
     *
     * @return true when native default removal is available
     */
    @Override
    protected boolean supportsStandardColumnDefault() {
        return true;
    }

    /**
     * Returns the UPSERT family used by PostgreSQL.
     *
     * @return {@link Behavior#INSERT_ON_CONFLICT}
     */
    @Override
    public Behavior getUpsertType() {
        return Behavior.INSERT_ON_CONFLICT;
    }

    /**
     * Returns the database behavior set advertised by this dialect.
     *
     * @return the supported behavior set
     */
    @Override
    public EnumSet<Behavior> types() {
        return schemaTypes(getUpsertType());
    }

    /**
     * Resolves the SQL type descriptor used by this dialect for the supplied mapper column.
     *
     * @param column the mapper column metadata
     * @return the SQL type descriptor for the column
     */
    @Override
    public SqlTypeDescriptor resolveType(ColumnMeta column) {
        return postgresqlType(column);
    }

    /**
     * Builds the DDL used to change the SQL type of a column.
     *
     * @param table        the mapper table metadata
     * @param column       the mapper column metadata
     * @param actualColumn the column metadata currently present in the database
     * @return the generated type-change SQL
     */
    @Override
    public String modifyColumnType(TableMeta table, ColumnMeta column, ColumnSnapshot actualColumn) {
        return postgresqlModifyColumnType(table, column);
    }

    /**
     * Builds the DDL used to change the character length of a column.
     *
     * @param table        the mapper table metadata
     * @param column       the mapper column metadata
     * @param actualColumn the column metadata currently present in the database
     * @return the generated length-change SQL
     */
    @Override
    public String modifyColumnLength(TableMeta table, ColumnMeta column, ColumnSnapshot actualColumn) {
        return modifyColumnType(table, column, actualColumn);
    }

    /**
     * Builds the DDL used to change the numeric precision or scale of a column.
     *
     * @param table        the mapper table metadata
     * @param column       the mapper column metadata
     * @param actualColumn the column metadata currently present in the database
     * @return the generated decimal-change SQL
     */
    @Override
    public String modifyColumnDecimal(TableMeta table, ColumnMeta column, ColumnSnapshot actualColumn) {
        return modifyColumnType(table, column, actualColumn);
    }

    /**
     * Builds the DDL used to change the nullable flag for a column.
     *
     * @param table        the mapper table metadata
     * @param column       the mapper column metadata
     * @param actualColumn the column metadata currently present in the database
     * @return the generated nullable-change SQL
     */
    @Override
    public String modifyColumnNullable(TableMeta table, ColumnMeta column, ColumnSnapshot actualColumn) {
        return postgresqlModifyColumnNullable(table, column);
    }

    /**
     * Builds the dialect-specific DDL used to replace or modify a column definition.
     *
     * @param table  the mapper table metadata
     * @param column the mapper column metadata
     * @return the generated column modification SQL
     */
    @Override
    protected String modifyColumn(TableMeta table, ColumnMeta column) {
        return modifyColumnType(table, column, null);
    }

    /**
     * Builds paginated SQL using PostgreSQL {@code LIMIT/OFFSET} syntax.
     *
     * @param originalSql the original SQL statement
     * @param pageable    the requested pagination information
     * @return the paginated SQL statement
     */
    @Override
    public String buildPaginationSql(String originalSql, Pageable pageable) {
        return buildPaginatedSql(originalSql, pageable);
    }

    /**
     * Acquires a PostgreSQL advisory lock for schema initialization.
     *
     * @param connection the active database connection
     * @param table      the table metadata
     * @return the advisory lock handle
     * @throws SQLException when the lock cannot be acquired
     */
    @Override
    public AutoCloseable acquireSchemaInitializationLock(Connection connection, TableMeta table) throws SQLException {
        long lockKey = advisoryLockKey(connection, table);
        try (PreparedStatement statement = connection.prepareStatement("SELECT pg_advisory_lock(?)")) {
            statement.setLong(1, lockKey);
            statement.execute();
        }
        return () -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT pg_advisory_unlock(?)")) {
                statement.setLong(1, lockKey);
                statement.execute();
            }
        };
    }

    /**
     * Builds a stable PostgreSQL advisory lock key.
     *
     * @param connection the active database connection
     * @param table      the table metadata
     * @return the advisory lock key
     * @throws SQLException when catalog or schema lookup fails
     */
    private long advisoryLockKey(Connection connection, TableMeta table) throws SQLException {
        String catalog = firstNonBlank(table.catalog(), connection.getCatalog());
        String schema = firstNonBlank(table.schema(), connection.getSchema());
        String tableName = table.tableName();
        return stableHash(normalize(catalog) + Symbol.COLON + normalize(schema) + Symbol.COLON + normalize(tableName));
    }

    /**
     * Returns the first non-blank value.
     *
     * @param values the candidate values
     * @return the first non-blank value, or an empty string
     */
    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return Normal.EMPTY;
    }

    /**
     * Normalizes lock key text.
     *
     * @param value the value to normalize
     * @return the normalized value
     */
    private String normalize(String value) {
        return value == null ? Normal.EMPTY : StringKit.toLowerCase(value);
    }

    /**
     * Computes a stable 64-bit FNV-1a hash.
     *
     * @param value the value to hash
     * @return the stable hash
     */
    private long stableHash(String value) {
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < value.length(); i++) {
            hash ^= value.charAt(i);
            hash *= 0x100000001b3L;
        }
        return hash;
    }

}
