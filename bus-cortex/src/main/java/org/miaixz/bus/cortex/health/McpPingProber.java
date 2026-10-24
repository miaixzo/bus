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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Port;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.cortex.*;

/**
 * MCP JSON-RPC ping prober.
 *
 * @author Kimi Liu
 */
public class McpPingProber implements Prober {

    /**
     * Fixed MCP ping request body.
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
     * @param timeoutMs timeout in milliseconds, clamped to at least 1
     */
    public McpPingProber(long timeoutMs) {
        this.timeoutMs = Math.max(1L, timeoutMs);
    }

    /**
     * Returns whether this prober can issue an MCP ping to the supplied instance.
     *
     * @param instance candidate instance
     * @return true when the instance and host are non-null; blank hosts and protocol support are not validated
     */
    @Override
    public boolean supports(Instance instance) {
        return instance != null && instance.getHost() != null;
    }

    /**
     * Posts the fixed JSON-RPC ping body to http://host:port/mcp, using port 80 when unspecified. Success requires no
     * reported call error, a 2xx HTTP status, and a response body containing the literal substring result; the body is
     * not parsed as a JSON-RPC response.
     *
     * @param instance non-null instance with the host to probe
     * @return healthy status only when all three checks pass; otherwise a failed status
     * @throws NullPointerException if instance is null
     */
    @Override
    public Status check(Instance instance) {
        String host = instance.getHost();
        int port = instance.getPort() != null ? instance.getPort() : Port._80.getPort();
        String url = Protocol.HTTP_PREFIX + host + Symbol.COLON + port + Symbol.SLASH + "mcp";
        long start = DateKit.current();
        Callout.Response response = Callout.postJson(url, PING_PAYLOAD, timeoutMs);
        long latency = DateKit.current() - start;
        if (response.errorMessage() != null) {
            return Status.fail("MCP ping failed: " + response.errorMessage(), name()).detail("url", url);
        }
        if (!response.successful()) {
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
