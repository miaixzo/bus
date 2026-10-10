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
package org.miaixz.bus.starter.metrics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.nimble.prometheus.PrometheusExporter;

/**
 * Exposes the Prometheus-format metrics scrape endpoint at {@code /metricz}.
 * <p>
 * Intentionally NOT annotated with {@code @Controller} or {@code @Component}: this class must never be picked up by
 * component scanning. It is registered by the endpoint section of {@link MetricsConfiguration} when
 * {@code @EnableMetrics} is active and {@code bus.metrics.endpoint.enabled=true}.
 * <p>
 * Spring MVC detects this class as a handler because {@code RequestMappingHandlerMapping.isHandler()} checks for a
 * class-level {@code @RequestMapping} in addition to {@code @Controller}.
 *
 * @author Kimi Liu
 */
@RequestMapping
public class MetricsEndpoint {

    /**
     * Bound metrics endpoint configuration properties.
     */
    private final MetricsProperties properties;

    /**
     * Provider used to collect application metrics for the endpoint.
     */
    private final PrometheusExporter exporter;

    /**
     * Creates a metrics endpoint.
     *
     * @param properties metrics properties
     * @param provider   provider owned by the current application context
     */
    public MetricsEndpoint(MetricsProperties properties, Provider provider) {
        this.properties = properties;
        this.exporter = new PrometheusExporter(provider);
    }

    /**
     * Scrapes metrics in Prometheus text format.
     *
     * @return Prometheus text response
     */
    @ResponseBody
    @GetMapping(path = "${bus.metrics.endpoint.path:/metricz}", produces = PrometheusExporter.CONTENT_TYPE)
    public String scrape() {
        Logger.debug(true, "Starter", "Metrics scrape requested: endpoint={}", this.properties.getEndpoint().path());
        return this.exporter.scrape();
    }

}
