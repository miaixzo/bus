/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.linux.hardware;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.GraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;

/**
 * Graphics card info obtained by lshw
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class LinuxGraphicsCard extends AbstractGraphicsCard {

    /**
     * Constructor for LinuxGraphicsCard
     *
     * @param name        The name
     * @param deviceId    The device ID
     * @param vendor      The vendor
     * @param versionInfo The version info
     * @param vram        The VRAM
     */
    LinuxGraphicsCard(String name, String deviceId, String vendor, String versionInfo, long vram) {
        super(name, deviceId, vendor, versionInfo, vram);
    }

    /**
     * public method used by {@link AbstractHardwareAbstractionLayer} to access the graphics cards.
     *
     * @return List of {@link LinuxGraphicsCard} objects.
     */
    public static List<GraphicsCard> getGraphicsCards() {
        List<GraphicsCard> cardList = getGraphicsCardsFromLspci();
        if (cardList.isEmpty()) {
            cardList = getGraphicsCardsFromLshw();
        }
        return cardList;
    }

    // Faster, use as primary
    private static List<GraphicsCard> getGraphicsCardsFromLspci() {
        List<GraphicsCard> cardList = new ArrayList<>();
        // Machine readable version
        List<String> lspci = Executor.runNative("lspci -vnnm");
        String name = Normal.UNKNOWN;
        String deviceId = Normal.UNKNOWN;
        String vendor = Normal.UNKNOWN;
        List<String> versionInfoList = new ArrayList<>();
        boolean found = false;
        String lookupDevice = null;
        for (String line : lspci) {
            String[] split = line.trim().split(Symbol.COLON, 2);
            String prefix = split[0];
            // Skip until line contains "VGA"
            if (prefix.equals("Class") && line.contains("VGA")) {
                found = true;
            } else if (prefix.equals("Device") && !found && split.length > 1) {
                lookupDevice = split[1].trim();
            }
            if (found) {
                if (split.length < 2) {
                    // Save previous card
                    cardList.add(new LinuxGraphicsCard(name, deviceId, vendor,
                            versionInfoList.isEmpty() ? Normal.UNKNOWN : String.join(", ", versionInfoList),
                            queryLspciMemorySize(lookupDevice)));
                    versionInfoList.clear();
                    found = false;
                } else {
                    if (prefix.equals("Device")) {
                        Pair<String, String> pair = Parsing.parseLspciMachineReadable(split[1].trim());
                        if (pair != null) {
                            name = pair.getLeft();
                            deviceId = "0x" + pair.getRight();
                        }
                    } else if (prefix.equals("Vendor")) {
                        Pair<String, String> pair = Parsing.parseLspciMachineReadable(split[1].trim());
                        if (pair != null) {
                            vendor = pair.getLeft() + " (0x" + pair.getRight() + Symbol.PARENTHESE_RIGHT;
                        } else {
                            vendor = split[1].trim();
                        }
                    } else if (prefix.equals("Rev:")) {
                        versionInfoList.add(line.trim());
                    }
                }
            }
        }
        // If we haven't yet written the last card do so now
        if (found) {
            cardList.add(new LinuxGraphicsCard(name, deviceId, vendor,
                    versionInfoList.isEmpty() ? Normal.UNKNOWN : String.join(", ", versionInfoList),
                    queryLspciMemorySize(lookupDevice)));
        }
        return cardList;
    }

    private static long queryLspciMemorySize(String lookupDevice) {
        long vram = 0L;
        // Lookup memory
        // Human readable version, includes memory
        List<String> lspciMem = Executor.runNative("lspci -v -s " + lookupDevice);
        for (String mem : lspciMem) {
            if (mem.contains(" prefetchable")) {
                vram += Parsing.parseLspciMemorySize(mem);
            }
        }
        return vram;
    }

    // Slower, use as backup
    private static List<GraphicsCard> getGraphicsCardsFromLshw() {
        List<GraphicsCard> cardList = new ArrayList<>();
        List<String> lshw = Executor.runNative("lshw -C display");
        String name = Normal.UNKNOWN;
        String deviceId = Normal.UNKNOWN;
        String vendor = Normal.UNKNOWN;
        List<String> versionInfoList = new ArrayList<>();
        long vram = 0;
        int cardNum = 0;
        for (String line : lshw) {
            String[] split = line.trim().split(Symbol.COLON);
            if (split[0].startsWith("*-display")) {
                // Save previous card
                if (cardNum++ > 0) {
                    cardList.add(new LinuxGraphicsCard(name, deviceId, vendor,
                            versionInfoList.isEmpty() ? Normal.UNKNOWN : String.join(", ", versionInfoList), vram));
                    versionInfoList.clear();
                }
            } else if (split.length == 2) {
                String prefix = split[0];
                if (prefix.equals("product")) {
                    name = split[1].trim();
                } else if (prefix.equals("vendor")) {
                    vendor = split[1].trim();
                } else if (prefix.equals("version")) {
                    versionInfoList.add(line.trim());
                } else if (prefix.startsWith("resources")) {
                    vram = Parsing.parseLshwResourceString(split[1].trim());
                }
            }
        }
        cardList.add(new LinuxGraphicsCard(name, deviceId, vendor,
                versionInfoList.isEmpty() ? Normal.UNKNOWN : String.join(", ", versionInfoList), vram));
        return cardList;
    }
}
