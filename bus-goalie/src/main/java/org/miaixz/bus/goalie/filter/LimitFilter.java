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
package org.miaixz.bus.goalie.filter;

import java.util.HashSet;
import java.util.Set;

import org.miaixz.bus.goalie.Assets;
import org.miaixz.bus.goalie.Context;
import org.miaixz.bus.goalie.magic.Limiter;
import org.miaixz.bus.goalie.registry.LimiterRegistry;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * 限流过滤器，基于令牌桶算法对请求进行流量限制
 *
 * @author Justubborn
 * @since Java 17+
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 3)
public class LimitFilter implements WebFilter {

    /**
     * 限流注册表，用于获取限流配置
     */
    private final LimiterRegistry limiterRegistry;

    /**
     * 构造器，初始化限流注册表
     *
     * @param limiterRegistry 限流注册表
     */
    public LimitFilter(LimiterRegistry limiterRegistry) {
        this.limiterRegistry = limiterRegistry;
    }

    /**
     * 过滤器主逻辑，应用限流规则并继续处理请求
     *
     * @param exchange 当前的 ServerWebExchange 对象，包含请求和响应
     * @param chain    过滤器链，用于继续处理请求
     * @return {@link Mono<Void>} 表示异步处理完成
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 获取上下文和资产信息
        Context context = Context.get(exchange);
        Assets assets = context.getAssets();
        // 获取请求的 IP 地址
        String ip = context.getRequestMap().get("x-remote-ip");
        // 获取适用的限流配置
        Set<Limiter> cfgList = getLimiter(assets.getMethod() + assets.getVersion(), ip);
        // 应用所有限流规则
        for (Limiter cfg : cfgList) {
            cfg.acquire(); // 尝试获取令牌，可能阻塞
        }
        // 继续执行过滤器链
        return chain.filter(exchange);
    }

    /**
     * 获取适用的限流配置
     *
     * @param methodVersion 方法名和版本号的组合
     * @param ip            请求的 IP 地址
     * @return 适用的限流配置集合
     */
    private Set<Limiter> getLimiter(String methodVersion, String ip) {
        // 定义限流键：方法版本号和 IP+方法版本号
        String[] limitKeys = new String[] { methodVersion, ip + methodVersion };
        Set<Limiter> limitCfgList = new HashSet<>();
        // 遍历键，获取对应的限流配置
        for (String limitKey : limitKeys) {
            Limiter limitCfg = limiterRegistry.get(limitKey);
            if (null != limitCfg) {
                limitCfgList.add(limitCfg);
            }
        }
        return limitCfgList;
    }

}