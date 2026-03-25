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

import java.util.*;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.Printer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractPrinter;
import org.miaixz.bus.health.unix.jna.Cups;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * CUPS-based printer implementation for Unix-like systems.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public final class UnixPrinter extends AbstractPrinter {

    // Local URI schemes for directly-attached or local printers
    private static final String[] LOCAL_URI_PREFIXES = { "usb:", "parallel:", "serial:", "file:", "direct:", "hp:",
            "lpd://127.", "lpd://localhost", "socket://127.", "socket://localhost" };

    private static final boolean HAS_CUPS;

    static {
        boolean hasCups = false;
        try {
            Cups lib = Cups.INSTANCE;
            hasCups = true;
        } catch (UnsatisfiedLinkError e) {
            Logger.debug("libcups not found. Falling back to lpstat command.");
        }
        HAS_CUPS = hasCups;
    }

    UnixPrinter(String name, String driverName, String description, PrinterStatus status, String statusReason,
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
        return getPrintersFromLpstat();
    }

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
                            new UnixPrinter(name, printerMakeModel, printerInfo, status, statusReason, isDefault,
                                    isLocal, deviceUri));
                }
            } finally {
                Cups.INSTANCE.cupsFreeDests(numDests, destsPtr);
            }
        }
        return printers;
    }

    private static String getOption(Cups.CupsDest dest, String optionName) {
        String value = Cups.INSTANCE.cupsGetOption(optionName, dest.num_options, dest.options);
        return value != null ? value : "";
    }

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

    private static List<Printer> getPrintersFromLpstat() {
        List<Printer> printers = new ArrayList<>();
        String defaultPrinter = getDefaultPrinter();

        // Pre-fetch printer info with aggregated commands to reduce subprocess spawning
        Map<String, String> portMap = parsePortMap();
        Map<String, String> descriptionMap = parseDescriptionMap();

        for (String line : Executor.runNative(new String[] { "lpstat", "-p" })) {
            if (line.startsWith("printer ")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 3) {
                    String name = parts[1];
                    PrinterStatus status = parseStatusFromLpstat(line);
                    boolean isDefault = name.equals(defaultPrinter);

                    String portName = portMap.getOrDefault(name, "");
                    boolean isLocal = isLocalUri(portName);
                    String driverName = getDriverForPrinter(name);
                    String description = descriptionMap.getOrDefault(name, "");
                    String statusReason = getStatusReasonFromLpstat(line);

                    printers.add(
                            new UnixPrinter(name, driverName, description, status, statusReason, isDefault, isLocal,
                                    portName));
                }
            }
        }
        return printers;
    }

    private static Map<String, String> parsePortMap() {
        Map<String, String> map = new HashMap<>();
        // lpstat -v output: "device for PrinterName: uri"
        for (String line : Executor.runNative(new String[] { "lpstat", "-v" })) {
            if (line.contains("device for")) {
                int forIdx = line.indexOf("device for ") + 11;
                int colonIdx = line.indexOf(':', forIdx);
                if (colonIdx > forIdx) {
                    String name = line.substring(forIdx, colonIdx).trim();
                    String uri = line.substring(colonIdx + 1).trim();
                    map.put(name, uri);
                }
            }
        }
        return map;
    }

    private static Map<String, String> parseDescriptionMap() {
        Map<String, String> map = new HashMap<>();
        String currentPrinter = null;
        // lpstat -l -p output: "printer PrinterName ..." followed by "\tDescription: ..."
        for (String line : Executor.runNative(new String[] { "lpstat", "-l", "-p" })) {
            if (line.startsWith("printer ")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    currentPrinter = parts[1];
                }
            } else if (currentPrinter != null && line.trim().startsWith("Description:")) {
                String desc = line.substring(line.indexOf(':') + 1).trim();
                map.put(currentPrinter, desc);
            }
        }
        return map;
    }

    // Retrieves driver via lpoptions which requires a per-printer subprocess call.
    // On systems with many printers, this may add latency.
    private static String getDriverForPrinter(String printerName) {
        // lpoptions -p requires per-printer call as there's no global option
        for (String line : Executor.runNative(new String[] { "lpoptions", "-p", printerName })) {
            int idx = line.indexOf("printer-make-and-model='");
            if (idx >= 0) {
                int start = idx + 24;
                int end = line.indexOf('\'', start);
                if (end > start) {
                    return line.substring(start, end);
                }
            }
        }
        return "";
    }

    private static String getDefaultPrinter() {
        for (String line : Executor.runNative(new String[] { "lpstat", "-d" })) {
            if (line.contains("default destination:")) {
                String[] parts = line.split(":", 2);
                if (parts.length >= 2) {
                    return parts[1].trim();
                }
            }
        }
        return "";
    }

    private static PrinterStatus parseStatusFromLpstat(String line) {
        String lower = line.toLowerCase(Locale.ROOT);
        if (lower.contains("disabled") || lower.contains("not accepting")) {
            return PrinterStatus.OFFLINE;
        } else if (lower.contains("printing")) {
            return PrinterStatus.PRINTING;
        } else if (lower.contains("idle")) {
            return PrinterStatus.IDLE;
        } else if (lower.contains("error") || lower.contains("fault")) {
            return PrinterStatus.ERROR;
        }
        return PrinterStatus.UNKNOWN;
    }

    private static String getStatusReasonFromLpstat(String line) {
        int dashIdx = line.indexOf(" - ");
        if (dashIdx > 0) {
            return line.substring(dashIdx + 3).trim();
        }
        return "";
    }

    private static boolean isLocalUri(String uri) {
        if (uri.startsWith("/dev")) {
            return true;
        }
        for (String prefix : LOCAL_URI_PREFIXES) {
            if (uri.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

}
