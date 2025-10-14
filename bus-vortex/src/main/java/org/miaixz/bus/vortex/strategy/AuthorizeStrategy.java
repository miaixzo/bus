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
 * A strategy that serves as the primary gatekeeper for API access, handling authentication and authorization.
 * <p>
 * This strategy verifies the caller's identity and permissions by collaborating with two key components:
 * <ul>
 * <li>{@link AssetsRegistry}: Retrieves the configuration ({@link Assets}) for the requested API method and
 * version.</li>
 * <li>{@link AuthorizeProvider}: Validates credentials (e.g., tokens, API keys, or licenses) and retrieves user
 * permissions.</li>
 * </ul>
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
     * @param chain    The {@link StrategyChain} to invoke the next strategy in the chain.
     * @return A {@link Mono<Void>} signaling the completion of this strategy's processing.
     * @throws ValidateException If the assets are not found (error code {@link ErrorCode#_100800}) or if the HTTP
     *                           method does not match.
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, StrategyChain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);

            Map<String, String> params = context.getParameters();

            // Extract and set context parameters
            context.setFormat(Formats.valueOf(StringKit.toUpperCase(params.get(Args.FORMAT))));
            context.setChannel(Channel.get(params.get(Args.X_REMOTE_CHANNEL)));
            context.setToken(exchange.getRequest().getHeaders().getFirst(Args.X_ACCESS_TOKEN));

            // Retrieve API asset configuration
            String method = params.get(Args.METHOD);
            String version = params.get(Args.VERSION);
            Assets assets = registry.get(method, version);
            if (null == assets) {
                Logger.warn("==>     Filter: Assets not found for method: {}, version: {}", method, version);
                return Mono.error(new ValidateException(ErrorCode._100800));
            }

            // Validate HTTP method
            this.method(exchange, assets);

            // Perform authorization if the asset is not public
            if (Consts.ZERO != assets.getFirewall()) {
                this.authorize(context, assets);
            }

            // Set assets in context
            context.setAssets(assets);

            // Remove internal gateway parameters
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
     * Authenticates the request using a prioritized strategy: token, then API key, then license.
     * <p>
     * This method attempts authentication in the following order:
     * <ol>
     * <li><b>Token Authentication:</b> Checks for a valid token in the request headers.</li>
     * <li><b>API Key Authentication:</b> If token authentication fails or no token is present, checks for a valid API
     * key in the request parameters or headers.</li>
     * <li><b>License Authentication:</b> If API key authentication fails or no API key is present, checks for a valid
     * license in the request parameters or headers.</li>
     * </ol>
     * If all authentication methods fail, a {@link ValidateException} is thrown with error code
     * {@link ErrorCode#_100806}.
     *
     * @param context The {@link Context} containing request details such as token, parameters, and headers.
     * @param assets  The {@link Assets} configuration for the requested API.
     * @throws ValidateException If all authentication methods fail.
     */
    protected void authorize(Context context, Assets assets) {
        // Try token-based authentication
        if (tryToken(context, assets)) {
            Logger.info("==>     Filter: Token authentication succeeded.");
            return;
        }

        // Fallback to API key-based authentication
        if (tryApiKey(context, assets)) {
            Logger.info("==>     Filter: API Key authentication succeeded.");
            return;
        }

        // Fallback to license-based authentication
        if (tryLicense(context, assets)) {
            Logger.info("==>     Filter: License authentication succeeded.");
            return;
        }

        // If all authentication methods fail, deny access
        Logger.warn("==>     Filter: Token, API Key, and License authentication failed.");
        throw new ValidateException(ErrorCode._100806);
    }

    /**
     * Attempts to authenticate the request using a token from the request headers.
     * <p>
     * This method checks for a token in the {@link Context} and, if present, delegates validation to the
     * {@link AuthorizeProvider}. If validation succeeds, the resulting authorization details are added to the context
     * parameters.
     *
     * @param context The {@link Context} containing the token and other request details.
     * @param assets  The {@link Assets} configuration for the requested API.
     * @return {@code true} if a token was present and successfully validated, {@code false} if no token was present.
     * @throws ValidateException If a token was present but failed validation, with the error code and message from the
     *                           {@link Delegate}.
     */
    protected boolean tryToken(Context context, Assets assets) {
        if (StringKit.isBlank(context.getToken())) {
            return false; // No token present
        }

        Delegate delegate = this.provider.authorize(
                Principal.builder().type(Consts.ONE).value(context.getToken()).channel(context.getChannel().getType())
                        .assets(assets).build());

        if (delegate.isOk()) {
            Map<String, Object> authMap = new HashMap<>();
            BeanKit.beanToMap(
                    delegate.getAuthorize(),
                    authMap,
                    CopyOptions.of().setTransientSupport(false).setIgnoreCase(true));
            authMap.forEach((k, v) -> context.getParameters().put(k, String.valueOf(v)));
            return true;
        }

        Logger.error(
                "==>     Filter: Token validation failed - Error code: {}, message: {}",
                delegate.getMessage().errcode,
                delegate.getMessage().errmsg);
        throw new ValidateException(delegate.getMessage().errcode, delegate.getMessage().errmsg);
    }

    /**
     * Attempts to authenticate the request using an API key from the request parameters or headers.
     * <p>
     * This method searches for an API key in a predefined list of parameter or header names. If found, it delegates
     * validation to the {@link AuthorizeProvider}. If validation succeeds, the resulting authorization details are
     * added to the context parameters.
     *
     * @param context The {@link Context} containing request parameters and headers.
     * @param assets  The {@link Assets} configuration for the requested API.
     * @return {@code true} if an API key was found and successfully validated, {@code false} if no API key was found.
     * @throws ValidateException If an API key was found but failed validation, with the error code and message from the
     *                           {@link Delegate}.
     */
    protected boolean tryApiKey(Context context, Assets assets) {
        String[] apiKeyParams = { "apiKey", "api_key", "x_api_key", "api_id", "x_api_id", "X-API-ID", "X-API-KEY",
                "API-KEY", "API-ID" };

        // Try to get API key from request parameters
        String apiKey = MapKit.getFirstNonNull(context.getParameters(), apiKeyParams);

        // If not found in parameters, try headers
        if (StringKit.isBlank(apiKey)) {
            apiKey = MapKit.getFirstNonNull(context.getHeaders(), apiKeyParams);
        }

        if (StringKit.isBlank(apiKey)) {
            return false; // No API key present
        }

        Delegate delegate = this.provider.authorize(
                Principal.builder().type(Consts.TWO).value(apiKey).channel(context.getChannel().getType())
                        .assets(assets).build());

        if (delegate.isOk()) {
            Map<String, Object> authMap = new HashMap<>();
            BeanKit.beanToMap(
                    delegate.getAuthorize(),
                    authMap,
                    CopyOptions.of().setTransientSupport(false).setIgnoreCase(true));
            authMap.forEach((k, v) -> context.getParameters().put(k, String.valueOf(v)));
            return true;
        }

        Logger.error(
                "==>     Filter: API Key validation failed - Error code: {}, message: {}",
                delegate.getMessage().errcode,
                delegate.getMessage().errmsg);
        throw new ValidateException(delegate.getMessage().errcode, delegate.getMessage().errmsg);
    }

    /**
     * Attempts to authenticate the request using a license from the request parameters or headers.
     * <p>
     * This method checks if a license is required (i.e., {@code assets.getLicense() != Consts.ZERO}) and searches for a
     * domain in the request parameters or headers (e.g., "x_request_domain"). If found, it delegates validation to the
     * {@link AuthorizeProvider}. If validation succeeds, the resulting authorization details are added to the context
     * parameters.
     *
     * @param context The {@link Context} containing request parameters, headers, and domain information.
     * @param assets  The {@link Assets} configuration for the requested API.
     * @return {@code true} if no license is required or if a domain was found and successfully validated, {@code false}
     *         if no domain was found.
     * @throws ValidateException If a domain was found but failed validation, with the error code and message from the
     *                           {@link Delegate}.
     */
    protected boolean tryLicense(Context context, Assets assets) {
        if (Consts.ZERO == assets.getLicense()) {
            return true; // No license required
        }

        String[] keys = { "x_request_domain", "X_REQUEST_DOMAIN" };

        // Try to get domain from context
        String domain = context.getX_request_domain();

        // If not found in context, try parameters
        if (StringKit.isBlank(domain)) {
            domain = MapKit.getFirstNonNull(context.getParameters(), keys);
        }

        if (StringKit.isBlank(domain)) {
            return false; // No domain present
        }

        Delegate delegate = this.provider.authorize(
                Principal.builder().type(Consts.THREE).value(domain).channel(context.getChannel().getType())
                        .assets(assets).build());

        if (delegate.isOk()) {
            Map<String, Object> authMap = new HashMap<>();
            BeanKit.beanToMap(
                    delegate.getAuthorize(),
                    authMap,
                    CopyOptions.of().setTransientSupport(false).setIgnoreCase(true));
            authMap.forEach((k, v) -> context.getParameters().put(k, String.valueOf(v)));
            return true;
        }

        Logger.error(
                "==>     Filter: License validation failed - Error code: {}, message: {}",
                delegate.getMessage().errcode,
                delegate.getMessage().errmsg);
        throw new ValidateException(delegate.getMessage().errcode, delegate.getMessage().errmsg);
    }

}
