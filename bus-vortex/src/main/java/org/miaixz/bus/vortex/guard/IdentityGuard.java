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

import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.guard.GuardEvaluator;
import org.miaixz.bus.cortex.guard.GuardEvaluator.Mode;
import org.miaixz.bus.cortex.registry.MetadataCodec;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.routing.slug.SlugRouteMatcher;

/**
 * Adapts Vortex requests and resolved assets to the transport-neutral service identity rules.
 *
 * @author Kimi Liu
 */
public final class IdentityGuard {

    /**
     * Exchange attribute containing header values up to the service identity decision.
     */
    private static final String TOKENS = IdentityGuard.class.getName() + ".tokens";

    /**
     * Route asset identity policy selected during REST qualification for outbound signing.
     */
    private static final String ASSET = IdentityGuard.class.getName() + ".asset";

    /**
     * Exchange attribute set by ingress after validating the private listener and path.
     */
    private static final String PRIVATE_SDC = IdentityGuard.class.getName() + ".privateSdc";

    /**
     * Audience assigned to the bootstrap asset by the SDC qualifier.
     */
    private static final String BOOTSTRAP_AUDIENCE = IdentityGuard.class.getName() + ".bootstrapAudience";

    /**
     * Shared service identity rule evaluator.
     */
    private final GuardEvaluator identity;

    /**
     * Resolves a registered application ID into its current service code.
     */
    private final FunctionX<String, String> resolver;

    /**
     * Port reserved for private SDC ingress, or zero when that listener is disabled.
     */
    private final int privatePort;

    /**
     * Exact application path reserved for private SDC ingress.
     */
    private final String privatePath;

    /**
     * Creates the request adapter for one gateway context.
     *
     * @param identity common identity evaluator
     * @param resolver trusted application code directory
     */
    public IdentityGuard(GuardEvaluator identity, FunctionX<String, String> resolver) {
        this(identity, resolver, 0, null);
    }

    /**
     * Creates the identity adapter with the configured private ingress.
     *
     * @param identity    shared service identity evaluator
     * @param resolver    trusted application code directory
     * @param privatePort private listener port, or zero when it is disabled
     * @param privatePath exact path reserved for private SDC, or null when unset
     */
    public IdentityGuard(GuardEvaluator identity, FunctionX<String, String> resolver, int privatePort,
            String privatePath) {
        this.identity = Objects.requireNonNull(identity);
        this.resolver = Objects.requireNonNull(resolver);
        this.privatePort = privatePort;
        this.privatePath = privatePath;
    }

    /**
     * Selects the route's service identity requirement and removes the external credential from the request. REST
     * retains the header values until the resolved asset can override the path rule.
     *
     * @param exchange actual ingress exchange
     * @return exchange with the reserved header removed, or the original exchange when no header was supplied
     * @throws ResponseStatusException if the request path is served by a different listener
     */
    public ServerWebExchange ingress(ServerWebExchange exchange) {
        var local = exchange.getRequest().getLocalAddress();
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        boolean privateListener = privatePort > 0 && local != null && local.getPort() == privatePort;
        if (path.equals(privatePath) != privateListener) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        exchange.getAttributes().put(PRIVATE_SDC, privateListener);
        var received = identity
                .receive(path, fixed(path), Args.isRestRequest(path), exchange.getRequest().getHeaders()::get);
        if (!received.present())
            return exchange;
        if (!received.values().isEmpty())
            exchange.getAttributes().put(TOKENS, received.values());
        return exchange.mutate().request(
                request -> request
                        .headers(headers -> identity.clearHeaders(null, headers.headerNames(), headers::remove)))
                .build();
    }

    /**
     * Checks whether ingress admitted the exchange on the private SDC listener.
     *
     * @param exchange current gateway exchange
     * @return true when both the listener port and configured private path matched
     */
    public static boolean isPrivateSdc(ServerWebExchange exchange) {
        return Boolean.TRUE.equals(exchange.getAttribute(PRIVATE_SDC));
    }

    /**
     * Sets the configured service audience after the private bootstrap route is selected.
     *
     * @param exchange current private SDC exchange
     * @param audience trusted bootstrap service code
     */
    public static void setBootstrapAudience(ServerWebExchange exchange, String audience) {
        exchange.getAttributes().put(BOOTSTRAP_AUDIENCE, audience);
    }

    /**
     * Removes pending credentials when a request terminates before qualification consumes them.
     *
     * @param exchange gateway exchange owning pending ingress credentials
     */
    public void discard(ServerWebExchange exchange) {
        exchange.getAttributes().remove(TOKENS);
    }

    /**
     * Takes ownership of the stored credential values so they cannot be consumed by a later stage.
     *
     * @param exchange sanitized request exchange
     * @return supplied token values, or an empty list if none were kept
     */
    private static List<String> takeTokens(ServerWebExchange exchange) {
        List<String> tokens = exchange.getAttributeOrDefault(TOKENS, List.of());
        exchange.getAttributes().remove(TOKENS);
        return tokens;
    }

    /**
     * Applies the common service identity policy after the gateway attempts to resolve the asset.
     *
     * @param exchange sanitized gateway exchange
     * @param assets   selected runtime asset, or null for an unsuccessful lookup
     * @throws ResponseStatusException if identity verification fails, the policy is unavailable, or the selected asset
     *                                 is incompatible with the ingress route
     */
    public void qualified(ServerWebExchange exchange, Assets assets) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        boolean control = Args.isSdcRequest(path);
        boolean rest = Args.isRestRequest(path);
        List<String> tokens = takeTokens(exchange);
        boolean sdcAsset = assets != null && Integer.valueOf(Args.ROUTE_SDC).equals(assets.getProtocol());
        if (!Args.isMcpRequest(path) && (!rest || assets == null)) {
            GuardEvaluator.Asset policy = control && sdcAsset ? policy(assets) : null;
            verify(exchange, path, tokens, policy);
            if (control && sdcAsset
                    && (policy == null || policy.inbound() != Mode.REQUIRED || policy.subjects().isEmpty()))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (assets == null)
            return;
        if (control != sdcAsset || control && isPrivateSdc(exchange) != privateOperation(assets))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (rest) {
            GuardEvaluator.Asset policy = policy(assets);
            if (policy != null)
                exchange.getAttributes().put(ASSET, policy);
            verify(exchange, path, tokens, policy);
        }
    }

    /**
     * Checks whether an SDC asset is reserved for private ingress.
     *
     * @param asset selected SDC asset
     * @return {@code true} if the asset targets an internal path or its metadata declares it internal
     */
    private static boolean privateOperation(Assets asset) {
        var metadata = MetadataCodec.object(MetadataCodec.root(asset.getMetadata()).get(MetadataCodec.ROOT));
        return asset.getUrl() != null && asset.getUrl().startsWith("/internal/")
                || metadata != null && Boolean.TRUE.equals(metadata.get("internal"));
    }

    /**
     * Reads the registered asset policy and maps malformed configuration to an HTTP service error.
     *
     * @param assets selected runtime asset
     * @return declared service identity policy, or {@code null} when absent
     * @throws ResponseStatusException if the configured policy is invalid
     */
    private static GuardEvaluator.Asset policy(Assets assets) {
        try {
            return GuardEvaluator.asset(assets.getMetadata());
        } catch (IllegalArgumentException invalid) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * Checks inbound service identity once and publishes only the verified caller.
     *
     * @param exchange sanitized request exchange
     * @param path     effective gateway route
     * @param tokens   all supplied service identity values
     * @param asset    optional policy from the resolved route asset
     * @throws ResponseStatusException if authentication is rejected or a required direction is unavailable
     */
    private void verify(
            ServerWebExchange exchange,
            String path,
            List<String> tokens,
            GuardEvaluator.Asset asset) {
        var result = identity.accept(path, tokens, fixed(path), asset);
        switch (result.status()) {
            case REJECTED -> throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            case UNAVAILABLE -> throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
            case ACCEPTED -> {
                if (result.verified() != null)
                    exchange.getAttributes().put(GuardEvaluator.VERIFIED_ATTRIBUTE, result.verified());
            }
        }
    }

    /**
     * Signs for the configured next hop and includes the authenticated caller and resolved SDC asset.
     *
     * @param exchange qualified request
     * @param context  gateway routing context
     * @return exchange carrying the next-hop credential if required
     * @throws ResponseStatusException if a required caller or next-hop credential is unavailable
     */
    public ServerWebExchange outbound(ServerWebExchange exchange, Context context) {
        String route = exchange.getRequest().getPath().pathWithinApplication().value();
        Mode fixed = fixed(route);
        var asset = context.getAssets();
        if (fixed == Mode.OFF || asset == null && fixed == null)
            return forward(exchange, context, null);
        GuardEvaluator.Asset policy = exchange.getAttribute(ASSET);
        Mode mode = policy == null ? null : policy.outbound();
        if (Args.isRestRequest(route) && mode != Mode.REQUIRED)
            return forward(exchange, context, null);
        try {
            SlugRouteMatcher.Match slug = exchange.getAttribute(SlugRouteMatcher.MATCH_ATTRIBUTE);
            URI destination = slug != null ? SlugRouteMatcher.operation(slug, exchange.getRequest().getPath().value())
                    : asset == null || asset.getUrl() == null ? null : URI.create(asset.getUrl());
            String audience = null;
            if (destination != null && !destination.isAbsolute() && destination.getRawAuthority() == null
                    && asset.getApp_id() != null && asset.getInstances() != null && asset.getInstances().size() == 1
                    && asset.getApp_id().equals(asset.getInstances().getFirst().getApp_id())) {
                audience = isPrivateSdc(exchange) ? exchange.getAttribute(BOOTSTRAP_AUDIENCE) : null;
                if (audience == null) {
                    audience = resolver.apply(asset.getApp_id());
                }
            }
            GuardEvaluator.Verified caller = exchange
                    .getAttribute(GuardEvaluator.VERIFIED_ATTRIBUTE);
            if (fixed == Mode.REQUIRED && caller == null)
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            if (fixed == Mode.REQUIRED && (asset == null || StringKit.isBlank(asset.getId())))
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
            String token = identity.outbound(
                    route,
                    audience,
                    destination == null ? null : destination.getPath(),
                    fixed,
                    mode,
                    Args.isRestRequest(route),
                    fixed == Mode.REQUIRED ? caller : null,
                    fixed == Mode.REQUIRED ? asset.getId() : null);
            return forward(exchange, context, token);
        } catch (IllegalArgumentException | IllegalStateException unavailable) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    /**
     * Applies the shared next-hop header handling to the gateway context and request.
     *
     * @param exchange qualified request
     * @param context  gateway routing context
     * @param token    freshly signed credential, or null to clear the reserved header
     * @return exchange with the outgoing service identity header applied
     */
    private ServerWebExchange forward(ServerWebExchange exchange, Context context, String token) {
        identity.forwardHeaders(null, token, context.getHeaders().keySet(), context::removeHeader, context::setHeader);
        if (token == null && exchange.getRequest().getHeaders().headerNames().stream()
                .noneMatch(name -> identity.reservesHeader(null, name)))
            return exchange;
        return exchange.mutate().request(
                request -> request.headers(
                        headers -> identity
                                .forwardHeaders(null, token, headers.headerNames(), headers::remove, headers::set)))
                .build();
    }

    /**
     * Resolves the fixed boundary of a gateway route family.
     *
     * @param path actual gateway route path
     * @return fixed mode for SDC or MCP, or null for a configurable route
     */
    private static Mode fixed(String path) {
        if (Args.isSdcRequest(path))
            return Mode.REQUIRED;
        return Args.isMcpRequest(path) ? Mode.OFF : null;
    }

}
