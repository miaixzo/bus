/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.health.windows.hardware;

import com.sun.jna.platform.win32.*;
import org.miaixz.bus.core.annotation.Immutable;
import org.miaixz.bus.health.builtin.hardware.Display;
import org.miaixz.bus.health.builtin.hardware.common.AbstractDisplay;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * A Display
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class WindowsDisplay extends AbstractDisplay {

    private static final SetupApi SU = SetupApi.INSTANCE;
    private static final Advapi32 ADV = Advapi32.INSTANCE;

    private static final Guid.GUID GUID_DEVINTERFACE_MONITOR = new Guid.GUID("E6F07B5F-EE97-4a90-B076-33F57BF4EAA7");

    /**
     * Constructor for WindowsDisplay.
     *
     * @param edid a byte array representing a display EDID
     */
    WindowsDisplay(byte[] edid) {
        super(edid);
        Logger.debug("Initialized WindowsDisplay");
    }

    /**
     * Gets Display Information
     *
     * @return An array of Display objects representing monitors, etc.
     */
    public static List<Display> getDisplays() {
        List<Display> displays = new ArrayList<>();

        WinNT.HANDLE hDevInfo = SU.SetupDiGetClassDevs(GUID_DEVINTERFACE_MONITOR, null, null,
                SetupApi.DIGCF_PRESENT | SetupApi.DIGCF_DEVICEINTERFACE);
        if (!hDevInfo.equals(WinBase.INVALID_HANDLE_VALUE)) {
            try (Struct.CloseableSpDeviceInterfaceData deviceInterfaceData = new Struct.CloseableSpDeviceInterfaceData();
                 Struct.CloseableSpDevinfoData info = new Struct.CloseableSpDevinfoData()) {
                deviceInterfaceData.cbSize = deviceInterfaceData.size();

                for (int memberIndex = 0; SU.SetupDiEnumDeviceInfo(hDevInfo, memberIndex, info); memberIndex++) {
                    WinReg.HKEY key = SU.SetupDiOpenDevRegKey(hDevInfo, info, SetupApi.DICS_FLAG_GLOBAL, 0, SetupApi.DIREG_DEV,
                            WinNT.KEY_QUERY_VALUE);

                    byte[] edid = new byte[1];

                    try (ByRef.CloseableIntByReference pType = new ByRef.CloseableIntByReference();
                         ByRef.CloseableIntByReference lpcbData = new ByRef.CloseableIntByReference()) {
                        if (ADV.RegQueryValueEx(key, "EDID", 0, pType, edid, lpcbData) == WinError.ERROR_MORE_DATA) {
                            edid = new byte[lpcbData.getValue()];
                            if (ADV.RegQueryValueEx(key, "EDID", 0, pType, edid, lpcbData) == WinError.ERROR_SUCCESS) {
                                Display display = new WindowsDisplay(edid);
                                displays.add(display);
                            }
                        }
                    }
                    Advapi32.INSTANCE.RegCloseKey(key);
                }
            }
            SU.SetupDiDestroyDeviceInfoList(hDevInfo);
        }
        return displays;
    }

}
