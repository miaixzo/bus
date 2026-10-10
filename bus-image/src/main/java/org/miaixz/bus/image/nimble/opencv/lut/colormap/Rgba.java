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
package org.miaixz.bus.image.nimble.opencv.lut.colormap;

import java.awt.*;

/**
 * A color with straight (non-premultiplied) alpha, each component in {@code [0, 1]}. *
 *
 * @author Kimi Liu
 */
public record Rgba(float red, float green, float blue, float alpha) {

    public static final Rgba TRANSPARENT = new Rgba(0f, 0f, 0f, 0f);

    public static final Rgba BLACK = new Rgba(0f, 0f, 0f, 1f);

    public static final Rgba WHITE = new Rgba(1f, 1f, 1f, 1f);

    public Rgba {
        red = clamp(red);
        green = clamp(green);
        blue = clamp(blue);
        alpha = clamp(alpha);
    }

    public static Rgba of(Color color) {
        return new Rgba(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1f);
    }

    static int to8(float component) {
        return Math.round(clamp(component) * 255f);
    }

    static float clamp(float value) {
        return Float.isNaN(value) ? 0f : Math.max(0f, Math.min(1f, value));
    }

    public Rgba withAlpha(float newAlpha) {
        return new Rgba(red, green, blue, newAlpha);
    }

    public Color toColor() {
        return new Color(red, green, blue, alpha);
    }

    public int red8() {
        return to8(red);
    }

    public int green8() {
        return to8(green);
    }

    public int blue8() {
        return to8(blue);
    }

    public int alpha8() {
        return to8(alpha);
    }
}
