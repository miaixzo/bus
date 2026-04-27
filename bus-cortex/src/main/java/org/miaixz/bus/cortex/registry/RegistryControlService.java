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
package org.miaixz.bus.cortex.registry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.builtin.batch.BatchOperation;
import org.miaixz.bus.cortex.builtin.batch.BatchResult;
import org.miaixz.bus.cortex.builtin.graph.DependencyGraph;
import org.miaixz.bus.cortex.builtin.graph.ImpactAnalysis;
import org.miaixz.bus.cortex.guard.CortexGuard;
import org.miaixz.bus.cortex.guard.GuardContext;
import org.miaixz.bus.cortex.registry.api.ApiAssets;
import org.miaixz.bus.cortex.registry.api.ApiRegistry;
import org.miaixz.bus.cortex.registry.mcp.McpAssets;
import org.miaixz.bus.cortex.registry.mcp.McpRegistry;
import org.miaixz.bus.cortex.registry.prompt.PromptAssets;
import org.miaixz.bus.cortex.registry.prompt.PromptRegistry;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Control-plane service for registry content management.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RegistryControlService {

    /**
     * API registry used for service-definition management operations.
     */
    private final ApiRegistry apiRegistry;
    /**
     * MCP registry used for tool-definition management operations.
     */
    private final McpRegistry mcpRegistry;
    /**
     * Prompt registry used for prompt-definition management operations.
     */
    private final PromptRegistry promptRegistry;
    /**
     * Optional shared guard applied to registry mutations.
     */
    private final CortexGuard cortexGuard;

    /**
     * Creates a RegistryControlService.
     *
     * @param apiRegistry    API registry
     * @param mcpRegistry    MCP registry
     * @param promptRegistry prompt registry
     */
    public RegistryControlService(ApiRegistry apiRegistry, McpRegistry mcpRegistry, PromptRegistry promptRegistry) {
        this(apiRegistry, mcpRegistry, promptRegistry, null);
    }

    /**
     * Creates a RegistryControlService with an optional shared guard.
     *
     * @param apiRegistry    API registry
     * @param mcpRegistry    MCP registry
     * @param promptRegistry prompt registry
     * @param cortexGuard    optional shared guard
     */
    public RegistryControlService(ApiRegistry apiRegistry, McpRegistry mcpRegistry, PromptRegistry promptRegistry,
            CortexGuard cortexGuard) {
        this.apiRegistry = apiRegistry;
        this.mcpRegistry = mcpRegistry;
        this.promptRegistry = promptRegistry;
        this.cortexGuard = cortexGuard;
    }

    /**
     * Lists registry entries matching the query.
     *
     * @param query admin query
     * @return matching entries
     */
    public List<Assets> list(Vector query) {
        Vector criteria = vector(query);
        if (criteria.getType() == null) {
            return snapshot(criteria);
        }
        return assets(registry(RegistryIdentity.type(criteria.getType())).refresh(criteria));
    }

    /**
     * Loads one registry entry by ID or route identity.
     *
     * @param query admin query
     * @return matching entry or {@code null}
     */
    public Assets get(Vector query) {
        Vector criteria = vector(query);
        if (criteria.getType() == null) {
            return firstAcrossTypes(criteria, this::getSingle);
        }
        return getSingle(criteria);
    }

    /**
     * Creates or updates a registry entry through its concrete registry.
     *
     * @param asset entry to upsert
     * @return stored entry snapshot
     */
    public Assets upsert(Assets asset) {
        if (asset == null) {
            return null;
        }
        Type type = RegistryIdentity.type(asset);
        Assets prepared = RegistryIdentity.normalize(RegistryAssets.normalize(asset), type);
        enforceRegistry("upsert", type, prepared);
        if (prepared.getId() == null && prepared.getMethod() != null && prepared.getVersion() != null) {
            Assets existing = getByRoute(
                    type,
                    prepared.getNamespace_id(),
                    prepared.getApp_id(),
                    prepared.getMethod(),
                    prepared.getVersion());
            if (existing != null) {
                prepared.setId(existing.getId());
            }
        }
        return switch (type) {
            case API -> upsertApi((ApiAssets) prepared);
            case MCP -> upsertMcp((McpAssets) prepared);
            case PROMPT -> upsertPrompt((PromptAssets) prepared);
            default -> throw new IllegalArgumentException("Unsupported admin registry type: " + type);
        };
    }

    /**
     * Deletes one registry entry by type and ID.
     *
     * @param type      target type
     * @param namespace target namespace
     * @param id        entry identifier
     */
    public void delete(Type type, String namespace, String id) {
        enforceRegistry("delete", type, namespace, id, null, null, null);
        registry(type).deregister(namespace, id);
    }

    /**
     * Applies one builtin batch operation to registry content.
     *
     * @param operation builtin batch operation
     * @return aggregate batch result
     */
    public BatchResult batch(BatchOperation operation) {
        long start = System.currentTimeMillis();
        BatchResult result = new BatchResult();
        if (operation == null || operation.getEntries() == null || operation.getEntries().isEmpty()) {
            return result;
        }
        for (Assets source : operation.getEntries()) {
            if (source == null) {
                result.recordFailure(
                        BatchResult.Failure
                                .of(null, operation.getNamespace_id(), null, null, null, "Null batch entry"));
                if (!operation.isContinueOnError()) {
                    break;
                }
                continue;
            }
            Assets entry = RegistryAssets.copy(source);
            if (operation.getNamespace_id() != null
                    && (entry.getNamespace_id() == null || entry.getNamespace_id().isBlank())) {
                entry.setNamespace_id(operation.getNamespace_id());
            }
            if (operation.getType() != null) {
                entry.setType(operation.getType().key());
            }
            try {
                enforceRegistry(
                        operation.getOperationType() == null ? "batch"
                                : operation.getOperationType().name().toLowerCase(),
                        RegistryIdentity.type(entry),
                        entry);
                applyBatchEntry(operation, entry, result);
            } catch (Exception e) {
                result.recordFailure(
                        BatchResult.Failure.of(
                                RegistryIdentity.type(entry),
                                entry.getNamespace_id(),
                                entry.getId(),
                                entry.getMethod(),
                                entry.getVersion(),
                                e.getMessage()));
                if (!operation.isContinueOnError()) {
                    break;
                }
            }
        }
        result.setElapsedMs(System.currentTimeMillis() - start);
        return result;
    }

    /**
     * Computes the downstream impact set for one registry entry.
     *
     * @param scope source asset scope
     * @return impacted assets in breadth-first traversal order
     */
    public List<Assets> impact(Vector scope) {
        Vector criteria = vector(scope);
        Assets source = resolveImpactSource(criteria);
        if (source == null) {
            return List.of();
        }
        List<Assets> snapshot = normalizedSnapshot(criteria);
        DependencyGraph graph = new DependencyGraph();
        Map<String, Assets> assetsByKey = new LinkedHashMap<>();
        Map<String, String> aliases = new LinkedHashMap<>();
        for (Assets asset : snapshot) {
            String canonical = canonicalDependencyKey(asset);
            if (canonical == null) {
                continue;
            }
            assetsByKey.putIfAbsent(canonical, asset);
            registerAlias(aliases, canonical, asset);
        }
        for (Assets asset : snapshot) {
            String from = canonicalDependencyKey(asset);
            if (from == null) {
                continue;
            }
            for (String dependency : dependenciesOf(asset)) {
                String reference = normalizeDependencyReference(asset, dependency);
                if (reference != null) {
                    graph.addEdge(from, aliases.getOrDefault(reference, reference));
                }
            }
        }
        String sourceKey = aliases.getOrDefault(scopedDependencyKey(source), canonicalDependencyKey(source));
        if (sourceKey == null) {
            return List.of();
        }
        List<String> impactedKeys = new ImpactAnalysis(graph).findImpacted(sourceKey);
        List<Assets> impacted = new ArrayList<>(impactedKeys.size());
        for (String key : impactedKeys) {
            Assets asset = assetsByKey.get(key);
            if (asset != null) {
                impacted.add(asset);
            }
        }
        return impacted;
    }

    /**
     * Resolves the source entry for impact analysis across all supported types when necessary.
     *
     * @param scope impact-analysis scope
     * @return source asset or {@code null}
     */
    private Assets resolveImpactSource(Vector scope) {
        if (scope == null) {
            return null;
        }
        if (scope.getType() != null) {
            return getSingle(scope);
        }
        return firstAcrossTypes(scope, this::getSingle);
    }

    /**
     * Loads an existing registry entry by route identity so admin upserts can preserve deterministic IDs.
     *
     * @param type      registry type
     * @param namespace namespace
     * @param app_id    application identifier
     * @param method    route method
     * @param version   route version
     * @return existing entry or {@code null}
     */
    private Assets getByRoute(Type type, String namespace, String app_id, String method, String version) {
        Vector query = new Vector();
        query.setType(type == null ? null : type.key());
        query.setNamespace_id(namespace);
        query.setApp_id(app_id);
        query.setMethod(method);
        query.setVersion(version);
        return getSingle(query);
    }

    /**
     * Applies registry guard policy to one asset mutation.
     *
     * @param action guarded action
     * @param type   registry type
     * @param asset  registry asset
     */
    private void enforceRegistry(String action, Type type, Assets asset) {
        enforceRegistry(
                action,
                type,
                asset == null ? null : asset.getNamespace_id(),
                asset == null ? null : asset.getId(),
                asset == null ? null : asset.getApp_id(),
                asset == null ? null : asset.getMethod(),
                asset == null ? null : asset.getVersion());
    }

    private void enforceRegistry(
            String action,
            Type type,
            String namespace,
            String id,
            String app_id,
            String method,
            String version) {
        if (cortexGuard == null) {
            return;
        }
        Type effective = Type.requireRegistry(type);
        GuardContext context = new GuardContext();
        context.setDomain("registry");
        context.setAction(action);
        context.setResourceType(effective.name());
        context.setResourceId(id);
        context.setAssetId(id);
        context.namespace_id(namespace);
        context.setApp_id(app_id);
        context.putAttribute("type", effective.name());
        context.putAttribute("typeKey", effective.key());
        context.putAttribute("method", method);
        context.putAttribute("version", version);
        cortexGuard.enforce(context);
    }

    /**
     * Persists an API definition through the API registry.
     *
     * @param asset API definition
     * @return stored API definition
     */
    private ApiAssets upsertApi(ApiAssets asset) {
        apiRegistry.register(asset);
        return apiRegistry.find(asset.getNamespace_id(), asset.getId());
    }

    /**
     * Persists an MCP definition through the MCP registry.
     *
     * @param asset MCP definition
     * @return stored MCP definition
     */
    private McpAssets upsertMcp(McpAssets asset) {
        mcpRegistry.register(asset);
        return mcpRegistry.find(asset.getNamespace_id(), asset.getId());
    }

    /**
     * Persists a prompt definition through the prompt registry.
     *
     * @param asset prompt definition
     * @return stored prompt definition
     */
    private PromptAssets upsertPrompt(PromptAssets asset) {
        promptRegistry.register(asset);
        return promptRegistry.find(asset.getNamespace_id(), asset.getId());
    }

    /**
     * Converts an admin query into the shared vector selector used by registry queries.
     *
     * @param query admin query
     * @return vector selector
     */
    private Vector vector(Vector query) {
        Vector vector = new Vector();
        if (query != null) {
            vector.setNamespace_id(query.getNamespace_id());
            vector.setApp_id(query.getApp_id());
            vector.setType(query.getType());
            vector.setId(query.getId());
            vector.setMethod(query.getMethod());
            vector.setVersion(query.getVersion());
            vector.setLabels(query.getLabels());
            vector.setSelectors(query.getSelectors());
            vector.setLimit(query.getLimit());
            vector.setOffset(query.getOffset());
        }
        return vector;
    }

    /**
     * Resolves one concrete registry entry using ID first, then route identity, then broad refresh semantics.
     *
     * @param criteria lookup criteria
     * @return matching registry entry or {@code null}
     */
    private Assets getSingle(Vector criteria) {
        StoreBackedRegistry<? extends Assets> registry = registry(RegistryIdentity.type(criteria.getType()));
        if (criteria.getId() != null) {
            return registry.refresh(criteria.getNamespace_id(), criteria.getId());
        }
        if (criteria.getMethod() != null && criteria.getVersion() != null) {
            return registry.refreshByMethodVersion(
                    criteria.getNamespace_id(),
                    criteria.getApp_id(),
                    criteria.getMethod(),
                    criteria.getVersion());
        }
        List<? extends Assets> entries = registry.refresh(criteria);
        return entries.isEmpty() ? null : entries.getFirst();
    }

    /**
     * Applies one batch entry.
     *
     * @param operation operation type
     * @param entry     batch entry
     * @param result    aggregate result
     */
    private void applyBatchEntry(BatchOperation operation, Assets entry, BatchResult result) {
        BatchOperation.OperationType operationType = operation.getOperationType();
        if (operationType == null) {
            throw new IllegalArgumentException("Batch operation type is required");
        }
        if (operation.isDryRun()) {
            result.recordSkip();
            result.getWarnings().add("Dry-run skipped mutation for " + entry.getId());
            return;
        }
        switch (operationType) {
            case DEREGISTER -> {
                Assets existing = resolveExisting(entry);
                if (existing == null) {
                    result.recordSkip();
                    return;
                }
                delete(RegistryIdentity.type(existing), existing.getNamespace_id(), existing.getId());
                result.recordDelete();
            }
            case REGISTER, UPDATE, UPSERT -> {
                Assets existing = resolveExisting(entry);
                if (existing != null && operation.getConflictPolicy() == BatchOperation.ConflictPolicy.FAIL_FAST) {
                    throw new IllegalStateException("Conflict detected for " + existing.getId());
                }
                if (existing != null && operation.getConflictPolicy() == BatchOperation.ConflictPolicy.SKIP) {
                    result.recordSkip();
                    result.getWarnings().add("Skipped existing entry " + existing.getId());
                    return;
                }
                upsert(entry);
                if (existing == null) {
                    result.recordInsert();
                } else {
                    result.recordUpdate();
                }
            }
        }
    }

    /**
     * Resolves an existing asset using ID first, then method/version route identity.
     *
     * @param entry candidate asset
     * @return existing asset or {@code null}
     */
    private Assets resolveExisting(Assets entry) {
        Type type = RegistryIdentity.type(entry);
        if (entry.getId() != null) {
            Assets byId = registry(type).refresh(entry.getNamespace_id(), entry.getId());
            if (byId != null) {
                return byId;
            }
        }
        if (entry.getMethod() != null && entry.getVersion() != null) {
            return getByRoute(type, entry.getNamespace_id(), entry.getApp_id(), entry.getMethod(), entry.getVersion());
        }
        return null;
    }

    /**
     * Builds a registry snapshot for one impact-analysis scope.
     *
     * @param scope impact-analysis scope
     * @return registry snapshot
     */
    private List<Assets> snapshot(Vector scope) {
        if (scope != null && scope.getType() != null) {
            Vector filtered = vector(scope);
            filtered.setLimit(Integer.MAX_VALUE);
            filtered.setOffset(0);
            return list(filtered);
        }
        List<Assets> snapshot = new ArrayList<>();
        for (Type type : supportedTypes()) {
            snapshot.addAll(list(typeScope(scope, type)));
        }
        return snapshot;
    }

    /**
     * Builds and normalizes one registry snapshot for impact analysis and administrative scans.
     *
     * @param scope snapshot scope
     * @return normalized snapshot
     */
    private List<Assets> normalizedSnapshot(Vector scope) {
        List<Assets> snapshot = snapshot(scope);
        if (snapshot.isEmpty()) {
            return snapshot;
        }
        List<Assets> normalized = new ArrayList<>(snapshot.size());
        for (Assets asset : snapshot) {
            normalized.add(normalize(asset));
        }
        return normalized;
    }

    /**
     * Creates one type-specific scope for broad registry scans.
     *
     * @param scope base scope
     * @param type  target type
     * @return type-specific scope
     */
    private Vector typeScope(Vector scope, Type type) {
        Vector filtered = vector(scope);
        filtered.setType(type.key());
        filtered.setLimit(Integer.MAX_VALUE);
        filtered.setOffset(0);
        filtered.setId(null);
        filtered.setMethod(null);
        filtered.setVersion(null);
        return filtered;
    }

    /**
     * Creates one type-specific scope used to resolve the source entry for impact analysis.
     *
     * @param scope base scope
     * @param type  target type
     * @return type-specific source scope
     */
    private Vector sourceScope(Vector scope, Type type) {
        Vector filtered = vector(scope);
        filtered.setType(type.key());
        return filtered;
    }

    /**
     * Returns all registry types currently available to this service.
     *
     * @return supported type list
     */
    private List<Type> supportedTypes() {
        return List.of(Type.API, Type.MCP, Type.PROMPT);
    }

    /**
     * Applies metadata decoding and identity normalization to one asset snapshot.
     *
     * @param asset asset snapshot
     * @return normalized asset snapshot
     */
    private Assets normalize(Assets asset) {
        return RegistryIdentity.normalize(RegistryAssets.normalize(asset), RegistryIdentity.type(asset));
    }

    /**
     * Copies one registry result list into a mutable list of base assets.
     *
     * @param entries typed registry entries
     * @return mutable asset list
     */
    private List<Assets> assets(List<? extends Assets> entries) {
        return new ArrayList<>(entries);
    }

    /**
     * Resolves the first non-null result produced while scanning all supported types.
     *
     * @param criteria base criteria
     * @param resolver type-specific resolver
     * @param <T>      resolved value type
     * @return first non-null result or {@code null}
     */
    private <T> T firstAcrossTypes(Vector criteria, Function<Vector, T> resolver) {
        for (Type type : supportedTypes()) {
            T resolved = resolver.apply(sourceScope(criteria, type));
            if (resolved != null) {
                return resolved;
            }
        }
        return null;
    }

    /**
     * Registers known dependency aliases for one asset.
     *
     * @param aliases   alias map
     * @param canonical canonical dependency key
     * @param asset     asset
     */
    private void registerAlias(Map<String, String> aliases, String canonical, Assets asset) {
        aliases.putIfAbsent(canonical, canonical);
        if (asset.getId() != null) {
            aliases.putIfAbsent(asset.getId(), canonical);
        }
        String route = routeDependencyKey(asset);
        if (route != null) {
            aliases.putIfAbsent(route, canonical);
        }
        String scoped = scopedDependencyKey(asset);
        if (scoped != null) {
            aliases.putIfAbsent(scoped, canonical);
        }
        if (StringKit.isEmpty(asset.getApp_id()) && asset.getMethod() != null && asset.getVersion() != null) {
            aliases.putIfAbsent(asset.getMethod() + ":" + asset.getVersion(), canonical);
        }
    }

    /**
     * Returns the canonical dependency key used in the impact graph.
     *
     * @param asset asset
     * @return canonical dependency key
     */
    private String canonicalDependencyKey(Assets asset) {
        String scoped = scopedDependencyKey(asset);
        if (scoped != null) {
            return scoped;
        }
        return asset == null ? null : asset.getId();
    }

    /**
     * Returns the type-qualified dependency key for one asset.
     *
     * @param asset asset
     * @return scoped dependency key
     */
    private String scopedDependencyKey(Assets asset) {
        if (asset == null || asset.getMethod() == null || asset.getVersion() == null) {
            return null;
        }
        return RegistryIdentity
                .scopedRouteKey(RegistryIdentity.type(asset), asset.getApp_id(), asset.getMethod(), asset.getVersion());
    }

    /**
     * Returns the application-scoped route alias for one asset.
     *
     * @param asset asset
     * @return application-scoped route alias
     */
    private String routeDependencyKey(Assets asset) {
        if (asset == null) {
            return null;
        }
        return RegistryIdentity.routeKey(asset.getApp_id(), asset.getMethod(), asset.getVersion());
    }

    /**
     * Normalizes one declared dependency reference into the canonical application-aware alias space.
     *
     * @param asset      dependent asset
     * @param dependency raw declared dependency
     * @return normalized dependency reference
     */
    private String normalizeDependencyReference(Assets asset, String dependency) {
        if (dependency == null) {
            return null;
        }
        String trimmed = dependency.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (asset == null || StringKit.isEmpty(asset.getApp_id())) {
            return trimmed;
        }
        String[] segments = trimmed.split(":", -1);
        if (segments.length == 2) {
            return RegistryIdentity.routeKey(asset.getApp_id(), segments[0], segments[1]);
        }
        if (segments.length == 3 && isTypeToken(segments[0])) {
            return RegistryIdentity.scopedRouteKey(
                    Type.valueOf(segments[0].toUpperCase()),
                    asset.getApp_id(),
                    segments[1],
                    segments[2]);
        }
        return trimmed;
    }

    /**
     * Returns whether the supplied token is a known type name.
     *
     * @param token raw token
     * @return {@code true} when the token names a supported type
     */
    private boolean isTypeToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        for (Type type : Type.registryTypes()) {
            if (type.name().equalsIgnoreCase(token)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts declared dependencies from the asset metadata payload.
     *
     * @param asset asset whose metadata is inspected
     * @return declared dependency keys
     */
    private List<String> dependenciesOf(Assets asset) {
        if (asset == null || asset.getMetadata() == null || asset.getMetadata().isBlank()) {
            return List.of();
        }
        Map<String, Object> root = JsonKit.toMap(asset.getMetadata());
        List<String> dependencies = strings(root.get("dependsOn"));
        if (!dependencies.isEmpty()) {
            return dependencies;
        }
        Map<String, Object> cortex = objectMap(root.get("_cortex"));
        if (cortex != null) {
            return strings(cortex.get("dependsOn"));
        }
        return List.of();
    }

    /**
     * Converts one metadata value into a list of dependency strings.
     *
     * @param value metadata value
     * @return dependency strings
     */
    private List<String> strings(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof Iterable<?> iterable) {
            List<String> result = new ArrayList<>();
            for (Object item : iterable) {
                String text = scalarString(item);
                if (text != null) {
                    result.add(text);
                }
            }
            return result;
        }
        try {
            List<Object> parsed = JsonKit.toList(JsonKit.toJsonString(value), Object.class);
            if (parsed == null) {
                return List.of();
            }
            List<String> result = new ArrayList<>(parsed.size());
            for (Object item : parsed) {
                String text = scalarString(item);
                if (text != null) {
                    result.add(text);
                }
            }
            return result;
        } catch (Exception ignore) {
            return List.of();
        }
    }

    /**
     * Converts one scalar dependency payload into a string.
     *
     * @param value dependency payload
     * @return string value or {@code null}
     */
    private String scalarString(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof CharSequence || value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        try {
            return JsonKit.toPojo(JsonKit.toJsonString(value), String.class);
        } catch (Exception ignore) {
            return value.toString();
        }
    }

    /**
     * Converts one metadata payload into a mutable string-keyed map.
     *
     * @param value metadata payload
     * @return mutable metadata map or {@code null}
     */
    private Map<String, Object> objectMap(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    result.put(entry.getKey().toString(), entry.getValue());
                }
            }
            return result;
        }
        try {
            Map<String, Object> result = JsonKit.toMap(JsonKit.toJsonString(value));
            return result == null ? null : new LinkedHashMap<>(result);
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * Resolves the concrete registry implementation for the requested type.
     *
     * @param type target registry type
     * @return matching concrete registry
     */
    private StoreBackedRegistry<? extends Assets> registry(Type type) {
        Type effective = type == null ? Type.API : type;
        return switch (effective) {
            case API -> apiRegistry;
            case MCP -> mcpRegistry;
            case PROMPT -> promptRegistry;
            default -> throw new IllegalArgumentException("Unsupported admin registry type: " + effective);
        };
    }

}
