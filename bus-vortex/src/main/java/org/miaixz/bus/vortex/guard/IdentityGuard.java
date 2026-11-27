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

import org.miaixz.bus.auth.guard.GuardEvaluator;
import org.miaixz.bus.auth.guard.GuardEvaluator.Mode;
import org.miaixz.bus.auth.guard.GuardEvaluator.Target;
import org.miaixz.bus.auth.guard.inbound.InboundDecision;
import org.miaixz.bus.core.center.function.BiConsumerX;
import org.miaixz.bus.core.center.function.FunctionX;
import org.miaixz.bus.core.center.function.PredicateX;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.registry.MetadataCodec;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.routing.slug.SlugRouteMatcher;

/**
 * Supplies gateway route and destination information to Auth and applies the resulting decisions to the exchange.
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
     * Shared service identity rule evaluator.
     */
    private final GuardEvaluator identity;

    /**
     * Resolves a registered application ID into its current service code.
     */
    private final FunctionX<String, String> resolver;

    /**
     * Application policy that opts an asset into service identity verification when inbound Guard is enabled.
     */
    private final PredicateX<Assets> requiresIdentity;

    /**
     * Application admission policy for a resolved asset and its verified caller, if present.
     */
    private final BiConsumerX<Assets, GuardEvaluator.Verified> admission;

    /**
     * Configured service discovery and configuration route.
     */
    private final String registrationPath;

    /**
     * Creates the gateway service identity adapter.
     *
     * @param identity shared service identity evaluator
     * @param resolver trusted application code directory
     */
    public IdentityGuard(GuardEvaluator identity, FunctionX<String, String> resolver) {
        this(identity, resolver, asset -> false);
    }

    /**
     * Creates the adapter with an application decision about assets requiring service identity.
     *
     * @param identity         shared service identity evaluator
     * @param resolver         trusted application code directory
     * @param requiresIdentity application decision to require inbound service verification
     */
    public IdentityGuard(GuardEvaluator identity, FunctionX<String, String> resolver,
            PredicateX<Assets> requiresIdentity) {
        this(identity, resolver, requiresIdentity, (asset, caller) -> {
        });
    }

    /**
     * Creates the adapter with an application admission policy applied after service identity evaluation.
     *
     * @param identity         shared service identity evaluator
     * @param resolver         trusted application code directory
     * @param requiresIdentity application decision to require inbound service verification
     * @param admission        application policy called once per resolved asset with the verified identity or null
     */
    public IdentityGuard(GuardEvaluator identity, FunctionX<String, String> resolver,
            PredicateX<Assets> requiresIdentity, BiConsumerX<Assets, GuardEvaluator.Verified> admission) {
        this(identity, resolver, requiresIdentity, admission, null);
    }

    /**
     * Creates the adapter with the deployment's service discovery and configuration route.
     *
     * @param identity         shared service identity evaluator
     * @param resolver         trusted application code directory
     * @param requiresIdentity application decision to require inbound service verification
     * @param admission        application admission policy for resolved assets
     * @param registrationPath configured SDC route, or {@code null} when disabled
     */
    public IdentityGuard(GuardEvaluator identity, FunctionX<String, String> resolver,
            PredicateX<Assets> requiresIdentity, BiConsumerX<Assets, GuardEvaluator.Verified> admission,
            String registrationPath) {
        if (registrationPath != null) {
            Args.validateSdcPath(registrationPath);
        }
        this.registrationPath = registrationPath;
        this.identity = Objects.requireNonNull(identity);
        this.resolver = Objects.requireNonNull(resolver);
        this.requiresIdentity = Objects.requireNonNull(requiresIdentity);
        this.admission = Objects.requireNonNull(admission);
    }

    /**
     * Removes the external credential from the request and retains it only when inbound verification is enabled.
     *
     * @param exchange actual ingress exchange
     * @return exchange with the reserved header removed, or the original exchange when no header was supplied
     */
    public ServerWebExchange ingress(ServerWebExchange exchange) {
        var headers = exchange.getRequest().getHeaders();
        if (!headers.containsHeader(Http.Auth.SERVICE_TOKEN_HEADER))
            return exchange;
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        var received = identity.receive(path, fixed(path), true, headers::get);
        if (!received.values().isEmpty())
            exchange.getAttributes().put(TOKENS, received.values());
        return exchange.mutate().request(
                request -> request
                        .headers(mutable -> identity.clearHeaders(null, mutable.headerNames(), mutable::remove)))
                .build();
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
     * Evaluates service identity after route resolution, then applies control-channel and asset admission before
     * business authorization. These admission rules also apply when token verification is disabled. A route miss still
     * obeys the ingress identity rules and has no asset to admit.
     *
     * @param exchange sanitized gateway exchange
     * @param assets   selected runtime asset, or null for an unsuccessful lookup
     * @throws ResponseStatusException if identity verification fails, the policy is unavailable, or the selected asset
     *                                 is incompatible with the ingress route, or application admission rejects it
     */
    public void qualified(ServerWebExchange exchange, Assets assets) {
        verifyRouteIdentity(exchange, assets);
        if (assets != null) {
            GuardEvaluator.Verified caller = verified(exchange);
            if (caller == null && (Integer.valueOf(Args.ROUTE_SDC).equals(assets.getProtocol())
                    || requiresIdentity.test(assets))) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
            admission.accept(assets, caller);
        }
    }

    /**
     * Verifies service identity under the effective route policy and publishes the verified caller to the exchange.
     * Protocol and asset compatibility is enforced even when inbound verification is disabled.
     *
     * @param exchange sanitized gateway exchange
     * @param assets   resolved asset, or null for a route miss
     */
    private void verifyRouteIdentity(ServerWebExchange exchange, Assets assets) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        boolean control = Args.isSdcRequest(path, registrationPath);
        boolean rest = Args.isRestRequest(path);
        boolean sdcAsset = assets != null && Integer.valueOf(Args.ROUTE_SDC).equals(assets.getProtocol());
        if (!identity.inboundEnabled()) {
            exchange.getAttributes().put(InboundDecision.ATTRIBUTE, identity.accept(path, List.of(), null, null));
            if (assets != null && control != sdcAsset)
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            return;
        }
        List<String> tokens = takeTokens(exchange);
        boolean required = assets != null && requiresIdentity.test(assets);
        if (!Args.isMcpRequest(path) && (!rest || assets == null) || required && !rest) {
            GuardEvaluator.Asset policy = control && sdcAsset || required
                    ? policy(assets, true, identity.outboundEnabled())
                    : null;
            verify(exchange, path, tokens, policy, required);
            if (control && sdcAsset && (policy == null || !policy.requiresRestrictedCaller()))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (assets == null)
            return;
        if (control != sdcAsset)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (rest) {
            GuardEvaluator.Asset policy = policy(assets, true, identity.outboundEnabled());
            if (identity.outboundEnabled() && policy != null)
                exchange.getAttributes().put(ASSET, policy);
            verify(exchange, path, tokens, policy, required);
        }
    }

    /**
     * Reads the registered asset policy and maps malformed configuration to an HTTP service error.
     *
     * @param assets   selected runtime asset
     * @param inbound  whether to read inbound identity metadata
     * @param outbound whether to read outbound identity metadata
     * @return declared service identity policy, or {@code null} when absent
     * @throws ResponseStatusException if the configured policy is invalid
     */
    private static GuardEvaluator.Asset policy(Assets assets, boolean inbound, boolean outbound) {
        try {
            return !inbound && !outbound ? null
                    : GuardEvaluator.asset(MetadataCodec.serviceIdentity(assets.getMetadata()), inbound, outbound);
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
     * @param required whether application policy requires a verified caller
     * @throws ResponseStatusException if authentication is rejected or a required direction is unavailable
     */
    private void verify(
            ServerWebExchange exchange,
            String path,
            List<String> tokens,
            GuardEvaluator.Asset asset,
            boolean required) {
        var result = identity.accept(path, tokens, fixed(path), asset, required);
        switch (result.status()) {
            case REJECTED -> throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            case UNAVAILABLE -> throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
            case ACCEPTED -> exchange.getAttributes().put(InboundDecision.ATTRIBUTE, result);
        }
    }

    /**
     * Reads the optional caller from the Auth decision for this request.
     *
     * @param exchange current exchange
     * @return verified caller, or null if the decision contains none
     */
    private static GuardEvaluator.Verified verified(ServerWebExchange exchange) {
        InboundDecision decision = exchange.getAttribute(InboundDecision.ATTRIBUTE);
        return decision == null ? null : decision.verified();
    }

    /**
     * Applies the Auth forwarding decision at the gateway's outbound stage.
     *
     * @param exchange qualified request
     * @param context  gateway routing context
     * @return exchange carrying the next-hop credential if required
     * @throws ResponseStatusException if a required caller or next-hop credential is unavailable
     */
    public ServerWebExchange outbound(ServerWebExchange exchange, Context context) {
        String route = exchange.getRequest().getPath().pathWithinApplication().value();
        Assets asset = context.getAssets();
        boolean rest = Args.isRestRequest(route);
        var decision = identity.forward(
                route,
                fixed(route),
                asset != null,
                rest,
                () -> !identity.inboundEnabled() && rest && asset != null ? policy(asset, false, true)
                        : exchange.getAttribute(ASSET),
                () -> target(exchange, asset),
                verified(exchange),
                asset == null ? null : asset.getId());
        return switch (decision.status()) {
            case READY -> forward(exchange, context, decision.token());
            case REJECTED -> throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            case UNAVAILABLE -> throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE);
        };
    }

    /**
     * Resolves the gateway's authorized target path and the registered service that owns it.
     *
     * @param exchange qualified request
     * @param asset    resolved route asset
     * @return target coordinates for Auth to evaluate
     */
    private Target target(ServerWebExchange exchange, Assets asset) {
        SlugRouteMatcher.Match slug = exchange.getAttribute(SlugRouteMatcher.MATCH_ATTRIBUTE);
        URI destination = slug != null ? SlugRouteMatcher.operation(slug, exchange.getRequest().getPath().value())
                : asset == null || asset.getUrl() == null ? null : URI.create(asset.getUrl());
        String audience = null;
        if (destination != null && !destination.isAbsolute() && destination.getRawAuthority() == null
                && asset.getApp_id() != null && asset.getInstances() != null && asset.getInstances().size() == 1
                && asset.getApp_id().equals(asset.getInstances().getFirst().getApp_id())) {
            audience = resolver.apply(asset.getApp_id());
        }
        return new Target(audience, destination == null ? null : destination.getPath());
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
    private Mode fixed(String path) {
        if (Args.isSdcRequest(path, registrationPath))
            return Mode.REQUIRED;
        return Args.isMcpRequest(path) ? Mode.OFF : null;
    }

}
