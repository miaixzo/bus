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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ClassUtils;

import org.miaixz.bus.cache.Collector;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.metrics.Metrics;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.builtin.CacheMetricsAdapter;
import org.miaixz.bus.metrics.guard.CardinalityGuard;
import org.miaixz.bus.metrics.guard.CardinalityPolicy;
import org.miaixz.bus.metrics.guard.ProviderLease;
import org.miaixz.bus.metrics.nimble.ProviderSelection;
import org.miaixz.bus.metrics.nimble.ProviderSelection.Candidate;
import org.miaixz.bus.metrics.nimble.ProviderSelection.Type;
import org.miaixz.bus.metrics.nimble.indigenous.NativeProvider;
import org.miaixz.bus.metrics.nimble.micrometer.MicrometerProvider;
import org.miaixz.bus.metrics.nimble.opentelemetry.OpenTelemetryProvider;
import org.miaixz.bus.metrics.nimble.prometheus.PrometheusProvider;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.spring.boot.startup.SpringStartupPublisher;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableMetrics;

/**
 * Single Spring configuration source for provider selection, binders, endpoint and exporters.
 *
 * @author Kimi Liu
 */
@EnableConfigurationProperties(MetricsProperties.class)
@Configuration(proxyBeanMethods = false)
@ConditionalOnEnabled(annotation = EnableMetrics.class, prefix = GeniusBuilder.METRICS)
@Import({ MetricsConfiguration.NativeBackendConfiguration.class,
        MetricsConfiguration.MicrometerBackendConfiguration.class,
        MetricsConfiguration.OpenTelemetryBackendConfiguration.class,
        MetricsConfiguration.PrometheusBackendConfiguration.class, MetricsConfiguration.JvmBinderConfiguration.class,
        MetricsConfiguration.HostBinderConfiguration.class, MetricsConfiguration.EndpointConfiguration.class,
        MetricsConfiguration.CortexConfiguration.class })
public class MetricsConfiguration {

    /**
     * Micrometer registry type resolved only after its classpath condition has matched.
     */
    private static final String MICROMETER_REGISTRY_TYPE = "io.micrometer.core.instrument.MeterRegistry";

    /**
     * OpenTelemetry API type resolved only after its classpath condition has matched.
     */
    private static final String OPEN_TELEMETRY_TYPE = "io.opentelemetry.api.OpenTelemetry";

    /**
     * Prometheus registry type resolved only after its classpath condition has matched.
     */
    private static final String PROMETHEUS_REGISTRY_TYPE = "io.prometheus.metrics.model.registry.PrometheusRegistry";

    /**
     * Immutable metrics settings bound for this application context.
     */
    private final MetricsProperties properties;

    /**
     * Creates the root configuration with Bus-owned property types only.
     *
     * @param properties immutable metrics settings
     */
    public MetricsConfiguration(MetricsProperties properties) {
        this.properties = properties;
        Type.parse(properties.getProvider());
    }

    /**
     * Finds beans for an optional integration type without exposing that type in an auto-configuration signature. Bean
     * initialization is deliberately disabled so an unselected backend remains lazy.
     *
     * @param applicationContext current application context
     * @param typeName           optional integration type name
     * @return matching bean names without creating the beans
     */
    private static String[] beanNamesForType(ApplicationContext applicationContext, String typeName) {
        Class<?> type = ClassUtils.resolveClassName(typeName, applicationContext.getClassLoader());
        return applicationContext.getBeanNamesForType(type, true, false);
    }

    /**
     * Creates one isolated cardinality scope for this application context.
     *
     * @return context-local cardinality scope
     */
    @Bean
    @ConditionalOnMissingBean(CardinalityGuard.Scope.class)
    public CardinalityGuard.Scope metricsCardinalityScope() {
        MetricsProperties.Cardinality settings = properties.getCardinality();
        CardinalityGuard.Scope scope = new CardinalityGuard.Scope(settings.defaultMax());
        for (String key : settings.denyList()) {
            scope.policy(key, CardinalityPolicy.deny());
        }
        for (MetricsProperties.CardinalityRule rule : settings.rules()) {
            CardinalityPolicy policy = switch (rule.policy()) {
                case "top-n" -> CardinalityPolicy.topN(rule.max());
                case "deny" -> CardinalityPolicy.deny();
                default -> CardinalityPolicy.firstN(rule.max());
            };
            scope.policy(rule.tag(), policy);
        }
        return scope;
    }

    /**
     * Evaluates host dependencies immediately when metrics configuration is created.
     *
     * @return host integration availability
     */
    @Bean
    public HostMetricsAvailability hostMetricsAvailability() {
        HostMetricsAvailability availability = HostMetricsAvailability.evaluate(
                properties.getHost().enabled(),
                properties.getHost().required(),
                MetricsConfiguration.class.getClassLoader());
        if (availability.status() == HostMetricsAvailability.Status.FAILED) {
            Logger.warn(false, "Metrics", availability.reason());
        }
        return availability;
    }

    /**
     * Selects one lazy backend when the application did not supply a provider bean.
     *
     * @param candidates available and unavailable backend candidates
     * @return selected provider
     */
    @Bean
    @ConditionalOnMissingBean(Provider.class)
    public Provider metricsProvider(List<Candidate> candidates) {
        return ProviderSelection.select(Type.parse(properties.getProvider()), candidates);
    }

    /**
     * Installs the context's unique provider into the static compatibility facade with reversible ownership.
     *
     * @param providers provider beans visible to the application context
     * @return reversible runtime lease
     * @throws IllegalStateException if the context does not contain exactly one provider
     */
    @Bean(destroyMethod = "close")
    public ProviderLease metricsRuntimeLease(ObjectProvider<Provider> providers) {
        List<Provider> instances = providers.orderedStream().toList();
        if (instances.size() != 1) {
            throw new IllegalStateException("Exactly one metrics Provider bean is required; found " + instances.size());
        }
        return Metrics.installProvider(instances.get(0), this);
    }

    /**
     * Publishes completed Spring startup summaries through the leased provider.
     *
     * @param lease active provider lease
     * @return startup metrics publisher
     */
    @Bean
    @ConditionalOnMissingBean(SpringStartupPublisher.class)
    public SpringStartupPublisher startupMetricsPublisher(ProviderLease lease) {
        return new StartupMetricsPublisher(lease.provider());
    }

    /**
     * Supplies the existing bus-cache adapter without introducing a second metrics registry.
     *
     * @return cache metrics adapter
     */
    @Bean
    @ConditionalOnClass(name = "org.miaixz.bus.cache.Collector")
    @ConditionalOnMissingBean(Collector.class)
    public CacheMetricsAdapter cacheMetricsAdapter() {
        return new CacheMetricsAdapter();
    }

    /**
     * Dependency-only host metric availability result used during auto-configuration.
     *
     * @param status         host integration status
     * @param missingClasses required classes that could not be linked
     * @param reason         stable diagnostic explanation
     */
    static record HostMetricsAvailability(Status status, List<String> missingClasses, String reason) {

        /**
         * Defensively copies the result.
         */
        HostMetricsAvailability {
            missingClasses = List.copyOf(missingClasses == null ? List.of() : missingClasses);
            reason = reason == null ? "" : reason;
        }

        /**
         * Evaluates host dependencies without initializing them.
         *
         * @param enabled  whether host metric collection is enabled
         * @param required whether missing dependencies must fail startup
         * @param loader   application class loader used for linkage checks
         * @return host integration availability
         */
        static HostMetricsAvailability evaluate(boolean enabled, boolean required, ClassLoader loader) {
            if (!enabled) {
                return new HostMetricsAvailability(Status.DISABLED, List.of(), "Host metrics are disabled");
            }
            List<String> missing = new ArrayList<>();
            check(loader, "org.miaixz.bus.health.Collector", missing);
            check(loader, "com.sun.jna.Native", missing);
            check(loader, "com.sun.jna.platform.FileUtils", missing);
            if (missing.isEmpty()) {
                return new HostMetricsAvailability(Status.AVAILABLE, List.of(), "");
            }
            String explanation = "Host metrics require bus-health and JNA; missing " + String.join(", ", missing)
                    + ". Add bus-health with its JNA dependencies or disable bus.metrics.host.enabled";
            if (required) {
                throw new IllegalStateException(explanation);
            }
            return new HostMetricsAvailability(Status.FAILED, missing, explanation);
        }

        /**
         * Adds a class name to the missing list when it cannot be linked.
         *
         * @param loader    application class loader
         * @param className required class name
         * @param missing   destination for missing class names
         */
        private static void check(ClassLoader loader, String className, List<String> missing) {
            if (!ClassUtils.isPresent(className, loader)) {
                missing.add(className);
            }
        }

        /**
         * Reports whether host binders may be created.
         *
         * @return whether host binders may be created
         */
        boolean available() {
            return status == Status.AVAILABLE;
        }

        /**
         * Host integration state.
         */
        enum Status {
            /**
             * Required classes are available.
             */
            AVAILABLE,
            /**
             * Host collection is effectively disabled.
             */
            DISABLED,
            /**
             * Host collection was requested but required classes are absent.
             */
            FAILED
        }
    }

    /**
     * Always contributes the lazy native candidate.
     */
    @Configuration(proxyBeanMethods = false)
    static class NativeBackendConfiguration {

        /**
         * Creates the native candidate without constructing the provider yet.
         */
        @Bean
        Candidate nativeMetricsProviderCandidate(CardinalityGuard.Scope scope) {
            return Candidate.available(Type.NATIVE, false, () -> new NativeProvider(scope));
        }
    }

    /**
     * Isolates Micrometer linkage in one nested configuration class.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = MICROMETER_REGISTRY_TYPE)
    static class MicrometerBackendConfiguration {

        /**
         * Describes Micrometer availability without constructing an adapter.
         */
        @Bean
        Candidate micrometerMetricsProviderCandidate(
                ApplicationContext applicationContext,
                CardinalityGuard.Scope scope) {
            String[] beanNames = beanNamesForType(applicationContext, MICROMETER_REGISTRY_TYPE);
            if (beanNames.length != 1) {
                return Candidate.unavailable(
                        Type.MICROMETER,
                        true,
                        "expected exactly one MeterRegistry bean but found " + beanNames.length);
            }
            return Candidate.available(
                    Type.MICROMETER,
                    true,
                    () -> MicrometerProvider.fromRegistry(applicationContext.getBean(beanNames[0]), scope));
        }
    }

    /**
     * Isolates OpenTelemetry API linkage and keeps SDK ownership external.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = OPEN_TELEMETRY_TYPE)
    static class OpenTelemetryBackendConfiguration {

        /**
         * Describes OpenTelemetry availability using the API interface only.
         */
        @Bean
        Candidate openTelemetryMetricsProviderCandidate(
                ApplicationContext applicationContext,
                CardinalityGuard.Scope scope) {
            String[] beanNames = beanNamesForType(applicationContext, OPEN_TELEMETRY_TYPE);
            if (beanNames.length != 1) {
                return Candidate.unavailable(
                        Type.OPENTELEMETRY,
                        true,
                        "expected exactly one OpenTelemetry bean but found " + beanNames.length);
            }
            return Candidate.available(
                    Type.OPENTELEMETRY,
                    true,
                    () -> OpenTelemetryProvider.fromOpenTelemetry(applicationContext.getBean(beanNames[0]), scope));
        }
    }

    /**
     * Isolates Prometheus core linkage and registry ownership.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = PROMETHEUS_REGISTRY_TYPE)
    static class PrometheusBackendConfiguration {

        /**
         * Describes a private, reused, or ambiguous Prometheus registry.
         */
        @Bean
        Candidate prometheusMetricsProviderCandidate(
                ApplicationContext applicationContext,
                MetricsProperties properties,
                CardinalityGuard.Scope scope) {
            String[] beanNames = beanNamesForType(applicationContext, PROMETHEUS_REGISTRY_TYPE);
            if (beanNames.length > 1) {
                return Candidate.unavailable(
                        Type.PROMETHEUS,
                        true,
                        "expected zero or one PrometheusRegistry bean but found " + beanNames.length);
            }
            if (beanNames.length == 1) {
                return Candidate.available(
                        Type.PROMETHEUS,
                        true,
                        () -> PrometheusProvider.fromRegistry(applicationContext.getBean(beanNames[0]), scope));
            }
            if (Type.parse(properties.getProvider()) == Type.PROMETHEUS) {
                return Candidate.available(Type.PROMETHEUS, true, () -> PrometheusProvider.withPrivateRegistry(scope));
            }
            return Candidate.unavailable(
                    Type.PROMETHEUS,
                    true,
                    "no PrometheusRegistry bean was provided; select bus.metrics.provider=prometheus explicitly "
                            + "to use a private registry");
        }
    }

    /**
     * Independently binds JVM metrics.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = GeniusBuilder.METRICS, name = "jvm", havingValue = "true", matchIfMissing = true)
    static class JvmBinderConfiguration {

        /**
         * Creates and binds one closeable JVM binder after the provider lease exists.
         */
        @Bean(destroyMethod = "close")
        org.miaixz.bus.metrics.builtin.JvmMetrics jvmMetrics(ProviderLease lease) {
            org.miaixz.bus.metrics.builtin.JvmMetrics binder = new org.miaixz.bus.metrics.builtin.JvmMetrics();
            binder.bind(lease.provider());
            return binder;
        }
    }

    /**
     * Isolates bus-health and JNA linkage from the root configuration.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = { "org.miaixz.bus.health.Collector", "com.sun.jna.Native",
            "com.sun.jna.platform.FileUtils" })
    static class HostBinderConfiguration {

        /**
         * Maps starter properties to the dependency-neutral host binder options.
         *
         * @param host structured host properties
         * @return host binder options
         */
        private static org.miaixz.bus.metrics.builtin.HostMetricsOptions hostOptions(MetricsProperties.Host host) {
            MetricsProperties.Categories categories = host.categories();
            return new org.miaixz.bus.metrics.builtin.HostMetricsOptions(host.required(), "OTEL_1_44", host.cacheTtl(),
                    host.process().stateCountsCacheTtl(),
                    new org.miaixz.bus.metrics.builtin.HostMetricsOptions.Categories(categories.general(),
                            categories.cpu(), categories.memory(), categories.paging(), categories.disk(),
                            categories.filesystem(), categories.network(), categories.process(),
                            categories.container()),
                    new org.miaixz.bus.metrics.builtin.HostMetricsOptions.Cpu(host.cpu().maxLogicalCpus()),
                    new org.miaixz.bus.metrics.builtin.HostMetricsOptions.Process(host.process().currentOnly(),
                            host.process().stateCounts()),
                    new org.miaixz.bus.metrics.builtin.HostMetricsOptions.Disk(host.disk().includeVirtual(),
                            host.disk().maxDevices()),
                    new org.miaixz.bus.metrics.builtin.HostMetricsOptions.FileSystem(host.filesystem().localOnly(),
                            host.filesystem().mountpointMode().toUpperCase(), host.filesystem().maxFilesystems()),
                    new org.miaixz.bus.metrics.builtin.HostMetricsOptions.Network(host.network().includeLoopback(),
                            host.network().includeVirtual(), host.network().connections(),
                            host.network().maxInterfaces()));
        }

        /**
         * Creates host metrics only after the dependency decision and provider lease are available.
         */
        @Bean(destroyMethod = "close")
        @ConditionalOnExpression("${bus.metrics.host.enabled:true}")
        org.miaixz.bus.metrics.bridge.HealthMetrics hostMetrics(
                MetricsProperties properties,
                HostMetricsAvailability availability,
                ProviderLease lease) {
            if (!availability.available()) {
                throw new IllegalStateException(availability.reason());
            }
            org.miaixz.bus.metrics.builtin.HostMetricsOptions options = hostOptions(properties.getHost());
            try {
                org.miaixz.bus.metrics.bridge.HealthMetrics binder = new org.miaixz.bus.metrics.bridge.HealthMetrics(
                        new org.miaixz.bus.health.Collector(), options);
                binder.bind(lease.provider());
                return binder;
            } catch (RuntimeException | LinkageError exception) {
                if (options.required()) {
                    throw new IllegalStateException("Required host metrics could not be initialized", exception);
                }
                Logger.warn(
                        false,
                        "Metrics",
                        exception,
                        "Optional host metrics disabled after Collector initialization failure: {}",
                        exception.getMessage());
                org.miaixz.bus.metrics.bridge.HealthMetrics disabled = org.miaixz.bus.metrics.bridge.HealthMetrics
                        .disabled();
                disabled.bind(lease.provider());
                return disabled;
            }
        }
    }

    /**
     * Independently exposes the servlet scrape endpoint.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class EndpointConfiguration {

        /**
         * Creates an endpoint only when explicitly enabled and scrape capability is present.
         */
        @Bean
        @ConditionalOnProperty(prefix = GeniusBuilder.METRICS + ".endpoint", name = "enabled", havingValue = "true")
        MetricsEndpoint metricsEndpoint(MetricsProperties properties, ProviderLease lease) {
            if (lease.provider().capabilities().scrape().isEmpty()) {
                throw new IllegalStateException("bus.metrics.endpoint.enabled requires a scrape-capable Provider");
            }
            return new MetricsEndpoint(properties, lease.provider());
        }
    }

    /**
     * Independently starts and closes the Cortex snapshot exporter.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "org.miaixz.bus.cache.CacheX")
    static class CortexConfiguration {

        /**
         * Creates Cortex export only when explicitly enabled and one cache store exists.
         */
        @Bean(destroyMethod = "close")
        @ConditionalOnProperty(prefix = GeniusBuilder.METRICS + ".cortex", name = "enabled", havingValue = "true")
        org.miaixz.bus.metrics.bridge.CortexExporter metricsCortexExporter(
                MetricsProperties properties,
                ProviderLease lease,
                ObjectProvider<org.miaixz.bus.cache.CacheX> stores) {
            List<org.miaixz.bus.cache.CacheX> instances = stores.orderedStream().toList();
            if (instances.size() != 1) {
                throw new IllegalStateException(
                        "Cortex metrics export requires exactly one CacheX bean; found " + instances.size());
            }
            MetricsProperties.Cortex settings = properties.getCortex();
            org.miaixz.bus.metrics.bridge.CortexExporter exporter = new org.miaixz.bus.metrics.bridge.CortexExporter(
                    lease.provider(), instances.get(0), settings.space(), settings.serviceId(), settings.interval());
            exporter.start();
            return exporter;
        }
    }

}
