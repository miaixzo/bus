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
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.GraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.driver.wmi.Win32VideoController;
import org.miaixz.bus.health.windows.driver.wmi.Win32VideoController.VideoControllerProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Graphics Card obtained from WMI
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class WindowsGraphicsCard extends AbstractGraphicsCard {

    public static final String ADAPTER_STRING = "HardwareInformation.AdapterString";
    public static final String DRIVER_DESC = "DriverDesc";
    public static final String DRIVER_VERSION = "DriverVersion";
    public static final String VENDOR = "ProviderName";
    public static final String QW_MEMORY_SIZE = "HardwareInformation.qwMemorySize";
    public static final String MEMORY_SIZE = "HardwareInformation.MemorySize";
    public static final String DISPLAY_DEVICES_REGISTRY_PATH = "SYSTEM\\CurrentControlSet\\Control\\Class\\{4d36e968-e325-11ce-bfc1-08002be10318}\\";
    private static final boolean IS_VISTA_OR_GREATER = VersionHelpers.IsWindowsVistaOrGreater();

    /**
     * Constructor for WindowsGraphicsCard
     *
     * @param name        The name
     * @param deviceId    The device ID
     * @param vendor      The vendor
     * @param versionInfo The version info
     * @param vram        The VRAM
     */
    WindowsGraphicsCard(String name, String deviceId, String vendor, String versionInfo, long vram) {
        super(name, deviceId, vendor, versionInfo, vram);
    }

    /**
     * public method used by {@link AbstractHardwareAbstractionLayer} to access the graphics cards.
     *
     * @return List of {@link WindowsGraphicsCard} objects.
     */
    public static List<GraphicsCard> getGraphicsCards() {
        List<GraphicsCard> cardList = new ArrayList<>();

        int index = 1;
        String[] keys = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, DISPLAY_DEVICES_REGISTRY_PATH);
        for (String key : keys) {
            if (!key.startsWith("0")) {
                continue;
            }

            try {
                String fullKey = DISPLAY_DEVICES_REGISTRY_PATH + key;
                if (!Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, fullKey, ADAPTER_STRING)) {
                    continue;
                }

                String name = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, DRIVER_DESC);
                String deviceId = "VideoController" + index++;
                String vendor = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, VENDOR);
                String versionInfo = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, fullKey,
                        DRIVER_VERSION);
                long vram = 0L;

                if (Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, fullKey, QW_MEMORY_SIZE)) {
                    vram = Advapi32Util.registryGetLongValue(WinReg.HKEY_LOCAL_MACHINE, fullKey, QW_MEMORY_SIZE);
                } else if (Advapi32Util.registryValueExists(WinReg.HKEY_LOCAL_MACHINE, fullKey, MEMORY_SIZE)) {
                    Object genericValue = Advapi32Util.registryGetValue(WinReg.HKEY_LOCAL_MACHINE, fullKey,
                            MEMORY_SIZE);
                    if (genericValue instanceof Long) {
                        vram = (long) genericValue;
                    } else if (genericValue instanceof Integer) {
                        vram = Integer.toUnsignedLong((int) genericValue);
                    } else if (genericValue instanceof byte[] bytes) {
                        vram = Parsing.byteArrayToLong(bytes, bytes.length, false);
                    }
                }

                cardList.add(new WindowsGraphicsCard(StringKit.isBlank(name) ? Normal.UNKNOWN : name,
                        StringKit.isBlank(deviceId) ? Normal.UNKNOWN : deviceId,
                        StringKit.isBlank(vendor) ? Normal.UNKNOWN : vendor,
                        StringKit.isBlank(versionInfo) ? Normal.UNKNOWN : versionInfo, vram));
            } catch (Win32Exception e) {
                if (e.getErrorCode() != WinError.ERROR_ACCESS_DENIED) {
                    // Ignore access denied errors, re-throw others
                    throw e;
                }
            }
        }

        if (cardList.isEmpty()) {
            return getGraphicsCardsFromWmi();
        }
        return cardList;
    }

    // fall back if something went wrong
    private static List<GraphicsCard> getGraphicsCardsFromWmi() {
        List<GraphicsCard> cardList = new ArrayList<>();
        if (IS_VISTA_OR_GREATER) {
            WmiResult<VideoControllerProperty> cards = Win32VideoController.queryVideoController();
            for (int index = 0; index < cards.getResultCount(); index++) {
                String name = WmiKit.getString(cards, VideoControllerProperty.NAME, index);
                Triplet<String, String, String> idPair = Parsing.parseDeviceIdToVendorProductSerial(
                        WmiKit.getString(cards, VideoControllerProperty.PNPDEVICEID, index));
                String deviceId = idPair == null ? Normal.UNKNOWN : idPair.getMiddle();
                String vendor = WmiKit.getString(cards, VideoControllerProperty.ADAPTERCOMPATIBILITY, index);
                if (idPair != null) {
                    if (StringKit.isBlank(vendor)) {
                        deviceId = idPair.getLeft();
                    } else {
                        vendor = vendor + " (" + idPair.getLeft() + Symbol.PARENTHESE_RIGHT;
                    }
                }
                String versionInfo = WmiKit.getString(cards, VideoControllerProperty.DRIVERVERSION, index);
                if (!StringKit.isBlank(versionInfo)) {
                    versionInfo = "DriverVersion=" + versionInfo;
                } else {
                    versionInfo = Normal.UNKNOWN;
                }
                long vram = WmiKit.getUint32asLong(cards, VideoControllerProperty.ADAPTERRAM, index);
                cardList.add(new WindowsGraphicsCard(StringKit.isBlank(name) ? Normal.UNKNOWN : name, deviceId,
                        StringKit.isBlank(vendor) ? Normal.UNKNOWN : vendor, versionInfo, vram));
            }
        }
        return cardList;
    }

}
