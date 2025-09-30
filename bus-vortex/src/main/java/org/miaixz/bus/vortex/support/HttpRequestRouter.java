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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.vortex.*;
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
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;
import reactor.util.annotation.NonNull;

/**
 * HTTP策略路由器，负责将请求路由到HTTP服务
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HttpRequestRouter implements Router {

    /**
     * 预定义的ExchangeStrategies实例，用于WebClient配置。
     * <p>
     * 该实例在类加载时初始化并缓存，避免重复创建，提高性能。 配置了最大内存大小限制，防止大请求导致内存溢出。
     * </p>
     */
    private static final ExchangeStrategies CACHED_EXCHANGE_STRATEGIES = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(Math.toIntExact(Normal.MEBI_128))).build();

    /**
     * 线程安全的 WebClient 缓存，按 baseUrl 存储已初始化的 WebClient 实例
     */
    private final Map<String, WebClient> clients = new ConcurrentHashMap<>();

    /**
     * 处理客户端请求，构建并转发到目标服务，返回响应
     *
     * @param request 客户端的 ServerRequest 对象
     * @param context 请求上下文，包含请求参数和配置信息
     * @param assets  配置资产，包含目标服务的配置信息
     * @return {@link Mono<ServerResponse>} 包含目标服务的响应
     */
    @NonNull
    @Override
    public Mono<ServerResponse> route(ServerRequest request, Context context, Assets assets) {
        // 1. 构建基础URL
        String baseUrl = buildBaseUrl(assets);

        // 2. 获取或创建WebClient
        WebClient webClient = clients.computeIfAbsent(
                baseUrl,
                client -> WebClient.builder().exchangeStrategies(CACHED_EXCHANGE_STRATEGIES).baseUrl(baseUrl).build());

        // 3. 构建目标URI
        String targetUri = buildTargetUri(assets, context);

        // 4. 配置请求
        WebClient.RequestBodySpec bodySpec = webClient.method(context.getHttpMethod()).uri(targetUri);

        // 5. 配置请求头
        bodySpec.headers(headers -> {
            headers.addAll(request.headers().asHttpHeaders());
            headers.remove(HttpHeaders.HOST);
            headers.clearContentHeaders();
        });

        // 6. 处理请求体（仅对非GET请求）
        if (!HttpMethod.GET.equals(context.getHttpMethod())) {
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
                        Format.debug(
                                request.exchange(),
                                "JSON_REQUEST_BODY",
                                "JSON request body size: " + jsonBody.length());
                        return bodySpec.contentType(MediaType.APPLICATION_JSON).bodyValue(jsonBody)
                                .httpRequest(clientHttpRequest -> {
                                    HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
                                    reactorRequest.responseTimeout(Duration.ofMillis(assets.getTimeout()));
                                }).retrieve().toEntity(DataBuffer.class).flatMap(this::processResponse);
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
        }).retrieve().toEntity(DataBuffer.class).flatMap(this::processResponse);
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
        if (HttpMethod.GET.equals(context.getHttpMethod())) {
            Map<String, String> params = context.getRequestMap();
            if (!params.isEmpty()) {
                MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>(params.size());
                params.forEach(multiValueMap::add);
                builder.queryParams(multiValueMap);
            }
        }
        return builder.build().toUriString();
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
        }).body(
                responseEntity.getBody() == null ? BodyInserters.empty()
                        : BodyInserters.fromDataBuffers(Flux.just(responseEntity.getBody())));
    }

}
