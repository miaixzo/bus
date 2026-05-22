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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Holder;
import org.miaixz.bus.mapper.feature.audit.AuditConfig;
import org.miaixz.bus.mapper.feature.audit.AuditHandler;
import org.miaixz.bus.mapper.feature.audit.AuditProvider;
import org.miaixz.bus.mapper.feature.operation.OperationHandler;
import org.miaixz.bus.mapper.feature.paging.PageHandler;
import org.miaixz.bus.mapper.feature.populate.PopulateConfig;
import org.miaixz.bus.mapper.feature.populate.PopulateHandler;
import org.miaixz.bus.mapper.feature.populate.PopulateProvider;
import org.miaixz.bus.mapper.feature.prefix.TablePrefixConfig;
import org.miaixz.bus.mapper.feature.prefix.TablePrefixHandler;
import org.miaixz.bus.mapper.feature.prefix.TablePrefixProvider;
import org.miaixz.bus.mapper.feature.tenant.TenantConfig;
import org.miaixz.bus.mapper.feature.tenant.TenantHandler;
import org.miaixz.bus.mapper.feature.tenant.TenantProvider;
import org.miaixz.bus.mapper.feature.visible.VisibleConfig;
import org.miaixz.bus.mapper.feature.visible.VisibleHandler;
import org.miaixz.bus.mapper.feature.visible.VisibleProvider;
import org.miaixz.bus.mapper.handler.MapperHandler;
import org.miaixz.bus.mapper.handler.MybatisInterceptor;

/**
 * Pure MyBatis mapper plugin factory.
 * <p>
 * This factory builds the mapper interceptor from {@link MapperOptions} and optional runtime providers without reading
 * a Spring container.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MapperPluginFactory {

    /**
     * Fallback namespace used when simplified YAML options are converted into handler properties.
     */
    private static final String DEFAULT_KEY = "default";

    /**
     * Constructs a new MapperPluginFactory instance.
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
        List<MapperHandler> handlers = new ArrayList<>();
        if (options != null) {
            Properties resolved = options.resolveConfigurationProperties();
            // Handler execution order is critical. The order determines the SQL modification sequence.
            // Execution order: Operation Check -> Table Prefix -> Tenant Vector -> Visible Vector -> Populate
            // -> Pagination -> Audit.
            configureOperation(options, handlers);
            configurePrefix(options, providers, resolved, handlers);
            configureTenant(options, providers, resolved, handlers);
            configureVisible(options, providers, resolved, handlers);
            configurePopulate(options, providers, resolved, handlers);
            configurePagination(options, handlers);
            configureAudit(options, providers, resolved, handlers);
        }

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
     * @param handlers handler list to update
     */
    private static void configureOperation(MapperOptions options, List<MapperHandler> handlers) {
        MapperOptions.OperationOptions operationOptions = options.getOperation();
        if (operationOptions != null && !operationOptions.isEnabled()) {
            Logger.info(false, "Mapper", "Operation handler is disabled by configuration");
            return;
        }

        OperationHandler<?> handler = new OperationHandler<>();
        if (operationOptions != null) {
            handler.setStrictMode(operationOptions.isStrictMode());
        }
        handlers.add(handler);
        Logger.info(false, "Mapper", "Operation handler configured successfully");
    }

    /**
     * Configures MyBatis pagination and adds the {@link PageHandler}.
     * <p>
     * Pagination uses top-level mapper options, not per-datasource configuration. Pagination settings are global across
     * all datasources.
     *
     * @param options  mapper runtime options
     * @param handlers handler list to update
     */
    private static void configurePagination(MapperOptions options, List<MapperHandler> handlers) {
        Properties props = new Properties();
        if (StringKit.isNotEmpty(options.getAutoDelimitKeywords())) {
            props.setProperty(Args.PAGE_AUTO_DELIMIT_KEYWORDS, options.getAutoDelimitKeywords());
        }
        if (StringKit.isNotEmpty(options.getReasonable())) {
            props.setProperty(Args.PAGE_REASONABLE, options.getReasonable());
        }
        if (StringKit.isNotEmpty(options.getSupportMethodsArguments())) {
            props.setProperty(Args.PAGE_SUPPORT_METHOD_ARGUMENTS, options.getSupportMethodsArguments());
        }
        if (StringKit.isNotEmpty(options.getParams())) {
            props.setProperty(Args.PAGE_PARAMS, options.getParams());
        }

        PageHandler<?> pageHandler = new PageHandler<>();
        pageHandler.setProperties(props);
        handlers.add(pageHandler);
        Logger.info(false, "Mapper", "Pagination handler configured");
    }

    /**
     * Configures multi-tenancy properties and adds the {@link TenantHandler}.
     * <p>
     * Configuration priority:
     * </p>
     * <ol>
     * <li>Provider.getConfig() - if provider exists and returns non-null</li>
     * <li>Simplified YAML config ({@code bus.mapper.tenant.*})</li>
     * <li>Configuration properties - datasource-specific > shared</li>
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
        MapperOptions.PrefixOptions prefixOptions = options.getPrefix();
        if (tenantOptions != null && !tenantOptions.isEnabled()) {
            Logger.info(false, "Mapper", "Tenant handler is disabled by configuration");
            return;
        }

        boolean hasSimplifiedConfig = tenantOptions != null;
        boolean hasConfigFile = hasConfiguration(resolved);
        if (!hasSimplifiedConfig && !hasConfigFile) {
            return;
        }

        Properties props = new Properties();
        if (hasSimplifiedConfig) {
            Logger.info(false, "Mapper", "Loading tenant config from simplified YAML configuration");
            props.setProperty(
                    DEFAULT_KEY + Symbol.DOT + Args.TENANT_KEY + Symbol.DOT + Args.TENANT_COLUMN,
                    tenantOptions.getColumn() != null ? tenantOptions.getColumn() : Args.TENANT_ID);
            if (StringKit.isNotEmpty(tenantOptions.getIgnore())) {
                props.setProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.TENANT_KEY + Symbol.DOT + Args.PROP_IGNORE,
                        tenantOptions.getIgnore());
            }
            if (prefixOptions != null && StringKit.isNotEmpty(prefixOptions.getPrefix())) {
                props.setProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT + Args.TABLE_PREFIX,
                        prefixOptions.getPrefix());
            }
        } else {
            Logger.info(false, "Mapper", "Loading tenant config from configuration file");
            props.putAll(resolved);
        }

        TenantProvider provider = providers != null ? providers.getTenantProvider() : null;
        if (provider != null) {
            Logger.info(false, "Mapper", "TenantProvider instance found");
            TenantConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                Logger.info(false, "Mapper", "Using tenant config from Provider.getConfig()");
                handlers.add(new TenantHandler<>(withTablePrefix(providerConfig, props)));
                return;
            }
            props.put(Args.PROVIDER_KEY, provider);
        }

        TenantHandler<?> handler = new TenantHandler<>();
        if (handler.setProperties(props)) {
            handlers.add(handler);
            Logger.info(false, "Mapper", "Tenant handler configured successfully");
        }
    }

    /**
     * Returns a tenant configuration that contains the table prefix supplied by mapper properties.
     *
     * @param config the tenant configuration returned by a provider
     * @param props  the resolved mapper properties
     * @return the tenant configuration with a table prefix
     */
    private static TenantConfig withTablePrefix(TenantConfig config, Properties props) {
        if (config == null || StringKit.isNotEmpty(config.getTablePrefix()) || props == null) {
            return config;
        }
        String defaultKey = Holder.getDefault();
        String tablePrefix = props.getProperty(
                defaultKey + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT + Args.TABLE_PREFIX,
                props.getProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT + Args.TABLE_PREFIX,
                        props.getProperty(
                                Args.SHARED_KEY + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT + Args.TABLE_PREFIX)));
        if (StringKit.isEmpty(tablePrefix)) {
            return config;
        }
        return TenantConfig.builder().mode(config.getMode()).column(config.getColumn()).ignore(config.getIgnore())
                .ignoreMappers(config.getIgnoreMappers()).tablePrefix(tablePrefix)
                .enableSqlCache(config.isEnableSqlCache()).provider(config.getProvider()).build();
    }

    /**
     * Configures automatic data fill and adds the {@link PopulateHandler}.
     * <p>
     * Configuration priority:
     * </p>
     * <ol>
     * <li>Provider.getConfig() - if provider exists and returns non-null</li>
     * <li>Simplified YAML config ({@code bus.mapper.populate.*})</li>
     * <li>Configuration properties - datasource-specific > shared</li>
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
        if (populateOptions != null && !populateOptions.isEnabled()) {
            Logger.info(false, "Mapper", "Populate handler is disabled by configuration");
            return;
        }

        boolean hasSimplifiedConfig = populateOptions != null;
        boolean hasConfigFile = hasConfiguration(resolved);
        if (!hasSimplifiedConfig && !hasConfigFile) {
            return;
        }

        Properties props = new Properties();
        if (hasSimplifiedConfig) {
            Logger.info(false, "Mapper", "Loading populate config from simplified YAML configuration");
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
        } else {
            Logger.info(false, "Mapper", "Loading populate config from configuration file");
            props.putAll(resolved);
        }

        PopulateProvider provider = providers != null ? providers.getPopulateProvider() : null;
        if (provider != null) {
            Logger.info(false, "Mapper", "PopulateProvider instance found");
            PopulateConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                Logger.info(false, "Mapper", "Using populate config from Provider.getConfig()");
                handlers.add(new PopulateHandler<>(providerConfig));
                return;
            }
            props.put(Args.PROVIDER_KEY, provider);
        }

        PopulateHandler<?> handler = new PopulateHandler<>();
        if (handler.setProperties(props)) {
            handlers.add(handler);
            Logger.info(false, "Mapper", "Populate handler configured successfully");
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
     * <li>Configuration properties - datasource-specific > shared</li>
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
        if (visibleOptions != null && !visibleOptions.isEnabled()) {
            Logger.info(false, "Mapper", "Visible handler is disabled by configuration");
            return;
        }

        boolean hasSimplifiedConfig = visibleOptions != null;
        boolean hasConfigFile = hasConfiguration(resolved);
        if (!hasSimplifiedConfig && !hasConfigFile) {
            return;
        }

        Properties props = new Properties();
        if (hasSimplifiedConfig) {
            Logger.info(false, "Mapper", "Loading visible config from simplified YAML configuration");
            if (StringKit.isNotEmpty(visibleOptions.getIgnore())) {
                props.setProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.VISIBLE_KEY + Symbol.DOT + Args.PROP_IGNORE,
                        visibleOptions.getIgnore());
            }
        } else {
            Logger.info(false, "Mapper", "Loading visible config from configuration file");
            props.putAll(resolved);
        }

        VisibleProvider provider = providers != null ? providers.getVisibleProvider() : null;
        if (provider != null) {
            Logger.info(false, "Mapper", "VisibleProvider instance found");
            VisibleConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                Logger.info(false, "Mapper", "Using visible config from Provider.getConfig()");
                handlers.add(new VisibleHandler<>(providerConfig));
                return;
            }
            props.put(Args.PROVIDER_KEY, provider);
        }

        VisibleHandler<?> handler = new VisibleHandler<>();
        if (handler.setProperties(props)) {
            handlers.add(handler);
            Logger.info(false, "Mapper", "Visible handler configured successfully");
        }
    }

    /**
     * Configures table prefix support and adds the {@link TablePrefixHandler}.
     * <p>
     * Configuration priority:
     * </p>
     * <ol>
     * <li>Provider.getConfig() - if provider exists and returns non-null</li>
     * <li>Simplified YAML config ({@code bus.mapper.prefix.*})</li>
     * <li>Configuration properties - datasource-specific > shared</li>
     * <li>Provider instance only - if provider exists but no provider config is returned</li>
     * <li>Default values inside the handler</li>
     * </ol>
     *
     * @param options   mapper runtime options
     * @param providers runtime provider instances
     * @param resolved  flattened mapper configuration properties
     * @param handlers  handler list to update
     */
    private static void configurePrefix(
            MapperOptions options,
            MapperPluginProviders providers,
            Properties resolved,
            List<MapperHandler> handlers) {
        MapperOptions.PrefixOptions prefixOptions = options.getPrefix();
        if (prefixOptions != null && !prefixOptions.isEnabled()) {
            Logger.info(false, "Mapper", "Prefix handler is disabled by configuration");
            return;
        }

        boolean hasSimplifiedConfig = prefixOptions != null;
        boolean hasConfigFile = hasConfiguration(resolved);
        if (!hasSimplifiedConfig && !hasConfigFile) {
            return;
        }

        Properties props = new Properties();
        if (hasSimplifiedConfig) {
            Logger.info(false, "Mapper", "Loading prefix config from simplified YAML configuration");
            if (StringKit.isNotEmpty(prefixOptions.getPrefix())) {
                props.setProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT + Args.TABLE_PREFIX,
                        prefixOptions.getPrefix());
            }
            if (StringKit.isNotEmpty(prefixOptions.getIgnore())) {
                props.setProperty(
                        DEFAULT_KEY + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT + Args.PROP_IGNORE,
                        prefixOptions.getIgnore());
            }
        } else {
            Logger.info(false, "Mapper", "Loading prefix config from configuration file");
            props.putAll(resolved);
        }

        TablePrefixProvider provider = providers != null ? providers.getPrefixProvider() : null;
        if (provider != null) {
            Logger.info(false, "Mapper", "TablePrefixProvider instance found");
            TablePrefixConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                Logger.info(false, "Mapper", "Using prefix config from Provider.getConfig()");
                handlers.add(new TablePrefixHandler(providerConfig));
                return;
            }
            props.put(Args.PROVIDER_KEY, provider);
        }

        TablePrefixHandler handler = new TablePrefixHandler();
        if (handler.setProperties(props)) {
            handlers.add(handler);
            Logger.info(false, "Mapper", "Prefix handler configured successfully");
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
     * <li>Configuration properties - datasource-specific > shared</li>
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
        if (auditOptions != null && !auditOptions.isEnabled()) {
            Logger.info(false, "Mapper", "Audit handler is disabled by configuration");
            return;
        }

        boolean hasSimplifiedConfig = auditOptions != null;
        boolean hasConfigFile = hasConfiguration(resolved);
        if (!hasSimplifiedConfig && !hasConfigFile) {
            return;
        }

        Properties props = new Properties();
        if (hasSimplifiedConfig) {
            Logger.info(false, "Mapper", "Loading audit config from simplified YAML configuration");
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
        } else {
            Logger.info(false, "Mapper", "Loading audit config from configuration file");
            props.putAll(resolved);
        }

        AuditProvider provider = providers != null ? providers.getAuditProvider() : null;
        if (provider != null) {
            Logger.info(false, "Mapper", "AuditProvider instance found");
            AuditConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                Logger.info(false, "Mapper", "Using audit config from Provider.getConfig()");
                handlers.add(new AuditHandler<>(providerConfig));
                return;
            }
            props.put(Args.PROVIDER_KEY, provider);
        }

        AuditHandler<?> handler = new AuditHandler<>();
        if (handler.setProperties(props)) {
            handlers.add(handler);
            Logger.info(false, "Mapper", "Audit handler configured successfully");
        }
    }

    /**
     * Returns whether the flattened mapper configuration contains any handler configuration.
     *
     * @param properties flattened mapper properties
     * @return {@code true} when the properties contain at least one entry
     */
    private static boolean hasConfiguration(Properties properties) {
        return properties != null && !properties.isEmpty();
    }

}
