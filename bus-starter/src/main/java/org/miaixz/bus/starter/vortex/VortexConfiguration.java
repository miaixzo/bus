/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.vortex;

import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.PORT;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.builtin.RegistryGenerator;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.vortex.*;
import org.miaixz.bus.vortex.filter.PrimaryFilter;
import org.miaixz.bus.vortex.handler.ErrorsHandler;
import org.miaixz.bus.vortex.handler.VortexHandler;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;
import org.miaixz.bus.vortex.registry.LimiterRegistry;
import org.miaixz.bus.vortex.routing.*;
import org.miaixz.bus.vortex.routing.grpc.GrpcExecutor;
import org.miaixz.bus.vortex.routing.llm.LlmExecutor;
import org.miaixz.bus.vortex.routing.llm.LlmFactory;
import org.miaixz.bus.vortex.routing.mcp.McpExecutor;
import org.miaixz.bus.vortex.routing.mq.MqExecutor;
import org.miaixz.bus.vortex.routing.rest.RestExecutor;
import org.miaixz.bus.vortex.routing.ws.WsExecutor;
import org.miaixz.bus.vortex.strategy.*;
import org.miaixz.bus.vortex.strategy.qualifier.CstQualifierStrategy;
import org.miaixz.bus.vortex.strategy.qualifier.McpQualifierStrategy;
import org.miaixz.bus.vortex.strategy.qualifier.RestQualifierStrategy;
import org.miaixz.bus.vortex.strategy.request.CstRequestStrategy;
import org.miaixz.bus.vortex.strategy.request.McpRequestStrategy;
import org.miaixz.bus.vortex.strategy.request.RestRequestStrategy;
import org.miaixz.bus.vortex.strategy.vetting.CstVettingStrategy;
import org.miaixz.bus.vortex.strategy.vetting.McpVettingStrategy;
import org.miaixz.bus.vortex.strategy.vetting.RestVettingStrategy;

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
 * <b>Architecture:</b> The gateway follows a clean separation of concerns where {@link Router} implementations
 * coordinate request handling, while {@link Executor} implementations perform protocol-specific execution. This design
 * enables easy extension with new protocols and consistent request processing across all protocols.
 * </p>
 * <p>
 * <b>Supported Protocols:</b>
 * <ul>
 * <li>HTTP/REST (protocol 1): Standard REST API proxying</li>
 * <li>MQ (protocol 2): Message Queue integration</li>
 * <li>MCP (protocol 3): Model Context Protocol Streamable HTTP proxying</li>
 * <li>gRPC (protocol 4): gRPC-Web and gRPC-HTTP proxying</li>
 * <li>WebSocket (protocol 5): WebSocket connection management</li>
 * <li>LLM (protocol 6): Large language model proxying</li>
 * </ul>
 *
 * @see Router
 * @see Executor
 * @see Strategy
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { VortexProperties.class })
@ConditionalOnProperty(prefix = GeniusBuilder.VORTEX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class VortexConfiguration {

    /**
     * Creates a Vortex auto-configuration.
     */
    public VortexConfiguration() {
        // No initialization required.
    }

    /**
     * Bound Vortex configuration properties.
     */
    @Resource
    private VortexProperties properties;

    /**
     * Configures the core Vortex request processing component. This method sets up the WebFlux handler, integrates
     * filters and exception handlers, and configures the Reactor Netty HTTP server.
     *
     * @param filters    A list of all available {@link Filter} beans, injected by Spring.
     * @param handlers   A list of all available {@link Handler} beans, injected by Spring.
     * @param httpRouter The HTTP router bean.
     * @param mqRouter   The MQ router bean.
     * @param mcpRouter  The MCP router bean.
     * @param grpcRouter The gRPC router bean.
     * @param wsRouter   The WebSocket router bean.
     * @param llmRouter  The LLM router bean.
     * @return A {@link Vortex} core component instance, including the HTTP server.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Vortex vortex(
            List<Filter> filters,
            List<Handler> handlers,
            @Qualifier("http") Router<ServerRequest, ?> httpRouter,
            @Qualifier("mq") Router<ServerRequest, ?> mqRouter,
            @Qualifier("mcp") Router<ServerRequest, ?> mcpRouter,
            @Qualifier("grpc") Router<ServerRequest, ?> grpcRouter,
            @Qualifier("ws") Router<ServerRequest, ?> wsRouter,
            @Qualifier("llm") Router<ServerRequest, ?> llmRouter) {
        Holder.of(properties.getPerformance());

        Map<Integer, Router<ServerRequest, ?>> routers = Map.of(
                Args.PROTOCOL_HTTP,
                httpRouter,
                Args.PROTOCOL_MQ,
                mqRouter,
                Args.PROTOCOL_MCP,
                mcpRouter,
                Args.PROTOCOL_GRPC,
                grpcRouter,
                Args.PROTOCOL_WS,
                wsRouter,
                Args.PROTOCOL_LLM,
                llmRouter);
        VortexHandler vortexHandler = new VortexHandler(handlers, routers);

        RouterFunction<ServerResponse> routerFunction = RouterFunctions.route(
                RequestPredicates.path(this.properties.getPath() + Symbol.SLASH + Symbol.STAR + Symbol.STAR),
                vortexHandler::handle);

        ServerCodecConfigurer configurer = ServerCodecConfigurer.create();
        configurer.defaultCodecs().maxInMemorySize(Math.toIntExact(Normal.MEBI_128));

        WebHandler webHandler = RouterFunctions.toWebHandler(routerFunction);
        HttpHandler httpHandler = WebHttpHandlerBuilder.webHandler(webHandler).filters(list -> list.addAll(filters))
                .exceptionHandlers(list -> list.add(new ErrorsHandler())).codecConfigurer(configurer).build();

        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
        HttpServer server = HttpServer.create()
                .port(this.properties.getPort() != 0 ? this.properties.getPort() : PORT._8765.getPort())
                .handle(adapter);

        return new Vortex(server);
    }

    /**
     * Configures the ForwardedHeaderTransformer bean.
     * <p>
     * This transformer processes proxy headers (X-Forwarded-For, X-Forwarded-Proto, Forwarded) to correctly identify
     * the original client IP address, protocol, and host when the application is behind a reverse proxy (e.g., Nginx,
     * HAProxy, AWS ELB).
     *
     * @return A new {@link ForwardedHeaderTransformer} instance.
     */
    @Bean
    public ForwardedHeaderTransformer forwardedHeaderTransformer() {
        return new ForwardedHeaderTransformer();
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
     * Configures and provides the LLM router bean. This router is responsible for handling Large Language Model proxy
     * routing logic.
     *
     * @param executor The LlmExecutor instance to be used by the router.
     * @return A new instance of LlmRouter.
     */
    @Bean(name = "llm")
    public Router<ServerRequest, ServerResponse> llm(LlmExecutor executor) {
        return new LlmRouter(executor);
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
     * Provides the McpExecutor bean. This executor proxies standard MCP Streamable HTTP requests.
     *
     * @return A new instance of McpExecutor.
     */
    @Bean
    public McpExecutor mcpExecutor() {
        return new McpExecutor();
    }

    /**
     * Provides the MqExecutor bean. This executor handles sending messages to a message queue.
     * <p>
     * Uses {@link Holder#get()} to obtain the global Performance configuration, which is initialized during
     * {@code Holder.of(properties.getPerformance())} call in the vortex bean.
     *
     * @return A new instance of MqExecutor with globally configured performance settings
     */
    @Bean
    public MqExecutor mqExecutor() {
        return new MqExecutor();
    }

    /**
     * Provides the RestExecutor bean. This executor is responsible for executing HTTP requests to downstream services.
     * <p>
     * The HTTP connection pool is obtained from {@link Holder#connectionProvider()} which is configured globally via
     * {@code vortex.performance.maxConnections} property.
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
     * Provides the LlmFactory bean. This factory creates and caches LLM provider instances.
     *
     * @return A new instance of LlmFactory.
     */
    @Bean
    public LlmFactory llmProviderFactory() {
        return new LlmFactory();
    }

    /**
     * Provides the LlmExecutor bean. This executor handles LLM proxy requests.
     *
     * @param providerFactory The LLM provider factory.
     * @return A new instance of LlmExecutor.
     */
    @Bean
    public LlmExecutor llmExecutor(LlmFactory providerFactory) {
        return new LlmExecutor(providerFactory);
    }

    /**
     * Provides the AssetsRegistry bean with the effective route-key strategy.
     *
     * @param keyingProvider optional route-key strategy bean
     * @return assets registry
     */
    @Bean
    @ConditionalOnMissingBean(AssetsRegistry.class)
    public AssetsRegistry assetsRegistry(
            @Qualifier("registryKeying") ObjectProvider<Keying<Keying.RegistrySpec>> keyingProvider) {
        return new AssetsRegistry(keyingProvider.getIfAvailable(() -> RegistryGenerator.INSTANCE));
    }

    /**
     * Provides the basic request strategy bean. This strategy initializes request metadata without protocol-specific
     * body parsing.
     *
     * @return A new instance of RequestStrategy.
     */
    @Bean
    public RequestStrategy requestStrategy() {
        return new RequestStrategy();
    }

    /**
     * Provides the REST request strategy bean. This strategy parses REST/API request parameters.
     *
     * @return A new instance of RestRequestStrategy.
     */
    @Bean
    public RestRequestStrategy restRequestStrategy() {
        return new RestRequestStrategy();
    }

    /**
     * Provides the CST request strategy bean. This strategy uses REST-like parameter parsing with CST-specific chain
     * typing.
     *
     * @return A new instance of CstRequestStrategy.
     */
    @Bean
    public CstRequestStrategy cstRequestStrategy() {
        return new CstRequestStrategy();
    }

    /**
     * Provides the MCP request strategy bean. This strategy passes MCP requests through without body parsing.
     *
     * @return A new instance of McpRequestStrategy.
     */
    @Bean
    public McpRequestStrategy mcpRequestStrategy() {
        return new McpRequestStrategy();
    }

    /**
     * Provides the basic vetting strategy bean. This strategy supplies common undefined-value validation,
     * authorization-attribute merge, and request metadata enrichment for routes that do not yet have protocol-specific
     * vetting.
     *
     * @return A new instance of VettingStrategy.
     */
    @Bean
    public VettingStrategy vettingStrategy() {
        return new VettingStrategy();
    }

    /**
     * Provides the REST vetting strategy bean. This strategy validates REST/API parameters, timestamps, and signatures
     * after route assets have been resolved.
     *
     * @return A new instance of RestVettingStrategy.
     */
    @Bean
    public RestVettingStrategy restVettingStrategy() {
        return new RestVettingStrategy();
    }

    /**
     * Provides the CST vetting strategy bean. This strategy supplies CST-specific strategy typing while using common
     * vetting behavior.
     *
     * @return A new instance of CstVettingStrategy.
     */
    @Bean
    public CstVettingStrategy cstVettingStrategy() {
        return new CstVettingStrategy();
    }

    /**
     * Provides the MCP vetting strategy bean. This strategy validates MCP Streamable HTTP rules and optional route
     * signatures using the asset already stored in the request context.
     *
     * @return A new instance of McpVettingStrategy.
     */
    @Bean
    public McpVettingStrategy mcpVettingStrategy() {
        return new McpVettingStrategy();
    }

    /**
     * Provides the basic qualifier strategy bean. This strategy resolves route assets and applies authorization for
     * routes that do not yet have protocol-specific qualification.
     *
     * @param authorizeProvider The AuthorizeProvider for handling authorization logic.
     * @param assetsRegistry    The AssetsRegistry for accessing API asset information.
     * @return A new instance of QualifierStrategy.
     */
    @Bean
    @ConditionalOnBean(AuthorizeProvider.class)
    public QualifierStrategy qualifierStrategy(AuthorizeProvider authorizeProvider, AssetsRegistry assetsRegistry) {
        return new QualifierStrategy(authorizeProvider, assetsRegistry);
    }

    /**
     * Provides the REST qualifier strategy bean. This strategy resolves REST/API route assets and applies authorization
     * before REST vetting.
     *
     * @param authorizeProvider The AuthorizeProvider for handling authorization logic.
     * @param assetsRegistry    The AssetsRegistry for accessing API asset information.
     * @return A new instance of RestQualifierStrategy.
     */
    @Bean
    @ConditionalOnBean(AuthorizeProvider.class)
    public RestQualifierStrategy restQualifierStrategy(
            AuthorizeProvider authorizeProvider,
            AssetsRegistry assetsRegistry) {
        return new RestQualifierStrategy(authorizeProvider, assetsRegistry);
    }

    /**
     * Provides the CST qualifier strategy bean. This strategy resolves URL-based CST route assets.
     *
     * @param authorizeProvider The AuthorizeProvider for handling authorization logic.
     * @param assetsRegistry    The AssetsRegistry for accessing API asset information.
     * @return A new instance of CstQualifierStrategy.
     */
    @Bean
    @ConditionalOnBean(AuthorizeProvider.class)
    public CstQualifierStrategy cstQualifierStrategy(
            AuthorizeProvider authorizeProvider,
            AssetsRegistry assetsRegistry) {
        return new CstQualifierStrategy(authorizeProvider, assetsRegistry);
    }

    /**
     * Provides the MCP qualifier strategy bean. This strategy resolves MCP ingress route assets and applies
     * authorization before MCP vetting.
     *
     * @param authorizeProvider The AuthorizeProvider for handling authorization logic.
     * @param assetsRegistry    The AssetsRegistry for accessing API asset information.
     * @return A new instance of McpQualifierStrategy.
     */
    @Bean
    @ConditionalOnBean(AuthorizeProvider.class)
    public McpQualifierStrategy mcpQualifierStrategy(
            AuthorizeProvider authorizeProvider,
            AssetsRegistry assetsRegistry) {
        return new McpQualifierStrategy(authorizeProvider, assetsRegistry);
    }

    /**
     * Provides the LimitStrategy bean. This strategy applies rate limiting to requests.
     *
     * @param limiterRegistryProvider The LimiterRegistry provider for managing rate limiter configurations.
     * @return A new instance of LimitStrategy.
     */
    @Bean
    public LimiterStrategy limitStrategy(ObjectProvider<LimiterRegistry> limiterRegistryProvider) {
        return new LimiterStrategy(limiterRegistryProvider.getIfAvailable(LimiterRegistry::new));
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
