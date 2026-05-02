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
 * @since Java 21+
 */
@Immutable
final class LinuxComputerSystem extends AbstractComputerSystem {

    /**
     * The manufacturer value.
     */
    private final Supplier<String> manufacturer = Memoizer.memoize(LinuxComputerSystem::queryManufacturer);

    /**
     * The model value.
     */
    private final Supplier<String> model = Memoizer.memoize(LinuxComputerSystem::queryModel);

    /**
     * The serialNumber value.
     */
    private final Supplier<String> serialNumber = Memoizer.memoize(LinuxComputerSystem::querySerialNumber);

    /**
     * The uuid value.
     */
    private final Supplier<String> uuid = Memoizer.memoize(LinuxComputerSystem::queryUUID);

    /**
     * Queries the model.
     *
     * @return the query model result
     */
    private static String queryModel() {
        String result;
        if ((result = Sysfs.queryProductModel()) == null && (result = Devicetree.queryModel()) == null
                && (result = Lshw.queryModel()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    /**
     * Queries the manufacturer.
     *
     * @return the query manufacturer result
     */
    private static String queryManufacturer() {
        String result;
        if ((result = Sysfs.querySystemVendor()) == null && (result = CpuInfo.queryCpuManufacturer()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    /**
     * Queries the serial number.
     *
     * @return the query serial number result
     */
    private static String querySerialNumber() {
        String result;
        if ((result = Sysfs.queryProductSerial()) == null && (result = Dmidecode.querySerialNumber()) == null
                && (result = Lshal.querySerialNumber()) == null && (result = Lshw.querySerialNumber()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    /**
     * Queries the uuid.
     *
     * @return the query uuid result
     */
    private static String queryUUID() {
        String result;
        if ((result = Sysfs.queryUUID()) == null && (result = Dmidecode.queryUUID()) == null
                && (result = Lshal.queryUUID()) == null && (result = Lshw.queryUUID()) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    /**
     * Returns the manufacturer.
     *
     * @return the get manufacturer result
     */
    @Override
    public String getManufacturer() {
        return manufacturer.get();
    }

    /**
     * Returns the model.
     *
     * @return the get model result
     */
    @Override
    public String getModel() {
        return model.get();
    }

    /**
     * Returns the serial number.
     *
     * @return the get serial number result
     */
    @Override
    public String getSerialNumber() {
        return serialNumber.get();
    }

    /**
     * Returns the hardware uuid.
     *
     * @return the get hardware uuid result
     */
    @Override
    public String getHardwareUUID() {
        return uuid.get();
    }

    /**
     * Creates the firmware.
     *
     * @return the create firmware result
     */
    @Override
    public Firmware createFirmware() {
        return new LinuxFirmware();
    }

    /**
     * Creates the baseboard.
     *
     * @return the create baseboard result
     */
    @Override
    public Baseboard createBaseboard() {
        return new LinuxBaseboard();
    }

}
