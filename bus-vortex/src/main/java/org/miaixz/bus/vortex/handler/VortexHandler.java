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
package org.miaixz.bus.vortex.handler;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.exception.BusinessException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.vortex.*;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.support.HttpRequestRouter;
import org.miaixz.bus.vortex.support.MqRequestRouter;
import org.miaixz.bus.vortex.support.McpRequestRouter;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

/**
 * 请求处理入口类，负责路由请求并异步调用多个拦截器逻辑。
 * <p>
 * 该类实现了请求处理的控制流程，包括请求验证、路由策略选择、拦截器执行和响应处理。 具体协议处理逻辑完全委托给各自的策略实现者（HTTP、MQ、MCP）。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VortexHandler {

    /**
     * 线程安全的策略映射，存储按协议名称索引的策略实现。
     * <p>
     * 该映射允许根据协议动态选择路由策略，支持HTTP、MQ和MCP协议。 使用ConcurrentHashMap保证线程安全。
     * </p>
     */
    private final Map<String, Router> strategies = new ConcurrentHashMap<>();

    /**
     * 默认策略，当未提供或未找到特定策略时使用。
     * <p>
     * 默认使用HTTP策略作为回退行为。
     * </p>
     */
    private final Router defaultRouter;

    /**
     * 按顺序排序的拦截器列表，用于按特定顺序处理请求。
     * <p>
     * 拦截器在请求处理的不同阶段（如前置处理、后置处理）被调用。 拦截器按照order属性排序，确保执行顺序。
     * </p>
     */
    private final List<Handler> handlers;

    /**
     * 构造函数，初始化策略映射和拦截器列表。
     *
     * @param handlers 异步拦截器实例列表，用于处理请求的各个阶段
     * @throws NullPointerException 如果handlers或默认策略为null
     */
    public VortexHandler(List<Handler> handlers) {
        strategies.put(Protocol.HTTP.name, new HttpRequestRouter());
        strategies.put(Protocol.MQ.name, new MqRequestRouter());
        strategies.put(Protocol.MCP.name, new McpRequestRouter());
        defaultRouter = strategies.get(Protocol.HTTP.name);
        Objects.requireNonNull(defaultRouter, "Default strategy cannot be null");
        // 如果handlers为空，使用默认AccessHandler
        this.handlers = handlers.isEmpty() ? List.of(new AccessHandler())
                : handlers.stream().sorted(Comparator.comparingInt(Handler::getOrder)).collect(Collectors.toList());
    }

    /**
     * 处理客户端请求，执行控制流程并返回响应。
     * <p>
     * 该方法是请求处理的入口点，负责整个请求处理流程的协调。 处理流程包括：
     * <ol>
     * <li>初始化和验证请求上下文</li>
     * <li>验证配置资产</li>
     * <li>选择路由策略</li>
     * <li>执行前置处理</li>
     * <li>委托给策略实现者处理请求</li>
     * <li>执行后置处理</li>
     * </ol>
     *
     * @param request 客户端的ServerRequest对象，包含请求的所有信息
     * @return {@link Mono<ServerResponse>} 包含目标服务的响应，以响应式方式返回
     * @throws RuntimeException 如果请求上下文或配置资产为null
     */
    @NonNull
    public Mono<ServerResponse> handle(ServerRequest request) {
        return Mono.defer(() -> {
            // 1. 初始化和验证请求上下文
            Context context = Context.get(request);
            if (context == null) {
                Format.error(null, "CONTEXT_NULL", "Request context is null for path: " + request.path());
                throw new BusinessException(ErrorCode._80010002);
            }
            ServerWebExchange exchange = request.exchange();
            Format.requestStart(exchange);

            // 2. 验证配置资产
            Assets assets = context.getAssets();
            if (assets == null) {
                Format.error(exchange, "ASSETS_NULL", "Assets is null in request context");
                throw new BusinessException(ErrorCode._100800);
            }

            // 3. 选择路由策略
            String strategyName = assets.getStrategy();
            if (strategyName == null || strategyName.trim().isEmpty()) {
                Format.warn(exchange, "STRATEGY_NULL", "Strategy is null or empty, using default strategy");
                strategyName = Protocol.HTTP.name;
            }
            Router router = strategies.getOrDefault(strategyName, defaultRouter);
            Format.info(exchange, "STRATEGY_SELECTED", "Using route strategy: " + router.getClass().getSimpleName());

            // 4. 执行前置处理
            return executePreHandle(exchange, router).flatMap(preHandleResult -> {
                if (!preHandleResult) {
                    throw new BusinessException(ErrorCode._100800);
                }

                // 5. 委托给策略实现者处理请求
                return router.route(request, context, assets)
                        .flatMap(response -> executePostHandlers(exchange, router, response)).doOnSuccess(response -> {
                            long duration = System.currentTimeMillis() - context.getStartTime();
                            Format.info(exchange, "REQUEST_DURATION",
                                    "Method: " + assets.getMethod() + ", Duration: " + duration + "ms");
                        }).onErrorResume(error -> {
                            Format.error(exchange, "REQUEST_ERROR", "Error processing request: " + error.getMessage());
                            return Mono.whenDelayError(handlers.stream()
                                    .map(handler -> handler.afterCompletion(exchange, router, null, null, error))
                                    .collect(Collectors.toList())).then(Mono.error(error));
                        });
            });
        }).doOnSuccess(response -> Format.requestEnd(request.exchange(), response.statusCode().value()));
    }

    /**
     * 执行所有拦截器的前置处理逻辑。
     * <p>
     * 该方法会并行调用所有拦截器的preHandle方法，并收集处理结果。 只有所有拦截器都返回true时，该方法才返回true，表示所有前置处理都通过。
     * </p>
     *
     * @param exchange 服务器Web交换对象，包含请求和响应的上下文信息
     * @param router   路由策略，用于确定如何路由请求
     * @return Mono<Boolean> 表示所有前置处理是否都通过，true表示全部通过，false表示有拦截器阻止了请求
     */
    private Mono<Boolean> executePreHandle(ServerWebExchange exchange, Router router) {
        return Mono.zip(
                handlers.stream().map(handler -> handler.preHandle(exchange, router, null))
                        .collect(Collectors.toList()),
                results -> results.length > 0 && java.util.Arrays.stream(results).allMatch(Boolean.class::cast));
    }

    /**
     * 执行所有拦截器的后置处理逻辑。
     * <p>
     * 该方法会并行调用所有拦截器的postHandle方法和afterCompletion方法。 postHandle方法在响应返回给客户端之前执行，afterCompletion方法在请求完全处理完成后执行。
     * </p>
     *
     * @param exchange 服务器Web交换对象，包含请求和响应的上下文信息
     * @param router   路由策略，用于确定如何路由请求
     * @param response 服务器响应，包含响应状态、头和体
     * @return Mono<ServerResponse> 处理后的响应，可能被拦截器修改
     */
    private Mono<ServerResponse> executePostHandlers(ServerWebExchange exchange, Router router,
            ServerResponse response) {
        return Mono
                .whenDelayError(handlers.stream().map(handler -> handler.postHandle(exchange, router, null, response))
                        .collect(Collectors.toList()))
                .thenReturn(response)
                .flatMap(res -> Mono.whenDelayError(
                        handlers.stream().map(handler -> handler.afterCompletion(exchange, router, null, res, null))
                                .collect(Collectors.toList()))
                        .thenReturn(res));
    }

}