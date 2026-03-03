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

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

import javax.swing.*;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public record ByteLut(String name, byte[][] lutTable) {

    public ByteLut {
        Objects.requireNonNull(name);
        if (lutTable != null && (lutTable.length != 3 || lutTable[0].length != 256)) {
            throw new IllegalArgumentException("LUT must have 3 channels and 256 values per channel");
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ByteLut byteLut = (ByteLut) o;
        return Objects.equals(name, byteLut.name) && Arrays.deepEquals(lutTable, byteLut.lutTable);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name);
        result = 31 * result + Arrays.deepHashCode(lutTable);
        return result;
    }

    public Icon getIcon(int height) {
        return getIcon(256, height);
    }

    public Icon getIcon(int width, int height) {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Width and height are not valid");
        }
        int border = 2;
        return new Icon() {

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                if (g instanceof Graphics2D g2d) {
                    g2d.setStroke(new BasicStroke(1.2f));
                }
                int lutHeight = height - 2 * border;
                int sx = x + border;
                int sy = y + border;
                for (int k = 0; k < width; k++) {
                    g.setColor(getColor(k, width));
                    g.drawLine(sx + k, sy, sx + k, sy + lutHeight);
                }
            }

            @Override
            public int getIconWidth() {
                return width + 2 * border;
            }

            @Override
            public int getIconHeight() {
                return height;
            }
        };
    }

    Color getColor(int position, int width) {
        byte[][] lut = lutTable == null ? ColorLut.GRAY.getByteLut().lutTable() : lutTable;
        int i = (position * 255) / width;
        return new Color(lut[2][i] & 0xFF, lut[1][i] & 0xFF, lut[0][i] & 0xFF);
    }

}
