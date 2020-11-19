/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.goalie.reactor;

import org.aoju.bus.base.consts.ErrorCode;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.BusinessException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * router handler
 *
 * @author Justubborn
 * @since 2020/10/27
 */
public class RouterHandler {

    private final Map<String, WebClient> clients = new ConcurrentHashMap<>();


    @NonNull
    public Mono<ServerResponse> handle(ServerRequest request) {

        ExchangeContext context = ExchangeContext.get(request);

        Asset asset = context.getAsset();

        Map<String, String> params = context.getRequestMap();

        String baseUrl = asset.getHost() + Symbol.C_COLON + asset.getPort();

        WebClient webClient = clients.computeIfAbsent(baseUrl, client -> WebClient.create(baseUrl));
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl).path(asset.getUrl());
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.setAll(params);
        if (HttpMethod.GET.equals(asset.getHttpMethod())) {

            builder.queryParams(multiValueMap);
        }
        WebClient.RequestBodySpec bodySpec = webClient
                .method(asset.getHttpMethod())
                .uri(builder.build().toUri())
                .headers((headers) -> request.headers());
        if (!HttpMethod.GET.equals(asset.getHttpMethod())) {
            bodySpec.bodyValue(multiValueMap);
        }
        return bodySpec
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        Flux<DataBuffer> flux = clientResponse.body(BodyExtractors.toDataBuffers());
                        context.setBody(flux);
                        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(flux, DataBuffer.class);
                    }
                    return Mono.error(new BusinessException(ErrorCode.EM_100509));
                });

    }

}
