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
package org.miaixz.bus.vortex.support;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.vortex.Assets;
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
     * {@inheritDoc}
     * <p>
     * This implementation builds a URL string from the assets configuration. Subclasses that need different build
     * behavior should override this method.
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
