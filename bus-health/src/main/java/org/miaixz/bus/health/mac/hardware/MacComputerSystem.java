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
package org.miaixz.bus.health.mac.hardware;

import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.Baseboard;
import org.miaixz.bus.health.builtin.hardware.Firmware;
import org.miaixz.bus.health.builtin.hardware.common.AbstractComputerSystem;

/**
 * <p>
 * MacComputerSystem class.
 * </p>
 * Hardware data obtained from ioreg.
 *
 * @author Kimi Liu
 */
@Immutable
final class MacComputerSystem extends AbstractComputerSystem {

    /**
     * The manufacturerModelSerialUUID value.
     */
    private final SupplierX<Tuple> manufacturerModelSerialUUID = Memoizer.memoize(MacComputerSystem::platformExpert);

    /**
     * Queries the I/O Registry for platform expert device information.
     *
     * @return A {@link Tuple} containing the manufacturer, model, serial number, and UUID of the computer system.
     *         Returns {@link Normal#UNKNOWN} for any field that cannot be determined.
     */
    private static Tuple platformExpert() {
        String manufacturer = null;
        String model = null;
        String serialNumber = null;
        String uuid = null;
        IORegistryEntry platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        if (platformExpert != null) {
            byte[] data = platformExpert.getByteArrayProperty("manufacturer");
            if (data != null) {
                manufacturer = Parsing.decodeNulTerminated(data, Charset.UTF_8);
            }
            data = platformExpert.getByteArrayProperty("model");
            if (data != null) {
                model = Parsing.decodeNulTerminated(data, Charset.UTF_8);
            }
            serialNumber = platformExpert.getStringProperty("IOPlatformSerialNumber");
            uuid = platformExpert.getStringProperty("IOPlatformUUID");
            platformExpert.release();
        }
        return new Tuple(StringKit.isBlank(manufacturer) ? "Apple Inc." : manufacturer,
                StringKit.isBlank(model) ? Normal.UNKNOWN : model,
                StringKit.isBlank(serialNumber) ? Normal.UNKNOWN : serialNumber,
                StringKit.isBlank(uuid) ? Normal.UNKNOWN : uuid);
    }

    /**
     * Get the computer system manufacturer.
     *
     * @return The manufacturer.
     */
    @Override
    public String getManufacturer() {
        return manufacturerModelSerialUUID.get().get(0);
    }

    /**
     * Get the computer system model.
     *
     * @return The model.
     */
    @Override
    public String getModel() {
        return manufacturerModelSerialUUID.get().get(1);
    }

    /**
     * Get the computer system serial number, if available.
     * <p>
     * Performs a best-effort attempt to retrieve a unique serial number from the computer system. This may originate
     * from the baseboard, BIOS, processor, etc.
     * <p>
     * This value is provided for information only. Caution should be exercised if using this result to "fingerprint" a
     * system for licensing or other purposes, as the result may change based on program permissions or installation of
     * software packages. Specifically, on Linux and FreeBSD, this requires either root permissions, or installation of
     * the (deprecated) HAL library (lshal command). Linux also attempts to read the dmi/id serial number files in
     * sysfs, which are read-only root by default but may have permissions altered by the user.
     *
     * @return the System Serial Number, if available, otherwise returns "unknown"
     */
    @Override
    public String getSerialNumber() {
        return manufacturerModelSerialUUID.get().get(2);
    }

    /**
     * Get the computer system hardware UUID, if available.
     * <p>
     * Performs a best-effort attempt to retrieve the hardware UUID.
     *
     * @return the Hardware UUID, if available, otherwise returns "unknown"
     */
    @Override
    public String getHardwareUUID() {
        return manufacturerModelSerialUUID.get().get(3);
    }

    /**
     * Instantiates the platform-specific {@link Firmware} object
     *
     * @return platform-specific {@link Firmware} object
     */
    @Override
    public Firmware createFirmware() {
        return new MacFirmware();
    }

    /**
     * Instantiates the platform-specific {@link Baseboard} object
     *
     * @return platform-specific {@link Baseboard} object
     */
    @Override
    public Baseboard createBaseboard() {
        return new MacBaseboard();
    }

}
