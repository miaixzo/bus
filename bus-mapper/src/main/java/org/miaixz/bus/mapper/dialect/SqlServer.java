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
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.Charter.Modify;
import org.miaixz.bus.mapper.feature.identifier.IdentifierValidator;
import org.miaixz.bus.mapper.feature.paging.Pageable;
import org.miaixz.bus.mapper.feature.schema.SqlTypeDescriptor;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.IndexMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Dialect implementation for Microsoft SQL Server databases.
 *
 * <p>
 * This dialect uses SQL Server {@code OFFSET ... FETCH NEXT} pagination and maps UPSERT handling to the
 * {@code MERGE_USING_VALUES} family.
 * </p>
 *
 * @author Kimi Liu
 */
public class SqlServer extends AbstractDialect {

    /**
     * Reads the exact named default constraint attached to a SQL Server table column.
     */
    private static final String DEFAULT_CONSTRAINT_QUERY = """
            SELECT d.name FROM sys.default_constraints d JOIN sys.columns c
            ON c.object_id=d.parent_object_id AND c.column_id=d.parent_column_id
            WHERE d.parent_object_id=OBJECT_ID(?, N'U') AND c.name=?
            """;

    /**
     * Reads SQL Server's native filtered indexes with ordered physical key columns.
     */
    private static final String FILTERED_INDEXES_QUERY = """
            SELECT i.name, i.filter_definition, c.name, i.is_unique, k.key_ordinal,
                k.is_included_column, k.is_descending_key, i.is_disabled
            FROM sys.indexes i JOIN sys.index_columns k ON k.object_id=i.object_id AND k.index_id=i.index_id
            JOIN sys.columns c ON c.object_id=k.object_id AND c.column_id=k.column_id
            WHERE i.object_id=OBJECT_ID(?, N'U') AND i.has_filter=1 AND i.is_hypothetical=0
            ORDER BY i.index_id,k.key_ordinal
            """;

    /**
     * SQL clause that removes a named table constraint.
     */
    private static final String DROP_CONSTRAINT = "DROP CONSTRAINT";

    /**
     * Creates the SQL Server dialect.
     */
    public SqlServer() {
        super("Microsoft SQL Server", "jdbc:sqlserver:");
    }

    /**
     * Returns the UPSERT family used by SQL Server in this framework.
     *
     * @return {@link Behavior#MERGE_USING_VALUES}
     */
    @Override
    public Behavior getUpsertType() {
        return Behavior.MERGE_USING_VALUES;
    }

    /**
     * Returns the database behavior set advertised by this dialect.
     *
     * @return the supported behavior set
     */
    @Override
    public EnumSet<Behavior> types() {
        var types = schemaTypes(getUpsertType());
        types.add(Behavior.DROP_COLUMN_DEFAULT);
        return types;
    }

    /**
     * Looks up the SQL Server default constraint attached to the selected physical column.
     *
     * @param connection active connection in the target catalog
     * @param table      mapped physical table
     * @param column     actual column name
     * @return SQL to remove the exact default constraint, or null when none is attached
     * @throws SQLException when the system catalogue cannot be read
     */
    @Override
    public String dropColumnDefault(Connection connection, TableMeta table, String column) throws SQLException {
        return sqlServerDropColumnDefault(connection, table, column);
    }

    /**
     * Identifies the current SQL Server default using the active database and exact column identity.
     *
     * @param connection active connection for metadata discovery
     * @param table      mapped physical table
     * @param column     physical column with a default
     * @return SQL to drop the exact constraint or null when no attached constraint exists
     * @throws SQLException when the catalogue cannot be read or the connection targets a different catalog
     */
    static String sqlServerDropColumnDefault(Connection connection, TableMeta table, String column)
            throws SQLException {
        requireLocalCatalog(connection, table);
        try (var query = connection.prepareStatement(DEFAULT_CONSTRAINT_QUERY)) {
            query.setString(1, table.tableName());
            query.setString(2, column);
            try (var result = query.executeQuery()) {
                return result.next()
                        ? ALTER_TABLE + Symbol.SPACE + table.tableName() + Symbol.SPACE + DROP_CONSTRAINT + Symbol.SPACE
                                + Symbol.BRACKET_LEFT
                                + result.getString(1)
                                        .replace(Symbol.BRACKET_RIGHT, Symbol.BRACKET_RIGHT + Symbol.BRACKET_RIGHT)
                                + Symbol.BRACKET_RIGHT
                        : null;
            }
        }
    }

    /**
     * Matches the integer equality representation returned by SQL Server's index catalogue.
     */
    private static final Pattern FILTER = Pattern
            .compile("(?i)^\\(*\\[([a-z_][a-z_0-9]*)]\\s*=\\s*\\(*(-?[0-9]+)\\)*$");

    /**
     * Reports support for native SQL Server filtered indexes.
     *
     * @return true when mapped integer equality filters can be generated and inspected
     */
    @Override
    public boolean supportsPartialIndexes() {
        return true;
    }

    /**
     * Reads ordinary and native filtered SQL Server indexes for one physical table.
     *
     * @param connection active SQL Server connection
     * @param table      physical table scope
     * @return complete indexes or incomplete metadata for unrepresentable filters
     * @throws SQLException when the current database catalogue cannot be read
     */
    @Override
    public List<IndexMeta> readIndexes(Connection connection, TableMeta table) throws SQLException {
        return filteredIndexes(connection, table, super.readIndexes(connection, table));
    }

    /**
     * Augments driver index metadata using the server's filter and ordered-key catalogue.
     *
     * @param connection active SQL Server connection
     * @param table      checked table in the current catalogue
     * @param indexes    JDBC result to complete
     * @return indexes with server-authoritative filters
     * @throws SQLException when the system catalogue cannot be read
     */
    static List<IndexMeta> filteredIndexes(Connection connection, TableMeta table, List<IndexMeta> indexes)
            throws SQLException {
        requireLocalCatalog(connection, table);
        var filtered = new LinkedHashMap<String, IndexMeta>();
        Set<String> incomplete = new HashSet<>();
        try (var query = connection.prepareStatement(FILTERED_INDEXES_QUERY)) {
            query.setString(1, table.tableName());
            try (var rows = query.executeQuery()) {
                while (rows.next()) {
                    String name = rows.getString(1);
                    IndexMeta index = filtered.get(name);
                    if (index == null) {
                        String filter = rows.getString(2);
                        var match = FILTER.matcher(filter == null ? Normal.EMPTY : filter.strip());
                        String predicate = match.matches()
                                ? IndexMeta.integerEquality(match.group(1) + " = " + match.group(2))
                                : null;
                        index = new IndexMeta().name(name).unique(rows.getBoolean(4)).predicate(predicate);
                        filtered.put(name, index);
                    }
                    if (index.predicate() == null || rows.getInt(5) <= 0 || rows.getBoolean(6) || rows.getBoolean(7)
                            || rows.getBoolean(8)) {
                        incomplete.add(name);
                    } else {
                        index.columns().add(rows.getString(3));
                    }
                }
            }
        }
        for (IndexMeta index : filtered.values()) {
            if (incomplete.contains(index.name())) {
                index.columns().clear();
            }
        }
        indexes.removeIf(index -> filtered.containsKey(index.name()));
        indexes.addAll(filtered.values());
        return indexes;
    }

    /**
     * Verifies that system catalogue queries resolve the exact SQL Server database.
     *
     * @param connection active connection
     * @param table      physical table coordinate
     * @throws SQLException when the mapping names a different catalog or has unsupported qualification
     */
    private static void requireLocalCatalog(Connection connection, TableMeta table) throws SQLException {
        var rules = IdentifierValidator.metadataRules(connection.getMetaData());
        var parts = rules.parts(table.tableName());
        String catalog = parts.size() == 3 ? IdentifierValidator.unquoteIdentifier(parts.getFirst())
                : connection.getCatalog();
        if (parts.size() > 3 || !Objects.equals(connection.getCatalog(), catalog)) {
            throw new SQLException("SQL Server metadata lookup requires a connection to the target catalog");
        }
    }

    /**
     * Resolves the SQL type descriptor used by this dialect for the supplied mapper column.
     *
     * @param column the mapper column metadata
     * @return the SQL type descriptor for the column
     */
    @Override
    public SqlTypeDescriptor resolveType(ColumnMeta column) {
        return commonType(column, "INT", "DATETIME2", "DATETIMEOFFSET", "VARBINARY(MAX)", "VARCHAR(MAX)", "DECIMAL");
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
        return modifyColumn(table, column, Modify.ALTER_COLUMN);
    }

    /**
     * Builds the DDL used to set a SQL Server table comment.
     *
     * @param table the mapper table metadata
     * @return the generated table-comment SQL
     */
    @Override
    public String modifyTableComment(TableMeta table) {
        return sqlServerModifyTableComment(table);
    }

    /**
     * Builds the DDL used to set a SQL Server column comment.
     *
     * @param table  the mapper table metadata
     * @param column the mapper column metadata
     * @return the generated column-comment SQL
     */
    @Override
    public String modifyColumnComment(TableMeta table, ColumnMeta column) {
        return sqlServerModifyColumnComment(table, column);
    }

    /**
     * Builds paginated SQL using SQL Server {@code OFFSET ... FETCH NEXT} syntax.
     *
     * @param originalSql the original SQL statement
     * @param pageable    the requested pagination information
     * @return the paginated SQL statement
     */
    @Override
    public String buildPaginationSql(String originalSql, Pageable pageable) {
        if (pageable.isUnpaged()) {
            return originalSql;
        }
        StringBuilder sql = new StringBuilder(originalSql.length() + 100);
        sql.append(originalSql);
        sql.append(" OFFSET ").append(pageable.getOffset()).append(" ROWS");
        sql.append(" FETCH NEXT ").append(pageable.getPageSize()).append(" ROWS ONLY");
        return sql.toString();
    }

}
