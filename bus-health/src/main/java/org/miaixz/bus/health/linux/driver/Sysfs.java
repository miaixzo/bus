/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI Team and other contributors.          *
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
package org.miaixz.bus.health.linux.driver;

import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.linux.SysPath;

/**
 * Utility to read info from {@code sysfs}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class Sysfs {

    /**
     * Query the vendor from sysfs
     *
     * @return The vendor if available, null otherwise
     */
    public static String querySystemVendor() {
        final String sysVendor = Builder.getStringFromFile(SysPath.DMI_ID + "sys_vendor").trim();
        if (!sysVendor.isEmpty()) {
            return sysVendor;
        }
        return null;
    }

    /**
     * Query the model from sysfs
     *
     * @return The model if available, null otherwise
     */
    public static String queryProductModel() {
        final String productName = Builder.getStringFromFile(SysPath.DMI_ID + "product_name").trim();
        final String productVersion = Builder.getStringFromFile(SysPath.DMI_ID + "product_version").trim();
        if (productName.isEmpty()) {
            if (!productVersion.isEmpty()) {
                return productVersion;
            }
        } else {
            if (!productVersion.isEmpty() && !"None".equals(productVersion)) {
                return productName + " (version: " + productVersion + Symbol.PARENTHESE_RIGHT;
            }
            return productName;
        }
        return null;
    }

    /**
     * Query the product serial number from sysfs
     *
     * @return The serial number if available, null otherwise
     */
    public static String queryProductSerial() {
        // These sysfs files accessible by root, or can be chmod'd at boot time
        // to enable access without root
        String serial = Builder.getStringFromFile(SysPath.DMI_ID + "product_serial");
        if (!serial.isEmpty() && !"None".equals(serial)) {
            return serial;
        }
        return queryBoardSerial();
    }

    /**
     * Query the UUID from sysfs
     *
     * @return The UUID if available, null otherwise
     */
    public static String queryUUID() {
        // These sysfs files accessible by root, or can be chmod'd at boot time
        // to enable access without root
        String uuid = Builder.getStringFromFile(SysPath.DMI_ID + "product_uuid");
        if (!uuid.isEmpty() && !"None".equals(uuid)) {
            return uuid;
        }
        return null;
    }

    /**
     * Query the board vendor from sysfs
     *
     * @return The board vendor if available, null otherwise
     */
    public static String queryBoardVendor() {
        final String boardVendor = Builder.getStringFromFile(SysPath.DMI_ID + "board_vendor").trim();
        if (!boardVendor.isEmpty()) {
            return boardVendor;
        }
        return null;
    }

    /**
     * Query the board model from sysfs
     *
     * @return The board model if available, null otherwise
     */
    public static String queryBoardModel() {
        final String boardName = Builder.getStringFromFile(SysPath.DMI_ID + "board_name").trim();
        if (!boardName.isEmpty()) {
            return boardName;
        }
        return null;
    }

    /**
     * Query the board version from sysfs
     *
     * @return The board version if available, null otherwise
     */
    public static String queryBoardVersion() {
        final String boardVersion = Builder.getStringFromFile(SysPath.DMI_ID + "board_version").trim();
        if (!boardVersion.isEmpty()) {
            return boardVersion;
        }
        return null;
    }

    /**
     * Query the board serial number from sysfs
     *
     * @return The board serial number if available, null otherwise
     */
    public static String queryBoardSerial() {
        final String boardSerial = Builder.getStringFromFile(SysPath.DMI_ID + "board_serial").trim();
        if (!boardSerial.isEmpty()) {
            return boardSerial;
        }
        return null;
    }

    /**
     * Query the bios vendor from sysfs
     *
     * @return The bios vendor if available, null otherwise
     */
    public static String queryBiosVendor() {
        final String biosVendor = Builder.getStringFromFile(SysPath.DMI_ID + "bios_vendor").trim();
        if (biosVendor.isEmpty()) {
            return biosVendor;
        }
        return null;
    }

    /**
     * Query the bios description from sysfs
     *
     * @return The bios description if available, null otherwise
     */
    public static String queryBiosDescription() {
        final String modalias = Builder.getStringFromFile(SysPath.DMI_ID + "modalias").trim();
        if (!modalias.isEmpty()) {
            return modalias;
        }
        return null;
    }

    /**
     * Query the bios version from sysfs
     *
     * @param biosRevision A revision string to append
     * @return The bios version if available, null otherwise
     */
    public static String queryBiosVersion(String biosRevision) {
        final String biosVersion = Builder.getStringFromFile(SysPath.DMI_ID + "bios_version").trim();
        if (!biosVersion.isEmpty()) {
            return biosVersion + (StringKit.isBlank(biosRevision) ? Normal.EMPTY : " (revision " + biosRevision + Symbol.PARENTHESE_RIGHT);
        }
        return null;
    }

    /**
     * Query the bios release date from sysfs
     *
     * @return The bios release date if available, null otherwise
     */
    public static String queryBiosReleaseDate() {
        final String biosDate = Builder.getStringFromFile(SysPath.DMI_ID + "bios_date").trim();
        if (!biosDate.isEmpty()) {
            return Parsing.parseMmDdYyyyToYyyyMmDD(biosDate);
        }
        return null;
    }

}
