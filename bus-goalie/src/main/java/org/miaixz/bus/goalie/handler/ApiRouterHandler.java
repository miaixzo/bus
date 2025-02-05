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
 * router handler
 *
 * @author Justubborn
 * @since Java 17+
 */
public class ApiRouterHandler {

    private final Map<String, WebClient> clients = new ConcurrentHashMap<>();

    @NonNull
    public Mono<ServerResponse> handle(ServerRequest request) {
        Context context = Context.get(request);
        Assets assets = context.getAssets();
        Map<String, String> params = context.getRequestMap();

        String port = StringKit.isEmpty(Normal.EMPTY + assets.getPort()) ? Normal.EMPTY
                : Symbol.COLON + assets.getPort();
        String path = StringKit.isEmpty(assets.getPath()) ? Normal.EMPTY : Symbol.SLASH + assets.getPath();
        String baseUrl = assets.getHost() + port + path;

        WebClient webClient = clients.computeIfAbsent(baseUrl, client -> WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(Config.MAX_INMEMORY_SIZE))
                        .build())
                .baseUrl(baseUrl).build());
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl).path(assets.getUrl());
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.setAll(params);
        if (HttpMethod.GET.equals(assets.getHttpMethod())) {
            builder.queryParams(multiValueMap);
        }
        WebClient.RequestBodySpec bodySpec = webClient.method(assets.getHttpMethod())
                .uri(builder.build().encode().toUri()).headers(headers -> {
                    headers.addAll(request.headers().asHttpHeaders());
                    headers.remove(HttpHeaders.HOST);
                    headers.clearContentHeaders();
                });
        if (!HttpMethod.GET.equals(assets.getHttpMethod())) {
            if (request.headers().contentType().isPresent()) {
                MediaType mediaType = request.headers().contentType().get();
                // 文件
                if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
                    MultiValueMap<String, Part> partMap = new LinkedMultiValueMap<>();
                    partMap.setAll(context.getFilePartMap());
                    BodyInserters.MultipartInserter multipartInserter = BodyInserters.fromMultipartData(partMap);
                    params.forEach(multipartInserter::with);
                    bodySpec.body(multipartInserter);
                } else {
                    bodySpec.bodyValue(multiValueMap);
                }
            }
        }
        long start_time = System.currentTimeMillis();
        return bodySpec.httpRequest(clientHttpRequest -> {
            // 设置超时
            HttpClientRequest reactorRequest = clientHttpRequest.getNativeRequest();
            reactorRequest.responseTimeout(Duration.ofMillis(assets.getTimeout()));
        }).retrieve().toEntity(DataBuffer.class).flatMap(responseEntity -> ServerResponse.ok().headers(headers -> {
            headers.addAll(responseEntity.getHeaders());
            headers.remove(HttpHeaders.CONTENT_LENGTH);
        }).body(null == responseEntity.getBody() ? BodyInserters.empty()
                : BodyInserters.fromDataBuffers(Flux.just(responseEntity.getBody()))))
                .doOnTerminate(() -> Logger.info("method:{} 请求耗时:{} ms", context.getAssets().getMethod(),
                        System.currentTimeMillis() - start_time));
    }

}
