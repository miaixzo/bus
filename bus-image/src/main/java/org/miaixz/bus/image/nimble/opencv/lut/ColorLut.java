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
import java.util.function.Supplier;

/**
 * @author Kimi Liu
 * @since Java 21+
 */
public enum ColorLut {

    IMAGE("Default (image)", () -> null), FLAG("Flag", () -> {
        byte[][] flag = new byte[3][256];

        int[] r = { 255, 255, 0, 0 };
        int[] g = { 0, 255, 0, 0 };
        int[] b = { 0, 255, 255, 0 };
        for (int i = 0; i < 256; i++) {
            flag[0][i] = (byte) b[i % 4];
            flag[1][i] = (byte) g[i % 4];
            flag[2][i] = (byte) r[i % 4];
        }
        return flag;
    }), MULTICOLOR("Multi-Color", () -> {
        byte[][] multiColor = new byte[3][256];
        int[] r = { 255, 0, 255, 0, 255, 128, 64, 255, 0, 128, 236, 189, 250, 154, 221, 255, 128, 255, 0, 128, 228, 131,
                189, 0, 36, 66, 40, 132, 156, 135, 98, 194, 217, 251, 255, 0 };
        int[] g = { 3, 255, 245, 0, 0, 0, 128, 128, 0, 0, 83, 228, 202, 172, 160, 128, 128, 200, 187, 88, 93, 209, 89,
                255, 137, 114, 202, 106, 235, 85, 216, 226, 182, 247, 195, 173 };
        int[] b = { 0, 0, 55, 255, 255, 0, 64, 0, 128, 128, 153, 170, 87, 216, 246, 128, 64, 188, 236, 189, 39, 96, 212,
                255, 176, 215, 204, 221, 255, 70, 182, 84, 172, 176, 142, 95 };
        for (int i = 0; i < 256; i++) {
            int p = i % 36;
            multiColor[0][i] = (byte) b[p];
            multiColor[1][i] = (byte) g[p];
            multiColor[2][i] = (byte) r[p];
        }
        return multiColor;
    }), HUE("Hue", () -> {
        byte[][] ihs = new byte[3][256];
        Color c;
        for (int i = 0; i < 256; i++) {
            c = Color.getHSBColor(i / 255f, 1f, 1f);
            ihs[0][i] = (byte) c.getBlue();
            ihs[1][i] = (byte) c.getGreen();
            ihs[2][i] = (byte) c.getRed();
        }
        return ihs;
    }), RED("Red", () -> {
        byte[][] red = new byte[3][256];
        for (int i = 0; i < 256; i++) {
            red[0][i] = 0;
            red[1][i] = 0;
            red[2][i] = (byte) i;
        }
        return red;
    }), GREEN("Green", () -> {
        byte[][] green = new byte[3][256];
        for (int i = 0; i < 256; i++) {
            green[0][i] = 0;
            green[1][i] = (byte) i;
            green[2][i] = 0;
        }

        return green;
    }), BLUE("Blue", () -> {
        byte[][] blue = new byte[3][256];
        for (int i = 0; i < 256; i++) {
            blue[0][i] = (byte) i;
            blue[1][i] = 0;
            blue[2][i] = 0;
        }
        return blue;
    }), GRAY("Gray", () -> {
        byte[][] grays = new byte[3][256];
        for (int i = 0; i < 256; i++) {
            grays[0][i] = (byte) i;
            grays[1][i] = (byte) i;
            grays[2][i] = (byte) i;
        }
        return grays;
    });

    private final ByteLut byteLut;

    ColorLut(String name, Supplier<byte[][]> slut) {
        this.byteLut = new ByteLut(name, slut.get());
    }

    public String getName() {
        return byteLut.name();
    }

    public ByteLut getByteLut() {
        return byteLut;
    }

    @Override
    public String toString() {
        return byteLut.name();
    }

}
