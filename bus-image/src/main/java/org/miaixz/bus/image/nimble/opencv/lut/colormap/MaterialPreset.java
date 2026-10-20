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
 * Named Blinn-Phong settings that let callers select a visual treatment instead of individual coefficients.
 *
 * @author Kimi Liu
 */
public enum MaterialPreset {

    /**
     * Non-reflective matte surface.
     */
    MATTE(new Material(0.25f, 0.95f, 0.0f), 10f),
    /**
     * Soft-tissue rendering surface.
     */
    SOFT_TISSUE(new Material(0.2f, 0.9f, 0.1f), 10f),
    /**
     * Skin-like surface with a moderate highlight.
     */
    SKIN(new Material(0.2f, 0.85f, 0.25f), 15f),
    /**
     * Vascular surface with a stronger highlight.
     */
    VESSEL(new Material(0.15f, 0.8f, 0.6f), 30f),
    /**
     * Bone-like reflective surface.
     */
    BONE(new Material(0.3f, 0.9f, 0.5f), 20f),
    /**
     * Highly reflective glass-like surface.
     */
    GLASS(new Material(0.1f, 0.3f, 0.9f), 60f),
    /**
     * Highly reflective metallic surface.
     */
    METAL(new Material(0.35f, 0.6f, 0.95f), 80f);

    /**
     * Blinn-Phong coefficients for the preset.
     */
    private final Material material;

    /**
     * Exponent controlling the tightness of specular highlights.
     */
    private final float specularPower;

    /**
     * Creates a named material preset.
     *
     * @param material      shading coefficients
     * @param specularPower specular-highlight exponent
     */
    MaterialPreset(Material material, float specularPower) {
        this.material = material;
        this.specularPower = specularPower;
    }

    /**
     * Returns the preset's shading coefficients.
     *
     * @return shading coefficients
     */
    public Material material() {
        return material;
    }

    /**
     * Highlight tightness that goes with the material; a map-wide setting in {@link Lighting}.
     *
     * @return specular-highlight exponent
     */
    public float specularPower() {
        return specularPower;
    }

}
