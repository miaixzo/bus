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
package org.miaixz.bus.health.mac.hardware;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.Baseboard;
import org.miaixz.bus.health.builtin.hardware.Firmware;
import org.miaixz.bus.health.builtin.hardware.common.AbstractComputerSystem;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;

/**
 * <p>
 * MacComputerSystem class.
 * </p>
 * Hardware data obtained from ioreg.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
final class MacComputerSystem extends AbstractComputerSystem {

    private final Supplier<Tuple> manufacturerModelSerialUUID = Memoizer.memoize(MacComputerSystem::platformExpert);

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
                manufacturer = Native.toString(data, Charset.UTF_8);
            }
            data = platformExpert.getByteArrayProperty("model");
            if (data != null) {
                model = Native.toString(data, Charset.UTF_8);
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
     * Description inherited from parent class or interface.
     */
    @Override
    public String getManufacturer() {
        return manufacturerModelSerialUUID.get().get(0);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String getModel() {
        return manufacturerModelSerialUUID.get().get(1);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String getSerialNumber() {
        return manufacturerModelSerialUUID.get().get(2);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String getHardwareUUID() {
        return manufacturerModelSerialUUID.get().get(3);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public Firmware createFirmware() {
        return new MacFirmware();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public Baseboard createBaseboard() {
        return new MacBaseboard();
    }

}
