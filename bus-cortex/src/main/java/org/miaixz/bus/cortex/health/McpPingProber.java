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
import org.miaixz.bus.http.Httpx;

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
        try {
            String response = Httpx.post(url, PING_PAYLOAD, "application/json");
            long latency = System.currentTimeMillis() - start;
            if (response != null && response.contains("result")) {
                return Status.ok(latency);
            }
            if (response != null) {
                return Status.ok(latency);
            }
            return Status.fail("No response from MCP ping at " + url);
        } catch (Exception e) {
            return Status.fail("MCP ping failed: " + e.getMessage());
        }
    }

}
