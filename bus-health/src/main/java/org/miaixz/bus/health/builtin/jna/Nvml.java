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

    int NVML_SUCCESS = 0;
    int NVML_TEMPERATURE_GPU = 0;
    int NVML_CLOCK_GRAPHICS = 0;
    int NVML_CLOCK_MEM = 2;
    int NVML_DEVICE_NAME_BUFFER_SIZE = 96;

    interface NvmlLibrary extends Library {

        int nvmlInit_v2();

        int nvmlShutdown();

        int nvmlDeviceGetCount_v2(IntByReference deviceCount);

        int nvmlDeviceGetHandleByIndex_v2(int index, PointerByReference device);

        int nvmlDeviceGetName(Pointer device, byte[] name, int length);

        int nvmlDeviceGetPciInfo_v3(Pointer device, NvmlPciInfo pci);

        int nvmlDeviceGetUtilizationRates(Pointer device, NvmlUtilization utilization);

        int nvmlDeviceGetMemoryInfo(Pointer device, NvmlMemory memory);

        int nvmlDeviceGetTemperature(Pointer device, int sensorType, IntByReference temp);

        int nvmlDeviceGetPowerUsage(Pointer device, IntByReference power);

        int nvmlDeviceGetClockInfo(Pointer device, int clockType, IntByReference clock);

        int nvmlDeviceGetFanSpeed(Pointer device, IntByReference speed);
    }

    @FieldOrder({ "gpu", "memory" })
    class NvmlUtilization extends Structure {

        public int gpu;
        public int memory;
    }

    @FieldOrder({ "total", "free", "used" })
    class NvmlMemory extends Structure {

        public long total;
        public long free;
        public long used;
    }

    @FieldOrder({ "busIdLegacy", "domain", "bus", "device", "pciDeviceId", "pciSubSystemId", "busId" })
    class NvmlPciInfo extends Structure {

        public byte[] busIdLegacy = new byte[16];
        public int domain;
        public int bus;
        public int device;
        public int pciDeviceId;
        public int pciSubSystemId;
        public byte[] busId = new byte[32];
    }
}
