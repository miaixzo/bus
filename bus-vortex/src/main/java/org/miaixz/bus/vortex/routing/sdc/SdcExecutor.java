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
package org.miaixz.bus.vortex.routing.sdc;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.errors.transport.reactive.ReactiveErrorHandler;
import org.miaixz.bus.vortex.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Relays SDC requests to the qualified upstream through the shared outbound HTTP client.
 *
 * @author Kimi Liu
 */
public class SdcExecutor implements Executor<ServerRequest, ServerResponse> {

    /**
     * Query parameters defined by the public SDC pull API.
     */
    private static final List<String> PULL_PARAMETERS = List.of("app_id", "routes", "configs");

    /**
     * Representation headers for the response body.
     */
    private static final List<String> RESPONSE_HEADERS = List
            .of(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_ENCODING);

    /**
     * Creates an SDC executor that uses the shared outbound HTTP client.
     */
    public SdcExecutor() {
        // No initialization required.
    }

    /**
     * Builds the configured upstream URI and appends permitted metadata selectors for GET requests.
     *
     * @param context qualified target and validated query parameters
     * @return the outbound URI string
     */
    @Override
    public Mono<String> build(Context context) {
        return Mono.fromSupplier(() -> {
            String endpoint = context.getAssets().getUrl();
            if (context.getHttpMethod() != Http.Method.GET) {
                return endpoint;
            }
            Map<String, Object> query = new LinkedHashMap<>();
            for (String name : PULL_PARAMETERS) {
                Object value = context.getParameters().get(name);
                if (value != null) {
                    query.put(name, value);
                }
            }
            return query.isEmpty() ? endpoint : endpoint + "?" + UrlKit.toQuery(query, Charset.UTF_8);
        });
    }

    /**
     * Forwards the SDC request with the upstream credential, relays the POST body when present, and bounds the response
     * with the shared buffer budget.
     *
     * @param context qualified target, request method and forwarding credential
     * @param request incoming request with representation headers and body
     * @return the upstream response relayed through the gateway
     */
    @Override
    public Mono<ServerResponse> execute(Context context, ServerRequest request) {
        return build(context).flatMap(url -> {
            WebClient.RequestBodySpec downstream = Egress.Http
                    .request(HttpMethod.valueOf(context.getHttpMethod().value()), URI.create(url));
            downstream.headers(headers -> {
                headers.addAll(HttpHeaders.ACCEPT, request.headers().header(HttpHeaders.ACCEPT));
                headers.set(
                        Http.Auth.API_KEY_HEADER,
                        Objects.requireNonNull(
                                context.getHeaders().get(Http.Auth.API_KEY_HEADER),
                                "SDC forwarding credential is missing"));
            });
            if (context.getHttpMethod() == Http.Method.POST) {
                request.headers().contentType().ifPresent(downstream::contentType);
                downstream.body(
                        BodyInserters.fromDataBuffers(
                                request.exchange().getRequest().getBody()
                                        .doOnDiscard(PooledDataBuffer.class, Octets::release)));
            }
            return downstream.retrieve().onStatus(status -> status.isError(), ReactiveErrorHandler::upstreamFailure)
                    .toEntityFlux(DataBuffer.class).flatMap(this::relay);
        });
    }

    /**
     * Relays the upstream response within the shared atomic response budget.
     *
     * @param response upstream response
     * @return gateway response retaining ownership of the buffered body through the network write
     */
    private Mono<ServerResponse> relay(ResponseEntity<Flux<DataBuffer>> response) {
        var status = response.getStatusCode();
        var builder = ServerResponse.status(status).headers(headers -> {
            for (String name : RESPONSE_HEADERS) {
                var values = response.getHeaders().get(name);
                if (values != null) {
                    headers.put(name, values);
                }
            }
        });
        Flux<DataBuffer> body = response.getBody();
        if (body == null) {
            return builder.build();
        }
        if (status.is1xxInformational() || status.value() == HttpStatus.NO_CONTENT.value()
                || status.value() == HttpStatus.NOT_MODIFIED.value()) {
            return Octets.discard(body).then(builder.build());
        }

        int limit = Math.toIntExact(Holder.get().getMaxBufferedResponseSize());
        long length = response.getHeaders().getContentLength();
        if (length > limit) {
            return Octets.discard(body).then(Mono.error(new DataBufferLimitException("SDC response exceeds " + limit)));
        }
        return Octets.readForRelay(body, limit, Holder.responseBufferBudget(), length).flatMap(
                buffer -> Octets.own(
                        builder.contentLength(buffer.length())
                                .body(BodyInserters.fromDataBuffers(Octets.chunks(buffer))),
                        buffer));
    }

}
