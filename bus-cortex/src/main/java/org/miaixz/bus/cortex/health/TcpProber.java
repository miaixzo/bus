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

import java.net.InetSocketAddress;
import java.net.Socket;

import org.miaixz.bus.cortex.Builder;
import org.miaixz.bus.cortex.Instance;
import org.miaixz.bus.cortex.Prober;
import org.miaixz.bus.cortex.Status;

/**
 * TCP connectivity prober.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TcpProber implements Prober {

    /**
     * TCP connect timeout in milliseconds.
     */
    private final int timeoutMs;

    /**
     * Creates a TCP prober with the default timeout.
     */
    public TcpProber() {
        this(Builder.DEFAULT_HEALTH_TIMEOUT_MS);
    }

    /**
     * Creates a TCP prober with an explicit timeout.
     *
     * @param timeoutMs timeout in milliseconds
     */
    public TcpProber(long timeoutMs) {
        this.timeoutMs = (int) Math.max(1L, timeoutMs);
    }

    /**
     * Returns whether this prober can attempt a TCP connection to the supplied instance.
     *
     * @param instance candidate instance
     * @return {@code true} when TCP probing is supported
     */
    @Override
    public boolean supports(Instance instance) {
        return instance != null && instance.getHost() != null && instance.getPort() != null;
    }

    /**
     * Attempts a TCP socket connection to the instance's host and port.
     *
     * @param instance instance to probe
     * @return healthy result with latency on success, fail result on error
     */
    @Override
    public Status check(Instance instance) {
        String host = instance.getHost();
        int port = instance.getPort() != null ? instance.getPort() : 80;
        long start = System.currentTimeMillis();
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            long latency = System.currentTimeMillis() - start;
            return Status.ok(latency, name()).detail("host", host).detail("port", Integer.toString(port));
        } catch (Exception e) {
            return Status.fail("TCP check failed: " + e.getMessage(), name()).detail("host", host)
                    .detail("port", Integer.toString(port));
        }
    }

}
