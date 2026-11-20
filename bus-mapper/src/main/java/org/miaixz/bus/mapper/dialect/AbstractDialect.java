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

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.Date;

import jakarta.persistence.EnumType;

import org.apache.ibatis.type.JdbcType;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.Charter.Modify;
import org.miaixz.bus.mapper.feature.identifier.IdentifierValidator;
import org.miaixz.bus.mapper.feature.paging.Pageable;
import org.miaixz.bus.mapper.feature.schema.ColumnSnapshot;
import org.miaixz.bus.mapper.feature.schema.SqlTypeDescriptor;
import org.miaixz.bus.mapper.feature.schema.TableSnapshot;
import org.miaixz.bus.mapper.parsing.*;

/**
 * Base implementation for database dialects.
 *
 * <p>
 * This class centralizes common behavior shared by concrete dialects, including JDBC URL prefix-based resolution, count
 * SQL generation, and standard {@code LIMIT/OFFSET}-style pagination assembly.
 * </p>
 *
 * @author Kimi Liu
 */
public abstract class AbstractDialect implements Dialect {

    /**
     * Extra buffer size reserved when building paginated SQL statements.
     */
    protected static final int PAGINATION_SQL_EXTRA_CAPACITY = 50;

    /**
     * Comma plus one trailing space used in generated SQL lists.
     */
    protected static final String COMMA_SPACE = Symbol.COMMA + Symbol.SPACE;

    /**
     * JDBC table type used for table metadata lookup.
     */
    protected static final String TABLE_TYPE = "TABLE";

    /**
     * JDBC metadata column that stores table or column remarks.
     */
    protected static final String REMARKS_COLUMN = "REMARKS";

    /**
     * SQL Server extended-property name used for descriptions.
     */
    protected static final String SQLSERVER_DESCRIPTION = "MS_Description";

    /**
     * SQL Server schema level name.
     */
    protected static final String SQLSERVER_SCHEMA = "SCHEMA";

    /**
     * SQL Server table level name.
     */
    protected static final String SQLSERVER_TABLE = "TABLE";

    /**
     * SQL Server column level name.
     */
    protected static final String SQLSERVER_COLUMN = "COLUMN";

    /**
     * SQL Server column identifier property name.
     */
    protected static final String SQLSERVER_COLUMN_ID = "ColumnId";

    /**
     * Default SQL Server schema.
     */
    protected static final String SQLSERVER_DEFAULT_SCHEMA = "dbo";

    /**
     * SQL Server Unicode string literal prefix.
     */
    protected static final String SQLSERVER_UNICODE_PREFIX = "N";

    /**
     * Human-readable database name.
     */
    private final String databaseName;

    /**
     * Lower-level JDBC URL prefix used for dialect detection.
     */
    private final String jdbcUrlPrefix;

    /**
     * Creates a new dialect base instance.
     *
     * @param databaseName  the database product name exposed by this dialect
     * @param jdbcUrlPrefix the JDBC URL prefix used to identify the dialect
     */
    public AbstractDialect(String databaseName, String jdbcUrlPrefix) {
        this.databaseName = databaseName;
        this.jdbcUrlPrefix = jdbcUrlPrefix;
    }

    /**
     * Converts a JDBC type code to a MyBatis JDBC type.
     *
     * @param type the JDBC type code
     * @return the MyBatis JDBC type
     */
    private static JdbcType toJdbcType(int type) {
        try {
            return JdbcType.forCode(type);
        } catch (Exception ignored) {
            return switch (type) {
                case Types.VARCHAR, Types.NVARCHAR -> JdbcType.VARCHAR;
                case Types.INTEGER -> JdbcType.INTEGER;
                case Types.BIGINT -> JdbcType.BIGINT;
                case Types.DECIMAL, Types.NUMERIC -> JdbcType.DECIMAL;
                case Types.BOOLEAN, Types.BIT -> JdbcType.BOOLEAN;
                case Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE -> JdbcType.TIMESTAMP;
                case Types.DATE -> JdbcType.DATE;
                case Types.TIME -> JdbcType.TIME;
                case Types.BLOB, Types.BINARY, Types.VARBINARY, Types.LONGVARBINARY -> JdbcType.BLOB;
                default -> JdbcType.UNDEFINED;
            };
        }
    }

    /**
     * Reads a boolean result-set column defensively.
     *
     * @param rs     the result set
     * @param column the column label
     * @return the boolean value, or {@code false} when unavailable
     */
    private static boolean getBoolean(ResultSet rs, String column) {
        try {
            return rs.getBoolean(column);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Reads a string result-set column defensively.
     *
     * @param rs     the result set
     * @param column the column label
     * @return the string value, or {@code null} when unavailable
     */
    private static String getString(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Builds a full schema-capable type set.
     *
     * @param upsertType the UPSERT type
     * @return the supported type set
     */
    protected EnumSet<Behavior> schemaTypes(Behavior upsertType) {
        EnumSet<Behavior> types = EnumSet.of(
                Behavior.CREATE_TABLE,
                Behavior.ADD_COLUMN,
                Behavior.MODIFY_COLUMN_TYPE,
                Behavior.MODIFY_COLUMN_LENGTH,
                Behavior.MODIFY_COLUMN_DECIMAL,
                Behavior.MODIFY_COLUMN_NULLABLE,
                Behavior.RENAME_COLUMN,
                Behavior.DROP_COLUMN,
                Behavior.CREATE_INDEX,
                Behavior.DROP_INDEX,
                Behavior.CREATE_UNIQUE,
                Behavior.DROP_UNIQUE,
                Behavior.CREATE_PRIMARY_KEY,
                Behavior.DROP_PRIMARY_KEY,
                Behavior.CREATE_FOREIGN_KEY,
                Behavior.DROP_FOREIGN_KEY,
                Behavior.MODIFY_TABLE_COMMENT,
                Behavior.MODIFY_COLUMN_COMMENT,
                Behavior.READ_TABLE_METADATA,
                Behavior.READ_COLUMN_METADATA,
                Behavior.READ_INDEX_METADATA,
                Behavior.READ_PRIMARY_KEY_METADATA,
                Behavior.READ_FOREIGN_KEY_METADATA);
        if (upsertType != null && upsertType != Behavior.NONE) {
            types.add(upsertType);
        }
        return types;
    }

    /**
     * Returns the database name exposed by this dialect.
     *
     * @return the database name
     */
    @Override
    public String getDatabase() {
        return this.databaseName;
    }

    /**
     * Resolves the supplied JDBC URL by comparing it with the configured prefix.
     *
     * @param jdbcUrl the JDBC URL to evaluate
     * @return this dialect when the URL starts with the configured prefix, or {@code null} otherwise
     */
    @Override
    public Dialect resolve(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            return null;
        }
        return StringKit.toLowerCase(jdbcUrl).startsWith(StringKit.toLowerCase(this.jdbcUrlPrefix)) ? this : null;
    }

    /**
     * Wraps the supplied SQL as a subquery and produces a generic count statement.
     *
     * @param originalSql the original SQL statement
     * @return the generated count SQL
     */
    @Override
    public String buildCountSql(String originalSql) {
        return "SELECT COUNT(*) FROM (" + originalSql + ") AS total_count";
    }

    /**
     * Returns a concise textual representation of the dialect.
     *
     * @return the formatted dialect description
     */
    @Override
    public String toString() {
        return "Dialect[" + this.databaseName + Symbol.BRACKET_RIGHT;
    }

    /**
     * Builds a paginated SQL statement for dialects that use a plain {@code LIMIT/OFFSET} style syntax.
     *
     * @param originalSql the original SQL statement
     * @param pageable    the requested pagination information
     * @return the paginated SQL statement, or the original SQL when paging is disabled
     */
    protected String buildPaginatedSql(String originalSql, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return originalSql;
        }

        StringBuilder sql = new StringBuilder(originalSql.length() + PAGINATION_SQL_EXTRA_CAPACITY);
        sql.append(originalSql);
        sql.append(Symbol.SPACE).append(getLimitKeyword()).append(Symbol.SPACE).append(pageable.getPageSize());

        if (pageable.getOffset() > 0) {
            sql.append(Symbol.SPACE).append(getOffsetKeyword()).append(Symbol.SPACE).append(pageable.getOffset());
        }

        return sql.toString();
    }

    /**
     * Checks whether the physical table exists for the supplied mapper table metadata.
     *
     * @param connection the JDBC connection used to read database metadata
     * @param table      the mapper table metadata
     * @return {@code true} when the table exists
     * @throws SQLException if database metadata cannot be read
     */
    @Override
    public boolean existsTable(Connection connection, TableMeta table) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        for (String tableName : tableLookupNames(metaData, table)) {
            try (ResultSet rs = metaData.getTables(
                    metadataTarget(metaData, table).catalog(),
                    metadataPattern(metaData, metadataTarget(metaData, table).schema()),
                    metadataPattern(metaData, tableName),
                    new String[] { "TABLE" })) {
                while (rs.next()) {
                    if (metadataMatches(rs, metaData, table, Normal.EMPTY))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Reads the table metadata snapshot for the supplied mapper table.
     *
     * @param connection the JDBC connection used to read database metadata
     * @param table      the mapper table metadata
     * @return the table snapshot, or {@code null} when the table does not exist
     * @throws SQLException if database metadata cannot be read
     */
    @Override
    public TableSnapshot readTable(Connection connection, TableMeta table) throws SQLException {
        MetadataTarget target = metadataTarget(connection.getMetaData(), table);
        TableSnapshot snapshot = new TableSnapshot().name(table.tableName()).catalog(target.catalog())
                .schema(target.schema()).identifierRules(IdentifierValidator.metadataRules(connection.getMetaData()))
                .exists(existsTable(connection, table));
        if (snapshot.exists()) {
            snapshot.comment(readTableComment(connection, table));
            snapshot.columns(readColumns(connection, table));
            snapshot.indexes(readIndexes(connection, table));
            for (IndexMeta index : snapshot.indexes()) {
                if (index.columns().isEmpty()) {
                    snapshot.incompleteIndexes().add(snapshot.actualKey(index.name()));
                    snapshot.issues().add("Incomplete or non-column index metadata: " + index.name());
                }
            }
            snapshot.primaryKey(readPrimaryKey(connection, table));
            snapshot.foreignKeys(readForeignKeys(connection, table));
        }
        return snapshot;
    }

    /**
     * Reads a table comment from JDBC metadata remarks.
     *
     * @param connection the JDBC connection used to read database metadata
     * @param table      the mapper table metadata
     * @return the table comment, or {@code null} when unavailable
     * @throws SQLException if database metadata cannot be read
     */
    protected String readTableComment(Connection connection, TableMeta table) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        for (String tableName : tableLookupNames(metaData, table)) {
            try (ResultSet rs = metaData.getTables(
                    metadataTarget(metaData, table).catalog(),
                    metadataPattern(metaData, metadataTarget(metaData, table).schema()),
                    metadataPattern(metaData, tableName),
                    new String[] { TABLE_TYPE })) {
                while (rs.next()) {
                    if (metadataMatches(rs, metaData, table, Normal.EMPTY))
                        return getString(rs, REMARKS_COLUMN);
                }
            }
        }
        return null;
    }

    /**
     * Reads the column metadata snapshots for the supplied mapper table.
     *
     * @param connection the JDBC connection used to read database metadata
     * @param table      the mapper table metadata
     * @return the column snapshots found in database metadata
     * @throws SQLException if database metadata cannot be read
     */
    @Override
    public List<ColumnSnapshot> readColumns(Connection connection, TableMeta table) throws SQLException {
        List<ColumnSnapshot> columns = new ArrayList<>();
        DatabaseMetaData metaData = connection.getMetaData();
        for (String tableName : tableLookupNames(metaData, table)) {
            readColumns(metaData, table, tableName, columns);
            if (!columns.isEmpty()) {
                break;
            }
        }
        return columns;
    }

    /**
     * Reads column metadata for one physical table name.
     *
     * @param metaData  the database metadata
     * @param table     the table metadata
     * @param tableName the physical table name to query
     * @param columns   the column snapshot collector
     * @throws SQLException when metadata access fails
     */
    private void readColumns(DatabaseMetaData metaData, TableMeta table, String tableName, List<ColumnSnapshot> columns)
            throws SQLException {
        try (ResultSet rs = metaData.getColumns(
                metadataTarget(metaData, table).catalog(),
                metadataPattern(metaData, metadataTarget(metaData, table).schema()),
                metadataPattern(metaData, tableName),
                null)) {
            while (rs.next()) {
                if (!metadataMatches(rs, metaData, table, Normal.EMPTY))
                    continue;
                String typeName = rs.getString("TYPE_NAME");
                int jdbc = rs.getInt("DATA_TYPE");
                int sizeValue = rs.getInt("COLUMN_SIZE");
                Integer size = rs.wasNull() ? null : sizeValue;
                int scaleValue = rs.getInt("DECIMAL_DIGITS");
                Integer scale = rs.wasNull() ? null : scaleValue;
                int nullable = rs.getInt("NULLABLE");
                Boolean nullableValue = rs.wasNull() || nullable == DatabaseMetaData.columnNullableUnknown ? null
                        : nullable == DatabaseMetaData.columnNullable;
                SqlTypeDescriptor type = new SqlTypeDescriptor().jdbcType(toJdbcType(jdbc)).typeName(typeName);
                if (isLengthType(typeName)) {
                    type.length(size).lengthUnit(SqlTypeDescriptor.LengthUnit.CHARACTER);
                }
                if (isDecimalType(typeName)) {
                    type.precision(size).scale(scale);
                }
                columns.add(
                        new ColumnSnapshot().name(rs.getString("COLUMN_NAME")).comment(getString(rs, REMARKS_COLUMN))
                                .type(type).nullable(nullableValue));
            }
        }
    }

    /**
     * Reads the primary key metadata for the supplied mapper table.
     *
     * @param connection the JDBC connection used to read database metadata
     * @param table      the mapper table metadata
     * @return the primary key metadata, or {@code null} when no primary key exists
     * @throws SQLException if database metadata cannot be read
     */
    @Override
    public PrimaryKeyMeta readPrimaryKey(Connection connection, TableMeta table) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        for (String tableName : tableLookupNames(metaData, table)) {
            PrimaryKeyMeta primaryKey = readPrimaryKey(metaData, table, tableName);
            if (primaryKey != null) {
                return primaryKey;
            }
        }
        return null;
    }

    /**
     * Reads primary key metadata for one physical table name.
     *
     * @param metaData  the database metadata
     * @param table     the table metadata
     * @param tableName the physical table name to query
     * @return the primary key metadata, or {@code null} when no primary key exists
     * @throws SQLException when metadata access fails
     */
    private PrimaryKeyMeta readPrimaryKey(DatabaseMetaData metaData, TableMeta table, String tableName)
            throws SQLException {
        Map<Short, String> columns = new TreeMap<>();
        String name = null;
        try (ResultSet rs = metaData.getPrimaryKeys(
                metadataTarget(metaData, table).catalog(),
                metadataTarget(metaData, table).schema(),
                tableName)) {
            while (rs.next()) {
                if (!metadataMatches(rs, metaData, table, Normal.EMPTY))
                    continue;
                if (name == null) {
                    name = rs.getString("PK_NAME");
                }
                columns.put(rs.getShort("KEY_SEQ"), rs.getString("COLUMN_NAME"));
            }
        }
        if (columns.isEmpty()) {
            return null;
        }
        return PrimaryKeyMeta
                .of(name == null || name.isBlank() ? table.table() + "_pk" : name, new ArrayList<>(columns.values()));
    }

    /**
     * Reads foreign key metadata for the supplied mapper table.
     *
     * @param connection the JDBC connection used to read database metadata
     * @param table      the mapper table metadata
     * @return the foreign key metadata found in database metadata
     * @throws SQLException if database metadata cannot be read
     */
    @Override
    public List<ForeignKeyMeta> readForeignKeys(Connection connection, TableMeta table) throws SQLException {
        Map<String, ForeignKeyMeta> foreignKeys = new LinkedHashMap<>();
        DatabaseMetaData metaData = connection.getMetaData();
        for (String tableName : tableLookupNames(metaData, table)) {
            readForeignKeys(metaData, table, tableName, foreignKeys);
            if (!foreignKeys.isEmpty()) {
                break;
            }
        }
        return new ArrayList<>(foreignKeys.values());
    }

    /**
     * Reads foreign key metadata for one physical table name.
     *
     * @param metaData    the database metadata
     * @param table       the table metadata
     * @param tableName   the physical table name to query
     * @param foreignKeys the foreign key snapshot collector keyed by foreign key name
     * @throws SQLException when metadata access fails
     */
    private void readForeignKeys(
            DatabaseMetaData metaData,
            TableMeta table,
            String tableName,
            Map<String, ForeignKeyMeta> foreignKeys) throws SQLException {
        Map<String, SortedMap<Short, String[]>> members = new LinkedHashMap<>();
        try (ResultSet rs = metaData.getImportedKeys(
                metadataTarget(metaData, table).catalog(),
                metadataTarget(metaData, table).schema(),
                tableName)) {
            while (rs.next()) {
                if (!metadataMatches(rs, metaData, table, "FK"))
                    continue;
                String name = rs.getString("FK_NAME");
                String column = rs.getString("FKCOLUMN_NAME");
                String referencedColumn = rs.getString("PKCOLUMN_NAME");
                if (column == null || referencedColumn == null) {
                    continue;
                }
                String key = name == null || name.isBlank()
                        ? table.table() + Symbol.UNDERLINE + column + Symbol.UNDERLINE + "fk"
                        : name;
                ForeignKeyMeta foreignKey = foreignKeys.get(key);
                if (foreignKey == null) {
                    foreignKey = new ForeignKeyMeta().name(key).referencedTable(referencedTable(rs, metaData))
                            .onDelete(foreignKeyRule(rs, "DELETE_RULE")).onUpdate(foreignKeyRule(rs, "UPDATE_RULE"));
                    foreignKeys.put(key, foreignKey);
                }
                members.computeIfAbsent(key, ignored -> new TreeMap<>())
                        .put(rs.getShort("KEY_SEQ"), new String[] { column, referencedColumn });
            }
        }
        for (var entry : members.entrySet()) {
            ForeignKeyMeta key = foreignKeys.get(entry.getKey());
            for (String[] member : entry.getValue().values()) {
                key.columns().add(member[0]);
                key.referencedColumns().add(member[1]);
            }
        }
    }

    /**
     * Reads index metadata for the supplied mapper table. Columns are ordered by JDBC ordinal position; definitions
     * with unsupported filters, expression members, missing ordinals, or unknown uniqueness retain an empty column list
     * so the table snapshot can diagnose incomplete information instead of automatically replacing those indexes.
     *
     * @param connection the JDBC connection used to read database metadata
     * @param table      the mapper table metadata
     * @return the index metadata found in database metadata
     * @throws SQLException if database metadata cannot be read
     */
    @Override
    public List<IndexMeta> readIndexes(Connection connection, TableMeta table) throws SQLException {
        Map<String, IndexMeta> indexes = new LinkedHashMap<>();
        DatabaseMetaData metaData = connection.getMetaData();
        for (String tableName : tableLookupNames(metaData, table)) {
            readIndexes(metaData, table, tableName, indexes);
            if (!indexes.isEmpty()) {
                break;
            }
        }
        return new ArrayList<>(indexes.values());
    }

    /**
     * Reads index metadata for one physical table name.
     *
     * @param metaData  the database metadata
     * @param table     the table metadata
     * @param tableName the physical table name to query
     * @param indexes   the index snapshot collector keyed by index name
     * @throws SQLException when metadata access fails
     */
    private void readIndexes(
            DatabaseMetaData metaData,
            TableMeta table,
            String tableName,
            Map<String, IndexMeta> indexes) throws SQLException {
        Map<String, SortedMap<Integer, String>> ordered = new LinkedHashMap<>();
        Set<String> incomplete = new HashSet<>();
        try (ResultSet rs = metaData.getIndexInfo(
                metadataTarget(metaData, table).catalog(),
                metadataTarget(metaData, table).schema(),
                tableName,
                false,
                false)) {
            while (rs.next()) {
                if (!metadataMatches(rs, metaData, table, Normal.EMPTY))
                    continue;
                short type = rs.getShort("TYPE");
                String name = rs.getString("INDEX_NAME");
                if (type == DatabaseMetaData.tableIndexStatistic || name == null)
                    continue;
                String column = rs.getString("COLUMN_NAME");
                int ordinal = rs.getInt("ORDINAL_POSITION");
                boolean missingOrdinal = rs.wasNull() || ordinal <= 0;
                boolean nonUnique = rs.getBoolean("NON_UNIQUE");
                boolean missingUniqueness = rs.wasNull();
                IndexMeta index = indexes.computeIfAbsent(name, key -> new IndexMeta().name(key).unique(!nonUnique));
                String filter;
                try {
                    filter = rs.getString("FILTER_CONDITION");
                } catch (SQLException unsupported) {
                    filter = "unavailable";
                }
                boolean filtered = filter != null && !filter.isBlank();
                String predicate = filtered && supportsPartialIndexes() ? IndexMeta.integerEquality(filter) : null;
                index.predicate(predicate);
                SortedMap<Integer, String> cols = ordered.computeIfAbsent(name, key -> new TreeMap<>());
                if (column == null || missingOrdinal || missingUniqueness || filtered && predicate == null
                        || cols.putIfAbsent(ordinal, column) != null)
                    incomplete.add(name);
            }
        }
        for (var entry : ordered.entrySet()) {
            if (!incomplete.contains(entry.getKey()))
                indexes.get(entry.getKey()).columns().addAll(entry.getValue().values());
        }
    }

    /**
     * Builds the DDL used to create a table from mapper metadata.
     *
     * @param table the mapper table metadata
     * @return the generated create-table SQL
     */
    @Override
    public String createTable(TableMeta table) {
        StringJoiner joiner = new StringJoiner(COMMA_SPACE);
        for (ColumnMeta column : table.columns()) {
            joiner.add(columnDefinition(column));
        }
        return "CREATE TABLE" + Symbol.SPACE + tableName(table) + Symbol.SPACE + Symbol.PARENTHESE_LEFT + joiner
                + Symbol.PARENTHESE_RIGHT;
    }

    /**
     * Builds the DDL used to set or replace a table comment.
     *
     * @param table the mapper table metadata
     * @return the generated table-comment SQL
     */
    @Override
    public String modifyTableComment(TableMeta table) {
        return "COMMENT ON TABLE" + Symbol.SPACE + tableName(table) + Symbol.SPACE + "IS" + Symbol.SPACE
                + stringLiteral(table.comment());
    }

    /**
     * Builds the DDL used to add a column to an existing table.
     *
     * @param table  the mapper table metadata
     * @param column the mapper column metadata
     * @return the generated add-column SQL
     */
    @Override
    public String addColumn(TableMeta table, ColumnMeta column) {
        return "ALTER TABLE " + tableName(table) + " ADD COLUMN " + columnDefinition(column);
    }

    /**
     * Builds the DDL used to set or replace a column comment.
     *
     * @param table  the mapper table metadata
     * @param column the mapper column metadata
     * @return the generated column-comment SQL
     */
    @Override
    public String modifyColumnComment(TableMeta table, ColumnMeta column) {
        return "COMMENT ON COLUMN" + Symbol.SPACE + tableName(table) + Symbol.DOT + identifier(column.column())
                + Symbol.SPACE + "IS" + Symbol.SPACE + stringLiteral(column.comment());
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
        return modifyColumn(table, column);
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
        return modifyColumn(table, column);
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
        return modifyColumn(table, column);
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
        return modifyColumn(table, column);
    }

    /**
     * Builds the DDL used to rename a column.
     *
     * @param table     the mapper table metadata
     * @param oldColumn the existing column name
     * @param column    the mapper column metadata
     * @return the generated rename-column SQL
     */
    @Override
    public String renameColumn(TableMeta table, String oldColumn, ColumnMeta column) {
        return "ALTER TABLE " + tableName(table) + " RENAME COLUMN " + identifier(oldColumn) + " TO "
                + identifier(column.column());
    }

    /**
     * Builds the DDL used to drop a column from a table.
     *
     * @param table  the mapper table metadata
     * @param column the mapper column metadata
     * @return the generated drop-column SQL
     */
    @Override
    public String dropColumn(TableMeta table, ColumnSnapshot column) {
        return "ALTER TABLE " + tableName(table) + " DROP COLUMN " + identifier(column.name());
    }

    /**
     * Builds the DDL used to create a non-unique index.
     *
     * @param table the mapper table metadata
     * @param index the mapper index metadata
     * @return the generated create-index SQL
     */
    @Override
    public String createIndex(TableMeta table, IndexMeta index) {
        return "CREATE INDEX " + identifier(index.name()) + " ON " + tableName(table) + " ("
                + columnList(index.columns()) + Symbol.PARENTHESE_RIGHT + indexOptions(index);
    }

    /**
     * Appends the supported dialect predicate to an index definition.
     *
     * @param index mapped index definition
     * @return SQL clause for filtered indexes or an empty string for ordinary indexes
     */
    private String indexOptions(IndexMeta index) {
        if (index.predicate() == null) {
            return Normal.EMPTY;
        }
        if (!supportsPartialIndexes()) {
            throw new UnsupportedOperationException("Partial index requires a supporting dialect: " + index.name());
        }
        String predicate = IndexMeta.integerEquality(index.predicate());
        if (predicate == null) {
            throw new IllegalArgumentException("Unsupported partial index predicate: " + index.name());
        }
        return " WHERE " + predicate;
    }

    /**
     * Builds the DDL used to drop an index from a table.
     *
     * @param table the mapper table metadata
     * @param index the mapper index metadata
     * @return the generated drop-index SQL
     */
    @Override
    public String dropIndex(TableMeta table, IndexMeta index) {
        return "DROP INDEX " + identifier(index.name());
    }

    /**
     * Builds the DDL used to create a unique constraint or unique index.
     *
     * @param table the mapper table metadata
     * @param index the mapper index metadata
     * @return the generated create-unique SQL
     */
    @Override
    public String createUnique(TableMeta table, IndexMeta index) {
        return "CREATE UNIQUE INDEX " + identifier(index.name()) + " ON " + tableName(table) + " ("
                + columnList(index.columns()) + Symbol.PARENTHESE_RIGHT + indexOptions(index);
    }

    /**
     * Builds the DDL used to drop a unique constraint or unique index.
     *
     * @param table the mapper table metadata
     * @param index the mapper index metadata
     * @return the generated drop-unique SQL
     */
    @Override
    public String dropUnique(TableMeta table, IndexMeta index) {
        return dropIndex(table, index);
    }

    /**
     * Builds the DDL used to create a primary key constraint.
     *
     * @param table      the mapper table metadata
     * @param primaryKey the mapper primary key metadata
     * @return the generated create-primary-key SQL
     */
    @Override
    public String createPrimaryKey(TableMeta table, PrimaryKeyMeta primaryKey) {
        return "ALTER TABLE " + tableName(table) + " ADD CONSTRAINT " + identifier(primaryKeyName(table, primaryKey))
                + " PRIMARY KEY (" + columnList(primaryKey.columns()) + Symbol.PARENTHESE_RIGHT;
    }

    /**
     * Builds the DDL used to drop a primary key constraint.
     *
     * @param table      the mapper table metadata
     * @param primaryKey the mapper primary key metadata
     * @return the generated drop-primary-key SQL
     */
    @Override
    public String dropPrimaryKey(TableMeta table, PrimaryKeyMeta primaryKey) {
        return "ALTER TABLE " + tableName(table) + " DROP CONSTRAINT " + identifier(primaryKeyName(table, primaryKey));
    }

    /**
     * Builds the DDL used to create a foreign key constraint.
     *
     * @param table      the mapper table metadata
     * @param foreignKey the mapper foreign key metadata
     * @return the generated create-foreign-key SQL
     */
    @Override
    public String createForeignKey(TableMeta table, ForeignKeyMeta foreignKey) {
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(tableName(table));
        sql.append(" ADD CONSTRAINT ").append(identifier(foreignKey.name()));
        sql.append(" FOREIGN KEY (").append(columnList(foreignKey.columns())).append(Symbol.PARENTHESE_RIGHT);
        sql.append(" REFERENCES ").append(foreignKey.referencedTable());
        sql.append(" (").append(columnList(foreignKey.referencedColumns())).append(Symbol.PARENTHESE_RIGHT);
        if (foreignKey.onDelete() != null && !foreignKey.onDelete().isBlank()) {
            sql.append(" ON DELETE ").append(foreignKey.onDelete());
        }
        if (foreignKey.onUpdate() != null && !foreignKey.onUpdate().isBlank()) {
            sql.append(" ON UPDATE ").append(foreignKey.onUpdate());
        }
        return sql.toString();
    }

    /**
     * Builds the DDL used to drop a foreign key constraint.
     *
     * @param table      the mapper table metadata
     * @param foreignKey the mapper foreign key metadata
     * @return the generated drop-foreign-key SQL
     */
    @Override
    public String dropForeignKey(TableMeta table, ForeignKeyMeta foreignKey) {
        return "ALTER TABLE " + tableName(table) + " DROP CONSTRAINT " + identifier(foreignKey.name());
    }

    /**
     * Builds SQL for a column modification.
     *
     * @param table  the table metadata
     * @param column the column metadata
     * @return the column modification SQL
     */
    protected String modifyColumn(TableMeta table, ColumnMeta column) {
        throw unsupportedSchema("MODIFY_COLUMN", table);
    }

    /**
     * Builds SQL for a column modification using a common syntax shape.
     *
     * @param table      the table metadata
     * @param column     the column metadata
     * @param modifyMode the column modification syntax shape
     * @return the column modification SQL
     */
    protected String modifyColumn(TableMeta table, ColumnMeta column, Modify modifyMode) {
        return switch (modifyMode) {
            case ALTER_COLUMN -> "ALTER TABLE " + tableName(table) + " ALTER COLUMN " + columnDefinition(column);
            case MODIFY -> "ALTER TABLE " + tableName(table) + " MODIFY " + columnDefinition(column);
            case MODIFY_PARENTHESES -> "ALTER TABLE " + tableName(table) + " MODIFY (" + columnDefinition(column)
                    + Symbol.PARENTHESE_RIGHT;
            case ALTER_COLUMN_TYPE -> "ALTER TABLE " + tableName(table) + " ALTER COLUMN " + identifier(column.column())
                    + " TYPE " + resolveType(column).definition();
            case SET_DATA_TYPE -> "ALTER TABLE " + tableName(table) + " ALTER COLUMN " + identifier(column.column())
                    + " SET DATA TYPE " + resolveType(column).definition();
        };
    }

    /**
     * Builds SQL for changing column nullable constraints with ALTER COLUMN syntax.
     *
     * @param table  the table metadata
     * @param column the column metadata
     * @return the nullable modification SQL
     */
    protected String alterColumnNullable(TableMeta table, ColumnMeta column) {
        return "ALTER TABLE " + tableName(table) + " ALTER COLUMN " + identifier(column.column())
                + (Boolean.FALSE.equals(column.ddlNullable()) ? " SET NOT NULL" : " DROP NOT NULL");
    }

    /**
     * Resolves a MySQL-compatible SQL type.
     *
     * @param column the column metadata
     * @return the SQL type descriptor
     */
    protected SqlTypeDescriptor mysqlType(ColumnMeta column) {
        return commonType(column, "INT", "DATETIME", "TIMESTAMP", "BLOB", "LONGTEXT", "DECIMAL");
    }

    /**
     * Builds MySQL-compatible column modification SQL.
     *
     * @param table  the table metadata
     * @param column the column metadata
     * @return the column modification SQL
     */
    protected String mysqlModifyColumn(TableMeta table, ColumnMeta column) {
        return "ALTER TABLE" + Symbol.SPACE + tableName(table) + Symbol.SPACE + "MODIFY COLUMN" + Symbol.SPACE
                + mysqlColumnDefinition(column);
    }

    /**
     * Builds MySQL-compatible table comment SQL.
     *
     * @param table the table metadata
     * @return the table comment SQL
     */
    protected String mysqlModifyTableComment(TableMeta table) {
        return "ALTER TABLE" + Symbol.SPACE + tableName(table) + Symbol.SPACE + "COMMENT" + Symbol.SPACE + Symbol.EQUAL
                + Symbol.SPACE + stringLiteral(table.comment());
    }

    /**
     * Builds MySQL-compatible column comment SQL.
     *
     * @param table  the table metadata
     * @param column the column metadata
     * @return the column comment SQL
     */
    protected String mysqlModifyColumnComment(TableMeta table, ColumnMeta column) {
        return "ALTER TABLE" + Symbol.SPACE + tableName(table) + Symbol.SPACE + "MODIFY COLUMN" + Symbol.SPACE
                + mysqlColumnDefinition(column);
    }

    /**
     * Builds a MySQL column definition including an optional comment fragment.
     *
     * @param column the column metadata
     * @return the MySQL column definition SQL fragment
     */
    protected String mysqlColumnDefinition(ColumnMeta column) {
        String definition = columnDefinition(column);
        if (column.comment() == null || column.comment().isBlank()) {
            return definition;
        }
        return definition + Symbol.SPACE + "COMMENT" + Symbol.SPACE + stringLiteral(column.comment());
    }

    /**
     * Builds MySQL-compatible index drop SQL.
     *
     * @param table the table metadata
     * @param index the index metadata
     * @return the index drop SQL
     */
    protected String mysqlDropIndex(TableMeta table, IndexMeta index) {
        return "DROP INDEX " + identifier(index.name()) + " ON " + tableName(table);
    }

    /**
     * Resolves a PostgreSQL-compatible SQL type.
     *
     * @param column the column metadata
     * @return the SQL type descriptor
     */
    protected SqlTypeDescriptor postgresqlType(ColumnMeta column) {
        return commonType(column, "INTEGER", "TIMESTAMP", "TIMESTAMP WITH TIME ZONE", "BYTEA", "TEXT", "NUMERIC");
    }

    /**
     * Builds PostgreSQL-compatible column type modification SQL.
     *
     * @param table  the table metadata
     * @param column the column metadata
     * @return the column type modification SQL
     */
    protected String postgresqlModifyColumnType(TableMeta table, ColumnMeta column) {
        return "ALTER TABLE " + tableName(table) + " ALTER COLUMN " + identifier(column.column()) + " TYPE "
                + resolveType(column).definition();
    }

    /**
     * Builds PostgreSQL-compatible nullable modification SQL.
     *
     * @param table  the table metadata
     * @param column the column metadata
     * @return the nullable modification SQL
     */
    protected String postgresqlModifyColumnNullable(TableMeta table, ColumnMeta column) {
        return "ALTER TABLE " + tableName(table) + " ALTER COLUMN " + identifier(column.column())
                + (Boolean.FALSE.equals(column.ddlNullable()) ? " SET NOT NULL" : " DROP NOT NULL");
    }

    /**
     * Resolves an Oracle-compatible SQL type.
     *
     * @param column the column metadata
     * @return the SQL type descriptor
     */
    protected SqlTypeDescriptor oracleType(ColumnMeta column) {
        if (column.columnDefinition() != null && !column.columnDefinition().isBlank()) {
            return new SqlTypeDescriptor().nativeDefinition(column.columnDefinition());
        }
        Class<?> javaType = column.javaType();
        if (String.class == javaType) {
            if (Boolean.TRUE.equals(column.lob())) {
                return new SqlTypeDescriptor().jdbcType(JdbcType.LONGVARCHAR).typeName("CLOB");
            }
            return new SqlTypeDescriptor().jdbcType(JdbcType.VARCHAR).typeName("VARCHAR2")
                    .length(column.length() == null || column.length() <= 0 ? 255 : column.length());
        }
        if (javaType != null && javaType.isEnum()) {
            if (column.enumType() == EnumType.ORDINAL) {
                return new SqlTypeDescriptor().jdbcType(JdbcType.INTEGER).typeName("NUMBER").precision(10).scale(0);
            }
            return new SqlTypeDescriptor().jdbcType(JdbcType.VARCHAR).typeName("VARCHAR2")
                    .length(column.length() == null || column.length() <= 0 ? 255 : column.length());
        }
        if (javaType == int.class || javaType == Integer.class) {
            return new SqlTypeDescriptor().jdbcType(JdbcType.INTEGER).typeName("NUMBER").precision(10).scale(0);
        }
        if (javaType == long.class || javaType == Long.class) {
            return new SqlTypeDescriptor().jdbcType(JdbcType.BIGINT).typeName("NUMBER").precision(19).scale(0);
        }
        if (javaType == boolean.class || javaType == Boolean.class) {
            return new SqlTypeDescriptor().jdbcType(JdbcType.BOOLEAN).typeName("NUMBER").precision(1).scale(0);
        }
        if (javaType == BigDecimal.class) {
            return decimalType(column, "NUMBER");
        }
        if (javaType == LocalDate.class) {
            return new SqlTypeDescriptor().jdbcType(JdbcType.DATE).typeName("DATE");
        }
        if (javaType == LocalTime.class || javaType == LocalDateTime.class || javaType == Instant.class
                || javaType == Date.class) {
            return new SqlTypeDescriptor().jdbcType(JdbcType.TIMESTAMP).typeName("TIMESTAMP");
        }
        if (javaType == byte[].class || javaType == Byte[].class) {
            return new SqlTypeDescriptor().jdbcType(JdbcType.BLOB).typeName("BLOB");
        }
        return new SqlTypeDescriptor().jdbcType(JdbcType.VARCHAR).typeName("VARCHAR2")
                .length(column.length() == null || column.length() <= 0 ? 255 : column.length());
    }

    /**
     * Builds Oracle-compatible column add SQL.
     *
     * @param table  the table metadata
     * @param column the column metadata
     * @return the column add SQL
     */
    protected String oracleAddColumn(TableMeta table, ColumnMeta column) {
        return "ALTER TABLE " + tableName(table) + " ADD (" + columnDefinition(column) + Symbol.PARENTHESE_RIGHT;
    }

    /**
     * Builds Oracle-compatible column modification SQL.
     *
     * @param table  the table metadata
     * @param column the column metadata
     * @return the column modification SQL
     */
    protected String oracleModifyColumn(TableMeta table, ColumnMeta column) {
        return "ALTER TABLE " + tableName(table) + " MODIFY (" + columnDefinition(column) + Symbol.PARENTHESE_RIGHT;
    }

    /**
     * Builds SQL Server-compatible table comment SQL using the standard {@code MS_Description} extended property.
     *
     * @param table the table metadata
     * @return the table comment SQL
     */
    protected String sqlServerModifyTableComment(TableMeta table) {
        String schema = sqlServerSchema(table);
        String name = sqlServerTable(table);
        String qualified = schema + Symbol.DOT + name;
        String exists = "SELECT 1 FROM sys.extended_properties WHERE major_id = OBJECT_ID"
                + parenthesized(unicodeLiteral(qualified)) + " AND minor_id = 0 AND name = "
                + unicodeLiteral(SQLSERVER_DESCRIPTION);
        String levels = "@level0type" + Symbol.SPACE + Symbol.EQUAL + Symbol.SPACE + unicodeLiteral(SQLSERVER_SCHEMA)
                + COMMA_SPACE + "@level0name" + Symbol.SPACE + Symbol.EQUAL + Symbol.SPACE + unicodeLiteral(schema)
                + COMMA_SPACE + "@level1type" + Symbol.SPACE + Symbol.EQUAL + Symbol.SPACE
                + unicodeLiteral(SQLSERVER_TABLE) + COMMA_SPACE + "@level1name" + Symbol.SPACE + Symbol.EQUAL
                + Symbol.SPACE + unicodeLiteral(name);
        return "IF EXISTS" + Symbol.SPACE + parenthesized(exists) + Symbol.SPACE
                + "EXEC sp_updateextendedproperty @name = " + unicodeLiteral(SQLSERVER_DESCRIPTION) + COMMA_SPACE
                + "@value" + Symbol.SPACE + Symbol.EQUAL + Symbol.SPACE + unicodeLiteral(table.comment()) + COMMA_SPACE
                + levels + " ELSE EXEC sp_addextendedproperty @name = " + unicodeLiteral(SQLSERVER_DESCRIPTION)
                + COMMA_SPACE + "@value" + Symbol.SPACE + Symbol.EQUAL + Symbol.SPACE + unicodeLiteral(table.comment())
                + COMMA_SPACE + levels;
    }

    /**
     * Builds SQL Server-compatible column comment SQL using the standard {@code MS_Description} extended property.
     *
     * @param table  the table metadata
     * @param column the column metadata
     * @return the column comment SQL
     */
    protected String sqlServerModifyColumnComment(TableMeta table, ColumnMeta column) {
        String schema = sqlServerSchema(table);
        String name = sqlServerTable(table);
        String columnName = unquote(column.column());
        String qualified = schema + Symbol.DOT + name;
        String columnId = "COLUMNPROPERTY" + parenthesized(
                "OBJECT_ID" + parenthesized(unicodeLiteral(qualified)) + COMMA_SPACE + unicodeLiteral(columnName)
                        + COMMA_SPACE + unicodeLiteral(SQLSERVER_COLUMN_ID));
        String exists = "SELECT 1 FROM sys.extended_properties WHERE major_id = OBJECT_ID"
                + parenthesized(unicodeLiteral(qualified)) + " AND minor_id = " + columnId + " AND name = "
                + unicodeLiteral(SQLSERVER_DESCRIPTION);
        String levels = "@level0type" + Symbol.SPACE + Symbol.EQUAL + Symbol.SPACE + unicodeLiteral(SQLSERVER_SCHEMA)
                + COMMA_SPACE + "@level0name" + Symbol.SPACE + Symbol.EQUAL + Symbol.SPACE + unicodeLiteral(schema)
                + COMMA_SPACE + "@level1type" + Symbol.SPACE + Symbol.EQUAL + Symbol.SPACE
                + unicodeLiteral(SQLSERVER_TABLE) + COMMA_SPACE + "@level1name" + Symbol.SPACE + Symbol.EQUAL
                + Symbol.SPACE + unicodeLiteral(name) + COMMA_SPACE + "@level2type" + Symbol.SPACE + Symbol.EQUAL
                + Symbol.SPACE + unicodeLiteral(SQLSERVER_COLUMN) + COMMA_SPACE + "@level2name" + Symbol.SPACE
                + Symbol.EQUAL + Symbol.SPACE + unicodeLiteral(columnName);
        return "IF EXISTS" + Symbol.SPACE + parenthesized(exists) + Symbol.SPACE
                + "EXEC sp_updateextendedproperty @name = " + unicodeLiteral(SQLSERVER_DESCRIPTION) + COMMA_SPACE
                + "@value" + Symbol.SPACE + Symbol.EQUAL + Symbol.SPACE + unicodeLiteral(column.comment()) + COMMA_SPACE
                + levels + " ELSE EXEC sp_addextendedproperty @name = " + unicodeLiteral(SQLSERVER_DESCRIPTION)
                + COMMA_SPACE + "@value" + Symbol.SPACE + Symbol.EQUAL + Symbol.SPACE + unicodeLiteral(column.comment())
                + COMMA_SPACE + levels;
    }

    /**
     * Builds a column definition fragment.
     *
     * @param column the column metadata
     * @return the column definition SQL fragment
     */
    protected String columnDefinition(ColumnMeta column) {
        return identifier(column.column()) + Symbol.SPACE + resolveType(column).definition() + nullableSql(column);
    }

    /**
     * Builds the nullable SQL fragment for a column.
     *
     * @param column the column metadata
     * @return the nullable SQL fragment
     */
    protected String nullableSql(ColumnMeta column) {
        if (column.id() || Boolean.FALSE.equals(column.ddlNullable())) {
            return Symbol.SPACE + "NOT NULL";
        }
        return Normal.EMPTY;
    }

    /**
     * Builds the rendered table name.
     *
     * @param table the table metadata
     * @return the rendered table name
     */
    protected String tableName(TableMeta table) {
        return table.tableName();
    }

    /**
     * Builds the primary key constraint name.
     *
     * @param table      the table metadata
     * @param primaryKey the primary key metadata
     * @return the primary key constraint name
     */
    protected String primaryKeyName(TableMeta table, PrimaryKeyMeta primaryKey) {
        if (primaryKey != null && primaryKey.name() != null && !primaryKey.name().isBlank()) {
            return primaryKey.name();
        }
        return table.table() + Symbol.UNDERLINE + "pk";
    }

    /**
     * Builds a comma-separated identifier list.
     *
     * @param columns the column names
     * @return the rendered column list
     */
    protected String columnList(List<String> columns) {
        StringJoiner joiner = new StringJoiner(COMMA_SPACE);
        columns.forEach(column -> joiner.add(identifier(column)));
        return joiner.toString();
    }

    /**
     * Renders an identifier.
     *
     * @param identifier the raw identifier
     * @return the rendered identifier
     */
    protected String identifier(String identifier) {
        return identifier;
    }

    /**
     * Removes one complete outer identifier delimiter without deleting quotes embedded in the component.
     *
     * @param value the identifier value
     * @return the unquoted identifier value
     */
    protected String unquote(String value) {
        return IdentifierValidator.unquoteIdentifier(value);
    }

    /**
     * Builds a SQL string literal and escapes embedded quotes.
     *
     * @param value the literal value
     * @return the SQL string literal
     */
    protected String stringLiteral(String value) {
        String escaped = value == null ? Normal.EMPTY
                : value.replace(Symbol.SINGLE_QUOTE, Symbol.SINGLE_QUOTE + Symbol.SINGLE_QUOTE);
        return Symbol.SINGLE_QUOTE + escaped + Symbol.SINGLE_QUOTE;
    }

    /**
     * Builds a SQL Server Unicode string literal and escapes embedded quotes.
     *
     * @param value the literal value
     * @return the SQL Server Unicode string literal
     */
    protected String unicodeLiteral(String value) {
        return SQLSERVER_UNICODE_PREFIX + stringLiteral(value);
    }

    /**
     * Wraps a SQL fragment in parentheses.
     *
     * @param value the SQL fragment
     * @return the parenthesized SQL fragment
     */
    protected String parenthesized(String value) {
        return Symbol.PARENTHESE_LEFT + value + Symbol.PARENTHESE_RIGHT;
    }

    /**
     * Resolves the SQL Server schema name used in extended-property calls.
     *
     * @param table the table metadata
     * @return the schema name
     */
    protected String sqlServerSchema(TableMeta table) {
        if (table.schema() != null && !table.schema().isBlank()) {
            return unquote(table.schema());
        }
        String tableName = unquote(table.table());
        int dot = tableName == null ? -1 : tableName.lastIndexOf(Symbol.C_DOT);
        if (dot > 0) {
            return tableName.substring(0, dot);
        }
        return SQLSERVER_DEFAULT_SCHEMA;
    }

    /**
     * Resolves the SQL Server table name used in extended-property calls.
     *
     * @param table the table metadata
     * @return the simple table name
     */
    protected String sqlServerTable(TableMeta table) {
        String tableName = unquote(table.table());
        int dot = tableName == null ? -1 : tableName.lastIndexOf(Symbol.C_DOT);
        return dot < 0 ? tableName : tableName.substring(dot + 1);
    }

    /**
     * Exact physical lookup coordinates, before any JDBC pattern escaping.
     *
     * @param catalog the resolved physical catalog, or null for drivers without catalog scope
     * @param schema  the resolved physical schema, or null for drivers without schema scope
     * @param table   the resolved physical table component
     */
    private record MetadataTarget(String catalog, String schema, String table) {

    }

    /**
     * Resolves mapped qualifiers and the active connection namespace into exact physical coordinates. A missing active
     * namespace is an error when the driver advertises that namespace, preventing an accidental search of unrelated
     * schemas or catalogs. No resolved names are written back to shared table metadata.
     *
     * @param metadata the active connection metadata
     * @param table    the mapped table, possibly qualified or explicitly delimited
     * @return exact catalog/schema/table coordinates
     * @throws SQLException when metadata access fails, qualification is unsupported, or required namespace is absent
     */
    private MetadataTarget metadataTarget(DatabaseMetaData metadata, TableMeta table) throws SQLException {
        var rules = IdentifierValidator.metadataRules(metadata);
        List<String> parts = rules.parts(table.table());
        if (parts.size() > 3)
            throw new SQLException("Unsupported qualified table: " + table.table());
        String catalog = table.catalog(), schema = table.schema();
        String name = parts.getLast();
        if (parts.size() >= 2) {
            if (metadata.supportsSchemasInTableDefinitions())
                schema = parts.get(parts.size() - 2);
            else
                catalog = parts.get(parts.size() - 2);
        }
        if (parts.size() == 3)
            catalog = parts.getFirst();
        Connection connection = metadata.getConnection();
        String actualCatalog = catalog == null || catalog.isBlank() ? connection.getCatalog() : rules.key(catalog);
        String actualSchema = schema == null || schema.isBlank() ? connection.getSchema() : rules.key(schema);
        if ((actualSchema == null || actualSchema.isBlank()) && metadata.supportsSchemasInTableDefinitions()) {
            throw new SQLException(
                    "Schema metadata lookup requires an explicit or active schema: " + table.tableName());
        }
        if ((actualCatalog == null || actualCatalog.isBlank()) && metadata.supportsCatalogsInTableDefinitions()
                && !metadata.supportsSchemasInTableDefinitions()) {
            throw new SQLException(
                    "Schema metadata lookup requires an explicit or active catalog: " + table.tableName());
        }
        return new MetadataTarget(actualCatalog, actualSchema, rules.key(name));
    }

    /**
     * Escapes a physical component for JDBC methods accepting search patterns, not exact-name parameters. When the
     * driver supplies no escape string, null requests a broader search whose returned rows must still pass
     * metadataMatches before they are accepted.
     *
     * @param metadata the driver supplying the search escape string
     * @param value    the physical component, or null when the namespace has no value
     * @return an escaped pattern, or null when a broader filtered search is required
     * @throws SQLException when the driver cannot provide its escape string
     */
    private String metadataPattern(DatabaseMetaData metadata, String value) throws SQLException {
        if (value == null)
            return null;
        String escape = metadata.getSearchStringEscape();
        if (escape == null || escape.isEmpty())
            return null; // broader pattern, exact return filtering remains required
        return value.replace(escape, escape + escape).replace(Symbol.UNDERLINE, escape + Symbol.UNDERLINE)
                .replace(Symbol.PERCENT, escape + Symbol.PERCENT);
    }

    /**
     * Checks the current metadata row against all resolved physical coordinates. This filtering is required even when
     * the query pattern was escaped successfully.
     *
     * @param rs       the metadata result positioned on the candidate row
     * @param metadata the active driver metadata
     * @param table    the expected table mapping
     * @param prefix   column-label prefix: empty for table/column/index rows, or FK for imported-key rows
     * @return true when the row belongs to the exact resolved target
     * @throws SQLException when metadata coordinates cannot be resolved or read
     */
    private boolean metadataMatches(ResultSet rs, DatabaseMetaData metadata, TableMeta table, String prefix)
            throws SQLException {
        MetadataTarget target = metadataTarget(metadata, table);
        var rules = IdentifierValidator.metadataRules(metadata);
        String actualTable = rs.getString(prefix + "TABLE_NAME");
        String actualSchema = rs.getString(prefix + "TABLE_SCHEM");
        String actualCatalog = rs.getString(prefix + "TABLE_CAT");
        return rules.actualKey(actualTable).equals(rules.actualKey(target.table()))
                && (target.schema() == null || rules.actualKey(actualSchema).equals(rules.actualKey(target.schema())))
                && (target.catalog() == null
                        || rules.actualKey(actualCatalog).equals(rules.actualKey(target.catalog())));
    }

    /**
     * Returns the resolved physical table component for metadata lookup without speculative upper/lowercase aliases.
     *
     * @param metaData the database metadata
     * @param table    the table metadata
     * @return a singleton list containing the exact physical table component
     * @throws SQLException when metadata access fails
     */
    private List<String> tableLookupNames(DatabaseMetaData metaData, TableMeta table) throws SQLException {
        return List.of(metadataTarget(metaData, table).table());
    }

    /**
     * Adds a unique table lookup name.
     *
     * @param names the lookup name collector
     * @param name  the candidate table name
     */
    private void addLookupName(List<String> names, String name) {
        if (name != null && names.stream().noneMatch(existing -> existing.equals(name))) {
            names.add(name);
        }
    }

    /**
     * Builds a referenced table name from foreign key metadata.
     *
     * @param rs       the imported key result set
     * @param metadata the active metadata supplying the physical-name delimiter
     * @return the qualified referenced table name with each physical component separately delimited
     * @throws SQLException when metadata access fails
     */
    private String referencedTable(ResultSet rs, DatabaseMetaData metadata) throws SQLException {
        String quote = metadata.getIdentifierQuoteString();
        if (quote == null || quote.isBlank())
            quote = Normal.EMPTY;
        StringJoiner joiner = new StringJoiner(Symbol.DOT);
        for (String label : List.of("PKTABLE_CAT", "PKTABLE_SCHEM", "PKTABLE_NAME")) {
            String value = rs.getString(label);
            if (value != null && !value.isBlank())
                joiner.add(quote.isEmpty() ? value : quote + value.replace(quote, quote + quote) + quote);
        }
        return joiner.toString();
    }

    /**
     * Reads a foreign key rule from metadata.
     *
     * @param rs     the imported key result set
     * @param column the rule column name
     * @return the SQL rule name, or {@code null} when no explicit action is available
     * @throws SQLException when metadata access fails
     */
    private String foreignKeyRule(ResultSet rs, String column) throws SQLException {
        short rule = rs.getShort(column);
        return switch (rule) {
            case DatabaseMetaData.importedKeyCascade -> "CASCADE";
            case DatabaseMetaData.importedKeySetNull -> "SET NULL";
            case DatabaseMetaData.importedKeySetDefault -> "SET DEFAULT";
            case DatabaseMetaData.importedKeyRestrict -> "RESTRICT";
            default -> null;
        };
    }

    /**
     * Normalizes database-specific type aliases.
     *
     * @param typeName the database type name
     * @return the normalized type name
     */
    protected String normalizeTypeName(String typeName) {
        return SqlTypeDescriptor.normalizeTypeName(typeName);
    }

    /**
     * Tests whether a type should expose character length metadata.
     *
     * @param typeName the database type name
     * @return {@code true} when the type uses length
     */
    protected boolean isLengthType(String typeName) {
        return SqlTypeDescriptor.supportsLength(typeName);
    }

    /**
     * Tests whether a type should expose decimal precision and scale metadata.
     *
     * @param typeName the database type name
     * @return {@code true} when the type uses decimal metadata
     */
    protected boolean isDecimalType(String typeName) {
        return SqlTypeDescriptor.supportsPrecision(typeName);
    }

    /**
     * Builds the default VARCHAR type descriptor.
     *
     * @param column the column metadata
     * @return the SQL type descriptor
     */
    protected SqlTypeDescriptor stringType(ColumnMeta column) {
        return new SqlTypeDescriptor().jdbcType(JdbcType.VARCHAR).typeName("VARCHAR")
                .length(column.length() == null || column.length() <= 0 ? 255 : column.length())
                .lengthUnit(SqlTypeDescriptor.LengthUnit.CHARACTER);
    }

    /**
     * Builds a decimal type descriptor.
     *
     * @param column the column metadata
     * @param name   the database decimal type name
     * @return the SQL type descriptor
     */
    protected SqlTypeDescriptor decimalType(ColumnMeta column, String name) {
        int precision = column.precision() == null || column.precision() <= 0 ? 19 : column.precision();
        int scale = column.scale() == null ? 2 : column.scale();
        return new SqlTypeDescriptor().jdbcType(JdbcType.DECIMAL).typeName(name).precision(precision).scale(scale);
    }

    /**
     * Resolves common Java types to SQL types.
     *
     * @param column        the column metadata
     * @param integerName   the database integer type name
     * @param timestampName the database timestamp type name
     * @param instantName   the database instant type name
     * @param binaryName    the database binary type name
     * @param lobTextName   the database text LOB type name
     * @param decimalName   the database decimal type name
     * @return the SQL type descriptor
     */
    protected SqlTypeDescriptor commonType(
            ColumnMeta column,
            String integerName,
            String timestampName,
            String instantName,
            String binaryName,
            String lobTextName,
            String decimalName) {
        if (column.columnDefinition() != null && !column.columnDefinition().isBlank()) {
            return new SqlTypeDescriptor().nativeDefinition(column.columnDefinition());
        }
        Class<?> javaType = column.javaType();
        if (String.class == javaType) {
            if (Boolean.TRUE.equals(column.lob())) {
                return new SqlTypeDescriptor().jdbcType(JdbcType.LONGVARCHAR).typeName(lobTextName);
            }
            return stringType(column);
        }
        if (javaType != null && javaType.isEnum()) {
            if (column.enumType() == EnumType.ORDINAL) {
                return new SqlTypeDescriptor().jdbcType(JdbcType.INTEGER).typeName(integerName);
            }
            return stringType(column);
        }
        if (javaType == int.class || javaType == Integer.class) {
            return new SqlTypeDescriptor().jdbcType(JdbcType.INTEGER).typeName(integerName);
        }
        if (javaType == long.class || javaType == Long.class) {
            return new SqlTypeDescriptor().jdbcType(JdbcType.BIGINT).typeName("BIGINT");
        }
        if (javaType == boolean.class || javaType == Boolean.class) {
            return new SqlTypeDescriptor().jdbcType(JdbcType.BOOLEAN).typeName("BOOLEAN");
        }
        if (javaType == BigDecimal.class) {
            return decimalType(column, decimalName);
        }
        if (javaType == LocalDate.class) {
            return new SqlTypeDescriptor().jdbcType(JdbcType.DATE).typeName("DATE");
        }
        if (javaType == LocalTime.class) {
            return new SqlTypeDescriptor().jdbcType(JdbcType.TIME).typeName("TIME");
        }
        if (javaType == LocalDateTime.class || javaType == Date.class) {
            return new SqlTypeDescriptor().jdbcType(JdbcType.TIMESTAMP).typeName(timestampName);
        }
        if (javaType == Instant.class) {
            return new SqlTypeDescriptor().jdbcType(JdbcType.TIMESTAMP).typeName(instantName);
        }
        if (javaType == byte[].class || javaType == Byte[].class) {
            return new SqlTypeDescriptor().jdbcType(JdbcType.BLOB).typeName(binaryName);
        }
        return stringType(column);
    }

}
