/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.health.unix.platform.solaris.hardware;

import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.health.builtin.hardware.PowerSource;
import org.miaixz.bus.health.builtin.hardware.common.AbstractPowerSource;
import org.miaixz.bus.health.unix.platform.solaris.KstatKit;
import org.miaixz.bus.health.unix.platform.solaris.KstatKit.KstatChain;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * A Power Source
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class SolarisPowerSource extends AbstractPowerSource {

    // One-time lookup to see which kstat module to use
    private static final String[] KSTAT_BATT_MOD = {null, "battery", "acpi_drv"};

    private static final int KSTAT_BATT_IDX;

    static {
        try (KstatChain kc = KstatKit.openChain()) {
            if (kc.lookup(KSTAT_BATT_MOD[1], 0, null) != null) {
                KSTAT_BATT_IDX = 1;
            } else if (kc.lookup(KSTAT_BATT_MOD[2], 0, null) != null) {
                KSTAT_BATT_IDX = 2;
            } else {
                KSTAT_BATT_IDX = 0;
            }
        }
    }

    public SolarisPowerSource(String psName, String psDeviceName, double psRemainingCapacityPercent,
                              double psTimeRemainingEstimated, double psTimeRemainingInstant, double psPowerUsageRate, double psVoltage,
                              double psAmperage, boolean psPowerOnLine, boolean psCharging, boolean psDischarging,
                              PowerSource.CapacityUnits psCapacityUnits, int psCurrentCapacity, int psMaxCapacity, int psDesignCapacity,
                              int psCycleCount, String psChemistry, LocalDate psManufactureDate, String psManufacturer,
                              String psSerialNumber, double psTemperature) {
        super(psName, psDeviceName, psRemainingCapacityPercent, psTimeRemainingEstimated, psTimeRemainingInstant,
                psPowerUsageRate, psVoltage, psAmperage, psPowerOnLine, psCharging, psDischarging, psCapacityUnits,
                psCurrentCapacity, psMaxCapacity, psDesignCapacity, psCycleCount, psChemistry, psManufactureDate,
                psManufacturer, psSerialNumber, psTemperature);
    }

    /**
     * Gets Battery Information
     *
     * @return A list of PowerSource objects representing batteries, etc.
     */
    public static List<PowerSource> getPowerSources() {
        return Arrays.asList(getPowerSource("BAT0"));
    }

    private static SolarisPowerSource getPowerSource(String name) {
        String psName = name;
        String psDeviceName = Normal.UNKNOWN;
        double psRemainingCapacityPercent = 1d;
        double psTimeRemainingEstimated = -1d; // -1 = unknown, -2 = unlimited
        double psTimeRemainingInstant = 0d;
        double psPowerUsageRate = 0d;
        double psVoltage = -1d;
        double psAmperage = 0d;
        boolean psPowerOnLine = false;
        boolean psCharging = false;
        boolean psDischarging = false;
        PowerSource.CapacityUnits psCapacityUnits = PowerSource.CapacityUnits.RELATIVE;
        int psCurrentCapacity = 0;
        int psMaxCapacity = 1;
        int psDesignCapacity = 1;
        int psCycleCount = -1;
        String psChemistry = Normal.UNKNOWN;
        LocalDate psManufactureDate = null;
        String psManufacturer = Normal.UNKNOWN;
        String psSerialNumber = Normal.UNKNOWN;
        double psTemperature = 0d;

        // If no kstat info, return empty
        if (KSTAT_BATT_IDX > 0) {
            // Get kstat for the battery information
            try (KstatChain kc = KstatKit.openChain()) {
                Kstat ksp = kc.lookup(KSTAT_BATT_MOD[KSTAT_BATT_IDX], 0, "battery BIF0");
                if (ksp != null && kc.read(ksp)) {
                    // Predicted battery capacity when fully charged.
                    long energyFull = KstatKit.dataLookupLong(ksp, "bif_last_cap");
                    if (energyFull == 0xffffffff || energyFull <= 0) {
                        energyFull = KstatKit.dataLookupLong(ksp, "bif_design_cap");
                    }
                    if (energyFull != 0xffffffff && energyFull > 0) {
                        psMaxCapacity = (int) energyFull;
                    }
                    long unit = KstatKit.dataLookupLong(ksp, "bif_unit");
                    if (unit == 0) {
                        psCapacityUnits = PowerSource.CapacityUnits.MWH;
                    } else if (unit == 1) {
                        psCapacityUnits = PowerSource.CapacityUnits.MAH;
                    }
                    psDeviceName = KstatKit.dataLookupString(ksp, "bif_model");
                    psSerialNumber = KstatKit.dataLookupString(ksp, "bif_serial");
                    psChemistry = KstatKit.dataLookupString(ksp, "bif_type");
                    psManufacturer = KstatKit.dataLookupString(ksp, "bif_oem_info");
                }

                // Get kstat for the battery state
                ksp = kc.lookup(KSTAT_BATT_MOD[KSTAT_BATT_IDX], 0, "battery BST0");
                if (ksp != null && kc.read(ksp)) {
                    // estimated remaining battery capacity
                    long energyNow = KstatKit.dataLookupLong(ksp, "bst_rem_cap");
                    if (energyNow >= 0) {
                        psCurrentCapacity = (int) energyNow;
                    }
                    // power or current supplied at battery terminal
                    long powerNow = KstatKit.dataLookupLong(ksp, "bst_rate");
                    if (powerNow == 0xFFFFFFFF) {
                        powerNow = 0L;
                    }
                    // Battery State:
                    // bit 0 = discharging
                    // bit 1 = charging
                    // bit 2 = critical energy state
                    boolean isCharging = (KstatKit.dataLookupLong(ksp, "bst_state") & 0x10) > 0;

                    if (!isCharging) {
                        psTimeRemainingEstimated = powerNow > 0 ? 3600d * energyNow / powerNow : -1d;
                    }

                    long voltageNow = KstatKit.dataLookupLong(ksp, "bst_voltage");
                    if (voltageNow > 0) {
                        psVoltage = voltageNow / 1000d;
                        psAmperage = psPowerUsageRate * 1000d / voltageNow;
                    }
                }
            }
        }

        return new SolarisPowerSource(psName, psDeviceName, psRemainingCapacityPercent, psTimeRemainingEstimated,
                psTimeRemainingInstant, psPowerUsageRate, psVoltage, psAmperage, psPowerOnLine, psCharging,
                psDischarging, psCapacityUnits, psCurrentCapacity, psMaxCapacity, psDesignCapacity, psCycleCount,
                psChemistry, psManufactureDate, psManufacturer, psSerialNumber, psTemperature);
    }

}
