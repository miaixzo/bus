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

import java.util.Objects;

import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.builtin.StartupMetrics;
import org.miaixz.bus.metrics.builtin.StartupStage;
import org.miaixz.bus.spring.boot.startup.SpringStartupPublisher;
import org.miaixz.bus.spring.boot.startup.SpringStartupSummary;

/**
 * Publishes Spring Boot startup summaries through the configured Bus metrics provider.
 *
 * @author Kimi Liu
 */
public class StartupMetricsPublisher implements SpringStartupPublisher {

    /**
     * Metrics provider receiving startup measurements.
     */
    private final Provider provider;

    /**
     * Creates a startup metrics publisher backed by one provider.
     *
     * @param provider metrics provider
     */
    public StartupMetricsPublisher(Provider provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    /**
     * Publishes total startup duration, startup count, and stable top-level stage durations.
     *
     * @param summary completed Spring Boot startup summary
     */
    @Override
    public void publish(SpringStartupSummary summary) {
        Objects.requireNonNull(summary, "summary");
        StartupMetrics.record(
                provider,
                summary.applicationBootElapsedTime(),
                summary.stageStats().stream().map(stage -> new StartupStage(stage.name(), stage.durationMillis()))
                        .toList());
    }

}
