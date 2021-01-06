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
package org.aoju.bus.goalie.handler;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.goalie.Assets;
import org.aoju.bus.goalie.Context;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
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
 * @version 6.1.8
 * @since JDK 1.8+
 */
public class ApiRouterHandler {

    private final Map<String, WebClient> clients = new ConcurrentHashMap<>();

    @NonNull
    public Mono<ServerResponse> handle(ServerRequest request) {

        Context context = Context.get(request);

        Assets assets = context.getAssets();

        Map<String, String> params = context.getRequestMap();

        String baseUrl = assets.getHost() + Symbol.C_COLON + assets.getPort();

        WebClient webClient = clients.computeIfAbsent(baseUrl, client -> WebClient.create(baseUrl));
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl).path(assets.getUrl());
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.setAll(params);
        if (HttpMethod.GET.equals(assets.getHttpMethod())) {
            builder.queryParams(multiValueMap);
        }
        WebClient.RequestBodySpec bodySpec = webClient
            .method(assets.getHttpMethod())
            .uri(builder.build().encode().toUri())
            .headers((headers) -> headers.addAll(request.headers().asHttpHeaders()));
        if (!HttpMethod.GET.equals(assets.getHttpMethod())) {
            if (request.headers().contentType().isPresent()) {
                MediaType mediaType = request.headers().contentType().get();
                //文件
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
        Flux<DataBuffer> flux = bodySpec
            .retrieve().bodyToFlux(DataBuffer.class);
        return ServerResponse.ok().body(flux, DataBuffer.class);
    }

}
