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
 * HTTP-based prober.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HttpProber implements Prober {

    /**
     * Request timeout in milliseconds.
     */
    private final long timeoutMs;

    /**
     * Creates an HTTP prober with the default timeout.
     */
    public HttpProber() {
        this(Builder.DEFAULT_HEALTH_TIMEOUT_MS);
    }

    /**
     * Creates an HTTP prober with an explicit timeout.
     *
     * @param timeoutMs timeout in milliseconds
     */
    public HttpProber(long timeoutMs) {
        this.timeoutMs = Math.max(1L, timeoutMs);
    }

    /**
     * Returns whether this prober can probe the supplied instance through HTTP.
     *
     * @param instance candidate instance
     * @return {@code true} when HTTP probing is supported
     */
    @Override
    public boolean supports(Instance instance) {
        return instance != null && instance.getHost() != null && !"tcp".equalsIgnoreCase(instance.getScheme());
    }

    /**
     * Performs an HTTP GET request to the instance's health endpoint.
     *
     * @param instance instance to probe
     * @return healthy result with latency on success, fail result on error
     */
    @Override
    public Status check(Instance instance) {
        String scheme = instance.getScheme() != null ? instance.getScheme() : "http";
        String host = instance.getHost();
        int port = instance.getPort() != null ? instance.getPort() : 80;
        String path = instance.getHealthPath() != null ? instance.getHealthPath() : "/health";
        String url = scheme + "://" + host + ":" + port + path;
        long start = System.currentTimeMillis();
        Callout.Response response = Callout.get(url, timeoutMs);
        long latency = System.currentTimeMillis() - start;
        if (response.errorMessage() != null) {
            return Status.fail("HTTP check failed: " + response.errorMessage(), name()).detail("url", url);
        }
        if (response.isSuccessful()) {
            return Status.ok(latency, name()).detail("url", url)
                    .detail("statusCode", Integer.toString(response.statusCode()));
        }
        return Status.fail("HTTP status " + response.statusCode() + " from " + url, name()).detail("url", url)
                .detail("statusCode", Integer.toString(response.statusCode()));
    }

}
