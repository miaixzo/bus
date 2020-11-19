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
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.goalie.reactor.Constant;
import org.aoju.bus.goalie.reactor.ExchangeContext;
import org.aoju.bus.starter.goalie.ReactorConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 参数检查
 *
 * @author Justubborn
 * @since 2020/11/6
 */
@Component
@ConditionalOnBean(ReactorConfiguration.class)
@Order(FilterOrders.PARAMETER_CHECK)
public class ParameterCheckFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ExchangeContext context = ExchangeContext.get(exchange);
        Map<String, String> params = context.getRequestMap();

        if (StringKit.isBlank(params.get(Constant.METHOD))) {
            throw new BusinessException(ErrorCode.EM_100108);
        }
        if (StringKit.isBlank(params.get(Constant.VERSION))) {
            throw new BusinessException(ErrorCode.EM_100107);
        }
        return chain.filter(exchange);
    }

}
