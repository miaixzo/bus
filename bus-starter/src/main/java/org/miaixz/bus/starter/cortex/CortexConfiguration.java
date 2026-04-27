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
package org.miaixz.bus.starter.cortex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.Factory;
import org.miaixz.bus.cache.Hybrid;
import org.miaixz.bus.cache.Options;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.*;
import org.miaixz.bus.cortex.bridge.VortexBridge;
import org.miaixz.bus.cortex.builtin.event.LoggingWatchListener;
import org.miaixz.bus.cortex.builtin.event.SimpleSettingPublisher;
import org.miaixz.bus.cortex.setting.SettingPublisher;
import org.miaixz.bus.cortex.setting.SettingEnforcer;
import org.miaixz.bus.cortex.setting.SettingEnforcerGuardStrategy;
import org.miaixz.bus.cortex.setting.DefaultCurator;
import org.miaixz.bus.cortex.setting.item.GrayRuleMatcher;
import org.miaixz.bus.cortex.setting.curator.*;
import org.miaixz.bus.cortex.setting.delivery.ItemExportService;
import org.miaixz.bus.cortex.setting.delivery.ItemQueryService;
import org.miaixz.bus.cortex.setting.delivery.RuntimeItemOverlayService;
import org.miaixz.bus.cortex.setting.item.ItemStore;
import org.miaixz.bus.cortex.setting.item.StoreBackedItemStore;
import org.miaixz.bus.cortex.setting.item.revision.ItemRevisionStore;
import org.miaixz.bus.cortex.setting.secret.NoOpSecretCodec;
import org.miaixz.bus.cortex.setting.secret.SecretCodec;
import org.miaixz.bus.cortex.setting.secret.SecretMasker;
import org.miaixz.bus.cortex.guard.CortexGuard;
import org.miaixz.bus.cortex.guard.GuardStrategy;
import org.miaixz.bus.cortex.guard.token.TokenGuardConfig;
import org.miaixz.bus.cortex.guard.token.TokenGuardStrategy;
import org.miaixz.bus.cortex.magic.AuditLogger;
import org.miaixz.bus.cortex.magic.watch.WatchManager;
import org.miaixz.bus.cortex.registry.*;
import org.miaixz.bus.cortex.registry.api.ApiAssets;
import org.miaixz.bus.cortex.registry.api.ApiRegistry;
import org.miaixz.bus.cortex.registry.api.ApiRegistryStore;
import org.miaixz.bus.cortex.registry.mcp.McpAssets;
import org.miaixz.bus.cortex.registry.mcp.McpRegistry;
import org.miaixz.bus.cortex.registry.mcp.McpRegistryStore;
import org.miaixz.bus.cortex.registry.prompt.PromptAssets;
import org.miaixz.bus.cortex.registry.prompt.PromptRegistry;
import org.miaixz.bus.cortex.registry.prompt.PromptRegistryStore;
import org.miaixz.bus.cortex.version.VersionRegistry;
import org.miaixz.bus.cortex.version.VersionStore;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import jakarta.annotation.Resource;

/**
 * Auto-configuration for Bus Cortex starter wiring.
 * <p>
 * The starter exposes the shared bus-cortex components, then initializes the static {@link Cortex} facade so
 * application code and Spring-managed beans resolve the same handles. Starter defaults come from
 * {@link CortexProperties}, while cache backend settings may also be bound directly from the Spring
 * {@link Environment}.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(CortexProperties.class)
@ConditionalOnProperty(prefix = "bus.cortex", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CortexConfiguration {

    /**
     * Injected cache configuration properties.
     */
    @Resource
    CortexProperties properties;

    /**
     * Creates the Spring Boot auto-configuration container for Bus Cortex.
     */
    public CortexConfiguration() {

    }

    /**
     * Exposes the shared cache factory when the host application does not provide one.
     *
     * @return cache factory
     */
    @Bean
    @ConditionalOnMissingBean(Factory.class)
    public Factory factory() {
        return new Factory();
    }

    /**
     * Creates the default Cortex cache through the shared cache factory.
     * <p>
     * Cortex first resolves {@code bus.cortex.cache.*}. When no Cortex-specific backend settings are present, it falls
     * back to {@code bus.cache.*}. When neither prefix is present, Cortex falls back to its starter defaults.
     * </p>
     *
     * @param factory     shared cache factory
     * @param environment Spring environment used for property binding
     * @return default Cortex cache
     */
    @Bean("cortexCache")
    @ConditionalOnMissingBean(name = "cortexCache")
    public CacheX<String, Object> cortexCache(Factory factory, Environment environment) {
        if (hasCortexCacheConfiguration(environment)) {
            return factory.initializeExtended(bindCortexCacheProperties(environment));
        }
        if (hasGlobalCacheConfiguration(environment)) {
            return factory.initializeExtended(bindGlobalCacheProperties(environment));
        }
        return factory.initializeExtended(defaultCortexCacheProperties());
    }

    /**
     * Creates the shared watch manager for registry and setting subscriptions.
     *
     * @param cache shared cache abstraction
     * @return watch manager
     */
    @Bean
    public WatchManager watchManager(@Qualifier("cortexCache") CacheX cache) {
        return new WatchManager(cache(cache), properties.requireMaxWatchesPerNamespace(),
                properties.requireWatchExpireMs());
    }

    /**
     * Creates the secret masker used by management-facing APIs.
     *
     * @return secret masker
     */
    @Bean
    public SecretMasker secretMasker() {
        return new SecretMasker();
    }

    /**
     * Creates the default secret codec used by setting publishing and resolution.
     *
     * @return secret codec
     */
    @Bean
    public SecretCodec secretCodec() {
        return new NoOpSecretCodec();
    }

    /**
     * Exposes default token guard configuration from starter properties.
     *
     * @return token guard config
     */
    @Bean
    @ConditionalOnProperty(prefix = "bus.cortex.guard", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TokenGuardConfig tokenGuardConfig() {
        return properties.getGuard().getToken();
    }

    /**
     * Creates the default token guard strategy when guard support is enabled.
     *
     * @param tokenGuardConfig token guard configuration
     * @return token guard strategy
     */
    @Bean
    @ConditionalOnMissingBean(TokenGuardStrategy.class)
    @ConditionalOnProperty(prefix = "bus.cortex.guard", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TokenGuardStrategy tokenGuardStrategy(TokenGuardConfig tokenGuardConfig) {
        return new TokenGuardStrategy(tokenGuardConfig);
    }

    /**
     * Adapts an application-provided setting enforcer into the shared guard chain.
     *
     * @param settingEnforcer setting relation enforcer
     * @return setting guard strategy
     */
    @Bean
    @ConditionalOnBean(SettingEnforcer.class)
    @ConditionalOnMissingBean(SettingEnforcerGuardStrategy.class)
    public SettingEnforcerGuardStrategy settingEnforcerGuardStrategy(SettingEnforcer settingEnforcer) {
        return new SettingEnforcerGuardStrategy(settingEnforcer);
    }

    /**
     * Creates the shared Cortex guard used by registry, setting, and version mutation paths.
     *
     * @param strategies guard strategies discovered from the application context
     * @return shared Cortex guard
     */
    @Bean
    @ConditionalOnMissingBean(CortexGuard.class)
    public CortexGuard cortexGuard(List<GuardStrategy> strategies) {
        return new CortexGuard(strategies);
    }

    /**
     * Creates the audit logger when explicitly enabled.
     *
     * @param cache shared cache abstraction
     * @return audit logger
     */
    @Bean
    @ConditionalOnProperty(prefix = "bus.cortex.audit", name = "enabled", havingValue = "true")
    public AuditLogger auditLogger(@Qualifier("cortexCache") CacheX cache) {
        return new AuditLogger(cache(cache));
    }

    /**
     * Registers a builtin logging watch listener when watch logging is enabled.
     *
     * @param watchManager watch manager
     * @return logging watch listener
     */
    @Bean
    @ConditionalOnProperty(prefix = "bus.cortex.watch", name = "logging-enabled", havingValue = "true")
    public LoggingWatchListener loggingWatchListener(WatchManager watchManager) {
        LoggingWatchListener listener = new LoggingWatchListener();
        watchManager.addGlobalListener(listener);
        return listener;
    }

    /**
     * Creates the API registry backed by cache and, when available, the shared durable registry store.
     *
     * @param cache               shared cache abstraction
     * @param watchManager        watch manager
     * @param storeProvider       optional registry store provider
     * @param sharedStoreProvider optional shared registry store provider
     * @param syncListeners       post-commit API listeners
     * @return API registry
     */
    @Bean
    @ConditionalOnMissingBean(ApiRegistry.class)
    public ApiRegistry apiRegistry(
            @Qualifier("cortexCache") CacheX cache,
            WatchManager watchManager,
            ObjectProvider<ApiRegistryStore> storeProvider,
            ObjectProvider<RegistryStore<Assets>> sharedStoreProvider,
            List<Listener<RegistryChange<ApiAssets>>> syncListeners) {
        RegistryStore<ApiAssets> store = storeProvider.getIfAvailable();
        if (store == null) {
            store = typedStore(sharedStoreProvider.getIfAvailable(), ApiAssets.class);
        }
        return new ApiRegistry(cache(cache), watchManager, store, syncListeners);
    }

    /**
     * Creates the MCP registry backed by cache and, when available, the shared durable registry store.
     *
     * @param cache               shared cache abstraction
     * @param watchManager        watch manager
     * @param storeProvider       optional registry store provider
     * @param sharedStoreProvider optional shared registry store provider
     * @return MCP registry
     */
    @Bean
    @ConditionalOnMissingBean(McpRegistry.class)
    public McpRegistry mcpRegistry(
            @Qualifier("cortexCache") CacheX cache,
            WatchManager watchManager,
            ObjectProvider<McpRegistryStore> storeProvider,
            ObjectProvider<RegistryStore<Assets>> sharedStoreProvider) {
        RegistryStore<McpAssets> store = storeProvider.getIfAvailable();
        if (store == null) {
            store = typedStore(sharedStoreProvider.getIfAvailable(), McpAssets.class);
        }
        return new McpRegistry(cache(cache), watchManager, store);
    }

    /**
     * Creates the prompt registry backed by cache and, when available, the shared durable registry store.
     *
     * @param cache               shared cache abstraction
     * @param watchManager        watch manager
     * @param storeProvider       optional registry store provider
     * @param sharedStoreProvider optional shared registry store provider
     * @return prompt registry
     */
    @Bean
    @ConditionalOnMissingBean(PromptRegistry.class)
    public PromptRegistry promptRegistry(
            @Qualifier("cortexCache") CacheX cache,
            WatchManager watchManager,
            ObjectProvider<PromptRegistryStore> storeProvider,
            ObjectProvider<RegistryStore<Assets>> sharedStoreProvider) {
        RegistryStore<PromptAssets> store = storeProvider.getIfAvailable();
        if (store == null) {
            store = typedStore(sharedStoreProvider.getIfAvailable(), PromptAssets.class);
        }
        return new PromptRegistry(cache(cache), watchManager, store);
    }

    /**
     * Creates the version registry backed by cache and, when available, a dedicated durable version store.
     *
     * @param cache                shared cache abstraction
     * @param watchManager         watch manager
     * @param versionStoreProvider optional version store provider
     * @param cortexGuard          optional Cortex guard provider
     * @return version registry
     */
    @Bean("versionRegistry")
    @ConditionalOnMissingBean(VersionRegistry.class)
    @ConditionalOnProperty(prefix = "bus.cortex.version", name = "enabled", havingValue = "true")
    public VersionRegistry versionRegistry(
            @Qualifier("cortexCache") CacheX cache,
            WatchManager watchManager,
            ObjectProvider<VersionStore> versionStoreProvider,
            ObjectProvider<CortexGuard> cortexGuard) {
        return new VersionRegistry(cache(cache), watchManager, versionStoreProvider.getIfAvailable(),
                cortexGuard.getIfAvailable());
    }

    /**
     * Creates the registry refresh service for control-plane cache maintenance.
     *
     * @param runtime grouped Cortex runtime bundle
     * @return registry refresh service
     */
    @Bean
    public RegistryRefreshService registryRefreshService(Cortex.Runtime runtime) {
        return new RegistryRefreshService((ApiRegistry) runtime.api(), (McpRegistry) runtime.mcp(),
                (PromptRegistry) runtime.prompt());
    }

    /**
     * Creates the registry admin service for control-plane queries and updates.
     *
     * @param runtime     grouped Cortex runtime bundle
     * @param cortexGuard optional Cortex guard provider
     * @return registry admin service
     */
    @Bean
    public RegistryControlService registryAdminService(
            Cortex.Runtime runtime,
            ObjectProvider<CortexGuard> cortexGuard) {
        return new RegistryControlService((ApiRegistry) runtime.api(), (McpRegistry) runtime.mcp(),
                (PromptRegistry) runtime.prompt(), cortexGuard.getIfAvailable());
    }

    /**
     * Creates the remote Vortex bridge listener when push+pull mode is enabled.
     *
     * @return bridge listener
     */
    @Bean(name = "cortexBinding", destroyMethod = "close")
    @ConditionalOnProperty(prefix = "bus.cortex.bridge", name = "mode", havingValue = "push+pull")
    public VortexBridge vortexBridge() {
        CortexProperties.Bridge bridge = properties.getBridge();
        String syncUrl = bridge.requireUrl();
        int maxRetries = bridge.requireMaxRetries();
        String source = bridge.resolveSource();
        if (StringKit.isEmpty(source)) {
            return new VortexBridge(syncUrl, maxRetries);
        }
        return new VortexBridge(syncUrl, maxRetries, source);
    }

    /**
     * Creates the store-backed current-state setting coordinator.
     *
     * @param cache         shared cache abstraction
     * @param storeProvider optional durable current-state store
     * @return store-backed current-state setting coordinator
     */
    @Bean
    @ConditionalOnMissingBean(StoreBackedItemStore.class)
    public StoreBackedItemStore storeBackedSettingStore(
            @Qualifier("cortexCache") CacheX cache,
            ObjectProvider<ItemStore> storeProvider) {
        ItemStore store = storeProvider.getIfAvailable();
        if (store == null && properties.isServerEnabled() && properties.isSettingEnabled()) {
            throw new IllegalStateException(
                    "A production SettingStore is required when bus.cortex.server-enabled=true");
        }
        return new StoreBackedItemStore(cache(cache), store);
    }

    /**
     * Creates the default {@code setting.item.revision} store when the host application does not provide persistent
     * history storage.
     *
     * @param cache shared cache abstraction
     * @return cache-backed {@code setting.item.revision} store
     */
    @Bean
    @ConditionalOnMissingBean(ItemRevisionStore.class)
    public ItemRevisionStore revisionStore(@Qualifier("cortexCache") CacheX cache) {
        return new CacheItemRevisionStore(cache(cache));
    }

    /**
     * Creates the curator application service.
     *
     * @param settingStore    current-state setting store
     * @param revisionStore   revision history store
     * @param watchManager    watch manager
     * @param secretCodec     secret codec
     * @param settingEnforcer optional setting relation enforcer provider
     * @param cortexGuard     optional Cortex guard provider
     * @return curator application service
     */
    @Bean
    public ItemCuratorService settingCuratorService(
            StoreBackedItemStore settingStore,
            ItemRevisionStore revisionStore,
            WatchManager watchManager,
            SecretCodec secretCodec,
            ObjectProvider<SettingEnforcer> settingEnforcer,
            ObjectProvider<CortexGuard> cortexGuard) {
        if (revisionStore instanceof CacheItemRevisionStore && properties.isServerEnabled()
                && properties.isSettingEnabled()) {
            throw new IllegalStateException(
                    "A production ItemRevisionStore is required when bus.cortex.server-enabled=true");
        }
        ItemValueResolver resolver = new ItemValueResolver(settingSourceAdapters(), new GrayRuleMatcher(), secretCodec);
        SettingPublisher settingPublisher = new SettingPublisher(settingStore, revisionStore, watchManager, secretCodec,
                properties.requireMaxSettingVersions());
        return new ItemCuratorService(settingStore, revisionStore, resolver, settingPublisher,
                settingEnforcer.getIfAvailable(), cortexGuard.getIfAvailable());
    }

    /**
     * Creates the lightweight runtime overlay service backed by the shared cache.
     *
     * @param cache        shared cache abstraction
     * @param watchManager watch manager
     * @return runtime overlay service
     */
    @Bean
    public RuntimeItemOverlayService runtimeSettingOverlayService(
            @Qualifier("cortexCache") CacheX cache,
            WatchManager watchManager) {
        return new RuntimeItemOverlayService(new SimpleSettingPublisher(cache(cache)), watchManager);
    }

    /**
     * Exposes the consumer-facing curator interface.
     *
     * @param settingCuratorService        curator application service
     * @param watchManager                 watch manager
     * @param runtimeSettingOverlayService runtime overlay service
     * @return default curator implementation
     */
    @Bean
    public Curator curator(
            ItemCuratorService settingCuratorService,
            WatchManager watchManager,
            RuntimeItemOverlayService runtimeSettingOverlayService) {
        return new DefaultCurator(settingCuratorService, watchManager, properties.requireNamespace(),
                runtimeSettingOverlayService);
    }

    /**
     * Creates the consumer-facing setting query service.
     *
     * @param settingCuratorService        curator application service
     * @param secretMasker                 secret masker
     * @param runtimeSettingOverlayService runtime overlay service
     * @return setting query service
     */
    @Bean
    public ItemQueryService settingQueryService(
            ItemCuratorService settingCuratorService,
            SecretMasker secretMasker,
            RuntimeItemOverlayService runtimeSettingOverlayService) {
        return new ItemQueryService(settingCuratorService, secretMasker, runtimeSettingOverlayService);
    }

    /**
     * Creates the setting export service.
     *
     * @param settingCuratorService curator application service
     * @return setting export service
     */
    @Bean
    public ItemExportService settingExportService(ItemCuratorService settingCuratorService) {
        return new ItemExportService(settingCuratorService);
    }

    /**
     * Packages the assembled registries and curator into one immutable Cortex runtime bundle.
     *
     * @param apiRegistry     API registry
     * @param mcpRegistry     MCP registry
     * @param promptRegistry  prompt registry
     * @param versionRegistry optional version registry
     * @param curator         curator facade
     * @return Cortex runtime bundle
     */
    @Bean
    public Cortex.Runtime cortexRuntime(
            ApiRegistry apiRegistry,
            McpRegistry mcpRegistry,
            PromptRegistry promptRegistry,
            ObjectProvider<VersionRegistry> versionRegistry,
            Curator curator) {
        VersionRegistry optionalVersionRegistry = versionRegistry.getIfAvailable();
        if (optionalVersionRegistry != null) {
            return Cortex.runtime(apiRegistry, mcpRegistry, promptRegistry, optionalVersionRegistry, curator);
        }
        return Cortex.runtime(apiRegistry, mcpRegistry, promptRegistry, curator);
    }

    /**
     * Binds the assembled registries and curator interface into the static Cortex facade.
     *
     * @param runtime Cortex runtime bundle
     * @return lifecycle handle that clears the static facade when the Spring context stops
     */
    @Bean(destroyMethod = "close")
    public Cortex.RuntimeHandle cortexRuntimeHandle(Cortex.Runtime runtime) {
        return Cortex.bind(runtime);
    }

    /**
     * Creates the default setting source adapters used by the starter.
     *
     * @return ordered setting source adapters
     */
    private static List<ItemSourceAdapter> settingSourceAdapters() {
        return List.of(
                new InlineSourceAdapter(),
                new EnvSourceAdapter(),
                new StoredContentSourceAdapter("JDBC"),
                new StoredContentSourceAdapter("REDIS"),
                new StoredContentSourceAdapter("S3"));
    }

    /**
     * Binds Cortex-specific cache properties onto the starter default cache options.
     *
     * @param environment Spring environment used for property binding
     * @return bound cache options
     */
    private Options bindCortexCacheProperties(Environment environment) {
        Options options = defaultCortexCacheProperties();
        Binder.get(environment).bind(GeniusBuilder.CORTEX + ".cache", Bindable.ofInstance(options));
        return options;
    }

    /**
     * Binds the shared global cache properties when Cortex-specific settings are absent.
     *
     * @param environment Spring environment used for property binding
     * @return bound global cache options
     */
    private Options bindGlobalCacheProperties(Environment environment) {
        return Binder.get(environment).bind(GeniusBuilder.CACHE, Bindable.of(Options.class)).orElseGet(Options::new);
    }

    /**
     * Returns the starter's default Cortex cache options.
     *
     * @return default cache options
     */
    private Options defaultCortexCacheProperties() {
        Options options = new Options();
        options.setType("caffeine");
        options.setMaxSize(Hybrid.DEFAULT_MAXIMUM_SIZE);
        options.setExpire(properties.requireCacheExpireMs());
        return options;
    }

    /**
     * Returns whether Cortex-specific cache backend properties are present.
     *
     * @param environment Spring environment used for property binding
     * @return {@code true} when Cortex cache properties are configured
     */
    private boolean hasCortexCacheConfiguration(Environment environment) {
        return properties.getCache() != null
                || hasCacheBackendConfiguration(environment, GeniusBuilder.CORTEX + ".cache");
    }

    /**
     * Returns whether shared global cache backend properties are present.
     *
     * @param environment Spring environment used for property binding
     * @return {@code true} when global cache properties are configured
     */
    private boolean hasGlobalCacheConfiguration(Environment environment) {
        return hasCacheBackendConfiguration(environment, GeniusBuilder.CACHE);
    }

    /**
     * Checks whether any cache backend property under the given prefix is configured.
     *
     * @param environment Spring environment used for property binding
     * @param prefix      cache-property prefix to inspect
     * @return {@code true} when at least one backend property is bound
     */
    private boolean hasCacheBackendConfiguration(Environment environment, String prefix) {
        Binder binder = Binder.get(environment);
        String[] keys = { "type", "max-size", "expire", "nodes", "redis.host", "redis.port", "redis.password",
                "redis.timeout", "redis.max-active", "redis.max-idle", "redis.min-idle", "redis.nodes" };
        for (String key : keys) {
            if (binder.bind(prefix + "." + key, Bindable.of(String.class)).isBound()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Narrows the raw cache bean used by Spring auto-configuration to Cortex's cache key/value convention.
     *
     * @param cache shared cache bean
     * @return cache view using string keys and object values
     */
    private static CacheX<String, Object> cache(CacheX cache) {
        return (CacheX<String, Object>) cache;
    }

    /**
     * Wraps the shared registry store as a typed Cortex registry store without unchecked casts in bean methods.
     *
     * @param store shared registry store
     * @param type  target asset subtype
     * @param <T>   target asset subtype
     * @return typed store view
     */
    private static <T extends Assets> RegistryStore<T> typedStore(RegistryStore<Assets> store, Class<T> type) {
        return store == null ? null : new TypedRegistryStore<>(store, type);
    }

    /**
     * Type-safe view over the optional durable registry store provided by the host application.
     *
     * @param <T> target asset subtype
     */
    private static final class TypedRegistryStore<T extends Assets> implements RegistryStore<T> {

        /**
         * Shared durable registry store that persists all asset types.
         */
        private final RegistryStore<Assets> delegate;

        /**
         * Expected concrete asset type returned by this store view.
         */
        private final Class<T> type;

        /**
         * Creates a typed view over the shared durable registry store.
         *
         * @param delegate shared durable registry store
         * @param type     expected concrete asset type
         */
        private TypedRegistryStore(RegistryStore<Assets> delegate, Class<T> type) {
            this.delegate = delegate;
            this.type = type;
        }

        /**
         * Persists one typed registry entry through the shared durable store.
         *
         * @param entry typed registry entry
         */
        @Override
        public void save(T entry) {
            delegate.save(entry);
        }

        /**
         * Persists a batch of typed registry entries through the shared durable store.
         *
         * @param entries typed registry entries
         */
        @Override
        public void saveAll(List<T> entries) {
            if (entries == null || entries.isEmpty()) {
                return;
            }
            delegate.saveAll(new ArrayList<>(entries));
        }

        /**
         * Persists one typed registry entry together with a runtime instance snapshot.
         *
         * @param entry    typed registry entry
         * @param instance runtime instance snapshot
         */
        @Override
        public void save(T entry, Instance instance) {
            delegate.save(entry, instance);
        }

        /**
         * Deletes one registry entry from the shared durable store.
         *
         * @param type      asset type
         * @param namespace namespace
         * @param id        durable entry identifier
         */
        @Override
        public void delete(Type type, String namespace, String id) {
            delegate.delete(type, namespace, id);
        }

        /**
         * Deletes one runtime instance snapshot from the shared durable store.
         *
         * @param type        asset type
         * @param namespace   namespace
         * @param method      method name
         * @param version     version identifier
         * @param fingerprint instance fingerprint
         */
        @Override
        public void deleteInstance(
                Type type,
                String namespace,
                String app_id,
                String method,
                String version,
                String fingerprint) {
            delegate.deleteInstance(type, namespace, app_id, method, version, fingerprint);
        }

        /**
         * Finds one registry entry and converts it to the expected typed view.
         *
         * @param type      asset type
         * @param namespace namespace
         * @param id        durable entry identifier
         * @return typed asset, or {@code null} when absent or incompatible
         */
        @Override
        public T find(Type type, String namespace, String id) {
            return typed(delegate.find(type, namespace, id));
        }

        /**
         * Finds one registry entry by method and version and converts it to the expected typed view.
         *
         * @param type      asset type
         * @param namespace namespace
         * @param method    method name
         * @param version   version identifier
         * @return typed asset, or {@code null} when absent or incompatible
         */
        @Override
        public T findByMethodVersion(Type type, String namespace, String app_id, String method, String version) {
            return typed(delegate.findByMethodVersion(type, namespace, app_id, method, version));
        }

        /**
         * Queries registry entries and converts each returned asset to the expected typed view.
         *
         * @param type   asset type
         * @param vector route selector
         * @return typed assets that can be converted successfully
         */
        @Override
        public List<T> query(Type type, Vector vector) {
            List<Assets> assets = delegate.query(type, vector);
            if (assets == null || assets.isEmpty()) {
                return List.of();
            }
            List<T> result = new ArrayList<>(assets.size());
            for (Assets asset : assets) {
                T typed = typed(asset);
                if (typed != null) {
                    result.add(typed);
                }
            }
            return result;
        }

        /**
         * Queries runtime instance snapshots for the given service identity.
         *
         * @param type      asset type
         * @param namespace namespace
         * @param method    method name
         * @param version   version identifier
         * @return matching runtime instances
         */
        @Override
        public List<Instance> queryInstances(
                Type type,
                String namespace,
                String app_id,
                String method,
                String version) {
            return delegate.queryInstances(type, namespace, app_id, method, version);
        }

        /**
         * Returns trait flags exposed by the shared durable store.
         *
         * @return trait flags
         */
        @Override
        public Suite storeCapabilities() {
            return delegate.storeCapabilities();
        }

        /**
         * Returns legacy string-key trait flags for compatibility with older integrations.
         *
         * @return compatibility trait flags
         */
        @Override
        public Map<String, Boolean> capabilities() {
            return storeCapabilities().asMap();
        }

        /**
         * Converts one shared asset snapshot to the target subtype.
         *
         * @param asset raw asset returned by the shared store
         * @return typed asset or {@code null}
         */
        private T typed(Assets asset) {
            if (asset == null) {
                return null;
            }
            Assets decoded = RegistryAssets.normalize(asset);
            if (type.isInstance(decoded)) {
                return type.cast(decoded);
            }
            Assets copied = RegistryAssets.copy(decoded);
            if (type.isInstance(copied)) {
                return type.cast(copied);
            }
            throw new IllegalStateException("Registry store returned " + decoded.getClass().getName() + " but "
                    + type.getName() + " was required");
        }
    }

}
