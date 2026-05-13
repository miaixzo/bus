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
package org.miaixz.bus.image.nimble.opencv.lut;

import java.awt.Color;

/**
 * Color lookup tables (LUTs) for image processing and visualization. Each LUT defines color transformations with
 * 256-value mappings for red, green, and blue channels.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum ColorLut {

    /**
     * Default image LUT using grayscale mapping
     */
    IMAGE("Default (image)", null),
    /**
     * Cyclic flag pattern with 4 colors
     */
    FLAG("Flag", createFlagLut()),
    /**
     * 36-color palette for diverse visualization
     */
    MULTICOLOR("Multi-Color", createMultiColorLut()),
    /**
     * Full hue spectrum from HSB color space
     */
    HUE("Hue", createHueLut()),
    /**
     * Pure red gradient
     */
    RED("Red", createSingleChannelLut(Channel.RED)),
    /**
     * Pure green gradient
     */
    GREEN("Green", createSingleChannelLut(Channel.GREEN)),
    /**
     * Pure blue gradient
     */
    BLUE("Blue", createSingleChannelLut(Channel.BLUE)),
    /**
     * Grayscale gradient
     */
    GRAY("Gray", createGrayLut());

    /**
     * The LUT size value.
     */
    private static final int LUT_SIZE = 256;

    /**
     * The channel count value.
     */
    private static final int CHANNEL_COUNT = 3;

    /**
     * The byte LUT value.
     */
    private final ByteLut byteLut;

    /**
     * Creates a new instance.
     *
     * @param name     the name.
     * @param lutTable the LUT table.
     */
    ColorLut(String name, byte[][] lutTable) {
        this.byteLut = new ByteLut(name, lutTable);
    }

    /**
     * Returns the name.
     *
     * @return the name.
     */
    public String getName() {
        return byteLut.name();
    }

    /**
     * Returns the byte LUT.
     *
     * @return the byte LUT.
     */
    public ByteLut getByteLut() {
        return byteLut;
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return byteLut.name();
    }

    /**
     * Channel indices for BGR format.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private enum Channel {

        /**
         * Constant for the blue value.
         */
        BLUE(0),
        /**
         * Constant for the green value.
         */
        GREEN(1),
        /**
         * Constant for the red value.
         */
        RED(2);

        /**
         * The index value.
         */
        private final int index;

        /**
         * Creates a new instance.
         *
         * @param index the index.
         */
        Channel(int index) {
            this.index = index;
        }

        /**
         * Returns the index.
         *
         * @return the index.
         */
        int getIndex() {
            return index;
        }

    }

    // Static factory methods for LUT creation
    /**
     * Creates the flag LUT.
     *
     * @return the operation result.
     */
    private static byte[][] createFlagLut() {
        // Flag pattern: Blue, White, Magenta, Black (cycling every 4 values)
        var colors = new int[][] { { 0, 0, 255 }, // Blue
                { 255, 255, 255 }, // White
                { 255, 0, 255 }, // Magenta
                { 0, 0, 0 } // Black
        };
        return createPatternLut(colors);
    }

    /**
     * Creates the multi color LUT.
     *
     * @return the operation result.
     */
    private static byte[][] createMultiColorLut() {
        // 36-color palette optimized for diverse visualization
        var colors = new int[][] { { 255, 3, 0 }, { 0, 255, 0 }, { 255, 245, 55 }, { 0, 0, 255 }, { 255, 0, 255 },
                { 128, 0, 0 }, { 64, 128, 64 }, { 255, 128, 0 }, { 0, 0, 128 }, { 128, 0, 128 }, { 236, 83, 153 },
                { 189, 228, 170 }, { 250, 202, 87 }, { 154, 172, 216 }, { 221, 160, 246 }, { 255, 128, 128 },
                { 128, 128, 64 }, { 255, 200, 188 }, { 0, 187, 236 }, { 128, 88, 189 }, { 228, 93, 39 },
                { 131, 209, 96 }, { 189, 89, 212 }, { 0, 255, 255 }, { 36, 137, 176 }, { 66, 114, 215 },
                { 40, 202, 204 }, { 132, 106, 221 }, { 156, 235, 255 }, { 135, 85, 70 }, { 98, 216, 182 },
                { 194, 226, 84 }, { 217, 182, 172 }, { 251, 247, 176 }, { 255, 195, 142 }, { 0, 173, 95 } };
        return createPatternLut(colors);
    }

    /**
     * Creates the hue LUT.
     *
     * @return the operation result.
     */
    private static byte[][] createHueLut() {
        var lut = new byte[CHANNEL_COUNT][LUT_SIZE];

        for (int i = 0; i < LUT_SIZE; i++) {
            var color = Color.getHSBColor(i / 255f, 1f, 1f);
            lut[Channel.BLUE.getIndex()][i] = (byte) color.getBlue();
            lut[Channel.GREEN.getIndex()][i] = (byte) color.getGreen();
            lut[Channel.RED.getIndex()][i] = (byte) color.getRed();
        }
        return lut;
    }

    /**
     * Creates the single channel LUT.
     *
     * @param activeChannel the active channel.
     * @return the operation result.
     */
    private static byte[][] createSingleChannelLut(Channel activeChannel) {
        var lut = new byte[CHANNEL_COUNT][LUT_SIZE];

        for (int i = 0; i < LUT_SIZE; i++) {
            lut[activeChannel.getIndex()][i] = (byte) i;
        }
        return lut;
    }

    /**
     * Creates the gray LUT.
     *
     * @return the operation result.
     */
    private static byte[][] createGrayLut() {
        var lut = new byte[CHANNEL_COUNT][LUT_SIZE];

        for (int i = 0; i < LUT_SIZE; i++) {
            byte grayValue = (byte) i;
            lut[Channel.BLUE.getIndex()][i] = grayValue;
            lut[Channel.GREEN.getIndex()][i] = grayValue;
            lut[Channel.RED.getIndex()][i] = grayValue;
        }
        return lut;
    }

    /**
     * Creates the pattern LUT.
     *
     * @param colors the colors.
     * @return the operation result.
     */
    private static byte[][] createPatternLut(int[][] colors) {
        var lut = new byte[CHANNEL_COUNT][LUT_SIZE];

        for (int i = 0; i < LUT_SIZE; i++) {
            int[] color = colors[i % colors.length];
            lut[Channel.BLUE.getIndex()][i] = (byte) color[2];
            lut[Channel.GREEN.getIndex()][i] = (byte) color[1];
            lut[Channel.RED.getIndex()][i] = (byte) color[0];
        }
        return lut;
    }

}
