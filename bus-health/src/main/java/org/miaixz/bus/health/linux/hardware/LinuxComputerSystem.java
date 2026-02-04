/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.linux.hardware;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.Baseboard;
import org.miaixz.bus.health.builtin.hardware.Firmware;
import org.miaixz.bus.health.builtin.hardware.common.AbstractComputerSystem;
import org.miaixz.bus.health.linux.driver.*;
import org.miaixz.bus.health.linux.driver.proc.CpuInfo;

/**
 * Hardware data obtained from sysfs.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class LinuxComputerSystem extends AbstractComputerSystem {

    private final Supplier<String> manufacturer = Memoizer.memoize(LinuxComputerSystem::queryManufacturer);

    private final Supplier<String> model = Memoizer.memoize(LinuxComputerSystem::queryModel);

    private final Supplier<String> serialNumber = Memoizer.memoize(LinuxComputerSystem::querySerialNumber);

    private final Supplier<String> uuid = Memoizer.memoize(LinuxComputerSystem::queryUUID);

    private static String queryModel() {
        String result;
        if ((result = Sysfs.queryProductModel()) == null && (result = Devicetree.queryModel()) == null
                && (result = Lshw.queryModel()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private static String queryManufacturer() {
        String result;
        if ((result = Sysfs.querySystemVendor()) == null && (result = CpuInfo.queryCpuManufacturer()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private static String querySerialNumber() {
        String result;
        if ((result = Sysfs.queryProductSerial()) == null && (result = Dmidecode.querySerialNumber()) == null
                && (result = Lshal.querySerialNumber()) == null && (result = Lshw.querySerialNumber()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private static String queryUUID() {
        String result;
        if ((result = Sysfs.queryUUID()) == null && (result = Dmidecode.queryUUID()) == null
                && (result = Lshal.queryUUID()) == null && (result = Lshw.queryUUID()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    @Override
    public String getManufacturer() {
        return manufacturer.get();
    }

    @Override
    public String getModel() {
        return model.get();
    }

    @Override
    public String getSerialNumber() {
        return serialNumber.get();
    }

    @Override
    public String getHardwareUUID() {
        return uuid.get();
    }

    @Override
    public Firmware createFirmware() {
        return new LinuxFirmware();
    }

    @Override
    public Baseboard createBaseboard() {
        return new LinuxBaseboard();
    }

}
