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
package org.miaixz.bus.mapper.feature.identifier;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.MapperException;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Order;
import org.miaixz.bus.mapper.feature.affix.AffixRuleConfig;
import org.miaixz.bus.mapper.feature.paging.Sort;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.ForeignKeyMeta;
import org.miaixz.bus.mapper.parsing.IndexMeta;
import org.miaixz.bus.mapper.parsing.MapperFactory;
import org.miaixz.bus.mapper.parsing.PrimaryKeyMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;
import org.miaixz.bus.mapper.runtime.MapperOptions;

/**
 * Default-enabled physical SQL identifier validator.
 *
 * <p>
 * Validation may be disabled globally or for an individual datasource namespace. The validator loads database reserved
 * words and quote rules lazily from JDBC metadata, validates entity metadata during application startup, and validates
 * pagination sort identifiers before SQL assembly. All physical identifier syntax, quoting, and reserved-word
 * compliance rules are owned here. The validator never changes, quotes, or renames identifiers.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class IdentifierValidator {

    /**
     * Portable unquoted SQL identifier syntax.
     */
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    /**
     * SQL standard reserved words excluded from {@link DatabaseMetaData#getSQLKeywords()}, whose JDBC contract reports
     * database-specific words outside the SQL standard.
     */
    private static final Set<String> STANDARD_RESERVED_WORDS = words(
            "ADD",
            "ALL",
            "ALTER",
            "AND",
            "ANY",
            "AS",
            "ASC",
            "AUTHORIZATION",
            "BETWEEN",
            "BOTH",
            "BY",
            "CASE",
            "CAST",
            "CHECK",
            "COLUMN",
            "CONSTRAINT",
            "CREATE",
            "CROSS",
            "CURRENT_DATE",
            "CURRENT_TIME",
            "CURRENT_TIMESTAMP",
            "CURRENT_USER",
            "DEFAULT",
            "DELETE",
            "DESC",
            "DISTINCT",
            "DROP",
            "ELSE",
            "END",
            "ESCAPE",
            "EXCEPT",
            "EXISTS",
            "FALSE",
            "FETCH",
            "FOR",
            "FOREIGN",
            "FROM",
            "FULL",
            "GRANT",
            "GROUP",
            "HAVING",
            "IN",
            "INNER",
            "INSERT",
            "INTERSECT",
            "INTO",
            "IS",
            "JOIN",
            "LEADING",
            "LEFT",
            "LIKE",
            "NATURAL",
            "NOT",
            "NULL",
            "ON",
            "OR",
            "ORDER",
            "OUTER",
            "PRIMARY",
            "REFERENCES",
            "RIGHT",
            "SELECT",
            "SET",
            "SOME",
            "TABLE",
            "THEN",
            "TO",
            "TRAILING",
            "TRUE",
            "UNION",
            "UNIQUE",
            "UPDATE",
            "USER",
            "USING",
            "VALUES",
            "WHEN",
            "WHERE",
            "WITH");

    /**
     * PostgreSQL query returning only fully reserved and type/function-reserved words.
     *
     * <p>
     * {@link DatabaseMetaData#getSQLKeywords()} cannot be used for this purpose because PostgreSQL exposes unreserved
     * and context-sensitive keywords through that JDBC method as well.
     * </p>
     */
    private static final String POSTGRESQL_RESERVED_WORDS_SQL = "SELECT word FROM pg_get_keywords() WHERE catcode IN ('R', 'T')";

    /**
     * Flattened identifier validation configuration.
     */
    private final Properties properties;

    /**
     * JDBC identifier policies cached for this validator lifecycle.
     */
    private final ConcurrentMap<DatabaseKey, Policy> policies = new ConcurrentHashMap<>();

    /**
     * Creates an identifier validator from a defensive property copy.
     *
     * @param properties flattened Mapper configuration
     */
    public IdentifierValidator(Properties properties) {
        this.properties = new Properties();
        if (properties != null) {
            this.properties.putAll(properties);
        }
    }

    /**
     * Creates the effective validator for Mapper options.
     *
     * <p>
     * Validation is enabled when no identifier setting is present. The method returns {@code null} only when the
     * default scope and every declared datasource namespace explicitly resolve to disabled.
     * </p>
     *
     * @param options Mapper runtime options
     * @return configured validator, or {@code null} when every effective scope is disabled
     */
    public static IdentifierValidator create(MapperOptions options) {
        if (options == null) {
            return null;
        }
        Properties properties = MapperOptions.resolve(options);
        MapperOptions.IdentifierOptions identifier = options.getIdentifier();
        if (identifier != null) {
            properties.setProperty(
                    Args.SHARED_KEY + Symbol.DOT + Args.IDENTIFIER_KEY + Symbol.DOT + Args.PROP_ENABLED,
                    Boolean.toString(identifier.isEnabled()));
        }
        return required(properties) ? new IdentifierValidator(properties) : null;
    }

    /**
     * Creates the default enabled validator used by direct {@code PageHandler} construction.
     *
     * @return default enabled validator
     */
    public static IdentifierValidator createDefault() {
        return new IdentifierValidator(null);
    }

    /**
     * Tests whether identifier validation is enabled for a datasource namespace.
     *
     * @param datasourceKey datasource namespace
     * @return {@code true} when validation is enabled
     */
    public boolean isEnabled(String datasourceKey) {
        return enabled(properties, datasourceKey);
    }

    /**
     * Validates physical identifiers resolved from Mapper entity metadata.
     *
     * @param dataSource      datasource used to load database rules
     * @param datasourceKey   datasource namespace
     * @param entityClasses   Mapper entity classes
     * @param affixRuleConfig effective physical table-name affix rules
     * @throws MapperException when JDBC metadata is unavailable or any resolved physical identifier violates the
     *                         effective database rules
     */
    public void validate(
            DataSource dataSource,
            String datasourceKey,
            Collection<Class<?>> entityClasses,
            AffixRuleConfig affixRuleConfig) {
        if (!isEnabled(datasourceKey)) {
            return;
        }
        if (dataSource == null) {
            throw new MapperException("Mapper identifier validation failed: datasource is unavailable");
        }
        try (Connection connection = dataSource.getConnection()) {
            Policy policy = policy(connection);
            List<Entry> entries = new ArrayList<>();
            if (entityClasses != null) {
                for (Class<?> entityClass : entityClasses) {
                    if (entityClass != null) {
                        entries.addAll(entries(MapperFactory.of(entityClass), affixRuleConfig));
                    }
                }
            }
            inspect(policy, datasourceKey, entries);
        } catch (SQLException exception) {
            throw new MapperException(
                    "Mapper identifier validation failed: unable to resolve database identifier rules for datasource="
                            + display(datasourceKey),
                    exception);
        }
    }

    /**
     * Validates pagination sort identifiers using the current MyBatis connection.
     *
     * @param connection    MyBatis-managed connection
     * @param datasourceKey datasource namespace
     * @param sort          requested sort specification
     * @throws MapperException when JDBC metadata is unavailable or any sort identifier violates the effective database
     *                         rules
     */
    public void validateSort(Connection connection, String datasourceKey, Sort sort) {
        if (!isEnabled(datasourceKey) || sort == null || !sort.isSorted()) {
            return;
        }
        if (connection == null) {
            throw new MapperException("Mapper sort identifier validation failed: JDBC connection is unavailable");
        }
        try {
            Policy policy = policy(connection);
            List<Entry> entries = new ArrayList<>();
            for (Order order : sort.getOrders()) {
                String property = order == null ? null : order.getProperty();
                entries.add(new Entry("SORT", null, property, null, property, property, property, "value", true));
            }
            inspect(policy, datasourceKey, entries);
        } catch (SQLException exception) {
            throw new MapperException(
                    "Mapper sort identifier validation failed: unable to resolve database identifier rules for datasource="
                            + display(datasourceKey),
                    exception);
        }
    }

    /**
     * Determines whether any effective scope requires the validator.
     */
    private static boolean required(Properties properties) {
        if (enabled(properties, Normal.DEFAULT)) {
            return true;
        }
        for (String namespace : MapperOptions.resolveNamespaceNames(properties)) {
            if (enabled(properties, namespace)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves enablement with namespace, shared, default, then enabled-default precedence.
     */
    private static boolean enabled(Properties properties, String datasourceKey) {
        if (properties == null) {
            return true;
        }
        String key = datasourceKey == null || datasourceKey.isBlank() ? Normal.DEFAULT : datasourceKey;
        String suffix = Symbol.DOT + Args.IDENTIFIER_KEY + Symbol.DOT + Args.PROP_ENABLED;
        String value = properties.getProperty(key + suffix);
        if (value == null) {
            value = properties.getProperty(Args.SHARED_KEY + suffix);
        }
        if (value == null) {
            value = properties.getProperty(Normal.DEFAULT + suffix, Boolean.TRUE.toString());
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * Resolves and caches the database identifier policy for a connection.
     */
    private Policy policy(Connection connection) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        DatabaseKey key = new DatabaseKey(value(metadata.getURL()), value(metadata.getDatabaseProductName()),
                value(metadata.getDatabaseProductVersion()));
        Policy cached = policies.get(key);
        if (cached != null) {
            return cached;
        }
        Policy resolved = policy(connection, metadata, key);
        Policy previous = policies.putIfAbsent(key, resolved);
        return previous == null ? resolved : previous;
    }

    /**
     * Builds one immutable policy from JDBC metadata and database-native keyword classification when available.
     */
    private Policy policy(Connection connection, DatabaseMetaData metadata, DatabaseKey key) throws SQLException {
        Set<String> reservedWords = new LinkedHashSet<>(STANDARD_RESERVED_WORDS);
        String product = key.product().toLowerCase(Locale.ROOT);
        if (product.contains("postgresql")) {
            addPostgreSqlReservedWords(connection, reservedWords);
        } else {
            addJdbcKeywords(metadata, reservedWords);
        }
        List<Quote> quotes = new ArrayList<>();
        String quote = metadata.getIdentifierQuoteString();
        if (quote != null && !quote.isBlank()) {
            addQuote(quotes, new Quote(quote, quote, quote + quote));
        }
        if (product.contains("sql server")) {
            addQuote(
                    quotes,
                    new Quote(Symbol.BRACKET_LEFT, Symbol.BRACKET_RIGHT, Symbol.BRACKET_RIGHT + Symbol.BRACKET_RIGHT));
        }
        return new Policy(key.product(), Set.copyOf(reservedWords), List.copyOf(quotes));
    }

    /**
     * Adds PostgreSQL words whose server-side category requires identifier quoting.
     */
    private static void addPostgreSqlReservedWords(Connection connection, Set<String> reservedWords)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(POSTGRESQL_RESERVED_WORDS_SQL);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                addWord(reservedWords, resultSet.getString(1));
            }
        }
    }

    /**
     * Adds JDBC-reported database keywords for drivers without a more precise classification source.
     */
    private static void addJdbcKeywords(DatabaseMetaData metadata, Set<String> reservedWords) throws SQLException {
        String keywords = metadata.getSQLKeywords();
        if (keywords != null) {
            for (String keyword : keywords.split(Symbol.COMMA)) {
                addWord(reservedWords, keyword);
            }
        }
    }

    /**
     * Normalizes and adds one non-empty word.
     */
    private static void addWord(Set<String> reservedWords, String word) {
        String normalized = normalize(word);
        if (!normalized.isEmpty()) {
            reservedWords.add(normalized);
        }
    }

    /**
     * Adds a non-duplicate quote rule.
     */
    private static void addQuote(List<Quote> quotes, Quote quote) {
        if (!quotes.contains(quote)) {
            quotes.add(quote);
        }
    }

    /**
     * Converts one entity table into generic validation entries.
     */
    private List<Entry> entries(TableMeta table, AffixRuleConfig affixRuleConfig) {
        List<Entry> entries = new ArrayList<>();
        String entity = table.entityClass() == null ? null : table.entityClass().getName();
        addOptional(entries, "CATALOG", entity, table.table(), table.catalog(), "catalog", true);
        addOptional(entries, "SCHEMA", entity, table.table(), table.schema(), "schema", true);
        String physicalTable = affixed(table.table(), affixRuleConfig);
        entries.add(
                new Entry("TABLE", entity, null, physicalTable, null, physicalTable, physicalTable, "record", true));

        for (ColumnMeta column : table.columns()) {
            entries.add(
                    new Entry("COLUMN", entity, column.property(), physicalTable, column.column(), column.column(),
                            column.column(), "value", false));
        }
        for (IndexMeta index : table.indexes()) {
            String columns = joined(index.columns());
            entries.add(
                    new Entry(index.unique() ? "UNIQUE" : "INDEX", entity, properties(table, index.columns()),
                            physicalTable, columns, index.name(), physicalTable + Symbol.UNDERLINE + columns,
                            index.unique() ? "uk" : "idx", false));
            addColumnReferences(entries, table, entity, physicalTable, index.columns());
        }
        PrimaryKeyMeta primaryKey = table.primaryKey();
        if (primaryKey != null) {
            String columns = joined(primaryKey.columns());
            entries.add(
                    new Entry("PRIMARY_KEY", entity, properties(table, primaryKey.columns()), physicalTable, columns,
                            primaryKey.name(), physicalTable, "pk", false));
            addColumnReferences(entries, table, entity, physicalTable, primaryKey.columns());
        }
        for (ForeignKeyMeta foreignKey : table.foreignKeys()) {
            String columns = joined(foreignKey.columns());
            String referencedTable = affixed(foreignKey.referencedTable(), affixRuleConfig);
            entries.add(
                    new Entry("FOREIGN_KEY", entity, properties(table, foreignKey.columns()), physicalTable, columns,
                            foreignKey.name(), physicalTable + Symbol.UNDERLINE + columns, "fk", false));
            addColumnReferences(entries, table, entity, physicalTable, foreignKey.columns());
            addOptional(entries, "REFERENCED_TABLE", entity, physicalTable, referencedTable, "record", true);
            if (foreignKey.referencedColumns() != null) {
                for (String column : foreignKey.referencedColumns()) {
                    entries.add(
                            new Entry("REFERENCED_COLUMN", entity, null, referencedTable, column, column, column,
                                    "value", false));
                }
            }
        }
        return entries;
    }

    /**
     * Adds an optional qualified identifier when present.
     */
    private void addOptional(
            List<Entry> entries,
            String type,
            String entity,
            String table,
            String identifier,
            String suffix,
            boolean qualified) {
        if (identifier != null && !identifier.isBlank()) {
            entries.add(new Entry(type, entity, null, table, null, identifier, identifier, suffix, qualified));
        }
    }

    /**
     * Adds referenced columns that are not already represented by an exact mapped column.
     */
    private void addColumnReferences(
            List<Entry> entries,
            TableMeta table,
            String entity,
            String physicalTable,
            List<String> columns) {
        if (columns == null) {
            return;
        }
        for (String column : columns) {
            ColumnMeta mapped = mapped(table, column);
            if (mapped == null || mapped.column() == null || !plain(mapped.column()).equalsIgnoreCase(plain(column))) {
                entries.add(
                        new Entry("COLUMN_REFERENCE", entity, mapped == null ? null : mapped.property(), physicalTable,
                                column, column, column, "value", false));
            }
        }
    }

    /**
     * Finds a mapped column for a physical column reference.
     */
    private ColumnMeta mapped(TableMeta table, String column) {
        String target = plain(column);
        return table.columns().stream().filter(value -> plain(value.column()).equalsIgnoreCase(target)).findFirst()
                .orElse(null);
    }

    /**
     * Resolves Java properties corresponding to physical columns.
     */
    private String properties(TableMeta table, List<String> columns) {
        if (columns == null || columns.isEmpty()) {
            return null;
        }
        return columns.stream().map(column -> mapped(table, column))
                .map(
                        value -> value == null || value.property() == null || value.property().isBlank() ? Symbol.MINUS
                                : value.property())
                .collect(Collectors.joining(Symbol.COMMA));
    }

    /**
     * Applies effective affix rules while retaining an explicit table quote.
     */
    private String affixed(String table, AffixRuleConfig config) {
        if (table == null || config == null || config.getProvider() == null) {
            return table;
        }
        String prefix = config.getProvider().getPrefix();
        String suffix = config.getProvider().getSuffix();
        String simple = plain(table);
        List<String> prefixIgnore = config.getPrefixIgnore();
        List<String> suffixIgnore = config.getSuffixIgnore();
        boolean ignorePrefix = prefixIgnore != null && prefixIgnore.stream().anyMatch(simple::equalsIgnoreCase);
        boolean ignoreSuffix = suffixIgnore != null && suffixIgnore.stream().anyMatch(simple::equalsIgnoreCase);
        if ((prefix == null || prefix.isBlank() || ignorePrefix)
                && (suffix == null || suffix.isBlank() || ignoreSuffix)) {
            return table;
        }
        String physical = (prefix == null || prefix.isBlank() || ignorePrefix || simple.startsWith(prefix)
                ? Normal.EMPTY
                : prefix) + simple
                + (suffix == null || suffix.isBlank() || ignoreSuffix || simple.endsWith(suffix) ? Normal.EMPTY
                        : suffix);
        if (table.length() > 1 && ((table.startsWith(Symbol.DOUBLE_QUOTES) && table.endsWith(Symbol.DOUBLE_QUOTES))
                || (table.startsWith(Symbol.BACKTICK) && table.endsWith(Symbol.BACKTICK))
                || (table.startsWith(Symbol.BRACKET_LEFT) && table.endsWith(Symbol.BRACKET_RIGHT)))) {
            return table.substring(0, 1) + physical + table.substring(table.length() - 1);
        }
        return physical;
    }

    /**
     * Validates all entries and reports every violation together.
     */
    private void inspect(Policy policy, String datasourceKey, Collection<Entry> entries) {
        List<String> violations = new ArrayList<>();
        if (entries != null) {
            for (Entry entry : entries) {
                if (entry == null) {
                    continue;
                }
                List<String> parts = entry.qualified() ? split(policy, entry.identifier())
                        : java.util.Collections.singletonList(entry.identifier());
                for (String part : parts) {
                    String reason = violation(policy, part);
                    if (reason != null) {
                        violations.add(message(entry, part, reason, suggestion(policy, entry)));
                    }
                }
            }
        }
        if (!violations.isEmpty()) {
            throw new MapperException("Mapper identifier validation failed: datasource=" + display(datasourceKey)
                    + ", database=" + display(policy.database()) + ", violations=" + violations.size()
                    + System.lineSeparator() + String.join(System.lineSeparator(), violations));
        }
    }

    /**
     * Returns a violation reason, or {@code null} when the identifier is valid.
     */
    private String violation(Policy policy, String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return "identifier is blank";
        }
        String value = identifier.trim();
        for (Quote quote : policy.quotes()) {
            boolean starts = value.startsWith(quote.start());
            boolean ends = value.endsWith(quote.end());
            if (starts || ends) {
                if (!starts || !ends || value.length() <= quote.start().length() + quote.end().length()) {
                    return "identifier quote is not closed or is empty";
                }
                String content = value.substring(quote.start().length(), value.length() - quote.end().length());
                return escaped(content, quote) ? null : "identifier contains an unescaped quote";
            }
        }
        if (!IDENTIFIER_PATTERN.matcher(value).matches()) {
            return "identifier contains unsupported characters or delimiters";
        }
        return policy.reservedWords().contains(normalize(value)) ? "reserved word" : null;
    }

    /**
     * Tests quote escaping inside an explicitly quoted identifier.
     */
    private boolean escaped(String content, Quote quote) {
        int index = 0;
        while (index < content.length()) {
            int end = content.indexOf(quote.end(), index);
            if (end < 0) {
                return true;
            }
            if (!content.startsWith(quote.escapedEnd(), end)) {
                return false;
            }
            index = end + quote.escapedEnd().length();
        }
        return true;
    }

    /**
     * Splits a qualified identifier without splitting quoted content.
     */
    private List<String> split(Policy policy, String identifier) {
        if (identifier == null) {
            return java.util.Collections.singletonList(null);
        }
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        Quote active = null;
        for (int index = 0; index < identifier.length(); index++) {
            if (active == null) {
                Quote opening = opening(policy, identifier, index);
                if (opening != null) {
                    active = opening;
                    current.append(opening.start());
                    index += opening.start().length() - 1;
                } else if (identifier.charAt(index) == Symbol.C_DOT) {
                    parts.add(current.toString());
                    current.setLength(0);
                } else {
                    current.append(identifier.charAt(index));
                }
            } else if (identifier.startsWith(active.escapedEnd(), index)) {
                current.append(active.escapedEnd());
                index += active.escapedEnd().length() - 1;
            } else if (identifier.startsWith(active.end(), index)) {
                current.append(active.end());
                index += active.end().length() - 1;
                active = null;
            } else {
                current.append(identifier.charAt(index));
            }
        }
        parts.add(current.toString());
        return parts;
    }

    /**
     * Finds a quote rule opening at an identifier position.
     */
    private Quote opening(Policy policy, String identifier, int index) {
        return policy.quotes().stream().filter(quote -> identifier.startsWith(quote.start(), index)).findFirst()
                .orElse(null);
    }

    /**
     * Builds one stable replacement suggestion without applying it.
     */
    private String suggestion(Policy policy, Entry entry) {
        String value = plain(entry.candidate()).replaceAll("[^A-Za-z0-9_]+", Symbol.UNDERLINE)
                .replaceAll("_+", Symbol.UNDERLINE).replaceAll("^_+|_+$", Normal.EMPTY).toLowerCase(Locale.ROOT);
        if (value.isBlank() || Character.isDigit(value.charAt(0))) {
            value = "mapper" + Symbol.UNDERLINE + value;
        }
        if (!IDENTIFIER_PATTERN.matcher(value).matches() || policy.reservedWords().contains(normalize(value))) {
            value += Symbol.UNDERLINE + entry.suffix();
        }
        return value;
    }

    /**
     * Builds one detailed diagnostic line.
     */
    private String message(Entry entry, String identifier, String reason, String suggestion) {
        return "type=" + entry.type() + ", entity=" + display(entry.entity()) + ", property="
                + display(entry.property()) + ", table=" + display(entry.table()) + ", column="
                + display(entry.column()) + ", identifier=" + display(identifier) + ", reason=" + reason
                + ", suggestion=" + suggestion;
    }

    /**
     * Removes qualification and common explicit quotes for comparisons and suggestions.
     */
    private static String plain(String identifier) {
        if (identifier == null) {
            return Normal.EMPTY;
        }
        String value = identifier.trim();
        int dot = value.lastIndexOf(Symbol.C_DOT);
        if (dot >= 0) {
            value = value.substring(dot + 1);
        }
        if (value.length() > 1 && ((value.startsWith(Symbol.DOUBLE_QUOTES) && value.endsWith(Symbol.DOUBLE_QUOTES))
                || (value.startsWith(Symbol.BACKTICK) && value.endsWith(Symbol.BACKTICK))
                || (value.startsWith(Symbol.BRACKET_LEFT) && value.endsWith(Symbol.BRACKET_RIGHT)))) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * Joins physical columns for diagnostics.
     */
    private static String joined(List<String> columns) {
        return columns == null ? null : String.join(Symbol.COMMA, columns);
    }

    /**
     * Creates an immutable normalized word set.
     */
    private static Set<String> words(String... values) {
        Set<String> words = new LinkedHashSet<>();
        if (values != null) {
            for (String value : values) {
                String normalized = normalize(value);
                if (!normalized.isEmpty()) {
                    words.add(normalized);
                }
            }
        }
        return Set.copyOf(words);
    }

    /**
     * Normalizes a keyword.
     */
    private static String normalize(String value) {
        return value == null ? Normal.EMPTY : value.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Normalizes an optional JDBC value.
     */
    private static String value(String value) {
        return value == null ? Normal.EMPTY : value;
    }

    /**
     * Formats an optional diagnostic value.
     */
    private static String display(String value) {
        return value == null || value.isBlank() ? Symbol.MINUS : value;
    }

    /**
     * JDBC policy cache key.
     */
    private record DatabaseKey(String url, String product, String version) {
    }

    /**
     * Immutable database reserved-word and identifier-quote rules.
     */
    private record Policy(String database, Set<String> reservedWords, List<Quote> quotes) {
    }

    /**
     * One legal identifier quote pair and its escaped closing representation.
     */
    private record Quote(String start, String end, String escapedEnd) {
    }

    /**
     * Physical identifier and diagnostic context.
     */
    private record Entry(String type, String entity, String property, String table, String column, String identifier,
            String candidate, String suffix, boolean qualified) {
    }

}
