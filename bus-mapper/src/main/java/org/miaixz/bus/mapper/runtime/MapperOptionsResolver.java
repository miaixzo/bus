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
package org.miaixz.bus.mapper.runtime;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.SetKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Charter.Schema;

/**
 * Mapper options resolver.
 * <p>
 * This resolver converts starter-friendly indexed namespace configuration into the flat property contract consumed by
 * mapper handlers, and resolves higher-level options that are stored inside that property contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class MapperOptionsResolver {

    /**
     * Pattern used to flatten indexed {@code namespaces[n].*} configuration entries into the legacy
     * {@code namespace.path=value} property shape consumed by mapper handlers.
     */
    private static final Pattern NAMESPACE_INDEXED_KEY = Pattern
            .compile("^namespaces(?:\\[(\\d+)\\]|\\.(\\d+))\\.(.+)$");

    /**
     * Schema property scope name.
     */
    private static final String SCHEMA_SCOPE = "schema";

    /**
     * Marker used to detect namespace schema properties.
     */
    private static final String SCHEMA_MARKER = Symbol.DOT + SCHEMA_SCOPE + Symbol.DOT;

    /**
     * Separators accepted by set-valued mapper options.
     */
    private static final String SET_VALUE_SEPARATOR = Symbol.BRACKET_LEFT + Symbol.COMMA + Symbol.SEMICOLON + "\\s"
            + Symbol.BRACKET_RIGHT + Symbol.PLUS;

    /**
     * Prevents instantiation.
     */
    private MapperOptionsResolver() {
        // Utility class.
    }

    /**
     * Resolves mapper configuration properties into the flat Properties contract consumed by mapper handlers.
     * <p>
     * Supports both legacy fixed-key configuration and the {@code namespaces[].name} structure.
     *
     * @param raw raw mapper configuration properties
     * @return flattened configuration properties
     */
    public static Properties resolve(Properties raw) {
        Properties resolved = new Properties();
        if (MapKit.isEmpty(raw)) {
            return resolved;
        }

        Map<Integer, Map<String, String>> groupedNamespaceProperties = new TreeMap<>();
        for (String key : raw.stringPropertyNames()) {
            Matcher matcher = NAMESPACE_INDEXED_KEY.matcher(key);
            if (matcher.matches()) {
                String indexText = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                int index = Integer.parseInt(indexText);
                String path = matcher.group(3);
                groupedNamespaceProperties.computeIfAbsent(index, ignored -> new LinkedHashMap<>())
                        .put(path, raw.getProperty(key));
                continue;
            }
            resolved.setProperty(key, raw.getProperty(key));
        }

        if (groupedNamespaceProperties.isEmpty()) {
            return resolved;
        }

        Set<String> namespaceNames = new HashSet<>(groupedNamespaceProperties.size());
        for (Map.Entry<Integer, Map<String, String>> entry : groupedNamespaceProperties.entrySet()) {
            String namespaceName = StringKit.trim(entry.getValue().get("name"));
            if (StringKit.isEmpty(namespaceName)) {
                throw new IllegalArgumentException("bus.mapper.configurationProperties.namespaces" + Symbol.BRACKET_LEFT
                        + entry.getKey() + Symbol.BRACKET_RIGHT + Symbol.DOT + "name must not be empty");
            }
            if (!namespaceNames.add(namespaceName)) {
                throw new IllegalArgumentException(
                        "Duplicate mapper namespace name" + Symbol.COLON + Symbol.SPACE + namespaceName);
            }

            for (Map.Entry<String, String> propertyEntry : entry.getValue().entrySet()) {
                String path = propertyEntry.getKey();
                if ("name".equals(path)) {
                    continue;
                }
                resolved.setProperty(namespaceName + Symbol.DOT + path, propertyEntry.getValue());
            }
        }
        return resolved;
    }

    /**
     * Resolves mapper configuration properties from mapper options.
     *
     * @param options mapper options
     * @return flattened configuration properties
     */
    public static Properties resolve(MapperOptions options) {
        return resolve(options == null ? null : options.getConfigurationProperties());
    }

    /**
     * Resolves namespace-level schema options from mapper options.
     *
     * @param options mapper options
     * @return resolved namespace schema options keyed by namespace name
     */
    public static Map<String, MapperOptions.SchemaOptions> resolveSchemaOptions(MapperOptions options) {
        if (options == null) {
            return new LinkedHashMap<>();
        }
        return resolveSchemaOptions(options.getSchema(), resolve(options));
    }

    /**
     * Resolves namespace-level schema options from flattened mapper properties.
     * <p>
     * The top-level schema object remains the legacy global configuration. When namespace {@code schema.*} entries or
     * {@code shared.schema.*} defaults exist, that global object becomes the default template for namespace
     * initialization and is not run as a second standalone initialization pass by starter integration.
     *
     * @param globalSchema       top-level schema options used as namespace defaults
     * @param resolvedProperties flattened mapper configuration properties
     * @return resolved namespace schema options keyed by namespace name
     */
    public static Map<String, MapperOptions.SchemaOptions> resolveSchemaOptions(
            MapperOptions.SchemaOptions globalSchema,
            Properties resolvedProperties) {
        if (MapKit.isEmpty(resolvedProperties)) {
            return new LinkedHashMap<>();
        }
        Properties sharedSchemaProperties = new Properties();
        Map<String, Properties> schemaPropertiesByNamespace = new LinkedHashMap<>();
        Set<String> namespaceNames = new LinkedHashSet<>();
        for (String key : resolvedProperties.stringPropertyNames()) {
            String namespace = namespaceName(key);
            if (namespace != null) {
                namespaceNames.add(namespace);
            }
            int markerIndex = key.indexOf(SCHEMA_MARKER);
            if (markerIndex <= 0) {
                continue;
            }
            String namespaceName = key.substring(0, markerIndex);
            String schemaPath = key.substring(markerIndex + SCHEMA_MARKER.length());
            if (Args.SHARED_KEY.equals(namespaceName)) {
                sharedSchemaProperties.setProperty(schemaPath, resolvedProperties.getProperty(key));
                continue;
            }
            schemaPropertiesByNamespace.computeIfAbsent(namespaceName, ignored -> new Properties())
                    .setProperty(schemaPath, resolvedProperties.getProperty(key));
        }
        if (MapKit.isNotEmpty(sharedSchemaProperties)) {
            for (String namespaceName : namespaceNames) {
                schemaPropertiesByNamespace.computeIfAbsent(namespaceName, ignored -> new Properties());
            }
        }
        if (MapKit.isEmpty(schemaPropertiesByNamespace)) {
            return new LinkedHashMap<>();
        }

        MapperOptions.SchemaOptions sharedSchema = copySchemaOptions(globalSchema);
        applySchemaProperties(sharedSchema, sharedSchemaProperties);
        Map<String, MapperOptions.SchemaOptions> namespaceSchemas = new LinkedHashMap<>();
        for (Map.Entry<String, Properties> entry : schemaPropertiesByNamespace.entrySet()) {
            MapperOptions.SchemaOptions schemaOptions = copySchemaOptions(sharedSchema);
            applySchemaProperties(schemaOptions, entry.getValue());
            namespaceSchemas.put(entry.getKey(), schemaOptions);
        }
        return namespaceSchemas;
    }

    /**
     * Resolves a datasource namespace name from a flattened configuration property.
     *
     * @param key flattened configuration key
     * @return namespace name, or {@code null} when the key is not namespace-scoped
     */
    private static String namespaceName(String key) {
        int dot = key == null ? -1 : key.indexOf('.');
        if (dot <= 0) {
            return null;
        }
        String namespaceName = key.substring(0, dot);
        if (Args.SHARED_KEY.equals(namespaceName) || Args.PROVIDER_KEY.equals(namespaceName)) {
            return null;
        }
        String path = key.substring(dot + 1);
        int pathDot = path.indexOf('.');
        String scope = pathDot < 0 ? path : path.substring(0, pathDot);
        return switch (scope) {
            case Args.TABLE_KEY, Args.TENANT_KEY, Args.POPULATE_KEY, Args.VISIBLE_KEY, Args.AUDIT_KEY, SCHEMA_SCOPE -> namespaceName;
            default -> null;
        };
    }

    /**
     * Applies flattened schema properties to schema options.
     *
     * @param schemaOptions target schema options
     * @param properties    flattened schema properties without the {@code schema.} prefix
     */
    private static void applySchemaProperties(MapperOptions.SchemaOptions schemaOptions, Properties properties) {
        if (schemaOptions == null || MapKit.isEmpty(properties)) {
            return;
        }
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            if (StringKit.isEmpty(value)) {
                continue;
            }
            switch (normalizePropertyName(key)) {
                case "enabled" -> schemaOptions.setEnabled(Boolean.parseBoolean(value));
                case "mode" -> schemaOptions.setMode(Schema.valueOf(value.trim().toUpperCase(Locale.ROOT)));
                case "dryrun" -> schemaOptions.setDryRun(Boolean.parseBoolean(value));
                case "printsql" -> schemaOptions.setPrintSql(Boolean.parseBoolean(value));
                case "failfast" -> schemaOptions.setFailFast(Boolean.parseBoolean(value));
                case "continueonerror" -> schemaOptions.setContinueOnError(Boolean.parseBoolean(value));
                case "allowcreatetable" -> schemaOptions.setAllowCreateTable(Boolean.parseBoolean(value));
                case "allowaddcolumn" -> schemaOptions.setAllowAddColumn(Boolean.parseBoolean(value));
                case "allowmodifytype" -> schemaOptions.setAllowModifyType(Boolean.parseBoolean(value));
                case "allowexpandlength" -> schemaOptions.setAllowExpandLength(Boolean.parseBoolean(value));
                case "allowshrinklength" -> schemaOptions.setAllowShrinkLength(Boolean.parseBoolean(value));
                case "allowexpanddecimal" -> schemaOptions.setAllowExpandDecimal(Boolean.parseBoolean(value));
                case "allowshrinkdecimal" -> schemaOptions.setAllowShrinkDecimal(Boolean.parseBoolean(value));
                case "allowmodifynullable" -> schemaOptions.setAllowModifyNullable(Boolean.parseBoolean(value));
                case "allowdropcolumn" -> schemaOptions.setAllowDropColumn(Boolean.parseBoolean(value));
                case "allowrenamecolumn" -> schemaOptions.setAllowRenameColumn(Boolean.parseBoolean(value));
                case "allowcreateindex" -> schemaOptions.setAllowCreateIndex(Boolean.parseBoolean(value));
                case "allowdropindex" -> schemaOptions.setAllowDropIndex(Boolean.parseBoolean(value));
                case "allowcreateunique" -> schemaOptions.setAllowCreateUnique(Boolean.parseBoolean(value));
                case "allowdropunique" -> schemaOptions.setAllowDropUnique(Boolean.parseBoolean(value));
                case "allowcreateprimarykey" -> schemaOptions.setAllowCreatePrimaryKey(Boolean.parseBoolean(value));
                case "allowdropprimarykey" -> schemaOptions.setAllowDropPrimaryKey(Boolean.parseBoolean(value));
                case "allowcreateforeignkey" -> schemaOptions.setAllowCreateForeignKey(Boolean.parseBoolean(value));
                case "allowdropforeignkey" -> schemaOptions.setAllowDropForeignKey(Boolean.parseBoolean(value));
                case "allowdangerous" -> schemaOptions.setAllowDangerous(Boolean.parseBoolean(value));
                case "scriptlocation" -> schemaOptions.setScriptLocation(StringKit.trim(value));
                case "datasourcekey" -> schemaOptions.setDatasourceKey(StringKit.trim(value));
                default -> {
                    // Collection and map properties are applied below.
                }
            }
        }
        Set<String> entityPackages = readValues(properties, "entityPackages", "entity-packages");
        if (entityPackages != null) {
            schemaOptions.setEntityPackages(entityPackages.toArray(new String[0]));
        }
        applySet(properties, schemaOptions::setIncludeTables, "includeTables", "include-tables");
        applySet(properties, schemaOptions::setExcludeTables, "excludeTables", "exclude-tables");
        applySet(properties, schemaOptions::setIncludeEntities, "includeEntities", "include-entities");
        applySet(properties, schemaOptions::setExcludeEntities, "excludeEntities", "exclude-entities");
        applySet(properties, schemaOptions::setDangerousWhitelist, "dangerousWhitelist", "dangerous-whitelist");
        Map<String, String> renameMappings = readMap(properties, "renameMappings", "rename-mappings");
        if (renameMappings != null) {
            schemaOptions.setRenameMappings(renameMappings);
        }
    }

    /**
     * Copies schema options so namespace overrides cannot mutate global options.
     *
     * @param source source schema options
     * @return copied schema options
     */
    private static MapperOptions.SchemaOptions copySchemaOptions(MapperOptions.SchemaOptions source) {
        MapperOptions.SchemaOptions copy = new MapperOptions.SchemaOptions();
        if (source == null) {
            return copy;
        }
        copy.setEnabled(source.isEnabled());
        copy.setMode(source.getMode());
        copy.setDryRun(source.isDryRun());
        copy.setPrintSql(source.isPrintSql());
        copy.setFailFast(source.isFailFast());
        copy.setContinueOnError(source.isContinueOnError());
        copy.setEntityPackages(
                source.getEntityPackages() == null ? null
                        : Arrays.copyOf(source.getEntityPackages(), source.getEntityPackages().length));
        copy.setAllowCreateTable(source.isAllowCreateTable());
        copy.setAllowAddColumn(source.isAllowAddColumn());
        copy.setAllowModifyType(source.isAllowModifyType());
        copy.setAllowExpandLength(source.isAllowExpandLength());
        copy.setAllowShrinkLength(source.isAllowShrinkLength());
        copy.setAllowExpandDecimal(source.isAllowExpandDecimal());
        copy.setAllowShrinkDecimal(source.isAllowShrinkDecimal());
        copy.setAllowModifyNullable(source.isAllowModifyNullable());
        copy.setAllowDropColumn(source.isAllowDropColumn());
        copy.setAllowRenameColumn(source.isAllowRenameColumn());
        copy.setAllowCreateIndex(source.isAllowCreateIndex());
        copy.setAllowDropIndex(source.isAllowDropIndex());
        copy.setAllowCreateUnique(source.isAllowCreateUnique());
        copy.setAllowDropUnique(source.isAllowDropUnique());
        copy.setAllowCreatePrimaryKey(source.isAllowCreatePrimaryKey());
        copy.setAllowDropPrimaryKey(source.isAllowDropPrimaryKey());
        copy.setAllowCreateForeignKey(source.isAllowCreateForeignKey());
        copy.setAllowDropForeignKey(source.isAllowDropForeignKey());
        copy.setAllowDangerous(source.isAllowDangerous());
        copy.setScriptLocation(source.getScriptLocation());
        copy.setDatasourceKey(source.getDatasourceKey());
        copy.setDangerousWhitelist(SetKit.of(true, source.getDangerousWhitelist()));
        copy.setIncludeTables(SetKit.of(true, source.getIncludeTables()));
        copy.setExcludeTables(SetKit.of(true, source.getExcludeTables()));
        copy.setIncludeEntities(SetKit.of(true, source.getIncludeEntities()));
        copy.setExcludeEntities(SetKit.of(true, source.getExcludeEntities()));
        copy.setRenameMappings(
                source.getRenameMappings() == null ? new LinkedHashMap<>()
                        : new LinkedHashMap<>(source.getRenameMappings()));
        return copy;
    }

    /**
     * Applies a set-valued property when present.
     *
     * @param properties source properties
     * @param consumer   target setter
     * @param aliases    property aliases
     */
    private static void applySet(Properties properties, Consumer<Set<String>> consumer, String... aliases) {
        Set<String> values = readValues(properties, aliases);
        if (values != null) {
            consumer.accept(values);
        }
    }

    /**
     * Reads a scalar or indexed collection property.
     *
     * @param properties source properties
     * @param aliases    property aliases
     * @return values, or {@code null} when none of the aliases is present
     */
    private static Set<String> readValues(Properties properties, String... aliases) {
        boolean found = false;
        Set<String> values = new LinkedHashSet<>();
        for (String key : properties.stringPropertyNames()) {
            if (!matchesCollectionProperty(key, aliases)) {
                continue;
            }
            found = true;
            String value = properties.getProperty(key);
            if (StringKit.isEmpty(value)) {
                continue;
            }
            for (String item : value.split(SET_VALUE_SEPARATOR)) {
                String trimmed = StringKit.trim(item);
                if (StringKit.isNotEmpty(trimmed)) {
                    values.add(trimmed);
                }
            }
        }
        return found ? values : null;
    }

    /**
     * Reads a map-valued property.
     *
     * @param properties source properties
     * @param aliases    property aliases
     * @return map values, or {@code null} when none of the aliases is present
     */
    private static Map<String, String> readMap(Properties properties, String... aliases) {
        Map<String, String> values = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            String mapKey = mapPropertyKey(key, aliases);
            if (mapKey != null) {
                values.put(mapKey, properties.getProperty(key));
            }
        }
        return values.isEmpty() ? null : values;
    }

    /**
     * Tests whether a property belongs to one of the collection aliases.
     *
     * @param key     property key
     * @param aliases accepted aliases
     * @return {@code true} when matched
     */
    private static boolean matchesCollectionProperty(String key, String... aliases) {
        String baseName = collectionBaseName(key);
        for (String alias : aliases) {
            if (normalizePropertyName(baseName).equals(normalizePropertyName(alias))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the map entry key for a map property.
     *
     * @param key     property key
     * @param aliases accepted aliases
     * @return map entry key, or {@code null} when unmatched
     */
    private static String mapPropertyKey(String key, String... aliases) {
        for (String alias : aliases) {
            if (key.startsWith(alias + Symbol.DOT)) {
                return key.substring(alias.length() + 1);
            }
            if (key.startsWith(alias + Symbol.BRACKET_LEFT) && key.endsWith(Symbol.BRACKET_RIGHT)) {
                return key.substring(alias.length() + 1, key.length() - 1);
            }
        }
        return null;
    }

    /**
     * Returns the base name of an indexed property.
     *
     * @param key property key
     * @return base name
     */
    private static String collectionBaseName(String key) {
        int index = key.indexOf(Symbol.C_BRACKET_LEFT);
        return index >= 0 ? key.substring(0, index) : key;
    }

    /**
     * Normalizes relaxed configuration names to one comparable form.
     *
     * @param key property key
     * @return normalized property key
     */
    private static String normalizePropertyName(String key) {
        String base = collectionBaseName(key);
        int dot = base.indexOf(Symbol.C_DOT);
        if (dot >= 0) {
            base = base.substring(0, dot);
        }
        return base.replace(Symbol.MINUS, Normal.EMPTY).replace(Symbol.UNDERLINE, Normal.EMPTY)
                .toLowerCase(Locale.ROOT);
    }

}
