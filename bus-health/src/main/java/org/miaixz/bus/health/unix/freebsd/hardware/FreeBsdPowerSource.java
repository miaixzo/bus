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
package org.miaixz.bus.health.unix.freebsd.hardware;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.PowerSource;
import org.miaixz.bus.health.builtin.hardware.common.AbstractPowerSource;
import org.miaixz.bus.health.unix.freebsd.BsdSysctlKit;

/**
 * A Power Source
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class FreeBsdPowerSource extends AbstractPowerSource {

    /**
     * Creates a new FreeBsdPowerSource instance.
     *
     * @param psName                     the ps name
     * @param psDeviceName               the ps device name
     * @param psRemainingCapacityPercent the ps remaining capacity percent
     * @param psTimeRemainingEstimated   the ps time remaining estimated
     * @param psTimeRemainingInstant     the ps time remaining instant
     * @param psPowerUsageRate           the ps power usage rate
     * @param psVoltage                  the ps voltage
     * @param psAmperage                 the ps amperage
     * @param psPowerOnLine              the ps power on line
     * @param psCharging                 the ps charging
     * @param psDischarging              the ps discharging
     * @param psCapacityUnits            the ps capacity units
     * @param psCurrentCapacity          the ps current capacity
     * @param psMaxCapacity              the ps max capacity
     * @param psDesignCapacity           the ps design capacity
     * @param psCycleCount               the ps cycle count
     * @param psChemistry                the ps chemistry
     * @param psManufactureDate          the ps manufacture date
     * @param psManufacturer             the ps manufacturer
     * @param psSerialNumber             the ps serial number
     * @param psTemperature              the ps temperature
     */
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
        int units = BsdSysctlKit.sysctl("hw.acpi.battery.units", Normal._0);
        if (units <= Normal._0) {
            return Collections.emptyList();
        }
        boolean powerOnline = BsdSysctlKit.sysctl("hw.acpi.acline", Normal._0) == Normal._1;
        int state = Normal.__1;
        int time = Normal.__1;
        int life = Normal.__1;
        if (units == Normal._1) {
            state = BsdSysctlKit.sysctl("hw.acpi.battery.state", Normal.__1);
            time = BsdSysctlKit.sysctl("hw.acpi.battery.time", Normal.__1);
            life = BsdSysctlKit.sysctl("hw.acpi.battery.life", Normal.__1);
        }
        List<PowerSource> batteries = new ArrayList<>();
        for (int unit = Normal._0; unit < units; unit++) {
            FreeBsdPowerSource battery = parsePowerSource(
                    "BAT" + unit,
                    Executor.runNative("acpiconf -i " + unit),
                    powerOnline,
                    state,
                    time,
                    life);
            if (battery != null) {
                batteries.add(battery);
            }
        }
        return Collections.unmodifiableList(batteries);
    }

    /**
     * Parses one battery from {@code acpiconf -i} output.
     *
     * @param name        the battery name
     * @param lines       the command output
     * @param powerOnline whether external power is connected
     * @param state       the aggregate battery state bits
     * @param time        the aggregate time remaining in minutes
     * @param life        the aggregate remaining percentage
     * @return the battery, or {@code null} when it is not present
     */
    private static FreeBsdPowerSource parsePowerSource(
            String name,
            List<String> lines,
            boolean powerOnline,
            int state,
            int time,
            int life) {
        Map<String, String> psMap = new HashMap<>();
        for (String line : lines) {
            String[] split = line.split(Symbol.COLON, Normal._2);
            if (split.length > Normal._1) {
                String value = split[1].trim();
                if (!value.isEmpty() && !"unknown".equals(value)) {
                    psMap.put(split[0].trim(), value);
                }
            }
        }
        boolean charging = state > Normal._0 && (state & Normal._2) != Normal._0;
        boolean discharging = state > Normal._0 && (state & Normal._1) != Normal._0;
        String reportedState = psMap.get("State");
        if (reportedState != null) {
            if ("not present".equals(reportedState)) {
                return null;
            }
            discharging = reportedState.contains("discharging");
            charging = !discharging && reportedState.contains("charging");
        }

        double remainingPercent = 1D;
        String reportedPercent = psMap.get("Remaining capacity");
        if (reportedPercent != null) {
            remainingPercent = Parsing.getFirstIntValue(reportedPercent) / 100D;
        } else if (life >= Normal._0) {
            remainingPercent = life / 100D;
        }

        double instantTime = -1D;
        String reportedTime = psMap.get("Remaining time");
        if (reportedTime != null) {
            String[] hhmm = reportedTime.split(Symbol.COLON);
            if (hhmm.length == Normal._2) {
                instantTime = 3600D * Parsing.parseIntOrDefault(hhmm[Normal._0], Normal._0)
                        + 60D * Parsing.parseIntOrDefault(hhmm[Normal._1], Normal._0);
            }
        }
        double estimatedTime = time >= Normal._0 ? 60D * time : instantTime;
        if (instantTime < Normal._0) {
            instantTime = estimatedTime;
        }

        double powerRate = 0D;
        double amperage = 0D;
        String rate = psMap.get("Present rate");
        if (rate != null) {
            if (rate.contains("mA")) {
                amperage = Parsing.getFirstIntValue(rate);
                int parenthesis = rate.indexOf(Symbol.C_PARENTHESE_LEFT);
                if (parenthesis >= Normal._0) {
                    powerRate = Parsing.getFirstIntValue(rate.substring(parenthesis));
                }
            } else {
                powerRate = Parsing.getFirstIntValue(rate);
            }
        }
        double voltage = -1D;
        String volts = psMap.get("Present voltage");
        if (volts != null) {
            int millivolts = Parsing.getFirstIntValue(volts);
            if (millivolts > Normal._0) {
                voltage = millivolts / 1000D;
                if (amperage == 0D) {
                    amperage = powerRate / voltage;
                }
            }
        }
        if (discharging) {
            powerRate = -powerRate;
            amperage = -amperage;
        }

        PowerSource.CapacityUnits capacityUnits = PowerSource.CapacityUnits.RELATIVE;
        int designCapacity = Normal._1;
        int maxCapacity = Normal._1;
        String design = psMap.get("Design capacity");
        String lastFull = psMap.get("Last full capacity");
        if (design != null) {
            String units = StringKit.toLowerCase(design);
            if (units.endsWith("mah")) {
                capacityUnits = PowerSource.CapacityUnits.MAH;
            } else if (units.endsWith("mwh")) {
                capacityUnits = PowerSource.CapacityUnits.MWH;
            }
            if (capacityUnits != PowerSource.CapacityUnits.RELATIVE) {
                designCapacity = Parsing.getFirstIntValue(design);
                maxCapacity = lastFull == null ? designCapacity : Parsing.getFirstIntValue(lastFull);
            }
        }
        int currentCapacity = (int) Math.round(remainingPercent * maxCapacity);
        int cycleCount = Parsing.parseIntOrDefault(psMap.getOrDefault("Cycle Count", Normal.EMPTY), Normal.__1);

        return new FreeBsdPowerSource(name, psMap.getOrDefault("Model number", Normal.UNKNOWN), remainingPercent,
                estimatedTime, instantTime, powerRate, voltage, amperage, powerOnline, charging, discharging,
                capacityUnits, currentCapacity, maxCapacity, designCapacity, cycleCount,
                psMap.getOrDefault("Type", Normal.UNKNOWN), null, psMap.getOrDefault("OEM info", Normal.UNKNOWN),
                psMap.getOrDefault("Serial number", Normal.UNKNOWN), 0D);
    }

    /**
     * Queries the power sources.
     *
     * @return the query power sources result
     */
    @Override
    protected List<PowerSource> queryPowerSources() {
        return getPowerSources();
    }

}
