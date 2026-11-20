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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.mapper.Charter.Behavior;
import org.miaixz.bus.mapper.Charter.Modify;
import org.miaixz.bus.mapper.feature.paging.Pageable;
import org.miaixz.bus.mapper.feature.schema.ColumnSnapshot;
import org.miaixz.bus.mapper.feature.schema.SchemaDiff;
import org.miaixz.bus.mapper.feature.schema.SqlTypeDescriptor;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.ForeignKeyMeta;
import org.miaixz.bus.mapper.parsing.IndexMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Dialect implementation for SQLite databases.
 *
 * <p>
 * This dialect uses standard {@code LIMIT/OFFSET} pagination and the SQLite {@code INSERT OR REPLACE} UPSERT family.
 * </p>
 *
 * @author Kimi Liu
 */
public class SQLite extends AbstractDialect {

    /**
     * Reads a main-database SQLite table definition without matching a temporary table.
     */
    private static final String TABLE_DEFINITION_QUERY = "SELECT sql FROM main.sqlite_schema WHERE type='table' AND name=? COLLATE NOCASE";

    /**
     * Reads the original SQL for all named indexes and triggers owned by one SQLite table.
     */
    private static final String ATTACHED_OBJECTS_QUERY = """
            SELECT type, name, sql FROM main.sqlite_schema WHERE tbl_name=? COLLATE NOCASE
            AND type IN ('index','trigger') AND sql IS NOT NULL ORDER BY type,name
            """;

    /**
     * Reads whether a SQLite index has a partial filter from the real table catalogue.
     */
    private static final String PARTIAL_INDEXES_QUERY = """
            SELECT s.name, s.sql FROM main.sqlite_schema s
            JOIN pragma_index_list(?,'main') i ON i.name=s.name
            WHERE s.type='index' AND i.partial=1
            """;

    /**
     * Discovers external objects and foreign keys that require a coordinated SQLite rebuild.
     */
    private static final String REBUILD_DEPENDENCIES_QUERY = """
            SELECT 1 FROM main.sqlite_schema WHERE type IN ('view','trigger')
            AND tbl_name<>? COLLATE NOCASE AND instr(lower(sql),lower(?))>0
            UNION ALL
            SELECT 1 FROM main.sqlite_schema t JOIN pragma_foreign_key_list(t.name,'main') f
            WHERE t.type='table' AND (f."table"=? COLLATE NOCASE OR t.name=? COLLATE NOCASE)
            """;

    /**
     * Compares original and replacement rows and rechecks SQLite table metadata before cutover.
     */
    private static final String VERIFY_REBUILD_SQL = """
            SELECT NOT EXISTS (%1$s EXCEPT %2$s) AND NOT EXISTS (%2$s EXCEPT %1$s)
            AND (SELECT count(*) FROM %3$s)=(SELECT count(*) FROM %4$s)
            AND (SELECT sql FROM main.sqlite_schema WHERE type='table' AND name=%5$s)=%6$s
            AND (SELECT count(*) FROM main.sqlite_schema WHERE tbl_name=%5$s AND type IN ('index','trigger')
                AND sql IS NOT NULL)=%7$d
            AND %8$s AND NOT EXISTS (%9$s)
            """;

    /**
     * Checks that the original SQLite index and trigger SQL still matches the prepared snapshot.
     */
    private static final String VERIFY_OBJECTS_SQL = """
            NOT EXISTS (SELECT 1 FROM main.sqlite_schema WHERE tbl_name=%1$s AND type IN ('index','trigger')
                AND sql IS NOT NULL AND sql NOT IN (%2$s))
            """;

    /**
     * Qualifier for objects held in SQLite's primary attached database file.
     */
    private static final String MAIN_SCHEMA = "main";

    /**
     * Prefix reserved for temporary SQLite replacement tables created inside a maintenance transaction.
     */
    private static final String REBUILD_PREFIX = "__bus_rebuild_";

    /**
     * SQLite table option that omits the internal row identifier.
     */
    private static final String WITHOUT_ROWID = "WITHOUT ROWID";

    /**
     * Introduces the source rows of a SQL copy.
     */
    private static final String SELECT = "SELECT";

    /**
     * Qualifies the relation supplying SQL rows.
     */
    private static final String FROM = "FROM";

    /**
     * SQL prefix for a row insert.
     */
    private static final String INSERT_INTO = "INSERT INTO";

    /**
     * Builds a SQLite replacement table in the main database.
     */
    private static final String CREATE_TABLE = "CREATE TABLE";

    /**
     * Removes a SQLite table after its rows have been verified in a replacement.
     */
    private static final String DROP_TABLE = "DROP TABLE";

    /**
     * Renames the selected physical table.
     */
    private static final String RENAME_TO = "RENAME TO";

    /**
     * Creates the SQLite dialect.
     */
    public SQLite() {
        super("SQLite", "jdbc:sqlite:");
    }

    /**
     * Returns the UPSERT family used by SQLite.
     *
     * @return {@link Behavior#INSERT_OR_REPLACE}
     */
    @Override
    public Behavior getUpsertType() {
        return Behavior.INSERT_OR_REPLACE;
    }

    /**
     * Returns the database behavior set advertised by this dialect.
     *
     * @return the supported behavior set
     */
    @Override
    public EnumSet<Behavior> types() {
        EnumSet<Behavior> types = schemaTypes(getUpsertType());
        types.add(Behavior.DROP_COLUMN_DEFAULT);
        types.add(Behavior.REORDER_COLUMNS);
        types.removeAll(
                EnumSet.of(
                        Behavior.CREATE_FOREIGN_KEY,
                        Behavior.DROP_FOREIGN_KEY,
                        Behavior.MODIFY_TABLE_COMMENT,
                        Behavior.MODIFY_COLUMN_COMMENT,
                        Behavior.MODIFY_COLUMN_TYPE,
                        Behavior.MODIFY_COLUMN_LENGTH,
                        Behavior.MODIFY_COLUMN_DECIMAL,
                        Behavior.MODIFY_COLUMN_NULLABLE,
                        Behavior.CREATE_PRIMARY_KEY,
                        Behavior.DROP_PRIMARY_KEY));
        return types;
    }

    /**
     * Indicates that a new table's primary key is emitted inside CREATE TABLE, because SQLite cannot add it to an
     * existing table with the generic ALTER TABLE operation.
     *
     * @return true so the differ omits a duplicate post-creation primary-key operation
     */
    @Override
    public boolean inlinePrimaryKey() {
        return true;
    }

    /**
     * Builds the column definitions and appends the mapped primary key inside the new table definition. This supports
     * table creation only; existing-table primary-key changes remain unsupported.
     *
     * @param table the mapped table and its ordered primary-key columns
     * @return CREATE TABLE SQL containing the primary key when one is mapped
     */
    @Override
    public String createTable(TableMeta table) {
        String base = super.createTable(table);
        if (table.primaryKey() == null)
            return base;
        return base.substring(0, base.length() - 1) + ", PRIMARY KEY (" + columnList(table.primaryKey().columns())
                + "))";
    }

    /**
     * Extracts the predicate after the enclosing key list of a SQLite partial index.
     */
    private static final Pattern PARTIAL_FILTER = Pattern.compile("(?is)\\)\\s+WHERE\\s+(.*)$");

    /**
     * Reports support for native SQLite filtered indexes.
     *
     * @return true because SQLite generates and reads mapped partial indexes
     */
    @Override
    public boolean supportsPartialIndexes() {
        return true;
    }

    /**
     * Reads ordinary indexes and resolves SQLite's native partial filter expressions.
     *
     * @param connection active SQLite connection
     * @param table      physical table whose indexes are inspected
     * @return indexes with recognized equality predicates or incomplete metadata for unknown predicates
     * @throws SQLException when SQLite index metadata cannot be read
     */
    @Override
    public List<IndexMeta> readIndexes(Connection connection, TableMeta table) throws SQLException {
        List<IndexMeta> indexes = super.readIndexes(connection, table);
        String name = unquote(table.table());
        if (name.indexOf('.') >= 0
                || table.schema() != null && !MAIN_SCHEMA.equalsIgnoreCase(unquote(table.schema()))) {
            return indexes;
        }
        try (var sql = connection.prepareStatement(PARTIAL_INDEXES_QUERY)) {
            sql.setString(1, name);
            try (var rows = sql.executeQuery()) {
                while (rows.next()) {
                    String indexName = rows.getString(1);
                    String definition = rows.getString(2);
                    var filter = PARTIAL_FILTER.matcher(definition == null ? "" : definition);
                    String predicate = filter.find() ? IndexMeta.integerEquality(filter.group(1)) : null;
                    for (IndexMeta index : indexes) {
                        if (index.name().equals(indexName)) {
                            index.predicate(predicate);
                            if (predicate == null) {
                                index.columns().clear();
                            }
                        }
                    }
                }
            }
        }
        return indexes;
    }

    /**
     * Prepares a SQLite table replacement for a default or physical column order change.
     *
     * @param connection active SQLite connection
     * @param diff       selected table or column operation
     * @throws SQLException when the source definition or dependencies cannot be safely restored
     */
    @Override
    public void prepareMaintenance(Connection connection, SchemaDiff diff) throws SQLException {
        if (diff.type() == Behavior.DROP_COLUMN_DEFAULT || diff.type() == Behavior.REORDER_COLUMNS) {
            prepareReplacement(connection, diff, diff.actualColumn() == null ? null : diff.actualColumn().name());
        } else {
            throw unsupportedSchema(diff.type().name(), diff.tableMeta());
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
        return commonType(column, "INTEGER", "TEXT", "TEXT", "BLOB", "TEXT", "NUMERIC");
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
        return alterColumnNullable(table, column);
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
        return modifyColumn(table, column, Modify.ALTER_COLUMN_TYPE);
    }

    /**
     * Rejects foreign key creation on existing SQLite tables.
     *
     * @param table      the mapper table metadata
     * @param foreignKey the mapper foreign key metadata
     * @return never returns normally
     */
    @Override
    public String createForeignKey(TableMeta table, ForeignKeyMeta foreignKey) {
        throw unsupportedSchema("CREATE_FOREIGN_KEY", table);
    }

    /**
     * Rejects foreign key dropping on existing SQLite tables.
     *
     * @param table      the mapper table metadata
     * @param foreignKey the mapper foreign key metadata
     * @return never returns normally
     */
    @Override
    public String dropForeignKey(TableMeta table, ForeignKeyMeta foreignKey) {
        throw unsupportedSchema("DROP_FOREIGN_KEY", table);
    }

    /**
     * Builds paginated SQL using SQLite {@code LIMIT/OFFSET} syntax.
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
     * Matches an ordinary table definition followed by its parenthesized column and constraint body.
     */
    private static final Pattern TABLE_START = Pattern.compile(
            "(?is)^CREATE\\s+TABLE\\s+(?:\"(?:[^\"]|\"\")*\"|`(?:[^`]|``)*`|\\[[^]]+]|[a-z_][a-z_0-9]*)\\s*\\(");

    /**
     * Locates the column identifier in table definition fragments.
     */
    private static final Pattern COLUMN_START = Pattern
            .compile("(?s)^(\"(?:[^\"]|\"\")*\"|`(?:[^`]|``)*`|\\[[^]]+]|[a-zA-Z_][a-zA-Z_0-9]*)\\s");

    /**
     * Locates a default modifier after lexical masking has removed nested expressions and quoted strings.
     */
    private static final Pattern DEFAULT_KEYWORD = Pattern.compile("(?i)(?<![a-z_0-9])DEFAULT(?![a-z_0-9])");

    /**
     * Reads a single unparenthesized SQLite constant default.
     */
    private static final Pattern DEFAULT_CONSTANT = Pattern.compile(
            "(?i)^(?:NULL|TRUE|FALSE|CURRENT_TIME|CURRENT_DATE|CURRENT_TIMESTAMP|[+-]?\\s*"
                    + "(?:0x[0-9a-f]+|(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:e[+-]?[0-9]+)?))(?=\\s|$)");

    /**
     * Defines the approved ordinary table forms and dependencies before data can be changed.
     *
     * @param dialect       SQLite dialect used to obtain column metadata
     * @param connection    active SQLite connection
     * @param table         mapped table with requested entity column order
     * @param removeDefault actual column whose default should be removed, or null to reorder
     * @throws SQLException when the catalogue or table cannot be safely represented
     */
    private void prepareReplacement(Connection connection, SchemaDiff diff, String removeDefault) throws SQLException {
        TableMeta table = diff.tableMeta();
        if (table.catalog() != null || table.schema() != null && !MAIN_SCHEMA.equalsIgnoreCase(unquote(table.schema()))
                || table.table().contains(Symbol.DOT)) {
            throw new SQLException("SQLite rebuild requires an ordinary table in the main database");
        }
        var snapshot = readTable(connection, table);
        require(
                snapshot.exists() && snapshot.primaryKey() != null,
                "A checked SQLite replacement requires a primary key");
        String name = snapshot.name();
        String source = MAIN_SCHEMA + Symbol.DOT + quote(name);
        String stage = MAIN_SCHEMA + Symbol.DOT + quote(REBUILD_PREFIX + Integer.toUnsignedString(name.hashCode(), 36));
        String definition = definition(connection, name);
        List<String> dependents = dependents(connection, name);
        String suffix;
        Map<String, String> definitions = new LinkedHashMap<>();
        List<String> constraints = new ArrayList<>();
        var start = TABLE_START.matcher(definition);
        require(
                start.find() && !definition.contains("/*") && !definition.contains("--"),
                "Unsupported SQLite table definition");
        int end = matchingParenthesis(definition, start.end() - 1);
        suffix = definition.substring(end + 1);
        require(
                !definition.toUpperCase(Locale.ROOT).contains("AUTOINCREMENT")
                        && snapshot.columns().stream().noneMatch(ColumnSnapshot::generated),
                "SQLite sequence and generated column tables require a specialized plan");
        for (String fragment : fragments(definition.substring(start.end(), end))) {
            var identifier = COLUMN_START.matcher(fragment);
            if (identifier.find()) {
                ColumnSnapshot column = snapshot.column(physicalName(identifier.group(1)));
                if (column != null) {
                    definitions.put(snapshot.actualKey(column.name()), fragment);
                    continue;
                }
            }
            constraints.add(fragment);
        }
        require(definitions.size() == snapshot.columns().size(), "SQLite column definitions could not be verified");
        List<String> order = removeDefault == null
                ? table.columns().stream().map(ColumnMeta::column).map(snapshot::key).toList()
                : snapshot.columns().stream().map(ColumnSnapshot::name).map(snapshot::actualKey).toList();
        List<String> create = new ArrayList<>();
        for (String column : order) {
            String fragment = definitions.remove(column);
            require(fragment != null, "Missing SQLite column in replacement: " + column);
            if (removeDefault != null && snapshot.actualKey(removeDefault).equals(column)) {
                fragment = removeDefault(fragment);
            }
            create.add(fragment);
        }
        require(definitions.isEmpty(), "SQLite replacement would drop an unrepresented column");
        create.addAll(constraints);
        String columns = snapshot.columns().stream().map(ColumnSnapshot::name).map(SQLite::quote)
                .collect(Collectors.joining(Symbol.COMMA));
        if (!suffix.toUpperCase(Locale.ROOT).contains(WITHOUT_ROWID)) {
            String rowId = List.of("rowid", "_rowid_", "oid").stream().filter(id -> snapshot.column(id) == null)
                    .findFirst().orElseThrow(() -> new SQLException("The hidden row identifier cannot be preserved"));
            columns = quote(rowId) + Symbol.COMMA + columns;
        }
        String copied = SELECT + Symbol.SPACE + columns + Symbol.SPACE + FROM + Symbol.SPACE + stage;
        String original = SELECT + Symbol.SPACE + columns + Symbol.SPACE + FROM + Symbol.SPACE + source;
        String literalName = stringLiteral(name);
        String expectedObjects = dependents.stream().map(this::stringLiteral).collect(Collectors.joining(Symbol.COMMA));
        String verifyObjects = dependents.isEmpty() ? Normal.EMPTY + 1
                : VERIFY_OBJECTS_SQL.formatted(literalName, expectedObjects);
        String verify = VERIFY_REBUILD_SQL.formatted(
                original,
                copied,
                source,
                stage,
                literalName,
                stringLiteral(definition),
                dependents.size(),
                verifyObjects,
                REBUILD_DEPENDENCIES_QUERY.replace(Symbol.QUESTION_MARK, literalName));
        List<String> replace = new ArrayList<>();
        replace.add(DROP_TABLE + Symbol.SPACE + source);
        replace.add(ALTER_TABLE + Symbol.SPACE + stage + Symbol.SPACE + RENAME_TO + Symbol.SPACE + quote(name));
        replace.addAll(dependents);
        diff.preparation(
                List.of(
                        CREATE_TABLE + Symbol.SPACE + stage + Symbol.PARENTHESE_LEFT + String.join(Symbol.COMMA, create)
                                + Symbol.PARENTHESE_RIGHT + suffix,
                        INSERT_INTO + Symbol.SPACE + stage + Symbol.PARENTHESE_LEFT + columns + Symbol.PARENTHESE_RIGHT
                                + Symbol.SPACE + original))
                .verification(verify).cutover(List.copyOf(replace));
    }

    /**
     * Reads the exact trusted DDL of the table being rebuilt.
     *
     * @param connection current database connection
     * @param name       physical table name
     * @return the database's original create statement
     * @throws SQLException when the table definition is unavailable
     */
    private static String definition(Connection connection, String name) throws SQLException {
        try (var query = connection.prepareStatement(TABLE_DEFINITION_QUERY)) {
            query.setString(1, name);
            try (var rows = query.executeQuery()) {
                require(rows.next() && rows.getString(1) != null, "SQLite catalogue has no table definition");
                return rows.getString(1);
            }
        }
    }

    /**
     * Captures table-owned indexes and triggers after rejecting dependencies outside the replacement.
     *
     * @param connection active database connection
     * @param name       target SQLite table
     * @return the SQL statements that restore table-owned objects
     * @throws SQLException when dependencies need a coordinated rebuild
     */
    private static List<String> dependents(Connection connection, String name) throws SQLException {
        try (var query = connection.prepareStatement(REBUILD_DEPENDENCIES_QUERY)) {
            for (int position = 1; position <= 4; position++) {
                query.setString(position, name);
            }
            try (var rows = query.executeQuery()) {
                require(!rows.next(), "SQLite foreign keys and external dependencies require a coordinated rebuild");
            }
        }
        try (var query = connection.prepareStatement(ATTACHED_OBJECTS_QUERY)) {
            query.setString(1, name);
            List<String> sql = new ArrayList<>();
            try (var rows = query.executeQuery()) {
                while (rows.next()) {
                    sql.add(rows.getString(3));
                }
            }
            return sql;
        }
    }

    /**
     * Splits the table body without changing expressions or quoted content.
     *
     * @param body contents between the table's opening and closing parentheses
     * @return intact column and constraint definitions
     * @throws SQLException when a quoted token or group is incomplete
     */
    private static List<String> fragments(String body) throws SQLException {
        String mask = topLevel(body);
        List<String> results = new ArrayList<>();
        int start = 0;
        for (int at = 0; at < mask.length(); at++) {
            if (mask.charAt(at) == Symbol.C_COMMA) {
                results.add(body.substring(start, at).strip());
                start = at + 1;
            }
        }
        results.add(body.substring(start).strip());
        return results;
    }

    /**
     * Masks quoted tokens and groups while retaining their string offsets.
     *
     * @param sql SQLite definition fragment
     * @return text with only top-level unquoted tokens visible
     * @throws SQLException when a quoted token or expression remains open
     */
    private static String topLevel(String sql) throws SQLException {
        StringBuilder mask = new StringBuilder(sql);
        for (int at = 0; at < sql.length(); at++) {
            char ch = sql.charAt(at);
            if (quoted(ch) || ch == Symbol.C_PARENTHESE_LEFT) {
                int end = quoted(ch) ? lastQuoted(sql, at) : matchingParenthesis(sql, at);
                mask.replace(at, end + 1, Symbol.SPACE.repeat(end - at + 1));
                at = end;
            }
        }
        return mask.toString();
    }

    /**
     * Removes exactly one top-level constant or expression default from a column definition.
     *
     * @param fragment current physical column definition
     * @return definition with its default clause removed
     * @throws SQLException when the default cannot be identified safely
     */
    private static String removeDefault(String fragment) throws SQLException {
        var keyword = DEFAULT_KEYWORD.matcher(topLevel(fragment));
        require(keyword.find(), "SQLite default clause was not located");
        int begin = keyword.start();
        int value = keyword.end();
        while (value < fragment.length() && Character.isWhitespace(fragment.charAt(value))) {
            value++;
        }
        require(value < fragment.length(), "SQLite default expression is incomplete");
        char first = fragment.charAt(value);
        int end;
        if (first == Symbol.C_PARENTHESE_LEFT) {
            end = matchingParenthesis(fragment, value) + 1;
        } else if (quoted(first)) {
            end = lastQuoted(fragment, value) + 1;
        } else {
            var constant = DEFAULT_CONSTANT.matcher(fragment.substring(value));
            require(constant.find(), "Unsupported SQLite default expression");
            end = value + constant.end();
        }
        return (fragment.substring(0, begin).stripTrailing() + Symbol.SPACE + fragment.substring(end).stripLeading())
                .stripTrailing();
    }

    /**
     * Finds the matching closing parenthesis of a SQL group.
     *
     * @param sql   definition fragment
     * @param start offset of the opening parenthesis
     * @return the matching closing offset
     * @throws SQLException if the expression is incomplete
     */
    private static int matchingParenthesis(String sql, int start) throws SQLException {
        int depth = 0;
        for (int at = start; at < sql.length(); at++) {
            char ch = sql.charAt(at);
            if (quoted(ch)) {
                at = lastQuoted(sql, at);
            } else if (ch == Symbol.C_PARENTHESE_LEFT) {
                depth++;
            } else if (ch == Symbol.C_PARENTHESE_RIGHT && --depth == 0) {
                return at;
            }
        }
        throw new SQLException("Incomplete SQLite table expression");
    }

    /**
     * Identifies SQLite's literal and identifier delimiter characters.
     *
     * @param ch character that may introduce a quoted token
     * @return whether the character introduces a quoted token
     */
    private static boolean quoted(char ch) {
        return ch == Symbol.C_SINGLE_QUOTE || ch == Symbol.C_DOUBLE_QUOTES || ch == Symbol.C_BACKTICK
                || ch == Symbol.C_BRACKET_LEFT;
    }

    /**
     * Reads a quoted SQLite token, accounting for doubled delimiters.
     *
     * @param sql   definition fragment
     * @param start offset of the first delimiter
     * @return offset of the final delimiter
     * @throws SQLException when the token is incomplete
     */
    private static int lastQuoted(String sql, int start) throws SQLException {
        char open = sql.charAt(start);
        char close = open == Symbol.C_BRACKET_LEFT ? Symbol.C_BRACKET_RIGHT : open;
        for (int at = start + 1; at < sql.length(); at++) {
            if (sql.charAt(at) == close) {
                if (open == Symbol.C_BRACKET_LEFT || at + 1 == sql.length() || sql.charAt(at + 1) != close) {
                    return at;
                }
                at++;
            }
        }
        throw new SQLException("Unterminated SQLite quoted token");
    }

    /**
     * Decodes one column identifier from a SQLite table definition.
     *
     * @param token stored column name with optional delimiters
     * @return actual column name
     */
    private static String physicalName(String token) {
        char open = token.charAt(0);
        if (!quoted(open)) {
            return token;
        }
        String delimiter = String.valueOf(open);
        return token.substring(1, token.length() - 1).replace(delimiter + delimiter, delimiter);
    }

    /**
     * Quotes a physical SQLite identifier.
     *
     * @param name actual database identifier
     * @return SQL identifier with escaped delimiters
     */
    private static String quote(String name) {
        return AbstractDialect.quotePhysicalIdentifier(name, Symbol.DOUBLE_QUOTES);
    }

    /**
     * Rejects a replacement when a chapter of the original structure cannot be checked.
     *
     * @param condition property required by the replacement
     * @param reason    diagnostic when the property cannot be established
     * @throws SQLException for unsupported table shapes or metadata
     */
    private static void require(boolean condition, String reason) throws SQLException {
        if (!condition) {
            throw new SQLException(reason);
        }
    }

}
