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
