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
package org.miaixz.bus.extra.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.core.lang.loader.spi.ServiceLoader;

/**
 * Factory for creating JSON provider instances. Explicit selection is deterministic; automatic selection is accepted
 * only when exactly one provider can be instantiated from the current classpath.
 *
 * @author Kimi Liu
 */
public class JsonFactory {

    /**
     * Stores the application-wide JSON provider state after its first successful resolution or explicit installation.
     */
    private static final AtomicReference<ProviderState> DEFAULT_PROVIDER = new AtomicReference<>();

    /**
     * JVM system property used when resolving the provider outside a dependency-injection container.
     */
    private static final String PROVIDER_PROPERTY = "bus.json.provider";

    /**
     * Constructs a new JsonFactory instance.
     */
    public JsonFactory() {
        // No initialization required.
    }

    /**
     * Retrieves the application-wide singleton {@link JsonProvider}.
     *
     * @return The singleton {@link JsonProvider} instance.
     */
    public static JsonProvider get() {
        while (true) {
            ProviderState state = DEFAULT_PROVIDER.get();
            if (state != null) {
                return state.provider();
            }
            JsonProvider resolved = create(System.getProperty(PROVIDER_PROPERTY, "auto"));
            if (DEFAULT_PROVIDER.compareAndSet(null, new ProviderState(resolved, false))) {
                return resolved;
            }
        }
    }

    /**
     * Creates a new instance of {@link JsonProvider} based on the JSON engine JARs available on the classpath. It is
     * recommended to use the singleton instance provided by {@link #get()} for better performance, as this method
     * creates a new engine instance on each call.
     *
     * @return A new {@link JsonProvider} instance.
     * @throws InternalException if no JSON library (e.g., Jackson, Gson, Fastjson) is found on the classpath.
     */
    public static JsonProvider of() {
        return of("auto");
    }

    /**
     * Creates a provider with the requested name. Automatic selection is allowed only when exactly one provider can be
     * instantiated from the current classpath.
     *
     * @param requestedProvider provider name, or {@code auto}
     * @return selected provider
     */
    public static JsonProvider of(String requestedProvider) {
        return create(requestedProvider);
    }

    /**
     * Installs the application-wide provider used by {@link #get()} and {@link JsonKit}.
     *
     * @param provider selected provider
     */
    public static void install(JsonProvider provider) {
        Objects.requireNonNull(provider, "provider");
        while (true) {
            ProviderState state = DEFAULT_PROVIDER.get();
            if (state == null) {
                if (DEFAULT_PROVIDER.compareAndSet(null, new ProviderState(provider, true))) {
                    return;
                }
                continue;
            }

            JsonProvider current = state.provider();
            if (current == provider) {
                if (state.bound()) {
                    return;
                }
                if (DEFAULT_PROVIDER.compareAndSet(state, new ProviderState(provider, true))) {
                    return;
                }
                continue;
            }

            String currentName = normalize(current.name());
            String providerName = normalize(provider.name());
            if (!currentName.equals(providerName)) {
                throw new IllegalStateException("JSON provider is already initialized with " + current.name()
                        + " and cannot be replaced by " + provider.name());
            }
            if (state.bound()) {
                throw new IllegalStateException("JSON provider is already bound with " + current.name()
                        + " and cannot be replaced by another " + provider.name() + " instance");
            }
            if (DEFAULT_PROVIDER.compareAndSet(state, new ProviderState(provider, true))) {
                return;
            }
        }
    }

    /**
     * Removes an application-managed provider when it is still the active global provider.
     *
     * @param provider provider previously installed by the owning application context
     * @return {@code true} when the supplied provider was removed
     */
    public static boolean uninstall(JsonProvider provider) {
        if (provider == null) {
            return false;
        }
        while (true) {
            ProviderState state = DEFAULT_PROVIDER.get();
            if (state == null || state.provider() != provider || !state.bound()) {
                return false;
            }
            if (DEFAULT_PROVIDER.compareAndSet(state, null)) {
                return true;
            }
        }
    }

    /**
     * Creates a provider with the requested name without changing the application-wide provider state.
     *
     * @param requestedProvider provider name, or {@code auto}
     * @return selected provider
     */
    private static JsonProvider create(String requestedProvider) {
        String requested = normalize(requestedProvider);
        List<JsonProvider> providers = loadAvailableProviders();
        if (providers.isEmpty()) {
            throw new InternalException(
                    "No JSON provider is available. Add Fastjson2, Gson, or Jackson 3 to the project.");
        }
        if (!"auto".equals(requested)) {
            return providers.stream().filter(provider -> normalize(provider.name()).equals(requested)).findFirst()
                    .orElseThrow(
                            () -> new InternalException("JSON provider '" + requested
                                    + "' is not available. Available providers: " + providerNames(providers)));
        }
        if (providers.size() != 1) {
            throw new InternalException("Multiple JSON providers are available: " + providerNames(providers)
                    + ". Configure bus.json.provider explicitly.");
        }
        return providers.getFirst();
    }

    /**
     * Normalizes a configured provider name and maps the Fastjson2 alias to its canonical provider name.
     *
     * @param name configured provider name; {@code null} and blank values mean {@code auto}
     * @return canonical lower-case provider name
     */
    private static String normalize(String name) {
        String normalized = name == null ? "auto" : name.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return "auto";
        }
        return "fastjson2".equals(normalized) ? "fastjson" : normalized;
    }

    /**
     * Returns provider names in deterministic alphabetical order for diagnostics.
     *
     * @param providers available providers
     * @return sorted provider names
     */
    private static List<String> providerNames(List<JsonProvider> providers) {
        return providers.stream().map(JsonProvider::name).sorted().toList();
    }

    /**
     * Loads legacy Provider SPI entries independently. Optional JSON engines are intentionally absent from many
     * applications, so a linkage failure in one entry must not prevent later candidates from being inspected.
     *
     * @return providers that can be instantiated from the current runtime classpath
     */
    private static List<JsonProvider> loadAvailableProviders() {
        ServiceLoader<JsonProvider> loader = NormalSpiLoader.loadList(JsonProvider.class);
        Map<String, JsonProvider> providers = new LinkedHashMap<>();
        for (String serviceName : loader.getServiceNames()) {
            try {
                JsonProvider provider = loader.getService(serviceName);
                if (provider != null) {
                    providers.putIfAbsent(normalize(provider.name()), provider);
                }
            } catch (RuntimeException | ServiceConfigurationError | LinkageError unavailable) {
                // An optional engine is not installed or the third-party provider is incompatible.
            }
        }
        return new ArrayList<>(providers.values());
    }

    /**
     * Application-wide provider state. An unbound provider is created by {@link #get()} and may be taken over by the
     * dependency-injection managed provider of the same type. A bound provider is owned by an application context and
     * cannot be replaced by another instance.
     *
     * @param provider active provider
     * @param bound    whether the provider has been explicitly installed by an application context
     */
    private record ProviderState(JsonProvider provider, boolean bound) {

    }

}
