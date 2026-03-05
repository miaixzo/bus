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
package org.miaixz.bus.vortex;

import org.miaixz.bus.vortex.handler.VortexHandler;
import reactor.core.publisher.Mono;

/**
 * Defines the contract for routing a request to a specific downstream protocol (e.g., HTTP, MCP, MQ).
 * <p>
 * A {@code Router} is the final step in the request processing pipeline, invoked by the {@link VortexHandler}. Its sole
 * responsibility is to take the fully processed request and translate it into an interaction with a specific backend
 * protocol, as defined by the matched {@link Assets}.
 * <p>
 * The interface uses generics to provide type safety for different protocol implementations:
 * <ul>
 * <li>{@code I} - The input type expected by the router (typically ServerRequest, but can be String or other
 * types)</li>
 * <li>{@code O} - The output type produced by the router (typically ServerResponse, String, or Object)</li>
 * </ul>
 *
 * @param <I> The input type expected by this router
 * @param <O> The output type produced by this router
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Router<I, O> {

    /**
     * Routes the request to the target service.
     * <p>
     * Implementations of this method must retrieve the {@link Context} and {@link Assets} from the reactive stream to
     * get the necessary routing information. A typical implementation will look like this:
     *
     * <pre>{@code
     * return Mono.deferContextual(contextView -> {
     *     final Context context = contextView.get(Context.class);
     *     final Assets assets = context.getAssets();
     *     // ... use assets and context to perform routing logic ...
     * });
     * }</pre>
     * <p>
     * Generic type parameters provide compile-time type safety:
     * <ul>
     * <li>REST/HTTP: {@code Router<ServerRequest, ServerResponse>}</li>
     * <li>WebSocket: {@code Router<ServerRequest, ServerResponse>}</li>
     * <li>gRPC: {@code Router<ServerRequest, String>}</li>
     * <li>Message Queue: {@code Router<ServerRequest, String>}</li>
     * <li>MCP: {@code Router<ServerRequest, ServerResponse>}</li>
     * </ul>
     *
     * @param input An input object of type {@code I} (typically ServerRequest, but can be String payload or other
     *              types).
     * @return A {@code Mono<O>} representing the asynchronous response from the downstream service.
     */
    Mono<O> route(I input);

}
