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

        RouterHandler routerHandler = new RouterHandler(assetRegistries);

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

        return new Athlete(server);
    }

}
