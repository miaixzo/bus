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

import org.miaixz.bus.metrics.Metrics;

/**
 * Registers standard JVM metrics: memory, GC pause, thread counts.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JvmMetrics {

    /**
     * Register standard JVM gauges: heap/non-heap memory, thread counts, and GC collection stats. Safe to call multiple
     * times; subsequent calls are no-ops due to provider-level deduplication.
     */
    public static void register() {
        MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
        Metrics.gauge("jvm.memory.used.heap", mem, m -> (double) m.getHeapMemoryUsage().getUsed());
        Metrics.gauge("jvm.memory.max.heap", mem, m -> (double) m.getHeapMemoryUsage().getMax());
        Metrics.gauge("jvm.memory.used.nonheap", mem, m -> (double) m.getNonHeapMemoryUsage().getUsed());
        Metrics.gauge("jvm.memory.max.nonheap", mem, m -> (double) m.getNonHeapMemoryUsage().getMax());

        ThreadMXBean threads = ManagementFactory.getThreadMXBean();
        Metrics.gauge("jvm.threads.live", threads, t -> (double) t.getThreadCount());
        Metrics.gauge("jvm.threads.peak", threads, t -> (double) t.getPeakThreadCount());
        Metrics.gauge("jvm.threads.daemon", threads, t -> (double) t.getDaemonThreadCount());

        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gc : gcBeans) {
            String gcName = gc.getName().replace(" ", "_").toLowerCase();
            Metrics.gauge("jvm.gc.collection.count", gc, b -> (double) b.getCollectionCount(), "gc", gcName);
            Metrics.gauge("jvm.gc.collection.time.ms", gc, b -> (double) b.getCollectionTime(), "gc", gcName);
        }
    }

}
