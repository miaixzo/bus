package org.aoju.bus.starter.goalie.filter;

import org.aoju.bus.base.consts.ErrorCode;
import org.aoju.bus.base.entity.OAuth2;
import org.aoju.bus.core.lang.exception.BusinessException;
import org.aoju.bus.core.toolkit.BeanKit;
import org.aoju.bus.goalie.reactor.Asset;
import org.aoju.bus.goalie.reactor.Constant;
import org.aoju.bus.goalie.reactor.ExchangeContext;
import org.aoju.bus.goalie.reactor.login.AuthProvider;
import org.aoju.bus.goalie.reactor.login.LoginResponse;
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
 * 权限认证
 *
 * @author Justubborn
 * @since 2020/11/6
 */
@Component
@ConditionalOnBean(ReactorConfiguration.class)
@Order(FilterOrders.AUTH)
public class AuthFilter implements WebFilter {

  @Autowired
  AuthProvider provider;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    ExchangeContext context = ExchangeContext.get(exchange);
    Asset asset = context.getAsset();
    Map<String, String> requestMap = context.getRequestMap();
    if (asset.isToken()) {
      String token = exchange.getRequest().getHeaders().getFirst(Constant.X_ACCESS_TOKEN);
      LoginResponse response = provider.authorize(token);
      if (response.isOk()) {
        OAuth2 auth2 = response.getOAuth2();
        Map<String, Object> map = BeanKit.beanToMap(auth2, false, true);
        map.forEach((k, v) -> requestMap.put(k, v.toString()));
      } else {
        throw new BusinessException(ErrorCode.EM_FAILURE, response.getMessage().errmsg);
      }


    }
    return chain.filter(exchange);
  }

}
