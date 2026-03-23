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
package org.miaixz.bus.vortex.support;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Executor;

import reactor.core.publisher.Mono;

/**
 * Coordinator base class for all Vortex service executors.
 * <p>
 * This class coordinates common functionality shared across different service types such as REST, WebSocket, gRPC, MQ,
 * and MCP. It implements the {@link Executor} interface and provides default implementations for all methods,
 * encapsulating:
 * <ul>
 * <li>Common URL building utilities for constructing service endpoints</li>
 * <li>GraalVM Native Image compatibility fixes for JSON encoding</li>
 * <li>Shared logging patterns and utilities</li>
 * <li>Resource management lifecycle hooks</li>
 * </ul>
 * <p>
 * As a coordinator, this class centralizes cross-cutting concerns and provides a unified framework for all service
 * executors. Services extending this class inherit these coordinated utilities while maintaining their specific
 * execution logic. This abstract class follows the Template Method pattern, allowing subclasses to override specific
 * behaviors while inheriting the common execution framework.
 * <p>
 * Generic type parameters are passed through from the {@link Executor} interface:
 * <ul>
 * <li>{@code I} - The input type expected by implementations extending this coordinator</li>
 * <li>{@code O} - The output type produced by implementations extending this coordinator</li>
 * </ul>
 *
 * @param <I> The input type expected by this coordinator
 * @param <O> The output type produced by this coordinator
 * @author Kimi Liu
 * @since Java 17+
 * @see Executor
 * @see org.miaixz.bus.vortex.support.rest.RestExecutor
 * @see org.miaixz.bus.vortex.support.ws.WsExecutor
 * @see org.miaixz.bus.vortex.support.grpc.GrpcExecutor
 * @see org.miaixz.bus.vortex.support.mq.MqExecutor
 * @see org.miaixz.bus.vortex.support.mcp.McpExecutor
 */
public abstract class Coordinator<I, O> implements Executor<I, O> {

    /**
     * Builds the URL or connection target for the downstream service.
     * <p>
     * Description inherited from parent interface.
     * <p>
     * This implementation builds a URL string from the assets configuration. Subclasses that need different build
     * behavior should override this method.
     *
     * @param context The request context containing the assets configuration.
     * @return A {@link Mono<String>} emitting the constructed URL string (e.g., "http://api.example.com:8080/v1").
     */
    @Override
    public Mono<String> build(Context context) {
        return Mono.fromCallable(() -> buildBaseUrl(context));
    }

    /**
     * Builds the base URL for the target service synchronously.
     * <p>
     * This is a synchronous version of {@link #build(Context)} that can be used when blocking is acceptable (e.g., the
     * operation does not involve I/O and completes quickly).
     * <p>
     * This implementation builds a URL string from the assets configuration. Subclasses that need different build
     * behavior should override this method.
     *
     * @param context The request context
     * @return The base URL string
     */
    protected String buildBaseUrl(Context context) {
        Assets assets = context.getAssets();
        StringBuilder baseUrlBuilder = new StringBuilder(assets.getHost());
        if (assets.getPort() != null && assets.getPort() > 0) {
            baseUrlBuilder.append(Symbol.COLON).append(assets.getPort());
        }
        if (assets.getPath() != null && !assets.getPath().isEmpty()) {
            if (!assets.getPath().startsWith(Symbol.SLASH)) {
                baseUrlBuilder.append(Symbol.SLASH);
            }
            baseUrlBuilder.append(assets.getPath());
        }
        return baseUrlBuilder.toString();
    }

    protected String fixJsonEncoding(String json) {
        return json;
    }

}
