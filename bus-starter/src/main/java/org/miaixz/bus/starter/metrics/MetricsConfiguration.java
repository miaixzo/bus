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
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.builtin.CacheMetricsAdapter;
import org.miaixz.bus.metrics.builtin.JvmMetrics;
import org.miaixz.bus.metrics.builtin.SystemMetrics;
import org.miaixz.bus.metrics.guard.CardinalityGuard;
import org.miaixz.bus.metrics.guard.CardinalityPolicy;
import org.miaixz.bus.metrics.bridge.HealthMetrics;
import org.miaixz.bus.metrics.metric.micrometer.MicrometerProvider;
import org.miaixz.bus.metrics.metric.indigenous.NativeProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for bus-metrics. Imported via {@link org.miaixz.bus.starter.annotation.EnableMetrics}
 * through {@code @Import}.
 * <p>
 * When {@code bus-health} is on the classpath, {@link HealthMetrics} is used for system/JVM metrics (JNA-backed,
 * hardware-accurate). Otherwise falls back to {@link JvmMetrics} and {@link SystemMetrics} (JVM MXBean-backed).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(MetricsProperties.class)
public class MetricsConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "bus.metrics", name = "provider", havingValue = "native", matchIfMissing = true)
    public Provider metricsProvider(MetricsProperties props) {
        applyCardinalityGuard(props.getCardinality());
        Provider provider = new NativeProvider();
        Metrics.setProvider(provider);
        registerBuiltinMetrics(props);
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean(Provider.class)
    @ConditionalOnClass(name = "io.micrometer.core.instrument.MeterRegistry")
    @ConditionalOnProperty(prefix = "bus.metrics", name = "provider", havingValue = "micrometer")
    public Provider micrometerProvider(MetricsProperties props, io.micrometer.core.instrument.MeterRegistry registry) {
        applyCardinalityGuard(props.getCardinality());
        Provider provider = new MicrometerProvider(registry);
        Metrics.setProvider(provider);
        registerBuiltinMetrics(props);
        return provider;
    }

    /**
     * Exposes a {@link CacheMetricsAdapter} bean that bridges bus-cache hit/miss statistics into the bus-metrics
     * observability backend (Prometheus, Micrometer, OTel).
     * <p>
     * Inject this bean into the bus-cache {@code Context} via {@code Context.newBuilder().hitting(adapter)} to activate
     * automatic hit-rate tracking for all {@code @Cached} methods. Skipped when the application provides its own
     * {@link org.miaixz.bus.cache.Collector} bean.
     */
    @Bean
    @ConditionalOnClass(name = "org.miaixz.bus.cache.Collector")
    @ConditionalOnMissingBean(org.miaixz.bus.cache.Collector.class)
    public CacheMetricsAdapter cacheMetricsAdapter() {
        return new CacheMetricsAdapter();
    }

    @Bean
    @ConditionalOnProperty(prefix = "bus.metrics", name = "endpoint", havingValue = "true", matchIfMissing = true)
    public MetricsEndpoint metricsEndpoint(MetricsProperties props) {
        return new MetricsEndpoint(props);
    }

    /**
     * Registers system/JVM builtin metrics. Prefers bus-health (JNA-backed, more accurate) when available on the
     * classpath. Falls back to MXBean-based metrics when bus-health is absent.
     */
    private void registerBuiltinMetrics(MetricsProperties props) {
        boolean healthOnClasspath = isHealthAvailable();

        if (props.isHealth() && healthOnClasspath && (props.isJvm() || props.isSystem())) {
            HealthMetrics healthMetrics = new HealthMetrics();
            healthMetrics.register();
        } else {
            if (props.isJvm()) {
                JvmMetrics.register();
            }
            if (props.isSystem()) {
                SystemMetrics.register();
            }
        }
    }

    private static boolean isHealthAvailable() {
        try {
            Class.forName("org.miaixz.bus.health.Platform");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void applyCardinalityGuard(MetricsProperties.Cardinality cardinality) {
        CardinalityGuard.setDefaultMax(cardinality.getDefaultMax());
        for (String key : cardinality.getDenyList()) {
            CardinalityGuard.policy(key, CardinalityPolicy.deny());
        }
        for (MetricsProperties.CardinalityRule rule : cardinality.getRules()) {
            CardinalityPolicy policy = switch (rule.getPolicy()) {
                case "top-n" -> CardinalityPolicy.topN(rule.getMax());
                case "deny" -> CardinalityPolicy.deny();
                default -> CardinalityPolicy.firstN(rule.getMax());
            };
            CardinalityGuard.policy(rule.getTag(), policy);
        }
    }

}
