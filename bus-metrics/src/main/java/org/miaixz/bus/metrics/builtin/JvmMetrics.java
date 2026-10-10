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
package org.miaixz.bus.metrics.builtin;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.metrics.Metrics;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.guard.RegistrationGroup;
import org.miaixz.bus.metrics.nimble.InstrumentKind;
import org.miaixz.bus.metrics.nimble.MetricBinder;
import org.miaixz.bus.metrics.nimble.MetricDescriptor;
import org.miaixz.bus.metrics.nimble.NumberKind;
import org.miaixz.bus.metrics.observe.tag.AttributeDescriptor;
import org.miaixz.bus.metrics.observe.tag.Attributes;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Registers standard JVM memory, thread and garbage-collector metrics.
 *
 * @author Kimi Liu
 */
public class JvmMetrics implements MetricBinder {

    /**
     * Garbage-collector name attribute used by GC metric families.
     */
    private static final AttributeDescriptor<String> GC = AttributeDescriptor.string("jvm.gc.name", true);

    /**
     * Registrations owned by this binder.
     */
    private RegistrationGroup registrations;
    /**
     * Whether the compatibility path registered non-closeable legacy gauges.
     */
    private boolean legacyBound;

    /**
     * Constructs a JVM binder.
     */
    public JvmMetrics() {
        // No initialization required.
    }

    /**
     * Registers against the global compatibility provider.
     */
    public static void register() {
        new JvmMetrics().bind(Metrics.getProvider());
    }

    /**
     * Registers the legacy gauge set for providers without observable support.
     *
     * @param provider selected provider
     */
    private static void bindLegacy(Provider provider) {
        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        provider.gauge("jvm.memory.used.heap", memory, value -> value.getHeapMemoryUsage().getUsed());
        provider.gauge("jvm.memory.max.heap", memory, value -> value.getHeapMemoryUsage().getMax());
        provider.gauge("jvm.memory.used.nonheap", memory, value -> value.getNonHeapMemoryUsage().getUsed());
        provider.gauge("jvm.memory.max.nonheap", memory, value -> value.getNonHeapMemoryUsage().getMax());
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        provider.gauge("jvm.threads.live", threads, value -> value.getThreadCount());
        provider.gauge("jvm.threads.peak", threads, value -> value.getPeakThreadCount());
        provider.gauge("jvm.threads.daemon", threads, value -> value.getDaemonThreadCount());
        for (GarbageCollectorMXBean collector : ManagementFactory.getGarbageCollectorMXBeans()) {
            String name = normalize(collector.getName());
            provider.gauge(
                    "jvm.gc.collection.count",
                    collector,
                    value -> value.getCollectionCount(),
                    Tag.of("gc", name));
            provider.gauge(
                    "jvm.gc.collection.time.ms",
                    collector,
                    value -> value.getCollectionTime(),
                    Tag.of("gc", name));
        }
    }

    /**
     * Registers one non-negative long supplier as an observable double gauge.
     *
     * @param group    registration transaction
     * @param provider selected provider
     * @param name     metric name
     * @param unit     metric unit
     * @param supplier MXBean value supplier
     */
    private static void observableDouble(
            RegistrationGroup group,
            Provider provider,
            String name,
            String unit,
            java.util.function.LongSupplier supplier) {
        group.add(
                provider.registerObservable(
                        descriptor(name, InstrumentKind.GAUGE, NumberKind.DOUBLE, unit, List.of()),
                        measurement -> {
                            long value = supplier.getAsLong();
                            if (value >= 0) {
                                measurement.recordDouble(value, Attributes.empty());
                            }
                        }));
    }

    /**
     * Normalizes an MXBean component name for use as an attribute value.
     *
     * @param name source name
     * @return normalized name
     */
    private static String normalize(String name) {
        return name.replace(Symbol.SPACE, Symbol.UNDERLINE).toLowerCase();
    }

    /**
     * Creates a JVM metric descriptor in the Bus instrumentation scope.
     *
     * @param name       metric name
     * @param kind       instrument kind
     * @param number     numeric kind
     * @param unit       metric unit
     * @param attributes ordered attribute schema
     * @return metric descriptor
     */
    private static MetricDescriptor descriptor(
            String name,
            InstrumentKind kind,
            NumberKind number,
            String unit,
            List<AttributeDescriptor<?>> attributes) {
        return MetricDescriptor.of(name, kind, number, unit, "", attributes);
    }

    @Override
    public synchronized void bind(Provider provider) {
        if (registrations != null || legacyBound) {
            throw new IllegalStateException("JVM metrics are already bound");
        }
        Logger.info(true, "Metrics", "JVM metrics registration started");
        if (!provider.capabilities().observable()) {
            bindLegacy(provider);
            legacyBound = true;
            return;
        }
        RegistrationGroup group = new RegistrationGroup();
        try {
            MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
            observableDouble(
                    group,
                    provider,
                    "jvm.memory.used.heap",
                    "By",
                    () -> memory.getHeapMemoryUsage().getUsed());
            observableDouble(group, provider, "jvm.memory.max.heap", "By", () -> memory.getHeapMemoryUsage().getMax());
            observableDouble(
                    group,
                    provider,
                    "jvm.memory.used.nonheap",
                    "By",
                    () -> memory.getNonHeapMemoryUsage().getUsed());
            observableDouble(
                    group,
                    provider,
                    "jvm.memory.max.nonheap",
                    "By",
                    () -> memory.getNonHeapMemoryUsage().getMax());

            ThreadMXBean threads = ManagementFactory.getThreadMXBean();
            observableDouble(group, provider, "jvm.threads.live", "{thread}", threads::getThreadCount);
            observableDouble(group, provider, "jvm.threads.peak", "{thread}", threads::getPeakThreadCount);
            observableDouble(group, provider, "jvm.threads.daemon", "{thread}", threads::getDaemonThreadCount);

            List<GarbageCollectorMXBean> collectors = ManagementFactory.getGarbageCollectorMXBeans();
            MetricDescriptor count = descriptor(
                    "jvm.gc.collection.count",
                    InstrumentKind.COUNTER,
                    NumberKind.LONG,
                    "{collection}",
                    List.of(GC));
            group.add(provider.registerObservable(count, measurement -> collectors.forEach(collector -> {
                long value = collector.getCollectionCount();
                if (value >= 0) {
                    measurement.recordLong(value, Attributes.of(GC, normalize(collector.getName())));
                }
            })));
            MetricDescriptor time = descriptor(
                    "jvm.gc.collection.time.ms",
                    InstrumentKind.COUNTER,
                    NumberKind.LONG,
                    "ms",
                    List.of(GC));
            group.add(provider.registerObservable(time, measurement -> collectors.forEach(collector -> {
                long value = collector.getCollectionTime();
                if (value >= 0) {
                    measurement.recordLong(value, Attributes.of(GC, normalize(collector.getName())));
                }
            })));
            group.commit();
            registrations = group;
            Logger.info(false, "Metrics", "JVM metrics registration finished: gcCollectorCount={}", collectors.size());
        } catch (RuntimeException exception) {
            group.close();
            throw exception;
        }
    }

    @Override
    public synchronized void close() {
        if (registrations != null) {
            registrations.close();
            registrations = null;
        }
    }

}
