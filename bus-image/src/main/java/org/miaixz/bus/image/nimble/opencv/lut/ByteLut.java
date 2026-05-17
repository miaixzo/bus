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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import javax.swing.Icon;

import org.miaixz.bus.image.nimble.opencv.op.ByteLutCollection;

/**
 * A record representing a Byte Lookup Table (LUT) with a name and a 2D byte array for color mapping. The LUT contains 3
 * channels (Red, Green, Blue) with 256 values per channel used for color transformation.
 *
 * @param name     the name.
 * @param lutTable the lut table.
 * @author Kimi Liu
 * @since Java 21+
 */
public record ByteLut(String name, byte[][] lutTable) {

    /**
     * The channel count value.
     */
    private static final int CHANNEL_COUNT = 3;

    /**
     * The channel size value.
     */
    private static final int CHANNEL_SIZE = 256;
    /**
     * The default icon width value.
     */
    private static final int DEFAULT_ICON_WIDTH = 256;

    /**
     * The default gray LUT value.
     */
    private static final byte[][] DEFAULT_GRAY_LUT = createDefaultGrayLut();

    /**
     * Creates the default gray LUT.
     *
     * @return the operation result.
     */
    private static byte[][] createDefaultGrayLut() {
        var lut = new byte[CHANNEL_COUNT][CHANNEL_SIZE];
        for (int i = 0; i < CHANNEL_SIZE; i++) {
            byte value = (byte) i;
            lut[0][i] = value; // Blue
            lut[1][i] = value; // Green
            lut[2][i] = value; // Red
        }
        return lut;
    }

    /**
     * Creates a new instance.
     *
     * @param name     the name.
     * @param lutTable the LUT table.
     */
    public ByteLut {
        Objects.requireNonNull(name, "Name cannot be null");
        validateLutTable(lutTable);
    }

    /**
     * Validates the LUT table.
     *
     * @param lutTable the LUT table.
     */
    private static void validateLutTable(byte[][] lutTable) {
        if (lutTable == null)
            return;

        if (lutTable.length != CHANNEL_COUNT) {
            throw new IllegalArgumentException("LUT must have exactly %d channels (RGB)".formatted(CHANNEL_COUNT));
        }

        for (int i = 0; i < CHANNEL_COUNT; i++) {
            if (lutTable[i] == null || lutTable[i].length != CHANNEL_SIZE) {
                throw new IllegalArgumentException(
                        "Each LUT channel must have exactly %d values".formatted(CHANNEL_SIZE));
            }
        }
    }

    /**
     * Returns the string representation.
     *
     * @return the string representation.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Executes the equals operation.
     *
     * @param o the o.
     * @return true if the equals condition is true; otherwise false.
     */
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ByteLut other && Objects.equals(name, other.name)
                && Arrays.deepEquals(lutTable, other.lutTable));
    }

    /**
     * Checks whether the hash code condition is true.
     *
     * @return true if the hash code condition is true; otherwise false.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, Arrays.deepHashCode(lutTable));
    }

    /**
     * Returns the icon.
     *
     * @param height the height.
     * @return the icon.
     */
    public Icon getIcon(int height) {
        return getIcon(DEFAULT_ICON_WIDTH, height);
    }

    /**
     * Returns the icon.
     *
     * @param width  the width.
     * @param height the height.
     * @return the icon.
     */
    public Icon getIcon(int width, int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Width and height must be positive values");
        }

        return new LutIcon(width, height);
    }

    /**
     * Executes the inverted operation.
     *
     * @return the operation result.
     */
    public ByteLut inverted() {
        return new ByteLut(name + " inverse", ByteLutCollection.invert(lutTable != null ? lutTable : DEFAULT_GRAY_LUT));
    }

    /**
     * Reads the LUT file.
     *
     * @param scanner the scanner.
     * @return the operation result.
     */
    public static byte[][] readLutFile(Scanner scanner) {
        return ByteLutCollection.readLutFile(scanner);
    }

    /**
     * Reads the LUT files.
     *
     * @param folder the folder.
     * @return the operation result.
     */
    public static List<ByteLut> readLutFiles(Path folder) {
        List<ByteLut> entries = new ArrayList<>();
        ByteLutCollection.readLutFilesFromResourcesDir(entries, folder);
        return List.copyOf(entries);
    }

    /**
     * Returns the color.
     *
     * @param position the position.
     * @param width    the width.
     * @return the color.
     */
    Color getColor(int position, int width) {
        var lut = lutTable != null ? lutTable : DEFAULT_GRAY_LUT;
        int index = calculateLutIndex(position, width);
        return createColorFromLut(lut, index);
    }

    /**
     * Calculates the LUT index.
     *
     * @param position the position.
     * @param width    the width.
     * @return the operation result.
     */
    private int calculateLutIndex(int position, int width) {
        return width <= 1 ? CHANNEL_SIZE - 1
                : Math.min(CHANNEL_SIZE - 1, (position * (CHANNEL_SIZE - 1)) / (width - 1));
    }

    /**
     * Creates the color from LUT.
     *
     * @param lut   the LUT.
     * @param index the index.
     * @return the operation result.
     */
    private Color createColorFromLut(byte[][] lut, int index) {
        int red = Byte.toUnsignedInt(lut[2][index]);
        int green = Byte.toUnsignedInt(lut[1][index]);
        int blue = Byte.toUnsignedInt(lut[0][index]);

        return new Color(red, green, blue);
    }

    /**
     * Icon implementation for LUT visualization.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private class LutIcon implements Icon {

        /**
         * The border value.
         */
        private static final int BORDER = 2;

        /**
         * The stroke width value.
         */
        private static final float STROKE_WIDTH = 1.2f;

        /**
         * The width value.
         */
        private final int width;

        /**
         * The height value.
         */
        private final int height;

        /**
         * Creates a new instance.
         *
         * @param width  the width.
         * @param height the height.
         */
        LutIcon(int width, int height) {
            this.width = width;
            this.height = height;
        }

        /**
         * Executes the icon operation.
         *
         * @param c the c.
         * @param g the g.
         * @param x the x.
         * @param y the y.
         */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            setupGraphics(g);
            drawLutBars(g, x, y);
        }

        /**
         * Sets the setup graphics.
         *
         * @param g the g.
         */
        private void setupGraphics(Graphics g) {
            if (g instanceof Graphics2D g2d) {
                g2d.setStroke(new BasicStroke(STROKE_WIDTH));
            }
        }

        /**
         * Executes the LUT bars operation.
         *
         * @param g the g.
         * @param x the x.
         * @param y the y.
         */
        private void drawLutBars(Graphics g, int x, int y) {
            int lutHeight = height - 2 * BORDER;
            int startX = x + BORDER;
            int startY = y + BORDER;
            for (int k = 0; k < width; k++) {
                g.setColor(getColor(k, width));
                g.drawLine(startX + k, startY, startX + k, startY + lutHeight);
            }
        }

        /**
         * Returns the icon width.
         *
         * @return the icon width.
         */
        @Override
        public int getIconWidth() {
            return width + 2 * BORDER;
        }

        /**
         * Returns the icon height.
         *
         * @return the icon height.
         */
        @Override
        public int getIconHeight() {
            return height;
        }

    }

}
