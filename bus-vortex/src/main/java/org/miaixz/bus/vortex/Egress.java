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
package org.miaixz.bus.vortex;

import java.net.URI;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.logger.Logger;

import reactor.netty.http.client.HttpClient;

/**
 * Facade for outbound HTTP resources used by Vortex reverse proxy executors.
 * <p>
 * The class intentionally centralizes the current Reactor Netty {@link HttpClient} and Spring {@link WebClient}
 * construction without introducing a multi-client transport abstraction. REST and MCP executors use this facade to
 * create downstream request specs while keeping the low-level client, connector, codec limit and connection pool wiring
 * in one place.
 * <p>
 * Connection pool ownership remains in {@link Holder}; this class only creates HTTP client resources backed by
 * {@link Holder#connectionProvider()} and does not dispose the provider.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Egress {

    /**
     * Maximum in-memory codec buffer used by the shared outbound {@link WebClient}.
     */
    private static final int MAX_IN_MEMORY_SIZE = Math.toIntExact(Normal.MEBI_128);

    /**
     * Utility class constructor.
     */
    private Egress() {
        throw new UnsupportedOperationException("Egress class cannot be instantiated");
    }

    /**
     * Creates a downstream request spec from the shared outbound {@link WebClient}.
     * <p>
     * The returned spec is still configured by the caller for headers, body insertion and response handling. This keeps
     * protocol-specific proxy behavior in the executor while centralizing the underlying HTTP client resources here.
     *
     * @param method The downstream HTTP method.
     * @param uri    The downstream URI.
     * @return The request body spec used by REST and MCP executors.
     */
    public static WebClient.RequestBodySpec request(HttpMethod method, URI uri) {
        return client().method(method).uri(uri);
    }

    /**
     * Returns the shared outbound {@link WebClient}.
     * <p>
     * This method is exposed for framework-level integration points that need direct WebClient access. Changes applied
     * to the shared client construction, such as global filters or codecs, affect all REST and MCP outbound proxy
     * flows.
     *
     * @return The shared WebClient.
     */
    public static WebClient webClient() {
        return client();
    }

    /**
     * Checks whether outbound HTTP resources have already been initialized.
     * <p>
     * This is primarily useful for diagnostics and lifecycle checks because resource creation is lazy and
     * request-driven.
     *
     * @return {@code true} if resources are initialized.
     */
    public static boolean initialized() {
        return ClientHolder.initialized();
    }

    /**
     * Returns the lazily initialized outbound HTTP client facade.
     *
     * @return The shared outbound WebClient.
     */
    private static WebClient client() {
        return ClientHolder.get();
    }

    /**
     * Creates the shared WebClient facade backed by Reactor Netty.
     * <p>
     * The {@link HttpClient} is created from the existing Vortex connection provider so current connection pool
     * performance settings continue to apply. The {@link ExchangeStrategies} instance keeps the previous codec memory
     * limit while moving the configuration out of individual executors.
     *
     * @return The newly created outbound WebClient.
     */
    private static WebClient create() {
        HttpClient httpClient = HttpClient.create(Holder.connectionProvider());
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE)).build();
        WebClient webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies).build();

        Logger.info(true, "Vortex", "Outbound HTTP client initialized: implementation={}", "reactor-netty");
        return webClient;
    }

    /**
     * Lazy holder for the outbound HTTP client.
     * <p>
     * The holder uses double-checked locking so the connection provider is not touched during class loading. This
     * avoids creating HTTP resources before {@link Holder#of(org.miaixz.bus.vortex.magic.Performance)} has applied the
     * runtime performance configuration.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class ClientHolder {

        /**
         * Shared outbound WebClient, initialized on the first outbound proxy request.
         */
        private static volatile WebClient client;

        /**
         * Utility holder constructor.
         */
        private ClientHolder() {
            // No initialization required.
        }

        /**
         * Returns the shared client, creating it once when first requested.
         *
         * @return The shared outbound WebClient.
         */
        private static WebClient get() {
            WebClient current = client;
            if (current == null) {
                synchronized (ClientHolder.class) {
                    current = client;
                    if (current == null) {
                        current = create();
                        client = current;
                    }
                }
            }
            return current;
        }

        /**
         * Checks whether the lazy holder has already created the shared resources.
         *
         * @return {@code true} if resources are already initialized.
         */
        private static boolean initialized() {
            return client != null;
        }

    }

}
