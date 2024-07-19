/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.image.nimble.opencv.lut;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

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
