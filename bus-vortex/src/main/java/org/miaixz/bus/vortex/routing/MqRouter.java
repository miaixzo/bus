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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.routing.mq.MqExecutor;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Mono;

/**
 * A {@link Router} implementation for forwarding requests to a message queue (MQ).
 * <p>
 * This class acts as a simple coordinator. Its sole responsibility is to retrieve the necessary context and request
 * body, then delegate the actual execution to the {@link MqExecutor}.
 * <p>
 * The executor handles all protocol-specific logic including:
 * <ul>
 * <li>Sending messages to the message broker</li>
 * <li>Selecting response strategy (streaming vs buffering)
 * <li>Building the appropriate acknowledgment {@link ServerResponse}</li>
 * </ul>
 * <p>
 * Generic type parameters: {@code Router<ServerRequest, ServerResponse>}
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MqRouter implements Router<ServerRequest, ServerResponse> {

    /**
     * The executor responsible for sending messages to the message queue.
     */
    private final MqExecutor executor;

    /**
     * Constructs a new {@code MqRouter}.
     *
     * @param executor The executor that will perform the message sending.
     */
    public MqRouter(MqExecutor executor) {
        this.executor = executor;
    }

    /**
     * Routes a client request by sending its body as a message to a message queue.
     * <p>
     * This method retrieves the {@link Context} from the reactive stream and delegates the execution to the
     * {@link MqExecutor}. The executor is responsible for sending the message and building the acknowledgment response.
     *
     * @param input The ServerRequest object (strongly typed)
     * @return A {@code Mono<ServerResponse>} containing the acknowledgment response
     */
    @Override
    public Mono<ServerResponse> route(ServerRequest input) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);

            Logger.info("MQ Router: Routing request for topic: {}", context.getAssets().getMethod());

            // Read request body and delegate to executor
            return input.bodyToMono(String.class)
                    // .switchIfEmpty() handles cases where the body might be empty
                    .switchIfEmpty(Mono.just(Normal.EMPTY)).flatMap(body -> executor.execute(context, body)).doOnError(
                            error -> Logger.error(
                                    true,
                                    "MQ",
                                    "[MQ_ROUTER_ERROR] - Failed to forward request to topic: {} - {}",
                                    context.getAssets().getMethod(),
                                    error.getMessage()));
        });
    }

}
