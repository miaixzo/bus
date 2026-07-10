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
package org.miaixz.bus.health.builtin.jna;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * JNA bindings for the NVIDIA Management Library (NVML). This class should be considered non-API as it may be removed
 * if/when its code is incorporated into the JNA project.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Nvml {

    /**
     * The NVML_SUCCESS value.
     */
    int NVML_SUCCESS = 0;

    /**
     * The NVML_TEMPERATURE_GPU value.
     */
    int NVML_TEMPERATURE_GPU = 0;

    /**
     * The NVML_CLOCK_GRAPHICS value.
     */
    int NVML_CLOCK_GRAPHICS = 0;

    /**
     * The NVML_CLOCK_MEM value.
     */
    int NVML_CLOCK_MEM = 2;

    /**
     * The NVML_DEVICE_NAME_BUFFER_SIZE value.
     */
    int NVML_DEVICE_NAME_BUFFER_SIZE = 96;

    /**
     * The NvmlLibrary interface.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    interface NvmlLibrary extends Library {

        /**
         * Returns the nvml init v2 result.
         *
         * @return the nvml init v2 result
         */
        int nvmlInit_v2();

        /**
         * Returns the nvml shutdown result.
         *
         * @return the nvml shutdown result
         */
        int nvmlShutdown();

        /**
         * Returns the nvml device get count v2 result.
         *
         * @param deviceCount the device count
         * @return the nvml device get count v2 result
         */
        int nvmlDeviceGetCount_v2(IntByReference deviceCount);

        /**
         * Returns the nvml device get handle by index v2 result.
         *
         * @param index  the index
         * @param device the device
         * @return the nvml device get handle by index v2 result
         */
        int nvmlDeviceGetHandleByIndex_v2(int index, PointerByReference device);

        /**
         * Returns the nvml device get name result.
         *
         * @param device the device
         * @param name   the name
         * @param length the length
         * @return the nvml device get name result
         */
        int nvmlDeviceGetName(Pointer device, byte[] name, int length);

        /**
         * Returns the nvml device get pci info v3 result.
         *
         * @param device the device
         * @param pci    the pci
         * @return the nvml device get pci info v3 result
         */
        int nvmlDeviceGetPciInfo_v3(Pointer device, NvmlPciInfo pci);

        /**
         * Returns the nvml device get utilization rates result.
         *
         * @param device      the device
         * @param utilization the utilization
         * @return the nvml device get utilization rates result
         */
        int nvmlDeviceGetUtilizationRates(Pointer device, NvmlUtilization utilization);

        /**
         * Returns the nvml device get memory info result.
         *
         * @param device the device
         * @param memory the memory
         * @return the nvml device get memory info result
         */
        int nvmlDeviceGetMemoryInfo(Pointer device, NvmlMemory memory);

        /**
         * Returns the nvml device get temperature result.
         *
         * @param device     the device
         * @param sensorType the sensor type
         * @param temp       the temp
         * @return the nvml device get temperature result
         */
        int nvmlDeviceGetTemperature(Pointer device, int sensorType, IntByReference temp);

        /**
         * Returns the nvml device get power usage result.
         *
         * @param device the device
         * @param power  the power
         * @return the nvml device get power usage result
         */
        int nvmlDeviceGetPowerUsage(Pointer device, IntByReference power);

        /**
         * Returns the nvml device get clock info result.
         *
         * @param device    the device
         * @param clockType the clock type
         * @param clock     the clock
         * @return the nvml device get clock info result
         */
        int nvmlDeviceGetClockInfo(Pointer device, int clockType, IntByReference clock);

        /**
         * Returns the nvml device get fan speed result.
         *
         * @param device the device
         * @param speed  the speed
         * @return the nvml device get fan speed result
         */
        int nvmlDeviceGetFanSpeed(Pointer device, IntByReference speed);

    }

    /**
     * The NvmlUtilization class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "gpu", "memory" })
    class NvmlUtilization extends Structure {

        /**
         * Creates a new NvmlUtilization instance.
         */
        public NvmlUtilization() {
            // No initialization required.
        }

        /**
         * The gpu value.
         */
        public int gpu;

        /**
         * The memory value.
         */
        public int memory;

    }

    /**
     * The NvmlMemory class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "total", "free", "used" })
    class NvmlMemory extends Structure {

        /**
         * Creates a new NvmlMemory instance.
         */
        public NvmlMemory() {
            // No initialization required.
        }

        /**
         * The total value.
         */
        public long total;

        /**
         * The free value.
         */
        public long free;

        /**
         * The used value.
         */
        public long used;

    }

    /**
     * The NvmlPciInfo class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "busIdLegacy", "domain", "bus", "device", "pciDeviceId", "pciSubSystemId", "busId" })
    class NvmlPciInfo extends Structure {

        /**
         * Creates a new NvmlPciInfo instance.
         */
        public NvmlPciInfo() {
            // No initialization required.
        }

        /**
         * The busIdLegacy value.
         */
        public byte[] busIdLegacy = new byte[16];

        /**
         * The domain value.
         */
        public int domain;

        /**
         * The bus value.
         */
        public int bus;

        /**
         * The device value.
         */
        public int device;

        /**
         * The pciDeviceId value.
         */
        public int pciDeviceId;

        /**
         * The pciSubSystemId value.
         */
        public int pciSubSystemId;

        /**
         * The busId value.
         */
        public byte[] busId = new byte[32];

    }

}
