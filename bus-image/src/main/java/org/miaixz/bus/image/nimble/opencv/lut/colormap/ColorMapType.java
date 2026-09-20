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
 * Taxonomy of a color map (matplotlib, ColorBrewer); drives editor behaviour and menu grouping. *
 *
 * @author Kimi Liu
 */
public enum ColorMapType {
    /**
     * Ordered ramp from low to high.
     */
    SEQUENTIAL,
    /**
     * Two ramps meeting at a neutral center.
     */
    DIVERGING,
    /**
     * Ends meet: positions wrap around the domain instead of clamping.
     */
    CYCLIC,
    /**
     * Distinct colors for categories, one step per stop.
     */
    QUALITATIVE,
    /**
     * Color plus opacity, for overlays and volume rendering.
     */
    TRANSFER
}
