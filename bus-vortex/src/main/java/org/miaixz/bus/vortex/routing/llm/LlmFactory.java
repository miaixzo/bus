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
package org.miaixz.bus.vortex.routing.llm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.logger.Logger;

/**
 * Factory for creating and caching {@link LlmProvider} instances.
 * <p>
 * This factory creates providers dynamically based on provider configuration. Providers are cached by a composite key
 * (type + endpoint + apiKey) to avoid repeated creation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class LlmFactory {

    /**
     * Cache for storing created LLM provider instances.
     * <p>
     * The cache key is a composite string in the format: {@code "type:endpoint:apiKey"}. This ensures that providers
     * with the same configuration are reused, avoiding redundant object creation and connection establishment.
     * </p>
     * <p>
     * Thread-safe: Uses {@link ConcurrentHashMap} to support concurrent access from multiple threads.
     * </p>
     */
    private final Map<String, LlmProvider> providerCache = new ConcurrentHashMap<>();

    /**
     * Gets or creates an LLM provider for the specified configuration.
     *
     * @param type     The provider type (e.g., "openai", "gemini", "qwen").
     * @param endpoint The endpoint URL of the LLM service.
     * @param apiKey   The API key for authentication.
     * @param model    The default model to use.
     * @return The LLM provider instance.
     */
    public LlmProvider getProvider(final String type, final String endpoint, final String apiKey, final String model) {
        Assert.notBlank(type, "Provider type must not be blank");
        Assert.notBlank(endpoint, "Endpoint URL must not be blank");
        Assert.notBlank(apiKey, "API key must not be blank");

        // Create cache key: type:endpoint:apiKey
        final String cacheKey = type + ":" + endpoint + ":" + apiKey;

        return providerCache.computeIfAbsent(cacheKey, key -> {
            Logger.info(true, "LLM", "Creating provider: type={}, endpoint={}", type, endpoint);

            return new OpenAIProvider(type, endpoint, apiKey, model);
        });
    }

    /**
     * Clears the provider cache. Useful for testing or when configuration changes.
     */
    public void clearCache() {
        providerCache.clear();
        Logger.info(true, "LLM", "Provider cache cleared");
    }

    /**
     * Gets the number of cached providers.
     *
     * @return The cache size.
     */
    public int getCacheSize() {
        return providerCache.size();
    }

}
