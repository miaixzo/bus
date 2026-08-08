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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.builder.MapperEntityResolver;
import org.miaixz.bus.mapper.feature.affix.AffixRewriteHandler;
import org.miaixz.bus.mapper.feature.affix.AffixRuleConfig;
import org.miaixz.bus.mapper.feature.affix.AffixValueProvider;
import org.miaixz.bus.mapper.feature.audit.AuditProvider;
import org.miaixz.bus.mapper.feature.identifier.IdentifierValidator;
import org.miaixz.bus.mapper.feature.populate.PopulateProvider;
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
import org.miaixz.bus.spring.annotation.PlaceholderBinder;
import org.miaixz.bus.spring.bean.BeanProvider;
import org.miaixz.bus.spring.jdbc.DataSourceHolder;
import org.miaixz.bus.spring.jdbc.DynamicDataSource;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Starter adapter for creating Mapper plugins, validating physical identifiers, and coordinating schema initialization.
 * <p>
 * Pure plugin-chain construction lives in {@link MapperPluginFactory}. Spring-specific work such as property binding,
 * provider lookup, datasource lookup and package scanning stays here so {@link MapperConfiguration} only declares
 * Spring beans.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class MapperPluginBuilder {

    /**
     * MapperFactoryBean property that stores the mapper interface class.
     */
    private static final String MAPPER_INTERFACE_PROPERTY = "mapperInterface";

    /**
     * Delimiter pattern for package configuration values.
     */
    private static final String PACKAGE_SPLIT_PATTERN = "[,;\\s]+";

    /**
     * Restricts the class to static mapper plugin assembly operations.
     */
    private MapperPluginBuilder() {
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
        MapperProperties properties = PlaceholderBinder.bind(environment, MapperProperties.class, GeniusBuilder.MAPPER);
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
        return build(mapperProperties, resolvePluginProviders(mapperProperties, null));
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
     * The interceptor is attached to the factory first, physical identifiers are validated for each enabled datasource
     * scope, and then schema initialization runs when global, namespace, or provider configuration enables it.
     *
     * @param factory        MyBatis session factory bean
     * @param properties     mapper properties
     * @param environment    Spring environment used by package scanning
     * @param resourceLoader Spring resource loader used by package scanning
     * @param dataSource     primary data source
     * @param beanFactory    bean factory used to discover mapper definitions
     * @param beanProvider   Spring Bean provider
     * @throws Exception if identifier validation or schema initialization fails
     */
    public static void configureSqlSessionFactory(
            SqlSessionFactoryBean factory,
            MapperProperties properties,
            Environment environment,
            ResourceLoader resourceLoader,
            DataSource dataSource,
            ConfigurableListableBeanFactory beanFactory,
            BeanProvider beanProvider) throws Exception {
        MapperProperties mapperProperties = properties == null ? new MapperProperties() : properties;
        MapperPluginProviders mapperProviders = resolvePluginProviders(mapperProperties, beanProvider);
        IdentifierValidator identifierValidator = IdentifierValidator.create(mapperProperties);
        if (factory != null) {
            factory.setPlugins(MapperPluginFactory.build(mapperProperties, mapperProviders, identifierValidator));
        }
        validateIdentifiersIfNecessary(identifierValidator, mapperProperties, mapperProviders, dataSource, beanFactory);
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
     * @param beanProvider Spring Bean provider
     * @param properties   mapper properties bound from the Spring environment
     * @return provider holder passed to the pure Mapper plugin factory
     */
    private static MapperPluginProviders resolvePluginProviders(
            MapperProperties properties,
            BeanProvider beanProvider) {
        MapperPluginProviders providers = new MapperPluginProviders();
        DataSourceHolder dataSourceHolder = provider(beanProvider, DataSourceHolder.class);
        if (dataSourceHolder != null) {
            providers.setDatasourceKeyProvider(dataSourceHolder::getKey);
        }
        if (properties == null) {
            return providers;
        }
        Properties resolved = MapperOptions.resolve(properties);
        if (properties.getTenant() != null || hasScope(resolved, Args.TENANT_KEY)
                || hasProviderBean(beanProvider, TenantProvider.class)) {
            providers.setTenantProvider(provider(beanProvider, TenantProvider.class));
        }
        if (properties.getAffix() != null || hasScope(resolved, Args.AFFIX_KEY)
                || hasProviderBean(beanProvider, AffixValueProvider.class)) {
            providers.setAffixProvider(provider(beanProvider, AffixValueProvider.class));
        }
        if (properties.getVisible() != null || hasScope(resolved, Args.VISIBLE_KEY)
                || hasProviderBean(beanProvider, VisibleProvider.class)) {
            providers.setVisibleProvider(provider(beanProvider, VisibleProvider.class));
        }
        if (properties.getPopulate() != null || hasScope(resolved, Args.POPULATE_KEY)
                || hasProviderBean(beanProvider, PopulateProvider.class)) {
            providers.setPopulateProvider(provider(beanProvider, PopulateProvider.class));
        }
        if (properties.getAudit() != null || hasScope(resolved, Args.AUDIT_KEY)
                || hasProviderBean(beanProvider, AuditProvider.class)) {
            providers.setAuditProvider(provider(beanProvider, AuditProvider.class));
        }
        if ((properties.getSchema() != null && properties.getSchema().isEnabled()) || hasScope(resolved, "schema")
                || hasProviderBean(beanProvider, SchemaProvider.class)) {
            providers.setSchemaProvider(provider(beanProvider, SchemaProvider.class));
        }
        return providers;
    }

    /**
     * Tests whether flattened mapper properties contain configuration for the requested scope.
     *
     * @param properties flattened mapper properties
     * @param scope      mapper feature scope
     * @return {@code true} when at least one property belongs to the scope
     */
    private static boolean hasScope(Properties properties, String scope) {
        if (properties == null || scope == null) {
            return false;
        }
        String marker = Symbol.DOT + scope + Symbol.DOT;
        return properties.stringPropertyNames().stream().anyMatch(key -> key.contains(marker));
    }

    /**
     * Tests whether the Spring container exposes a provider bean of the requested type.
     * <p>
     * Provider discovery may run before a Spring context is available in direct builder use. In that case the missing
     * container is treated the same as a missing optional provider.
     *
     * @param beanProvider Spring Bean provider
     * @param providerType provider type to inspect
     * @return {@code true} when at least one matching provider bean is registered
     */
    private static boolean hasProviderBean(BeanProvider beanProvider, Class<?> providerType) {
        if (beanProvider == null) {
            return false;
        }
        try {
            return beanProvider.getBeanNamesForType(providerType).length > 0;
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Resolves effective physical table-name affix rules for Mapper startup operations.
     * <p>
     * Affix rules follow the same provider and property resolution path used by the runtime handler, so
     * identifier validation and schema DDL generation observe the same global and datasource-specific table names.
     *
     * @param properties         mapper properties
     * @param providers          provider holder
     * @param datasourceKey      data source key
     * @param resolvedProperties flattened mapper configuration properties
     * @return effective affix rules, or {@code null} when no affix feature is configured
     */
    private static AffixRuleConfig resolveAffixRuleConfig(
            MapperProperties properties,
            MapperPluginProviders providers,
            String datasourceKey,
            Properties resolvedProperties) {
        if (properties == null) {
            return null;
        }
        MapperOptions.AffixOptions affixOptions = properties.getAffix();
        if (affixOptions != null && !affixOptions.isEnabled()) {
            return null;
        }
        AffixValueProvider provider = providers == null ? null : providers.getAffixProvider();
        if (provider != null) {
            AffixRuleConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                return providerConfig;
            }
        }
        Properties affixProperties = new Properties();
        if (resolvedProperties != null) {
            affixProperties.putAll(resolvedProperties);
        }
        String sharedAffixScope = Args.SHARED_KEY + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT;
        String defaultAffixScope = Normal.DEFAULT + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT;
        MapperOptions.AffixPartOptions prefixPart = affixOptions == null ? null : affixOptions.getPrefix();
        MapperOptions.AffixPartOptions suffixPart = affixOptions == null ? null : affixOptions.getSuffix();
        String configuredPrefix = prefixPart == null ? null : prefixPart.getValue();
        String configuredSuffix = suffixPart == null ? null : suffixPart.getValue();
        String configuredPrefixIgnore = prefixPart == null ? null : prefixPart.getIgnore();
        String configuredSuffixIgnore = suffixPart == null ? null : suffixPart.getIgnore();
        if (StringKit.isNotEmpty(configuredPrefix)) {
            if (!affixProperties.containsKey(sharedAffixScope + Args.PREFIX_KEY + Symbol.DOT + Args.PROP_VALUE)) {
                affixProperties
                        .setProperty(sharedAffixScope + Args.PREFIX_KEY + Symbol.DOT + Args.PROP_VALUE, configuredPrefix);
            }
            if (!affixProperties.containsKey(defaultAffixScope + Args.PREFIX_KEY + Symbol.DOT + Args.PROP_VALUE)) {
                affixProperties
                        .setProperty(defaultAffixScope + Args.PREFIX_KEY + Symbol.DOT + Args.PROP_VALUE, configuredPrefix);
            }
        }
        if (StringKit.isNotEmpty(configuredSuffix)) {
            if (!affixProperties.containsKey(sharedAffixScope + Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_VALUE)) {
                affixProperties
                        .setProperty(sharedAffixScope + Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_VALUE, configuredSuffix);
            }
            if (!affixProperties.containsKey(defaultAffixScope + Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_VALUE)) {
                affixProperties
                        .setProperty(defaultAffixScope + Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_VALUE, configuredSuffix);
            }
        }
        if (StringKit.isNotEmpty(configuredPrefixIgnore)) {
            if (!affixProperties.containsKey(sharedAffixScope + Args.PREFIX_KEY + Symbol.DOT + Args.PROP_IGNORE)) {
                affixProperties.setProperty(
                        sharedAffixScope + Args.PREFIX_KEY + Symbol.DOT + Args.PROP_IGNORE,
                        configuredPrefixIgnore);
            }
            if (!affixProperties.containsKey(defaultAffixScope + Args.PREFIX_KEY + Symbol.DOT + Args.PROP_IGNORE)) {
                affixProperties.setProperty(
                        defaultAffixScope + Args.PREFIX_KEY + Symbol.DOT + Args.PROP_IGNORE,
                        configuredPrefixIgnore);
            }
        }
        if (StringKit.isNotEmpty(configuredSuffixIgnore)) {
            if (!affixProperties.containsKey(sharedAffixScope + Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_IGNORE)) {
                affixProperties.setProperty(
                        sharedAffixScope + Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_IGNORE,
                        configuredSuffixIgnore);
            }
            if (!affixProperties.containsKey(defaultAffixScope + Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_IGNORE)) {
                affixProperties.setProperty(
                        defaultAffixScope + Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_IGNORE,
                        configuredSuffixIgnore);
            }
        }
        String key = StringKit.isNotEmpty(datasourceKey) ? datasourceKey : Normal.DEFAULT;
        return AffixRewriteHandler.resolveConfig(key, affixProperties, provider);
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
        ResolvedDataSource schemaDataSource = resolveDataSourceTarget(dataSource, beanFactory, null);
        runSchemaInitialization(
                properties,
                providers,
                schemaProvider,
                environment,
                resourceLoader,
                schemaDataSource.dataSource(),
                beanFactory,
                null,
                resolvedProperties,
                schemaProperties,
                schemaConfig);
    }

    /**
     * Runs schema initialization for every database namespace that declares effective schema configuration.
     *
     * @param properties         mapper properties
     * @param providers          provider holder
     * @param schemaProvider     schema provider
     * @param environment        Spring environment used by package scanning
     * @param resourceLoader     Spring resource loader used by package scanning
     * @param primaryDataSource  primary data source supplied to Mapper bean assembly
     * @param beanFactory        bean factory used for named data source lookup
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
            ResolvedDataSource schemaDataSource = resolveDataSourceTarget(
                    primaryDataSource,
                    beanFactory,
                    namespaceName);
            runSchemaInitialization(
                    properties,
                    providers,
                    schemaProvider,
                    environment,
                    resourceLoader,
                    schemaDataSource.dataSource(),
                    beanFactory,
                    namespaceName,
                    resolvedProperties,
                    schemaProperties,
                    schemaConfig);
        }
    }

    /**
     * Runs one schema initialization pass for a data source target.
     * <p>
     * Entity classes are collected from mapper generic declarations, configured entity packages, and the optional
     * {@link SchemaProvider}. Provider lookups and affix rule resolution receive the namespace explicitly and do not
     * mutate Mapper runtime context.
     *
     * @param properties         mapper properties
     * @param providers          provider holder
     * @param schemaProvider     schema provider
     * @param environment        Spring environment used by package scanning
     * @param resourceLoader     Spring resource loader used by package scanning
     * @param dataSource         data source used for metadata reads and DDL execution
     * @param beanFactory        bean factory used to discover mapper definitions
     * @param namespaceName      namespace name, or {@code null} for the legacy global configuration
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
            Properties resolvedProperties,
            MapperOptions.SchemaOptions schemaProperties,
            SchemaConfig schemaConfig) throws Exception {
        String namespace = StringKit.trim(namespaceName);
        Set<Class<?>> entityClasses = new LinkedHashSet<>();
        entityClasses.addAll(resolveMapperEntityClassesFromBeanFactory(beanFactory));
        entityClasses.addAll(scanSchemaEntityClasses(schemaProperties, environment, resourceLoader));
        entityClasses.addAll(resolveProviderEntityClasses(schemaProvider, namespace));
        AffixRuleConfig affixRuleConfig = resolveAffixRuleConfig(properties, providers, namespace, resolvedProperties);
        Logger.info(
                true,
                "Starter",
                "Mapper schema initialization started: namespace={}, mode={}, entityCount={}",
                namespaceName,
                schemaConfig.mode(),
                entityClasses.size());
        SchemaReport report = new EntitySchemaInitializer()
                .initialize(dataSource, entityClasses, schemaConfig, affixRuleConfig);
        Logger.info(
                false,
                "Starter",
                "Mapper schema initialization finished: namespace={}, executedSqlCount={}, skippedSqlCount={}, failedDiffCount={}",
                namespaceName,
                report.executedSqls().size(),
                report.skippedSqls().size(),
                report.failedDiffs().size());
    }

    /**
     * Validates Mapper entity identifiers for each configured datasource namespace, or the primary datasource when no
     * namespace is configured, while the default-enabled identifier validator is active.
     *
     * @param identifierValidator identifier validator, or {@code null} when explicitly disabled for every scope
     * @param properties          mapper properties
     * @param providers           mapper runtime providers
     * @param primaryDataSource   primary datasource supplied to Mapper assembly
     * @param beanFactory         bean factory used to resolve Mapper entities and named datasources
     */
    private static void validateIdentifiersIfNecessary(
            IdentifierValidator identifierValidator,
            MapperProperties properties,
            MapperPluginProviders providers,
            DataSource primaryDataSource,
            ConfigurableListableBeanFactory beanFactory) {
        if (identifierValidator == null) {
            return;
        }
        Set<Class<?>> entityClasses = resolveMapperEntityClassesFromBeanFactory(beanFactory);
        Properties resolvedProperties = MapperOptions.resolve(properties);
        Set<String> namespaces = new LinkedHashSet<>(MapperOptions.resolveNamespaceNames(resolvedProperties));
        namespaces.remove(Normal.DEFAULT);
        if (primaryDataSource instanceof DynamicDataSource dynamicDataSource) {
            dynamicDataSource.getAllDataSources().keySet().stream().map(String::valueOf).forEach(namespaces::add);
        }
        if (namespaces.isEmpty()) {
            if (!identifierValidator.isEnabled(null)) {
                return;
            }
            AffixRuleConfig affix = resolveAffixRuleConfig(properties, providers, null, resolvedProperties);
            identifierValidator.validate(primaryDataSource, null, entityClasses, affix);
            return;
        }
        for (String namespace : namespaces) {
            if (!identifierValidator.isEnabled(namespace)) {
                continue;
            }
            ResolvedDataSource target = resolveDataSourceTarget(primaryDataSource, beanFactory, namespace);
            AffixRuleConfig affix = resolveAffixRuleConfig(properties, providers, namespace, resolvedProperties);
            identifierValidator.validate(target.dataSource(), namespace, entityClasses, affix);
        }
    }

    /**
     * Resolves a target datasource used by Mapper startup features.
     * <p>
     * The namespace name is the only datasource route key. A named {@link DataSource} bean matching the namespace is
     * preferred, followed by a route registered in {@link DynamicDataSource}. An unresolved namespace is rejected so
     * startup operations cannot silently run against the primary datasource. This method never changes JDBC routing
     * state.
     *
     * @param primaryDataSource data source supplied to mapper Bean assembly
     * @param beanFactory       bean factory used for named data source lookup
     * @param namespaceName     namespace name, or {@code null} for the primary datasource
     * @return resolved target datasource
     */
    private static ResolvedDataSource resolveDataSourceTarget(
            DataSource primaryDataSource,
            ConfigurableListableBeanFactory beanFactory,
            String namespaceName) {
        String namespace = StringKit.trim(namespaceName);
        if (StringKit.isEmpty(namespace)) {
            return new ResolvedDataSource(primaryDataSource);
        }
        DataSource namedDataSource = resolveNamedDataSource(beanFactory, namespace);
        if (namedDataSource != null) {
            return new ResolvedDataSource(namedDataSource);
        }
        if (primaryDataSource instanceof DynamicDataSource dynamicDataSource) {
            Object routedDataSource = dynamicDataSource.getAllDataSources().get(namespace);
            if (routedDataSource instanceof DataSource dataSource) {
                return new ResolvedDataSource(dataSource);
            }
        }
        throw new IllegalStateException("Unable to locate datasource route for mapper namespace " + Symbol.SINGLE_QUOTE
                + namespace + Symbol.SINGLE_QUOTE);
    }

    /**
     * Finds a data source bean by name.
     *
     * @param beanFactory   bean factory used for named data source lookup
     * @param datasourceKey data source key
     * @return data source, or {@code null} when unavailable
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
     * configuration for the mapper namespace. Provider-returned configuration is copied to avoid mutating user-owned
     * instances.
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
        String namespace = StringKit.trim(namespaceName);
        SchemaConfig providerConfig = provider == null ? null : provider.getConfig(namespace);
        if (providerConfig == null) {
            return schemaConfig;
        }
        return copySchemaConfig(providerConfig);
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
                .allowModifyComment(source.allowModifyComment()).allowDropColumn(source.allowDropColumn())
                .allowRenameColumn(source.allowRenameColumn()).allowCreateIndex(source.allowCreateIndex())
                .allowDropIndex(source.allowDropIndex()).allowCreateUnique(source.allowCreateUnique())
                .allowDropUnique(source.allowDropUnique()).allowCreatePrimaryKey(source.allowCreatePrimaryKey())
                .allowDropPrimaryKey(source.allowDropPrimaryKey()).allowCreateForeignKey(source.allowCreateForeignKey())
                .allowDropForeignKey(source.allowDropForeignKey()).allowDangerous(source.allowDangerous())
                .dangerousWhitelist(copySet(source.dangerousWhitelist()))
                .renameMappings(copyMap(source.renameMappings()))
                .scriptLocation(StringKit.trim(source.scriptLocation()));
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
                .allowModifyComment(schemaProperties.isAllowModifyComment())
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
                .scriptLocation(StringKit.trim(schemaProperties.getScriptLocation()));
    }

    /**
     * Resolves entity classes from the optional schema provider.
     *
     * @param provider      schema provider
     * @param namespaceName mapper namespace name
     * @return entity classes, never {@code null}
     */
    private static Collection<Class<?>> resolveProviderEntityClasses(SchemaProvider provider, String namespaceName) {
        if (provider == null) {
            return Collections.emptyList();
        }
        Collection<Class<?>> entityClasses = provider.getEntityClasses(namespaceName);
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
     * @param mapperInterfaceValue mapper interface represented as a Class or fully qualified class name
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
                return ClassKit.forName(mapperInterfaceName, beanFactory.getBeanClassLoader());
            } catch (InternalException e) {
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
                    entityClasses.add(ClassKit.forName(className, classLoader));
                } catch (InternalException e) {
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
     * Resolved datasource target for one Mapper startup pass.
     *
     * @param dataSource data source used for metadata reads and optional DDL execution
     */
    private record ResolvedDataSource(DataSource dataSource) {

    }

    /**
     * Finds a provider bean by type from the Spring container.
     * <p>
     * A missing provider is normal for most applications, so lookup failures are treated as absence rather than startup
     * errors.
     *
     * @param beanProvider Spring Bean provider
     * @param providerType provider type to resolve
     * @param <T>          provider type
     * @return provider bean, or {@code null} when none is available
     */
    private static <T> T provider(BeanProvider beanProvider, Class<T> providerType) {
        if (beanProvider == null) {
            return null;
        }
        try {
            return beanProvider.getBean(providerType);
        } catch (Exception e) {
            return null;
        }
    }

}
