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
 * @since Java 17+
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
    Mono<String> callTool(String toolName, Map<String, Object> arguments);

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
