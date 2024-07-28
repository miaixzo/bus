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

import org.miaixz.bus.goalie.Assets;
import org.miaixz.bus.goalie.Context;
import org.miaixz.bus.goalie.metric.Limiter;
import org.miaixz.bus.goalie.registry.LimiterRegistry;
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
 * @since Java 17+
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
        String[] limitKeys = new String[] { methodVersion, ip + methodVersion };
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
