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
package org.miaixz.bus.health.windows.hardware;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.builtin.gpu.AdlKit;
import org.miaixz.bus.health.builtin.gpu.NvmlKit;
import org.miaixz.bus.health.builtin.hardware.GpuStats;
import org.miaixz.bus.health.builtin.hardware.GpuTicks;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.driver.perfmon.GpuInformation;
import org.miaixz.bus.health.windows.driver.wmi.LhmSensor;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * Windows {@link GpuStats} session.
 *
 * <p>
 * Metric source priority by method:
 * <ul>
 * <li>{@code getGpuTicks()}: PDH GPU Engine counters ({@code Running Time} / {@code Running Time_Base}).</li>
 * <li>{@code getGpuUtilization()}: LHM WMI {@code GPU Core} load sensor. Falls back to PDH tick-delta
 * ({@code getGpuTicks()} delta) when LHM is not running; returns -1 on the first call (priming).</li>
 * <li>{@code getVramUsed()}: PDH GPU Adapter Memory {@code DedicatedUsage}, then LHM {@code GPU Memory Used}.</li>
 * <li>{@code getSharedMemoryUsed()}: PDH GPU Adapter Memory {@code SharedUsage}.</li>
 * <li>{@code getTemperature()}: NVML, then ADL, then LHM {@code GPU Core} temperature.</li>
 * <li>{@code getPowerDraw()}: NVML, then ADL, then LHM {@code GPU Package} / {@code GPU Power}.</li>
 * <li>{@code getCoreClockMhz()}: NVML, then ADL, then LHM {@code GPU Core} clock.</li>
 * <li>{@code getMemoryClockMhz()}: NVML, then ADL, then LHM {@code GPU Memory} clock.</li>
 * <li>{@code getFanSpeedPercent()}: NVML, then ADL, then LHM {@code GPU Fan} / {@code GPU Fan 1}.</li>
 * </ul>
 *
 * <p>
 * PDH metrics require a valid LUID prefix (populated from DXGI). NVML requires an NVIDIA GPU with the NVML library
 * present. ADL requires an AMD GPU with the ADL library present. LHM requires LibreHardwareMonitor to be running.
 * 
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class WindowsGpuStats implements GpuStats {

    private static final long MB_TO_BYTES = 1_048_576L;

    private final String luidPrefix;
    private final String lhmParent;
    private final int pciBusNumber;
    private final String pciBusId;
    private final String cardName;

    private boolean closed;

    // Cached device lookups; null = not yet resolved, empty = unavailable
    private String cachedNvmlDevice;
    // Integer.MIN_VALUE = not yet resolved, -1 = unavailable
    private int cachedAdlIndex = Integer.MIN_VALUE;
    // Previous tick snapshot for PDH-based utilization fallback; null = not yet sampled
    private GpuTicks prevUtilTicks;

    WindowsGpuStats(String luidPrefix, String lhmParent, int pciBusNumber, String pciBusId, String cardName) {
        this.luidPrefix = luidPrefix;
        this.lhmParent = lhmParent;
        this.pciBusNumber = pciBusNumber;
        this.pciBusId = pciBusId;
        this.cardName = cardName;
    }

    @Override
    public synchronized void close() {
        closed = true;
    }

    @Override
    public synchronized boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized GpuTicks getGpuTicks() {
        checkOpen();
        if (luidPrefix.isEmpty()) {
            return new GpuTicks(0L, 0L);
        }
        Pair<List<String>, Map<GpuInformation.GpuEngineProperty, List<Long>>> engineData = GpuInformation
                .queryGpuEngineCounters();
        List<String> instances = engineData.getLeft();
        Map<GpuInformation.GpuEngineProperty, List<Long>> values = engineData.getRight();
        List<Long> runningTimes = values.get(GpuInformation.GpuEngineProperty.RUNNING_TIME);
        List<Long> runningTimeBases = values.get(GpuInformation.GpuEngineProperty.RUNNING_TIME_BASE);
        if (instances.isEmpty() || runningTimes == null || runningTimeBases == null) {
            return new GpuTicks(0L, 0L);
        }
        Map<String, Long> activeByType = new HashMap<>();
        Map<String, Long> baseByType = new HashMap<>();
        String luidLower = luidPrefix.toLowerCase(Locale.ROOT);
        int limit = Math.min(instances.size(), Math.min(runningTimes.size(), runningTimeBases.size()));
        for (int i = 0; i < limit; i++) {
            String inst = instances.get(i).toLowerCase(Locale.ROOT);
            if (!inst.contains(luidLower)) {
                continue;
            }
            int engTypeIdx = inst.lastIndexOf("_engtype_");
            String engType = engTypeIdx >= 0 ? inst.substring(engTypeIdx) : inst;
            activeByType.merge(engType, runningTimes.get(i), Long::sum);
            baseByType.merge(engType, runningTimeBases.get(i), Long::sum);
        }
        if (activeByType.isEmpty()) {
            return new GpuTicks(0L, 0L);
        }
        long totalActive = 0L;
        long totalBase = 0L;
        for (String key : activeByType.keySet()) {
            totalActive += activeByType.get(key);
            totalBase += baseByType.getOrDefault(key, 0L);
        }
        long idle = totalBase >= totalActive ? totalBase - totalActive : 0L;
        return new GpuTicks(totalActive, idle);
    }

    @Override
    public synchronized double getGpuUtilization() {
        checkOpen();
        if (!lhmParent.isEmpty()) {
            try {
                WmiResult<LhmSensor.LhmSensorProperty> sensors = LhmSensor.querySensors(lhmParent, "Load");
                for (int i = 0; i < sensors.getResultCount(); i++) {
                    if ("GPU Core".equals(WmiKit.getString(sensors, LhmSensor.LhmSensorProperty.NAME, i))) {
                        return WmiKit.getFloat(sensors, LhmSensor.LhmSensorProperty.VALUE, i);
                    }
                }
            } catch (Exception e) {
                Logger.debug("LHM GPU utilization query failed: {}", e.getMessage());
            }
        }
        // Fallback: derive utilization from PDH tick counters
        GpuTicks curr = getGpuTicks();
        if (prevUtilTicks != null) {
            long dActive = curr.getActiveTicks() - prevUtilTicks.getActiveTicks();
            long dIdle = curr.getIdleTicks() - prevUtilTicks.getIdleTicks();
            long dTotal = dActive + dIdle;
            prevUtilTicks = curr;
            return dTotal > 0 ? dActive * 100.0 / dTotal : -1d;
        }
        prevUtilTicks = curr;
        return -1d;
    }

    @Override
    public synchronized long getVramUsed() {
        checkOpen();
        long pdhResult = queryAdapterMemory(GpuInformation.GpuAdapterMemoryProperty.DEDICATED_USAGE);
        if (pdhResult >= 0) {
            return pdhResult;
        }
        if (!lhmParent.isEmpty()) {
            try {
                WmiResult<LhmSensor.LhmSensorProperty> sensors = LhmSensor.querySensors(lhmParent, "SmallData");
                for (int i = 0; i < sensors.getResultCount(); i++) {
                    if ("GPU Memory Used".equals(WmiKit.getString(sensors, LhmSensor.LhmSensorProperty.NAME, i))) {
                        float mb = WmiKit.getFloat(sensors, LhmSensor.LhmSensorProperty.VALUE, i);
                        return (long) (mb * MB_TO_BYTES);
                    }
                }
            } catch (Exception e) {
                Logger.debug("LHM GPU memory used query failed: {}", e.getMessage());
            }
        }
        return -1L;
    }

    @Override
    public synchronized long getSharedMemoryUsed() {
        checkOpen();
        if (luidPrefix.isEmpty()) {
            return -1L;
        }
        return queryAdapterMemory(GpuInformation.GpuAdapterMemoryProperty.SHARED_USAGE);
    }

    @Override
    public synchronized double getTemperature() {
        checkOpen();
        String nvmlDevice = findNvmlDevice();
        if (nvmlDevice != null) {
            double val = NvmlKit.getTemperature(nvmlDevice);
            if (val >= 0) {
                return val;
            }
        }
        int adlIndex = findAdlIndex();
        if (adlIndex >= 0) {
            double val = AdlKit.getTemperature(adlIndex);
            if (val >= 0) {
                return val;
            }
        }
        return lhmFloatSensor("Temperature", "GPU Core");
    }

    @Override
    public synchronized double getPowerDraw() {
        checkOpen();
        String nvmlDevice = findNvmlDevice();
        if (nvmlDevice != null) {
            double val = NvmlKit.getPowerDraw(nvmlDevice);
            if (val >= 0) {
                return val;
            }
        }
        int adlIndex = findAdlIndex();
        if (adlIndex >= 0) {
            double val = AdlKit.getPowerDraw(adlIndex);
            if (val >= 0) {
                return val;
            }
        }
        double lhm = lhmFloatSensor("Power", "GPU Package");
        if (lhm >= 0) {
            return lhm;
        }
        return lhmFloatSensor("Power", "GPU Power");
    }

    @Override
    public synchronized long getCoreClockMhz() {
        checkOpen();
        String nvmlDevice = findNvmlDevice();
        if (nvmlDevice != null) {
            long val = NvmlKit.getCoreClockMhz(nvmlDevice);
            if (val >= 0) {
                return val;
            }
        }
        int adlIndex = findAdlIndex();
        if (adlIndex >= 0) {
            long val = AdlKit.getCoreClockMhz(adlIndex);
            if (val >= 0) {
                return val;
            }
        }
        double lhm = lhmFloatSensor("Clock", "GPU Core");
        return lhm >= 0 ? (long) lhm : -1L;
    }

    @Override
    public synchronized long getMemoryClockMhz() {
        checkOpen();
        String nvmlDevice = findNvmlDevice();
        if (nvmlDevice != null) {
            long val = NvmlKit.getMemoryClockMhz(nvmlDevice);
            if (val >= 0) {
                return val;
            }
        }
        int adlIndex = findAdlIndex();
        if (adlIndex >= 0) {
            long val = AdlKit.getMemoryClockMhz(adlIndex);
            if (val >= 0) {
                return val;
            }
        }
        double lhm = lhmFloatSensor("Clock", "GPU Memory");
        return lhm >= 0 ? (long) lhm : -1L;
    }

    @Override
    public synchronized double getFanSpeedPercent() {
        checkOpen();
        String nvmlDevice = findNvmlDevice();
        if (nvmlDevice != null) {
            double val = NvmlKit.getFanSpeedPercent(nvmlDevice);
            if (val >= 0) {
                return val;
            }
        }
        int adlIndex = findAdlIndex();
        if (adlIndex >= 0) {
            double val = AdlKit.getFanSpeedPercent(adlIndex);
            if (val >= 0) {
                return val;
            }
        }
        double lhm = lhmFloatSensor("Control", "GPU Fan");
        if (lhm >= 0) {
            return lhm;
        }
        return lhmFloatSensor("Control", "GPU Fan 1");
    }

    private void checkOpen() {
        if (closed) {
            throw new IllegalStateException(
                    "GpuStats session has been closed. Obtain a new session via GraphicsCard.createStatsSession().");
        }
    }

    private long queryAdapterMemory(GpuInformation.GpuAdapterMemoryProperty property) {
        if (luidPrefix.isEmpty()) {
            return -1L;
        }
        Pair<List<String>, Map<GpuInformation.GpuAdapterMemoryProperty, List<Long>>> adapterData = GpuInformation
                .queryGpuAdapterMemoryCounters();
        List<String> instances = adapterData.getLeft();
        List<Long> values = adapterData.getRight().get(property);
        if (values != null) {
            String luidLower = luidPrefix.toLowerCase(Locale.ROOT);
            int limit = Math.min(instances.size(), values.size());
            for (int i = 0; i < limit; i++) {
                if (instances.get(i).toLowerCase(Locale.ROOT).contains(luidLower)) {
                    return values.get(i);
                }
            }
        }
        return -1L;
    }

    private String findNvmlDevice() {
        if (cachedNvmlDevice != null) {
            return cachedNvmlDevice.isEmpty() ? null : cachedNvmlDevice;
        }
        if (!NvmlKit.isAvailable()) {
            cachedNvmlDevice = "";
            return null;
        }
        String id = null;
        if (!pciBusId.isEmpty()) {
            id = NvmlKit.findDevice(pciBusId);
        }
        if (id == null) {
            id = NvmlKit.findDeviceByName(cardName);
        }
        cachedNvmlDevice = id != null ? id : "";
        return id;
    }

    private int findAdlIndex() {
        if (cachedAdlIndex != Integer.MIN_VALUE) {
            return cachedAdlIndex;
        }
        if (!AdlKit.isAvailable() || pciBusNumber < 0) {
            cachedAdlIndex = -1;
            return -1;
        }
        cachedAdlIndex = AdlKit.findAdapterIndex(pciBusNumber);
        return cachedAdlIndex;
    }

    private double lhmFloatSensor(String sensorType, String sensorName) {
        if (lhmParent.isEmpty()) {
            return -1d;
        }
        try {
            WmiResult<LhmSensor.LhmSensorProperty> sensors = LhmSensor.querySensors(lhmParent, sensorType);
            for (int i = 0; i < sensors.getResultCount(); i++) {
                if (sensorName.equals(WmiKit.getString(sensors, LhmSensor.LhmSensorProperty.NAME, i))) {
                    return WmiKit.getFloat(sensors, LhmSensor.LhmSensorProperty.VALUE, i);
                }
            }
        } catch (Exception e) {
            Logger.debug("LHM {} {} query failed: {}", sensorType, sensorName, e.getMessage());
        }
        return -1d;
    }

}
