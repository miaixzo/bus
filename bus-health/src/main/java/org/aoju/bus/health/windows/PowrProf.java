/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.windows;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * 电源配置文件数据。这个类应该被认为是非api的，因为如
 * 果/当它的代码被合并到JNA项目中时，它可能会被删除
 *
 * @author Kimi Liu
 * @version 6.0.3
 * @since JDK 1.8+
 */
public interface PowrProf extends com.sun.jna.platform.win32.PowrProf {

    /**
     * 常量 <code>INSTANCE</code>
     */
    PowrProf INSTANCE = Native.load("PowrProf", PowrProf.class);

    enum BATTERY_QUERY_INFORMATION_LEVEL {
        BatteryInformation, BatteryGranularityInformation, BatteryTemperature, BatteryEstimatedTime, BatteryDeviceName,
        BatteryManufactureDate, BatteryManufactureName, BatteryUniqueID, BatterySerialNumber
    }

    /**
     * 包含有关系统电池当前状态的信息
     */
    @FieldOrder({"acOnLine", "batteryPresent", "charging", "discharging", "spare1", "tag", "maxCapacity",
            "remainingCapacity", "rate", "estimatedTime", "defaultAlert1", "defaultAlert2"})
    class SystemBatteryState extends Structure {
        public byte acOnLine;
        public byte batteryPresent;
        public byte charging;
        public byte discharging;
        public byte[] spare1 = new byte[3];
        public byte tag;
        public int maxCapacity;
        public int remainingCapacity;
        public int rate;
        public int estimatedTime;
        public int defaultAlert1;
        public int defaultAlert2;

        public SystemBatteryState(Pointer p) {
            super(p);
            read();
        }

        public SystemBatteryState() {
            super();
        }
    }

    /**
     * 包含有关处理器的信息
     */
    @FieldOrder({"number", "maxMhz", "currentMhz", "mhzLimit", "maxIdleState", "currentIdleState"})
    class ProcessorPowerInformation extends Structure {
        public int number;
        public int maxMhz;
        public int currentMhz;
        public int mhzLimit;
        public int maxIdleState;
        public int currentIdleState;

        public ProcessorPowerInformation(Pointer p) {
            super(p);
            read();
        }

        public ProcessorPowerInformation() {
            super();
        }
    }

    @FieldOrder({"BatteryTag", "InformationLevel", "AtRate"})
    class BATTERY_QUERY_INFORMATION extends Structure {
        public int BatteryTag;
        public int InformationLevel;
        public int AtRate;
    }

    @FieldOrder({"Capabilities", "Technology", "Reserved", "Chemistry", "DesignedCapacity", "FullChargedCapacity",
            "DefaultAlert1", "DefaultAlert2", "CriticalBias", "CycleCount"})
    class BATTERY_INFORMATION extends Structure {
        public int Capabilities;
        public byte Technology;
        public byte[] Reserved = new byte[3];
        public byte[] Chemistry = new byte[4];
        public int DesignedCapacity;
        public int FullChargedCapacity;
        public int DefaultAlert1;
        public int DefaultAlert2;
        public int CriticalBias;
        public int CycleCount;
    }

    @FieldOrder({"BatteryTag", "Timeout", "PowerState", "LowCapacity", "HighCapacity"})
    class BATTERY_WAIT_STATUS extends Structure {
        public int BatteryTag;
        public int Timeout;
        public int PowerState;
        public int LowCapacity;
        public int HighCapacity;
    }

    @FieldOrder({"PowerState", "Capacity", "Voltage", "Rate"})
    class BATTERY_STATUS extends Structure {
        public int PowerState;
        public int Capacity;
        public int Voltage;
        public int Rate;
    }

    @FieldOrder({"Day", "Month", "Year"})
    class BATTERY_MANUFACTURE_DATE extends Structure {
        public byte Day;
        public byte Month;
        public short Year;
    }

}
