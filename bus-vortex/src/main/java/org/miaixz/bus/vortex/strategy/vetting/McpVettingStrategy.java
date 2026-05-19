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
package org.miaixz.bus.vortex.strategy.vetting;

import java.net.URI;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.PooledDataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.SignatureException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.center.HMac;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.strategy.VettingStrategy;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Performs MCP Streamable HTTP vetting after the MCP route asset has been resolved.
 * <p>
 * This strategy owns MCP protocol checks, Origin validation, optional MCP signature verification, and POST body replay
 * after signature verification consumes the raw request body.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.THIRD)
public class McpVettingStrategy extends VettingStrategy {

    /**
     * Creates an MCP vetting strategy.
     */
    public McpVettingStrategy() {
        // No initialization required.
    }

    /**
     * Applies MCP protocol validation and signature verification.
     *
     * @param exchange current exchange
     * @param chain    remaining strategy chain
     * @return validation completion signal
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            return validateAndEnrichMcpRequest(exchange, context).flatMap(chain::apply);
        });
    }

    /**
     * Validates Streamable HTTP method/header rules, enriches context metadata, and verifies route signatures.
     *
     * @param exchange current exchange
     * @param context  request context
     * @return exchange to continue with
     */
    protected Mono<ServerWebExchange> validateAndEnrichMcpRequest(ServerWebExchange exchange, Context context) {
        return Mono.fromRunnable(() -> {
            ServerHttpRequest request = exchange.getRequest();
            HTTP.Method method = context.getHttpMethod();
            if (method != HTTP.Method.POST && method != HTTP.Method.GET && method != HTTP.Method.DELETE) {
                throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "Unsupported MCP method");
            }
            if (method == HTTP.Method.POST) {
                MediaType contentType = request.getHeaders().getContentType();
                if (!isJsonContentType(contentType)) {
                    throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "MCP POST requires JSON");
                }
                if (!accepts(request, MediaType.APPLICATION_JSON) || !accepts(request, MediaType.TEXT_EVENT_STREAM)) {
                    throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                            "MCP POST Accept must include application/json and text/event-stream");
                }
            } else if (method == HTTP.Method.GET) {
                if (!accepts(request, MediaType.TEXT_EVENT_STREAM)) {
                    throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE,
                            "MCP GET Accept must include text/event-stream");
                }
            } else if (StringKit.isBlank(request.getHeaders().getFirst(Args.MCP_SESSION_ID))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "MCP DELETE requires Mcp-Session-Id");
            }
            validateTrustedOrigin(request);
            enrich(exchange, context);
            Logger.info(
                    true,
                    "Vortex",
                    "MCP request vetted: strategy=mcp-vetting, clientIp={}, method={}, path={}, protocolVersion={}",
                    context.getX_request_ip(),
                    method,
                    request.getPath().value(),
                    request.getHeaders().getFirst(Args.MCP_PROTOCOL_VERSION));
        }).then(validateSignature(exchange, context, context.getAssets()));
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
        if (context.getHttpMethod() != HTTP.Method.POST) {
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
            buffers.forEach(this::releaseIfPooled);
        }
    }

    /**
     * Releases a pooled buffer directly.
     *
     * @param dataBuffer data buffer to release when it is pooled
     */
    private void releaseIfPooled(DataBuffer dataBuffer) {
        if (dataBuffer instanceof PooledDataBuffer pooledDataBuffer && pooledDataBuffer.isAllocated()) {
            pooledDataBuffer.release();
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
     * Checks whether an MCP POST content type is JSON-compatible.
     *
     * @param contentType request content type
     * @return {@code true} when the content type is JSON or a structured JSON subtype
     */
    private boolean isJsonContentType(MediaType contentType) {
        return contentType != null && (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)
                || contentType.getSubtype().toLowerCase(java.util.Locale.ROOT).endsWith("+json"));
    }

    /**
     * Checks whether the request Accept header allows the expected response media type.
     *
     * @param request  current request
     * @param expected expected media type
     * @return {@code true} when the expected media type is accepted
     */
    private boolean accepts(ServerHttpRequest request, MediaType expected) {
        List<MediaType> accepted = request.getHeaders().getAccept();
        return accepted.stream().anyMatch(mediaType -> mediaType.isCompatibleWith(expected));
    }

    /**
     * Validates the MCP Origin header against configured trusted origins or the request host.
     *
     * @param request current request
     */
    private void validateTrustedOrigin(ServerHttpRequest request) {
        String origin = request.getHeaders().getFirst(HttpHeaders.ORIGIN);
        if (StringKit.isBlank(origin)) {
            return;
        }
        try {
            URI originUri = UrlKit.toURI(origin);
            if (StringKit.isBlank(originUri.getScheme()) || originUri.getHost() == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Malformed MCP Origin");
            }
            List<String> trustedOrigins = Holder.getMcpTrustedOrigins();
            boolean trusted = trustedOrigins.isEmpty() ? sameHostOrigin(originUri, request)
                    : trustedOrigins.stream().anyMatch(candidate -> trustedOriginMatches(candidate, originUri));
            if (!trusted) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Untrusted MCP Origin");
            }
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Malformed MCP Origin");
        }
    }

    /**
     * Checks whether the Origin equals the current request origin.
     * <p>
     * Origin matching uses scheme and host only; gateway deployments commonly terminate and forward different ports.
     *
     * @param originUri parsed Origin URI
     * @param request   current request
     * @return {@code true} when both origins match
     */
    private boolean sameHostOrigin(URI originUri, ServerHttpRequest request) {
        String requestHost = request.getHeaders().getHost() == null ? request.getURI().getHost()
                : request.getHeaders().getHost().getHostString();
        return sameOrigin(getProtocol(request), requestHost, originUri);
    }

    /**
     * Checks whether a configured trusted origin matches the request Origin URI.
     *
     * @param candidate configured trusted origin
     * @param originUri parsed Origin URI
     * @return {@code true} when the configured origin matches
     */
    private boolean trustedOriginMatches(String candidate, URI originUri) {
        if (StringKit.isBlank(candidate)) {
            return false;
        }
        String trusted = candidate.trim();
        try {
            URI trustedUri = UrlKit.toURI(trusted);
            if (StringKit.isBlank(trustedUri.getScheme()) || trustedUri.getHost() == null) {
                return false;
            }
            return sameOrigin(trustedUri.getScheme(), trustedUri.getHost(), originUri);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    /**
     * Checks whether one origin tuple matches a request Origin URI without comparing ports.
     *
     * @param expectedScheme trusted or request scheme
     * @param expectedHost   trusted or request host
     * @param actual         parsed Origin URI
     * @return {@code true} when the origin tuple matches the URI origin
     */
    private boolean sameOrigin(String expectedScheme, String expectedHost, URI actual) {
        if (StringKit.isBlank(expectedScheme) || StringKit.isBlank(expectedHost) || actual == null
                || StringKit.isBlank(actual.getScheme()) || StringKit.isBlank(actual.getHost())) {
            return false;
        }
        return expectedScheme.equalsIgnoreCase(actual.getScheme()) && expectedHost.equalsIgnoreCase(actual.getHost());
    }

}
