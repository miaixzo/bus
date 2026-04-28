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
package org.miaixz.bus.health.builtin.hardware.common;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.hardware.PowerSource;

/**
 * A Power Source
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public abstract class AbstractPowerSource implements PowerSource {

    /**
     * The name value.
     */
    private String name;
    /**
     * The deviceName value.
     */
    private String deviceName;
    /**
     * The remainingCapacityPercent value.
     */
    private double remainingCapacityPercent;
    /**
     * The timeRemainingEstimated value.
     */
    private double timeRemainingEstimated;
    /**
     * The timeRemainingInstant value.
     */
    private double timeRemainingInstant;
    /**
     * The powerUsageRate value.
     */
    private double powerUsageRate;
    /**
     * The voltage value.
     */
    private double voltage;
    /**
     * The amperage value.
     */
    private double amperage;
    /**
     * The powerOnLine value.
     */
    private boolean powerOnLine;
    /**
     * The charging value.
     */
    private boolean charging;
    /**
     * The discharging value.
     */
    private boolean discharging;
    /**
     * The capacityUnits value.
     */
    private CapacityUnits capacityUnits;
    /**
     * The currentCapacity value.
     */
    private int currentCapacity;
    /**
     * The maxCapacity value.
     */
    private int maxCapacity;
    /**
     * The designCapacity value.
     */
    private int designCapacity;
    /**
     * The cycleCount value.
     */
    private int cycleCount;
    /**
     * The chemistry value.
     */
    private String chemistry;
    /**
     * The manufactureDate value.
     */
    private LocalDate manufactureDate;
    /**
     * The manufacturer value.
     */
    private String manufacturer;
    /**
     * The serialNumber value.
     */
    private String serialNumber;
    /**
     * The temperature value.
     */
    private double temperature;

    /**
     * Creates a new AbstractPowerSource instance.
     *
     * @param name                     the name
     * @param deviceName               the device name
     * @param remainingCapacityPercent the remaining capacity percent
     * @param timeRemainingEstimated   the time remaining estimated
     * @param timeRemainingInstant     the time remaining instant
     * @param powerUsageRate           the power usage rate
     * @param voltage                  the voltage
     * @param amperage                 the amperage
     * @param powerOnLine              the power on line
     * @param charging                 the charging
     * @param discharging              the discharging
     * @param capacityUnits            the capacity units
     * @param currentCapacity          the current capacity
     * @param maxCapacity              the max capacity
     * @param designCapacity           the design capacity
     * @param cycleCount               the cycle count
     * @param chemistry                the chemistry
     * @param manufactureDate          the manufacture date
     * @param manufacturer             the manufacturer
     * @param serialNumber             the serial number
     * @param temperature              the temperature
     */
    protected AbstractPowerSource(String name, String deviceName, double remainingCapacityPercent,
            double timeRemainingEstimated, double timeRemainingInstant, double powerUsageRate, double voltage,
            double amperage, boolean powerOnLine, boolean charging, boolean discharging, CapacityUnits capacityUnits,
            int currentCapacity, int maxCapacity, int designCapacity, int cycleCount, String chemistry,
            LocalDate manufactureDate, String manufacturer, String serialNumber, double temperature) {
        super();
        this.name = name;
        this.deviceName = deviceName;
        this.remainingCapacityPercent = remainingCapacityPercent;
        this.timeRemainingEstimated = timeRemainingEstimated;
        this.timeRemainingInstant = timeRemainingInstant;
        this.powerUsageRate = powerUsageRate;
        this.voltage = voltage;
        this.amperage = amperage;
        this.powerOnLine = powerOnLine;
        this.charging = charging;
        this.discharging = discharging;
        this.capacityUnits = capacityUnits;
        this.currentCapacity = currentCapacity;
        this.maxCapacity = maxCapacity;
        this.designCapacity = designCapacity;
        this.cycleCount = cycleCount;
        this.chemistry = chemistry;
        this.manufactureDate = manufactureDate;
        this.manufacturer = manufacturer;
        this.serialNumber = serialNumber;
        this.temperature = temperature;
    }

    /**
     * Estimated time remaining on power source, formatted as HH:mm
     *
     * @param timeInSeconds The time remaining, in seconds
     * @return formatted String of time remaining
     */
    private static String formatTimeRemaining(double timeInSeconds) {
        String formattedTimeRemaining;
        if (timeInSeconds < -1.5) {
            formattedTimeRemaining = "Charging";
        } else if (timeInSeconds < 0) {
            formattedTimeRemaining = "Unknown";
        } else {
            int hours = (int) (timeInSeconds / 3600);
            int minutes = (int) (timeInSeconds % 3600 / 60);
            formattedTimeRemaining = String.format(Locale.ROOT, "%d:%02d", hours, minutes);
        }
        return formattedTimeRemaining;
    }

    /**
     * Returns the name.
     *
     * @return the get name result
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the device name.
     *
     * @return the get device name result
     */
    @Override
    public String getDeviceName() {
        return this.deviceName;
    }

    /**
     * Returns the remaining capacity percent.
     *
     * @return the get remaining capacity percent result
     */
    @Override
    public double getRemainingCapacityPercent() {
        return this.remainingCapacityPercent;
    }

    /**
     * Returns the time remaining estimated.
     *
     * @return the get time remaining estimated result
     */
    @Override
    public double getTimeRemainingEstimated() {
        return this.timeRemainingEstimated;
    }

    /**
     * Returns the time remaining instant.
     *
     * @return the get time remaining instant result
     */
    @Override
    public double getTimeRemainingInstant() {
        return this.timeRemainingInstant;
    }

    /**
     * Returns the power usage rate.
     *
     * @return the get power usage rate result
     */
    @Override
    public double getPowerUsageRate() {
        return this.powerUsageRate;
    }

    /**
     * Returns the voltage.
     *
     * @return the get voltage result
     */
    @Override
    public double getVoltage() {
        return this.voltage;
    }

    /**
     * Returns the amperage.
     *
     * @return the get amperage result
     */
    @Override
    public double getAmperage() {
        return this.amperage;
    }

    /**
     * Returns whether the power on line condition is true.
     *
     * @return the is power on line result
     */
    @Override
    public boolean isPowerOnLine() {
        return this.powerOnLine;
    }

    /**
     * Returns whether the charging condition is true.
     *
     * @return the is charging result
     */
    @Override
    public boolean isCharging() {
        return this.charging;
    }

    /**
     * Returns whether the discharging condition is true.
     *
     * @return the is discharging result
     */
    @Override
    public boolean isDischarging() {
        return this.discharging;
    }

    /**
     * Returns the capacity units.
     *
     * @return the get capacity units result
     */
    @Override
    public CapacityUnits getCapacityUnits() {
        return this.capacityUnits;
    }

    /**
     * Returns the current capacity.
     *
     * @return the get current capacity result
     */
    @Override
    public int getCurrentCapacity() {
        return this.currentCapacity;
    }

    /**
     * Returns the max capacity.
     *
     * @return the get max capacity result
     */
    @Override
    public int getMaxCapacity() {
        return this.maxCapacity;
    }

    /**
     * Returns the design capacity.
     *
     * @return the get design capacity result
     */
    @Override
    public int getDesignCapacity() {
        return this.designCapacity;
    }

    /**
     * Returns the cycle count.
     *
     * @return the get cycle count result
     */
    @Override
    public int getCycleCount() {
        return this.cycleCount;
    }

    /**
     * Returns the chemistry.
     *
     * @return the get chemistry result
     */
    @Override
    public String getChemistry() {
        return this.chemistry;
    }

    /**
     * Returns the manufacture date.
     *
     * @return the get manufacture date result
     */
    @Override
    public LocalDate getManufactureDate() {
        return this.manufactureDate;
    }

    /**
     * Returns the manufacturer.
     *
     * @return the get manufacturer result
     */
    @Override
    public String getManufacturer() {
        return this.manufacturer;
    }

    /**
     * Returns the serial number.
     *
     * @return the get serial number result
     */
    @Override
    public String getSerialNumber() {
        return this.serialNumber;
    }

    /**
     * Returns the temperature.
     *
     * @return the get temperature result
     */
    @Override
    public double getTemperature() {
        return this.temperature;
    }

    /**
     * Returns a fresh list of power sources for this platform, used by {@link #updateAttributes()}.
     *
     * @return A list of PowerSource objects representing batteries, etc.
     */
    protected abstract List<PowerSource> queryPowerSources();

    /**
     * Updates the attributes.
     *
     * @return the update attributes result
     */
    @Override
    public boolean updateAttributes() {
        List<PowerSource> psArr = queryPowerSources();
        for (PowerSource ps : psArr) {
            if (ps.getName().equals(this.name)) {
                this.name = ps.getName();
                this.deviceName = ps.getDeviceName();
                this.remainingCapacityPercent = ps.getRemainingCapacityPercent();
                this.timeRemainingEstimated = ps.getTimeRemainingEstimated();
                this.timeRemainingInstant = ps.getTimeRemainingInstant();
                this.powerUsageRate = ps.getPowerUsageRate();
                this.voltage = ps.getVoltage();
                this.amperage = ps.getAmperage();
                this.powerOnLine = ps.isPowerOnLine();
                this.charging = ps.isCharging();
                this.discharging = ps.isDischarging();
                this.capacityUnits = ps.getCapacityUnits();
                this.currentCapacity = ps.getCurrentCapacity();
                this.maxCapacity = ps.getMaxCapacity();
                this.designCapacity = ps.getDesignCapacity();
                this.cycleCount = ps.getCycleCount();
                this.chemistry = ps.getChemistry();
                this.manufactureDate = ps.getManufactureDate();
                this.manufacturer = ps.getManufacturer();
                this.serialNumber = ps.getSerialNumber();
                this.temperature = ps.getTemperature();
                return true;
            }
        }
        // Didn't find this battery
        return false;
    }

    /**
     * Returns the to string result.
     *
     * @return the to string result
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(getName()).append(", ");
        sb.append("Device Name: ").append(getDeviceName()).append(",\n ");
        sb.append("RemainingCapacityPercent: ").append(getRemainingCapacityPercent() * 100).append("%, ");
        sb.append("Time Remaining: ").append(formatTimeRemaining(getTimeRemainingEstimated())).append(", ");
        sb.append("Time Remaining Instant: ").append(formatTimeRemaining(getTimeRemainingInstant())).append(",\n ");
        sb.append("Power Usage Rate: ").append(getPowerUsageRate()).append("mW, ");
        sb.append("Voltage: ");
        if (getVoltage() > 0) {
            sb.append(getVoltage()).append("V, ");
        } else {
            sb.append(Normal.UNKNOWN).append(", ");
        }
        sb.append("Amperage: ").append(getAmperage()).append("mA,\n ");
        sb.append("Power OnLine: ").append(isPowerOnLine()).append(", ");
        sb.append("Charging: ").append(isCharging()).append(", ");
        sb.append("Discharging: ").append(isDischarging()).append(",\n ");
        sb.append("Capacity Units: ").append(getCapacityUnits()).append(", ");
        sb.append("Current Capacity: ").append(getCurrentCapacity()).append(", ");
        sb.append("Max Capacity: ").append(getMaxCapacity()).append(", ");
        sb.append("Design Capacity: ").append(getDesignCapacity()).append(",\n ");
        sb.append("Cycle Count: ").append(getCycleCount()).append(", ");
        sb.append("Chemistry: ").append(getChemistry()).append(", ");
        sb.append("Manufacture Date: ").append(getManufactureDate() != null ? getManufactureDate() : Normal.UNKNOWN)
                .append(", ");
        sb.append("Manufacturer: ").append(getManufacturer()).append(",\n ");
        sb.append("SerialNumber: ").append(getSerialNumber()).append(", ");
        sb.append("Temperature: ");
        if (getTemperature() > 0) {
            sb.append(getTemperature()).append("°C");
        } else {
            sb.append(Normal.UNKNOWN);
        }
        return sb.toString();
    }

}
