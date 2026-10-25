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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.Factory;
import org.miaixz.bus.cache.Hybrid;
import org.miaixz.bus.cache.Options;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.*;
import org.miaixz.bus.cortex.bridge.VortexBridge;
import org.miaixz.bus.cortex.builtin.RegistryGenerator;
import org.miaixz.bus.cortex.builtin.SettingGenerator;
import org.miaixz.bus.cortex.builtin.event.LoggingWatchListener;
import org.miaixz.bus.cortex.guard.CortexGuard;
import org.miaixz.bus.cortex.guard.GuardStrategy;
import org.miaixz.bus.cortex.guard.token.TokenGuardConfig;
import org.miaixz.bus.cortex.guard.token.TokenGuardStrategy;
import org.miaixz.bus.cortex.magic.AuditLogger;
import org.miaixz.bus.cortex.magic.event.CortexChangeLogStore;
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
import org.miaixz.bus.cortex.release.PublicationStore;
import org.miaixz.bus.cortex.release.ReleasePublisher;
import org.miaixz.bus.cortex.release.ReleaseRegistry;
import org.miaixz.bus.cortex.release.ReleaseStore;
import org.miaixz.bus.cortex.setting.SettingProvider;
import org.miaixz.bus.cortex.setting.SettingService;
import org.miaixz.bus.cortex.setting.SettingStore;
import org.miaixz.bus.cortex.setting.approval.ApprovalService;
import org.miaixz.bus.cortex.setting.approval.ApprovalStore;
import org.miaixz.bus.cortex.setting.curator.ItemEditor;
import org.miaixz.bus.cortex.setting.curator.ItemService;
import org.miaixz.bus.cortex.setting.delivery.ItemExportService;
import org.miaixz.bus.cortex.setting.delivery.ItemQueryService;
import org.miaixz.bus.cortex.setting.item.ItemCodec;
import org.miaixz.bus.cortex.setting.item.ItemStore;
import org.miaixz.bus.cortex.setting.reference.ReferenceCodec;
import org.miaixz.bus.cortex.setting.reference.ReferenceService;
import org.miaixz.bus.cortex.setting.reference.ReferenceStore;
import org.miaixz.bus.cortex.setting.revision.RevisionStore;
import org.miaixz.bus.cortex.setting.rollout.GrayRuleMatcher;
import org.miaixz.bus.cortex.setting.rollout.RolloutService;
import org.miaixz.bus.cortex.setting.rollout.RolloutStore;
import org.miaixz.bus.cortex.setting.secret.NoOpSecretCodec;
import org.miaixz.bus.cortex.setting.secret.SecretCodec;
import org.miaixz.bus.cortex.setting.secret.SecretMasker;
import org.miaixz.bus.cortex.setting.watch.WatchStore;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableCortex;

/**
 * Configures Bus Cortex clients, registries, watches, and optional embedded server integration.
 * <p>
 * The starter exposes the shared bus-cortex components, then initializes the static {@link Cortex} facade so
 * application code and Spring-managed beans resolve the same handles. Starter defaults come from
 * {@link CortexProperties}, while cache backend settings may also be bound directly from the Spring
 * {@link Environment}.
 * </p>
 *
 * @author Kimi Liu
 */
@EnableConfigurationProperties(CortexProperties.class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.miaixz.bus.cortex.Cortex")
@ConditionalOnEnabled(annotation = EnableCortex.class, prefix = GeniusBuilder.CORTEX)
public class CortexConfiguration {

    /**
     * Bound cortex configuration properties.
     */
    private final CortexProperties properties;

    /**
     * Stores the Cortex client, server, cache, watch, guard, and audit policies used by the Bean factories.
     *
     * @param properties bound configuration properties
     */
    public CortexConfiguration(CortexProperties properties) {
        this.properties = properties;
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
     * Resolves host access policy only when guard checks are enabled. Disabling the switch also bypasses a
     * host-provided guard for Cortex services; persistence and workflow checks are unaffected.
     *
     * @param guards optional host guard provider
     * @return enabled guard, or null when checks are disabled or no guard exists
     */
    private CortexGuard guard(ObjectProvider<CortexGuard> guards) {
        return properties.getGuard().enabled() ? guards.getIfAvailable() : null;
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
     * Creates the default Cortex cache through the shared cache factory.
     * <p>
     * Cortex first resolves {@code bus.cortex.cache.*}. When no Cortex-specific backend settings are present, it falls
     * back to {@code bus.cache.*}. When neither prefix is present, Cortex falls back to its starter defaults.
     * </p>
     *
     * @param factoryProvider optional shared cache factory provider
     * @param environment     Spring environment used for property binding
     * @return default Cortex cache
     */
    @Bean("cortexCache")
    @ConditionalOnMissingBean(name = "cortexCache")
    public CacheX<String, Object> cortexCache(ObjectProvider<Factory> factoryProvider, Environment environment) {
        Factory factory = factoryProvider.getIfAvailable(Factory::new);
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
    @ConditionalOnMissingBean(WatchManager.class)
    public WatchManager watchManager(@Qualifier("cortexCache") CacheX cache) {
        return new WatchManager(cache(cache), properties.requireMaxWatchesPerTenant(),
                properties.requireWatchExpireMs());
    }

    /**
     * Exposes the default registry-side key strategy.
     *
     * @return registry key strategy
     */
    @Bean(name = "registryKeying")
    @ConditionalOnMissingBean(name = "registryKeying")
    public Keying<Keying.RegistrySpec> registryKeying() {
        return RegistryGenerator.INSTANCE;
    }

    /**
     * Exposes the default setting-side key strategy.
     *
     * @return setting key strategy
     */
    @Bean(name = "settingKeying")
    @ConditionalOnMissingBean(name = "settingKeying")
    public Keying<Keying.SettingSpec> settingKeying() {
        return SettingGenerator.INSTANCE;
    }

    /**
     * Creates the secret masker used by management-facing APIs.
     *
     * @return secret masker
     */
    @Bean
    @ConditionalOnMissingBean(SecretMasker.class)
    public SecretMasker secretMasker() {
        return new SecretMasker();
    }

    /**
     * Creates the default secret codec used by setting publishing and resolution.
     *
     * @return secret codec
     */
    @Bean
    @ConditionalOnMissingBean(SecretCodec.class)
    public SecretCodec secretCodec() {
        return new NoOpSecretCodec();
    }

    /**
     * Exposes default token guard configuration from starter properties.
     *
     * @return token guard config
     */
    @Bean
    @ConditionalOnMissingBean(TokenGuardConfig.class)
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
     * Creates the shared Cortex guard used by registry, setting, and version mutation paths.
     *
     * @param strategies guard strategies discovered from the application context
     * @return shared Cortex guard
     */
    @Bean
    @ConditionalOnMissingBean(CortexGuard.class)
    @ConditionalOnProperty(prefix = "bus.cortex.guard", name = "enabled", havingValue = "true", matchIfMissing = true)
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
    @ConditionalOnMissingBean(AuditLogger.class)
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
    @ConditionalOnMissingBean(LoggingWatchListener.class)
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
     * @param changeLogStore      optional host-owned registry outbox
     * @param keyingProvider      optional registry keying provider
     * @return API registry
     */
    @Bean
    @ConditionalOnMissingBean(ApiRegistry.class)
    public ApiRegistry apiRegistry(
            @Qualifier("cortexCache") CacheX cache,
            WatchManager watchManager,
            ObjectProvider<ApiRegistryStore> storeProvider,
            ObjectProvider<RegistryStore<Assets>> sharedStoreProvider,
            List<Listener<RegistryChange<ApiAssets>>> syncListeners,
            ObjectProvider<CortexChangeLogStore> changeLogStore,
            @Qualifier("registryKeying") ObjectProvider<Keying<Keying.RegistrySpec>> keyingProvider) {
        RegistryStore<ApiAssets> store = storeProvider.getIfAvailable();
        if (store == null) {
            store = typedStore(sharedStoreProvider.getIfAvailable(), ApiAssets.class);
        }
        return new ApiRegistry(cache(cache), watchManager, store, syncListeners, changeLogStore.getIfAvailable(),
                keyingProvider.getIfAvailable(() -> RegistryGenerator.INSTANCE));
    }

    /**
     * Creates the MCP registry backed by cache and, when available, the shared durable registry store.
     *
     * @param cache               shared cache abstraction
     * @param watchManager        watch manager
     * @param storeProvider       optional registry store provider
     * @param sharedStoreProvider optional shared registry store provider
     * @param keyingProvider      optional registry keying provider
     * @return MCP registry
     */
    @Bean
    @ConditionalOnMissingBean(McpRegistry.class)
    public McpRegistry mcpRegistry(
            @Qualifier("cortexCache") CacheX cache,
            WatchManager watchManager,
            ObjectProvider<McpRegistryStore> storeProvider,
            ObjectProvider<RegistryStore<Assets>> sharedStoreProvider,
            @Qualifier("registryKeying") ObjectProvider<Keying<Keying.RegistrySpec>> keyingProvider) {
        RegistryStore<McpAssets> store = storeProvider.getIfAvailable();
        if (store == null) {
            store = typedStore(sharedStoreProvider.getIfAvailable(), McpAssets.class);
        }
        return new McpRegistry(cache(cache), watchManager, store,
                keyingProvider.getIfAvailable(() -> RegistryGenerator.INSTANCE));
    }

    /**
     * Creates the prompt registry backed by cache and, when available, the shared durable registry store.
     *
     * @param cache               shared cache abstraction
     * @param watchManager        watch manager
     * @param storeProvider       optional registry store provider
     * @param sharedStoreProvider optional shared registry store provider
     * @param keyingProvider      optional registry keying provider
     * @return prompt registry
     */
    @Bean
    @ConditionalOnMissingBean(PromptRegistry.class)
    public PromptRegistry promptRegistry(
            @Qualifier("cortexCache") CacheX cache,
            WatchManager watchManager,
            ObjectProvider<PromptRegistryStore> storeProvider,
            ObjectProvider<RegistryStore<Assets>> sharedStoreProvider,
            @Qualifier("registryKeying") ObjectProvider<Keying<Keying.RegistrySpec>> keyingProvider) {
        RegistryStore<PromptAssets> store = storeProvider.getIfAvailable();
        if (store == null) {
            store = typedStore(sharedStoreProvider.getIfAvailable(), PromptAssets.class);
        }
        return new PromptRegistry(cache(cache), watchManager, store,
                keyingProvider.getIfAvailable(() -> RegistryGenerator.INSTANCE));
    }

    /**
     * Exposes read-only formal release records from the authoritative host store.
     *
     * @param releaseStore authoritative formal releases
     * @param watchManager notification transport
     * @return read-only release registry
     */
    @Bean("releaseRegistry")
    @ConditionalOnBean(ReleaseStore.class)
    @ConditionalOnMissingBean(ReleaseRegistry.class)
    @ConditionalOnProperty(prefix = "bus.cortex.release", name = "enabled", havingValue = "true")
    public ReleaseRegistry releaseRegistry(ReleaseStore releaseStore, WatchManager watchManager) {
        return new ReleaseRegistry(releaseStore, watchManager);
    }

    /**
     * Creates the registry refresh service for control-plane cache maintenance.
     *
     * @param apiRegistry    API registry
     * @param mcpRegistry    MCP registry
     * @param promptRegistry prompt registry
     * @return registry refresh service
     */
    @Bean
    @ConditionalOnMissingBean(RegistryRefreshService.class)
    public RegistryRefreshService registryRefreshService(
            ApiRegistry apiRegistry,
            McpRegistry mcpRegistry,
            PromptRegistry promptRegistry) {
        return new RegistryRefreshService(apiRegistry, mcpRegistry, promptRegistry);
    }

    /**
     * Creates the registry admin service for control-plane queries and updates.
     *
     * @param apiRegistry    API registry
     * @param mcpRegistry    MCP registry
     * @param promptRegistry prompt registry
     * @param cortexGuard    optional Cortex guard provider
     * @param keyingProvider registry keying provider
     * @param batchExecutors external batch executors
     * @param batchResolvers external batch resolvers
     * @return registry admin service
     */
    @Bean
    @ConditionalOnMissingBean(RegistryControlService.class)
    public RegistryControlService registryAdminService(
            ApiRegistry apiRegistry,
            McpRegistry mcpRegistry,
            PromptRegistry promptRegistry,
            ObjectProvider<CortexGuard> cortexGuard,
            @Qualifier("registryKeying") ObjectProvider<Keying<Keying.RegistrySpec>> keyingProvider,
            ObjectProvider<RegistryBatchExecutor> batchExecutors,
            ObjectProvider<RegistryBatchResolver> batchResolvers) {
        return new RegistryControlService(apiRegistry, mcpRegistry, promptRegistry, guard(cortexGuard),
                keyingProvider.getIfAvailable(() -> RegistryGenerator.INSTANCE),
                batchExecutors.orderedStream().toList(), batchResolvers.orderedStream().toList());
    }

    /**
     * Creates the remote Vortex bridge listener when push-and-pull mode is enabled.
     *
     * @param changeLogStore optional host-owned delivery outbox
     * @return bridge listener
     */
    @Bean(name = "vortexBridge", destroyMethod = "close")
    @ConditionalOnMissingBean(VortexBridge.class)
    @ConditionalOnProperty(prefix = "bus.cortex.bridge", name = "mode", havingValue = "push+pull")
    public VortexBridge vortexBridge(ObjectProvider<CortexChangeLogStore> changeLogStore) {
        CortexProperties.Bridge bridge = properties.getBridge();
        String source = bridge.resolveSource();
        return new VortexBridge(bridge.requireUrl(), bridge.requireMaxRetries(),
                StringKit.isEmpty(source) ? "cortex" : source, Builder.DEFAULT_HEALTH_TIMEOUT_MS,
                changeLogStore.getIfAvailable());
    }

    /**
     * Creates the shared bounded definition parser without owning configuration persistence.
     *
     * @return definition parser
     */
    @Bean
    @ConditionalOnMissingBean(ItemCodec.class)
    public ItemCodec itemCodec() {
        return new ItemCodec(64, 100000);
    }

    /**
     * Creates the bounded relationship-option parser.
     *
     * @return relationship parser
     */
    @Bean
    @ConditionalOnMissingBean(ReferenceCodec.class)
    public ReferenceCodec referenceCodec() {
        return new ReferenceCodec(64, 100000);
    }

    /**
     * Creates current-definition protection and safe-view preparation.
     *
     * @param codec   shared definition parser
     * @param secrets host-overridable storage protection
     * @param masker  safe value projector
     * @return definition editor
     */
    @Bean
    @ConditionalOnMissingBean(ItemEditor.class)
    public ItemEditor itemEditor(ItemCodec codec, SecretCodec secrets, SecretMasker masker) {
        return new ItemEditor(codec, secrets, masker);
    }

    /**
     * Assembles current definition management using host stores and optional access policy.
     *
     * @param items     current definitions
     * @param revisions exact published snapshots
     * @param editor    definition preparation
     * @param guards    optional host access policy provider
     * @return current definition service
     */
    @Bean
    @ConditionalOnBean({ ItemStore.class, RevisionStore.class })
    @ConditionalOnMissingBean(ItemService.class)
    public ItemService itemService(
            ItemStore items,
            RevisionStore revisions,
            ItemEditor editor,
            ObjectProvider<CortexGuard> guards) {
        return new ItemService(items, revisions, editor, guard(guards));
    }

    /**
     * Assembles explicit application relationship management without duplicate policy storage.
     *
     * @param references current relationships
     * @param revisions  exact published pins
     * @param codec      relationship parser
     * @param editor     sensitive overlay preparation
     * @param guards     optional host access policy provider
     * @return relationship service
     */
    @Bean
    @ConditionalOnBean({ ReferenceStore.class, RevisionStore.class })
    @ConditionalOnMissingBean(ReferenceService.class)
    public ReferenceService referenceService(
            ReferenceStore references,
            RevisionStore revisions,
            ReferenceCodec codec,
            ItemEditor editor,
            ObjectProvider<CortexGuard> guards) {
        return new ReferenceService(references, revisions, codec, editor, guard(guards));
    }

    /**
     * Assembles the joint current-definition and relationship transaction boundary.
     *
     * @param items      current definition preparation
     * @param references relationship preparation
     * @param store      host-owned atomic current-state transaction
     * @param guards     optional host access policy provider
     * @return current-state coordinator
     */
    @Bean
    @ConditionalOnBean({ ItemService.class, ReferenceService.class, SettingStore.class })
    @ConditionalOnMissingBean(SettingService.class)
    public SettingService settingService(
            ItemService items,
            ReferenceService references,
            SettingStore store,
            ObjectProvider<CortexGuard> guards) {
        return new SettingService(items, references, store, guard(guards));
    }

    /**
     * Assembles immutable approval evidence using the optional host access policy provider.
     *
     * @param store  approval evidence
     * @param guards optional host access policy provider
     * @return approval service
     */
    @Bean
    @ConditionalOnBean(ApprovalStore.class)
    @ConditionalOnMissingBean(ApprovalService.class)
    public ApprovalService approvalService(ApprovalStore store, ObjectProvider<CortexGuard> guards) {
        return new ApprovalService(store, guard(guards));
    }

    /**
     * Assembles governed rollout management from current definitions and published baselines.
     *
     * @param store      rollout workflow authority
     * @param settings   current definition and relationship graph
     * @param releases   formal release baselines
     * @param revisions  exact published snapshots
     * @param editor     definition verification
     * @param references relationship parser
     * @param approvals  approval policy registry
     * @param guards     optional host access policy provider
     * @return rollout service
     */
    @Bean
    @ConditionalOnBean({ RolloutStore.class, SettingStore.class, ReleaseStore.class, RevisionStore.class,
            ApprovalStore.class })
    @ConditionalOnMissingBean(RolloutService.class)
    public RolloutService rolloutService(
            RolloutStore store,
            SettingStore settings,
            ReleaseStore releases,
            RevisionStore revisions,
            ItemEditor editor,
            ReferenceCodec references,
            ApprovalStore approvals,
            ObjectProvider<CortexGuard> guards) {
        return new RolloutService(store, settings, releases, revisions, editor, references, approvals, guard(guards));
    }

    /**
     * Assembles approved publication through the existing atomic host boundary.
     *
     * @param publications atomic formal-publication authority
     * @param rollouts     approved rollout inputs
     * @param guards       optional host access policy provider
     * @param releases     formal releases
     * @param revisions    exact published snapshots
     * @return formal release publisher
     */
    @Bean
    @ConditionalOnBean({ PublicationStore.class, RolloutStore.class, ReleaseStore.class, RevisionStore.class })
    @ConditionalOnMissingBean(ReleasePublisher.class)
    public ReleasePublisher releasePublisher(
            PublicationStore publications,
            RolloutStore rollouts,
            ObjectProvider<CortexGuard> guards,
            ReleaseStore releases,
            RevisionStore revisions) {
        return new ReleasePublisher(publications, rollouts, guard(guards), releases, revisions);
    }

    /**
     * Creates the stateless selector for application processes.
     *
     * @return gray selector
     */
    @Bean
    @ConditionalOnMissingBean(GrayRuleMatcher.class)
    public GrayRuleMatcher grayRuleMatcher() {
        return new GrayRuleMatcher();
    }

    /**
     * Assembles published-only delivery, which directly implements the consumer-facing curator contract.
     *
     * @param releases  formal delivery authority
     * @param revisions immutable published snapshots
     * @param editor    protected content handling
     * @param codec     definition parser and renderer
     * @param matcher   current process selector
     * @param watches   host-owned subscription evidence
     * @param transport notification transport
     * @param provider  invocation and current process data
     * @param guards    optional host access policy provider
     * @return published-only curator service
     */
    @Bean
    @ConditionalOnBean({ ReleaseStore.class, RevisionStore.class, WatchStore.class, SettingProvider.class })
    @ConditionalOnMissingBean({ ItemQueryService.class, Curator.class })
    public ItemQueryService settingQueryService(
            ReleaseStore releases,
            RevisionStore revisions,
            ItemEditor editor,
            ItemCodec codec,
            GrayRuleMatcher matcher,
            WatchStore watches,
            WatchManager transport,
            SettingProvider provider,
            ObjectProvider<CortexGuard> guards) {
        return new ItemQueryService(releases, revisions, editor, codec, matcher, watches, transport, provider,
                guard(guards));
    }

    /**
     * Assembles separate current-view and published-content export boundaries.
     *
     * @param items     current definition views
     * @param published published-only query service
     * @param guards    optional host access policy provider
     * @return setting export service
     */
    @Bean
    @ConditionalOnBean({ ItemService.class, ItemQueryService.class })
    @ConditionalOnMissingBean(ItemExportService.class)
    public ItemExportService settingExportService(
            ItemService items,
            ItemQueryService published,
            ObjectProvider<CortexGuard> guards) {
        return new ItemExportService(items, published, guard(guards));
    }

    /**
     * Packages the assembled registries and curator into one immutable Cortex runtime bundle.
     *
     * @param apiRegistry     API registry
     * @param mcpRegistry     MCP registry
     * @param promptRegistry  prompt registry
     * @param releaseRegistry optional release registry
     * @param curator         optional curator provider
     * @return Cortex runtime bundle
     */
    @Bean
    @ConditionalOnMissingBean(Cortex.Runtime.class)
    public Cortex.Runtime cortexRuntime(
            ApiRegistry apiRegistry,
            McpRegistry mcpRegistry,
            PromptRegistry promptRegistry,
            ObjectProvider<ReleaseRegistry> releaseRegistry,
            ObjectProvider<Curator> curator) {
        Curator optionalCurator = curator.getIfAvailable();
        ReleaseRegistry optionalReleaseRegistry = releaseRegistry.getIfAvailable();
        if (optionalReleaseRegistry != null) {
            return Cortex.runtime(apiRegistry, mcpRegistry, promptRegistry, optionalReleaseRegistry, optionalCurator);
        }
        return Cortex.runtime(apiRegistry, mcpRegistry, promptRegistry, optionalCurator);
    }

    /**
     * Binds the assembled runtime through its canonical lifecycle handle.
     *
     * @param runtime exact Spring-managed runtime assembly
     * @return handle that releases this binding when the application context closes
     */
    @Bean(destroyMethod = "close")
    @ConditionalOnBean(Cortex.Runtime.class)
    @ConditionalOnMissingBean(Cortex.RuntimeHandle.class)
    public Cortex.RuntimeHandle cortexRuntimeHandle(Cortex.Runtime runtime) {
        return Cortex.bind(runtime);
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
            if (binder.bind(prefix + Symbol.DOT + key, Bindable.of(String.class)).isBound()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Type-safe view over the optional durable registry store provided by the host application.
     *
     * @param <T> target asset subtype
     * @author Kimi Liu
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
         * Preserves the host transaction boundary for cache and event effects.
         *
         * @param action effect to execute after a successful host commit
         */
        @Override
        public void afterCommit(Runnable action) {
            delegate.afterCommit(action);
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
         * Saves the entry using the current snapshot already resolved by the mutation coordinator.
         *
         * @param entry    replacement registry entry
         * @param existing existing snapshot, or null when absent
         */
        @Override
        public void save(T entry, T existing) {
            delegate.save(entry, existing);
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
         * {@inheritDoc}
         */
        @Override
        public Instance updateHealth(T entry, Instance expected, Status observation, long started, long finished) {
            return delegate.updateHealth(entry, expected, observation, started, finished);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void publish(T entry, Instance instance, Instance previous, Runnable action) {
            delegate.publish(entry, instance, previous, action);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<Instance> queryInstances(T entry) {
            return delegate.queryInstances(entry);
        }

        /**
         * Deletes one registry entry from the shared durable store.
         *
         * @param type     asset type
         * @param tenantId logical registry tenantId
         * @param id       durable entry identifier
         */
        @Override
        public void delete(Type type, String tenantId, String id) {
            delegate.delete(type, tenantId, id);
        }

        /**
         * Deletes the entry using the snapshot already resolved by the mutation coordinator.
         *
         * @param type     registry type
         * @param tenantId storage tenant for MCP and prompt entries
         * @param id       exact entry identity
         * @param existing current entry snapshot
         */
        @Override
        public void delete(Type type, String tenantId, String id, T existing) {
            delegate.delete(type, tenantId, id, existing);
        }

        /**
         * Deletes one service relationship without deleting its shared runtime process.
         *
         * @param type     asset type
         * @param tenantId logical registry tenantId
         * @param app_id   owning application identifier
         * @param method   method name
         * @param version  version identifier
         * @param checksum process checksum
         */
        @Override
        public void deleteInstance(
                Type type,
                String tenantId,
                String app_id,
                String method,
                String version,
                String checksum) {
            delegate.deleteInstance(type, tenantId, app_id, method, version, checksum);
        }

        /**
         * Finds one registry entry and converts it to the expected typed view.
         *
         * @param type     asset type
         * @param tenantId logical registry tenantId
         * @param id       durable entry identifier
         * @return typed asset, or {@code null} when absent or incompatible
         */
        @Override
        public T find(Type type, String tenantId, String id) {
            return typed(delegate.find(type, tenantId, id));
        }

        /**
         * Finds one registry entry by method and version and converts it to the expected typed view.
         *
         * @param type     asset type
         * @param tenantId logical registry tenantId
         * @param app_id   owning application identifier
         * @param method   method name
         * @param version  version identifier
         * @return typed asset, or {@code null} when absent or incompatible
         */
        @Override
        public T findByMethodVersion(Type type, String tenantId, String app_id, String method, String version) {
            return typed(delegate.findByMethodVersion(type, tenantId, app_id, method, version));
        }

        /**
         * Preserves the host batch route lookup and converts existing entries to the requested subtype.
         *
         * @param registryType registry type
         * @param routes       exact route identities
         * @return typed entries keyed by the original routes
         */
        @Override
        public Map<RegistryRouteKey, T> findByRoutes(Type registryType, Collection<RegistryRouteKey> routes) {
            Map<RegistryRouteKey, Assets> found = delegate.findByRoutes(registryType, routes);
            if (found == null || found.isEmpty()) {
                return Map.of();
            }
            Map<RegistryRouteKey, T> result = new LinkedHashMap<>();
            found.forEach((route, asset) -> result.put(route, typed(asset)));
            return result;
        }

        /**
         * Forwards the original public query without changing its asset constraints or pagination.
         *
         * @param registryType registry type
         * @param vector       complete public selector
         * @return typed matching assets
         */
        @Override
        public List<T> query(Type registryType, Vector vector) {
            return typed(delegate.query(registryType, vector));
        }

        /**
         * Preserves a host override of the typed query contract instead of falling back to a different overload.
         *
         * @param registryType registry type
         * @param query        complete registry selector
         * @return typed matching assets
         */
        @Override
        public List<T> query(Type registryType, RegistryQuery query) {
            return typed(delegate.query(registryType, query));
        }

        /**
         * Preserves the host route-query implementation.
         *
         * @param registryType registry type
         * @param tenantId     storage tenant for MCP and prompt entries
         * @param app_id       owning application identifier
         * @param method       service method
         * @param version      service version
         * @return typed matching assets
         */
        @Override
        public List<T> queryByRoute(Type registryType, String tenantId, String app_id, String method, String version) {
            return typed(delegate.queryByRoute(registryType, tenantId, app_id, method, version));
        }

        /**
         * Converts an existing host query result without additional reads.
         *
         * @param assets host query results, or null
         * @return converted entries in their existing order
         */
        private List<T> typed(List<Assets> assets) {
            if (assets == null || assets.isEmpty()) {
                return List.of();
            }
            List<T> result = new ArrayList<>(assets.size());
            for (Assets asset : assets) {
                T converted = typed(asset);
                if (converted != null) {
                    result.add(converted);
                }
            }
            return result;
        }

        /**
         * Queries runtime instance snapshots for the given service identity.
         *
         * @param type     asset type
         * @param tenantId logical registry tenantId
         * @param app_id   owning application identifier
         * @param method   method name
         * @param version  version identifier
         * @return matching runtime instances
         */
        @Override
        public List<Instance> queryInstances(Type type, String tenantId, String app_id, String method, String version) {
            return delegate.queryInstances(type, tenantId, app_id, method, version);
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
