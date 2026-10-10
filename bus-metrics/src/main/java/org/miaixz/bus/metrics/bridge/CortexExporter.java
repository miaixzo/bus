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
package org.miaixz.bus.metrics.bridge;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.Provider;
import org.miaixz.bus.metrics.magic.*;
import org.miaixz.bus.metrics.observe.tag.Attributes;

/**
 * Periodically writes provider-neutral structured snapshots to a CacheX-backed Cortex transport.
 *
 * @author Kimi Liu
 */
public class CortexExporter implements AutoCloseable {

    /**
     * Provider supplying structured snapshots.
     */
    private final Provider provider;
    /**
     * Cache-backed transport receiving serialized families.
     */
    private final CacheX<String, String> store;
    /**
     * Logical Cortex namespace.
     */
    private final String space;
    /**
     * Service identifier included in every storage key.
     */
    private final String serviceId;
    /**
     * Period between snapshot exports.
     */
    private final long intervalMillis;
    /**
     * Single daemon scheduler owned by this exporter.
     */
    private final ScheduledExecutorService scheduler;
    /**
     * One-shot start state.
     */
    private final AtomicBoolean started = new AtomicBoolean();
    /**
     * Idempotent close state.
     */
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * Scheduled export task after startup.
     */
    private volatile ScheduledFuture<?> future;

    /**
     * Creates an exporter tied to one provider and one scheduler lifecycle.
     *
     * @param provider  provider supplying structured snapshots
     * @param store     cache-backed destination
     * @param space     logical namespace
     * @param serviceId service identifier
     * @param interval  export interval
     * @throws IllegalArgumentException if an identity value is blank or the interval is shorter than one millisecond
     * @throws IllegalStateException    if the provider does not support structured snapshots
     */
    public CortexExporter(Provider provider, CacheX<String, String> store, String space, String serviceId,
            Duration interval) {
        this.provider = Objects.requireNonNull(provider, "Metrics provider must not be null");
        this.store = Objects.requireNonNull(store, "Cortex store must not be null");
        this.space = nonBlank(space, "Cortex space");
        this.serviceId = nonBlank(serviceId, "Cortex service identifier");
        Duration checkedInterval = Objects.requireNonNull(interval, "Cortex interval must not be null");
        this.intervalMillis = saturatedMillis(checkedInterval);
        if (checkedInterval.isZero() || checkedInterval.isNegative() || intervalMillis == 0) {
            throw new IllegalArgumentException("Cortex interval must be at least one millisecond");
        }
        provider.capabilities().snapshot().orElseThrow(
                () -> new IllegalStateException("Metrics provider " + provider.getClass().getName()
                        + " does not support structured snapshots"));
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, Builder.THREAD_NAME_CORTEX);
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Appends one typed metric point to a JSON buffer.
     *
     * @param json  destination buffer
     * @param point metric point
     */
    private static void point(StringBuilder json, MetricPoint point) {
        json.append('{').append("\"attributes\":{");
        int attributeIndex = 0;
        for (Attributes.Value<?> attribute : point.attributes().values()) {
            if (attributeIndex++ > 0)
                json.append(',');
            json.append('\"').append(escape(attribute.descriptor().key())).append("\":");
            Object value = attribute.value();
            if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else {
                json.append('\"').append(escape(String.valueOf(value))).append('\"');
            }
        }
        json.append('}');
        if (point instanceof LongMetricPoint value) {
            json.append(",\"value\":").append(value.value());
        } else if (point instanceof DoubleMetricPoint value) {
            json.append(",\"value\":").append(value.value());
        } else if (point instanceof DistributionMetricPoint value) {
            json.append(",\"count\":").append(value.count()).append(",\"sum\":").append(value.sum()).append(",\"max\":")
                    .append(value.max()).append(",\"buckets\":[");
            for (int i = 0; i < value.buckets().size(); i++) {
                if (i > 0)
                    json.append(',');
                json.append('{').append("\"le\":").append(value.buckets().get(i).upperBound()).append(",\"count\":")
                        .append(value.buckets().get(i).cumulativeCount()).append('}');
            }
            json.append(']');
        }
        json.append('}');
    }

    /**
     * Escapes one JSON string value.
     *
     * @param value source value
     * @return escaped value without surrounding quotes
     */
    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20)
                        escaped.append(String.format("\\u%04x", (int) character));
                    else
                        escaped.append(character);
                }
            }
        }
        return escaped.toString();
    }

    /**
     * Requires a non-blank exporter identity value.
     *
     * @param value source value
     * @param label diagnostic label
     * @return validated value
     */
    private static String nonBlank(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return value;
    }

    /**
     * Multiplies two non-negative values without overflowing.
     *
     * @param value      source value
     * @param multiplier multiplier
     * @return product, or {@link Long#MAX_VALUE} when it would overflow
     */
    private static long saturatedMultiply(long value, long multiplier) {
        return value > Long.MAX_VALUE / multiplier ? Long.MAX_VALUE : value * multiplier;
    }

    /**
     * Converts a positive duration to milliseconds without propagating arithmetic overflow.
     *
     * @param duration source duration
     * @return millisecond value, or {@link Long#MAX_VALUE} when conversion would overflow
     */
    private static long saturatedMillis(Duration duration) {
        try {
            return duration.toMillis();
        } catch (ArithmeticException exception) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Serializes one complete versioned metric snapshot.
     *
     * @param snapshot complete provider snapshot
     * @return compact JSON value
     */
    private String serialize(MetricSnapshot snapshot) {
        StringBuilder json = new StringBuilder(256);
        json.append('{').append("\"schemaVersion\":1,\"collectedAtEpochMillis\":")
                .append(snapshot.collectedAtEpochMillis()).append(",\"space\":\"").append(escape(space))
                .append("\",\"serviceId\":\"").append(escape(serviceId)).append("\",\"families\":[");
        for (int familyIndex = 0; familyIndex < snapshot.families().size(); familyIndex++) {
            if (familyIndex > 0) {
                json.append(',');
            }
            MetricFamilySnapshot family = snapshot.families().get(familyIndex);
            json.append('{').append("\"name\":\"").append(escape(family.descriptor().name())).append("\",\"kind\":\"")
                    .append(family.descriptor().kind()).append("\",\"unit\":\"")
                    .append(escape(family.descriptor().unit())).append("\",\"points\":[");
            for (int pointIndex = 0; pointIndex < family.points().size(); pointIndex++) {
                if (pointIndex > 0) {
                    json.append(',');
                }
                point(json, family.points().get(pointIndex));
            }
            json.append("]}");
        }
        return json.append("]}").toString();
    }

    /**
     * Starts periodic export exactly once.
     *
     * @throws IllegalStateException if this exporter is closed or was already started
     */
    public void start() {
        if (closed.get()) {
            throw new IllegalStateException("Cortex exporter is closed");
        }
        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException("Cortex exporter is already started");
        }
        future = scheduler.scheduleAtFixedRate(this::push, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the exporter and its owned scheduler.
     */
    public void stop() {
        close();
    }

    /**
     * Performs one complete snapshot push. Collection failures do not terminate the scheduler.
     */
    public void push() {
        try {
            MetricSnapshot snapshot = provider.capabilities().snapshot().orElseThrow().snapshot();
            long ttlMillis = saturatedMultiply(intervalMillis, Builder.CORTEX_TTL_MULTIPLIER);
            String key = Builder.CORTEX_KEY_PREFIX + space + Symbol.COLON + serviceId + Symbol.COLON + "snapshot";
            store.write(key, serialize(snapshot), ttlMillis);
        } catch (RuntimeException exception) {
            Logger.warn(
                    false,
                    "Metrics",
                    exception,
                    "Cortex metrics export failed: exception={}",
                    exception.getClass().getSimpleName());
        }
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        ScheduledFuture<?> scheduled = future;
        if (scheduled != null) {
            scheduled.cancel(false);
        }
        scheduler.shutdown();
    }

}
