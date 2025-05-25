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
package org.miaixz.bus.goalie.handler;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.goalie.Assets;
import org.miaixz.bus.goalie.Config;
import org.miaixz.bus.goalie.Context;
import org.miaixz.bus.logger.Logger;
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
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;
import reactor.util.annotation.NonNull;

/**
 * API 路由处理器，负责处理和转发客户端请求到目标服务
 *
 * @author Justubborn
 * @since Java 17+
 */
public class ApiRouterHandler {

    /**
     * 线程安全的 WebClient 缓存，按 baseUrl 存储已初始化的 WebClient 实例
     */
    private final Map<String, WebClient> clients = new ConcurrentHashMap<>();

    /**
     * 处理客户端请求，构建并转发到目标服务，返回响应
     *
     * @param request 客户端的 ServerRequest 对象
     * @return {@link Mono<ServerResponse>} 包含目标服务的响应
     */
    @NonNull
    public Mono<ServerResponse> handle(ServerRequest request) {
        // 获取请求上下文和资产信息
        Context context = Context.get(request);
        Assets assets = context.getAssets();
        Map<String, String> params = context.getRequestMap();

        // 构建目标服务的基础 URL（主机 + 端口 + 路径）
        String port = StringKit.isEmpty(Normal.EMPTY + assets.getPort()) ? Normal.EMPTY
                : Symbol.COLON + assets.getPort();
        String path = StringKit.isEmpty(assets.getPath()) ? Normal.EMPTY : Symbol.SLASH + assets.getPath();
        String baseUrl = assets.getHost() + port + path;

        // 从缓存获取或创建 WebClient 实例，设置最大内存限制
        WebClient webClient = clients.computeIfAbsent(baseUrl, client -> WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(Config.MAX_INMEMORY_SIZE))
                        .build())
                .baseUrl(baseUrl).build());

        // 构建目标 URI，处理查询参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl).path(assets.getUrl());
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
                    BodyInserters.MultipartInserter multipartInserter = BodyInserters.fromMultipartData(partMap);
                    params.forEach(multipartInserter::with);
                    bodySpec.body(multipartInserter);
                } else {
                    // 处理普通表单数据
                    bodySpec.bodyValue(multiValueMap);
                }
            }
        }

        // 记录请求开始时间
        long start_time = System.currentTimeMillis();

        // 发送请求，设置超时并处理响应
        return bodySpec.httpRequest(clientHttpRequest -> {
            // 设置响应超时
            HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
            reactorRequest.responseTimeout(Duration.ofMillis(assets.getTimeout()));
        }).retrieve().toEntity(DataBuffer.class).flatMap(responseEntity -> ServerResponse.ok().headers(headers -> {
            // 复制响应头，移除 CONTENT_LENGTH 以避免冲突
            headers.addAll(responseEntity.getHeaders());
            headers.remove(HttpHeaders.CONTENT_LENGTH);
        }).body(null == responseEntity.getBody() ? BodyInserters.empty()
                : BodyInserters.fromDataBuffers(Flux.just(responseEntity.getBody()))))
                // 记录请求耗时日志
                .doOnTerminate(() -> Logger.info("method:{} 请求耗时:{} ms", context.getAssets().getMethod(),
                        System.currentTimeMillis() - start_time));
    }

}