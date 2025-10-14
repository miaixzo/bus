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
 * A strategy that acts as the primary gatekeeper for API access, performing authentication and authorization.
 * <p>
 * This strategy is responsible for verifying the caller's identity and permissions. It collaborates with two key
 * components:
 * <ul>
 * <li>{@link AssetsRegistry}: To look up the configuration ({@link Assets}) for the requested API method and
 * version.</li>
 * <li>{@link AuthorizeProvider}: To delegate the actual work of validating credentials (like tokens or API keys) and
 * fetching user permissions.</li>
 * </ul>
 * It is ordered to run after {@link CipherStrategy} to ensure that it operates on decrypted, plaintext parameters.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class AuthorizeStrategy extends AbstractStrategy {

    /**
     * The provider that performs the actual validation of tokens and API keys.
     */
    private final AuthorizeProvider provider;

    /**
     * The registry that holds the configuration for all available API assets.
     */
    private final AssetsRegistry registry;

    /**
     * Constructs a new {@code AuthorizeStrategy}.
     *
     * @param provider The authorization provider.
     * @param registry The assets registry.
     */
    public AuthorizeStrategy(AuthorizeProvider provider, AssetsRegistry registry) {
        this.provider = provider;
        this.registry = registry;
    }

    /**
     * Applies the full authorization and authentication workflow.
     * <p>
     * This method orchestrates the entire authorization process:
     * <ol>
     * <li>Extracts basic parameters (format, channel, token) from the request and populates the {@link Context}.</li>
     * <li>Looks up the corresponding {@link Assets} from the registry using the method and version parameters.</li>
     * <li>Validates that the request's HTTP method matches the one defined in the assets.</li>
     * <li>If the asset is not public (i.e., {@code firewall != 0}), it invokes the {@link #authorize} method to perform
     * authentication.</li>
     * <li>Populates the context with the resolved {@code Assets}.</li>
     * <li>Cleans up internal gateway parameters (method, version, etc.) from the context before passing the request
     * downstream.</li>
     * </ol>
     *
     * @param exchange The current server exchange.
     * @param chain    The next strategy in the chain.
     * @return A {@code Mono<Void>} that signals the completion of this strategy.
     */
    @Override
    public Mono<Void> apply(ServerWebExchange exchange, StrategyChain chain) {
        return Mono.deferContextual(contextView -> {
            final Context context = contextView.get(Context.class);

            Map<String, String> params = context.getParameters();

            context.setFormat(Formats.valueOf(StringKit.toUpperCase(params.get(Args.FORMAT))));
            context.setChannel(Channel.get(params.get(Args.X_REMOTE_CHANNEL)));
            context.setToken(exchange.getRequest().getHeaders().getFirst(Args.X_ACCESS_TOKEN));

            String method = params.get(Args.METHOD);
            String version = params.get(Args.VERSION);

            Assets assets = registry.get(method, version);
            if (null == assets) {
                Logger.warn("==>     Filter: Assets not found for method: {}, version: {}", method, version);
                return Mono.error(new ValidateException(ErrorCode._100800));
            }

            this.method(exchange, assets);
            if (Consts.ZERO != assets.getFirewall()) {
                this.authorize(context, assets);
            }

            context.setAssets(assets);

            // Purge internal gateway parameters before continuing the chain.
            params.remove(Args.METHOD);
            params.remove(Args.FORMAT);
            params.remove(Args.VERSION);
            params.remove(Args.SIGN);

            Logger.info("==>     Filter: Method: {}, Version: {} validated successfully", method, version);

            return chain.apply(exchange);
        });
    }

    /**
     * Validates if the request's HTTP method matches the one required by the API asset.
     *
     * @param exchange The current server exchange.
     * @param assets   The API asset configuration.
     * @throws ValidateException if the HTTP method does not match.
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
     * Authenticates the request using a "Token-first, then API Key" strategy.
     * <p>
     * This method attempts to authenticate the request using the following priority:
     * <ol>
     * <li><b>Token Authentication:</b> If a valid token is present in the request headers, it is validated.</li>
     * <li><b>API Key Authentication:</b> If no valid token is found, it falls back to validating an API Key from the
     * request parameters or headers.</li>
     * </ol>
     * If neither authentication method succeeds, a {@link ValidateException} is thrown.
     *
     * @param context The request context, containing token, parameters, and header information.
     * @param assets  The API asset configuration.
     * @throws ValidateException if both authentication methods fail.
     */
    protected void authorize(Context context, Assets assets) {
        // 1. Try Token-based authentication first.
        if (tryTokenAuthorize(context, assets)) {
            Logger.info("==>     Filter: Token authentication succeeded.");
            return;
        }

        // 2. Fallback to API Key-based authentication.
        if (tryApiKeyAuthorize(context, assets)) {
            Logger.info("==>     Filter: API Key authentication succeeded.");
            return;
        }

        // 3. If both methods failed, deny access.
        Logger.warn("==>     Filter: Both Token and API Key authentication failed.");
        throw new ValidateException(ErrorCode._100806); // Or a more appropriate error code
    }

    /**
     * Attempts to authenticate the request using a token from the request headers.
     *
     * @param context The request context.
     * @param assets  The API asset configuration.
     * @return {@code true} if a token was present and successfully validated, {@code false} if no token was present.
     * @throws ValidateException if a token was present but failed validation.
     */
    protected boolean tryTokenAuthorize(Context context, Assets assets) {
        if (StringKit.isBlank(context.getToken())) {
            return false; // No token present, so this method does not apply.
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
     * Attempts to authenticate the request using an API Key from the request parameters or headers.
     *
     * @param context The request context.
     * @param assets  The API asset configuration.
     * @return {@code true} if an API key was found and successfully validated, {@code false} if no key was found.
     * @throws ValidateException if an API key was found but failed validation.
     */
    protected boolean tryApiKeyAuthorize(Context context, Assets assets) {
        String[] apiKeyParams = { "apiKey", "api_key", "x_api_key", "api_id", "x_api_id", "X-API-ID", "X-API-KEY",
                "API-KEY", "API-ID" };

        // Try to get API Key from request parameters first.
        String apiKey = MapKit.getFirstNonNull(context.getParameters(), apiKeyParams);

        // If not found in parameters, try headers.
        if (StringKit.isBlank(apiKey)) {
            apiKey = MapKit.getFirstNonNull(context.getHeaders(), apiKeyParams);
        }

        if (StringKit.isBlank(apiKey)) {
            return false; // No API Key present, so this method does not apply.
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

}
