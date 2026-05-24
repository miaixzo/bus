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

import java.util.*;

import javax.sql.DataSource;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Holder;
import org.miaixz.bus.mapper.builder.MapperEntityResolver;
import org.miaixz.bus.mapper.feature.audit.AuditProvider;
import org.miaixz.bus.mapper.feature.populate.PopulateProvider;
import org.miaixz.bus.mapper.feature.prefix.TablePrefixConfig;
import org.miaixz.bus.mapper.feature.prefix.TablePrefixHandler;
import org.miaixz.bus.mapper.feature.prefix.TablePrefixProvider;
import org.miaixz.bus.mapper.feature.schema.EntitySchemaInitializer;
import org.miaixz.bus.mapper.feature.schema.SchemaConfig;
import org.miaixz.bus.mapper.feature.schema.SchemaProvider;
import org.miaixz.bus.mapper.feature.schema.SchemaReport;
import org.miaixz.bus.mapper.feature.tenant.TenantProvider;
import org.miaixz.bus.mapper.feature.visible.VisibleProvider;
import org.miaixz.bus.mapper.handler.MybatisInterceptor;
import org.miaixz.bus.mapper.runtime.MapperOptions;
import org.miaixz.bus.mapper.runtime.MapperPluginFactory;
import org.miaixz.bus.mapper.runtime.MapperPluginProviders;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.spring.SpringBuilder;
import org.miaixz.bus.spring.annotation.PlaceHolderBinder;

/**
 * Starter adapter for creating mapper plugins and coordinating mapper schema initialization.
 * <p>
 * Pure plugin-chain construction lives in {@link MapperPluginFactory}. Spring-specific work such as property binding,
 * provider lookup, datasource lookup and package scanning stays here so {@link MapperConfiguration} only declares
 * Spring beans.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MapperPluginBuilder {

    /**
     * MapperFactoryBean property that stores the mapper interface class.
     */
    private static final String MAPPER_INTERFACE_PROPERTY = "mapperInterface";

    /**
     * Delimiter pattern for package configuration values.
     */
    private static final String PACKAGE_SPLIT_PATTERN = "[,;\\s]+";

    /**
     * Constructs a new MapperPluginBuilder instance.
     */
    public MapperPluginBuilder() {
        // No initialization required.
    }

    /**
     * Builds the primary mapper interceptor from the Spring environment.
     * <p>
     * Spring-specific work stops at property binding and provider lookup. The actual handler chain is delegated to
     * {@link MapperPluginFactory} so the mapper module owns plugin assembly without depending on Spring.
     *
     * @param environment Spring environment
     * @return configured interceptor
     */
    public static MybatisInterceptor build(Environment environment) {
        if (environment == null) {
            return MapperPluginFactory.build(null);
        }
        MapperProperties properties = PlaceHolderBinder.bind(environment, MapperProperties.class, GeniusBuilder.MAPPER);
        return build(properties);
    }

    /**
     * Builds the primary mapper interceptor from mapper properties.
     *
     * @param properties mapper properties
     * @return configured interceptor
     */
    public static MybatisInterceptor build(MapperProperties properties) {
        MapperProperties mapperProperties = properties == null ? new MapperProperties() : properties;
        return build(mapperProperties, resolvePluginProviders(mapperProperties));
    }

    /**
     * Builds the primary mapper interceptor from mapper properties and provider instances.
     *
     * @param properties mapper properties
     * @param providers  provider holder
     * @return configured interceptor
     */
    private static MybatisInterceptor build(MapperProperties properties, MapperPluginProviders providers) {
        return MapperPluginFactory.build(properties, providers);
    }

    /**
     * Configures mapper plugins on the MyBatis session factory bean.
     * <p>
     * The interceptor is attached to the factory first, then mapper schema initialization is executed when global,
     * namespace, or provider configuration enables it.
     *
     * @param factory        MyBatis session factory bean
     * @param properties     mapper properties
     * @param environment    Spring environment used by package scanning
     * @param resourceLoader Spring resource loader used by package scanning
     * @param dataSource     primary datasource
     * @param beanFactory    bean factory used to discover mapper definitions
     * @throws Exception if schema initialization fails
     */
    public static void configureSqlSessionFactory(
            SqlSessionFactoryBean factory,
            MapperProperties properties,
            Environment environment,
            ResourceLoader resourceLoader,
            DataSource dataSource,
            ConfigurableListableBeanFactory beanFactory) throws Exception {
        MapperProperties mapperProperties = properties == null ? new MapperProperties() : properties;
        MapperPluginProviders mapperProviders = resolvePluginProviders(mapperProperties);
        if (factory != null) {
            factory.setPlugins(build(mapperProperties, mapperProviders));
        }
        initializeSchemaIfNecessary(
                mapperProperties,
                mapperProviders,
                environment,
                resourceLoader,
                dataSource,
                beanFactory);
    }

    /**
     * Resolves optional provider beans needed by mapper plugins.
     * <p>
     * Handler providers are queried when the matching simplified configuration exists or when legacy flattened handler
     * configuration is present. The schema provider is kept in the same holder so schema initialization follows the
     * same extension model as the other mapper plugins and can be enabled by provider configuration.
     *
     * @param properties mapper properties bound from the Spring environment
     * @return provider holder passed to the pure mapper plugin factory
     */
    private static MapperPluginProviders resolvePluginProviders(MapperProperties properties) {
        MapperPluginProviders providers = new MapperPluginProviders();
        if (properties == null) {
            return providers;
        }
        Properties resolved = MapperOptions.resolve(properties);
        boolean hasConfigFile = resolved != null && !resolved.isEmpty();
        if (properties.getTenant() != null || hasConfigFile) {
            providers.setTenantProvider(provider(TenantProvider.class));
        }
        if (properties.getPrefix() != null || hasConfigFile) {
            providers.setPrefixProvider(provider(TablePrefixProvider.class));
        }
        if (properties.getVisible() != null || hasConfigFile) {
            providers.setVisibleProvider(provider(VisibleProvider.class));
        }
        if (properties.getPopulate() != null || hasConfigFile) {
            providers.setPopulateProvider(provider(PopulateProvider.class));
        }
        if (properties.getAudit() != null || hasConfigFile) {
            providers.setAuditProvider(provider(AuditProvider.class));
        }
        providers.setSchemaProvider(provider(SchemaProvider.class));
        return providers;
    }

    /**
     * Resolves the effective table prefix configuration for schema initialization.
     * <p>
     * Prefix configuration follows the same provider and property resolution path used by the table prefix plugin, so
     * schema DDL generation observes both global and datasource-specific table prefix settings.
     *
     * @param properties         mapper properties
     * @param providers          provider holder
     * @param datasourceKey      datasource key
     * @param resolvedProperties flattened mapper configuration properties
     * @return table prefix configuration, or {@code null}
     */
    private static TablePrefixConfig resolveTablePrefixConfig(
            MapperProperties properties,
            MapperPluginProviders providers,
            String datasourceKey,
            Properties resolvedProperties) {
        if (properties == null) {
            return null;
        }
        MapperOptions.PrefixOptions prefixOptions = properties.getPrefix();
        if (prefixOptions != null && !prefixOptions.isEnabled()) {
            return null;
        }
        TablePrefixProvider provider = providers == null ? null : providers.getPrefixProvider();
        if (provider != null) {
            TablePrefixConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                return providerConfig;
            }
        }
        Properties prefixProperties = new Properties();
        if (resolvedProperties != null) {
            prefixProperties.putAll(resolvedProperties);
        }
        if (prefixOptions != null) {
            String sharedPrefix = Args.SHARED_KEY + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT;
            String defaultPrefix = Holder.getDefault() + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT;
            if (StringKit.isNotEmpty(prefixOptions.getPrefix())
                    && !prefixProperties.containsKey(sharedPrefix + Args.TABLE_PREFIX)) {
                prefixProperties.setProperty(sharedPrefix + Args.TABLE_PREFIX, prefixOptions.getPrefix());
            }
            if (StringKit.isNotEmpty(prefixOptions.getPrefix())
                    && !prefixProperties.containsKey(defaultPrefix + Args.TABLE_PREFIX)) {
                prefixProperties.setProperty(defaultPrefix + Args.TABLE_PREFIX, prefixOptions.getPrefix());
            }
            if (StringKit.isNotEmpty(prefixOptions.getIgnore())
                    && !prefixProperties.containsKey(sharedPrefix + Args.PROP_IGNORE)) {
                prefixProperties.setProperty(sharedPrefix + Args.PROP_IGNORE, prefixOptions.getIgnore());
            }
            if (StringKit.isNotEmpty(prefixOptions.getIgnore())
                    && !prefixProperties.containsKey(defaultPrefix + Args.PROP_IGNORE)) {
                prefixProperties.setProperty(defaultPrefix + Args.PROP_IGNORE, prefixOptions.getIgnore());
            }
        }
        String key = StringKit.isNotEmpty(datasourceKey) ? datasourceKey : Holder.getDefault();
        return TablePrefixHandler.resolveConfig(key, prefixProperties, provider);
    }

    /**
     * Initializes mapper entity schema metadata when schema initialization is enabled.
     * <p>
     * Namespace schema settings take precedence over the legacy global schema block. When no namespace schema settings
     * exist, the global schema block or an enabled {@link SchemaProvider} can still trigger a single initialization
     * pass.
     *
     * @param properties     mapper properties
     * @param providers      provider holder
     * @param environment    Spring environment used by package scanning
     * @param resourceLoader Spring resource loader used by package scanning
     * @param dataSource     primary datasource
     * @param beanFactory    bean factory used to discover mapper definitions
     * @throws Exception if schema initialization fails
     */
    private static void initializeSchemaIfNecessary(
            MapperProperties properties,
            MapperPluginProviders providers,
            Environment environment,
            ResourceLoader resourceLoader,
            DataSource dataSource,
            ConfigurableListableBeanFactory beanFactory) throws Exception {
        Properties resolvedProperties = MapperOptions.resolve(properties);
        MapperOptions.SchemaOptions schemaProperties = properties == null ? null : properties.getSchema();
        SchemaProvider schemaProvider = providers == null ? null : providers.getSchemaProvider();
        Map<String, MapperOptions.SchemaOptions> namespaceSchemas = MapperOptions
                .resolveSchemaOptions(schemaProperties, resolvedProperties);
        if (!namespaceSchemas.isEmpty()) {
            initializeNamespacedSchemas(
                    properties,
                    providers,
                    schemaProvider,
                    environment,
                    resourceLoader,
                    dataSource,
                    beanFactory,
                    resolvedProperties,
                    namespaceSchemas);
            return;
        }

        SchemaConfig schemaConfig = resolveEffectiveSchemaConfig(schemaProvider, schemaProperties, null);
        if (schemaConfig == null || !schemaConfig.enabled()) {
            return;
        }
        ResolvedSchemaDataSource schemaDataSource = resolveSchemaInitializationTarget(
                dataSource,
                beanFactory,
                null,
                schemaConfig);
        runSchemaInitialization(
                properties,
                providers,
                schemaProvider,
                environment,
                resourceLoader,
                schemaDataSource.dataSource(),
                beanFactory,
                null,
                schemaDataSource.holderKey(),
                resolvedProperties,
                schemaProperties,
                schemaConfig);
    }

    /**
     * Runs schema initialization for every datasource namespace that declares effective schema configuration.
     *
     * @param properties         mapper properties
     * @param providers          provider holder
     * @param schemaProvider     schema provider
     * @param environment        Spring environment used by package scanning
     * @param resourceLoader     Spring resource loader used by package scanning
     * @param primaryDataSource  primary datasource injected into mapper auto-configuration
     * @param beanFactory        bean factory used for named datasource lookup
     * @param resolvedProperties flattened mapper configuration properties
     * @param namespaceSchemas   namespace schema configurations keyed by namespace name
     * @throws Exception if schema initialization fails
     */
    private static void initializeNamespacedSchemas(
            MapperProperties properties,
            MapperPluginProviders providers,
            SchemaProvider schemaProvider,
            Environment environment,
            ResourceLoader resourceLoader,
            DataSource primaryDataSource,
            ConfigurableListableBeanFactory beanFactory,
            Properties resolvedProperties,
            Map<String, MapperOptions.SchemaOptions> namespaceSchemas) throws Exception {
        for (Map.Entry<String, MapperOptions.SchemaOptions> namespaceSchema : namespaceSchemas.entrySet()) {
            String namespaceName = namespaceSchema.getKey();
            MapperOptions.SchemaOptions schemaProperties = namespaceSchema.getValue();
            SchemaConfig schemaConfig = resolveEffectiveSchemaConfig(schemaProvider, schemaProperties, namespaceName);
            if (schemaConfig == null || !schemaConfig.enabled()) {
                Logger.info(
                        true,
                        "Starter",
                        "Mapper namespace schema initialization skipped: namespace={}, reason={}",
                        namespaceName,
                        "disabled");
                continue;
            }
            ResolvedSchemaDataSource schemaDataSource = resolveSchemaInitializationTarget(
                    primaryDataSource,
                    beanFactory,
                    namespaceName,
                    schemaConfig);
            runSchemaInitialization(
                    properties,
                    providers,
                    schemaProvider,
                    environment,
                    resourceLoader,
                    schemaDataSource.dataSource(),
                    beanFactory,
                    namespaceName,
                    schemaDataSource.holderKey(),
                    resolvedProperties,
                    schemaProperties,
                    schemaConfig);
        }
    }

    /**
     * Runs one schema initialization pass for a datasource target.
     * <p>
     * Entity classes are collected from mapper generic declarations, configured entity packages, and the optional
     * {@link SchemaProvider}. The datasource key is exposed through {@link Holder} while initialization runs so
     * provider lookups and table prefix resolution use the same routing context as normal mapper execution.
     *
     * @param properties         mapper properties
     * @param providers          provider holder
     * @param schemaProvider     schema provider
     * @param environment        Spring environment used by package scanning
     * @param resourceLoader     Spring resource loader used by package scanning
     * @param dataSource         datasource used for metadata reads and DDL execution
     * @param beanFactory        bean factory used to discover mapper definitions
     * @param namespaceName      namespace name, or {@code null} for the legacy global configuration
     * @param holderKey          datasource key temporarily exposed through {@link Holder}
     * @param resolvedProperties flattened mapper configuration properties
     * @param schemaProperties   schema options
     * @param schemaConfig       schema runtime configuration
     * @throws Exception if schema initialization fails
     */
    private static void runSchemaInitialization(
            MapperProperties properties,
            MapperPluginProviders providers,
            SchemaProvider schemaProvider,
            Environment environment,
            ResourceLoader resourceLoader,
            DataSource dataSource,
            ConfigurableListableBeanFactory beanFactory,
            String namespaceName,
            String holderKey,
            Properties resolvedProperties,
            MapperOptions.SchemaOptions schemaProperties,
            SchemaConfig schemaConfig) throws Exception {
        String datasourceKey = resolveSchemaDatasourceKey(schemaConfig, namespaceName);
        schemaConfig.datasourceKey(datasourceKey);
        Set<Class<?>> entityClasses = new LinkedHashSet<>();
        entityClasses.addAll(resolveMapperEntityClassesFromBeanFactory(beanFactory));
        entityClasses.addAll(scanSchemaEntityClasses(schemaProperties, environment, resourceLoader));
        entityClasses.addAll(resolveProviderEntityClasses(schemaProvider, datasourceKey));
        TablePrefixConfig tablePrefixConfig = resolveTablePrefixConfig(
                properties,
                providers,
                schemaConfig.datasourceKey(),
                resolvedProperties);
        Logger.info(
                true,
                "Starter",
                "Mapper schema initialization started: namespace={}, mode={}, datasourceKey={}, entityCount={}",
                namespaceName,
                schemaConfig.mode(),
                schemaConfig.datasourceKey(),
                entityClasses.size());
        String previousKey = Holder.getKey();
        boolean restorePreviousKey = !Objects.equals(previousKey, Holder.getDefault());
        try {
            if (StringKit.isNotEmpty(holderKey)) {
                Holder.setKey(holderKey);
            }
            SchemaReport report = new EntitySchemaInitializer()
                    .initialize(dataSource, entityClasses, schemaConfig, tablePrefixConfig);
            Logger.info(
                    false,
                    "Starter",
                    "Mapper schema initialization finished: namespace={}, executedSqlCount={}, skippedSqlCount={}, failedDiffCount={}",
                    namespaceName,
                    report.executedSqls().size(),
                    report.skippedSqls().size(),
                    report.failedDiffs().size());
        } finally {
            if (restorePreviousKey) {
                Holder.setKey(previousKey);
            } else {
                Holder.remove();
            }
        }
    }

    /**
     * Resolves the target datasource and holder key used by schema initialization.
     * <p>
     * When {@code schema.datasourceKey} is empty, namespace initialization uses the namespace name as the holder key.
     * Named {@link DataSource} beans are preferred when present; otherwise the primary datasource is used with
     * {@link Holder} set to the resolved key so routing datasources can select the target internally.
     *
     * @param primaryDataSource datasource injected into mapper auto-configuration
     * @param beanFactory       bean factory used for named datasource lookup
     * @param namespaceName     namespace name, or {@code null} for global schema initialization
     * @param schemaConfig      schema runtime configuration
     * @return schema initialization target datasource and holder key
     */
    private static ResolvedSchemaDataSource resolveSchemaInitializationTarget(
            DataSource primaryDataSource,
            ConfigurableListableBeanFactory beanFactory,
            String namespaceName,
            SchemaConfig schemaConfig) {
        String datasourceKey = StringKit.trim(schemaConfig.datasourceKey());
        boolean explicit = StringKit.isNotEmpty(datasourceKey);
        if (StringKit.isEmpty(datasourceKey)) {
            datasourceKey = StringKit.trim(namespaceName);
        }
        schemaConfig.datasourceKey(datasourceKey);
        if (StringKit.isEmpty(datasourceKey)) {
            return new ResolvedSchemaDataSource(primaryDataSource, null);
        }
        DataSource namedDataSource = resolveNamedDataSource(beanFactory, datasourceKey);
        if (namedDataSource != null) {
            return new ResolvedSchemaDataSource(namedDataSource, datasourceKey);
        }
        if (!explicit) {
            Logger.info(
                    true,
                    "Starter",
                    "Mapper namespace schema datasource uses Holder routing: namespace={}, datasourceKey={}, reason={}",
                    namespaceName,
                    datasourceKey,
                    "noNamedBean");
        }
        return new ResolvedSchemaDataSource(primaryDataSource, datasourceKey);
    }

    /**
     * Finds a datasource bean by name.
     *
     * @param beanFactory   bean factory used for named datasource lookup
     * @param datasourceKey datasource key
     * @return datasource, or {@code null} when unavailable
     */
    private static DataSource resolveNamedDataSource(
            ConfigurableListableBeanFactory beanFactory,
            String datasourceKey) {
        if (beanFactory.containsBean(datasourceKey)) {
            try {
                return beanFactory.getBean(datasourceKey, DataSource.class);
            } catch (Exception e) {
                Logger.debug(false, "Starter", "Datasource bean lookup skipped: datasourceKey={}", datasourceKey);
            }
        }
        return null;
    }

    /**
     * Resolves the effective schema runtime configuration.
     * <p>
     * The mapper options are converted first, then {@link SchemaProvider#getConfig(String)} can override the runtime
     * configuration for the resolved datasource key. Provider-returned configuration is copied before the datasource
     * key is filled to avoid mutating user-owned instances.
     *
     * @param provider         schema provider
     * @param schemaProperties schema options
     * @param namespaceName    namespace name
     * @return schema runtime configuration
     */
    private static SchemaConfig resolveEffectiveSchemaConfig(
            SchemaProvider provider,
            MapperOptions.SchemaOptions schemaProperties,
            String namespaceName) {
        SchemaConfig schemaConfig = toSchemaConfig(schemaProperties);
        String datasourceKey = resolveSchemaDatasourceKey(schemaConfig, namespaceName);
        SchemaConfig providerConfig = provider == null ? null : provider.getConfig(datasourceKey);
        if (providerConfig == null) {
            schemaConfig.datasourceKey(datasourceKey);
            return schemaConfig;
        }
        SchemaConfig copy = copySchemaConfig(providerConfig);
        if (StringKit.isEmpty(copy.datasourceKey())) {
            copy.datasourceKey(datasourceKey);
        }
        return copy;
    }

    /**
     * Copies schema runtime configuration so provider-returned instances are not mutated by starter integration.
     *
     * @param source source schema configuration
     * @return copied schema configuration
     */
    private static SchemaConfig copySchemaConfig(SchemaConfig source) {
        SchemaConfig copy = new SchemaConfig();
        if (source == null) {
            return copy;
        }
        return copy.enabled(source.enabled()).mode(source.mode()).dryRun(source.dryRun()).printSql(source.printSql())
                .failFast(source.failFast()).continueOnError(source.continueOnError())
                .includeTables(copySet(source.includeTables())).excludeTables(copySet(source.excludeTables()))
                .includeEntities(copySet(source.includeEntities())).excludeEntities(copySet(source.excludeEntities()))
                .allowCreateTable(source.allowCreateTable()).allowAddColumn(source.allowAddColumn())
                .allowModifyType(source.allowModifyType()).allowExpandLength(source.allowExpandLength())
                .allowShrinkLength(source.allowShrinkLength()).allowExpandDecimal(source.allowExpandDecimal())
                .allowShrinkDecimal(source.allowShrinkDecimal()).allowModifyNullable(source.allowModifyNullable())
                .allowDropColumn(source.allowDropColumn()).allowRenameColumn(source.allowRenameColumn())
                .allowCreateIndex(source.allowCreateIndex()).allowDropIndex(source.allowDropIndex())
                .allowCreateUnique(source.allowCreateUnique()).allowDropUnique(source.allowDropUnique())
                .allowCreatePrimaryKey(source.allowCreatePrimaryKey()).allowDropPrimaryKey(source.allowDropPrimaryKey())
                .allowCreateForeignKey(source.allowCreateForeignKey()).allowDropForeignKey(source.allowDropForeignKey())
                .allowDangerous(source.allowDangerous()).dangerousWhitelist(copySet(source.dangerousWhitelist()))
                .renameMappings(copyMap(source.renameMappings()))
                .scriptLocation(StringKit.trim(source.scriptLocation()))
                .datasourceKey(StringKit.trim(source.datasourceKey()));
    }

    /**
     * Converts starter-bound schema options into the mapper schema runtime configuration.
     * <p>
     * Collection values are copied so later Spring binding changes cannot mutate the initializer input after this
     * method returns.
     *
     * @param schemaProperties schema options bound from global or namespace mapper configuration
     * @return schema runtime configuration
     */
    private static SchemaConfig toSchemaConfig(MapperOptions.SchemaOptions schemaProperties) {
        if (schemaProperties == null) {
            return new SchemaConfig();
        }
        return new SchemaConfig().enabled(schemaProperties.isEnabled()).mode(schemaProperties.getMode())
                .dryRun(schemaProperties.isDryRun()).printSql(schemaProperties.isPrintSql())
                .failFast(schemaProperties.isFailFast()).continueOnError(schemaProperties.isContinueOnError())
                .includeTables(copySet(schemaProperties.getIncludeTables()))
                .excludeTables(copySet(schemaProperties.getExcludeTables()))
                .includeEntities(copySet(schemaProperties.getIncludeEntities()))
                .excludeEntities(copySet(schemaProperties.getExcludeEntities()))
                .allowCreateTable(schemaProperties.isAllowCreateTable())
                .allowAddColumn(schemaProperties.isAllowAddColumn())
                .allowModifyType(schemaProperties.isAllowModifyType())
                .allowExpandLength(schemaProperties.isAllowExpandLength())
                .allowShrinkLength(schemaProperties.isAllowShrinkLength())
                .allowExpandDecimal(schemaProperties.isAllowExpandDecimal())
                .allowShrinkDecimal(schemaProperties.isAllowShrinkDecimal())
                .allowModifyNullable(schemaProperties.isAllowModifyNullable())
                .allowDropColumn(schemaProperties.isAllowDropColumn())
                .allowRenameColumn(schemaProperties.isAllowRenameColumn())
                .allowCreateIndex(schemaProperties.isAllowCreateIndex())
                .allowDropIndex(schemaProperties.isAllowDropIndex())
                .allowCreateUnique(schemaProperties.isAllowCreateUnique())
                .allowDropUnique(schemaProperties.isAllowDropUnique())
                .allowCreatePrimaryKey(schemaProperties.isAllowCreatePrimaryKey())
                .allowDropPrimaryKey(schemaProperties.isAllowDropPrimaryKey())
                .allowCreateForeignKey(schemaProperties.isAllowCreateForeignKey())
                .allowDropForeignKey(schemaProperties.isAllowDropForeignKey())
                .allowDangerous(schemaProperties.isAllowDangerous())
                .dangerousWhitelist(copySet(schemaProperties.getDangerousWhitelist()))
                .renameMappings(copyMap(schemaProperties.getRenameMappings()))
                .scriptLocation(StringKit.trim(schemaProperties.getScriptLocation()))
                .datasourceKey(StringKit.trim(schemaProperties.getDatasourceKey()));
    }

    /**
     * Resolves schema datasource key from explicit configuration or namespace name.
     *
     * @param schemaConfig  schema runtime configuration
     * @param namespaceName namespace name
     * @return datasource key
     */
    private static String resolveSchemaDatasourceKey(SchemaConfig schemaConfig, String namespaceName) {
        String datasourceKey = StringKit.trim(schemaConfig.datasourceKey());
        return StringKit.isNotEmpty(datasourceKey) ? datasourceKey : StringKit.trim(namespaceName);
    }

    /**
     * Resolves entity classes from the optional schema provider.
     *
     * @param provider      schema provider
     * @param datasourceKey datasource key
     * @return entity classes, never {@code null}
     */
    private static Collection<Class<?>> resolveProviderEntityClasses(SchemaProvider provider, String datasourceKey) {
        if (provider == null) {
            return Collections.emptyList();
        }
        Collection<Class<?>> entityClasses = provider.getEntityClasses(datasourceKey);
        return entityClasses == null ? Collections.emptyList() : entityClasses;
    }

    /**
     * Resolves entity classes from mapper factory bean definitions.
     * <p>
     * Mapper interfaces are still discovered by Spring scanner infrastructure, while the generic entity type itself is
     * resolved by the pure mapper {@link MapperEntityResolver}.
     *
     * @param beanFactory bean factory containing mapper factory bean definitions
     * @return entity classes resolved from mapper interfaces
     */
    private static Set<Class<?>> resolveMapperEntityClassesFromBeanFactory(
            ConfigurableListableBeanFactory beanFactory) {
        Set<Class<?>> entityClasses = new LinkedHashSet<>();
        for (String beanName : beanFactory.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            PropertyValue propertyValue = beanDefinition.getPropertyValues()
                    .getPropertyValue(MAPPER_INTERFACE_PROPERTY);
            if (propertyValue == null || propertyValue.getValue() == null) {
                continue;
            }
            Class<?> mapperInterface = resolveMapperInterfaceClass(beanFactory, propertyValue.getValue());
            Class<?> entityClass = resolveEntityClassFromMapperInterface(mapperInterface);
            if (entityClass != null) {
                entityClasses.add(entityClass);
            }
        }
        return entityClasses;
    }

    /**
     * Resolves a mapper interface value from a bean definition property.
     * <p>
     * AOT-generated definitions may hold the mapper interface as a class name string, while normal JVM definitions may
     * already hold a {@link Class} instance.
     *
     * @param beanFactory          bean factory used to load class names
     * @param mapperInterfaceValue mapper interface property value
     * @return mapper interface class, or {@code null} when it cannot be resolved
     */
    private static Class<?> resolveMapperInterfaceClass(
            ConfigurableListableBeanFactory beanFactory,
            Object mapperInterfaceValue) {
        if (mapperInterfaceValue instanceof Class<?>) {
            return (Class<?>) mapperInterfaceValue;
        }
        if (mapperInterfaceValue instanceof String mapperInterfaceName) {
            try {
                return ClassUtils.forName(mapperInterfaceName, beanFactory.getBeanClassLoader());
            } catch (ClassNotFoundException e) {
                Logger.warn(
                        false,
                        "Starter",
                        e,
                        "Mapper schema skipped mapper interface: mapperInterface={}, exception={}",
                        mapperInterfaceName,
                        e.getClass().getSimpleName());
            }
        }
        return null;
    }

    /**
     * Resolves the entity class declared by a mapper interface.
     *
     * @param mapperInterface mapper interface class
     * @return mapper entity class, or {@code null} when it cannot be resolved
     */
    private static Class<?> resolveEntityClassFromMapperInterface(Class<?> mapperInterface) {
        return MapperEntityResolver.resolve(mapperInterface);
    }

    /**
     * Scans configured schema entity packages for JPA-style entity annotations.
     * <p>
     * Package scanning stays in the starter because it depends on Spring classpath scanning. The mapper module only
     * receives the final entity class set.
     *
     * @param schemaProperties schema options containing entity package names
     * @param environment      Spring environment used by the scanner
     * @param resourceLoader   Spring resource loader used by the scanner
     * @return entity classes discovered from configured packages
     */
    private static Set<Class<?>> scanSchemaEntityClasses(
            MapperOptions.SchemaOptions schemaProperties,
            Environment environment,
            ResourceLoader resourceLoader) {
        Set<Class<?>> entityClasses = new LinkedHashSet<>();
        Set<String> packages = splitPackages(schemaProperties == null ? null : schemaProperties.getEntityPackages());
        if (packages.isEmpty()) {
            return entityClasses;
        }
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        if (environment != null) {
            scanner.setEnvironment(environment);
        }
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Table.class));
        ClassLoader classLoader = resourceLoader == null ? null : resourceLoader.getClassLoader();
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (classLoader == null) {
            classLoader = MapperPluginBuilder.class.getClassLoader();
        }
        for (String basePackage : packages) {
            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(basePackage)) {
                String className = beanDefinition.getBeanClassName();
                if (StringKit.isEmpty(className)) {
                    continue;
                }
                try {
                    entityClasses.add(ClassUtils.forName(className, classLoader));
                } catch (ClassNotFoundException e) {
                    Logger.warn(
                            false,
                            "Starter",
                            e,
                            "Mapper schema skipped entity class: className={}, exception={}",
                            className,
                            e.getClass().getSimpleName());
                }
            }
        }
        return entityClasses;
    }

    /**
     * Splits package configuration entries into individual package names.
     * <p>
     * Each array element may itself contain comma, semicolon, or whitespace separated package names.
     *
     * @param packages configured package entries
     * @return normalized package names in declaration order
     */
    private static Set<String> splitPackages(String[] packages) {
        Set<String> result = new LinkedHashSet<>();
        if (packages == null) {
            return result;
        }
        for (String value : packages) {
            if (StringKit.isEmpty(value)) {
                continue;
            }
            for (String packageName : value.split(PACKAGE_SPLIT_PATTERN)) {
                if (StringKit.isNotEmpty(packageName)) {
                    result.add(packageName);
                }
            }
        }
        return result;
    }

    /**
     * Copies a string set into a mutable insertion-order preserving set.
     *
     * @param values source values, possibly {@code null}
     * @return copied values, never {@code null}
     */
    private static Set<String> copySet(Set<String> values) {
        return values == null ? new LinkedHashSet<>() : new LinkedHashSet<>(values);
    }

    /**
     * Copies a string map into a mutable insertion-order preserving map.
     *
     * @param values source values, possibly {@code null}
     * @return copied values, never {@code null}
     */
    private static Map<String, String> copyMap(Map<String, String> values) {
        return values == null ? new LinkedHashMap<>() : new LinkedHashMap<>(values);
    }

    /**
     * Resolved datasource target for one schema initialization pass.
     *
     * @param dataSource datasource used for metadata reads and DDL execution
     * @param holderKey  datasource key exposed to mapper providers while initialization runs
     */
    private record ResolvedSchemaDataSource(DataSource dataSource, String holderKey) {

    }

    /**
     * Finds a provider bean by type from the Spring container.
     * <p>
     * A missing provider is normal for most applications, so lookup failures are treated as absence rather than startup
     * errors.
     *
     * @param providerType provider type to resolve
     * @param <T>          provider type
     * @return provider bean, or {@code null} when none is available
     */
    private static <T> T provider(Class<T> providerType) {
        try {
            return SpringBuilder.getBean(providerType);
        } catch (Exception e) {
            return null;
        }
    }

}
