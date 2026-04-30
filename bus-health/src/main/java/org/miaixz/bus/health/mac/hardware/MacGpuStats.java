/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.mac.hardware;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.GpuStats;
import org.miaixz.bus.health.builtin.hardware.GpuTicks;
import org.miaixz.bus.health.mac.SmcKit;
import org.miaixz.bus.health.mac.driver.IOReportClient;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.CoreFoundation;
import com.sun.jna.platform.mac.CoreFoundation.CFMutableDictionaryRef;
import com.sun.jna.platform.mac.CoreFoundation.CFNumberRef;
import com.sun.jna.platform.mac.CoreFoundation.CFStringRef;
import com.sun.jna.platform.mac.IOKit.IOConnect;
import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;

/**
 * macOS {@link GpuStats} session.
 *
 * <p>
 * On Apple Silicon, GPU ticks, utilization, and power are sourced from an {@link IOReportClient} subscription.
 * Utilization falls back to IOAccelerator PerformanceStatistics when the IOReport subscription fails or returns -1.
 * Temperature is read from SMC first, then falls back to IOAccelerator {@code Temperature(C)}.
 *
 * <p>
 * On Intel Mac, utilization and VRAM used are sourced from IOAccelerator PerformanceStatistics.
 *
 * <p>
 * Clock speeds, fan speed, and shared memory are not available on any macOS path and always return -1.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class MacGpuStats implements GpuStats {

    /**
     * The CF constant.
     */
    private static final CoreFoundation CF = CoreFoundation.INSTANCE;

    /**
     * The PERF_STATS_KEY constant.
     */
    private static final String PERF_STATS_KEY = "PerformanceStatistics";
    /**
     * The GPU_CORE_UTIL_KEY constant.
     */
    private static final String GPU_CORE_UTIL_KEY = "GPU Core Utilization";
    /**
     * The DEVICE_UTIL_KEY constant.
     */
    private static final String DEVICE_UTIL_KEY = "Device Utilization %";
    /**
     * The VRAM_USED_KEY constant.
     */
    private static final String VRAM_USED_KEY = "vramUsedBytes";
    /**
     * The VRAM_USED_KEY_AS constant.
     */
    private static final String VRAM_USED_KEY_AS = "In use system memory";
    /**
     * The GPU_UTIL_DIVISOR constant.
     */
    private static final double GPU_UTIL_DIVISOR = 0xFFFFFFFFL;
    /**
     * The TRADEMARK_PATTERN constant.
     */
    private static final Pattern TRADEMARK_PATTERN = Pattern.compile("[®™]|\\([Rr]\\)|\\([Tt][Mm]\\)");

    /**
     * The isAppleSilicon value.
     */
    private final boolean isAppleSilicon;
    /**
     * The cardName value.
     */
    private final String cardName;

    // Non-null only on Apple Silicon
    /**
     * The ioReportClient value.
     */
    private final IOReportClient ioReportClient;

    /**
     * The closed value.
     */
    private boolean closed;

    /**
     * Creates a new MacGpuStats instance.
     *
     * @param isAppleSilicon the is apple silicon
     * @param cardName       the card name
     */
    MacGpuStats(boolean isAppleSilicon, String cardName) {
        this.isAppleSilicon = isAppleSilicon;
        this.cardName = cardName;
        this.ioReportClient = isAppleSilicon ? IOReportClient.create() : null;
        if (isAppleSilicon && ioReportClient == null) {
            Logger.warn(
                    false,
                    "Health",
                    "IOReport subscription failed for '{}'; GPU ticks and power will be unavailable."
                            + " Utilization will fall back to IOAccelerator PerformanceStatistics.",
                    cardName);
        }
    }

    /**
     * Closes this resource.
     */
    @Override
    public synchronized void close() {
        closed = true;
        if (ioReportClient != null) {
            ioReportClient.close();
        }
    }

    /**
     * Returns whether the closed condition is true.
     *
     * @return the is closed result
     */
    @Override
    public synchronized boolean isClosed() {
        return closed;
    }

    /**
     * Returns the gpu ticks.
     *
     * @return the get gpu ticks result
     */
    @Override
    public synchronized GpuTicks getGpuTicks() {
        checkOpen();
        if (isAppleSilicon && ioReportClient != null) {
            return ioReportClient.sampleGpuTicks();
        }
        return new GpuTicks(0L, 0L);
    }

    /**
     * Returns the gpu utilization.
     *
     * @return the get gpu utilization result
     */
    @Override
    public synchronized double getGpuUtilization() {
        checkOpen();
        if (isAppleSilicon && ioReportClient != null) {
            double util = ioReportClient.sampleGpuUtilization();
            if (util >= 0) {
                return util;
            }
        }
        CFMutableDictionaryRef perfStats = queryPerfStats();
        if (perfStats == null) {
            return -1d;
        }
        try {
            CFStringRef coreUtilKey = CFStringRef.createCFString(GPU_CORE_UTIL_KEY);
            Pointer result = perfStats.getValue(coreUtilKey);
            coreUtilKey.release();
            if (result != null) {
                return new CFNumberRef(result).longValue() / GPU_UTIL_DIVISOR * 100.0;
            }
            CFStringRef devUtilKey = CFStringRef.createCFString(DEVICE_UTIL_KEY);
            result = perfStats.getValue(devUtilKey);
            devUtilKey.release();
            if (result != null) {
                return new CFNumberRef(result).longValue();
            }
        } finally {
            perfStats.release();
        }
        return -1d;
    }

    /**
     * Returns the vram used.
     *
     * @return the get vram used result
     */
    @Override
    public synchronized long getVramUsed() {
        checkOpen();
        CFMutableDictionaryRef perfStats = queryPerfStats();
        if (perfStats == null) {
            return -1L;
        }
        try {
            String primaryKey = isAppleSilicon ? VRAM_USED_KEY_AS : VRAM_USED_KEY;
            String fallbackKey = isAppleSilicon ? VRAM_USED_KEY : VRAM_USED_KEY_AS;
            CFStringRef key = CFStringRef.createCFString(primaryKey);
            Pointer result = perfStats.getValue(key);
            key.release();
            if (result != null) {
                return new CFNumberRef(result).longValue();
            }
            CFStringRef fallback = CFStringRef.createCFString(fallbackKey);
            result = perfStats.getValue(fallback);
            fallback.release();
            if (result != null) {
                return new CFNumberRef(result).longValue();
            }
        } finally {
            perfStats.release();
        }
        return -1L;
    }

    /**
     * Returns the shared memory used.
     *
     * @return the get shared memory used result
     */
    @Override
    public synchronized long getSharedMemoryUsed() {
        checkOpen();
        return -1L;
    }

    /**
     * Returns the temperature.
     *
     * @return the get temperature result
     */
    @Override
    public synchronized double getTemperature() {
        checkOpen();
        if (isAppleSilicon) {
            IOConnect conn = SmcKit.smcOpen();
            if (conn != null) {
                try {
                    double temp = SmcKit.smcGetFirstFloat(conn, SmcKit.SMC_KEYS_GPU_TEMP_AS);
                    if (temp > 0) {
                        return temp;
                    }
                } finally {
                    SmcKit.smcClose(conn);
                }
            }
        }
        CFMutableDictionaryRef perfStats = queryPerfStats();
        if (perfStats == null) {
            return -1d;
        }
        try {
            CFStringRef tempKey = CFStringRef.createCFString("Temperature(C)");
            Pointer result = perfStats.getValue(tempKey);
            tempKey.release();
            if (result != null) {
                long val = new CFNumberRef(result).longValue();
                if (val > 0) {
                    return val;
                }
            }
        } finally {
            perfStats.release();
        }
        return -1d;
    }

    /**
     * Returns the power draw.
     *
     * @return the get power draw result
     */
    @Override
    public synchronized double getPowerDraw() {
        checkOpen();
        if (isAppleSilicon && ioReportClient != null) {
            return ioReportClient.samplePowerWatts();
        }
        return -1d;
    }

    /**
     * Returns the core clock mhz.
     *
     * @return the get core clock mhz result
     */
    @Override
    public synchronized long getCoreClockMhz() {
        checkOpen();
        return -1L;
    }

    /**
     * Returns the memory clock mhz.
     *
     * @return the get memory clock mhz result
     */
    @Override
    public synchronized long getMemoryClockMhz() {
        checkOpen();
        return -1L;
    }

    /**
     * Returns the fan speed percent.
     *
     * @return the get fan speed percent result
     */
    @Override
    public synchronized double getFanSpeedPercent() {
        checkOpen();
        return -1d;
    }

    /**
     * Handles the check open operation.
     */
    private void checkOpen() {
        if (closed) {
            throw new IllegalStateException(
                    "GpuStats session has been closed. Obtain a new session via GraphicsCard.createStatsSession().");
        }
    }

    /**
     * Queries the perf stats.
     *
     * @return the query perf stats result
     */
    private CFMutableDictionaryRef queryPerfStats() {
        IOIterator iter = IOKitUtil.getMatchingServices("IOAccelerator");
        if (iter == null) {
            return null;
        }
        CFStringRef perfStatsKey = CFStringRef.createCFString(PERF_STATS_KEY);
        CFStringRef modelKey = CFStringRef.createCFString("model");
        try {
            IORegistryEntry service = iter.next();
            while (service != null) {
                CFMutableDictionaryRef result = null;
                try {
                    CFMutableDictionaryRef props = service.createCFProperties();
                    if (props != null) {
                        try {
                            Pointer modelPtr = props.getValue(modelKey);
                            if (modelPtr != null && matchesName(new CFStringRef(modelPtr).stringValue())) {
                                Pointer statsPtr = props.getValue(perfStatsKey);
                                if (statsPtr != null) {
                                    CFMutableDictionaryRef stats = new CFMutableDictionaryRef();
                                    stats.setPointer(statsPtr);
                                    CF.CFRetain(stats);
                                    result = stats;
                                }
                            }
                        } finally {
                            props.release();
                        }
                    }
                } finally {
                    service.release();
                }
                if (result != null) {
                    return result;
                }
                service = iter.next();
            }
        } finally {
            iter.release();
            perfStatsKey.release();
            modelKey.release();
        }
        return null;
    }

    /**
     * Returns the matches name result.
     *
     * @param model the model
     * @return the matches name result
     */
    private boolean matchesName(String model) {
        if (model == null || model.isEmpty()) {
            return false;
        }
        String normModel = TRADEMARK_PATTERN.matcher(model.toLowerCase(Locale.ROOT)).replaceAll("").trim();
        String normName = TRADEMARK_PATTERN.matcher(cardName.toLowerCase(Locale.ROOT)).replaceAll("").trim();
        if (normModel.equals(normName)) {
            return true;
        }
        Matcher m = Pattern.compile("\\b" + Pattern.quote(normName) + "\\b").matcher(normModel);
        return m.find();
    }
}
