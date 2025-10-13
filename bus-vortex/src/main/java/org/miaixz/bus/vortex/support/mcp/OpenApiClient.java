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

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A client that adapts manually configured REST API endpoints to function as MCP tools. It does not parse an OpenAPI
 * spec, but instead relies on tool definitions provided in its configuration.
 */
public class OpenApiClient implements McpClient {

    private final Assets assets;
    private final WebClient webClient;
    private final String baseUrl;
    private final Map<String, Tool> adaptedTools;

    /**
     * Constructs a new OpenApiClient.
     * 
     * @param assets The Assets configuration, which must contain a "url" for baseUrl and a JSON string in its 'config'
     *               field containing a "tools" list.
     */
    public OpenApiClient(Assets assets) {
        this.assets = assets;
        this.baseUrl = assets.getUrl();
        if (StringKit.isEmpty(this.baseUrl)) {
            throw new IllegalArgumentException("OpenAPI assets must contain a 'url' for baseUrl");
        }
        this.webClient = WebClient.builder().baseUrl(this.baseUrl).build();
        this.adaptedTools = initializeTools();
    }

    /**
     * Initializes the client by parsing the manually defined tools from the configuration.
     * 
     * @return A Mono that completes immediately, as initialization is synchronous.
     */
    @Override
    public Mono<Void> initialize() {
        return Mono.fromRunnable(() -> {
            Logger.info(
                    "OpenApiClient initialized for baseUrl: {}. Found {} tools.",
                    this.baseUrl,
                    adaptedTools.size());
        });
    }

    /**
     * Parses the "tools" list from the Assets' config field.
     * 
     * @return A map of tools, keyed by their name.
     */
    private Map<String, Tool> initializeTools() {
        if (StringKit.isEmpty(assets.getConfig())) {
            Logger.warn("OpenAPI asset '{}' has no config field. No tools will be loaded.", assets.getName());
            return Collections.emptyMap();
        }

        Map<String, Object> rawConfig = JsonKit.toMap(assets.getConfig());
        List<Map<String, Object>> toolConfigs = (List<Map<String, Object>>) rawConfig.get("tools");

        if (toolConfigs == null || toolConfigs.isEmpty()) {
            Logger.warn("OpenAPI asset '{}' config has no 'tools' list. No tools will be loaded.", assets.getName());
            return Collections.emptyMap();
        }

        return toolConfigs.stream().map(toolConfig -> {
            String name = (String) toolConfig.get("name");
            String description = (String) toolConfig.get("description");
            Map<String, Object> schema = (Map<String, Object>) toolConfig.get("inputSchema");
            if (schema == null) {
                schema = Collections.emptyMap();
            }
            // Store the raw config map in the schema for later use in callTool
            schema.put("_rawConfig", toolConfig);
            return new Tool(name, description, schema);
        }).collect(Collectors.toMap(Tool::getName, Function.identity()));
    }

    @Override
    public void close() {
        Logger.info("Closing OpenAPI client for baseUrl: {}", this.baseUrl);
    }

    @Override
    public List<Tool> getTools() {
        return adaptedTools != null ? new ArrayList<>(adaptedTools.values()) : Collections.emptyList();
    }

    @Override
    public Mono<String> callTool(String toolName, Map<String, Object> arguments) {
        Tool tool = adaptedTools.get(toolName);
        if (tool == null) {
            return Mono.error(new UnsupportedOperationException("Tool '" + toolName + "' not found."));
        }

        Map<String, Object> rawConfig = (Map<String, Object>) tool.getInputSchema().get("_rawConfig");
        String endpoint = (String) rawConfig.get("endpoint");
        String httpMethod = ((String) rawConfig.get("method")).toUpperCase();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath(endpoint);

        // Separate arguments into path, query, and body
        Map<String, Object> pathParamsSchema = (Map<String, Object>) tool.getInputSchema()
                .getOrDefault("path", Collections.emptyMap());
        Map<String, Object> queryParamsSchema = (Map<String, Object>) tool.getInputSchema()
                .getOrDefault("query", Collections.emptyMap());

        // Populate query parameters
        queryParamsSchema.keySet().forEach(key -> {
            if (arguments.containsKey(key)) {
                uriBuilder.queryParam(key, arguments.get(key));
            }
        });

        // Build the final URI, substituting path variables
        String finalUri = uriBuilder.buildAndExpand(arguments).toUriString();

        WebClient.RequestBodySpec requestSpec = webClient.method(HttpMethod.valueOf(httpMethod)).uri(finalUri);

        // Handle request body
        if (tool.getInputSchema().containsKey("body")) {
            Object bodyValue = arguments.get("body");
            if (bodyValue != null) {
                requestSpec.bodyValue(bodyValue);
            }
        }

        Logger.info("Executing OpenAPI tool '{}': {} {}", toolName, httpMethod, finalUri);

        return requestSpec.retrieve().bodyToMono(String.class)
                .doOnSuccess(response -> Logger.info("Received response for tool '{}'", toolName));
    }

    /**
     * Checks the health of the remote API by sending a lightweight OPTIONS request to its base URL.
     * 
     * @return A Mono emitting true if the API is reachable, false otherwise.
     */
    @Override
    public Mono<Boolean> isHealthy() {
        return this.webClient.options().uri("") // Check the base URL
                .retrieve().toBodilessEntity().map(response -> !response.getStatusCode().is5xxServerError()) // Consider
                                                                                                             // any
                                                                                                             // non-5xx
                                                                                                             // code as
                                                                                                             // healthy/reachable
                .timeout(Duration.ofSeconds(5), Mono.just(false)).onErrorReturn(false);
    }
}
