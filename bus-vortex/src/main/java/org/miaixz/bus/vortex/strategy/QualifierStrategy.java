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
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.extra.json.JsonKit;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
     * <li>Retrieves the {@link Assets} configuration from the registry using the API type, namespace, method, and
     * version.</li>
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
            String method = Optional.ofNullable(params.get(Args.METHOD)).map(Object::toString).orElse(null);
            String version = Optional.ofNullable(params.get(Args.VERSION)).map(Object::toString).orElse(null);

            return Mono.fromCallable(() -> this.registry.get(Type.API, namespace, method, version))
                    .subscribeOn(Schedulers.boundedElastic()).switchIfEmpty(Mono.defer(() -> {
                        Logger.warn(
                                false,
                                "Qualifier",
                                "[{}] Assets not found for namespace: {}, method: {}, version: {}",
                                context.getX_request_ip(),
                                namespace,
                                method,
                                version);
                        return Mono.error(new ValidateException(ErrorCode._100800));
                    })).flatMap(assets -> {
                        context.setAssets(assets);
                        Logger.info(
                                true,
                                "Qualifier",
                                "[{}] Assets resolved: namespace={}, method={}, version={}, policy={}, sign={}, mode={}, type={}, host={}, port={}, path={}, url={}",
                                context.getX_request_ip(),
                                assets.getNamespace_id(),
                                assets.getMethod(),
                                assets.getVersion(),
                                assets.getPolicy(),
                                assets.getSign(),
                                assets.getProtocol(),
                                assets.getVerb(),
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
                        context.getParameters().remove(Args.METHOD);
                        context.getParameters().remove(Args.FORMAT);
                        context.getParameters().remove(Args.VERSION);
                        context.getParameters().remove(Args.SIGN);
                        Logger.info(
                                true,
                                "Qualifier",
                                "[{}] Namespace: {}, Method: {}, Version: {} validated successfully",
                                context.getX_request_ip(),
                                namespace,
                                method,
                                version);
                    })).then(chain.apply(exchange));
        });
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
            ServerHttpRequest request = exchange.getRequest();
            final HttpMethod expectedMethod = this.valueOf(assets.getVerb());

            if (!Objects.equals(request.getMethod(), expectedMethod)) {
                Logger.warn(
                        false,
                        "Qualifier",
                        "[{}] HTTP method mismatch, expected: {}, actual: {}",
                        context.getX_request_ip(),
                        expectedMethod,
                        request.getMethod());

                final Errors error = switch (expectedMethod.name()) {
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
                    "Qualifier",
                    "[{}] Invalid policy value: {}. Must be between 0 and 6.",
                    context.getX_request_ip(),
                    policy);
            return Mono.error(new ValidateException(ErrorCode._116002));
        }

        if (Consts.ZERO.equals(policy)) {
            Logger.info(true, "Qualifier", "[{}] Anonymous access granted.", context.getX_request_ip());
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
                        "Qualifier",
                        "[{}] Using Token (required by policy={}).",
                        context.getX_request_ip(),
                        policy);
            }
        }

        if (acceptApiKey) {
            credentialValue = this.getApiKey(context);
            if (StringKit.isNotBlank(credentialValue)) {
                Logger.info(
                        true,
                        "Qualifier",
                        "[{}] Using API Key (required by policy={}).",
                        context.getX_request_ip(),
                        policy);
            }
        }

        if (credentialValue == null) {
            Logger.warn(
                    false,
                    "Qualifier",
                    "[{}] Required credential not provided for policy={}.",
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
                                "Qualifier",
                                "[{}] Auth map after conversion: {}",
                                context.getX_request_ip(),
                                JsonKit.toJsonString(authMap));
                        Map<String, Object> nonNullAuthMap = authMap.entrySet().stream()
                                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                        context.getParameters().putAll(nonNullAuthMap);
                        Logger.info(
                                true,
                                "Qualifier",
                                "[{}] Authentication successful (policy={}).",
                                context.getX_request_ip(),
                                policy);
                        return Mono.empty();
                    }

                    Logger.error(
                            false,
                            "Qualifier",
                            "[{}] Authentication failed (policy={}) - Error code: {}, message: {}",
                            context.getX_request_ip(),
                            policy,
                            delegate.getMessage().errcode,
                            delegate.getMessage().errmsg);
                    return Mono
                            .error(new ValidateException(delegate.getMessage().errcode, delegate.getMessage().errmsg));
                });
    }

}
