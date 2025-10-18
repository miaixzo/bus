/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
import org.miaixz.bus.vortex.magic.Delegate;
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

/**
 * The strategy responsible for orchestrating authentication and authorization.
 * <p>
 * This class acts as a high-level workflow engine. Its primary responsibilities are:
 * <ul>
 * <li>Defining the credential discovery order (e.g., Token first, then API Key).</li>
 * <li>Extracting the found credential into a {@link Principal} object.</li>
 * <li>Delegating the actual, complex validation logic to a single, pluggable {@link AuthorizeProvider}.</li>
 * </ul>
 * It does not contain any specific validation logic itself, making it a stable part of the framework's core.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class AuthorizeStrategy extends AbstractStrategy {

    /**
     * The provider responsible for validating credentials and fetching permissions.
     */
    private final AuthorizeProvider provider;

    /**
     * The registry containing configuration for all available API assets.
     */
    private final AssetsRegistry registry;

    /**
     * Constructs a new {@code AuthorizeStrategy} with the specified provider and registry.
     *
     * @param provider The {@link AuthorizeProvider} used for credential validation.
     * @param registry The {@link AssetsRegistry} used to retrieve API asset configurations.
     */
    public AuthorizeStrategy(AuthorizeProvider provider, AssetsRegistry registry) {
        this.provider = provider;
        this.registry = registry;
    }

    /**
     * Executes the full authorization and authentication workflow for the incoming request.
     * <p>
     * This method orchestrates the following steps:
     * <ol>
     * <li>Extracts parameters (e.g., format, channel, token) from the request and populates the {@link Context}.</li>
     * <li>Retrieves the {@link Assets} configuration from the registry using the method and version parameters.</li>
     * <li>Validates that the request's HTTP method matches the one defined in the assets.</li>
     * <li>If the asset is not public (i.e., {@code firewall != 0}), invokes the {@link #authorize} method for
     * authentication.</li>
     * <li>Sets the resolved {@link Assets} in the context.</li>
     * <li>Removes internal gateway parameters (e.g., method, version, format, sign) from the context before passing the
     * request to the next strategy.</li>
     * </ol>
     *
     * @param exchange The {@link ServerWebExchange} representing the current request and response.
     * @param chain    The {@link Chain} to invoke the next strategy in the chain.
     * @return A {@link Mono<Void>} signaling the completion of this strategy's processing.
     * @throws ValidateException If the assets are not found (error code {@link ErrorCode#_100800}) or if the HTTP
     *                           method does not match.
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
            Assets assets = this.registry.get(method, version);
            if (null == assets) {
                Logger.warn("==>     Filter: Assets not found for method: {}, version: {}", method, version);
                return Mono.error(new ValidateException(ErrorCode._100800));
            }

            // Set assets in context for downstream strategies
            context.setAssets(assets);

            // Validate HTTP method
            this.method(exchange, assets);

            // If the API is protected, orchestrate the authorization process.
            if (Consts.ONE != assets.getFirewall()) {
                this.authorize(context);
            }

            // Remove internal gateway parameters before forwarding
            params.remove(Args.METHOD);
            params.remove(Args.FORMAT);
            params.remove(Args.VERSION);
            params.remove(Args.SIGN);

            Logger.info("==>     Filter: Method: {}, Version: {} validated successfully", method, version);

            return chain.apply(exchange);
        });
    }

    /**
     * Validates whether the request's HTTP method matches the expected method defined in the API asset configuration.
     *
     * @param exchange The {@link ServerWebExchange} containing the current request.
     * @param assets   The {@link Assets} configuration for the requested API.
     * @throws ValidateException If the HTTP method does not match the expected method, with an appropriate error code
     *                           (e.g., {@link ErrorCode#_100200} for GET, {@link ErrorCode#_100201} for POST, etc.).
     */
    protected void method(ServerWebExchange exchange, Assets assets) {
        ServerHttpRequest request = exchange.getRequest();
        final HttpMethod expectedMethod = this.valueOf(assets.getType());

        if (!Objects.equals(request.getMethod(), expectedMethod)) {
            String errorMessage = "HTTP method mismatch, expected: " + expectedMethod + ", actual: "
                    + request.getMethod();
            Logger.warn("==>     Filter: {}", errorMessage);

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
    }

    /**
     * Orchestrates the authorization process by finding credentials and delegating validation to the
     * {@link AuthorizeProvider}.
     * <p>
     * This method implements the framework's core credential discovery logic:
     * <ol>
     * <li>It first checks for a bearer token in the request.</li>
     * <li>If no token is found, it then searches for an API key.</li>
     * <li>It packages the found credential into a {@link Principal} object.</li>
     * <li>It makes a single call to {@code provider.authorize(principal)}, delegating all further validation
     * steps.</li>
     * </ol>
     *
     * @param context The request context.
     * @throws ValidateException if no credentials are found or if the provider reports a validation failure.
     */
    protected void authorize(Context context) {
        if (Consts.ZERO == context.getAssets().getToken()) {
            return;
        }

        Principal principal;
        // 1. Prioritize finding a token.
        String accessToken = findAccessTokenInRequest(context);
        if (StringKit.isNotBlank(accessToken)) {
            context.setBearer(accessToken);
            principal = Principal.builder().type(Consts.ONE) // Type 1: Token
                    .value(context.getBearer()).channel(context.getChannel().getType()).context(context).build();
            Logger.info("==>     Filter: Attempting authentication with Token.");
        }
        // 2. If no token, search for an API key.
        else {
            String apiKey = findApiKeyInRequest(context);
            if (StringKit.isBlank(apiKey)) {
                // 3. No credentials found for a protected resource.
                Logger.warn("==>     Filter: No valid credentials (Token or API Key) were provided.");
                throw new ValidateException(ErrorCode._100806);
            }
            principal = Principal.builder().type(Consts.TWO) // Type 2: API Key
                    .value(apiKey).channel(context.getChannel().getType()).context(context).build();
            Logger.info("==>     Filter: No token found. Attempting authentication with API Key.");
        }

        // 4. Delegate the entire validation process to the provider.
        Delegate delegate = this.provider.authorize(principal);

        // 5. Process the final result from the provider.
        if (delegate.isOk()) {
            Map<String, Object> authMap = new HashMap<>();
            BeanKit.beanToMap(
                    delegate.getAuthorize(),
                    authMap,
                    CopyOptions.of().setTransientSupport(false).setIgnoreCase(true));
            context.getParameters().putAll(authMap);
            Logger.info("==>     Filter: Authentication successful.");
            return;
        }
        Logger.error(
                "==>     Filter: Authentication failed - Error code: {}, message: {}",
                delegate.getMessage().errcode,
                delegate.getMessage().errmsg);
        throw new ValidateException(delegate.getMessage().errcode, delegate.getMessage().errmsg);
    }

    /**
     * Extracts the authentication token from the request headers, supporting both the standard `Authorization: Bearer`
     * scheme and a custom `X-Access-Token` header for backward compatibility.
     *
     * @param context The incoming {@link ServerHttpRequest}.
     * @return The extracted token string, or {@code null} if no token is found.
     */
    protected String findAccessTokenInRequest(Context context) {
        // 1. Prioritize the standard `Authorization` header with the `Bearer` scheme.
        final String[] keys = { Args.X_ACCESS_TOKEN, Args.X_ACCESS_TOKEN.toUpperCase(),
                Args.X_ACCESS_TOKEN.toLowerCase(), "X_Access_Token", "X_ACCESS_TOKEN", "x_access_token" };

        String accessToken = MapKit.getFirstNonNull(context.getHeaders(), keys);
        if (StringKit.startWithAnyIgnoreCase(accessToken, "Bearer ")) {
            return accessToken.substring(7);
        }
        if (StringKit.isNotEmpty(accessToken)) {
            return accessToken;
        }

        // If not found, search in request parameters.
        if (StringKit.isBlank(accessToken)) {
            accessToken = Optional.ofNullable(MapKit.getFirstNonNull(context.getParameters(), keys))
                    .map(Object::toString).orElse(null);
        }

        return accessToken;
    }

    /**
     * Searches for an API key in a predefined list of request parameters and headers.
     *
     * @param context The request context.
     * @return The found API key, or {@code null} if not present.
     */
    protected String findApiKeyInRequest(Context context) {
        final String[] keys = { "apiKey", "api_key", "x_api_key", "api_id", "x_api_id", "X-API-ID", "X-API-KEY",
                "API-KEY", "API-ID" };

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
