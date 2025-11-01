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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.support.mcp.Tool;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;

/**
 * A client that adapts manually configured REST API endpoints to function as MCP (Model Context Protocol) tools.
 * <p>
 * This client does not parse an OpenAPI specification file. Instead, it relies on a set of tool definitions provided
 * directly in its configuration (typically within the {@code metadata} field of an {@link Assets} object). Each tool
 * definition specifies an HTTP endpoint, method, and an input schema, which this client uses to translate MCP tool
 * calls into HTTP requests.
 *
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class OpenApiClient implements McpClient {

    /**
     * The configuration asset for this client, containing the base URL and tool definitions.
     */
    private final Assets assets;
    /**
     * The {@link WebClient} used to make HTTP requests to the target API.
     */
    private final WebClient client;
    /**
     * The base URL for all API endpoints configured for this client.
     */
    private final String baseUrl;
    /**
     * A map of available tools, keyed by their unique name for quick lookup.
     */
    private final Map<String, Tool> tools;

    /**
     * Constructs a new {@code OpenApiClient}.
     * <p>
     * The provided {@link Assets} must contain a {@code url} for the base API URL and a {@code metadata} field. The
     * {@code metadata} should be a JSON string containing a top-level {@code "tools"} array, where each element defines
     * a tool.
     *
     * @param assets The configuration asset for this client. It must not be {@code null} and must contain a valid URL.
     */
    public OpenApiClient(Assets assets) {
        this.assets = assets;
        this.baseUrl = assets.getUrl();
        if (StringKit.isEmpty(this.baseUrl)) {
            throw new IllegalArgumentException("OpenAPI assets must contain a 'url' for baseUrl");
        }
        this.client = WebClient.builder().baseUrl(this.baseUrl).build();
        this.tools = initializeTools();
    }

    /**
     * Initializes the client.
     * <p>
     * The actual tool parsing is performed synchronously in the constructor. This method exists to comply with the
     * {@link McpClient} interface and simply logs a confirmation message.
     *
     * @return A {@link Mono} that completes immediately after logging the initialization status.
     */
    @Override
    public Mono<Void> initialize() {
        return Mono.fromRunnable(() -> {
            Logger.info("OpenApiClient initialized for baseUrl: {}. Found {} tools.", this.baseUrl, this.tools.size());
        });
    }

    /**
     * Parses the tool definitions from the {@code metadata} of the {@link Assets} object.
     * <p>
     * The expected JSON structure in {@code assets.getMetadata()} is:
     * 
     * <pre>
     * {
     *   "tools": [
     *     {
     *       "name": "getUser",
     *       "description": "Retrieves a user by ID.",
     *       "endpoint": "/users/{id}",
     *       "method": "GET",
     *       "inputSchema": {
     *         "path": { "id": "string" },
     *         "query": { "details": "boolean" }
     *       }
     *     }
     *   ]
     * }
     * </pre>
     *
     * @return A map of {@link Tool} objects, keyed by their name.
     */
    private Map<String, Tool> initializeTools() {
        if (StringKit.isEmpty(assets.getMetadata())) {
            Logger.warn("OpenAPI asset '{}' has no metadata field. No tools will be loaded.", assets.getName());
            return Collections.emptyMap();
        }

        Map<String, Object> rawConfig = JsonKit.toMap(assets.getMetadata());
        List<Map<String, Object>> toolConfigs = (List<Map<String, Object>>) rawConfig.get("tools");

        if (toolConfigs == null || toolConfigs.isEmpty()) {
            Logger.warn("OpenAPI asset '{}' metadata has no 'tools' list. No tools will be loaded.", assets.getName());
            return Collections.emptyMap();
        }

        return toolConfigs.stream().map(toolConfig -> {
            String name = (String) toolConfig.get("name");
            String description = (String) toolConfig.get("description");
            // The inputSchema defines the parameters for the tool.
            Map<String, Object> schema = (Map<String, Object>) toolConfig.get("inputSchema");
            if (schema == null) {
                schema = new HashMap<>(); // Ensure it's a mutable map
            } else {
                // JsonKit might return an immutable map, so we create a new mutable one.
                schema = new HashMap<>(schema);
            }
            // Store the entire raw configuration map for later use in callTool.
            // This is a practical way to access endpoint and method without defining more fields.
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
        return tools != null ? new ArrayList<>(tools.values()) : Collections.emptyList();
    }

    /**
     * Executes a tool call by translating it into an HTTP request.
     * <p>
     * This method performs the following steps:
     * <ol>
     * <li>Looks up the tool by {@code toolName}.</li>
     * <li>Extracts the {@code endpoint} and {@code httpMethod} from the tool's raw configuration.</li>
     * <li>Separates the provided {@code arguments} into path, query, and body parameters based on the tool's
     * {@code inputSchema}.</li>
     * <li>Constructs the final request URI, substituting path variables.</li>
     * <li>Executes the HTTP request and returns the response body as a {@code String}.</li>
     * </ol>
     * 
     * @param toolName  The name of the tool to execute.
     * @param arguments A map of arguments for the tool, keyed by parameter name.
     * @return A {@link Mono} emitting the response body as a {@code String}, or an error if the tool is not found or
     *         the request fails.
     */
    @Override
    public Mono<String> callTool(String toolName, Map<String, Object> arguments) {
        // 1. Look up the tool definition.
        Tool tool = tools.get(toolName);
        if (tool == null) {
            return Mono.error(new ValidateException("Tool '" + toolName + "' not found."));
        }

        // 2. Extract raw configuration details (endpoint, method).
        Map<String, Object> rawConfig = (Map<String, Object>) tool.getInputSchema().get("_rawConfig");
        String endpoint = (String) rawConfig.get("endpoint");
        String httpMethod = ((String) rawConfig.get("method")).toUpperCase();

        // 3. Start building the URI.
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath(endpoint);

        // 4. Separate arguments into path, query, and body based on the schema.
        Map<String, Object> pathParamsSchema = (Map<String, Object>) tool.getInputSchema()
                .getOrDefault("path", Collections.emptyMap());
        Map<String, Object> queryParamsSchema = (Map<String, Object>) tool.getInputSchema()
                .getOrDefault("query", Collections.emptyMap());

        // 5. Populate query parameters from arguments.
        queryParamsSchema.keySet().forEach(key -> {
            if (arguments.containsKey(key)) {
                uriBuilder.queryParam(key, arguments.get(key));
            }
        });

        // 6. Build the final URI, substituting path variables from arguments.
        String finalUri = uriBuilder.buildAndExpand(arguments).toUriString();

        // 7. Create the request specification.
        WebClient.RequestBodySpec requestSpec = client.method(HttpMethod.valueOf(httpMethod)).uri(finalUri);

        // 8. Handle the request body if the schema defines one.
        if (tool.getInputSchema().containsKey("body")) {
            Object bodyValue = arguments.get("body");
            if (bodyValue != null) {
                requestSpec.bodyValue(bodyValue);
            }
        }

        Logger.info("Executing OpenAPI tool '{}': {} {}", toolName, httpMethod, finalUri);

        // 9. Execute the request and return the response body.
        return requestSpec.retrieve().bodyToMono(String.class)
                .doOnSuccess(response -> Logger.info("Received response for tool '{}'", toolName));
    }

    /**
     * Checks the health of the remote API by sending a lightweight {@code OPTIONS} request to its base URL.
     * <p>
     * An {@code OPTIONS} request is used as it is typically a low-overhead operation. The API is considered healthy if
     * the request completes without an error and does not return a 5xx server error status. The operation includes a
     * 5-second timeout to prevent the client from waiting indefinitely on an unresponsive host.
     * 
     * @return A {@link Mono} emitting {@code true} if the API is reachable and returns a non-5xx status, {@code false}
     *         otherwise.
     */
    @Override
    public Mono<Boolean> isHealthy() {
        return this.client.options().uri("") // Check the base URL
                .retrieve().toBodilessEntity().map(response -> !response.getStatusCode().is5xxServerError()) // Any
                                                                                                             // non-5xx
                                                                                                             // is
                                                                                                             // considered
                                                                                                             // healthy
                .timeout(Duration.ofSeconds(5), Mono.just(false)) // Fallback to false on timeout
                .onErrorReturn(false); // Fallback to false on any other error
    }

}
