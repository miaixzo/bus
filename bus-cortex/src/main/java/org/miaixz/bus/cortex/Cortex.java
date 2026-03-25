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
package org.miaixz.bus.cortex;

import java.util.List;
import java.util.Objects;

import org.miaixz.bus.cortex.registry.api.ApiDefinition;
import org.miaixz.bus.cortex.registry.mcp.McpAssets;
import org.miaixz.bus.cortex.registry.prompt.PromptAssets;

/**
 * Unified static façade exposing registry and configuration operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Cortex {

    /**
     * Static API registry used by the Cortex façade.
     */
    private static volatile Registry<ApiDefinition> apiRegistry;
    /**
     * Static MCP registry used by the Cortex façade.
     */
    private static volatile Registry<McpAssets> mcpRegistry;
    /**
     * Static prompt registry used by the Cortex façade.
     */
    private static volatile Registry<PromptAssets> promptRegistry;
    /**
     * Static configuration center used by the Cortex façade.
     */
    private static volatile Config config;

    private Cortex() {

    }

    /**
     * Initializes all static Cortex components.
     *
     * @param apiRegistry    API registry implementation
     * @param mcpRegistry    MCP registry implementation
     * @param promptRegistry prompt registry implementation
     * @param config         configuration center implementation
     */
    public static void init(
            Registry<ApiDefinition> apiRegistry,
            Registry<McpAssets> mcpRegistry,
            Registry<PromptAssets> promptRegistry,
            Config config) {
        Cortex.apiRegistry = apiRegistry;
        Cortex.mcpRegistry = mcpRegistry;
        Cortex.promptRegistry = promptRegistry;
        Cortex.config = config;
    }

    /**
     * Returns the initialized API registry.
     *
     * @return API registry component
     */
    public static Registry<ApiDefinition> apiRegistry() {
        return require(apiRegistry, "apiRegistry");
    }

    /**
     * Returns the initialized MCP registry.
     *
     * @return MCP registry component
     */
    public static Registry<McpAssets> mcpRegistry() {
        return require(mcpRegistry, "mcpRegistry");
    }

    /**
     * Returns the initialized prompt registry.
     *
     * @return prompt registry component
     */
    public static Registry<PromptAssets> promptRegistry() {
        return require(promptRegistry, "promptRegistry");
    }

    /**
     * Returns the registry matching the requested species-bearing asset class.
     *
     * @param type concrete entry class
     * @param <T>  entry generic type
     * @return registry responsible for the given asset class
     */
    public static <T extends Assets> Registry<T> registry(Class<T> type) {
        if (type == ApiDefinition.class) {
            return (Registry<T>) apiRegistry();
        }
        if (type == McpAssets.class) {
            return (Registry<T>) mcpRegistry();
        }
        if (type == PromptAssets.class) {
            return (Registry<T>) promptRegistry();
        }
        throw new IllegalArgumentException("Unsupported registry type: " + type);
    }

    /**
     * Returns the initialized configuration center component.
     *
     * @return configuration center component
     */
    public static Config config() {
        return require(config, "config");
    }

    /**
     * Registers a single assets through its matching registry.
     *
     * @param assets assets to persist
     */
    public static void register(Assets assets) {
        Registry<Assets> registry = (Registry<Assets>) registry((Class<? extends Assets>) assets.getClass());
        registry.register(assets);
    }

    /**
     * Deregisters the same entry identifier from all built-in registries.
     *
     * @param namespace namespace containing the entry
     * @param id        entry identifier
     */
    public static void deregister(String namespace, String id) {
        apiRegistry().deregister(namespace, id);
        mcpRegistry().deregister(namespace, id);
        promptRegistry().deregister(namespace, id);
    }

    /**
     * Queries entries from the registry matching the requested type.
     *
     * @param vector vector conditions
     * @param type   concrete entry class
     * @param <T>    entry generic type
     * @return matching entries
     */
    public static <T extends Assets> List<T> query(Vector vector, Class<T> type) {
        return registry(type).query(vector);
    }

    /**
     * Returns current config content for the given group and data ID.
     *
     * @param group  config group name
     * @param dataId config data identifier
     * @return published config content, or {@code null} if absent
     */
    public static String get(String group, String dataId) {
        return config().get(group, dataId);
    }

    /**
     * Publishes config content through the configured config center.
     *
     * @param group   config group name
     * @param dataId  config data identifier
     * @param content config content to publish
     */
    public static void publish(String group, String dataId, String content) {
        config().publish(group, dataId, content);
    }

    /**
     * Returns the given component or fails if Cortex has not been initialized.
     *
     * @param component component instance to validate
     * @param name      component logical name for error messages
     * @param <T>       component generic type
     * @return non-null component instance
     */
    private static <T> T require(T component, String name) {
        return Objects.requireNonNull(component, () -> "Cortex component is not initialized: " + name);
    }

}
