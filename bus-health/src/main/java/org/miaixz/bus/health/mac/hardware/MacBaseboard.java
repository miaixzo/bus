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
package org.miaixz.bus.health.mac.hardware;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractBaseboard;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;

/**
 * Baseboard data obtained from ioreg.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class MacBaseboard extends AbstractBaseboard {

    private final Supplier<Tuple> manufModelVersSerial = Memoizer.memoize(MacBaseboard::queryPlatform);

    /**
     * Queries the platform information from the I/O Registry to populate baseboard details.
     *
     * @return A {@link Tuple} containing the manufacturer, model, version, and serial number of the baseboard. Returns
     *         {@link Normal#UNKNOWN} for any field that cannot be determined.
     */
    private static Tuple queryPlatform() {
        String manufacturer = null;
        String model = null;
        String version = null;
        String serialNumber = null;

        IORegistryEntry platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        if (platformExpert != null) {
            byte[] data = platformExpert.getByteArrayProperty("manufacturer");
            if (data != null) {
                manufacturer = Native.toString(data, Charset.UTF_8);
            }
            data = platformExpert.getByteArrayProperty("board-id");
            if (data != null) {
                model = Native.toString(data, Charset.UTF_8);
            }
            if (StringKit.isBlank(model)) {
                data = platformExpert.getByteArrayProperty("model-number");
                if (data != null) {
                    model = Native.toString(data, Charset.UTF_8);
                }
            }
            data = platformExpert.getByteArrayProperty("version");
            if (data != null) {
                version = Native.toString(data, Charset.UTF_8);
            }
            data = platformExpert.getByteArrayProperty("mlb-serial-number");
            if (data != null) {
                serialNumber = Native.toString(data, Charset.UTF_8);
            }
            if (StringKit.isBlank(serialNumber)) {
                serialNumber = platformExpert.getStringProperty("IOPlatformSerialNumber");
            }
            platformExpert.release();
        }
        return new Tuple(StringKit.isBlank(manufacturer) ? "Apple Inc." : manufacturer,
                StringKit.isBlank(model) ? Normal.UNKNOWN : model,
                StringKit.isBlank(version) ? Normal.UNKNOWN : version,
                StringKit.isBlank(serialNumber) ? Normal.UNKNOWN : serialNumber);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String getManufacturer() {
        return manufModelVersSerial.get().get(0);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String getModel() {
        return manufModelVersSerial.get().get(1);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String getVersion() {
        return manufModelVersSerial.get().get(2);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String getSerialNumber() {
        return manufModelVersSerial.get().get(3);
    }

}
