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
import java.util.stream.Collectors;

import org.springframework.web.server.ServerWebExchange;

import org.miaixz.bus.core.Order;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.bean.copier.CopyOptions;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Args;
import org.miaixz.bus.vortex.Channel;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.Formats;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.magic.Principal;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;

import reactor.core.publisher.Mono;

/**
 * Basic qualifier strategy for routes without protocol-specific qualification rules.
 * <p>
 * This strategy owns the common asset-route, HTTP-method, and authorization rules used by CST/MQ/gRPC/WebSocket style
 * routes. Protocol-specific strategies can extend it and override {@link #apply(ServerWebExchange, Chain)} when they
 * need custom asset matching.
 *
 * @author Kimi Liu
 * @since Java 21+
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
     * Creates a qualifier support instance.
     *
     * @param provider credential validation provider
     * @param registry asset registry
     */
    public QualifierStrategy(AuthorizeProvider provider, AssetsRegistry registry) {
        this.provider = provider;
        this.registry = registry;
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
                        "Asset not found: strategy=qualifier, clientIp={}, namespace={}, type={}, appId={}, method={}, version={}, verb={}",
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
                Logger.info(
                        true,
                        "Vortex",
                        "Asset resolved: strategy=qualifier, clientIp={}, namespace={}, type={}, appId={}, method={}, version={}, verb={}, policy={}, sign={}, mode={}, host={}, port={}, path={}, url={}",
                        context.getX_request_ip(),
                        assets.getNamespace_id(),
                        assets.getType(),
                        assets.getApp_id(),
                        assets.getMethod(),
                        assets.getVersion(),
                        assets.getVerb(),
                        assets.getPolicy(),
                        assets.getSign(),
                        assets.getProtocol(),
                        assets.getHost(),
                        assets.getPort(),
                        assets.getPath(),
                        assets.getUrl());
                Mono<Void> validationMono = this.method(context, assets);
                Mono<Void> authMono = !Consts.ZERO.equals(assets.getPolicy()) ? this.authorize(context) : Mono.empty();
                return validationMono.then(authMono).then(finalizeParameters(exchange, context));
            }).then(
                    Mono.fromRunnable(
                            () -> Logger.info(
                                    true,
                                    "Vortex",
                                    "Qualifier completed: strategy=qualifier, clientIp={}, namespace={}, type={}, appId={}, method={}, version={}",
                                    context.getX_request_ip(),
                                    namespace,
                                    type,
                                    appId,
                                    method,
                                    version)))
                    .then(chain.apply(exchange));
        });
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

        String namespace = this.value(context, Args.NAMESPACE);
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
        return Keying.RegistrySpec.route(namespace, type, appId, method, version, verb);
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
            final HTTP.Method actualMethod = context.getHttpMethod();
            final HTTP.Method expectedMethod = this.methodOf(assets.getVerb());

            if (!Objects.equals(actualMethod, expectedMethod)) {
                Logger.warn(
                        false,
                        "Vortex",
                        "HTTP method mismatch: strategy=qualifier, clientIp={}, expected={}, actual={}",
                        context.getX_request_ip(),
                        expectedMethod,
                        actualMethod);

                final Errors error = switch (expectedMethod.value()) {
                    case HTTP.GET -> ErrorCode._100200;
                    case HTTP.POST -> ErrorCode._100201;
                    case HTTP.PUT -> ErrorCode._100202;
                    case HTTP.DELETE -> ErrorCode._100203;
                    case HTTP.OPTIONS -> ErrorCode._100204;
                    case HTTP.HEAD -> ErrorCode._100205;
                    case HTTP.PATCH -> ErrorCode._100206;
                    case HTTP.TRACE -> ErrorCode._100207;
                    default -> ErrorCode._100802;
                };
                throw new ValidateException(error);
            }
        });
    }

    /**
     * Performs policy-based authorization for the resolved asset.
     * <p>
     * Policy values decide whether bearer tokens or API keys are accepted. Successful authorization attributes are
     * copied into the request parameters for downstream executors.
     *
     * @param context current request context
     * @return completion signal
     */
    protected Mono<Void> authorize(Context context) {
        final Integer policy = context.getAssets().getPolicy();

        if (policy == null || policy < Consts.ZERO || policy > Consts.SIX) {
            Logger.error(
                    false,
                    "Vortex",
                    "Invalid policy value: strategy=qualifier, clientIp={}, policy={}, allowedRange=0..6",
                    context.getX_request_ip(),
                    policy);
            return Mono.error(new ValidateException(ErrorCode._116002));
        }

        if (Consts.ZERO.equals(policy)) {
            Logger.info(
                    true,
                    "Vortex",
                    "Anonymous access granted: strategy=qualifier, clientIp={}",
                    context.getX_request_ip());
            return Mono.empty();
        }

        final boolean acceptToken = Consts.ONE.equals(policy) || Consts.TWO.equals(policy)
                || Consts.THREE.equals(policy);
        final boolean acceptApiKey = Consts.FOUR.equals(policy) || Consts.FIVE.equals(policy)
                || Consts.SIX.equals(policy);

        String credentialValue = null;

        if (acceptToken) {
            credentialValue = this.getToken(context);
            if (StringKit.isNotBlank(credentialValue)) {
                context.setBearer(credentialValue);
                Logger.info(
                        true,
                        "Vortex",
                        "Bearer credential selected: strategy=qualifier, clientIp={}, policy={}",
                        context.getX_request_ip(),
                        policy);
            }
        }

        if (acceptApiKey) {
            credentialValue = this.getApiKey(context);
            if (StringKit.isNotBlank(credentialValue)) {
                Logger.info(
                        true,
                        "Vortex",
                        "API key credential selected: strategy=qualifier, clientIp={}, policy={}",
                        context.getX_request_ip(),
                        policy);
            }
        }

        if (credentialValue == null) {
            Logger.warn(
                    false,
                    "Vortex",
                    "Required credential missing: strategy=qualifier, clientIp={}, policy={}",
                    context.getX_request_ip(),
                    policy);
            return Mono.error(new ValidateException(ErrorCode._116002));
        }

        return this.provider.authorize(
                Principal.builder().channel(context.getChannel().getType()).context(context).type(policy)
                        .value(credentialValue).build())
                .flatMap(delegate -> {
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
                                "Authentication completed: strategy=qualifier, clientIp={}, policy={}",
                                context.getX_request_ip(),
                                policy);
                        return Mono.empty();
                    }

                    var message = delegate.getMessage();
                    Logger.error(
                            false,
                            "Vortex",
                            "Authentication failed: strategy=qualifier, clientIp={}, policy={}, errorCode={}, message={}",
                            context.getX_request_ip(),
                            policy,
                            message.errcode,
                            message.errmsg);
                    return Mono.error(new ValidateException(message.errcode, message.errmsg));
                });
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

}
