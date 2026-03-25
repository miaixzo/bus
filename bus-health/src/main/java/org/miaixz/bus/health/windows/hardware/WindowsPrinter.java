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
package org.miaixz.bus.health.windows.hardware;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.builtin.hardware.Printer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractPrinter;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.driver.wmi.Win32Printer;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * Printer data obtained from WMI
 * 
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
final class WindowsPrinter extends AbstractPrinter {

    // DetectedErrorState: 0=Unknown, 1=Other, 2=No Error, 3=Low Paper, 4=No Paper,
    // 5=Low Toner, 6=No Toner, 7=Door Open, 8=Jammed, 9=Offline, 10=Service Requested, 11=Output Bin Full
    private static final String[] ERROR_STATE_NAMES = { "Unknown", "Other", "", "Low Paper", "No Paper", "Low Toner",
            "No Toner", "Door Open", "Jammed", "Offline", "Service Requested", "Output Bin Full" };

    WindowsPrinter(String name, String driverName, String description, PrinterStatus status, String statusReason,
            boolean isDefault, boolean isLocal, String portName) {
        super(name, driverName, description, status, statusReason, isDefault, isLocal, portName);
    }

    /**
     * Gets printers from WMI Win32_Printer.
     *
     * @return A list of printers.
     */
    public static List<Printer> getPrinters() {
        List<Printer> printers = new ArrayList<>();
        WmiResult<Win32Printer.PrinterProperty> result = Win32Printer.queryPrinters();

        for (int i = 0; i < result.getResultCount(); i++) {
            String name = WmiKit.getString(result, Win32Printer.PrinterProperty.NAME, i);
            String driverName = WmiKit.getString(result, Win32Printer.PrinterProperty.DRIVERNAME, i);
            String description = WmiKit.getString(result, Win32Printer.PrinterProperty.DESCRIPTION, i);
            int statusCode = WmiKit.getUint16(result, Win32Printer.PrinterProperty.PRINTERSTATUS, i);
            int errorState = WmiKit.getUint16(result, Win32Printer.PrinterProperty.DETECTEDERRORSTATE, i);
            boolean isDefault = getBooleanValue(result, Win32Printer.PrinterProperty.DEFAULT, i);
            boolean isLocal = getBooleanValue(result, Win32Printer.PrinterProperty.LOCAL, i);
            String portName = WmiKit.getString(result, Win32Printer.PrinterProperty.PORTNAME, i);

            printers.add(
                    new WindowsPrinter(name, driverName, description, parseStatus(statusCode, errorState),
                            parseErrorState(errorState), isDefault, isLocal, portName));
        }
        return printers;
    }

    private static boolean getBooleanValue(
            WmiResult<Win32Printer.PrinterProperty> result,
            Win32Printer.PrinterProperty property,
            int index) {
        Object o = result.getValue(property, index);
        if (o instanceof Boolean) {
            return (Boolean) o;
        }
        return false;
    }

    /**
     * Parses Win32_Printer PrinterStatus and DetectedErrorState codes.
     * https://learn.microsoft.com/en-us/windows/win32/cimwin32prov/win32-printer
     *
     * DetectedErrorState values: 0=Unknown, 1=Other, 2=No Error, 3=Low Paper, 4=No Paper, 5=Low Toner, 6=No Toner,
     * 7=Door Open, 8=Jammed, 9=Offline, 10=Service Requested, 11=Output Bin Full.
     *
     * We treat values >= 4 as ERROR (hard failures preventing printing). Low consumable warnings (3=Low Paper, 5=Low
     * Toner) pass through to PrinterStatus check since the printer can still function. Callers can check
     * getStatusReason() for details.
     *
     * @param statusCode The PrinterStatus code from WMI.
     * @param errorState The DetectedErrorState code from WMI.
     * @return The corresponding PrinterStatus enum value.
     */
    private static PrinterStatus parseStatus(int statusCode, int errorState) {
        if (errorState == 4 || errorState == 6 || errorState >= 7) {
            return PrinterStatus.ERROR;
        }
        switch (statusCode) {
            case 1: // Other
            case 2: // Unknown
                return PrinterStatus.UNKNOWN;

            case 3: // Idle
                return PrinterStatus.IDLE;

            case 4: // Printing
                return PrinterStatus.PRINTING;

            case 5: // Warmup
                return PrinterStatus.IDLE;

            case 6: // Stopped Printing
            case 7: // Offline
                return PrinterStatus.OFFLINE;

            default:
                return PrinterStatus.UNKNOWN;
        }
    }

    private static String parseErrorState(int errorState) {
        if (errorState >= 0 && errorState < ERROR_STATE_NAMES.length) {
            return ERROR_STATE_NAMES[errorState];
        }
        return Normal.EMPTY;
    }

}
