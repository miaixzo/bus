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
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.PORT;
import org.miaixz.bus.vortex.*;
import org.miaixz.bus.vortex.filter.PrimaryFilter;
import org.miaixz.bus.vortex.handler.ErrorsHandler;
import org.miaixz.bus.vortex.handler.VortexHandler;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;
import org.miaixz.bus.vortex.provider.ProcessProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;
import org.miaixz.bus.vortex.registry.LimiterRegistry;
import org.miaixz.bus.vortex.strategy.*;
import org.miaixz.bus.vortex.support.*;
import org.miaixz.bus.vortex.support.grpc.GrpcExecutor;
import org.miaixz.bus.vortex.support.mcp.McpExecutor;
import org.miaixz.bus.vortex.support.mcp.server.ManageProvider;
import org.miaixz.bus.vortex.support.mq.MqExecutor;
import org.miaixz.bus.vortex.support.rest.RestExecutor;
import org.miaixz.bus.vortex.support.ws.WsExecutor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import jakarta.annotation.Resource;
import reactor.netty.http.server.HttpServer;

/**
 * Auto-configuration for the Vortex gateway.
 * <p>
 * This configuration class sets up the complete request processing pipeline for the Vortex API gateway, including:
 * </p>
 * <ul>
 * <li><b>Router Beans</b>: Protocol-specific routers (HTTP/REST, gRPC, WebSocket, MQ, MCP) that delegate to
 * executors</li>
 * <li><b>Executor Beans</b>: Protocol executors that handle actual request execution and response formatting</li>
 * <li><b>Strategy Beans</b>: Request processing strategies (request parsing, vetting, qualification, rate limiting,
 * response formatting)</li>
 * <li><b>Core Component</b>: The main Vortex bean that integrates WebFlux, filters, and the HTTP server</li>
 * </ul>
 * <p>
 * <b>Architecture:</b> The gateway follows a clean separation of concerns where {@link org.miaixz.bus.vortex.Router}
 * implementations coordinate request handling, while {@link org.miaixz.bus.vortex.Executor} implementations perform
 * protocol-specific execution. This design enables easy extension with new protocols and consistent request processing
 * across all protocols.
 * </p>
 * <p>
 * <b>Supported Protocols:</b>
 * <ul>
 * <li>HTTP/REST (mode 1): Standard RESTful API proxying</li>
 * <li>gRPC (mode 2): gRPC-Web and gRPC-HTTP proxying</li>
 * <li>WebSocket (mode 3): WebSocket connection management</li>
 * <li>MCP (mode 4-6): Model Context Protocol (STDIO, SSE, HTTP)</li>
 * <li>MQ (mode 7): Message Queue integration</li>
 * </ul>
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 * @see org.miaixz.bus.vortex.Router
 * @see org.miaixz.bus.vortex.Executor
 * @see org.miaixz.bus.vortex.Strategy
 */
@EnableConfigurationProperties(value = { VortexProperties.class })
public class VortexConfiguration {

    @Resource
    private VortexProperties properties;

    /**
     * Configures the core Vortex request processing component. This method sets up the WebFlux handler, integrates
     * filters and exception handlers, and configures the Reactor Netty HTTP server.
     *
     * @param filters  A list of all available {@link Filter} beans, injected by Spring.
     * @param handlers A list of all available {@link Handler} beans, injected by Spring.
     * @param routers  A map of all available {@link Router} beans, injected by Spring.
     * @return A {@link Vortex} core component instance, including the HTTP server.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Vortex vortex(List<Filter> filters, List<Handler> handlers, Map<String, Router<ServerRequest, ?>> routers) {
        // Create the main Vortex handler with all injected Handler instances.
        VortexHandler vortexHandler = new VortexHandler(handlers, routers);

        // Configure the router to handle requests at the specified path.
        RouterFunction<ServerResponse> routerFunction = RouterFunctions.route(
                RequestPredicates.path(this.properties.getPath() + Symbol.SLASH + Symbol.STAR + Symbol.STAR),
                vortexHandler::handle);

        // Configure codecs, setting the maximum in-memory size.
        ServerCodecConfigurer configurer = ServerCodecConfigurer.create();
        configurer.defaultCodecs().maxInMemorySize(Math.toIntExact(Normal.MEBI_128));

        // Build the WebHandler, integrating filters and exception handlers.
        WebHandler webHandler = RouterFunctions.toWebHandler(routerFunction);
        HttpHandler httpHandler = WebHttpHandlerBuilder.webHandler(webHandler).filters(list -> list.addAll(filters))
                .exceptionHandlers(list -> list.add(new ErrorsHandler())).codecConfigurer(configurer).build();

        // Create the Reactor Netty HTTP server adapter.
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
        HttpServer server = HttpServer.create()
                .port(this.properties.getPort() != 0 ? this.properties.getPort() : PORT._8765.getPort())
                .handle(adapter);

        return new Vortex(server);
    }

    /**
     * Configures and provides the gRPC router bean. This router is responsible for handling gRPC-specific routing
     * logic.
     *
     * @param executor The GrpcExecutor instance to be used by the router.
     * @return A new instance of GrpcRouter.
     */
    @Bean(name = "grpc")
    public Router<ServerRequest, ServerResponse> grpc(GrpcExecutor executor) {
        return new GrpcRouter(executor);
    }

    /**
     * Configures and provides the MCP router bean. This router is responsible for handling MCP (Model Context
     * Protocol)-specific routing logic.
     *
     * @param executor The McpExecutor instance to be used by the router.
     * @return A new instance of McpRouter.
     */
    @Bean(name = "mcp")
    public Router<ServerRequest, ServerResponse> mcp(McpExecutor executor) {
        return new McpRouter(executor);
    }

    /**
     * Configures and provides the MQ router bean. This router is responsible for handling Message Queue-specific
     * routing logic.
     *
     * @param executor The MqExecutor instance to be used by the router.
     * @return A new instance of MqRouter.
     */
    @Bean(name = "mq")
    public Router<ServerRequest, ServerResponse> mq(MqExecutor executor) {
        return new MqRouter(executor);
    }

    /**
     * Configures and provides the HTTP/REST router bean. This router is responsible for handling HTTP-specific routing
     * logic.
     * <p>
     * Note: The method name is {@code rest} to align with the new architecture naming convention (RestExecutor), but
     * the bean name remains {@code "http"} for backward compatibility with existing configurations.
     *
     * @param executor The RestExecutor instance to be used by the router.
     * @return A new instance of RestRouter.
     */
    @Bean(name = "http")
    public Router<ServerRequest, ServerResponse> rest(RestExecutor executor) {
        return new RestRouter(executor);
    }

    /**
     * Configures and provides the WebSocket router bean. This router is responsible for handling WebSocket-specific
     * routing logic.
     *
     * @param executor The WsExecutor instance to be used by the router.
     * @return A new instance of WsRouter.
     */
    @Bean(name = "ws")
    public Router<ServerRequest, ServerResponse> ws(WsExecutor executor) {
        return new WsRouter(executor);
    }

    /**
     * Provides the GrpcExecutor bean. This executor is responsible for executing gRPC requests to downstream services.
     *
     * @return A new instance of GrpcExecutor.
     */
    @Bean
    public GrpcExecutor grpcExecutor() {
        return new GrpcExecutor();
    }

    /**
     * Provides the McpExecutor bean. This executor manages the lifecycle and operations of MCP clients.
     *
     * @param assetsRegistry The AssetsRegistry instance used to access asset configurations.
     * @return A new instance of McpExecutor.
     */
    @Bean
    public McpExecutor mcpExecutor(AssetsRegistry assetsRegistry, ProcessProvider processProvider) {
        return new McpExecutor(assetsRegistry, processProvider);
    }

    /**
     * Provides the MqExecutor bean. This executor handles sending messages to a message queue.
     *
     * @return A new instance of MqExecutor.
     */
    @Bean
    public MqExecutor mqExecutor() {
        return new MqExecutor();
    }

    /**
     * Provides the RestExecutor bean. This executor is responsible for executing HTTP requests to downstream services.
     *
     * @return A new instance of RestExecutor.
     */
    @Bean
    public RestExecutor restExecutor() {
        return new RestExecutor();
    }

    /**
     * Provides the WsExecutor bean. This executor is responsible for managing WebSocket connections and sessions.
     *
     * @return A new instance of WsExecutor.
     */
    @Bean
    public WsExecutor wsExecutor() {
        return new WsExecutor();
    }

    /**
     * Provides the ProcessProvider bean. This provider is responsible for starting and managing external processes,
     * particularly for MCP (Model Context Protocol) clients.
     *
     * @return A new instance of ManageProvider.
     */
    @Bean
    public ProcessProvider processProvider() {
        return new ManageProvider();
    }

    /**
     * Provides the RequestStrategy bean. This strategy is responsible for initial request parsing and context
     * initialization.
     *
     * @return A new instance of RequestStrategy.
     */
    @Bean
    public RequestStrategy requestStrategy() {
        return new RequestStrategy();
    }

    /**
     * Provides the VettingStrategy bean. This strategy performs access authorization based on tokens, API keys, and
     * asset configurations.
     *
     * 
     * @return A new instance of VettingStrategy.
     */
    @Bean
    public VettingStrategy vettingStrategy() {
        return new VettingStrategy();
    }

    /**
     * Provides the QualifierStrategy bean. This strategy performs request qualification and routing based on asset
     * configurations.
     *
     * @param authorizeProvider The AuthorizeProvider for handling authorization logic.
     * @param assetsRegistry    The AssetsRegistry for accessing API asset information.
     * @return A new instance of QualifierStrategy.
     */
    @Bean
    public QualifierStrategy qualiferStrategy(AuthorizeProvider authorizeProvider, AssetsRegistry assetsRegistry) {
        return new QualifierStrategy(authorizeProvider, assetsRegistry);
    }

    /**
     * Provides the LimitStrategy bean. This strategy applies rate limiting to requests.
     *
     * @param limiterRegistry The LimiterRegistry for managing rate limiter configurations.
     * @return A new instance of LimitStrategy.
     */
    @Bean
    public LimiterStrategy limitStrategy(LimiterRegistry limiterRegistry) {
        return new LimiterStrategy(limiterRegistry);
    }

    /**
     * Provides the ResponseStrategy bean. This strategy handles response formatting, e.g., converting JSON to XML
     * responses when requested.
     *
     * @return A new instance of ResponseStrategy.
     */
    @Bean
    public ResponseStrategy responseStrategy() {
        return new ResponseStrategy();
    }

    /**
     * Defines the StrategyFactory bean. This factory is responsible for providing the correct chain of {@link Strategy}
     * instances based on the incoming request. Spring will inject all available {@link Strategy} beans into the
     * constructor.
     *
     * @param strategies A list of all available {@link Strategy} beans, injected by Spring.
     * @return A new instance of StrategyFactory.
     */
    @Bean
    public StrategyFactory strategyFactory(List<Strategy> strategies) {
        return new StrategyFactory(strategies);
    }

    /**
     * Defines the PrimaryFilter bean. This filter acts as the main entry point and dispatcher for a dynamic chain of
     * strategies. Spring will inject the {@link StrategyFactory} bean into the constructor. Since PrimaryFilter
     * implements {@link org.springframework.web.server.WebFilter} and is annotated with {@code @Order}, Spring Boot
     * will automatically register it in the WebFlux filter chain.
     *
     * @param strategyFactory The StrategyFactory instance.
     * @return A new instance of PrimaryFilter.
     */
    @Bean
    public PrimaryFilter primaryFilter(StrategyFactory strategyFactory) {
        return new PrimaryFilter(strategyFactory);
    }

}
