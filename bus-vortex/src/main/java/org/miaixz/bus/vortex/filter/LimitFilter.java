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
package org.miaixz.bus.vortex.filter;

import java.util.HashSet;
import java.util.Set;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Context;
import org.miaixz.bus.vortex.magic.Limiter;
import org.miaixz.bus.vortex.registry.LimiterRegistry;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Rate limiting filter that applies traffic restrictions to requests based on the token bucket algorithm.
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class LimitFilter extends AbstractFilter {

    /**
     * The registry for rate limiters.
     */
    private final LimiterRegistry registry;

    /**
     * Constructs a {@code LimitFilter} with the specified rate limiter registry.
     *
     * @param registry The rate limiter registry.
     */
    public LimitFilter(LimiterRegistry registry) {
        this.registry = registry;
    }

    /**
     * Internal filtering method, executing the rate limiting logic.
     *
     * @param exchange The current {@link ServerWebExchange} object.
     * @param chain    The filter chain.
     * @param context  The request context.
     * @return {@link Mono<Void>} indicating the asynchronous completion of processing.
     */
    @Override
    protected Mono<Void> doFilter(ServerWebExchange exchange, WebFilterChain chain, Context context) {
        String ip = getRequestMap(context).get("x-remote_ip");
        String methodVersion = getAssets(context).getMethod() + getAssets(context).getVersion();
        Set<Limiter> cfgList = getLimiter(methodVersion, ip);
        for (Limiter cfg : cfgList) {
            cfg.acquire();
        }
        Logger.info(
                "==>     Filter: Rate limit applied - Path: {}, Method: {}",
                exchange.getRequest().getURI().getPath(),
                methodVersion);
        return chain.filter(exchange);
    }

    /**
     * Retrieves the applicable rate limiter configurations.
     *
     * @param methodVersion The combination of method name and version number.
     * @param ip            The IP address of the request.
     * @return A set of applicable {@link Limiter} configurations.
     */
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
