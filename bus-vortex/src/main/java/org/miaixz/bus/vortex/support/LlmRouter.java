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
package org.miaixz.bus.vortex.support;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.support.llm.LlmExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

/**
 * Router implementation for Large Language Model (LLM) proxy requests.
 * <p>
 * This router handles requests to the {@code /router/llm/{model}} endpoint and delegates execution to the
 * {@link LlmExecutor}. It extracts the model name from the URL path and validates the project API key from the request
 * headers.
 * <p>
 * The project API key is used for authentication at the bus-vortex level, while the actual model API keys are stored in
 * Assets metadata and used by providers to access the real LLM services.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LlmRouter implements Router<ServerRequest, ServerResponse> {

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
     * This method extracts the model name from the URL path (e.g., {@code /router/llm/gpt-4}), validates the project
     * API key from the {@code X-API-Key} header, and delegates to the executor.
     *
     * @param input The incoming server request.
     * @return A {@link Mono} emitting the server response.
     */
    @Override
    public Mono<ServerResponse> route(final ServerRequest input) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);

            // Extract model name from path: /router/llm/{model}
            final String path = input.path();
            final String modelName = extractModelName(path);

            if (StringKit.isBlank(modelName)) {
                Logger.warn(true, "LLM", "{} Model name is missing in path: {}", context.getX_request_ip(), path);
                return ServerResponse.status(HttpStatus.BAD_REQUEST).bodyValue(
                        "{\"error\":{\"message\":\"Model name is required in path: /router/llm/{model}\",\"type\":\"invalid_request_error\",\"code\":\"model_name_missing\"}}");
            }

            // Extract project API key from header
            final String projectApiKey = input.headers().firstHeader("X-API-Key");
            if (StringKit.isBlank(projectApiKey)) {
                Logger.warn(true, "LLM", "{} Project API key is missing in header", context.getX_request_ip());
                return ServerResponse.status(HttpStatus.UNAUTHORIZED).bodyValue(
                        "{\"error\":{\"message\":\"Project API key is required in X-API-Key header\",\"type\":\"authentication_error\",\"code\":\"api_key_missing\"}}");
            }

            // Store model name, project API key, and ServerRequest in context
            context.getParameters().put("modelName", modelName);
            context.getParameters().put("projectApiKey", projectApiKey);
            context.getParameters().put("serverRequest", input);

            Logger.debug(true, "LLM", "{} Routing request to model: {}", context.getX_request_ip(), modelName);

            // Delegate to executor
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
