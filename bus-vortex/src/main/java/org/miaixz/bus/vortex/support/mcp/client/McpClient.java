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
package org.miaixz.bus.vortex.support.mcp.client;

import org.miaixz.bus.vortex.support.mcp.Tool;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

/**
 * Defines the contract for a client that communicates with a service compliant with the Model Context Protocol (MCP).
 * <p>
 * This interface abstracts the underlying transport mechanism (e.g., stdio, SSE, HTTP), providing a unified way to
 * interact with any MCP service. Implementations are responsible for handling the specifics of serialization,
 * deserialization, and communication for their respective protocols.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface McpClient extends Closeable {

    /**
     * Initializes the client and establishes a communication channel with the MCP service.
     * <p>
     * For implementations like {@link StdioClient}, this method typically starts a background thread to listen to the
     * process's stdout for incoming messages.
     *
     * @return A {@code Mono<Void>} that completes when the client is successfully initialized and ready to use.
     */
    Mono<Void> initialize();

    /**
     * Asynchronously calls a specific tool on the remote MCP service.
     * <p>
     * This method should handle the serialization of the request, sending it over the transport layer, and matching the
     * asynchronous response to the original request.
     *
     * @param toolName  The name of the tool to call.
     * @param arguments A map of arguments to be passed to the tool, which will be serialized.
     * @return A {@code Mono} emitting the string-based result from the tool execution.
     */
    Mono<Object> callTool(String toolName, Map<String, Object> arguments);

    /**
     * Retrieves the list of tools provided by the MCP service.
     * <p>
     * This list is typically received from the service upon initialization and is cached by the client.
     *
     * @return A list of {@link Tool} objects representing the available tools.
     */
    List<Tool> getTools();

    /**
     * Asynchronously checks the health of the connection or the underlying process of the MCP service.
     *
     * @return A {@code Mono<Boolean>} emitting {@code true} if the service is healthy and reachable, {@code false}
     *         otherwise.
     */
    Mono<Boolean> isHealthy();

    /**
     * Closes the communication channel and releases any associated resources (e.g., subprocesses, network connections).
     * <p>
     * This method should be idempotent, meaning calling it multiple times should not produce an error.
     */
    @Override
    void close();

}
