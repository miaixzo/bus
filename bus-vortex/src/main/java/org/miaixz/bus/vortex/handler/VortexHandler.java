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

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.vortex.*;
import org.miaixz.bus.vortex.support.HttpStrategyRouter;
import org.miaixz.bus.vortex.support.MQStrategyRouter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;
import reactor.util.annotation.NonNull;

/**
 * 请求处理入口类，负责路由请求并异步调用多个拦截器逻辑。
 * <p>
 * 该类实现了请求处理的完整流程，包括请求验证、路由策略选择、请求转发和响应处理。 它支持多种协议（HTTP、MQ）和多种内容类型（JSON、表单、多部分）的请求处理。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VortexHandler {

    /**
     * 预定义的ExchangeStrategies实例，用于WebClient配置。
     * <p>
     * 该实例在类加载时初始化并缓存，避免重复创建，提高性能。 配置了最大内存大小限制，防止大请求导致内存溢出。
     * </p>
     */
    private static final ExchangeStrategies CACHED_EXCHANGE_STRATEGIES = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(Config.MAX_INMEMORY_SIZE)).build();

    /**
     * 线程安全的策略映射，存储按协议名称索引的策略实现。
     * <p>
     * 该映射允许根据协议动态选择路由策略，支持HTTP和MQ协议。 使用ConcurrentHashMap保证线程安全。
     * </p>
     */
    private final Map<String, Strategy> strategies = new ConcurrentHashMap<>();

    /**
     * 线程安全的WebClient缓存，按baseUrl存储已初始化的WebClient实例。
     * <p>
     * 用于优化与目标服务的通信，避免重复创建WebClient实例。 使用ConcurrentHashMap保证线程安全。
     * </p>
     */
    private final Map<String, WebClient> clients = new ConcurrentHashMap<>();

    /**
     * 默认策略，当未提供或未找到特定策略时使用。
     * <p>
     * 默认使用HTTP策略作为回退行为。
     * </p>
     */
    private final Strategy defaultStrategy;

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
        strategies.put(Protocol.HTTP.name, new HttpStrategyRouter());
        strategies.put(Protocol.MQ.name, new MQStrategyRouter());
        defaultStrategy = strategies.get(Protocol.HTTP.name);
        Objects.requireNonNull(defaultStrategy, "Default strategy cannot be null");
        // 如果handlers为空，使用默认AccessHandler
        this.handlers = handlers.isEmpty() ? List.of(new AccessHandler())
                : handlers.stream().sorted(Comparator.comparingInt(Handler::getOrder)).collect(Collectors.toList());
    }

    /**
     * 处理客户端请求，构建并转发到目标服务，返回响应。
     * <p>
     * 该方法是请求处理的入口点，负责整个请求处理流程的协调。 处理流程包括：
     * <ol>
     * <li>初始化和验证请求上下文</li>
     * <li>验证配置资产</li>
     * <li>选择路由策略</li>
     * <li>执行前置处理</li>
     * <li>构建和发送请求</li>
     * <li>处理响应</li>
     * <li>执行后置处理</li>
     * </ol>
     * </p>
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
                throw new RuntimeException("Request context is null");
            }
            ServerWebExchange exchange = request.exchange();
            Format.requestStart(exchange);

            // 2. 验证配置资产
            Assets assets = context.getAssets();
            if (assets == null) {
                Format.error(exchange, "ASSETS_NULL", "Assets is null in request context");
                throw new RuntimeException("Assets is null in request context");
            }

            // 3. 选择路由策略
            String strategyName = assets.getStrategy();
            if (strategyName == null || strategyName.trim().isEmpty()) {
                Format.warn(exchange, "STRATEGY_NULL", "Strategy is null or empty, using default strategy");
                strategyName = Protocol.HTTP.name;
            }
            Strategy strategy = strategies.getOrDefault(strategyName, defaultStrategy);
            Format.info(exchange, "STRATEGY_SELECTED", "Using route strategy: " + strategy.getClass().getSimpleName());

            // 4. 执行前置处理
            return executePreHandle(exchange, strategy).flatMap(preHandleResult -> {
                if (!preHandleResult) {
                    throw new RuntimeException("Request blocked by handler");
                }
                // 5. 构建和发送请求
                return buildAndSendRequest(request, context, assets).flatMap(this::processResponse)
                        .flatMap(response -> executePostHandlers(exchange, strategy, response))
                        .doOnSuccess(response -> {
                            long duration = System.currentTimeMillis() - context.getStartTime();
                            Format.info(exchange, "REQUEST_DURATION",
                                    "Method: " + assets.getMethod() + ", Duration: " + duration + "ms");
                        }).onErrorResume(error -> {
                            Format.error(exchange, "REQUEST_ERROR", "Error processing request: " + error.getMessage());
                            return Mono.whenDelayError(handlers.stream()
                                    .map(handler -> handler.afterCompletion(exchange, strategy, null, null, error))
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
     * @param strategy 路由策略，用于确定如何路由请求
     * @return Mono<Boolean> 表示所有前置处理是否都通过，true表示全部通过，false表示有拦截器阻止了请求
     */
    private Mono<Boolean> executePreHandle(ServerWebExchange exchange, Strategy strategy) {
        return Mono.zip(
                handlers.stream().map(handler -> handler.preHandle(exchange, strategy, null))
                        .collect(Collectors.toList()),
                results -> results.length > 0 && java.util.Arrays.stream(results).allMatch(Boolean.class::cast));
    }

    /**
     * 构建和发送请求到目标服务。
     * <p>
     * 该方法负责构建目标URL、配置请求头、处理请求体，并发送请求到目标服务。 支持多种内容类型的请求体处理，包括JSON、表单和多部分数据。
     * </p>
     *
     * @param request 客户端的ServerRequest对象，包含原始请求信息
     * @param context 请求上下文，包含请求参数和配置信息
     * @param assets  配置资产，包含目标服务的配置信息
     * @return Mono<ResponseEntity<DataBuffer>> 目标服务的响应实体，包含响应头和响应体
     */
    private Mono<ResponseEntity<DataBuffer>> buildAndSendRequest(ServerRequest request, Context context,
            Assets assets) {
        // 1. 构建基础URL
        String baseUrl = buildBaseUrl(assets);

        // 2. 获取或创建WebClient
        WebClient webClient = clients.computeIfAbsent(baseUrl,
                client -> WebClient.builder().exchangeStrategies(CACHED_EXCHANGE_STRATEGIES).baseUrl(baseUrl).build());

        // 3. 构建目标URI
        String targetUri = buildTargetUri(assets, context);

        // 4. 配置请求
        WebClient.RequestBodySpec bodySpec = webClient.method(assets.getHttpMethod()).uri(targetUri);

        // 5. 配置请求头
        bodySpec.headers(headers -> {
            headers.addAll(request.headers().asHttpHeaders());
            headers.remove(HttpHeaders.HOST);
            headers.clearContentHeaders();
        });

        // 6. 处理请求体（仅对非GET请求）
        if (!HttpMethod.GET.equals(assets.getHttpMethod())) {
            MediaType mediaType = request.headers().contentType().orElse(null);
            if (mediaType != null) {
                if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
                    // 处理多部分请求体
                    Map<String, Part> fileParts = context.getFilePartMap();
                    Map<String, String> params = context.getRequestMap();
                    if (!fileParts.isEmpty() || !params.isEmpty()) {
                        MultiValueMap<String, Part> partMap = new LinkedMultiValueMap<>(fileParts.size());
                        partMap.setAll(fileParts);
                        BodyInserters.MultipartInserter multipartInserter = BodyInserters.fromMultipartData(partMap);
                        if (!params.isEmpty()) {
                            params.forEach(multipartInserter::with);
                        }
                        bodySpec.body(multipartInserter);
                    }
                } else if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                    // 处理JSON请求体
                    return request.bodyToMono(String.class).defaultIfEmpty(Normal.EMPTY).flatMap(jsonBody -> {
                        Format.debug(request.exchange(), "JSON_REQUEST_BODY",
                                "JSON request body size: " + jsonBody.length());
                        return bodySpec.contentType(MediaType.APPLICATION_JSON).bodyValue(jsonBody)
                                .httpRequest(clientHttpRequest -> {
                                    HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
                                    reactorRequest.responseTimeout(Duration.ofMillis(assets.getTimeout()));
                                }).retrieve().toEntity(DataBuffer.class);
                    });
                } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)) {
                    // 处理表单请求体
                    handleFormRequestBody(bodySpec, context);
                } else {
                    Format.warn(request.exchange(), "UNSUPPORTED_MEDIA_TYPE", "Unsupported media type: " + mediaType);
                    handleFormRequestBody(bodySpec, context);
                }
            } else {
                // 没有Content-Type头，默认按表单数据处理
                handleFormRequestBody(bodySpec, context);
            }
        }

        // 7. 发送请求
        return bodySpec.httpRequest(clientHttpRequest -> {
            HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
            reactorRequest.responseTimeout(Duration.ofMillis(assets.getTimeout()));
        }).retrieve().toEntity(DataBuffer.class);
    }

    /**
     * 处理表单请求体。
     * <p>
     * 该方法将请求上下文中的参数转换为MultiValueMap，并设置为请求体。 如果参数为空，则不设置请求体。
     * </p>
     *
     * @param bodySpec 请求体规范，用于配置请求体
     * @param context  请求上下文，包含请求参数
     */
    private void handleFormRequestBody(WebClient.RequestBodySpec bodySpec, Context context) {
        Map<String, String> params = context.getRequestMap();
        if (!params.isEmpty()) {
            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>(params.size());
            params.forEach(multiValueMap::add);
            bodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED).bodyValue(multiValueMap);
        }
    }

    /**
     * 构建目标服务的基础URL。
     * <p>
     * 该方法根据配置资产中的主机、端口和路径信息构建基础URL。 端口和路径是可选的，如果不存在则不包含在URL中。
     * </p>
     *
     * @param assets 配置资产，包含目标服务的主机、端口和路径信息
     * @return 构建的基础URL字符串
     */
    private String buildBaseUrl(Assets assets) {
        StringBuilder baseUrlBuilder = new StringBuilder(assets.getHost());
        if (assets.getPort() > 0) {
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

    /**
     * 构建目标URI。
     * <p>
     * 该方法根据配置资产中的URL和请求上下文中的参数构建目标URI。 对于GET请求，会将参数添加到URI的查询字符串中。
     * </p>
     *
     * @param assets  配置资产，包含目标服务的URL信息
     * @param context 请求上下文，包含请求参数
     * @return 构建的目标URI字符串
     */
    private String buildTargetUri(Assets assets, Context context) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(assets.getUrl());
        if (HttpMethod.GET.equals(assets.getHttpMethod())) {
            Map<String, String> params = context.getRequestMap();
            if (!params.isEmpty()) {
                MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>(params.size());
                params.forEach(multiValueMap::add);
                builder.queryParams(multiValueMap);
            }
        }
        return builder.build().encode().toUriString();
    }

    /**
     * 执行所有拦截器的后置处理逻辑。
     * <p>
     * 该方法会并行调用所有拦截器的postHandle方法和afterCompletion方法。 postHandle方法在响应返回给客户端之前执行，afterCompletion方法在请求完全处理完成后执行。
     * </p>
     *
     * @param exchange 服务器Web交换对象，包含请求和响应的上下文信息
     * @param strategy 路由策略，用于确定如何路由请求
     * @param response 服务器响应，包含响应状态、头和体
     * @return Mono<ServerResponse> 处理后的响应，可能被拦截器修改
     */
    private Mono<ServerResponse> executePostHandlers(ServerWebExchange exchange, Strategy strategy,
            ServerResponse response) {
        return Mono
                .whenDelayError(handlers.stream().map(handler -> handler.postHandle(exchange, strategy, null, response))
                        .collect(Collectors.toList()))
                .thenReturn(response)
                .flatMap(res -> Mono.whenDelayError(
                        handlers.stream().map(handler -> handler.afterCompletion(exchange, strategy, null, res, null))
                                .collect(Collectors.toList()))
                        .thenReturn(res));
    }

    /**
     * 处理响应数据。
     * <p>
     * 该方法将目标服务返回的响应实体转换为ServerResponse对象。 会复制响应头，但移除CONTENT_LENGTH头以避免冲突。 如果响应体为空，则返回空响应体。
     * </p>
     *
     * @param responseEntity 响应实体，包含响应头和响应体
     * @return Mono<ServerResponse> 处理后的响应
     */
    private Mono<ServerResponse> processResponse(ResponseEntity<DataBuffer> responseEntity) {
        return ServerResponse.ok().headers(headers -> {
            headers.addAll(responseEntity.getHeaders());
            headers.remove(HttpHeaders.CONTENT_LENGTH);
        }).body(responseEntity.getBody() == null ? BodyInserters.empty()
                : BodyInserters.fromDataBuffers(Flux.just(responseEntity.getBody())));
    }

}