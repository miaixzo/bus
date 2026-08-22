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
package org.miaixz.bus.health.unix.shared.driver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;

/**
 * Queries xrandr for display information.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class Xrandr {

    /**
     * The command to execute for verbose xrandr output.
     */
    private static final String[] XRANDR_VERBOSE = { "xrandr", "--verbose" };

    /**
     * Property names an X server may publish the EDID under.
     */
    private static final String[] EDID_PROPERTIES = { "EDID:", "RANDR_EDID:", "EDID_DATA:" };

    /**
     * Creates an XRandR command reader with no retained process state.
     */
    public Xrandr() {
        // No initialization required.
    }

    /**
     * Tests whether a property line names the EDID under any supported property name.
     *
     * @param trimmed a whitespace-trimmed line of {@code xrandr --verbose} output
     * @return {@code true} if the line is an EDID property header
     */
    private static boolean isEdidProperty(String trimmed) {
        for (String property : EDID_PROPERTIES) {
            if (property.equals(trimmed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves the EDID (Extended Display Identification Data) for all connected displays.
     *
     * @return A list of byte arrays, where each array represents the EDID of a display.
     */
    public static List<byte[]> getEdidArrays() {
        // Special handling for X commands, don't use LC_ALL
        return getEdidArrays(runXrandr());
    }

    /**
     * Parse EDID arrays from xrandr verbose output.
     *
     * @param xrandr output of {@code xrandr --verbose}
     * @return a list of EDID byte arrays
     */
    static List<byte[]> getEdidArrays(List<String> xrandr) {
        Map<String, Pair<Integer, byte[]>> data = getDisplayData(xrandr);
        List<byte[]> edids = new ArrayList<>(data.size());
        for (Pair<Integer, byte[]> pair : data.values()) {
            edids.add(pair.getRight());
        }
        return Collections.unmodifiableList(edids);
    }

    /**
     * Gets display data from the running X server via xrandr.
     *
     * @return an ordered map of output name to connector identifier and EDID byte array
     */
    public static Map<String, Pair<Integer, byte[]>> getDisplayData() {
        return getDisplayData(runXrandr());
    }

    /**
     * Parses display data from {@code xrandr --verbose} output.
     *
     * @param xrandr output of {@code xrandr --verbose}
     * @return an ordered map of output name to connector identifier and EDID byte array
     */
    static Map<String, Pair<Integer, byte[]>> getDisplayData(List<String> xrandr) {
        if (xrandr.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Pair<Integer, byte[]>> results = new LinkedHashMap<>();
        String currentPort = Normal.EMPTY;
        boolean currentConnected = false;
        int currentConnectorId = Normal.__1;
        byte[] currentEdid = null;
        StringBuilder sb = null;
        for (String s : xrandr) {
            if (!s.isEmpty() && !Character.isWhitespace(s.charAt(Normal._0))) {
                if (currentConnected && currentEdid != null) {
                    results.put(currentPort, Pair.of(currentConnectorId, currentEdid));
                }
                String[] words = Pattern.SPACES_PATTERN.split(s.trim(), Normal.__1);
                currentPort = words[Normal._0];
                currentConnected = words.length > Normal._1 && "connected".equals(words[Normal._1]);
                currentConnectorId = Normal.__1;
                currentEdid = null;
                sb = null;
                continue;
            }
            String trimmed = s.trim();
            if (trimmed.startsWith("CONNECTOR_ID:")) {
                currentConnectorId = Parsing.parseLastInt(trimmed, Normal.__1);
            } else if (isEdidProperty(trimmed)) {
                sb = new StringBuilder();
            } else if (sb != null) {
                sb.append(trimmed);
                if (sb.length() < Normal._256) {
                    continue;
                }
                currentEdid = ByteKit.hexStringToByteArray(sb.toString());
                if (currentEdid.length < Normal._128) {
                    currentEdid = null;
                }
                sb = null;
            }
        }
        if (currentConnected && currentEdid != null) {
            results.put(currentPort, Pair.of(currentConnectorId, currentEdid));
        }
        return Collections.unmodifiableMap(results);
    }

    /**
     * Finds the xrandr output name for a display by connector identifier or EDID comparison.
     *
     * @param xrandrData  xrandr display data
     * @param connectorId the DRM connector identifier, or {@code -1} if unavailable
     * @param edid        the display EDID byte array
     * @return the matching output name, or an empty optional
     */
    public static Optional<String> findOutputName(
            Map<String, Pair<Integer, byte[]>> xrandrData,
            int connectorId,
            byte[] edid) {
        if (xrandrData.isEmpty()) {
            return Optional.empty();
        }
        if (connectorId >= Normal._0) {
            for (Map.Entry<String, Pair<Integer, byte[]>> entry : xrandrData.entrySet()) {
                if (entry.getValue().getLeft() == connectorId) {
                    return Optional.of(entry.getKey());
                }
            }
        }
        if (edid.length >= Normal._128) {
            byte[] edid128 = Arrays.copyOf(edid, Normal._128);
            for (Map.Entry<String, Pair<Integer, byte[]>> entry : xrandrData.entrySet()) {
                byte[] xrandrEdid = entry.getValue().getRight();
                if (xrandrEdid.length >= Normal._128
                        && Arrays.equals(edid128, Arrays.copyOf(xrandrEdid, Normal._128))) {
                    return Optional.of(entry.getKey());
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Runs xrandr only when an X display is available.
     *
     * @return xrandr output lines, or an empty list when xrandr should not be queried
     */
    private static List<String> runXrandr() {
        if (System.getenv("DISPLAY") == null) {
            return Collections.emptyList();
        }
        return Executor.runNative(XRANDR_VERBOSE, null);
    }

}
