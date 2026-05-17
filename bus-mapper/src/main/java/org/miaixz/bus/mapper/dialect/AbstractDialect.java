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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

import jakarta.persistence.EnumType;

import org.apache.ibatis.type.JdbcType;

import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.Charter.Modify;
import org.miaixz.bus.mapper.feature.paging.Pageable;
import org.miaixz.bus.mapper.feature.schema.ColumnSnapshot;
import org.miaixz.bus.mapper.feature.schema.SqlTypeDescriptor;
import org.miaixz.bus.mapper.feature.schema.TableSnapshot;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.IndexMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Base implementation for database dialects.
 *
 * <p>
 * This class centralizes common behavior shared by concrete dialects, including JDBC URL prefix-based resolution, count
 * SQL generation, and standard {@code LIMIT/OFFSET}-style pagination assembly.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class AbstractDialect implements Dialect {

    /**
     * Extra buffer size reserved when building paginated SQL statements.
     */
    protected static final int PAGINATION_SQL_EXTRA_CAPACITY = 50;

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
    protected AbstractDialect(String databaseName, String jdbcUrlPrefix) {
        this.databaseName = databaseName;
        this.jdbcUrlPrefix = jdbcUrlPrefix;
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
                Behavior.READ_TABLE_METADATA,
                Behavior.READ_COLUMN_METADATA,
                Behavior.READ_INDEX_METADATA);
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
        return jdbcUrl.toLowerCase().startsWith(this.jdbcUrlPrefix.toLowerCase()) ? this : null;
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
        return "Dialect[" + this.databaseName + "]";
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
        sql.append(" ").append(getLimitKeyword()).append(" ").append(pageable.getPageSize());

        if (pageable.getOffset() > 0) {
            sql.append(" ").append(getOffsetKeyword()).append(" ").append(pageable.getOffset());
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
            try (ResultSet rs = metaData
                    .getTables(table.catalog(), table.schema(), tableName, new String[] { "TABLE" })) {
                if (rs.next()) {
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
        TableSnapshot snapshot = new TableSnapshot().name(table.tableName()).exists(existsTable(connection, table));
        if (snapshot.exists()) {
            snapshot.columns(readColumns(connection, table));
            snapshot.indexes(readIndexes(connection, table));
        }
        return snapshot;
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
        try (ResultSet rs = metaData.getColumns(table.catalog(), table.schema(), tableName, null)) {
            while (rs.next()) {
                String typeName = normalizeTypeName(rs.getString("TYPE_NAME"));
                int jdbc = rs.getInt("DATA_TYPE");
                int size = rs.getInt("COLUMN_SIZE");
                int scale = rs.getInt("DECIMAL_DIGITS");
                int nullable = rs.getInt("NULLABLE");
                SqlTypeDescriptor type = new SqlTypeDescriptor().jdbcType(toJdbcType(jdbc)).typeName(typeName);
                if (isLengthType(typeName)) {
                    type.length(size);
                }
                if (isDecimalType(typeName)) {
                    type.precision(size).scale(scale);
                }
                columns.add(
                        new ColumnSnapshot().name(rs.getString("COLUMN_NAME")).type(type)
                                .nullable(nullable == DatabaseMetaData.columnNullable));
            }
        }
    }

    /**
     * Reads the index metadata for the supplied mapper table.
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
        try (ResultSet rs = metaData.getIndexInfo(table.catalog(), table.schema(), tableName, false, false)) {
            while (rs.next()) {
                short type = rs.getShort("TYPE");
                String name = rs.getString("INDEX_NAME");
                String column = rs.getString("COLUMN_NAME");
                if (type == DatabaseMetaData.tableIndexStatistic || name == null || column == null) {
                    continue;
                }
                IndexMeta index = indexes
                        .computeIfAbsent(name, key -> new IndexMeta().name(key).unique(!getBoolean(rs, "NON_UNIQUE")));
                index.columns().add(column);
            }
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
        StringJoiner joiner = new StringJoiner(", ");
        for (ColumnMeta column : table.columns()) {
            joiner.add(columnDefinition(column));
        }
        List<ColumnMeta> ids = table.columns().stream().filter(ColumnMeta::id).toList();
        if (!ids.isEmpty()) {
            StringJoiner pk = new StringJoiner(", ");
            ids.forEach(column -> pk.add(identifier(column.column())));
            joiner.add("PRIMARY KEY (" + pk + ")");
        }
        return "CREATE TABLE " + tableName(table) + " (" + joiner + ")";
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
                + columnList(index.columns()) + ")";
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
                + columnList(index.columns()) + ")";
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
            case MODIFY_PARENTHESES -> "ALTER TABLE " + tableName(table) + " MODIFY (" + columnDefinition(column) + ")";
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
        return "ALTER TABLE " + tableName(table) + " MODIFY COLUMN " + columnDefinition(column);
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
                || javaType == java.util.Date.class) {
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
        return "ALTER TABLE " + tableName(table) + " ADD (" + columnDefinition(column) + ")";
    }

    /**
     * Builds Oracle-compatible column modification SQL.
     *
     * @param table  the table metadata
     * @param column the column metadata
     * @return the column modification SQL
     */
    protected String oracleModifyColumn(TableMeta table, ColumnMeta column) {
        return "ALTER TABLE " + tableName(table) + " MODIFY (" + columnDefinition(column) + ")";
    }

    /**
     * Builds a column definition fragment.
     *
     * @param column the column metadata
     * @return the column definition SQL fragment
     */
    protected String columnDefinition(ColumnMeta column) {
        return identifier(column.column()) + " " + resolveType(column).definition() + nullableSql(column);
    }

    /**
     * Builds the nullable SQL fragment for a column.
     *
     * @param column the column metadata
     * @return the nullable SQL fragment
     */
    protected String nullableSql(ColumnMeta column) {
        if (column.id() || Boolean.FALSE.equals(column.ddlNullable())) {
            return " NOT NULL";
        }
        return "";
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
     * Builds a comma-separated identifier list.
     *
     * @param columns the column names
     * @return the rendered column list
     */
    protected String columnList(List<String> columns) {
        StringJoiner joiner = new StringJoiner(", ");
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
     * Removes common identifier quote characters.
     *
     * @param value the identifier value
     * @return the unquoted identifier value
     */
    protected String unquote(String value) {
        return value == null ? null : value.replace("`", "").replace("\"", "");
    }

    /**
     * Builds candidate table names for metadata lookup.
     *
     * @param metaData the database metadata
     * @param table    the table metadata
     * @return the candidate table names
     * @throws SQLException when metadata access fails
     */
    private List<String> tableLookupNames(DatabaseMetaData metaData, TableMeta table) throws SQLException {
        List<String> names = new ArrayList<>();
        String name = unquote(table.table());
        addLookupName(names, name);
        if (name != null) {
            if (metaData.storesUpperCaseIdentifiers()) {
                addLookupName(names, name.toUpperCase(Locale.ROOT));
            }
            if (metaData.storesLowerCaseIdentifiers()) {
                addLookupName(names, name.toLowerCase(Locale.ROOT));
            }
            addLookupName(names, name.toUpperCase(Locale.ROOT));
            addLookupName(names, name.toLowerCase(Locale.ROOT));
        }
        return names;
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
                .length(column.length() == null || column.length() <= 0 ? 255 : column.length());
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
        if (javaType == LocalDateTime.class || javaType == java.util.Date.class) {
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

}
