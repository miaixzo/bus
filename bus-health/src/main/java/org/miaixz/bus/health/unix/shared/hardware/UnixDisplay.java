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
package org.miaixz.bus.health.unix.shared.hardware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.Display;
import org.miaixz.bus.health.builtin.hardware.common.AbstractDisplay;
import org.miaixz.bus.health.unix.shared.driver.Xrandr;

/**
 * Represents a display on a Unix-like system.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class UnixDisplay extends AbstractDisplay {

    /**
     * The platform-specific device port name.
     */
    private final String devicePort;

    /**
     * The DRM connector identifier, or {@code -1} when it is unavailable.
     */
    private final int connectorId;

    /**
     * The shared xrandr display data supplier.
     */
    private final SupplierX<Map<String, Pair<Integer, byte[]>>> xrandrData;

    /**
     * Constructor for UnixDisplay.
     *
     * @param edid a byte array representing a display EDID (Extended Display Identification Data).
     */
    public UnixDisplay(byte[] edid) {
        this(edid, Normal.UNKNOWN, -1);
    }

    /**
     * Constructor for UnixDisplay with a device port and connector identifier.
     *
     * @param edid        a byte array representing a display EDID (Extended Display Identification Data)
     * @param devicePort  the platform-specific device port name
     * @param connectorId the DRM connector identifier, or {@code -1} when it is unavailable
     */
    public UnixDisplay(byte[] edid, String devicePort, int connectorId) {
        this(edid, devicePort, connectorId, Memoizer.memoize(Xrandr::getDisplayData));
    }

    /**
     * Constructor for UnixDisplay with shared xrandr data.
     *
     * @param edid       a byte array representing a display EDID (Extended Display Identification Data)
     * @param devicePort the platform-specific device port name
     * @param connector  the DRM connector identifier, or {@code -1} when it is unavailable
     * @param xrandrData the shared xrandr display data supplier
     */
    private UnixDisplay(byte[] edid, String devicePort, int connector,
            SupplierX<Map<String, Pair<Integer, byte[]>>> xrandrData) {
        super(edid);
        this.devicePort = devicePort;
        this.connectorId = connector;
        this.xrandrData = xrandrData;
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
     * Gets the X11 output name for this display.
     *
     * @return the X11 output name, or an empty optional if unavailable
     */
    @Override
    public Optional<String> getOutputName() {
        return Xrandr.findOutputName(this.xrandrData.get(), this.connectorId, this.getDisplayInfo().getEdid());
    }

    /**
     * Gets display information.
     *
     * @return A list of {@link Display} objects representing monitors and other display devices.
     */
    public static List<Display> getDisplays() {
        Map<String, Pair<Integer, byte[]>> data = Xrandr.getDisplayData();
        List<Display> displays = new ArrayList<>(data.size());
        SupplierX<Map<String, Pair<Integer, byte[]>>> sharedData = () -> data;
        for (Map.Entry<String, Pair<Integer, byte[]>> entry : data.entrySet()) {
            displays.add(
                    new UnixDisplay(entry.getValue().getRight(), entry.getKey(), entry.getValue().getLeft(),
                            sharedData));
        }
        return displays;
    }

    /**
     * Gets display objects from DRM sysfs data.
     *
     * @param drmData the DRM connector name, connector identifier, and EDID triplets
     * @return A list of {@link Display} objects representing monitors and other display devices.
     */
    public static List<Display> getDisplays(List<Triplet<String, Integer, byte[]>> drmData) {
        return getDisplays(drmData, Xrandr::getDisplayData);
    }

    /**
     * Gets display objects from DRM sysfs data with a custom xrandr data supplier.
     *
     * @param drmData     the DRM connector name, connector identifier, and EDID triplets
     * @param xrandrQuery the xrandr data supplier
     * @return A list of {@link Display} objects representing monitors and other display devices.
     */
    static List<Display> getDisplays(
            List<Triplet<String, Integer, byte[]>> drmData,
            SupplierX<Map<String, Pair<Integer, byte[]>>> xrandrQuery) {
        List<Display> displays = new ArrayList<>(drmData.size());
        SupplierX<Map<String, Pair<Integer, byte[]>>> sharedData = Memoizer.memoize(xrandrQuery);
        for (Triplet<String, Integer, byte[]> drm : drmData) {
            displays.add(new UnixDisplay(drm.getRight(), drm.getLeft(), drm.getMiddle(), sharedData));
        }
        return displays;
    }

}
