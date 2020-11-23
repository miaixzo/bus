/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.starter.goalie.filter;

import org.aoju.bus.base.consts.ErrorCode;
import org.aoju.bus.base.entity.OAuth2;
import org.aoju.bus.core.lang.exception.BusinessException;
import org.aoju.bus.core.toolkit.BeanKit;
import org.aoju.bus.goalie.Assets;
import org.aoju.bus.goalie.Consts;
import org.aoju.bus.goalie.Context;
import org.aoju.bus.goalie.metric.Authorize;
import org.aoju.bus.goalie.metric.Delegate;
import org.aoju.bus.starter.goalie.GoalieConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 权限认证
 *
 * @author Justubborn
 * @since 2020/11/6
 */
@Component
@ConditionalOnBean(GoalieConfiguration.class)
@Order(FilterOrders.AUTH)
public class AuthFilter implements WebFilter {

    @Autowired
    Authorize authorize;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Context context = Context.get(exchange);
        Assets assets = context.getAssets();
        Map<String, String> requestMap = context.getRequestMap();
        if (assets.isToken()) {
            String token = exchange.getRequest().getHeaders().getFirst(Consts.X_ACCESS_TOKEN);
            Delegate delegate = authorize.authorize(token);
            if (delegate.isOk()) {
                OAuth2 auth2 = delegate.getOAuth2();
                Map<String, Object> map = BeanKit.beanToMap(auth2, false, true);
                map.forEach((k, v) -> requestMap.put(k, v.toString()));
            } else {
                throw new BusinessException(ErrorCode.EM_FAILURE, delegate.getMessage().errmsg);
            }


        }
        return chain.filter(exchange);
    }

}
