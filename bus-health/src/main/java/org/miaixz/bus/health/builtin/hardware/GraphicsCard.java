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
package org.miaixz.bus.health.builtin.hardware;

import org.miaixz.bus.core.lang.annotation.Immutable;

/**
 * GraphicsCard interface.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
public interface GraphicsCard {

    /**
     * Retrieves the full name of the card.
     *
     * @return The name of the card.
     */
    String getName();

    /**
     * Retrieves the card's Device ID
     *
     * @return The Device ID of the card
     */
    String getDeviceId();

    /**
     * Retrieves the card's manufacturer/vendor
     *
     * @return The vendor of the card as human-readable text if possible, or the Vendor ID (VID) otherwise
     */
    String getVendor();

    /**
     * Retrieves a list of version/revision data from the card. Users may need to further parse this list to identify
     * specific GPU capabilities.
     *
     * @return A comma-delimited list of version/revision data
     */
    String getVersionInfo();

    /**
     * Retrieves the Video RAM (VRAM) available on the GPU
     *
     * @return Total number of bytes.
     */
    long getVRam();

}
