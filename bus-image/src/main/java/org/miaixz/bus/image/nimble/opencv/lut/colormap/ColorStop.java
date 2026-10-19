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
 * @param position domain position of the stop
 * @param color    color value, or null when the stop does not shape the color curve
 * @param alpha    opacity in {@code [0, 1]}, or null when the stop does not shape the alpha curve
 * @param material shading coefficients for volume rendering, or null
 * @param group    optional editing label, ignored when compiling
 * @author Kimi Liu
 */
public record ColorStop(double position, Color color, Float alpha, Material material, String group) {

    /**
     * Validates the stop position and optional values.
     *
     * @throws IllegalArgumentException if the position is not finite, neither color nor alpha is present, or alpha is
     *                                  outside {@code [0, 1]}
     */
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

    /**
     * Creates an opaque color stop.
     *
     * @param position domain position
     * @param color    stop color
     * @return color stop
     */
    public static ColorStop of(double position, Color color) {
        return new ColorStop(position, Objects.requireNonNull(color), null, null, null);
    }

    /**
     * Creates a stop that defines both color and opacity.
     *
     * @param position domain position
     * @param color    stop color
     * @param alpha    opacity in {@code [0, 1]}
     * @return color and opacity stop
     */
    public static ColorStop of(double position, Color color, float alpha) {
        return new ColorStop(position, Objects.requireNonNull(color), alpha, null, null);
    }

    /**
     * Creates a stop that affects only the opacity curve.
     *
     * @param position domain position
     * @param alpha    opacity in {@code [0, 1]}
     * @return opacity-only stop
     */
    public static ColorStop ofAlpha(double position, float alpha) {
        return new ColorStop(position, null, alpha, null, null);
    }

    /**
     * Tests whether this stop defines a color.
     *
     * @return {@code true} when a color is present
     */
    public boolean hasColor() {
        return color != null;
    }

    /**
     * Tests whether this stop defines opacity.
     *
     * @return {@code true} when an opacity value is present
     */
    public boolean hasAlpha() {
        return alpha != null;
    }

    /**
     * Tests whether this stop defines a rendering material.
     *
     * @return {@code true} when a material is present
     */
    public boolean hasMaterial() {
        return material != null;
    }

    /**
     * Returns a copy at a different domain position.
     *
     * @param newPosition replacement position
     * @return updated stop
     */
    public ColorStop withPosition(double newPosition) {
        return new ColorStop(newPosition, color, alpha, material, group);
    }

    /**
     * Returns a copy with a different color.
     *
     * @param newColor replacement color
     * @return updated stop
     */
    public ColorStop withColor(Color newColor) {
        return new ColorStop(position, newColor, alpha, material, group);
    }

    /**
     * Returns a copy with a different opacity value.
     *
     * @param newAlpha replacement opacity, or null to remove it
     * @return updated stop
     */
    public ColorStop withAlpha(Float newAlpha) {
        return new ColorStop(position, color, newAlpha, material, group);
    }

    /**
     * Returns a copy with a different rendering material.
     *
     * @param newMaterial replacement material, or null to remove it
     * @return updated stop
     */
    public ColorStop withMaterial(Material newMaterial) {
        return new ColorStop(position, color, alpha, newMaterial, group);
    }

    /**
     * Returns a copy with a different editing group.
     *
     * @param newGroup replacement group label, or null
     * @return updated stop
     */
    public ColorStop withGroup(String newGroup) {
        return new ColorStop(position, color, alpha, material, newGroup);
    }

}
