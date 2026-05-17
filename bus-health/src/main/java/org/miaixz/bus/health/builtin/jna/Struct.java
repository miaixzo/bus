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

import org.miaixz.bus.health.Builder;

/**
 * Wrapper classes for JNA clases which extend {@link com.sun.jna.Structure} intended for use in try-with-resources
 * blocks.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Struct {

    /*
     * Linux
     */
    /**
     * The CloseableSysinfo class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableSysinfo extends Sysinfo implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /*
     * macOS
     */

    /**
     * The CloseableHostCpuLoadInfo class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableHostCpuLoadInfo extends HostCpuLoadInfo implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableProcTaskInfo class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableProcTaskInfo extends ProcTaskInfo implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableProcTaskAllInfo class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableProcTaskAllInfo extends ProcTaskAllInfo implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableRUsageInfoV2 class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableRUsageInfoV2 extends RUsageInfoV2 implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableTimeval class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableTimeval extends Timeval implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableVMStatistics class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableVMStatistics extends VMStatistics implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableVnodePathInfo class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableVnodePathInfo extends VnodePathInfo implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableXswUsage class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableXswUsage extends XswUsage implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /*
     * Windows
     */

    /**
     * The CloseableMibIfRow class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableMibIfRow extends MIB_IFROW implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableMibIfRow2 class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableMibIfRow2 extends MIB_IF_ROW2 implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableMibTcpStats class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableMibTcpStats extends MIB_TCPSTATS implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableMibUdpStats class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableMibUdpStats extends MIB_UDPSTATS implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseablePdhRawCounter class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseablePdhRawCounter extends PDH_RAW_COUNTER implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseablePerformanceInformation class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseablePerformanceInformation extends PERFORMANCE_INFORMATION implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableSpDeviceInterfaceData class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableSpDeviceInterfaceData extends SP_DEVICE_INTERFACE_DATA implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableSpDevinfoData class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableSpDevinfoData extends SP_DEVINFO_DATA implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

    /**
     * The CloseableSystemInfo class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    class CloseableSystemInfo extends SYSTEM_INFO implements AutoCloseable {

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }

    }

}
