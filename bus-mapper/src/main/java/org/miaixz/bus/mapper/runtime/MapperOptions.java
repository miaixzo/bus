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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.mapper.Charter.Schema;

/**
 * Runtime mapper configuration options.
 * <p>
 * This class provides the pure Java/MyBatis configuration model shared by the starter adapter and mapper runtime
 * assembly code. It exposes lightweight resolution helpers while delegating parsing details to package-private runtime
 * infrastructure. It deliberately avoids Spring resource resolution, bean lookup, classpath scanning, and application
 * context concerns so it can live inside {@code bus-mapper}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
public class MapperOptions {

    /**
     * Constructs a new MapperOptions instance with the default schema options.
     */
    public MapperOptions() {
        // No initialization required.
    }

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
     * Packages to search for type aliases. Package separators are ",; \t\n".
     */
    private String typeAliasesPackage;

    /**
     * The superclass for filtering type aliases. If this is not specified, all classes found in
     * {@code typeAliasesPackage} will be treated as type aliases.
     */
    private Class<?> typeAliasesSuperType;

    /**
     * Packages to search for type handlers. Package separators are ",; \t\n".
     */
    private String typeHandlersPackage;

    /**
     * Indicates whether to check for the presence of the MyBatis XML configuration file.
     */
    private boolean checkConfigLocation = false;

    /**
     * The execution mode for the MyBatis session template.
     */
    private ExecutorType executorType;

    /**
     * Externalized properties for MyBatis configuration and mapper handlers.
     * <p>
     * The value supports both existing flattened keys and indexed namespace keys such as {@code namespaces[0].name},
     * {@code namespaces[0].tenant.column}, {@code namespaces[0].table.prefix}, and
     * {@code namespaces[0].schema.enabled}.
     */
    private Properties configurationProperties;

    /**
     * A {@link Configuration} object for customizing default settings. This property is not used if
     * {@link #configLocation} is specified by the starter.
     */
    private Configuration configuration;

    /**
     * General parameter settings, often used for plugins. For example, for PageContext:
     * {@code helperDialect=mysql,reasonable=true,supportMethodsArguments=true,params=count=countSql}.
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
     * Operation safety configuration used to enable or disable the unsafe SQL guard.
     */
    private OperationOptions operation;

    /**
     * Tenant configuration used to set a default tenant column and ignored tables or mappers.
     */
    private TenantOptions tenant;

    /**
     * Audit configuration used to control SQL audit logging behavior.
     */
    private AuditOptions audit;

    /**
     * Populate configuration used to control automatic create/update field population.
     */
    private PopulateOptions populate;

    /**
     * Visible configuration used to control data visibility filtering.
     */
    private VisibleOptions visible;

    /**
     * Prefix configuration used to apply a default table prefix and ignored table list.
     */
    private PrefixOptions prefix;

    /**
     * Entity schema initialization configuration used by starter-side table structure initialization.
     */
    private SchemaOptions schema = new SchemaOptions();

    /**
     * Resolves mapper configuration properties into the flat Properties contract consumed by mapper handlers.
     * <p>
     * Supports both legacy fixed-key configuration and the {@code namespaces[].name} structure.
     *
     * @param raw raw mapper configuration properties
     * @return flattened configuration properties
     */
    public static Properties resolve(Properties raw) {
        return MapperOptionsResolver.resolve(raw);
    }

    /**
     * Resolves mapper configuration properties from mapper options.
     *
     * @param options mapper options
     * @return flattened configuration properties
     */
    public static Properties resolve(MapperOptions options) {
        return MapperOptionsResolver.resolve(options);
    }

    /**
     * Resolves namespace-level schema options from mapper options.
     *
     * @param options mapper options
     * @return resolved namespace schema options keyed by namespace name
     */
    public static Map<String, SchemaOptions> resolveSchemaOptions(MapperOptions options) {
        return MapperOptionsResolver.resolveSchemaOptions(options);
    }

    /**
     * Resolves namespace-level schema options from flattened mapper properties.
     * <p>
     * The top-level schema object remains the legacy global configuration. When namespace {@code schema.*} entries or
     * {@code shared.schema.*} defaults exist, that global object becomes the default template for namespace
     * initialization.
     *
     * @param globalSchema       top-level schema options used as namespace defaults
     * @param resolvedProperties flattened mapper configuration properties
     * @return resolved namespace schema options keyed by namespace name
     */
    public static Map<String, SchemaOptions> resolveSchemaOptions(
            SchemaOptions globalSchema,
            Properties resolvedProperties) {
        return MapperOptionsResolver.resolveSchemaOptions(globalSchema, resolvedProperties);
    }

    /**
     * Operation safety options.
     * <p>
     * These options are consumed by the mapper plugin factory when creating the operation safety handler.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class OperationOptions {

        /**
         * Constructs a new OperationOptions instance.
         */
        public OperationOptions() {
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
     * Tenant options.
     * <p>
     * These options provide starter-friendly defaults for the mapper tenant handler. Runtime tenant values can still be
     * supplied by a {@code TenantProvider}.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class TenantOptions {

        /**
         * Constructs a new TenantOptions instance.
         */
        public TenantOptions() {
            // No initialization required.
        }

        /**
         * Enable/disable tenant handler (default: true).
         */
        private boolean enabled = true;

        /**
         * Tenant column name used when no provider-specific tenant configuration is supplied.
         */
        private String column = "tenant_id";

        /**
         * Tables to ignore tenant filtering. Multiple table names may be separated with commas.
         */
        private String ignore;

        /**
         * Mapper method identifiers to ignore tenant filtering. Multiple identifiers may be separated with commas.
         */
        private String ignoreMappers;

    }

    /**
     * Audit options.
     * <p>
     * These options control the mapper audit handler when no provider-specific audit configuration is supplied.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class AuditOptions {

        /**
         * Constructs a new AuditOptions instance.
         */
        public AuditOptions() {
            // No initialization required.
        }

        /**
         * Enable/disable audit handler (default: true).
         */
        private boolean enabled = true;

        /**
         * Slow SQL threshold in milliseconds. SQL taking longer than this value is treated as slow SQL.
         */
        private long slowSqlThreshold = 1000;

        /**
         * Whether to log SQL parameters alongside the SQL text.
         */
        private boolean logParameters = true;

        /**
         * Whether to log SQL results returned by mapper execution.
         */
        private boolean logResults = false;

        /**
         * Whether to log all SQL (not just slow SQL).
         */
        private boolean logAllSql = false;

        /**
         * Whether to print audit logs to the console in addition to the configured logging pipeline.
         */
        private boolean printConsole = false;

    }

    /**
     * Populate options.
     * <p>
     * These options control the mapper populate handler when no provider-specific populate configuration is supplied.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class PopulateOptions {

        /**
         * Constructs a new PopulateOptions instance.
         */
        public PopulateOptions() {
            // No initialization required.
        }

        /**
         * Enable/disable populate handler (default: true).
         */
        private boolean enabled = true;

        /**
         * Whether to populate created-time fields on insert operations.
         */
        private boolean created = true;

        /**
         * Whether to populate modified-time fields on insert and update operations.
         */
        private boolean modified = true;

        /**
         * Whether to populate creator fields on insert operations.
         */
        private boolean creator = true;

        /**
         * Whether to populate modifier fields on insert and update operations.
         */
        private boolean modifier = true;

    }

    /**
     * Visible options.
     * <p>
     * These options control the mapper visible handler when no provider-specific visible configuration is supplied.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class VisibleOptions {

        /**
         * Constructs a new VisibleOptions instance.
         */
        public VisibleOptions() {
            // No initialization required.
        }

        /**
         * Enable/disable visible handler (default: true).
         */
        private boolean enabled = true;

        /**
         * Tables to ignore visibility filtering. Multiple table names may be separated with commas.
         */
        private String ignore;

    }

    /**
     * Table prefix options.
     * <p>
     * These options control the mapper table prefix handler when no provider-specific prefix configuration is supplied.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class PrefixOptions {

        /**
         * Constructs a new PrefixOptions instance.
         */
        public PrefixOptions() {
            // No initialization required.
        }

        /**
         * Enable/disable prefix handler (default: true).
         */
        private boolean enabled = true;

        /**
         * Table prefix value applied to mapper SQL when the table is not ignored.
         */
        private String prefix;

        /**
         * Tables to ignore prefix handling. Multiple table names may be separated with commas.
         */
        private String ignore;

    }

    /**
     * Entity schema initialization options.
     * <p>
     * The options describe what schema differences may be produced or executed. Spring-specific package scanning and
     * datasource lookup stay outside this class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @Getter
    @Setter
    public static class SchemaOptions {

        /**
         * Constructs a new SchemaOptions instance.
         */
        public SchemaOptions() {
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
         * Whether to continue after a DDL error when {@link #failFast} is false.
         */
        private boolean continueOnError = false;

        /**
         * Packages to scan for classes annotated with {@link jakarta.persistence.Entity} or
         * {@link jakarta.persistence.Table}. Package scanning is performed by the starter layer.
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

}
