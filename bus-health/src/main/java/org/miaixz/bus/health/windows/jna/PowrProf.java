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
package org.miaixz.bus.health.windows.jna;

import org.miaixz.bus.health.Builder;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Power profile stats. This class should be considered non-API as it may be removed if/when its code is incorporated
 * into the JNA project.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface PowrProf extends com.sun.jna.platform.win32.PowrProf {

    /**
     * Constant <code>INSTANCE</code>
     */
    PowrProf INSTANCE = Native.load("PowrProf", PowrProf.class);

    /**
     * The BATTERY_QUERY_INFORMATION_LEVEL enum.
     */
    enum BATTERY_QUERY_INFORMATION_LEVEL {
        BatteryInformation, BatteryGranularityInformation, BatteryTemperature, BatteryEstimatedTime, BatteryDeviceName,
        BatteryManufactureDate, BatteryManufactureName, BatteryUniqueID, BatterySerialNumber
    }

    /**
     * Contains information about the current state of the system battery.
     */
    @FieldOrder({ "acOnLine", "batteryPresent", "charging", "discharging", "spare1", "tag", "maxCapacity",
            "remainingCapacity", "rate", "estimatedTime", "defaultAlert1", "defaultAlert2" })
    class SystemBatteryState extends Structure implements AutoCloseable {

        /**
         * The acOnLine value.
         */
        public byte acOnLine;
        /**
         * The batteryPresent value.
         */
        public byte batteryPresent;
        /**
         * The charging value.
         */
        public byte charging;
        /**
         * The discharging value.
         */
        public byte discharging;
        /**
         * The spare1 value.
         */
        public byte[] spare1 = new byte[3];
        /**
         * The tag value.
         */
        public byte tag;
        /**
         * The maxCapacity value.
         */
        public int maxCapacity;
        /**
         * The remainingCapacity value.
         */
        public int remainingCapacity;
        /**
         * The rate value.
         */
        public int rate;
        /**
         * The estimatedTime value.
         */
        public int estimatedTime;
        /**
         * The defaultAlert1 value.
         */
        public int defaultAlert1;
        /**
         * The defaultAlert2 value.
         */
        public int defaultAlert2;

        /**
         * Creates a new SystemBatteryState instance.
         *
         * @param p the p
         */
        public SystemBatteryState(Pointer p) {
            super(p);
            read();
        }

        /**
         * Creates a new SystemBatteryState instance.
         */
        public SystemBatteryState() {
            super();
        }

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    /**
     * Contains information about a processor.
     */
    @FieldOrder({ "number", "maxMhz", "currentMhz", "mhzLimit", "maxIdleState", "currentIdleState" })
    class ProcessorPowerInformation extends Structure {

        /**
         * The number value.
         */
        public int number;
        /**
         * The maxMhz value.
         */
        public int maxMhz;
        /**
         * The currentMhz value.
         */
        public int currentMhz;
        /**
         * The mhzLimit value.
         */
        public int mhzLimit;
        /**
         * The maxIdleState value.
         */
        public int maxIdleState;
        /**
         * The currentIdleState value.
         */
        public int currentIdleState;

        /**
         * Creates a new ProcessorPowerInformation instance.
         *
         * @param p the p
         */
        public ProcessorPowerInformation(Pointer p) {
            super(p);
            read();
        }

        /**
         * Creates a new ProcessorPowerInformation instance.
         */
        public ProcessorPowerInformation() {
            super();
        }
    }

    /**
     * JNA wrapper for the BATTERY_QUERY_INFORMATION structure.
     * <p>
     * This class maps to the native Windows structure used to query battery information.
     * </p>
     */
    // MOVE?
    /**
     * The BATTERY_QUERY_INFORMATION class.
     */
    @FieldOrder({ "BatteryTag", "InformationLevel", "AtRate" })
    class BATTERY_QUERY_INFORMATION extends Structure implements AutoCloseable {

        /**
         * The BatteryTag value.
         */
        public int BatteryTag;
        /**
         * The InformationLevel value.
         */
        public int InformationLevel;
        /**
         * The AtRate value.
         */
        public int AtRate;

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    /**
     * JNA wrapper for the BATTERY_INFORMATION structure.
     * <p>
     * This class maps to the native Windows structure: {@code
     * typedef struct _BATTERY_INFORMATION {
     *     ULONG Capabilities;
     *     BYTE Technology;
     *     BYTE Reserved[3];
     *     CHAR Chemistry[4];
     *     ULONG DesignedCapacity; ULONG FullChargedCapacity; ULONG DefaultAlert1; ULONG DefaultAlert2; ULONG
     * CriticalBias; ULONG CycleCount; } BATTERY_INFORMATION; }
     * </p>
     */
    @FieldOrder({ "Capabilities", "Technology", "Reserved", "Chemistry", "DesignedCapacity", "FullChargedCapacity",
            "DefaultAlert1", "DefaultAlert2", "CriticalBias", "CycleCount" })
    class BATTERY_INFORMATION extends Structure implements AutoCloseable {

        /**
         * The Capabilities value.
         */
        public int Capabilities;
        /**
         * The Technology value.
         */
        public byte Technology;
        /**
         * The Reserved value.
         */
        public byte[] Reserved = new byte[3];
        /**
         * The Chemistry value.
         */
        public byte[] Chemistry = new byte[4];
        /**
         * The DesignedCapacity value.
         */
        public int DesignedCapacity;
        /**
         * The FullChargedCapacity value.
         */
        public int FullChargedCapacity;
        /**
         * The DefaultAlert1 value.
         */
        public int DefaultAlert1;
        /**
         * The DefaultAlert2 value.
         */
        public int DefaultAlert2;
        /**
         * The CriticalBias value.
         */
        public int CriticalBias;
        /**
         * The CycleCount value.
         */
        public int CycleCount;

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    /**
     * JNA wrapper for the BATTERY_WAIT_STATUS structure.
     * <p>
     * This class maps to the native Windows structure used to wait for battery status changes.
     * </p>
     */
    @FieldOrder({ "BatteryTag", "Timeout", "PowerState", "LowCapacity", "HighCapacity" })
    class BATTERY_WAIT_STATUS extends Structure implements AutoCloseable {

        /**
         * The BatteryTag value.
         */
        public int BatteryTag;
        /**
         * The Timeout value.
         */
        public int Timeout;
        /**
         * The PowerState value.
         */
        public int PowerState;
        /**
         * The LowCapacity value.
         */
        public int LowCapacity;
        /**
         * The HighCapacity value.
         */
        public int HighCapacity;

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    /**
     * JNA wrapper for the BATTERY_STATUS structure.
     * <p>
     * This class maps to the native Windows structure: {@code typedef struct _BATTERY_STATUS { ULONG PowerState; ULONG
     * Capacity; ULONG Voltage; ULONG Rate; } BATTERY_STATUS; }
     * </p>
     */
    @FieldOrder({ "PowerState", "Capacity", "Voltage", "Rate" })
    class BATTERY_STATUS extends Structure implements AutoCloseable {

        /**
         * The PowerState value.
         */
        public int PowerState;
        /**
         * The Capacity value.
         */
        public int Capacity;
        /**
         * The Voltage value.
         */
        public int Voltage;
        /**
         * The Rate value.
         */
        public int Rate;

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

    /**
     * JNA wrapper for the BATTERY_MANUFACTURE_DATE structure.
     * <p>
     * This class maps to the native Windows structure: {@code typedef struct _BATTERY_MANUFACTURE_DATE { UCHAR Day;
     * UCHAR Month; USHORT Year; } BATTERY_MANUFACTURE_DATE; }
     * </p>
     */
    @FieldOrder({ "Day", "Month", "Year" })
    class BATTERY_MANUFACTURE_DATE extends Structure implements AutoCloseable {

        /**
         * The Day value.
         */
        public byte Day;
        /**
         * The Month value.
         */
        public byte Month;
        /**
         * The Year value.
         */
        public short Year;

        /**
         * Closes this resource.
         */
        @Override
        public void close() {
            Builder.freeMemory(getPointer());
        }
    }

}
