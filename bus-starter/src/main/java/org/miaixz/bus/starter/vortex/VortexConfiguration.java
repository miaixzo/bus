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
package org.miaixz.bus.starter.vortex;

import java.util.List;

import org.miaixz.bus.vortex.Config;
import org.miaixz.bus.vortex.Filter;
import org.miaixz.bus.vortex.Handler;
import org.miaixz.bus.vortex.Vortex;
import org.miaixz.bus.vortex.filter.*;
import org.miaixz.bus.vortex.handler.VortexHandler;
import org.miaixz.bus.vortex.handler.ErrorsHandler;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;
import org.miaixz.bus.vortex.registry.LimiterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import jakarta.annotation.Resource;
import reactor.netty.http.server.HttpServer;

/**
 * 路由自动配置类，负责配置 WebFlux 路由和拦截器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ConditionalOnWebApplication
@EnableConfigurationProperties(value = { VortexProperties.class })
public class VortexConfiguration {

    @Resource
    VortexProperties properties;

    /**
     * 自动注入所有 filter 实现
     */
    @Resource
    List<Filter> filters;

    /**
     * 自动注入所有 Handler 实现
     */
    @Resource
    List<Handler> handlers;

    /**
     * 配置主过滤器
     *
     * @return WebFilter 主过滤器实例
     */
    @Bean
    public Filter primaryFilter() {
        return new PrimaryFilter();
    }

    /**
     * 配置格式化过滤器
     *
     * @return WebFilter 格式化过滤器实例
     */
    @Bean
    public Filter formatFilter() {
        return new FormatFilter();
    }

    /**
     * 创建安全加解密过滤器
     * <p>
     * 当解密或加密任一功能启用时创建该Bean
     * </p>
     *
     * @return 安全加解密过滤器实例
     */
    @Bean
    public Filter cipherFilter() {
        return new CipherFilter(this.properties.getServer().getDecrypt(), this.properties.getServer().getEncrypt());
    }

    /**
     * 配置授权过滤器
     *
     * @param authorizeProvider 授权提供者
     * @param registry          资产注册表
     * @return WebFilter 授权过滤器实例
     */
    @Bean
    public Filter authorizeFilter(AuthorizeProvider authorizeProvider, AssetsRegistry registry) {
        return new AuthorizeFilter(authorizeProvider, registry);
    }

    /**
     * 配置限流过滤器，根据配置决定是否启用
     *
     * @param registry 限流注册表
     * @return WebFilter 限流过滤器实例，若未启用返回 null
     */
    @Bean
    public Filter limitFilter(LimiterRegistry registry) {
        return this.properties.getServer().getLimit().isEnabled() ? new LimitFilter(registry) : null;
    }

    /**
     * 配置 Vortex 请求处理核心组件
     *
     * @return Vortex 核心组件实例，包含 HTTP 服务器
     */
    @Bean(initMethod = "init", destroyMethod = "destroy")
    public Vortex athlete() {
        // 使用注入的所有 Handler 实例创建 VortexHandler
        VortexHandler vortexHandler = new VortexHandler(handlers);

        // 配置路由，处理指定路径的请求
        RouterFunction<ServerResponse> routerFunction = RouterFunctions.route(
                RequestPredicates.path(properties.getServer().getPath()).and(
                        RequestPredicates.accept(MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON)),
                vortexHandler::handle);

        // 配置编解码器，设置最大内存大小
        ServerCodecConfigurer configurer = ServerCodecConfigurer.create();
        configurer.defaultCodecs().maxInMemorySize(Config.MAX_INMEMORY_SIZE);

        // 构建 WebHandler，集成过滤器和异常处理器
        WebHandler webHandler = RouterFunctions.toWebHandler(routerFunction);
        HttpHandler handler = WebHttpHandlerBuilder.webHandler(webHandler).filters(list -> list.addAll(filters))
                .exceptionHandlers(list -> list.add(new ErrorsHandler())).codecConfigurer(configurer).build();

        // 创建 HTTP 服务器适配器
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
        HttpServer server = HttpServer.create().port(properties.getServer().getPort()).handle(adapter);

        return new Vortex(server);
    }

}