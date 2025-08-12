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

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.miaixz.bus.core.basic.entity.Authorize;
import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.bean.copier.CopyOptions;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.BusinessException;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.vortex.*;
import org.miaixz.bus.vortex.magic.Delegate;
import org.miaixz.bus.vortex.magic.Token;
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
            Format.warn(exchange, "AUTH_ASSETS_NOT_FOUND",
                    "Assets not found for method: " + method + ", version: " + version);
            return Mono.error(new BusinessException(ErrorCode._100500, "Assets not found"));
        }

        // 执行各种验证
        checkMethod(exchange, assets);
        checkToken(exchange, context, assets, params);
        checkAppId(exchange, assets, params);

        // 填充额外参数并清理不需要的参数
        fillXParam(exchange, params);
        cleanParam(params);

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
     * @throws BusinessException 如果方法不匹配，抛出对应错误： - 对于GET方法，错误码为_100200 - 对于POST方法，错误码为_100201 - 对于其他方法，错误码为_100508
     */
    protected void checkMethod(ServerWebExchange exchange, Assets assets) {
        ServerHttpRequest request = exchange.getRequest();
        if (!Objects.equals(request.getMethod(), assets.getHttpMethod())) {
            String error = "HTTP method mismatch, expected: " + assets.getHttpMethod() + ", actual: "
                    + request.getMethod();
            Format.warn(exchange, "AUTH_METHOD_MISMATCH", error);
            if (Objects.equals(assets.getHttpMethod(), HttpMethod.GET)) {
                throw new BusinessException(ErrorCode._100200, error);
            } else if (Objects.equals(assets.getHttpMethod(), HttpMethod.POST)) {
                throw new BusinessException(ErrorCode._100201, error);
            } else {
                throw new BusinessException(ErrorCode._100508, error);
            }
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
     * @param params   请求参数，将用于存储认证结果
     * @throws BusinessException 如果令牌缺失或认证失败，抛出对应错误： - 令牌缺失时，错误码为_100106 - 令牌认证失败时，使用授权提供者返回的错误码和消息
     */
    protected void checkToken(ServerWebExchange exchange, Context context, Assets assets, Map<String, String> params) {
        if (assets.isToken()) {
            // 检查令牌是否存在
            if (StringKit.isBlank(context.getToken())) {
                Format.warn(exchange, "AUTH_TOKEN_MISSING", "Access token is missing");
                throw new BusinessException(ErrorCode._100106, "Access token is missing");
            }

            // 创建令牌对象并进行授权验证
            Token access = new Token(context.getToken(), context.getChannel().getType(), assets);
            Delegate delegate = provider.authorize(access);

            // 处理授权结果
            if (delegate.isOk()) {
                Authorize auth = delegate.getAuthorize();
                Map<String, Object> map = new HashMap<>();
                // 将授权信息转换为Map并添加到请求参数中
                BeanKit.beanToMap(auth, map, CopyOptions.of().setTransientSupport(false).setIgnoreCase(true));
                map.forEach((k, v) -> params.put(k, v.toString()));
                Format.info(exchange, "AUTH_TOKEN_VALIDATED",
                        "Token validated successfully for channel: " + context.getChannel().getType());
            } else {
                // 令牌验证失败
                Format.error(exchange, "AUTH_TOKEN_FAILED",
                        "Error code: " + delegate.getMessage().errcode + ", message: " + delegate.getMessage().errmsg);
                throw new BusinessException(delegate.getMessage().errcode, delegate.getMessage().errmsg);
            }
        }
    }

    /**
     * 校验应用 ID 是否匹配
     * <p>
     * 该方法验证请求中的应用ID是否与资产配置中的应用ID一致。 资产的应用ID是从方法名中提取的（方法名的第一部分）。 如果请求中未提供应用ID，会自动设置默认值。 如果提供了应用ID但不匹配，将抛出业务异常。
     * </p>
     *
     * @param exchange     ServerWebExchange 对象，包含请求和响应信息
     * @param assets       资产信息，从中提取期望的应用ID
     * @param requestParam 请求参数，包含或将要包含应用ID
     * @throws BusinessException 如果应用ID不匹配，抛出错误码为_100511的业务异常
     */
    protected void checkAppId(ServerWebExchange exchange, Assets assets, Map<String, String> requestParam) {
        // 从方法名中提取应用ID（方法名的第一部分）
        String appId = assets.getMethod().split("\\.")[0];

        // 如果请求参数中没有应用ID，设置默认值
        requestParam.putIfAbsent("x_app_id", appId);

        // 获取请求中的应用ID并验证
        String xAppId = requestParam.get("x_app_id");
        if (StringKit.isNotBlank(xAppId) && !appId.equals(xAppId)) {
            Format.warn(exchange, "AUTH_APPID_MISMATCH", "App ID mismatch, expected: " + appId + ", actual: " + xAppId);
            throw new BusinessException(ErrorCode._100511, "App ID mismatch");
        }
    }

    /**
     * 清理网关相关参数
     * <p>
     * 该方法从请求参数中移除网关内部使用的参数，这些参数不应该传递给后续的业务处理。 清理的参数包括：method、format、version和sign。
     * </p>
     *
     * @param params 请求参数，将从中移除网关内部参数
     */
    private void cleanParam(Map<String, String> params) {
        params.remove(Config.METHOD);
        params.remove(Config.FORMAT);
        params.remove(Config.VERSION);
        params.remove(Config.SIGN);
    }

    /**
     * 填充 IP 参数到请求参数中
     * <p>
     * 该方法尝试从请求中提取客户端IP地址，并将其添加到请求参数中。 IP地址的提取顺序为： 1. 首先检查"x_remote_ip"请求头 2. 如果不存在，检查"X-Forwarded-For"请求头（处理代理情况） 3.
     * 如果仍不存在，使用请求的远程地址
     * </p>
     *
     * @param exchange     ServerWebExchange 对象，包含请求和响应信息
     * @param requestParam 请求参数，将向其中添加IP地址信息
     */
    protected void fillXParam(ServerWebExchange exchange, Map<String, String> requestParam) {
        String ip = exchange.getRequest().getHeaders().getFirst("x_remote_ip");
        if (StringKit.isBlank(ip)) {
            // 尝试从X-Forwarded-For头获取IP
            ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (!StringKit.isBlank(ip)) {
                // 如果有多个IP（经过多层代理），取第一个
                ip = ip.contains(Symbol.COMMA) ? ip.split(Symbol.COMMA)[0] : ip;
            } else {
                // 使用请求的远程地址
                InetSocketAddress address = exchange.getRequest().getRemoteAddress();
                if (null != address) {
                    ip = address.getAddress().getHostAddress();
                }
            }
            // 将IP地址添加到请求参数中
            requestParam.put("x_remote_ip", ip);
        }
    }

}