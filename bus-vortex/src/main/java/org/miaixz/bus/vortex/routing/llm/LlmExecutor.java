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

import java.util.List;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.errors.ErrorType;
import org.miaixz.bus.errors.transport.http.PublicHttpError;
import org.miaixz.bus.errors.transport.reactive.ReactiveErrorHandler;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.Octets;
import org.miaixz.bus.vortex.guard.AdmissionGate;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.routing.Coordinator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Executor for Large Language Model (LLM) proxy requests.
 * <p>
 * This executor supports multiple providers per project API key, allowing a single project API key to access models
 * from different LLM services (OpenAIProvider, Gemini, Qwen, VLLM, Ollama, etc.). The actual model API keys are stored
 * in Assets metadata.
 *
 * @author Kimi Liu
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
     * Executes an LLM request under bounded request and streaming capacity.
     * <p>
     * The JSON request may use Content-Length or chunked transfer encoding and retains its logical-byte lease while the
     * parsed request model is live. Streaming responses additionally own a realtime-stream permit through completion,
     * failure or client cancellation.
     *
     * @param context The request context containing Assets, model name, and request body.
     * @param args    Additional arguments (not used for LLM requests).
     * @return A {@link Mono} emitting the server response.
     */
    @Override
    public Mono<ServerResponse> execute(final Context context, final Object args) {
        final String modelName = (String) context.getParameters().get("modelName");
        final Assets assets = context.getAssets();

        Logger.debug(true, "Vortex", "{} Executing LLM request for model: {}", context.getX_request_ip(), modelName);

        if (assets == null) {
            return error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorType.CONFIGURATION_FAILURE,
                    null,
                    "assets_not_found",
                    "Assets configuration not found");
        }

        final ProviderConfig providerConfig;
        try {
            providerConfig = findProviderForModel(assets, modelName);
        } catch (Exception e) {
            return error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorType.CONFIGURATION_FAILURE,
                    e,
                    "config_parse_error",
                    "Failed to parse provider configuration");
        }

        if (providerConfig == null) {
            return error(HttpStatus.NOT_FOUND, ErrorType.ROUTE_NOT_FOUND, null, "model_not_found", "Model not found");
        }

        Logger.debug(
                true,
                "Vortex",
                "{} Found provider for model {}: type={}, baseUrl={}",
                context.getX_request_ip(),
                modelName,
                providerConfig.type,
                providerConfig.endpoint);

        final LlmProvider provider;
        try {
            provider = this.factory.getProvider(
                    providerConfig.type,
                    providerConfig.endpoint,
                    providerConfig.apiKey,
                    providerConfig.model);
        } catch (Exception e) {
            return error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ErrorType.CONFIGURATION_FAILURE,
                    e,
                    "provider_creation_error",
                    "Failed to create provider");
        }

        final ServerRequest serverRequest = (ServerRequest) context.getParameters().get("serverRequest");
        return Octets
                .readForParsing(
                        serverRequest.bodyToFlux(DataBuffer.class),
                        Math.toIntExact(Holder.get().getMaxBufferedRequestSize()),
                        Holder.requestBufferBudget(),
                        serverRequest.headers().contentLength().orElse(-1))
                .onErrorMap(DataBufferLimitException.class, error -> new ValidateException(ErrorCode._100530))
                .flatMap(bufferedBody -> {
                    String body = new String(bufferedBody.bytes(), Charset.UTF_8);
                    bufferedBody.discardBytes();
                    Logger.debug(
                            true,
                            "Vortex",
                            "Request parameter snapshot: clientIp={}, model={}, bodyChars={}",
                            context.getX_request_ip(),
                            modelName,
                            body.length());
                    Logger.debug(
                            true,
                            "Vortex",
                            "Request parameters: clientIp={}, parameters={}",
                            context.getX_request_ip(),
                            Map.of("body", body));
                    final LlmRequest request;
                    try {
                        request = parseRequest(body, modelName);
                    } catch (Exception e) {
                        return error(
                                HttpStatus.BAD_REQUEST,
                                ErrorType.INVALID_REQUEST,
                                e,
                                "invalid_request_body",
                                "Invalid request body").doFinally(signal -> bufferedBody.close());
                    }

                    final boolean stream = request.isStream();

                    if (stream) {
                        Logger.debug(
                                true,
                                "Vortex",
                                "{} Streaming response for model: {}",
                                context.getX_request_ip(),
                                modelName);
                        Flux<String> streamBody = Flux.using(() -> {
                            AdmissionGate.Lease lease = Holder.admissionGate().tryAcquireRealtimeStream();
                            if (lease == null || Holder.memoryPressure().rejectStreaming()) {
                                if (lease != null) {
                                    lease.close();
                                }
                                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                                        "Realtime streaming capacity exhausted");
                            }
                            return lease;
                        }, lease -> provider.stream(request), AdmissionGate.Lease::close);
                        return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM)
                                .body(streamBody, String.class).doFinally(signal -> bufferedBody.close());
                    } else {
                        Logger.debug(
                                true,
                                "Vortex",
                                "{} Non-streaming response for model: {}",
                                context.getX_request_ip(),
                                modelName);
                        return provider.chat(request)
                                .flatMap(
                                        response -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(response))
                                .onErrorResume(
                                        e -> error(
                                                HttpStatus.INTERNAL_SERVER_ERROR,
                                                ErrorType.DEPENDENCY_UNAVAILABLE,
                                                e,
                                                "llm_request_failed",
                                                "LLM request failed"))
                                .doFinally(signal -> bufferedBody.close());
                    }
                });
    }

    /**
     * Renders the existing LLM envelope using a code-owned error definition.
     *
     * @param status  verified response status
     * @param type    shared classification
     * @param failure optional diagnostic cause
     * @param code    fixed LLM error code
     * @param message fixed public description
     * @return response owned by the common error boundary
     */
    public static Mono<ServerResponse> error(
            HttpStatus status,
            ErrorType type,
            Throwable failure,
            String code,
            String message) {
        String category = status.is5xxServerError() ? "server_error" : "invalid_request_error";
        return ReactiveErrorHandler.render(
                status,
                type,
                failure,
                new PublicHttpError(code, message),
                error -> ServerResponse.status(status).bodyValue(
                        JsonKit.toJsonString(
                                Map.of(
                                        "error",
                                        Map.of(
                                                "message",
                                                error.description(),
                                                "type",
                                                category,
                                                "code",
                                                error.code())))));
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

        Object decoded = JsonKit.toJavaValue(JsonKit.readValue(metadata.getBytes(Charset.UTF_8), 64, true));
        if (!(decoded instanceof Map<?, ?> root) || !(root.get("providers") instanceof List<?> providers)) {
            return null;
        }

        for (Object value : providers) {
            if (value instanceof Map<?, ?> provider && provider.get("models") instanceof List<?> models
                    && models.contains(modelName)) {
                return new ProviderConfig((String) provider.get("type"), (String) provider.get("endpoint"),
                        (String) provider.get("apiKey"), (String) provider.get("model"));
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
     *
     * @author Kimi Liu
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
