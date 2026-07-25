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
package org.miaixz.bus.vortex.strategy.qualifier;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.SignatureException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.HMac;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;
import org.miaixz.bus.vortex.strategy.QualifierStrategy;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Qualifies MCP ingress requests by resolving the registered MCP service route asset.
 * <p>
 * MCP qualification owns ingress-route matching, HTTP method verification, authorization, optional signature checks,
 * and final downstream parameter cleanup.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.THIRD)
public class McpQualifierStrategy extends QualifierStrategy {

    /**
     * Creates an MCP qualifier strategy.
     *
     * @param provider credential validation provider
     * @param registry asset registry
     */
    public McpQualifierStrategy(AuthorizeProvider provider, AssetsRegistry registry) {
        super(provider, registry);
    }

    /**
     * Resolves the MCP route version and supplies the default when the request does not carry one.
     *
     * @param exchange current web exchange
     * @param context  current request context
     * @return resolved MCP route version
     */
    @Override
    protected String version(ServerWebExchange exchange, Context context) {
        String version = value(context, Args.VERSION);
        return StringKit.isBlank(version) ? Args.DEFAULT_VERSION : version;
    }

    /**
     * Resolves the MCP service-prefix asset and stores it in the request context.
     *
     * @param exchange current exchange
     * @param chain    remaining strategy chain
     * @return qualification completion signal
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            Keying.RegistrySpec requestRoute = route(exchange, context);
            String namespace = requestRoute.namespacePart();
            String type = requestRoute.typeKeyPart();
            String appId = requestRoute.appIdPart();
            String method = requestRoute.methodPart();
            String version = requestRoute.versionPart();
            Integer verb = requestRoute.verbPart();

            return Mono.defer(() -> Mono.justOrEmpty(this.registry.get(requestRoute))).switchIfEmpty(Mono.defer(() -> {
                Logger.warn(
                        false,
                        "Vortex",
                        "MCP route asset not found: strategy=mcp-qualifier, clientIp={}, namespace={}, type={}, appId={}, method={}, version={}, verb={}",
                        context.getX_request_ip(),
                        namespace,
                        type,
                        appId,
                        method,
                        version,
                        verb);
                return Mono.error(new ValidateException(ErrorCode._100800));
            })).flatMap(match -> {
                Assets assets = match.assets();
                context.setAssets(assets);
                context.setRemainingPath(match.remainingPath());
                Logger.info(
                        true,
                        "Vortex",
                        "MCP route asset resolved: strategy=mcp-qualifier, clientIp={}, namespace={}, type={}, appId={}, method={}, version={}, verb={}, policy={}, sign={}, remainingPath={}",
                        context.getX_request_ip(),
                        assets.getNamespace_id(),
                        assets.getType(),
                        assets.getApp_id(),
                        assets.getMethod(),
                        assets.getVersion(),
                        assets.getVerb(),
                        assets.getPolicy(),
                        assets.getSign(),
                        match.remainingPath());
                Mono<Void> validationMono = this.method(context, assets);
                Mono<Void> authMono = !Consts.ZERO.equals(assets.getPolicy()) ? this.authorize(context) : Mono.empty();
                return validationMono.then(authMono).then(validateSignature(exchange, context, assets)).flatMap(
                        validatedExchange -> finalizeParameters(validatedExchange, context)
                                .thenReturn(validatedExchange));
            }).flatMap(
                    validatedExchange -> Mono.fromRunnable(
                            () -> Logger.info(
                                    true,
                                    "Vortex",
                                    "MCP qualifier completed: strategy=mcp-qualifier, clientIp={}, namespace={}, type={}, appId={}, method={}, version={}",
                                    context.getX_request_ip(),
                                    namespace,
                                    type,
                                    appId,
                                    method,
                                    version))
                            .then(chain.apply(validatedExchange)));
        });
    }

    /**
     * Verifies an MCP route signature and restores the POST body when it was consumed for hashing.
     *
     * @param exchange current exchange
     * @param context  request context
     * @param assets   resolved MCP route asset from the request context
     * @return exchange to continue with
     */
    protected Mono<ServerWebExchange> validateSignature(ServerWebExchange exchange, Context context, Assets assets) {
        if (assets == null) {
            return Mono.error(new ValidateException(ErrorCode._100800));
        }
        if (!Integer.valueOf(1).equals(assets.getSign())) {
            return Mono.just(exchange);
        }
        if (context.getHttpMethod() != Http.Method.POST) {
            return Mono.fromRunnable(() -> verifyMcpSignature(exchange.getRequest(), context, assets, new byte[0]))
                    .thenReturn(exchange);
        }
        return exchange.getRequest().getBody().collectList().map(this::readAndRelease).flatMap(
                body -> Mono.fromRunnable(() -> verifyMcpSignature(exchange.getRequest(), context, assets, body))
                        .thenReturn(cacheBody(exchange, body)));
    }

    /**
     * Reads all request body buffers into one byte array and releases pooled buffers directly.
     *
     * @param buffers request body buffers
     * @return request body bytes
     */
    private byte[] readAndRelease(List<DataBuffer> buffers) {
        if (buffers == null || buffers.isEmpty()) {
            return new byte[0];
        }
        int size = buffers.stream().mapToInt(DataBuffer::readableByteCount).sum();
        byte[] bytes = new byte[size];
        int offset = 0;
        try {
            for (DataBuffer buffer : buffers) {
                int readable = buffer.readableByteCount();
                buffer.read(bytes, offset, readable);
                offset += readable;
            }
            return bytes;
        } finally {
            buffers.forEach(dataBuffer -> {
                if (dataBuffer instanceof PooledDataBuffer pooledDataBuffer && pooledDataBuffer.isAllocated()) {
                    pooledDataBuffer.release();
                }
            });
        }
    }

    /**
     * Builds the MCP canonical signature payload and compares it with the request signature.
     *
     * @param request current HTTP request
     * @param context request context
     * @param assets  resolved MCP route asset
     * @param body    cached request body bytes, or an empty array for body-less requests
     */
    private void verifyMcpSignature(ServerHttpRequest request, Context context, Assets assets, byte[] body) {
        String timestamp = request.getHeaders().getFirst(Args.X_TIMESTAMP);
        String nonce = request.getHeaders().getFirst(Args.X_NONCE);
        String signature = request.getHeaders().getFirst(Args.X_SIGN);
        if (StringKit.isBlank(timestamp) || StringKit.isBlank(nonce) || StringKit.isBlank(signature)) {
            throw new SignatureException(ErrorCode._100109);
        }
        String canonical = request.getMethod().name() + Symbol.LF + request.getURI().getRawPath() + Symbol.LF
                + canonicalQuery(request) + Symbol.LF + timestamp + Symbol.LF + nonce + Symbol.LF
                + StringKit.toStringOrEmpty(request.getHeaders().getFirst(Args.MCP_PROTOCOL_VERSION)) + Symbol.LF
                + StringKit.toStringOrEmpty(request.getHeaders().getFirst(Args.MCP_SESSION_ID)) + Symbol.LF
                + Builder.sha256().digestHex(body == null ? new byte[0] : body);
        String secret = StringKit.isNotBlank(context.getBearer()) ? context.getBearer() : getToken(context);
        if (StringKit.isNotBlank(secret)) {
            context.setBearer(secret);
        } else {
            secret = assets.getMethod();
        }
        HMac mac = StringKit.isBlank(secret) ? null
                : Builder.hmac(Algorithm.HMACSHA256, secret.getBytes(Charset.UTF_8));
        String expected = mac == null ? null : Base64.encode(mac.digest(canonical.getBytes(Charset.UTF_8)));
        if (!constantTimeEquals(expected, signature)) {
            throw new SignatureException(ErrorCode._100109);
        }
    }

    /**
     * Builds a stable query-string representation for MCP signature verification.
     *
     * @param request current HTTP request
     * @return query parameters sorted by key and value
     */
    private String canonicalQuery(ServerHttpRequest request) {
        TreeMap<String, List<String>> sorted = new TreeMap<>();
        request.getQueryParams().forEach((key, values) -> sorted.put(key, values.stream().sorted().toList()));
        return sorted.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(value -> entry.getKey() + Symbol.EQUAL + value))
                .collect(Collectors.joining(Symbol.AND));
    }

    /**
     * Replaces the consumed request body with a replayable body backed by cached bytes.
     *
     * @param exchange current web exchange
     * @param body     cached body bytes
     * @return exchange carrying a decorated request body
     */
    private ServerWebExchange cacheBody(ServerWebExchange exchange, byte[] body) {
        ServerHttpRequest request = new ServerHttpRequestDecorator(exchange.getRequest()) {

            @Override
            public Flux<DataBuffer> getBody() {
                return Flux.defer(() -> Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
            }

        };
        return exchange.mutate().request(request).build();
    }

    /**
     * Compares two signature strings without returning early on the first differing byte.
     *
     * @param expected expected signature
     * @param actual   request signature
     * @return {@code true} when both signatures match
     */
    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        byte[] left = expected.getBytes(Charset.UTF_8);
        byte[] right = actual.getBytes(Charset.UTF_8);
        int diff = left.length ^ right.length;
        int max = Math.max(left.length, right.length);
        for (int i = 0; i < max; i++) {
            byte l = i < left.length ? left[i] : 0;
            byte r = i < right.length ? right[i] : 0;
            diff |= l ^ r;
        }
        return diff == 0;
    }

}
