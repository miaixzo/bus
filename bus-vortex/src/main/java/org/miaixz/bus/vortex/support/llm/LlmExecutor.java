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

import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.support.Coordinator;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

/**
 * Executor for Large Language Model (LLM) proxy requests.
 * <p>
 * This executor supports multiple providers per project API key, allowing a single project API key to access models
 * from different LLM services (OpenAIProvider, Gemini, Qwen, VLLM, Ollama, etc.). The actual model API keys are stored
 * in Assets metadata.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LlmExecutor extends Coordinator {

    /**
     * The factory for creating and caching LLM provider instances.
     * <p>
     * This factory is responsible for creating {@link LlmProvider} instances based on provider configuration extracted
     * from Assets metadata. It maintains a cache to reuse provider instances with the same configuration.
     * </p>
     */
    private final LlmFactory factory;

    /**
     * Constructs a new {@code LlmExecutor}.
     *
     * @param factory The factory for creating LLM providers.
     */
    public LlmExecutor(final LlmFactory factory) {
        this.factory = factory;
    }

    /**
     * Executes an LLM request.
     *
     * @param context The request context containing Assets, model name, and request body.
     * @param args    Additional arguments (not used for LLM requests).
     * @return A {@link Mono} emitting the server response.
     */
    @Override
    public Mono<ServerResponse> execute(final Context context, final Object args) {
        final String modelName = (String) context.getParameters().get("modelName");
        final Assets assets = context.getAssets();

        Logger.debug(true, "LLM", "{} Executing LLM request for model: {}", context.getX_request_ip(), modelName);

        // Validate Assets
        if (assets == null) {
            Logger.warn(true, "LLM", "{} No Assets found in context", context.getX_request_ip());
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(
                    "{\"error\":{\"message\":\"Assets configuration not found\",\"type\":\"server_error\",\"code\":\"assets_not_found\"}}");
        }

        // Find the provider configuration for the requested model from Assets metadata
        final ProviderConfig providerConfig;
        try {
            providerConfig = findProviderForModel(assets, modelName);
        } catch (Exception e) {
            Logger.error(
                    true,
                    "LLM",
                    "{} Failed to parse provider config: {}",
                    context.getX_request_ip(),
                    e.getMessage());
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(
                    "{\"error\":{\"message\":\"Failed to parse provider configuration\",\"type\":\"server_error\",\"code\":\"config_parse_error\"}}");
        }

        if (providerConfig == null) {
            Logger.warn(true, "LLM", "{} Model not found in any provider: {}", context.getX_request_ip(), modelName);
            return ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(
                    "{\"error\":{\"message\":\"Model not found: " + modelName
                            + "\",\"type\":\"invalid_request_error\",\"code\":\"model_not_found\"}}");
        }

        Logger.debug(
                true,
                "LLM",
                "{} Found provider for model {}: type={}, baseUrl={}",
                context.getX_request_ip(),
                modelName,
                providerConfig.type,
                providerConfig.endpoint);

        // Get or create provider
        final LlmProvider provider;
        try {
            provider = this.factory.getProvider(
                    providerConfig.type,
                    providerConfig.endpoint,
                    providerConfig.apiKey,
                    providerConfig.model);
        } catch (Exception e) {
            Logger.error(true, "LLM", "{} Failed to create provider: {}", context.getX_request_ip(), e.getMessage());
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(
                    "{\"error\":{\"message\":\"Failed to create provider\",\"type\":\"server_error\",\"code\":\"provider_creation_error\"}}");
        }

        // Parse request body from ServerRequest
        final ServerRequest serverRequest = (ServerRequest) context.getParameters().get("serverRequest");
        return serverRequest.bodyToMono(String.class).flatMap(body -> {
            final LlmRequest request;
            try {
                request = parseRequest(body, modelName);
            } catch (Exception e) {
                Logger.error(true, "LLM", "{} Failed to parse request: {}", context.getX_request_ip(), e.getMessage());
                return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(
                        "{\"error\":{\"message\":\"Invalid request body\",\"type\":\"invalid_request_error\",\"code\":\"invalid_request_body\"}}");
            }

            // Check if streaming is requested
            final boolean stream = request.isStream();

            if (stream) {
                // Streaming response (SSE)
                Logger.debug(true, "LLM", "{} Streaming response for model: {}", context.getX_request_ip(), modelName);
                return ServerResponse.ok().contentType(org.springframework.http.MediaType.TEXT_EVENT_STREAM)
                        .body(provider.stream(request), String.class);
            } else {
                // Non-streaming response (JSON)
                Logger.debug(
                        true,
                        "LLM",
                        "{} Non-streaming response for model: {}",
                        context.getX_request_ip(),
                        modelName);
                return provider.chat(request).flatMap(
                        response -> ServerResponse.ok().contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .bodyValue(response))
                        .onErrorResume(e -> {
                            Logger.error(
                                    true,
                                    "LLM",
                                    "{} LLM request failed: {}",
                                    context.getX_request_ip(),
                                    e.getMessage());
                            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).bodyValue(
                                    "{\"error\":{\"message\":\"LLM request failed: " + e.getMessage()
                                            + "\",\"type\":\"server_error\",\"code\":\"llm_request_failed\"}}");
                        });
            }
        });
    }

    /**
     * Finds the provider configuration that supports the specified model.
     * <p>
     * This method parses the Assets metadata JSON and searches through the providers array to find one that includes
     * the requested model.
     *
     * @param assets    The Assets configuration.
     * @param modelName The model name to search for.
     * @return The matching {@link ProviderConfig}, or {@code null} if not found.
     */
    private ProviderConfig findProviderForModel(final Assets assets, final String modelName) {
        final String metadata = assets.getMetadata();
        if (StringKit.isBlank(metadata)) {
            return null;
        }

        final Map<String, Object> root = JsonKit.toMap(metadata);
        final Object providersObj = root.get("providers");

        if (!(providersObj instanceof List)) {
            Logger.warn(true, "LLM", "Invalid metadata format: 'providers' array not found");
            return null;
        }

        final List<Map<String, Object>> providers = (List<Map<String, Object>>) providersObj;

        // Iterate through providers to find one that supports the model
        for (Map<String, Object> provider : providers) {
            final String type = (String) provider.get("type");
            final String endpoint = (String) provider.get("endpoint");
            final String apiKey = (String) provider.get("apiKey");
            final String model = (String) provider.get("model");

            // Check if this provider supports the requested model
            final Object modelsObj = provider.get("models");
            if (modelsObj instanceof List) {
                final List<String> models = (List<String>) modelsObj;
                for (String mod : models) {
                    if (mod.equals(modelName)) {
                        // Found matching provider
                        return new ProviderConfig(type, endpoint, apiKey, model);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Parses the request body into an {@link LlmRequest}.
     *
     * @param body      The request body JSON string.
     * @param modelName The model name to use.
     * @return The parsed {@link LlmRequest}.
     */
    private LlmRequest parseRequest(final String body, final String modelName) {
        final LlmRequest request = JsonKit.toPojo(body, LlmRequest.class);

        // Set the model name from the URL path
        request.setModel(modelName);

        return request;
    }

    /**
     * Internal class to hold provider configuration extracted from Assets metadata.
     * <p>
     * This class encapsulates the configuration details required to create and use an LLM provider. The configuration
     * is typically parsed from the Assets metadata JSON, which contains an array of provider configurations.
     * </p>
     * <p>
     * Example metadata structure:
     * </p>
     *
     * <pre>
     * {
     *   "providers": [
     *     {
     *       "type": "openai",
     *       "endpoint": "https://api.openai.com",
     *       "apiKey": "sk-...",
     *       "model": "gpt-4",
     *       "models": ["gpt-4", "gpt-3.5-turbo"]
     *     }
     *   ]
     * }
     * </pre>
     */
    static class ProviderConfig {

        /**
         * The provider type identifier.
         * <p>
         * Examples: "openai", "gemini", "qwen", "vllm", "ollama", "azure".
         * </p>
         */
        final String type;

        /**
         * The base endpoint URL of the LLM service.
         * <p>
         * Example: "https://api.openai.com".
         * </p>
         */
        final String endpoint;

        /**
         * The API key for authenticating requests to the LLM service.
         * <p>
         * This key is used in the Authorization header as "Bearer {apiKey}".
         * </p>
         */
        final String apiKey;

        /**
         * The default model name for this provider.
         * <p>
         * Examples: "gpt-4", "gemini-pro", "qwen-turbo". This is used as a fallback when the request does not specify a
         * model.
         * </p>
         */
        final String model;

        /**
         * Constructs a new {@code ProviderConfig} with the specified configuration.
         *
         * @param type     The provider type identifier.
         * @param endpoint The base endpoint URL.
         * @param apiKey   The API key for authentication.
         * @param model    The default model name.
         */
        ProviderConfig(String type, String endpoint, String apiKey, String model) {
            this.type = type;
            this.endpoint = endpoint;
            this.apiKey = apiKey;
            this.model = model;
        }
    }

}
