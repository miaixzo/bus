/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.bean.copier.CopyOptions;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.*;
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
 * @since Java 17+
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
     * <li>Extracts key gateway parameters (e.g., format, channel, method, version) from the request and populates the
     * {@link Context}.</li>
     * <li>Retrieves the {@link Assets} configuration from the registry using the method and version.</li>
     * <li>Sets the resolved {@link Assets} in the context for downstream use.</li>
     * <li>Validates that the request's HTTP method matches the one defined in the assets.</li>
     * <li>If the API asset is protected, it invokes the authorization workflow.</li>
     * <li>Removes internal gateway parameters (e.g., method, version, format, sign) before passing the request to the
     * next strategy.</li>
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

            // Extract and set context parameters
            context.setFormat(
                    Formats.get(Optional.ofNullable(params.get(Args.FORMAT)).map(Object::toString).orElse(null)));
            context.setChannel(
                    Channel.get(
                            Optional.ofNullable(params.get(Args.X_REMOTE_CHANNEL)).map(Object::toString).orElse(null)));

            // Retrieve API asset configuration
            String method = Optional.ofNullable(params.get(Args.METHOD)).map(Object::toString).orElse(null);
            String version = Optional.ofNullable(params.get(Args.VERSION)).map(Object::toString).orElse(null);

            // 1. Asynchronously retrieve API asset configuration, offloading if blocking.
            // (Assuming registry.get() is a fast in-memory lookup, but keeping
            // .subscribeOn() for consistency if it *could* be I/O).
            return Mono.fromCallable(() -> this.registry.get(method, version)).subscribeOn(Schedulers.boundedElastic()) // Offload
                    // potential
                    // I/O
                    .switchIfEmpty(Mono.defer(() -> {
                        // switchIfEmpty defers the error creation
                        Logger.warn(
                                false,
                                "Qualifier",
                                "[{}] Assets not found for method: {}, version: {}",
                                context.getX_request_ipv4(),
                                method,
                                version);
                        return Mono.error(new ValidateException(ErrorCode._100800));
                    })).flatMap(assets -> {
                        // 2. Set assets in context
                        context.setAssets(assets);

                        // 3. Chain HTTP method validation
                        Mono<Void> validationMono = this.method(exchange, context, assets);

                        // 4. Chain authorization if the API is protected
                        Mono<Void> authMono = (Consts.ZERO != assets.getPolicy()) ? this.authorize(context)
                                : Mono.empty();

                        // 5. Execute validation then authorization sequentially
                        return validationMono.then(authMono);
                    })
                    // 6. After all validations, remove internal parameters
                    .then(Mono.fromRunnable(() -> {
                        params.remove(Args.METHOD);
                        params.remove(Args.FORMAT);
                        params.remove(Args.VERSION);
                        params.remove(Args.SIGN);
                        Logger.info(
                                true,
                                "Qualifier",
                                "[{}] Method: {}, Version: {} validated successfully",
                                context.getX_request_ipv4(),
                                method,
                                version);
                    }))
                    // 7. Proceed to the next strategy in the chain
                    .then(chain.apply(exchange));
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
        // Wrap synchronous logic that can throw an exception
        return Mono.fromRunnable(() -> {
            ServerHttpRequest request = exchange.getRequest();
            final HttpMethod expectedMethod = this.valueOf(assets.getType());

            if (!Objects.equals(request.getMethod(), expectedMethod)) {
                Logger.warn(
                        false,
                        "Qualifier",
                        "[{}] HTTP method mismatch, expected: {}, actual: {}",
                        context.getX_request_ipv4(),
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
                // This exception will be caught by fromRunnable and emitted as Mono.error()
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

        // Validate policy value
        if (policy == null || policy < Consts.ZERO || policy > Consts.FIVE) {
            Logger.error(
                    false,
                    "Qualifier",
                    "[{}] Invalid policy value: {}. Must be between 0 and 5.",
                    context.getX_request_ipv4(),
                    policy);
            return Mono.error(new ValidateException(ErrorCode._116002));
        }

        // Policy 0: Anonymous access - no authentication required
        if (Consts.ZERO.equals(policy)) {
            Logger.info(true, "Qualifier", "[{}] Anonymous access granted.", context.getX_request_ipv4());
            return Mono.empty();
        }

        // Determine acceptable credential types based on policy
        // Policy 1, 2: Token ONLY (reject API Key even if provided)
        // Policy 3, 4: API Key ONLY (reject Token even if provided)
        // Policy 5: Token or API Key (both accepted)
        final boolean acceptToken = Consts.ONE.equals(policy) || Consts.TWO.equals(policy)
                || Consts.FIVE.equals(policy);
        final boolean acceptApiKey = Consts.THREE.equals(policy) || Consts.FOUR.equals(policy)
                || Consts.FIVE.equals(policy);

        // Try to find acceptable credentials based on policy requirements
        String credentialValue = null;

        // 1. Policy 1, 2, 5: Must use Token (ignore API Key even if provided)
        if (acceptToken) {
            credentialValue = this.getToken(context);
            if (StringKit.isNotBlank(credentialValue)) {
                context.setBearer(credentialValue);
                Logger.info(
                        true,
                        "Qualifier",
                        "[{}] Using Token (required by policy={}).",
                        context.getX_request_ipv4(),
                        policy);
            }
        }

        // 2. Policy 3, 4, 5: Must use API Key (ignore Token if policy is 3 or 4)
        // Only check API Key if Token not found (for policy 5) or if policy is 3,4 (API Key only)
        if (acceptApiKey) {
            credentialValue = this.getApiKey(context);
            if (StringKit.isNotBlank(credentialValue)) {
                Logger.info(
                        true,
                        "Qualifier",
                        "[{}] Using API Key (required by policy={}).",
                        context.getX_request_ipv4(),
                        policy);
            }
        }

        // 3. Validate that required credential type was provided
        if (credentialValue == null) {
            Logger.warn(
                    false,
                    "Qualifier",
                    "[{}] Required credential not provided for policy={}.",
                    context.getX_request_ipv4(),
                    policy);
            return Mono.error(new ValidateException(ErrorCode._116002));
        }

        // 4. Delegate the validation to the provider
        // The type field is set to the policy value, provider uses it to determine validation method
        return this.provider.authorize(
                Principal.builder().channel(context.getChannel().getType()).context(context).type(policy)
                        .value(credentialValue).build())
                .flatMap(delegate -> {
                    // 5. Process the final result from the provider
                    if (delegate.isOk()) {
                        Map<String, Object> authMap = new HashMap<>();
                        BeanKit.beanToMap(
                                delegate.getAuthorize(),
                                authMap,
                                CopyOptions.of().setTransientSupport(false).setIgnoreCase(true)
                                        .setIgnoreProperties("id"));

                        context.getParameters().putAll(authMap);
                        Logger.info(
                                true,
                                "Qualifier",
                                "[{}] Authentication successful (policy={}).",
                                context.getX_request_ipv4(),
                                policy);
                        return Mono.empty();
                    }

                    Logger.error(
                            false,
                            "Qualifier",
                            "[{}] Authentication failed (policy={}) - Error code: {}, message: {}",
                            context.getX_request_ipv4(),
                            policy,
                            delegate.getMessage().errcode,
                            delegate.getMessage().errmsg);
                    // Signal failure
                    return Mono
                            .error(new ValidateException(delegate.getMessage().errcode, delegate.getMessage().errmsg));
                });
    }

    /**
     * Extracts the authentication token from the incoming request.
     *
     * <p>
     * The token extraction follows a specific order of precedence to ensure compatibility with both standard and legacy
     * authentication methods:
     * </p>
     *
     * <ol>
     * <li><b>Standard Authorization Header:</b> It first checks for the standard {@code Authorization: Bearer <token>}
     * header. This is the preferred and most secure method.</li>
     * <li><b>Custom Header for Backward Compatibility:</b> If the standard header is not found, it searches for a
     * custom header, {@code X-Access-Token}. This check is performed against a list of common case variations (e.g.,
     * {@code X_ACCESS_TOKEN}, {@code x_access_token}) to accommodate different client implementations.</li>
     * <li><b>Request Parameter as Fallback:</b> As a final fallback, if no token is found in the headers, the method
     * searches for the token in the request parameters (query string) using the same set of keys as the custom header.
     * </li>
     * </ol>
     *
     * @param context The incoming {@link ServerHttpRequest} context containing headers and parameters.
     * @return The extracted token string, or {@code null} if no token is found in any of the checked locations.
     */
    protected String getToken(Context context) {
        // 1. Prioritize the standard `Authorization` header with the `Bearer` scheme.
        String authorization = context.getHeaders().get("Authorization");
        if (StringKit.isNotEmpty(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        // 2. Check for a custom `X-Access-Token` header for backward compatibility.
        final String[] keys = { Args.X_ACCESS_TOKEN, Args.X_ACCESS_TOKEN.toUpperCase(),
                Args.X_ACCESS_TOKEN.toLowerCase(), "X_Access_Token", "X_ACCESS_TOKEN", "x_access_token" };
        String token = MapKit.getFirstNonNull(context.getHeaders(), keys);
        if (StringKit.isNotEmpty(token)) {
            return token;
        }

        // 3. If not found in headers, search in request parameters as a fallback.
        if (StringKit.isBlank(token)) {
            token = Optional.ofNullable(MapKit.getFirstNonNull(context.getParameters(), keys)).map(Object::toString)
                    .orElse(null);
        }

        return token;
    }

    /**
     * Searches for an API key in a predefined list of request parameters and headers.
     *
     * @param context The request context.
     * @return The found API key, or {@code null} if not present.
     */
    protected String getApiKey(Context context) {
        final String[] keys = { "apiKey", "apikey", "api_key", "x_api_key", "api_id", "x_api_id", "X-API-ID",
                "X-API-KEY", "API-KEY", "API-ID" };

        // First, search in request parameters.
        String apiKey = Optional.ofNullable(MapKit.getFirstNonNull(context.getParameters(), keys)).map(Object::toString)
                .orElse(null);

        // If not found, search in request headers.
        if (StringKit.isBlank(apiKey)) {
            apiKey = MapKit.getFirstNonNull(context.getHeaders(), keys);
        }

        return apiKey;
    }

}
