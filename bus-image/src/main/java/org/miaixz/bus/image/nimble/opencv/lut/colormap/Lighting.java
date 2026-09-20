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
 * @param gradientOpacity opacity factor by gradient magnitude, or null for none
 * @author Kimi Liu
 */
public record Lighting(boolean shade, float specularPower, GradientOpacity gradientOpacity) {

    public static final Lighting DEFAULT = new Lighting(true, 10f);

    public Lighting {
        if (Float.isNaN(specularPower) || specularPower <= 0f) {
            throw new IllegalArgumentException("Specular power must be positive: " + specularPower);
        }
    }

    public Lighting(boolean shade, float specularPower) {
        this(shade, specularPower, null);
    }

    public Lighting withGradientOpacity(GradientOpacity value) {
        return new Lighting(shade, specularPower, value);
    }
}
