package org.aoju.bus.starter.goalie.filter;

import org.aoju.bus.goalie.reactor.ExchangeContext;
import org.aoju.bus.starter.goalie.ReactorConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Objects;


/**
 * 参数过滤
 *
 * @author Justubborn
 * @since 2020/10/29
 */
@Component
@ConditionalOnBean(ReactorConfiguration.class)
@Order(FilterOrders.FIRST)
public class FirstFilter implements WebFilter {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();
    if (Objects.equals(request.getMethod(), HttpMethod.GET)) {
      MultiValueMap<String, String> params = request.getQueryParams();
      ExchangeContext.get(exchange).setRequestMap(params.toSingleValueMap());
      return chain.filter(exchange);
    } else {
      return exchange.getFormData().flatMap(params -> {
        ExchangeContext.get(exchange).setRequestMap(params.toSingleValueMap());
        return chain.filter(exchange);
      });
    }
  }

}
