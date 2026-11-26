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

import java.net.InetSocketAddress;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import org.miaixz.bus.auth.guard.CallerValidator;
import org.miaixz.bus.auth.guard.inbound.InboundDecision;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.routing.rest.RestExecutor;

import reactor.core.publisher.Mono;

/**
 * A {@link Router} implementation for forwarding requests to standard RESTful HTTP/HTTPS downstream services.
 * <p>
 * This class acts as a simple coordinator. Its sole responsibility is to retrieve the necessary context and asset
 * information for the current request and delegate the actual execution of the HTTP reverse proxy logic to the
 * {@link RestExecutor}.
 * <p>
 * Generic type parameters: {@code Router<ServerRequest, ServerResponse>}
 *
 * @author Kimi Liu
 */
public class RestRouter implements Router<ServerRequest, ServerResponse> {

    /**
     * The executor responsible for executing the downstream HTTP request.
     */
    private final RestExecutor executor;

    /**
     * Constructs a new {@code RestRouter}.
     * <p>
     * This is the preferred constructor for the new architecture.
     *
     * @param executor The executor that will perform the HTTP request execution.
     */
    public RestRouter(RestExecutor executor) {
        this.executor = executor;
    }

    /**
     * Routes the request to a downstream HTTP service.
     * <p>
     * This method retrieves the {@link Context} from the reactive stream and delegates the execution to the
     * {@link RestExecutor}.
     *
     * @param input The ServerRequest object (strongly typed)
     * @return A {@code Mono<ServerResponse>} containing the ServerResponse from the downstream service.
     */
    @Override
    public Mono<ServerResponse> route(ServerRequest input) {
        ServerRequest request = input;
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            Logger.debug(
                    true,
                    "Vortex",
                    "Request header snapshot: protocol=http, clientIp={}, path={}",
                    context.getX_request_ip(),
                    request.path());
            Logger.debug(
                    true,
                    "Vortex",
                    "Request header names: protocol=http, clientIp={}, headerNames={}",
                    context.getX_request_ip(),
                    request.headers().asHttpHeaders().headerNames());
            Logger.debug(
                    true,
                    "Vortex",
                    "Request parameter names: protocol=http, clientIp={}, parameterNames={}",
                    context.getX_request_ip(),
                    context.getParameters().keySet());
            return this.executor.execute(context, request);
        });
    }

    /**
     * Routes a callback accessible only to the named service through its loopback listener.
     *
     * @param request qualified gateway request
     * @param service expected direct service identity
     * @return downstream response if admitted
     */
    public Mono<ServerResponse> routeLocal(ServerRequest request, String service) {
        return Mono.defer(() -> {
            InboundDecision decision = request.exchange().getAttribute(InboundDecision.ATTRIBUTE);
            if (!CallerValidator.matchesDirect(decision == null ? null : decision.verified(), service)
                    || request.remoteAddress().map(InetSocketAddress::getAddress)
                            .filter(address -> address.isLoopbackAddress()).isEmpty()) {
                return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN));
            }
            return route(request);
        });
    }

}
