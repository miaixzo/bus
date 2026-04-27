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

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.cortex.registry.api.ApiAssets;
import org.miaixz.bus.cortex.registry.mcp.McpAssets;
import org.miaixz.bus.cortex.registry.prompt.PromptAssets;
import org.miaixz.bus.cortex.magic.runtime.CortexDiagnostics;
import org.miaixz.bus.cortex.magic.runtime.CortexLifecycle;
import org.miaixz.bus.cortex.magic.runtime.DiagnosticsSnapshot;
import org.miaixz.bus.cortex.version.VersionAssets;

/**
 * Unified static facade exposing registry and setting operations.
 *
 * <p>
 * Cortex is a convenience entrypoint, not the primary runtime container. Framework integration code is expected to
 * assemble the concrete registries and curator implementation first, then bind them into this facade for simplified
 * access.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Cortex {

    /**
     * Guard object protecting one-time facade binding.
     */
    private static final Object MONITOR = new Object();

    /**
     * Bound components currently exposed by the facade.
     */
    private static volatile Runtime current;

    /**
     * Creates the static facade holder.
     */
    private Cortex() {

    }

    /**
     * Creates one runtime bundle containing the supplied registries and curator.
     *
     * @param api     API registry implementation
     * @param mcp     MCP registry implementation
     * @param prompt  prompt registry implementation
     * @param version version registry implementation
     * @param curator curator implementation
     * @return immutable runtime bundle
     */
    public static Runtime runtime(
            Registry<ApiAssets> api,
            Registry<McpAssets> mcp,
            Registry<PromptAssets> prompt,
            Registry<VersionAssets> version,
            Curator curator) {
        return new Runtime(Objects.requireNonNull(api, "api"), Objects.requireNonNull(mcp, "mcp"),
                Objects.requireNonNull(prompt, "prompt"), Objects.requireNonNull(version, "version"),
                Objects.requireNonNull(curator, "curator"), List.of());
    }

    /**
     * Creates one runtime bundle containing registries, curator and additional managed runtime components.
     *
     * @param api        API registry implementation
     * @param mcp        MCP registry implementation
     * @param prompt     prompt registry implementation
     * @param version    version registry implementation
     * @param curator    curator implementation
     * @param components extra lifecycle or diagnostics components bound to the runtime
     * @return immutable runtime bundle
     */
    public static Runtime runtime(
            Registry<ApiAssets> api,
            Registry<McpAssets> mcp,
            Registry<PromptAssets> prompt,
            Registry<VersionAssets> version,
            Curator curator,
            Object... components) {
        return new Runtime(Objects.requireNonNull(api, "api"), Objects.requireNonNull(mcp, "mcp"),
                Objects.requireNonNull(prompt, "prompt"), Objects.requireNonNull(version, "version"),
                Objects.requireNonNull(curator, "curator"), components(components));
    }

    /**
     * Creates one runtime bundle without the optional version registry.
     *
     * @param api     API registry implementation
     * @param mcp     MCP registry implementation
     * @param prompt  prompt registry implementation
     * @param curator curator implementation
     * @return immutable runtime bundle
     */
    public static Runtime runtime(
            Registry<ApiAssets> api,
            Registry<McpAssets> mcp,
            Registry<PromptAssets> prompt,
            Curator curator) {
        return new Runtime(Objects.requireNonNull(api, "api"), Objects.requireNonNull(mcp, "mcp"),
                Objects.requireNonNull(prompt, "prompt"), null, Objects.requireNonNull(curator, "curator"), List.of());
    }

    /**
     * Creates one runtime bundle without version registry and with additional managed components.
     *
     * @param api        API registry implementation
     * @param mcp        MCP registry implementation
     * @param prompt     prompt registry implementation
     * @param curator    curator implementation
     * @param components extra lifecycle or diagnostics components bound to the runtime
     * @return immutable runtime bundle
     */
    public static Runtime runtime(
            Registry<ApiAssets> api,
            Registry<McpAssets> mcp,
            Registry<PromptAssets> prompt,
            Curator curator,
            Object... components) {
        return new Runtime(Objects.requireNonNull(api, "api"), Objects.requireNonNull(mcp, "mcp"),
                Objects.requireNonNull(prompt, "prompt"), null, Objects.requireNonNull(curator, "curator"),
                components(components));
    }

    /**
     * Initializes the facade with the given runtime bundle.
     *
     * <p>
     * Repeated initialization is only allowed when the exact same component instances are provided. Supplying different
     * instances after initialization fails fast.
     *
     * @param runtime runtime bundle to expose through {@code Cortex}
     */
    public static void init(Runtime runtime) {
        install(Objects.requireNonNull(runtime, "runtime"));
    }

    /**
     * Initializes the facade with the given components.
     *
     * @param api     API registry implementation
     * @param mcp     MCP registry implementation
     * @param prompt  prompt registry implementation
     * @param version version registry implementation
     * @param curator curator implementation
     */
    public static void init(
            Registry<ApiAssets> api,
            Registry<McpAssets> mcp,
            Registry<PromptAssets> prompt,
            Registry<VersionAssets> version,
            Curator curator) {
        init(runtime(api, mcp, prompt, version, curator));
    }

    /**
     * Initializes the facade without binding the optional version registry.
     *
     * @param api     API registry implementation
     * @param mcp     MCP registry implementation
     * @param prompt  prompt registry implementation
     * @param curator curator implementation
     */
    public static void init(
            Registry<ApiAssets> api,
            Registry<McpAssets> mcp,
            Registry<PromptAssets> prompt,
            Curator curator) {
        init(runtime(api, mcp, prompt, curator));
    }

    /**
     * Binds the given runtime bundle into the facade and returns a lifecycle handle that can later release the runtime.
     *
     * @param runtime runtime bundle to expose through {@code Cortex}
     * @return lifecycle handle for the installed runtime
     */
    public static RuntimeHandle bind(Runtime runtime) {
        Runtime next = Objects.requireNonNull(runtime, "runtime");
        install(next);
        return new RuntimeHandle(next);
    }

    /**
     * Binds the given components into the facade and returns a lifecycle handle that can later release the binding.
     *
     * @param api     API registry implementation
     * @param mcp     MCP registry implementation
     * @param prompt  prompt registry implementation
     * @param version version registry implementation
     * @param curator curator implementation
     * @return lifecycle handle for the installed runtime
     */
    public static RuntimeHandle bind(
            Registry<ApiAssets> api,
            Registry<McpAssets> mcp,
            Registry<PromptAssets> prompt,
            Registry<VersionAssets> version,
            Curator curator) {
        return bind(runtime(api, mcp, prompt, version, curator));
    }

    /**
     * Binds the facade without the optional version registry.
     *
     * @param api     API registry implementation
     * @param mcp     MCP registry implementation
     * @param prompt  prompt registry implementation
     * @param curator curator implementation
     * @return lifecycle handle for the installed runtime
     */
    public static RuntimeHandle bind(
            Registry<ApiAssets> api,
            Registry<McpAssets> mcp,
            Registry<PromptAssets> prompt,
            Curator curator) {
        return bind(runtime(api, mcp, prompt, curator));
    }

    /**
     * Returns whether the facade has already been initialized.
     *
     * @return {@code true} when all required components are bound
     */
    public static boolean isInitialized() {
        return current != null;
    }

    /**
     * Clears the current facade binding and releases all runtime-managed resources.
     */
    public static void reset() {
        Runtime previous;
        synchronized (MONITOR) {
            previous = current;
            current = null;
        }
        if (previous != null) {
            previous.stopComponents();
            return;
        }
        Callout.shutdown();
    }

    /**
     * Returns diagnostics snapshots for the currently bound runtime and shared callout pool.
     *
     * @return immutable diagnostics snapshots
     */
    public static List<DiagnosticsSnapshot> diagnostics() {
        List<DiagnosticsSnapshot> snapshots = new ArrayList<>();
        Runtime runtime = current;
        if (runtime != null) {
            runtime.collectDiagnostics(snapshots);
        }
        snapshots.add(Callout.diagnostics());
        return List.copyOf(snapshots);
    }

    /**
     * Returns the initialized API registry.
     *
     * @return API registry component
     */
    public static Registry<ApiAssets> api() {
        return requireRuntime().api();
    }

    /**
     * Returns the initialized MCP registry.
     *
     * @return MCP registry component
     */
    public static Registry<McpAssets> mcp() {
        return requireRuntime().mcp();
    }

    /**
     * Returns the initialized prompt registry.
     *
     * @return prompt registry component
     */
    public static Registry<PromptAssets> prompt() {
        return requireRuntime().prompt();
    }

    /**
     * Returns the initialized version registry.
     *
     * @return version registry component
     */
    public static Registry<VersionAssets> version() {
        return Objects.requireNonNull(requireRuntime().version(), "Version registry is not initialized");
    }

    /**
     * Returns the registry matching the requested typed asset class.
     *
     * @param type concrete entry class
     * @param <T>  entry generic type
     * @return registry responsible for the given asset class
     */
    public static <T extends Assets> Registry<T> registry(Class<T> type) {
        if (ApiAssets.class.isAssignableFrom(type)) {
            return (Registry<T>) api();
        }
        if (McpAssets.class.isAssignableFrom(type)) {
            return (Registry<T>) mcp();
        }
        if (PromptAssets.class.isAssignableFrom(type)) {
            return (Registry<T>) prompt();
        }
        throw new IllegalArgumentException("Unsupported registry type: " + type);
    }

    /**
     * Returns the registry matching the requested asset type.
     *
     * @param type registry type
     * @return matching registry
     */
    public static Registry<? extends Assets> registry(Type type) {
        Type effective = type == null ? Type.API : type;
        return switch (effective) {
            case API -> api();
            case MCP -> mcp();
            case PROMPT -> prompt();
            default -> throw new IllegalArgumentException("Unsupported registry type: " + effective);
        };
    }

    /**
     * Returns the registry matching the requested asset type without applying compatibility defaults.
     *
     * @param type registry type, must be one of API/MCP/PROMPT
     * @return matching registry
     */
    public static Registry<? extends Assets> registryOrThrow(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("Registry type must not be null");
        }
        Type registryType = Type.requireRegistry(type);
        return switch (registryType) {
            case API -> api();
            case MCP -> mcp();
            case PROMPT -> prompt();
            default -> throw new IllegalArgumentException("Unsupported registry type: " + registryType);
        };
    }

    /**
     * Returns the initialized curator component.
     *
     * @return curator component
     */
    public static Curator curator() {
        return requireRuntime().curator();
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
        api().deregister(namespace, id);
        mcp().deregister(namespace, id);
        prompt().deregister(namespace, id);
    }

    /**
     * Deregisters one entry from the registry matching the requested asset type.
     *
     * @param type      registry type
     * @param namespace namespace containing the entry
     * @param id        entry identifier
     */
    public static void deregister(Type type, String namespace, String id) {
        registry(type).deregister(namespace, id);
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
     * Returns current setting content for the given group and data ID.
     *
     * @param group   setting group name
     * @param data_id setting data identifier
     * @return published setting content, or {@code null} if absent
     */
    public static String get(String group, String data_id) {
        return curator().get(group, data_id);
    }

    /**
     * Publishes setting content through the configured curator.
     *
     * @param group   setting group name
     * @param data_id setting data identifier
     * @param content setting content to publish
     */
    public static void publish(String group, String data_id, String content) {
        curator().publish(group, data_id, content);
    }

    /**
     * Installs the given runtime snapshot into the static facade.
     *
     * @param next runtime bundle to expose through {@code Cortex}
     */
    private static void install(Runtime next) {
        synchronized (MONITOR) {
            if (current == null) {
                current = next;
                return;
            }
            if (!current.sameAs(next)) {
                throw new IllegalStateException("Cortex is already initialized with different components");
            }
        }
    }

    /**
     * Returns the currently installed runtime snapshot or fails when the facade has not been bound yet.
     *
     * @return current runtime snapshot
     */
    private static Runtime requireRuntime() {
        return Objects.requireNonNull(current, "Cortex is not initialized");
    }

    /**
     * Normalizes optional runtime components by dropping null placeholders.
     *
     * @param components supplied runtime components
     * @return immutable component list
     */
    private static List<Object> components(Object... components) {
        if (components == null || components.length == 0) {
            return List.of();
        }
        List<Object> result = new ArrayList<>(components.length);
        for (Object component : components) {
            if (component != null) {
                result.add(component);
            }
        }
        return List.copyOf(result);
    }

    /**
     * Lifecycle handle for a specific facade runtime.
     */
    public static class RuntimeHandle implements AutoCloseable {

        /**
         * Runtime snapshot owned by this lifecycle handle.
         */
        private final Runtime runtime;

        /**
         * Whether this lifecycle handle has already been closed.
         */
        private boolean closed;

        /**
         * Creates a lifecycle handle for the given runtime snapshot.
         *
         * @param runtime runtime bundle installed into the facade
         */
        protected RuntimeHandle(Runtime runtime) {
            this.runtime = runtime;
        }

        /**
         * Releases this runtime handle and clears the static facade when it still owns the active runtime snapshot.
         */
        @Override
        public void close() {
            Runtime runtimeToStop;
            synchronized (MONITOR) {
                if (closed) {
                    return;
                }
                if (Cortex.current != null && Cortex.current.sameAs(runtime)) {
                    Cortex.current = null;
                }
                closed = true;
                runtimeToStop = runtime;
            }
            runtimeToStop.stopComponents();
        }

    }

    /**
     * Immutable runtime bundle exposed by the static facade.
     */
    public static final class Runtime {

        /**
         * API registry bound into the facade.
         */
        private final Registry<ApiAssets> api;

        /**
         * MCP registry bound into the facade.
         */
        private final Registry<McpAssets> mcp;

        /**
         * Prompt registry bound into the facade.
         */
        private final Registry<PromptAssets> prompt;
        /**
         * Optional version registry bound into the facade.
         */
        private final Registry<VersionAssets> version;

        /**
         * Curator facade bound into the facade.
         */
        private final Curator curator;

        /**
         * Extra runtime-managed components such as watch, health or bridge services.
         */
        private final List<Object> components;

        /**
         * Whether lifecycle resources have already been released.
         */
        private final AtomicBoolean stopped = new AtomicBoolean();

        /**
         * Creates one immutable runtime snapshot for static facade binding.
         *
         * @param api     API registry
         * @param mcp     MCP registry
         * @param prompt  prompt registry
         * @param version version registry
         * @param curator curator facade
         */
        private Runtime(Registry<ApiAssets> api, Registry<McpAssets> mcp, Registry<PromptAssets> prompt,
                Registry<VersionAssets> version, Curator curator, List<Object> components) {
            this.api = api;
            this.mcp = mcp;
            this.prompt = prompt;
            this.version = version;
            this.curator = curator;
            this.components = components == null ? List.of() : List.copyOf(components);
        }

        /**
         * Returns the API registry from this runtime snapshot.
         *
         * @return API registry
         */
        public Registry<ApiAssets> api() {
            return api;
        }

        /**
         * Returns the MCP registry from this runtime snapshot.
         *
         * @return MCP registry
         */
        public Registry<McpAssets> mcp() {
            return mcp;
        }

        /**
         * Returns the prompt registry from this runtime snapshot.
         *
         * @return prompt registry
         */
        public Registry<PromptAssets> prompt() {
            return prompt;
        }

        /**
         * Returns whether a version registry was bound into this runtime snapshot.
         *
         * @return {@code true} when the version registry is available
         */
        public boolean hasVersion() {
            return version != null;
        }

        /**
         * Returns the version registry from this runtime snapshot.
         *
         * @return version registry
         */
        public Registry<VersionAssets> version() {
            return version;
        }

        /**
         * Returns the curator facade from this runtime snapshot.
         *
         * @return curator facade
         */
        public Curator curator() {
            return curator;
        }

        /**
         * Returns additional runtime-managed components.
         *
         * @return immutable component list
         */
        public List<Object> components() {
            return components;
        }

        /**
         * Returns whether this snapshot and the given snapshot refer to the exact same runtime assembly.
         *
         * @param other snapshot to compare
         * @return {@code true} when every component instance is identical
         */
        private boolean sameAs(Runtime other) {
            return api == other.api && mcp == other.mcp && prompt == other.prompt && version == other.version
                    && curator == other.curator && sameComponents(other);
        }

        /**
         * Collects diagnostics snapshots from every runtime component implementing the diagnostics contract.
         *
         * @param snapshots mutable destination list
         */
        private void collectDiagnostics(List<DiagnosticsSnapshot> snapshots) {
            for (Object component : uniqueComponents()) {
                if (component instanceof CortexDiagnostics diagnostics) {
                    snapshots.add(diagnostics.diagnostics());
                }
            }
        }

        /**
         * Stops every runtime component once, then clears the static callout pool.
         */
        private void stopComponents() {
            if (!stopped.compareAndSet(false, true)) {
                return;
            }
            for (Object component : uniqueComponents()) {
                stopQuietly(component);
            }
            Callout.shutdown();
        }

        /**
         * Returns runtime components once, preserving the configured component order.
         *
         * @return unique runtime components
         */
        private List<Object> uniqueComponents() {
            List<Object> candidates = new ArrayList<>(5 + components.size());
            candidates.add(api);
            candidates.add(mcp);
            candidates.add(prompt);
            candidates.add(version);
            candidates.add(curator);
            candidates.addAll(components);
            IdentityHashMap<Object, Boolean> seen = new IdentityHashMap<>();
            List<Object> unique = new ArrayList<>(candidates.size());
            for (Object candidate : candidates) {
                if (candidate == null || seen.containsKey(candidate)) {
                    continue;
                }
                seen.put(candidate, Boolean.TRUE);
                unique.add(candidate);
            }
            return unique;
        }

        /**
         * Returns whether the additional component list is identical by reference.
         *
         * @param other runtime snapshot to compare
         * @return {@code true} when every additional component instance is identical
         */
        private boolean sameComponents(Runtime other) {
            if (components.size() != other.components.size()) {
                return false;
            }
            for (int index = 0; index < components.size(); index++) {
                if (components.get(index) != other.components.get(index)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Stops one runtime component without allowing shutdown failures to escape.
         *
         * @param component component to stop
         */
        private void stopQuietly(Object component) {
            try {
                if (component instanceof CortexLifecycle lifecycle) {
                    lifecycle.stop();
                    return;
                }
                if (component instanceof AutoCloseable closeable) {
                    closeable.close();
                }
            } catch (Exception ignored) {
            }
        }

    }

}
