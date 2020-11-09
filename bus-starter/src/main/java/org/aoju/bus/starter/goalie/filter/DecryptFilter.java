package org.aoju.bus.starter.goalie.filter;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.goalie.reactor.ExchangeContext;
import org.aoju.bus.starter.goalie.GoalieProperties;
import org.aoju.bus.starter.goalie.ReactorConfiguration;
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
 * 解密
 *
 * @author Justubborn
 * @since 2020/11/7
 */
@Component
@ConditionalOnBean(ReactorConfiguration.class)
@Order(FilterOrders.DECRYPT)
public class DecryptFilter implements WebFilter {
    @Autowired
    GoalieProperties.Server.Decrypt decrypt;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerWebExchange.Builder builder = exchange.mutate();
        if (decrypt.isEnabled()) {
            doDecrypt(ExchangeContext.get(exchange).getRequestMap());
        }

        return chain.filter(builder.build());
    }

    /**
     * 解密
     *
     * @param map 参数
     */
    private void doDecrypt(Map<String, String> map) {
        map.forEach((k, v) -> map.put(k, org.aoju.bus.crypto.Builder.decrypt(encrypt.getType(), encrypt.getKey(), v, Charset.UTF_8)));
    }

}
