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

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.support.grpc.GrpcExecutor;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

/**
 * A {@link Router} implementation for forwarding requests to gRPC services.
 * <p>
 * This class acts as a simple coordinator. Its sole responsibility is to retrieve the necessary context and request
 * body, then delegate the actual execution to the {@link GrpcExecutor}.
 * <p>
 * The executor handles all protocol-specific logic including:
 * <ul>
 * <li>Invoking the gRPC method via HTTP gateway</li>
 * <li>Selecting execution strategy (streaming vs buffering)</li>
 * <li>Building the appropriate {@link ServerResponse}</li>
 * </ul>
 * <p>
 * Generic type parameters: {@code Router<ServerRequest, ServerResponse>}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GrpcRouter implements Router<ServerRequest, ServerResponse> {

    /**
     * The executor responsible for executing gRPC calls.
     */
    private final GrpcExecutor executor;

    /**
     * Constructs a new {@code GrpcRouter}.
     *
     * @param executor The executor that will perform the gRPC invocation.
     */
    public GrpcRouter(GrpcExecutor executor) {
        this.executor = executor;
    }

    /**
     * Routes a client request by invoking a gRPC method on the target service.
     * <p>
     * This method retrieves the {@link Context} from the reactive stream and delegates the execution to the
     * {@link GrpcExecutor}. The executor is responsible for protocol interaction, strategy selection, and response
     * building.
     *
     * @param input The ServerRequest object (strongly typed)
     * @return A {@code Mono<ServerResponse>} containing the response from the gRPC service
     */
    @Override
    public Mono<ServerResponse> route(ServerRequest input) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);

            Logger.info(
                    true,
                    "gRPC",
                    "[GRPC_ROUTER_START] - Routing request to gRPC service: {}",
                    context.getAssets().getMethod());

            // Read request body and delegate to executor
            return input.bodyToMono(String.class).switchIfEmpty(Mono.just("{}"))
                    .flatMap(body -> executor.execute(context, body)).doOnError(
                            error -> Logger.error(
                                    true,
                                    "gRPC",
                                    "[GRPC_ROUTER_ERROR] - Failed to invoke gRPC service: {} - {}",
                                    context.getAssets().getMethod(),
                                    error.getMessage()));
        });
    }

}
