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
import java.util.Objects;

/**
 * A control point of a color map. Color and alpha are two independent curves sharing the position axis: a stop may
 * carry either or both. The alpha component of {@code color} is ignored.
 *
 * @param alpha    opacity in {@code [0, 1]}, or null when the stop does not shape the alpha curve
 * @param material shading coefficients for volume rendering, or null
 * @param group    optional editing label, ignored when compiling
 * @author Kimi Liu
 */
public record ColorStop(double position, Color color, Float alpha, Material material, String group) {

    public ColorStop {
        if (!Double.isFinite(position)) {
            throw new IllegalArgumentException("Position must be finite: " + position);
        }
        if (color == null && alpha == null) {
            throw new IllegalArgumentException("A stop needs a color or an alpha");
        }
        if (alpha != null && (alpha.isNaN() || alpha < 0f || alpha > 1f)) {
            throw new IllegalArgumentException("Alpha must be in [0, 1]: " + alpha);
        }
    }

    public static ColorStop of(double position, Color color) {
        return new ColorStop(position, Objects.requireNonNull(color), null, null, null);
    }

    public static ColorStop of(double position, Color color, float alpha) {
        return new ColorStop(position, Objects.requireNonNull(color), alpha, null, null);
    }

    public static ColorStop ofAlpha(double position, float alpha) {
        return new ColorStop(position, null, alpha, null, null);
    }

    public boolean hasColor() {
        return color != null;
    }

    public boolean hasAlpha() {
        return alpha != null;
    }

    public boolean hasMaterial() {
        return material != null;
    }

    public ColorStop withPosition(double newPosition) {
        return new ColorStop(newPosition, color, alpha, material, group);
    }

    public ColorStop withColor(Color newColor) {
        return new ColorStop(position, newColor, alpha, material, group);
    }

    public ColorStop withAlpha(Float newAlpha) {
        return new ColorStop(position, color, newAlpha, material, group);
    }

    public ColorStop withMaterial(Material newMaterial) {
        return new ColorStop(position, color, alpha, newMaterial, group);
    }

    public ColorStop withGroup(String newGroup) {
        return new ColorStop(position, color, alpha, material, newGroup);
    }
}
