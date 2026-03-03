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

import org.miaixz.bus.health.Builder;

import com.sun.jna.platform.linux.LibC.Sysinfo;
import com.sun.jna.platform.mac.SystemB.*;
import com.sun.jna.platform.win32.IPHlpAPI.MIB_IFROW;
import com.sun.jna.platform.win32.IPHlpAPI.MIB_IF_ROW2;
import com.sun.jna.platform.win32.IPHlpAPI.MIB_TCPSTATS;
import com.sun.jna.platform.win32.IPHlpAPI.MIB_UDPSTATS;
import com.sun.jna.platform.win32.Pdh.PDH_RAW_COUNTER;
import com.sun.jna.platform.win32.Psapi.PERFORMANCE_INFORMATION;
import com.sun.jna.platform.win32.SetupApi.SP_DEVICE_INTERFACE_DATA;
import com.sun.jna.platform.win32.SetupApi.SP_DEVINFO_DATA;
import com.sun.jna.platform.win32.WinBase.SYSTEM_INFO;

/**
 * Wrapper classes for JNA clases which extend {@link com.sun.jna.Structure} intended for use in try-with-resources
 * blocks.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Struct {

    /*
     * Linux
     */
    class CloseableSysinfo extends Sysinfo implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    /*
     * macOS
     */

    class CloseableHostCpuLoadInfo extends HostCpuLoadInfo implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableProcTaskInfo extends ProcTaskInfo implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableProcTaskAllInfo extends ProcTaskAllInfo implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableRUsageInfoV2 extends RUsageInfoV2 implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableTimeval extends Timeval implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableVMStatistics extends VMStatistics implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableVnodePathInfo extends VnodePathInfo implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableXswUsage extends XswUsage implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    /*
     * Windows
     */

    class CloseableMibIfRow extends MIB_IFROW implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableMibIfRow2 extends MIB_IF_ROW2 implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableMibTcpStats extends MIB_TCPSTATS implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableMibUdpStats extends MIB_UDPSTATS implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseablePdhRawCounter extends PDH_RAW_COUNTER implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseablePerformanceInformation extends PERFORMANCE_INFORMATION implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableSpDeviceInterfaceData extends SP_DEVICE_INTERFACE_DATA implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableSpDevinfoData extends SP_DEVINFO_DATA implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    class CloseableSystemInfo extends SYSTEM_INFO implements AutoCloseable {

        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

}
