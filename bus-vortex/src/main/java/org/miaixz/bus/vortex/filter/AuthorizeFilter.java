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
package org.miaixz.bus.vortex.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.bean.copier.CopyOptions;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
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
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * Access authorization filter responsible for verifying the legality of requests, including method, token, and
 * application ID.
 * <p>
 * This filter is a crucial part of the request processing chain. It validates the legality of requests by checking the
 * request method, version, token (if required), and application ID. The filter ensures that only legitimate requests
 * are allowed to proceed by verifying the HTTP method, token, and application ID against the configured asset
 * information.
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class AuthorizeFilter extends AbstractFilter {

    /**
     * The authorization provider, used for handling token validation and authorization logic.
     * <p>
     * This component is responsible for validating incoming access tokens and returning authorization results,
     * including user information and permissions.
     * </p>
     */
    private final AuthorizeProvider provider;

    /**
     * The assets registry, used for storing and retrieving API asset information.
     * <p>
     * This component maintains all available API assets, including their methods, versions, HTTP method types, and
     * whether token validation is required.
     * </p>
     */
    private final AssetsRegistry registry;

    /**
     * Constructs an {@code AuthorizeFilter} with the specified authorization provider and assets registry.
     *
     * @param provider The authorization provider, used for handling token validation and authorization logic.
     * @param registry The assets registry, used for storing and retrieving API asset information.
     */
    public AuthorizeFilter(AuthorizeProvider provider, AssetsRegistry registry) {
        this.provider = provider;
        this.registry = registry;
    }

    /**
     * The internal filtering method, which executes the authorization validation logic.
     * <p>
     * This method is the core implementation of the filter, responsible for executing the complete authorization
     * validation process, including:
     * <ol>
     * <li>Extracting parameters from the request and setting the context.</li>
     * <li>Finding the corresponding asset based on the method and version.</li>
     * <li>Validating if the HTTP method matches.</li>
     * <li>Validating the access token if required.</li>
     * <li>Validating if the application ID matches.</li>
     * <li>Populating and cleaning request parameters.</li>
     * <li>Setting asset information into the context.</li>
     * </ol>
     *
     * @param exchange The current {@link ServerWebExchange} object, containing request and response information.
     * @param chain    The filter chain, used to pass the request to the next filter.
     * @param context  The request context, containing request-related state information.
     * @return {@link Mono<Void>} indicating the asynchronous completion of processing. The filter chain continues
     *         execution if all validations pass.
     */
    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        // Get the request parameter map from the context
        Map<String, String> params = getRequestMap(context);

        // Set the format, channel, and token information in the context
        context.setFormats(Formats.valueOf(StringKit.toUpperCase(params.get(Args.FORMAT))));
        context.setChannel(Channel.get(params.get(Args.X_REMOTE_CHANNEL)));
        context.setToken(exchange.getRequest().getHeaders().getFirst(Args.X_ACCESS_TOKEN));

        // Get the request method and version
        String method = params.get(Args.METHOD);
        String version = params.get(Args.VERSION);

        // Get the corresponding asset information from the registry
        Assets assets = registry.get(method, version);
        if (null == assets) {
            Logger.warn("==>     Filter: Assets not found for method: {}, version: {}", method, version);
            return Mono.error(new ValidateException(ErrorCode._100800));
        }

        // Basic request validation
        this.method(exchange, assets);
        if (Consts.ZERO != assets.getFirewall()) {
            // Perform authorize
            this.authorize(exchange, context, assets);
        }

        // Set asset information into the context
        context.setAssets(assets);

        // Populates IP-related parameters into the request context.
        this.populate(exchange, context);

        // Log successful validation
        Logger.info("==>     Filter: Method: {}, Version: {} validated successfully", method, version);

        // Continue with the filter chain
        return chain.filter(exchange);
    }

    /**
     * Validates if the HTTP method of the request matches the asset configuration.
     * <p>
     * This method checks if the HTTP method of the current request is consistent with the method required in the asset
     * configuration. If there is a mismatch, a business exception will be thrown based on the expected HTTP method
     * type.
     * </p>
     *
     * @param exchange The {@link ServerWebExchange} object, containing request and response information.
     * @param assets   The asset information, containing the expected HTTP method type.
     * @throws ValidateException if the method does not match, throwing the corresponding error.
     */
    protected void method(ServerWebExchange exchange, Assets assets) {
        ServerHttpRequest request = exchange.getRequest();

        final HttpMethod expectedMethod = this.valueOf(assets.getType());

        if (!Objects.equals(request.getMethod(), expectedMethod)) {
            String errors = "HTTP method mismatch, expected: " + expectedMethod + ", actual: " + request.getMethod();
            Logger.warn("==>     Filter: {}", errors);

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
     * Validates the request using either API Key or Token authorize.
     * <p>
     * This method attempts to authenticate the request using the following priority:
     * <ol>
     * <li><b>Token Authentication</b>: If a valid token is present in the request headers.</li>
     * <li><b>API Key Authentication</b>: If no valid token is found, attempts to use API Key from parameters or
     * headers.</li>
     * </ol>
     * If neither authorize method succeeds, throws a {@link ValidateException}.
     *
     * @param exchange The {@link ServerWebExchange} object, containing request and response information.
     * @param context  The context object, containing token and channel information.
     * @param assets   The asset information (may not contain token/scope configuration).
     * @throws ValidateException if both authorize methods fail.
     */
    protected void authorize(ServerWebExchange exchange, Context context, Assets assets) {
        // Try Token Authorize first
        if (tryApiKeyAuthorize(context, assets)) {
            Logger.info("==>     Filter: Token authorize succeeded");
            return;
        }

        // Fallback to API Key Authorize
        if (tryTokenAuthorize(context, assets)) {
            Logger.info("==>     Filter: API Key authorize succeeded");
            return;
        }

        // If both methods failed
        Logger.warn("==>     Filter: Both Token and API Key authorize failed");
        throw new ValidateException(ErrorCode._100806); // Or a more appropriate error code
    }

    /**
     * Attempts to authenticate using Token.
     * <p>
     * Checks if a valid token is present in the request headers and validates it.
     * </p>
     *
     * @param context The request context
     * @param assets  The asset configuration
     * @return {@code true} if authorize succeeded, {@code false} if no token was present
     * @throws ValidateException if token validation failed
     */
    protected boolean tryTokenAuthorize(Context context, Assets assets) {
        if (StringKit.isBlank(context.getToken())) {
            return false; // No token present
        }

        Delegate delegate = this.provider.authorize(
                Principal.builder().type(Consts.ONE).value(context.getToken()).channel(context.getChannel().getType())
                        .assets(assets).build());

        if (delegate.isOk()) {
            populate(delegate.getAuthorize(), context);
            return true;
        }

        Logger.error(
                "==>     Filter: Token validation failed - Error code: {}, message: {}",
                delegate.getMessage().errcode,
                delegate.getMessage().errmsg);
        throw new ValidateException(delegate.getMessage().errcode, delegate.getMessage().errmsg);
    }

    /**
     * Attempts to authenticate using API Key.
     * <p>
     * Checks for API Key in request parameters or headers and validates it.
     * </p>
     *
     * @param context The request context
     * @param assets  The asset configuration
     * @return {@code true} if authorize succeeded, {@code false} if no API Key was present
     * @throws ValidateException if API Key validation failed
     */
    protected boolean tryApiKeyAuthorize(Context context, Assets assets) {
        String[] apiKeyParams = { "apiKey", "api_key", "x_api_key", "api_id", "x_api_id", "X-API-ID", "X-API-KEY",
                "API-KEY", "API-ID" };

        // Try to get API Key from request parameters first
        String apiKey = MapKit.getFirstNonNull(context.getRequestMap(), apiKeyParams);

        // If not found in parameters, try headers
        if (StringKit.isBlank(apiKey)) {
            apiKey = MapKit.getFirstNonNull(context.getHeaderMap(), apiKeyParams);
        }

        if (StringKit.isBlank(apiKey)) {
            return false; // No API Key present
        }

        Delegate delegate = this.provider.authorize(
                Principal.builder().type(Consts.TWO).value(apiKey).channel(context.getChannel().getType())
                        .assets(assets).build());

        if (delegate.isOk()) {
            populate(delegate.getAuthorize(), context);
            return true;
        }

        Logger.error(
                "==>     Filter: API Key validation failed - Error code: {}, message: {}",
                delegate.getMessage().errcode,
                delegate.getMessage().errmsg);
        throw new ValidateException(delegate.getMessage().errcode, delegate.getMessage().errmsg);
    }

    /**
     * Populates IP-related parameters into the request context.
     * <p>
     * This method extracts the client's IP address and adds it to the request context. The IP address is determined in
     * the following order of precedence:
     * <ol>
     * <li>Checks the "x_remote_ip" request header.</li>
     * <li>If absent, checks the "X-Forwarded-For" request header to handle cases with proxies.</li>
     * <li>If still absent, falls back to the request's remote address.</li>
     * </ol>
     * Additionally, it retrieves the request's authority (host and port) and adds it to the context. If the authority
     * cannot be determined, a default value is used to ensure continued processing without throwing an exception.
     *
     * @param exchange ServerWebExchange object containing request and response information.
     * @param context  Request context where IP address and authority information will be stored.
     */
    protected void populate(ServerWebExchange exchange, Context context) {
        // Extract the client's IP address
        String clientIp = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("x_request_ip")).orElseGet(
                () -> Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-Forwarded-For")).orElseGet(
                        () -> exchange.getRequest().getRemoteAddress() != null
                                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                                : "unknown"));

        // Add the client IP to the context
        context.getRequestMap().put("x_request_ipv4", clientIp);

        // Get the domain name and port (authority)
        Optional<String> authorityOptional = getOriginalAuthority(exchange.getRequest());

        String unknown = Normal.UNKNOWN + Symbol.COLON + Symbol.ZERO;
        // Use a default authority if none is found to prevent exceptions and ensure smooth processing
        String domain = authorityOptional.orElse(unknown);

        // Log a warning if the authority could not be determined
        if (unknown.equals(domain)) {
            Logger.warn(
                    "==> Filter: Unable to determine the request domain (host:port). Using default value: {}",
                    domain);
        }

        // Add the authority to the request context
        context.getRequestMap().put("x_request_domain", domain);

        // Purges parameters reserved for internal gateway use.
        context.getRequestMap().remove(Args.METHOD);
        context.getRequestMap().remove(Args.FORMAT);
        context.getRequestMap().remove(Args.VERSION);
        context.getRequestMap().remove(Args.SIGN);
    }

    /**
     * Populates the authorize result into the request context.
     * <p>
     * Extracts user information from the authorization result and adds it to the request parameters.
     * </p>
     *
     * @param auth    The authorization result
     * @param context The request context
     */
    protected void populate(Authorize auth, Context context) {
        Map<String, Object> authMap = new HashMap<>();
        BeanKit.beanToMap(auth, authMap, CopyOptions.of().setTransientSupport(false).setIgnoreCase(true));

        authMap.forEach((k, v) -> context.getRequestMap().put(k, v.toString()));
    }

}
