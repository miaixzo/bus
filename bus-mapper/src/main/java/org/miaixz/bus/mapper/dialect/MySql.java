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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.feature.paging.Pageable;
import org.miaixz.bus.mapper.feature.schema.SqlTypeDescriptor;
import org.miaixz.bus.mapper.parsing.*;

/**
 * Dialect implementation for MySQL databases.
 *
 * <p>
 * This dialect uses standard {@code LIMIT/OFFSET} pagination and the MySQL {@code ON DUPLICATE KEY UPDATE} UPSERT
 * family.
 * </p>
 *
 * @author Kimi Liu
 */
public class MySql extends AbstractDialect {

    /**
     * Reads unique MySQL indexes with exactly one functional key part.
     */
    private static final String CONDITIONAL_INDEXES_QUERY = """
            SELECT INDEX_NAME, EXPRESSION FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA=? AND TABLE_NAME=? AND NON_UNIQUE=0
            AND INDEX_NAME IN (
                SELECT INDEX_NAME FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA=? AND TABLE_NAME=? GROUP BY INDEX_NAME HAVING COUNT(*)=1
            ) AND EXPRESSION IS NOT NULL
            """;

    /**
     * Reads MySQL's session SQL modes, which control parsing of catalogue literals.
     */
    private static final String SESSION_SQL_MODE_QUERY = "SELECT @@sql_mode";

    /**
     * Introduces a MySQL server-generated column and table definition query.
     */
    private static final String SHOW_CREATE_TABLE = "SHOW CREATE TABLE";

    /**
     * SQL clause that creates a MySQL database if missing.
     */
    private static final String CREATE_DATABASE_IF_ABSENT = "CREATE DATABASE IF NOT EXISTS";

    /**
     * SQL prefix for a MySQL table rename or namespace transfer.
     */
    private static final String RENAME_TABLE = "RENAME TABLE";

    /**
     * Token separating source and target coordinates in rename statements.
     */
    private static final String TO = "TO";

    /**
     * Creates the MySQL dialect.
     */
    public MySql() {
        super("MySQL", "jdbc:mysql:");
    }

    /**
     * Returns the UPSERT family used by MySQL.
     *
     * @return {@link Behavior#INSERT_ON_DUPLICATE}
     */
    @Override
    public Behavior getUpsertType() {
        return Behavior.INSERT_ON_DUPLICATE;
    }

    /**
     * Returns the database behavior set advertised by this dialect.
     *
     * @return the supported behavior set
     */
    @Override
    public EnumSet<Behavior> types() {
        var types = schemaTypes(getUpsertType());
        types.add(Behavior.REORDER_COLUMNS);
        return types;
    }

    /**
     * Reports the native column default removal supported by this dialect.
     *
     * @return true when an ALTER COLUMN DROP DEFAULT statement can be generated
     */
    @Override
    protected boolean supportsStandardColumnDefault() {
        return true;
    }

    /**
     * Supplies the MySQL identifier delimiter for physical maintenance operations.
     *
     * @return MySQL backtick delimiter
     */
    @Override
    protected String schemaQuote() {
        return Symbol.BACKTICK;
    }

    /**
     * Matches only the functional index expression generated for an integer-equality predicate.
     */
    private static final Pattern CONDITIONAL_KEY = Pattern.compile(
            "\\(case when \\(`([a-z_][a-z_0-9]*)` = (-?[0-9]+)\\) then `([a-z_][a-z_0-9]*)` else null end\\)",
            Pattern.CASE_INSENSITIVE);

    /**
     * Reports support for single-column conditional unique functional indexes on MySQL 8+.
     *
     * @return whether the dialect can represent a conditional uniqueness rule
     */
    @Override
    public boolean supportsConditionalUniqueIndexes() {
        return true;
    }

    /**
     * Creates a normal unique index or a conditional MySQL functional index.
     *
     * @param table table receiving the index
     * @param index mapped unique index definition
     * @return generated index definition
     */
    @Override
    public String createUnique(TableMeta table, IndexMeta index) {
        if (index.predicate() == null) {
            return super.createUnique(table, index);
        }
        String predicate = IndexMeta.integerEquality(index.predicate());
        if (predicate == null || index.columns().size() != 1) {
            throw unsupportedSchema("CREATE_CONDITIONAL_UNIQUE", table);
        }
        String[] condition = predicate.split(" = ", 2);
        String column = quotePhysicalIdentifier(unquote(index.columns().getFirst()), schemaQuote());
        return "CREATE UNIQUE INDEX " + quotePhysicalIdentifier(unquote(index.name()), schemaQuote()) + " ON "
                + tableName(table) + " ((CASE WHEN " + quotePhysicalIdentifier(condition[0], schemaQuote()) + " = "
                + condition[1] + " THEN " + column + " ELSE NULL END))";
    }

    /**
     * Reads native and conditional functional indexes using JDBC and the MySQL server's metadata.
     *
     * @param connection active MySQL connection
     * @param table      physical table whose indexes are inspected
     * @return index metadata, including recognized conditional expressions
     * @throws SQLException when an index catalogue cannot be read
     */
    @Override
    public List<IndexMeta> readIndexes(Connection connection, TableMeta table) throws SQLException {
        List<IndexMeta> indexes = super.readIndexes(connection, table);
        if (connection.getMetaData().getDatabaseMajorVersion() < 8
                || indexes.stream().noneMatch(index -> index.unique() && index.columns().isEmpty())) {
            return indexes;
        }
        String catalog = table.catalog() == null ? connection.getCatalog() : unquote(table.catalog());
        String tableName = unquote(table.table());
        if (tableName.indexOf(Symbol.C_DOT) >= 0) {
            return indexes;
        }
        try (var query = connection.prepareStatement(CONDITIONAL_INDEXES_QUERY)) {
            query.setString(1, catalog);
            query.setString(2, tableName);
            query.setString(3, catalog);
            query.setString(4, tableName);
            try (var rows = query.executeQuery()) {
                while (rows.next()) {
                    String name = rows.getString(1);
                    var match = CONDITIONAL_KEY.matcher(rows.getString(2).strip());
                    if (match.matches()) {
                        for (var index : indexes) {
                            if (index.name().equals(name) && index.columns().isEmpty()) {
                                index.predicate(match.group(1) + " = " + match.group(2));
                                index.columns().add(match.group(3));
                            }
                        }
                    }
                }
            }
        }
        return indexes;
    }

    /**
     * Creates an explicitly selected MySQL database namespace if it is missing.
     *
     * @param schema destination MySQL catalog
     * @return the generated create-database statement
     */
    @Override
    public String createSchema(String schema) {
        return CREATE_DATABASE_IF_ABSENT + Symbol.SPACE + quotePhysicalIdentifier(schema, schemaQuote());
    }

    /**
     * Transfers a table to another MySQL catalog using the native rename operation.
     *
     * @param table  mapped source table
     * @param schema destination MySQL catalog
     * @return the generated rename statement
     */
    @Override
    public String moveTable(TableMeta table, String schema) {
        return RENAME_TABLE + Symbol.SPACE + tableName(table) + Symbol.SPACE + TO + Symbol.SPACE
                + quotePhysicalIdentifier(schema, schemaQuote()) + Symbol.DOT
                + quotePhysicalIdentifier(unquote(table.table()), schemaQuote());
    }

    /**
     * Reorders a MySQL 8+ InnoDB table in one native ALTER, preserving the original column definitions.
     *
     * @param connection current MySQL connection
     * @param table      entity table with the desired ordered columns
     * @return a single native ALTER statement
     * @throws SQLException if engine capabilities or column definitions cannot be verified
     */
    @Override
    public String reorderColumns(Connection connection, TableMeta table) throws SQLException {
        var metadata = connection.getMetaData();
        if (!"MySQL".equalsIgnoreCase(metadata.getDatabaseProductName()) || metadata.getDatabaseMajorVersion() < 8) {
            throw unsupportedSchema("REORDER_COLUMNS", table);
        }
        String definition;
        try (var statement = connection.createStatement();
                var result = statement.executeQuery(SHOW_CREATE_TABLE + Symbol.SPACE + tableName(table))) {
            if (!result.next()) {
                throw new SQLException("Unable to read current table definition: " + table.tableName());
            }
            definition = result.getString(2);
        }
        int firstLine = definition.indexOf(Symbol.C_LF);
        int lastLine = definition.lastIndexOf("\n)");
        if (firstLine < 0 || lastLine <= firstLine
                || !definition.substring(lastLine).matches("(?is)\\n\\) ENGINE=InnoDB(?:\\s.*)?")) {
            throw new SQLException("Column reordering requires an ordinary MySQL InnoDB table");
        }
        boolean noBackslashEscapes;
        try (var statement = connection.createStatement();
                var result = statement.executeQuery(SESSION_SQL_MODE_QUERY)) {
            result.next();
            noBackslashEscapes = List.of(result.getString(1).split(Symbol.COMMA)).contains("NO_BACKSLASH_ESCAPES");
        }
        var snapshot = readTable(connection, table);
        Map<String, String> columns = new LinkedHashMap<>();
        for (String part : splitDefinition(definition.substring(firstLine + 1, lastLine), noBackslashEscapes)) {
            if (part.startsWith(Symbol.BACKTICK)) {
                String name = columnName(part);
                if (columns.put(snapshot.actualKey(name), part) != null) {
                    throw new SQLException("Duplicate column definition: " + name);
                }
            }
        }
        StringJoiner statement = new StringJoiner(Symbol.COMMA + Symbol.SPACE,
                ALTER_TABLE + Symbol.SPACE + tableName(table) + Symbol.SPACE, ", ALGORITHM=COPY, LOCK=EXCLUSIVE");
        String previous = null;
        for (ColumnMeta column : table.columns()) {
            String part = columns.remove(snapshot.key(column.column()));
            if (part == null) {
                throw new SQLException("Missing physical column definition: " + column.column());
            }
            statement.add("MODIFY COLUMN " + part + (previous == null ? " FIRST" : " AFTER " + previous));
            previous = quotePhysicalIdentifier(columnName(part), schemaQuote());
        }
        if (!columns.isEmpty()) {
            throw new SQLException("Reordering would omit physical columns: " + columns.keySet());
        }
        return statement.toString();
    }

    /**
     * Splits the server's table body at top-level commas, preserving quoted contents and nested expressions.
     *
     * @param body               server-generated column and constraint definitions
     * @param noBackslashEscapes whether session strings use doubled delimiters alone
     * @return intact column and table constraint fragments
     * @throws SQLException for incomplete or unrecognized definitions
     */
    private List<String> splitDefinition(String body, boolean noBackslashEscapes) throws SQLException {
        var result = new java.util.ArrayList<String>();
        int start = 0;
        int depth = 0;
        char quote = 0;
        for (int i = 0; i < body.length(); i++) {
            char ch = body.charAt(i);
            if (quote != 0) {
                if (ch == Symbol.C_BACKSLASH && quote != Symbol.C_BACKTICK && !noBackslashEscapes) {
                    i++;
                } else if (ch == quote) {
                    if (i + 1 < body.length() && body.charAt(i + 1) == quote) {
                        i++;
                    } else {
                        quote = 0;
                    }
                }
            } else if (ch == Symbol.C_SINGLE_QUOTE || ch == Symbol.C_DOUBLE_QUOTES || ch == Symbol.C_BACKTICK) {
                quote = ch;
            } else if (ch == Symbol.C_PARENTHESE_LEFT) {
                depth++;
            } else if (ch == Symbol.C_PARENTHESE_RIGHT) {
                if (--depth < 0) {
                    throw new SQLException("Unbalanced physical table definition");
                }
            } else if (ch == Symbol.C_COMMA && depth == 0) {
                result.add(body.substring(start, i).strip());
                start = i + 1;
            }
        }
        if (quote != 0 || depth != 0) {
            throw new SQLException("Unbalanced physical table definition");
        }
        result.add(body.substring(start).strip());
        return result;
    }

    /**
     * Extracts an actual column name from the start of a MySQL catalogue definition.
     *
     * @param definition intact column definition beginning with a quoted identifier
     * @return unescaped physical column name
     * @throws SQLException when the column identifier is incomplete
     */
    private String columnName(String definition) throws SQLException {
        StringBuilder result = new StringBuilder();
        for (int i = 1; i < definition.length(); i++) {
            char ch = definition.charAt(i);
            if (ch == Symbol.C_BACKTICK
                    && (i + 1 == definition.length() || definition.charAt(i + 1) != Symbol.C_BACKTICK)) {
                return result.toString();
            }
            if (ch == Symbol.C_BACKTICK) {
                i++;
            }
            result.append(ch);
        }
        throw new SQLException("Incomplete physical column identifier");
    }

    /**
     * Restores a named check constraint from a single catalogue definition in this dialect.
     *
     * @param table      physical table receiving the check
     * @param name       actual constraint name
     * @param definition complete CHECK clause from the database catalogue
     * @return native DDL for the named constraint
     */
    @Override
    public String restoreCheck(TableMeta table, String name, String definition) {
        return standardRestoreCheck(table, name, definition);
    }

    /**
     * Resolves the SQL type descriptor used by this dialect for the supplied mapper column.
     *
     * @param column the mapper column metadata
     * @return the SQL type descriptor for the column
     */
    @Override
    public SqlTypeDescriptor resolveType(ColumnMeta column) {
        return mysqlType(column);
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
        return mysqlModifyColumn(table, column);
    }

    /**
     * Builds the DDL used to set a MySQL table comment.
     *
     * @param table the mapper table metadata
     * @return the generated table-comment SQL
     */
    @Override
    public String modifyTableComment(TableMeta table) {
        return mysqlModifyTableComment(table);
    }

    /**
     * Builds the DDL used to set a MySQL column comment.
     *
     * @param table  the mapper table metadata
     * @param column the mapper column metadata
     * @return the generated column-comment SQL
     */
    @Override
    public String modifyColumnComment(TableMeta table, ColumnMeta column) {
        return mysqlModifyColumnComment(table, column);
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
        return mysqlDropIndex(table, index);
    }

    /**
     * Builds the DDL used to drop a MySQL primary key.
     *
     * @param table      the mapper table metadata
     * @param primaryKey the mapper primary key metadata
     * @return the generated drop-primary-key SQL
     */
    @Override
    public String dropPrimaryKey(TableMeta table, PrimaryKeyMeta primaryKey) {
        return "ALTER TABLE " + tableName(table) + " DROP PRIMARY KEY";
    }

    /**
     * Builds the DDL used to drop a MySQL foreign key.
     *
     * @param table      the mapper table metadata
     * @param foreignKey the mapper foreign key metadata
     * @return the generated drop-foreign-key SQL
     */
    @Override
    public String dropForeignKey(TableMeta table, ForeignKeyMeta foreignKey) {
        return "ALTER TABLE " + tableName(table) + " DROP FOREIGN KEY " + identifier(foreignKey.name());
    }

    /**
     * Builds paginated SQL using MySQL {@code LIMIT/OFFSET} syntax.
     *
     * @param originalSql the original SQL statement
     * @param pageable    the requested pagination information
     * @return the paginated SQL statement
     */
    @Override
    public String buildPaginationSql(String originalSql, Pageable pageable) {
        return buildPaginatedSql(originalSql, pageable);
    }

}
