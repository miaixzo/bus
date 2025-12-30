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

import reactor.core.publisher.Mono;

/**
 * Base interface for all Vortex service executors.
 * <p>
 * This interface defines the core contract for all service implementations in the Vortex gateway, providing a unified
 * API for request execution and lifecycle control. Implementations of this interface handle different protocols and
 * communication patterns such as REST, WebSocket, gRPC, Message Queue, and MCP.
 * <p>
 * The interface uses generics to provide type safety for different protocol implementations:
 * <ul>
 * <li>{@code I} - The input type expected by the executor (e.g., ServerRequest, String, WebSocketSession)</li>
 * <li>{@code O} - The output type produced by the executor (e.g., ServerResponse, String, byte[])</li>
 * </ul>
 * <p>
 * The interface follows a consistent design philosophy with other Vortex components:
 * <ul>
 * <li>Functional: Focuses on what the executor does (execute requests, build URLs)</li>
 * <li>Extensible: Allows protocol-specific implementations while maintaining common capabilities</li>
 * <li>Lifecycle-aware: Supports initialization and destruction callbacks</li>
 * <li>Type-safe: Generic types provide compile-time type checking</li>
 * </ul>
 * <p>
 * <b>Implementation Architecture:</b><br>
 * Most implementations should extend {@link org.miaixz.bus.vortex.support.Coordinator} rather than implementing this
 * interface directly. The {@code Coordinator} abstract class provides default implementations for common functionality
 * such as URL building, JSON encoding fixes, and logging utilities.
 *
 * @param <I> The input type expected by this executor
 * @param <O> The output type produced by this executor
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Executor<I, O> {

    /**
     * Builds the URL or connection target for the downstream service.
     * <p>
     * This method constructs the connection target by combining the host, port, and context path from the asset
     * configuration. The resulting target follows the format: {@code host:port/path} (with proper handling of omitted
     * components).
     * <p>
     * This method always returns a {@link Mono<String>} regardless of the executor's output type {@code O}, as it
     * constructs a URL string for the downstream service endpoint.
     *
     * @param context The request context containing the assets configuration.
     * @return A {@link Mono<String>} emitting the constructed URL string (e.g., "http://api.example.com:8080/v1").
     */
    Mono<String> build(Context context);

    /**
     * Executes a request using the provided request context and input object.
     * <p>
     * This is the core method of the executor interface. It processes the incoming request according to the asset's
     * configuration (protocol, endpoint, timeout, retry policy, etc.) and returns a response. The execution is
     * asynchronous and returns a {@link Mono} for reactive composition.
     * <p>
     * Generic type parameters provide compile-time type safety:
     * <ul>
     * <li>REST/HTTP: {@code Executor<ServerRequest, ServerResponse>}</li>
     * <li>WebSocket: {@code Executor<WebSocketSession, Void>}</li>
     * <li>gRPC: {@code Executor<String, String>}</li>
     * <li>Message Queue: {@code Executor<String, Void>}</li>
     * <li>MCP: {@code Executor<Void, Object>}</li>
     * </ul>
     *
     * @param context The request context containing parameters, metadata, assets configuration, and state information.
     * @param input   An input object of type {@code I} (e.g., ServerRequest, String payload, WebSocketSession, etc.).
     * @return A {@link Mono<O>} that emits the response object, or empty if there's no return value.
     */
    Mono<O> execute(Context context, I input);

    /**
     * Destroys the executor and releases all held resources.
     * <p>
     * This method is called during application shutdown or when the executor is no longer needed. Implementations
     * should:
     * <ul>
     * <li>Close all open connections and streams</li>
     * <li>Release thread pools and executors</li>
     * <li>Clear caches and buffers</li>
     * <li>Unregister from any registries or notification systems</li>
     * </ul>
     * <p>
     * This method should be idempotent - calling it multiple times should have no adverse effects. After calling this
     * method, the executor should not be used for new executions.
     * <p>
     * Implementations typically use {@link jakarta.annotation.PreDestroy} or implement
     * {@link org.springframework.beans.factory.DisposableBean} to hook into Spring's lifecycle.
     * <p>
     * <b>Default Implementation:</b> This method has a default empty implementation. Executors that do not hold
     * resources needing cleanup do not need to override this method.
     * <p>
     * The return type uses generic {@code O} to match the executor's output type, allowing implementations to return
     * cleanup status, final results, or other completion information if needed.
     *
     * @return A {@link Mono<O>} that completes when destruction is finished, potentially emitting a final result.
     */
    default Mono<O> destroy() {
        return Mono.empty();
    }

}
