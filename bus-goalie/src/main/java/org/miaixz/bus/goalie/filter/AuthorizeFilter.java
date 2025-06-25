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
package org.miaixz.bus.goalie.filter;

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
import org.miaixz.bus.goalie.Assets;
import org.miaixz.bus.goalie.Config;
import org.miaixz.bus.goalie.Context;
import org.miaixz.bus.goalie.magic.Delegate;
import org.miaixz.bus.goalie.magic.Token;
import org.miaixz.bus.goalie.provider.AuthorizeProvider;
import org.miaixz.bus.goalie.registry.AssetsRegistry;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * 访问鉴权过滤器，负责验证请求的合法性、方法、令牌和应用 ID
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class AuthorizeFilter implements WebFilter {

    /**
     * 授权提供者，用于执行令牌认证
     */
    private final AuthorizeProvider authorizeProvider;

    /**
     * 资产注册表，用于查询请求对应的资产信息
     */
    private final AssetsRegistry registry;

    /**
     * 构造器，初始化授权提供者和资产注册表
     *
     * @param authorizeProvider 授权提供者
     * @param registry          资产注册表
     */
    public AuthorizeFilter(AuthorizeProvider authorizeProvider, AssetsRegistry registry) {
        this.authorizeProvider = authorizeProvider;
        this.registry = registry;
    }

    /**
     * 过滤器主逻辑，处理请求的鉴权和参数校验
     *
     * @param exchange 当前的 ServerWebExchange 对象，包含请求和响应
     * @param chain    过滤器链，用于继续处理请求
     * @return {@link Mono<Void>} 表示异步处理完成
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 获取请求上下文和参数
        Context context = Context.get(exchange);
        Map<String, String> params = context.getRequestMap();

        // 设置上下文的格式、渠道和令牌
        context.setFormat(Context.Format.valueOf(params.get(Config.FORMAT)));
        context.setChannel(Context.Channel.getChannel(params.get(Config.X_REMOTE_CHANNEL)));
        context.setToken(exchange.getRequest().getHeaders().getFirst(Config.X_ACCESS_TOKEN));

        // 获取方法名和版本号，查询对应的资产
        String method = params.get(Config.METHOD);
        String version = params.get(Config.VERSION);
        Assets assets = registry.getAssets(method, version);

        // 校验资产是否存在
        if (null == assets) {
            return Mono.error(new BusinessException(ErrorCode._100500));
        }

        // 校验 HTTP 方法
        checkMethod(exchange.getRequest(), assets);

        // 校验令牌（如果需要）
        checkTokenIfNecessary(context, assets, params);

        // 校验应用 ID
        checkAppId(assets, params);

        // 填充 IP 参数
        fillXParam(exchange, params);

        // 清理网关参数
        cleanParam(params);

        // 设置上下文资产
        context.setAssets(assets);

        // 继续执行过滤器链
        return chain.filter(exchange);
    }

    /**
     * 校验请求的 HTTP 方法是否匹配资产配置
     *
     * @param request 请求对象
     * @param assets  资产信息
     * @throws BusinessException 如果方法不匹配，抛出对应错误
     */
    private void checkMethod(ServerHttpRequest request, Assets assets) {
        if (!Objects.equals(request.getMethod(), assets.getHttpMethod())) {
            if (Objects.equals(assets.getHttpMethod(), HttpMethod.GET)) {
                throw new BusinessException(ErrorCode._100200);
            } else if (Objects.equals(assets.getHttpMethod(), HttpMethod.POST)) {
                throw new BusinessException(ErrorCode._100201);
            } else {
                throw new BusinessException(ErrorCode._100508);
            }
        }
    }

    /**
     * 校验令牌（如果资产要求）并将认证结果参数填充到请求参数中
     *
     * @param context 上下文对象
     * @param assets  资产信息
     * @param params  请求参数
     * @throws BusinessException 如果令牌缺失或认证失败，抛出对应错误
     */
    private void checkTokenIfNecessary(Context context, Assets assets, Map<String, String> params) {
        if (assets.isToken()) {
            if (StringKit.isBlank(context.getToken())) {
                throw new BusinessException(ErrorCode._100106);
            }
            // 创建令牌对象并进行认证
            Token access = new Token(context.getToken(), context.getChannel().getTokenType(), assets);
            Delegate delegate = authorizeProvider.authorize(access);
            if (delegate.isOk()) {
                // 认证成功，将 Authorize 信息转换为参数
                Authorize auth = delegate.getAuthorize();
                Map<String, Object> map = new HashMap<>();
                BeanKit.beanToMap(auth, map, CopyOptions.of().setTransientSupport(false).setIgnoreCase(true));
                map.forEach((k, v) -> params.put(k, v.toString()));
            } else {
                // 认证失败，抛出错误
                throw new BusinessException(delegate.getMessage().errcode, delegate.getMessage().errmsg);
            }
        }
    }

    /**
     * 清理网关相关参数
     *
     * @param params 请求参数
     */
    private void cleanParam(Map<String, String> params) {
        params.remove(Config.METHOD);
        params.remove(Config.FORMAT);
        params.remove(Config.VERSION);
        params.remove(Config.SIGN);
    }

    /**
     * 填充 IP 参数到请求参数中
     *
     * @param exchange     ServerWebExchange 对象
     * @param requestParam 请求参数
     */
    private void fillXParam(ServerWebExchange exchange, Map<String, String> requestParam) {
        String ip = exchange.getRequest().getHeaders().getFirst("x_remote_ip");
        if (StringKit.isBlank(ip)) {
            // 尝试从 X-Forwarded-For 获取 IP
            ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (!StringKit.isBlank(ip)) {
                ip = ip.contains(Symbol.COMMA) ? ip.split(Symbol.COMMA)[0] : ip;
            } else {
                // 回退到远程地址
                InetSocketAddress address = exchange.getRequest().getRemoteAddress();
                if (null != address) {
                    ip = address.getAddress().getHostAddress();
                }
            }
            requestParam.put("x_remote_ip", ip);
        }
    }

    /**
     * 校验应用 ID 是否匹配
     *
     * @param assets       资产信息
     * @param requestParam 请求参数
     * @throws BusinessException 如果应用 ID 不匹配，抛出错误
     */
    private void checkAppId(Assets assets, Map<String, String> requestParam) {
        String appId = assets.getMethod().split("\\.")[0];
        requestParam.putIfAbsent("x_app_id", appId);
        String xAppId = requestParam.get("x_app_id");
        if (StringKit.isNotBlank(xAppId) && !appId.equals(xAppId)) {
            throw new BusinessException(ErrorCode._100511);
        }
    }

}