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
 * Printer interface representing a printer device.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public interface Printer {

    /**
     * Retrieves the name of the printer.
     *
     * @return The printer name.
     */
    String getName();

    /**
     * Retrieves the driver name or make/model of the printer.
     *
     * @return The driver or model name.
     */
    String getDriverName();

    /**
     * Retrieves the user-friendly description of the printer.
     *
     * @return The printer description.
     */
    String getDescription();

    /**
     * Retrieves the current status of the printer.
     *
     * @return The printer status.
     */
    PrinterStatus getStatus();

    /**
     * Retrieves the reason for the current printer status, if available.
     *
     * @return A string describing the status reason (e.g., "Paper Jam", "media-empty"), or empty string if none.
     */
    String getStatusReason();

    /**
     * Indicates whether this is the default printer.
     *
     * @return {@code true} if this is the default printer, {@code false} otherwise.
     */
    boolean isDefault();

    /**
     * Indicates whether this is a local printer (as opposed to a network printer).
     *
     * @return {@code true} if this is a local printer, {@code false} if it is a network printer.
     */
    boolean isLocal();

    /**
     * Retrieves the port name or device URI of the printer.
     *
     * @return The port name or URI.
     */
    String getPortName();

    /**
     * Printer status enumeration.
     */
    enum PrinterStatus {
        IDLE, PRINTING, ERROR, OFFLINE, UNKNOWN
    }

}
