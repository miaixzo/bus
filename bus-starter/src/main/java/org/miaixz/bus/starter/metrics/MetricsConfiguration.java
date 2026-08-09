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

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.bridge.HealthMetrics;
import org.miaixz.bus.metrics.builtin.CacheMetricsAdapter;
import org.miaixz.bus.metrics.builtin.JvmMetrics;
import org.miaixz.bus.metrics.builtin.SystemMetrics;
import org.miaixz.bus.metrics.guard.CardinalityGuard;
import org.miaixz.bus.metrics.guard.CardinalityPolicy;
import org.miaixz.bus.metrics.nimble.indigenous.NativeProvider;
import org.miaixz.bus.metrics.nimble.micrometer.MicrometerProvider;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.spring.boot.startup.SpringStartupPublisher;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableMetrics;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Configures bus-metrics providers, guards, collectors, and management endpoint. It is imported through
 * {@link org.miaixz.bus.starter.annotation.EnableMetrics}.
 * <p>
 * When {@code bus-health} is on the classpath, {@link HealthMetrics} is used for system/JVM metrics (JNA-backed,
 * hardware-accurate). Otherwise falls back to {@link JvmMetrics} and {@link SystemMetrics} (JVM MXBean-backed).
 *
 * @author Kimi Liu
 */
@EnableConfigurationProperties(MetricsProperties.class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnEnabled(annotation = EnableMetrics.class, prefix = GeniusBuilder.METRICS)
public class MetricsConfiguration {

    /**
     * Bound metrics configuration properties.
     */
    private final MetricsProperties properties;

    /**
     * Stores the metrics policy used by provider, guard, collector, and endpoint Bean factories.
     *
     * @param properties bound configuration properties
     */
    public MetricsConfiguration(MetricsProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the native metrics provider.
     *
     * @return native metrics provider
     */
    @Bean
    @ConditionalOnMissingBean({ Provider.class, MeterRegistry.class })
    public Provider metricsProvider() {
        applyCardinalityGuard(this.properties.getCardinality());
        Provider provider = new NativeProvider();
        registerBuiltinMetrics(this.properties);
        return provider;
    }

    /**
     * Creates the Micrometer-backed metrics provider.
     *
     * @param registry Micrometer meter registry
     * @return Micrometer-backed metrics provider
     */
    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    @ConditionalOnMissingBean(Provider.class)
    @ConditionalOnClass(name = "io.micrometer.core.instrument.MeterRegistry")
    public Provider micrometerProvider(io.micrometer.core.instrument.MeterRegistry registry) {
        applyCardinalityGuard(this.properties.getCardinality());
        Provider provider = new MicrometerProvider(registry);
        registerBuiltinMetrics(this.properties);
        return provider;
    }

    /**
     * Bridges completed Spring Boot startup summaries into the configured metrics provider.
     *
     * @param provider metrics provider
     * @return startup metrics publisher
     */
    @Bean
    @ConditionalOnBean(Provider.class)
    @ConditionalOnMissingBean(SpringStartupPublisher.class)
    public SpringStartupPublisher startupMetricsPublisher(Provider provider) {
        return new StartupMetricsPublisher(provider);
    }

    /**
     * Exposes a {@link CacheMetricsAdapter} bean that bridges bus-cache hit/miss statistics into the bus-metrics
     * observability backend (Prometheus, Micrometer, OTel).
     * <p>
     * Inject this bean into the bus-cache {@code Context} via {@code Context.newBuilder().hitting(adapter)} to activate
     * automatic hit-rate tracking for all {@code @Cached} methods. Skipped when the application provides its own
     * {@link org.miaixz.bus.cache.Collector} bean.
     *
     * @return cache metrics adapter
     */
    @Bean
    @ConditionalOnClass(name = "org.miaixz.bus.cache.Collector")
    @ConditionalOnMissingBean(org.miaixz.bus.cache.Collector.class)
    public CacheMetricsAdapter cacheMetricsAdapter() {
        return new CacheMetricsAdapter();
    }

    /**
     * Creates the metrics scrape endpoint.
     *
     * @param provider provider instance
     * @return metrics endpoint
     */
    @Bean
    @ConditionalOnBean(Provider.class)
    @ConditionalOnMissingBean(MetricsEndpoint.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = GeniusBuilder.METRICS
            + ".endpoint", name = "enabled", havingValue = "true", matchIfMissing = false)
    public MetricsEndpoint metricsEndpoint(Provider provider) {
        return new MetricsEndpoint(this.properties, provider);
    }

    /**
     * Registers system/JVM builtin metrics. Prefers bus-health (JNA-backed, more accurate) when available on the
     * classpath. Falls back to MXBean-based metrics when bus-health is absent.
     *
     * @param props meter registry properties
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

    /**
     * Tests whether the optional health subsystem is present and enabled for metric collection.
     *
     * @return whether health available
     */
    private static boolean isHealthAvailable() {
        try {
            Class.forName("org.miaixz.bus.health.Platform");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Applies the cardinality guard.
     *
     * @param cardinality metric cardinality settings
     */
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
