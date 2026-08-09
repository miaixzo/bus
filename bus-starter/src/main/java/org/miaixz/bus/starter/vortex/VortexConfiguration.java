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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.multipart.DefaultPartHttpMessageReader;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.ForwardedHeaderTransformer;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Port;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Keying;
import org.miaixz.bus.cortex.builtin.RegistryGenerator;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableVortex;
import org.miaixz.bus.vortex.*;
import org.miaixz.bus.vortex.filter.AdmissionFilter;
import org.miaixz.bus.vortex.filter.PrimaryFilter;
import org.miaixz.bus.vortex.handler.ErrorsHandler;
import org.miaixz.bus.vortex.handler.VortexHandler;
import org.miaixz.bus.vortex.magic.Delegate;
import org.miaixz.bus.vortex.magic.ErrorCode;
import org.miaixz.bus.vortex.magic.Principal;
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
import org.miaixz.bus.vortex.routing.slug.SlugExecutor;
import org.miaixz.bus.vortex.routing.slug.SlugRouteMatcher;
import org.miaixz.bus.vortex.routing.ws.WsExecutor;
import org.miaixz.bus.vortex.strategy.*;
import org.miaixz.bus.vortex.strategy.qualifier.CstQualifierStrategy;
import org.miaixz.bus.vortex.strategy.qualifier.McpQualifierStrategy;
import org.miaixz.bus.vortex.strategy.qualifier.RestQualifierStrategy;
import org.miaixz.bus.vortex.strategy.qualifier.SlugQualifierStrategy;
import org.miaixz.bus.vortex.strategy.request.CstRequestStrategy;
import org.miaixz.bus.vortex.strategy.request.McpRequestStrategy;
import org.miaixz.bus.vortex.strategy.request.RestRequestStrategy;
import org.miaixz.bus.vortex.strategy.request.SlugRequestStrategy;
import org.miaixz.bus.vortex.strategy.vetting.CstVettingStrategy;
import org.miaixz.bus.vortex.strategy.vetting.McpVettingStrategy;
import org.miaixz.bus.vortex.strategy.vetting.RestVettingStrategy;
import org.miaixz.bus.vortex.strategy.vetting.SlugVettingStrategy;

import io.netty.channel.ChannelOption;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;

/**
 * Configures the Vortex routing gateway and its asset registry lifecycle.
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
 */
@EnableConfigurationProperties(value = { VortexProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = { "org.miaixz.bus.vortex.Context", "reactor.netty.http.server.HttpServer" })
@ConditionalOnEnabled(annotation = EnableVortex.class, prefix = GeniusBuilder.VORTEX)
public class VortexConfiguration {

    /**
     * Bean name of the HTTP router.
     */
    private static final String HTTP_ROUTER = "http";

    /**
     * Bean name of the WebSocket router.
     */
    private static final String WS_ROUTER = "ws";

    /**
     * Stores and validates the Vortex gateway properties and installs performance settings before any runtime Bean is
     * created.
     *
     * @param properties bound configuration properties
     */
    public VortexConfiguration(VortexProperties properties) {
        this.properties = properties;
        this.properties.validate();
        Holder.of(this.properties.getPerformance());
    }

    /**
     * Bound Vortex configuration properties.
     */
    private final VortexProperties properties;

    /**
     * Configures the complete Vortex reactive server.
     * <p>
     * The codec limit follows the bounded-request limit, multipart parts spill to disk after their configured memory
     * threshold, and {@link AdmissionFilter} is installed before protocol filters. Inbound channels use the unified
     * allocator and configured write-buffer watermarks so downloads remain backpressure-aware.
     *
     * @param filters    A list of all available {@link Filter} beans, injected by Spring.
     * @param handlers   A list of all available {@link Handler} beans, injected by Spring.
     * @param httpRouter The HTTP router bean.
     * @param mqRouter   The MQ router bean.
     * @param mcpRouter  The MCP router bean.
     * @param grpcRouter The gRPC router bean.
     * @param wsRouter   The WebSocket router bean.
     * @param llmRouter  The LLM router bean.
     * @param slugRouter The public slug forwarding router bean.
     * @return A {@link Vortex} core component instance, including the HTTP server.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean(Vortex.class)
    public Vortex vortex(
            List<Filter> filters,
            List<Handler> handlers,
            @Qualifier(HTTP_ROUTER) Router<ServerRequest, ?> httpRouter,
            @Qualifier("mq") Router<ServerRequest, ?> mqRouter,
            @Qualifier("mcp") Router<ServerRequest, ?> mcpRouter,
            @Qualifier("grpc") Router<ServerRequest, ?> grpcRouter,
            @Qualifier(WS_ROUTER) Router<ServerRequest, ?> wsRouter,
            @Qualifier("llm") Router<ServerRequest, ?> llmRouter,
            @Qualifier("slug") Router<ServerRequest, ?> slugRouter) {
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
                llmRouter,
                Args.PROTOCOL_SLUG,
                slugRouter);
        VortexHandler vortexHandler = new VortexHandler(handlers, routers);

        String routePath = this.properties.getPath();
        routePath = StringKit.isBlank(routePath) ? Symbol.SLASH + Symbol.STAR + Symbol.STAR : routePath.trim();
        if (Symbol.SLASH.equals(routePath)) {
            routePath = Symbol.SLASH + Symbol.STAR + Symbol.STAR;
        } else {
            if (!routePath.startsWith(Symbol.SLASH)) {
                routePath = Symbol.SLASH + routePath;
            }
            while (routePath.length() > 1 && routePath.endsWith(Symbol.SLASH)) {
                routePath = routePath.substring(0, routePath.length() - 1);
            }
            if (!routePath.endsWith(Symbol.SLASH + Symbol.STAR + Symbol.STAR)) {
                routePath = routePath + Symbol.SLASH + Symbol.STAR + Symbol.STAR;
            }
        }

        RouterFunction<ServerResponse> routerFunction = RouterFunctions
                .route(RequestPredicates.path(routePath), vortexHandler::handle);

        ServerCodecConfigurer configurer = ServerCodecConfigurer.create();
        configurer.defaultCodecs()
                .maxInMemorySize(Math.toIntExact(this.properties.getPerformance().getMaxBufferedRequestSize()));
        DefaultPartHttpMessageReader partReader = new DefaultPartHttpMessageReader();
        partReader.setMaxInMemorySize(this.properties.getPerformance().getMultipartMemoryThresholdBytes());
        partReader.setMaxDiskUsagePerPart(this.properties.getPerformance().getMaxMultipartRequestSize());
        configurer.defaultCodecs().multipartReader(new MultipartHttpMessageReader(partReader));

        WebHandler webHandler = RouterFunctions.toWebHandler(routerFunction);
        AdmissionFilter admissionFilter = new AdmissionFilter();
        HttpHandler httpHandler = WebHttpHandlerBuilder.webHandler(webHandler).filters(list -> {
            list.add(admissionFilter);
            list.addAll(filters);
        }).exceptionHandlers(list -> list.add(new ErrorsHandler())).codecConfigurer(configurer).build();

        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
        io.netty.channel.WriteBufferWaterMark writeWaterMark = new io.netty.channel.WriteBufferWaterMark(
                this.properties.getPerformance().getWriteBufferLowWatermarkBytes(),
                this.properties.getPerformance().getWriteBufferHighWatermarkBytes());
        HttpServer server = HttpServer.create().option(ChannelOption.ALLOCATOR, Holder.allocator())
                .childOption(ChannelOption.ALLOCATOR, Holder.allocator())
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, writeWaterMark)
                .port(this.properties.getPort() != 0 ? this.properties.getPort() : Port._8765.getPort())
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
    @ConditionalOnMissingBean(ForwardedHeaderTransformer.class)
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
    @ConditionalOnMissingBean(name = "grpc")
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
    @ConditionalOnMissingBean(name = "mcp")
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
    @ConditionalOnMissingBean(name = "mq")
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
    @Bean(name = HTTP_ROUTER)
    @ConditionalOnMissingBean(name = HTTP_ROUTER)
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
    @Bean(name = WS_ROUTER)
    @ConditionalOnMissingBean(name = WS_ROUTER)
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
    @ConditionalOnMissingBean(name = "llm")
    public Router<ServerRequest, ServerResponse> llm(LlmExecutor executor) {
        return new LlmRouter(executor);
    }

    /**
     * Configures and provides the public slug forwarding router bean.
     *
     * @param executor The SlugExecutor instance to be used by the router.
     * @return A new instance of SlugRouter.
     */
    @Bean(name = "slug")
    @ConditionalOnMissingBean(name = "slug")
    public Router<ServerRequest, ServerResponse> slug(SlugExecutor executor) {
        return new SlugRouter(executor);
    }

    /**
     * Provides the GrpcExecutor bean. This executor is responsible for executing gRPC requests to downstream services.
     *
     * @return A new instance of GrpcExecutor.
     */
    @Bean
    @ConditionalOnMissingBean(GrpcExecutor.class)
    public GrpcExecutor grpcExecutor() {
        return new GrpcExecutor();
    }

    /**
     * Provides the McpExecutor bean. This executor proxies standard MCP Streamable HTTP requests.
     *
     * @return A new instance of McpExecutor.
     */
    @Bean
    @ConditionalOnMissingBean(McpExecutor.class)
    public McpExecutor mcpExecutor() {
        return new McpExecutor();
    }

    /**
     * Provides the MqExecutor bean. This executor handles sending messages to a message queue.
     * <p>
     * Uses {@link Holder#get()} to obtain the global Performance configuration installed by this configuration's
     * constructor before executor Beans are created.
     *
     * @return A new instance of MqExecutor with globally configured performance settings
     */
    @Bean
    @ConditionalOnMissingBean(MqExecutor.class)
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
    @ConditionalOnMissingBean(RestExecutor.class)
    public RestExecutor restExecutor() {
        return new RestExecutor();
    }

    /**
     * Provides the WsExecutor bean. This executor is responsible for managing WebSocket connections and sessions.
     *
     * @return A new instance of WsExecutor.
     */
    @Bean
    @ConditionalOnMissingBean(WsExecutor.class)
    public WsExecutor wsExecutor() {
        return new WsExecutor();
    }

    /**
     * Provides the LlmFactory bean. This factory creates and caches LLM provider instances.
     *
     * @return A new instance of LlmFactory.
     */
    @Bean
    @ConditionalOnMissingBean(LlmFactory.class)
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
    @ConditionalOnMissingBean(LlmExecutor.class)
    public LlmExecutor llmExecutor(LlmFactory providerFactory) {
        return new LlmExecutor(providerFactory);
    }

    /**
     * Provides the public slug forwarding executor bean.
     *
     * @param matcher The slug route matcher.
     * @return A new instance of SlugExecutor.
     */
    @Bean
    @ConditionalOnMissingBean(SlugExecutor.class)
    public SlugExecutor slugExecutor(SlugRouteMatcher matcher) {
        return new SlugExecutor(matcher);
    }

    /**
     * Provides the public slug route matcher bean.
     *
     * @param registry The assets registry.
     * @return A new instance of SlugRouteMatcher.
     */
    @Bean
    @ConditionalOnMissingBean(SlugRouteMatcher.class)
    public SlugRouteMatcher slugRouteMatcher(AssetsRegistry registry) {
        return new SlugRouteMatcher(registry, properties.getAssets().getSlugMethod());
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
     * Provides a fail-closed authorization provider when applications do not supply one.
     * <p>
     * Anonymous routes with {@code policy=0} do not invoke this provider. Protected routes require applications to
     * provide a real {@link AuthorizeProvider}; otherwise authorization fails instead of silently allowing access.
     *
     * @return A conservative default authorization provider.
     */
    @Bean
    @ConditionalOnMissingBean(AuthorizeProvider.class)
    public AuthorizeProvider authorizeProvider() {
        return new AuthorizeProvider() {

            /**
             * Rejects protected routes when no application-specific authorization provider is configured.
             *
             * @param principal The principal to authorize.
             * @return A failed authorization signal.
             */
            @Override
            public Mono<Delegate> authorize(Principal principal) {
                return Mono.error(new ValidateException(ErrorCode._100160));
            }

        };
    }

    /**
     * Provides the basic request strategy bean. This strategy initializes request metadata without protocol-specific
     * body parsing.
     *
     * @return A new instance of RequestStrategy.
     */
    @Bean
    @ConditionalOnMissingBean(RequestStrategy.class)
    public RequestStrategy requestStrategy() {
        return new RequestStrategy();
    }

    /**
     * Provides the REST request strategy bean. This strategy parses REST/API request parameters.
     *
     * @return A new instance of RestRequestStrategy.
     */
    @Bean
    @ConditionalOnMissingBean(RestRequestStrategy.class)
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
    @ConditionalOnMissingBean(CstRequestStrategy.class)
    public CstRequestStrategy cstRequestStrategy() {
        return new CstRequestStrategy();
    }

    /**
     * Provides the MCP request strategy bean. This strategy passes MCP requests through without body parsing.
     *
     * @return A new instance of McpRequestStrategy.
     */
    @Bean
    @ConditionalOnMissingBean(McpRequestStrategy.class)
    public McpRequestStrategy mcpRequestStrategy() {
        return new McpRequestStrategy();
    }

    /**
     * Provides the public slug request strategy bean.
     *
     * @param matcher The slug route matcher.
     * @return A new instance of SlugRequestStrategy.
     */
    @Bean
    @ConditionalOnMissingBean(SlugRequestStrategy.class)
    public SlugRequestStrategy slugRequestStrategy(SlugRouteMatcher matcher) {
        return new SlugRequestStrategy(matcher);
    }

    /**
     * Provides the basic vetting strategy bean. This strategy supplies common undefined-value validation,
     * authorization-attribute merge, and request metadata enrichment for routes that do not yet have protocol-specific
     * vetting.
     *
     * @return A new instance of VettingStrategy.
     */
    @Bean
    @ConditionalOnMissingBean(VettingStrategy.class)
    public VettingStrategy vettingStrategy() {
        return new VettingStrategy();
    }

    /**
     * Provides the REST vetting strategy bean. This strategy validates REST/API parameters and timestamps before route
     * assets are resolved.
     *
     * @return A new instance of RestVettingStrategy.
     */
    @Bean
    @ConditionalOnMissingBean(RestVettingStrategy.class)
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
    @ConditionalOnMissingBean(CstVettingStrategy.class)
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
    @ConditionalOnMissingBean(McpVettingStrategy.class)
    public McpVettingStrategy mcpVettingStrategy() {
        return new McpVettingStrategy();
    }

    /**
     * Provides the public slug vetting strategy bean.
     *
     * @return A new instance of SlugVettingStrategy.
     */
    @Bean
    @ConditionalOnMissingBean(SlugVettingStrategy.class)
    public SlugVettingStrategy slugVettingStrategy() {
        return new SlugVettingStrategy();
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
    @ConditionalOnMissingBean(QualifierStrategy.class)
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
    @ConditionalOnMissingBean(RestQualifierStrategy.class)
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
    @ConditionalOnMissingBean(CstQualifierStrategy.class)
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
    @ConditionalOnMissingBean(McpQualifierStrategy.class)
    public McpQualifierStrategy mcpQualifierStrategy(
            AuthorizeProvider authorizeProvider,
            AssetsRegistry assetsRegistry) {
        return new McpQualifierStrategy(authorizeProvider, assetsRegistry);
    }

    /**
     * Provides the public slug qualifier strategy bean.
     *
     * @param matcher           The slug route matcher.
     * @param authorizeProvider The AuthorizeProvider for handling authorization logic.
     * @return A new instance of SlugQualifierStrategy.
     */
    @Bean
    @ConditionalOnMissingBean(SlugQualifierStrategy.class)
    public SlugQualifierStrategy slugQualifierStrategy(SlugRouteMatcher matcher, AuthorizeProvider authorizeProvider) {
        return new SlugQualifierStrategy(matcher, authorizeProvider);
    }

    /**
     * Provides the LimitStrategy bean. This strategy applies rate limiting to requests.
     *
     * @param limiterRegistryProvider The LimiterRegistry provider for managing rate limiter configurations.
     * @return A new instance of LimitStrategy.
     */
    @Bean
    @ConditionalOnMissingBean(LimiterStrategy.class)
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
    @ConditionalOnMissingBean(ResponseStrategy.class)
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
    @ConditionalOnMissingBean(StrategyFactory.class)
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
    @ConditionalOnMissingBean(PrimaryFilter.class)
    public PrimaryFilter primaryFilter(StrategyFactory strategyFactory) {
        return new PrimaryFilter(strategyFactory);
    }

}
