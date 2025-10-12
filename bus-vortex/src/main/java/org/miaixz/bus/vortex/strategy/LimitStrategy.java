/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.vortex.strategy;

import java.util.HashSet;
import java.util.Set;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.Limiter;
import org.miaixz.bus.vortex.registry.LimiterRegistry;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * A filter strategy for rate limiting. This strategy applies traffic restrictions to requests based on the token bucket
 * algorithm, using configurations from the {@link LimiterRegistry}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class LimitStrategy extends AbstractStrategy {

    private final LimiterRegistry registry;

    public LimitStrategy(LimiterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Mono<Void> apply(ServerWebExchange exchange, StrategyChain chain, Context context) {
        Assert.notNull(context, "Context must be initialized by a preceding strategy.");
        Assert.notNull(context.getAssets(), "Assets must be resolved by a preceding strategy.");

        String ip = context.getRequestMap().get("x_request_ipv4");
        String methodVersion = context.getAssets().getMethod() + context.getAssets().getVersion();

        Set<Limiter> cfgList = getLimiter(methodVersion, ip);
        for (Limiter cfg : cfgList) {
            cfg.acquire();
        }

        Logger.info(
                "==>     Strategy: Rate limit applied - Path: {}, Method: {}",
                exchange.getRequest().getURI().getPath(),
                methodVersion);

        return chain.apply(exchange);
    }

    private Set<Limiter> getLimiter(String methodVersion, String ip) {
        String[] limitKeys = new String[] { methodVersion, ip + methodVersion };
        Set<Limiter> limitCfgList = new HashSet<>();
        for (String limitKey : limitKeys) {
            Limiter limitCfg = registry.get(limitKey);
            if (null != limitCfg) {
                limitCfgList.add(limitCfg);
            }
        }
        return limitCfgList;
    }

}
