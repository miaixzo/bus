/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.vortex.registry;

import org.miaixz.bus.core.lang.EnumValue;
import org.miaixz.bus.vortex.Assets;
import org.miaixz.bus.vortex.magic.Metrics;
import org.miaixz.bus.vortex.magic.Transmit;
import org.miaixz.bus.vortex.provider.MetricsProvider;
import org.miaixz.bus.vortex.provider.ProcessProvider;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * A central, in-memory registry that provides a consolidated, real-time view of the status and metrics of all managed
 * services.
 * <p>
 * This service is intended to be used by management APIs. It orchestrates calls to the {@link AssetsRegistry},
 * {@link ProcessProvider}, and {@link MetricsProvider} to build a comprehensive {@link Transmit} for each configured
 * service.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ServerRegistry {

    /**
     * The registry for static API asset configurations.
     */
    private final AssetsRegistry assetsRegistry;

    /**
     * The provider for managing service process lifecycles (e.g., start, stop, status).
     */
    private final ProcessProvider processProvider;

    /**
     * The provider for fetching dynamic process performance metrics (e.g., CPU, memory).
     */
    private final MetricsProvider metricsProvider;

    /**
     * Constructs a new {@code ServerRegistry}.
     *
     * @param assetsRegistry  The registry for static API asset configurations.
     * @param processProvider The provider for managing service process lifecycles.
     * @param metricsProvider The provider for fetching process performance metrics.
     */
    public ServerRegistry(AssetsRegistry assetsRegistry, ProcessProvider processProvider,
            MetricsProvider metricsProvider) {
        this.assetsRegistry = assetsRegistry;
        this.processProvider = processProvider;
        this.metricsProvider = metricsProvider;
    }

    /**
     * Retrieves a list of all configured servers, enriched with their current runtime status and performance metrics.
     *
     * @return A {@code Flux} emitting a {@link Transmit} for each configured server.
     */
    public Flux<Transmit> getAllServerStatus() {
        // 1. Wrap the potentially blocking I/O call in fromCallable
        return Mono.fromCallable(() -> assetsRegistry.getAll())
                // 2. Offload the blocking call from the event loop
                .subscribeOn(Schedulers.boundedElastic())
                // 3. Convert the resulting List into a Flux
                .flatMapMany(Flux::fromIterable)
                // 4. Process each item with the already-asynchronous method
                .flatMap(this::buildServerStatusView);
    }

    /**
     * Builds a comprehensive status view for a single service by combining its static configuration with its dynamic
     * runtime data.
     *
     * @param assets The static configuration for the service.
     * @return A {@code Mono} emitting the consolidated {@link Transmit}.
     */
    private Mono<Transmit> buildServerStatusView(Assets assets) {
        // This method is already perfectly asynchronous. No changes needed.
        Mono<EnumValue.Lifecycle> statusMono = processProvider.getStatus(assets)
                .defaultIfEmpty(EnumValue.Lifecycle.UNKNOWN);
        Mono<Metrics> metricsMono = metricsProvider.getMetrics(assets.getId())
                .defaultIfEmpty(Metrics.builder().cpu(0).memory(0).build());

        return Mono.zip(statusMono, metricsMono, (status, metrics) -> new Transmit(assets.getName(), metrics, status));
    }

}
