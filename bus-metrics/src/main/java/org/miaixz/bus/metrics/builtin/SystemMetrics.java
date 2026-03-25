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

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import org.miaixz.bus.metrics.Metrics;

/**
 * Registers OS-level system metrics: CPU load, process uptime.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SystemMetrics {

    /**
     * Register OS-level gauges: system CPU load average, available processors, and process uptime. Safe to call
     * multiple times; subsequent calls are no-ops due to provider-level deduplication.
     */
    public static void register() {
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        Metrics.gauge("system.cpu.load", os, o -> o.getSystemLoadAverage() / o.getAvailableProcessors());
        Metrics.gauge("system.available.processors", os, o -> (double) o.getAvailableProcessors());

        RuntimeMXBeanHolder rt = new RuntimeMXBeanHolder();
        Metrics.gauge(
                "process.uptime.seconds",
                rt,
                h -> (double) ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0);
    }

    // Holder to allow weak reference in gauge
    private static final class RuntimeMXBeanHolder {
    }

}
