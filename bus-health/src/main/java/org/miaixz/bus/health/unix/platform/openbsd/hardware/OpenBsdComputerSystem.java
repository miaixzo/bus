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
package org.miaixz.bus.health.unix.platform.openbsd.hardware;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.Baseboard;
import org.miaixz.bus.health.builtin.hardware.Firmware;
import org.miaixz.bus.health.builtin.hardware.common.AbstractComputerSystem;
import org.miaixz.bus.health.unix.hardware.UnixBaseboard;
import org.miaixz.bus.health.unix.platform.openbsd.OpenBsdSysctlKit;

/**
 * OpenBSD ComputerSystem implementation
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public class OpenBsdComputerSystem extends AbstractComputerSystem {

    private final Supplier<String> manufacturer = Memoizer.memoize(OpenBsdComputerSystem::queryManufacturer);

    private final Supplier<String> model = Memoizer.memoize(OpenBsdComputerSystem::queryModel);

    private final Supplier<String> serialNumber = Memoizer.memoize(OpenBsdComputerSystem::querySerialNumber);

    private final Supplier<String> uuid = Memoizer.memoize(OpenBsdComputerSystem::queryUUID);

    private static String queryManufacturer() {
        return OpenBsdSysctlKit.sysctl("hw.vendor", Normal.UNKNOWN);
    }

    private static String queryModel() {
        return OpenBsdSysctlKit.sysctl("hw.version", Normal.UNKNOWN);
    }

    private static String querySerialNumber() {
        return OpenBsdSysctlKit.sysctl("hw.serialno", Normal.UNKNOWN);
    }

    private static String queryUUID() {
        return OpenBsdSysctlKit.sysctl("hw.uuid", Normal.UNKNOWN);
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
    protected Firmware createFirmware() {
        return new OpenBsdFirmware();
    }

    @Override
    protected Baseboard createBaseboard() {
        return new UnixBaseboard(manufacturer.get(), model.get(), serialNumber.get(),
                OpenBsdSysctlKit.sysctl("hw.product", Normal.UNKNOWN));
    }

}
