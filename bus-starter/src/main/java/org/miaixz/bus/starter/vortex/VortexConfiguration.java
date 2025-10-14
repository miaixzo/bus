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
import org.miaixz.bus.vortex.filter.PrimaryFilter;
import org.miaixz.bus.vortex.handler.ErrorsHandler;
import org.miaixz.bus.vortex.handler.VortexHandler;
import org.miaixz.bus.vortex.magic.Delegate;
import org.miaixz.bus.vortex.magic.Principal;
import org.miaixz.bus.vortex.provider.AuthorizeProvider;
import org.miaixz.bus.vortex.provider.LicenseProvider;
import org.miaixz.bus.vortex.registry.AssetsRegistry;
import org.miaixz.bus.vortex.registry.LimiterRegistry;
import org.miaixz.bus.vortex.strategy.AuthorizeStrategy;
import org.miaixz.bus.vortex.strategy.CipherStrategy;
import org.miaixz.bus.vortex.strategy.FormatStrategy;
import org.miaixz.bus.vortex.strategy.LicenseStrategy;
import org.miaixz.bus.vortex.strategy.LimitStrategy;
import org.miaixz.bus.vortex.strategy.RequestStrategy;
import org.miaixz.bus.vortex.Strategy;
import org.miaixz.bus.vortex.strategy.StrategyFactory;
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
     * Configures and provides the HTTP router bean. This router is responsible for handling HTTP-specific routing
     * logic.
     *
     * @param service The HttpService instance to be used by the router.
     * @return A new instance of HttpRouter.
     */
    @Bean(name = "http")
    public Router http(HttpService service) {
        return new HttpRouter(service);
    }

    /**
     * Configures and provides the MCP router bean. This router is responsible for handling MCP (Model Context
     * Protocol)-specific routing logic.
     *
     * @param service The McpService instance to be used by the router.
     * @return A new instance of McpRouter.
     */
    @Bean(name = "mcp")
    public Router mcp(McpService service) {
        return new McpRouter(service);
    }

    /**
     * Configures and provides the MQ router bean. This router is responsible for handling Message Queue-specific
     * routing logic.
     *
     * @param service The MqService instance to be used by the router.
     * @return A new instance of MqRouter.
     */
    @Bean(name = "mq")
    public Router mq(MqService service) {
        return new MqRouter(service);
    }

    /**
     * Provides the HttpService bean. This service is responsible for executing HTTP requests to downstream services.
     *
     * @return A new instance of HttpService.
     */
    @Bean
    public HttpService httpService() {
        return new HttpService();
    }

    /**
     * Provides the McpService bean. This service manages the lifecycle and operations of MCP clients.
     *
     * @param assetsRegistry The AssetsRegistry instance used to access asset configurations.
     * @return A new instance of McpService.
     */
    @Bean
    public McpService mcpService(AssetsRegistry assetsRegistry) {
        return new McpService(assetsRegistry);
    }

    /**
     * Provides the MqService bean. This service handles sending messages to a message queue.
     *
     * @return A new instance of MqService.
     */
    @Bean
    public MqService mqService() {
        return new MqService();
    }

    // --- Strategy Dependencies ---

    /**
     * Default LicenseProvider implementation. This bean provides a default license validation mechanism that always
     * returns true. Users can provide their own {@code @Bean} of type {@link LicenseProvider} to override this default
     * behavior.
     *
     * @return A default implementation of LicenseProvider.
     */
    @Bean
    public LicenseProvider licenseProvider() {
        return new LicenseProvider() {

            @Override
            public boolean validate(String principal) {
                // Default implementation: always valid
                return true;
            }
        };
    }

    /**
     * Default AuthorizeProvider implementation. This bean provides a default authorization mechanism that always
     * returns a successful delegate. Users can provide their own {@code @Bean} of type {@link AuthorizeProvider} to
     * override this default behavior.
     *
     * @return A default implementation of AuthorizeProvider.
     */
    @Bean
    public AuthorizeProvider authorizeProvider() {
        return new AuthorizeProvider() {

            @Override
            public Delegate authorize(Principal principal) {
                // Default implementation: always authorized
                return new Delegate();
            }
        };
    }

    // --- Strategy Implementations ---

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
     * Provides the CipherStrategy bean. This strategy handles request decryption and response encryption based on
     * configured properties.
     *
     * @param vortexProperties The Vortex configuration properties, containing decryption and encryption settings.
     * @return A new instance of CipherStrategy.
     */
    @Bean
    public CipherStrategy cipherStrategy(VortexProperties vortexProperties) {
        return new CipherStrategy(vortexProperties.getDecrypt(), vortexProperties.getEncrypt());
    }

    /**
     * Provides the AuthorizeStrategy bean. This strategy performs access authorization based on tokens, API keys, and
     * asset configurations.
     *
     * @param authorizeProvider The AuthorizeProvider for handling authorization logic.
     * @param assetsRegistry    The AssetsRegistry for accessing API asset information.
     * @return A new instance of AuthorizeStrategy.
     */
    @Bean
    public AuthorizeStrategy authorizeStrategy(AuthorizeProvider authorizeProvider, AssetsRegistry assetsRegistry) {
        return new AuthorizeStrategy(authorizeProvider, assetsRegistry);
    }

    /**
     * Provides the LicenseStrategy bean. This strategy enforces license validity checks.
     *
     * @param licenseProvider The LicenseProvider for handling license validation logic.
     * @return A new instance of LicenseStrategy.
     */
    @Bean
    public LicenseStrategy licenseStrategy(LicenseProvider licenseProvider) {
        return new LicenseStrategy(licenseProvider);
    }

    /**
     * Provides the LimitStrategy bean. This strategy applies rate limiting to requests.
     *
     * @param limiterRegistry The LimiterRegistry for managing rate limiter configurations.
     * @return A new instance of LimitStrategy.
     */
    @Bean
    public LimitStrategy limitStrategy(LimiterRegistry limiterRegistry) {
        return new LimitStrategy(limiterRegistry);
    }

    /**
     * Provides the FormatStrategy bean. This strategy handles response formatting, e.g., converting XML requests to
     * JSON responses.
     *
     * @return A new instance of FormatStrategy.
     */
    @Bean
    public FormatStrategy formatStrategy() {
        return new FormatStrategy();
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

    /**
     * Configures the core Vortex request processing component. This method sets up the WebFlux handler, integrates
     * filters and exception handlers, and configures the Reactor Netty HTTP server.
     *
     * @param filters  A list of all available {@link Filter} beans, injected by Spring.
     * @param handlers A list of all available {@link Handler} beans, injected by Spring.
     * @param routers  A map of all available {@link Router} beans, injected by Spring.
     * @return A {@link Vortex} core component instance, including the HTTP server.
     */
    @Bean(initMethod = "init", destroyMethod = "destroy")
    public Vortex vortex(List<Filter> filters, List<Handler> handlers, Map<String, Router> routers) {
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
        HttpHandler httpHandler = WebHttpHandlerBuilder.webHandler(webHandler).filters(list -> list.addAll(filters))
                .exceptionHandlers(list -> list.add(new ErrorsHandler())).codecConfigurer(configurer).build();

        // Create the Reactor Netty HTTP server adapter.
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
        HttpServer server = HttpServer.create()
                .port(this.properties.getPort() != 0 ? this.properties.getPort() : PORT._8765).handle(adapter);

        return new Vortex(server);
    }

}
