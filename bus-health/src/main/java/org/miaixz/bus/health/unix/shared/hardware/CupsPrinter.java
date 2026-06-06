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
package org.miaixz.bus.health.unix.shared.hardware;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.Printer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractPrinter;
import org.miaixz.bus.health.unix.shared.driver.Lpstat;
import org.miaixz.bus.health.unix.shared.jna.Cups;
import org.miaixz.bus.logger.Logger;

/**
 * CUPS-based printer implementation for Unix-like systems.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public final class CupsPrinter extends AbstractPrinter {

    /**
     * The HAS_CUPS constant.
     */
    private static final boolean HAS_CUPS;

    static {
        boolean hasCups = false;
        try {
            Cups lib = Cups.INSTANCE;
            hasCups = true;
        } catch (UnsatisfiedLinkError e) {
            Logger.debug(false, "Health", "libcups not found. Falling back to lpstat command.");
        }
        HAS_CUPS = hasCups;
    }

    /**
     * Creates a new CupsPrinter instance.
     *
     * @param name         the name
     * @param driverName   the driver name
     * @param description  the description
     * @param status       the status
     * @param statusReason the status reason
     * @param isDefault    the is default
     * @param isLocal      the is local
     * @param portName     the port name
     */
    private CupsPrinter(String name, String driverName, String description, PrinterStatus status, String statusReason,
            boolean isDefault, boolean isLocal, String portName) {
        super(name, driverName, description, status, statusReason, isDefault, isLocal, portName);
    }

    /**
     * Gets printers using CUPS. Uses libcups if available, otherwise falls back to lpstat command.
     *
     * @return A list of printers.
     */
    public static List<Printer> getPrinters() {
        if (HAS_CUPS) {
            return getPrintersFromLibCups();
        }
        return getPrintersFromLpstat(CupsPrinter::new);
    }

    /**
     * Returns the printers from lib cups.
     *
     * @return the get printers from lib cups result
     */
    private static List<Printer> getPrintersFromLibCups() {
        List<Printer> printers = new ArrayList<>();
        PointerByReference destsRef = new PointerByReference();
        int numDests = Cups.INSTANCE.cupsGetDests(destsRef);
        Pointer destsPtr = destsRef.getValue();

        if (destsPtr != null && numDests > 0) {
            try {
                Cups.CupsDest[] dests = (Cups.CupsDest[]) new Cups.CupsDest(destsPtr).toArray(numDests);
                for (Cups.CupsDest dest : dests) {
                    if (dest.name == null) {
                        continue;
                    }
                    String name = dest.name;
                    boolean isDefault = dest.is_default != 0;

                    String deviceUri = "";
                    String printerInfo = "";
                    String printerMakeModel = "";
                    String printerState = "";
                    String stateReasons = "";

                    if (dest.num_options > 0 && dest.options != null) {
                        deviceUri = getOption(dest, "device-uri");
                        printerInfo = getOption(dest, "printer-info");
                        printerMakeModel = getOption(dest, "printer-make-and-model");
                        printerState = getOption(dest, "printer-state");
                        stateReasons = getOption(dest, "printer-state-reasons");
                    }

                    PrinterStatus status = parseStateFromCups(printerState, stateReasons);
                    String statusReason = "none".equals(stateReasons) ? "" : stateReasons;
                    // Use printer-type bit flag for locality (device-uri not available to non-root)
                    int printerType = Parsing.parseIntOrDefault(getOption(dest, "printer-type"), 0);
                    boolean isLocal = (printerType & Cups.CUPS_PRINTER_REMOTE) == 0;

                    printers.add(
                            new CupsPrinter(name, printerMakeModel, printerInfo, status, statusReason, isDefault,
                                    isLocal, deviceUri));
                }
            } finally {
                Cups.INSTANCE.cupsFreeDests(numDests, destsPtr);
            }
        }
        return printers;
    }

    /**
     * Returns the option.
     *
     * @param dest       the dest
     * @param optionName the option name
     * @return the get option result
     */
    private static String getOption(Cups.CupsDest dest, String optionName) {
        String value = Cups.INSTANCE.cupsGetOption(optionName, dest.num_options, dest.options);
        return value != null ? value : "";
    }

    /**
     * Parses the state from cups.
     *
     * @param state        the state
     * @param stateReasons the state reasons
     * @return the parse state from cups result
     */
    private static PrinterStatus parseStateFromCups(String state, String stateReasons) {
        if (!stateReasons.isEmpty() && !"none".equals(stateReasons)) {
            String lower = stateReasons.toLowerCase(Locale.ROOT);
            if (lower.contains("error") || lower.contains("fault")) {
                return PrinterStatus.ERROR;
            }
        }
        if (state.isEmpty()) {
            return PrinterStatus.UNKNOWN;
        }
        int stateInt = Parsing.parseIntOrDefault(state, -1);
        switch (stateInt) {
            case Cups.IPP_PRINTER_IDLE:
                return PrinterStatus.IDLE;

            case Cups.IPP_PRINTER_PROCESSING:
                return PrinterStatus.PRINTING;

            case Cups.IPP_PRINTER_STOPPED:
                return PrinterStatus.OFFLINE;

            default:
                return PrinterStatus.UNKNOWN;
        }
    }

    /**
     * Gets printers by parsing lpstat command output.
     *
     * @param factory function to create concrete printer instances
     * @return list of printers
     */
    protected static List<Printer> getPrintersFromLpstat(PrinterFactory factory) {
        return getPrintersFromLpstat(Executor.runNative(new String[] { "lpstat", "-p" }), factory);
    }

    /**
     * Parse lpstat output to build a list of printers.
     *
     * @param lpstatLines output of {@code lpstat -p}
     * @param factory     function to create concrete printer instances
     * @return list of printers
     */
    static List<Printer> getPrintersFromLpstat(List<String> lpstatLines, PrinterFactory factory) {
        return getPrintersFromLpstat(
                lpstatLines,
                Lpstat.queryDefaultPrinter(),
                Lpstat.queryPortMap(),
                Lpstat.queryDescriptionMap(),
                Lpstat::queryDriver,
                factory);
    }

    /**
     * Parse lpstat output to build a list of printers with pre-fetched data.
     *
     * @param lpstatLines    output of {@code lpstat -p}
     * @param defaultPrinter the default printer name
     * @param portMap        map of printer name to device URI
     * @param descriptionMap map of printer name to description
     * @param driverLookup   function to look up driver name for a printer
     * @param factory        function to create concrete printer instances
     * @return list of printers
     */
    static List<Printer> getPrintersFromLpstat(
            List<String> lpstatLines,
            String defaultPrinter,
            Map<String, String> portMap,
            Map<String, String> descriptionMap,
            Function<String, String> driverLookup,
            PrinterFactory factory) {
        List<Printer> printers = new ArrayList<>();
        for (String line : lpstatLines) {
            if (line.startsWith("printer ")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    String name = parts[1];
                    PrinterStatus status = Lpstat.parseStatus(line);
                    boolean isDefault = name.equals(defaultPrinter);
                    String portName = portMap.getOrDefault(name, "");
                    boolean isLocal = Lpstat.isLocalUri(portName);
                    String driverName = driverLookup.apply(name);
                    String description = descriptionMap.getOrDefault(name, "");
                    String statusReason = Lpstat.parseStatusReason(line);

                    printers.add(
                            factory.create(
                                    name,
                                    driverName,
                                    description,
                                    status,
                                    statusReason,
                                    isDefault,
                                    isLocal,
                                    portName));
                }
            }
        }
        return printers;
    }

    /**
     * Factory interface for creating concrete printer instances.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    @FunctionalInterface
    protected interface PrinterFactory {

        /**
         * Create a printer instance.
         *
         * @param name         printer name
         * @param driverName   driver name
         * @param description  description
         * @param status       status
         * @param statusReason status reason
         * @param isDefault    whether this is the default printer
         * @param isLocal      whether this is a local printer
         * @param portName     port or URI
         * @return a new printer instance
         */
        Printer create(
                String name,
                String driverName,
                String description,
                PrinterStatus status,
                String statusReason,
                boolean isDefault,
                boolean isLocal,
                String portName);

    }

}
