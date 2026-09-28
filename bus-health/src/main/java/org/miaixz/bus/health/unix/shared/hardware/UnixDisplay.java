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
import java.util.Optional;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.Display;
import org.miaixz.bus.health.builtin.hardware.DisplayMode;
import org.miaixz.bus.health.builtin.hardware.common.AbstractDisplay;
import org.miaixz.bus.health.unix.shared.driver.Xrandr;
import org.miaixz.bus.health.unix.shared.driver.Xrandr.Output;

/**
 * Represents a display on a Unix-like system.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class UnixDisplay extends AbstractDisplay {

    /** Built-in connector name prefixes. */
    private static final String[] BUILT_IN_CONNECTORS = { "eDP", "LVDS", "DSI" };

    /** External connector name prefixes. */
    private static final String[] EXTERNAL_CONNECTORS = { "DP", "DisplayPort", "HDMI", "DVI", "VGA", "TV", "Composite",
            "SVIDEO", "S-video", "Component", "DIN", "USB" };

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
    private final SupplierX<List<Output>> xrandrData;

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
        this(edid, devicePort, connectorId, Memoizer.memoize(Xrandr::getOutputs));
    }

    /**
     * Constructor for UnixDisplay with shared xrandr data.
     *
     * @param edid       a byte array representing a display EDID (Extended Display Identification Data)
     * @param devicePort the platform-specific device port name
     * @param connector  the DRM connector identifier, or {@code -1} when it is unavailable
     * @param xrandrData the shared xrandr display data supplier
     */
    private UnixDisplay(byte[] edid, String devicePort, int connector, SupplierX<List<Output>> xrandrData) {
        super(edid);
        this.devicePort = devicePort;
        this.connectorId = connector;
        this.xrandrData = xrandrData;
    }

    /**
     * Gets display information.
     *
     * @return A list of {@link Display} objects representing monitors and other display devices.
     */
    public static List<Display> getDisplays() {
        List<Output> outputs = Xrandr.getOutputs();
        List<Display> displays = new ArrayList<>(outputs.size());
        SupplierX<List<Output>> sharedData = () -> outputs;
        for (Output output : outputs) {
            displays.add(new UnixDisplay(output.getEdid(), output.getName(), output.getConnectorId(), sharedData));
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
        return getDisplays(drmData, Xrandr::getOutputs);
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
            SupplierX<List<Output>> xrandrQuery) {
        List<Display> displays = new ArrayList<>(drmData.size());
        SupplierX<List<Output>> sharedData = Memoizer.memoize(xrandrQuery);
        for (Triplet<String, Integer, byte[]> drm : drmData) {
            displays.add(new UnixDisplay(drm.getRight(), drm.getLeft(), drm.getMiddle(), sharedData));
        }
        return displays;
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
        return findOutput().map(Output::getName);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<DisplayMode> getCurrentMode() {
        return findOutput().flatMap(Output::getMode);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Boolean> isBuiltIn() {
        if (!Normal.UNKNOWN.equals(this.devicePort)) {
            return isBuiltInConnector(this.devicePort);
        }
        return getOutputName().flatMap(UnixDisplay::isBuiltInConnector);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Boolean> isPrimary() {
        return findOutput().map(Output::isPrimary);
    }

    /**
     * Finds the xrandr output matching this display.
     *
     * @return the matching output, or an empty optional
     */
    private Optional<Output> findOutput() {
        return Xrandr.findOutput(this.xrandrData.get(), this.connectorId, this.getDisplayInfo().getEdid());
    }

    /**
     * Classifies a connector by its standard DRM or X11 prefix.
     *
     * @param connector the connector or output name
     * @return the built-in status, or an empty optional when the connector type is ambiguous
     */
    static Optional<Boolean> isBuiltInConnector(String connector) {
        for (String prefix : BUILT_IN_CONNECTORS) {
            if (connector.startsWith(prefix)) {
                return Optional.of(Boolean.TRUE);
            }
        }
        if (connector.startsWith("DPI")) {
            return Optional.empty();
        }
        for (String prefix : EXTERNAL_CONNECTORS) {
            if (connector.startsWith(prefix)) {
                return Optional.of(Boolean.FALSE);
            }
        }
        return Optional.empty();
    }

}
