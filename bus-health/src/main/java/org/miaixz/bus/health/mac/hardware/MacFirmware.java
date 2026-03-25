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
import org.miaixz.bus.health.builtin.hardware.common.AbstractFirmware;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;

/**
 * Firmware data obtained from ioreg.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
final class MacFirmware extends AbstractFirmware {

    private final Supplier<Tuple> manufNameDescVersRelease = Memoizer.memoize(MacFirmware::queryEfi);

    /**
     * Queries the EFI (Extensible Firmware Interface) information from the I/O Registry.
     *
     * @return A {@link Tuple} containing the manufacturer, name, description, version, and release date of the EFI.
     *         Returns {@link Normal#UNKNOWN} for any field that cannot be determined.
     */
    private static Tuple queryEfi() {
        String manufacturer = null;
        String name = null;
        String description = null;
        String version = null;
        String releaseDate = null;

        IORegistryEntry platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        byte[] data;
        if (platformExpert != null) {
            IOIterator iter = platformExpert.getChildIterator("IODeviceTree");
            if (iter != null) {
                IORegistryEntry entry = iter.next();
                while (entry != null) {
                    switch (entry.getName()) {
                        case "rom":
                            data = entry.getByteArrayProperty("vendor");
                            if (data != null) {
                                manufacturer = Native.toString(data, Charset.UTF_8);
                            }
                            data = entry.getByteArrayProperty("version");
                            if (data != null) {
                                version = Native.toString(data, Charset.UTF_8);
                            }
                            data = entry.getByteArrayProperty("release-date");
                            if (data != null) {
                                releaseDate = Native.toString(data, Charset.UTF_8);
                            }
                            break;

                        case "chosen":
                            data = entry.getByteArrayProperty("booter-name");
                            if (data != null) {
                                name = Native.toString(data, Charset.UTF_8);
                            }
                            break;

                        case "efi":
                            data = entry.getByteArrayProperty("firmware-abi");
                            if (data != null) {
                                description = Native.toString(data, Charset.UTF_8);
                            }
                            break;

                        default:
                            if (StringKit.isBlank(name)) {
                                name = entry.getStringProperty("IONameMatch");
                            }
                            break;
                    }
                    entry.release();
                    entry = iter.next();
                }
                iter.release();
            }
            if (StringKit.isBlank(manufacturer)) {
                data = platformExpert.getByteArrayProperty("manufacturer");
                if (data != null) {
                    manufacturer = Native.toString(data, Charset.UTF_8);
                }
            }
            if (StringKit.isBlank(version)) {
                data = platformExpert.getByteArrayProperty("target-type");
                if (data != null) {
                    version = Native.toString(data, Charset.UTF_8);
                }
            }
            if (StringKit.isBlank(name)) {
                data = platformExpert.getByteArrayProperty("device_type");
                if (data != null) {
                    name = Native.toString(data, Charset.UTF_8);
                }
            }
            platformExpert.release();
        }
        return new Tuple(StringKit.isBlank(manufacturer) ? Normal.UNKNOWN : manufacturer,
                StringKit.isBlank(name) ? Normal.UNKNOWN : name,
                StringKit.isBlank(description) ? Normal.UNKNOWN : description,
                StringKit.isBlank(version) ? Normal.UNKNOWN : version,
                StringKit.isBlank(releaseDate) ? Normal.UNKNOWN : releaseDate);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String getManufacturer() {
        return manufNameDescVersRelease.get().get(0);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String getName() {
        return manufNameDescVersRelease.get().get(1);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String getDescription() {
        return manufNameDescVersRelease.get().get(2);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String getVersion() {
        return manufNameDescVersRelease.get().get(3);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public String getReleaseDate() {
        return manufNameDescVersRelease.get().get(4);
    }

}
