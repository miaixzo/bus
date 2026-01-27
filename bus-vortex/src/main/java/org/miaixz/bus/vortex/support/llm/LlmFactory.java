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
package org.miaixz.bus.vortex.support.llm;

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
 * @since Java 17+
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
