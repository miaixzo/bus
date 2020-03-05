/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.health.hardware.unix.freebsd;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Command;
import org.aoju.bus.health.common.unix.freebsd.BsdSysctlUtils;
import org.aoju.bus.health.hardware.AbstractPowerSource;
import org.aoju.bus.health.hardware.PowerSource;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Power Source
 *
 * @author Kimi Liu
 * @version 5.6.6
 * @since JDK 1.8+
 */
public class FreeBsdPowerSource extends AbstractPowerSource {

    public FreeBsdPowerSource(String psName, String psDeviceName, double psRemainingCapacityPercent,
                              double psTimeRemainingEstimated, double psTimeRemainingInstant, double psPowerUsageRate, double psVoltage,
                              double psAmperage, boolean psPowerOnLine, boolean psCharging, boolean psDischarging,
                              CapacityUnits psCapacityUnits, int psCurrentCapacity, int psMaxCapacity, int psDesignCapacity,
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
     * @return An array of PowerSource objects representing batteries, etc.
     */
    public static PowerSource[] getPowerSources() {
        FreeBsdPowerSource[] ps = new FreeBsdPowerSource[1];
        ps[0] = getPowerSource("BAT0");
        return ps;
    }

    private static FreeBsdPowerSource getPowerSource(String name) {
        String psName = name;
        double psRemainingCapacityPercent = 1d;
        double psTimeRemainingEstimated = -1d; // -1 = unknown, -2 = unlimited
        double psPowerUsageRate = 0d;
        double psVoltage = -1d;
        double psAmperage = 0d;
        boolean psPowerOnLine = false;
        boolean psCharging = false;
        boolean psDischarging = false;
        CapacityUnits psCapacityUnits = CapacityUnits.RELATIVE;
        int psCurrentCapacity = 0;
        int psMaxCapacity = 1;
        int psDesignCapacity = 1;
        int psCycleCount = -1;
        LocalDate psManufactureDate = null;

        double psTemperature = 0d;

        // state 0=full, 1=discharging, 2=charging
        int state = BsdSysctlUtils.sysctl("hw.acpi.battery.state", 0);
        if (state == 2) {
            psCharging = true;
        } else {
            int time = BsdSysctlUtils.sysctl("hw.acpi.battery.time", -1);
            // time is in minutes
            psTimeRemainingEstimated = time < 0 ? -1d : 60d * time;
            if (state == 1) {
                psDischarging = true;
            }
        }
        // life is in percent
        int life = BsdSysctlUtils.sysctl("hw.acpi.battery.life", -1);
        if (life > 0) {
            psRemainingCapacityPercent = life / 100d;
        }
        List<String> acpiconf = Command.runNative("acpiconf -i 0");
        Map<String, String> psMap = new HashMap<>();
        for (String line : acpiconf) {
            String[] split = line.split(Symbol.COLON, 2);
            if (split.length > 1) {
                String value = split[1].trim();
                if (!value.isEmpty()) {
                    psMap.put(split[0], value);
                }
            }
        }

        String psDeviceName = psMap.getOrDefault("Model number", Builder.UNKNOWN);
        String psSerialNumber = psMap.getOrDefault("Serial number", Builder.UNKNOWN);
        String psChemistry = psMap.getOrDefault("Type", Builder.UNKNOWN);
        String psManufacturer = psMap.getOrDefault("OEM info", Builder.UNKNOWN);
        String cap = psMap.get("Design capacity");
        if (cap != null) {
            psDesignCapacity = Builder.getFirstIntValue(cap);
            if (cap.toLowerCase().contains("mah")) {
                psCapacityUnits = CapacityUnits.MAH;
            } else if (cap.toLowerCase().contains("mwh")) {
                psCapacityUnits = CapacityUnits.MWH;
            }
        }
        cap = psMap.get("Last full capacity");
        if (cap != null) {
            psMaxCapacity = Builder.getFirstIntValue(cap);
        } else {
            psMaxCapacity = psDesignCapacity;
        }
        double psTimeRemainingInstant = psTimeRemainingEstimated;
        String time = psMap.get("Remaining time");
        if (time != null) {
            String[] hhmm = time.split(Symbol.COLON);
            if (hhmm.length == 2) {
                psTimeRemainingInstant = 3600d * Builder.parseIntOrDefault(hhmm[0], 0)
                        + 60d * Builder.parseIntOrDefault(hhmm[1], 0);
            }
        }
        String rate = psMap.get("Present rate");
        if (rate != null) {
            psPowerUsageRate = Builder.getFirstIntValue(rate);
        }
        String volts = psMap.get("Present voltage");
        if (volts != null) {
            psVoltage = Builder.getFirstIntValue(volts);
            if (psVoltage != 0d) {
                psAmperage = psPowerUsageRate / psVoltage;
            }
        }

        return new FreeBsdPowerSource(psName, psDeviceName, psRemainingCapacityPercent, psTimeRemainingEstimated,
                psTimeRemainingInstant, psPowerUsageRate, psVoltage, psAmperage, psPowerOnLine, psCharging,
                psDischarging, psCapacityUnits, psCurrentCapacity, psMaxCapacity, psDesignCapacity, psCycleCount,
                psChemistry, psManufactureDate, psManufacturer, psSerialNumber, psTemperature);
    }

}
