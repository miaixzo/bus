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
package org.miaixz.bus.vortex.support.mcp.client;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.support.mcp.Tool;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * An abstract base class for {@link McpClient} implementations that communicate with a remote MCP service over HTTP.
 * <p>
 * This class encapsulates the common logic for using Spring's {@link WebClient} to interact with an HTTP-based service.
 * It provides default implementations for initialization, health checks, and tool calls, which can be overridden by
 * subclasses if needed.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class HttpClient implements McpClient {

    /**
     * The configuration for the remote service.
     */
    protected final Assets assets;
    /**
     * The reactive web client used for all HTTP communication.
     */
    protected final WebClient webClient;
    /**
     * A cached list of tools provided by the remote service.
     */
    protected List<Tool> tools;

    /**
     * Constructs a new {@code HttpClient}.
     *
     * @param assets The {@link Assets} configuration, which must contain a non-empty {@code url} property pointing to
     *               the base URL of the remote service.
     * @throws IllegalArgumentException if the URL in the assets is missing.
     */
    public HttpClient(Assets assets) {
        this.assets = assets;
        String url = assets.getUrl();
        if (StringKit.isEmpty(url)) {
            throw new IllegalArgumentException("HTTP-based client assets must contain a 'url'");
        }
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    /**
     * Initializes the client by fetching the list of available tools from the remote service.
     *
     * @return A {@code Mono<Void>} that completes when the tool list has been successfully fetched and cached.
     */
    @Override
    public Mono<Void> initialize() {
        Logger.info("Initializing HTTP-based client for URL: {}", assets.getUrl());
        return listToolsFromRemote().doOnSuccess(toolList -> {
            this.tools = toolList;
            Logger.info("Successfully fetched {} tools from remote.", toolList.size());
        }).then();
    }

    /**
     * Closes the client. For {@link WebClient}, this is typically a no-op as connection pools are managed by the
     * underlying HTTP client library.
     */
    @Override
    public void close() {
        Logger.info("Closing HTTP-based client for URL: {}", assets.getUrl());
    }

    /**
     * Returns the cached list of tools provided by the remote service.
     *
     * @return A list of {@link Tool} objects; may be empty if initialization has not completed or failed.
     */
    @Override
    public List<Tool> getTools() {
        return tools != null ? tools : Collections.emptyList();
    }

    /**
     * Calls a specific tool on the remote service by sending a POST request to a conventional endpoint.
     * <p>
     * This default implementation assumes the remote service has an endpoint (e.g., {@code /mcp/call}) that accepts a
     * JSON object containing the tool name and arguments.
     *
     * @param toolName  The name of the tool to call.
     * @param arguments The arguments required by the tool.
     * @return A {@code Mono} emitting the response object from the tool execution.
     */
    @Override
    public Mono<Object> callTool(String toolName, Map<String, Object> arguments) {
        Logger.info("Calling tool '{}' on remote HTTP-based service with args: {}", toolName, arguments);
        return this.webClient.post().uri("/mcp/call") // Assuming a standard endpoint for tool calls
                .bodyValue(Map.of("toolName", toolName, "arguments", arguments)).retrieve().bodyToMono(String.class)
                .timeout(Duration.ofMillis(assets.getTimeout())).map(response -> response); // Cast String to Object to
                                                                                            // match interface
    }

    /**
     * Checks the health of the remote service by sending a GET request to a conventional health check endpoint.
     * <p>
     * This default implementation assumes the remote service has a {@code /health} endpoint that returns a 2xx status
     * code if the service is healthy.
     *
     * @return A {@code Mono<Boolean>} emitting {@code true} if the service is healthy, {@code false} otherwise.
     */
    @Override
    public Mono<Boolean> isHealthy() {
        return this.webClient.get().uri("/health") // Assuming a /health endpoint
                .retrieve().toBodilessEntity() // We only care about the status code, not the body
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .timeout(Duration.ofSeconds(5), Mono.just(false)) // Consider it unhealthy on timeout
                .onErrorReturn(false); // Consider it unhealthy on any connection error
    }

    /**
     * Fetches the list of tools from the remote service. Subclasses can override this to point to a specific endpoint.
     * <p>
     * The default implementation returns a simulated list containing a single example tool. A real implementation
     * should make a GET request to an endpoint like "/mcp/listTools" and deserialize the JSON array response into a
     * list of {@link Tool} objects.
     *
     * @return A {@code Mono} emitting a list of tools.
     */
    private Mono<List<Tool>> listToolsFromRemote() {
        // A real implementation would look like this:
        // return this.webClient.get().uri("/mcp/listTools").retrieve().bodyToFlux(Tool.class).collectList();

        // Simulation for demonstration purposes:
        Tool remoteTool = new Tool("remote_tool", "A tool from a remote HTTP service", Collections.emptyMap());
        return Mono.just(List.of(remoteTool));
    }

}
