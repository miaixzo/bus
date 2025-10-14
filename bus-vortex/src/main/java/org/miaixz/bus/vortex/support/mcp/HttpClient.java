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
package org.miaixz.bus.vortex.support.mcp;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * Abstract base class for HTTP-based MCP clients. It encapsulates the common logic for communicating with remote MCP
 * services using WebClient.
 */
public abstract class HttpClient implements McpClient {

    protected final Assets assets;
    protected final WebClient webClient;
    protected List<Tool> tools;

    /**
     * Constructs a new HttpClient.
     * 
     * @param assets The Assets configuration, must contain a "url".
     */
    public HttpClient(Assets assets) {
        this.assets = assets;
        String url = assets.getUrl();
        if (StringKit.isEmpty(url)) {
            throw new IllegalArgumentException("HTTP-based client assets must contain a 'url'");
        }
        this.webClient = WebClient.builder().baseUrl(url).build();
    }

    @Override
    public Mono<Void> initialize() {
        // In a real implementation, this would fetch the tool list from a remote endpoint.
        Logger.info("Initializing HTTP-based client for URL: {}", assets.getUrl());
        return listToolsFromRemote().doOnSuccess(toolList -> {
            this.tools = toolList;
            Logger.info("Successfully fetched {} tools from remote.", toolList.size());
        }).then();
    }

    @Override
    public void close() {
        // WebClient typically does not need explicit closing.
        Logger.info("Closing HTTP-based client for URL: {}", assets.getUrl());
    }

    @Override
    public List<Tool> getTools() {
        return tools != null ? tools : Collections.emptyList();
    }

    @Override
    public Mono<String> callTool(String toolName, Map<String, Object> arguments) {
        // A real implementation would construct a proper MCP JSON request body.
        Logger.info("Calling tool '{}' on remote HTTP-based service with args: {}", toolName, arguments);
        return this.webClient.post().uri("/mcp/call") // Assuming a standard endpoint for tool calls
                .bodyValue(Map.of("toolName", toolName, "arguments", arguments)).retrieve().bodyToMono(String.class)
                .timeout(Duration.ofMillis(assets.getTimeout()));
    }

    /**
     * Checks the health of the remote service by sending a lightweight request.
     * 
     * @return A Mono emitting true if the service is healthy, false otherwise.
     */
    @Override
    public Mono<Boolean> isHealthy() {
        // A common practice is to have a dedicated /health or /ping endpoint.
        return this.webClient.get().uri("/health") // Assuming a /health endpoint
                .retrieve().toBodilessEntity() // We only care about the status code, not the body
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .timeout(Duration.ofSeconds(5), Mono.just(false)) // Consider it unhealthy on timeout
                .onErrorReturn(false); // Consider it unhealthy on any connection error
    }

    /**
     * Simulates fetching the tool list from the remote service.
     * 
     * @return A Mono containing a list of tools.
     */
    private Mono<List<Tool>> listToolsFromRemote() {
        // A real implementation would make a GET request to an endpoint like "/mcp/listTools"
        // and expect a JSON array of Tool objects.
        // return this.webClient.get().uri("/mcp/listTools").retrieve().bodyToFlux(Tool.class).collectList();

        // Simulation:
        Tool remoteTool = new Tool("remote_tool", "A tool from a remote HTTP service", Collections.emptyMap());
        return Mono.just(List.of(remoteTool));
    }

}
