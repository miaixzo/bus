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
package org.miaixz.bus.health.windows.hardware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.builtin.hardware.Display;
import org.miaixz.bus.health.builtin.hardware.common.AbstractDisplay;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.health.windows.driver.DisplayConnector;
import org.miaixz.bus.health.windows.jna.User32;
import org.miaixz.bus.logger.Logger;

/**
 * A Display
 *
 * @author Kimi Liu
 */
@Immutable
final class WindowsDisplay extends AbstractDisplay {

    /**
     * The SU constant.
     */
    private static final SetupApi SU = SetupApi.INSTANCE;

    /**
     * The ADV constant.
     */
    private static final Advapi32 ADV = Advapi32.INSTANCE;

    /**
     * The GUID_DEVINTERFACE_MONITOR constant.
     */
    private static final Guid.GUID GUID_DEVINTERFACE_MONITOR = new Guid.GUID("E6F07B5F-EE97-4a90-B076-33F57BF4EAA7");

    /**
     * The {@code SP_DEVICE_INTERFACE_DETAIL_DATA.cbSize} value.
     */
    private static final int DETAIL_CBSIZE = Native.POINTER_SIZE == 8 ? 8 : 6;

    /**
     * The platform-specific device port name.
     */
    private final String devicePort;

    /**
     * Constructor for WindowsDisplay.
     *
     * @param edid a byte array representing a display EDID
     */
    WindowsDisplay(byte[] edid) {
        this(edid, Normal.UNKNOWN);
    }

    /**
     * Constructor for WindowsDisplay with a device port.
     *
     * @param edid       a byte array representing a display EDID
     * @param devicePort the connector this display is attached to
     */
    WindowsDisplay(byte[] edid, String devicePort) {
        super(edid);
        this.devicePort = devicePort;
        Logger.debug(false, "Health", "Initialized WindowsDisplay");
    }

    /**
     * Gets the platform-specific device port name.
     *
     * @return the platform-specific device port name
     */
    @Override
    public String getDevicePort() {
        return this.devicePort;
    }

    /**
     * Gets Display Information
     *
     * @return An array of Display objects representing monitors, etc.
     */
    public static List<Display> getDisplays() {
        List<Display> displays = new ArrayList<>();
        Map<String, String> portByPath = queryConnectorPorts();

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

                    try {
                        byte[] edid = new byte[1];

                        try (ByRef.CloseableIntByReference pType = new ByRef.CloseableIntByReference();
                                ByRef.CloseableIntByReference lpcbData = new ByRef.CloseableIntByReference()) {
                            if (ADV.RegQueryValueEx(
                                    key,
                                    "EDID",
                                    0,
                                    pType,
                                    edid,
                                    lpcbData) == WinError.ERROR_MORE_DATA) {
                                edid = new byte[lpcbData.getValue()];
                                if (ADV.RegQueryValueEx(
                                        key,
                                        "EDID",
                                        0,
                                        pType,
                                        edid,
                                        lpcbData) == WinError.ERROR_SUCCESS) {
                                    String port = lookupPort(hDevInfo, info, deviceInterfaceData, portByPath);
                                    Display display = new WindowsDisplay(edid, port);
                                    displays.add(display);
                                }
                            }
                        }
                    } finally {
                        Advapi32.INSTANCE.RegCloseKey(key);
                    }
                }
            } finally {
                SU.SetupDiDestroyDeviceInfoList(hDevInfo);
            }
        }
        return displays;
    }

    /**
     * Resolves the connector name for the current device by querying its device interface path.
     *
     * @param hDevInfo            the device information set handle
     * @param info                the device information data
     * @param deviceInterfaceData the device interface data
     * @param portByPath          the normalized device path to port name map
     * @return the connector name, or {@link Normal#UNKNOWN} if unavailable
     */
    private static String lookupPort(
            WinNT.HANDLE hDevInfo,
            Struct.CloseableSpDevinfoData info,
            Struct.CloseableSpDeviceInterfaceData deviceInterfaceData,
            Map<String, String> portByPath) {
        if (!SU.SetupDiEnumDeviceInterfaces(
                hDevInfo,
                info.getPointer(),
                GUID_DEVINTERFACE_MONITOR,
                0,
                deviceInterfaceData)) {
            return Normal.UNKNOWN;
        }
        String path = getDeviceInterfacePath(hDevInfo, deviceInterfaceData);
        if (path == null) {
            return Normal.UNKNOWN;
        }
        return portByPath.getOrDefault(DisplayConnector.normalizePath(path), Normal.UNKNOWN);
    }

    /**
     * Reads the device interface path using the two-call SetupAPI pattern.
     *
     * @param hDevInfo            the device information set handle
     * @param deviceInterfaceData the device interface data
     * @return the device interface path, or {@code null} if unavailable
     */
    private static String getDeviceInterfacePath(
            WinNT.HANDLE hDevInfo,
            Struct.CloseableSpDeviceInterfaceData deviceInterfaceData) {
        try (ByRef.CloseableIntByReference requiredSize = new ByRef.CloseableIntByReference()) {
            SU.SetupDiGetDeviceInterfaceDetail(hDevInfo, deviceInterfaceData, null, 0, requiredSize, null);
            int size = requiredSize.getValue();
            if (size <= Normal._4) {
                return null;
            }
            try (Memory detail = new Memory(size)) {
                detail.clear();
                detail.setInt(0, DETAIL_CBSIZE);
                if (SU.SetupDiGetDeviceInterfaceDetail(
                        hDevInfo,
                        deviceInterfaceData,
                        detail,
                        size,
                        requiredSize,
                        null)) {
                    return detail.getWideString(Normal._4);
                }
            }
        }
        return null;
    }

    /**
     * Builds a map from monitor device interface path to connector name.
     *
     * @return the normalized device path to connector name map
     */
    private static Map<String, String> queryConnectorPorts() {
        User32 user32 = User32.INSTANCE;
        for (int attempt = Normal._0; attempt < Normal._3; attempt++) {
            Map<String, String> map = queryConnectorPortsOnce(user32);
            if (map != null) {
                return map;
            }
        }
        Logger.debug(false, "Health", "Display configuration kept changing; unable to map connectors.");
        return new HashMap<>();
    }

    /**
     * Builds the connector map once, returning {@code null} when the buffers must be resized and retried.
     *
     * @param user32 the User32 binding
     * @return the connector map, or {@code null} when the caller should retry
     */
    private static Map<String, String> queryConnectorPortsOnce(User32 user32) {
        Map<String, String> map = new HashMap<>();
        try (ByRef.CloseableIntByReference numPaths = new ByRef.CloseableIntByReference();
                ByRef.CloseableIntByReference numModes = new ByRef.CloseableIntByReference()) {
            if (user32.GetDisplayConfigBufferSizes(Normal._2, numPaths, numModes) != WinError.ERROR_SUCCESS) {
                return map;
            }
            int pathCount = numPaths.getValue();
            int modeCount = numModes.getValue();
            if (pathCount <= Normal._0) {
                return map;
            }
            try (Memory paths = new Memory((long) pathCount * Normal._72);
                    Memory modes = new Memory(Math.max((long) Normal._1, (long) modeCount * Normal._64))) {
                paths.clear();
                modes.clear();
                int rc = user32.QueryDisplayConfig(Normal._2, numPaths, paths, numModes, modes, null);
                if (rc == WinError.ERROR_INSUFFICIENT_BUFFER) {
                    return null;
                }
                if (rc != WinError.ERROR_SUCCESS) {
                    return map;
                }
                int actualPaths = numPaths.getValue();
                for (int i = Normal._0; i < actualPaths; i++) {
                    long base = (long) i * Normal._72;
                    int flags = paths.getInt(base + Normal._68);
                    if ((flags & Normal._1) == Normal._0) {
                        continue;
                    }
                    long adapterId = paths.getLong(base + Normal._20);
                    int targetId = paths.getInt(base + Normal._28);
                    addConnector(map, user32, adapterId, targetId);
                }
            }
        }
        return map;
    }

    /**
     * Adds one connector from a {@code DISPLAYCONFIG_TARGET_DEVICE_NAME} response.
     *
     * @param map       the normalized device path to connector name map
     * @param user32    the User32 binding
     * @param adapterId the target adapter identifier
     * @param targetId  the target identifier
     */
    private static void addConnector(Map<String, String> map, User32 user32, long adapterId, int targetId) {
        try (Memory targetDeviceName = new Memory(Normal._400 + Normal._20)) {
            targetDeviceName.clear();
            targetDeviceName.setInt(Normal._0, Normal._2);
            targetDeviceName.setInt(Normal._4, Normal._400 + Normal._20);
            targetDeviceName.setLong(Normal._8, adapterId);
            targetDeviceName.setInt(Normal._16, targetId);
            if (user32.DisplayConfigGetDeviceInfo(targetDeviceName) != WinError.ERROR_SUCCESS) {
                return;
            }
            int outputTechnology = targetDeviceName.getInt(Normal._24);
            int connectorInstance = targetDeviceName.getInt(Normal._32);
            String key = DisplayConnector
                    .normalizePath(targetDeviceName.getWideString(Normal._128 + Normal._32 + Normal._4));
            if (!Normal.UNKNOWN.equals(key)) {
                map.put(key, DisplayConnector.connectorName(outputTechnology, connectorInstance));
            }
        }
    }

}
