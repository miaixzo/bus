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

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.builtin.hardware.Display;
import org.miaixz.bus.health.builtin.hardware.common.AbstractDisplay;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.platform.win32.*;

/**
 * A Display
 *
 * @author Kimi Liu
 * @since Java 21+
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

        WinNT.HANDLE hDevInfo = SU.SetupDiGetClassDevs(
                GUID_DEVINTERFACE_MONITOR,
                null,
                null,
                SetupApi.DIGCF_PRESENT | SetupApi.DIGCF_DEVICEINTERFACE);
        if (!hDevInfo.equals(WinBase.INVALID_HANDLE_VALUE)) {
            try (Struct.CloseableSpDeviceInterfaceData deviceInterfaceData = new Struct.CloseableSpDeviceInterfaceData();
                    Struct.CloseableSpDevinfoData info = new Struct.CloseableSpDevinfoData()) {
                deviceInterfaceData.cbSize = deviceInterfaceData.size();

                for (int memberIndex = 0; SU.SetupDiEnumDeviceInfo(hDevInfo, memberIndex, info); memberIndex++) {
                    WinReg.HKEY key = SU.SetupDiOpenDevRegKey(
                            hDevInfo,
                            info,
                            SetupApi.DICS_FLAG_GLOBAL,
                            0,
                            SetupApi.DIREG_DEV,
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
