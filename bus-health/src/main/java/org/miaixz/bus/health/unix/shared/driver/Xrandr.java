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
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.annotation.NotThreadSafe;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.DisplayMode;
import org.miaixz.bus.health.builtin.hardware.DisplayModeImpl;

/**
 * Queries {@code xrandr} for connected displays, EDID data, active modes, and primary-output state.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class Xrandr {

    /** The command used to request verbose display information. */
    private static final String[] XRANDR_VERBOSE = { "xrandr", "--verbose" };

    /** Property names under which an X server can publish EDID data. */
    private static final String[] EDID_PROPERTIES = { "EDID:", "RANDR_EDID:", "EDID_DATA:" };

    /** A display geometry token such as {@code 1920x1080+1920+0}. */
    private static final java.util.regex.Pattern GEOMETRY = java.util.regex.Pattern
            .compile("(\\d+)x(\\d+)\\+(-?\\d+)\\+(-?\\d+)");

    /**
     * Creates an xrandr command reader with no retained process state.
     */
    public Xrandr() {
        // No initialization required.
    }

    /**
     * Tests whether a property line starts an EDID block.
     *
     * @param trimmed a whitespace-trimmed line from {@code xrandr --verbose}
     * @return {@code true} if the line names an EDID property
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
     * Gets EDID byte arrays from the running X server.
     *
     * @return an immutable list of EDID byte arrays
     */
    public static List<byte[]> getEdidArrays() {
        return getEdidArrays(runXrandr());
    }

    /**
     * Parses EDID byte arrays from verbose xrandr output.
     *
     * @param xrandr verbose xrandr output
     * @return an immutable list of EDID byte arrays
     */
    static List<byte[]> getEdidArrays(List<String> xrandr) {
        List<Output> outputs = getOutputs(xrandr);
        List<byte[]> edids = new ArrayList<>(outputs.size());
        for (Output output : outputs) {
            edids.add(output.getEdid());
        }
        return Collections.unmodifiableList(edids);
    }

    /**
     * Gets connected outputs from the running X server.
     *
     * @return connected outputs that report at least 128 bytes of EDID data
     */
    public static List<Output> getOutputs() {
        return getOutputs(runXrandr());
    }

    /**
     * Parses connected outputs from verbose xrandr output. Each result includes the output name, connector identifier,
     * EDID, current mode, and primary status. A connected but disabled output has no current mode.
     *
     * @param xrandr verbose xrandr output
     * @return connected outputs that report a valid EDID, in xrandr order
     */
    static List<Output> getOutputs(List<String> xrandr) {
        if (xrandr.isEmpty()) {
            return Collections.emptyList();
        }
        List<Output> results = new ArrayList<>();
        OutputBuilder current = null;
        StringBuilder edidText = null;
        for (String line : xrandr) {
            if (!line.isEmpty() && !Character.isWhitespace(line.charAt(Normal._0))) {
                addIfValid(results, current);
                String[] words = Pattern.SPACES_PATTERN.split(line.trim(), Normal.__1);
                current = words.length > Normal._1 && "connected".equals(words[Normal._1]) ? new OutputBuilder(words)
                        : null;
                edidText = null;
                continue;
            }
            if (current == null) {
                continue;
            }
            String trimmed = line.trim();
            if (trimmed.startsWith("CONNECTOR_ID:")) {
                current.connectorId = Parsing.parseLastInt(trimmed, Normal.__1);
            } else if (isEdidProperty(trimmed)) {
                edidText = new StringBuilder();
            } else if (edidText != null) {
                edidText.append(trimmed);
                if (edidText.length() < Normal._256) {
                    continue;
                }
                byte[] edid = ByteKit.hexStringToByteArray(edidText.toString());
                current.edid = edid.length < Normal._128 ? null : edid;
                edidText = null;
            } else {
                current.parseModeLine(trimmed);
            }
        }
        addIfValid(results, current);
        return Collections.unmodifiableList(results);
    }

    /**
     * Adds a parsed output when it contains a valid EDID.
     *
     * @param results the destination list
     * @param builder the current output builder, or {@code null}
     */
    private static void addIfValid(List<Output> results, OutputBuilder builder) {
        if (builder != null && builder.edid != null) {
            results.add(builder.build(builder.edid));
        }
    }

    /**
     * Finds the output matching a DRM connector identifier or, as a fallback, the first 128 EDID bytes.
     *
     * @param outputs     the shared xrandr output data
     * @param connectorId the DRM connector identifier, or {@code -1} if unavailable
     * @param edid        the display EDID
     * @return the matching output, or an empty optional
     */
    public static Optional<Output> findOutput(List<Output> outputs, int connectorId, byte[] edid) {
        if (connectorId >= Normal._0) {
            for (Output output : outputs) {
                if (output.getConnectorId() == connectorId) {
                    return Optional.of(output);
                }
            }
        }
        if (edid.length >= Normal._128) {
            byte[] edid128 = Arrays.copyOf(edid, Normal._128);
            for (Output output : outputs) {
                byte[] outputEdid = output.edid;
                if (outputEdid.length >= Normal._128
                        && Arrays.equals(edid128, Arrays.copyOf(outputEdid, Normal._128))) {
                    return Optional.of(output);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Runs xrandr only when an X display is available.
     *
     * @return verbose xrandr output, or an empty list when no X display is configured
     */
    private static List<String> runXrandr() {
        if (System.getenv("DISPLAY") == null) {
            return Collections.emptyList();
        }
        return Executor.runNative(XRANDR_VERBOSE, null);
    }

    /**
     * Describes one connected output reported by xrandr.
     */
    @Immutable
    public static final class Output {

        /** The xrandr output name. */
        private final String name;

        /** The DRM connector identifier, or {@code -1}. */
        private final int connectorId;

        /** The output EDID. */
        private final byte[] edid;

        /** The current mode, or {@code null} when disabled. */
        private final DisplayMode mode;

        /** Whether xrandr marks this output as primary. */
        private final boolean primary;

        /**
         * Constructs an xrandr output snapshot.
         *
         * @param name        the output name
         * @param connectorId the DRM connector identifier, or {@code -1}
         * @param edid        the EDID byte array
         * @param mode        the current mode, or {@code null} when disabled
         * @param primary     whether the output is marked primary
         */
        public Output(String name, int connectorId, byte[] edid, DisplayMode mode, boolean primary) {
            this.name = name;
            this.connectorId = connectorId;
            this.edid = Arrays.copyOf(edid, edid.length);
            this.mode = mode;
            this.primary = primary;
        }

        /**
         * Gets the xrandr output name.
         *
         * @return the output name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Gets the DRM connector identifier.
         *
         * @return the connector identifier, or {@code -1}
         */
        public int getConnectorId() {
            return this.connectorId;
        }

        /**
         * Gets a defensive copy of the EDID.
         *
         * @return the EDID copy
         */
        public byte[] getEdid() {
            return Arrays.copyOf(this.edid, this.edid.length);
        }

        /**
         * Gets the current mode.
         *
         * @return the current mode, or an empty optional when disabled
         */
        public Optional<DisplayMode> getMode() {
            return Optional.ofNullable(this.mode);
        }

        /**
         * Reports whether this output is primary.
         *
         * @return {@code true} when xrandr marks the output primary
         */
        public boolean isPrimary() {
            return this.primary;
        }

    }

    /**
     * Accumulates one output while its xrandr block is parsed.
     */
    @NotThreadSafe
    private static final class OutputBuilder {

        /** The output name. */
        private final String name;

        /** The DRM connector identifier, or {@code -1}. */
        private int connectorId = Normal.__1;

        /** The parsed EDID, or {@code null}. */
        private byte[] edid;

        /** Whether the output is primary. */
        private boolean primary;

        /** The logical width from the header geometry. */
        private int width;

        /** The logical height from the header geometry. */
        private int height;

        /** The desktop x coordinate. */
        private int x;

        /** The desktop y coordinate. */
        private int y;

        /** The output rotation. */
        private int rotation;

        /** Whether the parser is inside the current mode block. */
        private boolean inCurrentMode;

        /** The unrotated current-mode width. */
        private int modeWidth;

        /** The unrotated current-mode height. */
        private int modeHeight;

        /** The current-mode refresh rate. */
        private double refreshRate;

        /**
         * Starts an output builder from its header tokens.
         *
         * @param header the output header tokens
         */
        private OutputBuilder(String[] header) {
            this.name = header[Normal._0];
            for (int i = Normal._2; i < header.length; i++) {
                String token = header[i];
                Matcher matcher = GEOMETRY.matcher(token);
                if ("primary".equals(token)) {
                    this.primary = true;
                } else if (matcher.matches()) {
                    this.width = Parsing.parseIntOrDefault(matcher.group(Normal._1), Normal._0);
                    this.height = Parsing.parseIntOrDefault(matcher.group(Normal._2), Normal._0);
                    this.x = Parsing.parseIntOrDefault(matcher.group(Normal._3), Normal._0);
                    this.y = Parsing.parseIntOrDefault(matcher.group(Normal._4), Normal._0);
                } else if (token.startsWith("(") && !token.startsWith("(0x")) {
                    break;
                } else if (this.width > Normal._0) {
                    this.rotation = parseRotation(token, this.rotation);
                }
            }
        }

        /**
         * Parses one line from the output's mode list.
         *
         * @param trimmed a whitespace-trimmed mode line
         */
        private void parseModeLine(String trimmed) {
            if (trimmed.startsWith("h:")) {
                if (this.inCurrentMode) {
                    this.modeWidth = parseTimingValue(trimmed, "width");
                }
            } else if (trimmed.startsWith("v:")) {
                if (this.inCurrentMode) {
                    this.modeHeight = parseTimingValue(trimmed, "height");
                    String last = trimmed.substring(trimmed.lastIndexOf(' ') + Normal._1);
                    if (last.endsWith("Hz") && !last.endsWith("KHz") && !last.endsWith("MHz")) {
                        this.refreshRate = Parsing
                                .parseDoubleOrDefault(last.substring(Normal._0, last.length() - Normal._2), Normal._0);
                    }
                }
            } else if (trimmed.contains("MHz")) {
                this.inCurrentMode = trimmed.contains("*current");
            }
        }

        /**
         * Builds the immutable output.
         *
         * @param edidBytes the validated EDID bytes
         * @return the parsed output
         */
        private Output build(byte[] edidBytes) {
            DisplayMode mode = null;
            if (this.width > Normal._0 && this.height > Normal._0) {
                int pixelWidth = this.width;
                int pixelHeight = this.height;
                if (this.modeWidth > Normal._0 && this.modeHeight > Normal._0) {
                    boolean quarterTurn = this.rotation == Normal._90 || this.rotation == Normal._90 * Normal._3;
                    pixelWidth = quarterTurn ? this.modeHeight : this.modeWidth;
                    pixelHeight = quarterTurn ? this.modeWidth : this.modeHeight;
                }
                mode = new DisplayModeImpl(this.x, this.y, this.width, this.height, pixelWidth, pixelHeight,
                        this.refreshRate, this.rotation);
            }
            return new Output(this.name, this.connectorId, edidBytes, mode, this.primary);
        }

        /**
         * Extracts an integer following a timing key.
         *
         * @param line the timing line
         * @param key  the key to find
         * @return the parsed value, or 0
         */
        private static int parseTimingValue(String line, String key) {
            String[] tokens = Pattern.SPACES_PATTERN.split(line, Normal.__1);
            for (int i = Normal._1; i < tokens.length - Normal._1; i++) {
                if (key.equals(tokens[i])) {
                    return Parsing.parseIntOrDefault(tokens[i + Normal._1], Normal._0);
                }
            }
            return Normal._0;
        }

        /**
         * Converts an xrandr rotation name to clockwise degrees.
         *
         * @param token           the xrandr rotation token
         * @param defaultRotation the value to retain for an unrecognized token
         * @return the clockwise rotation in degrees
         */
        private static int parseRotation(String token, int defaultRotation) {
            switch (token) {
                case "normal":
                    return Normal._0;

                case "right":
                    return Normal._90;

                case "inverted":
                    return Normal._90 * Normal._2;

                case "left":
                    return Normal._90 * Normal._3;

                default:
                    return defaultRotation;
            }
        }

    }

}
