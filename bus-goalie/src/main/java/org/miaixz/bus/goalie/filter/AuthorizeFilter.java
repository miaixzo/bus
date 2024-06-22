/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.basics.entity.OAuth2;
import org.miaixz.bus.core.basics.normal.ErrorCode;
import org.miaixz.bus.core.beans.copier.CopyOptions;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.BusinessException;
import org.miaixz.bus.core.xyz.BeanKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.goalie.Assets;
import org.miaixz.bus.goalie.Config;
import org.miaixz.bus.goalie.Context;
import org.miaixz.bus.goalie.metric.Authorize;
import org.miaixz.bus.goalie.metric.Delegate;
import org.miaixz.bus.goalie.metric.Token;
import org.miaixz.bus.goalie.registry.AssetsRegistry;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 访问鉴权
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class AuthorizeFilter implements WebFilter {

    private final Authorize authorize;

    private final AssetsRegistry registry;

    public AuthorizeFilter(Authorize authorize, AssetsRegistry registry) {
        this.authorize = authorize;
        this.registry = registry;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Context context = Context.get(exchange);
        Map<String, String> params = context.getRequestMap();

        context.setFormat(Context.Format.valueOf(params.get(Config.FORMAT)));
        context.setChannel(Context.Channel.getChannel(params.get(Config.X_REMOTE_CHANNEL)));
        context.setToken(exchange.getRequest().getHeaders().getFirst(Config.X_ACCESS_TOKEN));

        String method = params.get(Config.METHOD);
        String version = params.get(Config.VERSION);
        Assets assets = registry.getAssets(method, version);

        if (null == assets) {
            return Mono.error(new BusinessException(ErrorCode.EM_100500));
        }
        //校验方法
        checkMethod(exchange.getRequest(), assets);
        //校验参数
        checkTokenIfNecessary(context, assets, params);
        //校验 appid
        checkAppId(assets, params);
        //填充Ip
        fillXParam(exchange, params);

        //清理 method 和 version
        cleanParam(params);
        context.setAssets(assets);

        return chain.filter(exchange);
    }

    /**
     * 校验方法
     *
     * @param request 请求
     * @param assets  路由
     */
    private void checkMethod(ServerHttpRequest request, Assets assets) {
        if (!Objects.equals(request.getMethod(), assets.getHttpMethod())) {
            if (Objects.equals(assets.getHttpMethod(), HttpMethod.GET)) {
                throw new BusinessException(ErrorCode.EM_100200);
            } else if (Objects.equals(assets.getHttpMethod(), HttpMethod.POST)) {
                throw new BusinessException(ErrorCode.EM_100201);
            } else {
                throw new BusinessException(ErrorCode.EM_100508);
            }

        }
    }

    /**
     * 校验 token 并 填充参数
     *
     * @param context 请求
     * @param assets  路由
     * @param params  参数
     */
    private void checkTokenIfNecessary(Context context, Assets assets, Map<String, String> params) {
        // 访问授权校验
        if (assets.isToken()) {
            if (StringKit.isBlank(context.getToken())) {
                throw new BusinessException(ErrorCode.EM_100106);
            }
            Token access = new Token(context.getToken(), context.getChannel().getTokenType(), assets);
            Delegate delegate = authorize.authorize(access);
            if (delegate.isOk()) {
                OAuth2 auth2 = delegate.getOAuth2();
                Map<String, Object> map = new HashMap<>();
                BeanKit.beanToMap(auth2, map, CopyOptions.of().setTransientSupport(false).ignoreNullValue());
                map.forEach((k, v) -> params.put(k, v.toString()));
            } else {
                throw new BusinessException(delegate.getMessage().errcode, delegate.getMessage().errmsg);
            }
        }
    }

    /**
     * 清理网关参数
     *
     * @param params 参数
     */
    private void cleanParam(Map<String, String> params) {
        params.remove(Config.METHOD);
        params.remove(Config.FORMAT);
        params.remove(Config.VERSION);
        params.remove(Config.SIGN);
    }

    /**
     * 填充参数
     *
     * @param exchange     exchange
     * @param requestParam 请求参数
     */
    private void fillXParam(ServerWebExchange exchange, Map<String, String> requestParam) {
        String ip = exchange.getRequest().getHeaders().getFirst("x_remote_ip");
        if (StringKit.isBlank(ip)) {
            ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (!StringKit.isBlank(ip)) {
                ip = ip.contains(Symbol.COMMA) ? ip.split(Symbol.COMMA)[0] : ip;
            } else {
                InetSocketAddress address = exchange.getRequest().getRemoteAddress();
                if (null != address) {
                    ip = address.getAddress().getHostAddress();
                }
            }
            requestParam.put("x_remote_ip", ip);
        }
    }

    /**
     * 设置appid
     *
     * @param assets       资源
     * @param requestParam 参数
     */
    private void checkAppId(Assets assets, Map<String, String> requestParam) {
        String appId = assets.getMethod().split("\\.")[0];
        requestParam.putIfAbsent("x_app_id", appId);
        String xAppId = requestParam.get("x_app_id");
        if (StringKit.isNotBlank(xAppId) && !appId.equals(xAppId)) {
            throw new BusinessException(ErrorCode.EM_100511);
        }

    }

}
