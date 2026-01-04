/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
