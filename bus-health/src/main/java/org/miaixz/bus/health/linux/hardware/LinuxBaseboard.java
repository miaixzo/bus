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
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractBaseboard;
import org.miaixz.bus.health.linux.driver.Sysfs;
import org.miaixz.bus.health.linux.driver.proc.CpuInfo;

/**
 * Baseboard data obtained by sysfs
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class LinuxBaseboard extends AbstractBaseboard {

    private final Supplier<Tuple> manufacturerModelVersionSerial = Memoizer.memoize(CpuInfo::queryBoardInfo);
    private final Supplier<String> manufacturer = Memoizer.memoize(this::queryManufacturer);
    private final Supplier<String> model = Memoizer.memoize(this::queryModel);
    private final Supplier<String> version = Memoizer.memoize(this::queryVersion);
    private final Supplier<String> serialNumber = Memoizer.memoize(this::querySerialNumber);

    @Override
    public String getManufacturer() {
        return manufacturer.get();
    }

    @Override
    public String getModel() {
        return model.get();
    }

    @Override
    public String getVersion() {
        return version.get();
    }

    @Override
    public String getSerialNumber() {
        return serialNumber.get();
    }

    private String queryManufacturer() {
        String result = null;
        if ((result = Sysfs.queryBoardVendor()) == null
                && (result = manufacturerModelVersionSerial.get().get(0)) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String queryModel() {
        String result;
        if ((result = Sysfs.queryBoardModel()) == null
                && (result = manufacturerModelVersionSerial.get().get(1)) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String queryVersion() {
        String result;
        if ((result = Sysfs.queryBoardVersion()) == null
                && (result = manufacturerModelVersionSerial.get().get(2)) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String querySerialNumber() {
        String result;
        if ((result = Sysfs.queryBoardSerial()) == null
                && (result = manufacturerModelVersionSerial.get().get(3)) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

}
