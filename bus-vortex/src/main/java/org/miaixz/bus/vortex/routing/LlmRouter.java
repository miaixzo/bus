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
package org.miaixz.bus.vortex.routing;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.routing.llm.LlmExecutor;

import reactor.core.publisher.Mono;

/**
 * Router implementation for Large Language Model (LLM) proxy requests.
 * <p>
 * This router handles requests to the {@code /router/llm/{model}} endpoint and delegates execution to the
 * {@link LlmExecutor}. It extracts the model name from the URL path after the shared qualifier has resolved the route
 * asset and authorized the request credential.
 * <p>
 * Client credentials are resolved uniformly by {@code QualifierStrategy}: a standard Bearer token has priority over the
 * supported API-key headers. Provider API keys remain stored in Assets metadata and are used only for calls to the
 * underlying LLM service.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class LlmRouter implements Router<ServerRequest, ServerResponse> {

    /**
     * Executor responsible for provider selection and LLM request dispatch.
     */
    private final LlmExecutor executor;

    /**
     * Constructs a new {@code LlmRouter} with the specified executor.
     *
     * @param executor The LLM executor to handle the actual request processing.
     */
    public LlmRouter(final LlmExecutor executor) {
        this.executor = executor;
    }

    /**
     * Routes an LLM request to the appropriate executor.
     * <p>
     * This method extracts the model name from the URL path (e.g., {@code /router/llm/gpt-4}) and delegates to the
     * executor. Route qualification and credential authorization have already completed before the router is invoked.
     *
     * @param input The incoming server request.
     * @return A {@link Mono} emitting the server response.
     */
    @Override
    public Mono<ServerResponse> route(final ServerRequest input) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);

            final String path = input.path();
            final String modelName = extractModelName(path);

            if (StringKit.isBlank(modelName)) {
                Logger.warn(false, "Vortex", "{} Model name is missing in path: {}", context.getX_request_ip(), path);
                return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(
                        "{\"error\":{\"message\":\"Model name is required in path: /router/llm/{model}\",\"type\":\"invalid_request_error\",\"code\":\"model_name_missing\"}}");
            }

            context.getParameters().put("modelName", modelName);
            context.getParameters().put("serverRequest", input);

            Logger.debug(
                    true,
                    "Vortex",
                    "Request header snapshot: clientIp={}, path={}, model={}",
                    context.getX_request_ip(),
                    path,
                    modelName);
            Logger.debug(
                    true,
                    "Vortex",
                    "Request headers: clientIp={}, headers={}",
                    context.getX_request_ip(),
                    input.headers().asHttpHeaders().toSingleValueMap());
            Logger.debug(
                    true,
                    "Vortex",
                    "Request parameters: clientIp={}, parameters={}",
                    context.getX_request_ip(),
                    input.queryParams().toSingleValueMap());
            Logger.debug(true, "Vortex", "{} Routing request to model: {}", context.getX_request_ip(), modelName);

            return executor.execute(context, null);
        });
    }

    /**
     * Extracts the model name from the URL path.
     * <p>
     * Example: {@code /router/llm/gpt-4} → {@code gpt-4}
     *
     * @param path The URL path.
     * @return The model name, or {@code null} if not found.
     */
    private String extractModelName(final String path) {
        if (path.startsWith(Args.LLM_PATH_PREFIX) && path.length() > Args.LLM_PATH_PREFIX.length() + 1) {
            return path.substring(Args.LLM_PATH_PREFIX.length() + 1);
        }
        return null;
    }

}
