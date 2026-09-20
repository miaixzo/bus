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
 * Blinn-Phong coefficients attached to a stop, each in {@code [0, 1]}. *
 *
 * @author Kimi Liu
 */
public record Material(float ambient, float diffuse, float specular) {

    public static final Material DEFAULT = new Material(0.2f, 0.9f, 0.2f);

    public Material {
        validate(ambient, "ambient");
        validate(diffuse, "diffuse");
        validate(specular, "specular");
    }

    private static void validate(float value, String label) {
        if (Float.isNaN(value) || value < 0f || value > 1f) {
            throw new IllegalArgumentException(label + " must be in [0, 1]: " + value);
        }
    }

    public Material mix(Material other, double t) {
        float f = (float) t;
        return new Material(ambient + (other.ambient - ambient) * f, diffuse + (other.diffuse - diffuse) * f,
                specular + (other.specular - specular) * f);
    }
}
