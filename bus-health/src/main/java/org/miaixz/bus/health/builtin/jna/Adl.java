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
import com.sun.jna.win32.StdCallLibrary;

/**
 * JNA bindings for the AMD Display Library (ADL) on Windows. This class should be considered non-API as it may be
 * removed if/when its code is incorporated into the JNA project.
 * 
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Adl {

    int ADL_OK = 0;
    int ADL_OVERDRIVE_VERSION_N = 8;
    int ADL_FAN_SPEED_MODE_PERCENT = 1;
    int ADL_OVERDRIVE_TEMPERATURE_EDGE = 1;

    /** ADL malloc callback: allocates memory and returns a pointer. Uses __stdcall per ADL SDK. */
    interface AdlMallocCallback extends StdCallLibrary.StdCallCallback {

        Pointer invoke(int size);
    }

    interface AdlLibrary extends Library {

        int ADL2_Main_Control_Create(
                AdlMallocCallback callback,
                int iEnumConnectedAdapters,
                PointerByReference context);

        int ADL2_Main_Control_Destroy(Pointer context);

        int ADL2_Adapter_NumberOfAdapters_Get(Pointer context, IntByReference numAdapters);

        int ADL2_Adapter_AdapterInfo_Get(Pointer context, AdapterInfo[] info, int inputSize);

        int ADL2_Overdrive_Caps(
                Pointer context,
                int iAdapterIndex,
                IntByReference iSupported,
                IntByReference iEnabled,
                IntByReference iVersion);

        int ADL2_OverdriveN_Temperature_Get(
                Pointer context,
                int iAdapterIndex,
                int iTemperatureType,
                IntByReference iTemperature);

        int ADL2_OverdriveN_PerformanceStatus_Get(
                Pointer context,
                int iAdapterIndex,
                ADLODNPerformanceStatus perfStatus);

        int ADL2_OverdriveN_FanControl_Get(Pointer context, int iAdapterIndex, ADLODNFanControl fanControl);

        int ADL2_Overdrive6_CurrentPower_Get(
                Pointer context,
                int iAdapterIndex,
                int iPowerType,
                IntByReference lpCurrentValue);
    }

    @FieldOrder({ "iSize", "iAdapterIndex", "strUDID", "iBusNumber", "iDeviceNumber", "iFunctionNumber", "iVendorID",
            "strAdapterName", "strDisplayName", "iPresent", "iExist", "strDriverPath", "strDriverPathExt",
            "strPNPString", "iOSDisplayIndex" })
    class AdapterInfo extends Structure {

        public int iSize;
        public int iAdapterIndex;
        public byte[] strUDID = new byte[256];
        public int iBusNumber;
        public int iDeviceNumber;
        public int iFunctionNumber;
        public int iVendorID;
        public byte[] strAdapterName = new byte[256];
        public byte[] strDisplayName = new byte[256];
        public int iPresent;
        public int iExist;
        public byte[] strDriverPath = new byte[256];
        public byte[] strDriverPathExt = new byte[256];
        public byte[] strPNPString = new byte[256];
        public int iOSDisplayIndex;
    }

    @FieldOrder({ "iCoreClock", "iMemoryClock", "iDCEFClock", "iGFXClock", "iUVDClock", "iVCEClock",
            "iGPUActivityPercent", "iCurrentCorePerformanceLevel", "iCurrentMemoryPerformanceLevel",
            "iCurrentDCEFPerformanceLevel", "iCurrentGFXPerformanceLevel", "iUVDPerformanceLevel",
            "iVCEPerformanceLevel", "iCurrentBusSpeed", "iCurrentBusLanes", "iMaximumBusLanes", "iVDDC", "iVDDCI" })
    class ADLODNPerformanceStatus extends Structure {

        public int iCoreClock;
        public int iMemoryClock;
        public int iDCEFClock;
        public int iGFXClock;
        public int iUVDClock;
        public int iVCEClock;
        public int iGPUActivityPercent;
        public int iCurrentCorePerformanceLevel;
        public int iCurrentMemoryPerformanceLevel;
        public int iCurrentDCEFPerformanceLevel;
        public int iCurrentGFXPerformanceLevel;
        public int iUVDPerformanceLevel;
        public int iVCEPerformanceLevel;
        public int iCurrentBusSpeed;
        public int iCurrentBusLanes;
        public int iMaximumBusLanes;
        public int iVDDC;
        public int iVDDCI;
    }

    @FieldOrder({ "iMode", "iFanControlMode", "iCurrentFanSpeedMode", "iCurrentFanSpeed", "iTargetFanSpeed",
            "iTargetTemperature", "iMinPerformanceClock", "iMinFanLimit" })
    class ADLODNFanControl extends Structure {

        public int iMode;
        public int iFanControlMode;
        public int iCurrentFanSpeedMode;
        public int iCurrentFanSpeed;
        public int iTargetFanSpeed;
        public int iTargetTemperature;
        public int iMinPerformanceClock;
        public int iMinFanLimit;
    }
}
