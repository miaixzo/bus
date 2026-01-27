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

import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A unified provider implementation for all OpenAIProvider-compatible LLM services.
 * <p>
 * This provider supports multiple LLM services that implement the OpenAIProvider Chat Completions API:
 * <ul>
 * <li>OpenAIProvider</li>
 * <li>Google Gemini (via OpenAIProvider-compatible endpoint)</li>
 * <li>Alibaba Qwen</li>
 * <li>VLLM</li>
 * <li>Ollama</li>
 * <li>Azure OpenAIProvider</li>
 * <li>LocalAI</li>
 * <li>LM Studio</li>
 * <li>Text Generation WebUI</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OpenAIProvider implements LlmProvider {

    /**
     * The provider type identifier.
     * <p>
     * Examples: "openai", "gemini", "qwen", "vllm", "ollama", "azure", "localai".
     * </p>
     */
    private final String type;

    /**
     * The base endpoint URL of the LLM service.
     * <p>
     * Trailing slashes are automatically removed during initialization. Example: "https://api.openai.com".
     * </p>
     */
    private final String endpoint;

    /**
     * The API key for authenticating requests to the LLM service.
     * <p>
     * This key is included in the Authorization header as "Bearer {apiKey}".
     * </p>
     */
    private final String apiKey;

    /**
     * The default model name to use when not specified in the request.
     * <p>
     * Examples: "gpt-4", "gemini-pro", "qwen-turbo". Can be null if the model is always specified in requests.
     * </p>
     */
    private final String model;

    /**
     * The pre-configured WebClient instance for making HTTP requests to the LLM service.
     * <p>
     * This client is initialized with the base URL, API key authentication header, and content type header.
     * </p>
     */
    private final WebClient webClient;

    /**
     * Constructs a new {@code OpenAIProvider} instance.
     * <p>
     * This constructor initializes the provider with the specified configuration and creates a pre-configured WebClient
     * for making HTTP requests to the LLM service. The endpoint URL is normalized by removing any trailing slashes.
     * </p>
     *
     * @param type     The provider type identifier (e.g., "openai", "gemini", "qwen", "vllm", "ollama"). Must not be
     *                 blank.
     * @param endpoint The base endpoint URL of the LLM service (e.g., "https://api.openai.com"). Must not be blank.
     *                 Trailing slashes are automatically removed.
     * @param apiKey   The API key for authentication. Must not be blank. This key is used in the Authorization header
     *                 as "Bearer {apiKey}".
     * @param model    The default model name to use when not specified in requests (e.g., "gpt-4", "gemini-pro"). Can
     *                 be null if the model is always provided in requests.
     * @throws IllegalArgumentException if type, endpoint, or apiKey is blank.
     */
    public OpenAIProvider(final String type, final String endpoint, final String apiKey, final String model) {
        Assert.notBlank(type, "Provider type must not be blank");
        Assert.notBlank(endpoint, "Endpoint URL must not be blank");
        Assert.notBlank(apiKey, "API key must not be blank");

        this.type = type;
        this.endpoint = endpoint.endsWith(Symbol.SLASH) ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        this.apiKey = apiKey;
        this.model = model;

        // Build WebClient with API key authentication
        this.webClient = WebClient.builder().baseUrl(this.endpoint)
                .defaultHeader(HTTP.AUTHORIZATION, HTTP.BEARER + this.apiKey)
                .defaultHeader(HTTP.CONTENT_TYPE, "application/json").build();

        Logger.info(true, "LLM", "Initialized {} provider: endpoint={}", type, endpoint);
    }

    /**
     * Sends a non-streaming chat completion request to the LLM service.
     * <p>
     * This method sends a POST request to the {@code /v1/chat/completions} endpoint with the provided request
     * parameters. The response is buffered and returned as a complete {@link LlmResponse} object.
     * </p>
     *
     * @param request The LLM request containing messages, model, and optional parameters (temperature, max_tokens,
     *                etc.).
     * @return A {@link Mono} emitting the complete {@link LlmResponse} when the request completes successfully.
     * @throws RuntimeException if the request fails or the response cannot be parsed.
     */
    @Override
    public Mono<LlmResponse> chat(final LlmRequest request) {
        final Map<String, Object> requestBody = toRequestBody(request, false);

        Logger.debug(true, "LLM", "Sending non-streaming request to {}: model={}", type, request.getModel());

        return webClient.post().uri("/v1/chat/completions").bodyValue(requestBody).retrieve().bodyToMono(String.class)
                .map(this::parseResponse)
                .doOnError(e -> Logger.error(true, "LLM", "Request failed: {}", e.getMessage()));
    }

    /**
     * Sends a streaming chat completion request to the LLM service.
     * <p>
     * This method sends a POST request to the {@code /v1/chat/completions} endpoint with streaming enabled. The
     * response is returned as a {@link Flux} of Server-Sent Events (SSE) formatted strings, allowing real-time
     * processing of the LLM's output as it is generated.
     * </p>
     * <p>
     * Each chunk is formatted as SSE data: {@code "data: {json}\n\n"}.
     * </p>
     *
     * @param request The LLM request containing messages, model, and optional parameters.
     * @return A {@link Flux} emitting SSE-formatted strings as the LLM generates the response. Each emission represents
     *         a chunk of the streaming response.
     * @throws RuntimeException if the streaming request fails.
     */
    @Override
    public Flux<String> stream(final LlmRequest request) {
        final Map<String, Object> requestBody = toRequestBody(request, true);

        Logger.debug(true, "LLM", "Sending streaming request to {}: model={}", type, request.getModel());

        return webClient.post().uri("/v1/chat/completions").bodyValue(requestBody).retrieve().bodyToFlux(String.class)
                .map(chunk -> "data: " + chunk + "\n\n")
                .doOnError(e -> Logger.error(true, "LLM", "Streaming request failed: {}", e.getMessage()));
    }

    /**
     * Returns the provider type identifier.
     * <p>
     * This identifier is used to distinguish between different LLM service providers in the system.
     * </p>
     *
     * @return The provider type (e.g., "openai", "gemini", "qwen", "vllm", "ollama").
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Returns the base endpoint URL of the LLM service.
     * <p>
     * This URL is used as the base for all API requests to the LLM service.
     * </p>
     *
     * @return The endpoint URL (e.g., "https://api.openai.com").
     */
    @Override
    public String getUrl() {
        return endpoint;
    }

    /**
     * Converts an {@link LlmRequest} to a request body map for the OpenAI-compatible API.
     * <p>
     * This method builds a request body map containing all the parameters required by the OpenAI Chat Completions API.
     * It includes the model name, messages, streaming flag, and optional parameters such as temperature, max_tokens,
     * top_p, frequency_penalty, presence_penalty, stop sequences, and user identifier.
     * </p>
     * <p>
     * If the request does not specify a model, the default model configured in this provider is used. Optional
     * parameters are only included in the request body if they are not null.
     * </p>
     *
     * @param request The LLM request containing the parameters to convert.
     * @param stream  Whether to enable streaming mode. If {@code true}, the API will return a stream of partial
     *                responses; if {@code false}, it will return a single complete response.
     * @return A map representing the request body to be sent to the LLM service. The map contains keys such as "model",
     *         "messages", "stream", "temperature", "max_tokens", etc.
     */
    private Map<String, Object> toRequestBody(final LlmRequest request, final boolean stream) {
        final Map<String, Object> body = new HashMap<>();
        body.put("model", StringKit.isNotBlank(request.getModel()) ? request.getModel() : model);
        body.put("messages", request.getMessages());
        body.put("stream", stream);

        if (request.getTemperature() != null) {
            body.put("temperature", request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            body.put("max_tokens", request.getMaxTokens());
        }
        if (request.getTopP() != null) {
            body.put("top_p", request.getTopP());
        }
        if (request.getFrequencyPenalty() != null) {
            body.put("frequency_penalty", request.getFrequencyPenalty());
        }
        if (request.getPresencePenalty() != null) {
            body.put("presence_penalty", request.getPresencePenalty());
        }
        if (request.getStop() != null) {
            body.put("stop", request.getStop());
        }
        if (request.getUser() != null) {
            body.put("user", request.getUser());
        }

        return body;
    }

    /**
     * Parses the JSON response string into an {@link LlmResponse} object.
     * <p>
     * This method deserializes the JSON response received from the LLM service into a structured {@link LlmResponse}
     * object. If parsing fails, an error is logged and a {@link RuntimeException} is thrown.
     * </p>
     *
     * @param json The JSON response string received from the LLM service.
     * @return The parsed {@link LlmResponse} object containing the completion result, usage statistics, and other
     *         metadata.
     * @throws RuntimeException if the JSON cannot be parsed into an {@link LlmResponse} object.
     */
    private LlmResponse parseResponse(final String json) {
        try {
            return JsonKit.toPojo(json, LlmResponse.class);
        } catch (Exception e) {
            Logger.error(true, "LLM", "Failed to parse response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse LLM response", e);
        }
    }

}
