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

import java.util.Map;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.routing.grpc.GrpcExecutor;

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
 * @since Java 21+
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
                    "Vortex",
                    "GRPC routing started: protocol=grpc, event=GRPC_ROUTER_START, service={}",
                    context.getAssets().getMethod());
            Logger.debug(
                    true,
                    "Vortex",
                    "Request header snapshot: protocol=grpc, clientIp={}, path={}",
                    context.getX_request_ip(),
                    input.path());
            Logger.debug(
                    true,
                    "Vortex",
                    "Request headers: protocol=grpc, clientIp={}, headers={}",
                    context.getX_request_ip(),
                    input.headers().asHttpHeaders().toSingleValueMap());

            return input.bodyToMono(String.class).switchIfEmpty(Mono.just("{}")).flatMap(body -> {
                Logger.debug(
                        true,
                        "Vortex",
                        "Request parameter snapshot: protocol=grpc, clientIp={}, path={}, bodyChars={}",
                        context.getX_request_ip(),
                        input.path(),
                        body.length());
                Logger.debug(
                        true,
                        "Vortex",
                        "Request parameters: protocol=grpc, clientIp={}, parameters={}",
                        context.getX_request_ip(),
                        Map.of("body", body));
                return executor.execute(context, body);
            }).doOnError(
                    error -> Logger.error(
                            true,
                            "Vortex",
                            "GRPC routing failed: protocol=grpc, event=GRPC_ROUTER_ERROR, service={}, exception={}",
                            context.getAssets().getMethod(),
                            error.getMessage()));
        });
    }

}
