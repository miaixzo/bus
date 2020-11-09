package org.aoju.bus.goalie.reactor.filter;

import org.aoju.bus.base.consts.ErrorCode;
import org.aoju.bus.core.lang.exception.BusinessException;
import org.aoju.bus.goalie.reactor.Asset;
import org.aoju.bus.goalie.reactor.Athlete;
import org.aoju.bus.goalie.reactor.Constant;
import org.aoju.bus.goalie.reactor.ExchangeContext;
import org.aoju.bus.starter.goalie.ReactorConfiguration;
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
@ConditionalOnBean(ReactorConfiguration.class)
@Order(FilterOrders.PERMISSION)
public class PermissionFilter implements WebFilter {

  @Autowired
  Athlete athlete;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    ExchangeContext context = ExchangeContext.get(exchange);

    Map<String, String> params = context.getRequestMap();

    String method = params.get(Constant.METHOD);
    String version = params.get(Constant.VERSION);

    Set<Asset> assets = athlete.getAssets();

    List<Asset> assetsList = assets.parallelStream()
      .filter(asset -> Objects.equals(method, asset.getMethod())).collect(Collectors.toList());

    if (assetsList.size() < 1) {
      return Mono.error(new BusinessException(ErrorCode.EM_100103));
    }

    Asset asset = assetsList.parallelStream()
      .filter(c -> Objects.equals(version, c.getVersion())).findFirst()
      .orElseThrow(() -> new BusinessException(ErrorCode.EM_100102));

    context.setAsset(asset);

    return chain.filter(exchange);
  }
}
