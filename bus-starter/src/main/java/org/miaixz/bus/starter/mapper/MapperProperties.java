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
package org.miaixz.bus.starter.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.mapper.Charter.Schema;
import org.miaixz.bus.spring.GeniusBuilder;

/**
 * Configuration properties for MyBatis Mapper.
 * <p>
 * This class provides a way to configure MyBatis and the Mapper framework through Spring Boot's property mechanism
 * (e.g., {@code application.yml}).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.MAPPER)
public class MapperProperties {

    /**
     * Constructs a new MapperProperties instance.
     */
    public MapperProperties() {
        // No initialization required.
    }

    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
    private static final Pattern NAMESPACE_INDEXED_KEY = Pattern
            .compile("^namespaces(?:\\[(\\d+)\\]|\\.(\\d+))\\.(.+)$");

    /**
     * Base packages to scan for MyBatis mapper interfaces.
     * <p>
     * You can specify more than one package by using a comma or semicolon as a separator.
     * </p>
     */
    private String[] basePackage;

    /**
     * Location of the MyBatis XML configuration file.
     */
    private String configLocation;

    /**
     * Locations of MyBatis mapper XML files.
     */
    private String[] mapperLocations;

    /**
     * Packages to search for type aliases. (Package separators are ",; \t\n")
     */
    private String typeAliasesPackage;

    /**
     * The superclass for filtering type aliases. If this is not specified, all classes found in
     * {@code typeAliasesPackage} will be treated as type aliases.
     */
    private Class<?> typeAliasesSuperType;

    /**
     * Packages to search for type handlers. (Package separators are ",; \t\n")
     */
    private String typeHandlersPackage;

    /**
     * Indicates whether to check for the presence of the MyBatis XML configuration file.
     */
    private boolean checkConfigLocation = false;

    /**
     * The execution mode for the {@link org.mybatis.spring.SqlSessionTemplate}.
     */
    private ExecutorType executorType;

    /**
     * Externalized properties for MyBatis configuration.
     */
    private Properties configurationProperties;

    /**
     * A {@link Configuration} object for customizing default settings. This property is not used if
     * {@link #configLocation} is specified.
     */
    @NestedConfigurationProperty
    private Configuration configuration;

    /**
     * General parameter settings, often used for plugins. For example, for PageContext:
     * {@code helperDialect=mysql,reasonable=true,supportMethodsArguments=true,params=count=countSql}
     */
    private String params;

    /**
     * Automatically delimits SQL keywords in column names.
     */
    private String autoDelimitKeywords;

    /**
     * Enables pagination parameter rationalization. When enabled, if pageNum < 1, it will query page 1. If pageNum >
     * total pages, it will query the last page.
     */
    private String reasonable;

    /**
     * Whether to support passing pagination parameters through Mapper interface method arguments.
     */
    private String supportMethodsArguments;

    /**
     * Operation safety configuration (simplified configuration).
     */
    @NestedConfigurationProperty
    private OperationProperties operation;

    /**
     * Tenant configuration (simplified configuration, supports per-datasource).
     */
    @NestedConfigurationProperty
    private TenantProperties tenant;

    /**
     * Audit configuration (simplified configuration, supports per-datasource).
     */
    @NestedConfigurationProperty
    private AuditProperties audit;

    /**
     * Populate configuration (simplified configuration, supports per-datasource).
     */
    @NestedConfigurationProperty
    private PopulateProperties populate;

    /**
     * Visible configuration (simplified configuration, supports per-datasource).
     */
    @NestedConfigurationProperty
    private VisibleProperties visible;

    /**
     * Prefix configuration (simplified configuration, supports per-datasource).
     */
    @NestedConfigurationProperty
    private PrefixProperties prefix;

    /**
     * Entity schema initialization configuration.
     */
    @NestedConfigurationProperty
    private SchemaProperties schema = new SchemaProperties();

    /**
     * Resolves mapper configuration properties into the flat Properties contract consumed by mapper handlers.
     * <p>
     * Supports both legacy fixed-key configuration and the new {@code namespaces[].name} structure.
     * </p>
     *
     * @return flattened configuration properties
     */
    public Properties resolveConfigurationProperties() {
        Properties raw = this.configurationProperties;
        Properties resolved = new Properties();
        if (raw == null || raw.isEmpty()) {
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
                throw new IllegalArgumentException(
                        "bus.mapper.configurationProperties.namespaces[" + entry.getKey() + "].name must not be empty");
            }
            if (!namespaceNames.add(namespaceName)) {
                throw new IllegalArgumentException("Duplicate mapper namespace name: " + namespaceName);
            }

            for (Map.Entry<String, String> propertyEntry : entry.getValue().entrySet()) {
                String path = propertyEntry.getKey();
                if ("name".equals(path)) {
                    continue;
                }
                resolved.setProperty(namespaceName + "." + path, propertyEntry.getValue());
            }
        }
        return resolved;
    }

    /**
     * Operation safety configuration class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class OperationProperties {

        /**
         * Constructs a new OperationProperties instance.
         */
        public OperationProperties() {
            // No initialization required.
        }

        /**
         * Enable/disable operation handler (default: true).
         */
        private boolean enabled = true;

        /**
         * Enable/disable strict mode for operation safety checks (default: true).
         */
        private boolean strictMode = true;

    }

    /**
     * Tenant configuration class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class TenantProperties {

        /**
         * Constructs a new TenantProperties instance.
         */
        public TenantProperties() {
            // No initialization required.
        }

        /**
         * Enable/disable tenant handler (default: true).
         */
        private boolean enabled = true;

        /**
         * Tenant column name.
         */
        private String column = "tenant_id";

        /**
         * Tables to ignore tenant filtering (comma-separated).
         */
        private String ignore;

        /**
         * Mappers to ignore tenant filtering (comma-separated).
         */
        private String ignoreMappers;

    }

    /**
     * Audit configuration class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class AuditProperties {

        /**
         * Constructs a new AuditProperties instance.
         */
        public AuditProperties() {
            // No initialization required.
        }

        /**
         * Enable/disable audit handler (default: true).
         */
        private boolean enabled = true;

        /**
         * Slow SQL threshold in milliseconds.
         */
        private long slowSqlThreshold = 1000;

        /**
         * Whether to log SQL parameters.
         */
        private boolean logParameters = true;

        /**
         * Whether to log SQL results.
         */
        private boolean logResults = false;

        /**
         * Whether to log all SQL (not just slow SQL).
         */
        private boolean logAllSql = false;

        /**
         * Whether to print audit logs to console.
         */
        private boolean printConsole = false;

    }

    /**
     * Populate configuration class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class PopulateProperties {

        /**
         * Constructs a new PopulateProperties instance.
         */
        public PopulateProperties() {
            // No initialization required.
        }

        /**
         * Enable/disable populate handler (default: true).
         */
        private boolean enabled = true;

        /**
         * Whether to enable created time field.
         */
        private boolean created = true;

        /**
         * Whether to enable modified time field.
         */
        private boolean modified = true;

        /**
         * Whether to enable creator field.
         */
        private boolean creator = true;

        /**
         * Whether to enable modifier field.
         */
        private boolean modifier = true;

    }

    /**
     * Visible configuration class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class VisibleProperties {

        /**
         * Constructs a new VisibleProperties instance.
         */
        public VisibleProperties() {
            // No initialization required.
        }

        /**
         * Enable/disable visible handler (default: true).
         */
        private boolean enabled = true;

        /**
         * Tables to ignore visibility filtering (comma-separated).
         */
        private String ignore;

    }

    /**
     * Prefix configuration class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class PrefixProperties {

        /**
         * Constructs a new PrefixProperties instance.
         */
        public PrefixProperties() {
            // No initialization required.
        }

        /**
         * Enable/disable prefix handler (default: true).
         */
        private boolean enabled = true;

        /**
         * Table prefix value.
         */
        private String prefix;

        /**
         * Tables to ignore prefix (comma-separated).
         */
        private String ignore;

    }

    /**
     * Entity schema initialization configuration class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class SchemaProperties {

        /**
         * Constructs a new SchemaProperties instance.
         */
        public SchemaProperties() {
            // No initialization required.
        }

        /**
         * Whether entity schema initialization is enabled.
         */
        private boolean enabled = false;

        /**
         * Schema initialization mode used to determine whether mapper reads metadata, writes scripts, validates
         * differences, creates missing tables, or updates allowed differences.
         */
        private Schema mode = Schema.NONE;

        /**
         * Whether generated DDL should be collected without execution.
         */
        private boolean dryRun = true;

        /**
         * Whether generated schema SQL should be printed through mapper logging.
         */
        private boolean printSql = true;

        /**
         * Whether to fail immediately on the first DDL error.
         */
        private boolean failFast = true;

        /**
         * Whether to continue after a DDL error when failFast is false.
         */
        private boolean continueOnError = false;

        /**
         * Packages to scan for {@link jakarta.persistence.Entity}.
         */
        private String[] entityPackages;

        /**
         * Whether missing tables may be created.
         */
        private boolean allowCreateTable = false;

        /**
         * Whether missing columns may be added to existing tables.
         */
        private boolean allowAddColumn = false;

        /**
         * Whether existing column SQL types may be changed.
         */
        private boolean allowModifyType = false;

        /**
         * Whether character column lengths may be expanded.
         */
        private boolean allowExpandLength = false;

        /**
         * Whether character column lengths may be shrunk.
         */
        private boolean allowShrinkLength = false;

        /**
         * Whether numeric precision or scale may be expanded.
         */
        private boolean allowExpandDecimal = false;

        /**
         * Whether numeric precision or scale may be shrunk.
         */
        private boolean allowShrinkDecimal = false;

        /**
         * Whether column nullable constraints may be changed.
         */
        private boolean allowModifyNullable = false;

        /**
         * Whether unmapped database columns may be dropped.
         */
        private boolean allowDropColumn = false;

        /**
         * Whether configured column rename mappings may be executed.
         */
        private boolean allowRenameColumn = false;

        /**
         * Whether missing normal indexes may be created.
         */
        private boolean allowCreateIndex = false;

        /**
         * Whether unmapped normal indexes may be dropped.
         */
        private boolean allowDropIndex = false;

        /**
         * Whether missing unique indexes or unique constraints may be created.
         */
        private boolean allowCreateUnique = false;

        /**
         * Whether unmapped unique indexes or unique constraints may be dropped.
         */
        private boolean allowDropUnique = false;

        /**
         * Whether missing primary key constraints may be created.
         */
        private boolean allowCreatePrimaryKey = false;

        /**
         * Whether unmapped database primary key constraints may be dropped.
         */
        private boolean allowDropPrimaryKey = false;

        /**
         * Whether missing foreign key constraints may be created.
         */
        private boolean allowCreateForeignKey = false;

        /**
         * Whether unmapped database foreign key constraints may be dropped.
         */
        private boolean allowDropForeignKey = false;

        /**
         * Whether dangerous schema changes may be executed after the specific operation flag and whitelist also match.
         */
        private boolean allowDangerous = false;

        /**
         * Output path used by {@link Schema#SCRIPT} mode to write generated schema SQL.
         */
        private String scriptLocation = Normal.EMPTY;

        /**
         * Datasource bean name used for schema initialization when it must target a datasource different from the
         * primary datasource.
         */
        private String datasourceKey = Normal.EMPTY;

        /**
         * Whitelist of dangerous schema change keys that may run when {@link #allowDangerous} is enabled.
         */
        private Set<String> dangerousWhitelist = new HashSet<>();

        /**
         * Table names explicitly included in schema initialization. Empty means all discovered tables are eligible
         * unless excluded.
         */
        private Set<String> includeTables = new HashSet<>();

        /**
         * Table names excluded from schema initialization.
         */
        private Set<String> excludeTables = new HashSet<>();

        /**
         * Entity class names explicitly included in schema initialization. Empty means all discovered entities are
         * eligible unless excluded.
         */
        private Set<String> includeEntities = new HashSet<>();

        /**
         * Entity class names excluded from schema initialization.
         */
        private Set<String> excludeEntities = new HashSet<>();

        /**
         * Column rename mappings used when {@link #allowRenameColumn} is enabled.
         */
        private Map<String, String> renameMappings = new LinkedHashMap<>();
    }

    /**
     * Resolves the mapper locations specified in the properties into an array of {@link Resource} objects.
     *
     * @return An array of resolved {@link Resource} objects.
     */
    public Resource[] resolveMapperLocations() {
        List<Resource> resources = new ArrayList<>();
        if (this.mapperLocations != null) {
            for (String mapperLocation : this.mapperLocations) {
                resources.addAll(Arrays.asList(getResources(mapperLocation)));
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    /**
     * Retrieves resources from a given location pattern.
     *
     * @param location The location pattern (e.g., "classpath*:com/my/mapper/*.xml").
     * @return An array of {@link Resource} objects.
     */
    private Resource[] getResources(String location) {
        try {
            return resourceResolver.getResources(location);
        } catch (IOException e) {
            return new Resource[0];
        }
    }

}
