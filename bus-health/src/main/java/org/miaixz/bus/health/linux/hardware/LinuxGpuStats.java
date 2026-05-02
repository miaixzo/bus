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
package org.miaixz.bus.health.linux.hardware;

import java.io.File;
import java.util.Locale;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.gpu.NvmlKit;
import org.miaixz.bus.health.builtin.hardware.GpuStats;
import org.miaixz.bus.health.builtin.hardware.GpuTicks;

/**
 * Linux {@link GpuStats} session. Dynamic metrics are sourced in priority order: NVML (NVIDIA GPUs), then sysfs DRM
 * driver files under {@code /sys/class/drm/cardN/device/}. The hwmon path and driver-specific sysfs paths are resolved
 * once at construction time.
 *
 * <p>
 * GPU ticks are not available on Linux and always return {@code (0L, 0L)}. Shared memory is not available and always
 * returns -1.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class LinuxGpuStats implements GpuStats {

    /**
     * The drmDevicePath value.
     */
    private final String drmDevicePath;
    /**
     * The driverName value.
     */
    private final String driverName;
    /**
     * The pciBusId value.
     */
    private final String pciBusId;
    /**
     * The cardName value.
     */
    private final String cardName;

    // Cached at construction; empty string if not found
    /**
     * The hwmonPath value.
     */
    private final String hwmonPath;
    // Cached Intel gt0 path
    /**
     * The gt0Path value.
     */
    private final String gt0Path;

    // Cached NVML device id; null means not yet resolved, empty string means unavailable
    /**
     * The nvmlDeviceId value.
     */
    private String nvmlDeviceId;

    /**
     * The closed value.
     */
    private boolean closed;

    /**
     * Creates a new LinuxGpuStats instance.
     *
     * @param drmDevicePath the drm device path
     * @param driverName    the driver name
     * @param pciBusId      the pci bus id
     * @param cardName      the card name
     */
    LinuxGpuStats(String drmDevicePath, String driverName, String pciBusId, String cardName) {
        this.drmDevicePath = drmDevicePath;
        this.driverName = driverName;
        this.pciBusId = pciBusId;
        this.cardName = cardName;
        this.hwmonPath = resolveHwmonPath(drmDevicePath);
        this.gt0Path = drmDevicePath.isEmpty() ? "" : drmDevicePath + "/../gt/gt0";
    }

    /**
     * Closes this resource.
     */
    @Override
    public synchronized void close() {
        closed = true;
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
        if (drmDevicePath.isEmpty()) {
            return -1d;
        }
        String driver = driverName.toLowerCase(Locale.ROOT);
        if ("amdgpu".equals(driver)) {
            int pct = Builder.getIntFromFile(drmDevicePath + "/gpu_busy_percent");
            return pct >= 0 ? pct : -1d;
        }
        if ("i915".equals(driver) || "xe".equals(driver)) {
            // Approximates utilization as actual_freq / max_freq; not a true busy-time percentage
            // but the best available metric without kernel tracepoints.
            long actual = Builder.getLongFromFile(gt0Path + "/rps_act_freq_mhz");
            long max = Builder.getLongFromFile(gt0Path + "/rps_max_freq_mhz");
            if (actual >= 0 && max > 0) {
                return actual == 0 ? 0.0 : Math.min(100.0, actual * 100.0 / max);
            }
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
        String nvmlDevice = findNvmlDevice();
        if (nvmlDevice != null) {
            long val = NvmlKit.getVramUsed(nvmlDevice);
            if (val >= 0) {
                return val;
            }
        }
        if (drmDevicePath.isEmpty()) {
            return -1L;
        }
        if ("amdgpu".equals(driverName.toLowerCase(Locale.ROOT))) {
            long used = Builder.getLongFromFile(drmDevicePath + "/mem_info_vram_used");
            return used >= 0 ? used : -1L;
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
        String nvmlDevice = findNvmlDevice();
        if (nvmlDevice != null) {
            double val = NvmlKit.getTemperature(nvmlDevice);
            if (val >= 0) {
                return val;
            }
        }
        if (!hwmonPath.isEmpty()) {
            long milliC = Builder.getLongFromFile(hwmonPath + "/temp1_input");
            if (milliC >= 0) {
                return milliC / 1000.0;
            }
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
        String nvmlDevice = findNvmlDevice();
        if (nvmlDevice != null) {
            double val = NvmlKit.getPowerDraw(nvmlDevice);
            if (val >= 0) {
                return val;
            }
        }
        if (!hwmonPath.isEmpty()) {
            long microW = Builder.getLongFromFile(hwmonPath + "/power1_average");
            if (microW >= 0) {
                return microW / 1_000_000.0;
            }
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
        String nvmlDevice = findNvmlDevice();
        if (nvmlDevice != null) {
            long val = NvmlKit.getCoreClockMhz(nvmlDevice);
            if (val >= 0) {
                return val;
            }
        }
        if (drmDevicePath.isEmpty()) {
            return -1L;
        }
        String driver = driverName.toLowerCase(Locale.ROOT);
        if ("amdgpu".equals(driver)) {
            if (!hwmonPath.isEmpty()) {
                long hz = Builder.getLongFromFile(hwmonPath + "/freq1_input");
                if (hz > 0) {
                    return hz / 1_000_000L;
                }
            }
            return parseDpmActiveMhz(drmDevicePath + "/pp_dpm_sclk");
        }
        if ("i915".equals(driver) || "xe".equals(driver)) {
            long mhz = Builder.getLongFromFile(gt0Path + "/rps_cur_freq_mhz");
            return mhz > 0 ? mhz : -1L;
        }
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
        String nvmlDevice = findNvmlDevice();
        if (nvmlDevice != null) {
            long val = NvmlKit.getMemoryClockMhz(nvmlDevice);
            if (val >= 0) {
                return val;
            }
        }
        if (drmDevicePath.isEmpty()) {
            return -1L;
        }
        if ("amdgpu".equals(driverName.toLowerCase(Locale.ROOT))) {
            if (!hwmonPath.isEmpty()) {
                long hz = Builder.getLongFromFile(hwmonPath + "/freq2_input");
                if (hz > 0) {
                    return hz / 1_000_000L;
                }
            }
            return parseDpmActiveMhz(drmDevicePath + "/pp_dpm_mclk");
        }
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
        String nvmlDevice = findNvmlDevice();
        if (nvmlDevice != null) {
            double val = NvmlKit.getFanSpeedPercent(nvmlDevice);
            if (val >= 0) {
                return val;
            }
        }
        if (!hwmonPath.isEmpty()) {
            long fanRpm = Builder.getLongFromFile(hwmonPath + "/fan1_input");
            long fanMax = Builder.getLongFromFile(hwmonPath + "/fan1_max");
            if (fanRpm >= 0 && fanMax > 0) {
                return Math.min(100.0, fanRpm * 100.0 / fanMax);
            }
            long pwm = Builder.getLongFromFile(hwmonPath + "/pwm1");
            if (pwm >= 0) {
                return pwm / 255.0 * 100.0;
            }
        }
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
     * Returns the find nvml device result.
     *
     * @return the find nvml device result
     */
    private String findNvmlDevice() {
        if (nvmlDeviceId != null) {
            return nvmlDeviceId.isEmpty() ? null : nvmlDeviceId;
        }
        if (!NvmlKit.isAvailable()) {
            nvmlDeviceId = "";
            return null;
        }
        String id = null;
        if (!pciBusId.isEmpty()) {
            id = NvmlKit.findDevice(pciBusId);
        }
        if (id == null) {
            id = NvmlKit.findDeviceByName(cardName);
        }
        nvmlDeviceId = id != null ? id : "";
        return id;
    }

    /**
     * Returns the resolve hwmon path result.
     *
     * @param drmDevicePath the drm device path
     * @return the resolve hwmon path result
     */
    private static String resolveHwmonPath(String drmDevicePath) {
        if (drmDevicePath.isEmpty()) {
            return "";
        }
        File hwmonDir = new File(drmDevicePath + "/hwmon");
        File[] entries = hwmonDir.listFiles(f -> f.getName().startsWith("hwmon"));
        if (entries != null && entries.length > 0) {
            return entries[0].getAbsolutePath();
        }
        return "";
    }

    /**
     * Parses the dpm active mhz.
     *
     * @param path the path
     * @return the parse dpm active mhz result
     */
    private static long parseDpmActiveMhz(String path) {
        for (String line : Builder.readFile(path, false)) {
            if (line.endsWith("*")) {
                int mhzIdx = line.toLowerCase(Locale.ROOT).indexOf("mhz");
                if (mhzIdx > 0) {
                    int start = line.lastIndexOf(' ', mhzIdx - 1);
                    if (start >= 0) {
                        return Parsing.parseLongOrDefault(line.substring(start + 1, mhzIdx), -1L);
                    }
                }
            }
        }
        return -1L;
    }

}
