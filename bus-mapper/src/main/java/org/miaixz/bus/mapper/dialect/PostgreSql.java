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
import java.util.Locale;

import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;
import org.miaixz.bus.mapper.feature.paging.Pageable;
import org.miaixz.bus.mapper.feature.schema.ColumnSnapshot;
import org.miaixz.bus.mapper.feature.schema.SqlTypeDescriptor;

/**
 * Dialect implementation for PostgreSQL databases.
 *
 * <p>
 * This dialect uses standard {@code LIMIT/OFFSET} pagination and PostgreSQL {@code ON CONFLICT} UPSERT semantics.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PostgreSql extends AbstractDialect {

    /**
     * Creates the PostgreSQL dialect.
     */
    public PostgreSql() {
        super("PostgreSQL", "jdbc:postgresql:");
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
        return stableHash(normalize(catalog) + ":" + normalize(schema) + ":" + normalize(tableName));
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
        return "";
    }

    /**
     * Normalizes lock key text.
     *
     * @param value the value to normalize
     * @return the normalized value
     */
    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
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
