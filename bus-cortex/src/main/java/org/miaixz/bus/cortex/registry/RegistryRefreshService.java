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
import java.util.List;
import java.util.function.Function;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.Vector;
import org.miaixz.bus.cortex.registry.api.ApiRegistry;
import org.miaixz.bus.cortex.registry.mcp.McpRegistry;
import org.miaixz.bus.cortex.registry.prompt.PromptRegistry;

/**
 * Cache-maintenance service for registry projections.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RegistryRefreshService {

    /**
     * API registry whose cache projections may be refreshed.
     */
    private final ApiRegistry apiRegistry;
    /**
     * MCP registry whose cache projections may be refreshed.
     */
    private final McpRegistry mcpRegistry;
    /**
     * Prompt registry whose cache projections may be refreshed.
     */
    private final PromptRegistry promptRegistry;

    /**
     * Creates a RegistryRefreshService.
     *
     * @param apiRegistry    API registry
     * @param mcpRegistry    MCP registry
     * @param promptRegistry prompt registry
     */
    public RegistryRefreshService(ApiRegistry apiRegistry, McpRegistry mcpRegistry, PromptRegistry promptRegistry) {
        this.apiRegistry = apiRegistry;
        this.mcpRegistry = mcpRegistry;
        this.promptRegistry = promptRegistry;
    }

    /**
     * Refreshes one entry or route projection from durable state.
     *
     * @param scope refresh scope
     * @return refreshed entry or {@code null}
     */
    public Assets refresh(Vector scope) {
        Vector criteria = vector(scope);
        if (criteria.getType() == null) {
            return firstAcrossTypes(criteria, this::refreshSingle);
        }
        return refreshSingle(criteria);
    }

    /**
     * Rebuilds registry cache for the given scope.
     *
     * @param scope refresh scope
     * @return rebuilt entries
     */
    public List<Assets> rebuild(Vector scope) {
        if (scope == null || scope.getType() == null) {
            List<Assets> result = new ArrayList<>();
            for (Type type : supportedTypes()) {
                result.addAll(rebuildSingle(typeScope(scope, type)));
            }
            return result;
        }
        return rebuildSingle(vector(scope));
    }

    /**
     * Evicts one entry from cache without touching durable state.
     *
     * @param scope refresh scope
     */
    public void evict(Vector scope) {
        Vector criteria = vector(scope);
        if (StringKit.isEmpty(criteria.getId())) {
            return;
        }
        if (criteria.getType() == null) {
            for (Type type : supportedTypes()) {
                registry(type).evict(criteria.getNamespace_id(), criteria.getId());
            }
            return;
        }
        registry(RegistryIdentity.type(criteria.getType())).evict(criteria.getNamespace_id(), criteria.getId());
    }

    /**
     * Applies one type override to the supplied refresh scope.
     *
     * @param scope base refresh scope
     * @param type  target type
     * @return type-specific refresh scope
     */
    private Vector typeScope(Vector scope, Type type) {
        Vector criteria = vector(scope);
        criteria.setType(type.key());
        return criteria;
    }

    /**
     * Resolves one concrete refresh target using ID first, then route identity, then broad queries.
     *
     * @param criteria refresh criteria
     * @return refreshed asset or {@code null}
     */
    private Assets refreshSingle(Vector criteria) {
        StoreBackedRegistry<? extends Assets> registry = registry(RegistryIdentity.type(criteria.getType()));
        if (StringKit.isNotEmpty(criteria.getId())) {
            return registry.refresh(criteria.getNamespace_id(), criteria.getId());
        }
        if (StringKit.isNotEmpty(criteria.getMethod()) && StringKit.isNotEmpty(criteria.getVersion())) {
            return registry.refreshByMethodVersion(
                    criteria.getNamespace_id(),
                    criteria.getApp_id(),
                    criteria.getMethod(),
                    criteria.getVersion());
        }
        List<? extends Assets> result = registry.refresh(vector(criteria));
        return result.isEmpty() ? null : result.getFirst();
    }

    /**
     * Rebuilds one type-specific registry snapshot and converts it to a mutable list.
     *
     * @param criteria rebuild criteria
     * @return rebuilt registry snapshot
     */
    private List<Assets> rebuildSingle(Vector criteria) {
        List<? extends Assets> entries = registry(RegistryIdentity.type(criteria.getType())).rebuild(criteria);
        return new ArrayList<>(entries);
    }

    /**
     * Converts a refresh scope into the shared vector selector used by registries.
     *
     * @param scope refresh scope
     * @return vector selector
     */
    private Vector vector(Vector scope) {
        Vector vector = new Vector();
        if (scope != null) {
            vector.setNamespace_id(scope.getNamespace_id());
            vector.setApp_id(scope.getApp_id());
            vector.setType(scope.getType());
            vector.setId(scope.getId());
            vector.setMethod(scope.getMethod());
            vector.setVersion(scope.getVersion());
            vector.setLabels(scope.getLabels());
            vector.setSelectors(scope.getSelectors());
            vector.setLimit(scope.getLimit());
            vector.setOffset(scope.getOffset());
        }
        return vector;
    }

    /**
     * Returns all registry types currently supported by this service.
     *
     * @return supported type list
     */
    private List<Type> supportedTypes() {
        return List.of(Type.API, Type.MCP, Type.PROMPT);
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
            T resolved = resolver.apply(typeScope(criteria, type));
            if (resolved != null) {
                return resolved;
            }
        }
        return null;
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
            default -> throw new IllegalArgumentException("Unsupported registry refresh type: " + effective);
        };
    }

}
