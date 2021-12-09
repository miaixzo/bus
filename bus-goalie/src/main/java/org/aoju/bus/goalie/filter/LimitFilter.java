package org.aoju.bus.goalie.filter;

import org.aoju.bus.goalie.Assets;
import org.aoju.bus.goalie.Context;
import org.aoju.bus.goalie.metric.Limiter;
import org.aoju.bus.goalie.registry.LimiterRegistry;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

/**
 * 限流
 *
 * @author Justubborn
 * @version 6.3.2
 * @since JDK 1.8+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class LimitFilter implements WebFilter {

    private final LimiterRegistry limiterRegistry;

    public LimitFilter(LimiterRegistry limiterRegistry) {
        this.limiterRegistry = limiterRegistry;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Context context = Context.get(exchange);
        Assets assets = context.getAssets();
        String ip = context.getRequestMap().get("x-remote-ip");
        Set<Limiter> cfgList = getLimiter(assets.getMethod() + assets.getVersion(), ip);
        for (Limiter cfg : cfgList) {
            cfg.acquire();
        }
        return chain.filter(exchange);
    }

    private Set<Limiter> getLimiter(String methodVersion, String ip) {
        String[] limitKeys = new String[]{
                methodVersion,
                ip + methodVersion
        };
        Set<Limiter> limitCfgList = new HashSet<>();
        for (String limitKey : limitKeys) {
            Limiter limitCfg = limiterRegistry.get(limitKey);
            if (null != limitCfg) {
                limitCfgList.add(limitCfg);
            }
        }
        return limitCfgList;
    }

}
