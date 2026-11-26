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
package org.miaixz.bus.vortex.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.auth.guard.GuardEvaluator;
import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.bean.copier.CopyOptions;
import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.errors.transport.http.RouteFailure;
import org.miaixz.bus.errors.transport.reactive.ReactiveErrorHandler;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Channel;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Formats;
import org.miaixz.bus.vortex.guard.IdentityGuard;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.magic.Principal;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;

import reactor.core.publisher.Mono;

/**
 * Resolves route assets and applies shared service identity, HTTP verb and business authorization rules. A strategy
 * with custom asset resolution calls {@link #validateIdentity} before applying protocol-specific stages.
 *
 * @author Kimi Liu
 */
@org.springframework.core.annotation.Order(Order.THIRD)
public class QualifierStrategy extends AbstractStrategy {

    /**
     * Provider used to validate route credentials and load authorization attributes.
     */
    protected final AuthorizeProvider provider;

    /**
     * Registry used by qualifier strategies to resolve route assets.
     */
    protected final AssetsRegistry registry;

    /**
     * Evaluates service identity after the actual runtime asset has been resolved.
     */
    private final IdentityGuard identities;

    /**
     * Creates a qualifier support instance.
     *
     * @param provider credential validation provider
     * @param registry asset registry
     */
    public QualifierStrategy(AuthorizeProvider provider, AssetsRegistry registry) {
        this(provider, registry, new IdentityGuard(GuardEvaluator.disabled(), appId -> null));
    }

    /**
     * Creates a qualifier with the gateway's service identity adapter.
     *
     * @param provider   credential validation provider
     * @param registry   runtime asset registry
     * @param identities shared service identity adapter
     */
    public QualifierStrategy(AuthorizeProvider provider, AssetsRegistry registry, IdentityGuard identities) {
        this.provider = provider;
        this.registry = registry;
        this.identities = identities;
    }

    /**
     * Resolves a generic route asset, validates its HTTP verb, and performs policy authorization.
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
            String type = requestRoute.typeKeyPart();
            String appId = requestRoute.appIdPart();
            String method = requestRoute.methodPart();
            String version = requestRoute.versionPart();
            Integer verb = requestRoute.verbPart();

            return resolve(exchange, requestRoute).switchIfEmpty(Mono.defer(() -> {
                Logger.warn(
                        false,
                        "Vortex",
                        "Asset not found: strategy=qualifier, clientIp={}, type={}, appId={}, method={}, version={}, verb={}",
                        context.getX_request_ip(),
                        type,
                        appId,
                        method,
                        version,
                        verb);
                return Mono.error(routeMiss(exchange, requestRoute, missingRoute()));
            })).flatMap(match -> {
                Assets assets = match.assets();
                context.setAssets(assets);
                context.setRemainingPath(match.remainingPath());
                Logger.info(
                        true,
                        "Vortex",
                        "Asset resolved: strategy=qualifier, clientIp={}, type={}, appId={}, method={}, version={}, verb={}, policy={}, signing={}, mode={}, instances={}, url={}",
                        context.getX_request_ip(),
                        assets.getType(),
                        assets.getApp_id(),
                        assets.getMethod(),
                        assets.getVersion(),
                        assets.getVerb(),
                        assets.getPolicy(),
                        assets.getSigning(),
                        assets.getProtocol(),
                        assets.getInstances() == null ? Normal._0 : assets.getInstances().size(),
                        assets.getUrl());
                Mono<Void> validationMono = validateOperation(context, assets);
                Mono<Void> authMono = !Consts.ZERO.equals(assets.getPolicy())
                        ? Mono.defer(() -> this.authorize(context))
                        : Mono.empty();
                return validationMono.then(authMono).then(finalizeParameters(exchange, context));
            }).then(
                    Mono.fromRunnable(
                            () -> Logger.info(
                                    true,
                                    "Vortex",
                                    "Qualifier completed: strategy=qualifier, clientIp={}, type={}, appId={}, method={}, version={}",
                                    context.getX_request_ip(),
                                    type,
                                    appId,
                                    method,
                                    version)))
                    .then(chain.apply(exchange));
        });
    }

    /**
     * Resolves one registry asset and evaluates service identity before exposing the lookup result.
     *
     * @param exchange sanitized request exchange
     * @param route    requested asset identity
     * @return matched asset and remainder, or an empty result for a miss
     */
    protected final Mono<AssetsRegistry.RouteMatch> resolve(ServerWebExchange exchange, Keying.RegistrySpec route) {
        return Mono.defer(() -> {
            AssetsRegistry.RouteMatch match;
            try {
                match = findRoute(exchange, route);
            } catch (RuntimeException unavailable) {
                if (unavailable instanceof ResponseStatusException
                        || Args.isRestRequest(exchange.getRequest().getPath().pathWithinApplication().value())) {
                    validateIdentity(exchange, null);
                }
                throw unavailable;
            }
            validateIdentity(exchange, match == null ? null : match.assets());
            return Mono.justOrEmpty(match);
        });
    }

    /**
     * Resolves a route asset from the runtime registry.
     *
     * @param exchange current gateway exchange
     * @param route    structured route key
     * @return matching route, or {@code null} when no route is available
     */
    protected AssetsRegistry.RouteMatch findRoute(ServerWebExchange exchange, Keying.RegistrySpec route) {
        return registry.get(route);
    }

    /**
     * Applies the shared service identity policy to the result of asset resolution.
     *
     * @param exchange sanitized request exchange
     * @param assets   selected runtime asset, or null for a lookup miss
     */
    protected final void validateIdentity(ServerWebExchange exchange, Assets assets) {
        identities.qualified(exchange, assets);
    }

    /**
     * Validates the operation and its protocol signature before business authorization.
     *
     * @param context current request context
     * @param assets  selected runtime asset
     * @return completion of HTTP verb and protocol signature validation
     */
    protected final Mono<Void> validateOperation(Context context, Assets assets) {
        return method(context, assets).then(Mono.defer(() -> signature(context, assets)));
    }

    /**
     * Applies the signature convention specific to this protocol, when it uses one.
     *
     * @param context qualified request context
     * @param assets  selected runtime asset
     * @return signature validation completion
     */
    protected Mono<Void> signature(Context context, Assets assets) {
        return Mono.empty();
    }

    /**
     * Provides the legacy error code for an unsuccessful route lookup.
     *
     * @return route lookup error for this protocol
     */
    protected Errors missingRoute() {
        return ErrorCode._100800;
    }

    /**
     * Builds the runtime route specification from gateway parameters and query/header fallbacks.
     * <p>
     * REST and MCP share the same registry dimensions, so both concrete qualifiers use this method before asset lookup.
     *
     * @param exchange current web exchange
     * @param context  current request context
     * @return runtime route specification
     */
    protected Keying.RegistrySpec route(ServerWebExchange exchange, Context context) {
        context.setFormat(Formats.get(this.value(context, Args.FORMAT)));
        context.setChannel(Channel.get(this.value(context, Args.X_REMOTE_CHANNEL)));

        String appId = this.value(context, Args.APP_ID);
        if (StringKit.isBlank(appId)) {
            appId = exchange.getRequest().getHeaders().getFirst(Args.APP_ID);
        }
        Type type = this.type(this.value(context, Args.TYPE));
        String method = this.value(context, Args.METHOD);
        if (StringKit.isBlank(method)) {
            method = exchange.getRequest().getURI().getPath();
        }
        String version = this.version(exchange, context);
        Integer verb = context.getHttpMethod() == null ? null : context.getHttpMethod().verb();
        return Keying.RegistrySpec.route(null, type, appId, method, version, verb);
    }

    /**
     * Supplies route-specific status and headers for a confirmed lookup miss. The original validation code remains
     * available to legacy clients.
     *
     * @param exchange gateway exchange with its selected policy
     * @param route    exact request coordinates used for lookup
     * @param legacy   original lookup failure code
     * @return routing failure or the original validation failure for an incomplete lookup
     */
    protected ValidateException routeMiss(ServerWebExchange exchange, Keying.RegistrySpec route, Errors legacy) {
        if (!route.routable()) {
            return new ValidateException(legacy);
        }
        if (ReactiveErrorHandler.needsRouteMethodFacts(exchange)) {
            Set<HttpMethod> methods = this.registry.publicMethods(route);
            if (!methods.isEmpty()) {
                HttpHeaders headers = new HttpHeaders();
                headers.setAllow(methods);
                return new RouteFailure(legacy, HttpStatus.METHOD_NOT_ALLOWED, headers);
            }
        }
        return new RouteFailure(legacy, HttpStatus.NOT_FOUND, HttpHeaders.EMPTY);
    }

    /**
     * Resolves the route version for asset lookup.
     *
     * @param exchange current web exchange
     * @param context  current request context
     * @return resolved route version, or {@code null}
     */
    protected String version(ServerWebExchange exchange, Context context) {
        return this.value(context, Args.VERSION);
    }

    /**
     * Resolves one optional request type token from either the numeric type key or the historical enum name.
     *
     * @param token raw request token
     * @return resolved registry type or {@code null}
     */
    protected Type type(String token) {
        if (StringKit.isBlank(token)) {
            return null;
        }
        Type resolved = Type.tryFrom(token).orElseThrow(() -> new ValidateException(ErrorCode._100800));
        if (!resolved.isRegistry()) {
            throw new ValidateException(ErrorCode._100800);
        }
        return resolved;
    }

    /**
     * Validates whether the request's HTTP method matches the route asset definition.
     *
     * @param context request context used for logging
     * @param assets  resolved route asset
     * @return completion signal
     */
    protected Mono<Void> method(Context context, Assets assets) {
        return Mono.fromRunnable(() -> {
            final Http.Method actualMethod = context.getHttpMethod();
            final Http.Method expectedMethod = this.methodOf(assets.getVerb());

            if (!Objects.equals(actualMethod, expectedMethod)) {
                Logger.warn(
                        false,
                        "Vortex",
                        "HTTP method mismatch: strategy=qualifier, clientIp={}, expected={}, actual={}",
                        context.getX_request_ip(),
                        expectedMethod,
                        actualMethod);

                final Errors error = switch (expectedMethod) {
                    case GET -> ErrorCode._100200;
                    case POST -> ErrorCode._100201;
                    case PUT -> ErrorCode._100202;
                    case DELETE -> ErrorCode._100203;
                    case OPTIONS -> ErrorCode._100204;
                    case HEAD -> ErrorCode._100205;
                    case PATCH -> ErrorCode._100206;
                    case TRACE -> ErrorCode._100207;
                    default -> ErrorCode._100802;
                };
                throw new ValidateException(error);
            }
        });
    }

    /**
     * Performs policy-based authorization for the resolved asset.
     * <p>
     * Policy values 1-3 enable authorization. The request credential type is selected from the request: bearer token
     * first, API key fallback. Successful authorization attributes are copied into the request parameters for
     * downstream executors.
     *
     * @param context current request context
     * @return completion signal
     */
    protected Mono<Void> authorize(Context context) {
        final Integer policy = context.getAssets().getPolicy();

        if (policy == null || policy < Consts.ZERO || policy > Consts.THREE) {
            Logger.error(
                    false,
                    "Vortex",
                    "Invalid policy value: strategy=qualifier, clientIp={}, policy={}, allowedRange=0..3",
                    context.getX_request_ip(),
                    policy);
            return Mono.error(new ValidateException(ErrorCode._100160));
        }

        if (Consts.ZERO.equals(policy)) {
            Logger.info(
                    true,
                    "Vortex",
                    "Anonymous access granted: strategy=qualifier, clientIp={}",
                    context.getX_request_ip());
            return Mono.empty();
        }

        Credential credential = this.selectCredential(context, policy);
        if (credential == null) {
            Logger.warn(
                    false,
                    "Vortex",
                    "Required credential missing: strategy=qualifier, clientIp={}, policy={}",
                    context.getX_request_ip(),
                    policy);
            return Mono.error(new ValidateException(ErrorCode._100160));
        }

        return this.provider
                .authorize(
                        Principal.builder().channel(context.getChannel().getType()).context(context)
                                .type(credential.type()).value(credential.value()).build())
                .switchIfEmpty(Mono.error(new ValidateException(ErrorCode._100160))).flatMap(delegate -> {
                    if (delegate.isOk()) {
                        Map<String, Object> authMap = new HashMap<>();
                        BeanKit.beanToMap(
                                delegate.getAuthorize(),
                                authMap,
                                CopyOptions.of().setTransientSupport(false).setIgnoreCase(true)
                                        .setIgnoreProperties("id"));
                        Logger.info(
                                true,
                                "Vortex",
                                "Credential attributes converted: strategy=qualifier, clientIp={}, attributeCount={}",
                                context.getX_request_ip(),
                                authMap.size());
                        Map<String, Object> nonNullAuthMap = authMap.entrySet().stream()
                                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                        context.getParameters().putAll(nonNullAuthMap);
                        Logger.info(
                                true,
                                "Vortex",
                                "Authentication completed: strategy=qualifier, clientIp={}, policy={}, credential={}",
                                context.getX_request_ip(),
                                policy,
                                credential.name());
                        return Mono.empty();
                    }

                    var message = delegate.getMessage();
                    if (message == null) {
                        return Mono.error(new ValidateException(ErrorCode._100160));
                    }
                    Logger.error(
                            false,
                            "Vortex",
                            "Authentication failed: strategy=qualifier, clientIp={}, policy={}, credential={}, code={}, desc={}",
                            context.getX_request_ip(),
                            policy,
                            credential.name(),
                            message.code,
                            message.desc);
                    return Mono.error(new ValidateException(message.code, message.desc));
                });
    }

    /**
     * Selects an enabled request credential. An enabled Bearer channel has priority over the API key channel.
     *
     * @param context current request context
     * @param policy  current route policy
     * @return selected credential, or {@code null} when no supported credential is present
     */
    protected Credential selectCredential(Context context, Integer policy) {
        Http.Auth.Credential credential = context.clientCredential();
        if (credential == null) {
            return null;
        }
        boolean token = EnumValue.Credential.TOKEN.equals(credential.type());
        context.setBearer(credential.value());
        String name = token && Http.Auth.BEARER.equalsIgnoreCase(credential.scheme()) ? Http.Auth.BEARER
                : token ? "Token" : "API key";
        return this.selectedCredential(context, policy, token ? Consts.ONE : Consts.TWO, credential, name);
    }

    /**
     * Builds a selected credential and writes the common selection log.
     *
     * @param context    current request context
     * @param policy     current route policy
     * @param type       credential type
     * @param credential resolved request credential
     * @param name       credential display name
     * @return selected credential
     */
    protected Credential selectedCredential(
            Context context,
            Integer policy,
            Integer type,
            Http.Auth.Credential credential,
            String name) {
        Logger.info(
                true,
                "Vortex",
                "{} credential selected: strategy=qualifier, clientIp={}, policy={}, source={}, key={}",
                name,
                context.getX_request_ip(),
                policy,
                credential.source(),
                credential.key());
        return new Credential(type, credential.value(), name);
    }

    /**
     * Adds final downstream parameters and removes gateway-only control parameters.
     * <p>
     * This method must run after route resolution, signature verification, and authorization so gateway parameters stay
     * available for those checks but are not forwarded to the target service.
     *
     * @param exchange current exchange
     * @param context  request context
     * @return completion signal
     */
    protected Mono<Void> finalizeParameters(ServerWebExchange exchange, Context context) {
        return Mono.fromRunnable(() -> {
            enrich(exchange, context);
            removeForwardingControlParameters(context);
        });
    }

    /**
     * Removes gateway control parameters from the downstream-visible parameter map after qualification has finished.
     * <p>
     * Matching is case-insensitive so variants such as {@code Method}, {@code VERSION}, or {@code Sign} are removed as
     * well.
     *
     * @param context request context
     */
    protected void removeForwardingControlParameters(Context context) {
        context.getParameters().keySet().removeIf(Args::isForwardingControlParameter);
    }

    /**
     * Selected request credential.
     *
     * @param type  credential type
     * @param value credential value
     * @param name  credential display name
     * @author Kimi Liu
     */
    protected record Credential(Integer type, String value, String name) {

    }

}
