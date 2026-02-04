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
package org.miaixz.bus.health.linux.driver;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
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
            return biosVersion + (StringKit.isBlank(biosRevision) ? Normal.EMPTY
                    : " (revision " + biosRevision + Symbol.PARENTHESE_RIGHT);
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
