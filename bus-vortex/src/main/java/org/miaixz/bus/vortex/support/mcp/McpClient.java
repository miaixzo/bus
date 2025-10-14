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

import java.util.List;
import java.util.Map;

import reactor.core.publisher.Mono;

/**
 * The unified interface for an MCP client. It defines the core behaviors for interacting with any type of MCP service.
 */
public interface McpClient {

    /**
     * Asynchronously initializes the client and connects to the backend service.
     * 
     * @return A Mono that completes upon successful initialization and connection.
     */
    Mono<Void> initialize();

    /**
     * Closes the client and releases all associated resources (e.g., subprocesses, network connections).
     */
    void close();

    /**
     * Retrieves the list of tools provided by the service this client is connected to.
     * 
     * @return A list of Tool objects.
     */
    List<Tool> getTools();

    /**
     * Calls a specific tool provided by the service.
     * 
     * @param toolName  The name of the tool to call.
     * @param arguments The arguments required by the tool.
     * @return A Mono containing the execution result as a JSON string.
     */
    Mono<String> callTool(String toolName, Map<String, Object> arguments);

    /**
     * Asynchronously checks the health of the underlying connection or process.
     * 
     * @return A Mono that emits true if the client is healthy, false otherwise.
     */
    Mono<Boolean> isHealthy();

}
