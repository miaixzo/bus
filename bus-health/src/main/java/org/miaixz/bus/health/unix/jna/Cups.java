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
package org.miaixz.bus.health.unix.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.ptr.PointerByReference;

/**
 * CUPS (Common Unix Printing System) library. This class should be considered non-API as it may be removed if/when its
 * code is incorporated into the JNA project.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Cups extends Library {

    Cups INSTANCE = Native.load("cups", Cups.class);

    // Printer state constants from cups/cups.h
    int IPP_PRINTER_IDLE = 3;
    int IPP_PRINTER_PROCESSING = 4;
    int IPP_PRINTER_STOPPED = 5;

    // Printer type bit flags from cups/cups.h
    int CUPS_PRINTER_REMOTE = 0x0002;

    /**
     * CUPS destination (printer) structure.
     */
    @FieldOrder({ "name", "instance", "is_default", "num_options", "options" })
    class CupsDest extends Structure {

        public String name;
        public String instance;
        public int is_default;
        public int num_options;
        public Pointer options; // cups_option_t*

        public CupsDest() {
            super();
        }

        public CupsDest(Pointer p) {
            super(p);
            read();
        }
    }

    /**
     * Gets all available destinations (printers and classes).
     *
     * @param dests Pointer to receive destination array
     * @return Number of destinations
     */
    int cupsGetDests(PointerByReference dests);

    /**
     * Frees the memory used by a destination array.
     *
     * @param num_dests Number of destinations
     * @param dests     Pointer to destination array
     */
    void cupsFreeDests(int num_dests, Pointer dests);

    /**
     * Gets the default printer name.
     *
     * @return Default printer name or null if none
     */
    String cupsGetDefault();

    /**
     * Gets an option value from a destination.
     *
     * @param name        Option name
     * @param num_options Number of options
     * @param options     Pointer to options array
     * @return Option value or null if not found
     */
    String cupsGetOption(String name, int num_options, Pointer options);
}
