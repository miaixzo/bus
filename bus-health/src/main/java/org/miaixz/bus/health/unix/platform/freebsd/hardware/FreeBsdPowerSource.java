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
package org.miaixz.bus.health.unix.platform.freebsd.hardware;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.PowerSource;
import org.miaixz.bus.health.builtin.hardware.common.AbstractPowerSource;
import org.miaixz.bus.health.unix.platform.freebsd.BsdSysctlKit;

/**
 * A Power Source
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class FreeBsdPowerSource extends AbstractPowerSource {

    public FreeBsdPowerSource(String psName, String psDeviceName, double psRemainingCapacityPercent,
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
        return List.of(getPowerSource("BAT0"));
    }

    private static FreeBsdPowerSource getPowerSource(String name) {
        String psName = name;
        double psRemainingCapacityPercent = 1d;
        double psTimeRemainingEstimated = -1d; // -1 = unknown, -2 = unlimited
        double psPowerUsageRate = 0d;
        int psVoltage = -1;
        double psAmperage = 0d;
        boolean psPowerOnLine = false;
        boolean psCharging = false;
        boolean psDischarging = false;
        PowerSource.CapacityUnits psCapacityUnits = PowerSource.CapacityUnits.RELATIVE;
        int psCurrentCapacity = 0;
        int psMaxCapacity = 1;
        int psDesignCapacity = 1;
        int psCycleCount = -1;
        LocalDate psManufactureDate = null;

        double psTemperature = 0d;

        // state 0=full, 1=discharging, 2=charging
        int state = BsdSysctlKit.sysctl("hw.acpi.battery.state", 0);
        if (state == 2) {
            psCharging = true;
        } else {
            int time = BsdSysctlKit.sysctl("hw.acpi.battery.time", -1);
            // time is in minutes
            psTimeRemainingEstimated = time < 0 ? -1d : 60d * time;
            if (state == 1) {
                psDischarging = true;
            }
        }
        // life is in percent
        int life = BsdSysctlKit.sysctl("hw.acpi.battery.life", -1);
        if (life > 0) {
            psRemainingCapacityPercent = life / 100d;
        }
        List<String> acpiconf = Executor.runNative("acpiconf -i 0");
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

        String psDeviceName = psMap.getOrDefault("Model number", Normal.UNKNOWN);
        String psSerialNumber = psMap.getOrDefault("Serial number", Normal.UNKNOWN);
        String psChemistry = psMap.getOrDefault("Type", Normal.UNKNOWN);
        String psManufacturer = psMap.getOrDefault("OEM info", Normal.UNKNOWN);
        String cap = psMap.get("Design capacity");
        if (cap != null) {
            psDesignCapacity = Parsing.getFirstIntValue(cap);
            if (cap.toLowerCase(Locale.ROOT).contains("mah")) {
                psCapacityUnits = PowerSource.CapacityUnits.MAH;
            } else if (cap.toLowerCase(Locale.ROOT).contains("mwh")) {
                psCapacityUnits = PowerSource.CapacityUnits.MWH;
            }
        }
        cap = psMap.get("Last full capacity");
        if (cap != null) {
            psMaxCapacity = Parsing.getFirstIntValue(cap);
        } else {
            psMaxCapacity = psDesignCapacity;
        }
        double psTimeRemainingInstant = psTimeRemainingEstimated;
        String time = psMap.get("Remaining time");
        if (time != null) {
            String[] hhmm = time.split(Symbol.COLON);
            if (hhmm.length == 2) {
                psTimeRemainingInstant = 3600d * Parsing.parseIntOrDefault(hhmm[0], 0)
                        + 60d * Parsing.parseIntOrDefault(hhmm[1], 0);
            }
        }
        String rate = psMap.get("Present rate");
        if (rate != null) {
            psPowerUsageRate = Parsing.getFirstIntValue(rate);
        }
        String volts = psMap.get("Present voltage");
        if (volts != null) {
            psVoltage = Parsing.getFirstIntValue(volts);
            if (psVoltage != 0) {
                psAmperage = psPowerUsageRate / psVoltage;
            }
        }

        return new FreeBsdPowerSource(psName, psDeviceName, psRemainingCapacityPercent, psTimeRemainingEstimated,
                psTimeRemainingInstant, psPowerUsageRate, psVoltage, psAmperage, psPowerOnLine, psCharging,
                psDischarging, psCapacityUnits, psCurrentCapacity, psMaxCapacity, psDesignCapacity, psCycleCount,
                psChemistry, psManufactureDate, psManufacturer, psSerialNumber, psTemperature);
    }

}
