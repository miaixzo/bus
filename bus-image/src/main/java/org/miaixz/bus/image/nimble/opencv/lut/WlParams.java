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

/**
 * Interface extending {@link WlPresentation} with comprehensive window/level parameters.
 * <p>
 * This interface defines all parameters required for window/level transformations in medical image processing,
 * including windowing values, LUT behavior, and display options.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface WlParams extends WlPresentation {

    /**
     * Gets the window width for the window/level transformation.
     *
     * @return the window width value
     */
    double getWindow();

    /**
     * Gets the window center (level) for the window/level transformation.
     *
     * @return the window center value
     */
    double getLevel();

    /**
     * Gets the minimum level value allowed for this transformation.
     *
     * @return the minimum level value
     */
    double getLevelMin();

    /**
     * Gets the maximum level value allowed for this transformation.
     *
     * @return the maximum level value
     */
    double getLevelMax();

    /**
     * Determines whether the lookup table should be inverted.
     *
     * @return {@code true} if LUT inversion is enabled, {@code false} otherwise
     */
    boolean isInverseLut();

    /**
     * Determines whether values outside the LUT range should be filled.
     *
     * @return {@code true} if outside range filling is enabled, {@code false} otherwise
     */
    boolean isFillOutsideLutRange();

    /**
     * Determines whether window/level adjustments are allowed on color images.
     *
     * @return {@code true} if color image adjustments are allowed, {@code false} otherwise
     */
    boolean isAllowWinLevelOnColorImage();

    /**
     * Gets the lookup table shape function used for the transformation.
     *
     * @return the LUT shape configuration
     */
    LutShape getLutShape();

}
