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
package org.miaixz.bus.health.unix.platform.aix.hardware;

import java.util.List;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.builtin.hardware.common.AbstractBaseboard;
import org.miaixz.bus.health.unix.platform.aix.driver.Lscfg;

/**
 * Baseboard data obtained by lscfg
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
final class AixBaseboard extends AbstractBaseboard {

    /**
     * The IBM constant.
     */
    private static final String IBM = "IBM";
    /**
     * The model value.
     */
    private final String model;
    /**
     * The serialNumber value.
     */
    private final String serialNumber;
    /**
     * The version value.
     */
    private final String version;

    /**
     * Creates a new AixBaseboard instance.
     *
     * @param lscfg the lscfg
     */
    AixBaseboard(Supplier<List<String>> lscfg) {
        Triplet<String, String, String> msv = Lscfg.queryBackplaneModelSerialVersion(lscfg.get());
        this.model = StringKit.isBlank(msv.getLeft()) ? Normal.UNKNOWN : msv.getLeft();
        this.serialNumber = StringKit.isBlank(msv.getMiddle()) ? Normal.UNKNOWN : msv.getMiddle();
        this.version = StringKit.isBlank(msv.getRight()) ? Normal.UNKNOWN : msv.getRight();
    }

    /**
     * Returns the manufacturer.
     *
     * @return the get manufacturer result
     */
    @Override
    public String getManufacturer() {
        return IBM;
    }

    /**
     * Returns the model.
     *
     * @return the get model result
     */
    @Override
    public String getModel() {
        return this.model;
    }

    /**
     * Returns the serial number.
     *
     * @return the get serial number result
     */
    @Override
    public String getSerialNumber() {
        return this.serialNumber;
    }

    /**
     * Returns the version.
     *
     * @return the get version result
     */
    @Override
    public String getVersion() {
        return this.version;
    }

}
