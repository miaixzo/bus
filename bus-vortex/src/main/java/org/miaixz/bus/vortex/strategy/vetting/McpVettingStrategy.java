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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.core.xyz.UrlKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Holder;
import org.miaixz.bus.vortex.strategy.VettingStrategy;

import reactor.core.publisher.Mono;

/**
 * Performs MCP Streamable HTTP vetting before the MCP route asset is resolved.
 * <p>
 * This strategy owns MCP protocol checks and Origin validation. Route resolution, authorization, signature
 * verification, and forwarding cleanup are handled by {@code McpQualifierStrategy}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@org.springframework.core.annotation.Order(Order.SECOND)
public class McpVettingStrategy extends VettingStrategy {

    /**
     * Creates an MCP vetting strategy.
     */
    public McpVettingStrategy() {
        // No initialization required.
    }

    /**
     * Applies MCP protocol validation.
     *
     * @param exchange current exchange
     * @param chain    remaining strategy chain
     * @return validation completion signal
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            return validateAndEnrichMcpRequest(exchange, context)
                    .map(validatedExchange -> sanitizeForwardHeaders(validatedExchange, context)).flatMap(chain::apply);
        });
    }

    /**
     * Validates Streamable HTTP method/header rules and trusted Origin configuration.
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
            Logger.info(
                    true,
                    "Vortex",
                    "MCP request vetted: strategy=mcp-vetting, clientIp={}, method={}, path={}, protocolVersion={}",
                    context.getX_request_ip(),
                    method,
                    request.getPath().value(),
                    request.getHeaders().getFirst(Args.MCP_PROTOCOL_VERSION));
        }).thenReturn(exchange);
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
