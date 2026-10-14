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

/**
 * Volume rendering shading settings of a map; absent on 2D-only maps.
 *
 * @param shade           whether lighting-based shading is enabled
 * @param specularPower   exponent controlling specular-highlight tightness
 * @param gradientOpacity opacity factor by gradient magnitude, or null for none
 * @author Kimi Liu
 */
public record Lighting(boolean shade, float specularPower, GradientOpacity gradientOpacity) {

    /**
     * Default enabled lighting with a broad specular highlight.
     */
    public static final Lighting DEFAULT = new Lighting(true, 10f);

    /**
     * Validates the specular exponent.
     *
     * @throws IllegalArgumentException if the exponent is not positive
     */
    public Lighting {
        if (Float.isNaN(specularPower) || specularPower <= 0f) {
            throw new IllegalArgumentException("Specular power must be positive: " + specularPower);
        }
    }

    /**
     * Creates lighting without gradient-opacity modulation.
     *
     * @param shade         whether shading is enabled
     * @param specularPower specular-highlight exponent
     */
    public Lighting(boolean shade, float specularPower) {
        this(shade, specularPower, null);
    }

    /**
     * Returns a copy with the supplied gradient-opacity curve.
     *
     * @param value replacement curve, or null to disable gradient opacity
     * @return updated lighting settings
     */
    public Lighting withGradientOpacity(GradientOpacity value) {
        return new Lighting(shade, specularPower, value);
    }
}
