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

import org.miaixz.bus.metrics.Metrics;
import org.miaixz.bus.metrics.metric.prometheus.PrometheusExporter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Exposes the Prometheus-format metrics scrape endpoint at {@code /metricz}.
 * <p>
 * Intentionally NOT annotated with {@code @Controller} or {@code @Component}: this class must never be picked up by
 * component scanning. It is registered as a bean exclusively by {@link MetricsConfiguration#metricsEndpoint} when
 * {@code @EnableMetrics} is active and {@code bus.metrics.endpoint=true} (the default).
 * <p>
 * Spring MVC detects this class as a handler because {@code RequestMappingHandlerMapping.isHandler()} checks for a
 * class-level {@code @RequestMapping} in addition to {@code @Controller}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@RequestMapping
public class MetricsEndpoint {

    private final MetricsProperties properties;

    public MetricsEndpoint(MetricsProperties properties) {
        this.properties = properties;
    }

    @ResponseBody
    @GetMapping(path = "${bus.metrics.path:/metricz}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String scrape() {
        PrometheusExporter exporter = new PrometheusExporter(Metrics.getProvider());
        return exporter.scrape();
    }

}
