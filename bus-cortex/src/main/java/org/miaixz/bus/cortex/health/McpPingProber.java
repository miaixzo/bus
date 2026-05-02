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
package org.miaixz.bus.cortex.health;

import org.miaixz.bus.cortex.Prober;
import org.miaixz.bus.cortex.Status;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Callout;

/**
 * MCP JSON-RPC ping prober.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class McpPingProber implements Prober {

    /**
     * Ping payload.
     */

    private static final String PING_PAYLOAD = "{\"jsonrpc\":\"2.0\",\"method\":\"ping\",\"id\":1}";
    /**
     * Request timeout in milliseconds.
     */
    private final long timeoutMs;

    /**
     * Creates an MCP ping prober with the default timeout.
     */
    public McpPingProber() {
        this(Builder.DEFAULT_HEALTH_TIMEOUT_MS);
    }

    /**
     * Creates an MCP ping prober with an explicit timeout.
     *
     * @param timeoutMs timeout in milliseconds
     */
    public McpPingProber(long timeoutMs) {
        this.timeoutMs = Math.max(1L, timeoutMs);
    }

    /**
     * Returns whether this prober can issue an MCP ping to the supplied instance.
     *
     * @param instance candidate instance
     * @return {@code true} when MCP probing is supported
     */
    @Override
    public boolean supports(Instance instance) {
        return instance != null && instance.getHost() != null;
    }

    /**
     * Sends a JSON-RPC ping to the instance's MCP endpoint.
     *
     * @param instance instance to probe
     * @return healthy result with latency if a response is received, fail result otherwise
     */
    @Override
    public Status check(Instance instance) {
        String host = instance.getHost();
        int port = instance.getPort() != null ? instance.getPort() : 80;
        String url = "http://" + host + ":" + port + "/mcp";
        long start = System.currentTimeMillis();
        Callout.Response response = Callout.postJson(url, PING_PAYLOAD, timeoutMs);
        long latency = System.currentTimeMillis() - start;
        if (response.errorMessage() != null) {
            return Status.fail("MCP ping failed: " + response.errorMessage(), name()).detail("url", url);
        }
        if (!response.isSuccessful()) {
            return Status.fail("MCP ping returned status " + response.statusCode(), name()).detail("url", url)
                    .detail("statusCode", Integer.toString(response.statusCode()));
        }
        if (response.body() != null && response.body().contains("result")) {
            return Status.ok(latency, name()).detail("url", url)
                    .detail("statusCode", Integer.toString(response.statusCode()));
        }
        return Status.fail("No response from MCP ping at " + url, name()).detail("url", url);
    }

}
