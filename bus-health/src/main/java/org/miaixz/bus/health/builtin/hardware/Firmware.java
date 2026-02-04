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
 * The Firmware represents the low level BIOS or equivalent.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
public interface Firmware {

    /**
     * Get the firmware manufacturer.
     *
     * @return the manufacturer
     */
    String getManufacturer();

    /**
     * Get the firmware name.
     *
     * @return the name
     */
    String getName();

    /**
     * Get the firmware description.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Get the firmware version.
     *
     * @return the version
     */
    String getVersion();

    /**
     * Get the firmware release date.
     *
     * @return The release date.
     */
    String getReleaseDate();

}
