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
package org.miaixz.bus.vortex.guard;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.auth.guard.CallerValidator;
import org.miaixz.bus.auth.guard.SignValidator;
import org.miaixz.bus.auth.guard.SignValidator.RequestMode;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.SignatureException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Signing;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.Octets;
import org.miaixz.bus.vortex.magic.ErrorCode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Adapts resolved REST, MCP, and Slug requests to the Auth signature validator. It extracts protocol fields, maps
 * verification outcomes to gateway errors, and owns the read and replay lifecycle of signed MCP bodies.
 *
 * @author Kimi Liu
 */
public class SignatureVerifier {

    /**
     * Exchange attribute that owns an MCP signed POST body's replay buffer until request termination.
     */
    private static final String MCP_CACHED_BODY = "X.MCP_CACHED_BODY";

    /**
     * Prevents additional verifier instances.
     */
    public SignatureVerifier() {
        // No initialization required.
    }

    /**
     * Verifies the REST signature required by the resolved asset.
     *
     * @param context current request context
     * @param assets  resolved route asset
     * @return verification completion signal
     */
    public static Mono<Void> verifyRest(Context context, Assets assets) {
        return verifyParameters(context, assets, true);
    }

    /**
     * Verifies the legacy parameter signature used by public Slug routes.
     *
     * @param context current request context
     * @param assets  resolved Slug route asset
     * @return verification completion signal
     */
    public static Mono<Void> verifySlug(Context context, Assets assets) {
        return verifyParameters(context, assets, false);
    }

    /**
     * Verifies a signed parameter request through Auth while retaining the gateway's error mapping and scheduler.
     *
     * @param context    current request context
     * @param assets     resolved route asset
     * @param supportsV1 whether the protocol supports credential-bound signatures
     * @return verification completion signal
     */
    private static Mono<Void> verifyParameters(Context context, Assets assets, boolean supportsV1) {
        if (assets == null) {
            return Mono.empty();
        }
        RequestMode mode = SignValidator
                .requestMode(context.isSigningEnabled(), () -> Signing.of(assets.getSigning()).key(), supportsV1);
        if (mode == RequestMode.DISABLED || mode == RequestMode.SKIP) {
            return Mono.empty();
        }
        if (mode == RequestMode.UNSUPPORTED) {
            return Mono.error(new ValidateException(ErrorCode._116000));
        }
        if (StringKit.isBlank(value(context, Args.SIGN))) {
            return Mono.error(new ValidateException(ErrorCode._100108));
        }
        return Mono.fromCallable(() -> {
            var result = SignValidator.verifyRequest(
                    mode,
                    context.getHttpMethod(),
                    context.getParameters(),
                    name -> value(context, name),
                    () -> value(context.getParameters(), Args.SIGN),
                    // Public routes still accept a signing credential independently of authentication switches.
                    () -> Consts.ZERO.equals(assets.getPolicy())
                            ? CallerValidator.credential(context.getHeaders(), true, true)
                            : context.clientCredential(),
                    context.getBearerMaxLength(),
                    context.getApiKeyMaxLength());
            return switch (result) {
                case MISSING_CREDENTIAL -> throw new ValidateException(ErrorCode._116001);
                case INVALID_CREDENTIAL -> throw new ValidateException(ErrorCode._100109);
                case INVALID_SIGNATURE -> throw new SignatureException(ErrorCode._100109);
                case VALID -> (Void) null;
            };
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Verifies an MCP route signature, exposes any restored POST body to the continuation, and releases the body lease
     * when downstream processing terminates.
     *
     * @param exchange     current web exchange
     * @param context      current request context
     * @param assets       resolved MCP route asset
     * @param continuation downstream processing that consumes the verified exchange
     * @return downstream completion signal
     */
    public static Mono<Void> verifyMcp(
            ServerWebExchange exchange,
            Context context,
            Assets assets,
            FunctionX<ServerWebExchange, Mono<Void>> continuation) {
        Objects.requireNonNull(continuation, "Continuation cannot be null");
        if (assets == null) {
            return Mono.error(new ValidateException(ErrorCode._100800));
        }
        RequestMode mode = SignValidator
                .requestMode(context.isSigningEnabled(), () -> Signing.of(assets.getSigning()).key(), false);
        if (mode == RequestMode.DISABLED) {
            return Mono.defer(() -> continuation.apply(exchange));
        }
        if (mode == RequestMode.UNSUPPORTED) {
            return Mono.error(new ValidateException(ErrorCode._116000));
        }
        Mono<ServerWebExchange> verified = mode == RequestMode.SKIP ? Mono.just(exchange)
                : verifyMcpExchange(exchange, context, assets);
        return verified.flatMap(
                validatedExchange -> Mono.defer(() -> continuation.apply(validatedExchange))
                        .doFinally(signal -> releaseMcpBody(validatedExchange)));
    }

    /**
     * Verifies an MCP route signature and restores a consumed signed POST body for downstream processing.
     *
     * @param exchange current web exchange
     * @param context  current request context
     * @param assets   resolved MCP route asset
     * @return exchange carrying the original or restored request body
     */
    private static Mono<ServerWebExchange> verifyMcpExchange(
            ServerWebExchange exchange,
            Context context,
            Assets assets) {
        if (context.getHttpMethod() != Http.Method.POST) {
            return Mono
                    .fromRunnable(
                            () -> verifyMcpRequest(exchange.getRequest(), context, assets, Normal.EMPTY_BYTE_ARRAY))
                    .thenReturn(exchange);
        }
        return Octets
                .readForParsing(
                        exchange.getRequest().getBody(),
                        Math.toIntExact(Holder.get().getMaxBufferedRequestSize()),
                        Holder.requestBufferBudget(),
                        exchange.getRequest().getHeaders().getContentLength())
                .onErrorMap(DataBufferLimitException.class, error -> new ValidateException(ErrorCode._100530)).flatMap(
                        body -> Mono
                                .fromRunnable(
                                        () -> verifyMcpRequest(exchange.getRequest(), context, assets, body.bytes()))
                                .thenReturn(cacheMcpBody(exchange, body)).doOnError(error -> {
                                    exchange.getAttributes().remove(MCP_CACHED_BODY);
                                    body.close();
                                }));
    }

    /**
     * Releases an MCP replay body owned by the supplied exchange.
     *
     * @param exchange exchange that may own a cached signed body
     */
    private static void releaseMcpBody(ServerWebExchange exchange) {
        Object cached = exchange.getAttributes().remove(MCP_CACHED_BODY);
        if (cached instanceof Octets.BufferedBody body) {
            body.close();
        }
    }

    /**
     * Extracts MCP canonical fields and delegates body signature verification to Auth.
     *
     * @param request current HTTP request
     * @param context request context
     * @param assets  resolved MCP route asset
     * @param body    cached request bytes, or an empty array
     */
    private static void verifyMcpRequest(ServerHttpRequest request, Context context, Assets assets, byte[] body) {
        String timestamp = request.getHeaders().getFirst(Http.Auth.TIMESTAMP_HEADER);
        String nonce = request.getHeaders().getFirst(Http.Auth.NONCE_HEADER);
        String signature = request.getHeaders().getFirst(Http.Auth.SIGNATURE_HEADER);
        if (StringKit.isBlank(timestamp) || StringKit.isBlank(nonce) || StringKit.isBlank(signature)) {
            throw new SignatureException(ErrorCode._100109);
        }
        String canonical = request.getMethod().name() + Symbol.LF + request.getURI().getRawPath() + Symbol.LF
                + canonicalMcpQuery(request) + Symbol.LF + timestamp + Symbol.LF + nonce + Symbol.LF
                + StringKit.toStringOrEmpty(request.getHeaders().getFirst(Args.MCP_PROTOCOL_VERSION)) + Symbol.LF
                + StringKit.toStringOrEmpty(request.getHeaders().getFirst(Args.MCP_SESSION_ID)) + Symbol.LF;
        boolean valid = SignValidator.verifyBody(() -> {
            String credential = CallerValidator.signingCredential(context.getBearer(), context.getHeaders());
            if (StringKit.isNotBlank(credential) && context.isBearerEnabled()) {
                context.setBearer(credential);
            }
            return credential;
        }, assets.getMethod(), canonical, body, signature);
        if (!valid) {
            throw new SignatureException(ErrorCode._100109);
        }
    }

    /**
     * Builds the stable sorted MCP query representation.
     *
     * @param request current HTTP request
     * @return query parameters sorted by key and value
     */
    private static String canonicalMcpQuery(ServerHttpRequest request) {
        Map<String, List<String>> sorted = new TreeMap<>();
        request.getQueryParams().forEach((key, values) -> sorted.put(key, values.stream().sorted().toList()));
        return sorted.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(value -> entry.getKey() + Symbol.EQUAL + value))
                .collect(Collectors.joining(Symbol.AND));
    }

    /**
     * Replaces a consumed MCP request body with a replay publisher backed by the leased byte array.
     *
     * @param exchange current web exchange
     * @param body     cached body and its request-byte lease
     * @return exchange carrying the restored request body
     */
    private static ServerWebExchange cacheMcpBody(ServerWebExchange exchange, Octets.BufferedBody body) {
        exchange.getAttributes().put(MCP_CACHED_BODY, body);
        ServerHttpRequest request = new ServerHttpRequestDecorator(exchange.getRequest()) {

            /**
             * Replays the leased request bytes for downstream MCP handlers.
             *
             * @return deferred publisher containing the cached request body
             */
            @Override
            public Flux<DataBuffer> getBody() {
                return Flux.defer(() -> Mono.just(exchange.getResponse().bufferFactory().wrap(body.bytes())));
            }

        };
        return exchange.mutate().request(request).build();
    }

    /**
     * Resolves one request parameter from the parsed parameter and query maps.
     *
     * @param context current request context
     * @param key     canonical parameter name
     * @return resolved value, or {@code null}
     */
    private static String value(Context context, String key) {
        String value = value(context == null ? null : context.getParameters(), key);
        return value != null || context == null ? value : value(context.getQuery(), key);
    }

    /**
     * Resolves one value by exact key first and then by an unambiguous case-insensitive match.
     *
     * @param values source values
     * @param key    canonical key
     * @return resolved value, or {@code null}
     */
    private static String value(Map<?, ?> values, String key) {
        if (values == null || values.isEmpty() || StringKit.isBlank(key)) {
            return null;
        }
        if (values.containsKey(key)) {
            Object exact = values.get(key);
            return exact == null ? null : exact.toString();
        }
        boolean matched = false;
        String matchedValue = null;
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            Object name = entry.getKey();
            if (name != null && key.equalsIgnoreCase(name.toString())) {
                Object candidate = entry.getValue();
                String candidateValue = candidate == null ? null : candidate.toString();
                if (matched && !Objects.equals(matchedValue, candidateValue)) {
                    throw new ValidateException(ErrorCode._100101);
                }
                matched = true;
                matchedValue = candidateValue;
            }
        }
        return matchedValue;
    }

}
