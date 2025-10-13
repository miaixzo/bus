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
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.net.PORT;
import org.miaixz.bus.vortex.Filter;
import org.miaixz.bus.vortex.Handler;
import org.miaixz.bus.vortex.Router;
import org.miaixz.bus.vortex.Vortex;
import org.miaixz.bus.vortex.handler.ErrorsHandler;
import org.miaixz.bus.vortex.handler.VortexHandler;
import org.miaixz.bus.vortex.support.HttpRouter;
import org.miaixz.bus.vortex.support.McpRouter;
import org.miaixz.bus.vortex.support.MqRouter;
import org.miaixz.bus.vortex.support.http.HttpService;
import org.miaixz.bus.vortex.support.mcp.McpService;
import org.miaixz.bus.vortex.support.mq.MqService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
 * Auto-configuration for the Vortex gateway, based on a strategy pattern for filters.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { VortexProperties.class })
public class VortexConfiguration {

    @Resource
    private VortexProperties properties;

    /**
     * Automatically injects all beans that implement the {@link Filter} interface.
     */
    @Resource
    private List<Filter> filters;

    /**
     * Automatically injects all beans that implement the {@link Handler} interface.
     */
    @Resource
    private List<Handler> handlers;

    /**
     * A thread-safe map of strategies, storing strategy implementations indexed by protocol name.
     * <p>
     * This map allows dynamic selection of routing strategies based on the protocol, supporting HTTP, MQ, and MCP
     * protocols. {@code ConcurrentHashMap} is used to ensure thread safety.
     * </p>
     */
    @Resource
    private Map<String, Router> routers;

    @Bean(name = "http")
    public Router http(HttpService service) {
        return new HttpRouter(service);
    }

    @Bean(name = "mq")
    public Router mq(MqService service) {
        return new MqRouter(service);
    }

    @Bean(name = "mcp")
    public Router mcp(McpService service) {
        return new McpRouter(service);
    }

    /**
     * Configures the core Vortex request processing component.
     *
     * @return A {@link Vortex} core component instance, including the HTTP server.
     */
    @Bean(initMethod = "init", destroyMethod = "destroy")
    public Vortex vortex() {
        // Create the main Vortex handler with all injected Handler instances.
        VortexHandler vortexHandler = new VortexHandler(handlers, routers);

        // Configure the router to handle requests at the specified path.
        RouterFunction<ServerResponse> routerFunction = RouterFunctions
                .route(RequestPredicates.path(this.properties.getPath() + "/**"), vortexHandler::handle);

        // Configure codecs, setting the maximum in-memory size.
        ServerCodecConfigurer configurer = ServerCodecConfigurer.create();
        configurer.defaultCodecs().maxInMemorySize(Math.toIntExact(Normal.MEBI_128));

        // Build the WebHandler, integrating filters and exception handlers.
        WebHandler webHandler = RouterFunctions.toWebHandler(routerFunction);
        HttpHandler httpHandler = WebHttpHandlerBuilder.webHandler(webHandler)
                .filters(list -> list.addAll(this.filters)).exceptionHandlers(list -> list.add(new ErrorsHandler()))
                .codecConfigurer(configurer).build();

        // Create the Reactor Netty HTTP server adapter.
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
        HttpServer server = HttpServer.create()
                .port(this.properties.getPort() != 0 ? this.properties.getPort() : PORT._8765).handle(adapter);

        return new Vortex(server);
    }

}
