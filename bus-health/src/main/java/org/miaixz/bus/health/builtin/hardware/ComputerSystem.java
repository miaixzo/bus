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
package org.miaixz.bus.health.builtin.hardware;

import org.miaixz.bus.core.lang.annotation.Immutable;

/**
 * The ComputerSystem represents the physical hardware, of a computer system/product and includes BIOS/firmware and a
 * motherboard, logic board, etc.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
public interface ComputerSystem {

    /**
     * Get the computer system manufacturer.
     *
     * @return The manufacturer.
     */
    String getManufacturer();

    /**
     * Get the computer system model.
     *
     * @return The model.
     */
    String getModel();

    /**
     * Get the computer system serial number, if available.
     * <p>
     * Performs a best-effort attempt to retrieve a unique serial number from the computer system. This may originate
     * from the baseboard, BIOS, processor, etc.
     * <p>
     * This value is provided for information only. Caution should be exercised if using this result to "fingerprint" a
     * system for licensing or other purposes, as the result may change based on program permissions or installation of
     * software packages. Specifically, on Linux and FreeBSD, this requires either root permissions, or installation of
     * the (deprecated) HAL library (lshal command). Linux also attempts to read the dmi/id serial number files in
     * sysfs, which are read-only root by default but may have permissions altered by the user.
     *
     * @return the System Serial Number, if available, otherwise returns "unknown"
     */
    String getSerialNumber();

    /**
     * Get the computer system hardware UUID, if available.
     * <p>
     * Performs a best-effort attempt to retrieve the hardware UUID.
     *
     * @return the Hardware UUID, if available, otherwise returns "unknown"
     */
    String getHardwareUUID();

    /**
     * Get the computer system firmware/BIOS.
     *
     * @return A {@link Firmware} object for this system
     */
    Firmware getFirmware();

    /**
     * Get the computer system baseboard/motherboard.
     *
     * @return A {@link Baseboard} object for this system
     */
    Baseboard getBaseboard();

}
