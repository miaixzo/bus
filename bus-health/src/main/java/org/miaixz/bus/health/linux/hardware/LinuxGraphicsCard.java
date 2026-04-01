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
package org.miaixz.bus.health.linux.hardware;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.GpuStats;
import org.miaixz.bus.health.builtin.hardware.GraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGraphicsCard;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHardwareAbstractionLayer;

/**
 * Graphics card info obtained by lshw
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
final class LinuxGraphicsCard extends AbstractGraphicsCard {

    private static final String DRM_PATH = "/sys/class/drm/";

    // sysfs path for this card's device directory, e.g. /sys/class/drm/card0/device
    // Empty string if this card has no associated DRM sysfs entry.
    private final String drmDevicePath;

    // Driver name detected from the sysfs driver symlink, e.g. "amdgpu", "i915", "xe", "nvidia"
    private final String driverName;

    // PCI bus ID string for NVML correlation, e.g. "0000:01:00.0". Empty if unknown.
    private final String pciBusId;

    /**
     * Constructor for LinuxGraphicsCard
     *
     * @param name          The name
     * @param deviceId      The device ID
     * @param vendor        The vendor
     * @param versionInfo   The version info
     * @param vram          The VRAM
     * @param drmDevicePath sysfs device path for this card, or empty string if unavailable
     * @param driverName    driver name (e.g. "amdgpu"), or empty string if unknown
     * @param pciBusId      PCI bus ID for NVML correlation, or empty string if unknown
     */
    LinuxGraphicsCard(String name, String deviceId, String vendor, String versionInfo, long vram, String drmDevicePath,
            String driverName, String pciBusId) {
        super(name, deviceId, vendor, versionInfo, vram);
        this.drmDevicePath = drmDevicePath;
        this.driverName = driverName;
        this.pciBusId = pciBusId;
    }

    @Override
    public GpuStats createStatsSession() {
        return new LinuxGpuStats(drmDevicePath, driverName, pciBusId, getName());
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
            String[] split = line.trim().split(":", 2);
            String prefix = split[0];
            // Skip until line contains "VGA" or "3D controller"
            if (prefix.equals("Class") && (line.contains("VGA") || line.contains("3D controller"))) {
                found = true;
                lookupDevice = null;
                name = Normal.UNKNOWN;
                deviceId = Normal.UNKNOWN;
                vendor = Normal.UNKNOWN;
                versionInfoList.clear();
            } else if (prefix.equals("Slot") && split.length > 1) {
                // Capture PCI slot address (e.g. "01:00.0") for use with lspci -s
                lookupDevice = split[1].trim();
            }
            if (found) {
                if (split.length < 2) {
                    // Save previous card
                    Triplet<String, String, String> drmInfo = findDrmInfo(lookupDevice);
                    cardList.add(
                            new LinuxGraphicsCard(name, deviceId, vendor,
                                    versionInfoList.isEmpty() ? Normal.UNKNOWN : String.join(", ", versionInfoList),
                                    lookupDevice != null ? queryLspciMemorySize(lookupDevice) : 0L, drmInfo.getLeft(),
                                    drmInfo.getMiddle(), drmInfo.getRight()));
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
                            vendor = pair.getLeft() + " (0x" + pair.getLeft() + ")";
                        } else {
                            vendor = split[1].trim();
                        }
                    } else if (prefix.equals("Rev")) {
                        versionInfoList.add(line.trim());
                    }
                }
            }
        }
        // If we haven't yet written the last card do so now
        if (found) {
            Triplet<String, String, String> drmInfo = findDrmInfo(lookupDevice);
            cardList.add(
                    new LinuxGraphicsCard(name, deviceId, vendor,
                            versionInfoList.isEmpty() ? Normal.UNKNOWN : String.join(", ", versionInfoList),
                            lookupDevice != null ? queryLspciMemorySize(lookupDevice) : 0L, drmInfo.getLeft(),
                            drmInfo.getMiddle(), drmInfo.getRight()));
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
        String busInfo = null;
        for (String line : lshw) {
            String[] split = line.trim().split(":", 2);
            if (split[0].startsWith("*-display")) {
                // Save previous card
                if (cardNum++ > 0) {
                    Triplet<String, String, String> drmInfo = findDrmInfo(busInfo);
                    cardList.add(
                            new LinuxGraphicsCard(name, deviceId, vendor,
                                    versionInfoList.isEmpty() ? Normal.UNKNOWN : String.join(", ", versionInfoList),
                                    vram, drmInfo.getLeft(), drmInfo.getMiddle(), drmInfo.getRight()));
                }
                name = Normal.UNKNOWN;
                deviceId = Normal.UNKNOWN;
                vendor = Normal.UNKNOWN;
                vram = 0;
                versionInfoList.clear();
                busInfo = null;
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
                } else if (prefix.equals("bus info")) {
                    // lshw reports PCI slot as "pci@0000:01:00.0"; the value contains multiple
                    // colons so we locate the first colon in the original line to get the full value.
                    int colonIdx = line.indexOf(':');
                    String raw = colonIdx >= 0 ? line.substring(colonIdx + 1).trim() : "";
                    busInfo = raw.startsWith("pci@") ? raw.substring(4) : raw;
                }
            }
        }
        if (cardNum > 0) {
            Triplet<String, String, String> drmInfo = findDrmInfo(busInfo);
            cardList.add(
                    new LinuxGraphicsCard(name, deviceId, vendor,
                            versionInfoList.isEmpty() ? Normal.UNKNOWN : String.join(", ", versionInfoList), vram,
                            drmInfo.getLeft(), drmInfo.getMiddle(), drmInfo.getRight()));
        }
        return cardList;
    }

    /**
     * Finds the sysfs DRM device path, driver name, and PCI bus ID for a GPU by matching against the PCI slot address
     * from the uevent file under each DRM card's device directory.
     *
     * <p>
     * When {@code pciSlot} is non-null, each card's {@code device/uevent} file is read and the {@code PCI_SLOT_NAME}
     * key is compared against the supplied slot (e.g. {@code "0000:01:00.0"} or {@code "01:00.0"}). The first card
     * whose slot matches is returned. If no match is found, or if {@code pciSlot} is null (lshw path), the first card
     * with a non-empty driver symlink is returned as a best-effort fallback.
     *
     * @param pciSlot the PCI slot address from lspci (e.g. {@code "01:00.0"}), or {@code null} to use first-match
     * @return triplet of (drmDevicePath, driverName, pciBusId), all empty strings if not found
     */
    private static Triplet<String, String, String> findDrmInfo(String pciSlot) {
        File drmDir = new File(DRM_PATH);
        File[] cards = drmDir.listFiles(f -> f.getName().matches("card\\d+"));
        if (cards == null) {
            return new Triplet<>("", "", "");
        }
        Triplet<String, String, String> firstWithDriver = null;
        for (File card : cards) {
            String devicePath = card.getAbsolutePath() + "/device";
            String driver = readDriverName(devicePath + "/driver");
            if (driver.isEmpty()) {
                continue;
            }
            String slotName = readUeventValue(devicePath + "/uevent", "PCI_SLOT_NAME");
            if (firstWithDriver == null) {
                firstWithDriver = new Triplet<>(devicePath, driver, slotName);
            }
            // Attempt PCI slot match via uevent
            if (pciSlot != null && slotName.endsWith(pciSlot)) {
                return new Triplet<>(devicePath, driver, slotName);
            }
        }
        // Fall back to first card with a driver symlink
        return firstWithDriver != null ? firstWithDriver : new Triplet<>("", "", "");
    }

    /**
     * Reads a key=value entry from a sysfs uevent file.
     *
     * @param ueventPath absolute path to the uevent file
     * @param key        the key to look up (e.g. {@code "PCI_SLOT_NAME"})
     * @return the value string, or empty string if not found
     */
    private static String readUeventValue(String ueventPath, String key) {
        List<String> lines = Builder.readFile(ueventPath);
        String prefix = key + "=";
        for (String line : lines) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        return "";
    }

    private static String readDriverName(String driverSymlink) {
        String target = Builder.readSymlinkTarget(new File(driverSymlink));
        if (target == null || target.isEmpty()) {
            return Normal.EMPTY;
        }
        // The symlink target resolves to a driver directory,
        // e.g. "../../../bus/pci/drivers/amdgpu"; the last path segment is the driver name.
        int lastSlash = target.lastIndexOf('/');
        return lastSlash >= 0 ? target.substring(lastSlash + 1) : target;
    }

}
