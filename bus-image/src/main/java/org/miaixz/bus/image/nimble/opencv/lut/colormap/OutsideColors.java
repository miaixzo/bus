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
 * Colors for values outside the stops. A null entry clamps to the nearest stop; a null {@code nan} is transparent.
 *
 * @param low  color below the first stop, or null to clamp
 * @param high color above the last stop, or null to clamp
 * @param nan  color for NaN values, or null for transparent
 * @author Kimi Liu
 */
public record OutsideColors(Rgba low, Rgba high, Rgba nan) {

    /**
     * Policy that clamps finite values to the nearest stop and renders NaN transparently.
     */
    public static final OutsideColors CLAMP = new OutsideColors(null, null, null);

    /**
     * Policy that renders every out-of-range or NaN value transparently.
     */
    public static final OutsideColors TRANSPARENT = new OutsideColors(Rgba.TRANSPARENT, Rgba.TRANSPARENT,
            Rgba.TRANSPARENT);
}
