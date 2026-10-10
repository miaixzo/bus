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
package org.miaixz.bus.metrics.nimble.prometheus;

import java.util.Objects;

import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.nimble.ScrapeSupport;

/**
 * Exposes the scrape capability of a metrics provider using Prometheus text format 0.0.4.
 *
 * @author Kimi Liu
 */
public class PrometheusExporter {

    /**
     * Content type returned by this exporter.
     */
    public static final String CONTENT_TYPE = ScrapeSupport.PROMETHEUS_TEXT_CONTENT_TYPE;

    /**
     * Provider capability used to produce Prometheus text.
     */
    private final ScrapeSupport scrapeSupport;

    /**
     * Creates an exporter backed by the selected provider.
     *
     * @param provider the provider selected by the application
     * @throws IllegalStateException if the provider has no text scrape capability
     */
    public PrometheusExporter(Provider provider) {
        Provider resolved = Objects.requireNonNull(provider, "Metrics provider must not be null");
        this.scrapeSupport = resolved.capabilities().scrape().orElseThrow(
                () -> new IllegalStateException("Metrics provider " + resolved.getClass().getName()
                        + " does not support Prometheus text scraping"));
        if (!CONTENT_TYPE.equals(scrapeSupport.contentType())) {
            throw new IllegalStateException("Unsupported metrics scrape content type: " + scrapeSupport.contentType());
        }
    }

    /**
     * Returns the response content type.
     *
     * @return Prometheus text 0.0.4 content type
     */
    public String contentType() {
        return CONTENT_TYPE;
    }

    /**
     * Produces a complete scrape from the provider's current state.
     *
     * @return text exposition
     */
    public String scrape() {
        return scrapeSupport.scrape();
    }

}
