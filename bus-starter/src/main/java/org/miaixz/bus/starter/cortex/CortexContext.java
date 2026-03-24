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

import lombok.Setter;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cortex.Config;
import org.miaixz.bus.cortex.registry.api.ApiRegistry;
import org.miaixz.bus.cortex.registry.mcp.McpRegistry;
import org.miaixz.bus.cortex.registry.prompt.PromptRegistry;

import lombok.Getter;

/**
 * Starter-side aggregate holding initialized Cortex components.
 * <p>
 * The starter uses this context as the single source of truth for resolved properties, local cache-backed registry
 * handles and the config center exposed to Spring beans.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
public class CortexContext {

    /**
     * Bound starter properties.
     */
    private final CortexProperties properties;

    /**
     * Resolved server address.
     */
    private final String serverAddr;

    /**
     * Resolved namespace.
     */
    private final String namespace;

    /**
     * Shared local cache used by starter-backed Cortex services.
     */
    private final CacheX<String, Object> cacheX;

    /**
     * API registry handle.
     */
    private final ApiRegistry apiRegistry;

    /**
     * MCP registry handle.
     */
    private final McpRegistry mcpRegistry;

    /**
     * Prompt registry handle.
     */
    private final PromptRegistry promptRegistry;

    /**
     * Config center handle.
     */
    private final Config config;

    /**
     * Creates a starter context with initialized Cortex handles.
     *
     * @param properties     bound starter properties
     * @param serverAddr     resolved server address
     * @param namespace      resolved namespace
     * @param cacheX         shared starter cache
     * @param apiRegistry    API registry handle
     * @param mcpRegistry    MCP registry handle
     * @param promptRegistry prompt registry handle
     * @param config         config center handle
     */
    public CortexContext(CortexProperties properties, String serverAddr, String namespace,
            CacheX<String, Object> cacheX, ApiRegistry apiRegistry, McpRegistry mcpRegistry,
            PromptRegistry promptRegistry, Config config) {
        this.properties = properties;
        this.serverAddr = serverAddr;
        this.namespace = namespace;
        this.cacheX = cacheX;
        this.apiRegistry = apiRegistry;
        this.mcpRegistry = mcpRegistry;
        this.promptRegistry = promptRegistry;
        this.config = config;
    }

}
