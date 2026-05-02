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
package org.miaixz.bus.health.unix.hardware;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.builtin.hardware.common.AbstractBaseboard;

/**
 * Baseboard data obtained by a calling class
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public final class UnixBaseboard extends AbstractBaseboard {

    /**
     * The manufacturer value.
     */
    private final String manufacturer;
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
     * Creates a new UnixBaseboard instance.
     *
     * @param manufacturer the manufacturer
     * @param model        the model
     * @param serialNumber the serial number
     * @param version      the version
     */
    public UnixBaseboard(String manufacturer, String model, String serialNumber, String version) {
        this.manufacturer = manufacturer;
        this.model = model;
        this.serialNumber = serialNumber;
        this.version = version;
    }

    /**
     * Returns the manufacturer.
     *
     * @return the get manufacturer result
     */
    @Override
    public String getManufacturer() {
        return this.manufacturer;
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
