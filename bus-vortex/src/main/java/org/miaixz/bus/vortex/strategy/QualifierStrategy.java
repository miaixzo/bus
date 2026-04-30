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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * A strategy that qualifies incoming requests by validating API metadata and orchestrating authorization.
 * <p>
 * This class acts as a crucial gatekeeper in the request pipeline. It performs initial validation and then delegates
 * credential-specific checks. Its primary responsibilities are:
 * <ul>
 * <li>Looking up the API asset configuration based on request parameters.</li>
 * <li>Validating the HTTP method against the asset's definition.</li>
 * <li>Establishing the credential discovery order (e.g., Bearer Token first, then API Key).</li>
 * <li>Encapsulating the found credential into a {@link Principal} object.</li>
 * <li>Delegating the core validation logic to a pluggable {@link AuthorizeProvider}.</li>
 * </ul>
 * It does not contain any specific validation logic itself, making it a stable part of the framework's core.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class QualifierStrategy extends AbstractStrategy {

    /**
     * The provider responsible for validating credentials and fetching permissions.
     */
    private final AuthorizeProvider provider;

    /**
     * The registry containing configuration for all available API assets.
     */
    private final AssetsRegistry registry;

    /**
     * Constructs a new {@code QualiferStrategy} with the specified provider and registry.
     *
     * @param provider The {@link AuthorizeProvider} used for credential validation.
     * @param registry The {@link AssetsRegistry} used to retrieve API asset configurations.
     */
    public QualifierStrategy(AuthorizeProvider provider, AssetsRegistry registry) {
        this.provider = provider;
        this.registry = registry;
    }

    /**
     * Executes the full qualification and authorization workflow for the incoming request.
     * <p>
     * This method orchestrates the following steps:
     * <ol>
     * <li>Extracts key gateway parameters (e.g., format, channel, namespace, method, version) from the request and
     * populates the {@link Context}.</li>
     * <li>Retrieves the {@link Assets} configuration from the registry using namespace, method, version, and verb.</li>
     * <li>Sets the resolved {@link Assets} in the context for downstream use.</li>
     * <li>Validates that the request's HTTP method matches the one defined in the assets.</li>
     * <li>If the API asset is protected, it invokes the authorization workflow.</li>
     * <li>Removes internal gateway parameters (e.g., namespace, method, version, format, sign) before passing the
     * request to the next strategy.</li>
     * </ol>
     *
     * @param exchange The {@link ServerWebExchange} representing the current request and response.
     * @param chain    The {@link Chain} to invoke the next strategy in the chain.
     * @return A {@link Mono<Void>} signaling the completion of this strategy's processing.
     * @throws ValidateException If the API asset is not found ({@link ErrorCode#_100800}), or if the request's HTTP
     *                           method does not match the one configured for the asset.
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, Chain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);
            Map<String, Object> params = context.getParameters();

            context.setFormat(
                    Formats.get(Optional.ofNullable(params.get(Args.FORMAT)).map(Object::toString).orElse(null)));
            context.setChannel(
                    Channel.get(
                            Optional.ofNullable(params.get(Args.X_REMOTE_CHANNEL)).map(Object::toString).orElse(null)));

            String namespace = Optional.ofNullable(params.get(Args.NAMESPACE)).map(Object::toString).orElse(null);
            String appId = Optional.ofNullable(params.get(Args.APP_ID)).map(Object::toString)
                    .orElseGet(() -> exchange.getRequest().getHeaders().getFirst(Args.X_APP_ID));
            String method = Optional.ofNullable(params.get(Args.METHOD)).map(Object::toString).orElse(null);
            Type type = this.type(Optional.ofNullable(params.get(Args.TYPE)).map(Object::toString).orElse(null));
            String version = Optional.ofNullable(params.get(Args.VERSION)).map(Object::toString).orElse(null);
            Integer verb = context.getHttpMethod() == null ? null : context.getHttpMethod().verb();
            Keying.RegistrySpec requestRoute = Keying.RegistrySpec.route(namespace, type, appId, method, version, verb);

            return Mono.fromCallable(() -> this.registry.get(requestRoute)).subscribeOn(Schedulers.boundedElastic())
                    .switchIfEmpty(Mono.defer(() -> {
                        Logger.warn(
                                false,
                                "Vortex",
                                "strategy=qualifier, clientIp={}, assets not found: namespace={}, type={}, appId={}, method={}, version={}, verb={}",
                                context.getX_request_ip(),
                                namespace,
                                type == null ? null : type.key(),
                                appId,
                                method,
                                version,
                                verb);
                        return Mono.error(new ValidateException(ErrorCode._100800));
                    })).flatMap(assets -> {
                        context.setAssets(assets);
                        Keying.RegistrySpec resolvedRoute = Keying.RegistrySpec.route(assets);
                        String matchedRoute = null;
                        Integer matchedLevel = null;
                        List<String> requestedRoutes = this.registry.keying().keys(requestRoute);
                        List<String> resolvedRoutes = this.registry.keying().keys(resolvedRoute);
                        for (int i = 0; i < requestedRoutes.size(); i++) {
                            String requested = requestedRoutes.get(i);
                            if (resolvedRoutes.contains(requested)) {
                                matchedRoute = requested;
                                matchedLevel = i + 1;
                                break;
                            }
                        }
                        Logger.info(
                                true,
                                "Vortex",
                                "strategy=qualifier, clientIp={}, assets resolved: namespace={}, type={}, appId={}, method={}, version={}, verb={}, matchedLevel={}, matchedRoute={}, policy={}, sign={}, mode={}, host={}, port={}, path={}, url={}",
                                context.getX_request_ip(),
                                assets.getNamespace_id(),
                                assets.getType(),
                                assets.getApp_id(),
                                assets.getMethod(),
                                assets.getVersion(),
                                assets.getVerb(),
                                matchedLevel,
                                matchedRoute,
                                assets.getPolicy(),
                                assets.getSign(),
                                assets.getProtocol(),
                                assets.getHost(),
                                assets.getPort(),
                                assets.getPath(),
                                assets.getUrl());

                        Mono<Void> validationMono = this.method(exchange, context, assets);

                        Mono<Void> authMono = (Consts.ZERO != assets.getPolicy()) ? this.authorize(context)
                                : Mono.empty();

                        return validationMono.then(authMono);
                    }).then(Mono.fromRunnable(() -> {
                        context.getParameters().remove(Args.NAMESPACE);
                        context.getParameters().remove(Args.APP_ID);
                        context.getParameters().remove(Args.METHOD);
                        context.getParameters().remove(Args.FORMAT);
                        context.getParameters().remove(Args.TYPE);
                        context.getParameters().remove(Args.VERSION);
                        context.getParameters().remove(Args.SIGN);
                        Logger.info(
                                true,
                                "Vortex",
                                "strategy=qualifier, clientIp={}, qualifier validation completed: namespace={}, type={}, appId={}, method={}, version={}",
                                context.getX_request_ip(),
                                namespace,
                                type == null ? null : type.key(),
                                appId,
                                method,
                                version);
                    })).then(chain.apply(exchange));
        });
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
     * Validates whether the request's HTTP method matches the expected method defined in the API asset configuration.
     *
     * @param exchange The {@link ServerWebExchange} containing the current request.
     * @param context  The request context (for logging).
     * @param assets   The {@link Assets} configuration for the requested API.
     * @return A {@link Mono<Void>} that completes if valid, or signals an error if mismatched.
     */
    protected Mono<Void> method(ServerWebExchange exchange, Context context, Assets assets) {
        return Mono.fromRunnable(() -> {
            final HTTP.Method actualMethod = context.getHttpMethod();
            final HTTP.Method expectedMethod = this.methodOf(assets.getVerb());

            if (!Objects.equals(actualMethod, expectedMethod)) {
                Logger.warn(
                        false,
                        "Vortex",
                        "strategy=qualifier, clientIp={}, HTTP method mismatch: expected={}, actual={}",
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
     * Orchestrates the authorization process by finding credentials and delegating validation to the
     * {@link AuthorizeProvider}.
     * <p>
     * This method enforces the access control policy defined in the {@link Assets} configuration. The policy determines
     * which credential types are accepted and the validation level required.
     *
     * @param context The request context.
     * @return A {@link Mono<Void>} that completes on success, or signals an error on failure.
     */
    protected Mono<Void> authorize(Context context) {
        final Integer policy = context.getAssets().getPolicy();

        if (policy == null || policy < Consts.ZERO || policy > Consts.SIX) {
            Logger.error(
                    false,
                    "Vortex",
                    "strategy=qualifier, clientIp={}, invalid policy value: policy={}, allowedRange=0..6",
                    context.getX_request_ip(),
                    policy);
            return Mono.error(new ValidateException(ErrorCode._116002));
        }

        if (Consts.ZERO.equals(policy)) {
            Logger.info(
                    true,
                    "Vortex",
                    "strategy=qualifier, clientIp={}, anonymous access granted",
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
                        "strategy=qualifier, clientIp={}, bearer credential selected: policy={}",
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
                        "strategy=qualifier, clientIp={}, API key credential selected: policy={}",
                        context.getX_request_ip(),
                        policy);
            }
        }

        if (credentialValue == null) {
            Logger.warn(
                    false,
                    "Vortex",
                    "strategy=qualifier, clientIp={}, required credential missing: policy={}",
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
                                "strategy=qualifier, clientIp={}, credential attributes converted: attributeCount={}",
                                context.getX_request_ip(),
                                authMap.size());
                        Map<String, Object> nonNullAuthMap = authMap.entrySet().stream()
                                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                        context.getParameters().putAll(nonNullAuthMap);
                        Logger.info(
                                true,
                                "Vortex",
                                "strategy=qualifier, clientIp={}, authentication completed: policy={}",
                                context.getX_request_ip(),
                                policy);
                        return Mono.empty();
                    }

                    var message = delegate.getMessage();
                    Logger.error(
                            false,
                            "Vortex",
                            "strategy=qualifier, clientIp={}, authentication failed: policy={}, errorCode={}, message={}",
                            context.getX_request_ip(),
                            policy,
                            message.errcode,
                            message.errmsg);
                    return Mono.error(new ValidateException(message.errcode, message.errmsg));
                });
    }

}
