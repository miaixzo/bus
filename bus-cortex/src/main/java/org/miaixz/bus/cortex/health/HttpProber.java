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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Port;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.cortex.*;
import org.miaixz.bus.cortex.registry.ServiceRoutes;
import org.miaixz.bus.logger.Logger;

/**
 * HTTP-based prober.
 *
 * @author Kimi Liu
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
     * @param timeoutMs timeout in milliseconds, clamped to at least 1
     */
    public HttpProber(long timeoutMs) {
        this.timeoutMs = Math.max((long) Normal._1, timeoutMs);
    }

    /**
     * Returns whether this prober can probe the supplied instance through HTTP.
     *
     * @param instance candidate instance
     * @return true for a SERVICE listener explicitly using HTTP/HTTPS; legacy instance kinds retain their non-TCP
     *         eligibility check. Complete endpoint validity is checked before probing a SERVICE listener.
     */
    @Override
    public boolean supports(Instance instance) {
        return instance != null && instance.getHost() != null
                && (Builder.INSTANCE_KIND_SERVICE.equals(instance.getKind())
                        ? Protocol.HTTP.name.equals(instance.getScheme())
                                || Protocol.HTTPS.name.equals(instance.getScheme())
                        : !Protocol.TCP.name.equalsIgnoreCase(instance.healthView().getScheme()));
    }

    /**
     * Probes SERVICE instances using their explicit listener scheme and port, including the deployment context in the
     * default health path. A separate health.scheme cannot downgrade a SERVICE listener. Other legacy instance kinds
     * retain their existing health.scheme, port-80 and /health defaults.
     *
     * @param instance non-null instance defining the health endpoint
     * @return healthy status for a 2xx response without a reported call error; failed status for other responses or
     *         reported transport failures
     * @throws NullPointerException     if instance is null
     * @throws IllegalArgumentException if a SERVICE listener has incomplete or unsupported coordinates
     */
    @Override
    public Status check(Instance instance) {
        var health = instance.healthView();
        boolean service = Builder.INSTANCE_KIND_SERVICE.equals(instance.getKind());
        if (service)
            ServiceRoutes.validate(instance);
        String scheme = service ? instance.getScheme()
                : health.getScheme() != null ? health.getScheme() : Protocol.HTTP.name;
        String host = instance.getHost();
        if (host.contains(Symbol.COLON) && !host.startsWith(Symbol.BRACKET_LEFT))
            host = Symbol.BRACKET_LEFT + host + Symbol.BRACKET_RIGHT;
        int port = instance.getPort() != null ? instance.getPort() : Port._80.getPort();
        String context = service && instance.getPath() != null ? instance.getPath() : Normal.EMPTY;
        String path = health.getPath() != null ? health.getPath()
                : (context + Http.Path.HEALTH).replaceAll(Symbol.SLASH + Symbol.PLUS, Symbol.SLASH);
        String url = scheme + Symbol.COLON + Symbol.FORWARDSLASH + host + Symbol.COLON + port + path;
        long start = DateKit.current();
        Logger.debug(
                true,
                "Cortex",
                "HTTP health probe started: host={}, port={}, path={}, timeoutMs={}",
                host,
                port,
                path,
                timeoutMs);
        Callout.Response response = Callout.get(url, timeoutMs);
        long latency = DateKit.current() - start;
        if (response.errorMessage() != null) {
            Logger.warn(
                    false,
                    "Cortex",
                    "HTTP health probe failed: host={}, port={}, path={}, latencyMs={}, error={}",
                    host,
                    port,
                    path,
                    latency,
                    response.errorMessage());
            return Status.fail("HTTP check failed: " + response.errorMessage(), name()).detail("url", url);
        }
        if (response.successful()) {
            Logger.debug(
                    false,
                    "Cortex",
                    "HTTP health probe passed: host={}, port={}, path={}, status={}, latencyMs={}",
                    host,
                    port,
                    path,
                    response.statusCode(),
                    latency);
            return Status.ok(latency, name()).detail("url", url)
                    .detail("statusCode", Integer.toString(response.statusCode()));
        }
        Logger.warn(
                false,
                "Cortex",
                "HTTP health probe returned unhealthy status: host={}, port={}, path={}, status={}, latencyMs={}",
                host,
                port,
                path,
                response.statusCode(),
                latency);
        return Status.fail("HTTP status " + response.statusCode() + " from " + url, name()).detail("url", url)
                .detail("statusCode", Integer.toString(response.statusCode()));
    }

}
