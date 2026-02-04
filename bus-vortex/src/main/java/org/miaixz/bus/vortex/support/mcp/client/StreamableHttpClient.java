/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.vortex.support.mcp.client;

import org.miaixz.bus.vortex.Assets;

/**
 * An {@link McpClient} implementation for services that provide responses over a generic, streamable HTTP connection.
 * <p>
 * This client extends {@link HttpClient} and is intended for services that stream data using mechanisms like chunked
 * transfer encoding, but do not follow the formal Server-Sent Events (SSE) protocol. The base implementation assumes a
 * simple request-response pattern.
 * <p>
 * <strong>Note:</strong> To handle actual streaming responses, the {@link #callTool} method would need to be overridden
 * to process a {@code Flux<String>} or a similar reactive stream from the {@code WebClient} response.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StreamableHttpClient extends HttpClient {

    /**
     * Constructs a new {@code StreamableHttpClient}.
     *
     * @param assets The {@link Assets} configuration for this client, containing the base URL of the remote service.
     */
    public StreamableHttpClient(Assets assets) {
        super(assets);
    }

}
