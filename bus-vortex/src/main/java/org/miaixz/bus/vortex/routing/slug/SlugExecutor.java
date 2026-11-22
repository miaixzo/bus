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
package org.miaixz.bus.vortex.routing.slug;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.registry.ServiceRoutes;
import org.miaixz.bus.errors.transport.reactive.ReactiveErrorHandler;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.*;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.routing.FlowEnforcer;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Executes public slug forwarding requests as strict realtime streams or protected file downloads.
 * <p>
 * Slug responses are never aggregated: route assets must declare {@code stream=2} or {@code stream=3}. The response
 * body is passed through {@link FlowEnforcer}, so admission, memory pressure, backpressure and cancellation semantics
 * match other streaming protocols.
 *
 * @author Kimi Liu
 */
public class SlugExecutor implements Executor<ServerRequest, ServerResponse> {

    /**
     * Matcher used to resolve target templates.
     */
    private final SlugRouteMatcher matcher;

    /**
     * Creates a slug executor.
     *
     * @param matcher slug route matcher
     */
    public SlugExecutor(SlugRouteMatcher matcher) {
        this.matcher = matcher;
    }

    /**
     * Builds the target base URL from the current asset.
     *
     * @param context request context
     * @return base URL
     */
    @Override
    public Mono<String> build(Context context) {
        return Mono.fromCallable(() -> base(context.getAssets()));
    }

    /**
     * Executes the public slug forwarding request.
     *
     * @param context request context
     * @param request server request
     * @return downstream response
     */
    @Override
    public Mono<ServerResponse> execute(Context context, ServerRequest request) {
        URI target = target(context, request);
        WebClient.RequestBodySpec bodySpec = Egress.Http
                .request(HttpMethod.valueOf(context.getHttpMethod().value()), target);
        bodySpec.headers(headers -> {
            headers.addAll(request.headers().asHttpHeaders());
            context.getHeaders().forEach((name, value) -> {
                if (StringKit.isNotBlank(name) && value != null) {
                    headers.remove(name);
                    headers.add(name, value);
                }
            });
            headers.remove(HttpHeaders.HOST);
            headers.remove(HttpHeaders.TRANSFER_ENCODING);
            headers.remove(HttpHeaders.CONTENT_LENGTH);
            if (StringKit.isNotBlank(context.getX_request_domain())) {
                headers.set(HttpHeaders.HOST, context.getX_request_domain());
                headers.set("X-Forwarded-Host", context.getX_request_domain());
            }
        });

        if (context.getHttpMethod() != Http.Method.GET && context.getHttpMethod() != Http.Method.HEAD) {
            bodySpec.body(BodyInserters.fromDataBuffers(request.exchange().getRequest().getBody()));
        }

        Logger.info(
                true,
                "Vortex",
                "Public slug forwarding request: clientIp={}, method={}, path={}, target={}",
                context.getX_request_ip(),
                context.getHttpMethod(),
                request.path(),
                target);

        return bodySpec.retrieve().onStatus(status -> status.isError(), ReactiveErrorHandler::upstreamFailure)
                .toEntityFlux(DataBuffer.class).flatMap(responseEntity -> {
                    ServerResponse.BodyBuilder responseBuilder = ServerResponse.status(responseEntity.getStatusCode());
                    responseBuilder.headers(headers -> {
                        headers.addAll(responseEntity.getHeaders());
                        headers.remove(HttpHeaders.HOST);
                        headers.remove(HttpHeaders.TRANSFER_ENCODING);
                        headers.remove(HttpHeaders.CONTENT_LENGTH);
                    });
                    Flux<DataBuffer> body = responseEntity.getBody() == null ? Flux.empty()
                            : responseEntity.getBody().doOnDiscard(PooledDataBuffer.class, Octets::release);
                    if (context.getHttpMethod() == Http.Method.HEAD) {
                        return Octets.discard(body).then(responseBuilder.build());
                    }
                    Delivery delivery = Delivery.of(context.getAssets().getStream());
                    if (delivery == Delivery.BUFFERED) {
                        return Octets.discard(body).then(
                                Mono.error(
                                        new IllegalStateException(
                                                "Slug responses must use stream mode 2 or download mode 3")));
                    }
                    if (responseEntity.getHeaders().getContentLength() >= 0) {
                        responseBuilder.contentLength(responseEntity.getHeaders().getContentLength());
                    }
                    return responseBuilder.body(BodyInserters.fromDataBuffers(FlowEnforcer.relay(body, delivery)));
                });
    }

    /**
     * Builds the full target URI.
     *
     * @param context request context
     * @param request server request
     * @return target URI
     */
    private URI target(Context context, ServerRequest request) {
        SlugRouteMatcher.Match match = request.exchange().getAttribute(SlugRouteMatcher.MATCH_ATTRIBUTE);
        if (match == null) {
            match = this.matcher.match(request);
        }
        if (match == null) {
            throw new ValidateException(ErrorCode._100800);
        }
        URI renderedUri = SlugRouteMatcher.operation(match, request.path());
        String path = SlugRouteMatcher.stripQueryAndFragment(renderedUri.toASCIIString());
        String target = renderedUri.isAbsolute() ? path : SlugRouteMatcher.joinPath(base(match.assets()), path);

        List<String> queries = new ArrayList<>();
        if (StringKit.isNotBlank(renderedUri.getRawQuery())) {
            queries.add(renderedUri.getRawQuery());
        }
        String forwardQuery = query(context, request);
        if (StringKit.isNotBlank(forwardQuery)) {
            queries.add(forwardQuery);
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(target);
        if (!queries.isEmpty()) {
            builder.replaceQuery(String.join(Symbol.AND, queries));
        }
        return builder.build(true).toUri();
    }

    /**
     * Builds the asset base URL.
     *
     * @param assets resolved asset
     * @return base URL
     */
    private String base(Assets assets) {
        return ServiceRoutes.resolve(assets).toASCIIString();
    }

    /**
     * Builds the forwarded query string.
     *
     * @param context request context
     * @param request server request
     * @return raw query string
     */
    private String query(Context context, ServerRequest request) {
        List<String> parts = new ArrayList<>();
        Map<String, String> query = context.getQuery();
        if (!query.isEmpty()) {
            query.forEach((key, value) -> {
                if (!Args.isForwardingControlParameter(key)) {
                    parts.add(UrlKit.toQuery(Map.of(key, StringKit.toStringOrEmpty(value)), Charset.UTF_8));
                }
            });
        }
        return String.join(Symbol.AND, parts);
    }

}
