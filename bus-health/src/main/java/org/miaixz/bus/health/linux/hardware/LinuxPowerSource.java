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
package org.miaixz.bus.health.linux.hardware;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.PowerSource;
import org.miaixz.bus.health.builtin.hardware.common.AbstractPowerSource;
import org.miaixz.bus.health.linux.SysPath;
import org.miaixz.bus.health.linux.software.LinuxOperatingSystem;

import com.sun.jna.platform.linux.Udev;
import com.sun.jna.platform.linux.Udev.UdevContext;
import com.sun.jna.platform.linux.Udev.UdevDevice;
import com.sun.jna.platform.linux.Udev.UdevEnumerate;
import com.sun.jna.platform.linux.Udev.UdevListEntry;

/**
 * A Power Source
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class LinuxPowerSource extends AbstractPowerSource {

    enum Prop {
        POWER_SUPPLY_NAME, POWER_SUPPLY_STATUS, POWER_SUPPLY_CAPACITY, POWER_SUPPLY_PRESENT, POWER_SUPPLY_ONLINE,
        POWER_SUPPLY_ENERGY_NOW, POWER_SUPPLY_CHARGE_NOW, POWER_SUPPLY_ENERGY_FULL, POWER_SUPPLY_CHARGE_FULL,
        POWER_SUPPLY_ENERGY_FULL_DESIGN, POWER_SUPPLY_CHARGE_FULL_DESIGN, POWER_SUPPLY_VOLTAGE_NOW,
        POWER_SUPPLY_POWER_NOW, POWER_SUPPLY_CURRENT_NOW, POWER_SUPPLY_CYCLE_COUNT, POWER_SUPPLY_TECHNOLOGY,
        POWER_SUPPLY_MODEL_NAME, POWER_SUPPLY_MANUFACTURER, POWER_SUPPLY_SERIAL_NUMBER, POWER_SUPPLY_TEMP,
        POWER_SUPPLY_TIME_TO_EMPTY_NOW, POWER_SUPPLY_TIME_TO_FULL_NOW, POWER_SUPPLY_MANUFACTURE_YEAR,
        POWER_SUPPLY_MANUFACTURE_MONTH, POWER_SUPPLY_MANUFACTURE_DAY
    }

    /**
     * Initialization-on-demand holder: the map is only constructed if the non-udev path is reached, avoiding
     * unnecessary work on systems where udev is available.
     */
    private static final class PropByName {

        static final Map<String, Prop> MAP = new HashMap<>();
        static {
            for (Prop p : Prop.values()) {
                MAP.put(p.name(), p);
            }
        }
    }

    public LinuxPowerSource(String psName, String psDeviceName, double psRemainingCapacityPercent,
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
    public static List<PowerSource> getPowerSources() {
        List<PowerSource> psList = new ArrayList<>();
        if (LinuxOperatingSystem.HAS_UDEV) {
            UdevContext udev = Udev.INSTANCE.udev_new();
            try {
                UdevEnumerate enumerate = udev.enumerateNew();
                try {
                    enumerate.addMatchSubsystem("power_supply");
                    enumerate.scanDevices();
                    for (UdevListEntry entry = enumerate.getListEntry(); entry != null; entry = entry.getNext()) {
                        String syspath = entry.getName();
                        String name = syspath.substring(syspath.lastIndexOf(File.separatorChar) + 1);
                        if (!name.startsWith("ADP") && !name.startsWith("AC") && !name.contains("USBC")) {
                            UdevDevice device = udev.deviceNewFromSyspath(syspath);
                            if (device != null) {
                                try {
                                    if (Parsing.parseIntOrDefault(
                                            device.getPropertyValue(Prop.POWER_SUPPLY_PRESENT.name()),
                                            1) > 0
                                            && Parsing.parseIntOrDefault(
                                                    device.getPropertyValue(Prop.POWER_SUPPLY_ONLINE.name()),
                                                    1) > 0) {
                                        Map<Prop, String> props = new EnumMap<>(Prop.class);
                                        for (Prop p : Prop.values()) {
                                            String val = device.getPropertyValue(p.name());
                                            if (val != null) {
                                                props.put(p, val);
                                            }
                                        }
                                        psList.add(buildPowerSource(name, props));
                                    }
                                } finally {
                                    device.unref();
                                }
                            }
                        }
                    }
                } finally {
                    enumerate.unref();
                }
            } finally {
                udev.unref();
            }
        } else {
            File psDir = new File(SysPath.POWER_SUPPLY);
            File[] psArr = psDir.listFiles();
            if (psArr == null) {
                return Collections.emptyList();
            }
            for (File ps : psArr) {
                String name = ps.getName();
                if (!name.startsWith("ADP") && !name.startsWith("AC") && !name.contains("USBC")) {
                    List<String> psInfo = Builder.readFile(SysPath.POWER_SUPPLY + "/" + name + "/uevent", false);
                    Map<Prop, String> props = new EnumMap<>(Prop.class);
                    for (String line : psInfo) {
                        String[] split = line.split("=", 2);
                        if (split.length > 1 && !split[1].isEmpty()) {
                            Prop p = PropByName.MAP.get(split[0]);
                            if (p != null) {
                                props.put(p, split[1]);
                            }
                        }
                    }
                    if (Parsing.parseIntOrDefault(props.get(Prop.POWER_SUPPLY_PRESENT), 1) > 0) {
                        psList.add(buildPowerSource(name, props));
                    }
                }
            }
        }
        return psList;
    }

    static LinuxPowerSource buildPowerSource(String name, Map<Prop, String> props) {
        String psName = props.getOrDefault(Prop.POWER_SUPPLY_NAME, name);
        String status = props.get(Prop.POWER_SUPPLY_STATUS);
        boolean psCharging = "Charging".equals(status);
        boolean psDischarging = "Discharging".equals(status);
        double psRemainingCapacityPercent = Parsing.parseIntOrDefault(props.get(Prop.POWER_SUPPLY_CAPACITY), -100)
                / 100d;

        // Debian/Ubuntu provides Energy (µWh). Fedora/RHEL provides Charge (µAh).
        // Parse raw values in micro-units first, fall back across Energy/Charge, then
        // divide by 1000 at the end to avoid integer division truncating the -1 sentinel.
        int rawNow = Parsing.parseIntOrDefault(props.get(Prop.POWER_SUPPLY_ENERGY_NOW), -1);
        CapacityUnits psCapacityUnits;
        if (rawNow >= 0) {
            psCapacityUnits = CapacityUnits.MWH;
        } else {
            rawNow = Parsing.parseIntOrDefault(props.get(Prop.POWER_SUPPLY_CHARGE_NOW), -1);
            psCapacityUnits = CapacityUnits.MAH;
        }
        int psCurrentCapacity = rawNow >= 0 ? rawNow / 1000 : -1;

        int rawFull = Parsing.parseIntOrDefault(props.get(Prop.POWER_SUPPLY_ENERGY_FULL), -1);
        if (rawFull < 0) {
            rawFull = Parsing.parseIntOrDefault(props.get(Prop.POWER_SUPPLY_CHARGE_FULL), -1);
        }
        int psMaxCapacity = rawFull >= 0 ? rawFull / 1000 : -1;

        int rawDesign = Parsing.parseIntOrDefault(props.get(Prop.POWER_SUPPLY_ENERGY_FULL_DESIGN), -1);
        if (rawDesign < 0) {
            rawDesign = Parsing.parseIntOrDefault(props.get(Prop.POWER_SUPPLY_CHARGE_FULL_DESIGN), -1);
        }
        int psDesignCapacity = rawDesign >= 0 ? rawDesign / 1000 : -1;

        // Debian/Ubuntu provides Voltage and Power. Fedora/RHEL provides Voltage and Current.
        double psVoltage = Parsing.parseDoubleOrDefault(props.get(Prop.POWER_SUPPLY_VOLTAGE_NOW), -1d) / 1_000_000d;
        double psPowerUsageRate = 0d;
        double psAmperage = 0d;
        // From Physics we know P = IV so I = P/V
        // Linux always reports POWER_NOW and CURRENT_NOW as non-negative; sign is
        // conveyed by STATUS. Negate when discharging to match the PowerSource contract
        // (positive = charging, negative = discharging).
        double sign = psDischarging ? -1d : 1d;
        if (psVoltage > 0) {
            double rawPower = Parsing.parseDoubleOrDefault(props.get(Prop.POWER_SUPPLY_POWER_NOW), -1d);
            double rawCurrent = Parsing.parseDoubleOrDefault(props.get(Prop.POWER_SUPPLY_CURRENT_NOW), -1d);
            if (rawPower >= 0) {
                psPowerUsageRate = sign * rawPower / 1000d;
            }
            if (rawCurrent >= 0) {
                psAmperage = sign * rawCurrent / 1000d;
            }
            if (rawPower < 0 && rawCurrent >= 0) {
                psPowerUsageRate = psAmperage * psVoltage;
            } else if (rawCurrent < 0 && rawPower >= 0) {
                psAmperage = psPowerUsageRate / psVoltage;
            }
        }

        int psCycleCount = Parsing.parseIntOrDefault(props.get(Prop.POWER_SUPPLY_CYCLE_COUNT), -1);
        String psChemistry = props.getOrDefault(Prop.POWER_SUPPLY_TECHNOLOGY, Normal.UNKNOWN);
        String psDeviceName = props.getOrDefault(Prop.POWER_SUPPLY_MODEL_NAME, Normal.UNKNOWN);
        String psManufacturer = props.getOrDefault(Prop.POWER_SUPPLY_MANUFACTURER, Normal.UNKNOWN);
        String psSerialNumber = props.getOrDefault(Prop.POWER_SUPPLY_SERIAL_NUMBER, Normal.UNKNOWN);

        // TEMP is in tenths of degrees Celsius
        int rawTemp = Parsing.parseIntOrDefault(props.get(Prop.POWER_SUPPLY_TEMP), -1);
        double psTemperature = rawTemp >= 0 ? rawTemp / 10d : 0d;

        // TIME_TO_EMPTY_NOW when discharging, TIME_TO_FULL_NOW when charging; both in seconds
        Prop timeKey = psCharging ? Prop.POWER_SUPPLY_TIME_TO_FULL_NOW : Prop.POWER_SUPPLY_TIME_TO_EMPTY_NOW;
        double psTimeRemainingInstant = Parsing.parseIntOrDefault(props.get(timeKey), -1);

        int year = Parsing.parseIntOrDefault(props.get(Prop.POWER_SUPPLY_MANUFACTURE_YEAR), -1);
        int month = Parsing.parseIntOrDefault(props.get(Prop.POWER_SUPPLY_MANUFACTURE_MONTH), -1);
        int day = Parsing.parseIntOrDefault(props.get(Prop.POWER_SUPPLY_MANUFACTURE_DAY), -1);
        LocalDate psManufactureDate = (year > 0 && month > 0 && day > 0) ? LocalDate.of(year, month, day) : null;

        return new LinuxPowerSource(psName, psDeviceName, psRemainingCapacityPercent, -1d, psTimeRemainingInstant,
                psPowerUsageRate, psVoltage, psAmperage, false, psCharging, psDischarging, psCapacityUnits,
                psCurrentCapacity, psMaxCapacity, psDesignCapacity, psCycleCount, psChemistry, psManufactureDate,
                psManufacturer, psSerialNumber, psTemperature);
    }

}
