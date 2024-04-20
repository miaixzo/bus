/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.health.windows.hardware;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import com.sun.jna.platform.win32.VersionHelpers;
import org.miaixz.bus.core.annotation.Immutable;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.tuple.Triple;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.builtin.hardware.AbstractGraphicsCard;
import org.miaixz.bus.health.builtin.hardware.AbstractHardwareAbstractionLayer;
import org.miaixz.bus.health.builtin.hardware.GraphicsCard;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.drivers.wmi.Win32VideoController;

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
     * public method used by
     * {@link AbstractHardwareAbstractionLayer} to access the
     * graphics cards.
     *
     * @return List of {@link WindowsGraphicsCard}
     * objects.
     */
    public static List<GraphicsCard> getGraphicsCards() {
        List<GraphicsCard> cardList = new ArrayList<>();
        if (IS_VISTA_OR_GREATER) {
            WmiResult<Win32VideoController.VideoControllerProperty> cards = Win32VideoController.queryVideoController();
            for (int index = 0; index < cards.getResultCount(); index++) {
                String name = WmiKit.getString(cards, Win32VideoController.VideoControllerProperty.NAME, index);
                Triple<String, String, String> idPair = Builder.parseDeviceIdToVendorProductSerial(
                        WmiKit.getString(cards, Win32VideoController.VideoControllerProperty.PNPDEVICEID, index));
                String deviceId = idPair == null ? Normal.UNKNOWN : idPair.getMiddle();
                String vendor = WmiKit.getString(cards, Win32VideoController.VideoControllerProperty.ADAPTERCOMPATIBILITY, index);
                if (idPair != null) {
                    if (StringKit.isBlank(vendor)) {
                        deviceId = idPair.getLeft();
                    } else {
                        vendor = vendor + " (" + idPair.getLeft() + ")";
                    }
                }
                String versionInfo = WmiKit.getString(cards, Win32VideoController.VideoControllerProperty.DRIVERVERSION, index);
                if (!StringKit.isBlank(versionInfo)) {
                    versionInfo = "DriverVersion=" + versionInfo;
                } else {
                    versionInfo = Normal.UNKNOWN;
                }
                long vram = WmiKit.getUint32asLong(cards, Win32VideoController.VideoControllerProperty.ADAPTERRAM, index);
                cardList.add(new WindowsGraphicsCard(StringKit.isBlank(name) ? Normal.UNKNOWN : name, deviceId,
                        StringKit.isBlank(vendor) ? Normal.UNKNOWN : vendor, versionInfo, vram));
            }
        }
        return cardList;
    }

}
