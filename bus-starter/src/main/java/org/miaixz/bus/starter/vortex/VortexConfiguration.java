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

import org.miaixz.bus.vortex.Athlete;
import org.miaixz.bus.vortex.Config;
import org.miaixz.bus.vortex.filter.*;
import org.miaixz.bus.vortex.handler.ApiRouterHandler;
import org.miaixz.bus.vortex.handler.ApiWebMvcRegistrations;
import org.miaixz.bus.vortex.handler.GlobalExceptionHandler;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;
import org.miaixz.bus.vortex.registry.DefaultAssetsRegistry;
import org.miaixz.bus.vortex.registry.DefaultLimiterRegistry;
import org.miaixz.bus.vortex.registry.LimiterRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.autoconfigure.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import jakarta.annotation.Resource;
import reactor.netty.http.server.HttpServer;

/**
 * 路由自动配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ConditionalOnWebApplication
@EnableConfigurationProperties(value = { VortexProperties.class })
public class VortexConfiguration {

    @Resource
    VortexProperties vortexProperties;

    @Resource
    List<WebExceptionHandler> webExceptionHandlers;

    @Resource
    List<WebFilter> webFilters;

    @ConditionalOnMissingBean
    @Bean
    AssetsRegistry assetsRegistry() {
        return new DefaultAssetsRegistry();
    }

    @ConditionalOnMissingBean
    @Bean
    LimiterRegistry limiterRegistry() {
        return new DefaultLimiterRegistry();
    }

    @Bean
    WebFilter primaryFilter() {
        return new PrimaryFilter();
    }

    @Bean
    WebFilter decryptFilter() {
        return this.vortexProperties.getServer().getDecrypt().isEnabled()
                ? new DecryptFilter(this.vortexProperties.getServer().getDecrypt())
                : null;
    }

    @Bean
    WebFilter authorizeFilter(AuthorizeProvider authorizeProvider, AssetsRegistry registry) {
        return new AuthorizeFilter(authorizeProvider, registry);
    }

    @Bean
    WebFilter encryptFilter() {
        return this.vortexProperties.getServer().getEncrypt().isEnabled()
                ? new EncryptFilter(this.vortexProperties.getServer().getEncrypt())
                : null;
    }

    @Bean
    WebFilter limitFilter(LimiterRegistry registry) {
        return this.vortexProperties.getServer().getLimit().isEnabled() ? new LimitFilter(registry) : null;
    }

    @Bean
    WebFilter formatFilter() {
        return new FormatFilter();
    }

    @Bean
    WebExceptionHandler webExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    Athlete athlete() {
        ApiRouterHandler apiRouterHandler = new ApiRouterHandler();

        RouterFunction<ServerResponse> routerFunction = RouterFunctions
                .route(RequestPredicates.path(vortexProperties.getServer().getPath()).and(
                        RequestPredicates.accept(MediaType.APPLICATION_FORM_URLENCODED)), apiRouterHandler::handle);

        ServerCodecConfigurer configurer = ServerCodecConfigurer.create();
        configurer.defaultCodecs().maxInMemorySize(Config.MAX_INMEMORY_SIZE);

        WebHandler webHandler = RouterFunctions.toWebHandler(routerFunction);
        HttpHandler handler = WebHttpHandlerBuilder.webHandler(webHandler)
                .filters(filters -> filters.addAll(webFilters))
                .exceptionHandlers(handlers -> handlers.addAll(webExceptionHandlers)).codecConfigurer(configurer)
                .build();
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
        HttpServer server = HttpServer.create().port(vortexProperties.getServer().getPort()).handle(adapter);

        return new Athlete(server);
    }

    @Bean
    public WebMvcRegistrations customWebMvcRegistrations() {
        return this.vortexProperties.isCondition() ? null : new ApiWebMvcRegistrations();
    }

}
