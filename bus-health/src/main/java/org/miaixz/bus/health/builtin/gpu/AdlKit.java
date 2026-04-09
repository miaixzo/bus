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
package org.miaixz.bus.health.builtin.gpu;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.jna.Adl;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Optional runtime binding to the AMD Display Library (ADL) on Windows. All methods return sentinel values ({@code -1}
 * or {@code -1L}) when ADL is unavailable or a specific query fails.
 *
 * <p>
 * Only Overdrive N (Radeon RX 400 series and newer) is supported. Older Overdrive versions return -1.
 *
 * <p>
 * The native library is loaded once at class initialization. Each metric call pairs {@code ADL2_Main_Control_Create}
 * with {@code ADL2_Main_Control_Destroy} to correctly manage ADL's internal reference count, ensuring OSHI does not
 * interfere with other code in the same process that may also be managing the ADL lifecycle.
 *
 * <p>
 * Adapter bus-number-to-index mappings are enumerated once on first successful init and cached thereafter.
 * 
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class AdlKit {

    // -------------------------------------------------------------------------
    // Library loading (holder pattern — loads the .dll once)
    // -------------------------------------------------------------------------

    private static final class Holder {

        static final Adl.AdlLibrary LIB;
        static final boolean LIBRARY_LOADED;
        // Strong reference prevents GC of the callback while ADL holds a native function pointer to it.
        // Uses raw Pointer (not Memory) because ADL frees the native allocation directly via C free().
        // Memory's destructor would double-free the same address.
        static final Adl.AdlMallocCallback MALLOC_CB;

        static {
            Adl.AdlLibrary lib = null;
            boolean loaded = false;
            Adl.AdlMallocCallback cb = null;
            try {
                try {
                    lib = Native.load("atiadlxx", Adl.AdlLibrary.class);
                } catch (UnsatisfiedLinkError e) {
                    lib = Native.load("atiadlxy", Adl.AdlLibrary.class);
                }
                cb = size -> {
                    if (size <= 0) {
                        return Pointer.NULL;
                    }
                    long addr = Native.malloc((long) size);
                    return addr == 0L ? Pointer.NULL : new Pointer(addr);
                };
                loaded = true;
                Logger.debug("ADL library loaded");
            } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
                Logger.debug("ADL library not available: {}", e.getMessage());
            }
            LIB = lib;
            LIBRARY_LOADED = loaded;
            MALLOC_CB = cb;
        }
    }

    private AdlKit() {

    }

    // Lazy adapter enumeration state — written once, read-only thereafter
    private static volatile boolean adaptersEnumerated = false;
    private static volatile Map<Integer, Integer> busToIndex = Collections.emptyMap();

    // -------------------------------------------------------------------------
    // Init/uninit helpers (COM pattern)
    // -------------------------------------------------------------------------

    /**
     * Calls {@code ADL2_Main_Control_Create}, incrementing ADL's internal reference count. Every successful call must
     * be paired with exactly one call to {@link #adlUninit(Pointer)}, which decrements the same counter. This ensures
     * OSHI does not permanently hold a reference that would interfere with other code in the process managing the ADL
     * lifecycle.
     *
     * @return the ADL context pointer if initialization succeeded, or {@code null} if it failed
     */
    private static Pointer adlInit() {
        if (!Holder.LIBRARY_LOADED) {
            return null;
        }
        PointerByReference ctxRef = new PointerByReference();
        int ret = Holder.LIB.ADL2_Main_Control_Create(Holder.MALLOC_CB, 1, ctxRef);
        if (ret == Adl.ADL_OK) {
            return ctxRef.getValue();
        }
        Logger.debug("ADL2_Main_Control_Create failed with code {}", ret);
        return null;
    }

    /**
     * Calls {@code ADL2_Main_Control_Destroy}, decrementing the same internal reference count that {@link #adlInit()}
     * incremented. Must be called exactly once for each successful call to {@link #adlInit()}.
     *
     * @param context the ADL context pointer returned by {@link #adlInit()}
     */
    private static void adlUninit(Pointer context) {
        Holder.LIB.ADL2_Main_Control_Destroy(context);
    }

    /**
     * Enumerates adapter mappings on first call after a successful init. Subsequent calls are no-ops. Must be called
     * while ADL is initialized (i.e. between adlInit and adlUninit).
     *
     * @param context the ADL context pointer
     */
    private static void ensureAdaptersEnumerated(Pointer context) {
        if (adaptersEnumerated) {
            return;
        }
        Map<Integer, Integer> result = enumerateAdapters(context);
        if (result != null) {
            busToIndex = result;
            adaptersEnumerated = true;
            Logger.debug("ADL enumerated {} adapter(s)", busToIndex.size());
        } else {
            Logger.debug("ADL adapter enumeration failed; will retry on next call");
        }
    }

    private static Map<Integer, Integer> enumerateAdapters(Pointer ctx) {
        IntByReference numRef = new IntByReference();
        if (Holder.LIB.ADL2_Adapter_NumberOfAdapters_Get(ctx, numRef) != Adl.ADL_OK) {
            return null;
        }
        int num = numRef.getValue();
        if (num <= 0) {
            return Collections.emptyMap();
        }
        Adl.AdapterInfo[] infos = (Adl.AdapterInfo[]) new Adl.AdapterInfo().toArray(num);
        for (Adl.AdapterInfo info : infos) {
            info.iSize = info.size();
        }
        if (Holder.LIB.ADL2_Adapter_AdapterInfo_Get(ctx, infos, infos[0].size() * num) != Adl.ADL_OK) {
            return null;
        }
        Map<Integer, Integer> map = new HashMap<>();
        for (Adl.AdapterInfo info : infos) {
            if (info.iPresent != 0) {
                map.put(info.iBusNumber, info.iAdapterIndex);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    private static boolean supportsOverdriveN(Pointer context, int adapterIndex) {
        IntByReference supported = new IntByReference();
        IntByReference enabled = new IntByReference();
        IntByReference version = new IntByReference();
        if (Holder.LIB.ADL2_Overdrive_Caps(context, adapterIndex, supported, enabled, version) == Adl.ADL_OK) {
            return version.getValue() >= Adl.ADL_OVERDRIVE_VERSION_N;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns whether the ADL native library was successfully loaded. Does not indicate whether any AMD GPU is present
     * or whether {@code ADL2_Main_Control_Create} will succeed.
     *
     * @return true if the ADL library is available
     */
    public static boolean isAvailable() {
        return Holder.LIBRARY_LOADED;
    }

    /**
     * Finds the ADL adapter index for the given PCI bus number.
     *
     * @param pciBusNumber PCI bus number
     * @return adapter index, or -1 if not found or ADL unavailable
     */
    public static int findAdapterIndex(int pciBusNumber) {
        if (!Holder.LIBRARY_LOADED) {
            return -1;
        }
        Pointer ctx = adlInit();
        if (ctx == null) {
            return -1;
        }
        try {
            ensureAdaptersEnumerated(ctx);
            return busToIndex.getOrDefault(pciBusNumber, -1);
        } finally {
            adlUninit(ctx);
        }
    }

    /**
     * Returns GPU temperature in degrees Celsius, or -1 if unavailable.
     *
     * @param adapterIndex ADL adapter index from {@link #findAdapterIndex(int)}
     * @return temperature in °C or -1
     */
    public static double getTemperature(int adapterIndex) {
        if (adapterIndex < 0) {
            return -1d;
        }
        Pointer ctx = adlInit();
        if (ctx == null) {
            return -1d;
        }
        try {
            if (!supportsOverdriveN(ctx, adapterIndex)) {
                return -1d;
            }
            IntByReference temp = new IntByReference();
            if (Holder.LIB.ADL2_OverdriveN_Temperature_Get(
                    ctx,
                    adapterIndex,
                    Adl.ADL_OVERDRIVE_TEMPERATURE_EDGE,
                    temp) == Adl.ADL_OK) {
                return temp.getValue() / 1000.0;
            }
            return -1d;
        } finally {
            adlUninit(ctx);
        }
    }

    /**
     * Returns GPU core utilization percentage (0–100), or -1 if unavailable.
     *
     * @param adapterIndex ADL adapter index
     * @return utilization percentage or -1
     */
    public static double getGpuUtilization(int adapterIndex) {
        if (adapterIndex < 0) {
            return -1d;
        }
        Pointer ctx = adlInit();
        if (ctx == null) {
            return -1d;
        }
        try {
            if (!supportsOverdriveN(ctx, adapterIndex)) {
                return -1d;
            }
            Adl.ADLODNPerformanceStatus perf = new Adl.ADLODNPerformanceStatus();
            if (Holder.LIB.ADL2_OverdriveN_PerformanceStatus_Get(ctx, adapterIndex, perf) == Adl.ADL_OK) {
                perf.read();
                return perf.iGPUActivityPercent;
            }
            return -1d;
        } finally {
            adlUninit(ctx);
        }
    }

    /**
     * Returns GPU core clock speed in MHz, or -1 if unavailable. ADL reports clocks in 10 kHz units.
     *
     * @param adapterIndex ADL adapter index
     * @return core clock in MHz or -1
     */
    public static long getCoreClockMhz(int adapterIndex) {
        if (adapterIndex < 0) {
            return -1L;
        }
        Pointer ctx = adlInit();
        if (ctx == null) {
            return -1L;
        }
        try {
            if (!supportsOverdriveN(ctx, adapterIndex)) {
                return -1L;
            }
            Adl.ADLODNPerformanceStatus perf = new Adl.ADLODNPerformanceStatus();
            if (Holder.LIB.ADL2_OverdriveN_PerformanceStatus_Get(ctx, adapterIndex, perf) == Adl.ADL_OK) {
                perf.read();
                // iCoreClock is in 10 kHz units; divide by 100 to get MHz
                return perf.iCoreClock / 100L;
            }
            return -1L;
        } finally {
            adlUninit(ctx);
        }
    }

    /**
     * Returns GPU memory clock speed in MHz, or -1 if unavailable.
     *
     * @param adapterIndex ADL adapter index
     * @return memory clock in MHz or -1
     */
    public static long getMemoryClockMhz(int adapterIndex) {
        if (adapterIndex < 0) {
            return -1L;
        }
        Pointer ctx = adlInit();
        if (ctx == null) {
            return -1L;
        }
        try {
            if (!supportsOverdriveN(ctx, adapterIndex)) {
                return -1L;
            }
            Adl.ADLODNPerformanceStatus perf = new Adl.ADLODNPerformanceStatus();
            if (Holder.LIB.ADL2_OverdriveN_PerformanceStatus_Get(ctx, adapterIndex, perf) == Adl.ADL_OK) {
                perf.read();
                return perf.iMemoryClock / 100L;
            }
            return -1L;
        } finally {
            adlUninit(ctx);
        }
    }

    /**
     * Returns GPU power draw in watts, or -1 if unavailable. Uses Overdrive 6 power API which is available on Overdrive
     * N adapters. Power is reported in units of 1/256 watts.
     *
     * @param adapterIndex ADL adapter index
     * @return power in watts or -1
     */
    public static double getPowerDraw(int adapterIndex) {
        if (adapterIndex < 0) {
            return -1d;
        }
        Pointer ctx = adlInit();
        if (ctx == null) {
            return -1d;
        }
        try {
            if (!supportsOverdriveN(ctx, adapterIndex)) {
                return -1d;
            }
            IntByReference power = new IntByReference();
            if (Holder.LIB.ADL2_Overdrive6_CurrentPower_Get(ctx, adapterIndex, 0, power) == Adl.ADL_OK) {
                return power.getValue() / 256.0;
            }
            return -1d;
        } finally {
            adlUninit(ctx);
        }
    }

    /**
     * Returns GPU fan speed as a percentage (0–100), or -1 if unavailable.
     *
     * @param adapterIndex ADL adapter index
     * @return fan speed percentage or -1
     */
    public static double getFanSpeedPercent(int adapterIndex) {
        if (adapterIndex < 0) {
            return -1d;
        }
        Pointer ctx = adlInit();
        if (ctx == null) {
            return -1d;
        }
        try {
            if (!supportsOverdriveN(ctx, adapterIndex)) {
                return -1d;
            }
            Adl.ADLODNFanControl fan = new Adl.ADLODNFanControl();
            if (Holder.LIB.ADL2_OverdriveN_FanControl_Get(ctx, adapterIndex, fan) == Adl.ADL_OK) {
                fan.read();
                if (fan.iFanControlMode == Adl.ADL_FAN_SPEED_MODE_PERCENT) {
                    return fan.iCurrentFanSpeed;
                }
                // RPM mode: no max available here, return -1
            }
            return -1d;
        } finally {
            adlUninit(ctx);
        }
    }

}
