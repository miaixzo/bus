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
import org.aoju.bus.core.lang.exception.BusinessException;
import org.aoju.bus.goalie.Assets;
import org.aoju.bus.goalie.Athlete;
import org.aoju.bus.goalie.Consts;
import org.aoju.bus.goalie.Context;
import org.aoju.bus.starter.goalie.GoalieConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 鉴权
 *
 * @author Justubborn
 * @since 2020/11/7
 */
@Component
@ConditionalOnBean(GoalieConfiguration.class)
@Order(FilterOrders.PERMISSION)
public class PermissionFilter implements WebFilter {

    @Autowired
    Athlete athlete;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Context context = Context.get(exchange);

        Map<String, String> params = context.getRequestMap();

        String method = params.get(Consts.METHOD);
        String version = params.get(Consts.VERSION);

        Set<Assets> assets = athlete.getAssets();

        List<Assets> assetsList = assets.parallelStream()
                .filter(asset -> Objects.equals(method, asset.getMethod())).collect(Collectors.toList());

        if (assetsList.size() < 1) {
            return Mono.error(new BusinessException(ErrorCode.EM_100103));
        }

        Assets asset = assetsList.parallelStream()
                .filter(c -> Objects.equals(version, c.getVersion())).findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.EM_100102));

        context.setAssets(asset);

        return chain.filter(exchange);
    }

}
