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

import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.basic.normal.Consts;
import org.miaixz.bus.core.basic.normal.Errors;
import org.miaixz.bus.core.bean.copier.CopyOptions;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
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
 * 访问鉴权过滤器，负责验证请求的合法性、方法、令牌和应用 ID
 * <p>
 * 该过滤器是请求处理链中的重要组成部分，它通过检查请求的方法、版本、令牌和应用ID来验证请求的合法性。 过滤器会根据配置的资产信息验证请求的HTTP方法、令牌（如果需要）以及应用ID，确保只有合法的请求能够通过。
 * </p>
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class AuthorizeFilter extends AbstractFilter {

    /**
     * 授权提供者，用于处理令牌验证和授权逻辑
     * <p>
     * 该组件负责验证传入的访问令牌，并返回授权结果，包括用户信息和权限等。
     * </p>
     */
    private final AuthorizeProvider provider;

    /**
     * 资产注册表，用于存储和检索API资产信息
     * <p>
     * 该组件维护所有可用的API资产，包括它们的方法、版本、HTTP方法类型以及是否需要令牌验证等信息。
     * </p>
     */
    private final AssetsRegistry registry;

    /**
     * 构造器，初始化授权提供者和资产注册表
     *
     * @param provider 授权提供者，用于处理令牌验证和授权逻辑
     * @param registry 资产注册表，用于存储和检索API资产信息
     */
    public AuthorizeFilter(AuthorizeProvider provider, AssetsRegistry registry) {
        this.provider = provider;
        this.registry = registry;
    }

    /**
     * 内部过滤方法，执行授权验证逻辑
     * <p>
     * 该方法是过滤器的核心实现，负责执行完整的授权验证流程，包括： 1. 从请求中提取参数并设置上下文 2. 根据方法和版本查找对应的资产 3. 验证HTTP方法是否匹配 4. 如果需要，验证访问令牌 5. 验证应用ID是否匹配
     * 6. 填充和清理请求参数 7. 将资产信息设置到上下文中
     * </p>
     *
     * @param exchange 当前的 ServerWebExchange 对象，包含请求和响应信息
     * @param chain    过滤器链，用于将请求传递给下一个过滤器
     * @param context  请求上下文，包含请求相关的状态信息
     * @return {@link Mono<Void>} 表示异步处理完成，当所有验证通过后继续执行过滤器链
     */
    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        // 从上下文中获取请求参数映射
        Map<String, String> params = getRequestMap(context);

        // 设置上下文中的格式、通道和令牌信息
        context.setFormat(Format.valueOf(StringKit.toUpperCase(params.get(Config.FORMAT))));
        context.setChannel(Channel.get(params.get(Config.X_REMOTE_CHANNEL)));
        context.setToken(exchange.getRequest().getHeaders().getFirst(Config.X_ACCESS_TOKEN));

        // 获取请求方法和版本
        String method = params.get(Config.METHOD);
        String version = params.get(Config.VERSION);

        // 从注册表中获取对应的资产信息
        Assets assets = registry.get(method, version);
        if (null == assets) {
            Format.warn(
                    exchange,
                    "AUTH_ASSETS_NOT_FOUND",
                    "Assets not found for method: " + method + ", version: " + version);
            return Mono.error(new ValidateException(ErrorCode._100800));
        }

        // 请求基础校验
        this.method(exchange, assets);
        if (Consts.TYPE_ONE != assets.getFirewall()) {
            // 执行认证
            this.authorize(exchange, context, assets);
        }

        // 将资产信息设置到上下文中
        context.setAssets(assets);

        // 记录验证通过的信息
        Format.info(exchange, "AUTH_VALIDATED", "Method: " + method + ", Version: " + version);

        // 继续执行过滤器链
        return chain.filter(exchange);
    }

    /**
     * 校验请求的 HTTP 方法是否匹配资产配置
     * <p>
     * 该方法检查当前请求的HTTP方法是否与资产配置中要求的方法一致。 如果不匹配，将根据期望的HTTP方法类型抛出不同的业务异常。
     * </p>
     *
     * @param exchange ServerWebExchange 对象，包含请求和响应信息
     * @param assets   资产信息，包含期望的HTTP方法类型
     * @throws ValidateException 如果方法不匹配，抛出对应错误
     */
    protected void method(ServerWebExchange exchange, Assets assets) {
        ServerHttpRequest request = exchange.getRequest();

        final HttpMethod expectedMethod = this.valueOf(assets.getType());

        if (!Objects.equals(request.getMethod(), expectedMethod)) {
            String errors = "HTTP method mismatch, expected: " + expectedMethod + ", actual: " + request.getMethod();
            Format.warn(exchange, "AUTH_METHOD_MISMATCH", errors);

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
     * 校验令牌（如果资产要求）并将认证结果参数填充到请求参数中
     * <p>
     * 如果资产配置要求令牌验证，该方法将检查请求中是否包含有效的访问令牌。 如果令牌存在且有效，将从授权结果中提取用户信息并添加到请求参数中。 如果令牌缺失或无效，将抛出相应的业务异常。
     * </p>
     *
     * @param exchange ServerWebExchange 对象，包含请求和响应信息
     * @param context  上下文对象，包含令牌和通道信息
     * @param assets   资产信息，指示是否需要令牌验证
     * @throws ValidateException 如果令牌缺失或认证失败
     */
    protected void authorize(ServerWebExchange exchange, Context context, Assets assets) {
        if (Consts.TYPE_ONE == assets.getToken()) {
            // 检查令牌是否存在
            if (StringKit.isBlank(context.getToken())) {
                Format.warn(exchange, "AUTH_TOKEN_MISSING", "Access token is missing");
                throw new ValidateException(ErrorCode._100106);
            }

            // 创建令牌对象并进行授权验证
            Delegate delegate = provider.authorize(
                    Principal.builder().type(Consts.TYPE_ONE).value(context.getToken())
                            .channel(context.getChannel().getType()).assets(assets).build());

            // 处理授权结果
            if (delegate.isOk()) {
                Authorize auth = delegate.getAuthorize();
                Map<String, Object> map = new HashMap<>();
                // 将授权信息转换为Map并添加到请求参数中
                BeanKit.beanToMap(auth, map, CopyOptions.of().setTransientSupport(false).setIgnoreCase(true));
                map.forEach((k, v) -> context.getRequestMap().put(k, v.toString()));
                Format.info(
                        exchange,
                        "AUTH_TOKEN_VALIDATED",
                        "Token validated successfully for channel: " + context.getChannel().getType());
            } else {
                // 令牌验证失败
                Format.error(
                        exchange,
                        "AUTH_TOKEN_FAILED",
                        "Error code: " + delegate.getMessage().errcode + ", message: " + delegate.getMessage().errmsg);
                throw new ValidateException(delegate.getMessage().errcode, delegate.getMessage().errmsg);
            }
        }
        if (Consts.TYPE_ONE == assets.getScope()) {
            // 优先从请求参数中获取应用ID
            String[] api_key_params = { "apiKey", "api_key", "x_api_key", "api_id", "x_api_id", "X-API-ID", "X-API-KEY",
                    "API-KEY", "API-ID" };
            String apiKey = MapKit.getFirstNonNull(context.getRequestMap(), api_key_params);
            // 如果请求参数中没有，尝试从请求头中获取
            if (StringKit.isBlank(apiKey)) {
                apiKey = MapKit.getFirstNonNull(context.getHeaderMap(), api_key_params);
            }

            // 如果仍然没有找到应用ID，抛出异常
            if (StringKit.isBlank(apiKey)) {
                Format.warn(exchange, "AUTH_APIKEY_MISSING", "No Api Key provided in the request");
                throw new ValidateException(ErrorCode._100805);
            }

            // 创建apiKey对象并进行授权验证
            Delegate delegate = provider.authorize(
                    Principal.builder().type(Consts.TYPE_TWO).value(apiKey).channel(context.getChannel().getType())
                            .assets(assets).build());

            // 处理授权结果
            if (delegate.isOk()) {
                Authorize auth = delegate.getAuthorize();
                Map<String, Object> map = new HashMap<>();
                // 将授权信息转换为Map并添加到请求参数中
                BeanKit.beanToMap(auth, map, CopyOptions.of().setTransientSupport(false).setIgnoreCase(true));
                map.forEach((k, v) -> context.getRequestMap().put(k, v.toString()));
                Format.info(
                        exchange,
                        "AUTH_TOKEN_VALIDATED",
                        "Token validated successfully for channel: " + context.getChannel().getType());
            } else {
                // apiKey验证失败
                Format.error(
                        exchange,
                        "AUTH_TOKEN_FAILED",
                        "Error code: " + delegate.getMessage().errcode + ", message: " + delegate.getMessage().errmsg);
                throw new ValidateException(delegate.getMessage().errcode, delegate.getMessage().errmsg);
            }
        }
    }

}
