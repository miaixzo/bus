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
package org.miaixz.bus.health.builtin.hardware;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Builder;

/**
 * Holds the human-readable information described by a display's EDID.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public interface DisplayInfo {

    /**
     * Gets the EDID byte array.
     *
     * @return a copy of the EDID byte array
     */
    byte[] getEdid();

    /**
     * Returns whether the EDID was synthesized from reported display attributes.
     *
     * @return {@code true} if the EDID is synthetic, otherwise {@code false}
     */
    boolean isEdidSynthetic();

    /**
     * Gets the manufacturer ID.
     *
     * @return the manufacturer ID
     * @see Builder#getManufacturerID(byte[])
     */
    String getManufacturerID();

    /**
     * Gets the product ID.
     *
     * @return the product ID
     * @see Builder#getProductID(byte[])
     */
    String getProductID();

    /**
     * Gets the numeric ID serial number from bytes 12-15 of the EDID.
     *
     * @return the serial number
     * @see Builder#getSerialNo(byte[])
     */
    String getSerialNo();

    /**
     * Gets the week of manufacture.
     *
     * @return the week of manufacture
     */
    byte getWeek();

    /**
     * Gets the year of manufacture.
     *
     * @return the year of manufacture
     */
    int getYear();

    /**
     * Gets the EDID version.
     *
     * @return the EDID version
     */
    String getVersion();

    /**
     * Returns whether the display is digital.
     *
     * @return {@code true} if the display is digital, otherwise {@code false}
     */
    boolean isDigital();

    /**
     * Gets the monitor width in centimeters.
     *
     * @return the monitor width in centimeters
     */
    int getHcm();

    /**
     * Gets the monitor height in centimeters.
     *
     * @return the monitor height in centimeters
     */
    int getVcm();

    /**
     * Gets the preferred resolution.
     *
     * @return the preferred resolution, such as {@code 2560x1440}
     */
    String getPreferredResolution();

    /**
     * Gets the monitor model.
     *
     * @return the monitor model
     */
    String getModel();

    /**
     * Gets the display product serial number descriptor text.
     *
     * @return the product serial number, or an empty string if unavailable
     */
    String getProductSerialNumber();

}
