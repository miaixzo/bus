/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.basic.entity.Message;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.support.mcp.Tool;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * An implementation of {@link McpClient} that communicates with a local MCP service process via standard I/O (stdio).
 * <p>
 * This client sends JSON-RPC 2.0 requests to the process's stdin and receives responses from its stdout. It manages the
 * asynchronous request-response lifecycle using a map of {@link CompletableFuture}s.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StdioClient implements McpClient {

    /**
     * Configuration of the MCP service.
     */
    private final Assets assets;

    /**
     * The running process handle for the MCP service.
     */
    private final Process process;

    /**
     * Writer for sending JSON-RPC requests to the process's stdin.
     */
    private final BufferedWriter writer;

    /**
     * Executor service for listening to stdout in a separate thread.
     */
    private final ExecutorService stdoutListenerExecutor;

    /**
     * Map of pending requests keyed by request ID, used to match responses with requests.
     */
    private final ConcurrentMap<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

    /**
     * List of available tools provided by the MCP service.
     */
    private List<Tool> tools = Collections.emptyList();

    /**
     * Constructs a new {@code StdioClient}.
     *
     * @param assets  The configuration of the service.
     * @param process The running {@link Process} handle for the service.
     */
    public StdioClient(Assets assets, Process process) {
        Assert.notNull(assets, "Assets cannot be null");
        Assert.notNull(process, "Process handle cannot be null");
        this.assets = assets;
        this.process = process;
        this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        this.stdoutListenerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "mcp-stdio-client-" + assets.getName());
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Initializes the client by starting a thread to listen for responses from the process's stdout.
     *
     * @return A {@link Mono} that completes when initialization is done.
     */
    @Override
    public Mono<Void> initialize() {
        return Mono.fromRunnable(() -> {
            stdoutListenerExecutor.submit(() -> {
                try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        handleIncomingMessage(line);
                    }
                } catch (IOException e) {
                    if (!process.isAlive()) {
                        Logger.info("Process for service '{}' terminated, stopping stdout listener.", assets.getName());
                    } else {
                        Logger.error("Error reading stdout for service '{}': {}", assets.getName(), e.getMessage());
                    }
                }
            });
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    /**
     * Calls a tool on the MCP service with the given name and arguments.
     *
     * @param toolName  The name of the tool to call.
     * @param arguments The arguments to pass to the tool.
     * @return A {@link Mono} that emits the tool's result as an Object.
     */
    @Override
    public Mono<Object> callTool(String toolName, Map<String, Object> arguments) {
        return Mono.fromFuture(() -> {
            String requestId = UUID.randomUUID().toString();
            McpRequest request = new McpRequest("2.0", requestId, toolName, arguments);
            CompletableFuture<String> future = new CompletableFuture<>();
            pendingRequests.put(requestId, future);

            try {
                String jsonRequest = JsonKit.toJsonString(request);
                writer.write(jsonRequest + "\n");
                writer.flush();
            } catch (IOException e) {
                pendingRequests.remove(requestId);
                future.completeExceptionally(e);
            }

            return future;
        }).map(response -> (Object) response); // Cast String to Object to match interface
    }

    /**
     * Gets the list of available tools from the MCP service.
     *
     * @return A list of {@link Tool} objects representing the available tools.
     */
    @Override
    public List<Tool> getTools() {
        return this.tools;
    }

    /**
     * Checks if the MCP service process is still alive.
     *
     * @return A {@link Mono} that emits {@code true} if the process is alive, {@code false} otherwise.
     */
    @Override
    public Mono<Boolean> isHealthy() {
        return Mono.just(process.isAlive());
    }

    /**
     * Closes the client by shutting down the stdout listener and destroying the process if it's still alive.
     */
    @Override
    public void close() {
        Logger.info("Closing StdioClient for service '{}'", assets.getName());
        stdoutListenerExecutor.shutdownNow();
        try {
            if (process.isAlive()) {
                process.destroy();
            }
        } catch (Exception e) {
            Logger.error("Error destroying process for service '{}'", assets.getName(), e);
        }
    }

    /**
     * Handles incoming messages from the process's stdout, parsing them as JSON-RPC responses or notifications.
     *
     * @param jsonLine The JSON message received from stdout.
     */
    private void handleIncomingMessage(String jsonLine) {
        try {
            Map<String, Object> messageMap = JsonKit.toMap(jsonLine);

            // Check if it's a response (has an 'id')
            if (messageMap.containsKey("id") && messageMap.get("id") != null) {
                McpMessage response = JsonKit.toPojo(messageMap, McpMessage.class);
                CompletableFuture<String> future = pendingRequests.remove(response.id);
                if (future != null) {
                    if (response.error != null) {
                        future.completeExceptionally(
                                new RuntimeException("Tool execution error: " + response.error.errmsg));
                    } else {
                        // The result can be a string or a structured object, always serialize to string
                        future.complete(JsonKit.toJsonString(response.result));
                    }
                }
            }
            // Check if it's a notification (has a 'method')
            else if (messageMap.containsKey("method")) {
                McpMessage notification = JsonKit.toPojo(messageMap, McpMessage.class);
                if ("tool_list".equals(notification.method)) {
                    if (notification.params instanceof List) {
                        List<Map<String, Object>> toolMaps = (List<Map<String, Object>>) notification.params;
                        this.tools = toolMaps.stream().map(toolMap -> JsonKit.toPojo(toolMap, Tool.class))
                                .collect(Collectors.toList());
                        Logger.info(
                                "Received tool list from service '{}': {} tools found.",
                                assets.getName(),
                                tools.size());
                    }
                }
            }
        } catch (Exception e) {
            Logger.warn("Received non-JSON or malformed message from service '{}': {}", assets.getName(), jsonLine, e);
        }
    }

    // Inner classes for JSON-RPC 2.0 serialization/deserialization

    /**
     * Represents a JSON-RPC 2.0 request sent to the MCP service.
     */
    private static class McpRequest {

        /**
         * The JSON-RPC version, always "2.0".
         */
        String jsonrpc;

        /**
         * Unique identifier for the request.
         */
        String id;

        /**
         * The method name to invoke.
         */
        String method;

        /**
         * Parameters for the method.
         */
        Map<String, Object> params;

        McpRequest(String jsonrpc, String id, String method, Map<String, Object> params) {
            this.jsonrpc = jsonrpc;
            this.id = id;
            this.method = method;
            this.params = params;
        }
    }

    /**
     * Represents a JSON-RPC 2.0 message received from the MCP service, which can be either a response or a
     * notification.
     */
    private static class McpMessage {

        /**
         * The JSON-RPC version, always "2.0".
         */
        String jsonrpc;

        /**
         * Unique identifier for the request/response (null for notifications).
         */
        String id;

        /**
         * The method name (for notifications).
         */
        String method;

        /**
         * The result of a successful request.
         */
        Object result;

        /**
         * Parameters for notifications.
         */
        Object params;

        /**
         * Error information if the request failed.
         */
        Message error;
    }

}
