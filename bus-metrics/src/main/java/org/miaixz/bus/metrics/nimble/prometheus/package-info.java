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
/**
 * Prometheus integration based only on {@code prometheus-metrics-core} and its model API.
 * <p>
 * {@link org.miaixz.bus.metrics.nimble.prometheus.PrometheusProvider} owns the collector families it creates, while the
 * registry supplied by an application remains application-owned. The package-local snapshot encoder supplies text
 * format 0.0.4 without requiring the optional exposition-formats artifact.
 * {@link org.miaixz.bus.metrics.nimble.prometheus.PrometheusExporter} is the provider-neutral HTTP-facing adapter: it
 * delegates to a selected provider's scrape capability and does not inspect provider implementations.
 *
 * @author Kimi Liu
 */
package org.miaixz.bus.metrics.nimble.prometheus;
