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
import com.sun.jna.win32.StdCallLibrary;

/**
 * JNA bindings for the AMD Display Library (ADL) on Windows. This class should be considered non-API as it may be
 * removed if/when its code is incorporated into the JNA project.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Adl {

    /**
     * The ADL_OK value.
     */
    int ADL_OK = 0;

    /**
     * The ADL_OVERDRIVE_VERSION_N value.
     */
    int ADL_OVERDRIVE_VERSION_N = 8;

    /**
     * The ADL_FAN_SPEED_MODE_PERCENT value.
     */
    int ADL_FAN_SPEED_MODE_PERCENT = 1;

    /**
     * The ADL_OVERDRIVE_TEMPERATURE_EDGE value.
     */
    int ADL_OVERDRIVE_TEMPERATURE_EDGE = 1;

    /**
     * ADL malloc callback: allocates memory and returns a pointer. Uses __stdcall per ADL SDK.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    interface AdlMallocCallback extends StdCallLibrary.StdCallCallback {

        /**
         * Returns the invoke result.
         *
         * @param size the size
         * @return the invoke result
         */
        Pointer invoke(int size);

    }

    /**
     * The AdlLibrary interface.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    interface AdlLibrary extends Library {

        /**
         * Returns the adl2 main control create result.
         *
         * @param callback               the callback
         * @param iEnumConnectedAdapters the i enum connected adapters
         * @param context                the context
         * @return the adl2 main control create result
         */
        int ADL2_Main_Control_Create(
                AdlMallocCallback callback,
                int iEnumConnectedAdapters,
                PointerByReference context);

        /**
         * Returns the adl2 main control destroy result.
         *
         * @param context the context
         * @return the adl2 main control destroy result
         */
        int ADL2_Main_Control_Destroy(Pointer context);

        /**
         * Returns the adl2 adapter number of adapters get result.
         *
         * @param context     the context
         * @param numAdapters the num adapters
         * @return the adl2 adapter number of adapters get result
         */
        int ADL2_Adapter_NumberOfAdapters_Get(Pointer context, IntByReference numAdapters);

        /**
         * Returns the adl2 adapter adapter info get result.
         *
         * @param context   the context
         * @param info      the info
         * @param inputSize the input size
         * @return the adl2 adapter adapter info get result
         */
        int ADL2_Adapter_AdapterInfo_Get(Pointer context, AdapterInfo[] info, int inputSize);

        /**
         * Returns the adl2 overdrive caps result.
         *
         * @param context       the context
         * @param iAdapterIndex the i adapter index
         * @param iSupported    the i supported
         * @param iEnabled      the i enabled
         * @param iVersion      the i version
         * @return the adl2 overdrive caps result
         */
        int ADL2_Overdrive_Caps(
                Pointer context,
                int iAdapterIndex,
                IntByReference iSupported,
                IntByReference iEnabled,
                IntByReference iVersion);

        /**
         * Returns the adl2 overdrive n temperature get result.
         *
         * @param context          the context
         * @param iAdapterIndex    the i adapter index
         * @param iTemperatureType the i temperature type
         * @param iTemperature     the i temperature
         * @return the adl2 overdrive n temperature get result
         */
        int ADL2_OverdriveN_Temperature_Get(
                Pointer context,
                int iAdapterIndex,
                int iTemperatureType,
                IntByReference iTemperature);

        /**
         * Returns the adl2 overdrive n performance status get result.
         *
         * @param context       the context
         * @param iAdapterIndex the i adapter index
         * @param perfStatus    the perf status
         * @return the adl2 overdrive n performance status get result
         */
        int ADL2_OverdriveN_PerformanceStatus_Get(
                Pointer context,
                int iAdapterIndex,
                ADLODNPerformanceStatus perfStatus);

        /**
         * Returns the adl2 overdrive n fan control get result.
         *
         * @param context       the context
         * @param iAdapterIndex the i adapter index
         * @param fanControl    the fan control
         * @return the adl2 overdrive n fan control get result
         */
        int ADL2_OverdriveN_FanControl_Get(Pointer context, int iAdapterIndex, ADLODNFanControl fanControl);

        /**
         * Returns the adl2 overdrive6 current power get result.
         *
         * @param context        the context
         * @param iAdapterIndex  the i adapter index
         * @param iPowerType     the i power type
         * @param lpCurrentValue the lp current value
         * @return the adl2 overdrive6 current power get result
         */
        int ADL2_Overdrive6_CurrentPower_Get(
                Pointer context,
                int iAdapterIndex,
                int iPowerType,
                IntByReference lpCurrentValue);

    }

    /**
     * The AdapterInfo class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "iSize", "iAdapterIndex", "strUDID", "iBusNumber", "iDeviceNumber", "iFunctionNumber", "iVendorID",
            "strAdapterName", "strDisplayName", "iPresent", "iExist", "strDriverPath", "strDriverPathExt",
            "strPNPString", "iOSDisplayIndex" })
    class AdapterInfo extends Structure {

        /**
         * Constructs a new AdapterInfo instance.
         */
        public AdapterInfo() {
            // No initialization required.
        }

        /**
         * The iSize value.
         */
        public int iSize;

        /**
         * The iAdapterIndex value.
         */
        public int iAdapterIndex;

        /**
         * The strUDID value.
         */
        public byte[] strUDID = new byte[256];

        /**
         * The iBusNumber value.
         */
        public int iBusNumber;

        /**
         * The iDeviceNumber value.
         */
        public int iDeviceNumber;

        /**
         * The iFunctionNumber value.
         */
        public int iFunctionNumber;

        /**
         * The iVendorID value.
         */
        public int iVendorID;

        /**
         * The strAdapterName value.
         */
        public byte[] strAdapterName = new byte[256];

        /**
         * The strDisplayName value.
         */
        public byte[] strDisplayName = new byte[256];

        /**
         * The iPresent value.
         */
        public int iPresent;

        /**
         * The iExist value.
         */
        public int iExist;

        /**
         * The strDriverPath value.
         */
        public byte[] strDriverPath = new byte[256];

        /**
         * The strDriverPathExt value.
         */
        public byte[] strDriverPathExt = new byte[256];

        /**
         * The strPNPString value.
         */
        public byte[] strPNPString = new byte[256];

        /**
         * The iOSDisplayIndex value.
         */
        public int iOSDisplayIndex;

    }

    /**
     * The ADLODNPerformanceStatus class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "iCoreClock", "iMemoryClock", "iDCEFClock", "iGFXClock", "iUVDClock", "iVCEClock",
            "iGPUActivityPercent", "iCurrentCorePerformanceLevel", "iCurrentMemoryPerformanceLevel",
            "iCurrentDCEFPerformanceLevel", "iCurrentGFXPerformanceLevel", "iUVDPerformanceLevel",
            "iVCEPerformanceLevel", "iCurrentBusSpeed", "iCurrentBusLanes", "iMaximumBusLanes", "iVDDC", "iVDDCI" })
    class ADLODNPerformanceStatus extends Structure {

        /**
         * Constructs a new ADLODNPerformanceStatus instance.
         */
        public ADLODNPerformanceStatus() {
            // No initialization required.
        }

        /**
         * The iCoreClock value.
         */
        public int iCoreClock;

        /**
         * The iMemoryClock value.
         */
        public int iMemoryClock;

        /**
         * The iDCEFClock value.
         */
        public int iDCEFClock;

        /**
         * The iGFXClock value.
         */
        public int iGFXClock;

        /**
         * The iUVDClock value.
         */
        public int iUVDClock;

        /**
         * The iVCEClock value.
         */
        public int iVCEClock;

        /**
         * The iGPUActivityPercent value.
         */
        public int iGPUActivityPercent;

        /**
         * The iCurrentCorePerformanceLevel value.
         */
        public int iCurrentCorePerformanceLevel;

        /**
         * The iCurrentMemoryPerformanceLevel value.
         */
        public int iCurrentMemoryPerformanceLevel;

        /**
         * The iCurrentDCEFPerformanceLevel value.
         */
        public int iCurrentDCEFPerformanceLevel;

        /**
         * The iCurrentGFXPerformanceLevel value.
         */
        public int iCurrentGFXPerformanceLevel;

        /**
         * The iUVDPerformanceLevel value.
         */
        public int iUVDPerformanceLevel;

        /**
         * The iVCEPerformanceLevel value.
         */
        public int iVCEPerformanceLevel;

        /**
         * The iCurrentBusSpeed value.
         */
        public int iCurrentBusSpeed;

        /**
         * The iCurrentBusLanes value.
         */
        public int iCurrentBusLanes;

        /**
         * The iMaximumBusLanes value.
         */
        public int iMaximumBusLanes;

        /**
         * The iVDDC value.
         */
        public int iVDDC;

        /**
         * The iVDDCI value.
         */
        public int iVDDCI;

    }

    /**
     * The ADLODNFanControl class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FieldOrder({ "iMode", "iFanControlMode", "iCurrentFanSpeedMode", "iCurrentFanSpeed", "iTargetFanSpeed",
            "iTargetTemperature", "iMinPerformanceClock", "iMinFanLimit" })
    class ADLODNFanControl extends Structure {

        /**
         * Constructs a new ADLODNFanControl instance.
         */
        public ADLODNFanControl() {
            // No initialization required.
        }

        /**
         * The iMode value.
         */
        public int iMode;

        /**
         * The iFanControlMode value.
         */
        public int iFanControlMode;

        /**
         * The iCurrentFanSpeedMode value.
         */
        public int iCurrentFanSpeedMode;

        /**
         * The iCurrentFanSpeed value.
         */
        public int iCurrentFanSpeed;

        /**
         * The iTargetFanSpeed value.
         */
        public int iTargetFanSpeed;

        /**
         * The iTargetTemperature value.
         */
        public int iTargetTemperature;

        /**
         * The iMinPerformanceClock value.
         */
        public int iMinPerformanceClock;

        /**
         * The iMinFanLimit value.
         */
        public int iMinFanLimit;

    }

}
