/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.starter.goalie;

import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.goalie.reactor.AssetRegistry;
import org.aoju.bus.goalie.reactor.Athlete;
import org.aoju.bus.goalie.reactor.GlobalExceptionHandler;
import org.aoju.bus.goalie.reactor.RouterHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.WebFilter;
import reactor.netty.http.server.HttpServer;

import java.util.List;

/**
 * @author Justubborn
 * @since 2020/10/27
 */
@ConditionalOnWebApplication
@EnableConfigurationProperties(value = {GoalieProperties.class})
public class ReactorConfiguration {

    @Autowired
    GoalieProperties goalieProperties;

    @Autowired(required = false)
    List<AssetRegistry> assetRegistries;

    @Autowired(required = false)
    List<WebExceptionHandler> webExceptionHandlers;

    @Autowired(required = false)
    List<WebFilter> webFilters;

    @Bean
    WebExceptionHandler webExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean(initMethod = "init", destroyMethod = "destroy")
    Athlete athlete() {

        RouterHandler routerHandler = new RouterHandler();

        RouterFunction<ServerResponse> routerFunction = RouterFunctions
                .route(RequestPredicates.path(goalieProperties.getServer().getPath())
                        .and(RequestPredicates.accept(MediaType.APPLICATION_FORM_URLENCODED)), routerHandler::handle);

        HandlerStrategies.Builder builder = HandlerStrategies.builder();

        if (CollKit.isNotEmpty(webExceptionHandlers)) {
            AnnotationAwareOrderComparator.sort(webExceptionHandlers);
            webExceptionHandlers.forEach(builder::exceptionHandler);
        }

        if (CollKit.isNotEmpty(webFilters)) {
            AnnotationAwareOrderComparator.sort(webFilters);
            webFilters.forEach(builder::webFilter);
        }

        HandlerStrategies handlerStrategies = builder.build();


        HttpHandler handler = RouterFunctions.toHttpHandler(routerFunction, handlerStrategies);
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
        HttpServer server = HttpServer.create()
                .port(goalieProperties.getServer().getPort()).handle(adapter);

        return new Athlete(server, assetRegistries);
    }

}
