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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.feature.affix.AffixRewriteHandler;
import org.miaixz.bus.mapper.feature.affix.AffixRuleConfig;
import org.miaixz.bus.mapper.feature.affix.AffixValueProvider;
import org.miaixz.bus.mapper.feature.audit.AuditConfig;
import org.miaixz.bus.mapper.feature.audit.AuditHandler;
import org.miaixz.bus.mapper.feature.audit.AuditProvider;
import org.miaixz.bus.mapper.feature.identifier.IdentifierValidator;
import org.miaixz.bus.mapper.feature.operation.OperationHandler;
import org.miaixz.bus.mapper.feature.paging.PageHandler;
import org.miaixz.bus.mapper.feature.populate.PopulateConfig;
import org.miaixz.bus.mapper.feature.populate.PopulateHandler;
import org.miaixz.bus.mapper.feature.populate.PopulateProvider;
import org.miaixz.bus.mapper.feature.tenant.TenantConfig;
import org.miaixz.bus.mapper.feature.tenant.TenantHandler;
import org.miaixz.bus.mapper.feature.tenant.TenantProvider;
import org.miaixz.bus.mapper.feature.visible.VisibleConfig;
import org.miaixz.bus.mapper.feature.visible.VisibleHandler;
import org.miaixz.bus.mapper.feature.visible.VisibleProvider;
import org.miaixz.bus.mapper.handler.AbstractSqlHandler;
import org.miaixz.bus.mapper.handler.MapperHandler;
import org.miaixz.bus.mapper.handler.MybatisInterceptor;

/**
 * Pure MyBatis mapper plugin factory.
 * <p>
 * This factory builds the mapper interceptor from {@link MapperOptions} and optional runtime providers without reading
 * a Spring container.
 *
 * @author Kimi Liu
 */
public class MapperPluginFactory {

    /**
     * Fallback namespace used when simplified YAML options are converted into handler properties.
     */
    private static final String DEFAULT_KEY = "default";

    /**
     * Initializes the factory that assembles mapper interceptors from immutable runtime options.
     */
    public MapperPluginFactory() {
        // No initialization required.
    }

    /**
     * Builds the primary mapper interceptor from mapper options.
     * <p>
     * This overload is intended for callers that only use property-based configuration and do not need runtime provider
     * instances.
     *
     * @param options mapper options
     * @return configured interceptor
     */
    public static MybatisInterceptor build(MapperOptions options) {
        return build(options, null);
    }

    /**
     * Builds the primary mapper interceptor from mapper options and runtime providers.
     * <p>
     * Handler construction is deterministic: each enabled handler is added in the same order regardless of whether its
     * configuration comes from simplified options, flattened properties, or provider instances.
     *
     * @param options   mapper options
     * @param providers runtime providers
     * @return configured interceptor
     */
    public static MybatisInterceptor build(MapperOptions options, MapperPluginProviders providers) {
        return build(options, providers, IdentifierValidator.create(options));
    }

    /**
     * Builds the primary mapper interceptor with an explicitly resolved identifier validator.
     *
     * <p>
     * Starter integration uses this overload so the same validator instance performs startup validation and pagination
     * sort validation. A {@code null} validator means that every effective identifier scope is explicitly disabled.
     * </p>
     *
     * @param options             mapper options
     * @param providers           runtime providers
     * @param identifierValidator identifier validator, or {@code null} when disabled
     * @return configured interceptor
     */
    public static MybatisInterceptor build(
            MapperOptions options,
            MapperPluginProviders providers,
            IdentifierValidator identifierValidator) {
        List<MapperHandler> handlers = new ArrayList<>();
        if (options != null) {
            Properties resolved = effectiveProperties(options);
            // Handler execution order is critical. The order determines the SQL modification sequence.
            // Execution order: Operation Check -> Affix Rewrite -> Tenant Vector -> Visible Vector -> Populate
            // -> Pagination -> Audit.
            configureOperation(options, resolved, handlers);
            configureAffix(options, providers, resolved, handlers);
            configureTenant(options, providers, resolved, handlers);
            configureVisible(options, providers, resolved, handlers);
            configurePopulate(options, providers, resolved, handlers);
            configurePagination(options, resolved, identifierValidator, handlers);
            configureAudit(options, providers, resolved, handlers);
        }

        Supplier<String> datasourceKeyProvider = providers == null ? null : providers.getDatasourceKeyProvider();
        handlers.stream().filter(AbstractSqlHandler.class::isInstance).map(AbstractSqlHandler.class::cast)
                .forEach(handler -> handler.setDatasourceKeyProvider(datasourceKeyProvider));

        MybatisInterceptor interceptor = new MybatisInterceptor();
        interceptor.setHandlers(handlers);
        return interceptor;
    }

    /**
     * Configures SQL operation safety checks and adds the {@link OperationHandler}.
     * <p>
     * This handler can be enabled or disabled through simplified YAML configuration:
     * {@code bus.mapper.operation.enabled}. It is enabled by default.
     *
     * @param options  mapper runtime options
     * @param resolved flattened Mapper configuration
     * @param handlers handler list to update
     */
    private static void configureOperation(MapperOptions options, Properties resolved, List<MapperHandler> handlers) {
        MapperOptions.OperationOptions operationOptions = options.getOperation();

        OperationHandler<?> handler = new OperationHandler<>();
        if (operationOptions != null) {
            handler.setStrictMode(operationOptions.isStrictMode());
        }
        handler.setProperties(resolved);
        handlers.add(handler);
        Logger.debug(false, "Mapper", "Operation handler configured successfully");
    }

    /**
     * Configures MyBatis pagination and adds the {@link PageHandler}.
     * <p>
     * Pagination configuration is selected by the effective JDBC data source key while preserving legacy defaults.
     *
     * @param options             mapper runtime options
     * @param resolved            flattened Mapper configuration
     * @param identifierValidator optional identifier validator
     * @param handlers            handler list to update
     */
    private static void configurePagination(
            MapperOptions options,
            Properties resolved,
            IdentifierValidator identifierValidator,
            List<MapperHandler> handlers) {
        PageHandler<?> pageHandler = new PageHandler<>(identifierValidator);
        pageHandler.setProperties(resolved);
        handlers.add(pageHandler);
        Logger.debug(false, "Mapper", "Pagination handler configured");
    }

    /**
     * Configures multi-tenancy properties and adds the {@link TenantHandler}.
     * <p>
     * Configuration priority:
     * </p>
     * <ol>
     * <li>Provider.getConfig() - if provider exists and returns non-null</li>
     * <li>Simplified YAML config ({@code bus.mapper.tenant.*})</li>
     * <li>Configuration properties - database-specific &gt; shared</li>
     * <li>Provider instance only - if provider exists but no provider config is returned</li>
     * <li>Default values inside the handler</li>
     * </ol>
     *
     * @param options   mapper runtime options
     * @param providers runtime provider instances
     * @param resolved  flattened mapper configuration properties
     * @param handlers  handler list to update
     */
    private static void configureTenant(
            MapperOptions options,
            MapperPluginProviders providers,
            Properties resolved,
            List<MapperHandler> handlers) {
        MapperOptions.TenantOptions tenantOptions = options.getTenant();
        MapperOptions.AffixOptions affixOptions = options.getAffix();
        boolean hasSimplifiedConfig = tenantOptions != null;
        boolean hasConfigFile = hasConfiguration(resolved, Args.TENANT_KEY);
        boolean hasProvider = providers != null && providers.getTenantProvider() != null;
        if (!hasSimplifiedConfig && !hasConfigFile && !hasProvider) {
            return;
        }

        Properties props = new Properties();
        props.putAll(resolved);
        if (hasSimplifiedConfig) {
            Logger.debug(false, "Mapper", "Loading tenant config from simplified YAML configuration");
            props.setProperty(
                    DEFAULT_KEY + Symbol.DOT + Args.TENANT_KEY + Symbol.DOT + Args.TENANT_COLUMN,
                    tenantOptions.getColumn() != null ? tenantOptions.getColumn() : Args.TENANT_ID);
            if (StringKit.isNotEmpty(tenantOptions.getIgnore())) {
                props.setProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.TENANT_KEY + Symbol.DOT + Args.PROP_IGNORE,
                        tenantOptions.getIgnore());
            }
            String affixPrefix = affixOptions != null && affixOptions.getPrefix() != null
                    ? affixOptions.getPrefix().getValue()
                    : null;
            String affixSuffix = affixOptions == null || affixOptions.getSuffix() == null ? null
                    : affixOptions.getSuffix().getValue();
            if (StringKit.isNotEmpty(affixPrefix)) {
                props.setProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT + Args.PREFIX_KEY + Symbol.DOT
                                + Args.PROP_VALUE,
                        affixPrefix);
            }
            if (StringKit.isNotEmpty(affixSuffix)) {
                props.setProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT + Args.SUFFIX_KEY + Symbol.DOT
                                + Args.PROP_VALUE,
                        affixSuffix);
            }
        }

        TenantProvider provider = providers != null ? providers.getTenantProvider() : null;
        if (provider != null) {
            Logger.debug(false, "Mapper", "TenantProvider instance found");
            TenantConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                Logger.debug(false, "Mapper", "Using tenant config from Provider.getConfig()");
                TenantHandler<?> handler = new TenantHandler<>(withAffixValues(providerConfig, props, providers));
                handler.setActivationProperties(props);
                handlers.add(handler);
                return;
            }
            props.put(Args.PROVIDER_KEY, provider);
        }

        TenantHandler<?> handler = new TenantHandler<>();
        if (handler.setProperties(props)) {
            handlers.add(handler);
            Logger.debug(false, "Mapper", "Tenant handler configured successfully");
        }
    }

    /**
     * Adds affix values from Mapper properties to a provider-supplied tenant configuration.
     *
     * @param config    the tenant configuration returned by a provider
     * @param props     the resolved mapper properties
     * @param providers runtime providers, including the read-only JDBC key provider
     * @return the tenant configuration containing the effective affix values
     */
    private static TenantConfig withAffixValues(
            TenantConfig config,
            Properties props,
            MapperPluginProviders providers) {
        if (config == null || props == null
                || StringKit.isNotEmpty(config.getAffixPrefix()) && StringKit.isNotEmpty(config.getAffixSuffix())) {
            return config;
        }
        Supplier<String> keyProvider = providers == null ? null : providers.getDatasourceKeyProvider();
        String defaultKey = keyProvider == null ? DEFAULT_KEY : keyProvider.get();
        if (StringKit.isEmpty(defaultKey)) {
            defaultKey = DEFAULT_KEY;
        }
        String affixPrefix = props.getProperty(
                defaultKey + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT + Args.PREFIX_KEY + Symbol.DOT + Args.PROP_VALUE,
                props.getProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT + Args.PREFIX_KEY + Symbol.DOT
                                + Args.PROP_VALUE,
                        props.getProperty(
                                Args.SHARED_KEY + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT + Args.PREFIX_KEY
                                        + Symbol.DOT + Args.PROP_VALUE)));
        String affixSuffix = props.getProperty(
                defaultKey + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT + Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_VALUE,
                props.getProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT + Args.SUFFIX_KEY + Symbol.DOT
                                + Args.PROP_VALUE,
                        props.getProperty(
                                Args.SHARED_KEY + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT + Args.SUFFIX_KEY
                                        + Symbol.DOT + Args.PROP_VALUE)));
        if (StringKit.isNotEmpty(config.getAffixPrefix())) {
            affixPrefix = config.getAffixPrefix();
        }
        if (StringKit.isNotEmpty(config.getAffixSuffix())) {
            affixSuffix = config.getAffixSuffix();
        }
        if (StringKit.isEmpty(affixPrefix) && StringKit.isEmpty(affixSuffix)) {
            return config;
        }
        return TenantConfig.builder().mode(config.getMode()).column(config.getColumn()).ignore(config.getIgnore())
                .ignoreMappers(config.getIgnoreMappers()).affixPrefix(affixPrefix).affixSuffix(affixSuffix)
                .enableSqlCache(config.isEnableSqlCache()).required(config.isRequired()).provider(config.getProvider())
                .build();
    }

    /**
     * Configures automatic data fill and adds the {@link PopulateHandler}.
     * <p>
     * Configuration priority:
     * </p>
     * <ol>
     * <li>Provider.getConfig() - if provider exists and returns non-null</li>
     * <li>Simplified YAML config ({@code bus.mapper.populate.*})</li>
     * <li>Configuration properties - database-specific &gt; shared</li>
     * <li>Provider instance only - if provider exists but no provider config is returned</li>
     * <li>Default values inside the handler</li>
     * </ol>
     *
     * @param options   mapper runtime options
     * @param providers runtime provider instances
     * @param resolved  flattened mapper configuration properties
     * @param handlers  handler list to update
     */
    private static void configurePopulate(
            MapperOptions options,
            MapperPluginProviders providers,
            Properties resolved,
            List<MapperHandler> handlers) {
        MapperOptions.PopulateOptions populateOptions = options.getPopulate();
        boolean hasSimplifiedConfig = populateOptions != null;
        boolean hasConfigFile = hasConfiguration(resolved, Args.POPULATE_KEY);
        boolean hasProvider = providers != null && providers.getPopulateProvider() != null;
        if (!hasSimplifiedConfig && !hasConfigFile && !hasProvider) {
            return;
        }

        Properties props = new Properties();
        props.putAll(resolved);
        if (hasSimplifiedConfig) {
            Logger.debug(false, "Mapper", "Loading populate config from simplified YAML configuration");
            props.setProperty(
                    DEFAULT_KEY + Symbol.DOT + Args.POPULATE_KEY + Symbol.DOT + Args.POPULATE_CREATED,
                    String.valueOf(populateOptions.isCreated()));
            props.setProperty(
                    DEFAULT_KEY + Symbol.DOT + Args.POPULATE_KEY + Symbol.DOT + Args.POPULATE_MODIFIED,
                    String.valueOf(populateOptions.isModified()));
            props.setProperty(
                    DEFAULT_KEY + Symbol.DOT + Args.POPULATE_KEY + Symbol.DOT + Args.POPULATE_CREATOR,
                    String.valueOf(populateOptions.isCreator()));
            props.setProperty(
                    DEFAULT_KEY + Symbol.DOT + Args.POPULATE_KEY + Symbol.DOT + Args.POPULATE_MODIFIER,
                    String.valueOf(populateOptions.isModifier()));
        }

        PopulateProvider provider = providers != null ? providers.getPopulateProvider() : null;
        if (provider != null) {
            Logger.debug(false, "Mapper", "PopulateProvider instance found");
            PopulateConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                Logger.debug(false, "Mapper", "Using populate config from Provider.getConfig()");
                PopulateHandler<?> handler = new PopulateHandler<>(providerConfig);
                handler.setActivationProperties(props);
                handlers.add(handler);
                return;
            }
            props.put(Args.PROVIDER_KEY, provider);
        }

        PopulateHandler<?> handler = new PopulateHandler<>();
        if (handler.setProperties(props)) {
            handlers.add(handler);
            Logger.debug(false, "Mapper", "Populate handler configured successfully");
        }
    }

    /**
     * Configures data perimeter control and adds the {@link VisibleHandler}.
     * <p>
     * Configuration priority:
     * </p>
     * <ol>
     * <li>Provider.getConfig() - if provider exists and returns non-null</li>
     * <li>Simplified YAML config ({@code bus.mapper.visible.*})</li>
     * <li>Configuration properties - database-specific &gt; shared</li>
     * <li>Provider instance only - if provider exists but no provider config is returned</li>
     * <li>Default values inside the handler</li>
     * </ol>
     *
     * @param options   mapper runtime options
     * @param providers runtime provider instances
     * @param resolved  flattened mapper configuration properties
     * @param handlers  handler list to update
     */
    private static void configureVisible(
            MapperOptions options,
            MapperPluginProviders providers,
            Properties resolved,
            List<MapperHandler> handlers) {
        MapperOptions.VisibleOptions visibleOptions = options.getVisible();
        boolean hasSimplifiedConfig = visibleOptions != null;
        boolean hasConfigFile = hasConfiguration(resolved, Args.VISIBLE_KEY);
        boolean hasProvider = providers != null && providers.getVisibleProvider() != null;
        if (!hasSimplifiedConfig && !hasConfigFile && !hasProvider) {
            return;
        }

        Properties props = new Properties();
        props.putAll(resolved);
        if (hasSimplifiedConfig) {
            Logger.debug(false, "Mapper", "Loading visible config from simplified YAML configuration");
            if (StringKit.isNotEmpty(visibleOptions.getIgnore())) {
                props.setProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.VISIBLE_KEY + Symbol.DOT + Args.PROP_IGNORE,
                        visibleOptions.getIgnore());
            }
        }

        VisibleProvider provider = providers != null ? providers.getVisibleProvider() : null;
        if (provider != null) {
            Logger.debug(false, "Mapper", "VisibleProvider instance found");
            VisibleConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                Logger.debug(false, "Mapper", "Using visible config from Provider.getConfig()");
                VisibleHandler<?> handler = new VisibleHandler<>(providerConfig);
                handler.setActivationProperties(props);
                handlers.add(handler);
                return;
            }
            props.put(Args.PROVIDER_KEY, provider);
        }

        VisibleHandler<?> handler = new VisibleHandler<>();
        if (handler.setProperties(props)) {
            handlers.add(handler);
            Logger.debug(false, "Mapper", "Visible handler configured successfully");
        }
    }

    /**
     * Configures physical table-name affix rewriting and adds the {@link AffixRewriteHandler}.
     * <p>
     * Configuration priority:
     * </p>
     * <ol>
     * <li>Provider.getConfig() - if provider exists and returns non-null</li>
     * <li>Simplified YAML config ({@code bus.mapper.affix.*})</li>
     * <li>Configuration properties - database-specific &gt; shared</li>
     * <li>Provider instance only - if provider exists but no provider config is returned</li>
     * <li>Default values inside the handler</li>
     * </ol>
     *
     * @param options   mapper runtime options
     * @param providers runtime provider instances
     * @param resolved  flattened mapper configuration properties
     * @param handlers  handler list to update
     */
    private static void configureAffix(
            MapperOptions options,
            MapperPluginProviders providers,
            Properties resolved,
            List<MapperHandler> handlers) {
        MapperOptions.AffixOptions affixOptions = options.getAffix();
        boolean hasSimplifiedConfig = affixOptions != null;
        boolean hasConfigFile = hasConfiguration(resolved, Args.AFFIX_KEY);
        boolean hasProvider = providers != null && providers.getAffixProvider() != null;
        if (!hasSimplifiedConfig && !hasConfigFile && !hasProvider) {
            return;
        }

        Properties props = new Properties();
        props.putAll(resolved);
        if (hasSimplifiedConfig) {
            Logger.debug(false, "Mapper", "Loading affix rules from simplified YAML configuration");
            MapperOptions.AffixPartOptions prefixPart = affixOptions == null ? null : affixOptions.getPrefix();
            MapperOptions.AffixPartOptions suffixPart = affixOptions == null ? null : affixOptions.getSuffix();
            String prefix = prefixPart == null ? null : prefixPart.getValue();
            String suffix = suffixPart == null ? null : suffixPart.getValue();
            String prefixIgnore = prefixPart == null ? null : prefixPart.getIgnore();
            String suffixIgnore = suffixPart == null ? null : suffixPart.getIgnore();
            if (StringKit.isNotEmpty(prefix)) {
                props.setProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT + Args.PREFIX_KEY + Symbol.DOT
                                + Args.PROP_VALUE,
                        prefix);
            }
            if (StringKit.isNotEmpty(suffix)) {
                props.setProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT + Args.SUFFIX_KEY + Symbol.DOT
                                + Args.PROP_VALUE,
                        suffix);
            }
            if (StringKit.isNotEmpty(prefixIgnore)) {
                props.setProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT + Args.PREFIX_KEY + Symbol.DOT
                                + Args.PROP_IGNORE,
                        prefixIgnore);
            }
            if (StringKit.isNotEmpty(suffixIgnore)) {
                props.setProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT + Args.SUFFIX_KEY + Symbol.DOT
                                + Args.PROP_IGNORE,
                        suffixIgnore);
            }
        }

        AffixValueProvider provider = providers != null ? providers.getAffixProvider() : null;
        if (provider != null) {
            Logger.debug(false, "Mapper", "AffixValueProvider instance found");
            AffixRuleConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                Logger.debug(false, "Mapper", "Using affix rules from AffixValueProvider.getConfig()");
                AffixRewriteHandler handler = new AffixRewriteHandler(providerConfig);
                handler.setActivationProperties(props);
                handlers.add(handler);
                return;
            }
            props.put(Args.PROVIDER_KEY, provider);
        }

        AffixRewriteHandler handler = new AffixRewriteHandler();
        if (handler.setProperties(props)) {
            handlers.add(handler);
            Logger.debug(false, "Mapper", "Affix rewrite handler configured successfully");
        }
    }

    /**
     * Configures SQL audit and adds the {@link AuditHandler}.
     * <p>
     * Configuration priority:
     * </p>
     * <ol>
     * <li>Provider.getConfig() - if provider exists and returns non-null</li>
     * <li>Simplified YAML config ({@code bus.mapper.audit.*})</li>
     * <li>Configuration properties - database-specific &gt; shared</li>
     * <li>Provider instance only - if provider exists but no provider config is returned</li>
     * <li>Default values inside the handler</li>
     * </ol>
     *
     * @param options   mapper runtime options
     * @param providers runtime provider instances
     * @param resolved  flattened mapper configuration properties
     * @param handlers  handler list to update
     */
    private static void configureAudit(
            MapperOptions options,
            MapperPluginProviders providers,
            Properties resolved,
            List<MapperHandler> handlers) {
        MapperOptions.AuditOptions auditOptions = options.getAudit();
        boolean hasSimplifiedConfig = auditOptions != null;
        boolean hasConfigFile = hasConfiguration(resolved, Args.AUDIT_KEY);
        boolean hasProvider = providers != null && providers.getAuditProvider() != null;
        if (!hasSimplifiedConfig && !hasConfigFile && !hasProvider) {
            return;
        }

        Properties props = new Properties();
        props.putAll(resolved);
        if (hasSimplifiedConfig) {
            Logger.debug(false, "Mapper", "Loading audit config from simplified YAML configuration");
            props.setProperty(
                    DEFAULT_KEY + Symbol.DOT + Args.AUDIT_KEY + Symbol.DOT + Args.AUDIT_SLOW_SQL_THRESHOLD,
                    String.valueOf(auditOptions.getSlowSqlThreshold()));
            props.setProperty(
                    DEFAULT_KEY + Symbol.DOT + Args.AUDIT_KEY + Symbol.DOT + Args.AUDIT_LOG_PARAMETERS,
                    String.valueOf(auditOptions.isLogParameters()));
            props.setProperty(
                    DEFAULT_KEY + Symbol.DOT + Args.AUDIT_KEY + Symbol.DOT + Args.AUDIT_LOG_RESULTS,
                    String.valueOf(auditOptions.isLogResults()));
            props.setProperty(
                    DEFAULT_KEY + Symbol.DOT + Args.AUDIT_KEY + Symbol.DOT + Args.AUDIT_LOG_ALL_SQL,
                    String.valueOf(auditOptions.isLogAllSql()));
            props.setProperty(
                    DEFAULT_KEY + Symbol.DOT + Args.AUDIT_KEY + Symbol.DOT + Args.AUDIT_PRINT_CONSOLE,
                    String.valueOf(auditOptions.isPrintConsole()));
        }

        AuditProvider provider = providers != null ? providers.getAuditProvider() : null;
        if (provider != null) {
            Logger.debug(false, "Mapper", "AuditProvider instance found");
            AuditConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                Logger.debug(false, "Mapper", "Using audit config from Provider.getConfig()");
                AuditHandler<?> handler = new AuditHandler<>(providerConfig);
                handler.setActivationProperties(props);
                handlers.add(handler);
                return;
            }
            props.put(Args.PROVIDER_KEY, provider);
        }

        AuditHandler<?> handler = new AuditHandler<>();
        if (handler.setProperties(props)) {
            handlers.add(handler);
            Logger.debug(false, "Mapper", "Audit handler configured successfully");
        }
    }

    /**
     * Combines complete datasource properties with top-level global defaults without discarding either source.
     * Datasource entries remain highest priority; top-level values replace matching shared fields one field at a time.
     *
     * @param options mapper runtime options
     * @return flattened effective properties
     */
    private static Properties effectiveProperties(MapperOptions options) {
        Properties properties = MapperOptions.resolve(options);
        MapperOptions.OperationOptions operation = options.getOperation();
        if (operation != null) {
            shared(properties, Args.OPERATION_KEY, Args.PROP_ENABLED, operation.isEnabled());
            shared(properties, Args.OPERATION_KEY, Args.OPERATION_STRICT_MODE, operation.isStrictMode());
        }

        MapperOptions.PageOptions page = options.getPage();
        if (page != null) {
            shared(properties, Args.PAGE_KEY, Args.PROP_ENABLED, page.isEnabled());
            shared(properties, Args.PAGE_KEY, Args.PAGE_REASONABLE, page.isReasonable());
            shared(properties, Args.PAGE_KEY, Args.PAGE_SUPPORT_METHOD_ARGUMENTS, page.isSupportMethodsArguments());
            shared(properties, Args.PAGE_KEY, Args.PAGE_PARAMS, page.getParams());
        } else {
            shared(properties, Args.PAGE_KEY, Args.PAGE_REASONABLE, options.getReasonable());
            shared(properties, Args.PAGE_KEY, Args.PAGE_SUPPORT_METHOD_ARGUMENTS, options.getSupportMethodsArguments());
            shared(properties, Args.PAGE_KEY, Args.PAGE_PARAMS, options.getParams());
        }

        MapperOptions.IdentifierOptions identifier = options.getIdentifier();
        if (identifier != null) {
            shared(properties, Args.IDENTIFIER_KEY, Args.PROP_ENABLED, identifier.isEnabled());
        }

        MapperOptions.AffixOptions affix = options.getAffix();
        if (affix != null) {
            shared(properties, Args.AFFIX_KEY, Args.PROP_ENABLED, affix.isEnabled());
            MapperOptions.AffixPartOptions prefix = affix.getPrefix();
            MapperOptions.AffixPartOptions suffix = affix.getSuffix();
            if (prefix != null) {
                shared(properties, Args.AFFIX_KEY, Args.PREFIX_KEY + Symbol.DOT + Args.PROP_VALUE, prefix.getValue());
                shared(properties, Args.AFFIX_KEY, Args.PREFIX_KEY + Symbol.DOT + Args.PROP_IGNORE, prefix.getIgnore());
            }
            if (suffix != null) {
                shared(properties, Args.AFFIX_KEY, Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_VALUE, suffix.getValue());
                shared(properties, Args.AFFIX_KEY, Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_IGNORE, suffix.getIgnore());
            }
        }
        MapperOptions.TenantOptions tenant = options.getTenant();
        if (tenant != null) {
            shared(properties, Args.TENANT_KEY, Args.PROP_ENABLED, tenant.isEnabled());
            shared(properties, Args.TENANT_KEY, Args.TENANT_MODE, tenant.getMode());
            shared(properties, Args.TENANT_KEY, Args.TENANT_COLUMN, tenant.getColumn());
            shared(properties, Args.TENANT_KEY, Args.PROP_IGNORE, tenant.getIgnore());
            shared(properties, Args.TENANT_KEY, Args.TENANT_IGNORE_MAPPERS, tenant.getIgnoreMappers());
            shared(properties, Args.TENANT_KEY, Args.TENANT_ENABLE_SQL_CACHE, tenant.isEnableSqlCache());
        }
        MapperOptions.PopulateOptions populate = options.getPopulate();
        if (populate != null) {
            shared(properties, Args.POPULATE_KEY, Args.PROP_ENABLED, populate.isEnabled());
            shared(properties, Args.POPULATE_KEY, Args.POPULATE_CREATED, populate.isCreated());
            shared(properties, Args.POPULATE_KEY, Args.POPULATE_MODIFIED, populate.isModified());
            shared(properties, Args.POPULATE_KEY, Args.POPULATE_CREATOR, populate.isCreator());
            shared(properties, Args.POPULATE_KEY, Args.POPULATE_MODIFIER, populate.isModifier());
        }
        MapperOptions.VisibleOptions visible = options.getVisible();
        if (visible != null) {
            shared(properties, Args.VISIBLE_KEY, Args.PROP_ENABLED, visible.isEnabled());
            shared(properties, Args.VISIBLE_KEY, Args.PROP_IGNORE, visible.getIgnore());
        }
        MapperOptions.AuditOptions audit = options.getAudit();
        if (audit != null) {
            shared(properties, Args.AUDIT_KEY, Args.PROP_ENABLED, audit.isEnabled());
            shared(properties, Args.AUDIT_KEY, Args.AUDIT_SLOW_SQL_THRESHOLD, audit.getSlowSqlThreshold());
            shared(properties, Args.AUDIT_KEY, Args.AUDIT_LOG_PARAMETERS, audit.isLogParameters());
            shared(properties, Args.AUDIT_KEY, Args.AUDIT_LOG_RESULTS, audit.isLogResults());
            shared(properties, Args.AUDIT_KEY, Args.AUDIT_LOG_ALL_SQL, audit.isLogAllSql());
            shared(properties, Args.AUDIT_KEY, Args.AUDIT_PRINT_CONSOLE, audit.isPrintConsole());
        }
        return properties;
    }

    /**
     * Writes a non-empty top-level option into the shared fallback scope.
     *
     * @param properties flattened mapper properties to update
     * @param scope      handler configuration scope
     * @param name       setting name
     * @param value      configured value, possibly {@code null}
     */
    private static void shared(Properties properties, String scope, String name, Object value) {
        if (value != null && (!(value instanceof String text) || StringKit.isNotEmpty(text))) {
            properties.setProperty(Args.SHARED_KEY + Symbol.DOT + scope + Symbol.DOT + name, String.valueOf(value));
        }
    }

    /**
     * Returns whether the flattened mapper configuration contains any handler configuration.
     *
     * @param properties flattened mapper properties
     * @param scope      handler configuration scope
     * @return {@code true} when the properties contain at least one entry
     */
    private static boolean hasConfiguration(Properties properties, String scope) {
        if (properties == null || scope == null) {
            return false;
        }
        String marker = Symbol.DOT + scope + Symbol.DOT;
        return properties.stringPropertyNames().stream().anyMatch(key -> key.contains(marker));
    }

}
