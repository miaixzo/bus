/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.plugin.Interceptor;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.handler.MapperHandler;
import org.miaixz.bus.mapper.handler.MybatisInterceptor;
import org.miaixz.bus.mapper.support.audit.AuditConfig;
import org.miaixz.bus.mapper.support.audit.AuditHandler;
import org.miaixz.bus.mapper.support.audit.AuditProvider;
import org.miaixz.bus.mapper.support.operation.OperationHandler;
import org.miaixz.bus.mapper.support.paging.PageHandler;
import org.miaixz.bus.mapper.support.populate.PopulateConfig;
import org.miaixz.bus.mapper.support.populate.PopulateHandler;
import org.miaixz.bus.mapper.support.populate.PopulateProvider;
import org.miaixz.bus.mapper.support.prefix.TablePrefixConfig;
import org.miaixz.bus.mapper.support.prefix.TablePrefixHandler;
import org.miaixz.bus.mapper.support.prefix.TablePrefixProvider;
import org.miaixz.bus.mapper.support.tenant.TenantConfig;
import org.miaixz.bus.mapper.support.tenant.TenantHandler;
import org.miaixz.bus.mapper.support.tenant.TenantProvider;
import org.miaixz.bus.mapper.support.visible.VisibleConfig;
import org.miaixz.bus.mapper.support.visible.VisibleHandler;
import org.miaixz.bus.mapper.support.visible.VisibleProvider;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.spring.SpringBuilder;
import org.miaixz.bus.spring.annotation.PlaceHolderBinder;
import org.springframework.core.env.Environment;

/**
 * A builder for creating and configuring MyBatis {@link Interceptor} instances.
 * <p>
 * This class is responsible for initializing and setting up various interceptor handlers, such as for pagination and
 * multi-tenancy, based on the application's configuration.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapperPluginBuilder {

    /**
     * Builds and configures the primary {@link MybatisInterceptor}.
     *
     * @param environment The Spring {@link Environment} object, used to retrieve configuration properties.
     * @return A fully configured {@link MybatisInterceptor} instance.
     */
    public static MybatisInterceptor build(Environment environment) {
        List<MapperHandler> handlers = new ArrayList<>();

        // Handler execution order is critical! The order determines SQL modification sequence.
        // Execution order: Operation Check → Table Prefix → Tenant Filter → Visible Filter → Populate → Pagination →
        // Audit

        // 1. SQL safety check (must be first to block dangerous operations)
        handlers.add(new OperationHandler());

        if (ObjectKit.isNotEmpty(environment)) {
            // 2. Table prefix (modify table names before adding WHERE conditions)
            configurePrefix(environment, handlers);

            // 3. Tenant filter (mandatory data isolation by tenant_id)
            configureTenant(environment, handlers);

            // 4. Visible filter (data permission control by org_id, dept_id, etc.)
            configureVisible(environment, handlers);

            // 5. Field population (auto-fill created_time, creator, etc. - doesn't modify SQL)
            configurePopulate(environment, handlers);

            // 6. Pagination (must be last SQL modifier to apply LIMIT based on complete WHERE clause)
            configurePagination(environment, handlers);

            // 7. SQL audit (performance monitoring and slow SQL detection - observer only)
            configureAudit(environment, handlers);
        }

        MybatisInterceptor interceptor = new MybatisInterceptor();
        interceptor.setHandlers(handlers);
        return interceptor;
    }

    /**
     * Configures MyBatis pagination and adds the {@link PageHandler}.
     *
     * <p>
     * This method reads pagination configuration from MapperProperties (top-level, not per-datasource). Pagination
     * settings are global across all datasources.
     * </p>
     *
     * @param environment The Spring environment.
     * @param handlers    The list of handlers to add the pagination handler to.
     */
    private static void configurePagination(Environment environment, List<MapperHandler> handlers) {
        // Pagination uses top-level MapperProperties, not per-datasource configuration
        MapperProperties properties = PlaceHolderBinder.bind(environment, MapperProperties.class, GeniusBuilder.MAPPER);
        if (ObjectKit.isEmpty(properties)) {
            return;
        }

        // Build Properties for PageHandler
        Properties props = new Properties();
        if (StringKit.isNotEmpty(properties.getAutoDelimitKeywords())) {
            props.setProperty(Args.PAGE_AUTO_DELIMIT_KEYWORDS, properties.getAutoDelimitKeywords());
        }
        if (StringKit.isNotEmpty(properties.getReasonable())) {
            props.setProperty(Args.PAGE_REASONABLE, properties.getReasonable());
        }
        if (StringKit.isNotEmpty(properties.getSupportMethodsArguments())) {
            props.setProperty(Args.PAGE_SUPPORT_METHOD_ARGUMENTS, properties.getSupportMethodsArguments());
        }
        if (StringKit.isNotEmpty(properties.getParams())) {
            props.setProperty(Args.PAGE_PARAMS, properties.getParams());
        }

        // Create PageHandler and set properties
        PageHandler pageHandler = new PageHandler();
        pageHandler.setProperties(props);
        handlers.add(pageHandler);
        Logger.info(false, "Mapper", "Pagination handler configured");
    }

    /**
     * Configures multi-tenancy properties and adds the {@link TenantHandler}.
     *
     * <p>
     * Configuration priority:
     * </p>
     * <ol>
     * <li>Provider.getConfig() - if provider bean exists and returns non-null</li>
     * <li>Simplified YAML config (bus.mapper.tenant.*)</li>
     * <li>Configuration file (configurationProperties) - datasource-specific > shared</li>
     * <li>Provider bean only - if provider exists but no config file configuration</li>
     * <li>Default values</li>
     * </ol>
     *
     * @param environment The Spring environment.
     * @param handlers    The list of handlers to add the tenant handler to.
     */
    private static void configureTenant(Environment environment, List<MapperHandler> handlers) {
        // Step 3: Load from configuration file (supports both simplified and traditional config)
        MapperProperties properties = PlaceHolderBinder.bind(environment, MapperProperties.class, GeniusBuilder.MAPPER);

        // Check for simplified tenant configuration (bus.mapper.tenant.*)
        MapperProperties.TenantProperties tenantProps = properties != null ? properties.getTenant() : null;

        // Check if explicitly disabled
        if (tenantProps != null && !tenantProps.isEnabled()) {
            Logger.info(false, "Mapper", "Tenant handler is disabled by configuration");
            return;
        }

        boolean hasSimplifiedConfig = tenantProps != null;
        boolean hasConfigFile = ObjectKit.isNotEmpty(properties)
                && ObjectKit.isNotEmpty(properties.getConfigurationProperties());

        // If no config and no provider, return early
        if (!hasSimplifiedConfig && !hasConfigFile) {
            return;
        }

        // If simplified config is enabled, convert to Properties format
        Properties props = new Properties();
        if (hasSimplifiedConfig) {
            Logger.info(false, "Mapper", "Loading tenant config from simplified YAML configuration");
            String defaultKey = "default";
            props.setProperty(
                    defaultKey + Symbol.DOT + Args.TENANT_KEY + Symbol.DOT + Args.TENANT_COLUMN,
                    tenantProps.getColumn() != null ? tenantProps.getColumn() : "tenant_id");
            if (StringKit.isNotEmpty(tenantProps.getIgnore())) {
                props.setProperty(
                        defaultKey + Symbol.DOT + Args.TENANT_KEY + Symbol.DOT + Args.PROP_IGNORE,
                        tenantProps.getIgnore());
            }
        } else if (hasConfigFile) {
            Logger.info(false, "Mapper", "Loading tenant config from configuration file");
            props.putAll(properties.getConfigurationProperties());
        }

        // Step 1: Try to get TenantProvider Bean
        TenantProvider provider = null;
        try {
            provider = SpringBuilder.getBean(TenantProvider.class);
        } catch (Exception e) {
            // No provider bean
        }

        // Step 2: Use Provider.getConfig() if available
        if (provider != null) {
            Logger.info(false, "Mapper", "TenantProvider bean found");
            TenantConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                Logger.info(false, "Mapper", "Using tenant config from Provider.getConfig()");
                handlers.add(new TenantHandler(providerConfig));
                return;
            }
        }

        if (provider != null) {
            props.put(Args.PROVIDER_KEY, provider);
        }

        // Create handler and set properties
        TenantHandler<?> handler = new TenantHandler<>();
        if (handler.setProperties(props)) {
            handlers.add(handler);
            Logger.info(false, "Mapper", "Tenant handler configured successfully");
        }
    }

    /**
     * Configures automatic data fill and adds the {@link PopulateHandler}.
     *
     * <p>
     * Configuration priority:
     * </p>
     * <ol>
     * <li>Provider.getConfig() - if provider bean exists and returns non-null</li>
     * <li>Simplified YAML config (bus.mapper.populate.*)</li>
     * <li>Configuration file (configurationProperties) - datasource-specific > shared</li>
     * <li>Provider bean only - if provider exists but no config file configuration</li>
     * <li>Default values</li>
     * </ol>
     *
     * @param environment The Spring environment.
     * @param handlers    The list of handlers to add the data fill handler to.
     */
    private static void configurePopulate(Environment environment, List<MapperHandler> handlers) {
        // Step 3: Load from configuration file (supports both simplified and traditional config)
        MapperProperties properties = PlaceHolderBinder.bind(environment, MapperProperties.class, GeniusBuilder.MAPPER);

        // Check for simplified populate configuration (bus.mapper.populate.*)
        MapperProperties.PopulateProperties populateProps = properties != null ? properties.getPopulate() : null;

        // Check if explicitly disabled
        if (populateProps != null && !populateProps.isEnabled()) {
            Logger.info(false, "Mapper", "Populate handler is disabled by configuration");
            return;
        }

        boolean hasSimplifiedConfig = populateProps != null;
        boolean hasConfigFile = ObjectKit.isNotEmpty(properties)
                && ObjectKit.isNotEmpty(properties.getConfigurationProperties());

        // If no config and no provider, return early
        if (!hasSimplifiedConfig && !hasConfigFile) {
            return;
        }

        // If simplified config is enabled, convert to Properties format
        Properties props = new Properties();
        if (hasSimplifiedConfig) {
            Logger.info(false, "Mapper", "Loading populate config from simplified YAML configuration");
            String defaultKey = "default";
            props.setProperty(
                    defaultKey + Symbol.DOT + Args.POPULATE_KEY + Symbol.DOT + Args.POPULATE_CREATED,
                    String.valueOf(populateProps.isCreated()));
            props.setProperty(
                    defaultKey + Symbol.DOT + Args.POPULATE_KEY + Symbol.DOT + Args.POPULATE_MODIFIED,
                    String.valueOf(populateProps.isModified()));
            props.setProperty(
                    defaultKey + Symbol.DOT + Args.POPULATE_KEY + Symbol.DOT + Args.POPULATE_CREATOR,
                    String.valueOf(populateProps.isCreator()));
            props.setProperty(
                    defaultKey + Symbol.DOT + Args.POPULATE_KEY + Symbol.DOT + Args.POPULATE_MODIFIER,
                    String.valueOf(populateProps.isModifier()));
        } else if (hasConfigFile) {
            Logger.info(false, "Mapper", "Loading populate config from configuration file");
            props.putAll(properties.getConfigurationProperties());
        }

        // Step 1: Try to get PopulateProvider Bean
        PopulateProvider provider = null;
        try {
            provider = SpringBuilder.getBean(PopulateProvider.class);
        } catch (Exception e) {
            // No provider bean
        }

        // Step 2: Use Provider.getConfig() if available
        if (provider != null) {
            Logger.info(false, "Mapper", "PopulateProvider bean found");
            PopulateConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                Logger.info(false, "Mapper", "Using populate config from Provider.getConfig()");
                handlers.add(new PopulateHandler(providerConfig));
                return;
            }
        }

        if (provider != null) {
            props.put(Args.PROVIDER_KEY, provider);
        }

        // Create handler and set properties
        PopulateHandler<?> handler = new PopulateHandler<>();
        if (handler.setProperties(props)) {
            handlers.add(handler);
            Logger.info(false, "Mapper", "Populate handler configured successfully");
        }
    }

    /**
     * Configures data perimeter control and adds the {@link VisibleHandler}.
     *
     * <p>
     * Configuration priority:
     * </p>
     * <ol>
     * <li>Provider.getConfig() - if provider bean exists and returns non-null</li>
     * <li>Simplified YAML config (bus.mapper.visible.*)</li>
     * <li>Configuration file (configurationProperties) - datasource-specific > shared</li>
     * <li>Provider bean only - if provider exists but no config file configuration</li>
     * <li>Default values</li>
     * </ol>
     *
     * @param environment The Spring environment.
     * @param handlers    The list of handlers to add the perimeter handler to.
     */
    private static void configureVisible(Environment environment, List<MapperHandler> handlers) {
        // Step 3: Load from configuration file (supports both simplified and traditional config)
        MapperProperties properties = PlaceHolderBinder.bind(environment, MapperProperties.class, GeniusBuilder.MAPPER);

        // Check for simplified visible configuration (bus.mapper.visible.*)
        MapperProperties.VisibleProperties visibleProps = properties != null ? properties.getVisible() : null;

        // Check if explicitly disabled
        if (visibleProps != null && !visibleProps.isEnabled()) {
            Logger.info(false, "Mapper", "Visible handler is disabled by configuration");
            return;
        }

        boolean hasSimplifiedConfig = visibleProps != null;
        boolean hasConfigFile = ObjectKit.isNotEmpty(properties)
                && ObjectKit.isNotEmpty(properties.getConfigurationProperties());

        // If no config and no provider, return early
        if (!hasSimplifiedConfig && !hasConfigFile) {
            return;
        }

        // If simplified config is enabled, convert to Properties format
        Properties props = new Properties();
        if (hasSimplifiedConfig) {
            Logger.info(false, "Mapper", "Loading visible config from simplified YAML configuration");
            String defaultKey = "default";
            if (StringKit.isNotEmpty(visibleProps.getIgnore())) {
                props.setProperty(
                        defaultKey + Symbol.DOT + Args.VISIBLE_KEY + Symbol.DOT + Args.PROP_IGNORE,
                        visibleProps.getIgnore());
            }
        } else if (hasConfigFile) {
            Logger.info(false, "Mapper", "Loading visible config from configuration file");
            props.putAll(properties.getConfigurationProperties());
        }

        // Step 1: Try to get VisibleProvider Bean
        VisibleProvider provider = null;
        try {
            provider = SpringBuilder.getBean(VisibleProvider.class);
        } catch (Exception e) {
            // No provider bean
        }

        // Step 2: Use Provider.getConfig() if available
        if (provider != null) {
            Logger.info(false, "Mapper", "VisibleProvider bean found");
            VisibleConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                Logger.info(false, "Mapper", "Using visible config from Provider.getConfig()");
                handlers.add(new VisibleHandler(providerConfig));
                return;
            }
        }

        if (provider != null) {
            props.put(Args.PROVIDER_KEY, provider);
        }

        // Create handler and set properties
        VisibleHandler<?> handler = new VisibleHandler<>();
        if (handler.setProperties(props)) {
            handlers.add(handler);
            Logger.info(false, "Mapper", "Visible handler configured successfully");
        }
    }

    /**
     * Configures table prefix support and adds the {@link TablePrefixHandler}.
     *
     * <p>
     * Configuration priority:
     * </p>
     * <ol>
     * <li>Provider.getConfig() - if provider bean exists and returns non-null</li>
     * <li>Simplified YAML config (bus.mapper.prefix.*)</li>
     * <li>Configuration file (configurationProperties) - datasource-specific > shared</li>
     * <li>Provider bean only - if provider exists but no config file configuration</li>
     * <li>Default values</li>
     * </ol>
     *
     * @param environment The Spring environment.
     * @param handlers    The list of handlers to add the prefix handler to.
     */
    private static void configurePrefix(Environment environment, List<MapperHandler> handlers) {
        // Step 3: Load from configuration file (supports both simplified and traditional config)
        MapperProperties properties = PlaceHolderBinder.bind(environment, MapperProperties.class, GeniusBuilder.MAPPER);

        // Check for simplified prefix configuration (bus.mapper.prefix.*)
        MapperProperties.PrefixProperties prefixProps = properties != null ? properties.getPrefix() : null;

        // Check if explicitly disabled
        if (prefixProps != null && !prefixProps.isEnabled()) {
            Logger.info(false, "Mapper", "Prefix handler is disabled by configuration");
            return;
        }

        boolean hasSimplifiedConfig = prefixProps != null;
        boolean hasConfigFile = ObjectKit.isNotEmpty(properties)
                && ObjectKit.isNotEmpty(properties.getConfigurationProperties());

        // If no config and no provider, return early
        if (!hasSimplifiedConfig && !hasConfigFile) {
            return;
        }

        // If simplified config is enabled, convert to Properties format
        Properties props = new Properties();
        if (hasSimplifiedConfig) {
            Logger.info(false, "Mapper", "Loading prefix config from simplified YAML configuration");
            String defaultKey = "default";
            if (StringKit.isNotEmpty(prefixProps.getPrefix())) {
                props.setProperty(
                        defaultKey + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT + Args.TABLE_PREFIX,
                        prefixProps.getPrefix());
            }
            if (StringKit.isNotEmpty(prefixProps.getIgnore())) {
                props.setProperty(
                        defaultKey + Symbol.DOT + Args.TABLE_KEY + Symbol.DOT + Args.PROP_IGNORE,
                        prefixProps.getIgnore());
            }
        } else if (hasConfigFile) {
            Logger.info(false, "Mapper", "Loading prefix config from configuration file");
            props.putAll(properties.getConfigurationProperties());
        }

        // Step 1: Try to get TablePrefixProvider Bean
        TablePrefixProvider provider = null;
        try {
            provider = SpringBuilder.getBean(TablePrefixProvider.class);
        } catch (Exception e) {
            // No provider bean
        }

        // Step 2: Use Provider.getConfig() if available
        if (provider != null) {
            Logger.info(false, "Mapper", "TablePrefixProvider bean found");
            TablePrefixConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                Logger.info(false, "Mapper", "Using prefix config from Provider.getConfig()");
                handlers.add(new TablePrefixHandler(providerConfig));
                return;
            }
        }

        if (provider != null) {
            props.put(Args.PROVIDER_KEY, provider);
        }

        // Create handler and set properties
        TablePrefixHandler handler = new TablePrefixHandler();
        boolean configured = handler.setProperties(props);
        if (configured) {
            handlers.add(handler);
            Logger.info(false, "Mapper", "Prefix handler configured successfully");
        }
    }

    /**
     * Configures SQL audit and adds the {@link AuditHandler}.
     *
     * <p>
     * Configuration priority:
     * </p>
     * <ol>
     * <li>Provider.getConfig() - if provider bean exists and returns non-null</li>
     * <li>Simplified YAML config (bus.mapper.audit.*)</li>
     * <li>Configuration file (configurationProperties) - datasource-specific > shared</li>
     * <li>Provider bean only - if provider exists but no config file configuration</li>
     * <li>Default values</li>
     * </ol>
     *
     * @param environment The Spring environment.
     * @param handlers    The list of handlers to add the audit handler to.
     */
    private static void configureAudit(Environment environment, List<MapperHandler> handlers) {
        // Step 3: Load from configuration file (supports both simplified and traditional config)
        MapperProperties properties = PlaceHolderBinder.bind(environment, MapperProperties.class, GeniusBuilder.MAPPER);

        // Check for simplified audit configuration (bus.mapper.audit.*)
        MapperProperties.AuditProperties auditProps = properties != null ? properties.getAudit() : null;

        // Check if explicitly disabled
        if (auditProps != null && !auditProps.isEnabled()) {
            Logger.info(false, "Mapper", "Audit handler is disabled by configuration");
            return;
        }

        boolean hasSimplifiedConfig = auditProps != null;
        boolean hasConfigFile = ObjectKit.isNotEmpty(properties)
                && ObjectKit.isNotEmpty(properties.getConfigurationProperties());

        // If no config and no provider, return early
        if (!hasSimplifiedConfig && !hasConfigFile) {
            return;
        }

        // If simplified config is enabled, convert to Properties format
        Properties props = new Properties();
        if (hasSimplifiedConfig) {
            Logger.info(false, "Mapper", "Loading audit config from simplified YAML configuration");
            String defaultKey = "default";
            props.setProperty(
                    defaultKey + Symbol.DOT + Args.AUDIT_KEY + Symbol.DOT + Args.AUDIT_SLOW_SQL_THRESHOLD,
                    String.valueOf(auditProps.getSlowSqlThreshold()));
            props.setProperty(
                    defaultKey + Symbol.DOT + Args.AUDIT_KEY + Symbol.DOT + Args.AUDIT_LOG_PARAMETERS,
                    String.valueOf(auditProps.isLogParameters()));
            props.setProperty(
                    defaultKey + Symbol.DOT + Args.AUDIT_KEY + Symbol.DOT + Args.AUDIT_LOG_RESULTS,
                    String.valueOf(auditProps.isLogResults()));
            props.setProperty(
                    defaultKey + Symbol.DOT + Args.AUDIT_KEY + Symbol.DOT + Args.AUDIT_LOG_ALL_SQL,
                    String.valueOf(auditProps.isLogAllSql()));
            props.setProperty(
                    defaultKey + Symbol.DOT + Args.AUDIT_KEY + Symbol.DOT + Args.AUDIT_PRINT_CONSOLE,
                    String.valueOf(auditProps.isPrintConsole()));
        } else if (hasConfigFile) {
            Logger.info(false, "Mapper", "Loading audit config from configuration file");
            props.putAll(properties.getConfigurationProperties());
        }

        // Step 1: Try to get AuditProvider Bean
        AuditProvider provider = null;
        try {
            provider = SpringBuilder.getBean(AuditProvider.class);
        } catch (Exception e) {
            // No provider bean
        }

        // Step 2: Use Provider.getConfig() if available
        if (provider != null) {
            Logger.info(false, "Mapper", "AuditProvider bean found");
            AuditConfig providerConfig = provider.getConfig();
            if (providerConfig != null) {
                Logger.info(false, "Mapper", "Using audit config from Provider.getConfig()");
                handlers.add(new AuditHandler(providerConfig));
                return;
            }
        }

        if (provider != null) {
            props.put(Args.PROVIDER_KEY, provider);
        }

        // Create handler and set properties
        AuditHandler<?> handler = new AuditHandler<>();
        if (handler.setProperties(props)) {
            handlers.add(handler);
            Logger.info(false, "Mapper", "Audit handler configured successfully");
        }
    }

}
