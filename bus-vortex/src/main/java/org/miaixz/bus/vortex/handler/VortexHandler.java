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
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.vortex.*;
import org.miaixz.bus.vortex.support.HttpStrategyRouter;
import org.miaixz.bus.vortex.support.MQStrategyRouter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
 * 请求处理入口类，负责路由请求并异步调用多个拦截器逻辑
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class VortexHandler {

    /**
     * 线程安全的映射，存储按协议名称（如 "HTTP"、"MQ"）索引的策略实现。 该映射允许根据协议动态选择路由策略。
     */
    private final Map<String, Strategy> strategies = new ConcurrentHashMap<>();

    /**
     * 默认策略，当未提供或未找到特定策略时使用。 默认使用 HTTP 策略作为回退行为。
     */
    private final Strategy defaultStrategy;

    /**
     * 按顺序排序的拦截器列表，用于按特定顺序处理请求。 拦截器在请求处理的不同阶段（如前置处理、后置处理）被调用。
     */
    private final List<Handler> handlers;

    /**
     * 线程安全的 WebClient 缓存，按 baseUrl 存储已初始化的 WebClient 实例。 用于优化与目标服务的通信，避免重复创建 WebClient。
     */
    private final Map<String, WebClient> clients = new ConcurrentHashMap<>();

    /**
     * 构造函数，初始化策略映射和拦截器列表
     *
     * @param handlers 异步拦截器实例列表，用于处理请求的各个阶段
     * @throws NullPointerException 如果 handlers 或默认策略为 null
     */
    public VortexHandler(List<Handler> handlers) {
        strategies.put(Protocol.HTTP.name, new HttpStrategyRouter());
        strategies.put(Protocol.MQ.name, new MQStrategyRouter());
        defaultStrategy = strategies.get(Protocol.HTTP.name);
        Objects.requireNonNull(defaultStrategy, "Default strategy cannot be null");
        // 如果 handlers 为空，使用默认 AccessHandler
        this.handlers = handlers.isEmpty() ? List.of(new AccessHandler())
                : handlers.stream().sorted(Comparator.comparingInt(Handler::getOrder)).collect(Collectors.toList());
    }

    /**
     * 处理客户端请求，构建并转发到目标服务，返回响应
     *
     * @param request 客户端的 ServerRequest 对象
     * @return {@link Mono<ServerResponse>} 包含目标服务的响应
     */
    @NonNull
    public Mono<ServerResponse> handle(ServerRequest request) {
        return Mono.defer(() -> {
            try {
                // 获取请求上下文
                Context context = Context.get(request);
                if (context == null) {
                    // 无法直接获取exchange，使用VortexLogger记录基本信息
                    Format.error(null, "CONTEXT_NULL", "Request context is null for path: " + request.path());
                    // 不直接返回响应，而是抛出异常让FormatFilter处理
                    throw new RuntimeException("Request context is null");
                }

                // 从上下文中获取exchange
                ServerWebExchange exchange = request.exchange();

                // 记录请求开始
                Format.requestStart(exchange);

                // 获取配置资产
                Assets assets = context.getAssets();
                if (assets == null) {
                    Format.error(exchange, "ASSETS_NULL", "Assets is null in request context");
                    // 不直接返回响应，而是抛出异常让FormatFilter处理
                    throw new RuntimeException("Assets is null in request context");
                }

                // 获取策略名称，默认为 HTTP
                String strategyName = assets.getStrategy();
                if (strategyName == null || strategyName.trim().isEmpty()) {
                    Format.warn(exchange, "STRATEGY_NULL", "Strategy is null or empty, using default strategy");
                    strategyName = Protocol.HTTP.name;
                }

                // 初始化策略，确保有效最终
                final Strategy strategy = strategies.getOrDefault(strategyName, defaultStrategy);
                Format.info(exchange, "STRATEGY_SELECTED",
                        "Using route strategy: " + strategy.getClass().getSimpleName());

                // 异步调用所有 Handler 的 preHandle
                return Mono.zip(
                        handlers.stream().map(handler -> handler.preHandle(exchange, strategy, null))
                                .collect(Collectors.toList()),
                        results -> results.length > 0 && java.util.Arrays.stream(results).allMatch(Boolean.class::cast))
                        .flatMap(preHandleResult -> {
                            // 检查权限验证结果
                            if (!preHandleResult) {
                                Format.warn(exchange, "PREHANDLE_BLOCKED", "Request blocked by preHandle");
                                // 不直接返回响应，而是抛出异常让FormatFilter处理
                                throw new RuntimeException("Request blocked by handler");
                            }

                            // 获取请求参数
                            Map<String, String> params = context.getRequestMap();

                            // 构建目标服务的基础 URL（主机 + 端口 + 路径）
                            String port = StringKit.isEmpty(Normal.EMPTY + assets.getPort()) ? Normal.EMPTY
                                    : Symbol.COLON + assets.getPort();
                            String path = StringKit.isEmpty(assets.getPath()) ? Normal.EMPTY
                                    : Symbol.SLASH + assets.getPath();
                            String baseUrl = assets.getHost() + port + path;

                            // 从缓存获取或创建 WebClient 实例，设置最大内存限制
                            WebClient webClient = clients.computeIfAbsent(baseUrl,
                                    client -> WebClient.builder()
                                            .exchangeStrategies(ExchangeStrategies.builder()
                                                    .codecs(configurer -> configurer.defaultCodecs()
                                                            .maxInMemorySize(Config.MAX_INMEMORY_SIZE))
                                                    .build())
                                            .baseUrl(baseUrl).build());

                            // 构建目标 URI，处理查询参数
                            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                                    .path(assets.getUrl());
                            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
                            multiValueMap.setAll(params);
                            if (HttpMethod.GET.equals(assets.getHttpMethod())) {
                                builder.queryParams(multiValueMap);
                            }

                            // 配置请求，包括方法、URI 和请求头
                            WebClient.RequestBodySpec bodySpec = webClient.method(assets.getHttpMethod())
                                    .uri(builder.build().encode().toUri()).headers(headers -> {
                                        headers.addAll(request.headers().asHttpHeaders());
                                        headers.remove(HttpHeaders.HOST); // 移除 HOST 头以避免冲突
                                        headers.clearContentHeaders(); // 清除内容相关头，确保由 bodySpec 设置
                                    });

                            // 处理非 GET 请求的请求体
                            if (!HttpMethod.GET.equals(assets.getHttpMethod())) {
                                if (request.headers().contentType().isPresent()) {
                                    MediaType mediaType = request.headers().contentType().get();
                                    // 处理multipart/form-data类型的请求（如文件上传）
                                    if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
                                        MultiValueMap<String, Part> partMap = new LinkedMultiValueMap<>();
                                        partMap.setAll(context.getFilePartMap());
                                        BodyInserters.MultipartInserter multipartInserter = BodyInserters
                                                .fromMultipartData(partMap);
                                        params.forEach(multipartInserter::with);
                                        bodySpec.body(multipartInserter);
                                    }
                                    // 处理application/json类型的请求
                                    else if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                                        // 读取JSON请求体并直接转发
                                        return request.bodyToMono(String.class).flatMap(jsonBody -> {
                                            Format.debug(exchange, "JSON_REQUEST_BODY",
                                                    "JSON request body size: " + jsonBody.length());

                                            return bodySpec.contentType(MediaType.APPLICATION_JSON).bodyValue(jsonBody)
                                                    .httpRequest(clientHttpRequest -> {
                                                        HttpClientRequest reactorRequest = clientHttpRequest
                                                                .getNativeRequest();
                                                        reactorRequest.responseTimeout(
                                                                Duration.ofMillis(assets.getTimeout()));
                                                    }).retrieve().toEntity(DataBuffer.class)
                                                    .flatMap(responseEntity -> processResponse(exchange, context,
                                                            assets, responseEntity))
                                                    .doOnTerminate(() -> {
                                                        long duration = System.currentTimeMillis()
                                                                - context.getStartTime();
                                                        Format.info(exchange, "REQUEST_DURATION",
                                                                "Method: " + assets.getMethod() + ", Duration: "
                                                                        + duration + "ms");
                                                    });
                                        });
                                    }
                                    // 处理application/x-www-form-urlencoded类型的请求
                                    else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)) {
                                        // 处理普通表单数据
                                        bodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                                .bodyValue(multiValueMap);
                                    }
                                    // 处理其他类型的请求
                                    else {
                                        Format.warn(exchange, "UNSUPPORTED_MEDIA_TYPE",
                                                "Unsupported media type: " + mediaType);
                                        bodySpec.bodyValue(multiValueMap);
                                    }
                                } else {
                                    // 没有Content-Type头，默认按表单数据处理
                                    bodySpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                            .bodyValue(multiValueMap);
                                }
                            }

                            // 记录请求开始时间
                            long start_time = System.currentTimeMillis();

                            // 发送请求，设置超时并处理响应
                            return bodySpec.httpRequest(clientHttpRequest -> {
                                // 设置响应超时
                                HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
                                reactorRequest.responseTimeout(Duration.ofMillis(assets.getTimeout()));
                            }).retrieve().toEntity(DataBuffer.class).flatMap(
                                    responseEntity -> processResponse(exchange, context, assets, responseEntity))
                                    .doOnSuccess(response -> {
                                        Format.info(exchange, "REQUEST_PROCESSED",
                                                "Request processed successfully, execution time: "
                                                        + (System.currentTimeMillis() - context.getStartTime()) + "ms");
                                    }).flatMap(response ->
                            // 异步调用所有 Handler 的 postHandle
                            Mono.when(handlers.stream()
                                    .map(handler -> handler.postHandle(exchange, strategy, null, response))
                                    .collect(Collectors.toList())).thenReturn(response)).flatMap(response ->
                            // 异步调用所有 Handler 的 afterCompletion（成功）
                            Mono.when(handlers.stream()
                                    .map(handler -> handler.afterCompletion(exchange, strategy, null, response, null))
                                    .collect(Collectors.toList())).thenReturn(response))
                                    // 记录请求耗时日志
                                    .doOnTerminate(() -> {
                                        Format.info(exchange, "REQUEST_DURATION", "Method: " + assets.getMethod()
                                                + ", Duration: " + (System.currentTimeMillis() - start_time) + "ms");
                                    });
                        }).onErrorResume(error -> {
                            // 记录错误日志
                            Format.error(exchange, "REQUEST_ERROR", "Error processing request: " + error.getMessage());

                            // 异步错误，调用所有 Handler 的 afterCompletion
                            return Mono
                                    .when(handlers.stream()
                                            .map(handler -> handler.afterCompletion(exchange, strategy, null, null,
                                                    error))
                                            .collect(Collectors.toList()))
                                    // 不直接返回响应，而是抛出异常让FormatFilter处理
                                    .then(Mono.error(error));
                        });
            } catch (Exception e) {
                // 捕获同步异常，调用所有 Handler 的 afterCompletion
                // 在异常情况下，使用VortexLogger记录基本信息
                Format.error(null, "REQUEST_EXCEPTION",
                        "Exception processing request for path: " + request.path() + ", Error: " + e.getMessage());

                return Mono
                        .when(handlers.stream()
                                .map(handler -> handler.afterCompletion(request.exchange(), null, null, null, e))
                                .collect(Collectors.toList()))
                        // 不直接返回响应，而是抛出异常让FormatFilter处理
                        .then(Mono.error(e));
            }
        }).doOnSuccess(response -> {
            // 获取请求上下文和exchange
            Context context = Context.get(request);
            if (context != null) {
                // 记录请求结束
                Format.requestEnd(request.exchange(), response.statusCode().value());
            } else {
                Format.info(null, "REQUEST_END_NO_CONTEXT",
                        "Request completed without context for path: " + request.path());
            }
        });
    }

    /**
     * 处理响应数据
     *
     * @param exchange       ServerWebExchange对象
     * @param context        请求上下文
     * @param assets         资产信息
     * @param responseEntity 响应实体
     * @return Mono<ServerResponse> 响应
     */
    private Mono<ServerResponse> processResponse(ServerWebExchange exchange, Context context, Assets assets,
            org.springframework.http.ResponseEntity<DataBuffer> responseEntity) {
        return ServerResponse.ok().headers(headers -> {
            // 复制响应头，移除 CONTENT_LENGTH 以避免冲突
            headers.addAll(responseEntity.getHeaders());
            headers.remove(HttpHeaders.CONTENT_LENGTH);
        }).body(null == responseEntity.getBody() ? BodyInserters.empty()
                : BodyInserters.fromDataBuffers(Flux.just(responseEntity.getBody())));
    }

}